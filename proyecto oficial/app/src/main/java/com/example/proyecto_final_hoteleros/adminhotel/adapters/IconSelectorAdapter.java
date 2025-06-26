package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;
import java.util.List;

public class IconSelectorAdapter extends RecyclerView.Adapter<IconSelectorAdapter.IconViewHolder> {

    public interface OnIconClickListener {
        void onIconClick(IconHelper.IconItem iconItem);
    }

    private List<IconHelper.IconItem> icons;
    private String selectedIconKey;
    private OnIconClickListener listener;

    public IconSelectorAdapter(String selectedIconKey, OnIconClickListener listener) {
        this.selectedIconKey = selectedIconKey;
        this.listener = listener;
    }

    public void updateIcons(List<IconHelper.IconItem> newIcons) {
        this.icons = newIcons;
        notifyDataSetChanged();
    }

    public void setSelectedIconKey(String iconKey) {
        String oldSelected = this.selectedIconKey;
        this.selectedIconKey = iconKey;

        // Actualizar solo los items que cambiaron
        if (icons != null) {
            for (int i = 0; i < icons.size(); i++) {
                IconHelper.IconItem icon = icons.get(i);
                if (icon.getKey().equals(oldSelected) || icon.getKey().equals(iconKey)) {
                    notifyItemChanged(i);
                }
            }
        }
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_icon_selector, parent, false);
        return new IconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        if (icons != null) {
            holder.bind(icons.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return icons != null ? icons.size() : 0;
    }

    class IconViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon, ivSelectionIndicator;
        private TextView tvIconName;
        private View iconBackground, iconContainer;

        public IconViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            ivSelectionIndicator = itemView.findViewById(R.id.ivSelectionIndicator);
            tvIconName = itemView.findViewById(R.id.tvIconName);
            iconBackground = itemView.findViewById(R.id.iconBackground);
            iconContainer = itemView.findViewById(R.id.iconContainer);
        }

        public void bind(IconHelper.IconItem iconItem) {
            // Configurar icono
            ivIcon.setImageResource(iconItem.getResourceId());
            tvIconName.setText(iconItem.getName());

            // Verificar si está seleccionado
            boolean isSelected = iconItem.getKey().equals(selectedIconKey);

            if (isSelected) {
                ivSelectionIndicator.setVisibility(View.VISIBLE);
                iconBackground.setBackgroundResource(R.drawable.bg_circle_orange);
                iconContainer.setBackgroundResource(R.drawable.bg_service_card_selected);
                ivIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.orange));
            } else {
                ivSelectionIndicator.setVisibility(View.GONE);
                iconBackground.setBackgroundResource(R.drawable.bg_circle_orange_light);
                iconContainer.setBackgroundResource(R.drawable.bg_service_card);
                ivIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIconClick(iconItem);
                }
            });
        }
    }
}