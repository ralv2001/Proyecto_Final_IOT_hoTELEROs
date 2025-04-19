package com.example.proyecto_final_hoteleros.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.model.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_HOTEL = 2;

    private Context context;
    private List<Message> messageList;
    private SimpleDateFormat timeFormat;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        return message.getType() == Message.MessageType.USER ? VIEW_TYPE_USER : VIEW_TYPE_HOTEL;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_USER) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_user, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_hotel, parent, false);
        }

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.tvMessageText.setText(message.getText());
        holder.tvTime.setText(formatTime(message.getTimestamp()));

        // Check if we need to show the time header (if it's a different day from previous message)
        if (position > 0) {
            Message prevMessage = messageList.get(position - 1);
            if (shouldShowDateHeader(prevMessage.getTimestamp(), message.getTimestamp())) {
                holder.tvDateHeader.setVisibility(View.VISIBLE);
                holder.tvDateHeader.setText(formatDate(message.getTimestamp()));
            } else {
                holder.tvDateHeader.setVisibility(View.GONE);
            }
        } else {
            // First message should always show date
            holder.tvDateHeader.setVisibility(View.VISIBLE);
            holder.tvDateHeader.setText(formatDate(message.getTimestamp()));
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private String formatTime(long timestamp) {
        return timeFormat.format(new Date(timestamp));
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM", new Locale("es", "ES"));
        return dateFormat.format(new Date(timestamp));
    }

    private boolean shouldShowDateHeader(long prevTimestamp, long currentTimestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return !dateFormat.format(new Date(prevTimestamp)).equals(dateFormat.format(new Date(currentTimestamp)));
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText;
        TextView tvTime;
        TextView tvDateHeader;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
        }
    }
}