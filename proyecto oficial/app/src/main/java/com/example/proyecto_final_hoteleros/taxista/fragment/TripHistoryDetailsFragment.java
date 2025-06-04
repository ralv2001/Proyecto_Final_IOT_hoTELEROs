package com.example.proyecto_final_hoteleros.taxista.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.taxista.model.CompletedTrip;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Locale;

public class TripHistoryDetailsFragment extends Fragment {

    private static final String TAG = "TripHistoryDetails";
    private CompletedTrip completedTrip;

    // Vistas principales
    private ImageButton btnBack;
    private TextView toolbarTitle;
    private ImageView imgHotelBanner;

    // Información del viaje
    private TextView tvHotelName, tvClientName, tvTripType;
    private TextView tvCompletedDate, tvCompletedTime, tvStatus;
    private TextView tvDuration, tvDistance, tvClientRating;

    // Direcciones
    private TextView tvOriginAddress, tvDestinationAddress;

    // Información de pago
    private TextView tvTotalAmount, tvEarnings, tvPaymentMethod;

    // Información adicional
    private TextView tvAdditionalInfo;

    // Botones de acción
    private MaterialButton btnRepeatTrip;
    private MaterialButton btnReportIssue;

    public TripHistoryDetailsFragment() {
        // Constructor vacío requerido
    }

    /**
     * Constructor que recibe el viaje completado como Parcelable
     */
    public static TripHistoryDetailsFragment newInstance(CompletedTrip completedTrip) {
        Log.d(TAG, "newInstance: Creando fragmento para " + completedTrip.getId());
        TripHistoryDetailsFragment fragment = new TripHistoryDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable("completed_trip", completedTrip);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Inflando vista de detalles del historial");
        View view = inflater.inflate(R.layout.taxi_fragment_trip_history_details, container, false);

        initializeViews(view);
        setupListeners();

        // Recuperar viaje completado utilizando Parcelable
        if (getArguments() != null) {
            Log.d(TAG, "onCreateView: Obteniendo argumentos");
            completedTrip = getArguments().getParcelable("completed_trip");
            if (completedTrip != null) {
                Log.d(TAG, "onCreateView: Viaje recuperado con ID: " + completedTrip.getId());
                setupDataFromCompletedTrip();
            } else {
                Log.w(TAG, "onCreateView: No se pudo recuperar el viaje");
                setupDummyData();
            }
        } else {
            Log.w(TAG, "onCreateView: No hay argumentos, usando datos de ejemplo");
            setupDummyData();
        }

        return view;
    }

    /**
     * Inicializar todas las vistas según el XML
     */
    private void initializeViews(View view) {
        // Toolbar y elementos principales
        btnBack = view.findViewById(R.id.btn_back);
        toolbarTitle = view.findViewById(R.id.toolbar_title);
        imgHotelBanner = view.findViewById(R.id.img_hotel_banner);

        // Información del viaje
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        tvClientName = view.findViewById(R.id.tv_client_name);
        tvTripType = view.findViewById(R.id.tv_trip_type);
        tvCompletedDate = view.findViewById(R.id.tv_completed_date);
        tvCompletedTime = view.findViewById(R.id.tv_completed_time);
        tvStatus = view.findViewById(R.id.tv_status);
        tvDuration = view.findViewById(R.id.tv_duration);
        tvDistance = view.findViewById(R.id.tv_distance);
        tvClientRating = view.findViewById(R.id.tv_client_rating);

        // Direcciones
        tvOriginAddress = view.findViewById(R.id.tv_origin_address);
        tvDestinationAddress = view.findViewById(R.id.tv_destination_address);

        // Información de pago
        tvTotalAmount = view.findViewById(R.id.tv_total_amount);
        tvEarnings = view.findViewById(R.id.tv_earnings);
        tvPaymentMethod = view.findViewById(R.id.tv_payment_method);

        // Información adicional
        tvAdditionalInfo = view.findViewById(R.id.tv_additional_info);

        // Botones de acción
        btnRepeatTrip = view.findViewById(R.id.btn_repeat_trip);
        btnReportIssue = view.findViewById(R.id.btn_report_issue);

        // Configurar título
        toolbarTitle.setText("Detalles del Viaje");
    }

