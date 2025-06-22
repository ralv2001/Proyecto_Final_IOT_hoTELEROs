package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.example.proyecto_final_hoteleros.adminhotel.adapters.AdminMessageAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.ChatSummary;
import com.example.proyecto_final_hoteleros.client.data.model.Message;
import com.example.proyecto_final_hoteleros.client.data.service.FirebaseChatService;

import java.util.ArrayList;
import java.util.List;

public class AdminChatConversationFragment extends Fragment {

    private static final String TAG = "AdminChatConversation";

    // UI Components
    private Toolbar toolbar;
    private TextView tvClientName;
    private TextView tvHotelStatus;
    private ImageView ivBack;
    private ImageView ivClientImage;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private View emptyStateView;
    private LinearLayout chatFinishedBanner;

    // Data
    private String chatId;
    private String hotelId;
    private String clientReservation;
    private String hotelName;
    private List<Message> messageList;
    private AdminMessageAdapter messageAdapter;
    private ChatSummary.ChatStatus chatStatus;
    private boolean isFirstLoad = true;

    // Firebase service
    private FirebaseChatService chatService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = null;
        try {
            rootView = inflater.inflate(R.layout.admin_hotel_fragment_chat_admin_conversacion, container, false);

            if (getActivity() != null) {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }

            chatService = FirebaseChatService.getInstance();

            if (getArguments() != null) {
                chatId = getArguments().getString("chat_id", "");
                hotelId = getArguments().getString("hotel_id", "hotel_1");
                hotelName = getArguments().getString("hotel_name", "Hotel");
                clientReservation = getArguments().getString("client_reservation", "Cliente");
                String statusStr = getArguments().getString("chat_status", "ACTIVE");
                chatStatus = ChatSummary.ChatStatus.valueOf(statusStr);

                Log.d(TAG, "Admin received arguments - chatId: " + chatId
                        + ", hotelId: " + hotelId + ", clientReservation: " + clientReservation
                        + ", status: " + chatStatus);
            } else {
                Log.e(TAG, "No arguments received");
                chatId = "";
                hotelId = "hotel_1";
                hotelName = "Hotel";
                clientReservation = "Cliente";
                chatStatus = ChatSummary.ChatStatus.ACTIVE;
            }

            initViews(rootView);
            setupToolbar(clientReservation != null ? clientReservation : "Cliente");
            setupMessageList();
            configureUIForChatStatus();

            if (chatId != null && !chatId.isEmpty()) {
                loadMessagesFromFirebase();
            } else {
                Log.e(TAG, "Invalid chatId, showing empty state");
                showEmptyState(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage());
            e.printStackTrace();

            if (rootView == null && inflater != null && container != null) {
                rootView = inflater.inflate(R.layout.admin_hotel_fragment_chat_admin_conversacion, container, false);
            }
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }
    }

