package com.example.proyecto_final_hoteleros.client.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adapters.MessageAdapter;
import com.example.proyecto_final_hoteleros.client.model.Message;
import com.example.proyecto_final_hoteleros.client.model.ChatSummary;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatConversationFragment extends Fragment {

    // UI Components
    private Toolbar toolbar;
    private TextView tvHotelName;
    private ImageView ivBack;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private View emptyStateView;

    // Data
    private String chatId;
    private String hotelId;
    private String hotelName;
    private List<Message> messageList;
    private MessageAdapter messageAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat_conversation, container, false);

        // Extract arguments
        if (getArguments() != null) {
            chatId = getArguments().getString("chat_id");
            hotelId = getArguments().getString("hotel_id");
            hotelName = getArguments().getString("hotel_name");
        }

        // Initialize views
        initViews(rootView);

        // Setup toolbar
        setupToolbar(hotelName);

        // Setup message list
        setupMessageList();

        // Load messages
        loadMessages();

        return rootView;
    }

    private void initViews(View rootView) {
        toolbar = rootView.findViewById(R.id.toolbar);
        tvHotelName = rootView.findViewById(R.id.tvHotelName);
        ivBack = rootView.findViewById(R.id.ivBack);
        rvMessages = rootView.findViewById(R.id.rvMessages);
        etMessage = rootView.findViewById(R.id.etMessage);
        btnSend = rootView.findViewById(R.id.btnSend);
        emptyStateView = rootView.findViewById(R.id.emptyStateView);

        // Set up back button
        ivBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Set up send button
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupToolbar(String hotelName) {
        tvHotelName.setText(hotelName);
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

    private void loadMessages() {
        // In a real app, you would load messages from a database or API
        // Here we'll create some demo messages for illustration
        messageList.clear();

        if (chatId.equals("chat_2")) {
            // For active chat, show some demo messages
            messageList.add(new Message("1", "user_1", hotelId,
                    "Hola, ¿podrían indicarme cómo llegar a la piscina?",
                    System.currentTimeMillis() - 3600000, Message.MessageType.USER));

            messageList.add(new Message("2", hotelId, "user_1",
                    "¡Buenos días! La piscina se encuentra en la planta baja, siguiendo las indicaciones hacia el spa.",
                    System.currentTimeMillis() - 3500000, Message.MessageType.HOTEL));

            messageList.add(new Message("3", "user_1", hotelId,
                    "¿Podría solicitar servicio de habitaciones?",
                    System.currentTimeMillis() - 900000, Message.MessageType.USER));
        } else if (chatId.equals("chat_3")) {
            // For finished chat, show some demo messages
            messageList.add(new Message("1", "user_1", hotelId,
                    "Necesito hacer checkout mañana a las 6 AM, ¿es posible?",
                    System.currentTimeMillis() - 259200000, Message.MessageType.USER));

            messageList.add(new Message("2", hotelId, "user_1",
                    "Por supuesto, hemos registrado su solicitud de checkout temprano. El personal estará disponible para atenderle.",
                    System.currentTimeMillis() - 258000000, Message.MessageType.HOTEL));

            messageList.add(new Message("3", "user_1", hotelId,
                    "Muchas gracias por todo, ha sido una estancia muy agradable.",
                    System.currentTimeMillis() - 172800000, Message.MessageType.USER));

            messageList.add(new Message("4", hotelId, "user_1",
                    "Gracias por su estancia, esperamos verle pronto.",
                    System.currentTimeMillis() - 172000000, Message.MessageType.HOTEL));
        }

        // Update UI based on message list
        updateUI();
    }

    private void updateUI() {
        if (messageList.isEmpty()) {
            rvMessages.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            rvMessages.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
            messageAdapter.notifyDataSetChanged();
            rvMessages.scrollToPosition(messageList.size() - 1);
        }

        // Disable input for finished chats
        if (chatId.equals("chat_3")) {
            etMessage.setEnabled(false);
            btnSend.setEnabled(false);
            etMessage.setHint("Chat finalizado");
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

        // Simulate response after a short delay
        if (!chatId.equals("chat_3")) {
            simulateResponse();
        }
    }

    private void simulateResponse() {
        // Simulate hotel response after a delay (only for demo purposes)
        rvMessages.postDelayed(() -> {
            Message response = new Message(
                    String.valueOf(System.currentTimeMillis()),
                    hotelId,
                    "user_1",
                    "Gracias por su mensaje. Un representante del hotel le atenderá en breve.",
                    System.currentTimeMillis(),
                    Message.MessageType.HOTEL
            );

            messageList.add(response);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            rvMessages.scrollToPosition(messageList.size() - 1);
        }, 1500);
    }
}