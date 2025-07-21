// client/ui/fragment/RoomSelectionFragment.java - SIMPLIFICADO: Sin complicaciones de timing
package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelProfile;
import com.example.proyecto_final_hoteleros.client.ui.activity.AllHotelServicesActivity;
import com.example.proyecto_final_hoteleros.client.ui.adapters.RoomTypeAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.RoomType;
import com.example.proyecto_final_hoteleros.client.utils.ClientRoomManager;
import com.example.proyecto_final_hoteleros.client.ui.fragment.BookingSummaryFragment;

import java.util.ArrayList;
import java.util.List;

public class RoomSelectionFragment extends Fragment {

    private static final String TAG = "RoomSelectionFragment";

    // Views
    private RecyclerView rvRoomTypes;
    private Button btnNextStep;
    private TextView tvHotelName;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;
    private TextView tvEmptyMessage;

    // Datos
    private RoomTypeAdapter adapter;
    private String hotelName;
    private String hotelPrice;
    private HotelProfile currentHotel;
    private List<RoomType> roomTypes;

    // Firebase
    private ClientRoomManager clientRoomManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.client_fragment_room_selection, container, false);

        // Inicializar vistas
        initViews(view);

        // Inicializar Firebase manager
        clientRoomManager = ClientRoomManager.getInstance(getContext());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener datos del hotel desde los argumentos
        extractHotelDataFromArguments();

        // Configurar el RecyclerView con servicios reales
        setupRecyclerView();

        // Configurar botones y eventos
        setupActions();

        // Cargar habitaciones reales
        loadRealHotelRooms();
    }

    // ========== INICIALIZACIÓN ==========

    private void initViews(View view) {
        rvRoomTypes = view.findViewById(R.id.rv_room_types);
        btnNextStep = view.findViewById(R.id.btn_next_step);
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        btnBack = view.findViewById(R.id.btn_back);

        // Nuevas vistas para estados de carga
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        // Si no existen en el layout, crearlas programáticamente (fallback)
        if (progressBar == null) {
            progressBar = new ProgressBar(getContext());
            progressBar.setVisibility(View.GONE);
        }
        if (layoutEmptyState == null) {
            layoutEmptyState = new LinearLayout(getContext());
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void extractHotelDataFromArguments() {
        if (getArguments() != null) {
            hotelName = getArguments().getString("hotel_name", "Hotel");
            hotelPrice = getArguments().getString("hotel_price", "S/0");

            // ✅ Obtener el HotelProfile completo si está disponible
            currentHotel = getArguments().getParcelable("hotel_profile");

            // Si no hay HotelProfile pero sí hay hotelAdminId, creamos uno básico
            if (currentHotel == null) {
                String hotelAdminId = getArguments().getString("hotel_admin_id");
                if (hotelAdminId != null) {
                    currentHotel = new HotelProfile();
                    currentHotel.setHotelAdminId(hotelAdminId);
                    currentHotel.setName(hotelName);
                    Log.d(TAG, "✅ HotelProfile creado desde hotelAdminId: " + hotelAdminId);
                }
            }

            tvHotelName.setText(hotelName);

            Log.d(TAG, "✅ Datos del hotel extraídos - Nombre: " + hotelName +
                    ", HotelProfile: " + (currentHotel != null ? "disponible (AdminId: " + currentHotel.getHotelAdminId() + ")" : "no disponible"));
        }
    }

    // ✅ SIMPLE: Crear adapter y configurar RecyclerView
    private void setupRecyclerView() {
        roomTypes = new ArrayList<>();

        // ✅ Crear adapter con currentHotel para que cargue servicios automáticamente
        adapter = new RoomTypeAdapter(roomTypes, position -> {
            // Callback cuando se selecciona una habitación
            if (position >= 0 && position < roomTypes.size()) {
                RoomType selectedRoom = roomTypes.get(position);
                Toast.makeText(getContext(), "Habitación seleccionada: " + selectedRoom.getName(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "🏨 Habitación seleccionada: " + selectedRoom.getName() + " - " + selectedRoom.getPrice());
            }
        }, getContext(), currentHotel); // ✅ PASAR currentHotel para carga automática de servicios

        rvRoomTypes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRoomTypes.setAdapter(adapter);

        Log.d(TAG, "✅ RecyclerView configurado con servicios automáticos");
    }

    private void setupActions() {
        // Botón de retroceso
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Botón de siguiente paso
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

    // ========== CARGA DE HABITACIONES ==========

    /**
     * ✅ SIMPLE: Cargar habitaciones del hotel desde Firebase
     */
    private void loadRealHotelRooms() {
        if (currentHotel == null || currentHotel.getHotelAdminId() == null) {
            Log.w(TAG, "⚠️ No se puede cargar habitaciones: HotelProfile o hotelAdminId no disponible");
            showEmptyState("No se puede cargar habitaciones del hotel");
            return;
        }

        showLoading();
        String hotelAdminId = currentHotel.getHotelAdminId();

        Log.d(TAG, "🔄 Cargando habitaciones reales del hotel: " + hotelAdminId);

        clientRoomManager.getHotelRooms(hotelAdminId, new ClientRoomManager.RoomsCallback() {
            @Override
            public void onSuccess(List<RoomType> rooms) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        hideLoading();

                        if (rooms.isEmpty()) {
                            showEmptyState("Este hotel aún no ha configurado sus habitaciones");
                        } else {
                            roomTypes.clear();
                            roomTypes.addAll(rooms);
                            adapter.notifyDataSetChanged();
                            showRoomsList();

                            Log.d(TAG, "✅ " + rooms.size() + " habitaciones cargadas exitosamente");
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        hideLoading();
                        showEmptyState("Error cargando habitaciones: " + error);
                        Log.e(TAG, "❌ Error cargando habitaciones: " + error);
                    });
                }
            }
        });
    }

    // ========== ESTADOS DE UI ==========

    private void showLoading() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        if (rvRoomTypes != null) rvRoomTypes.setVisibility(View.GONE);
    }

    private void hideLoading() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    private void showEmptyState(String message) {
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
        if (tvEmptyMessage != null) tvEmptyMessage.setText(message);
        if (rvRoomTypes != null) rvRoomTypes.setVisibility(View.GONE);
    }

    private void showRoomsList() {
        if (rvRoomTypes != null) rvRoomTypes.setVisibility(View.VISIBLE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
    }

    // ========== NAVEGACIÓN ==========

    private void navigateToServiceSelection() {
        RoomType selectedRoom = adapter.getSelectedPosition() != -1 ?
                roomTypes.get(adapter.getSelectedPosition()) : null;

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

        // Agregar precio numérico para cálculos del taxi
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
                roomTypes.get(adapter.getSelectedPosition()) : null;

        if (selectedRoom == null) return;

        Bundle args = new Bundle();
        args.putString("hotel_name", hotelName);
        args.putString("hotel_address", currentHotel != null ? currentHotel.getAddress() : "Dirección no disponible");
        args.putParcelable("selected_room", selectedRoom);
        args.putString("check_in_date", "8 abril");
        args.putString("check_out_date", "9 abril");
        args.putInt("num_adults", 2);
        args.putInt("num_children", 0);
        args.putString("room_number", generateRandomRoomNumber());

        // Calcular automáticamente si taxi es gratis
        double roomPrice = getRoomPriceValue(selectedRoom);
        boolean isTaxiFree = selectedServices != null && selectedServices.contains("taxi") && roomPrice >= 350.0;
        args.putBoolean("has_free_transport", isTaxiFree);

        // Pasar servicios seleccionados
        args.putString("selected_services", selectedServices);

        // Calcular precio de servicios adicionales
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

        // Aquí puedes agregar lógica para otros servicios en el futuro
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