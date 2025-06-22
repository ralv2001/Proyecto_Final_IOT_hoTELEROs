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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.taxista.model.SolicitudViaje;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.Locale;

public class TripDetailsFragment extends Fragment {

    private static final String TAG = "TripDetailsFragment";
    private SolicitudViaje solicitudViaje;

    // Vistas principales
    private ImageButton btnBack;
    private TextView toolbarTitle;
    private ImageView imgHotelBanner;

    // Información del hotel
    private TextView tvHotelName, tvHotelAddress, tvRating;

    // Información del servicio
    private TextView tvCheckInOut, tvNumberOfGuests, tvRoomType, tvRoomNumber, tvFreeTransport;

    // Direcciones
    private TextView tvOriginAddress, tvDestinationAddress;

    // Información de pago
    private TextView tvRoomPriceValue, tvAdditionalServices, tvTotalPrice;

    // Información adicional
    private TextView tvAdditionalInfo;

    // Botón de confirmación y diálogo
    private MaterialButton btnConfirmReservation;
    private ConstraintLayout confirmationDialogOverlay;
    private MaterialCardView confirmationDialog;
    private MaterialButton btnOk;

    /**
     * Constructor que recibe la solicitud de viaje como Parcelable
     */
    public static TripDetailsFragment newInstance(SolicitudViaje solicitudViaje) {
        Log.d(TAG, "newInstance: Creando fragmento para " + solicitudViaje.getId());
        TripDetailsFragment fragment = new TripDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable("solicitud", solicitudViaje);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Inflando vista de detalles del viaje");
        View view = inflater.inflate(R.layout.taxi_fragment_trip_details, container, false);

        initializeViews(view);
        setupListeners();

        // Recuperar solicitud utilizando Parcelable
        if (getArguments() != null) {
            Log.d(TAG, "onCreateView: Obteniendo argumentos");
            solicitudViaje = getArguments().getParcelable("solicitud");
            if (solicitudViaje != null) {
                Log.d(TAG, "onCreateView: Solicitud recuperada con ID: " + solicitudViaje.getId());
                setupDataFromSolicitud();
            } else {
                Log.w(TAG, "onCreateView: No se pudo recuperar la solicitud, usando datos de ejemplo");
                setupDummyData();
            }
        } else {
            Log.w(TAG, "onCreateView: No hay argumentos, usando datos de ejemplo");
            setupDummyData();
        }

        return view;
    }

    /**
     * Inicializar todas las vistas según el XML de la plantilla
     */
    private void initializeViews(View view) {
        // Toolbar y elementos principales
        btnBack = view.findViewById(R.id.btn_back);
        toolbarTitle = view.findViewById(R.id.toolbar_title);
        imgHotelBanner = view.findViewById(R.id.img_hotel_banner);

        // Información del hotel
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        tvHotelAddress = view.findViewById(R.id.tv_hotel_address);
        tvRating = view.findViewById(R.id.tv_rating);

        // Información del servicio
        tvCheckInOut = view.findViewById(R.id.tv_check_in_out);
        tvNumberOfGuests = view.findViewById(R.id.tv_number_of_guests);
        tvRoomType = view.findViewById(R.id.tv_room_type);
        tvRoomNumber = view.findViewById(R.id.tv_room_number);
        tvFreeTransport = view.findViewById(R.id.tv_free_transport);

        // Direcciones
        tvOriginAddress = view.findViewById(R.id.tv_origin_address);
        tvDestinationAddress = view.findViewById(R.id.tv_destination_address);

        // Información de pago
        tvRoomPriceValue = view.findViewById(R.id.tv_room_price_value);
        tvAdditionalServices = view.findViewById(R.id.tv_additional_services);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);

        // Información adicional
        tvAdditionalInfo = view.findViewById(R.id.tv_additional_info);

        // Botón de confirmación y diálogo
        btnConfirmReservation = view.findViewById(R.id.btn_confirm_reservation);
        confirmationDialogOverlay = view.findViewById(R.id.confirmation_dialog_overlay);
        confirmationDialog = view.findViewById(R.id.confirmation_dialog);
        btnOk = view.findViewById(R.id.btn_ok);

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

