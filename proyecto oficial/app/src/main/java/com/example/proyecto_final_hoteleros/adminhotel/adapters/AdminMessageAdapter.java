package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.Message;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_HOTEL = 2;
    private static final int VIEW_TYPE_SYSTEM = 3;

    private Context context;
    private List<Message> messages;
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateFormat;

    public AdminMessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.dateFormat = new SimpleDateFormat("d MMMM", new Locale("es", "ES"));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(context).inflate(R.layout.admin_hotel_item_message_client, parent, false);
            return new ClientMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_HOTEL) {
            View view = LayoutInflater.from(context).inflate(R.layout.admin_hotel_item_message_admin, parent, false);
            return new AdminMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.admin_hotel_item_message_system, parent, false);
            return new SystemMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        boolean showDateHeader = shouldShowDateHeader(position);

        if (getItemViewType(position) == VIEW_TYPE_USER) {
            ((ClientMessageViewHolder) holder).bind(message, showDateHeader);
        } else if (getItemViewType(position) == VIEW_TYPE_HOTEL) {
            ((AdminMessageViewHolder) holder).bind(message, showDateHeader);
        } else {
            ((SystemMessageViewHolder) holder).bind(message, showDateHeader);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message.MessageType type = messages.get(position).getType();
        if (type == Message.MessageType.USER) {
            return VIEW_TYPE_USER;
        } else if (type == Message.MessageType.HOTEL) {
            return VIEW_TYPE_HOTEL;
        } else {
            return VIEW_TYPE_SYSTEM;
        }
    }

    private boolean shouldShowDateHeader(int position) {
        if (position == 0) {
            return true;
        }

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal1.setTimeInMillis(messages.get(position).getTimestamp());
        cal2.setTimeInMillis(messages.get(position - 1).getTimestamp());

        return cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR) ||
                cal1.get(Calendar.DAY_OF_YEAR) != cal2.get(Calendar.DAY_OF_YEAR);
    }

    private String getFormattedTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) {
            return "Ahora";
        }

        if (diff < 3600000) {
            int minutes = (int) (diff / 60000);
            return "Hace " + minutes + (minutes == 1 ? " minuto" : " minutos");
        }

        Calendar msgCalendar = Calendar.getInstance();
        msgCalendar.setTimeInMillis(timestamp);
        Calendar today = Calendar.getInstance();

        if (msgCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                msgCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return timeFormat.format(new Date(timestamp));
        }

        if (msgCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                msgCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1) {
            return "Ayer " + timeFormat.format(new Date(timestamp));
        }

        if (diff < 604800000) {
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("es", "ES"));
            return dayFormat.format(new Date(timestamp)) + " " + timeFormat.format(new Date(timestamp));
        }

        SimpleDateFormat fullFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("es", "ES"));
        return fullFormat.format(new Date(timestamp));
    }

    class ClientMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDateHeader;
        private TextView tvMessageText;
        private TextView tvTime;

        public ClientMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(Message message, boolean showDateHeader) {
            tvMessageText.setText(message.getText());
            tvTime.setText(getFormattedTime(message.getTimestamp()));

            if (showDateHeader) {
                Calendar msgCalendar = Calendar.getInstance();
                msgCalendar.setTimeInMillis(message.getTimestamp());
                Calendar today = Calendar.getInstance();

                if (msgCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        msgCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    tvDateHeader.setText("Hoy");
                } else if (msgCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        msgCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1) {
                    tvDateHeader.setText("Ayer");
                } else {
                    tvDateHeader.setText(dateFormat.format(new Date(message.getTimestamp())));
                }
                tvDateHeader.setVisibility(View.VISIBLE);
            } else {
                tvDateHeader.setVisibility(View.GONE);
            }
        }
    }

    class AdminMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDateHeader;
        private TextView tvMessageText;
        private TextView tvTime;
        private ImageView ivMessageStatus;

        public AdminMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivMessageStatus = itemView.findViewById(R.id.ivMessageStatus);
        }

        public void bind(Message message, boolean showDateHeader) {
            tvMessageText.setText(message.getText());
            tvTime.setText(getFormattedTime(message.getTimestamp()));

            if (showDateHeader) {
                Calendar msgCalendar = Calendar.getInstance();
                msgCalendar.setTimeInMillis(message.getTimestamp());
                Calendar today = Calendar.getInstance();

                if (msgCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        msgCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    tvDateHeader.setText("Hoy");
                } else if (msgCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        msgCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1) {
                    tvDateHeader.setText("Ayer");
                } else {
                    tvDateHeader.setText(dateFormat.format(new Date(message.getTimestamp())));
                }
                tvDateHeader.setVisibility(View.VISIBLE);
            } else {
                tvDateHeader.setVisibility(View.GONE);
            }

            if (ivMessageStatus != null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - message.getTimestamp() < 60000) {
                    ivMessageStatus.setImageResource(R.drawable.ic_message_sent);
                } else if (currentTime - message.getTimestamp() < 300000) {
                    ivMessageStatus.setImageResource(R.drawable.ic_message_delivered);
                } else {
                    ivMessageStatus.setImageResource(R.drawable.ic_message_seen);
                }
            }
        }
    }

    class SystemMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDateHeader;
        private TextView tvSystemMessage;
        private TextView tvTime;

        public SystemMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
            tvSystemMessage = itemView.findViewById(R.id.tvSystemMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(Message message, boolean showDateHeader) {
            tvSystemMessage.setText(message.getText());
            tvTime.setText(getFormattedTime(message.getTimestamp()));

            if (showDateHeader) {
                Calendar msgCalendar = Calendar.getInstance();
                msgCalendar.setTimeInMillis(message.getTimestamp());
                Calendar today = Calendar.getInstance();

                if (msgCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        msgCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    tvDateHeader.setText("Hoy");
                } else if (msgCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        msgCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1) {
                    tvDateHeader.setText("Ayer");
                } else {
                    tvDateHeader.setText(dateFormat.format(new Date(message.getTimestamp())));
                }
                tvDateHeader.setVisibility(View.VISIBLE);
            } else {
                tvDateHeader.setVisibility(View.GONE);
            }
        }
    }
}