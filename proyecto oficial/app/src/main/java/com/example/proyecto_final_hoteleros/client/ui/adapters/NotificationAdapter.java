package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notifications;
    private Context context;
    private OnNotificationListener onNotificationListener;

    // Constructor
    public NotificationAdapter(Context context, List<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    // Interfaz para manejar eventos de click
    public interface OnNotificationListener {
        void onNotificationClick(Notification notification);
        void onViewDetailsClick(Notification notification);
        void onMarkAsReadClick(Notification notification);
    }

    // Setter para el listener
    public void setOnNotificationListener(OnNotificationListener listener) {
        this.onNotificationListener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.client_item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        // Configuración de los elementos visuales
        holder.ivIcon.setImageResource(notification.getIconResource());
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        holder.tvTimeAgo.setText(notification.getTimeAgo());

        // Mostrar/ocultar indicador de no leído
        holder.unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

        // Mostrar/ocultar botones de acción dependiendo del tipo
        if (notification.getType() == Notification.TYPE_BOOKING ||
                notification.getType() == Notification.TYPE_CHECK_IN) {
            holder.layoutActionButtons.setVisibility(View.VISIBLE);
            // Ocultar botón de marcar como leído si ya está leído
            holder.btnMarkAsRead.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
        } else {
            holder.layoutActionButtons.setVisibility(View.GONE);
        }

        // Estilo adicional para notificaciones leídas
        if (notification.isRead()) {
            holder.itemView.setAlpha(0.8f);
            holder.tvTitle.setTextColor(Color.parseColor("#757575"));
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.tvTitle.setTextColor(Color.parseColor("#000000"));
        }

        // Configurar eventos de click
        holder.itemView.setOnClickListener(v -> {
            if (onNotificationListener != null) {
                onNotificationListener.onNotificationClick(notification);
            }
        });

        holder.btnViewDetails.setOnClickListener(v -> {
            if (onNotificationListener != null) {
                onNotificationListener.onViewDetailsClick(notification);
            }
        });

        holder.btnMarkAsRead.setOnClickListener(v -> {
            if (onNotificationListener != null) {
                onNotificationListener.onMarkAsReadClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    // Método para actualizar los datos
    public void updateData(List<Notification> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    // ViewHolder para las notificaciones
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvMessage, tvTimeAgo;
        View unreadIndicator;
        LinearLayout layoutActionButtons;
        Button btnViewDetails, btnMarkAsRead;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_notification_icon);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTimeAgo = itemView.findViewById(R.id.tv_time_ago);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
            layoutActionButtons = itemView.findViewById(R.id.layout_action_buttons);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnMarkAsRead = itemView.findViewById(R.id.btn_mark_as_read);
        }
    }
}