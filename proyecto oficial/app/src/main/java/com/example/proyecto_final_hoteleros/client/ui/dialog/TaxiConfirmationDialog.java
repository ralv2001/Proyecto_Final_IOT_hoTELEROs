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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.Reservation;
import com.example.proyecto_final_hoteleros.client.utils.FirebaseReservationManager;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaxiConfirmationDialog extends DialogFragment {

    private static final String TAG = "TaxiConfirmationDialog";
    private static final String ARG_RESERVATION = "reservation";

    private Reservation reservation;
    private TaxiConfirmationListener listener;

    // UI Components
    private TextView tvHotelName;
    private TextView tvReservationDates;
    private TextView tvMessage;
    private TextView tvValidityMessage;
    private TextView tvAmountInfo;
    private ImageView ivTaxiIcon;
    private MaterialButton btnConfirmTaxi;
    private MaterialButton btnDeclineTaxi;
    private ImageView btnClose;

    public interface TaxiConfirmationListener {
        void onTaxiConfirmed(Reservation reservation);
        void onTaxiDeclined(Reservation reservation);
        void onNavigateToTaxiFlow(Reservation reservation);
    }

    public static TaxiConfirmationDialog newInstance(Reservation reservation) {
        TaxiConfirmationDialog fragment = new TaxiConfirmationDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_RESERVATION, reservation);
        fragment.setArguments(args);
        return fragment;
    }

    public void setTaxiConfirmationListener(TaxiConfirmationListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reservation = getArguments().getParcelable(ARG_RESERVATION);
        }

        // ✅ VERIFICAR ELEGIBILIDAD ANTES DE MOSTRAR
        if (reservation == null || !reservation.isEligibleForFreeTaxi()) {
            Log.w(TAG, "⚠️ Reserva no elegible para taxi, cerrando diálogo");
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
            // ✅ AGREGAR ANIMACIONES DE ENTRADA
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_dialog_taxi_confirmation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupReservationInfo();
        setupClickListeners();

        // ✅ VERIFICAR VALIDEZ EN TIEMPO REAL
        if (!isTaxiValidToday()) {
            handleExpiredTaxi();
        }
    }

    private void initViews(View view) {
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        tvReservationDates = view.findViewById(R.id.tv_reservation_dates);
        tvMessage = view.findViewById(R.id.tv_message);
        tvValidityMessage = view.findViewById(R.id.tv_validity_message);
        ivTaxiIcon = view.findViewById(R.id.iv_taxi_icon);
        btnConfirmTaxi = view.findViewById(R.id.btn_confirm_taxi);
        btnDeclineTaxi = view.findViewById(R.id.btn_decline_taxi);
        btnClose = view.findViewById(R.id.btn_close);
    }

    private void setupReservationInfo() {
        if (reservation == null) return;

        // ✅ INFORMACIÓN BÁSICA
        if (tvHotelName != null) {
            tvHotelName.setText(reservation.getHotelName());
        }

        if (tvReservationDates != null) {
            tvReservationDates.setText(reservation.getDate());
        }

        // ✅ MENSAJE PRINCIPAL PERSONALIZADO
        if (tvMessage != null) {
            String message = String.format(
                    "🎉 ¡Felicidades! Tu estadía en %s superó los S/ %.2f, " +
                            "por lo que tienes derecho a nuestro servicio de taxi gratuito al aeropuerto.",
                    reservation.getHotelName(),
                    Reservation.MONTO_MINIMO_TAXI_GRATIS
            );
            tvMessage.setText(message);
        }

        // ✅ INFORMACIÓN DEL MONTO
        if (tvAmountInfo != null) {
            String amountText = String.format(
                    "💰 Total de tu estadía: S/ %.2f\n" +
                            "✅ Mínimo requerido: S/ %.2f",
                    reservation.getFinalTotal(),
                    Reservation.MONTO_MINIMO_TAXI_GRATIS
            );
            tvAmountInfo.setText(amountText);
        }

        // ✅ MENSAJE DE VALIDEZ CON CONTEO REGRESIVO
        setupValidityMessage();
    }

    private void setupValidityMessage() {
        if (tvValidityMessage == null) return;

        Date checkOutDate = parseCheckOutDate(reservation.getDate());
        if (checkOutDate != null) {
            Calendar today = Calendar.getInstance();
            Calendar checkOut = Calendar.getInstance();
            checkOut.setTime(checkOutDate);

            // ✅ VERIFICAR SI ES EL MISMO DÍA
            if (isSameDay(today.getTime(), checkOutDate)) {
                // Es hoy - mostrar que es válido hasta las 23:59
                Calendar endOfDay = Calendar.getInstance();
                endOfDay.set(Calendar.HOUR_OF_DAY, 23);
                endOfDay.set(Calendar.MINUTE, 59);

                long hoursLeft = (endOfDay.getTimeInMillis() - System.currentTimeMillis()) / (1000 * 60 * 60);
                long minutesLeft = ((endOfDay.getTimeInMillis() - System.currentTimeMillis()) % (1000 * 60 * 60)) / (1000 * 60);

                String validityText = String.format(
                        "⏰ Válido solo hoy hasta las 23:59\n" +
                                "Tiempo restante: %d horas y %d minutos",
                        Math.max(0, hoursLeft), Math.max(0, minutesLeft)
                );
                tvValidityMessage.setText(validityText);
                tvValidityMessage.setTextColor(getResources().getColor(R.color.orange_primary, null));
            } else {
                // No es el día del checkout
                tvValidityMessage.setText("❌ Servicio de taxi ya expirado");
                tvValidityMessage.setTextColor(getResources().getColor(R.color.warning_red, null));
            }
        } else {
            tvValidityMessage.setText("ℹ️ Disponible solo el día del checkout");
        }
    }

    private boolean isTaxiValidToday() {
        if (reservation == null) return false;

        Date checkOutDate = parseCheckOutDate(reservation.getDate());
        if (checkOutDate == null) return false;

        Date today = new Date();
        return isSameDay(today, checkOutDate) && isWithinValidHours();
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isWithinValidHours() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        // Válido todo el día (0-23), pero podrías restringir a horarios específicos
        return hour >= 0 && hour <= 23;
    }

    /**
     * ✅ MÉTODO ESTÁTICO PARA VERIFICAR SI DEBE MOSTRARSE EL DIÁLOGO
     */
    public static boolean shouldShowForReservation(Reservation reservation) {
        if (reservation == null) {
            Log.d(TAG, "🚖 Reserva null, no mostrar");
            return false;
        }

        if (reservation.getStatus() != Reservation.STATUS_COMPLETED) {
            Log.d(TAG, "🚖 Reserva no completada: " + reservation.getStatusText());
            return false;
        }

        if (!reservation.isEligibleForFreeTaxi()) {
            Log.d(TAG, "🚖 No elegible para taxi: S/ " + reservation.getFinalTotal() + " < S/ " + Reservation.MONTO_MINIMO_TAXI_GRATIS);
            return false;
        }

        // ✅ VERIFICAR SI ES EL MISMO DÍA DEL CHECKOUT
        Date checkOutDate = parseCheckOutDateStatic(reservation.getDate());
        if (checkOutDate == null) {
            Log.d(TAG, "🚖 No se pudo parsear fecha de checkout: " + reservation.getDate());
            return false;
        }

        Date today = new Date();
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(today);
        cal2.setTime(checkOutDate);

        boolean isSameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

        Log.d(TAG, "🚖 Validación para " + reservation.getHotelName() + ": " +
                (isSameDay ? "SÍ es hoy" : "NO es hoy") +
                " (Checkout: " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(checkOutDate) + ")");

        return isSameDay;
    }

    private static Date parseCheckOutDateStatic(String dateString) {
        try {
            if (dateString != null && dateString.contains(" - ")) {
                String checkOutPart = dateString.split(" - ")[1].trim();
                SimpleDateFormat parser = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
                return parser.parse(checkOutPart);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parseando fecha estática: " + dateString + " - " + e.getMessage());
        }
        return null;
    }

    private Date parseCheckOutDate(String dateString) {
        return parseCheckOutDateStatic(dateString);
    }

    private void setupClickListeners() {
        if (btnConfirmTaxi != null) {
            btnConfirmTaxi.setOnClickListener(v -> {
                if (!isTaxiValidToday()) {
                    Toast.makeText(getContext(), "❌ El servicio ya no está disponible", Toast.LENGTH_SHORT).show();
                    return;
                }
                confirmTaxiService();
            });
        }

        if (btnDeclineTaxi != null) {
            btnDeclineTaxi.setOnClickListener(v -> declineTaxiService());
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
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

    private void handleExpiredTaxi() {
        Log.w(TAG, "⚠️ Servicio de taxi expirado para: " + reservation.getHotelName());

        if (tvMessage != null) {
            tvMessage.setText("❌ Lo sentimos, el servicio de taxi gratuito ya no está disponible.");
        }

        if (btnConfirmTaxi != null) {
            btnConfirmTaxi.setEnabled(false);
            btnConfirmTaxi.setText("Servicio expirado");
            btnConfirmTaxi.setAlpha(0.5f);
        }

        if (btnDeclineTaxi != null) {
            btnDeclineTaxi.setText("Entendido");
        }
    }

    private void confirmTaxiService() {
        Log.d(TAG, "🚖 Confirmando servicio de taxi para reserva: " + reservation.getReservationId());

        // Deshabilitar botones durante el proceso
        setButtonsEnabled(false);

        // Mostrar loading
        if (btnConfirmTaxi != null) {
            btnConfirmTaxi.setText("Confirmando...");
        }

        // ✅ VERIFICAR ESTADO ANTES DE PROCEDER
        FirebaseReservationManager.getInstance().checkTaxiConfirmationStatus(
                reservation.getReservationId(),
                new FirebaseReservationManager.TaxiStatusCallback() {
                    @Override
                    public void onStatusChecked(boolean isConfirmed, long confirmedAt) {
                        if (isConfirmed) {
                            // Ya está confirmado
                            Log.d(TAG, "ℹ️ Taxi ya confirmado previamente");
                            Toast.makeText(getContext(), "ℹ️ Ya has confirmado este servicio", Toast.LENGTH_SHORT).show();

                            if (listener != null) {
                                listener.onNavigateToTaxiFlow(reservation);
                            }
                            dismiss();
                        } else {
                            // Proceder con confirmación
                            proceedWithConfirmation();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.w(TAG, "⚠️ Error verificando estado, procediendo: " + error);
                        proceedWithConfirmation();
                    }
                }
        );
    }

    private void proceedWithConfirmation() {
        FirebaseReservationManager.getInstance().confirmTaxiService(
                reservation.getReservationId(),
                true,
                new FirebaseReservationManager.TaxiConfirmationCallback() {
                    @Override
                    public void onConfirmed() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.d(TAG, "✅ Taxi confirmado exitosamente");
                                Toast.makeText(getContext(), "🚖 ¡Taxi confirmado! Te redirigiremos al proceso.", Toast.LENGTH_LONG).show();

                                // Notificar al listener y cerrar
                                if (listener != null) {
                                    listener.onTaxiConfirmed(reservation);
                                    listener.onNavigateToTaxiFlow(reservation);
                                }
                                dismiss();
                            });
                        }
                    }

                    @Override
                    public void onDeclined() {
                        // Este método no se llamará en confirmación
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.e(TAG, "❌ Error confirmando taxi: " + error);
                                Toast.makeText(getContext(), "❌ Error: " + error, Toast.LENGTH_SHORT).show();

                                // Restaurar botones
                                setButtonsEnabled(true);
                                if (btnConfirmTaxi != null) {
                                    btnConfirmTaxi.setText("Sí, quiero el taxi");
                                }
                            });
                        }
                    }
                }
        );
    }

    private void declineTaxiService() {
        Log.d(TAG, "❌ Declinando servicio de taxi para reserva: " + reservation.getReservationId());

        // Deshabilitar botones durante el proceso
        setButtonsEnabled(false);

        // Mostrar loading
        if (btnDeclineTaxi != null) {
            btnDeclineTaxi.setText("Procesando...");
        }

        FirebaseReservationManager.getInstance().confirmTaxiService(
                reservation.getReservationId(),
                false,
                new FirebaseReservationManager.TaxiConfirmationCallback() {
                    @Override
                    public void onConfirmed() {
                        // Este método no se llamará en decline
                    }

                    @Override
                    public void onDeclined() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.d(TAG, "❌ Taxi declinado exitosamente");
                                Toast.makeText(getContext(), "Entendido. El servicio de taxi ha sido declinado.", Toast.LENGTH_SHORT).show();

                                // Notificar al listener y cerrar
                                if (listener != null) {
                                    listener.onTaxiDeclined(reservation);
                                }
                                dismiss();
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.e(TAG, "❌ Error declinando taxi: " + error);
                                Toast.makeText(getContext(), "❌ Error: " + error, Toast.LENGTH_SHORT).show();

                                // Restaurar botones
                                setButtonsEnabled(true);
                                if (btnDeclineTaxi != null) {
                                    btnDeclineTaxi.setText("No, gracias");
                                }
                            });
                        }
                    }
                }
        );
    }

    private void setButtonsEnabled(boolean enabled) {
        if (btnConfirmTaxi != null) {
            btnConfirmTaxi.setEnabled(enabled);
        }
        if (btnDeclineTaxi != null) {
            btnDeclineTaxi.setEnabled(enabled);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // ✅ CONFIGURAR TAMAÑO DEL DIÁLOGO
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // ✅ LIMPIAR LISTENER PARA EVITAR MEMORY LEAKS
        listener = null;
    }
}