    /**
     * Configurar listeners para botones y acciones
     */
    private void setupListeners() {
        // Botón de regresar
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Botón regresar presionado");
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Botón para repetir viaje
        btnRepeatTrip.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Botón repetir viaje presionado");
            showRepeatTripConfirmation();
        });

        // Botón para reportar problema
        btnReportIssue.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Botón reportar problema presionado");
            showReportIssueDialog();
        });
    }

    /**
     * Configurar datos usando la información del viaje completado
     */
    private void setupDataFromCompletedTrip() {
        try {
            // Configurar datos del hotel
            tvHotelName.setText(completedTrip.getHotelName());
            tvClientName.setText(completedTrip.getClientName());
            tvTripType.setText(completedTrip.getTripType());

            // Cargar imagen del hotel con Glide
            Glide.with(this)
                    .load(completedTrip.getHotelImageUrl())
                    .placeholder(R.drawable.belmond)
                    .error(R.drawable.belmond)
                    .into(imgHotelBanner);

            // Configurar información del viaje
            tvCompletedDate.setText(completedTrip.getCompletedDate());
            tvCompletedTime.setText(completedTrip.getCompletedTime());
            tvStatus.setText(completedTrip.getStatus());

            // Color del estado
            if (completedTrip.isCompleted()) {
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
            } else if (completedTrip.isCancelled()) {
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
            }

            tvDuration.setText(completedTrip.getFormattedDuration());
            tvDistance.setText(completedTrip.getFormattedDistance());
            tvClientRating.setText(String.valueOf(completedTrip.getClientRating()));

            // Configurar direcciones
            tvOriginAddress.setText(completedTrip.getOriginAddress());
            tvDestinationAddress.setText(completedTrip.getDestinationAddress());

            // Configurar precios
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
            String formattedTotal = format.format(completedTrip.getTotalAmount()).replace("PEN", "S/");
            String formattedEarnings = format.format(completedTrip.getEarnings()).replace("PEN", "S/");

            tvTotalAmount.setText(formattedTotal);
            tvEarnings.setText(formattedEarnings);
            tvPaymentMethod.setText(completedTrip.getPaymentMethod());

            // Información adicional
            if (completedTrip.getNotes() != null && !completedTrip.getNotes().isEmpty()) {
                tvAdditionalInfo.setText(completedTrip.getNotes());
            } else {
                tvAdditionalInfo.setText("Sin observaciones adicionales");
            }

            // Mostrar/ocultar botón de repetir según el estado
            if (completedTrip.isCompleted()) {
                btnRepeatTrip.setVisibility(View.VISIBLE);
                btnRepeatTrip.setText("Repetir Viaje");
            } else {
                btnRepeatTrip.setVisibility(View.GONE);
            }

            Log.d(TAG, "setupDataFromCompletedTrip: Datos configurados exitosamente");

        } catch (Exception e) {
            Log.e(TAG, "Error al configurar datos del viaje completado", e);
            setupDummyData(); // Fallback a datos de ejemplo
        }
    }

    /**
     * Configurar datos de ejemplo cuando no hay viaje disponible
     */
    private void setupDummyData() {
        try {
            // Configurar datos del hotel
            tvHotelName.setText("Hotel Grand Plaza");
            tvClientName.setText("Carlos Mendoza");
            tvTripType.setText("Hotel-Aeropuerto");

            // Cargar imagen del hotel
            Glide.with(this)
                    .load("https://cf.bstatic.com/xdata/images/hotel/max1024x768/237363319.jpg")
                    .placeholder(R.drawable.belmond)
                    .error(R.drawable.belmond)
                    .into(imgHotelBanner);

            // Configurar información del viaje
            tvCompletedDate.setText("15 Ene");
            tvCompletedTime.setText("14:30");
            tvStatus.setText("Completado");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
            tvDuration.setText("25 min");
            tvDistance.setText("12.5 km");
            tvClientRating.setText("4.8");

            // Configurar direcciones
            tvOriginAddress.setText("Hotel Grand Plaza, Av. Principal 123, San Miguel");
            tvDestinationAddress.setText("Aeropuerto Internacional Jorge Chávez, Callao");

            // Configurar precios
            tvTotalAmount.setText("S/ 85.00");
            tvEarnings.setText("S/ 68.00");
            tvPaymentMethod.setText("Efectivo");

            // Información adicional
            tvAdditionalInfo.setText("Viaje completado exitosamente. Cliente muy amable y puntual.");

            btnRepeatTrip.setVisibility(View.VISIBLE);

            Log.d(TAG, "setupDummyData: Datos de ejemplo configurados");

        } catch (Exception e) {
            Log.e(TAG, "Error al configurar datos de ejemplo", e);
        }
    }

    /**
     * Mostrar confirmación para repetir viaje
     */
    private void showRepeatTripConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Repetir Viaje")
                .setMessage("¿Deseas crear una solicitud similar a este viaje?")
                .setIcon(R.drawable.ic_refresh)
                .setPositiveButton("Sí, repetir", (dialog, which) -> {
                    // Aquí implementarías la lógica para crear una nueva solicitud
                    Toast.makeText(requireContext(), "Funcionalidad de repetir viaje será implementada próximamente", Toast.LENGTH_SHORT).show();

                    // Podrías navegar de vuelta y crear una nueva solicitud
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Mostrar diálogo para reportar problema
     */
    private void showReportIssueDialog() {
        String[] options = {
                "Problema con el pago",
                "Cliente no apareció",
                "Dirección incorrecta",
                "Problema con el vehículo",
                "Cliente irrespetuoso",
                "Otro problema"
        };

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Reportar Problema")
                .setIcon(R.drawable.ic_info)
                .setItems(options, (dialog, which) -> {
                    String selectedIssue = options[which];

                    // Mostrar confirmación del reporte
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("Reporte Enviado")
                            .setMessage("Tu reporte sobre '" + selectedIssue + "' ha sido enviado. Nuestro equipo lo revisará pronto.")
                            .setIcon(R.drawable.ic_check_circle)
                            .setPositiveButton("Entendido", null)
                            .show();

                    // Aquí implementarías la lógica para enviar el reporte al servidor
                    Log.d(TAG, "Problema reportado: " + selectedIssue + " para viaje: " +
                            (completedTrip != null ? completedTrip.getId() : "desconocido"));

                    // Ejemplo de datos que enviarías al servidor:
                    sendReportToServer(selectedIssue);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Enviar reporte al servidor (método de ejemplo)
     */
    private void sendReportToServer(String issueType) {
        // Aquí implementarías la llamada al API para enviar el reporte
        // Ejemplo de estructura de datos:
        /*
        ReportData report = new ReportData();
        report.setTripId(completedTrip != null ? completedTrip.getId() : "unknown");
        report.setIssueType(issueType);
        report.setDriverId(getCurrentDriverId());
        report.setTimestamp(System.currentTimeMillis());
        report.setStatus("pending");

        // Enviar al servidor
        ApiService.sendTripReport(report, new Callback<ReportResponse>() {
            @Override
            public void onSuccess(ReportResponse response) {
                Log.d(TAG, "Reporte enviado exitosamente: " + response.getReportId());
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al enviar reporte: " + error);
            }
        });
        */

        // Por ahora solo log
        Log.d(TAG, "Enviando reporte al servidor - Tipo: " + issueType);
    }

    /**
     * Obtener ID del conductor actual (método de ejemplo)
     */
    private String getCurrentDriverId() {
        // Aquí obtendrías el ID del conductor desde SharedPreferences o sesión
        return "driver001"; // Valor de ejemplo
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "Fragment paused");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias para evitar memory leaks
        imgHotelBanner = null;
        btnBack = null;
        btnRepeatTrip = null;
        btnReportIssue = null;

        Log.d(TAG, "Vista destruida y referencias limpiadas");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Fragment destruido");
    }
}