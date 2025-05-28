package com.example.proyecto_final_hoteleros.client.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.model.RoomType;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class BookingSummaryFragment extends Fragment implements AddPaymentDialogFragment.PaymentDialogListener {
    // UI Components
    private ImageButton btnBack;
    private ImageView imgHotelBanner;
    private TextView tvHotelName, tvHotelAddress, tvRating;
    private TextView tvCheckInOut, tvNumberOfGuests, tvRoomType, tvRoomNumber, tvFreeTransport;
    private TextView tvRoomPriceValue, tvAdditionalServices, tvTotalPrice;
    private MaterialButton btnConfirmReservation;
    private MaterialButton btnAddPaymentMethod;
    private MaterialButton btnChangeCard;
    private ConstraintLayout layoutCardInfo;
    private TextView tvCardNumber, tvCardName, tvPaymentInfo;
    private ConstraintLayout confirmationDialogOverlay;
    private MaterialButton btnOk;

    // Variables para gestionar el estado
    private boolean isPaymentMethodAdded = false;

    // Data
    private String hotelName;
    private String hotelAddress;
    private double hotelRating;
    private RoomType selectedRoom;
    private String checkInDate;
    private String checkOutDate;
    private int numAdults;
    private int numChildren;
    private String roomNumber;
    private boolean hasFreeTransport;
    private double roomPrice;
    private double additionalServicesPrice;
    private double totalPrice;
    private int hotelImageResource;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_summary, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        retrieveArguments();
        setupData();
        setupActions();

        // Inicialmente, el botón de confirmar está deshabilitado
        updateConfirmButtonState();
    }

    /**
     * Inicializa todas las vistas del fragmento
     */
    private void initViews(View view) {
        // AppBar
        btnBack = view.findViewById(R.id.btn_back);
        // Hotel Info Card
        imgHotelBanner = view.findViewById(R.id.img_hotel_banner);
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        tvHotelAddress = view.findViewById(R.id.tv_hotel_address);
        tvRating = view.findViewById(R.id.tv_rating);
        // Stay Info Card
        tvCheckInOut = view.findViewById(R.id.tv_check_in_out);
        tvNumberOfGuests = view.findViewById(R.id.tv_number_of_guests);
        tvRoomType = view.findViewById(R.id.tv_room_type);
        tvRoomNumber = view.findViewById(R.id.tv_room_number);
        tvFreeTransport = view.findViewById(R.id.tv_free_transport);
        // Price Details Card
        tvRoomPriceValue = view.findViewById(R.id.tv_room_price_value);
        tvAdditionalServices = view.findViewById(R.id.tv_additional_services);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        // Payment Method Card
        btnAddPaymentMethod = view.findViewById(R.id.btn_add_payment_method);
        layoutCardInfo = view.findViewById(R.id.layout_card_info);
        tvCardNumber = view.findViewById(R.id.tv_card_number);
        tvCardName = view.findViewById(R.id.tv_card_name);
        btnChangeCard = view.findViewById(R.id.btn_change_card);
        tvPaymentInfo = view.findViewById(R.id.tv_payment_info);
        // Action Button
        btnConfirmReservation = view.findViewById(R.id.btn_confirm_reservation);
        // Confirmation Dialog Overlay
        confirmationDialogOverlay = view.findViewById(R.id.confirmation_dialog_overlay);
        btnOk = view.findViewById(R.id.btn_ok);
    }

    /**
     * Recupera los argumentos pasados al fragmento
     */
    private void retrieveArguments() {
        if (getArguments() != null) {
            // Hotel info
            hotelName = getArguments().getString("hotel_name", "Belmond Miraflores Park");
            hotelAddress = getArguments().getString("hotel_address", "Miraflores, Lima, Perú");
            hotelRating = getArguments().getFloat("hotel_rating", 4.9f);
            hotelImageResource = getArguments().getInt("hotel_image", R.drawable.belmond);
            // Room info
            selectedRoom = getArguments().getParcelable("selected_room");
            // Stay info
            checkInDate = getArguments().getString("check_in_date", "8 abril");
            checkOutDate = getArguments().getString("check_out_date", "9 abril");
            numAdults = getArguments().getInt("num_adults", 2);
            numChildren = getArguments().getInt("num_children", 0);
            // Room number - Generamos uno aleatorio si no se recibe
            roomNumber = getArguments().getString("room_number", generateRandomRoomNumber());
            // Additional services
            hasFreeTransport = getArguments().getBoolean("has_free_transport", false);
            additionalServicesPrice = getArguments().getDouble("additional_services_price", 60.0);
        } else {
            // Valores por defecto si no hay argumentos
            hotelName = "Belmond Miraflores Park";
            hotelAddress = "Miraflores, Lima, Perú";
            hotelRating = 4.9f;
            hotelImageResource = R.drawable.belmond;
            checkInDate = "8 abril";
            checkOutDate = "9 abril";
            numAdults = 2;
            numChildren = 0;
            roomNumber = generateRandomRoomNumber();
            hasFreeTransport = false;
            additionalServicesPrice = 60.0;
        }
    }

    /**
     * Configura la interfaz con los datos recibidos
     */
    private void setupData() {
        // Hotel info
        tvHotelName.setText(hotelName);
        tvHotelAddress.setText(hotelAddress);
        tvRating.setText(String.format(Locale.getDefault(), "%.1f", hotelRating));
        // Si tenemos un recurso de imagen específico, lo usamos
        if (hotelImageResource != 0) {
            imgHotelBanner.setImageResource(hotelImageResource);
        }
        // Room info y cálculo de precios
        if (selectedRoom != null) {
            tvRoomType.setText(selectedRoom.getName());
            // Extraer el precio numérico
            try {
                String priceStr = selectedRoom.getPrice().replace("S/", "").trim();
                roomPrice = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                roomPrice = 290.0; // Valor por defecto
            }
        } else {
            tvRoomType.setText("Estándar");
            roomPrice = 290.0;
        }
        // Configurar fecha de estancia
        tvCheckInOut.setText(String.format("%s - %s", checkInDate, checkOutDate));
        // Configurar huéspedes
        tvNumberOfGuests.setText(String.format("%d adultos - %d niños", numAdults, numChildren));
        // Configurar número de habitación
        tvRoomNumber.setText(roomNumber);
        // Configurar transporte gratuito
        tvFreeTransport.setText(hasFreeTransport ? "Sí" : "No");
        // Configurar precios
        tvRoomPriceValue.setText(String.format(Locale.getDefault(), "S/ %.2f", roomPrice));
        tvAdditionalServices.setText(String.format(Locale.getDefault(), "S/ %.2f", additionalServicesPrice));
        // Calcular precio total
        totalPrice = roomPrice + additionalServicesPrice;
        tvTotalPrice.setText(String.format(Locale.getDefault(), "S/ %.2f", totalPrice));
    }

    /**
     * Configura los eventos de los botones
     */
    private void setupActions() {
        // Botón de retroceso
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Botón para agregar método de pago (tarjeta)
        btnAddPaymentMethod.setOnClickListener(v -> showAddPaymentMethodDialog());

        // Botón para cambiar método de pago
        btnChangeCard.setOnClickListener(v -> showAddPaymentMethodDialog());

        // Botón de confirmar reserva
        btnConfirmReservation.setOnClickListener(v -> confirmBooking());

        // Botón OK del diálogo de confirmación
        btnOk.setOnClickListener(v -> {
            // Primero ocultamos el overlay con animación de fade
            confirmationDialogOverlay.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setInterpolator(new DecelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            confirmationDialogOverlay.setVisibility(View.GONE);
                            navigateToBookingDetails();
                        }
                    });
        });

        // Configurar el overlay para detectar clics
        confirmationDialogOverlay.setOnClickListener(null); // Anular comportamiento predeterminado

        // El diálogo no debe cerrarse al hacer clic fuera
        MaterialCardView confirmationDialog = confirmationDialogOverlay.findViewById(R.id.confirmation_dialog);
        confirmationDialog.setOnClickListener(v -> {
            // Consumir el evento para que no llegue al overlay
        });
    }

    /**
     * Actualiza el estado del botón de confirmar según si hay método de pago
     */
    private void updateConfirmButtonState() {
        btnConfirmReservation.setEnabled(isPaymentMethodAdded);
    }

    /**
     * Muestra el diálogo para agregar un método de pago
     */
    private void showAddPaymentMethodDialog() {
        AddPaymentDialogFragment dialogFragment = AddPaymentDialogFragment.newInstance();
        dialogFragment.setPaymentDialogListener(this);
        dialogFragment.show(getChildFragmentManager(), "AddPaymentDialog");
    }

    /**
     * Implementación del método de la interfaz PaymentDialogListener
     */
    @Override
    public void onPaymentMethodAdded(String cardNumber, String cardHolderName) {
        // Mostramos la información de la tarjeta
        tvCardNumber.setText(cardNumber);
        tvCardName.setText(cardHolderName);

        // Hacemos INVISIBLE (no GONE) el botón para mantener el espacio ocupado durante la animación
        btnAddPaymentMethod.setVisibility(View.INVISIBLE);

        // Hacemos visible el layout de información de tarjeta
        layoutCardInfo.setVisibility(View.VISIBLE);
        layoutCardInfo.setAlpha(0f);

        // Animamos la aparición de la información de la tarjeta
        layoutCardInfo.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Al finalizar la animación, ocultamos definitivamente el botón
                        btnAddPaymentMethod.setVisibility(View.GONE);

                        // Y ajustamos la posición del texto informativo
                        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tvPaymentInfo.getLayoutParams();
                        params.topToBottom = layoutCardInfo.getId();
                        tvPaymentInfo.setLayoutParams(params);
                    }
                });

        // Actualizamos el estado y habilitamos el botón de confirmar
        isPaymentMethodAdded = true;
        updateConfirmButtonState();

        // Mostrar mensaje de éxito
        Toast.makeText(requireContext(), "Tarjeta agregada correctamente", Toast.LENGTH_SHORT).show();
    }

    /**
     * Procesa la confirmación de la reserva
     */
    private void confirmBooking() {
        // Configuramos el texto en el diálogo de confirmación
        TextView tvSuccessMessage = confirmationDialogOverlay.findViewById(R.id.tv_success_message);
        tvSuccessMessage.setText(
                "Tu reserva en " + hotelName + " ha sido confirmada exitosamente. " +
                        "Recibirás un correo con los detalles de tu reserva."
        );

        // Mostramos el overlay con animación de fade in
        confirmationDialogOverlay.setVisibility(View.VISIBLE);
        confirmationDialogOverlay.setAlpha(0f);
        confirmationDialogOverlay.animate()
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(null);

        // Animación del diálogo de confirmación
        MaterialCardView confirmationDialog = confirmationDialogOverlay.findViewById(R.id.confirmation_dialog);
        confirmationDialog.setScaleX(0.7f);
        confirmationDialog.setScaleY(0.7f);
        confirmationDialog.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(null);
    }

    /**
     * Navega a la pantalla de detalles de la reserva
     */
    private void navigateToBookingDetails() {
        // Aquí implementarías la navegación hacia el fragmento de detalles de la reserva
        // Navigation.findNavController(requireView()).navigate(R.id.action_bookingSummary_to_bookingDetails);
        Toast.makeText(requireContext(), "Navegando a detalles de reserva...", Toast.LENGTH_SHORT).show();
    }

    /**
     * Genera un número de habitación aleatorio
     */
    private String generateRandomRoomNumber() {
        Random random = new Random();
        int floor = random.nextInt(10) + 1; // Piso 1-10
        int room = random.nextInt(20) + 1; // Habitación 1-20
        return String.format(Locale.getDefault(), "%d%02d", floor, room);
    }
}