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
import com.example.proyecto_final_hoteleros.adminhotel.adapters.ServiceManagementAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.AddServiceDialog;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.EditServiceDialog;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ServiceManagementFragment extends Fragment {

    private RecyclerView rvServices;
    private FloatingActionButton fabAddService;
    private ImageView ivBack;
    private TextInputEditText etSpecialTaxiAmount;
    private MaterialButton btnSaveSpecialOffer;

    private ServiceManagementAdapter serviceAdapter;
    private List<HotelServiceItem> services;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_service_management, container, false);

        initViews(rootView);
        setupRecyclerView();
        loadServices();
        setupClickListeners();

        return rootView;
    }

    private void initViews(View rootView) {
        ivBack = rootView.findViewById(R.id.ivBack);
        rvServices = rootView.findViewById(R.id.rvServices);
        fabAddService = rootView.findViewById(R.id.fabAddService);
        etSpecialTaxiAmount = rootView.findViewById(R.id.etSpecialTaxiAmount);
        btnSaveSpecialOffer = rootView.findViewById(R.id.btnSaveSpecialOffer);
    }

    private void setupRecyclerView() {
        services = new ArrayList<>();

        // Crear los listeners por separado
        ServiceManagementAdapter.OnServiceActionListener editListener = new ServiceManagementAdapter.OnServiceActionListener() {
            @Override
            public void onEditService(HotelServiceItem service, int position) {
                editService(service, position);
            }

            @Override
            public void onDeleteService(HotelServiceItem service, int position) {
                deleteService(service, position);
            }
        };

        serviceAdapter = new ServiceManagementAdapter(services, editListener, editListener);
        rvServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvServices.setAdapter(serviceAdapter);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        fabAddService.setOnClickListener(v -> showAddServiceDialog());

        btnSaveSpecialOffer.setOnClickListener(v -> saveSpecialTaxiOffer());
    }

    private void loadServices() {
        // Servicios Incluidos (sin precio)
        services.add(new HotelServiceItem("WiFi Gratuito", "Internet de alta velocidad en todo el hotel", 0.0, "ic_wifi", HotelServiceItem.ServiceType.INCLUDED, new ArrayList<>()));
        services.add(new HotelServiceItem("Aire Acondicionado", "Climatización en todas las habitaciones", 0.0, "ic_ac", HotelServiceItem.ServiceType.INCLUDED, new ArrayList<>()));
        services.add(new HotelServiceItem("TV Cable", "Canales premium y entretenimiento", 0.0, "ic_tv", HotelServiceItem.ServiceType.INCLUDED, new ArrayList<>()));
        services.add(new HotelServiceItem("Teléfono", "Línea directa y llamadas locales gratuitas", 0.0, "ic_phone", HotelServiceItem.ServiceType.INCLUDED, new ArrayList<>()));

        // Servicios Pagados
        services.add(new HotelServiceItem("Spa & Wellness", "Relajación y tratamientos de spa", 120.0, "ic_spa", HotelServiceItem.ServiceType.PAID, new ArrayList<>()));
        services.add(new HotelServiceItem("Room Service", "Servicio a la habitación 24/7", 25.0, "ic_room_service", HotelServiceItem.ServiceType.PAID, new ArrayList<>()));
        services.add(new HotelServiceItem("Minibar Premium", "Bebidas y snacks de primera calidad", 80.0, "ic_minibar", HotelServiceItem.ServiceType.PAID, new ArrayList<>()));
        services.add(new HotelServiceItem("Lavandería Express", "Lavado y planchado en 24 horas", 35.0, "ic_laundry", HotelServiceItem.ServiceType.PAID, new ArrayList<>()));
        services.add(new HotelServiceItem("Gimnasio VIP", "Acceso exclusivo con entrenador personal", 50.0, "ic_gym", HotelServiceItem.ServiceType.PAID, new ArrayList<>()));

        // Servicio Especial - LÍNEA CORREGIDA
        services.add(new HotelServiceItem("Taxi Aeropuerto VIP", "Transporte gratuito al alcanzar monto mínimo", 0.0, "ic_taxi", HotelServiceItem.ServiceType.SPECIAL, new ArrayList<>()));

        serviceAdapter.notifyDataSetChanged();

        // Cargar monto especial para taxi
        etSpecialTaxiAmount.setText("500.00");
    }

    private void showAddServiceDialog() {
        AddServiceDialog dialog = new AddServiceDialog(getContext(), service -> {
            services.add(service);
            serviceAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "✅ Servicio agregado", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    private void editService(HotelServiceItem service, int position) {
        EditServiceDialog dialog = new EditServiceDialog(getContext(), service, updatedService -> {
            services.set(position, updatedService);
            serviceAdapter.notifyItemChanged(position);
            Toast.makeText(getContext(), "✅ Servicio actualizado", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    private void deleteService(HotelServiceItem service, int position) {
        if (service.getType() == HotelServiceItem.ServiceType.INCLUDED &&
                services.stream().filter(s -> s.getType() == HotelServiceItem.ServiceType.INCLUDED).count() <= 4) {
            Toast.makeText(getContext(), "⚠️ Debe mantener al menos 4 servicios incluidos", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("🗑️ Eliminar Servicio")
                .setMessage("¿Estás seguro de eliminar '" + service.getName() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    services.remove(position);
                    serviceAdapter.notifyItemRemoved(position);
                    Toast.makeText(getContext(), "🗑️ Servicio eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void saveSpecialTaxiOffer() {
        String amountStr = etSpecialTaxiAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(getContext(), "⚠️ Ingresa el monto mínimo", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(getContext(), "⚠️ El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Guardar en base de datos
            Toast.makeText(getContext(), "✅ Oferta especial de taxi configurada: S/ " + String.format("%.2f", amount), Toast.LENGTH_LONG).show();

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "⚠️ Ingresa un monto válido", Toast.LENGTH_SHORT).show();
        }
    }
}