        // Botón para aceptar el viaje
        btnConfirmReservation.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Botón aceptar viaje presionado");
            showConfirmationDialog();
        });

        // Botón OK del diálogo de confirmación
        if (btnOk != null) {
            btnOk.setOnClickListener(v -> {
                Log.d(TAG, "onClick: Botón OK del diálogo presionado");
                hideConfirmationDialog();
                Toast.makeText(requireContext(), "Viaje aceptado exitosamente", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        }

        // Permitir cerrar el diálogo haciendo clic fuera de él
        if (confirmationDialogOverlay != null) {
            confirmationDialogOverlay.setOnClickListener(v -> {
                hideConfirmationDialog();
            });
        }

        // Evitar que clics en el diálogo cierren el overlay
        if (confirmationDialog != null) {
            confirmationDialog.setOnClickListener(v -> {
                // No hacer nada, solo consumir el clic
            });
        }
    }

    /**
     * Configurar datos usando la información de la solicitud real
     */
    private void setupDataFromSolicitud() {
        try {
            // Configurar datos del hotel
            tvHotelName.setText(solicitudViaje.getHotelName());
            tvHotelAddress.setText(solicitudViaje.getHotelAddress());
            tvRating.setText(String.valueOf(solicitudViaje.getRating()));

            // Cargar imagen del hotel con Glide
            Glide.with(this)
                    .load(solicitudViaje.getImageUrl())
                    .placeholder(R.drawable.belmond)
                    .error(R.drawable.belmond)
                    .into(imgHotelBanner);

            // Configurar información del servicio
            // Formatear la fecha de manera más amigable
            String fechaFormateada = formatearFecha(solicitudViaje.getDates());
            tvCheckInOut.setText(fechaFormateada);

            tvNumberOfGuests.setText("2 personas"); // Podrías agregar este campo a SolicitudViaje
            tvRoomType.setText(solicitudViaje.getClientName());
            tvRoomNumber.setText(solicitudViaje.getEstimatedTime() + " minutos");
            tvFreeTransport.setText("Traslado hotel-aeropuerto");

            // Configurar direcciones
            if (tvOriginAddress != null) {
                tvOriginAddress.setText(solicitudViaje.getOriginAddress());
            }
            if (tvDestinationAddress != null) {
                tvDestinationAddress.setText(solicitudViaje.getDestinationAddress());
            }

            // Configurar precios
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
            format.setMaximumFractionDigits(2);
            String formattedPrice = format.format(solicitudViaje.getPrice()).replace("PEN", "S/");

            tvRoomPriceValue.setText(formattedPrice);
            tvAdditionalServices.setText("Pago directo");
            tvTotalPrice.setText(formattedPrice);

            // Información adicional
            if (tvAdditionalInfo != null && solicitudViaje.getNotes() != null) {
                tvAdditionalInfo.setText(solicitudViaje.getNotes());
            }

            Log.d(TAG, "setupDataFromSolicitud: Datos configurados exitosamente");

        } catch (Exception e) {
            Log.e(TAG, "Error al configurar datos de la solicitud", e);
            setupDummyData(); // Fallback a datos de ejemplo
        }
    }

    /**
     * Formatear fecha para mostrar de manera más amigable
     */
    private String formatearFecha(String fechaOriginal) {
        try {
            // Si la fecha viene como "15 - 18 Mar", extraer solo la primera parte
            if (fechaOriginal != null && fechaOriginal.contains(" - ")) {
                String[] partes = fechaOriginal.split(" - ");
                return "Hoy, " + partes[0];
            }
            return "Hoy, " + fechaOriginal;
        } catch (Exception e) {
            return "Hoy, 14:30";
        }
    }

    /**
     * Configurar datos de ejemplo cuando no hay solicitud disponible
     */
    private void setupDummyData() {
        try {
            // Configurar datos del hotel
            tvHotelName.setText("Hotel Grand Plaza");
            tvHotelAddress.setText("San Miguel, Lima, Perú");
            tvRating.setText("4.8");

            // Cargar imagen del hotel
            Glide.with(this)
                    .load("https://cf.bstatic.com/xdata/images/hotel/max1024x768/237363319.jpg")
                    .placeholder(R.drawable.belmond)
                    .error(R.drawable.belmond)
                    .into(imgHotelBanner);

            // Configurar información del servicio
            tvCheckInOut.setText("Hoy, 14:30");
            tvNumberOfGuests.setText("2 personas");
            tvRoomType.setText("Juan Pérez");
            tvRoomNumber.setText("20 minutos");
            tvFreeTransport.setText("Traslado hotel-aeropuerto");

            // Configurar direcciones
            if (tvOriginAddress != null) {
                tvOriginAddress.setText("Hotel Grand Plaza, Av. Principal 123, San Miguel");
            }
            if (tvDestinationAddress != null) {
                tvDestinationAddress.setText("Aeropuerto Internacional Jorge Chávez, Callao");
            }

            // Configurar precios
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
            format.setMaximumFractionDigits(2);
            String formattedPrice = format.format(85.0).replace("PEN", "S/");

            tvRoomPriceValue.setText(formattedPrice);
            tvAdditionalServices.setText("Pago directo");
            tvTotalPrice.setText(formattedPrice);

            // Información adicional
            if (tvAdditionalInfo != null) {
                tvAdditionalInfo.setText("El cliente espera en el lobby del hotel. Tiene 2 maletas grandes. Se dirige al aeropuerto para tomar un vuelo internacional.");
            }

            Log.d(TAG, "setupDummyData: Datos de ejemplo configurados");

        } catch (Exception e) {
            Log.e(TAG, "Error al configurar datos de ejemplo", e);
        }
    }

    /**
     * Mostrar diálogo de confirmación con animación
     */
    private void showConfirmationDialog() {
        if (confirmationDialogOverlay != null) {
            confirmationDialogOverlay.setVisibility(View.VISIBLE);

            // Opcional: Añadir animación de entrada
            if (isAdded()) {
                try {
                    Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in);
                    confirmationDialogOverlay.startAnimation(fadeIn);

                    if (confirmationDialog != null) {
                        Animation slideUp = AnimationUtils.loadAnimation(requireContext(), android.R.anim.slide_in_left);
                        confirmationDialog.startAnimation(slideUp);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al animar diálogo", e);
                }
            }
        }
    }

    /**
     * Ocultar diálogo de confirmación con animación
     */
    private void hideConfirmationDialog() {
        if (confirmationDialogOverlay != null) {
            // Opcional: Añadir animación de salida
            if (isAdded()) {
                try {
                    Animation fadeOut = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_out);
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if (confirmationDialogOverlay != null) {
                                confirmationDialogOverlay.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                    confirmationDialogOverlay.startAnimation(fadeOut);
                } catch (Exception e) {
                    Log.e(TAG, "Error al animar salida del diálogo", e);
                    confirmationDialogOverlay.setVisibility(View.GONE);
                }
            } else {
                confirmationDialogOverlay.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias para evitar memory leaks
        imgHotelBanner = null;
        confirmationDialogOverlay = null;
        confirmationDialog = null;
    }
}