package com.example.proyecto_final_hoteleros.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.model.Message;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_HOTEL = 2;

    private Context context;
    private List<Message> messages;
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateFormat;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.dateFormat = new SimpleDateFormat("d MMMM", new Locale("es", "ES"));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_hotel, parent, false);
            return new HotelMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        // Check if we need to show date header
        boolean showDateHeader = shouldShowDateHeader(position);

        if (getItemViewType(position) == VIEW_TYPE_USER) {
            ((UserMessageViewHolder) holder).bind(message, showDateHeader);
        } else {
            ((HotelMessageViewHolder) holder).bind(message, showDateHeader);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType() == Message.MessageType.USER ? VIEW_TYPE_USER : VIEW_TYPE_HOTEL;
    }

    // Helper method to determine if we should show date header
    private boolean shouldShowDateHeader(int position) {
        if (position == 0) {
            return true; // Always show for first message
        }

        // Compare current message date with previous message date
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal1.setTimeInMillis(messages.get(position).getTimestamp());
        cal2.setTimeInMillis(messages.get(position - 1).getTimestamp());

        return cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR) ||
                cal1.get(Calendar.DAY_OF_YEAR) != cal2.get(Calendar.DAY_OF_YEAR);
    }

    // ViewHolder for user messages
    class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDateHeader;
        private TextView tvMessageText;
        private TextView tvTime;
        private ImageView ivMessageStatus;

        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivMessageStatus = itemView.findViewById(R.id.ivMessageStatus);
        }

        public void bind(Message message, boolean showDateHeader) {
            tvMessageText.setText(message.getText());

            // Format and set time
            Date messageDate = new Date(message.getTimestamp());
            tvTime.setText(timeFormat.format(messageDate));

            // Show/hide date header
            if (showDateHeader) {
                tvDateHeader.setVisibility(View.VISIBLE);
                tvDateHeader.setText(dateFormat.format(messageDate));
            } else {
                tvDateHeader.setVisibility(View.GONE);
            }

            // Set message status icon (sent, delivered, seen)
            ivMessageStatus.setImageResource(R.drawable.ic_message_sent);

            // Check if the message is recent (less than 1 minute ago)
            long currentTime = System.currentTimeMillis();
            if (currentTime - message.getTimestamp() < 60000) {
                // Just sent - show sent icon
                ivMessageStatus.setImageResource(R.drawable.ic_message_sent);
            } else {
                // Message is older - show seen icon
                ivMessageStatus.setImageResource(R.drawable.ic_message_seen);
            }
        }
    }

    // ViewHolder for hotel messages
    class HotelMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDateHeader;
        private TextView tvHotelNameInMessage;
        private TextView tvMessageText;
        private TextView tvTime;

        public HotelMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
            tvHotelNameInMessage = itemView.findViewById(R.id.tvHotelNameInMessage);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(Message message, boolean showDateHeader) {
            tvMessageText.setText(message.getText());

            // Format and set time
            Date messageDate = new Date(message.getTimestamp());
            tvTime.setText(timeFormat.format(messageDate));

            // Show/hide date header
            if (showDateHeader) {
                tvDateHeader.setVisibility(View.VISIBLE);
                tvDateHeader.setText(dateFormat.format(messageDate));
            } else {
                tvDateHeader.setVisibility(View.GONE);
            }

            // We're showing the hotel name for the first message or after a date change
            if (showDateHeader) {
                tvHotelNameInMessage.setVisibility(View.VISIBLE);
            } else {
                // Check if previous message was from hotel
                int position = getAdapterPosition();
                if (position > 0 &&
                        messages.get(position - 1).getType() == Message.MessageType.HOTEL &&
                        !shouldShowDateHeader(position)) {
                    tvHotelNameInMessage.setVisibility(View.GONE);
                } else {
                    tvHotelNameInMessage.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}