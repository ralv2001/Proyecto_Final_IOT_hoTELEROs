package com.example.proyecto_final_hoteleros.client.ui.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.adapters.AllServicesAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.HotelService;
import com.example.proyecto_final_hoteleros.client.domain.interfaces.ServiceSelectListener;
import com.example.proyecto_final_hoteleros.client.data.repository.ServicesRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class AllHotelServicesActivity extends AppCompatActivity implements ServiceSelectListener {

    private static final String TAG = "AllHotelServicesActivity";

    private RecyclerView recyclerViewServices;
    private LinearLayout emptyState;
    private CardView cartSummary;
    private TextView tvCartCount, tvCartTotal;
    private MaterialButton btnCheckout;
    private ImageButton btnBack, btnClearCart;

    // Filtros
    private Chip chipAll, chipFree, chipPaid, chipConditional;

    private List<HotelService> allServices = new ArrayList<>();
    private AllServicesAdapter adapter;
    private double currentReservationTotal = 350.0; // Simulando reserva existente
    private static final double TAXI_MIN_AMOUNT = 350.0;
    private String currentFilter = "all";
    private String activityMode; // ✅ NUEVO: "service_selection" o "browse_only"
    private String selectedRoomName;
    private String[] includedServiceIds;
    private String[] roomFeatures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Determinar el modo de la actividad
        activityMode = getIntent().getStringExtra("mode");
        if (activityMode == null) activityMode = "browse_only";

        Log.d(TAG, "=== FLUJO DE SERVICIOS ===");
        Log.d(TAG, "Modo de actividad: " + activityMode);

        setContentView(R.layout.client_activity_all_hotel_services);
        currentReservationTotal = getCurrentRoomPrice();

        Log.d(TAG, "Total inicial de reserva: " + currentReservationTotal);

        try {
            initViews();
            getIntentData();
            loadServices(); // ✅ Ahora carga servicios según el modo

            Log.d(TAG, "Servicios cargados en total: " + allServices.size());

            setupAdapter();
            setupFilterChips();
            setupClickListeners();
            updateCartDisplay();
            animateViewsEntry();

            configureUIForMode();

            Log.d(TAG, "Actividad inicializada correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando actividad: " + e.getMessage());
            Toast.makeText(this, "Error cargando servicios", Toast.LENGTH_SHORT).show();
        }
    }
    private void showRoomInfoCard() {
        View roomInfoCard = findViewById(R.id.room_info_card);
        if (roomInfoCard != null && selectedRoomName != null) {
            roomInfoCard.setVisibility(View.VISIBLE);

            TextView tvRoomName = roomInfoCard.findViewById(R.id.tv_selected_room_name);
            TextView tvIncludedServices = roomInfoCard.findViewById(R.id.tv_included_services_info);

            if (tvRoomName != null) {
                tvRoomName.setText(selectedRoomName + " seleccionada");
            }

            if (tvIncludedServices != null && includedServiceIds != null) {
                tvIncludedServices.setText(includedServiceIds.length + " servicios ya incluidos en tu habitación");
            }

            Log.d(TAG, "Room info card shown for: " + selectedRoomName);
        } else {
            if (roomInfoCard == null) {
                Log.w(TAG, "room_info_card not found in layout");
            }
            if (selectedRoomName == null) {
                Log.w(TAG, "selectedRoomName is null");
            }
        }
    }
    private void filterIncludedServices() {
        if (includedServiceIds == null || adapter == null) {
            Log.d(TAG, "No hay servicios incluidos para filtrar");
            return;
        }

        Log.d(TAG, "Filtrando servicios incluidos: " + Arrays.toString(includedServiceIds));

        // ✅ MARCAR servicios incluidos Y removerlos de la lista si están
        List<HotelService> servicesToRemove = new ArrayList<>();

        for (HotelService service : allServices) {
            if (service != null) {
                for (String includedId : includedServiceIds) {
                    if (service.getId() != null && service.getId().equals(includedId)) {
                        // Si el servicio está incluido en la habitación, marcarlo
                        service.setIncludedInRoom(true);

                        // ✅ REMOVER de la lista de servicios adicionales
                        // porque ya viene incluido en la habitación
                        if (!"taxi".equals(service.getId())) { // Taxi es especial, se mantiene
                            servicesToRemove.add(service);
                        }

                        Log.d(TAG, "Servicio incluido encontrado: " + service.getName());
                        break;
                    }
                }
            }
        }

        // Remover servicios incluidos de la lista
        allServices.removeAll(servicesToRemove);

        adapter.notifyDataSetChanged();
        Log.d(TAG, "Servicios después del filtrado: " + allServices.size());
    }
    private void configureUIForMode() {
        TextView headerTitle = findViewById(R.id.tv_header_title);
        TextView headerSubtitle = findViewById(R.id.tv_header_subtitle);

        if ("service_selection".equals(activityMode)) {
            // Modo selección de servicios
            if (headerTitle != null) {
                headerTitle.setText("Servicios adicionales");
            }
            if (headerSubtitle != null) {
                headerSubtitle.setText("Añade servicios extra a tu " + (selectedRoomName != null ? selectedRoomName : "habitación"));
            }

            showRoomInfoCard();
            filterIncludedServices();

        } else {
            // Modo navegación solamente
            if (headerTitle != null) {
                headerTitle.setText("Todos nuestros servicios");
            }
            if (headerSubtitle != null) {
                headerSubtitle.setText("Conoce todo lo que tenemos disponible");
            }

            // ✅ OCULTAR carrito en modo browse_only
            if (cartSummary != null) {
                cartSummary.setVisibility(View.GONE);
            }
        }
    }

    private void getIntentData() {
        try {
            selectedRoomName = getIntent().getStringExtra("selected_room_name");
            includedServiceIds = getIntent().getStringArrayExtra("included_service_ids");
            roomFeatures = getIntent().getStringArrayExtra("selected_room_features");

            Log.d(TAG, "Habitación seleccionada: " + selectedRoomName);
            Log.d(TAG, "Servicios incluidos: " + (includedServiceIds != null ? includedServiceIds.length : 0));

            if (includedServiceIds != null) {
                Log.d(TAG, "Servicios incluidos: " + Arrays.toString(includedServiceIds));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting intent data: " + e.getMessage());
            selectedRoomName = null;
            includedServiceIds = null;
            roomFeatures = null;
        }
    }

    private void initViews() {
        recyclerViewServices = findViewById(R.id.rv_services_list);
        emptyState = findViewById(R.id.empty_state);
        cartSummary = findViewById(R.id.cart_summary_improved);
        tvCartCount = findViewById(R.id.tv_cart_count_improved);
        tvCartTotal = findViewById(R.id.tv_cart_total_improved);
        btnBack = findViewById(R.id.btn_back);
        btnClearCart = findViewById(R.id.btn_clear_cart);
        btnCheckout = findViewById(R.id.btn_add_service);

        // Filtros
        chipAll = findViewById(R.id.chip_all);
        chipFree = findViewById(R.id.chip_free);
        chipPaid = findViewById(R.id.chip_paid);
        chipConditional = findViewById(R.id.chip_conditional);

        // Configurar RecyclerView
        if (recyclerViewServices != null) {
            recyclerViewServices.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewServices.setHasFixedSize(true);
        }

        Log.d(TAG, "Views inicializadas");
    }

    private void loadServices() {
        try {
            ServicesRepository repository = ServicesRepository.getInstance();

            // ✅ CORREGIDO: Cargar solo servicios adicionales, no todos
            if ("service_selection".equals(activityMode)) {
                // Modo selección: solo servicios adicionales (no incluidos en habitación)
                allServices = repository.getAdditionalServices();
                Log.d(TAG, "Servicios adicionales cargados: " + allServices.size());
            } else {
                // Modo navegación: todos los servicios para información
                allServices = repository.getAllServices();
                Log.d(TAG, "Todos los servicios cargados: " + allServices.size());
            }

            updateServiceEligibility();

        } catch (Exception e) {
            Log.e(TAG, "Error cargando servicios: " + e.getMessage());
            allServices = new ArrayList<>();
        }
    }

    private void updateServiceEligibility() {
        // ✅ TU CÓDIGO EXISTENTE se mantiene
        for (HotelService service : allServices) {
            if (service.getId().equals("taxi")) {
                service.setEligibleForFree(currentReservationTotal >= TAXI_MIN_AMOUNT);
                Log.d(TAG, "Taxi eligibility: " + service.isEligibleForFree());

                // ✅ AGREGAR mensaje dinámico basado en el total actual
                updateTaxiConditionalMessage(service, currentReservationTotal);
            }
        }
    }

    private void updateTaxiConditionalMessage(HotelService taxiService, double currentTotal) {
        if (currentTotal >= 350.0) {
            taxiService.setConditionalDescription("🎉 ¡INCLUIDO! Total: S/. " + String.format("%.0f", currentTotal) + " (Ahorro: S/. 60)");
        } else {
            double needed = 350.0 - currentTotal;
            taxiService.setConditionalDescription(
                    String.format("💡 Total actual: S/. %.0f - Agrega S/. %.0f más para GRATIS", currentTotal, needed)
            );
        }
    }



    private void setupAdapter() {
        try {
            adapter = new AllServicesAdapter(allServices, currentReservationTotal, this);
            if (recyclerViewServices != null) {
                recyclerViewServices.setAdapter(adapter);
                Log.d(TAG, "Adapter configurado");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error configurando adapter: " + e.getMessage());
        }
    }

    private void setupFilterChips() {
        if (chipAll != null) chipAll.setOnClickListener(v -> applyFilter("all", chipAll));
        if (chipFree != null) chipFree.setOnClickListener(v -> applyFilter("free", chipFree));
        if (chipPaid != null) chipPaid.setOnClickListener(v -> applyFilter("paid", chipPaid));
        if (chipConditional != null) chipConditional.setOnClickListener(v -> applyFilter("conditional", chipConditional));

        Log.d(TAG, "Filtros configurados");
    }

    private void applyFilter(String filterType, Chip selectedChip) {
        if (currentFilter.equals(filterType)) return;

        currentFilter = filterType;
        Log.d(TAG, "Aplicando filtro: " + filterType);

        // Actualizar UI de chips
        resetChipStyles();
        setChipActive(selectedChip);

        // Aplicar filtro al adaptador
        if (adapter != null) {
            adapter.filterServices(filterType);
        }

        // Mostrar/ocultar estado vacío
        updateEmptyState();

        // Animación de cambio de filtro
        animateFilterChange();
    }

    private void resetChipStyles() {
        Chip[] chips = {chipAll, chipFree, chipPaid, chipConditional};
        for (Chip chip : chips) {
            if (chip != null) {
                chip.setChipBackgroundColor(ContextCompat.getColorStateList(this, R.color.light_gray));
                chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                chip.setChipStrokeWidth(2);
                chip.setChipStrokeColor(ContextCompat.getColorStateList(this, R.color.gray_border));
            }
        }
    }

    private void setChipActive(Chip chip) {
        if (chip != null) {
            chip.setChipBackgroundColor(ContextCompat.getColorStateList(this, R.color.orange_primary));
            chip.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            chip.setChipStrokeWidth(0);

            // Animación de selección
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(chip, "scaleX", 1f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(chip, "scaleY", 1f, 1.1f, 1f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setDuration(300);
            animatorSet.start();
        }
    }

    private void updateEmptyState() {
        boolean isEmpty = adapter != null && adapter.getItemCount() == 0;
        if (emptyState != null) {
            emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (recyclerViewServices != null) {
            recyclerViewServices.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Log.d(TAG, "Botón atrás presionado");
                finish();
            });
        }

        if (btnClearCart != null) {
            btnClearCart.setOnClickListener(v -> {
                Log.d(TAG, "Limpiar carrito presionado");
                showClearCartDialog();
            });
        }

        if (btnCheckout != null) {
            btnCheckout.setOnClickListener(v -> {
                Log.d(TAG, "Checkout presionado");
                handleCheckout();
            });
        }
    }

    private void handleCheckout() {
        try {
            if ("service_selection".equals(activityMode)) {
                // Modo selección: procesar servicios y volver con resultado
                Set<String> selectedServices = adapter != null ? adapter.getSelectedServiceIds() : null;
                if (selectedServices != null && !selectedServices.isEmpty()) {
                    Log.d(TAG, "Servicios seleccionados: " + selectedServices.size());

                    Intent result = new Intent();
                    result.putExtra("SELECTED_SERVICES", selectedServices.toString());
                    setResult(RESULT_OK, result);
                    finish();
                } else {
                    Log.d(TAG, "No hay servicios seleccionados");
                    showNoServicesMessage();
                }
            } else {
                // Modo navegación: solo mostrar información
                showBrowseOnlyMessage();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en checkout: " + e.getMessage());
            Toast.makeText(this, "Error al procesar servicios", Toast.LENGTH_SHORT).show();
        }
    }

    private void showBrowseOnlyMessage() {
        Toast.makeText(this, "Para reservar servicios, selecciona primero una habitación", Toast.LENGTH_LONG).show();
    }

    private void updateCartDisplay() {
        try {
            Set<String> selectedServiceIds = adapter != null ? adapter.getSelectedServiceIds() : null;
            if (selectedServiceIds == null || selectedServiceIds.isEmpty()) {
                hideCartWithAnimation();
                return;
            }

            int count = 0;
            double additionalTotal = 0.0;
            double currentRoomPrice = getCurrentRoomPrice();

            // ✅ CALCULAR solo servicios que NO están incluidos en habitación
            for (HotelService service : allServices) {
                if (selectedServiceIds.contains(service.getId())) {

                    // ✅ SKIP si el servicio ya está incluido en la habitación
                    if (isServiceIncludedInRoom(service.getId())) {
                        Log.d(TAG, "Skipping included service: " + service.getName());
                        continue;
                    }

                    count++;

                    // ✅ LÓGICA ESPECIAL PARA TAXI
                    if (service.getId().equals("taxi")) {
                        double totalWithCurrentServices = currentRoomPrice + additionalTotal;
                        boolean shouldBeFree = totalWithCurrentServices >= 350.0;

                        service.setEligibleForFree(shouldBeFree);

                        if (!shouldBeFree) {
                            additionalTotal += 60.0;
                        }

                        updateTaxiConditionalMessage(service, totalWithCurrentServices);

                    } else if (service.getPrice() != null && service.getPrice() > 0) {
                        additionalTotal += service.getPrice();
                    }
                }
            }

            Log.d(TAG, "Cart - Count: " + count + ", Additional: " + additionalTotal + ", Room: " + currentRoomPrice);

            if (count > 0) {
                showCartWithAnimation();
                if (tvCartCount != null) {
                    tvCartCount.setText(count + (count == 1 ? " servicio adicional" : " servicios adicionales"));
                }
                if (tvCartTotal != null) {
                    tvCartTotal.setText(String.format("S/. %.2f", additionalTotal));
                }
            } else {
                hideCartWithAnimation();
            }

            // ✅ ACTUALIZAR total global para otros cálculos
            currentReservationTotal = currentRoomPrice + additionalTotal;

        } catch (Exception e) {
            Log.e(TAG, "Error updating cart: " + e.getMessage());
        }
    }
    private boolean isServiceIncludedInRoom(String serviceId) {
        if (includedServiceIds == null) return false;

        for (String includedId : includedServiceIds) {
            if (includedId.equals(serviceId)) {
                return true;
            }
        }
        return false;
    }



    private double getCurrentRoomPrice() {
        // ✅ USAR precio numérico directo del intent
        double roomPrice = getIntent().getDoubleExtra("room_price_numeric", 350.0);

        Log.d(TAG, "Room price from intent: " + roomPrice);
        return roomPrice;
    }

    private void showCartWithAnimation() {
        if (cartSummary != null && cartSummary.getVisibility() != View.VISIBLE) {
            cartSummary.setVisibility(View.VISIBLE);

            ObjectAnimator slideUp = ObjectAnimator.ofFloat(cartSummary, "translationY", 200f, 0f);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(cartSummary, "alpha", 0f, 1f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(slideUp, fadeIn);
            animatorSet.setDuration(400);
            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.start();
        }
    }

    private void hideCartWithAnimation() {
        if (cartSummary != null && cartSummary.getVisibility() == View.VISIBLE) {
            ObjectAnimator slideDown = ObjectAnimator.ofFloat(cartSummary, "translationY", 0f, 200f);
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(cartSummary, "alpha", 1f, 0f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(slideDown, fadeOut);
            animatorSet.setDuration(300);
            animatorSet.start();

            animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    if (cartSummary != null) {
                        cartSummary.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void animateViewsEntry() {
        if (recyclerViewServices != null) {
            recyclerViewServices.setAlpha(0f);
            recyclerViewServices.setTranslationY(100f);

            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(recyclerViewServices, "alpha", 0f, 1f);
            ObjectAnimator translateAnimator = ObjectAnimator.ofFloat(recyclerViewServices, "translationY", 100f, 0f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnimator, translateAnimator);
            animatorSet.setDuration(600);
            animatorSet.setStartDelay(200);
            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.start();
        }
    }

    private void animateFilterChange() {
        if (recyclerViewServices != null) {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(recyclerViewServices, "alpha", 1f, 0.7f);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(recyclerViewServices, "alpha", 0.7f, 1f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(fadeOut, fadeIn);
            animatorSet.setDuration(200);
            animatorSet.start();
        }
    }

    private void showClearCartDialog() {
        try {
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.client_dialog_clear_cart);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            MaterialButton btnConfirm = dialog.findViewById(R.id.btn_confirm_clear);
            MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel_clear);

            if (btnConfirm != null) {
                btnConfirm.setOnClickListener(v -> {
                    if (adapter != null) {
                        adapter.clearSelections();
                        updateCartDisplay();
                    }
                    dialog.dismiss();
                    Toast.makeText(this, "Carrito limpiado", Toast.LENGTH_SHORT).show();
                });
            }

            if (btnCancel != null) {
                btnCancel.setOnClickListener(v -> dialog.dismiss());
            }

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error mostrando diálogo limpiar: " + e.getMessage());
        }
    }

    private void showSuccessDialog() {
        try {
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.client_dialog_success);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            TextView title = dialog.findViewById(R.id.tv_dialog_title);
            TextView message = dialog.findViewById(R.id.tv_dialog_message);
            MaterialButton btnClose = dialog.findViewById(R.id.btn_close_success);
            MaterialButton btnViewReservation = dialog.findViewById(R.id.btn_view_reservation);

            if (title != null) title.setText("¡Servicios añadidos!");
            if (message != null) message.setText("Tus servicios han sido añadidos exitosamente a tu reserva.");

            if (btnClose != null) {
                btnClose.setOnClickListener(v -> dialog.dismiss());
            }

            if (btnViewReservation != null) {
                btnViewReservation.setOnClickListener(v -> {
                    dialog.dismiss();
                    Intent intent = new Intent();
                    Set<String> selectedServices = adapter != null ? adapter.getSelectedServiceIds() : null;
                    if (selectedServices != null) {
                        intent.putExtra("SELECTED_SERVICES", selectedServices.toString());
                    }
                    setResult(RESULT_OK, intent);
                    finish();
                });
            }

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error mostrando diálogo éxito: " + e.getMessage());
        }
    }

    private void showNoServicesMessage() {
        Toast.makeText(this, "Selecciona al menos un servicio para continuar", Toast.LENGTH_SHORT).show();
    }

    private void showTaxiDialog(boolean isFree) {
        try {
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.client_dialog_taxi_info);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            TextView tvMessage = dialog.findViewById(R.id.tv_taxi_message);
            MaterialButton btnOk = dialog.findViewById(R.id.btn_taxi_ok);

            if (tvMessage != null) {
                if (isFree) {
                    tvMessage.setText("¡Excelente! Tu reserva califica para el servicio de taxi gratuito al aeropuerto. Has ahorrado S/. 60.00");
                } else {
                    tvMessage.setText("Servicio de taxi añadido por S/. 60.00. Para obtenerlo gratis, aumenta tu reserva a S/. 350.00 o más.");
                }
            }

            if (btnOk != null) {
                btnOk.setOnClickListener(v -> dialog.dismiss());
            }

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error mostrando diálogo taxi: " + e.getMessage());
        }
    }

    @Override
    public void onServiceSelected(HotelService service, boolean isSelected) {
        try {
            Log.d(TAG, "Servicio seleccionado: " + service.getName() + ", estado: " + isSelected);

            // Actualizar selección en el adaptador
            if (adapter != null) {
                adapter.updateServiceSelection(service.getId(), isSelected);
            }

            // Mostrar diálogo especial para taxi
            if (service.getId().equals("taxi") && isSelected) {
                showTaxiDialog(service.isEligibleForFree());
            }

            // Actualizar display del carrito
            updateCartDisplay();

        } catch (Exception e) {
            Log.e(TAG, "Error en onServiceSelected: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back pressed");
        super.onBackPressed();
    }
}