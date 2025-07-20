package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceModel;

import java.util.List;

public class ServiceSelectionAdapter extends RecyclerView.Adapter<ServiceSelectionAdapter.ServiceViewHolder> {

    public interface OnServiceSelectedListener {
        void onServiceSelected(String serviceName, boolean isSelected);
    }

    private List<HotelServiceModel> services;
    private List<String> selectedServices;
    private OnServiceSelectedListener listener;

    public ServiceSelectionAdapter(List<HotelServiceModel> services, List<String> selectedServices, OnServiceSelectedListener listener) {
        this.services = services;
        this.selectedServices = selectedServices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_service_selection, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        HotelServiceModel service = services.get(position);
        holder.bind(service);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbService;
        private TextView tvServiceName;
        private TextView tvServiceDescription;
        private ImageView ivServiceIcon;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            cbService = itemView.findViewById(R.id.cbService);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceDescription = itemView.findViewById(R.id.tvServiceDescription);
            ivServiceIcon = itemView.findViewById(R.id.ivServiceIcon);
        }

        void bind(HotelServiceModel service) {
            tvServiceName.setText(service.getName());
            tvServiceDescription.setText(service.getDescription());

            // Configurar icono del servicio
            setupServiceIcon(service);

            // Verificar si está seleccionado
            boolean isSelected = selectedServices.contains(service.getName());
            cbService.setChecked(isSelected);

            // Configurar click listener
            itemView.setOnClickListener(v -> toggleService(service));
            cbService.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) { // Solo procesar si es click del usuario
                    toggleService(service);
                }
            });

            // Estilo visual según selección
            updateItemStyle(isSelected);
        }

        private void setupServiceIcon(HotelServiceModel service) {
            String iconName = service.getIconKey(); // ✅ CORREGIDO: getIconKey() en lugar de getIconName()
            if (iconName == null || iconName.isEmpty()) {
                iconName = "hotel_service_default";
            }

            // Obtener ID del recurso del icono
            int iconResourceId = itemView.getContext().getResources().getIdentifier(
                    "ic_" + iconName,
                    "drawable",
                    itemView.getContext().getPackageName()
            );

            if (iconResourceId != 0) {
                ivServiceIcon.setImageResource(iconResourceId);
            } else {
                ivServiceIcon.setImageResource(R.drawable.ic_hotel_service_default);
            }

            // Color del icono según selección
            boolean isSelected = selectedServices.contains(service.getName());
            int iconColor = isSelected ?
                    ContextCompat.getColor(itemView.getContext(), R.color.green) :
                    ContextCompat.getColor(itemView.getContext(), R.color.text_secondary);
            ivServiceIcon.setColorFilter(iconColor);
        }

        private void toggleService(HotelServiceModel service) {
            String serviceName = service.getName();
            boolean isCurrentlySelected = selectedServices.contains(serviceName);

            if (isCurrentlySelected) {
                selectedServices.remove(serviceName);
                cbService.setChecked(false);
            } else {
                selectedServices.add(serviceName);
                cbService.setChecked(true);
            }

            updateItemStyle(!isCurrentlySelected);
            setupServiceIcon(service); // Actualizar color del icono

            if (listener != null) {
                listener.onServiceSelected(serviceName, !isCurrentlySelected);
            }
        }

        private void updateItemStyle(boolean isSelected) {
            if (isSelected) {
                // Estilo seleccionado
                itemView.setBackgroundResource(R.drawable.bg_chip_green_light);
                tvServiceName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.green));
                tvServiceDescription.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.green));
            } else {
                // Estilo no seleccionado
                itemView.setBackgroundResource(R.drawable.bg_dialog_rounded);
                tvServiceName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
                tvServiceDescription.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
            }
        }
    }

    // ========== MÉTODOS PÚBLICOS ==========

    public void updateServices(List<HotelServiceModel> newServices) {
        this.services.clear();
        this.services.addAll(newServices);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedServices.clear();
        notifyDataSetChanged();
    }

    public List<String> getSelectedServices() {
        return selectedServices;
    }
}