    private void initViews(View rootView) {
        try {
            toolbar = rootView.findViewById(R.id.toolbar);
            tvClientName = rootView.findViewById(R.id.tvClientName);
            tvHotelStatus = rootView.findViewById(R.id.tvHotelStatus);
            ivBack = rootView.findViewById(R.id.ivBack);
            ivClientImage = rootView.findViewById(R.id.ivClientImage);
            rvMessages = rootView.findViewById(R.id.rvMessages);
            etMessage = rootView.findViewById(R.id.etMessage);
            btnSend = rootView.findViewById(R.id.btnSend);
            emptyStateView = rootView.findViewById(R.id.emptyStateView);
            chatFinishedBanner = rootView.findViewById(R.id.chatFinishedBanner);

            if (ivBack != null) {
                ivBack.setOnClickListener(v -> {
                    try {
                        if (getActivity() != null && getActivity().getSupportFragmentManager() != null) {
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in back button: " + e.getMessage());
                    }
                });
            }

            if (btnSend != null) {
                btnSend.setOnClickListener(v -> {
                    try {
                        sendMessage();
                    } catch (Exception e) {
                        Log.e(TAG, "Error sending message: " + e.getMessage());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error al enviar mensaje", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            if (etMessage != null) {
                etMessage.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            }

            View btnStartChat = emptyStateView != null ? emptyStateView.findViewById(R.id.btnStartChat) : null;
            if (btnStartChat != null) {
                btnStartChat.setOnClickListener(v -> {
                    etMessage.requestFocus();
                    hideEmptyStateWithAnimation();
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in initViews: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupToolbar(String clientReservation) {
        tvClientName.setText("Cliente - Reserva #" + clientReservation);

        if (chatStatus == ChatSummary.ChatStatus.ACTIVE) {
            tvHotelStatus.setText("Respondiendo como hotel");
            tvHotelStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.light_green));
        } else if (chatStatus == ChatSummary.ChatStatus.FINISHED) {
            tvHotelStatus.setText("Chat finalizado");
            tvHotelStatus.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        } else {
            tvHotelStatus.setText("Esperando cliente");
            tvHotelStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.light_green));
        }
    }

    private void setupMessageList() {
        messageList = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        messageAdapter = new AdminMessageAdapter(getContext(), messageList);
        rvMessages.setAdapter(messageAdapter);
    }

    private void configureUIForChatStatus() {
        if (chatStatus == ChatSummary.ChatStatus.FINISHED) {
            if (chatFinishedBanner != null) {
                chatFinishedBanner.setVisibility(View.VISIBLE);
            }
            etMessage.setEnabled(false);
            btnSend.setEnabled(false);
            etMessage.setHint("Chat finalizado");
            etMessage.setBackgroundResource(R.drawable.bg_message_input_disabled);
            btnSend.setAlpha(0.5f);
        } else {
            if (chatFinishedBanner != null) {
                chatFinishedBanner.setVisibility(View.GONE);
            }
            if (chatStatus == ChatSummary.ChatStatus.AVAILABLE) {
                etMessage.setHint("Responde al cliente...");
            }
        }
    }

    private void loadMessagesFromFirebase() {
        chatService.loadMessages(chatId, new FirebaseChatService.OnMessagesLoadedListener() {
            @Override
            public void onMessagesLoaded(List<Message> messages) {
                if (isAdded() && getContext() != null) {
                    messageList.clear();
                    messageList.addAll(messages);
                    updateUI();
                    Log.d(TAG, "Mensajes cargados para admin desde Firebase: " + messages.size());
                }
            }

            @Override
            public void onMessageAdded(Message message) {
                if (isAdded() && getContext() != null) {
                    if (!messageExists(message.getId())) {
                        messageList.add(message);
                        messageAdapter.notifyItemInserted(messageList.size() - 1);
                        rvMessages.scrollToPosition(messageList.size() - 1);
                    }
                }
            }

            @Override
            public void onMessagesError(String error) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Error al cargar mensajes: " + error, Toast.LENGTH_SHORT).show();
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

    private void showEmptyState(boolean show) {
        if (show) {
            rvMessages.setVisibility(View.GONE);
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
                    .withEndAction(() -> {
                        emptyStateView.setVisibility(View.GONE);
                        rvMessages.setVisibility(View.VISIBLE);
                        etMessage.requestFocus();
                    })
                    .start();
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

        if (chatStatus == ChatSummary.ChatStatus.FINISHED) {
            etMessage.setEnabled(false);
            btnSend.setEnabled(false);
            etMessage.setHint("Chat finalizado");
            etMessage.setBackgroundResource(R.drawable.bg_message_input_disabled);
            btnSend.setAlpha(0.5f);
            if (chatFinishedBanner != null) {
                chatFinishedBanner.setVisibility(View.VISIBLE);
            }
        }
    }

    private void sendMessage() {
        final String messageText = etMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            return;
        }

        String userId = chatService.getCurrentUserId();

        final Message newMessage = new Message(
                String.valueOf(System.currentTimeMillis()),
                hotelId, // El admin envía desde el hotel
                userId, // Enviado al usuario
                messageText,
                System.currentTimeMillis(),
                Message.MessageType.HOTEL
        );

        etMessage.setText("");

        if (chatStatus == ChatSummary.ChatStatus.AVAILABLE) {
            chatStatus = ChatSummary.ChatStatus.ACTIVE;
            chatService.updateChatStatus(chatId, ChatSummary.ChatStatus.ACTIVE);
        }

        chatService.sendMessage(chatId, newMessage, new FirebaseChatService.OnMessageSentListener() {
            @Override
            public void onMessageSent(Message message) {
                Log.d(TAG, "Mensaje del admin enviado exitosamente: " + message.getId());
            }

            @Override
            public void onMessageError(String error) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Error al enviar mensaje: " + error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al enviar mensaje: " + error);
                    etMessage.setText(messageText);
                }
            }
        });

        messageList.add(newMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);

        rvMessages.post(() -> rvMessages.scrollToPosition(messageList.size() - 1));

        if (messageList.size() == 1) {
            hideEmptyStateWithAnimation();
        }
    }
}