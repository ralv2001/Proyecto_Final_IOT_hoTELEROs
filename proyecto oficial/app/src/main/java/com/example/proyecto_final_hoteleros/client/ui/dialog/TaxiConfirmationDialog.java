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
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
        setupUI();
        setupClickListeners();
        updateValidityMessage(); // ✅ CAMBIADO: llamar directamente a updateValidityMessage
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

    private void setupUI() {
        if (reservation == null) return;

        // ✅ INFORMACIÓN BÁSICA DE LA RESERVA
        if (tvHotelName != null) {
            tvHotelName.setText(reservation.getHotelName());
        }

        if (tvReservationDates != null) {
            tvReservationDates.setText(reservation.getDate());
        }

        // ✅ MENSAJE PRINCIPAL
        if (tvMessage != null) {
            tvMessage.setText(
                    "¡Tu estadía ha finalizado! Como tu reserva incluyó servicio de taxi gratuito, " +
                            "¿te gustaría confirmar el servicio de taxi al aeropuerto?"
            );
        }

        // ✅ VERIFICAR SI AÚN ES VÁLIDO (mismo día del checkout)
        updateValidityMessage();
    }

    private void updateValidityMessage() {
        if (tvValidityMessage == null) return;

        try {
            boolean isValidToday = isTaxiValidToday();

            if (isValidToday) {
                tvValidityMessage.setText("✅ Válido solo hoy. Una vez que pase el día, ya no podrás solicitar el taxi gratuito.");
                tvValidityMessage.setTextColor(getResources().getColor(android.R.color.holo_green_light, null));

                // Habilitar botones
                if (btnConfirmTaxi != null) btnConfirmTaxi.setEnabled(true);
                if (btnDeclineTaxi != null) btnDeclineTaxi.setEnabled(true);

            } else {
                tvValidityMessage.setText("❌ El período de validez ha expirado. Solo era válido el día del checkout.");
                tvValidityMessage.setTextColor(getResources().getColor(android.R.color.holo_red_light, null));

                // Deshabilitar botones de confirmación
                if (btnConfirmTaxi != null) {
                    btnConfirmTaxi.setEnabled(false);
                    btnConfirmTaxi.setText("Expirado");
                }
                if (btnDeclineTaxi != null) {
                    btnDeclineTaxi.setEnabled(false);
                    btnDeclineTaxi.setText("Expirado");
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error verificando validez: " + e.getMessage());
            tvValidityMessage.setText("⚠️ No se pudo verificar la validez del servicio.");
            tvValidityMessage.setTextColor(getResources().getColor(R.color.warning_orange, null));
        }
    }

    private boolean isTaxiValidToday() {
        try {
            Date checkoutDate = parseCheckOutDate(reservation.getDate());
            if (checkoutDate == null) return false;

            // Comparar solo las fechas (ignorar horas)
            Calendar checkoutCal = Calendar.getInstance();
            checkoutCal.setTime(checkoutDate);
            checkoutCal.set(Calendar.HOUR_OF_DAY, 0);
            checkoutCal.set(Calendar.MINUTE, 0);
            checkoutCal.set(Calendar.SECOND, 0);
            checkoutCal.set(Calendar.MILLISECOND, 0);

            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);

            // ✅ VÁLIDO SOLO EL MISMO DÍA DEL CHECKOUT
            return checkoutCal.getTimeInMillis() == todayCal.getTimeInMillis();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error verificando validez de fecha: " + e.getMessage());
            return false;
        }
    }

    private Date parseCheckOutDate(String dateString) {
        try {
            // Formato esperado: "10 May - 15 May, 2025"
            if (dateString.contains(" - ")) {
                String checkOutPart = dateString.split(" - ")[1].trim();
                SimpleDateFormat parser = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
                return parser.parse(checkOutPart);
            }
        } catch (ParseException e) {
            Log.e(TAG, "❌ Error parseando fecha de checkout: " + dateString);
        }
        return null;
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

    private void confirmTaxiService() {
        Log.d(TAG, "🚖 Confirmando servicio de taxi para reserva: " + reservation.getReservationId());

        // Deshabilitar botones durante el proceso
        setButtonsEnabled(false);

        // Mostrar loading
        if (btnConfirmTaxi != null) {
            btnConfirmTaxi.setText("Confirmando...");
        }

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
        if (btnConfirmTaxi != null) btnConfirmTaxi.setEnabled(enabled);
        if (btnDeclineTaxi != null) btnDeclineTaxi.setEnabled(enabled);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Configurar tamaño del diálogo
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    /**
     * ✅ MÉTODO ESTÁTICO PARA VERIFICAR SI DEBE MOSTRARSE EL DIÁLOGO
     */
    public static boolean shouldShowForReservation(Reservation reservation) {
        if (reservation == null) return false;

        // Solo para reservas completadas con taxi incluido
        if (reservation.getStatus() != Reservation.STATUS_COMPLETED) return false;
        if (!reservation.isEligibleForFreeTaxi()) return false;

        // Verificar si ya fue confirmado/declinado
        // (esto se podría mejorar con una consulta a Firebase, pero por simplicidad asumimos que no)
        return true;
    }
}