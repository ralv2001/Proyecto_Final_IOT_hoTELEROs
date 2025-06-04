package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.HotelService;
import com.example.proyecto_final_hoteleros.client.domain.interfaces.ServiceSelectListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllServicesAdapter extends RecyclerView.Adapter<AllServicesAdapter.ServiceViewHolder> {

    private static final String TAG = "AllServicesAdapter";
    private List<HotelService> services;
    private List<HotelService> filteredServices;
    private double currentTotal;
    private Set<String> selectedServiceIds = new HashSet<>();
    private ServiceSelectListener listener;
    private Context context;
    private static final double TAXI_MIN_AMOUNT = 350.0;

    public AllServicesAdapter(List<HotelService> services, double currentTotal, ServiceSelectListener listener) {
        this.services = services != null ? services : new ArrayList<>();
        this.filteredServices = new ArrayList<>(this.services);
        this.currentTotal = currentTotal;
        this.listener = listener;
        updateServiceEligibility();
        Log.d(TAG, "Adapter creado con " + this.services.size() + " servicios");
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_service_detail, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        if (position < 0 || position >= filteredServices.size()) {
            Log.e(TAG, "Posición inválida: " + position);
            return;
        }

        HotelService service = filteredServices.get(position);
        boolean isSelected = selectedServiceIds.contains(service.getId());

        holder.bind(service, isSelected, currentTotal);
        animateItemEntry(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return filteredServices.size();
    }

    private void animateItemEntry(View view, int position) {
        view.setAlpha(0f);
        view.setTranslationY(30f);

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        ObjectAnimator translateAnimator = ObjectAnimator.ofFloat(view, "translationY", 30f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alphaAnimator, translateAnimator);
        animatorSet.setDuration(400);
        animatorSet.setStartDelay(position * 50L);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.start();
    }

    private void updateServiceEligibility() {
        for (HotelService service : services) {
            if (service.getId().equals("taxi")) {
                service.setEligibleForFree(currentTotal >= TAXI_MIN_AMOUNT);
                Log.d(TAG, "Taxi eligibility updated: " + service.isEligibleForFree());
            }
        }
    }

    public void filterServices(String filterType) {
        filteredServices.clear();

        if ("all".equals(filterType)) {
            filteredServices.addAll(services);
        } else {
            for (HotelService service : services) {
                if (service.getServiceType().equals(filterType)) {
                    filteredServices.add(service);
                }
            }
        }

        Log.d(TAG, "Filtro aplicado: " + filterType + ", servicios filtrados: " + filteredServices.size());
        notifyDataSetChanged();
    }

    public void updateServiceSelection(String serviceId, boolean isSelected) {
        if (isSelected) {
            selectedServiceIds.add(serviceId);
        } else {
            selectedServiceIds.remove(serviceId);
        }
        Log.d(TAG, "Servicio " + serviceId + " " + (isSelected ? "seleccionado" : "deseleccionado"));
        notifyDataSetChanged();
    }

    public Set<String> getSelectedServiceIds() {
        return selectedServiceIds;
    }

    public void clearSelections() {
        selectedServiceIds.clear();
        notifyDataSetChanged();
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardService;
        private CardView iconContainer;
        private ImageView ivServiceIcon;
        private TextView tvServiceName;
        private TextView tvServiceDescription;
        private TextView tvServiceFeatures;
        private TextView tvConditionalBadge;
        private TextView tvServicePrice;
        private MaterialButton btnAddToCart;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardService = itemView.findViewById(R.id.card_service);
            iconContainer = itemView.findViewById(R.id.fl_service_detail_container);
            ivServiceIcon = itemView.findViewById(R.id.iv_service_detail_icon);
            tvServiceName = itemView.findViewById(R.id.tv_service_detail_name);
            tvServiceDescription = itemView.findViewById(R.id.tv_service_detail_description);
            tvServiceFeatures = itemView.findViewById(R.id.tv_service_features);
            tvServicePrice = itemView.findViewById(R.id.tv_service_detail_price);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
            tvConditionalBadge = itemView.findViewById(R.id.tv_service_conditional_hint);

            Log.d(TAG, "ViewHolder creado - Views encontradas: " +
                    (cardService != null) + ", " + (btnAddToCart != null));
        }

        public void bind(HotelService service, boolean isSelected, double currentTotal) {
            if (service == null) {
                Log.e(TAG, "Servicio null en bind()");
                return;
            }

            // Configurar información básica
            tvServiceName.setText(service.getName());
            tvServiceDescription.setText(service.getDescription());

            // Configurar icono
            setupServiceIcon(service);

            // Configurar tipo de servicio
            setupServiceType(service, isSelected);

            // Configurar badge condicional
            setupConditionalBadge(service);

            // Configurar características
            setupFeatures(service);

            // Configurar eventos
            setupClickListeners(service);

            // Configurar estado de selección
            updateSelectionState(isSelected);

            Log.d(TAG, "Servicio vinculado: " + service.getName() + ", seleccionado: " + isSelected);
        }

        private void setupServiceIcon(HotelService service) {
            try {
                int resourceId = context.getResources().getIdentifier(
                        service.getIconResourceName(), "drawable", context.getPackageName());

                if (resourceId > 0) {
                    ivServiceIcon.setImageResource(resourceId);
                } else {
                    ivServiceIcon.setImageResource(R.drawable.ic_hotel_service_default);
                    Log.w(TAG, "Icono no encontrado para: " + service.getIconResourceName());
                }

                // Configurar fondo del contenedor según tipo
                setupIconBackground(service);
            } catch (Exception e) {
                Log.e(TAG, "Error configurando icono: " + e.getMessage());
                ivServiceIcon.setImageResource(R.drawable.ic_hotel_service_default);
            }
        }

        private void setupIconBackground(HotelService service) {
            int backgroundRes;
            switch (service.getServiceType()) {
                case "free":
                    backgroundRes = R.color.success_light;
                    break;
                case "paid":
                    backgroundRes = R.color.orange_light;
                    break;
                case "conditional":
                    backgroundRes = R.color.purple_light;
                    break;
                default:
                    backgroundRes = R.color.light_gray;
                    break;
            }
            iconContainer.setCardBackgroundColor(ContextCompat.getColor(context, backgroundRes));
        }

        private void setupServiceType(HotelService service, boolean isSelected) {
            String serviceType = service.getServiceType();

            switch (serviceType) {
                case "free":
                    tvServicePrice.setText("✓ Incluido");
                    tvServicePrice.setTextColor(ContextCompat.getColor(context, R.color.success_green));
                    tvServicePrice.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_price_free));
                    if (btnAddToCart != null) {
                        btnAddToCart.setVisibility(View.GONE);
                    }
                    break;

                case "paid":
                    tvServicePrice.setText(service.getPriceDisplay());
                    tvServicePrice.setTextColor(ContextCompat.getColor(context, R.color.orange_primary));
                    tvServicePrice.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_price_container));
                    if (btnAddToCart != null) {
                        btnAddToCart.setVisibility(View.VISIBLE);
                        btnAddToCart.setText(isSelected ? "Quitar" : "Añadir");
                        btnAddToCart.setEnabled(true);
                        btnAddToCart.setAlpha(1f);
                    }
                    break;

                case "conditional":
                    if (service.isEligibleForFree()) {
                        tvServicePrice.setText("¡GRATIS!");
                        tvServicePrice.setTextColor(ContextCompat.getColor(context, R.color.success_green));
                        tvServicePrice.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_price_free));
                        if (btnAddToCart != null) {
                            btnAddToCart.setText("Incluido");
                            btnAddToCart.setEnabled(false);
                            btnAddToCart.setAlpha(0.6f);
                        }
                    } else {
                        tvServicePrice.setText(service.getPriceDisplay());
                        tvServicePrice.setTextColor(ContextCompat.getColor(context, R.color.orange_primary));
                        tvServicePrice.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_price_container));
                        if (btnAddToCart != null) {
                            btnAddToCart.setVisibility(View.VISIBLE);
                            btnAddToCart.setText(isSelected ? "Quitar" : "Añadir");
                            btnAddToCart.setEnabled(true);
                            btnAddToCart.setAlpha(1f);
                        }
                    }
                    break;
            }
        }

        private void setupConditionalBadge(HotelService service) {
            if (tvConditionalBadge != null && service.isConditional()) {
                tvConditionalBadge.setVisibility(View.VISIBLE);
                tvConditionalBadge.setText(service.getConditionalBadgeText());
            } else if (tvConditionalBadge != null) {
                tvConditionalBadge.setVisibility(View.GONE);
            }
        }

        private void setupFeatures(HotelService service) {
            if (tvServiceFeatures != null) {
                String availability = service.getAvailability();
                if (availability != null && !availability.isEmpty()) {
                    tvServiceFeatures.setText("Disponible " + availability);
                    tvServiceFeatures.setVisibility(View.VISIBLE);
                } else {
                    tvServiceFeatures.setVisibility(View.GONE);
                }
            }
        }

        private void setupClickListeners(HotelService service) {
            if (btnAddToCart != null) {
                btnAddToCart.setOnClickListener(v -> {
                    try {
                        if (btnAddToCart.isEnabled()) {
                            boolean newState = !selectedServiceIds.contains(service.getId());

                            // Animación de botón
                            animateButtonPress(btnAddToCart);

                            if (listener != null) {
                                listener.onServiceSelected(service, newState);
                                Log.d(TAG, "Listener ejecutado para servicio: " + service.getId());
                            } else {
                                Log.w(TAG, "Listener es null");
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error en click listener: " + e.getMessage());
                    }
                });
            }
        }

        private void updateSelectionState(boolean isSelected) {
            if (cardService != null) {
                if (isSelected) {
                    cardService.setStrokeColor(ContextCompat.getColor(context, R.color.orange_primary));
                    cardService.setStrokeWidth(3);
                    cardService.setCardElevation(6f);
                } else {
                    cardService.setStrokeColor(ContextCompat.getColor(context, android.R.color.transparent));
                    cardService.setStrokeWidth(0);
                    cardService.setCardElevation(3f);
                }
            }
        }

        private void animateButtonPress(View button) {
            try {
                ObjectAnimator scaleDown = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f);
                ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f);
                ObjectAnimator scaleUp = ObjectAnimator.ofFloat(button, "scaleX", 0.95f, 1f);
                ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.95f, 1f);

                AnimatorSet scaleDownSet = new AnimatorSet();
                scaleDownSet.playTogether(scaleDown, scaleDownY);
                scaleDownSet.setDuration(100);

                AnimatorSet scaleUpSet = new AnimatorSet();
                scaleUpSet.playTogether(scaleUp, scaleUpY);
                scaleUpSet.setDuration(100);

                AnimatorSet fullSet = new AnimatorSet();
                fullSet.playSequentially(scaleDownSet, scaleUpSet);
                fullSet.start();
            } catch (Exception e) {
                Log.e(TAG, "Error en animación: " + e.getMessage());
            }
        }
    }
}