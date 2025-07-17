package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.app.Activity;
import android.app.AlertDialog;
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
import com.example.proyecto_final_hoteleros.adminhotel.dialog.ServicePhotoViewerDialog;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceItem;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceModel;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseServiceManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ServiceManagementFragment extends Fragment implements FirebaseServiceManager.OnServicesChangedListener {

    private static final String TAG = "ServiceManagementFragment";

    // Views principales
    private ImageView ivBack;
    private MaterialButton btnAddService;
    private FloatingActionButton fabAddService;

    // Campos para taxi
    private TextInputEditText etTaxiMinAmount;
    private MaterialButton btnSaveTaxiConfig;

    // RecyclerViews para TODOS los tipos de servicio
    private RecyclerView rvBasicServices, rvIncludedServices, rvPaidServices, rvConditionalServices;
    private View layoutBasicServicesEmpty, layoutIncludedServicesEmpty, layoutPaidServicesEmpty, layoutConditionalServicesEmpty;

    // Adapters para todos los tipos
    private ServiceManagementAdapter basicServicesAdapter, includedServicesAdapter, paidServicesAdapter, conditionalServicesAdapter;

    // Listas de servicios
    private List<HotelServiceItem> basicServices;
    private List<HotelServiceItem> includedServices;
    private List<HotelServiceItem> paidServices;
    private List<HotelServiceItem> conditionalServices;

    // Managers
    private FirebaseServiceManager firebaseServiceManager;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> photoPickerLauncher;

    // Referencia al diálogo activo para manejar fotos
    private AddServiceDialog currentAddServiceDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseServiceManager = FirebaseServiceManager.getInstance(getContext());

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
        setupListeners();

        // ✅ CRÍTICO: Registrar listener DESPUÉS de configurar todo
        if (firebaseServiceManager != null) {
            firebaseServiceManager.addListener(this);
        }

        // ✅ FORZAR carga inicial
        loadAllServicesFromFirebase();
    }

    private void setupActivityResultLaunchers() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Log.d(TAG, "📷 Resultado de foto recibido");
                        if (currentAddServiceDialog != null) {
                            Log.d(TAG, "📷 Enviando resultado de fotos al diálogo activo");
                            currentAddServiceDialog.handlePhotoResult(result.getData());
                        } else {
                            Log.w(TAG, "📷 No hay diálogo activo para recibir las fotos");
                        }
                    } else {
                        Log.w(TAG, "📷 Selección de foto cancelada o falló");
                    }
                }
        );
    }

    private void initializeViews(View view) {
        ivBack = view.findViewById(R.id.ivBack);
        fabAddService = view.findViewById(R.id.fabAddService);

        // Taxi fields
        btnSaveTaxiConfig = view.findViewById(R.id.btnSaveTaxiConfig);

        // RecyclerViews
        rvBasicServices = view.findViewById(R.id.rvBasicServices);
        rvIncludedServices = view.findViewById(R.id.rvIncludedServices);
        rvPaidServices = view.findViewById(R.id.rvPaidServices);
        rvConditionalServices = view.findViewById(R.id.rvConditionalServices);

        // Empty states
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
        // CREAR LISTENER DE ACCIONES
        ServiceManagementAdapter.OnServiceActionListener actionListener = new ServiceManagementAdapter.OnServiceActionListener() {
            @Override
            public void onEditService(HotelServiceItem service, int position) {
                showEditServiceDialog(service, position);
            }

            @Override
            public void onDeleteService(HotelServiceItem service, int position) {
                showDeleteServiceDialog(service, position);
            }

            @Override
            public void onToggleService(HotelServiceItem service, int position, boolean isActive) {
                toggleServiceState(service, position, isActive);
            }
        };

        // CREAR LISTENER DE FOTOS
        ServiceManagementAdapter.OnServicePhotoClickListener photoListener = this::showServicePhotos;

        // Configurar RecyclerView de servicios básicos
        basicServicesAdapter = new ServiceManagementAdapter(
                getContext(),
                basicServices,
                actionListener,
                photoListener
        );
        rvBasicServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBasicServices.setAdapter(basicServicesAdapter);

        // Configurar RecyclerView de servicios incluidos
        includedServicesAdapter = new ServiceManagementAdapter(
                getContext(),
                includedServices,
                actionListener,
                photoListener
        );
        rvIncludedServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvIncludedServices.setAdapter(includedServicesAdapter);

        // Configurar RecyclerView de servicios de pago
        paidServicesAdapter = new ServiceManagementAdapter(
                getContext(),
                paidServices,
                actionListener,
                photoListener
        );
        rvPaidServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPaidServices.setAdapter(paidServicesAdapter);

        // Configurar RecyclerView de servicios condicionales
        conditionalServicesAdapter = new ServiceManagementAdapter(
                getContext(),
                conditionalServices,
                actionListener,
                photoListener
        );
        rvConditionalServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvConditionalServices.setAdapter(conditionalServicesAdapter);

        Log.d(TAG, "✅ RecyclerViews configurados");
    }

    private void setupListeners() {
        // Botón volver
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    requireActivity().finish();
                }
            });
        }

        // Botones para agregar servicio
        if (btnAddService != null) {
            btnAddService.setOnClickListener(v -> openAddServiceDialog());
        }
        if (fabAddService != null) {
            fabAddService.setOnClickListener(v -> openAddServiceDialog());
        }

        // Configuración de taxi
        if (btnSaveTaxiConfig != null) {
            btnSaveTaxiConfig.setOnClickListener(v -> saveTaxiConfiguration());
        }
    }

    private void loadAllServicesFromFirebase() {
        Log.d(TAG, "🔄 Cargando todos los servicios desde Firebase...");

        if (firebaseServiceManager != null) {
            firebaseServiceManager.getAllServices(new FirebaseServiceManager.ServicesListCallback() {
                @Override
                public void onSuccess(List<HotelServiceModel> services) {
                    Log.d(TAG, "✅ Servicios cargados desde Firebase: " + services.size());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            classifyAndDisplayServices(services);
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
        } else {
            Log.e(TAG, "❌ FirebaseServiceManager es null");
        }
    }

    // ✅ MÉTODO CORREGIDO: Clasificar y mostrar servicios
    private void classifyAndDisplayServices(List<HotelServiceModel> allServices) {
        Log.d(TAG, "📊 Clasificando " + allServices.size() + " servicios");

        // Limpiar TODAS las listas
        basicServices.clear();
        includedServices.clear();
        paidServices.clear();
        conditionalServices.clear();

        // Clasificar servicios correctamente
        for (HotelServiceModel service : allServices) {
            try {
                HotelServiceItem item = convertToServiceItem(service);

                String serviceType = service.getServiceType();
                if (serviceType == null) serviceType = "included"; // Default

                switch (serviceType.toLowerCase()) {
                    case "basic":
                    case "básico":
                        basicServices.add(item);
                        Log.d(TAG, "➕ Servicio básico agregado: " + service.getName());
                        break;

                    case "included":
                    case "incluido":
                        includedServices.add(item);
                        Log.d(TAG, "➕ Servicio incluido agregado: " + service.getName());
                        break;

                    case "paid":
                    case "pagado":
                    case "pago":
                        paidServices.add(item);
                        Log.d(TAG, "➕ Servicio de pago agregado: " + service.getName());
                        break;

                    case "conditional":
                    case "condicional":
                        conditionalServices.add(item);
                        Log.d(TAG, "➕ Servicio condicional agregado: " + service.getName());
                        break;

                    default:
                        includedServices.add(item); // Default a incluidos
                        Log.w(TAG, "⚠️ Tipo de servicio desconocido: " + serviceType + " para " + service.getName() + " - agregado como incluido");
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error procesando servicio " + service.getName() + ": " + e.getMessage());
            }
        }

        // ✅ ACTUALIZAR ADAPTERS EN EL HILO PRINCIPAL
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                updateAdapters();
                updateEmptyStatesVisibility();
            });
        }

        Log.d(TAG, "✅ Servicios clasificados - Básicos: " + basicServices.size() +
                ", Incluidos: " + includedServices.size() +
                ", Pagados: " + paidServices.size() +
                ", Condicionales: " + conditionalServices.size());
    }

    // ✅ NUEVO MÉTODO: Actualizar adapters
    private void updateAdapters() {
        if (basicServicesAdapter != null) {
            basicServicesAdapter.updateServices(basicServices);
            Log.d(TAG, "🔄 Adapter básicos actualizado con " + basicServices.size() + " servicios");
        }
        if (includedServicesAdapter != null) {
            includedServicesAdapter.updateServices(includedServices);
            Log.d(TAG, "🔄 Adapter incluidos actualizado con " + includedServices.size() + " servicios");
        }
        if (paidServicesAdapter != null) {
            paidServicesAdapter.updateServices(paidServices);
            Log.d(TAG, "🔄 Adapter pagados actualizado con " + paidServices.size() + " servicios");
        }
        if (conditionalServicesAdapter != null) {
            conditionalServicesAdapter.updateServices(conditionalServices);
            Log.d(TAG, "🔄 Adapter condicionales actualizado con " + conditionalServices.size() + " servicios");
        }
    }

    // ✅ MÉTODO MEJORADO: Convertir HotelServiceModel a HotelServiceItem
    private HotelServiceItem convertToServiceItem(HotelServiceModel service) {
        // Convertir tipo de servicio
        HotelServiceItem.ServiceType serviceType = convertStringToServiceType(service.getServiceType());

        // Crear lista de URIs de fotos
        List<android.net.Uri> photoUris = new ArrayList<>();
        if (service.getPhotoUrls() != null) {
            for (String url : service.getPhotoUrls()) {
                try {
                    photoUris.add(android.net.Uri.parse(url));
                } catch (Exception e) {
                    Log.w(TAG, "Error parseando URL de foto: " + url);
                }
            }
        }

        // Crear servicio con datos completos
        HotelServiceItem item = new HotelServiceItem(
                service.getName() != null ? service.getName() : "",
                service.getDescription() != null ? service.getDescription() : "",
                service.getPrice(),
                service.getIconKey() != null ? service.getIconKey() : "ic_service_default",
                serviceType,
                photoUris,
                service.getConditionalAmount()
        );

        // Establecer ID de Firebase y estado activo
        item.setFirebaseId(service.getId());
        item.setActive(service.isActive());

        Log.d(TAG, "🔄 Convertido: " + service.getName() + " (Tipo: " + serviceType + ", Fotos: " + photoUris.size() + ")");
        return item;
    }

    // MÉTODO PARA CONVERTIR STRING A ServiceType ENUM
    private HotelServiceItem.ServiceType convertStringToServiceType(String serviceType) {
        if (serviceType == null) return HotelServiceItem.ServiceType.INCLUDED;

        switch (serviceType.toLowerCase()) {
            case "basic":
            case "básico":
                return HotelServiceItem.ServiceType.BASIC;
            case "included":
            case "incluido":
                return HotelServiceItem.ServiceType.INCLUDED;
            case "paid":
            case "pagado":
            case "pago":
                return HotelServiceItem.ServiceType.PAID;
            case "conditional":
            case "condicional":
                return HotelServiceItem.ServiceType.CONDITIONAL;
            default:
                return HotelServiceItem.ServiceType.INCLUDED;
        }
    }

    // Actualizar visibilidad de estados vacíos
    private void updateEmptyStatesVisibility() {
        // Básicos
        if (rvBasicServices != null && layoutBasicServicesEmpty != null) {
            rvBasicServices.setVisibility(basicServices.isEmpty() ? View.GONE : View.VISIBLE);
            layoutBasicServicesEmpty.setVisibility(basicServices.isEmpty() ? View.VISIBLE : View.GONE);
        }

        // Incluidos
        if (rvIncludedServices != null && layoutIncludedServicesEmpty != null) {
            rvIncludedServices.setVisibility(includedServices.isEmpty() ? View.GONE : View.VISIBLE);
            layoutIncludedServicesEmpty.setVisibility(includedServices.isEmpty() ? View.VISIBLE : View.GONE);
        }

        // De pago
        if (rvPaidServices != null && layoutPaidServicesEmpty != null) {
            rvPaidServices.setVisibility(paidServices.isEmpty() ? View.GONE : View.VISIBLE);
            layoutPaidServicesEmpty.setVisibility(paidServices.isEmpty() ? View.VISIBLE : View.GONE);
        }

        // Condicionales
        if (rvConditionalServices != null && layoutConditionalServicesEmpty != null) {
            rvConditionalServices.setVisibility(conditionalServices.isEmpty() ? View.GONE : View.VISIBLE);
            layoutConditionalServicesEmpty.setVisibility(conditionalServices.isEmpty() ? View.VISIBLE : View.GONE);
        }

        Log.d(TAG, "✅ Estados vacíos actualizados");
    }

    // MÉTODO CRÍTICO ACTUALIZADO: Ahora usa AddServiceDialog con manejo correcto de fotos
    private void openAddServiceDialog() {
        Log.d(TAG, "🚀 Abriendo diálogo para agregar servicio");

        currentAddServiceDialog = new AddServiceDialog(getContext(), photoPickerLauncher, new AddServiceDialog.OnServiceAddedListener() {
            @Override
            public void onServiceAdded(HotelServiceItem service) {
                Log.d(TAG, "✅ Servicio agregado desde diálogo: " + service.getName());
                Toast.makeText(getContext(), "✅ Servicio agregado: " + service.getName(), Toast.LENGTH_SHORT).show();

                // Limpiar referencia
                currentAddServiceDialog = null;

                // ✅ RECARGAR servicios después de agregar
                loadAllServicesFromFirebase();
            }
        });

        // Configurar listener para limpiar referencia cuando se cierre
        currentAddServiceDialog.setOnDismissListener(dialog -> {
            Log.d(TAG, "🚪 Diálogo de agregar servicio cerrado");
            currentAddServiceDialog = null;
        });

        currentAddServiceDialog.show();
    }

    private void saveTaxiConfiguration() {
        if (etTaxiMinAmount == null) return;

        String minAmount = etTaxiMinAmount.getText().toString().trim();

        if (minAmount.isEmpty()) {
            Toast.makeText(getContext(), "⚠️ Ingresa el monto mínimo", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(minAmount);
            Toast.makeText(getContext(), "💾 Configuración guardada: S/. " + amount, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "💾 Configuración de taxi guardada: S/. " + amount);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "⚠️ Ingresa un monto válido", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditServiceDialog(HotelServiceItem service, int position) {
        EditServiceDialog dialog = new EditServiceDialog(getContext(), service,
                editedService -> {
                    Log.d(TAG, "🔧 Servicio editado: " + editedService.getName());
                    loadAllServicesFromFirebase();
                }
        );
        dialog.show();
    }

    private void showServicePhotos(String photoUrl, int photoPosition, List<String> allPhotos) {
        ServicePhotoViewerDialog dialog = new ServicePhotoViewerDialog(
                getContext(),
                allPhotos,
                photoPosition,
                "Fotos del servicio"
        );
        dialog.show();
    }

    private void showDeleteServiceDialog(HotelServiceItem service, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Eliminar servicio");
        builder.setMessage("¿Estás seguro de que quieres eliminar el servicio '" + service.getName() + "'?");

        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            if (firebaseServiceManager != null && service.getFirebaseId() != null) {
                firebaseServiceManager.deleteService(service.getFirebaseId(), new FirebaseServiceManager.ServiceCallback() {
                    @Override
                    public void onSuccess(HotelServiceModel deletedService) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "✅ Servicio eliminado: " + service.getName(), Toast.LENGTH_SHORT).show();
                                // La recarga automática se maneja por el listener
                            });
                        }
                        Log.d(TAG, "✅ Servicio eliminado exitosamente: " + service.getName());
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "❌ Error eliminando servicio: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                        Log.e(TAG, "❌ Error eliminando servicio: " + error);
                    }
                });
            } else {
                Toast.makeText(getContext(), "❌ No se puede eliminar: ID no válido", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "❌ No se puede eliminar servicio: FirebaseServiceManager o ID es null");
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void toggleServiceState(HotelServiceItem service, int position, boolean isActive) {
        service.setActive(isActive);
        String status = isActive ? "activado" : "desactivado";
        Toast.makeText(getContext(), "🔄 Servicio " + status + ": " + service.getName(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "🔄 Estado del servicio cambiado: " + service.getName() + " -> " + status);
    }

    // ✅ IMPLEMENTANDO TODOS LOS MÉTODOS DE LA INTERFAZ OnServicesChangedListener

    @Override
    public void onBasicServicesUpdated(List<HotelServiceModel> basicServices) {
        Log.d(TAG, "🔄 Servicios básicos actualizados: " + basicServices.size());
        // No necesitamos hacer nada aquí, el onAllServicesUpdated se encarga
    }

    @Override
    public void onAllServicesUpdated(List<HotelServiceModel> allServices) {
        Log.d(TAG, "🔄 Todos los servicios actualizados: " + allServices.size());
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                classifyAndDisplayServices(allServices);
            });
        }
    }

    @Override
    public void onServiceAdded(HotelServiceModel service) {
        Log.d(TAG, "➕ Servicio agregado desde Firebase: " + service.getName());
        // El onAllServicesUpdated se encarga de recargar todos
    }

    @Override
    public void onServiceUpdated(HotelServiceModel service) {
        Log.d(TAG, "🔄 Servicio actualizado desde Firebase: " + service.getName());
        // El onAllServicesUpdated se encarga de recargar todos
    }

    @Override
    public void onServiceDeleted(String serviceId) {
        Log.d(TAG, "🗑️ Servicio eliminado desde Firebase: " + serviceId);
        // El onAllServicesUpdated se encarga de recargar todos
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "❌ Error desde Firebase: " + error);
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
        // Limpiar referencia al diálogo
        currentAddServiceDialog = null;
        Log.d(TAG, "🧹 Fragment destruido y listeners limpiados");
    }
}