package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.HotelService;
import com.example.proyecto_final_hoteleros.client.domain.interfaces.ServiceSelectListener;
import com.example.proyecto_final_hoteleros.client.utils.TaxiConfigManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllServicesAdapter extends RecyclerView.Adapter<AllServicesAdapter.ServiceViewHolder> {

    private static final String TAG = "AllServicesAdapter";

    private List<HotelService> allServices;
    private List<HotelService> filteredServices;
    private Set<String> selectedServices;
    private double currentTotal = 0.0;
    private double taxiMinAmount = 350.0;
    private ServiceSelectListener listener;
    private String currentFilter = "all";

    public AllServicesAdapter(List<HotelService> services, double currentTotal, ServiceSelectListener listener) {
        this.allServices = new ArrayList<>(services);
        this.filteredServices = new ArrayList<>(services);
        this.currentTotal = currentTotal;
        this.listener = listener;
        this.selectedServices = new HashSet<>();

        // ✅ EXTRAER el monto mínimo del taxi si está disponible
        extractTaxiMinAmount();
    }

    /**
     * ✅ Extraer monto mínimo del taxi desde los servicios cargados
     */
    private void extractTaxiMinAmount() {
        for (HotelService service : allServices) {
            if ("taxi".equals(service.getId()) && service.getPrice() != null) {
                taxiMinAmount = service.getPrice();
                Log.d(TAG, "✅ Monto mínimo taxi extraído: S/. " + taxiMinAmount);
                break;
            }
        }
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.client_item_service_detail, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        HotelService service = filteredServices.get(position);
        boolean isSelected = selectedServices.contains(service.getId());
        holder.bind(service, isSelected, currentTotal, taxiMinAmount);
    }

    @Override
    public int getItemCount() {
        return filteredServices.size();
    }

    // ========== MÉTODOS PÚBLICOS ==========

    /**
     * ✅ Actualizar total y recalcular taxi dinámicamente
     */
    public void updateTotalAndRecalculate(double newTotal) {
        this.currentTotal = newTotal;
        updateServiceEligibility();
        notifyDataSetChanged();

        Log.d(TAG, "🔄 Total actualizado: S/. " + newTotal + " - Mínimo taxi: S/. " + taxiMinAmount);
    }

    /**
     * ✅ Configurar monto mínimo del taxi dinámicamente
     */
    public void setTaxiMinAmount(double taxiMinAmount) {
        this.taxiMinAmount = taxiMinAmount;
        updateServiceEligibility();
        Log.d(TAG, "🚕 Monto mínimo taxi actualizado: S/. " + taxiMinAmount);
    }

    /**
     * ✅ Actualizar elegibilidad de servicios condicionales
     */
    private void updateServiceEligibility() {
        for (HotelService service : allServices) {
            if ("taxi".equals(service.getId())) {
                boolean wasEligible = service.isEligibleForFree();
                boolean isEligible = TaxiConfigManager.qualifiesForFreeTaxi(currentTotal, taxiMinAmount);

                service.setEligibleForFree(isEligible);
                service.setConditionalDescription(TaxiConfigManager.getTaxiMessage(currentTotal, taxiMinAmount));

                if (wasEligible != isEligible) {
                    Log.d(TAG, "🚕 Taxi eligibility changed: " + wasEligible + " -> " + isEligible);
                }
            }
        }
    }

    public Set<String> getSelectedServiceIds() {
        return new HashSet<>(selectedServices);
    }

    public void updateServiceSelection(String serviceId, boolean isSelected) {
        if (isSelected) {
            selectedServices.add(serviceId);
        } else {
            selectedServices.remove(serviceId);
        }
        notifyDataSetChanged();
    }

    public void clearSelections() {
        selectedServices.clear();
        notifyDataSetChanged();
    }

    /**
     * ✅ ARREGLADO: Filtrar servicios con DEBUG detallado
     */
    public void filterServices(String filterType) {
        currentFilter = filterType;
        filteredServices.clear();

        Log.d(TAG, "🔍 ========== INICIO FILTRO: " + filterType.toUpperCase() + " ==========");
        Log.d(TAG, "📋 Total de servicios a filtrar: " + allServices.size());

        int matchCount = 0;

        for (HotelService service : allServices) {
            boolean shouldInclude = false;
            String serviceType = service.getServiceType();
            boolean isIncludedInRoom = service.isIncludedInRoom();

            String debugReason = "";

            switch (filterType) {
                case "all":
                    // ✅ Mostrar TODOS los servicios
                    shouldInclude = true;
                    debugReason = "Filtro 'all' - siempre incluir";
                    break;

                case "basic":
                    // ✅ Solo servicios BÁSICOS (todos los cuartos los tienen)
                    shouldInclude = "basic".equals(serviceType);
                    debugReason = shouldInclude ? "Es básico" : "No es básico (tipo: " + serviceType + ")";
                    break;

                case "included":
                    // ✅ Solo servicios INCLUIDOS en este cuarto específico
                    shouldInclude = "included".equals(serviceType) && isIncludedInRoom;
                    debugReason = String.format("Tipo: %s, En cuarto: %s -> %s",
                            serviceType, isIncludedInRoom, shouldInclude ? "INCLUIR" : "EXCLUIR");
                    break;

                case "paid":
                    // ✅ Solo servicios DE PAGO
                    shouldInclude = "paid".equals(serviceType);
                    debugReason = shouldInclude ? "Es de pago" : "No es de pago (tipo: " + serviceType + ")";
                    break;

                case "conditional":
                    // ✅ Solo servicios CONDICIONALES (taxi)
                    shouldInclude = "conditional".equals(serviceType);
                    debugReason = shouldInclude ? "Es condicional (taxi)" : "No es condicional (tipo: " + serviceType + ")";
                    break;
            }

            // ✅ LOG detallado para cada servicio
            String status = shouldInclude ? "✅ INCLUIR" : "❌ EXCLUIR";
            Log.d(TAG, String.format("   📝 %-20s (%s) | Tipo: %-10s | En cuarto: %-5s | %s | %s",
                    service.getName(), service.getId(), serviceType, isIncludedInRoom, status, debugReason));

            if (shouldInclude) {
                filteredServices.add(service);
                matchCount++;
            }
        }

        Log.d(TAG, "📊 RESULTADO: " + matchCount + " servicios coinciden con filtro '" + filterType + "'");

        // ✅ MENSAJE ESPECIAL si filtro "included" está vacío
        if ("included".equals(filterType) && matchCount == 0) {
            Log.w(TAG, "⚠️ WARNING: Filtro 'included' devolvió 0 servicios!");
            Log.w(TAG, "   Esto significa que no hay servicios marcados como incluidos EN ESTE CUARTO específico.");
            Log.w(TAG, "   Revisar el método isServiceIncludedInRoom() y la categorización.");
        }

        Log.d(TAG, "========== FIN FILTRO ==========");

        notifyDataSetChanged();
    }

    // ========== VIEW HOLDER ==========

    public class ServiceViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardService;
        private ImageView ivServiceIcon;
        private TextView tvServiceName;
        private TextView tvServiceDescription;
        private TextView tvServicePrice;
        private TextView tvConditionalBadge;
        private MaterialButton btnToggleService;
        private ImageButton btnServiceInfo;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);

            cardService = itemView.findViewById(R.id.card_service);
            ivServiceIcon = itemView.findViewById(R.id.iv_service_icon);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvServiceDescription = itemView.findViewById(R.id.tv_service_description);
            tvServicePrice = itemView.findViewById(R.id.tv_service_price);
            tvConditionalBadge = itemView.findViewById(R.id.tv_conditional_badge);
            btnToggleService = itemView.findViewById(R.id.btn_toggle_service);
            btnServiceInfo = itemView.findViewById(R.id.btn_service_info);
        }

        public void bind(HotelService service, boolean isSelected, double currentTotal, double taxiMinAmount) {
            // Configurar información básica
            if (tvServiceName != null) tvServiceName.setText(service.getName());
            if (tvServiceDescription != null) tvServiceDescription.setText(service.getDescription());

            // Configurar icono del servicio
            setupServiceIcon(service);

            // Configurar precio según tipo de servicio
            configureServicePrice(service, currentTotal, taxiMinAmount);

            // Configurar badge condicional
            setupConditionalBadge(service);

            // Configurar botón de selección
            configureSelectionButton(service, isSelected);

            // Configurar estado visual de la tarjeta
            updateCardVisualState(isSelected);

            // Configurar botón de información
            setupInfoButton(service);
        }

        private void setupServiceIcon(HotelService service) {
            if (ivServiceIcon == null) return;

            try {
                int resourceId = itemView.getContext().getResources().getIdentifier(
                        service.getIconResourceName(), "drawable", itemView.getContext().getPackageName());

                if (resourceId > 0) {
                    ivServiceIcon.setImageResource(resourceId);
                } else {
                    ivServiceIcon.setImageResource(R.drawable.ic_hotel_service_default);
                }

                // Color del icono según estado
                int iconColor = getServiceIconColor(service);
                ivServiceIcon.setColorFilter(iconColor);

            } catch (Exception e) {
                Log.e(TAG, "Error configurando icono: " + e.getMessage());
                ivServiceIcon.setImageResource(R.drawable.ic_hotel_service_default);
            }
        }

        /**
         * ✅ Configurar precio con monto mínimo dinámico
         */
        private void configureServicePrice(HotelService service, double currentTotal, double taxiMinAmount) {
            if (tvServicePrice == null) return;

            String priceText;
            int priceColor;
            String serviceType = service.getServiceType();

            if ("basic".equals(serviceType)) {
                // ✅ BÁSICOS: Siempre incluidos
                priceText = "✓ Básico";
                priceColor = ContextCompat.getColor(itemView.getContext(), R.color.green);

            } else if ("included".equals(serviceType) && service.isIncludedInRoom()) {
                // ✅ INCLUIDOS: Específicos de este cuarto
                priceText = "✓ Incluido";
                priceColor = ContextCompat.getColor(itemView.getContext(), R.color.green);

            } else if ("conditional".equals(serviceType)) {
                // ✅ TAXI: Mostrar estado según monto DINÁMICO
                boolean isEligible = TaxiConfigManager.qualifiesForFreeTaxi(currentTotal, taxiMinAmount);
                service.setEligibleForFree(isEligible);

                if (isEligible) {
                    priceText = "¡DESBLOQUEADO!";
                    priceColor = ContextCompat.getColor(itemView.getContext(), R.color.green);
                } else {
                    // ✅ USAR EL MONTO MÍNIMO DINÁMICO, NO EL HARDCODED
                    priceText = String.format("Mínimo: S/. %.0f", taxiMinAmount);
                    priceColor = ContextCompat.getColor(itemView.getContext(), R.color.orange_primary);
                }

                Log.d(TAG, "🚕 Taxi price configured: " + priceText +
                        " (Current: S/. " + currentTotal + ", Min: S/. " + taxiMinAmount + ")");

            } else {
                // ✅ SERVICIOS PAGADOS: Mostrar precio
                priceText = service.getPriceDisplay();
                priceColor = ContextCompat.getColor(itemView.getContext(), R.color.orange_primary);
            }

            tvServicePrice.setText(priceText);
            tvServicePrice.setTextColor(priceColor);
        }

        /**
         * ✅ Badge condicional con mensaje dinámico
         */
        private void setupConditionalBadge(HotelService service) {
            if (tvConditionalBadge == null) return;

            if (service.isConditional() && service.getId().equals("taxi")) {
                tvConditionalBadge.setVisibility(View.VISIBLE);

                // ✅ USAR EL MENSAJE DINÁMICO DEL TaxiConfigManager
                String badgeText = TaxiConfigManager.getTaxiMessage(currentTotal, taxiMinAmount);
                tvConditionalBadge.setText(badgeText);

                // Color del badge según elegibilidad
                int badgeColor = service.isEligibleForFree() ?
                        ContextCompat.getColor(itemView.getContext(), R.color.green_light) :
                        ContextCompat.getColor(itemView.getContext(), R.color.orange_light);

                tvConditionalBadge.setBackgroundColor(badgeColor);

                Log.d(TAG, "🚕 Taxi badge: " + badgeText);
            } else {
                tvConditionalBadge.setVisibility(View.GONE);
            }
        }

        /**
         * ✅ Configurar botón de selección con lógica correcta
         */
        private void configureSelectionButton(HotelService service, boolean isSelected) {
            if (btnToggleService == null) return;

            String serviceType = service.getServiceType();

            // ✅ OCULTAR botón para básicos e incluidos (ya están incluidos)
            if ("basic".equals(serviceType) || ("included".equals(serviceType) && service.isIncludedInRoom())) {
                btnToggleService.setVisibility(View.GONE);
                return;
            }

            // ✅ MOSTRAR botón solo para pagados y condicionales
            btnToggleService.setVisibility(View.VISIBLE);

            if ("conditional".equals(serviceType)) {
                // ✅ TAXI: Solo se puede agregar si está desbloqueado
                if (service.isEligibleForFree()) {
                    btnToggleService.setEnabled(true);
                    btnToggleService.setText(isSelected ? "✓ Agregado" : "+ Agregar GRATIS");
                    btnToggleService.setBackgroundColor(ContextCompat.getColor(itemView.getContext(),
                            isSelected ? R.color.green : R.color.orange_primary));
                } else {
                    btnToggleService.setEnabled(false);
                    btnToggleService.setText(String.format("Bloqueado (S/. %.0f)", taxiMinAmount));
                    btnToggleService.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.gray));
                }
            } else {
                // ✅ SERVICIOS PAGADOS: Configuración normal
                btnToggleService.setEnabled(true);
                if (isSelected) {
                    btnToggleService.setText("✓ Seleccionado");
                    btnToggleService.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.green));
                } else {
                    btnToggleService.setText("+ Agregar");
                    btnToggleService.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.orange_primary));
                }
            }

            // ✅ CONFIGURAR click listener solo si está habilitado
            if (btnToggleService.isEnabled()) {
                btnToggleService.setOnClickListener(v -> {
                    boolean newState = !isSelected;

                    // Notificar al listener
                    if (listener != null) {
                        listener.onServiceSelected(service, newState);
                    }

                    // Animar botón
                    animateButtonClick(btnToggleService);
                });
            } else {
                btnToggleService.setOnClickListener(null);
            }
        }

        private void updateCardVisualState(boolean isSelected) {
            if (cardService == null) return;

            if (isSelected) {
                cardService.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.green_light));
                cardService.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.green));
                cardService.setStrokeWidth(4);
            } else {
                cardService.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
                cardService.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.gray_border));
                cardService.setStrokeWidth(1);
            }
        }

        private void setupInfoButton(HotelService service) {
            if (btnServiceInfo == null) return;

            btnServiceInfo.setOnClickListener(v -> showServiceInfoDialog(service));
        }

        private int getServiceIconColor(HotelService service) {
            if (service.isFree()) {
                return ContextCompat.getColor(itemView.getContext(), R.color.green);
            } else if (service.isConditional()) {
                return service.isEligibleForFree() ?
                        ContextCompat.getColor(itemView.getContext(), R.color.green) :
                        ContextCompat.getColor(itemView.getContext(), R.color.orange_primary);
            } else {
                return ContextCompat.getColor(itemView.getContext(), R.color.orange_primary);
            }
        }

        private void animateButtonClick(View button) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f, 1f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setDuration(150);
            animatorSet.start();
        }

        private void showServiceInfoDialog(HotelService service) {
            try {
                Dialog dialog = new Dialog(itemView.getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.client_dialog_service_info);

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }

                // Configurar contenido del diálogo
                TextView tvDialogTitle = dialog.findViewById(R.id.tv_dialog_service_name);
                TextView tvDialogDescription = dialog.findViewById(R.id.tv_dialog_service_description);
                TextView tvDialogFeatures = dialog.findViewById(R.id.tv_dialog_service_features);
                TextView tvDialogAvailability = dialog.findViewById(R.id.tv_dialog_service_availability);
                MaterialButton btnCloseDialog = dialog.findViewById(R.id.btn_close_dialog);

                if (tvDialogTitle != null) tvDialogTitle.setText(service.getName());
                if (tvDialogDescription != null) tvDialogDescription.setText(service.getDescription());

                if (tvDialogFeatures != null) {
                    StringBuilder features = new StringBuilder();
                    for (String feature : service.getFeatures()) {
                        features.append("• ").append(feature).append("\n");
                    }
                    tvDialogFeatures.setText(features.toString().trim());
                }

                if (tvDialogAvailability != null) {
                    tvDialogAvailability.setText("Disponibilidad: " + service.getAvailability());
                }

                if (btnCloseDialog != null) {
                    btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
                }

                dialog.show();

            } catch (Exception e) {
                Log.e(TAG, "Error mostrando diálogo de información: " + e.getMessage());
                Toast.makeText(itemView.getContext(), "Error mostrando información del servicio", Toast.LENGTH_SHORT).show();
            }
        }
    }
}