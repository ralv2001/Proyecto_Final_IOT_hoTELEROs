package com.example.proyecto_final_hoteleros.services;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.proyecto_final_hoteleros.client.model.ChatSummary;
import com.example.proyecto_final_hoteleros.client.model.FirebaseChatSummary;
import com.example.proyecto_final_hoteleros.client.model.FirebaseMessage;
import com.example.proyecto_final_hoteleros.client.model.Message;
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

public class FirebaseChatService {
    private static final String TAG = "FirebaseChatService";

    // Singleton instance
    private static FirebaseChatService instance;

    // Firebase references
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mChatSummariesRef;
    private DatabaseReference mMessagesRef;

    // Listeners
    private ValueEventListener chatSummariesListener;
    private Map<String, ChildEventListener> messageListeners = new HashMap<>();

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
        mChatSummariesRef = mDatabase.getReference("chatSummaries");
        mMessagesRef = mDatabase.getReference("messages");
    }

    // Método para obtener la instancia singleton
    public static FirebaseChatService getInstance() {
        if (instance == null) {
            instance = new FirebaseChatService();
        }
        return instance;
    }

    // Obtener ID del usuario actual
    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : "user_1"; // Fallback para pruebas
    }

    // Crear un nuevo chat
    public void createNewChat(ChatSummary chatSummary, final OnMessageSentListener listener) {
        String userId = getCurrentUserId();

        // Primero crear el resumen del chat
        FirebaseChatSummary fbChatSummary = FirebaseChatSummary.fromChatSummary(chatSummary, userId);

        mChatSummariesRef.child(chatSummary.getId()).setValue(fbChatSummary)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Chat creado exitosamente: " + chatSummary.getId());

                        // Crear un mensaje de sistema inicial
                        Message systemMessage = new Message(
                                "system_" + System.currentTimeMillis(),
                                "system",
                                userId,
                                "Bienvenido al chat. Un representante del hotel le atenderá en breve.",
                                System.currentTimeMillis(),
                                Message.MessageType.SYSTEM
                        );

                        // Enviar mensaje de sistema
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

    // Cargar lista de chats del usuario
    public void loadChatSummaries(final OnChatSummariesLoadedListener listener) {
        String userId = getCurrentUserId();
        // Remover listener anterior si existe
        if (chatSummariesListener != null) {
            mChatSummariesRef.removeEventListener(chatSummariesListener);
        }

        // Cargar todos los chats disponibles
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
                        // Continuar con el siguiente chat en caso de error
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

    // Cargar mensajes de un chat específico
    public void loadMessages(final String chatId, final OnMessagesLoadedListener listener) {
        // Remover listener anterior si existe
        if (messageListeners.containsKey(chatId)) {
            mMessagesRef.child(chatId).removeEventListener(messageListeners.get(chatId));
            messageListeners.remove(chatId);
        }

        // Consultar mensajes ordenados por timestamp
        Query query = mMessagesRef.child(chatId).orderByChild("timestamp");

        final List<Message> messagesList = new ArrayList<>();

        ChildEventListener messagesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                FirebaseMessage fbMessage = dataSnapshot.getValue(FirebaseMessage.class);
                if (fbMessage != null) {
                    Message message = fbMessage.toMessage();
                    messagesList.add(message);

                    // Notificar solo el mensaje añadido
                    if (listener != null) {
                        listener.onMessageAdded(message);
                    }

                    // Si el mensaje es del hotel y no está leído, marcarlo como leído
                    if (fbMessage.getType().equals("HOTEL") && !fbMessage.isRead()) {
                        markMessageAsRead(chatId, fbMessage.getId());
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // No necesitamos manejar cambios por ahora
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

        // También cargar todos los mensajes iniciales
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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

    // Enviar un mensaje
    public void sendMessage(final String chatId, Message message, final OnMessageSentListener listener) {
        String messageId = mMessagesRef.child(chatId).push().getKey();
        if (messageId == null) {
            if (listener != null) {
                listener.onMessageError("Error al generar ID de mensaje");
            }
            return;
        }

        // Asignar el ID generado
        message.setId(messageId);

        // Convertir a FirebaseMessage
        FirebaseMessage fbMessage = FirebaseMessage.fromMessage(message);

        // Guardar mensaje
        mMessagesRef.child(chatId).child(messageId).setValue(fbMessage)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Mensaje enviado exitosamente");

                        // Actualizar el último mensaje en el resumen del chat
                        updateChatLastMessage(chatId, message.getText());

                        // Si es el primer mensaje del usuario, actualizar estado a ACTIVE
                        if (message.getType() == Message.MessageType.USER) {
                            updateChatStatus(chatId, ChatSummary.ChatStatus.ACTIVE);
                        }

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

    // Actualizar el último mensaje en el resumen del chat
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

    // Actualizar el estado del chat
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

                        // Si el chat está finalizado, añadir mensaje de sistema
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

    // Enviar mensaje de sistema
    private void sendSystemMessage(String chatId, String text) {
        String userId = getCurrentUserId();

        Message systemMessage = new Message(
                "system_" + System.currentTimeMillis(),
                "system",
                "system",
                text,
                System.currentTimeMillis(),
                Message.MessageType.SYSTEM
        );

        sendMessage(chatId, systemMessage, null);
    }

    // Marcar mensaje como leído
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

    // Limpiar listeners al cerrar la app
    public void cleanup() {
        if (chatSummariesListener != null) {
            mChatSummariesRef.removeEventListener(chatSummariesListener);
        }

        for (String chatId : messageListeners.keySet()) {
            mMessagesRef.child(chatId).removeEventListener(messageListeners.get(chatId));
        }

        messageListeners.clear();
    }
}