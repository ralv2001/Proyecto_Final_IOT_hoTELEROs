package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.ServiceManagementAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.AddServiceDialog;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.EditServiceDialog;
import com.example.proyecto_final_hoteleros.adminhotel.model.BasicService;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import android.widget.ImageView;

public class ServiceManagementFragment extends Fragment {

    // Views
    private ImageView ivBack;
    private FloatingActionButton fabAddService;
    private MaterialButton btnSaveSpecialOffer;
    private TextInputEditText etSpecialTaxiAmount;

    // RecyclerViews para cada tipo de servicio
    private RecyclerView rvBasicServices, rvIncludedServices, rvPaidServices, rvConditionalServices;
    private View layoutBasicServicesEmpty;

    // Adapters
    private ServiceManagementAdapter basicServicesAdapter;
    private ServiceManagementAdapter includedServicesAdapter;
    private ServiceManagementAdapter paidServicesAdapter;
    private ServiceManagementAdapter conditionalServicesAdapter;

    // Listas de servicios
    private List<HotelServiceItem> basicServices;
    private List<HotelServiceItem> includedServices;
    private List<HotelServiceItem> paidServices;
    private List<HotelServiceItem> conditionalServices;

    // Dialog y Launcher
    private AddServiceDialog currentAddServiceDialog;
    private ActivityResultLauncher<Intent> photoPickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializePhotoLauncher();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_hotel_fragment_service_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initializeLists();
        setupRecyclerViews();
        setupClickListeners();
        loadServices();
        updateEmptyStates();
    }

    private void initializePhotoLauncher() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        if (currentAddServiceDialog != null) {
                            currentAddServiceDialog.handlePhotoResult(result.getData());
                        }
                    }
                }
        );
    }

    private void initViews(View view) {
        ivBack = view.findViewById(R.id.ivBack);
        fabAddService = view.findViewById(R.id.fabAddService);
        btnSaveSpecialOffer = view.findViewById(R.id.btnSaveSpecialOffer);
        etSpecialTaxiAmount = view.findViewById(R.id.etSpecialTaxiAmount);

        // RecyclerViews
        rvBasicServices = view.findViewById(R.id.rvBasicServices);
        rvIncludedServices = view.findViewById(R.id.rvIncludedServices);
        rvPaidServices = view.findViewById(R.id.rvPaidServices);
        rvConditionalServices = view.findViewById(R.id.rvConditionalServices);

        // Empty state
        layoutBasicServicesEmpty = view.findViewById(R.id.layoutBasicServicesEmpty);
    }

    private void initializeLists() {
        basicServices = new ArrayList<>();
        includedServices = new ArrayList<>();
        paidServices = new ArrayList<>();
        conditionalServices = new ArrayList<>();
    }

    private void setupRecyclerViews() {
        // Listener para editar servicios
        ServiceManagementAdapter.OnServiceActionListener editListener = new ServiceManagementAdapter.OnServiceActionListener() {
            @Override
            public void onEditService(HotelServiceItem service, int position) {
                editService(service, position);
            }

            @Override
            public void onDeleteService(HotelServiceItem service, int position) {
                deleteService(service, position);
            }

            @Override
            public void onToggleService(HotelServiceItem service, int position, boolean isActive) {
                service.setActive(isActive);
                updateServiceInCorrespondingList(service, position);
                Toast.makeText(getContext(),
                        service.getName() + (isActive ? " activado" : " desactivado"),
                        Toast.LENGTH_SHORT).show();
            }
        };

        // Setup Servicios Básicos
        basicServicesAdapter = new ServiceManagementAdapter(basicServices, editListener);
        rvBasicServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBasicServices.setAdapter(basicServicesAdapter);
        rvBasicServices.setNestedScrollingEnabled(false);

        // Setup Servicios Incluidos
        includedServicesAdapter = new ServiceManagementAdapter(includedServices, editListener);
        rvIncludedServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvIncludedServices.setAdapter(includedServicesAdapter);
        rvIncludedServices.setNestedScrollingEnabled(false);

        // Setup Servicios Pagados
        paidServicesAdapter = new ServiceManagementAdapter(paidServices, editListener);
        rvPaidServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPaidServices.setAdapter(paidServicesAdapter);
        rvPaidServices.setNestedScrollingEnabled(false);

        // Setup Servicios Condicionales
        conditionalServicesAdapter = new ServiceManagementAdapter(conditionalServices, editListener);
        rvConditionalServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvConditionalServices.setAdapter(conditionalServicesAdapter);
        rvConditionalServices.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        fabAddService.setOnClickListener(v -> showAddServiceDialog());

        btnSaveSpecialOffer.setOnClickListener(v -> saveSpecialTaxiOffer());
    }

    private void loadServices() {
        loadBasicServicesFromProfile();
        loadIncludedServices();
        loadPaidServices();
        loadConditionalServices();

        // Configurar monto del taxi especial
        etSpecialTaxiAmount.setText("500.00");
    }

    private void loadBasicServicesFromProfile() {
        // Simular carga de servicios básicos desde el perfil del hotel
        // En la implementación real, estos vendrían de SharedPreferences o Firebase
        basicServices.clear();

        // Servicios básicos por defecto (que normalmente vendrían del perfil)
        basicServices.add(new HotelServiceItem("WiFi Gratuito", "Internet de alta velocidad en todas las habitaciones", 0.0, "wifi", HotelServiceItem.ServiceType.BASIC, new ArrayList<>()));
        basicServices.add(new HotelServiceItem("Aire Acondicionado", "Climatización individual en cada habitación", 0.0, "ac", HotelServiceItem.ServiceType.BASIC, new ArrayList<>()));
        basicServices.add(new HotelServiceItem("TV por Cable", "Televisión por cable con canales premium", 0.0, "tv", HotelServiceItem.ServiceType.BASIC, new ArrayList<>()));
        basicServices.add(new HotelServiceItem("Recepción 24h", "Atención al cliente las 24 horas del día", 0.0, "reception", HotelServiceItem.ServiceType.BASIC, new ArrayList<>()));

        basicServicesAdapter.notifyDataSetChanged();
    }

    private void loadIncludedServices() {
        includedServices.clear();

        // Servicios incluidos (sin costo pero no básicos)
        includedServices.add(new HotelServiceItem("Desayuno Continental", "Desayuno buffet incluido en la estadía", 0.0, "breakfast", HotelServiceItem.ServiceType.INCLUDED, new ArrayList<>()));
        includedServices.add(new HotelServiceItem("Estacionamiento", "Parqueadero gratuito para huéspedes", 0.0, "parking", HotelServiceItem.ServiceType.INCLUDED, new ArrayList<>()));
        includedServices.add(new HotelServiceItem("Acceso a Piscina", "Uso libre de la piscina del hotel", 0.0, "pool", HotelServiceItem.ServiceType.INCLUDED, new ArrayList<>()));

        includedServicesAdapter.notifyDataSetChanged();
    }

    private void loadPaidServices() {
        paidServices.clear();

        // Servicios pagados
        paidServices.add(new HotelServiceItem("Spa & Wellness", "Relajación y tratamientos de spa", 120.0, "spa", HotelServiceItem.ServiceType.PAID, new ArrayList<>()));
        paidServices.add(new HotelServiceItem("Room Service 24h", "Servicio a la habitación las 24 horas", 25.0, "room_service", HotelServiceItem.ServiceType.PAID, new ArrayList<>()));
        paidServices.add(new HotelServiceItem("Minibar Premium", "Bebidas y snacks de primera calidad", 80.0, "minibar", HotelServiceItem.ServiceType.PAID, new ArrayList<>()));
        paidServices.add(new HotelServiceItem("Lavandería Express", "Lavado y planchado en 24 horas", 35.0, "laundry", HotelServiceItem.ServiceType.PAID, new ArrayList<>()));
        paidServices.add(new HotelServiceItem("Gimnasio VIP", "Acceso exclusivo con entrenador personal", 50.0, "gym", HotelServiceItem.ServiceType.PAID, new ArrayList<>()));

        paidServicesAdapter.notifyDataSetChanged();
    }

    private void loadConditionalServices() {
        conditionalServices.clear();

        // Servicios condicionales
        conditionalServices.add(new HotelServiceItem("Taxi Aeropuerto VIP", "Transporte gratuito al aeropuerto", 0.0, "taxi", HotelServiceItem.ServiceType.CONDITIONAL, new ArrayList<>(), 500.0));

        conditionalServicesAdapter.notifyDataSetChanged();
    }

    private void showAddServiceDialog() {
        currentAddServiceDialog = new AddServiceDialog(getContext(), photoPickerLauncher, new AddServiceDialog.OnServiceAddedListener() {
            @Override
            public void onServiceAdded(HotelServiceItem service) {
                addServiceToCorrespondingList(service);
                updateEmptyStates();
                Toast.makeText(getContext(), "✅ Servicio agregado exitosamente", Toast.LENGTH_SHORT).show();
                currentAddServiceDialog = null;
            }
        });
        currentAddServiceDialog.show();
    }

    private void addServiceToCorrespondingList(HotelServiceItem service) {
        switch (service.getType()) {
            case BASIC:
                basicServices.add(service);
                basicServicesAdapter.notifyItemInserted(basicServices.size() - 1);
                break;
            case INCLUDED:
                includedServices.add(service);
                includedServicesAdapter.notifyItemInserted(includedServices.size() - 1);
                break;
            case PAID:
                paidServices.add(service);
                paidServicesAdapter.notifyItemInserted(paidServices.size() - 1);
                break;
            case CONDITIONAL:
                conditionalServices.add(service);
                conditionalServicesAdapter.notifyItemInserted(conditionalServices.size() - 1);
                break;
        }
    }

    private void editService(HotelServiceItem service, int position) {
        EditServiceDialog dialog = new EditServiceDialog(getContext(), service, updatedService -> {
            updateServiceInCorrespondingList(updatedService, position);
            Toast.makeText(getContext(), "✅ Servicio actualizado", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    private void deleteService(HotelServiceItem service, int position) {
        // Verificar si es un servicio básico y hay mínimo requerido
        if (service.getType() == HotelServiceItem.ServiceType.BASIC && basicServices.size() <= 3) {
            Toast.makeText(getContext(), "⚠️ Debe mantener al menos 3 servicios básicos", Toast.LENGTH_LONG).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("🗑️ Eliminar Servicio")
                .setMessage("¿Estás seguro de eliminar '" + service.getName() + "'?\n\nEsta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    removeServiceFromCorrespondingList(service, position);
                    updateEmptyStates();
                    Toast.makeText(getContext(), "🗑️ Servicio eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void removeServiceFromCorrespondingList(HotelServiceItem service, int position) {
        switch (service.getType()) {
            case BASIC:
                if (position < basicServices.size()) {
                    basicServices.remove(position);
                    basicServicesAdapter.notifyItemRemoved(position);
                }
                break;
            case INCLUDED:
                if (position < includedServices.size()) {
                    includedServices.remove(position);
                    includedServicesAdapter.notifyItemRemoved(position);
                }
                break;
            case PAID:
                if (position < paidServices.size()) {
                    paidServices.remove(position);
                    paidServicesAdapter.notifyItemRemoved(position);
                }
                break;
            case CONDITIONAL:
                if (position < conditionalServices.size()) {
                    conditionalServices.remove(position);
                    conditionalServicesAdapter.notifyItemRemoved(position);
                }
                break;
        }
    }

    private void updateServiceInCorrespondingList(HotelServiceItem service, int position) {
        switch (service.getType()) {
            case BASIC:
                if (position < basicServices.size()) {
                    basicServices.set(position, service);
                    basicServicesAdapter.notifyItemChanged(position);
                }
                break;
            case INCLUDED:
                if (position < includedServices.size()) {
                    includedServices.set(position, service);
                    includedServicesAdapter.notifyItemChanged(position);
                }
                break;
            case PAID:
                if (position < paidServices.size()) {
                    paidServices.set(position, service);
                    paidServicesAdapter.notifyItemChanged(position);
                }
                break;
            case CONDITIONAL:
                if (position < conditionalServices.size()) {
                    conditionalServices.set(position, service);
                    conditionalServicesAdapter.notifyItemChanged(position);
                }
                break;
        }
    }

    private void updateEmptyStates() {
        // Mostrar/ocultar mensaje de servicios básicos vacíos
        if (basicServices.isEmpty()) {
            layoutBasicServicesEmpty.setVisibility(View.VISIBLE);
            rvBasicServices.setVisibility(View.GONE);
        } else {
            layoutBasicServicesEmpty.setVisibility(View.GONE);
            rvBasicServices.setVisibility(View.VISIBLE);
        }
    }

    private void saveSpecialTaxiOffer() {
        String amountStr = etSpecialTaxiAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(getContext(), "⚠️ Ingresa el monto mínimo para el taxi", Toast.LENGTH_SHORT).show();
            etSpecialTaxiAmount.requestFocus();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(getContext(), "⚠️ El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return;
            }

            // Actualizar el servicio de taxi condicional
            for (HotelServiceItem service : conditionalServices) {
                if (service.getName().contains("Taxi") && service.getType() == HotelServiceItem.ServiceType.CONDITIONAL) {
                    service.setConditionalAmount(amount);
                    break;
                }
            }

            conditionalServicesAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "✅ Oferta de taxi actualizada: S/ " + String.format("%.2f", amount), Toast.LENGTH_LONG).show();

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "⚠️ Ingresa un monto válido", Toast.LENGTH_SHORT).show();
            etSpecialTaxiAmount.requestFocus();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentAddServiceDialog != null && currentAddServiceDialog.isShowing()) {
            currentAddServiceDialog.dismiss();
            currentAddServiceDialog = null;
        }
    }

    // Método público para recibir servicios básicos desde el perfil del hotel
    public void updateBasicServicesFromProfile(List<BasicService> profileBasicServices) {
        basicServices.clear();

        for (BasicService basicService : profileBasicServices) {
            HotelServiceItem serviceItem = new HotelServiceItem(
                    basicService.getName(),
                    basicService.getDescription(),
                    0.0,
                    basicService.getIconKey(),
                    HotelServiceItem.ServiceType.BASIC,
                    basicService.getPhotos()
            );
            basicServices.add(serviceItem);
        }

        if (basicServicesAdapter != null) {
            basicServicesAdapter.notifyDataSetChanged();
        }
        updateEmptyStates();
    }
}