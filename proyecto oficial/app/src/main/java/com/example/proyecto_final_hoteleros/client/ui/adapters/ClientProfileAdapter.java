package com.example.proyecto_final_hoteleros.client.ui.adapters;

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
import com.example.proyecto_final_hoteleros.client.data.model.ClientProfile;
import com.example.proyecto_final_hoteleros.client.data.model.ClientProfileMenuItem;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ClientProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_MENU_ITEM = 1;

    private Context context;
    private List<Object> items;
    private OnClientProfileItemClickListener listener;

    public interface OnClientProfileItemClickListener {
        void onMenuItemClick(ClientProfileMenuItem item);
    }

    public ClientProfileAdapter(Context context, List<Object> items, OnClientProfileItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof ClientProfile) {
            return TYPE_HEADER;
        } else if (item instanceof ClientProfileMenuItem) {
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
                View headerView = inflater.inflate(R.layout.client_item_profile_header, parent, false);
                return new HeaderViewHolder(headerView);
            case TYPE_MENU_ITEM:
            default:
                View menuView = inflater.inflate(R.layout.client_item_profile_menu, parent, false);
                return new MenuItemViewHolder(menuView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        switch (holder.getItemViewType()) {
            case TYPE_HEADER:
                bindHeaderViewHolder((HeaderViewHolder) holder, (ClientProfile) item);
                break;
            case TYPE_MENU_ITEM:
                bindMenuItemViewHolder((MenuItemViewHolder) holder, (ClientProfileMenuItem) item);
                break;
        }
    }

    private void bindHeaderViewHolder(HeaderViewHolder holder, ClientProfile profile) {
        holder.tvClientName.setText(profile.getFullName());
        holder.tvEmail.setText(profile.getEmail());

        // Cargar imagen de perfil
        Glide.with(context)
                .load(profile.getProfileImageUrl())
                .placeholder(R.drawable.perfil)
                .circleCrop()
                .into(holder.ivProfileImage);
    }

    private void bindMenuItemViewHolder(MenuItemViewHolder holder, ClientProfileMenuItem menuItem) {
        holder.tvTitle.setText(menuItem.getTitle());
        holder.tvSubtitle.setText(menuItem.getSubtitle());
        holder.ivIcon.setImageResource(menuItem.getIconResId());
        holder.ivArrow.setVisibility(menuItem.hasArrow() ? View.VISIBLE : View.GONE);

        // Configurar colores según el tipo de opción
        int backgroundColor, iconColor;
        switch (menuItem.getTitle()) {
            case "Mis Reservas":
                backgroundColor = 0xFFFFF3E0; // Naranja claro
                iconColor = 0xFFF44336; // Rojo
                break;
            case "Editar Perfil":
                backgroundColor = 0xFFFFF3E0; // Verde claro
                iconColor = 0xFFF44336; // Verde
                break;
            case "Métodos de Pago":
                backgroundColor = 0xFFFFF3E0; // Azul claro
                iconColor = 0xFFF44336; // Azul
                break;
            case "Notificaciones":
                backgroundColor = 0xFFFFF3E0; // Amarillo claro
                iconColor = 0xFFF44336; // Naranja
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
        TextView tvClientName;
        TextView tvEmail;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.iv_profile_image);
            tvClientName = itemView.findViewById(R.id.tv_client_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
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
