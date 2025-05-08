package com.example.proyecto_final_hoteleros.client.fragment;

import android.os.Bundle;
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
import androidx.navigation.Navigation;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.model.RoomType;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class BookingSummaryFragment extends Fragment {

    // UI Components
    private ImageButton btnBack;
    private ImageView imgHotelBanner;
    private TextView tvHotelName, tvHotelAddress, tvRating;
    private TextView tvCheckInOut, tvNumberOfGuests, tvRoomType, tvRoomNumber, tvFreeTransport;
    private TextView tvRoomPriceValue, tvAdditionalServices, tvTotalPrice;
    private MaterialButton btnConfirmAndPay;

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

        // Action Button
        btnConfirmAndPay = view.findViewById(R.id.btn_confirm_and_pay);
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

        // Botón de confirmar reserva
        btnConfirmAndPay.setOnClickListener(v -> confirmBooking());
    }

    /**
     * Procesa la confirmación de la reserva
     */
    private void confirmBooking() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirmar Reserva")
                .setMessage("Su tarjeta ha sido registrada como garantía. El cobro de S/ " +
                        String.format(Locale.getDefault(), "%.2f", totalPrice) +
                        " se realizará durante el checkout.\n\n¿Desea confirmar la reserva?")
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    // Aquí se implementaría la lógica de guardar la reserva en la base de datos
                    Snackbar.make(
                                    requireView(),
                                    "¡Reserva confirmada con éxito! Confirmación enviada a su correo.",
                                    Snackbar.LENGTH_LONG
                            )
                            .setAction("Ver Detalle", v -> navigateToBookingDetails())
                            .show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
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
        int room = random.nextInt(20) + 1;  // Habitación 1-20
        return String.format(Locale.getDefault(), "%d%02d", floor, room);
    }
}