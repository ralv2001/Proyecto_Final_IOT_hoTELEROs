package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceItem;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceModel;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseServiceManager;
import com.example.proyecto_final_hoteleros.adminhotel.utils.ServiceSyncManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ServiceManagementFragment extends Fragment implements FirebaseServiceManager.OnServicesChangedListener {

    private static final String TAG = "ServiceManagementFragment";

    // Views principales
    private ImageView ivBack;

    // Botones para agregar servicios por categoría
    private MaterialButton btnAddIncludedService;
    private MaterialButton btnAddPaidService;
    private MaterialButton btnAddConditionalService;

    // RecyclerViews para cada tipo de servicio
    private RecyclerView rvBasicServices, rvIncludedServices, rvPaidServices, rvConditionalServices;
    private View layoutBasicServicesEmpty, layoutIncludedServicesEmpty, layoutPaidServicesEmpty, layoutConditionalServicesEmpty;

    // Adapters
    private ServiceManagementAdapter basicServicesAdapter, includedServicesAdapter, paidServicesAdapter, conditionalServicesAdapter;

    // Listas de servicios
    private List<HotelServiceItem> basicServices;
    private List<HotelServiceItem> includedServices;
    private List<HotelServiceItem> paidServices;
    private List<HotelServiceItem> conditionalServices;

    // Managers
    private FirebaseServiceManager firebaseServiceManager;
    private ServiceSyncManager serviceSyncManager;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> photoPickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar managers con Context
        firebaseServiceManager = FirebaseServiceManager.getInstance(getContext());
        firebaseServiceManager.addListener(this);
        serviceSyncManager = ServiceSyncManager.getInstance(getContext());

        setupActivityResultLaunchers();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_hotel_fragment_service_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        initializeLists();
        setupRecyclerViews();
        setupClickListeners();

        loadServicesFromFirebase();
    }

    private void initializeViews(View view) {
        // Header
        ivBack = view.findViewById(R.id.ivBack);

        // Botones para agregar servicios
        btnAddIncludedService = view.findViewById(R.id.btnAddIncludedService);
        btnAddPaidService = view.findViewById(R.id.btnAddPaidService);
        btnAddConditionalService = view.findViewById(R.id.btnAddConditionalService);

        // RecyclerViews
        rvBasicServices = view.findViewById(R.id.rvBasicServices);
        rvIncludedServices = view.findViewById(R.id.rvIncludedServices);
        rvPaidServices = view.findViewById(R.id.rvPaidServices);
        rvConditionalServices = view.findViewById(R.id.rvConditionalServices);

        // Estados vacíos
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

        // Setup adapters para todos los tipos de servicios
        basicServicesAdapter = new ServiceManagementAdapter(basicServices, editListener);
        includedServicesAdapter = new ServiceManagementAdapter(includedServices, editListener);
        paidServicesAdapter = new ServiceManagementAdapter(paidServices, editListener);
        conditionalServicesAdapter = new ServiceManagementAdapter(conditionalServices, editListener);

        // Configurar RecyclerViews
        setupRecyclerView(rvBasicServices, basicServicesAdapter);
        setupRecyclerView(rvIncludedServices, includedServicesAdapter);
        setupRecyclerView(rvPaidServices, paidServicesAdapter);
        setupRecyclerView(rvConditionalServices, conditionalServicesAdapter);

        Log.d(TAG, "✅ Todos los RecyclerViews configurados");
    }

    private void setupRecyclerView(RecyclerView recyclerView, ServiceManagementAdapter adapter) {
        if (recyclerView != null && adapter != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
            recyclerView.setNestedScrollingEnabled(false);
        }
    }

    private void setupClickListeners() {
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        if (btnAddIncludedService != null) {
            btnAddIncludedService.setOnClickListener(v -> showAddServiceDialog("included"));
        }

        if (btnAddPaidService != null) {
            btnAddPaidService.setOnClickListener(v -> showAddServiceDialog("paid"));
        }

        if (btnAddConditionalService != null) {
            btnAddConditionalService.setOnClickListener(v -> showAddServiceDialog("conditional"));
        }
    }

    private void setupActivityResultLaunchers() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // Manejar resultado de selección de fotos
                    }
                }
        );
    }

    // ========== CARGA DE SERVICIOS ==========

    private void loadServicesFromFirebase() {
        Log.d(TAG, "📊 Cargando servicios desde Firebase...");

        firebaseServiceManager.getAllServices(new FirebaseServiceManager.ServicesListCallback() {
            @Override
            public void onSuccess(List<HotelServiceModel> allServices) {
                Log.d(TAG, "✅ Todos los servicios obtenidos: " + allServices.size());

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        classifyAndDisplayServices(allServices);
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando servicios: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error cargando servicios: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void classifyAndDisplayServices(List<HotelServiceModel> allServices) {
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
                    break;
                case "included":
                    includedServices.add(item);
                    break;
                case "paid":
                    paidServices.add(item);
                    break;
                case "conditional":
                    conditionalServices.add(item);
                    break;
            }
        }

        // Actualizar UI
        updateAllAdapters();
        updateEmptyStates();
    }

    // ========== GESTIÓN DE SERVICIOS ==========

    private void showAddServiceDialog(String serviceType) {
        AddServiceDialog dialog = new AddServiceDialog(getContext(), photoPickerLauncher, service -> {
            if (service != null) {
                addNewService(service);
            }
        });
        dialog.show();
    }

    private void addNewService(HotelServiceItem service) {
        Log.d(TAG, "➕ Agregando nuevo servicio: " + service.getName());

        // Convertir a Firebase model
        HotelServiceModel firebaseModel = convertHotelServiceItemToFirebase(service);

        // Guardar en Firebase
        firebaseServiceManager.createService(firebaseModel, service.getPhotos(), new FirebaseServiceManager.ServiceCallback() {
            @Override
            public void onSuccess(HotelServiceModel serviceModel) {
                Log.d(TAG, "✅ Servicio guardado en Firebase");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Servicio agregado exitosamente", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error guardando servicio: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error guardando servicio: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void editService(HotelServiceItem service, int position) {
        Log.d(TAG, "✏️ Editando servicio: " + service.getName());

        EditServiceDialog dialog = new EditServiceDialog(getContext(), service, updatedService -> {
            if (updatedService != null) {
                updateService(updatedService, position);
            }
        });
        dialog.show();
    }

    private void updateService(HotelServiceItem updatedService, int position) {
        Log.d(TAG, "🔄 Actualizando servicio: " + updatedService.getName());

        // Convertir a Firebase model
        HotelServiceModel firebaseModel = convertHotelServiceItemToFirebase(updatedService);

        // Actualizar en Firebase
        firebaseServiceManager.updateService(firebaseModel, updatedService.getPhotos(), new FirebaseServiceManager.ServiceCallback() {
            @Override
            public void onSuccess(HotelServiceModel serviceModel) {
                Log.d(TAG, "✅ Servicio actualizado en Firebase");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Servicio actualizado exitosamente", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error actualizando servicio: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error actualizando servicio: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void deleteService(HotelServiceItem service, int position) {
        Log.d(TAG, "🗑️ Eliminando servicio: " + service.getName());

        // Buscar ID en Firebase
        String firebaseId = findFirebaseServiceId(service);
        if (firebaseId == null) {
            Toast.makeText(getContext(), "Error: No se pudo encontrar el servicio en Firebase", Toast.LENGTH_SHORT).show();
            return;
        }

        // Eliminar de Firebase
        firebaseServiceManager.deleteService(firebaseId, new FirebaseServiceManager.ServiceCallback() {
            @Override
            public void onSuccess(HotelServiceModel serviceModel) {
                Log.d(TAG, "✅ Servicio eliminado de Firebase");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Servicio eliminado exitosamente", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error eliminando servicio: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error eliminando servicio: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void toggleServiceStatus(HotelServiceItem service, int position, boolean isActive) {
        Log.d(TAG, "🔄 Cambiando estado de servicio: " + service.getName() + " a " + isActive);
        service.setActive(isActive);
        updateService(service, position);
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    private void updateAllAdapters() {
        if (basicServicesAdapter != null) basicServicesAdapter.notifyDataSetChanged();
        if (includedServicesAdapter != null) includedServicesAdapter.notifyDataSetChanged();
        if (paidServicesAdapter != null) paidServicesAdapter.notifyDataSetChanged();
        if (conditionalServicesAdapter != null) conditionalServicesAdapter.notifyDataSetChanged();
    }

    private void updateEmptyStates() {
        updateEmptyState(layoutBasicServicesEmpty, rvBasicServices, basicServices.isEmpty());
        updateEmptyState(layoutIncludedServicesEmpty, rvIncludedServices, includedServices.isEmpty());
        updateEmptyState(layoutPaidServicesEmpty, rvPaidServices, paidServices.isEmpty());
        updateEmptyState(layoutConditionalServicesEmpty, rvConditionalServices, conditionalServices.isEmpty());
    }

    private void updateEmptyState(View emptyLayout, RecyclerView recyclerView, boolean isEmpty) {
        if (emptyLayout != null) {
            emptyLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    // ========== CONVERSIONES ==========

    private HotelServiceItem convertFirebaseToHotelServiceItem(HotelServiceModel model) {
        HotelServiceItem.ServiceType type;
        switch (model.getServiceType()) {
            case "basic": type = HotelServiceItem.ServiceType.BASIC; break;
            case "included": type = HotelServiceItem.ServiceType.INCLUDED; break;
            case "paid": type = HotelServiceItem.ServiceType.PAID; break;
            case "conditional": type = HotelServiceItem.ServiceType.CONDITIONAL; break;
            default: type = HotelServiceItem.ServiceType.INCLUDED; break;
        }

        // Usar el constructor correcto con parámetros requeridos
        HotelServiceItem item = new HotelServiceItem(
                model.getName(),
                model.getDescription(),
                model.getPrice(),
                model.getIconKey(),
                type,
                new ArrayList<>(), // photos - se asignarán después
                model.getConditionalAmount()
        );

        item.setActive(model.isActive());

        // Convertir URLs a URIs
        List<android.net.Uri> photos = new ArrayList<>();
        if (model.getPhotoUrls() != null) {
            for (String url : model.getPhotoUrls()) {
                photos.add(android.net.Uri.parse(url));
            }
        }
        item.setPhotos(photos);

        if (model.getId() != null) {
            item.setFirebaseId(model.getId());
        }

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

        // Convertir URIs a URLs
        List<String> photoUrls = new ArrayList<>();
        if (item.getPhotos() != null) {
            for (android.net.Uri uri : item.getPhotos()) {
                photoUrls.add(uri.toString());
            }
        }
        model.setPhotoUrls(photoUrls);

        return model;
    }

    private String findFirebaseServiceId(HotelServiceItem service) {
        if (service.getFirebaseId() != null) {
            return service.getFirebaseId();
        }
        Log.w(TAG, "No se pudo encontrar ID de Firebase para: " + service.getName());
        return null;
    }

    // ========== CALLBACKS DE FIREBASE ==========

    @Override
    public void onBasicServicesUpdated(List<HotelServiceModel> basicServiceModels) {
        Log.d(TAG, "🔄 Servicios básicos actualizados: " + basicServiceModels.size());
        loadServicesFromFirebase();
    }

    @Override
    public void onAllServicesUpdated(List<HotelServiceModel> allServiceModels) {
        Log.d(TAG, "🔄 Todos los servicios actualizados: " + allServiceModels.size());
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                classifyAndDisplayServices(allServiceModels);
            });
        }
    }

    @Override
    public void onServiceAdded(HotelServiceModel service) {
        Log.d(TAG, "➕ Servicio agregado: " + service.getName());
        loadServicesFromFirebase();
    }

    @Override
    public void onServiceUpdated(HotelServiceModel service) {
        Log.d(TAG, "🔄 Servicio actualizado: " + service.getName());
        loadServicesFromFirebase();
    }

    @Override
    public void onServiceDeleted(String serviceId) {
        Log.d(TAG, "🗑️ Servicio eliminado: " + serviceId);
        loadServicesFromFirebase();
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "❌ Error en servicios: " + error);
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (firebaseServiceManager != null) {
            firebaseServiceManager.removeListener(this);
        }
    }
}