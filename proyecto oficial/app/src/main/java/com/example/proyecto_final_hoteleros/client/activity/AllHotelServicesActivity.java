package com.example.proyecto_final_hoteleros.client.activity;

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
import com.example.proyecto_final_hoteleros.client.adapters.AllServicesAdapter;
import com.example.proyecto_final_hoteleros.client.model.HotelService;
import com.example.proyecto_final_hoteleros.client.interfaces.ServiceSelectListener;
import com.example.proyecto_final_hoteleros.client.repository.ServicesRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_hotel_services);

        Log.d(TAG, "Iniciando AllHotelServicesActivity");

        try {
            initViews();
            loadServices();
            setupAdapter();
            setupFilterChips();
            setupClickListeners();
            updateCartDisplay();
            animateViewsEntry();

            Log.d(TAG, "Actividad inicializada correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando actividad: " + e.getMessage());
            Toast.makeText(this, "Error cargando servicios", Toast.LENGTH_SHORT).show();
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
            allServices = repository.getAllServices();
            updateServiceEligibility();

            Log.d(TAG, "Servicios cargados: " + allServices.size());
        } catch (Exception e) {
            Log.e(TAG, "Error cargando servicios: " + e.getMessage());
            allServices = new ArrayList<>();
        }
    }

    private void updateServiceEligibility() {
        for (HotelService service : allServices) {
            if (service.getId().equals("taxi")) {
                service.setEligibleForFree(currentReservationTotal >= TAXI_MIN_AMOUNT);
                Log.d(TAG, "Taxi eligibility: " + service.isEligibleForFree());
            }
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
            Set<String> selectedServices = adapter != null ? adapter.getSelectedServiceIds() : null;
            if (selectedServices != null && !selectedServices.isEmpty()) {
                Log.d(TAG, "Servicios seleccionados: " + selectedServices.size());
                showSuccessDialog();
            } else {
                Log.d(TAG, "No hay servicios seleccionados");
                showNoServicesMessage();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en checkout: " + e.getMessage());
            Toast.makeText(this, "Error al procesar servicios", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCartDisplay() {
        try {
            Set<String> selectedServiceIds = adapter != null ? adapter.getSelectedServiceIds() : null;
            if (selectedServiceIds == null) {
                hideCartWithAnimation();
                return;
            }

            int count = 0;
            double total = 0.0;

            // Calcular total (solo servicios pagados)
            for (HotelService service : allServices) {
                if (selectedServiceIds.contains(service.getId())) {
                    if (service.getId().equals("taxi") && service.isEligibleForFree()) {
                        // Taxi gratis, solo contar
                        count++;
                    } else if (service.getPrice() != null && service.getPrice() > 0 && !service.isFree()) {
                        count++;
                        total += service.getPrice();
                    } else if (service.isFree()) {
                        // Servicios gratuitos no se cuentan en el carrito
                        continue;
                    }
                }
            }

            Log.d(TAG, "Carrito actualizado - Count: " + count + ", Total: " + total);

            // Actualizar UI
            if (count > 0) {
                showCartWithAnimation();
                if (tvCartCount != null) {
                    tvCartCount.setText(count + (count == 1 ? " servicio" : " servicios"));
                }
                if (tvCartTotal != null) {
                    tvCartTotal.setText(String.format("S/. %.2f", total));
                }
            } else {
                hideCartWithAnimation();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error actualizando carrito: " + e.getMessage());
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

    private void showClearCartDialog() {
        try {
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_clear_cart);
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
            dialog.setContentView(R.layout.dialog_success);
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
            dialog.setContentView(R.layout.dialog_taxi_info);
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