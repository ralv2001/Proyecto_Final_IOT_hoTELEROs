package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceModel;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseServiceManager;
import com.example.proyecto_final_hoteleros.adminhotel.utils.ServiceSyncManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ServiceManagementFragment extends Fragment implements FirebaseServiceManager.OnServicesChangedListener {

    private static final String TAG = "ServiceManagementFragment";

    // Views
    private ImageView ivBack;
    private FloatingActionButton fabAddService;
    private MaterialButton btnSaveSpecialOffer;
    private TextInputEditText etSpecialTaxiAmount;

    // RecyclerViews para cada tipo de servicio
    private RecyclerView rvBasicServices, rvIncludedServices, rvPaidServices, rvConditionalServices;
    private View layoutBasicServicesEmpty, layoutIncludedServicesEmpty, layoutPaidServicesEmpty, layoutConditionalServicesEmpty;

    // Adapters
    private ServiceManagementAdapter basicServicesAdapter, includedServicesAdapter, paidServicesAdapter, conditionalServicesAdapter;

    // Listas de servicios
    private List<HotelServiceItem> basicServices, includedServices, paidServices, conditionalServices;

    // Firebase y managers
    private FirebaseServiceManager firebaseServiceManager;
    private ServiceSyncManager serviceSyncManager;
    private AddServiceDialog currentAddServiceDialog;
    private ActivityResultLauncher<Intent> photoPickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar managers
        firebaseServiceManager = FirebaseServiceManager.getInstance(requireContext());
        serviceSyncManager = ServiceSyncManager.getInstance(requireContext());

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

        // ✅ SOLUCIÓN: Registrar listener de Firebase ANTES de cargar
        Log.d(TAG, "📡 Registrando listener de Firebase");
        firebaseServiceManager.addListener(this);

        // ✅ MEJORADO: Cargar servicios desde Firebase inmediatamente
        loadServicesFromFirebase();
        updateEmptyStates();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firebaseServiceManager != null) {
            firebaseServiceManager.removeListener(this);
        }
        if (currentAddServiceDialog != null && currentAddServiceDialog.isShowing()) {
            currentAddServiceDialog.dismiss();
        }
    }

    // ========== IMPLEMENTACIÓN DE FIREBASE LISTENER ==========

    @Override
    public void onBasicServicesUpdated(List<HotelServiceModel> basicServiceModels) {
        Log.d(TAG, "🔄 Servicios básicos actualizados desde Firebase: " + basicServiceModels.size());

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                basicServices.clear();
                for (HotelServiceModel model : basicServiceModels) {
                    basicServices.add(convertFirebaseToHotelServiceItem(model));
                }

                Log.d(TAG, "📊 Lista básicos actualizada: " + basicServices.size() + " servicios");

                if (basicServicesAdapter != null) {
                    basicServicesAdapter.notifyDataSetChanged();
                }
                updateEmptyStates();
            });
        }
    }

    @Override
    public void onAllServicesUpdated(List<HotelServiceModel> allServices) {
        Log.d(TAG, "🔄 Todos los servicios actualizados desde Firebase: " + allServices.size());

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // ✅ Limpiar todas las listas
                basicServices.clear();
                includedServices.clear();
                paidServices.clear();
                conditionalServices.clear();

                // ✅ Clasificar servicios por tipo
                for (HotelServiceModel model : allServices) {
                    HotelServiceItem item = convertFirebaseToHotelServiceItem(model);

                    switch (model.getServiceType()) {
                        case "basic":
                            basicServices.add(item);
                            Log.d(TAG, "📋 Básico: " + item.getName());
                            break;
                        case "included":
                            includedServices.add(item);
                            Log.d(TAG, "📋 Incluido: " + item.getName());
                            break;
                        case "paid":
                            paidServices.add(item);
                            Log.d(TAG, "📋 Pagado: " + item.getName());
                            break;
                        case "conditional":
                            conditionalServices.add(item);
                            Log.d(TAG, "📋 Condicional: " + item.getName());
                            break;
                    }
                }

                Log.d(TAG, "📊 Servicios clasificados - Básicos: " + basicServices.size() +
                        ", Incluidos: " + includedServices.size() +
                        ", Pagados: " + paidServices.size() +
                        ", Condicionales: " + conditionalServices.size());

                // ✅ Actualizar todos los adapters
                if (basicServicesAdapter != null) basicServicesAdapter.notifyDataSetChanged();
                if (includedServicesAdapter != null) includedServicesAdapter.notifyDataSetChanged();
                if (paidServicesAdapter != null) paidServicesAdapter.notifyDataSetChanged();
                if (conditionalServicesAdapter != null) conditionalServicesAdapter.notifyDataSetChanged();

                updateEmptyStates();
            });
        }
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "❌ Error desde Firebase: " + error);
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Error cargando servicios: " + error, Toast.LENGTH_LONG).show();
            });
        }
    }
    @Override
    public void onResume() {
        super.onResume();

        // ✅ SOLUCIÓN: Verificar y recargar datos cuando se regresa a la pantalla
        Log.d(TAG, "🔄 onResume - Verificando servicios...");

        if (firebaseServiceManager != null) {
            // Forzar recarga si todas las listas están vacías
            if (basicServices.isEmpty() && includedServices.isEmpty() &&
                    paidServices.isEmpty() && conditionalServices.isEmpty()) {
                Log.d(TAG, "📋 Listas vacías, forzando recarga de todos los servicios");
                loadServicesFromFirebase();
            }
        }
    }
    // ========== INICIALIZACIÓN ==========

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

        // Empty states (opcionales si existen en el layout)
        layoutBasicServicesEmpty = view.findViewById(R.id.layoutBasicServicesEmpty);
        layoutIncludedServicesEmpty = view.findViewById(R.id.layoutIncludedServicesEmpty);
        layoutPaidServicesEmpty = view.findViewById(R.id.layoutPaidServicesEmpty);
        layoutConditionalServicesEmpty = view.findViewById(R.id.layoutConditionalServicesEmpty);
    }

    private void initializeLists() {
        basicServices = new ArrayList<>();
        includedServices = new ArrayList<>();
        paidServices = new ArrayList<>();
        conditionalServices = new ArrayList<>();
    }

    private void setupRecyclerViews() {
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
                toggleServiceStatus(service, position, isActive);
            }
        };

        // ✅ Setup Servicios Básicos
        basicServicesAdapter = new ServiceManagementAdapter(basicServices, editListener);
        if (rvBasicServices != null) {
            rvBasicServices.setLayoutManager(new LinearLayoutManager(getContext()));
            rvBasicServices.setAdapter(basicServicesAdapter);
            rvBasicServices.setNestedScrollingEnabled(false);
        }

        // ✅ Setup Servicios Incluidos
        includedServicesAdapter = new ServiceManagementAdapter(includedServices, editListener);
        if (rvIncludedServices != null) {
            rvIncludedServices.setLayoutManager(new LinearLayoutManager(getContext()));
            rvIncludedServices.setAdapter(includedServicesAdapter);
            rvIncludedServices.setNestedScrollingEnabled(false);
        }

        // ✅ Setup Servicios Pagados
        paidServicesAdapter = new ServiceManagementAdapter(paidServices, editListener);
        if (rvPaidServices != null) {
            rvPaidServices.setLayoutManager(new LinearLayoutManager(getContext()));
            rvPaidServices.setAdapter(paidServicesAdapter);
            rvPaidServices.setNestedScrollingEnabled(false);
        }

        // ✅ Setup Servicios Condicionales
        conditionalServicesAdapter = new ServiceManagementAdapter(conditionalServices, editListener);
        if (rvConditionalServices != null) {
            rvConditionalServices.setLayoutManager(new LinearLayoutManager(getContext()));
            rvConditionalServices.setAdapter(conditionalServicesAdapter);
            rvConditionalServices.setNestedScrollingEnabled(false);
        }

        Log.d(TAG, "✅ Todos los RecyclerViews configurados");
    }

    private void setupClickListeners() {
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        if (fabAddService != null) {
            fabAddService.setOnClickListener(v -> showAddServiceDialog());
        }

        if (btnSaveSpecialOffer != null) {
            btnSaveSpecialOffer.setOnClickListener(v -> saveSpecialTaxiOffer());
        }
    }

    // ========== CARGA DE SERVICIOS ==========

    private void loadServicesFromFirebase() {
        Log.d(TAG, "📊 Cargando servicios desde Firebase...");

        // ✅ SOLUCIÓN: Forzar carga explícita de todos los servicios
        firebaseServiceManager.getAllServices(new FirebaseServiceManager.ServicesListCallback() {
            @Override
            public void onSuccess(List<HotelServiceModel> allServices) {
                Log.d(TAG, "✅ Todos los servicios obtenidos: " + allServices.size());

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Limpiar todas las listas
                        basicServices.clear();
                        includedServices.clear();
                        paidServices.clear();
                        conditionalServices.clear();

                        // Clasificar servicios por tipo
                        for (HotelServiceModel model : allServices) {
                            HotelServiceItem item = convertFirebaseToHotelServiceItem(model);

                            switch (model.getServiceType()) {
                                case "basic":
                                    basicServices.add(item);
                                    Log.d(TAG, "📋 Básico cargado: " + item.getName());
                                    break;
                                case "included":
                                    includedServices.add(item);
                                    Log.d(TAG, "📋 Incluido cargado: " + item.getName());
                                    break;
                                case "paid":
                                    paidServices.add(item);
                                    Log.d(TAG, "📋 Pagado cargado: " + item.getName());
                                    break;
                                case "conditional":
                                    conditionalServices.add(item);
                                    Log.d(TAG, "📋 Condicional cargado: " + item.getName());
                                    break;
                            }
                        }

                        Log.d(TAG, "📊 Servicios clasificados - Básicos: " + basicServices.size() +
                                ", Incluidos: " + includedServices.size() +
                                ", Pagados: " + paidServices.size() +
                                ", Condicionales: " + conditionalServices.size());

                        // Actualizar todos los adapters
                        if (basicServicesAdapter != null) basicServicesAdapter.notifyDataSetChanged();
                        if (includedServicesAdapter != null) includedServicesAdapter.notifyDataSetChanged();
                        if (paidServicesAdapter != null) paidServicesAdapter.notifyDataSetChanged();
                        if (conditionalServicesAdapter != null) conditionalServicesAdapter.notifyDataSetChanged();

                        updateEmptyStates();
                        Log.d(TAG, "🔄 Todos los adapters actualizados");
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando servicios: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error cargando servicios: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });

        // Firebase automáticamente manejará los listeners para cambios futuros
        firebaseServiceManager.initializeDefaultServices();

        // Configurar taxi condicional por defecto
        if (etSpecialTaxiAmount != null) {
            etSpecialTaxiAmount.setText("500.00");
        }
    }

    private void updateEmptyStates() {
        // ✅ Actualizar estados vacíos si los layouts existen
        if (layoutBasicServicesEmpty != null) {
            layoutBasicServicesEmpty.setVisibility(basicServices.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (layoutIncludedServicesEmpty != null) {
            layoutIncludedServicesEmpty.setVisibility(includedServices.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (layoutPaidServicesEmpty != null) {
            layoutPaidServicesEmpty.setVisibility(paidServices.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (layoutConditionalServicesEmpty != null) {
            layoutConditionalServicesEmpty.setVisibility(conditionalServices.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    // ========== OPERACIONES CON SERVICIOS ==========

    private void showAddServiceDialog() {
        if (currentAddServiceDialog != null && currentAddServiceDialog.isShowing()) {
            return;
        }

        Log.d(TAG, "🔧 Mostrando diálogo para agregar servicio");

        currentAddServiceDialog = new AddServiceDialog(getContext(), photoPickerLauncher, service -> {
            // ✅ El servicio se agregará automáticamente via Firebase listener
            Log.d(TAG, "✅ Servicio agregado callback recibido: " + service.getName());
            Toast.makeText(getContext(), "✅ Servicio '" + service.getName() + "' agregado exitosamente", Toast.LENGTH_SHORT).show();
        });
        currentAddServiceDialog.show();
    }

    private void editService(HotelServiceItem service, int position) {
        if (service.getType() == HotelServiceItem.ServiceType.BASIC) {
            Toast.makeText(getContext(), "⚠️ Los servicios básicos se editan desde el perfil del hotel", Toast.LENGTH_LONG).show();
            return;
        }

        // Encontrar el modelo de Firebase correspondiente
        String serviceId = findFirebaseServiceId(service);
        if (serviceId == null) {
            Toast.makeText(getContext(), "Error: No se pudo encontrar el servicio para editar", Toast.LENGTH_SHORT).show();
            return;
        }

        EditServiceDialog dialog = new EditServiceDialog(getContext(), service, updatedService -> {
            updateServiceInFirebase(serviceId, updatedService);
        });
        dialog.show();
    }

    private void deleteService(HotelServiceItem service, int position) {
        if (service.getType() == HotelServiceItem.ServiceType.BASIC) {
            Toast.makeText(getContext(), "⚠️ Los servicios básicos se eliminan desde el perfil del hotel", Toast.LENGTH_LONG).show();
            return;
        }

        if (service.getType() == HotelServiceItem.ServiceType.CONDITIONAL &&
                service.getName().toLowerCase().contains("taxi")) {
            Toast.makeText(getContext(), "⚠️ El servicio de taxi condicional no puede eliminarse", Toast.LENGTH_LONG).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("🗑️ Eliminar Servicio")
                .setMessage("¿Estás seguro de eliminar '" + service.getName() + "'?\n\nEsta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    deleteServiceFromFirebase(service);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void toggleServiceStatus(HotelServiceItem service, int position, boolean isActive) {
        // Encontrar el servicio en Firebase y actualizar su estado
        String serviceId = findFirebaseServiceId(service);
        if (serviceId != null) {
            HotelServiceModel model = convertHotelServiceItemToFirebase(service);
            model.setId(serviceId);
            model.setActive(isActive);

            firebaseServiceManager.updateService(model, null, new FirebaseServiceManager.ServiceCallback() {
                @Override
                public void onSuccess(HotelServiceModel updatedService) {
                    Log.d(TAG, "✅ Estado del servicio actualizado: " + updatedService.getName());
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error actualizando estado del servicio: " + error);
                    // Revertir el cambio en la UI
                    service.setActive(!isActive);
                    updateAdapterForService(service);
                }
            });
        }

        Toast.makeText(getContext(),
                "🔧 " + service.getName() + (isActive ? " activado" : " desactivado"),
                Toast.LENGTH_SHORT).show();
    }

    private void saveSpecialTaxiOffer() {
        if (etSpecialTaxiAmount == null) {
            Toast.makeText(getContext(), "Campo de configuración no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountText = etSpecialTaxiAmount.getText().toString().trim();

        if (amountText.isEmpty()) {
            etSpecialTaxiAmount.setError("Ingresa un monto");
            etSpecialTaxiAmount.requestFocus();
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);

            if (amount <= 0) {
                etSpecialTaxiAmount.setError("El monto debe ser mayor a 0");
                etSpecialTaxiAmount.requestFocus();
                return;
            }

            // Buscar el servicio de taxi condicional y actualizarlo
            HotelServiceItem taxiService = null;
            for (HotelServiceItem service : conditionalServices) {
                if (service.getName().toLowerCase().contains("taxi")) {
                    taxiService = service;
                    break;
                }
            }

            if (taxiService != null) {
                // Actualizar en Firebase
                String serviceId = findFirebaseServiceId(taxiService);
                if (serviceId != null) {
                    HotelServiceModel model = convertHotelServiceItemToFirebase(taxiService);
                    model.setId(serviceId);
                    model.setConditionalAmount(amount);
                    model.setDescription("Transporte gratuito cuando el gasto total supere S/ " + String.format("%.2f", amount));

                    firebaseServiceManager.updateService(model, null, new FirebaseServiceManager.ServiceCallback() {
                        @Override
                        public void onSuccess(HotelServiceModel updatedService) {
                            Toast.makeText(getContext(), "✅ Oferta de taxi actualizada: S/ " + String.format("%.2f", amount), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), "Error actualizando oferta: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                // Crear nuevo servicio de taxi condicional
                HotelServiceModel newTaxiService = new HotelServiceModel(
                        "🚖 Taxi Gratuito al Aeropuerto",
                        "Transporte gratuito cuando el gasto total supere S/ " + String.format("%.2f", amount),
                        "taxi",
                        "conditional"
                );
                newTaxiService.setConditionalAmount(amount);

                firebaseServiceManager.createService(newTaxiService, null, new FirebaseServiceManager.ServiceCallback() {
                    @Override
                    public void onSuccess(HotelServiceModel createdService) {
                        Toast.makeText(getContext(), "✅ Servicio de taxi creado: S/ " + String.format("%.2f", amount), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), "Error creando servicio de taxi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (NumberFormatException e) {
            etSpecialTaxiAmount.setError("Ingresa un monto válido");
            etSpecialTaxiAmount.requestFocus();
        }
    }

    // ========== MÉTODOS DE FIREBASE ==========

    private void updateServiceInFirebase(String serviceId, HotelServiceItem updatedService) {
        HotelServiceModel model = convertHotelServiceItemToFirebase(updatedService);
        model.setId(serviceId);

        firebaseServiceManager.updateService(model, null, new FirebaseServiceManager.ServiceCallback() {
            @Override
            public void onSuccess(HotelServiceModel service) {
                Toast.makeText(getContext(), "✅ Servicio actualizado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error actualizando servicio: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteServiceFromFirebase(HotelServiceItem service) {
        String serviceId = findFirebaseServiceId(service);
        if (serviceId != null) {
            firebaseServiceManager.deleteService(serviceId, new FirebaseServiceManager.ServiceCallback() {
                @Override
                public void onSuccess(HotelServiceModel deletedService) {
                    Toast.makeText(getContext(), "🗑️ Servicio eliminado", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Error eliminando servicio: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // ========== MÉTODOS DE CONVERSIÓN Y UTILIDADES ==========

    private HotelServiceItem convertFirebaseToHotelServiceItem(HotelServiceModel model) {
        HotelServiceItem.ServiceType type;
        switch (model.getServiceType()) {
            case "basic": type = HotelServiceItem.ServiceType.BASIC; break;
            case "included": type = HotelServiceItem.ServiceType.INCLUDED; break;
            case "paid": type = HotelServiceItem.ServiceType.PAID; break;
            case "conditional": type = HotelServiceItem.ServiceType.CONDITIONAL; break;
            default: type = HotelServiceItem.ServiceType.INCLUDED; break;
        }

        // ✅ Convertir URLs a URIs
        List<android.net.Uri> photoUris = new ArrayList<>();
        if (model.getPhotoUrls() != null) {
            for (String url : model.getPhotoUrls()) {
                photoUris.add(android.net.Uri.parse(url));
            }
        }

        HotelServiceItem item = new HotelServiceItem(
                model.getName(),
                model.getDescription(),
                model.getPrice(),
                model.getIconKey(),
                type,
                photoUris,
                model.getConditionalAmount()
        );
        item.setActive(model.isActive());
        item.setFirebaseId(model.getId()); // ✅ Guardar ID de Firebase

        return item;
    }

    private HotelServiceModel convertHotelServiceItemToFirebase(HotelServiceItem item) {
        String typeString;
        switch (item.getType()) {
            case BASIC: typeString = "basic"; break;
            case INCLUDED: typeString = "included"; break;
            case PAID: typeString = "paid"; break;
            case CONDITIONAL: typeString = "conditional"; break;
            default: typeString = "included"; break;
        }

        HotelServiceModel model = new HotelServiceModel(
                item.getName(),
                item.getDescription(),
                item.getIconKey(),
                typeString
        );
        model.setPrice(item.getPrice());
        model.setConditionalAmount(item.getConditionalAmount());
        model.setActive(item.isActive());

        // ✅ Convertir URIs a URLs
        List<String> photoUrls = new ArrayList<>();
        for (android.net.Uri uri : item.getPhotos()) {
            photoUrls.add(uri.toString());
        }
        model.setPhotoUrls(photoUrls);

        return model;
    }

    private String findFirebaseServiceId(HotelServiceItem service) {
        // ✅ Si el servicio tiene ID de Firebase guardado
        if (service.getFirebaseId() != null) {
            return service.getFirebaseId();
        }

        Log.w(TAG, "No se pudo encontrar ID de Firebase para: " + service.getName());
        return null;
    }

    private void updateAdapterForService(HotelServiceItem service) {
        switch (service.getType()) {
            case BASIC:
                if (basicServicesAdapter != null) basicServicesAdapter.notifyDataSetChanged();
                break;
            case INCLUDED:
                if (includedServicesAdapter != null) includedServicesAdapter.notifyDataSetChanged();
                break;
            case PAID:
                if (paidServicesAdapter != null) paidServicesAdapter.notifyDataSetChanged();
                break;
            case CONDITIONAL:
                if (conditionalServicesAdapter != null) conditionalServicesAdapter.notifyDataSetChanged();
                break;
        }
    }
}