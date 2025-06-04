package com.example.proyecto_final_hoteleros.taxista.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.taxista.model.DriverProfile;
import com.example.proyecto_final_hoteleros.taxista.model.ProfileMenuItem;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class DriverProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_MENU_ITEM = 1;

    private Context context;
    private List<Object> items;
    private OnProfileItemClickListener listener;

    public interface OnProfileItemClickListener {
        void onMenuItemClick(ProfileMenuItem item);
        void onAvailabilityToggle(boolean isAvailable);
    }

    public interface OnProfileCloseListener {
        void onCloseClick();
    }

    public DriverProfileAdapter(Context context, List<Object> items, OnProfileItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof DriverProfile) {
            return TYPE_HEADER;
        } else if (item instanceof ProfileMenuItem) {
            return TYPE_MENU_ITEM;
        }
        return TYPE_MENU_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case TYPE_HEADER:
                View headerView = inflater.inflate(R.layout.item_profile_header, parent, false);
                return new HeaderViewHolder(headerView);
            case TYPE_MENU_ITEM:
            default:
                View menuView = inflater.inflate(R.layout.item_profile_menu, parent, false);
                return new MenuItemViewHolder(menuView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        switch (holder.getItemViewType()) {
            case TYPE_HEADER:
                bindHeaderViewHolder((HeaderViewHolder) holder, (DriverProfile) item);
                break;
            case TYPE_MENU_ITEM:
                bindMenuItemViewHolder((MenuItemViewHolder) holder, (ProfileMenuItem) item);
                break;
        }
    }

    private void bindHeaderViewHolder(HeaderViewHolder holder, DriverProfile profile) {
        holder.tvDriverName.setText(profile.getFullName());
        holder.tvVehicleInfo.setText("Toyota Rush 2023"); // Podrías agregar este campo al modelo
        holder.tvRating.setText(String.format("%.1f", profile.getAverageRating()));

        // Cargar imagen de perfil
        Glide.with(context)
                .load(profile.getProfileImageUrl())
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(holder.ivProfileImage);

        // Mostrar indicador de estado en línea
        holder.onlineIndicator.setVisibility(profile.isAvailable() ? View.VISIBLE : View.VISIBLE);

        // Listener para el botón cerrar
        holder.btnClose.setOnClickListener(v -> {
            // Manejar cierre si es necesario
            if (listener != null && listener instanceof OnProfileCloseListener) {
                ((OnProfileCloseListener) listener).onCloseClick();
            }
        });
    }

    private void bindMenuItemViewHolder(MenuItemViewHolder holder, ProfileMenuItem menuItem) {
        holder.tvTitle.setText(menuItem.getTitle());
        holder.tvSubtitle.setText(menuItem.getSubtitle());
        holder.ivIcon.setImageResource(menuItem.getIconResId());
        holder.ivArrow.setVisibility(menuItem.hasArrow() ? View.VISIBLE : View.GONE);

        // Configurar colores según el tipo de opción
        int backgroundColor, iconColor;
        switch (menuItem.getTitle()) {
            case "Hoteles Disponibles":
                backgroundColor = 0xFFFFF3E0; // Naranja claro
                iconColor = 0xFFF44336; // Rojo
                break;
            case "Editar Perfil":
                backgroundColor = 0xFFFFF3E0; // Verde claro
                iconColor = 0xFFF44336; // Verde
                break;
            case "Historial":
                backgroundColor = 0xFFFFF3E0; // Azul claro
                iconColor = 0xFFF44336; // Azul
                break;
            case "Métodos de pago":
                backgroundColor = 0xFFFFF3E0; // Amarillo claro
                iconColor = 0xFFF44336; // Naranja
                break;
            case "Notificaciones":
                backgroundColor = 0xFFFFF3E0; // Morado claro
                iconColor = 0xFFF44336; // Morado
                break;
            case "Cerrar Sesión":
                backgroundColor = 0xFFFFF3E0; // Rojo claro
                iconColor = 0xFFF44336; // Rojo
                break;
            default:
                backgroundColor = 0xFFF5F5F5; // Gris claro por defecto
                iconColor = 0xFFF44336; // Gris oscuro
                break;
        }

        holder.cardIconBackground.setCardBackgroundColor(backgroundColor);
        holder.ivIcon.setColorFilter(iconColor);

        // Configurar color de texto para "Cerrar Sesión"
        if ("Cerrar Sesión".equals(menuItem.getTitle())) {
            holder.tvTitle.setTextColor(0xFFF44336);
            holder.ivArrow.setColorFilter(0xFFF44336);
        } else {
            holder.tvTitle.setTextColor(0xFF1A1A1A);
            holder.ivArrow.setColorFilter(0xFFCCCCCC);
        }

        if (menuItem.getSubtitle() != null && !menuItem.getSubtitle().isEmpty()) {
            holder.tvSubtitle.setVisibility(View.VISIBLE);
        } else {
            holder.tvSubtitle.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMenuItemClick(menuItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolders
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfileImage;
        ImageView btnClose;
        TextView tvDriverName;
        TextView tvVehicleInfo;
        TextView tvRating;
        View onlineIndicator;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.iv_profile_image);
            btnClose = itemView.findViewById(R.id.btn_close);
            tvDriverName = itemView.findViewById(R.id.tv_driver_name);
            tvVehicleInfo = itemView.findViewById(R.id.tv_vehicle_info);
            tvRating = itemView.findViewById(R.id.tv_rating);
            onlineIndicator = itemView.findViewById(R.id.online_indicator);
        }
    }

    static class MenuItemViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardIconBackground;
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvSubtitle;
        ImageView ivArrow;

        MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cardIconBackground = itemView.findViewById(R.id.card_icon_background);
            ivIcon = itemView.findViewById(R.id.iv_menu_icon);
            tvTitle = itemView.findViewById(R.id.tv_menu_title);
            tvSubtitle = itemView.findViewById(R.id.tv_menu_subtitle);
            ivArrow = itemView.findViewById(R.id.iv_menu_arrow);
        }
    }
}