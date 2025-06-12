package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.HotelService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServicesViewOnlyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_SERVICE = 1;

    private List<Object> items = new ArrayList<>();

    public ServicesViewOnlyAdapter(Map<HotelService.ServiceCategory, List<HotelService>> servicesByCategory) {
        setupItems(servicesByCategory);
    }

    private void setupItems(Map<HotelService.ServiceCategory, List<HotelService>> servicesByCategory) {
        items.clear();

        // Orden específico de categorías
        HotelService.ServiceCategory[] orderedCategories = {
                HotelService.ServiceCategory.ESSENTIALS,
                HotelService.ServiceCategory.COMFORT,
                HotelService.ServiceCategory.WELLNESS,
                HotelService.ServiceCategory.GASTRONOMY,
                HotelService.ServiceCategory.BUSINESS,
                HotelService.ServiceCategory.TRANSPORT
        };

        for (HotelService.ServiceCategory category : orderedCategories) {
            List<HotelService> services = servicesByCategory.get(category);
            if (services != null && !services.isEmpty()) {
                items.add(category); // Header
                items.addAll(services); // Services
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof HotelService.ServiceCategory ? TYPE_HEADER : TYPE_SERVICE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.client_item_service_category_header, parent, false);
            return new CategoryHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.client_item_service_view_only, parent, false);
            return new ServiceViewOnlyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        if (holder instanceof CategoryHeaderViewHolder && item instanceof HotelService.ServiceCategory) {
            ((CategoryHeaderViewHolder) holder).bind((HotelService.ServiceCategory) item);
        } else if (holder instanceof ServiceViewOnlyViewHolder && item instanceof HotelService) {
            ((ServiceViewOnlyViewHolder) holder).bind((HotelService) item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder para headers de categoría
    static class CategoryHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategoryName;
        private TextView tvCategoryDescription;

        public CategoryHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvCategoryDescription = itemView.findViewById(R.id.tv_category_description);
        }

        public void bind(HotelService.ServiceCategory category) {
            tvCategoryName.setText(category.getDisplayName());

            String description = getCategoryDescription(category);
            tvCategoryDescription.setText(description);
        }

        private String getCategoryDescription(HotelService.ServiceCategory category) {
            switch (category) {
                case ESSENTIALS:
                    return "Servicios básicos incluidos en tu estadía";
                case COMFORT:
                    return "Servicios para mayor comodidad";
                case WELLNESS:
                    return "Servicios de relajación y bienestar";
                case GASTRONOMY:
                    return "Experiencias gastronómicas";
                case BUSINESS:
                    return "Servicios para viajes de negocios";
                case TRANSPORT:
                    return "Servicios de transporte especializado";
                default:
                    return "";
            }
        }
    }

    // ViewHolder para servicios (solo visualización)
    static class ServiceViewOnlyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivServiceIcon;
        private TextView tvServiceName;
        private TextView tvServiceDescription;
        private TextView tvServiceFeatures;
        private ImageView ivHasPhotos;

        public ServiceViewOnlyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServiceIcon = itemView.findViewById(R.id.iv_service_icon);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvServiceDescription = itemView.findViewById(R.id.tv_service_description);
            tvServiceFeatures = itemView.findViewById(R.id.tv_service_features);
            ivHasPhotos = itemView.findViewById(R.id.iv_has_photos);
        }

        public void bind(HotelService service) {
            tvServiceName.setText(service.getName());
            tvServiceDescription.setText(service.getDescription());

            // Mostrar características
            if (service.getFeatures() != null && !service.getFeatures().isEmpty()) {
                String features = "• " + String.join("\n• ", service.getFeatures());
                tvServiceFeatures.setText(features);
                tvServiceFeatures.setVisibility(View.VISIBLE);
            } else {
                tvServiceFeatures.setVisibility(View.GONE);
            }

            // Configurar icono
            try {
                int resourceId = itemView.getContext().getResources().getIdentifier(
                        service.getIconResourceName(), "drawable",
                        itemView.getContext().getPackageName());
                ivServiceIcon.setImageResource(resourceId > 0 ? resourceId : R.drawable.ic_hotel_service_default);
            } catch (Exception e) {
                ivServiceIcon.setImageResource(R.drawable.ic_hotel_service_default);
            }

            // Mostrar indicador de fotos
            boolean hasPhotos = service.getImageUrls() != null && !service.getImageUrls().isEmpty();
            ivHasPhotos.setVisibility(hasPhotos ? View.VISIBLE : View.GONE);
        }
    }
}