package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.ServiceSelectionAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.model.RoomType;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditRoomTypeDialog extends Dialog {

    public interface OnRoomTypeEditedListener {
        void onRoomTypeEdited(RoomType roomType);
    }

    private AutoCompleteTextView spinnerRoomType;
    private TextInputEditText etRoomArea, etRoomPrice, etAvailableRooms, etRoomCapacity; // ✅ AGREGADO etRoomCapacity
    private RecyclerView rvServices;
    private Button btnSave, btnCancel;
    private TextView tvSelectedCount;

    private OnRoomTypeEditedListener listener;
    private RoomType originalRoomType;
    private List<String> selectedServices;
    private List<String> basicServices;

    private final String[] roomTypesArray = {
            "Habitación Individual",
            "Habitación Doble",
            "Habitación Twin",
            "Habitación Triple",
            "Habitación Cuádruple",
            "Habitación Familiar",
            "Habitación Standard",
            "Habitación Superior",
            "Habitación Deluxe",
            "Habitación Premium",
            "Junior Suite",
            "Suite Ejecutiva",
            "Suite Familiar",
            "Suite Presidencial",
            "Suite Penthouse",
            "Habitación con Balcón",
            "Habitación con Vista al Mar",
            "Habitación con Vista a la Ciudad",
            "Habitación con Vista al Jardín",
            "Habitación Accesible",
            "Habitación Económica",
            "Habitación de Lujo",
            "Villa",
            "Bungalow",
            "Cabaña"
    };

    public EditRoomTypeDialog(Context context, RoomType roomType, OnRoomTypeEditedListener listener) {
        super(context);
        this.listener = listener;
        this.originalRoomType = roomType;
        this.selectedServices = new ArrayList<>();
        this.basicServices = createBasicServices();

        // Extraer servicios adicionales (quitando los básicos)
        for (String service : roomType.getIncludedServices()) {
            if (!basicServices.contains(service)) {
                selectedServices.add(service);
            }
        }

        setupDialog();
    }

    private void setupDialog() {
        setContentView(R.layout.admin_hotel_dialog_add_room_type);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        initViews();
        setupRoomTypeSpinner();
        setupServicesList();
        loadOriginalData();
        setupClickListeners();
    }

    private void initViews() {
        spinnerRoomType = findViewById(R.id.spinnerRoomType);
        etRoomArea = findViewById(R.id.etRoomArea);
        etRoomPrice = findViewById(R.id.etRoomPrice);
        etAvailableRooms = findViewById(R.id.etAvailableRooms);
        etRoomCapacity = findViewById(R.id.etRoomCapacity); // ✅ NUEVO
        rvServices = findViewById(R.id.rvServices);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
    }

    private void loadOriginalData() {
        // Cargar datos del tipo de habitación original
        spinnerRoomType.setText(originalRoomType.getName());
        etRoomArea.setText(String.valueOf((int) originalRoomType.getArea()));
        etRoomPrice.setText(String.valueOf((int) originalRoomType.getPricePerNight()));
        etAvailableRooms.setText(String.valueOf(originalRoomType.getAvailableRooms()));
        etRoomCapacity.setText(String.valueOf(originalRoomType.getCapacity())); // ✅ NUEVO

        // Cambiar texto del botón
        btnSave.setText("💾 Actualizar Habitación");
    }

    private void setupRoomTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, roomTypesArray);
        spinnerRoomType.setAdapter(adapter);
    }

    private void setupServicesList() {
        List<String> additionalServices = Arrays.asList(
                "Minibar",
                "Caja Fuerte",
                "Balcón",
                "Terraza",
                "Vista al Mar",
                "Vista a la Ciudad",
                "Vista al Jardín",
                "Sala de Estar",
                "Escritorio",
                "Bañera de Hidromasaje",
                "Jacuzzi",
                "Chimenea",
                "Cocina Equipada",
                "Kitchenette",
                "Comedor",
                "Sala de Reuniones",
                "Mayordomo Personal",
                "Servicio de Habitaciones 24h",
                "Servicio de Lavandería",
                "Plancha y Tabla de Planchar",
                "Secador de Cabello",
                "Artículos de Aseo Premium",
                "Batas y Pantuflas",
                "Almohadas Adicionales",
                "Servicio de Despertador"
        );

        ServiceSelectionAdapter adapter = new ServiceSelectionAdapter(getContext(), additionalServices, selectedServices);

        // ✅ MÉTODO CORRECTO usando tu adapter existente
        adapter.setOnSelectionChangedListener(this::updateSelectedCount);

        rvServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvServices.setAdapter(adapter);

        updateSelectedCount();
    }

    private List<String> createBasicServices() {
        return Arrays.asList(
                "WiFi Gratuito",
                "Aire Acondicionado",
                "TV Cable",
                "Teléfono",
                "Baño Privado"
        );
    }

    private void updateSelectedCount() {
        if (tvSelectedCount != null) {
            int count = selectedServices.size();
            String text = count + (count == 1 ? " seleccionado" : " seleccionados");
            tvSelectedCount.setText(text);

            if (count > 0) {
                tvSelectedCount.setBackgroundResource(R.drawable.bg_count_badge_active);
                tvSelectedCount.setTextColor(ContextCompat.getColor(getContext(), R.color.orange));
            } else {
                tvSelectedCount.setBackgroundResource(R.drawable.bg_count_badge);
                tvSelectedCount.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
            }
        }
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                updateRoomType();
            }
        });
    }

    private boolean validateInputs() {
        String roomTypeName = spinnerRoomType.getText().toString().trim();
        String area = etRoomArea.getText().toString().trim();
        String price = etRoomPrice.getText().toString().trim();
        String available = etAvailableRooms.getText().toString().trim();
        String capacity = etRoomCapacity.getText().toString().trim(); // ✅ NUEVO

        if (roomTypeName.isEmpty()) {
            Toast.makeText(getContext(), "❌ Selecciona un tipo de habitación", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (area.isEmpty()) {
            Toast.makeText(getContext(), "❌ Ingresa el área de la habitación", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (price.isEmpty()) {
            Toast.makeText(getContext(), "❌ Ingresa el precio por noche", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (available.isEmpty()) {
            Toast.makeText(getContext(), "❌ Ingresa la cantidad disponible", Toast.LENGTH_SHORT).show();
            return false;
        }

        // ✅ NUEVO: Validar capacidad
        if (capacity.isEmpty()) {
            Toast.makeText(getContext(), "❌ Ingresa la capacidad de personas", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Double.parseDouble(area);
            Double.parseDouble(price);
            Integer.parseInt(available);

            // ✅ NUEVO: Validar capacidad
            int capacityInt = Integer.parseInt(capacity);
            if (capacityInt <= 0 || capacityInt > 10) {
                Toast.makeText(getContext(), "❌ La capacidad debe ser entre 1 y 10 personas", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "❌ Verifica que los números sean válidos", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateRoomType() {
        String roomTypeName = spinnerRoomType.getText().toString().trim();
        double area = Double.parseDouble(etRoomArea.getText().toString().trim());
        double price = Double.parseDouble(etRoomPrice.getText().toString().trim());
        int available = Integer.parseInt(etAvailableRooms.getText().toString().trim());
        int capacity = Integer.parseInt(etRoomCapacity.getText().toString().trim()); // ✅ NUEVO

        // Combinar servicios básicos con los seleccionados
        List<String> allServices = new ArrayList<>(basicServices);
        allServices.addAll(selectedServices);

        // ✅ ACTUALIZADO: Pasar capacidad al constructor
        RoomType updatedRoomType = new RoomType(
                roomTypeName,
                originalRoomType.getDescription(),
                area,
                price,
                allServices,
                available,
                capacity // ✅ NUEVO parámetro
        );

        if (listener != null) {
            listener.onRoomTypeEdited(updatedRoomType);
        }

        dismiss();
    }
}