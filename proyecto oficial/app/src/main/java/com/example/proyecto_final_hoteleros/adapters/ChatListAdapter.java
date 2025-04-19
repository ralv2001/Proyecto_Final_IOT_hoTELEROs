package com.example.proyecto_final_hoteleros.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.model.ChatSummary;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private final Context context;
    private final List<ChatSummary> chatList;
    private final OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(ChatSummary chat);
    }

    public ChatListAdapter(Context context, OnChatClickListener listener) {
        this.context = context;
        this.chatList = new ArrayList<>();
        this.listener = listener;
    }

    public void setChatList(List<ChatSummary> chats) {
        chatList.clear();
        if (chats != null) {
            chatList.addAll(chats);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatSummary chat = chatList.get(position);
        holder.bind(chat);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivHotelLogo;
        TextView tvHotelName, tvReservationInfo, tvLastMessage;
        Button btnChatAction;

        ChatViewHolder(View itemView) {
            super(itemView);
            ivHotelLogo = itemView.findViewById(R.id.ivHotelLogo);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvReservationInfo = itemView.findViewById(R.id.tvReservationInfo);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            btnChatAction = itemView.findViewById(R.id.btnChatAction);
        }

        void bind(ChatSummary chat) {
            tvHotelName.setText(chat.getHotelName());
            tvReservationInfo.setText("Reserva #" + chat.getReservationId() + " • " + chat.getReservationDates());

            // Configurar el último mensaje si existe
            if (chat.getLastMessage() != null && !chat.getLastMessage().isEmpty()) {
                tvLastMessage.setText(chat.getLastMessage());
                tvLastMessage.setVisibility(View.VISIBLE);
            } else {
                tvLastMessage.setVisibility(View.GONE);
            }

            // Cargar imagen del hotel
            // Si usas Glide:
            // Glide.with(context).load(chat.getHotelImageUrl()).placeholder(R.drawable.default_profile).into(ivHotelLogo);

            // Configurar botón según el estado del chat
            switch (chat.getStatus()) {
                case AVAILABLE:
                    btnChatAction.setText("Iniciar chat");
                    btnChatAction.setBackgroundResource(R.drawable.bg_button_chat);
                    btnChatAction.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                    break;
                case ACTIVE:
                    btnChatAction.setText("Continuar");
                    btnChatAction.setBackgroundResource(R.drawable.bg_button_chat);
                    btnChatAction.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                    break;
                case FINISHED:
                    btnChatAction.setText("Chat finalizado");
                    btnChatAction.setBackgroundResource(R.drawable.bg_button_chat_disabled);
                    btnChatAction.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
                    break;
            }

            // Configurar click listener
            btnChatAction.setOnClickListener(v -> {
                if (chat.getStatus() != ChatSummary.ChatStatus.FINISHED) {
                    listener.onChatClick(chat);
                }
            });

            // Click en toda la tarjeta para chats activos
            itemView.setOnClickListener(v -> {
                if (chat.getStatus() != ChatSummary.ChatStatus.FINISHED) {
                    listener.onChatClick(chat);
                }
            });
        }
    }
}