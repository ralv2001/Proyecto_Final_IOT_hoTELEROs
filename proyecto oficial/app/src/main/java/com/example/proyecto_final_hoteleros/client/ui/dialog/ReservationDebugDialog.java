package com.example.proyecto_final_hoteleros.client.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.Reservation;
import com.example.proyecto_final_hoteleros.client.utils.ReservationDebugManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

/**
 * ✅ DIÁLOGO PARA TESTING Y DEBUG DE RESERVAS
 * Permite cambiar estados manualmente para verificar el flujo completo
 */
public class ReservationDebugDialog extends DialogFragment {

    private static final String TAG = "ReservationDebugDialog";
    private static final String ARG_RESERVATION = "reservation";

    private Reservation reservation;
    private ReservationDebugManager debugManager;
    private DebugActionListener listener;

    // UI Components
    private TextView tvHotelName;
    private TextView tvCurrentStatus;
    private TextView tvTotalAmount;
    private TextView tvTaxiEligibility;
    private Slider sliderTaxiAmount;
    private TextView tvTaxiAmountValue;

    // Botones de estado
    private MaterialButton btnUpcoming;
    private MaterialButton btnActive;
    private MaterialButton btnStaying;
    private MaterialButton btnCheckoutPending;
    private MaterialButton btnCompletedNoTaxi;
    private MaterialButton btnCompletedWithTaxi;
    private MaterialButton btnCompletedWithReview;

    // Botones de acción
    private MaterialButton btnSimulateFlow;
    private MaterialButton btnAddServices;
    private MaterialButton btnClose;
    private ImageView btnCloseX;

    public interface DebugActionListener {
        void onReservationStateChanged(Reservation reservation);
        void onFlowSimulationStarted(Reservation reservation);
    }

