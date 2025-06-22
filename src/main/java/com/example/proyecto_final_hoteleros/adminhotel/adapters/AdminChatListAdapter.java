package com.example.proyecto_final_hoteleros.adminhotel.adapters;

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
import com.example.proyecto_final_hoteleros.client.data.model.ChatSummary;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.ArrayList;
import java.util.List;

public class AdminChatListAdapter extends RecyclerView.Adapter<AdminChatListAdapter.ChatViewHolder> {
    private static final String TAG = "AdminChatListAdapter";
    private Context context;
    private List<ChatSummary> chatList;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(ChatSummary chat);
    }

    public AdminChatListAdapter(Context context, OnChatClickListener listener) {
        this.context = context;
        this.chatList = new ArrayList<>();
        this.listener = listener;
        Log.d(TAG, "AdminChatListAdapter initialized");
    }

    public void setChatList(List<ChatSummary> chatList) {
        this.chatList = chatList;
        notifyDataSetChanged();
        Log.d(TAG, "Admin ChatList updated with " + chatList.size() + " items");
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_hotel_item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatSummary chat = chatList.get(position);
        holder.bind(chat, position);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivClientAvatar;
        private TextView tvClientName;
        private TextView tvReservationInfo;
        private TextView tvLastMessage;
        private Button btnChatAction;
        private View statusIndicator;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            ivClientAvatar = itemView.findViewById(R.id.ivClientAvatar);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvReservationInfo = itemView.findViewById(R.id.tvReservationInfo);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            btnChatAction = itemView.findViewById(R.id.btnChatAction);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);

            itemView.setOnClickListener(v -> handleItemClick());
        }

        private void handleItemClick() {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onChatClick(chatList.get(position));
            }
        }

        public void bind(ChatSummary chat, int position) {
            tvClientName.setText("Cliente - Reserva #" + chat.getReservationId());

            String reservationInfo = "Hotel: " + chat.getHotelName() + " • " + chat.getReservationDates();
            tvReservationInfo.setText(reservationInfo);

            ivClientAvatar.setImageResource(R.drawable.ic_profile_placeholder);

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

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            params.setMargins(0, 2, 12, 0);
            tvLastMessage.setLayoutParams(params);
            tvLastMessage.setMaxLines(2);
            tvLastMessage.setEllipsize(android.text.TextUtils.TruncateAt.END);

            switch (chat.getStatus()) {
                case AVAILABLE:
                    tvLastMessage.setText("Cliente esperando respuesta");
                    btnChatAction.setText("Iniciar");
                    btnChatAction.setBackgroundResource(R.drawable.bg_button_blue);
                    break;
                case ACTIVE:
                    if (chat.getLastMessage() != null && !chat.getLastMessage().isEmpty()) {
                        tvLastMessage.setText(chat.getLastMessage());
                    } else {
                        tvLastMessage.setText("Conversación activa");
                    }
                    btnChatAction.setText("Responder");
                    btnChatAction.setBackgroundResource(R.drawable.bg_button_primary);
                    break;
                case FINISHED:
                    if (chat.getLastMessage() != null && !chat.getLastMessage().isEmpty()) {
                        tvLastMessage.setText(chat.getLastMessage());
                    } else {
                        tvLastMessage.setText("Chat finalizado");
                    }
                    btnChatAction.setText("Ver");
                    btnChatAction.setBackgroundResource(R.drawable.bg_button_gray);
                    break;
            }

            btnChatAction.setVisibility(View.VISIBLE);
            btnChatAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChatClick(chat);
                }
            });
        }
    }
}