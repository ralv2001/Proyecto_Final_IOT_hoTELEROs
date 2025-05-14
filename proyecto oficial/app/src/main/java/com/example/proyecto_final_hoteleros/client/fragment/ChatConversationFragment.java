package com.example.proyecto_final_hoteleros.client.fragment;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.proyecto_final_hoteleros.adapters.MessageAdapter;
import com.example.proyecto_final_hoteleros.client.model.Message;
import com.example.proyecto_final_hoteleros.client.model.ChatSummary;

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
    private ImageView ivEmptyChatIcon;
    private TextView tvEmptyStateTitle;
    private TextView tvEmptyStateMessage;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = null;
        try {
            // Inflate the layout first
            rootView = inflater.inflate(R.layout.fragment_chat_conversation, container, false);

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
                loadMessages();
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
            ivEmptyChatIcon = rootView.findViewById(R.id.ivEmptyChatIcon);
            tvEmptyStateTitle = rootView.findViewById(R.id.tvEmptyStateTitle);
            tvEmptyStateMessage = rootView.findViewById(R.id.tvEmptyStateMessage);

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

                // Customize empty state for new chat
                if (tvEmptyStateTitle != null) {
                    tvEmptyStateTitle.setText("Inicia tu conversación");
                }
                if (tvEmptyStateMessage != null) {
                    tvEmptyStateMessage.setText("Comunícate con " + hotelName + " para resolver cualquier duda sobre tu reserva");
                }
            }
        }
    }

    private void loadMessages() {
        // In a real app, you would load messages from a database or API
        // Here we'll create some demo messages for illustration
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

            // Show typing indicator briefly if this is first load
            if (isFirstLoad) {
                isFirstLoad = false;
                showTypingIndicator(true);
                typingHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showTypingIndicator(false);
                    }
                }, 3000);
            }
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

                // Also animate the icon
                if (ivEmptyChatIcon != null) {
                    Animation pulse = AnimationUtils.loadAnimation(getContext(), R.anim.pulse);
                    ivEmptyChatIcon.startAnimation(pulse);
                }

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
        String messageText = etMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            return;
        }

        // Create new message
        Message newMessage = new Message(
                String.valueOf(System.currentTimeMillis()),
                "user_1",  // In a real app, this would be the actual user ID
                hotelId,
                messageText,
                System.currentTimeMillis(),
                Message.MessageType.USER
        );

        // Add message to list
        messageList.add(newMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);

        // Clear input field
        etMessage.setText("");

        // In a real app, you would send the message to a backend

        // If this is the first message in an empty chat, update UI
        if (messageList.size() == 1) {
            hideEmptyStateWithAnimation();
        }

        // Simulate response after a short delay for active chats
        if (chatStatus != ChatSummary.ChatStatus.FINISHED) {
            simulateResponse();
        }
    }

    private void simulateResponse() {
        // Show typing indicator
        showTypingIndicator(true);

        // Simulate hotel response after a delay (only for demo purposes)
        typingRunnable = new Runnable() {
            @Override
            public void run() {
                // Hide typing indicator
                showTypingIndicator(false);

                // Generate appropriate response based on the last message
                String responseText = generateContextualResponse();

                Message response = new Message(
                        String.valueOf(System.currentTimeMillis()),
                        hotelId,
                        "user_1",
                        responseText,
                        System.currentTimeMillis(),
                        Message.MessageType.HOTEL
                );

                messageList.add(response);
                messageAdapter.notifyItemInserted(messageList.size() - 1);
                rvMessages.scrollToPosition(messageList.size() - 1);
            }
        };

        // Randomize response time between 1-3 seconds for more natural feel
        int responseDelay = 1000 + (int)(Math.random() * 2000);
        typingHandler.postDelayed(typingRunnable, responseDelay);
    }

    private String generateContextualResponse() {
        // Get the last user message
        String lastUserMessage = "";
        for (int i = messageList.size() - 1; i >= 0; i--) {
            Message message = messageList.get(i);
            if (message.getType() == Message.MessageType.USER) {
                lastUserMessage = message.getText().toLowerCase();
                break;
            }
        }

        // Generate contextual response based on keywords
        if (lastUserMessage.contains("hola") || lastUserMessage.contains("buenos") ||
                messageList.size() <= 2) {
            return "¡Hola! Bienvenido al servicio de chat de " + hotelName +
                    ". ¿En qué podemos ayudarle con su estancia?";
        } else if (lastUserMessage.contains("habitaci") || lastUserMessage.contains("room") ||
                lastUserMessage.contains("servicio")) {
            return "Por supuesto, nuestro servicio de habitaciones está disponible 24/7. " +
                    "¿Qué necesita que le enviemos a su habitación?";
        } else if (lastUserMessage.contains("piscina") || lastUserMessage.contains("spa") ||
                lastUserMessage.contains("gimnasio") || lastUserMessage.contains("gym")) {
            return "Nuestras instalaciones de ocio están en la planta baja. " +
                    "La piscina está abierta de 7AM a 10PM, el spa de 9AM a 9PM, " +
                    "y el gimnasio está disponible 24 horas para nuestros huéspedes.";
        } else if (lastUserMessage.contains("restaurante") || lastUserMessage.contains("comer") ||
                lastUserMessage.contains("comida") || lastUserMessage.contains("desayuno")) {
            return "Nuestro restaurante principal sirve desayuno de 6AM a 10:30AM, " +
                    "almuerzo de 12:30PM a 3PM, y cena de 7PM a 10:30PM. " +
                    "¿Desea hacer una reserva?";
        } else if (lastUserMessage.contains("checkout") || lastUserMessage.contains("salida")) {
            return "El checkout estándar es a las 12 del mediodía. Si necesita un late checkout, " +
                    "podemos arreglarlo dependiendo de la disponibilidad. ¿Necesita ayuda adicional con su salida?";
        } else if (lastUserMessage.contains("gracias")) {
            return "Ha sido un placer ayudarle. Si necesita cualquier otra cosa, no dude en contactarnos.";
        } else {
            return "Gracias por su mensaje. Un representante del hotel le atenderá en breve. " +
                    "¿Hay algo más en lo que podamos ayudarle mientras tanto?";
        }
    }

    private void showHotelInfoDialog() {
        // En una aplicación real, esto mostraría un diálogo con información del hotel
        // Para esta demostración, solo mostraremos un Toast
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
        // Limpiamos los callbacks del handler
        if (typingHandler != null && typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }
    }
}