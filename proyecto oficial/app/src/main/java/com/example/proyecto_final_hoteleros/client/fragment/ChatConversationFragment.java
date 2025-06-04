package com.example.proyecto_final_hoteleros.client.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.adapters.MessageAdapter;
import com.example.proyecto_final_hoteleros.client.model.ChatSummary;
import com.example.proyecto_final_hoteleros.client.model.Message;
import com.example.proyecto_final_hoteleros.client.service.FirebaseChatService;

import java.util.ArrayList;
import java.util.List;

public class ChatConversationFragment extends Fragment {

    private static final String TAG = "ChatConversationFrag";

    // UI Components
    private Toolbar toolbar;
    private TextView tvHotelName;
    private TextView tvHotelStatus;
    private ImageView ivBack;
    private ImageView ivHotelImage;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private View emptyStateView;
    private LinearLayout typingIndicatorContainer;
    private TextView tvTypingIndicator;
    private ImageView ivHotelInfo;
    private LinearLayout chatFinishedBanner;

    // Data
    private String chatId;
    private String hotelId;
    private String hotelName;
    private List<Message> messageList;
    private MessageAdapter messageAdapter;
    private ChatSummary.ChatStatus chatStatus;
    private Handler typingHandler = new Handler();
    private Runnable typingRunnable;
    private boolean isFirstLoad = true;

    // Firebase service
    private FirebaseChatService chatService;

    // Typing variables
    private boolean isTyping = false;
    private static final long TYPING_TIMER_LENGTH = 600;
    private Handler typingTimeoutHandler = new Handler();
    private Runnable typingTimeoutCallback = new Runnable() {
        @Override
        public void run() {
            isTyping = false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = null;
        try {
            // Inflate the layout first
            rootView = inflater.inflate(R.layout.fragment_chat_conversation, container, false);

            // NUEVO: Configurar el comportamiento del teclado solo para esta vista
            if (getActivity() != null) {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }

            // Obtener servicio de Firebase
            chatService = FirebaseChatService.getInstance();

            // Extract arguments safely
            if (getArguments() != null) {
                chatId = getArguments().getString("chat_id", "");
                hotelId = getArguments().getString("hotel_id", "");
                hotelName = getArguments().getString("hotel_name", "Hotel");
                String statusStr = getArguments().getString("chat_status", "ACTIVE");
                chatStatus = ChatSummary.ChatStatus.valueOf(statusStr);

                // Log received arguments
                Log.d(TAG, "Received arguments - chatId: " + chatId
                        + ", hotelId: " + hotelId + ", hotelName: " + hotelName
                        + ", status: " + chatStatus);
            } else {
                Log.e(TAG, "No arguments received");
                // Set default values
                chatId = "";
                hotelId = "";
                hotelName = "Hotel";
                chatStatus = ChatSummary.ChatStatus.ACTIVE;
            }

            // Initialize views
            initViews(rootView);

            // Setup toolbar with valid hotel name
            setupToolbar(hotelName != null ? hotelName : "Hotel");

            // Setup message list
            setupMessageList();

            // Configure UI based on chat status
            configureUIForChatStatus();

            // Load messages based on valid chatId
            if (chatId != null && !chatId.isEmpty()) {
                loadMessagesFromFirebase();
            } else {
                Log.e(TAG, "Invalid chatId, showing empty state");
                showEmptyState(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage());
            e.printStackTrace();

            // Still need to return a view even if there's an error
            if (rootView == null && inflater != null && container != null) {
                rootView = inflater.inflate(R.layout.fragment_chat_conversation, container, false);
            }
        }

        return rootView;
    }

    // NUEVO: Restablecer el comportamiento del teclado cuando se destruye la vista
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Restablecer el comportamiento del teclado
        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }
    }

