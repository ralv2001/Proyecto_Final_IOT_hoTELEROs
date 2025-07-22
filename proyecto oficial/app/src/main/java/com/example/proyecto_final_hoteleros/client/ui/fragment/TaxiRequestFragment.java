package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.Reservation;
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.Map;

public class TaxiRequestFragment extends Fragment {

    private static final String TAG = "TaxiRequestFragment";
    private static final String ARG_RESERVATION = "reservation";

    // UI Components
    private ImageButton btnBack;
    private TextView tvHotelName;
    private TextView tvHotelAddress;
    private TextView tvClientName;
    private TextView tvPhoneNumber;
    private TextView tvStatus;
    private TextView tvStatusDescription;
    private LinearLayout layoutWaiting;
    private LinearLayout layoutDriverFound;
    private LinearLayout layoutInProgress;
    private MaterialCardView cardDriverInfo;
    private TextView tvDriverName;
    private TextView tvDriverPhone;
    private TextView tvCarInfo;
    private MaterialButton btnCallDriver;
    private MaterialButton btnCancelRequest;

    // Data
    private Reservation reservation;
    private FirebaseManager firebaseManager;
    private String requestId;
    private boolean isRequestActive = false;

    public static TaxiRequestFragment newInstance(Reservation reservation) {
        TaxiRequestFragment fragment = new TaxiRequestFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_RESERVATION, reservation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reservation = getArguments().getParcelable(ARG_RESERVATION);
        }
        firebaseManager = FirebaseManager.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_fragment_taxi_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupReservationInfo();
        setupClickListeners();
        createTaxiRequest();
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btn_back);
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        tvHotelAddress = view.findViewById(R.id.tv_hotel_address);
        tvClientName = view.findViewById(R.id.tv_client_name);
        tvPhoneNumber = view.findViewById(R.id.tv_phone_number);
        tvStatus = view.findViewById(R.id.tv_status);
        tvStatusDescription = view.findViewById(R.id.tv_status_description);

        layoutWaiting = view.findViewById(R.id.layout_waiting);
        layoutDriverFound = view.findViewById(R.id.layout_driver_found);
        layoutInProgress = view.findViewById(R.id.layout_in_progress);

        cardDriverInfo = view.findViewById(R.id.card_driver_info);
        tvDriverName = view.findViewById(R.id.tv_driver_name);
        tvDriverPhone = view.findViewById(R.id.tv_driver_phone);
        tvCarInfo = view.findViewById(R.id.tv_car_info);

        btnCallDriver = view.findViewById(R.id.btn_call_driver);
        btnCancelRequest = view.findViewById(R.id.btn_cancel_request);
    }

    private void setupReservationInfo() {
        if (reservation == null) return;

        if (tvHotelName != null) {
            tvHotelName.setText(reservation.getHotelName());
        }

        if (tvHotelAddress != null) {
            tvHotelAddress.setText(reservation.getLocation());
        }

        // ✅ INFORMACIÓN DEL CLIENTE (simulada - se puede obtener del usuario actual)
        if (tvClientName != null) {
            tvClientName.setText("Cliente Hoteleros"); // Se puede mejorar con datos reales
        }

        if (tvPhoneNumber != null) {
            tvPhoneNumber.setText("+51 999 888 777"); // Se puede mejorar con datos reales
        }
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        if (btnCallDriver != null) {
            btnCallDriver.setOnClickListener(v -> {
                // ✅ IMPLEMENTAR LLAMADA AL TAXISTA
                Toast.makeText(requireContext(), "📞 Llamando al taxista...", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnCancelRequest != null) {
            btnCancelRequest.setOnClickListener(v -> {
                cancelTaxiRequest();
            });
        }
    }

    private void createTaxiRequest() {
        Log.d(TAG, "🚖 Creando solicitud de taxi para reserva: " + reservation.getReservationId());

        updateStatus("waiting", "Buscando taxista disponible...");
        showWaitingState();

        // ✅ CREAR SOLICITUD EN FIREBASE (usando la colección de reservas existente)
        Map<String, Object> taxiRequest = createTaxiRequestData();

        firebaseManager.createCheckoutTaxiReservation(taxiRequest, new FirebaseManager.DataCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d(TAG, "✅ Solicitud de taxi creada exitosamente");
                        isRequestActive = true;

                        // ✅ SIMULAR BÚSQUEDA DE TAXISTA
                        simulateDriverSearch();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e(TAG, "❌ Error creando solicitud de taxi: " + error);
                        updateStatus("error", "Error creando solicitud: " + error);
                        Toast.makeText(requireContext(), "❌ Error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private Map<String, Object> createTaxiRequestData() {
        Map<String, Object> data = new HashMap<>();

        data.put("hotelName", reservation.getHotelName());
        data.put("hotelAddress", reservation.getLocation());
        data.put("clientName", "Cliente Hoteleros"); // Mejorar con datos reales
        data.put("clientPhone", "+51 999 888 777"); // Mejorar con datos reales
        data.put("clientEmail", "cliente@hoteleros.com"); // Mejorar con datos reales

        // ✅ DATOS ESPECÍFICOS PARA TAXI AL AEROPUERTO
        data.put("checkoutDate", getCurrentDateString());
        data.put("checkoutTime", getCurrentTimeString());
        data.put("roomNumber", reservation.getRoomNumber());
        data.put("roomType", reservation.getRoomType());

        data.put("status", "checkout");
        data.put("freeTransport", true);
        data.put("taxiStatus", "pending");
        data.put("estimatedDistance", 18.5); // Distancia promedio al aeropuerto
        data.put("estimatedDuration", 30); // Duración estimada en minutos

        data.put("destinationAddress", "Aeropuerto Internacional Jorge Chávez, Callao");
        data.put("notes", "Servicio de taxi gratuito confirmado por cliente");
        data.put("createdAt", System.currentTimeMillis());

        return data;
    }

    private String getCurrentDateString() {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return dateFormat.format(new java.util.Date());
    }

    private String getCurrentTimeString() {
        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return timeFormat.format(new java.util.Date());
    }

    private void simulateDriverSearch() {
        // ✅ SIMULAR PROCESO DE BÚSQUEDA Y ASIGNACIÓN DE TAXISTA

        // Fase 1: Búsqueda (3 segundos)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            updateStatus("searching", "Contactando taxistas cercanos...");
        }, 3000);

        // Fase 2: Taxista encontrado (6 segundos)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            updateStatus("assigned", "¡Taxista asignado!");
            showDriverFoundState();
            simulateDriverInfo();
        }, 6000);

        // Fase 3: Taxista en camino (10 segundos)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            updateStatus("in_progress", "Taxista en camino");
            showInProgressState();
        }, 10000);
    }

    private void simulateDriverInfo() {
        // ✅ INFORMACIÓN SIMULADA DEL TAXISTA (en una implementación real vendría de Firebase)
        if (tvDriverName != null) {
            tvDriverName.setText("Carlos Mendoza");
        }

        if (tvDriverPhone != null) {
            tvDriverPhone.setText("+51 987 654 321");
        }

        if (tvCarInfo != null) {
            tvCarInfo.setText("Toyota Corolla • Placa ABC-123 • Gris");
        }
    }

    private void updateStatus(String status, String description) {
        if (tvStatus != null) {
            String statusText = getStatusText(status);
            tvStatus.setText(statusText);
        }

        if (tvStatusDescription != null) {
            tvStatusDescription.setText(description);
        }

        Log.d(TAG, "📊 Estado actualizado: " + status + " - " + description);
    }

    private String getStatusText(String status) {
        switch (status) {
            case "waiting":
                return "🔍 Buscando taxista";
            case "searching":
                return "📞 Contactando";
            case "assigned":
                return "✅ Taxista asignado";
            case "in_progress":
                return "🚖 En camino";
            case "completed":
                return "✅ Completado";
            case "cancelled":
                return "❌ Cancelado";
            case "error":
                return "⚠️ Error";
            default:
                return "🔄 Procesando";
        }
    }

    private void showWaitingState() {
        if (layoutWaiting != null) layoutWaiting.setVisibility(View.VISIBLE);
        if (layoutDriverFound != null) layoutDriverFound.setVisibility(View.GONE);
        if (layoutInProgress != null) layoutInProgress.setVisibility(View.GONE);
    }

    private void showDriverFoundState() {
        if (layoutWaiting != null) layoutWaiting.setVisibility(View.GONE);
        if (layoutDriverFound != null) layoutDriverFound.setVisibility(View.VISIBLE);
        if (layoutInProgress != null) layoutInProgress.setVisibility(View.GONE);
    }

    private void showInProgressState() {
        if (layoutWaiting != null) layoutWaiting.setVisibility(View.GONE);
        if (layoutDriverFound != null) layoutDriverFound.setVisibility(View.GONE);
        if (layoutInProgress != null) layoutInProgress.setVisibility(View.VISIBLE);
    }

    private void cancelTaxiRequest() {
        if (!isRequestActive) {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
            return;
        }

        Log.d(TAG, "❌ Cancelando solicitud de taxi");

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Cancelar solicitud")
                .setMessage("¿Estás seguro de que deseas cancelar la solicitud de taxi?")
                .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                    updateStatus("cancelled", "Solicitud cancelada");
                    isRequestActive = false;

                    Toast.makeText(requireContext(), "Solicitud de taxi cancelada", Toast.LENGTH_SHORT).show();

                    // Volver después de un breve delay
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    }, 1500);
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🔄 TaxiRequestFragment destruido");
    }
}