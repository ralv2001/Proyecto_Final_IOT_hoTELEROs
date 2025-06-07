package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.button.MaterialButton;

public class ReservationDetailFragment extends Fragment {

    private ImageButton btnBack;
    private ImageView imgHotelBanner;
    private TextView tvHotelName, tvHotelAddress, tvRating;
    private TextView tvCheckInOut, tvNumberOfGuests, tvRoomType, tvRoomNumber;
    private TextView tvReservationId, tvReservationStatus;
    private MaterialButton btnPrimaryAction;

    // Datos de la reserva
    private String hotelName, hotelAddress, reservationId, roomType;
    private String checkInOut, numberOfGuests, roomNumber;
    private float rating;
    private int hotelImageResource;
    private boolean isViewMode, isInvoiceMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.client_fragment_reservation_detail, container, false);
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

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btn_back);
        imgHotelBanner = view.findViewById(R.id.img_hotel_banner);
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        tvHotelAddress = view.findViewById(R.id.tv_hotel_address);
        tvRating = view.findViewById(R.id.tv_rating);
        tvCheckInOut = view.findViewById(R.id.tv_check_in_out);
        tvNumberOfGuests = view.findViewById(R.id.tv_number_of_guests);
        tvRoomType = view.findViewById(R.id.tv_room_type);
        tvRoomNumber = view.findViewById(R.id.tv_room_number);
        tvReservationId = view.findViewById(R.id.tv_reservation_id);
        tvReservationStatus = view.findViewById(R.id.tv_reservation_status);
        btnPrimaryAction = view.findViewById(R.id.btn_primary_action);
    }

    private void retrieveArguments() {
        if (getArguments() != null) {
            hotelName = getArguments().getString("hotel_name", "Hotel");
            hotelAddress = getArguments().getString("hotel_address", "Ubicación");
            reservationId = getArguments().getString("reservation_id", "");
            roomType = getArguments().getString("room_type", "Estándar");
            checkInOut = getArguments().getString("check_in_out", "");
            numberOfGuests = getArguments().getString("number_of_guests", "");
            roomNumber = getArguments().getString("room_number", "");
            rating = getArguments().getFloat("hotel_rating", 4.5f);
            hotelImageResource = getArguments().getInt("hotel_image", R.drawable.belmond);
            isViewMode = getArguments().getBoolean("view_mode", false);
            isInvoiceMode = getArguments().getBoolean("invoice_mode", false);
        }
    }

    private void setupData() {
        tvHotelName.setText(hotelName);
        tvHotelAddress.setText(hotelAddress);
        tvRating.setText(String.valueOf(rating));
        tvCheckInOut.setText(checkInOut);
        tvNumberOfGuests.setText(numberOfGuests);
        tvRoomType.setText(roomType);
        tvRoomNumber.setText(roomNumber);
        tvReservationId.setText("Reserva: " + reservationId);

        if (hotelImageResource != 0) {
            imgHotelBanner.setImageResource(hotelImageResource);
        }

        // Configurar según el modo
        if (isInvoiceMode) {
            tvReservationStatus.setText("Estadía Completada");
            btnPrimaryAction.setText("Descargar Factura");
        } else if (isViewMode) {
            tvReservationStatus.setText("Próxima Reserva");
            btnPrimaryAction.setText("Modificar Reserva");
        }
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnPrimaryAction.setOnClickListener(v -> {
            if (isInvoiceMode) {
                // Descargar factura
                android.widget.Toast.makeText(getContext(), "Descargando factura...", android.widget.Toast.LENGTH_SHORT).show();
            } else {
                // Modificar reserva
                android.widget.Toast.makeText(getContext(), "Función disponible próximamente", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}