package com.example.proyecto_final_hoteleros.client.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.model.RoomType;

public class BookingSummaryFragment extends Fragment {

    private ImageButton btnBack;
    private TextView tvHotelName, tvHotelAddress, tvRoomPrice, tvRoomType, tvNumberOfGuests;
    private TextView tvCheckInOut, tvRoomNumber, tvFreeTransport;
    private TextView tvReservationPrice, tvAdditionalServices, tvTotalPrice;
    private Button btnConfirmAndPay;

    private String hotelName;
    private String hotelAddress;
    private RoomType selectedRoom;
    private String checkInDate;
    private String checkOutDate;
    private int numAdults;
    private int numChildren;
    private String roomNumber;
    private boolean hasFreeTransport;
    private double additionalServicesPrice;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_summary, container, false);

        // Inicializar vistas
        btnBack = view.findViewById(R.id.btn_back);
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        tvHotelAddress = view.findViewById(R.id.tv_hotel_address);
        tvRoomPrice = view.findViewById(R.id.tv_room_price);
        tvCheckInOut = view.findViewById(R.id.tv_check_in_out);
        tvNumberOfGuests = view.findViewById(R.id.tv_number_of_guests);
        tvRoomType = view.findViewById(R.id.tv_room_type);
        tvRoomNumber = view.findViewById(R.id.tv_room_number);
        tvFreeTransport = view.findViewById(R.id.tv_free_transport);
        tvReservationPrice = view.findViewById(R.id.tv_reservation_price);
        tvAdditionalServices = view.findViewById(R.id.tv_additional_services);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        btnConfirmAndPay = view.findViewById(R.id.btn_confirm_and_pay);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Recuperar datos pasados como argumentos
        if (getArguments() != null) {
            hotelName = getArguments().getString("hotel_name", "Belmond Miraflores Park");
            hotelAddress = getArguments().getString("hotel_address", "Miraflores, Lima, Perú");
            selectedRoom = getArguments().getParcelable("selected_room");
            checkInDate = getArguments().getString("check_in_date", "8 abril");
            checkOutDate = getArguments().getString("check_out_date", "9 abril");
            numAdults = getArguments().getInt("num_adults", 2);
            numChildren = getArguments().getInt("num_children", 0);
            roomNumber = getArguments().getString("room_number", "9634448852");
            hasFreeTransport = getArguments().getBoolean("has_free_transport", false);
            additionalServicesPrice = getArguments().getDouble("additional_services_price", 60.0);
        }

        // Configurar la vista con los datos recibidos
        setupData();

        // Configurar acciones de botones
        setupActions();
    }

    private void setupData() {
        // Hotel info
        tvHotelName.setText(hotelName);
        tvHotelAddress.setText(hotelAddress);

        // Room info
        if (selectedRoom != null) {
            tvRoomPrice.setText(selectedRoom.getPrice() + "/noche");
            tvRoomType.setText(selectedRoom.getName());
        } else {
            tvRoomPrice.setText("S/290/noche");
            tvRoomType.setText("Estándar");
        }

        // Stay info
        tvCheckInOut.setText(checkInDate + " - " + checkOutDate);
        tvNumberOfGuests.setText(numAdults + " Adultos - " + numChildren + " niños");
        tvRoomNumber.setText(roomNumber);
        tvFreeTransport.setText(hasFreeTransport ? "Sí" : "No");

        // Price details
        double roomPrice = selectedRoom != null ?
                Double.parseDouble(selectedRoom.getPrice().replace("S/", "").trim()) : 290.0;
        tvReservationPrice.setText("S/ " + String.format("%.2f", roomPrice));
        tvAdditionalServices.setText("S/ " + String.format("%.2f", additionalServicesPrice));

        double totalPrice = roomPrice + additionalServicesPrice;
        tvTotalPrice.setText("S/ " + String.format("%.2f", totalPrice));
    }

    private void setupActions() {
        // Botón de retroceso
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Botón de confirmar y pagar
        btnConfirmAndPay.setOnClickListener(v -> {
            // Aquí implementarías la navegación al fragmento de pago
            Toast.makeText(getContext(), "Procesando pago...", Toast.LENGTH_SHORT).show();
            // Navigation.findNavController(requireView()).navigate(R.id.action_bookingSummary_to_payment);
        });
    }
}