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

import com.android.volley.BuildConfig;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.ServiceManagementAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.AddServiceDialog;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.EditServiceDialog;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.ServicePhotoViewerDialog;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.TaxiConfigSuccessDialog;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceModel;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseServiceManager;
import com.example.proyecto_final_hoteleros.adminhotel.utils.ScrollableLinearLayoutManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class ServiceManagementFragment extends Fragment implements FirebaseServiceManager.OnServicesChangedListener {

    private static final String TAG = "ServiceManagementFragment";

    // Views principales
    private ImageView ivBack;
    private FloatingActionButton fabAddService;

    // Campos para taxi
    private EditText etTaxiMinAmount;
    private MaterialButton btnSaveTaxiConfig;

    // RecyclerViews para los 3 tipos de servicio
    private RecyclerView rvBasicServices, rvIncludedServices, rvPaidServices;
    private View layoutBasicServicesEmpty, layoutIncludedServicesEmpty, layoutPaidServicesEmpty;

    // Adapters para los 3 tipos
    private ServiceManagementAdapter basicServicesAdapter, includedServicesAdapter, paidServicesAdapter;

    // ✅ SIMPLIFICADO: Listas de HotelServiceModel directamente
    private List<HotelServiceModel> basicServices;
    private List<HotelServiceModel> includedServices;
    private List<HotelServiceModel> paidServices;

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

        // ✅ NUEVO: Debugging automático después de 3 segundos
        view.postDelayed(() -> {
            Log.d(TAG, "🕒 === DEBUGGING AUTOMÁTICO DESPUÉS DE 3 SEGUNDOS ===");
            debugBasicServicesSpecifically();

            // Si no hay servicios básicos visibles, forzar refresh
            if (rvBasicServices != null && rvBasicServices.getChildCount() == 0 && !basicServices.isEmpty()) {
                Log.w(TAG, "⚠️ Servicios básicos en lista pero no visibles - FORZANDO REFRESH");
                forceBasicServicesRefresh();
            }
        }, 3000);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 onResume - Fragment activo");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "⏸️ onPause - Fragment pausado");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "🗑️ onDestroyView - Limpiando listeners");

        // Limpiar listener al destruir la vista
        if (firebaseServiceManager != null) {
            firebaseServiceManager.removeListener(this);
        }
    }

    private void setupActivityResultLaunchers() {
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

    private void initializeViews(View view) {
        // Views principales
        ivBack = view.findViewById(R.id.ivBack);
        fabAddService = view.findViewById(R.id.fabAddService);

        // Campos de taxi
        etTaxiMinAmount = view.findViewById(R.id.etTaxiMinAmount);
        btnSaveTaxiConfig = view.findViewById(R.id.btnSaveTaxiConfig);

        // RecyclerViews
        rvBasicServices = view.findViewById(R.id.rvBasicServices);
        rvIncludedServices = view.findViewById(R.id.rvIncludedServices);
        rvPaidServices = view.findViewById(R.id.rvPaidServices);

        // Estados vacíos
        layoutBasicServicesEmpty = view.findViewById(R.id.layoutBasicServicesEmpty);
        layoutIncludedServicesEmpty = view.findViewById(R.id.layoutIncludedServicesEmpty);
        layoutPaidServicesEmpty = view.findViewById(R.id.layoutPaidServicesEmpty);
    }

    private void initializeLists() {
        // ✅ SIMPLIFICADO: Solo HotelServiceModel
        basicServices = new ArrayList<>();
        includedServices = new ArrayList<>();
        paidServices = new ArrayList<>();
    }

    private void setupRecyclerViews() {
        // CREAR LISTENER DE ACCIONES
        ServiceManagementAdapter.OnServiceActionListener actionListener = new ServiceManagementAdapter.OnServiceActionListener() {
            @Override
            public void onEditService(HotelServiceModel service, int position) {
                showEditServiceDialog(service, position);
            }

            @Override
            public void onDeleteService(HotelServiceModel service, int position) {
                showDeleteServiceDialog(service, position);
            }

            @Override
            public void onToggleService(HotelServiceModel service, int position, boolean isActive) {
                toggleServiceState(service, position, isActive);
            }
        };

        // CREAR LISTENER DE FOTOS
        ServiceManagementAdapter.OnServicePhotoClickListener photoListener = this::showServicePhotos;

        // ✅ SOLUCIONADO: Usar ScrollableLinearLayoutManager para mostrar TODOS los elementos
        basicServicesAdapter = new ServiceManagementAdapter(
                getContext(),
                basicServices,
                actionListener,
                photoListener
        );
        ScrollableLinearLayoutManager basicLayoutManager = new ScrollableLinearLayoutManager(getContext());
        rvBasicServices.setLayoutManager(basicLayoutManager);
        rvBasicServices.setAdapter(basicServicesAdapter);
        rvBasicServices.setHasFixedSize(false);
        rvBasicServices.setNestedScrollingEnabled(false);

        // ✅ SOLUCIONADO: Usar ScrollableLinearLayoutManager para servicios incluidos
        includedServicesAdapter = new ServiceManagementAdapter(
                getContext(),
                includedServices,
                actionListener,
                photoListener
        );
        ScrollableLinearLayoutManager includedLayoutManager = new ScrollableLinearLayoutManager(getContext());
        rvIncludedServices.setLayoutManager(includedLayoutManager);
        rvIncludedServices.setAdapter(includedServicesAdapter);
        rvIncludedServices.setHasFixedSize(false);
        rvIncludedServices.setNestedScrollingEnabled(false);

        // ✅ SOLUCIONADO: Usar ScrollableLinearLayoutManager para servicios pagados
        paidServicesAdapter = new ServiceManagementAdapter(
                getContext(),
                paidServices,
                actionListener,
                photoListener
        );
        ScrollableLinearLayoutManager paidLayoutManager = new ScrollableLinearLayoutManager(getContext());
        rvPaidServices.setLayoutManager(paidLayoutManager);
        rvPaidServices.setAdapter(paidServicesAdapter);
        rvPaidServices.setHasFixedSize(false);
        rvPaidServices.setNestedScrollingEnabled(false);

        Log.d(TAG, "✅ Todos los RecyclerViews configurados con ScrollableLinearLayoutManager");
    }

    private void setupListeners() {
        // Botón de volver
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        // FAB para agregar servicio
        if (fabAddService != null) {
            fabAddService.setOnClickListener(v -> showAddServiceDialog());
        }

        // Botón para guardar configuración de taxi
        if (btnSaveTaxiConfig != null) {
            btnSaveTaxiConfig.setOnClickListener(v -> saveTaxiConfiguration());
        }

        // Cargar configuración de taxi al inicializar
        loadTaxiConfigurationFromFirebase();
    }

    // ========== MÉTODOS DE CARGA DE DATOS ==========

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
        }
    }

    // ✅ SIMPLIFICADO: Clasificar servicios sin conversiones
    private void classifyAndDisplayServices(List<HotelServiceModel> allServices) {
        Log.d(TAG, "📊 === CLASIFICANDO " + allServices.size() + " SERVICIOS ===");

        // Limpiar listas
        basicServices.clear();
        includedServices.clear();
        paidServices.clear();

        // Contadores para debugging
        int basicCount = 0, includedCount = 0, paidCount = 0, errorCount = 0;

        for (HotelServiceModel service : allServices) {
            try {
                String serviceType = service.getServiceType();
                Log.d(TAG, "🔄 Clasificando: " + service.getName() + " (Tipo: " + serviceType + ")");

                if (serviceType == null || serviceType.trim().isEmpty()) {
                    Log.w(TAG, "⚠️ Servicio sin tipo: " + service.getName() + ", asignando como INCLUIDO");
                    serviceType = "included";
                }

                // ✅ CORRECCIÓN: Usar lógica simple igual a HotelProfileFragment
                if ("basic".equals(serviceType)) {
                    basicServices.add(service);
                    basicCount++;
                    Log.d(TAG, "➕ BÁSICO agregado: " + service.getName() + " (Total básicos: " + basicCount + ")");
                } else if ("included".equals(serviceType)) {
                    includedServices.add(service);
                    includedCount++;
                    Log.d(TAG, "➕ INCLUIDO agregado: " + service.getName() + " (Total incluidos: " + includedCount + ")");
                } else if ("paid".equals(serviceType)) {
                    paidServices.add(service);
                    paidCount++;
                    Log.d(TAG, "➕ PAGADO agregado: " + service.getName() + " (Total pagados: " + paidCount + ")");
                } else {
                    // Fallback a incluidos
                    includedServices.add(service);
                    includedCount++;
                    Log.w(TAG, "⚠️ Tipo desconocido para " + service.getName() + ", agregado como INCLUIDO");
                }

            } catch (Exception e) {
                errorCount++;
                Log.e(TAG, "❌ Error procesando servicio " + service.getName() + ": " + e.getMessage());
            }
        }

        Log.d(TAG, "📊 === CLASIFICACIÓN COMPLETADA ===");
        Log.d(TAG, "✅ Servicios Básicos: " + basicCount + " (" + basicServices.size() + " en lista)");
        Log.d(TAG, "✅ Servicios Incluidos: " + includedCount + " (" + includedServices.size() + " en lista)");
        Log.d(TAG, "✅ Servicios Pagados: " + paidCount + " (" + paidServices.size() + " en lista)");
        Log.d(TAG, "❌ Errores: " + errorCount);

        // ✅ ACTUALIZAR UI EN EL HILO PRINCIPAL
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Log.d(TAG, "🔄 Actualizando UI con servicios clasificados...");
                updateAdapters();
                updateEmptyStatesVisibility();

                // ✅ DEBUGGING INMEDIATO después de actualización
                debugRecyclerViewsState();
            });
        } else {
            Log.e(TAG, "❌ Activity es null, no se puede actualizar UI");
        }
    }

    // ✅ SIMPLIFICADO: Actualizar adapters sin conversiones
    private void updateAdapters() {
        Log.d(TAG, "🔄 Iniciando actualización de adapters...");

        if (basicServicesAdapter != null) {
            Log.d(TAG, "🔄 Actualizando adapter básicos con " + basicServices.size() + " servicios");
            basicServicesAdapter.updateServices(basicServices);

            if (rvBasicServices != null) {
                rvBasicServices.post(() -> {
                    basicServicesAdapter.notifyDataSetChanged();
                    rvBasicServices.requestLayout();
                    Log.d(TAG, "📏 RV Básicos - Items visibles: " + rvBasicServices.getChildCount() + "/" + basicServicesAdapter.getItemCount());
                });
            }
        }

        if (includedServicesAdapter != null) {
            Log.d(TAG, "🔄 Actualizando adapter incluidos con " + includedServices.size() + " servicios");
            includedServicesAdapter.updateServices(includedServices);

            if (rvIncludedServices != null) {
                rvIncludedServices.post(() -> {
                    includedServicesAdapter.notifyDataSetChanged();
                    rvIncludedServices.requestLayout();
                    Log.d(TAG, "📏 RV Incluidos - Items visibles: " + rvIncludedServices.getChildCount() + "/" + includedServicesAdapter.getItemCount());
                });
            }
        }

        if (paidServicesAdapter != null) {
            Log.d(TAG, "🔄 Actualizando adapter pagados con " + paidServices.size() + " servicios");
            paidServicesAdapter.updateServices(paidServices);

            if (rvPaidServices != null) {
                rvPaidServices.post(() -> {
                    paidServicesAdapter.notifyDataSetChanged();
                    rvPaidServices.requestLayout();
                    Log.d(TAG, "📏 RV Pagados - Items visibles: " + rvPaidServices.getChildCount() + "/" + paidServicesAdapter.getItemCount());
                });
            }
        }

        Log.d(TAG, "✅ Todos los adapters actualizados");
    }

    // ✅ SIMPLIFICADO: Estados vacíos para 3 tipos
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

        // Pagados
        if (rvPaidServices != null && layoutPaidServicesEmpty != null) {
            rvPaidServices.setVisibility(paidServices.isEmpty() ? View.GONE : View.VISIBLE);
            layoutPaidServicesEmpty.setVisibility(paidServices.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    // ========== MÉTODOS DE ACCIÓN ==========

    private void showAddServiceDialog() {
        Log.d(TAG, "➕ Mostrando diálogo para agregar servicio");

        AddServiceDialog dialog = new AddServiceDialog(getContext(), new AddServiceDialog.OnServiceAddedListener() {
            @Override
            public void onServiceAdded(HotelServiceModel service) {
                Log.d(TAG, "✅ Servicio agregado exitosamente: " + service.getName());
                // No necesitamos hacer nada aquí, el listener automático se encarga
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error agregando servicio: " + error);
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        }, photoPickerLauncher);

        currentAddServiceDialog = dialog;
        dialog.show();
    }

    private void showEditServiceDialog(HotelServiceModel service, int position) {
        Log.d(TAG, "✏️ Mostrando diálogo para editar servicio: " + service.getName());

        EditServiceDialog dialog = new EditServiceDialog(getContext(), service, new EditServiceDialog.OnServiceEditedListener() {
            @Override
            public void onServiceEdited(HotelServiceModel editedService) {
                Log.d(TAG, "✅ Servicio editado exitosamente: " + editedService.getName());
                // No necesitamos hacer nada aquí, el listener automático se encarga
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error editando servicio: " + error);
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        }, photoPickerLauncher);

        dialog.show();
    }

    private void showDeleteServiceDialog(HotelServiceModel service, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("⚠️ Confirmar eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar el servicio '" + service.getName() + "'?\n\nEsta acción no se puede deshacer.")
                .setPositiveButton("🗑️ Eliminar", (dialog, which) -> {
                    deleteService(service, position);
                })
                .setNegativeButton("❌ Cancelar", null)
                .show();
    }

    private void deleteService(HotelServiceModel service, int position) {
        Log.d(TAG, "🗑️ Eliminando servicio: " + service.getName());

        if (firebaseServiceManager != null) {
            firebaseServiceManager.deleteService(service.getId(), new FirebaseServiceManager.ServiceCallback() {
                @Override
                public void onSuccess(HotelServiceModel deletedService) {
                    // ✅ CORRECCIÓN: Manejar null correctamente
                    String serviceName = (deletedService != null && deletedService.getName() != null)
                            ? deletedService.getName()
                            : service.getName(); // Usar el nombre del servicio original

                    Log.d(TAG, "✅ Servicio eliminado exitosamente: " + serviceName);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "🗑️ Servicio eliminado: " + serviceName, Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error eliminando servicio: " + error);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        }
    }

    private void toggleServiceState(HotelServiceModel service, int position, boolean isActive) {
        Log.d(TAG, "🔄 Cambiando estado del servicio: " + service.getName() + " -> " + (isActive ? "Activo" : "Inactivo"));

        service.setActive(isActive);

        if (firebaseServiceManager != null) {
            firebaseServiceManager.updateService(service, null, new FirebaseServiceManager.ServiceCallback() {
                @Override
                public void onSuccess(HotelServiceModel updatedService) {
                    Log.d(TAG, "✅ Estado del servicio actualizado: " + updatedService.getName());
                    // No necesitamos hacer nada aquí, el listener automático se encarga
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error actualizando estado del servicio: " + error);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        }

        String status = isActive ? "activado" : "desactivado";
        Toast.makeText(getContext(), "🔄 Servicio " + status + ": " + service.getName(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "🔄 Estado del servicio cambiado: " + service.getName() + " -> " + status);
    }

    private void showServicePhotos(String photoUrl, int position, List<String> allPhotos) {
        Log.d(TAG, "📷 Mostrando foto " + (position + 1) + " de " + allPhotos.size());

        ServicePhotoViewerDialog dialog = new ServicePhotoViewerDialog(
                getContext(),
                allPhotos,
                position,
                "Fotos del servicio"
        );
        dialog.show();
    }

    // ========== GESTIÓN DE TAXI ==========

    private void loadTaxiConfigurationFromFirebase() {
        Log.d(TAG, "🔄 Cargando configuración de taxi desde Firebase...");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "⚠️ Usuario no autenticado - no se puede cargar configuración de taxi");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("taxi_config")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double minAmount = documentSnapshot.getDouble("minAmount");
                        if (minAmount != null && etTaxiMinAmount != null) {
                            etTaxiMinAmount.setText(String.valueOf(minAmount.intValue()));
                            Log.d(TAG, "✅ Configuración de taxi cargada: monto mínimo = " + minAmount);
                        }
                    } else {
                        Log.d(TAG, "ℹ️ No existe configuración de taxi previa");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error cargando configuración de taxi: " + e.getMessage());
                });
    }

    private void saveTaxiConfiguration() {
        Log.d(TAG, "💾 Guardando configuración de taxi...");

        if (etTaxiMinAmount == null) {
            Log.e(TAG, "❌ Campo de monto mínimo no encontrado");
            return;
        }

        String amountText = etTaxiMinAmount.getText().toString().trim();
        if (amountText.isEmpty()) {
            etTaxiMinAmount.setError("Campo requerido");
            etTaxiMinAmount.requestFocus();
            return;
        }

        double minAmount;
        try {
            minAmount = Double.parseDouble(amountText);
            if (minAmount <= 0) {
                etTaxiMinAmount.setError("El monto debe ser mayor a 0");
                etTaxiMinAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etTaxiMinAmount.setError("Monto inválido");
            etTaxiMinAmount.requestFocus();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "❌ Usuario no autenticado");
            Toast.makeText(getContext(), "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón mientras se guarda
        if (btnSaveTaxiConfig != null) {
            btnSaveTaxiConfig.setEnabled(false);
            btnSaveTaxiConfig.setText("💾 Guardando...");
        }

        // Guardar en Firebase
        FirebaseFirestore.getInstance()
                .collection("taxi_config")
                .document(currentUser.getUid())
                .set(new java.util.HashMap<String, Object>() {{
                    put("minAmount", minAmount);
                    put("hotelAdminId", currentUser.getUid());
                    put("updatedAt", new java.util.Date());
                }})
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Configuración de taxi guardada exitosamente");

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // Mostrar diálogo de éxito
                            TaxiConfigSuccessDialog successDialog = new TaxiConfigSuccessDialog(getContext());
                            successDialog.show();

                            // Restaurar botón
                            if (btnSaveTaxiConfig != null) {
                                btnSaveTaxiConfig.setEnabled(true);
                                btnSaveTaxiConfig.setText("💾 Guardar Configuración");
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error guardando configuración de taxi: " + e.getMessage());

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error guardando configuración: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                            // Restaurar botón
                            if (btnSaveTaxiConfig != null) {
                                btnSaveTaxiConfig.setEnabled(true);
                                btnSaveTaxiConfig.setText("💾 Guardar Configuración");
                            }
                        });
                    }
                });
    }

    // ========== IMPLEMENTACIÓN DE OnServicesChangedListener ==========

    @Override
    public void onBasicServicesUpdated(List<HotelServiceModel> basicServices) {
        Log.d(TAG, "🔄 onBasicServicesUpdated llamado con " + basicServices.size() + " servicios");
        // No necesitamos hacer nada aquí, el onAllServicesUpdated se encarga
    }

    @Override
    public void onAllServicesUpdated(List<HotelServiceModel> allServices) {
        Log.d(TAG, "🔄 onAllServicesUpdated llamado con " + allServices.size() + " servicios");
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

    // ========== MÉTODOS DE DEBUGGING ==========

    private void debugBasicServicesSpecifically() {
        Log.d(TAG, "🔍 === DEBUGGING ESPECÍFICO DE SERVICIOS BÁSICOS ===");

        if (firebaseServiceManager == null) {
            Log.e(TAG, "❌ FirebaseServiceManager es NULL");
            return;
        }

        Log.d(TAG, "📋 Lista básicos - Size: " + basicServices.size());
        for (int i = 0; i < basicServices.size(); i++) {
            HotelServiceModel service = basicServices.get(i);
            Log.d(TAG, "📝 Servicio " + i + ": " + service.getName() +
                    " (Tipo: " + service.getServiceType() +
                    ", ID: " + service.getId() +
                    ", Activo: " + service.isActive() + ")");
        }

        // Verificar RecyclerView
        if (rvBasicServices == null) {
            Log.e(TAG, "❌ rvBasicServices es NULL");
            return;
        }

        Log.d(TAG, "📏 RV Básicos - Visibilidad: " + getVisibilityString(rvBasicServices.getVisibility()));

        // Verificar Adapter
        if (basicServicesAdapter == null) {
            Log.e(TAG, "❌ basicServicesAdapter es NULL");
            return;
        }

        Log.d(TAG, "🎯 Adapter Básicos:");
        Log.d(TAG, "   - Item Count: " + basicServicesAdapter.getItemCount());
        Log.d(TAG, "   - Context: " + (basicServicesAdapter.context != null ? "OK" : "NULL"));

        // Verificar Estado Empty
        View emptyState = getView() != null ? getView().findViewById(R.id.layoutBasicServicesEmpty) : null;
        if (emptyState != null) {
            Log.d(TAG, "📄 Empty State Básicos: " + getVisibilityString(emptyState.getVisibility()));
        } else {
            Log.w(TAG, "⚠️ Empty State Básicos no encontrado");
        }

        Log.d(TAG, "🔍 === FIN DEBUG ESPECÍFICO BÁSICOS ===");
    }

    private void forceBasicServicesRefresh() {
        Log.d(TAG, "🔄 FORZANDO REFRESH DE SERVICIOS BÁSICOS...");

        if (firebaseServiceManager != null) {
            firebaseServiceManager.getServicesByType("basic", new FirebaseServiceManager.ServicesListCallback() {
                @Override
                public void onSuccess(List<HotelServiceModel> services) {
                    Log.d(TAG, "✅ Servicios básicos obtenidos directamente: " + services.size());

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            basicServices.clear();
                            basicServices.addAll(services);

                            if (basicServicesAdapter != null) {
                                basicServicesAdapter.updateServices(basicServices);
                                basicServicesAdapter.notifyDataSetChanged();
                            }

                            updateEmptyStatesVisibility();
                            Log.d(TAG, "✅ Refresh forzado completado");
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error obteniendo servicios básicos directamente: " + error);
                }
            });
        }
    }

    private void debugRecyclerViewsState() {
        Log.d(TAG, "🔍 === DEBUGGING ESTADO DE RECYCLERVIEWS ===");

        if (rvBasicServices != null) {
            Log.d(TAG, "📏 RV Básicos - Visibilidad: " + getVisibilityString(rvBasicServices.getVisibility()) +
                    ", Items: " + rvBasicServices.getChildCount() +
                    ", Adapter: " + (rvBasicServices.getAdapter() != null ? "OK" : "NULL"));
        }

        if (rvIncludedServices != null) {
            Log.d(TAG, "📏 RV Incluidos - Visibilidad: " + getVisibilityString(rvIncludedServices.getVisibility()) +
                    ", Items: " + rvIncludedServices.getChildCount() +
                    ", Adapter: " + (rvIncludedServices.getAdapter() != null ? "OK" : "NULL"));
        }

        if (rvPaidServices != null) {
            Log.d(TAG, "📏 RV Pagados - Visibilidad: " + getVisibilityString(rvPaidServices.getVisibility()) +
                    ", Items: " + rvPaidServices.getChildCount() +
                    ", Adapter: " + (rvPaidServices.getAdapter() != null ? "OK" : "NULL"));
        }

        Log.d(TAG, "🔍 === FIN DEBUG RECYCLERVIEWS ===");
    }

    private String getVisibilityString(int visibility) {
        switch (visibility) {
            case View.VISIBLE: return "VISIBLE";
            case View.GONE: return "GONE";
            case View.INVISIBLE: return "INVISIBLE";
            default: return "UNKNOWN (" + visibility + ")";
        }
    }
}