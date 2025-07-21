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

    // Vistas principales (mantener las existentes)
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (solicitudViaje != null) {
            configurarVistaSegunTipoServicio();
        }
    }

    // ========== NUEVOS MÉTODOS PARA CHECKOUT ==========

    /**
     * Configurar vista según el tipo de servicio
     */
    private void configurarVistaSegunTipoServicio() {
        if (solicitudViaje.isCheckoutService()) {
            Log.d(TAG, "🏨 Configurando vista para servicio de checkout");
            configurarVistaCheckout();
        } else {
            Log.d(TAG, "🚕 Configurando vista para viaje normal");
            configurarVistaNormal();
        }
    }

    /**
     * Personalizar la vista para servicios de checkout
     */
    private void configurarVistaCheckout() {
        try {
            // Cambiar título del toolbar
            if (toolbarTitle != null) {
                toolbarTitle.setText("🏨 Servicio de Checkout");
            }

            // Personalizar indicador de transporte gratuito
            if (tvFreeTransport != null) {
                tvFreeTransport.setText("🚕 SERVICIO GRATUITO DE CHECKOUT");
                tvFreeTransport.setTextColor(getResources().getColor(R.color.theme_green_primary));
                tvFreeTransport.setVisibility(View.VISIBLE);
            }

            // Cambiar el texto y color del botón de confirmación
            if (btnConfirmReservation != null) {
                btnConfirmReservation.setText("✅ ACEPTAR SERVICIO DE CHECKOUT");
                btnConfirmReservation.setBackgroundTintList(
                        getResources().getColorStateList(R.color.theme_green_primary)
                );
                btnConfirmReservation.setTextColor(getResources().getColor(android.R.color.white));
            }

            // Personalizar información del servicio usando campos existentes
            if (tvCheckInOut != null) {
                String checkoutInfo = "Checkout: " + (solicitudViaje.getCheckoutTime() != null ?
                        solicitudViaje.getCheckoutTime() : "Hoy");
                tvCheckInOut.setText(checkoutInfo);
            }

            if (tvRoomType != null) {
                tvRoomType.setText("Cliente: " + solicitudViaje.getClientName());
            }

            if (tvRoomNumber != null) {
                tvRoomNumber.setText("⏱️ " + solicitudViaje.getEstimatedTime() + " min al aeropuerto");
            }

            if (tvNumberOfGuests != null) {
                tvNumberOfGuests.setText("🎯 Tipo: Checkout gratuito");
            }

            // Mostrar información específica de checkout en el área de información adicional
            if (tvAdditionalInfo != null) {
                String infoCheckout = buildCheckoutInfo();
                tvAdditionalInfo.setText(infoCheckout);
                tvAdditionalInfo.setVisibility(View.VISIBLE);
            }

            // Personalizar información de precio (es gratis)
            if (tvRoomPriceValue != null) {
                tvRoomPriceValue.setText("💰 GRATUITO");
                tvRoomPriceValue.setTextColor(getResources().getColor(R.color.theme_green_primary));
            }

            if (tvAdditionalServices != null) {
                tvAdditionalServices.setText("🎁 Cortesía del hotel");
            }

            if (tvTotalPrice != null) {
                tvTotalPrice.setText("💰 S/ 0.00 - GRATUITO");
                tvTotalPrice.setTextColor(getResources().getColor(R.color.theme_green_primary));
                tvTotalPrice.setTextSize(18);
            }

            Log.d(TAG, "✅ Vista de checkout configurada exitosamente");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error configurando vista de checkout: " + e.getMessage(), e);
        }
    }

    /**
     * Construir información detallada para checkout
     */
    private String buildCheckoutInfo() {
        StringBuilder info = new StringBuilder();

        info.append("🏨 INFORMACIÓN DEL CHECKOUT:\n\n");
        info.append("• Tipo: Servicio gratuito hotel → aeropuerto\n");
        info.append("• Cliente: ").append(solicitudViaje.getClientName()).append("\n");

        if (solicitudViaje.getCheckoutTime() != null) {
            info.append("• Hora checkout: ").append(solicitudViaje.getCheckoutTime()).append("\n");
        }

        info.append("• Destino: ").append(solicitudViaje.getDestinationAddress()).append("\n");
        info.append("• Tiempo estimado: ").append(solicitudViaje.getEstimatedTime()).append(" minutos\n\n");

        info.append("📞 CONTACTO:\n");
        if (solicitudViaje.getClientPhone() != null) {
            info.append("• Teléfono cliente: ").append(solicitudViaje.getClientPhone()).append("\n");
        } else {
            info.append("• Teléfono cliente: Consultar en recepción\n");
        }

        info.append("• Hotel: ").append(solicitudViaje.getHotelName()).append("\n\n");

        info.append("📝 INSTRUCCIONES:\n");
        info.append("• Presentarse en el lobby del hotel\n");
        info.append("• Preguntar por el huésped en recepción\n");
        info.append("• Ayudar con el equipaje si es necesario\n");
        info.append("• Destino: Terminal de salidas del aeropuerto\n\n");

        if (solicitudViaje.getNotes() != null && !solicitudViaje.getNotes().isEmpty()) {
            info.append("💬 NOTAS ESPECIALES:\n");
            info.append(solicitudViaje.getNotes());
        }

        return info.toString();
    }

    /**
     * Mantener configuración original para viajes normales
     */
    private void configurarVistaNormal() {
        try {
            // Mantener configuración original
            if (toolbarTitle != null) {
                toolbarTitle.setText("Detalles del Viaje");
            }

            if (btnConfirmReservation != null) {
                btnConfirmReservation.setText("Aceptar Viaje");
                btnConfirmReservation.setBackgroundTintList(
                        getResources().getColorStateList(R.color.theme_orange_primary)
                );
            }

            Log.d(TAG, "✅ Vista normal configurada");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error configurando vista normal: " + e.getMessage(), e);
        }
    }

    // ========== MÉTODOS EXISTENTES (mantener tal como están) ==========

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

        // Configurar título por defecto
        if (toolbarTitle != null) {
            toolbarTitle.setText("Detalles del Viaje");
        }
    }

    // ========== PASO 5: AGREGAR BOTONES DE ACCIÓN ==========

    /**
     * Configurar listeners para botones y acciones
     */
    private void setupListeners() {
        // Botón de regresar
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Botón regresar presionado");
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Botón para aceptar el viaje - ACTUALIZADO PARA CHECKOUT
        btnConfirmReservation.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Botón aceptar presionado");

            if (solicitudViaje != null && solicitudViaje.isCheckoutService()) {
                // Es un servicio de checkout
                mostrarConfirmacionCheckout();
            } else {
                // Es un viaje normal
                showConfirmationDialog();
            }
        });

        // Botón OK del diálogo de confirmación - ACTUALIZADO
        if (btnOk != null) {
            btnOk.setOnClickListener(v -> {
                Log.d(TAG, "onClick: Botón OK del diálogo presionado");
                hideConfirmationDialog();

                if (solicitudViaje != null && solicitudViaje.isCheckoutService()) {
                    // Para checkout: regresar y notificar aceptación
                    aceptarServicioCheckout();
                } else {
                    // Para viaje normal: comportamiento original
                    Toast.makeText(requireContext(), "Viaje aceptado exitosamente", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        // Permitir cerrar el diálogo haciendo clic fuera de él
        if (confirmationDialogOverlay != null) {
            confirmationDialogOverlay.setOnClickListener(v -> hideConfirmationDialog());
        }

        // Evitar que clics en el diálogo cierren el overlay
        if (confirmationDialog != null) {
            confirmationDialog.setOnClickListener(v -> {
                // No hacer nada, solo consumir el clic
            });
        }
    }

    /**
     * Mostrar confirmación específica para checkout
     */
    private void mostrarConfirmacionCheckout() {
        try {
            androidx.appcompat.app.AlertDialog.Builder builder =
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext());

            String mensaje = "🏨 CONFIRMAR SERVICIO DE CHECKOUT\n\n" +
                    "Hotel: " + solicitudViaje.getHotelName() + "\n" +
                    "Cliente: " + solicitudViaje.getClientName() + "\n" +
                    "Destino: Aeropuerto Jorge Chávez\n" +
                    "Tiempo estimado: " + solicitudViaje.getEstimatedTime() + " min\n\n" +
                    "¿Confirmas que puedes realizar este servicio?";

            builder.setTitle("🚕 Servicio de Checkout")
                    .setMessage(mensaje)
                    .setPositiveButton("✅ Sí, acepto", (dialog, which) -> {
                        aceptarServicioCheckout();
                    })
                    .setNegativeButton("❌ Cancelar", null)
                    .setNeutralButton("📞 Ver contacto", (dialog, which) -> {
                        mostrarInformacionContacto();
                    })
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "Error mostrando confirmación de checkout: " + e.getMessage(), e);
            // Fallback al diálogo original
            showConfirmationDialog();
        }
    }

    /**
     * Aceptar servicio de checkout
     */
    private void aceptarServicioCheckout() {
        Log.d(TAG, "🚕 Aceptando servicio de checkout desde TripDetailsFragment");

        try {
            // ✅ NAVEGAR DIRECTAMENTE AL MAPA CON ARGUMENTOS:
            DriverMapFragment mapFragment = new DriverMapFragment();

            Bundle args = new Bundle();
            args.putString("destination_address", solicitudViaje.getDestinationAddress());
            args.putString("client_name", solicitudViaje.getClientName());
            args.putString("destination_name", solicitudViaje.getHotelName());
            args.putString("client_phone", solicitudViaje.getClientPhone());
            args.putString("service_type", "checkout_gratuito");
            mapFragment.setArguments(args);

            // Navegar al mapa
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, mapFragment)
                        .addToBackStack("service_active")
                        .commit();

                Toast.makeText(requireContext(),
                        "🚕 Servicio aceptado!\n📍 Dirígete a: " + solicitudViaje.getHotelName(),
                        Toast.LENGTH_LONG).show();

                Log.d(TAG, "✅ Navegación al mapa desde TripDetails completada");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error aceptando servicio de checkout: " + e.getMessage(), e);
            Toast.makeText(requireContext(),
                    "Error procesando solicitud. Inténtalo de nuevo.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Mostrar información de contacto
     */
    private void mostrarInformacionContacto() {
        try {
            String contacto = "📞 INFORMACIÓN DE CONTACTO\n\n";

            if (solicitudViaje.getClientPhone() != null) {
                contacto += "Cliente: " + solicitudViaje.getClientPhone() + "\n";
            }

            contacto += "Hotel: " + solicitudViaje.getHotelName() + "\n";
            contacto += "Dirección: " + solicitudViaje.getOriginAddress() + "\n\n";
            contacto += "💡 Tip: Puedes llamar al hotel para coordinar la recogida";

            androidx.appcompat.app.AlertDialog.Builder builder =
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext());

            builder.setTitle("📞 Contacto")
                    .setMessage(contacto)
                    .setPositiveButton("✅ Entendido", null)
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "Error mostrando información de contacto: " + e.getMessage(), e);
            Toast.makeText(requireContext(),
                    "Cliente: " + (solicitudViaje.getClientPhone() != null ? solicitudViaje.getClientPhone() : "Consultar en recepción"),
                    Toast.LENGTH_LONG).show();
        }
    }

    // ========== MÉTODOS EXISTENTES (mantener exactamente como están) ==========

    private void setupDataFromSolicitud() {
        try {
            // Configurar datos del hotel
            tvHotelName.setText(solicitudViaje.getHotelName());
            tvHotelAddress.setText(solicitudViaje.getHotelAddress());
            tvRating.setText(String.valueOf(solicitudViaje.getRating()));

            // Cargar imagen del hotel con Glide
            // Usar imagen local para evitar errores HTTP
            imgHotelBanner.setImageResource(R.drawable.belmond);

            // Configurar direcciones
            if (tvOriginAddress != null) {
                tvOriginAddress.setText(solicitudViaje.getOriginAddress());
            }
            if (tvDestinationAddress != null) {
                tvDestinationAddress.setText(solicitudViaje.getDestinationAddress());
            }

            // Solo configurar precios si NO es servicio de checkout
            if (!solicitudViaje.isCheckoutService()) {
                // Configurar información del servicio normal
                String fechaFormateada = formatearFecha(solicitudViaje.getDates());
                tvCheckInOut.setText(fechaFormateada);
                tvNumberOfGuests.setText("2 personas");
                tvRoomType.setText(solicitudViaje.getClientName());
                tvRoomNumber.setText(solicitudViaje.getEstimatedTime() + " minutos");
                tvFreeTransport.setText("Traslado hotel-aeropuerto");

                // Configurar precios normales
                NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
                format.setMaximumFractionDigits(2);
                String formattedPrice = format.format(solicitudViaje.getPrice()).replace("PEN", "S/");

                tvRoomPriceValue.setText(formattedPrice);
                tvAdditionalServices.setText("Pago directo");
                tvTotalPrice.setText(formattedPrice);

                // Información adicional normal
                if (tvAdditionalInfo != null && solicitudViaje.getNotes() != null) {
                    tvAdditionalInfo.setText(solicitudViaje.getNotes());
                }
            }

            Log.d(TAG, "setupDataFromSolicitud: Datos configurados exitosamente");

        } catch (Exception e) {
            Log.e(TAG, "Error al configurar datos de la solicitud", e);
            setupDummyData();
        }
    }

    private String formatearFecha(String fechaOriginal) {
        try {
            if (fechaOriginal != null && fechaOriginal.contains(" - ")) {
                String[] partes = fechaOriginal.split(" - ");
                return "Hoy, " + partes[0];
            }
            return "Hoy, " + fechaOriginal;
        } catch (Exception e) {
            return "Hoy, 14:30";
        }
    }

    private void setupDummyData() {
        // Mantener exactamente como estaba...
        try {
            tvHotelName.setText("Hotel Grand Plaza");
            tvHotelAddress.setText("San Miguel, Lima, Perú");
            tvRating.setText("4.8");

            Glide.with(this)
                    .load("https://cf.bstatic.com/xdata/images/hotel/max1024x768/237363319.jpg")
                    .placeholder(R.drawable.belmond)
                    .error(R.drawable.belmond)
                    .into(imgHotelBanner);

            tvCheckInOut.setText("Hoy, 14:30");
            tvNumberOfGuests.setText("2 personas");
            tvRoomType.setText("Juan Pérez");
            tvRoomNumber.setText("20 minutos");
            tvFreeTransport.setText("Traslado hotel-aeropuerto");

            if (tvOriginAddress != null) {
                tvOriginAddress.setText("Hotel Grand Plaza, Av. Principal 123, San Miguel");
            }
            if (tvDestinationAddress != null) {
                tvDestinationAddress.setText("Aeropuerto Internacional Jorge Chávez, Callao");
            }

            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
            format.setMaximumFractionDigits(2);
            String formattedPrice = format.format(85.0).replace("PEN", "S/");

            tvRoomPriceValue.setText(formattedPrice);
            tvAdditionalServices.setText("Pago directo");
            tvTotalPrice.setText(formattedPrice);

            if (tvAdditionalInfo != null) {
                tvAdditionalInfo.setText("El cliente espera en el lobby del hotel. Tiene 2 maletas grandes. Se dirige al aeropuerto para tomar un vuelo internacional.");
            }

            Log.d(TAG, "setupDummyData: Datos de ejemplo configurados");

        } catch (Exception e) {
            Log.e(TAG, "Error al configurar datos de ejemplo", e);
        }
    }

    private void showConfirmationDialog() {
        if (confirmationDialogOverlay != null) {
            confirmationDialogOverlay.setVisibility(View.VISIBLE);

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

    private void hideConfirmationDialog() {
        if (confirmationDialogOverlay != null) {
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
        imgHotelBanner = null;
        confirmationDialogOverlay = null;
        confirmationDialog = null;
    }
}