    public static ReservationDebugDialog newInstance(Reservation reservation) {
        ReservationDebugDialog fragment = new ReservationDebugDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_RESERVATION, reservation);
        fragment.setArguments(args);
        return fragment;
    }

    public void setDebugActionListener(DebugActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reservation = getArguments().getParcelable(ARG_RESERVATION);
        }
        debugManager = ReservationDebugManager.getInstance();

        if (reservation == null) {
            Log.e(TAG, "❌ Reserva null, cerrando diálogo");
            dismiss();
            return;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_dialog_reservation_debug, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        updateReservationInfo();
        setupClickListeners();
        updateButtonStates();
    }

    private void initViews(View view) {
        // Información básica
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        tvCurrentStatus = view.findViewById(R.id.tv_current_status);
        tvTotalAmount = view.findViewById(R.id.tv_total_amount);
        tvTaxiEligibility = view.findViewById(R.id.tv_taxi_eligibility);

        // Control de taxi
        sliderTaxiAmount = view.findViewById(R.id.slider_taxi_amount);
        tvTaxiAmountValue = view.findViewById(R.id.tv_taxi_amount_value);

        // Botones de estado
        btnUpcoming = view.findViewById(R.id.btn_upcoming);
        btnActive = view.findViewById(R.id.btn_active);
        btnStaying = view.findViewById(R.id.btn_staying);
        btnCheckoutPending = view.findViewById(R.id.btn_checkout_pending);
        btnCompletedNoTaxi = view.findViewById(R.id.btn_completed_no_taxi);
        btnCompletedWithTaxi = view.findViewById(R.id.btn_completed_with_taxi);
        btnCompletedWithReview = view.findViewById(R.id.btn_completed_with_review);

        // Botones de acción
        btnSimulateFlow = view.findViewById(R.id.btn_simulate_flow);
        btnAddServices = view.findViewById(R.id.btn_add_services);
        btnClose = view.findViewById(R.id.btn_close);
        btnCloseX = view.findViewById(R.id.btn_close_x);

        // Configurar slider
        if (sliderTaxiAmount != null) {
            sliderTaxiAmount.setValueFrom(500.0f);
            sliderTaxiAmount.setValueTo(2000.0f);
            sliderTaxiAmount.setValue((float) debugManager.getTestTaxiMinAmount());
            sliderTaxiAmount.setStepSize(50.0f);

            sliderTaxiAmount.addOnChangeListener((slider, value, fromUser) -> {
                debugManager.setTestTaxiMinAmount(value);
                updateTaxiAmountDisplay();
                updateTaxiEligibility();
            });
        }
    }

    private void updateReservationInfo() {
        if (reservation == null) return;

        if (tvHotelName != null) {
            tvHotelName.setText(reservation.getHotelName());
        }

        updateCurrentStatus();
        updateTotalAmount();
        updateTaxiAmountDisplay();
        updateTaxiEligibility();
    }

    private void updateCurrentStatus() {
        if (tvCurrentStatus != null) {
            String detailedStatus = debugManager.getDetailedStatus(reservation);
            tvCurrentStatus.setText("Estado actual: " + detailedStatus);

            // Color según estado
            switch (reservation.getStatus()) {
                case Reservation.STATUS_UPCOMING:
                    tvCurrentStatus.setTextColor(getResources().getColor(R.color.blue_primary, null));
                    break;
                case Reservation.STATUS_ACTIVE:
                    tvCurrentStatus.setTextColor(getResources().getColor(R.color.orange_primary, null));
                    break;
                case Reservation.STATUS_COMPLETED:
                    tvCurrentStatus.setTextColor(getResources().getColor(R.color.success_green, null));
                    break;
            }
        }
    }

    private void updateTotalAmount() {
        if (tvTotalAmount != null) {
            double total = reservation.getFinalTotal();
            int serviceCount = reservation.getServices().size();
            String amountText = String.format("Total: S/ %.2f (%d servicio%s)",
                    total, serviceCount, serviceCount != 1 ? "s" : "");
            tvTotalAmount.setText(amountText);
        }
    }

    private void updateTaxiAmountDisplay() {
        if (tvTaxiAmountValue != null) {
            double amount = debugManager.getTestTaxiMinAmount();
            tvTaxiAmountValue.setText(String.format("S/ %.0f", amount));
        }
    }

    private void updateTaxiEligibility() {
        if (tvTaxiEligibility != null) {
            double total = reservation.getFinalTotal();
            double minAmount = debugManager.getTestTaxiMinAmount();
            boolean eligible = total >= minAmount;

            if (eligible) {
                tvTaxiEligibility.setText("✅ Califica para taxi gratuito");
                tvTaxiEligibility.setTextColor(getResources().getColor(R.color.success_green, null));
            } else {
                double needed = minAmount - total;
                tvTaxiEligibility.setText(String.format("❌ Necesita S/ %.0f más para taxi", needed));
                tvTaxiEligibility.setTextColor(getResources().getColor(R.color.warning_red, null));
            }
        }
    }

    private void setupClickListeners() {
        // Botones de estado
        if (btnUpcoming != null) {
            btnUpcoming.setOnClickListener(v -> changeState("Próximo", () -> {
                debugManager.forceToUpcoming(reservation);
                onStateChanged();
            }));
        }

        if (btnActive != null) {
            btnActive.setOnClickListener(v -> changeState("Check-in", () -> {
                debugManager.forceToActive(reservation);
                onStateChanged();
            }));
        }

        if (btnStaying != null) {
            btnStaying.setOnClickListener(v -> changeState("Estadía en curso", () -> {
                debugManager.forceToStaying(reservation);
                onStateChanged();
            }));
        }

        if (btnCheckoutPending != null) {
            btnCheckoutPending.setOnClickListener(v -> changeState("Checkout pendiente", () -> {
                debugManager.forceToCheckoutPending(reservation);
                onStateChanged();
            }));
        }

        if (btnCompletedNoTaxi != null) {
            btnCompletedNoTaxi.setOnClickListener(v -> changeState("Completado (Sin taxi)", () -> {
                debugManager.forceToCompletedNoTaxi(reservation);
                onStateChanged();
            }));
        }

        if (btnCompletedWithTaxi != null) {
            btnCompletedWithTaxi.setOnClickListener(v -> changeState("Completado (Con taxi)", () -> {
                debugManager.forceToCompletedWithTaxi(reservation);
                onStateChanged();
            }));
        }

        if (btnCompletedWithReview != null) {
            btnCompletedWithReview.setOnClickListener(v -> changeState("Completado (Review enviado)", () -> {
                debugManager.forceToCompletedWithReview(reservation);
                onStateChanged();
            }));
        }

        // Botones de acción
        if (btnSimulateFlow != null) {
            btnSimulateFlow.setOnClickListener(v -> {
                btnSimulateFlow.setEnabled(false);
                btnSimulateFlow.setText("Simulando...");

                debugManager.simulateFullReservationFlow(reservation, new ReservationDebugManager.FlowCallback() {
                    @Override
                    public void onStateChanged(String stateName, Reservation reservation) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                updateReservationInfo();
                                updateButtonStates();
                                Toast.makeText(getContext(), "📱 " + stateName, Toast.LENGTH_SHORT).show();

                                if (listener != null) {
                                    listener.onReservationStateChanged(reservation);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFlowCompleted() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                btnSimulateFlow.setEnabled(true);
                                btnSimulateFlow.setText("🎬 Simular flujo completo");
                                Toast.makeText(getContext(), "✅ Simulación completada", Toast.LENGTH_LONG).show();
                            });
                        }
                    }
                });

                if (listener != null) {
                    listener.onFlowSimulationStarted(reservation);
                }
            });
        }

        if (btnAddServices != null) {
            btnAddServices.setOnClickListener(v -> {
                // Agregar servicios adicionales para testing
                reservation.addService("Servicio Test 1", 100.0, 1);
                reservation.addService("Servicio Test 2", 150.0, 1);
                updateReservationInfo();
                updateTaxiEligibility();
                Toast.makeText(getContext(), "➕ Servicios agregados", Toast.LENGTH_SHORT).show();

                if (listener != null) {
                    listener.onReservationStateChanged(reservation);
                }
            });
        }

        // Botones de cerrar
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        if (btnCloseX != null) {
            btnCloseX.setOnClickListener(v -> dismiss());
        }

        // Click fuera del diálogo para cerrar
        if (getView() != null) {
            getView().setOnClickListener(v -> dismiss());

            // Evitar que el click en el contenido cierre el diálogo
            View dialogContent = getView().findViewById(R.id.dialog_content);
            if (dialogContent != null) {
                dialogContent.setOnClickListener(v -> {
                    // No hacer nada - evitar propagación
                });
            }
        }
    }

    private void changeState(String stateName, Runnable action) {
        Log.d(TAG, "🔄 Cambiando estado a: " + stateName);
        action.run();
        Toast.makeText(getContext(), "📱 Estado: " + stateName, Toast.LENGTH_SHORT).show();
    }

    private void onStateChanged() {
        updateReservationInfo();
        updateButtonStates();

        if (listener != null) {
            listener.onReservationStateChanged(reservation);
        }
    }

    private void updateButtonStates() {
        // Resetear todos los botones
        resetAllButtons();

        // Marcar el botón activo según el estado actual
        switch (reservation.getStatus()) {
            case Reservation.STATUS_UPCOMING:
                highlightButton(btnUpcoming);
                break;

            case Reservation.STATUS_ACTIVE:
                switch (reservation.getSubStatus()) {
                    case Reservation.SUBSTATUS_CHECKED_IN:
                        highlightButton(btnActive);
                        break;
                    case Reservation.SUBSTATUS_STAYING:
                        highlightButton(btnStaying);
                        break;
                    case Reservation.SUBSTATUS_CHECKOUT_PENDING:
                        highlightButton(btnCheckoutPending);
                        break;
                    default:
                        highlightButton(btnActive);
                        break;
                }
                break;

            case Reservation.STATUS_COMPLETED:
                if (reservation.isReviewSubmitted()) {
                    highlightButton(btnCompletedWithReview);
                } else if (reservation.isEligibleForFreeTaxi()) {
                    highlightButton(btnCompletedWithTaxi);
                } else {
                    highlightButton(btnCompletedNoTaxi);
                }
                break;
        }
    }

    private void resetAllButtons() {
        MaterialButton[] buttons = {
                btnUpcoming, btnActive, btnStaying, btnCheckoutPending,
                btnCompletedNoTaxi, btnCompletedWithTaxi, btnCompletedWithReview
        };

        for (MaterialButton button : buttons) {
            if (button != null) {
                button.setBackgroundTintList(getResources().getColorStateList(R.color.text_secondary, null));
                button.setTextColor(getResources().getColor(android.R.color.white, null));
                button.setAlpha(0.7f);
            }
        }
    }

    private void highlightButton(MaterialButton button) {
        if (button != null) {
            button.setBackgroundTintList(getResources().getColorStateList(R.color.orange_primary, null));
            button.setTextColor(getResources().getColor(android.R.color.white, null));
            button.setAlpha(1.0f);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.95),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener = null;
    }
}