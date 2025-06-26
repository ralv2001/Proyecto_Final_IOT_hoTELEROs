package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceItem;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ServiceManagementAdapter extends RecyclerView.Adapter<ServiceManagementAdapter.ServiceViewHolder> {

    public interface OnServiceActionListener {
        void onEditService(HotelServiceItem service, int position);
        void onDeleteService(HotelServiceItem service, int position);
        void onToggleService(HotelServiceItem service, int position, boolean isActive);
    }

    private List<HotelServiceItem> services;
    private OnServiceActionListener listener;
    private NumberFormat currencyFormat;

    public ServiceManagementAdapter(List<HotelServiceItem> services, OnServiceActionListener listener) {
        this.services = services;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_service_management, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        HotelServiceItem service = services.get(position);
        holder.bind(service, position);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardServiceIcon;
        private ImageView ivServiceIcon, ivServiceMenu;
        private TextView tvServiceName, tvServiceDescription, tvServicePrice, tvServiceTypeLabel, tvConditionalInfo, tvPhotoCount;
        private SwitchMaterial switchServiceActive;
        private View layoutPhotoIndicator;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardServiceIcon = itemView.findViewById(R.id.cardServiceIcon);
            ivServiceIcon = itemView.findViewById(R.id.ivServiceIcon);
            ivServiceMenu = itemView.findViewById(R.id.ivServiceMenu);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceDescription = itemView.findViewById(R.id.tvServiceDescription);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            tvServiceTypeLabel = itemView.findViewById(R.id.tvServiceTypeLabel);
            tvConditionalInfo = itemView.findViewById(R.id.tvConditionalInfo);
            tvPhotoCount = itemView.findViewById(R.id.tvPhotoCount);
            switchServiceActive = itemView.findViewById(R.id.switchServiceActive);
            layoutPhotoIndicator = itemView.findViewById(R.id.layoutPhotoIndicator);
        }

        public void bind(HotelServiceItem service, int position) {
            // ✅ Información básica del servicio
            tvServiceName.setText(service.getName());
            tvServiceDescription.setText(service.getDescription());

            // ✅ Icono del servicio
            int iconResource = IconHelper.getIconResource(service.getIconKey());
            ivServiceIcon.setImageResource(iconResource);

            // ✅ Configurar apariencia según tipo de servicio
            setupServiceTypeAppearance(service);

            // ✅ Precio del servicio
            setupPriceDisplay(service);

            // ✅ Información condicional
            setupConditionalInfo(service);

            // ✅ Contador de fotos
            setupPhotoIndicator(service);

            // ✅ Switch de activación
            switchServiceActive.setOnCheckedChangeListener(null); // Evitar callbacks durante el setup
            switchServiceActive.setChecked(service.isActive());
            switchServiceActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggleService(service, position, isChecked);
                }
            });

            // ✅ Menú de opciones
            ivServiceMenu.setOnClickListener(v -> showServiceMenu(service, position));

            // ✅ Click en la card para editar
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditService(service, position);
                }
            });
        }

        private void setupServiceTypeAppearance(HotelServiceItem service) {
            int strokeColor, labelColor, iconBackgroundColor, chipBackground;
            String labelText = service.getTypeLabel();

            switch (service.getType()) {
                case BASIC:
                    strokeColor = ContextCompat.getColor(itemView.getContext(), R.color.orange);
                    labelColor = R.color.orange;
                    iconBackgroundColor = R.color.orange_light;
                    chipBackground = R.drawable.bg_chip_orange;
                    break;
                case INCLUDED:
                    strokeColor = ContextCompat.getColor(itemView.getContext(), R.color.green);
                    labelColor = R.color.green;
                    iconBackgroundColor = R.color.green_light;
                    chipBackground = R.drawable.bg_chip_green;
                    break;
                case PAID:
                    strokeColor = ContextCompat.getColor(itemView.getContext(), R.color.blue);
                    labelColor = R.color.blue;
                    iconBackgroundColor = R.color.blue_light;
                    chipBackground = R.drawable.bg_chip_blue;
                    break;
                case CONDITIONAL:
                    strokeColor = ContextCompat.getColor(itemView.getContext(), R.color.purple);
                    labelColor = R.color.purple;
                    iconBackgroundColor = R.color.purple_light;
                    chipBackground = R.drawable.bg_chip_purple;
                    break;
                default:
                    strokeColor = ContextCompat.getColor(itemView.getContext(), R.color.text_secondary);
                    labelColor = R.color.text_secondary;
                    iconBackgroundColor = R.color.background_light;
                    chipBackground = R.drawable.bg_chip_green;
                    break;
            }

            // ✅ Aplicar colores
            ((MaterialCardView) itemView).setStrokeColor(strokeColor);
            cardServiceIcon.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), iconBackgroundColor));
            tvServiceTypeLabel.setText(labelText);
            tvServiceTypeLabel.setTextColor(ContextCompat.getColor(itemView.getContext(), labelColor));
            tvServiceTypeLabel.setBackgroundResource(chipBackground);
        }

        private void setupPriceDisplay(HotelServiceItem service) {
            if (service.getPrice() > 0) {
                tvServicePrice.setText(currencyFormat.format(service.getPrice()));
                tvServicePrice.setVisibility(View.VISIBLE);
            } else {
                tvServicePrice.setVisibility(View.GONE);
            }
        }

        private void setupConditionalInfo(HotelServiceItem service) {
            if (service.getType() == HotelServiceItem.ServiceType.CONDITIONAL && service.getConditionalAmount() > 0) {
                tvConditionalInfo.setText("Activo con compras > " + currencyFormat.format(service.getConditionalAmount()));
                tvConditionalInfo.setVisibility(View.VISIBLE);
            } else {
                tvConditionalInfo.setVisibility(View.GONE);
            }
        }

        private void setupPhotoIndicator(HotelServiceItem service) {
            int photoCount = service.getPhotos().size();
            if (photoCount > 0) {
                tvPhotoCount.setText(photoCount + " foto" + (photoCount > 1 ? "s" : ""));
                layoutPhotoIndicator.setVisibility(View.VISIBLE);
            } else {
                layoutPhotoIndicator.setVisibility(View.GONE);
            }
        }

        private void showServiceMenu(HotelServiceItem service, int position) {
            PopupMenu popup = new PopupMenu(itemView.getContext(), ivServiceMenu);
            popup.getMenuInflater().inflate(R.menu.service_management_menu, popup.getMenu());

            // ✅ Deshabilitar edición/eliminación para servicios básicos
            if (service.getType() == HotelServiceItem.ServiceType.BASIC) {
                popup.getMenu().findItem(R.id.action_edit).setEnabled(false);
                popup.getMenu().findItem(R.id.action_delete).setEnabled(false);
            }

            // ✅ Deshabilitar eliminación para taxi condicional
            if (service.getType() == HotelServiceItem.ServiceType.CONDITIONAL &&
                    service.getName().toLowerCase().contains("taxi")) {
                popup.getMenu().findItem(R.id.action_delete).setEnabled(false);
            }

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    if (listener != null) {
                        listener.onEditService(service, position);
                    }
                    return true;
                } else if (id == R.id.action_delete) {
                    if (listener != null) {
                        listener.onDeleteService(service, position);
                    }
                    return true;
                }
                return false;
            });

            popup.show();
        }
    }
}