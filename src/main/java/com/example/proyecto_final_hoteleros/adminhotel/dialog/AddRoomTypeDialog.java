
package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.ServiceSelectionAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.model.RoomType;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddRoomTypeDialog extends Dialog {

    public interface OnRoomTypeAddedListener {
        void onRoomTypeAdded(RoomType roomType);
    }

    private Context context;
    private OnRoomTypeAddedListener listener;
    private TextInputEditText etRoomName, etRoomDescription, etRoomArea, etRoomPrice, etAvailableRooms;
    private RecyclerView rvServices;
    private AppCompatButton btnSave, btnCancel;
    private List<String> selectedServices;

    public AddRoomTypeDialog(Context context, OnRoomTypeAddedListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.selectedServices = new ArrayList<>();
        setupDialog();
    }

    private void setupDialog() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.admin_hotel_dialog_add_room_type);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        initViews();
        setupServicesList();
        setupClickListeners();
    }

    private void initViews() {
        etRoomName = findViewById(R.id.etRoomName);
        etRoomDescription = findViewById(R.id.etRoomDescription);
        etRoomArea = findViewById(R.id.etRoomArea);
        etRoomPrice = findViewById(R.id.etRoomPrice);
        etAvailableRooms = findViewById(R.id.etAvailableRooms);
        rvServices = findViewById(R.id.rvServices);
        btnSave = findViewById(R.id.btnSave);  // Ya no hace cast a MaterialButton
        btnCancel = findViewById(R.id.btnCancel);  // Ya no hace cast a MaterialButton
    }

    private void setupServicesList() {
        List<String> availableServices = Arrays.asList(
                "WiFi Gratuito", "Aire Acondicionado", "TV Cable", "Teléfono",
                "Minibar", "Caja Fuerte", "Balcón", "Sala de Estar",
                "Escritorio", "Bañera de Hidromasaje", "Mayordomo Personal",
                "Cocina Equipada", "Terraza Privada"
        );

        // Agregar servicios básicos por defecto
        selectedServices.addAll(Arrays.asList("WiFi Gratuito", "Aire Acondicionado", "TV Cable", "Teléfono"));

        ServiceSelectionAdapter adapter = new ServiceSelectionAdapter(availableServices, selectedServices);
        rvServices.setLayoutManager(new LinearLayoutManager(context));
        rvServices.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveRoomType());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void saveRoomType() {
        String name = etRoomName.getText().toString().trim();
        String description = etRoomDescription.getText().toString().trim();
        String areaStr = etRoomArea.getText().toString().trim();
        String priceStr = etRoomPrice.getText().toString().trim();
        String roomsStr = etAvailableRooms.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || areaStr.isEmpty() || priceStr.isEmpty() || roomsStr.isEmpty()) {
            Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double area = Double.parseDouble(areaStr);
            double price = Double.parseDouble(priceStr);
            int availableRooms = Integer.parseInt(roomsStr);

            if (area <= 0 || price <= 0 || availableRooms <= 0) {
                Toast.makeText(context, "Los valores deben ser mayores a 0", Toast.LENGTH_SHORT).show();
                return;
            }

            RoomType roomType = new RoomType(name, description, area, price, new ArrayList<>(selectedServices), availableRooms);

            if (listener != null) {
                listener.onRoomTypeAdded(roomType);
            }

            dismiss();

        } catch (NumberFormatException e) {
            Toast.makeText(context, "Ingresa valores numéricos válidos", Toast.LENGTH_SHORT).show();
        }
    }
}

