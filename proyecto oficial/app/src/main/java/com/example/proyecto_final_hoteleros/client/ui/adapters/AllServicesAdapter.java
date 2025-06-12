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
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
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

    // ✅ AGREGAR estas variables de instancia al inicio de la clase
    private List<HotelService> allServices;
    private List<HotelService> filteredServices;
    private Set<String> selectedServices;
    private double currentTotal = 0.0;
    private ServiceSelectListener listener;
    private String currentFilter = "all";

    // ✅ CONSTRUCTOR CORREGIDO
    public AllServicesAdapter(List<HotelService> services, double currentTotal, ServiceSelectListener listener) {
        this.allServices = new ArrayList<>(services);
        this.filteredServices = new ArrayList<>(services);
        this.currentTotal = currentTotal;
        this.listener = listener;
        this.selectedServices = new HashSet<>();
    }

    // ✅ AGREGAR este método
    public void updateTotalAndRecalculate(double newTotal) {
        this.currentTotal = newTotal;
        updateServiceEligibility();
        notifyDataSetChanged();
    }

    // ✅ AGREGAR este método
    private void updateServiceEligibility() {
        for (HotelService service : filteredServices) {
            if (service.getId().equals("taxi")) {
                service.setEligibleForFree(currentTotal >= 350.0);
            }
        }
    }

    // ✅ AGREGAR otros métodos necesarios
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

    public void filterServices(String filterType) {
        currentFilter = filterType;
        filteredServices.clear();

        for (HotelService service : allServices) {
            boolean shouldInclude = false;

            switch (filterType) {
                case "all":
                    shouldInclude = true;
                    break;
                case "free":
                    shouldInclude = service.isFree();
                    break;
                case "paid":
                    shouldInclude = !service.isFree() && !service.isConditional();
                    break;
                case "conditional":
                    shouldInclude = service.isConditional();
                    break;
            }

            if (shouldInclude) {
                filteredServices.add(service);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return filteredServices.size();
    }

    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.client_item_service_detail, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        HotelService service = filteredServices.get(position);
        holder.bind(service);
    }

    // ✅ VIEWHOLDER CORREGIDO
    class ServiceViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivServiceDetailIcon;
        private TextView tvServiceDetailName;
        private TextView tvServiceDetailDescription;
        private TextView tvServiceDetailPrice; // ✅ AGREGAR esta variable
        private MaterialButton btnAddToCart;
        private CardView cardTaxiCondition;
        private TextView tvServiceConditionalHint;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServiceDetailIcon = itemView.findViewById(R.id.iv_service_detail_icon);
            tvServiceDetailName = itemView.findViewById(R.id.tv_service_detail_name);
            tvServiceDetailDescription = itemView.findViewById(R.id.tv_service_detail_description);
            tvServiceDetailPrice = itemView.findViewById(R.id.tv_service_detail_price); // ✅ INICIALIZAR
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
            cardTaxiCondition = itemView.findViewById(R.id.card_taxi_condition);
            tvServiceConditionalHint = itemView.findViewById(R.id.tv_service_conditional_hint);
        }

        public void bind(HotelService service) {
            // Configurar información básica
            tvServiceDetailName.setText(service.getName());
            tvServiceDetailDescription.setText(service.getDescription());

            // Configurar icono
            setupServiceIcon(service);

            // Configurar precio y estado
            setupPriceAndStatus(service);

            // Configurar conditional hints
            setupConditionalHints(service);

            // Configurar botón
            setupButton(service);
        }

        private void setupServiceIcon(HotelService service) {
            try {
                // ✅ PRIMERO intentar cargar imagen principal del servicio
                String mainImage = service.getMainImageUrl();
                if (mainImage != null && !mainImage.isEmpty()) {
                    int imageResourceId = itemView.getContext().getResources().getIdentifier(
                            mainImage, "drawable", itemView.getContext().getPackageName());

                    if (imageResourceId > 0) {
                        ivServiceDetailIcon.setImageResource(imageResourceId);
                        return; // ✅ Si encontró imagen, usar esa
                    }
                }

                // ✅ FALLBACK: usar icono normal
                int resourceId = itemView.getContext().getResources().getIdentifier(
                        service.getIconResourceName(), "drawable",
                        itemView.getContext().getPackageName());

                if (resourceId > 0) {
                    ivServiceDetailIcon.setImageResource(resourceId);
                } else {
                    ivServiceDetailIcon.setImageResource(R.drawable.ic_hotel_service_default);
                }
            } catch (Exception e) {
                ivServiceDetailIcon.setImageResource(R.drawable.ic_hotel_service_default);
            }
        }

        private void setupPriceAndStatus(HotelService service) {
            if (service.isIncludedInRoom()) {
                tvServiceDetailPrice.setText("✓ Incluido en habitación");
                tvServiceDetailPrice.setBackgroundResource(R.drawable.bg_price_free);
            } else if (service.isFree()) {
                configureFreeService();
            } else if (service.isConditional()) {
                configureConditionalService(service);
            } else {
                configurePaidService(service);
            }
        }

        // ✅ MÉTODOS FALTANTES
        private void configureFreeService() {
            btnAddToCart.setVisibility(View.GONE);
            tvServiceDetailPrice.setText("✓ Incluido");
            tvServiceDetailPrice.setBackgroundResource(R.drawable.bg_price_free);
        }

        private void configureConditionalService(HotelService service) {
            if (service.isEligibleForFree()) {
                tvServiceDetailPrice.setText("¡GRATIS!");
                tvServiceDetailPrice.setBackgroundResource(R.drawable.bg_price_free);
            } else {
                tvServiceDetailPrice.setText(service.getPriceDisplay());
                tvServiceDetailPrice.setBackgroundResource(R.drawable.bg_price_conditional_improved);
            }
        }

        private void configurePaidService(HotelService service) {
            tvServiceDetailPrice.setText(service.getPriceDisplay());
            tvServiceDetailPrice.setBackgroundResource(R.drawable.bg_price_paid_improved);
        }

        private void setupConditionalHints(HotelService service) {
            if (service.isConditional()) {
                cardTaxiCondition.setVisibility(View.VISIBLE);
                tvServiceConditionalHint.setText(service.getConditionalBadgeText());
            } else {
                cardTaxiCondition.setVisibility(View.GONE);
            }
        }

        private void setupButton(HotelService service) {
            if (service.isIncludedInRoom() || service.isFree()) {
                btnAddToCart.setVisibility(View.GONE);
                return;
            }

            btnAddToCart.setVisibility(View.VISIBLE);
            boolean isSelected = selectedServices.contains(service.getId());
            updateButtonState(isSelected);

            btnAddToCart.setOnClickListener(v -> {
                boolean newSelectionState = !selectedServices.contains(service.getId());

                if (listener != null) {
                    listener.onServiceSelected(service, newSelectionState);
                }

                updateButtonState(newSelectionState);
            });

            // ✅ AGREGAR: Long click para galería (solo si tiene múltiples imágenes)
            itemView.setOnLongClickListener(v -> {
                if (service.hasMultipleImages()) {
                    showServiceGallery(service);

                    // Mostrar hint la primera vez
                    Toast.makeText(itemView.getContext(),
                            "Mantén presionado para ver más imágenes",
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });

            // ✅ OPCIONAL: Agregar indicador visual si tiene galería
            if (service.hasMultipleImages()) {
                // Agregar pequeño indicador de galería en el layout si quieres
            }

            // ✅ MOSTRAR indicador de galería si tiene múltiples imágenes
            View galleryIndicator = itemView.findViewById(R.id.gallery_indicator);
            if (galleryIndicator != null) {
                // ✅ MOSTRAR para servicios específicos (por ahora hardcoded para testing)
                if (service.getId().equals("breakfast") ||
                        service.getId().equals("spa") ||
                        service.getId().equals("gym") ||
                        service.hasMultipleImages()) {

                    galleryIndicator.setVisibility(View.VISIBLE);

                    // ✅ ANIMACIÓN DE ENTRADA
                    galleryIndicator.setAlpha(0f);
                    galleryIndicator.setScaleX(0.5f);
                    galleryIndicator.setScaleY(0.5f);

                    galleryIndicator.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(300)
                            .setStartDelay(500)
                            .start();

                    Log.d("ServiceAdapter", "Badge mostrado para: " + service.getName());
                } else {
                    galleryIndicator.setVisibility(View.GONE);
                }
            }

            // ✅ LONG CLICK PARA GALERÍA
            itemView.setOnLongClickListener(v -> {
                if (service.getId().equals("breakfast") || service.hasMultipleImages()) {
                    showServiceGallery(service);
                    return true;
                }
                return false;
            });
        }

        private void showServiceGallery(HotelService service) {
            try {
                // Crear el diálogo
                Dialog dialog = new Dialog(itemView.getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_service_images);

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                }

                // Configurar vistas del diálogo
                TextView tvServiceTitle = dialog.findViewById(R.id.tv_service_title);
                RecyclerView rvServiceImages = dialog.findViewById(R.id.rv_service_images);
                MaterialButton btnCloseGallery = dialog.findViewById(R.id.btn_close_gallery);

                if (tvServiceTitle != null) {
                    tvServiceTitle.setText("Galería: " + service.getName());
                }

                // Configurar RecyclerView horizontal
                if (rvServiceImages != null) {
                    LinearLayoutManager layoutManager = new LinearLayoutManager(
                            itemView.getContext(), LinearLayoutManager.HORIZONTAL, false);
                    rvServiceImages.setLayoutManager(layoutManager);

                    // Crear adaptador para la galería
                    ServiceGalleryAdapter galleryAdapter = new ServiceGalleryAdapter(
                            service.getImageUrls(),
                            new ServiceGalleryAdapter.OnImageClickListener() {
                                @Override
                                public void onImageClick(String imageName, int position) {
                                    // Opcional: mostrar imagen en pantalla completa
                                    showFullscreenImage(imageName, service.getName());
                                }
                            }
                    );
                    rvServiceImages.setAdapter(galleryAdapter);
                }

                // Botón cerrar
                if (btnCloseGallery != null) {
                    btnCloseGallery.setOnClickListener(v -> dialog.dismiss());
                }

                // Mostrar diálogo con animación
                dialog.show();

                // Animación de entrada
                if (dialog.getWindow() != null) {
                    View dialogView = dialog.findViewById(android.R.id.content);
                    if (dialogView != null) {
                        dialogView.setAlpha(0f);
                        dialogView.setScaleX(0.8f);
                        dialogView.setScaleY(0.8f);

                        dialogView.animate()
                                .alpha(1f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(300)
                                .start();
                    }
                }

                Log.d("ServiceGallery", "Galería mostrada para: " + service.getName() +
                        " con " + service.getImageUrls().size() + " imágenes");

            } catch (Exception e) {
                Log.e("ServiceGallery", "Error mostrando galería: " + e.getMessage());
                Toast.makeText(itemView.getContext(),
                        "Error mostrando galería", Toast.LENGTH_SHORT).show();
            }
        }

        private void showFullscreenImage(String imageName, String serviceName) {
            try {
                Dialog fullscreenDialog = new Dialog(itemView.getContext(),
                        android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                fullscreenDialog.setContentView(R.layout.dialog_fullscreen_image);

                ImageView ivFullscreen = fullscreenDialog.findViewById(R.id.iv_fullscreen_image);
                TextView tvImageTitle = fullscreenDialog.findViewById(R.id.tv_image_title);
                ImageButton btnCloseFullscreen = fullscreenDialog.findViewById(R.id.btn_close_fullscreen);

                // Configurar imagen
                if (ivFullscreen != null) {
                    int resourceId = itemView.getContext().getResources().getIdentifier(
                            imageName, "drawable", itemView.getContext().getPackageName());
                    ivFullscreen.setImageResource(resourceId > 0 ? resourceId : R.drawable.ic_hotel_service_default);
                }

                // Configurar título
                if (tvImageTitle != null) {
                    tvImageTitle.setText(serviceName);
                }

                // Botón cerrar
                if (btnCloseFullscreen != null) {
                    btnCloseFullscreen.setOnClickListener(v -> fullscreenDialog.dismiss());
                }

                // Cerrar con tap
                if (ivFullscreen != null) {
                    ivFullscreen.setOnClickListener(v -> fullscreenDialog.dismiss());
                }

                fullscreenDialog.show();

            } catch (Exception e) {
                Log.e("ServiceGallery", "Error mostrando imagen completa: " + e.getMessage());
            }
        }

        // ✅ MÉTODO FALTANTE
        private void updateButtonState(boolean isSelected) {
            if (isSelected) {
                btnAddToCart.setText("Añadido");
                btnAddToCart.setIcon(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_check_circle));
                btnAddToCart.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.success_green));
            } else {
                btnAddToCart.setText("Añadir");
                btnAddToCart.setIcon(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_add_circle));
                btnAddToCart.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.orange_primary));
            }
        }
    }
}