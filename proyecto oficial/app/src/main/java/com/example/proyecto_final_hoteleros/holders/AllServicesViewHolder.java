package com.example.proyecto_final_hoteleros.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.model.HotelService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * ViewHolder para elementos de la lista detallada de servicios del hotel
 * Actualizado para el nuevo layout mejorado
 */
public class AllServicesViewHolder extends RecyclerView.ViewHolder {

    // Views principales
    private MaterialCardView cardService;
    private CardView iconContainer;
    private ImageView ivServiceImage;
    private TextView tvServiceName;
    private TextView tvServiceDescription;
    private TextView tvServicePrice;
    private TextView tvServiceFeatures;
    private MaterialButton btnAddToCart;
    private TextView tvServiceConditionalHint;
    private View dividerService;

    public AllServicesViewHolder(@NonNull View itemView) {
        super(itemView);

        // Inicializar views según el nuevo layout
        cardService = itemView.findViewById(R.id.card_service);
        iconContainer = itemView.findViewById(R.id.fl_service_detail_container);
        ivServiceImage = itemView.findViewById(R.id.iv_service_detail_icon);
        tvServiceName = itemView.findViewById(R.id.tv_service_detail_name);
        tvServiceDescription = itemView.findViewById(R.id.tv_service_detail_description);
        tvServicePrice = itemView.findViewById(R.id.tv_service_detail_price);
        tvServiceFeatures = itemView.findViewById(R.id.tv_service_features);
        btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        tvServiceConditionalHint = itemView.findViewById(R.id.tv_service_conditional_hint);
        dividerService = itemView.findViewById(R.id.divider_service);
    }

    /**
     * Configura la vista con los datos del servicio
     */
    public void bind(HotelService service, boolean isSelected, double currentTotal, double taxiMinAmount) {
        if (service == null) return;

        // Configurar información básica
        tvServiceName.setText(service.getName());
        tvServiceDescription.setText(service.getDescription());

        // Configurar icono
        setupServiceIcon(service);

        // Configurar características del servicio
        setupServiceFeatures(service);

        // Configurar según el tipo de servicio
        if (service.isConditional() && service.getId().equals("taxi")) {
            configureConditionalService(service, currentTotal, taxiMinAmount);
        } else if (!service.isFree()) {
            configurePaidService(service, isSelected);
        } else {
            configureFreeService();
        }

        // Configurar badge condicional
        setupConditionalBadge(service);

        // Configurar estado de selección
        updateSelectionState(isSelected);
    }

    private void setupServiceIcon(HotelService service) {
        if (ivServiceImage == null || iconContainer == null) return;

        try {
            // Configurar icono
            int resourceId = itemView.getContext().getResources().getIdentifier(
                    service.getIconResourceName(), "drawable", itemView.getContext().getPackageName());
            ivServiceImage.setImageResource(resourceId > 0 ? resourceId : R.drawable.ic_hotel_service_default);

            // Configurar color del contenedor según tipo
            setupIconBackground(service);
        } catch (Exception e) {
            ivServiceImage.setImageResource(R.drawable.ic_hotel_service_default);
        }
    }

    private void setupIconBackground(HotelService service) {
        if (iconContainer == null) return;

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
        iconContainer.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), backgroundRes));
    }

    private void setupServiceFeatures(HotelService service) {
        if (tvServiceFeatures == null) return;

        String availability = service.getAvailability();
        if (availability != null && !availability.isEmpty()) {
            tvServiceFeatures.setText("Disponible " + availability);
            tvServiceFeatures.setVisibility(View.VISIBLE);
        } else {
            tvServiceFeatures.setVisibility(View.GONE);
        }
    }

    private void setupConditionalBadge(HotelService service) {
        if (tvServiceConditionalHint == null) return;

        if (service.isConditional()) {
            tvServiceConditionalHint.setVisibility(View.VISIBLE);
            tvServiceConditionalHint.setText(service.getConditionalBadgeText());
        } else {
            tvServiceConditionalHint.setVisibility(View.GONE);
        }
    }

    /**
     * Configura la visualización de un servicio condicional
     */
    private void configureConditionalService(HotelService service, double currentTotal, double taxiMinAmount) {
        boolean isFree = currentTotal >= taxiMinAmount;

        if (isFree) {
            tvServicePrice.setText("¡GRATIS!");
            tvServicePrice.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.success_green));

            if (btnAddToCart != null) {
                btnAddToCart.setText("Incluido");
                btnAddToCart.setEnabled(false);
                btnAddToCart.setAlpha(0.6f);
            }
        } else {
            tvServicePrice.setText(String.format("S/. %.2f", service.getPrice()));
            tvServicePrice.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.orange_primary));

            if (btnAddToCart != null) {
                btnAddToCart.setVisibility(View.VISIBLE);
                btnAddToCart.setEnabled(true);
                btnAddToCart.setAlpha(1f);
            }
        }
    }

    /**
     * Configura la visualización de un servicio pagado
     */
    private void configurePaidService(HotelService service, boolean isSelected) {
        tvServicePrice.setText(String.format("S/. %.2f", service.getPrice()));
        tvServicePrice.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.orange_primary));

        if (btnAddToCart != null) {
            btnAddToCart.setVisibility(View.VISIBLE);
            btnAddToCart.setEnabled(true);
            btnAddToCart.setAlpha(1f);
            updateButtonState(isSelected);
        }
    }

    /**
     * Configura la visualización de un servicio gratuito
     */
    private void configureFreeService() {
        tvServicePrice.setText("✓ Incluido");
        tvServicePrice.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.success_green));

        if (btnAddToCart != null) {
            btnAddToCart.setVisibility(View.GONE);
        }
    }

    /**
     * Actualiza el estado visual del botón según la selección
     */
    public void updateButtonState(boolean isSelected) {
        if (btnAddToCart == null) return;

        btnAddToCart.setText(isSelected ? "Quitar" : "Añadir");

        int colorRes = isSelected ? R.color.orange_accent : R.color.orange_primary;
        btnAddToCart.setBackgroundTintList(
                ContextCompat.getColorStateList(itemView.getContext(), colorRes));
    }

    /**
     * Actualiza el estado de selección de la tarjeta
     */
    private void updateSelectionState(boolean isSelected) {
        if (cardService == null) return;

        if (isSelected) {
            cardService.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.orange_primary));
            cardService.setStrokeWidth(3);
            cardService.setCardElevation(6f);
        } else {
            cardService.setStrokeColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
            cardService.setStrokeWidth(0);
            cardService.setCardElevation(3f);
        }
    }

    /**
     * Establece el listener para el botón de añadir al carrito
     */
    public void setAddToCartClickListener(View.OnClickListener listener) {
        if (btnAddToCart != null) {
            btnAddToCart.setOnClickListener(listener);
        }
    }
}