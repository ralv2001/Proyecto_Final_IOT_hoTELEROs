package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.RoomTypeAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.AddRoomTypeDialog;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.EditRoomTypeDialog;
import com.example.proyecto_final_hoteleros.adminhotel.model.RoomType;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class RoomManagementFragment extends Fragment {

    private RecyclerView rvRoomTypes;
    private FloatingActionButton fabAddRoom;
    private ImageView ivBack;

    private RoomTypeAdapter roomAdapter;
    private List<RoomType> roomTypes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_room_management, container, false);

        initViews(rootView);
        setupRecyclerView();
        loadRoomTypes();
        setupClickListeners();

        return rootView;
    }

    private void initViews(View rootView) {
        ivBack = rootView.findViewById(R.id.ivBack);
        rvRoomTypes = rootView.findViewById(R.id.rvRoomTypes);
        fabAddRoom = rootView.findViewById(R.id.fabAddRoom);
    }

    private void setupRecyclerView() {
        roomTypes = new ArrayList<>();

        // Crear los listeners por separado en lugar de usar method references
        RoomTypeAdapter.OnRoomActionListener editListener = new RoomTypeAdapter.OnRoomActionListener() {
            @Override
            public void onEditRoom(RoomType roomType, int position) {
                editRoomType(roomType, position);
            }

            @Override
            public void onDeleteRoom(RoomType roomType, int position) {
                deleteRoomType(roomType, position);
            }
        };

        roomAdapter = new RoomTypeAdapter(roomTypes, editListener, editListener);
        rvRoomTypes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRoomTypes.setAdapter(roomAdapter);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        fabAddRoom.setOnClickListener(v -> showAddRoomDialog());
    }

    private void loadRoomTypes() {
        // Cargar tipos de habitación existentes
        roomTypes.add(new RoomType("Habitación Estándar", "Habitación cómoda con servicios básicos", 25.0, 150.0, createBasicServices(), 4));
        roomTypes.add(new RoomType("Habitación Deluxe", "Habitación amplia con vista panorámica", 35.0, 250.0, createDeluxeServices(), 2));
        roomTypes.add(new RoomType("Suite Ejecutiva", "Suite con sala de estar independiente", 45.0, 400.0, createSuiteServices(), 1));
        roomTypes.add(new RoomType("Suite Presidencial", "La mejor suite del hotel con todas las comodidades", 80.0, 800.0, createPresidentialServices(), 1));

        roomAdapter.notifyDataSetChanged();
    }

    private List<String> createBasicServices() {
        List<String> services = new ArrayList<>();
        services.add("WiFi Gratuito");
        services.add("Aire Acondicionado");
        services.add("TV Cable");
        services.add("Teléfono");
        return services;
    }

    private List<String> createDeluxeServices() {
        List<String> services = createBasicServices();
        services.add("Minibar");
        services.add("Caja Fuerte");
        services.add("Balcón");
        return services;
    }

    private List<String> createSuiteServices() {
        List<String> services = createDeluxeServices();
        services.add("Sala de Estar");
        services.add("Escritorio");
        services.add("Bañera de Hidromasaje");
        return services;
    }

    private List<String> createPresidentialServices() {
        List<String> services = createSuiteServices();
        services.add("Mayordomo Personal");
        services.add("Cocina Equipada");
        services.add("Terraza Privada");
        return services;
    }

    private void showAddRoomDialog() {
        AddRoomTypeDialog dialog = new AddRoomTypeDialog(getContext(), roomType -> {
            roomTypes.add(roomType);
            roomAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "✅ Tipo de habitación agregado", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    private void editRoomType(RoomType roomType, int position) {
        EditRoomTypeDialog dialog = new EditRoomTypeDialog(getContext(), roomType, updatedRoom -> {
            roomTypes.set(position, updatedRoom);
            roomAdapter.notifyItemChanged(position);
            Toast.makeText(getContext(), "✅ Habitación actualizada", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    private void deleteRoomType(RoomType roomType, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("🗑️ Eliminar Tipo de Habitación")
                .setMessage("¿Estás seguro de eliminar '" + roomType.getName() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    roomTypes.remove(position);
                    roomAdapter.notifyItemRemoved(position);
                    Toast.makeText(getContext(), "🗑️ Tipo de habitación eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}

