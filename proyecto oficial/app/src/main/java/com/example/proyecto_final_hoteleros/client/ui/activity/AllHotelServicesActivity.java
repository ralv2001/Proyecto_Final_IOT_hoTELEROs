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
import android.widget.Button;
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
import com.example.proyecto_final_hoteleros.client.utils.ClientServicesManager;
import com.example.proyecto_final_hoteleros.client.utils.TaxiConfigManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllHotelServicesActivity extends AppCompatActivity implements ServiceSelectListener {

    private static final String TAG = "AllHotelServicesActivity";

    // ✅ REFERENCIAS A CHIPS
    private Chip chipAll, chipBasic, chipIncluded, chipPaid, chipConditional;
    private MaterialButton btnContinueBooking;
    private ImageButton  btnClearCartMini;
    private RecyclerView recyclerViewServices;
    private LinearLayout emptyState;
    private CardView cartSummary;
    private TextView tvCartCount, tvCartTotal;
    private MaterialButton btnCheckout;
    private ImageButton btnBack, btnClearCart;

    private List<HotelService> allServices = new ArrayList<>();
    private AllServicesAdapter adapter;
    private double currentReservationTotal = 350.0;
    private double TAXI_MIN_AMOUNT = 350.0; // ✅ VALOR DINÁMICO
    private String currentFilter = "all";
    private String activityMode;
    private String selectedRoomName;
    private String[] includedServiceIds;
    private String[] roomFeatures;
    private String hotelAdminId;

    // ✅ ClientServicesManager para cargar desde Firebase
    private ClientServicesManager clientServicesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Determinar el modo de la actividad
        activityMode = getIntent().getStringExtra("mode");
        if (activityMode == null) activityMode = "browse_only";

        Log.d(TAG, "=== FLUJO DE SERVICIOS CON FIREBASE ARREGLADO ===");
        Log.d(TAG, "Modo de actividad: " + activityMode);

        setContentView(R.layout.client_activity_all_hotel_services);
        currentReservationTotal = getCurrentRoomPrice();

        Log.d(TAG, "Total inicial de reserva: " + currentReservationTotal);

        try {
            initViews();
            getIntentData();

            // ✅ INICIALIZAR ClientServicesManager
            clientServicesManager = ClientServicesManager.getInstance(this);

            loadTaxiConfiguration();

        } catch (Exception e) {
            Log.e(TAG, "Error inicializando actividad: " + e.getMessage());
            Toast.makeText(this, "Error cargando servicios", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ✅ Cargar configuración de taxi y actualizar adapter
     */
    private void loadTaxiConfiguration() {
        Log.d(TAG, "🔄 Cargando configuración de taxi desde Firebase...");

        if (hotelAdminId == null || hotelAdminId.isEmpty()) {
            Log.w(TAG, "⚠️ No se encontró hotel_admin_id, usando valor por defecto");
            continueInitialization();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("taxi_config")
                .document(hotelAdminId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double minAmount = documentSnapshot.getDouble("minAmount");
                        if (minAmount != null) {
                            TAXI_MIN_AMOUNT = minAmount;
                            Log.d(TAG, "✅ Configuración de taxi cargada: monto mínimo = " + TAXI_MIN_AMOUNT);
                        }
                    } else {
                        Log.d(TAG, "ℹ️ No existe configuración de taxi para este hotel, usando valor por defecto");
                    }

                    continueInitialization();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error cargando configuración de taxi: " + e.getMessage());
                    continueInitialization();
                });
    }

    private void continueInitialization() {
        try {
            // ✅ CAMBIO PRINCIPAL: Cargar servicios desde Firebase
            loadServicesFromFirebase();

        } catch (Exception e) {
            Log.e(TAG, "Error en inicialización final: " + e.getMessage());
            Toast.makeText(this, "Error cargando servicios", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ✅ Cargar servicios reales desde Firebase con configuración correcta
     */
    private void loadServicesFromFirebase() {
        Log.d(TAG, "🔄 Cargando servicios desde Firebase para hotel: " + hotelAdminId);

        // Mostrar loading si existe
        showLoading();

        if ("service_selection".equals(activityMode)) {
            // ✅ MODO SELECCIÓN: Cargar solo servicios adicionales
            clientServicesManager.loadServicesForSelection(hotelAdminId, includedServiceIds,
                    new ClientServicesManager.ServicesCallback() {
                        @Override
                        public void onSuccess(List<HotelService> services) {
                            runOnUiThread(() -> {
                                hideLoading();

                                allServices.clear();
                                allServices.addAll(services);

                                // ✅ ACTUALIZAR monto mínimo del taxi en el adapter
                                updateTaxiConfigInServices();

                                finishSetup();

                                Log.d(TAG, "✅ Servicios para selección cargados: " + allServices.size());
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                hideLoading();
                                Log.e(TAG, "❌ Error cargando servicios para selección: " + error);
                                finishSetup();
                            });
                        }
                    });

        } else {
            // ✅ MODO NAVEGACIÓN: Cargar todos los servicios
            clientServicesManager.loadAllServicesForBrowsing(hotelAdminId,
                    new ClientServicesManager.ServicesCallback() {
                        @Override
                        public void onSuccess(List<HotelService> services) {
                            runOnUiThread(() -> {
                                hideLoading();

                                allServices.clear();
                                allServices.addAll(services);

                                // ✅ ACTUALIZAR monto mínimo del taxi en el adapter
                                updateTaxiConfigInServices();

                                finishSetup();

                                Log.d(TAG, "✅ Todos los servicios cargados: " + allServices.size());
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                hideLoading();
                                Log.e(TAG, "❌ Error cargando todos los servicios: " + error);
                                finishSetup();
                            });
                        }
                    });
        }
    }

    /**
     * ✅ Actualizar configuración del taxi en los servicios cargados
     */
    private void updateTaxiConfigInServices() {
        for (HotelService service : allServices) {
            if ("taxi".equals(service.getId())) {
                // ✅ ACTUALIZAR el precio de referencia con el monto mínimo correcto
                service.setPrice(TAXI_MIN_AMOUNT);
                service.setConditionalDescription(TaxiConfigManager.getTaxiMessage(currentReservationTotal, TAXI_MIN_AMOUNT));
                service.setEligibleForFree(TaxiConfigManager.qualifiesForFreeTaxi(currentReservationTotal, TAXI_MIN_AMOUNT));

                Log.d(TAG, "🚕 Taxi actualizado con monto mínimo: S/. " + TAXI_MIN_AMOUNT);
                break;
            }
        }
    }

    /**
     * ✅ ARREGLADO: Actualizar texto del botón según el estado del carrito
     */
    private void updateContinueButtonText() {
        if (btnContinueBooking == null) return;

        Set<String> selectedServiceIds = adapter != null ? adapter.getSelectedServiceIds() : null;

        if (selectedServiceIds != null && !selectedServiceIds.isEmpty()) {
            // ✅ HAY SERVICIOS SELECCIONADOS
            int paidServicesCount = 0;
            double totalPaid = 0.0;

            for (HotelService service : allServices) {
                if (selectedServiceIds.contains(service.getId())) {
                    String serviceType = service.getServiceType();

                    // Solo contar servicios de pago (no básicos, incluidos o taxi)
                    if ("paid".equals(serviceType) && service.getPrice() != null && service.getPrice() > 0) {
                        paidServicesCount++;
                        totalPaid += service.getPrice();
                    }
                }
            }

            if (paidServicesCount > 0) {
                // ✅ SERVICIOS PAGADOS SELECCIONADOS
                btnContinueBooking.setText(String.format("Agregar servicios (S/. %.2f)", totalPaid));
                btnContinueBooking.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_add_circle));
            } else {
                // ✅ SOLO SERVICIOS GRATUITOS/TAXI SELECCIONADOS
                btnContinueBooking.setText("Continuar con servicios incluidos");
                btnContinueBooking.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_arrow_forward));
            }
        } else {
            // ✅ NO HAY SERVICIOS SELECCIONADOS
            if ("service_selection".equals(activityMode)) {
                btnContinueBooking.setText("Continuar sin servicios extra");
            } else {
                btnContinueBooking.setText("Seleccionar habitación");
            }
            btnContinueBooking.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_arrow_forward));
        }

        Log.d(TAG, "🔄 Botón actualizado: " + btnContinueBooking.getText());
    }

    /**
     * ✅ FINALIZAR configuración de la UI
     */
    private void finishSetup() {
        try {
            updateServiceEligibility();
            classifyServicesBasic();

            Log.d(TAG, "Servicios cargados en total: " + allServices.size());

            setupAdapter();
            setupFilterChips();
            setupClickListeners();
            updateCartDisplay();
            animateViewsEntry();
            configureUIForMode();

            Log.d(TAG, "Actividad inicializada correctamente");

        } catch (Exception e) {
            Log.e(TAG, "Error en configuración final: " + e.getMessage());
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
        }
    }

    private void configureUIForMode() {
        TextView headerTitle = findViewById(R.id.tv_header_title);
        TextView headerSubtitle = findViewById(R.id.tv_header_subtitle);

        if ("service_selection".equals(activityMode)) {
            // Modo selección de servicios
            if (headerTitle != null) {
                headerTitle.setText("Servicios del hotel");
            }
            if (headerSubtitle != null) {
                headerSubtitle.setText("Revisa los servicios de tu " + (selectedRoomName != null ? selectedRoomName : "habitación"));
            }

            showRoomInfoCard();

            // ✅ MOSTRAR botón continuar
            if (btnContinueBooking != null) {
                btnContinueBooking.setVisibility(View.VISIBLE);
            }

        } else {
            // Modo navegación solamente
            if (headerTitle != null) {
                headerTitle.setText("Todos nuestros servicios");
            }
            if (headerSubtitle != null) {
                headerSubtitle.setText("Conoce todo lo que tenemos disponible");
            }

            // ✅ OCULTAR carrito en modo browse_only pero mostrar botón continuar
            if (cartSummary != null) {
                cartSummary.setVisibility(View.GONE);
            }
            if (btnContinueBooking != null) {
                btnContinueBooking.setText("Seleccionar habitación");
                btnContinueBooking.setVisibility(View.VISIBLE);
            }
        }
    }

    private void getIntentData() {
        try {
            selectedRoomName = getIntent().getStringExtra("selected_room_name");
            includedServiceIds = getIntent().getStringArrayExtra("included_service_ids");
            roomFeatures = getIntent().getStringArrayExtra("selected_room_features");
            hotelAdminId = getIntent().getStringExtra("hotel_admin_id");

            Log.d(TAG, "Habitación seleccionada: " + selectedRoomName);
            Log.d(TAG, "Hotel Admin ID: " + hotelAdminId);
            Log.d(TAG, "Servicios incluidos: " + (includedServiceIds != null ? includedServiceIds.length : 0));

            if (includedServiceIds != null) {
                Log.d(TAG, "Servicios incluidos: " + Arrays.toString(includedServiceIds));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting intent data: " + e.getMessage());
            selectedRoomName = null;
            includedServiceIds = null;
            roomFeatures = null;
            hotelAdminId = null;
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

        // ✅ NUEVAS PESTAÑAS
        chipAll = findViewById(R.id.chip_all);
        chipBasic = findViewById(R.id.chip_basic);
        chipIncluded = findViewById(R.id.chip_included);
        chipPaid = findViewById(R.id.chip_paid);
        chipConditional = findViewById(R.id.chip_conditional);

        // ✅ NUEVO BOTÓN CONTINUAR y mini limpiar
        btnContinueBooking = findViewById(R.id.btn_continue_booking);
        btnClearCartMini = findViewById(R.id.btn_clear_cart_mini);

        // Configurar RecyclerView
        if (recyclerViewServices != null) {
            recyclerViewServices.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewServices.setHasFixedSize(true);
        }

        Log.d(TAG, "Views inicializadas");
    }

    /**
     * ✅ Actualizar elegibilidad de servicios con monto dinámico
     */
    private void updateServiceEligibility() {
        for (HotelService service : allServices) {
            if (service.getId().equals("taxi")) {
                // ✅ USAR TaxiConfigManager con monto dinámico
                service.setEligibleForFree(TaxiConfigManager.qualifiesForFreeTaxi(currentReservationTotal, TAXI_MIN_AMOUNT));
                service.setConditionalDescription(TaxiConfigManager.getTaxiMessage(currentReservationTotal, TAXI_MIN_AMOUNT));

                Log.d(TAG, "🚕 Taxi eligibility updated: " + service.isEligibleForFree() +
                        " (Total: S/. " + currentReservationTotal + ", Mínimo: S/. " + TAXI_MIN_AMOUNT + ")");
            }
        }
    }

    private void classifyServicesBasic() {
        if (!allServices.isEmpty()) {
            Log.d(TAG, "🔄 Clasificando servicios para mostrar en 3 secciones...");

            // ✅ LOGGING SIMPLE para mostrar las 3 categorías
            int basicCount = 0, includedCount = 0, paidCount = 0;

            for (HotelService service : allServices) {
                if (service == null) continue;

                if (isServiceIncludedInRoom(service.getId())) {
                    includedCount++;
                    Log.d(TAG, "   ✅ Incluido: " + service.getName());
                } else if (service.getPrice() != null && service.getPrice() > 0 && "paid".equals(service.getServiceType())) {
                    paidCount++;
                    Log.d(TAG, "   💰 De pago: " + service.getName() + " - S/. " + service.getPrice());
                } else if ("basic".equals(service.getServiceType())) {
                    basicCount++;
                    Log.d(TAG, "   📋 Básico: " + service.getName());
                }
            }

            Log.d(TAG, "✅ Clasificación completada:");
            Log.d(TAG, "   - Básicos: " + basicCount);
            Log.d(TAG, "   - Incluidos: " + includedCount);
            Log.d(TAG, "   - De pago: " + paidCount);
        }
    }

    /**
     * ✅ Configurar adapter con monto mínimo correcto
     */
    private void setupAdapter() {
        try {
            adapter = new AllServicesAdapter(allServices, currentReservationTotal, this);

            // ✅ CONFIGURAR monto mínimo del taxi en el adapter
            adapter.setTaxiMinAmount(TAXI_MIN_AMOUNT);

            if (recyclerViewServices != null) {
                recyclerViewServices.setAdapter(adapter);
                Log.d(TAG, "Adapter configurado con " + allServices.size() + " servicios y taxi mínimo: S/. " + TAXI_MIN_AMOUNT);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error configurando adapter: " + e.getMessage());
        }
    }

    private void setupFilterChips() {
        if (chipAll != null) chipAll.setOnClickListener(v -> applyFilter("all", chipAll));
        if (chipBasic != null) chipBasic.setOnClickListener(v -> applyFilter("basic", chipBasic));
        if (chipIncluded != null) chipIncluded.setOnClickListener(v -> applyFilter("included", chipIncluded));
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
        Chip[] chips = {chipAll, chipBasic, chipIncluded, chipPaid, chipConditional};
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

        // ✅ NUEVO: Botón continuar siempre disponible
        if (btnContinueBooking != null) {
            btnContinueBooking.setOnClickListener(v -> {
                Log.d(TAG, "Continuar con reserva presionado");
                handleContinueBooking();
            });
        }

        // ✅ NUEVO: Botón limpiar carrito mini
        if (btnClearCartMini != null) {
            btnClearCartMini.setOnClickListener(v -> {
                Log.d(TAG, "Limpiar carrito mini presionado");
                showClearCartDialog();
            });
        }
    }

    // ✅ NUEVO MÉTODO: Continuar a BookingSummary
    private void handleContinueBooking() {
        try {
            if ("service_selection".equals(activityMode)) {
                // Modo selección: procesar servicios y volver con resultado
                Set<String> selectedServices = adapter != null ? adapter.getSelectedServiceIds() : new HashSet<>();

                // ✅ CALCULAR PRECIO REAL DE SERVICIOS ADICIONALES
                double calculatedAdditionalPrice = calculateCurrentAdditionalServicesPrice(selectedServices);

                Log.d(TAG, "🎯 Finalizando selección de servicios:");
                Log.d(TAG, "   - Servicios seleccionados: " + selectedServices);
                Log.d(TAG, "   - Precio adicional calculado: S/. " + calculatedAdditionalPrice);

                Intent result = new Intent();
                result.putExtra("SELECTED_SERVICES", selectedServices.toString());
                result.putExtra("ADDITIONAL_SERVICES_PRICE", calculatedAdditionalPrice); // ✅ AGREGAR PRECIO
                setResult(RESULT_OK, result);
                finish();
            } else {
                // Modo navegación: mostrar mensaje informativo
                showBrowseOnlyMessage();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en continuar: " + e.getMessage());
            Toast.makeText(this, "Error al continuar", Toast.LENGTH_SHORT).show();
        }
    }
    private double calculateCurrentAdditionalServicesPrice(Set<String> selectedServiceIds) {
        double totalAdditionalCost = 0.0;
        double currentRoomPrice = getCurrentRoomPrice();

        Log.d(TAG, "💰 Calculando precio de servicios adicionales:");
        Log.d(TAG, "   - Precio habitación base: S/. " + currentRoomPrice);

        for (HotelService service : allServices) {
            if (selectedServiceIds.contains(service.getId())) {
                String serviceType = service.getServiceType();
                boolean isIncluded = service.isIncludedInRoom();

                Log.d(TAG, "   - Evaluando: " + service.getName() +
                        " (Tipo: " + serviceType + ", Incluido: " + isIncluded + ")");

                // ✅ SKIP servicios básicos e incluidos (ya están incluidos)
                if ("basic".equals(serviceType) ||
                        ("included".equals(serviceType) && isIncluded)) {
                    Log.d(TAG, "     → INCLUIDO, no suma al total");
                    continue;
                }

                // ✅ SERVICIOS PAGADOS: Suman al total
                if ("paid".equals(serviceType) && service.getPrice() != null && service.getPrice() > 0) {
                    totalAdditionalCost += service.getPrice();
                    Log.d(TAG, "     → PAGADO: +S/. " + service.getPrice() +
                            " (Total acumulado: S/. " + totalAdditionalCost + ")");
                }

                // ✅ TAXI (condicional): NUNCA suma al total (siempre gratis cuando califica)
                else if ("conditional".equals(serviceType)) {
                    Log.d(TAG, "     → TAXI: Gratis (no suma al total)");
                }
            }
        }

        Log.d(TAG, "💰 Precio final de servicios adicionales: S/. " + totalAdditionalCost);
        return totalAdditionalCost;
    }

    private void showBrowseOnlyMessage() {
        Toast.makeText(this, "Para reservar servicios, selecciona primero una habitación", Toast.LENGTH_LONG).show();
    }

    /**
     * ✅ updateCartDisplay con cálculo dinámico del taxi
     */
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

            // ✅ CALCULAR solo servicios PAGADOS y CONDICIONALES (no básicos ni incluidos)
            for (HotelService service : allServices) {
                if (selectedServiceIds.contains(service.getId())) {
                    String serviceType = service.getServiceType();

                    // ✅ SKIP servicios básicos e incluidos (ya están incluidos)
                    if ("basic".equals(serviceType) ||
                            ("included".equals(serviceType) && service.isIncludedInRoom())) {
                        Log.d(TAG, "Skipping included service: " + service.getName());
                        continue;
                    }

                    count++;

                    // ✅ LÓGICA ESPECIAL PARA TAXI (condicional)
                    if ("conditional".equals(serviceType)) {
                        double totalWithCurrentServices = currentRoomPrice + additionalTotal;
                        boolean shouldBeFree = TaxiConfigManager.qualifiesForFreeTaxi(totalWithCurrentServices, TAXI_MIN_AMOUNT);

                        service.setEligibleForFree(shouldBeFree);

                        // ✅ TAXI NUNCA SUMA AL TOTAL (siempre gratis cuando se puede agregar)
                        if (!shouldBeFree) {
                            // Si no califica para gratis, no debería estar en el carrito
                            Log.w(TAG, "Taxi en carrito pero no califica para gratis - removiendo");
                            continue;
                        }

                    } else if ("paid".equals(serviceType) && service.getPrice() != null && service.getPrice() > 0) {
                        // ✅ SERVICIOS PAGADOS: Suman al total
                        additionalTotal += service.getPrice();
                    }
                }
            }

            Log.d(TAG, "Cart - Count: " + count + ", Additional: " + additionalTotal + ", Room: " + currentRoomPrice);

            if (count > 0) {
                showCartWithAnimation();
                if (tvCartCount != null) {
                    String countText = count == 1 ? "1 servicio adicional" : count + " servicios adicionales";
                    tvCartCount.setText(countText);
                }
                if (tvCartTotal != null) {
                    if (additionalTotal > 0) {
                        tvCartTotal.setText(String.format("+ S/. %.2f", additionalTotal));
                    } else {
                        tvCartTotal.setText("¡Servicios incluidos!");
                    }
                }
            } else {
                hideCartWithAnimation();
            }

            // ✅ ACTUALIZAR total global para otros cálculos
            currentReservationTotal = currentRoomPrice + additionalTotal;
            updateContinueButtonText();

        } catch (Exception e) {
            Log.e(TAG, "Error updating cart: " + e.getMessage());
        }
    }

    private boolean isServiceIncludedInRoom(String serviceId) {
        return isServiceIncludedInRoom(serviceId, includedServiceIds);
    }

    private boolean isServiceIncludedInRoom(String serviceId, String[] includedServiceIds) {
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

    /**
     * ✅ Actualizar taxi con monto dinámico y notificar adapter
     */
    private void updateTaxiWithDynamicAmount(double currentTotal) {
        for (HotelService service : allServices) {
            if (service.getId().equals("taxi")) {
                boolean wasEligible = service.isEligibleForFree();
                boolean isEligible = TaxiConfigManager.qualifiesForFreeTaxi(currentTotal, TAXI_MIN_AMOUNT);

                service.setEligibleForFree(isEligible);
                service.setConditionalDescription(TaxiConfigManager.getTaxiMessage(currentTotal, TAXI_MIN_AMOUNT));

                if (wasEligible != isEligible && adapter != null) {
                    // ✅ ACTUALIZAR adapter con el nuevo total y configuración
                    adapter.updateTotalAndRecalculate(currentTotal);
                    Log.d(TAG, "🚕 Taxi eligibility updated: " + isEligible + " (total: " + currentTotal + ", mínimo: " + TAXI_MIN_AMOUNT + ")");
                }
                break;
            }
        }
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

    /**
     * ✅ ARREGLADO: Diálogo de limpiar carrito que actualiza el botón correctamente
     */
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
                        Log.d(TAG, "🧹 Limpiando carrito...");

                        // ✅ LIMPIAR selecciones
                        adapter.clearSelections();

                        // ✅ ACTUALIZAR displays INMEDIATAMENTE
                        updateCartDisplay();
                        updateContinueButtonText();

                        // ✅ RESETEAR total de reserva al precio base del cuarto
                        currentReservationTotal = getCurrentRoomPrice();

                        // ✅ RECALCULAR taxi con el monto base
                        updateTaxiWithDynamicAmount(currentReservationTotal);

                        Log.d(TAG, "✅ Carrito limpiado - Total reseteado a: S/. " + currentReservationTotal);
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

    // ✅ NUEVOS MÉTODOS PARA MANEJO DE ESTADOS

    private void showLoading() {
        // Mostrar indicador de carga si existe en el layout
        View loadingView = findViewById(R.id.loading_container);
        if (loadingView != null) {
            loadingView.setVisibility(View.VISIBLE);
        }

        if (recyclerViewServices != null) {
            recyclerViewServices.setVisibility(View.GONE);
        }
        if (emptyState != null) {
            emptyState.setVisibility(View.GONE);
        }
    }

    private void hideLoading() {
        View loadingView = findViewById(R.id.loading_container);
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }

        if (recyclerViewServices != null) {
            recyclerViewServices.setVisibility(View.VISIBLE);
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

            // ✅ NUEVO: Recalcular taxi dinámicamente
            onServiceSelectionChanged();

        } catch (Exception e) {
            Log.e(TAG, "Error en onServiceSelected: " + e.getMessage());
        }
    }

    /**
     * ✅ ARREGLADO: Recalcular servicios en tiempo real con taxi dinámico
     */
    @Override
    public void onServiceSelectionChanged() {
        updateCartDisplay();
        updateContinueButtonText();

        // ✅ Recalcular taxi en tiempo real con monto dinámico
        if (adapter != null) {
            Set<String> selectedServiceIds = adapter.getSelectedServiceIds();
            double additionalTotal = 0.0;
            double currentRoomPrice = getCurrentRoomPrice();

            // Calcular total de servicios seleccionados (sin taxi)
            for (HotelService service : allServices) {
                if (selectedServiceIds.contains(service.getId()) &&
                        !service.getId().equals("taxi") &&
                        !isServiceIncludedInRoom(service.getId()) &&
                        "paid".equals(service.getServiceType()) &&
                        service.getPrice() != null && service.getPrice() > 0) {
                    additionalTotal += service.getPrice();
                }
            }

            double newTotal = currentRoomPrice + additionalTotal;

            // ✅ ACTUALIZAR taxi y adapter con nuevo total
            updateTaxiWithDynamicAmount(newTotal);

            Log.d(TAG, "🔄 Recalculando servicios - Nuevo total: S/. " + newTotal);
        }
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
                    tvMessage.setText("Servicio de taxi añadido por S/. 60.00. Para obtenerlo gratis, aumenta tu reserva a S/. " + TAXI_MIN_AMOUNT + " o más.");
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
    public void onBackPressed() {
        Log.d(TAG, "Back pressed");
        super.onBackPressed();
    }
}