    private void initViews(View rootView) {
        try {
            toolbar = rootView.findViewById(R.id.toolbar);
            tvHotelName = rootView.findViewById(R.id.tvHotelName);
            tvHotelStatus = rootView.findViewById(R.id.tvHotelStatus);
            ivBack = rootView.findViewById(R.id.ivBack);
            ivHotelImage = rootView.findViewById(R.id.ivHotelImage);
            rvMessages = rootView.findViewById(R.id.rvMessages);
            etMessage = rootView.findViewById(R.id.etMessage);
            btnSend = rootView.findViewById(R.id.btnSend);
            emptyStateView = rootView.findViewById(R.id.emptyStateView);
            typingIndicatorContainer = rootView.findViewById(R.id.typingIndicatorContainer);
            tvTypingIndicator = rootView.findViewById(R.id.tvTypingIndicator);
            ivHotelInfo = rootView.findViewById(R.id.ivHotelInfo);
            chatFinishedBanner = rootView.findViewById(R.id.chatFinishedBanner);

            // Check for null views
            if (ivBack == null) {
                Log.e(TAG, "ivBack is null");
            } else {
                // Set up back button with error handling
                ivBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (getActivity() != null && getActivity().getSupportFragmentManager() != null) {
                                getActivity().getSupportFragmentManager().popBackStack();
                            } else {
                                Log.e(TAG, "Cannot pop back stack - activity or fragmentManager is null");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error in back button: " + e.getMessage());
                        }
                    }
                });
            }

            // Set up send button with error handling
            if (btnSend != null) {
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            sendMessage();
                        } catch (Exception e) {
                            Log.e(TAG, "Error sending message: " + e.getMessage());
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Error al enviar mensaje", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            } else {
                Log.e(TAG, "btnSend is null");
            }

            // Monitor typing
            etMessage.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!isTyping) {
                        isTyping = true;
                        // Aquí podrías enviar una notificación de "está escribiendo" a Firebase
                        // para que el hotel lo vea, si implementas esa funcionalidad
                    }

                    typingTimeoutHandler.removeCallbacks(typingTimeoutCallback);
                    typingTimeoutHandler.postDelayed(typingTimeoutCallback, TYPING_TIMER_LENGTH);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // Set up hotel info button
            if (ivHotelInfo != null) {
                ivHotelInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showHotelInfoDialog();
                    }
                });
            }

            // Set up start chat button
            View btnStartChat = emptyStateView.findViewById(R.id.btnStartChat);
            if (btnStartChat != null) {
                btnStartChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Focus on the message input
                        etMessage.requestFocus();

                        // Hide empty state with animation
                        hideEmptyStateWithAnimation();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in initViews: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupToolbar(String hotelName) {
        tvHotelName.setText(hotelName);

        // Set hotel status based on chat status
        if (chatStatus == ChatSummary.ChatStatus.ACTIVE) {
            tvHotelStatus.setText("En línea");
            tvHotelStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.light_green));
        } else if (chatStatus == ChatSummary.ChatStatus.FINISHED) {
            tvHotelStatus.setText("Chat finalizado");
            tvHotelStatus.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        } else {
            tvHotelStatus.setText("Disponible");
            tvHotelStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.light_green));
        }
    }

    private void setupMessageList() {
        // Initialize message list
        messageList = new ArrayList<>();

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true); // Messages appear from bottom
        rvMessages.setLayoutManager(layoutManager);

        // Initialize adapter
        messageAdapter = new MessageAdapter(getContext(), messageList);
        rvMessages.setAdapter(messageAdapter);
    }

    private void configureUIForChatStatus() {
        // Configure UI elements based on chat status
        if (chatStatus == ChatSummary.ChatStatus.FINISHED) {
            // Show finished banner
            chatFinishedBanner.setVisibility(View.VISIBLE);

            // Disable input for finished chats
            etMessage.setEnabled(false);
            btnSend.setEnabled(false);
            etMessage.setHint("Chat finalizado");

            // Add a visual indication that chat is finished
            etMessage.setBackgroundResource(R.drawable.bg_message_input_disabled);
            btnSend.setAlpha(0.5f);
        } else {
            // Hide finished banner
            chatFinishedBanner.setVisibility(View.GONE);

            if (chatStatus == ChatSummary.ChatStatus.AVAILABLE) {
                // For a new chat, show welcome message in input hint
                etMessage.setHint("Escribe tu primer mensaje...");
            }
        }
    }

    private void loadMessagesFromFirebase() {
        showLoadingIndicator(true);

        chatService.loadMessages(chatId, new FirebaseChatService.OnMessagesLoadedListener() {
            @Override
            public void onMessagesLoaded(List<Message> messages) {
                if (isAdded() && getContext() != null) {
                    // Actualizar la lista de mensajes
                    messageList.clear();
                    messageList.addAll(messages);

                    // Actualizar UI
                    updateUI();

                    // Ocultar indicador de carga
                    showLoadingIndicator(false);

                    Log.d(TAG, "Mensajes cargados desde Firebase: " + messages.size());
                }
            }

            @Override
            public void onMessageAdded(Message message) {
                if (isAdded() && getContext() != null) {
                    // Añadir solo el nuevo mensaje
                    if (!messageExists(message.getId())) {
                        messageList.add(message);
                        messageAdapter.notifyItemInserted(messageList.size() - 1);
                        rvMessages.scrollToPosition(messageList.size() - 1);
                    }

                    // Si es un mensaje del hotel, mostrar brevemente el indicador "escribiendo..."
                    if (message.getType() == Message.MessageType.HOTEL) {
                        showTypingIndicator(false);
                    }
                }
            }

            @Override
            public void onMessagesError(String error) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Error al cargar mensajes: " + error, Toast.LENGTH_SHORT).show();

                    // Ocultar indicador de carga
                    showLoadingIndicator(false);

                    // Cargar mensajes de ejemplo en caso de error
                    loadDemoMessages();

                    Log.e(TAG, "Error al cargar mensajes: " + error);
                }
            }
        });
    }

    private boolean messageExists(String messageId) {
        for (Message message : messageList) {
            if (message.getId().equals(messageId)) {
                return true;
            }
        }
        return false;
    }

    private void showLoadingIndicator(boolean show) {
        // Aquí podrías mostrar un ProgressBar durante la carga de mensajes
        // Por ahora, simplemente mostramos u ocultamos el indicador de escribiendo
        if (show && messageList.isEmpty()) {
            showTypingIndicator(true);
        } else if (!show) {
            showTypingIndicator(false);
        }
    }

    private void loadDemoMessages() {
        // Similar al método anterior, pero con datos locales de ejemplo
        messageList.clear();

        if (chatId.equals("chat_1")) {
            // New chat - no messages yet
            showEmptyState(true);
            return;
        } else if (chatId.equals("chat_2")) {
            // For active chat, show some demo messages
            showEmptyState(false);

            // Add sample messages with proper timestamps
            long now = System.currentTimeMillis();
            long oneHourAgo = now - 3600000;
            long fiftyFiveMinutesAgo = now - 3300000;
            long fifteenMinutesAgo = now - 900000;

            messageList.add(new Message("1", "user_1", hotelId,
                    "Hola, ¿podrían indicarme cómo llegar a la piscina?",
                    oneHourAgo, Message.MessageType.USER));

            messageList.add(new Message("2", hotelId, "user_1",
                    "¡Buenos días! La piscina se encuentra en la planta baja, siguiendo las indicaciones hacia el spa.",
                    fiftyFiveMinutesAgo, Message.MessageType.HOTEL));

            messageList.add(new Message("3", "user_1", hotelId,
                    "¿Podría solicitar servicio de habitaciones?",
                    fifteenMinutesAgo, Message.MessageType.USER));

        } else if (chatId.equals("chat_3")) {
            // For finished chat, show some demo messages
            showEmptyState(false);

            // Add complete conversation with timestamps
            long threeDaysAgo = System.currentTimeMillis() - 259200000;
            long threeDaysAgoPlus10Minutes = threeDaysAgo + 600000;
            long twoDaysAgo = System.currentTimeMillis() - 172800000;
            long twoDaysAgoPlus10Minutes = twoDaysAgo + 600000;

            messageList.add(new Message("1", "user_1", hotelId,
                    "Necesito hacer checkout mañana a las 6 AM, ¿es posible?",
                    threeDaysAgo, Message.MessageType.USER));

            messageList.add(new Message("2", hotelId, "user_1",
                    "Por supuesto, hemos registrado su solicitud de checkout temprano. El personal estará disponible para atenderle.",
                    threeDaysAgoPlus10Minutes, Message.MessageType.HOTEL));

            messageList.add(new Message("3", "user_1", hotelId,
                    "Muchas gracias por todo, ha sido una estancia muy agradable.",
                    twoDaysAgo, Message.MessageType.USER));

            messageList.add(new Message("4", hotelId, "user_1",
                    "Gracias por su estancia, esperamos verle pronto.",
                    twoDaysAgoPlus10Minutes, Message.MessageType.HOTEL));

            // Add system message indicating the chat is closed
            messageList.add(new Message("5", "system", "system",
                    "Este chat ha sido cerrado. Su reserva ha finalizado.",
                    twoDaysAgoPlus10Minutes + 60000, Message.MessageType.SYSTEM));
        }

        // Update UI based on message list
        updateUI();
    }

    private void showEmptyState(boolean show) {
        if (show) {
            rvMessages.setVisibility(View.GONE);

            // Show empty state with animation if first load
            if (isFirstLoad) {
                emptyStateView.setAlpha(0f);
                emptyStateView.setVisibility(View.VISIBLE);
                emptyStateView.animate().alpha(1f).setDuration(300).start();

                isFirstLoad = false;
            } else {
                emptyStateView.setVisibility(View.VISIBLE);
            }
        } else {
            rvMessages.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private void hideEmptyStateWithAnimation() {
        if (emptyStateView.getVisibility() == View.VISIBLE) {
            emptyStateView.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            emptyStateView.setVisibility(View.GONE);
                            rvMessages.setVisibility(View.VISIBLE);
                            // Request focus on the message input
                            etMessage.requestFocus();
                        }
                    })
                    .start();
        }
    }

    private void showTypingIndicator(boolean show) {
        if (typingIndicatorContainer != null) {
            if (show) {
                typingIndicatorContainer.setVisibility(View.VISIBLE);
                // Add bobbing animation for more realism
                Animation bobbing = AnimationUtils.loadAnimation(getContext(), R.anim.typing_bobbing);
                typingIndicatorContainer.startAnimation(bobbing);
            } else {
                typingIndicatorContainer.clearAnimation();
                typingIndicatorContainer.setVisibility(View.GONE);
            }
        }
    }

    private void updateUI() {
        if (messageList.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
            messageAdapter.notifyDataSetChanged();
            rvMessages.scrollToPosition(messageList.size() - 1);
        }

        // Update UI for finished chats
        if (chatStatus == ChatSummary.ChatStatus.FINISHED) {
            etMessage.setEnabled(false);
            btnSend.setEnabled(false);
            etMessage.setHint("Chat finalizado");
            etMessage.setBackgroundResource(R.drawable.bg_message_input_disabled);
            btnSend.setAlpha(0.5f);
            chatFinishedBanner.setVisibility(View.VISIBLE);
        }
    }

    private void sendMessage() {
        final String messageText = etMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            return;
        }

        // Obtener el ID actual del usuario desde Firebase Auth
        String userId = chatService.getCurrentUserId();

        // Crear nuevo mensaje
        final Message newMessage = new Message(
                String.valueOf(System.currentTimeMillis()),
                userId,
                hotelId,
                messageText,
                System.currentTimeMillis(),
                Message.MessageType.USER
        );

        // Limpiar campo de entrada
        etMessage.setText("");

        // Si el chat es nuevo (sin mensajes previos), activarlo
        if (chatStatus == ChatSummary.ChatStatus.AVAILABLE) {
            chatStatus = ChatSummary.ChatStatus.ACTIVE;
            chatService.updateChatStatus(chatId, ChatSummary.ChatStatus.ACTIVE);
        }

        // Enviar mensaje a Firebase
        chatService.sendMessage(chatId, newMessage, new FirebaseChatService.OnMessageSentListener() {
            @Override
            public void onMessageSent(Message message) {
                // Mensaje enviado exitosamente
                Log.d(TAG, "Mensaje enviado exitosamente: " + message.getId());

                // Mostrar brevemente el indicador de escritura para simular respuesta
                if (chatStatus == ChatSummary.ChatStatus.ACTIVE) {
                    // Simular que el hotel está escribiendo en 1-3 segundos
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isAdded() && getContext() != null) {
                                showTypingIndicator(true);
                            }
                        }
                    }, 1000 + (int)(Math.random() * 2000));
                }
            }

            @Override
            public void onMessageError(String error) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Error al enviar mensaje: " + error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al enviar mensaje: " + error);

                    // Devolver el mensaje al campo de entrada para que el usuario pueda intentar de nuevo
                    etMessage.setText(messageText);
                }
            }
        });

        // Añadir mensaje a la lista localmente para actualización inmediata de la UI
        // Esto se actualizará cuando Firebase devuelva el mensaje con su ID real
        messageList.add(newMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);

        // MODIFICADO: Asegurarse de que el scroll se mueve al último mensaje después de enviar
        rvMessages.post(new Runnable() {
            @Override
            public void run() {
                rvMessages.scrollToPosition(messageList.size() - 1);
            }
        });

        // Si es el primer mensaje, ocultar el estado vacío
        if (messageList.size() == 1) {
            hideEmptyStateWithAnimation();
        }
    }

    private void showHotelInfoDialog() {
        // En una aplicación real, mostrarías un diálogo con información del hotel
        if (getContext() != null) {
            String info = "Hotel: " + hotelName + "\n" +
                    "Servicios: Restaurante, Piscina, Spa, Gimnasio 24h\n" +
                    "Horario de recepción: 24 horas";

            Toast.makeText(getContext(), info, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Limpiar handlers
        if (typingHandler != null && typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }

        if (typingTimeoutHandler != null) {
            typingTimeoutHandler.removeCallbacks(typingTimeoutCallback);
        }
    }
}