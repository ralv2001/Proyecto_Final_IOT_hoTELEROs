package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.content.Context;
import android.util.Log;
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

/**
 * Adaptador mejorado para la vista previa de servicios en la pantalla principal
 */
public class ServicePreviewAdapter extends RecyclerView.Adapter<ServicePreviewAdapter.ServicePreviewViewHolder> {
    private static final String TAG = "ServicePreviewAdapter";
    private List<HotelService> services;
    private ServiceClickListener listener;

    public ServicePreviewAdapter(List<HotelService> services, ServiceClickListener listener) {
        this.services = services;
        this.listener = listener;
        Log.d(TAG, "Adapter creado con " + (services != null ? services.size() : 0) + " servicios");
    }

    public ServicePreviewAdapter(List<HotelService> services) {
        this(services, null);
    }

    @NonNull
    @Override
    public ServicePreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_preview, parent, false);
        return new ServicePreviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicePreviewViewHolder holder, int position) {
        if (services != null && position < services.size()) {
            HotelService service = services.get(position);
            holder.bind(service, listener);
        }
    }

    @Override
    public int getItemCount() {
        return services != null ? services.size() : 0;
    }

    /**
     * ViewHolder mejorado para elementos de vista previa de servicios
     */
    public static class ServicePreviewViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "ServicePreviewVH";
        private CardView iconContainer;
        private ImageView ivServiceIcon;
        private TextView tvServiceName;
        private TextView tvPriceBadge;
        private View conditionalIndicator;
        private Context context;

        public ServicePreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            iconContainer = itemView.findViewById(R.id.fl_service_icon_container);
            ivServiceIcon = itemView.findViewById(R.id.iv_service_icon);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvPriceBadge = itemView.findViewById(R.id.tv_price_badge);
            conditionalIndicator = itemView.findViewById(R.id.conditional_indicator);

            Log.d(TAG, "ViewHolder creado - Views: " +
                    (iconContainer != null) + ", " + (ivServiceIcon != null) + ", " +
                    (tvServiceName != null));
        }

        /**
         * Configura la vista con los datos del servicio
         */
        public void bind(HotelService service, ServiceClickListener listener) {
            if (service == null) {
                Log.e(TAG, "Servicio null en bind()");
                return;
            }

            Log.d(TAG, "Vinculando servicio: " + service.getName());

            // Configurar nombre del servicio
            if (tvServiceName != null) {
                tvServiceName.setText(service.getName());
            }

            // Configurar icono
            setupServiceIcon(service);

            // Configurar indicadores según el tipo de servicio
            setupServiceIndicators(service);

            // Configurar evento de clic
            setupClickListener(service, listener);
        }

        private void setupServiceIcon(HotelService service) {
            if (ivServiceIcon == null || iconContainer == null) {
                Log.e(TAG, "Views de icono son null");
                return;
            }

            try {
                // Configurar icono del servicio
                String iconName = service.getIconResourceName();
                if (iconName == null || iconName.isEmpty()) {
                    iconName = "ic_hotel_service_default";
                }

                int resourceId = context.getResources().getIdentifier(
                        iconName, "drawable", context.getPackageName());

                if (resourceId > 0) {
                    ivServiceIcon.setImageResource(resourceId);
                    Log.d(TAG, "Icono configurado: " + iconName);
                } else {
                    // Fallback a un icono genérico
                    ivServiceIcon.setImageResource(R.drawable.ic_hotel_service_default);
                    Log.w(TAG, "Icono no encontrado: " + iconName + ", usando default");
                }

                // Configurar color del contenedor según el tipo de servicio
                setupIconContainerColor(service);

            } catch (Exception e) {
                Log.e(TAG, "Error configurando icono: " + e.getMessage());
                ivServiceIcon.setImageResource(R.drawable.ic_hotel_service_default);
            }
        }

        private void setupIconContainerColor(HotelService service) {
            if (iconContainer == null || ivServiceIcon == null) return;

            int backgroundColor;
            int iconTint;

            String serviceType = service.getServiceType();
            switch (serviceType) {
                case "free":
                    backgroundColor = R.color.success_light;
                    iconTint = R.color.success_green;
                    break;
                case "paid":
                    backgroundColor = R.color.orange_light;
                    iconTint = R.color.orange_primary;
                    break;
                case "conditional":
                    backgroundColor = R.color.purple_light;
                    iconTint = R.color.purple_primary;
                    break;
                default:
                    backgroundColor = R.color.orange_light;
                    iconTint = R.color.orange_primary;
                    break;
            }

            try {
                iconContainer.setCardBackgroundColor(
                        ContextCompat.getColor(context, backgroundColor));
                ivServiceIcon.setColorFilter(
                        ContextCompat.getColor(context, iconTint));

                Log.d(TAG, "Colores configurados para tipo: " + serviceType);
            } catch (Exception e) {
                Log.e(TAG, "Error configurando colores: " + e.getMessage());
            }
        }

        private void setupServiceIndicators(HotelService service) {
            // Badge de precio para servicios pagados
            if (tvPriceBadge != null) {
                if (service.getPrice() != null && service.getPrice() > 0 && !service.isFree()) {
                    tvPriceBadge.setVisibility(View.VISIBLE);
                    tvPriceBadge.setText("$");
                } else {
                    tvPriceBadge.setVisibility(View.GONE);
                }
            }

            // Indicador para servicios condicionales
            if (conditionalIndicator != null) {
                if (service.isConditional()) {
                    conditionalIndicator.setVisibility(View.VISIBLE);
                    // Cambiar color según elegibilidad
                    int indicatorColor = service.isEligibleForFree() ?
                            R.color.success_green : R.color.warning_orange;
                    try {
                        conditionalIndicator.setBackgroundColor(
                                ContextCompat.getColor(context, indicatorColor));
                    } catch (Exception e) {
                        Log.e(TAG, "Error configurando indicador condicional: " + e.getMessage());
                    }
                } else {
                    conditionalIndicator.setVisibility(View.GONE);
                }
            }
        }

        private void setupClickListener(HotelService service, ServiceClickListener listener) {
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    Log.d(TAG, "Click en servicio: " + service.getName());
                    listener.onServiceClicked(service);
                } else {
                    Log.d(TAG, "No hay listener configurado para: " + service.getName());
                }
            });
        }
    }
}