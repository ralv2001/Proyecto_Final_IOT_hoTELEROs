package com.example.proyecto_final_hoteleros.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.model.ChatSummary;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
    private static final String TAG = "ChatListAdapter";
    private Context context;
    private List<ChatSummary> chatList;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(ChatSummary chat);
    }

    public ChatListAdapter(Context context, OnChatClickListener listener) {
        this.context = context;
        this.chatList = new ArrayList<>();
        this.listener = listener;
        Log.d(TAG, "ChatListAdapter initialized with listener: " + (listener != null));
    }

    public void setChatList(List<ChatSummary> chatList) {
        this.chatList = chatList;
        notifyDataSetChanged();
        Log.d(TAG, "ChatList updated with " + chatList.size() + " items");
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        Log.d(TAG, "Creating new ChatViewHolder");
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatSummary chat = chatList.get(position);
        holder.bind(chat, position);
        Log.d(TAG, "Binding chat at position " + position + ": " + chat.getHotelName());
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivHotelLogo;
        private TextView tvHotelName;
        private TextView tvReservationInfo;
        private TextView tvLastMessage;
        private Button btnChatAction;
        private View statusIndicator;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHotelLogo = itemView.findViewById(R.id.ivHotelLogo);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvReservationInfo = itemView.findViewById(R.id.tvReservationInfo);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            btnChatAction = itemView.findViewById(R.id.btnChatAction);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);

            // Improved debugging for button setup
            if (btnChatAction == null) {
                Log.e(TAG, "btnChatAction is null!");
            } else {
                Log.d(TAG, "btnChatAction found successfully");
            }

            // Set click listener for the whole item
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleItemClick();
                }
            });
        }

        private void handleItemClick() {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                Log.d(TAG, "Item clicked at position: " + position);
                listener.onChatClick(chatList.get(position));
            } else {
                Log.e(TAG, "Click ignored - position: " + position + ", listener: " + (listener != null));
            }
        }

        public void bind(ChatSummary chat, int position) {
            // Set hotel name
            tvHotelName.setText(chat.getHotelName());

            // Set reservation info
            String reservationInfo = "Reserva #" + chat.getReservationId() + " • " + chat.getReservationDates();
            tvReservationInfo.setText(reservationInfo);

            // Set hotel image with improved error handling
            if (chat.getHotelImageUrl() != null && !chat.getHotelImageUrl().isEmpty()) {
                try {
                    Glide.with(context)
                            .load(chat.getHotelImageUrl())
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_hotel_avatar)
                                    .error(R.drawable.ic_hotel_avatar)
                                    .centerCrop())
                            .into(ivHotelLogo);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading image: " + e.getMessage());
                    ivHotelLogo.setImageResource(R.drawable.ic_hotel_avatar);
                }
            } else {
                ivHotelLogo.setImageResource(R.drawable.ic_hotel_avatar);
            }

            // Set status indicator color based on chat status
            if (statusIndicator != null) {
                switch (chat.getStatus()) {
                    case AVAILABLE:
                        statusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
                        break;
                    case ACTIVE:
                        statusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
                        break;
                    case FINISHED:
                        statusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray));
                        break;
                }
            }

            // MODIFICADO: Solución para el problema de mensajes cortados
            // Configurar el texto del último mensaje con una anchura adecuada
            // y asegurar que no se corte el texto
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
            );
            params.setMargins(0, 2, 12, 0);  // margen derecho para separar del botón
            tvLastMessage.setLayoutParams(params);

            // Asegurar que se muestra elipsis si el texto es demasiado largo
            tvLastMessage.setMaxLines(2);
            tvLastMessage.setEllipsize(android.text.TextUtils.TruncateAt.END);

            // Set last message and button based on chat status
            switch (chat.getStatus()) {
                case AVAILABLE:
                    tvLastMessage.setText("No hay mensajes");
                    tvLastMessage.setVisibility(View.VISIBLE);
                    btnChatAction.setText("Iniciar chat");
                    btnChatAction.setEnabled(true);

                    // Use blue button for available chats
                    btnChatAction.setBackgroundResource(R.drawable.bg_button_blue);
                    break;
                case ACTIVE:
                    if (chat.getLastMessage() != null && !chat.getLastMessage().isEmpty()) {
                        tvLastMessage.setText(chat.getLastMessage());
                        tvLastMessage.setVisibility(View.VISIBLE);
                    } else {
                        tvLastMessage.setText("No hay mensajes");
                        tvLastMessage.setVisibility(View.VISIBLE);
                    }
                    btnChatAction.setText("Continuar");
                    btnChatAction.setEnabled(true);

                    // Use orange button for active chats
                    btnChatAction.setBackgroundResource(R.drawable.bg_button_primary);
                    break;
                case FINISHED:
                    if (chat.getLastMessage() != null && !chat.getLastMessage().isEmpty()) {
                        tvLastMessage.setText(chat.getLastMessage());
                        tvLastMessage.setVisibility(View.VISIBLE);
                    } else {
                        tvLastMessage.setText("Chat finalizado");
                        tvLastMessage.setVisibility(View.VISIBLE);
                    }
                    btnChatAction.setText("Ver");
                    btnChatAction.setEnabled(true);

                    // Use gray button for finished chats
                    btnChatAction.setBackgroundResource(R.drawable.bg_button_gray);
                    break;
            }

            // IMPORTANT: Make sure the button is visible, clickable, and has proper OnClickListener
            btnChatAction.setVisibility(View.VISIBLE);
            btnChatAction.setClickable(true);
            btnChatAction.setFocusable(true);

            // FIXED: Use a final copy of the position for the listener
            final int currentPos = position;

            // Clear any existing click listeners (to avoid duplicates)
            btnChatAction.setOnClickListener(null);

            // Add a new click listener for the button with improved error handling
            btnChatAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Log.d(TAG, "Button clicked for chat: " + chat.getHotelName() + " at position: " + currentPos);
                        if (listener != null) {
                            // Remove toast to prevent UI delays
                            listener.onChatClick(chat);
                        } else {
                            Log.e(TAG, "Listener is null when button clicked");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in button click: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}