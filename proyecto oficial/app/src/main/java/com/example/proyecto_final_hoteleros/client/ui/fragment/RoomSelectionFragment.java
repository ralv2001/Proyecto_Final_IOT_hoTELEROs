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

        // ✅ SOLO ESTAS 6 LÍNEAS AÑADIDAS:
        if (currentHotel != null && currentHotel.getHotelAdminId() != null) {
            intent.putExtra("hotel_admin_id", currentHotel.getHotelAdminId());
            Log.d(TAG, "✅ Enviando hotel_admin_id: " + currentHotel.getHotelAdminId());
        } else {
            Log.w(TAG, "⚠️ No se pudo obtener hotel_admin_id");
        }

        startActivityForResult(intent, 100);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == getActivity().RESULT_OK) {
            // El usuario completó la selección de servicios, ir al resumen
            if (data != null) {
                String selectedServices = data.getStringExtra("SELECTED_SERVICES");
                double additionalServicesPrice = data.getDoubleExtra("ADDITIONAL_SERVICES_PRICE", 0.0); // ✅ OBTENER PRECIO REAL

                Log.d(TAG, "🎯 Resultado de AllHotelServicesActivity:");
                Log.d(TAG, "   - Servicios: " + selectedServices);
                Log.d(TAG, "   - Precio adicional: S/. " + additionalServicesPrice);

                navigateToBookingWithServices(selectedServices, additionalServicesPrice);
            }
        }
    }

    private void navigateToBookingWithServices(String selectedServices, double additionalServicesPrice) {
        RoomType selectedRoom = adapter.getSelectedPosition() != -1 ?
                roomTypes.get(adapter.getSelectedPosition()) : null;

        if (selectedRoom == null) return;

        // ✅ USAR FECHAS Y HUÉSPEDES REALES DESDE LOS ARGUMENTOS
        String[] realDates = getRealDatesFromArguments();
        int[] realGuestCounts = getRealGuestCountsFromArguments();

        Bundle args = new Bundle();
        args.putString("hotel_name", hotelName);
        args.putString("hotel_address", currentHotel != null ? currentHotel.getAddress() : "Dirección no disponible");
        args.putParcelable("selected_room", selectedRoom);

        // ✅ USAR FECHAS REALES EN LUGAR DE ALEATORIAS
        args.putString("check_in_date", realDates[0]);
        args.putString("check_out_date", realDates[1]);

        Log.d(TAG, "📅 Usando fechas REALES desde argumentos:");
        Log.d(TAG, "   Check-in: " + realDates[0]);
        Log.d(TAG, "   Check-out: " + realDates[1]);

        // ✅ USAR HUÉSPEDES REALES EN LUGAR DE ALEATORIOS
        args.putInt("num_adults", realGuestCounts[0]);
        args.putInt("num_children", realGuestCounts[1]);

        Log.d(TAG, "👥 Usando huéspedes REALES desde argumentos:");
        Log.d(TAG, "   Adultos: " + realGuestCounts[0]);
        Log.d(TAG, "   Niños: " + realGuestCounts[1]);

        args.putString("room_number", generateRandomRoomNumber());

        // ✅ CALCULAR SI TAXI ES GRATIS BASADO EN EL TOTAL REAL
        double roomPrice = getRoomPriceValue(selectedRoom);
        double totalReservation = roomPrice + additionalServicesPrice;
        boolean isTaxiFree = selectedServices != null && selectedServices.contains("taxi") && totalReservation >= 350.0;
        args.putBoolean("has_free_transport", isTaxiFree);

        // ✅ PASAR SERVICIOS Y PRECIO REAL
        args.putString("selected_services", selectedServices);
        args.putDouble("additional_services_price", additionalServicesPrice);

        Log.d(TAG, "📋 Navegando a BookingSummary con datos REALES:");
        Log.d(TAG, "   - Fechas: " + realDates[0] + " - " + realDates[1]);
        Log.d(TAG, "   - Huéspedes: " + realGuestCounts[0] + " adultos, " + realGuestCounts[1] + " niños");
        Log.d(TAG, "   - Precio habitación: S/. " + roomPrice);
        Log.d(TAG, "   - Precio servicios: S/. " + additionalServicesPrice);
        Log.d(TAG, "   - Total: S/. " + totalReservation);
        Log.d(TAG, "   - Taxi gratis: " + isTaxiFree);

        BookingSummaryFragment bookingSummaryFragment = new BookingSummaryFragment();
        bookingSummaryFragment.setArguments(args);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, bookingSummaryFragment)
                .addToBackStack(null)
                .commit();
    }

    private String[] getRealDatesFromArguments() {
        if (getArguments() != null) {
            // ✅ OPCIÓN 1: Fechas ya parseadas individualmente
            String checkInDate = getArguments().getString("check_in_date");
            String checkOutDate = getArguments().getString("check_out_date");

            if (checkInDate != null && checkOutDate != null) {
                Log.d(TAG, "✅ Usando fechas individuales desde argumentos: " + checkInDate + " - " + checkOutDate);
                return new String[]{checkInDate, checkOutDate};
            }

            // ✅ OPCIÓN 2: Fechas como string combinado
            String searchDates = getArguments().getString("search_dates");
            if (searchDates != null && !searchDates.isEmpty()) {
                String[] parsedDates = parseCombinedDates(searchDates);
                Log.d(TAG, "✅ Usando fechas combinadas desde argumentos: " + searchDates + " -> " + parsedDates[0] + " - " + parsedDates[1]);
                return parsedDates;
            }
        }

        // ✅ FALLBACK: Solo si no hay datos en argumentos, usar valores por defecto (no aleatorios)
        Log.d(TAG, "⚠️ No se encontraron fechas en argumentos, usando por defecto: Hoy - Mañana");
        return new String[]{"Hoy", "Mañana"};
    }

    /**
     * Obtener números de huéspedes reales desde los argumentos, no generar aleatorios
     */
    private int[] getRealGuestCountsFromArguments() {
        if (getArguments() != null) {
            // ✅ OPCIÓN 1: Números ya parseados individualmente
            int numAdults = getArguments().getInt("num_adults", -1);
            int numChildren = getArguments().getInt("num_children", -1);

            if (numAdults != -1) {
                Log.d(TAG, "✅ Usando números de huéspedes desde argumentos: " + numAdults + " adultos, " + numChildren + " niños");
                return new int[]{numAdults, numChildren};
            }

            // ✅ OPCIÓN 2: Huéspedes como string
            String searchGuests = getArguments().getString("search_guests");
            if (searchGuests != null && !searchGuests.isEmpty()) {
                int[] parsedGuests = parseCombinedGuests(searchGuests);
                Log.d(TAG, "✅ Usando huéspedes desde string: " + searchGuests + " -> " + parsedGuests[0] + " adultos, " + parsedGuests[1] + " niños");
                return parsedGuests;
            }
        }

        // ✅ FALLBACK: Solo si no hay datos en argumentos, usar valores por defecto (no aleatorios)
        Log.d(TAG, "⚠️ No se encontraron huéspedes en argumentos, usando por defecto: 2 adultos, 0 niños");
        return new int[]{2, 0};
    }
    private String[] parseCombinedDates(String combinedDates) {
        try {
            // Buscar diferentes separadores
            String separator = combinedDates.contains(" - ") ? " - " :
                    combinedDates.contains("-") ? "-" :
                            combinedDates.contains("–") ? "–" : " - ";

            String[] parts = combinedDates.split(separator);
            if (parts.length >= 2) {
                return new String[]{parts[0].trim(), parts[1].trim()};
            } else if (parts.length == 1) {
                return new String[]{parts[0].trim(), "Día siguiente"};
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parseando fechas combinadas: " + e.getMessage());
        }

        return new String[]{"Hoy", "Mañana"};
    }

    /**
     * Parsear huéspedes desde string como "2 adultos" o "3 adultos • 1 niños"
     */
    private int[] parseCombinedGuests(String combinedGuests) {
        int adults = 2;
        int children = 0;

        try {
            if (combinedGuests != null && !combinedGuests.isEmpty()) {
                // Buscar adultos
                if (combinedGuests.contains("adultos")) {
                    String[] parts = combinedGuests.split("adultos");
                    if (parts.length > 0) {
                        String adultsPart = parts[0].trim();
                        String adultsNumber = adultsPart.replaceAll("[^0-9]", "");
                        if (!adultsNumber.isEmpty()) {
                            adults = Integer.parseInt(adultsNumber);
                        }
                    }
                }

                // Buscar niños
                if (combinedGuests.contains("niños")) {
                    String[] parts = combinedGuests.split("•");
                    for (String part : parts) {
                        if (part.contains("niños")) {
                            String childrenPart = part.trim();
                            String childrenNumber = childrenPart.replaceAll("[^0-9]", "");
                            if (!childrenNumber.isEmpty()) {
                                children = Integer.parseInt(childrenNumber);
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parseando huéspedes: " + e.getMessage());
        }

        return new int[]{adults, children};
    }
    private String[] generateDynamicDates() {
        try {
            java.util.Calendar calendar = java.util.Calendar.getInstance();

            // Fecha de entrada: Hoy + 1-7 días (más realista)
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1 + (int)(Math.random() * 7));
            int checkInDay = calendar.get(java.util.Calendar.DAY_OF_MONTH);
            String checkInMonth = getMonthName(calendar.get(java.util.Calendar.MONTH));

            // Fecha de salida: Entrada + 1-3 días
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1 + (int)(Math.random() * 3));
            int checkOutDay = calendar.get(java.util.Calendar.DAY_OF_MONTH);
            String checkOutMonth = getMonthName(calendar.get(java.util.Calendar.MONTH));

            String checkInDate = checkInDay + " " + checkInMonth;
            String checkOutDate = checkOutDay + " " + checkOutMonth;

            return new String[]{checkInDate, checkOutDate};

        } catch (Exception e) {
            Log.e(TAG, "❌ Error generando fechas: " + e.getMessage());
            return new String[]{"Próximamente", "A definir"};
        }
    }

    // ✅ NUEVO MÉTODO: Generar número de huéspedes dinámico
    private int[] generateDynamicGuestCounts() {
        // Generar números más realistas
        int adults = 1 + (int)(Math.random() * 4); // 1-4 adultos
        int children = (int)(Math.random() * 3);   // 0-2 niños

        return new int[]{adults, children};
    }

    // ✅ NUEVO MÉTODO: Obtener nombre del mes en español
    private String getMonthName(int month) {
        String[] months = {
                "enero", "febrero", "marzo", "abril", "mayo", "junio",
                "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
        };
        return months[month];
    }

    private double getRoomPriceValue(RoomType room) {
        try {
            return Double.parseDouble(room.getPrice().replace("S/", "").trim());
        } catch (NumberFormatException e) {
            return 290.0;
        }
    }

    private String generateRandomRoomNumber() {
        return String.valueOf(System.currentTimeMillis()).substring(7);
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



    // Interface para comunicarse con el adapter
    public interface OnRoomSelectedListener {
        void onRoomSelected(int position);
    }
}