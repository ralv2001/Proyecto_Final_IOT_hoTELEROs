package com.example.proyecto_final_hoteleros.client.data.service;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.proyecto_final_hoteleros.client.data.model.ChatSummary;
import com.example.proyecto_final_hoteleros.client.data.model.FirebaseChatSummary;
import com.example.proyecto_final_hoteleros.client.data.model.FirebaseMessage;
import com.example.proyecto_final_hoteleros.client.data.model.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FirebaseChatService {
    private static final String TAG = "FirebaseChatService";

    // Singleton instance
    private static FirebaseChatService instance;

    // Firebase references
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mChatSummariesRef;
    private DatabaseReference mMessagesRef;

    // ✅ MEJORA: Usar ConcurrentHashMap para thread safety
    private Map<String, ChildEventListener> messageListeners = new ConcurrentHashMap<>();
    private ValueEventListener chatSummariesListener;

    // ✅ MEJORA: Cache para optimizar rendimiento
    private Map<String, List<Message>> messagesCache = new ConcurrentHashMap<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 30000; // 30 segundos

    // Interfaces para callbacks
    public interface OnChatSummariesLoadedListener {
        void onChatSummariesLoaded(List<ChatSummary> chatSummaries);
        void onChatSummariesError(String error);
    }

    public interface OnMessagesLoadedListener {
        void onMessagesLoaded(List<Message> messages);
        void onMessageAdded(Message message);
        void onMessagesError(String error);
    }

    public interface OnMessageSentListener {
        void onMessageSent(Message message);
        void onMessageError(String error);
    }

    private FirebaseChatService() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        // ✅ MEJORA: Habilitar persistencia offline
        try {
            mDatabase.setPersistenceEnabled(true);
        } catch (Exception e) {
            Log.d(TAG, "Firebase persistence already enabled or failed: " + e.getMessage());
        }

        mChatSummariesRef = mDatabase.getReference("chatSummaries");
        mMessagesRef = mDatabase.getReference("messages");

        // ✅ MEJORA: Configurar cache offline
        mChatSummariesRef.keepSynced(true);
    }

    public static FirebaseChatService getInstance() {
        if (instance == null) {
            synchronized (FirebaseChatService.class) {
                if (instance == null) {
                    instance = new FirebaseChatService();
                }
            }
        }
        return instance;
    }

    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : "user_1"; // Fallback para pruebas
    }

    public void createNewChat(ChatSummary chatSummary, final OnMessageSentListener listener) {
        String userId = getCurrentUserId();

        FirebaseChatSummary fbChatSummary = FirebaseChatSummary.fromChatSummary(chatSummary, userId);

        mChatSummariesRef.child(chatSummary.getId()).setValue(fbChatSummary)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Chat creado exitosamente: " + chatSummary.getId());

                        Message systemMessage = new Message(
                                "system_" + System.currentTimeMillis(),
                                "system",
                                userId,
                                "Bienvenido al chat. Un representante del hotel le atenderá en breve.",
                                System.currentTimeMillis(),
                                Message.MessageType.SYSTEM
                        );

                        sendMessage(chatSummary.getId(), systemMessage, listener);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al crear chat: " + e.getMessage());
                        if (listener != null) {
                            listener.onMessageError("Error al crear chat: " + e.getMessage());
                        }
                    }
                });
    }

    public void loadChatSummaries(final OnChatSummariesLoadedListener listener) {
        String userId = getCurrentUserId();

        // ✅ MEJORA: Cleanup anterior listener
        if (chatSummariesListener != null) {
            mChatSummariesRef.removeEventListener(chatSummariesListener);
        }

        Query query = mChatSummariesRef;

        chatSummariesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ChatSummary> chatSummaries = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        FirebaseChatSummary fbChatSummary = snapshot.getValue(FirebaseChatSummary.class);
                        if (fbChatSummary != null) {
                            ChatSummary chatSummary = fbChatSummary.toChatSummary();
                            chatSummaries.add(chatSummary);
                            Log.d(TAG, "Chat cargado: " + chatSummary.getId() + ", estado: " +
                                    (fbChatSummary.getStatus() != null ? fbChatSummary.getStatus() : "null"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al convertir chat: " + e.getMessage());
                    }
                }
                if (listener != null) {
                    listener.onChatSummariesLoaded(chatSummaries);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error al cargar chats: " + databaseError.getMessage());
                if (listener != null) {
                    listener.onChatSummariesError(databaseError.getMessage());
                }
            }
        };
        query.addValueEventListener(chatSummariesListener);
    }

    public void loadMessages(final String chatId, final OnMessagesLoadedListener listener) {
        // ✅ MEJORA: Cleanup listener anterior si existe
        if (messageListeners.containsKey(chatId)) {
            mMessagesRef.child(chatId).removeEventListener(messageListeners.get(chatId));
            messageListeners.remove(chatId);
        }

        // ✅ MEJORA: Verificar cache primero
        if (isCacheValid(chatId)) {
            List<Message> cachedMessages = messagesCache.get(chatId);
            if (cachedMessages != null && listener != null) {
                listener.onMessagesLoaded(new ArrayList<>(cachedMessages));
                Log.d(TAG, "Mensajes cargados desde cache para: " + chatId);
            }
        }

        Query query = mMessagesRef.child(chatId).orderByChild("timestamp");

        final List<Message> messagesList = new ArrayList<>();

        ChildEventListener messagesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                try {
                    FirebaseMessage fbMessage = dataSnapshot.getValue(FirebaseMessage.class);
                    if (fbMessage != null) {
                        Message message = fbMessage.toMessage();
                        messagesList.add(message);

                        if (listener != null) {
                            listener.onMessageAdded(message);
                        }

                        // ✅ MEJORA: Marcar mensajes del hotel como leídos automáticamente
                        if (fbMessage.getType().equals("HOTEL") && !fbMessage.isRead()) {
                            markMessageAsRead(chatId, fbMessage.getId());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing message: " + e.getMessage());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Manejar cambios en mensajes (ej: estado de leído)
                try {
                    FirebaseMessage fbMessage = dataSnapshot.getValue(FirebaseMessage.class);
                    if (fbMessage != null) {
                        Log.d(TAG, "Mensaje actualizado: " + fbMessage.getId());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing message update: " + e.getMessage());
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // No necesitamos manejar eliminaciones por ahora
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // No necesitamos manejar movimientos por ahora
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error al cargar mensajes: " + databaseError.getMessage());
                if (listener != null) {
                    listener.onMessagesError(databaseError.getMessage());
                }
            }
        };

        query.addChildEventListener(messagesListener);
        messageListeners.put(chatId, messagesListener);

        // ✅ MEJORA: Cargar mensajes iniciales y actualizar cache
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Actualizar cache
                messagesCache.put(chatId, new ArrayList<>(messagesList));
                lastCacheUpdate = System.currentTimeMillis();

                if (listener != null) {
                    listener.onMessagesLoaded(messagesList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error al cargar mensajes iniciales: " + databaseError.getMessage());
                if (listener != null) {
                    listener.onMessagesError(databaseError.getMessage());
                }
            }
        });
    }

    public void sendMessage(String chatId, Message message, final OnMessageSentListener listener) {
        // ✅ MEJORA: Validar datos antes de enviar
        if (chatId == null || chatId.isEmpty()) {
            if (listener != null) {
                listener.onMessageError("ID de chat inválido");
            }
            return;
        }

        if (message.getText() == null || message.getText().trim().isEmpty()) {
            if (listener != null) {
                listener.onMessageError("El mensaje no puede estar vacío");
            }
            return;
        }

        FirebaseMessage fbMessage = FirebaseMessage.fromMessage(message);

        mMessagesRef.child(chatId).child(message.getId()).setValue(fbMessage)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Mensaje enviado exitosamente: " + message.getId());

                        // ✅ MEJORA: Actualizar último mensaje en el chat summary
                        updateChatLastMessage(chatId, message.getText());

                        // ✅ MEJORA: Invalidar cache para forzar actualización
                        messagesCache.remove(chatId);

                        if (listener != null) {
                            listener.onMessageSent(message);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al enviar mensaje: " + e.getMessage());
                        if (listener != null) {
                            listener.onMessageError("Error al enviar mensaje: " + e.getMessage());
                        }
                    }
                });
    }

    // ✅ MEJORA: Método para verificar validez del cache
    private boolean isCacheValid(String chatId) {
        return messagesCache.containsKey(chatId) &&
                (System.currentTimeMillis() - lastCacheUpdate) < CACHE_DURATION;
    }

    private void markMessageAsRead(String chatId, String messageId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("read", true);

        mMessagesRef.child(chatId).child(messageId).updateChildren(updates)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al marcar mensaje como leído: " + e.getMessage());
                    }
                });
    }

    private void updateChatLastMessage(String chatId, String lastMessage) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", lastMessage);
        updates.put("lastUpdated", System.currentTimeMillis());

        mChatSummariesRef.child(chatId).updateChildren(updates)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al actualizar último mensaje: " + e.getMessage());
                    }
                });
    }

    public void updateChatStatus(String chatId, ChatSummary.ChatStatus status) {
        String statusStr;
        switch (status) {
            case AVAILABLE:
                statusStr = "AVAILABLE";
                break;
            case ACTIVE:
                statusStr = "ACTIVE";
                break;
            case FINISHED:
                statusStr = "FINISHED";
                break;
            default:
                statusStr = "ACTIVE";
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", statusStr);
        updates.put("lastUpdated", System.currentTimeMillis());

        mChatSummariesRef.child(chatId).updateChildren(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Estado del chat actualizado exitosamente");

                        if (status == ChatSummary.ChatStatus.FINISHED) {
                            sendSystemMessage(chatId, "Este chat ha sido cerrado. Su reserva ha finalizado.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al actualizar estado del chat: " + e.getMessage());
                    }
                });
    }

    private void sendSystemMessage(String chatId, String messageText) {
        Message systemMessage = new Message(
                "system_" + System.currentTimeMillis(),
                "system",
                "system",
                messageText,
                System.currentTimeMillis(),
                Message.MessageType.SYSTEM
        );

        sendMessage(chatId, systemMessage, null);
    }

    // ✅ MEJORA: Método mejorado de cleanup con protección contra memory leaks
    public void cleanup() {
        try {
            // Remover listener de chat summaries
            if (chatSummariesListener != null && mChatSummariesRef != null) {
                mChatSummariesRef.removeEventListener(chatSummariesListener);
                chatSummariesListener = null;
            }

            // Remover todos los listeners de mensajes
            for (Map.Entry<String, ChildEventListener> entry : messageListeners.entrySet()) {
                String chatId = entry.getKey();
                ChildEventListener listener = entry.getValue();
                if (listener != null && mMessagesRef != null) {
                    mMessagesRef.child(chatId).removeEventListener(listener);
                }
            }
            messageListeners.clear();

            // Limpiar cache
            messagesCache.clear();

            Log.d(TAG, "FirebaseChatService cleanup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error during cleanup: " + e.getMessage());
        }
    }

    // ✅ MEJORA: Método para obtener estadísticas del cache (útil para debugging)
    public void logCacheStats() {
        Log.d(TAG, "Cache Stats - Chats cached: " + messagesCache.size() +
                ", Active listeners: " + messageListeners.size());
    }
}