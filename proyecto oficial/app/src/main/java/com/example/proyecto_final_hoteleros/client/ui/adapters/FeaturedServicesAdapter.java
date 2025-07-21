package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.HotelService;
import com.example.proyecto_final_hoteleros.client.domain.interfaces.ServiceClickListener;
import java.util.List;

public class FeaturedServicesAdapter extends RecyclerView.Adapter<FeaturedServicesAdapter.FeaturedServiceViewHolder> {

    private List<HotelService> services;
    private ServiceClickListener listener;
    private Context context;

    public FeaturedServicesAdapter(List<HotelService> services, ServiceClickListener listener) {
        this.services = services;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FeaturedServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.client_item_featured_service, parent, false);
        return new FeaturedServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedServiceViewHolder holder, int position) {
        HotelService service = services.get(position);
        holder.bind(service, position);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    class FeaturedServiceViewHolder extends RecyclerView.ViewHolder {
        private CardView iconContainer;
        private ImageView ivServiceIcon;
        private TextView tvServiceName;
        private View conditionalIndicator;
        private ImageView ivConditionalIcon;
        private TextView tvStatusBadge;

        public FeaturedServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            iconContainer = itemView.findViewById(R.id.icon_container);
            ivServiceIcon = itemView.findViewById(R.id.iv_service_icon);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            conditionalIndicator = itemView.findViewById(R.id.conditional_indicator);
            ivConditionalIcon = itemView.findViewById(R.id.iv_conditional_icon);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
        }

        public void bind(HotelService service, int position) {
            tvServiceName.setText(service.getName());

            // Configurar icono
            setupServiceIcon(service);

            // ✅ CONFIGURAR INDICADORES CON LÓGICA CORREGIDA
            setupServiceIndicatorsFixed(service);

            // Configurar colores según tipo
            setupServiceColors(service);

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onServiceClicked(service);
                }
            });
        }

        private void setupServiceIcon(HotelService service) {
            try {
                int resourceId = context.getResources().getIdentifier(
                        service.getIconResourceName(), "drawable", context.getPackageName());
                ivServiceIcon.setImageResource(resourceId > 0 ? resourceId : R.drawable.ic_hotel_service_default);
            } catch (Exception e) {
                ivServiceIcon.setImageResource(R.drawable.ic_hotel_service_default);
            }
        }

        // ✅ MÉTODO CORREGIDO: Lógica para mostrar etiquetas según tipo de servicio
        private void setupServiceIndicatorsFixed(HotelService service) {
            // ✅ OBTENER EL TIPO DE SERVICIO CORRECTAMENTE
            String serviceType = getServiceTypeFromService(service);

            if (service.isConditional()) {
                // Servicios condicionales (ej: taxi)
                conditionalIndicator.setVisibility(View.VISIBLE);
                ivConditionalIcon.setVisibility(View.VISIBLE);

                if (service.isEligibleForFree()) {
                    // Gratis por condición cumplida
                    ivConditionalIcon.setImageResource(R.drawable.ic_star);
                    ivConditionalIcon.setColorFilter(ContextCompat.getColor(context, R.color.success_green));
                    conditionalIndicator.setBackgroundTintList(
                            ContextCompat.getColorStateList(context, R.color.success_green));

                    tvStatusBadge.setVisibility(View.VISIBLE);
                    tvStatusBadge.setText("¡GRATIS!");
                    tvStatusBadge.setBackgroundTintList(
                            ContextCompat.getColorStateList(context, R.color.success_green));
                } else {
                    // Condicional pero no cumple condición
                    ivConditionalIcon.setImageResource(R.drawable.ic_info);
                    ivConditionalIcon.setColorFilter(ContextCompat.getColor(context, R.color.warning_orange));
                    conditionalIndicator.setBackgroundTintList(
                            ContextCompat.getColorStateList(context, R.color.warning_orange));

                    tvStatusBadge.setVisibility(View.VISIBLE);
                    tvStatusBadge.setText("ESPECIAL");
                    tvStatusBadge.setBackgroundTintList(
                            ContextCompat.getColorStateList(context, R.color.warning_orange));
                }
            } else if ("basic".equals(serviceType)) {
                // ✅ SERVICIOS BÁSICOS → Etiqueta "Básico"
                tvStatusBadge.setVisibility(View.VISIBLE);
                tvStatusBadge.setText("Básico");
                tvStatusBadge.setBackgroundTintList(
                        ContextCompat.getColorStateList(context, R.color.success_green));

                conditionalIndicator.setVisibility(View.GONE);
                ivConditionalIcon.setVisibility(View.GONE);
            } else if ("included".equals(serviceType) || service.isIncludedInRoom()) {
                // ✅ SERVICIOS INCLUIDOS → Etiqueta "Incluido"
                tvStatusBadge.setVisibility(View.VISIBLE);
                tvStatusBadge.setText("Incluido");
                tvStatusBadge.setBackgroundTintList(
                        ContextCompat.getColorStateList(context, R.color.success_green));

                conditionalIndicator.setVisibility(View.GONE);
                ivConditionalIcon.setVisibility(View.GONE);
            } else if (service.isFree()) {
                // ✅ OTROS SERVICIOS GRATUITOS → Etiqueta "Básico" por defecto
                tvStatusBadge.setVisibility(View.VISIBLE);
                tvStatusBadge.setText("Básico");
                tvStatusBadge.setBackgroundTintList(
                        ContextCompat.getColorStateList(context, R.color.success_green));

                conditionalIndicator.setVisibility(View.GONE);
                ivConditionalIcon.setVisibility(View.GONE);
            } else {
                // Servicios de pago - sin etiqueta
                conditionalIndicator.setVisibility(View.GONE);
                ivConditionalIcon.setVisibility(View.GONE);
                tvStatusBadge.setVisibility(View.GONE);
            }
        }

        // ✅ MÉTODO PARA DETERMINAR EL TIPO DE SERVICIO CORRECTAMENTE
        private String getServiceTypeFromService(HotelService service) {
            // 1. Si el servicio tiene una categoría específica, usarla
            if (service.getCategory() != null) {
                HotelService.ServiceCategory category = service.getCategory();
                if (category == HotelService.ServiceCategory.ROOM_INCLUDED) {
                    return "included";
                } else if (category == HotelService.ServiceCategory.ESSENTIALS) {
                    return "basic";
                }
            }

            // 2. Si no tiene categoría, usar el tipo estándar
            String standardType = service.getServiceType();
            if ("room_included".equals(standardType)) {
                return "included";
            } else if ("free".equals(standardType)) {
                return "basic";
            }

            // 3. Fallback: Para servicios creados desde Firebase con serviceType "basic"
            // (Como los que se están cargando en HotelDetailFragment)
            // Si es gratuito pero no está marcado como incluido en habitación, es básico
            if (service.isFree() && !service.isIncludedInRoom()) {
                return "basic";
            }

            return standardType; // "conditional", "paid", etc.
        }

        private void setupServiceColors(HotelService service) {
            int backgroundColor;
            int iconTint;

            if (service.isConditional()) {
                if (service.isEligibleForFree()) {
                    backgroundColor = R.color.success_light;
                    iconTint = R.color.success_green;
                } else {
                    backgroundColor = R.color.warning_light;
                    iconTint = R.color.warning_orange;
                }
            } else if (service.isFree()) {
                // ✅ SERVICIOS GRATUITOS (tanto básicos como incluidos)
                backgroundColor = R.color.success_light;
                iconTint = R.color.success_green;
            } else {
                backgroundColor = R.color.orange_light;
                iconTint = R.color.orange_primary;
            }

            iconContainer.setCardBackgroundColor(ContextCompat.getColor(context, backgroundColor));
            ivServiceIcon.setColorFilter(ContextCompat.getColor(context, iconTint));
        }
    }
}