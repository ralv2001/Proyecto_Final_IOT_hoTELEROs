package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceSelectionAdapter extends RecyclerView.Adapter<ServiceSelectionAdapter.ServiceViewHolder> {

    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    private List<String> availableServices;
    private List<String> selectedServices;
    private Context context;
    private OnSelectionChangedListener selectionChangedListener;
    private Map<String, String> serviceCategories;
    private Map<String, String> serviceIcons;

    public ServiceSelectionAdapter(Context context, List<String> availableServices, List<String> selectedServices) {
        this.context = context;
        this.availableServices = availableServices;
        this.selectedServices = selectedServices;

        initializeServiceData();
    }

    private void initializeServiceData() {
        serviceCategories = new HashMap<>();
        serviceIcons = new HashMap<>();

        // Comodidades de Habitación
        serviceCategories.put("Minibar", "Comodidades");
        serviceIcons.put("Minibar", "minibar");

        serviceCategories.put("Caja Fuerte", "Seguridad");
        serviceIcons.put("Caja Fuerte", "security");

        serviceCategories.put("Secador de Cabello", "Comodidades");
        serviceIcons.put("Secador de Cabello", "hair_dryer");

        serviceCategories.put("Plancha y Tabla de Planchar", "Comodidades");
        serviceIcons.put("Plancha y Tabla de Planchar", "iron");

        serviceCategories.put("Artículos de Aseo Premium", "Comodidades");
        serviceIcons.put("Artículos de Aseo Premium", "toiletries");

        serviceCategories.put("Batas y Pantuflas", "Comodidades");
        serviceIcons.put("Batas y Pantuflas", "bathrobe");

        // Vistas y Espacios
        serviceCategories.put("Balcón", "Espacios");
        serviceIcons.put("Balcón", "balcony");

        serviceCategories.put("Terraza", "Espacios");
        serviceIcons.put("Terraza", "terrace");

        serviceCategories.put("Vista al Mar", "Vistas");
        serviceIcons.put("Vista al Mar", "sea_view");

        serviceCategories.put("Vista a la Ciudad", "Vistas");
        serviceIcons.put("Vista a la Ciudad", "city_view");

        serviceCategories.put("Vista al Jardín", "Vistas");
        serviceIcons.put("Vista al Jardín", "garden_view");

        // Espacios Adicionales
        serviceCategories.put("Sala de Estar", "Espacios");
        serviceIcons.put("Sala de Estar", "living_room");

        serviceCategories.put("Escritorio", "Espacios");
        serviceIcons.put("Escritorio", "desk");

        serviceCategories.put("Comedor", "Espacios");
        serviceIcons.put("Comedor", "dining");

        serviceCategories.put("Kitchenette", "Espacios");
        serviceIcons.put("Kitchenette", "kitchenette");

        serviceCategories.put("Cocina Equipada", "Espacios");
        serviceIcons.put("Cocina Equipada", "kitchen");

        // Relajación y Bienestar
        serviceCategories.put("Bañera de Hidromasaje", "Bienestar");
        serviceIcons.put("Bañera de Hidromasaje", "jacuzzi");

        serviceCategories.put("Jacuzzi", "Bienestar");
        serviceIcons.put("Jacuzzi", "spa");

        serviceCategories.put("Chimenea", "Bienestar");
        serviceIcons.put("Chimenea", "fireplace");

        // Servicios Premium
        serviceCategories.put("Mayordomo Personal", "Premium");
        serviceIcons.put("Mayordomo Personal", "concierge");

        serviceCategories.put("Servicio de Habitaciones 24h", "Premium");
        serviceIcons.put("Servicio de Habitaciones 24h", "room_service");

        serviceCategories.put("Servicio de Lavandería", "Premium");
        serviceIcons.put("Servicio de Lavandería", "laundry");

        serviceCategories.put("Sala de Reuniones", "Premium");
        serviceIcons.put("Sala de Reuniones", "meeting");

        // Extras
        serviceCategories.put("Almohadas Adicionales", "Extras");
        serviceIcons.put("Almohadas Adicionales", "pillow");

        serviceCategories.put("Servicio de Despertador", "Extras");
        serviceIcons.put("Servicio de Despertador", "alarm");
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_service_selection, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        String service = availableServices.get(position);
        holder.bind(service);
    }

    @Override
    public int getItemCount() {
        return availableServices.size();
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        private CardView cardService;
        private View selectionIndicator;
        private View iconBackground;
        private ImageView ivServiceIcon;
        private TextView tvServiceName;
        private TextView tvServiceCategory;
        private CheckBox cbService;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardService = itemView.findViewById(R.id.cardService);
            selectionIndicator = itemView.findViewById(R.id.selectionIndicator);
            iconBackground = itemView.findViewById(R.id.iconBackground);
            ivServiceIcon = itemView.findViewById(R.id.ivServiceIcon);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceCategory = itemView.findViewById(R.id.tvServiceCategory);
            cbService = itemView.findViewById(R.id.cbService);
        }

        public void bind(String service) {
            // Configurar textos
            tvServiceName.setText(service);

            String category = serviceCategories.get(service);
            if (category != null) {
                tvServiceCategory.setText(category);
                tvServiceCategory.setVisibility(View.VISIBLE);
            } else {
                tvServiceCategory.setText("General");
                tvServiceCategory.setVisibility(View.VISIBLE);
            }

            // Configurar icono
            String iconKey = serviceIcons.get(service);
            if (iconKey != null) {
                int iconResource = IconHelper.getIconResource(iconKey);
                ivServiceIcon.setImageResource(iconResource);
            } else {
                ivServiceIcon.setImageResource(R.drawable.ic_hotel_service_default);
            }

            // Configurar estado de selección
            boolean isSelected = selectedServices.contains(service);
            updateSelectionUI(isSelected);
            cbService.setChecked(isSelected);

            // Listeners
            cbService.setOnCheckedChangeListener(null); // Evitar loops
            cbService.setOnCheckedChangeListener((buttonView, isChecked) -> {
                handleSelectionChange(service, isChecked);
            });

            itemView.setOnClickListener(v -> {
                boolean newState = !selectedServices.contains(service);
                cbService.setChecked(newState);
                handleSelectionChange(service, newState);
            });
        }

        private void handleSelectionChange(String service, boolean isSelected) {
            if (isSelected) {
                if (!selectedServices.contains(service)) {
                    selectedServices.add(service);
                }
            } else {
                selectedServices.remove(service);
            }

            updateSelectionUI(isSelected);

            // Notificar al listener si existe
            if (selectionChangedListener != null) {
                selectionChangedListener.onSelectionChanged();
            }
        }

        private void updateSelectionUI(boolean isSelected) {
            if (isSelected) {
                selectionIndicator.setVisibility(View.VISIBLE);
                iconBackground.setBackgroundResource(R.drawable.bg_circle_green_light);
                cardService.setCardElevation(6f);
            } else {
                selectionIndicator.setVisibility(View.GONE);
                iconBackground.setBackgroundResource(R.drawable.bg_circle_orange_light);
                cardService.setCardElevation(2f);
            }
        }
    }
}