package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.activity.AllHotelServicesActivity;
import com.example.proyecto_final_hoteleros.client.ui.adapters.RoomTypeAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.RoomType;

import java.util.ArrayList;
import java.util.List;

public class RoomSelectionFragment extends Fragment {

    private RecyclerView rvRoomTypes;
    private Button btnNextStep;
    private TextView tvHotelName;
    private ImageButton btnBack;
    private RoomTypeAdapter adapter;
    private String hotelName;
    private String hotelPrice;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.client_fragment_room_selection, container, false);

        // Inicializar vistas
        rvRoomTypes = view.findViewById(R.id.rv_room_types);
        btnNextStep = view.findViewById(R.id.btn_next_step);
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        btnBack = view.findViewById(R.id.btn_back);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener datos del hotel desde los argumentos
        if (getArguments() != null) {
            hotelName = getArguments().getString("hotel_name", "Belmond Miraflores Park");
            hotelPrice = getArguments().getString("hotel_price", "S/290");
            tvHotelName.setText(hotelName);
        }

        // Configurar el RecyclerView
        setupRoomTypes();

        // Configurar botones y eventos
        setupActions();
    }

    private void setupRoomTypes() {
        // Crear lista de tipos de habitación con servicios incluidos
        List<RoomType> roomTypes = new ArrayList<>();

        // Habitación Estándar
        List<String> standardServices = new ArrayList<>();
        standardServices.add("wifi");
        standardServices.add("reception");
        standardServices.add("parking");

        List<String> standardFeatures = new ArrayList<>();
        standardFeatures.add("TV de 32 pulgadas");
        standardFeatures.add("Aire acondicionado");
        standardFeatures.add("Baño privado");

        roomTypes.add(new RoomType("Habitación Estándar", 30, "S/290", R.drawable.belmond,
                standardServices, "Habitación cómoda con comodidades básicas", standardFeatures));

        // Habitación Deluxe
        List<String> deluxeServices = new ArrayList<>();
        deluxeServices.add("wifi");
        deluxeServices.add("reception");
        deluxeServices.add("parking");
        deluxeServices.add("minibar");

        List<String> deluxeFeatures = new ArrayList<>();
        deluxeFeatures.add("TV de 42 pulgadas");
        deluxeFeatures.add("Baño con bañera");
        deluxeFeatures.add("Vista a la ciudad");
        deluxeFeatures.add("Minibar incluido");

        roomTypes.add(new RoomType("Habitación Deluxe", 40, "S/350", R.drawable.belmond,
                deluxeServices, "Habitación espaciosa con amenidades premium", deluxeFeatures));

        // Suite Junior
        List<String> juniorServices = new ArrayList<>();
        juniorServices.add("wifi");
        juniorServices.add("reception");
        juniorServices.add("parking");
        juniorServices.add("minibar");
        juniorServices.add("room_service");

        List<String> juniorFeatures = new ArrayList<>();
        juniorFeatures.add("Sala de estar separada");
        juniorFeatures.add("Baño con jacuzzi");
        juniorFeatures.add("Vista panorámica");
        juniorFeatures.add("Servicio 24/7");

        roomTypes.add(new RoomType("Suite Junior", 50, "S/450", R.drawable.belmond,
                juniorServices, "Suite elegante con área de estar independiente", juniorFeatures));

        // Suite Presidencial
        List<String> presServices = new ArrayList<>();
        presServices.add("wifi");
        presServices.add("reception");
        presServices.add("parking");
        presServices.add("minibar");
        presServices.add("room_service");
        presServices.add("laundry");

        List<String> presFeatures = new ArrayList<>();
        presFeatures.add("Dormitorio principal");
        presFeatures.add("Sala amplia");
        presFeatures.add("Balcón privado");
        presFeatures.add("Servicio de mayordomía");

        roomTypes.add(new RoomType("Suite Presidencial", 70, "S/650", R.drawable.belmond,
                presServices, "La suite más lujosa con servicios exclusivos", presFeatures));

        // Configurar adapter
        adapter = new RoomTypeAdapter(roomTypes, position -> {
            // Callback cuando se selecciona una habitación
            RoomType selectedRoom = roomTypes.get(position);
            Toast.makeText(getContext(), "Habitación seleccionada: " + selectedRoom.getName(), Toast.LENGTH_SHORT).show();
        });

        rvRoomTypes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRoomTypes.setAdapter(adapter);
    }

    private void setupActions() {
        // Botón de retroceso
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Botón de siguiente paso - NUEVO FLUJO
        btnNextStep.setOnClickListener(v -> {
            // Verificar si hay una habitación seleccionada
            if (adapter.getSelectedPosition() == -1) {
                Toast.makeText(getContext(), "Por favor selecciona una habitación", Toast.LENGTH_SHORT).show();
                return;
            }

            // Navegar a la selección de servicios adicionales
            navigateToServiceSelection();
        });
    }

    private void navigateToServiceSelection() {
        RoomType selectedRoom = adapter.getSelectedPosition() != -1 ?
                adapter.getRoomTypes().get(adapter.getSelectedPosition()) : null;

        if (selectedRoom == null) {
            Toast.makeText(getContext(), "Por favor selecciona una habitación", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), AllHotelServicesActivity.class);
        intent.putExtra("hotel_name", hotelName);
        intent.putExtra("selected_room_name", selectedRoom.getName());
        intent.putExtra("selected_room_price", selectedRoom.getPrice());
        intent.putExtra("selected_room_features", selectedRoom.getFeatures().toArray(new String[0]));
        intent.putExtra("included_service_ids", selectedRoom.getIncludedServiceIds().toArray(new String[0]));
        intent.putExtra("mode", "service_selection");

        // ✅ AGREGAR precio numérico para cálculos del taxi
        double roomPriceNumeric = 0.0;
        try {
            roomPriceNumeric = Double.parseDouble(selectedRoom.getPrice().replace("S/", "").trim());
        } catch (NumberFormatException e) {
            roomPriceNumeric = 290.0; // Fallback
        }
        intent.putExtra("room_price_numeric", roomPriceNumeric);

        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == getActivity().RESULT_OK) {
            // El usuario completó la selección de servicios, ir al resumen
            if (data != null) {
                String selectedServices = data.getStringExtra("SELECTED_SERVICES");
                navigateToBookingWithServices(selectedServices);
            }
        }
    }

    private void navigateToBookingWithServices(String selectedServices) {
        RoomType selectedRoom = adapter.getSelectedPosition() != -1 ?
                adapter.getRoomTypes().get(adapter.getSelectedPosition()) : null;

        if (selectedRoom == null) return;

        Bundle args = new Bundle();
        args.putString("hotel_name", hotelName);
        args.putString("hotel_address", "Miraflores, Lima, Perú");
        args.putParcelable("selected_room", selectedRoom);
        args.putString("check_in_date", "8 abril");
        args.putString("check_out_date", "9 abril");
        args.putInt("num_adults", 2);
        args.putInt("num_children", 0);
        args.putString("room_number", generateRandomRoomNumber());

        // ✅ CALCULAR automáticamente si taxi es gratis
        double roomPrice = getRoomPriceValue(selectedRoom);
        boolean isTaxiFree = selectedServices != null && selectedServices.contains("taxi") && roomPrice >= 350.0;
        args.putBoolean("has_free_transport", isTaxiFree);

        // ✅ PASAR servicios seleccionados
        args.putString("selected_services", selectedServices);

        // ✅ CALCULAR precio de servicios adicionales
        double additionalPrice = calculateAdditionalServicesPrice(selectedServices, roomPrice);
        args.putDouble("additional_services_price", additionalPrice);

        BookingSummaryFragment bookingSummaryFragment = new BookingSummaryFragment();
        bookingSummaryFragment.setArguments(args);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, bookingSummaryFragment)
                .addToBackStack(null)
                .commit();
    }
    private double getRoomPriceValue(RoomType room) {
        try {
            return Double.parseDouble(room.getPrice().replace("S/", "").trim());
        } catch (NumberFormatException e) {
            return 290.0;
        }
    }
    private double calculateAdditionalServicesPrice(String selectedServices, double roomPrice) {
        double total = 0.0;

        if (selectedServices != null && selectedServices.contains("taxi")) {
            // Solo agregar precio del taxi si no es gratis
            if (roomPrice < 350.0) {
                total += 60.0;
            }
            // Si roomPrice >= 350.0, el taxi es gratis, no se agrega al total
        }

        // ✅ AQUÍ puedes agregar lógica para otros servicios en el futuro
        // if (selectedServices.contains("spa")) total += 120.0;
        // if (selectedServices.contains("breakfast")) total += 45.0;

        return total;
    }

    private String generateRandomRoomNumber() {
        return String.valueOf(System.currentTimeMillis()).substring(7);
    }

    // Interface para comunicarse con el adapter
    public interface OnRoomSelectedListener {
        void onRoomSelected(int position);
    }
}