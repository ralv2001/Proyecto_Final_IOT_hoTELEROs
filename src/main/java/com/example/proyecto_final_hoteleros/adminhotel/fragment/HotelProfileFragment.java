package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.HotelPhotosAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.BasicServicesAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.BasicServiceDialog;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.ServicePhotoViewerDialog;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelProfile;
import com.example.proyecto_final_hoteleros.adminhotel.model.BasicService;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceModel;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseServiceManager;
import com.example.proyecto_final_hoteleros.adminhotel.utils.ServiceSyncManager;
import com.example.proyecto_final_hoteleros.utils.AwsFileManager;
import com.example.proyecto_final_hoteleros.utils.UniqueIdGenerator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HotelProfileFragment extends Fragment implements FirebaseServiceManager.OnServicesChangedListener {

    private static final String TAG = "HotelProfileFragment";

    // Views
    private TextInputEditText etHotelName, etHotelAddress;
    private RecyclerView rvHotelPhotos, rvBasicServices;
    private MaterialButton btnSaveProfile, btnAddPhoto, btnAddBasicService;
    private ImageView ivBack;
    private TextView tvPhotosStatus, tvPhotosCounter;

    // ✅ AGREGADO: Estados vacíos
    private LinearLayout emptyPhotosState, emptyServicesState;

    // Adapters y datos
    private HotelPhotosAdapter photosAdapter;
    private BasicServicesAdapter servicesAdapter;
    private List<Uri> hotelPhotos;
    private List<BasicService> basicServices;

    // Launchers
    private ActivityResultLauncher<Intent> servicePhotoPickerLauncher;
    private ActivityResultLauncher<Intent> photoPickerLauncher;

    // Managers y servicios
    private FirebaseServiceManager firebaseServiceManager;
    private ServiceSyncManager serviceSyncManager;
    private AwsFileManager awsFileManager;
    private UniqueIdGenerator idGenerator;
    private BasicServiceDialog currentBasicServiceDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "🚀 onCreateView iniciado");

        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_profile_management, container, false);

        // Inicializar managers
        firebaseServiceManager = FirebaseServiceManager.getInstance(requireContext());
        serviceSyncManager = ServiceSyncManager.getInstance(requireContext());
        awsFileManager = new AwsFileManager(requireContext());
        idGenerator = UniqueIdGenerator.getInstance(requireContext());

        initViews(rootView);
        initLaunchers();
        setupRecyclerViews();
        setupClickListeners();

        // ✅ SOLUCIÓN: Registrar listener de Firebase ANTES de cargar datos
        Log.d(TAG, "📡 Registrando listener de Firebase");
        firebaseServiceManager.addListener(this);

        // ✅ MEJORADO: Cargar datos básicos inmediatamente
        loadHotelProfile();

        return rootView;
    }
    @Override
    public void onServiceAdded(HotelServiceModel service) {
        Log.d(TAG, "➕ Servicio agregado: " + service.getName());
        if ("basic".equals(service.getServiceType())) {
            loadBasicServicesFromFirebase();
        }
    }

    @Override
    public void onServiceUpdated(HotelServiceModel service) {
        Log.d(TAG, "🔄 Servicio actualizado: " + service.getName());
        if ("basic".equals(service.getServiceType())) {
            loadBasicServicesFromFirebase();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firebaseServiceManager != null) {
            firebaseServiceManager.removeListener(this);
        }
        if (currentBasicServiceDialog != null && currentBasicServiceDialog.isShowing()) {
            currentBasicServiceDialog.dismiss();
        }
    }

    // ========== IMPLEMENTACIÓN DE FIREBASE LISTENER ==========
    private void loadBasicServicesFromFirebase() {
        Log.d(TAG, "📊 Cargando servicios básicos desde Firebase...");

        firebaseServiceManager.getServicesByType("basic", new FirebaseServiceManager.ServicesListCallback() {
            @Override
            public void onSuccess(List<HotelServiceModel> basicServiceModels) {
                Log.d(TAG, "✅ Servicios básicos obtenidos: " + basicServiceModels.size());

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Convertir y actualizar la lista
                        basicServices.clear();
                        for (HotelServiceModel model : basicServiceModels) {
                            BasicService basicService = convertFirebaseToBasicService(model);
                            basicServices.add(basicService);
                        }

                        // Actualizar adapter y estados
                        if (servicesAdapter != null) {
                            servicesAdapter.notifyDataSetChanged();
                        }
                        updateBasicServicesVisibility();
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando servicios básicos: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error cargando servicios: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    public void onBasicServicesUpdated(List<HotelServiceModel> basicServiceModels) {
        Log.d(TAG, "🔄 onBasicServicesUpdated llamado con " + basicServiceModels.size() + " servicios");

        // Imprimir detalles de cada servicio
        for (int i = 0; i < basicServiceModels.size(); i++) {
            HotelServiceModel model = basicServiceModels.get(i);
            Log.d(TAG, "  📋 Servicio " + i + ": " + model.getName() + " (Fotos: " +
                    (model.getPhotoUrls() != null ? model.getPhotoUrls().size() : 0) + ")");
        }

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Log.d(TAG, "🎯 Ejecutando actualización en UI Thread");

                int oldSize = basicServices.size();
                basicServices.clear();
                Log.d(TAG, "🧹 Lista limpiada (antes: " + oldSize + " servicios)");

                for (HotelServiceModel model : basicServiceModels) {
                    BasicService basicService = convertFirebaseToBasicService(model);
                    basicServices.add(basicService);
                    Log.d(TAG, "➕ Agregado: " + basicService.getName() + " con " +
                            basicService.getPhotos().size() + " fotos");
                }

                Log.d(TAG, "📊 Lista actualizada: " + basicServices.size() + " servicios");

                // ✅ MEJORA: Verificar que el adapter y RecyclerView existan antes de actualizar
                if (servicesAdapter != null && rvBasicServices != null) {
                    Log.d(TAG, "🔄 Notificando adapter...");
                    servicesAdapter.notifyDataSetChanged();

                    // ✅ CRÍTICO: Actualizar visibilidad de estados vacíos
                    updateBasicServicesVisibility();

                    // ✅ Forzar layout del RecyclerView
                    rvBasicServices.post(() -> {
                        if (rvBasicServices.getLayoutManager() != null) {
                            rvBasicServices.getLayoutManager().requestLayout();
                        }
                    });

                    Log.d(TAG, "✅ Adapter notificado y RecyclerView actualizado");
                } else {
                    Log.e(TAG, "❌ ERROR: adapter=" + (servicesAdapter != null ? "OK" : "NULL") +
                            ", recyclerView=" + (rvBasicServices != null ? "OK" : "NULL"));
                }
            });
        } else {
            Log.e(TAG, "❌ ERROR: getActivity() es null!");
        }
    }

    @Override
    public void onAllServicesUpdated(List<HotelServiceModel> allServices) {
        // Filtrar solo los servicios básicos
        List<HotelServiceModel> basicServiceModels = new ArrayList<>();
        for (HotelServiceModel service : allServices) {
            if ("basic".equals(service.getServiceType())) {
                basicServiceModels.add(service);
            }
        }
        onBasicServicesUpdated(basicServiceModels);
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "Error desde Firebase: " + error);
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
            // Forzar recarga si la lista está vacía
            if (basicServices.isEmpty()) {
                Log.d(TAG, "📋 Lista vacía, forzando recarga de servicios básicos");
                loadHotelProfile();
            }
        }
    }
    // Agregar este método faltante en HotelProfileFragment
    @Override
    public void onServiceDeleted(String serviceId) {
        Log.d(TAG, "🗑️ Servicio eliminado: " + serviceId);
        loadBasicServicesFromFirebase();
    }

    // ========== INICIALIZACIÓN ==========

    private void initViews(View rootView) {
        etHotelName = rootView.findViewById(R.id.etHotelName);
        etHotelAddress = rootView.findViewById(R.id.etHotelAddress);
        rvHotelPhotos = rootView.findViewById(R.id.rvHotelPhotos);
        rvBasicServices = rootView.findViewById(R.id.rvBasicServices);
        btnSaveProfile = rootView.findViewById(R.id.btnSaveProfile);
        btnAddPhoto = rootView.findViewById(R.id.btnAddPhoto);
        btnAddBasicService = rootView.findViewById(R.id.btnAddBasicService);
        ivBack = rootView.findViewById(R.id.ivBack);
        tvPhotosStatus = rootView.findViewById(R.id.tvPhotosStatus);
        tvPhotosCounter = rootView.findViewById(R.id.tvPhotosCounter);

        // ✅ NUEVO: Inicializar estados vacíos
        emptyPhotosState = rootView.findViewById(R.id.emptyPhotosState);
        emptyServicesState = rootView.findViewById(R.id.emptyServicesState);

        hotelPhotos = new ArrayList<>();
        basicServices = new ArrayList<>();

        // ✅ NUEVO: Configurar visibilidades iniciales
        updatePhotosVisibility();
        updateBasicServicesVisibility();
    }

    private void initLaunchers() {
        // Launcher para fotos del hotel
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            addHotelPhoto(selectedImageUri);
                        }
                    }
                }
        );

        // Launcher separado para fotos de servicios básicos
        servicePhotoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null && currentBasicServiceDialog != null) {
                            currentBasicServiceDialog.addPhoto(selectedImageUri);
                        }
                    }
                }
        );
    }

    private void setupRecyclerViews() {
        Log.d(TAG, "🔧 Configurando RecyclerViews");

        // RecyclerView para fotos del hotel
        photosAdapter = new HotelPhotosAdapter(hotelPhotos, this::removePhoto);
        rvHotelPhotos.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvHotelPhotos.setAdapter(photosAdapter);

        // ✅ RecyclerView para servicios básicos con soporte de fotos
        servicesAdapter = new BasicServicesAdapter(
                getContext(),
                basicServices,
                this::removeBasicService,
                this::onServicePhotoClick  // Nuevo callback para fotos
        );
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvBasicServices.setLayoutManager(layoutManager);
        rvBasicServices.setAdapter(servicesAdapter);
        rvBasicServices.setNestedScrollingEnabled(false);
        rvBasicServices.setHasFixedSize(false);

        Log.d(TAG, "✅ RecyclerView configurado con adapter mejorado para fotos");
    }

    // ✅ NUEVO: Método para manejar clicks en fotos de servicios
    private void onServicePhotoClick(String photoUrl, int position, List<String> allPhotos) {
        Log.d(TAG, "📸 Click en foto del servicio: " + photoUrl + " posición: " + position);

        if (allPhotos != null && !allPhotos.isEmpty()) {
            showServicePhotoViewerDialog(photoUrl, allPhotos, position);
        }
    }

    private void showServicePhotoViewerDialog(String photoUrl, List<String> allPhotos, int position) {
        if (getContext() == null) return;

        try {
            // Encontrar el nombre del servicio para el título
            String serviceName = "Servicio";
            for (BasicService service : basicServices) {
                if (service.getPhotos() != null && service.getPhotos().contains(photoUrl)) {
                    serviceName = service.getName();
                    break;
                }
            }

            ServicePhotoViewerDialog dialog = new ServicePhotoViewerDialog(
                    getContext(),
                    allPhotos,
                    position,
                    serviceName
            );
            dialog.show();

            Log.d(TAG, "📸 Dialog de fotos mostrado para: " + serviceName);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error mostrando dialog de fotos: " + e.getMessage());
            Toast.makeText(getContext(), "Error al mostrar la foto", Toast.LENGTH_SHORT).show();
        }
    }
    private void notifyBasicServicesChanged() {
        if (servicesAdapter != null) {
            servicesAdapter.updateServices(basicServices);
            Log.d(TAG, "📋 Adapter de servicios notificado de cambios");
        }
        updateBasicServicesVisibility();
    }
    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnSaveProfile.setOnClickListener(v -> saveHotelProfile());

        btnAddPhoto.setOnClickListener(v -> {
            if (hotelPhotos.size() < 10) {
                selectPhoto();
            } else {
                Toast.makeText(getContext(), "Máximo 10 fotos permitidas", Toast.LENGTH_SHORT).show();
            }
        });

        btnAddBasicService.setOnClickListener(v -> showAddBasicServiceDialog());
    }

    // ========== GESTIÓN DE FOTOS DEL HOTEL ==========

    private void addHotelPhoto(Uri photoUri) {
        if (hotelPhotos.size() < 10) {
            hotelPhotos.add(photoUri);
            photosAdapter.notifyItemInserted(hotelPhotos.size() - 1);
            updatePhotosStatus();
            // ✅ NUEVO: Actualizar visibilidad
            updatePhotosVisibility();
        }
    }

    private void updatePhotosStatus() {
        int photoCount = hotelPhotos.size();

        tvPhotosCounter.setText(photoCount + " fotos");

        if (photoCount == 0) {
            tvPhotosStatus.setText("Las fotos mejoran la visibilidad de tu hotel");
            btnAddPhoto.setText("Agregar Fotos del Hotel");
        } else if (photoCount < 4) {
            tvPhotosStatus.setText("Se recomiendan mínimo 4 fotos para mejor visibilidad");
            btnAddPhoto.setText("Agregar Más Fotos (" + photoCount + "/10)");
        } else {
            tvPhotosStatus.setText(photoCount + " de 10 fotos subidas - ¡Excelente galería!");
            btnAddPhoto.setText("Agregar Más Fotos (" + photoCount + "/10)");
        }

        btnAddPhoto.setEnabled(photoCount < 10);
    }

    // ✅ NUEVO: Método para manejar visibilidad de fotos
    private void updatePhotosVisibility() {
        Log.d(TAG, "🔄 Actualizando visibilidad de fotos: " + hotelPhotos.size() + " fotos");

        if (hotelPhotos.isEmpty()) {
            // Mostrar estado vacío, ocultar RecyclerView
            if (emptyPhotosState != null) {
                emptyPhotosState.setVisibility(View.VISIBLE);
                Log.d(TAG, "📷 Mostrando estado vacío de fotos");
            }
            if (rvHotelPhotos != null) {
                rvHotelPhotos.setVisibility(View.GONE);
                Log.d(TAG, "📷 Ocultando RecyclerView de fotos");
            }
        } else {
            // Mostrar RecyclerView, ocultar estado vacío
            if (emptyPhotosState != null) {
                emptyPhotosState.setVisibility(View.GONE);
                Log.d(TAG, "📷 Ocultando estado vacío de fotos");
            }
            if (rvHotelPhotos != null) {
                rvHotelPhotos.setVisibility(View.VISIBLE);
                Log.d(TAG, "📷 Mostrando RecyclerView de fotos");
            }
        }
    }

    // ✅ NUEVO: Método para manejar visibilidad de servicios básicos
    private void updateBasicServicesVisibility() {
        Log.d(TAG, "🔄 Actualizando visibilidad de servicios básicos: " + basicServices.size() + " servicios");

        if (basicServices.isEmpty()) {
            // Mostrar estado vacío, ocultar RecyclerView
            if (emptyServicesState != null) {
                emptyServicesState.setVisibility(View.VISIBLE);
                Log.d(TAG, "⚡ Mostrando estado vacío de servicios");
            }
            if (rvBasicServices != null) {
                rvBasicServices.setVisibility(View.GONE);
                Log.d(TAG, "⚡ Ocultando RecyclerView de servicios");
            }
        } else {
            // Mostrar RecyclerView, ocultar estado vacío
            if (emptyServicesState != null) {
                emptyServicesState.setVisibility(View.GONE);
                Log.d(TAG, "⚡ Ocultando estado vacío de servicios");
            }
            if (rvBasicServices != null) {
                rvBasicServices.setVisibility(View.VISIBLE);
                Log.d(TAG, "⚡ Mostrando RecyclerView de servicios");
            }
        }
    }

    private void selectPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        photoPickerLauncher.launch(intent);
    }

    private void removePhoto(int position) {
        if (position >= 0 && position < hotelPhotos.size()) {
            hotelPhotos.remove(position);
            photosAdapter.notifyItemRemoved(position);
            updatePhotosStatus();
            // ✅ NUEVO: Actualizar visibilidad
            updatePhotosVisibility();
        }
    }

    // ========== GESTIÓN DE SERVICIOS BÁSICOS ==========

    private void showAddBasicServiceDialog() {
        currentBasicServiceDialog = new BasicServiceDialog(getContext(), servicePhotoPickerLauncher, service -> {
            Log.d(TAG, "🔧 Servicio básico recibido del diálogo: " + service.getName() +
                    " con " + service.getPhotos().size() + " fotos");

            // ✅ Crear modelo para Firebase - SIN pasar photoUris porque ya están las URLs
            HotelServiceModel firebaseService = convertBasicServiceToFirebase(service);

            // ✅ Las fotos ya están subidas en AWS, solo pasar null para photoUris
            firebaseServiceManager.createService(firebaseService, null, new FirebaseServiceManager.ServiceCallback() {
                @Override
                public void onSuccess(HotelServiceModel createdService) {
                    Log.d(TAG, "✅ Servicio básico creado en Firebase: " + createdService.getId());
                    Toast.makeText(getContext(), "✅ Servicio básico agregado y sincronizado", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error creando servicio básico: " + error);
                    Toast.makeText(getContext(), "Error creando servicio: " + error, Toast.LENGTH_LONG).show();
                }
            });

            currentBasicServiceDialog = null;
        });
        currentBasicServiceDialog.show();

        currentBasicServiceDialog.setOnDismissListener(dialog -> {
            currentBasicServiceDialog = null;
        });
    }

    private void removeBasicService(int position) {
        if (position >= 0 && position < basicServices.size()) {
            BasicService serviceToRemove = basicServices.get(position);

            // Encontrar el servicio en Firebase y eliminarlo
            String serviceId = findFirebaseServiceId(serviceToRemove);
            if (serviceId != null) {
                firebaseServiceManager.deleteService(serviceId, new FirebaseServiceManager.ServiceCallback() {
                    @Override
                    public void onSuccess(HotelServiceModel service) {
                        Toast.makeText(getContext(), "🗑️ Servicio básico eliminado y sincronizado", Toast.LENGTH_SHORT).show();
                        // ✅ NUEVO: La visibilidad se actualizará automáticamente vía onBasicServicesUpdated
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), "Error eliminando servicio: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Error: No se pudo encontrar el servicio para eliminar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ========== GUARDAR PERFIL DEL HOTEL ==========

    private void loadHotelProfile() {
        Log.d(TAG, "📋 Cargando perfil del hotel");

        // Cargar datos básicos del hotel
        etHotelName.setText("Hotel Belmond");
        etHotelAddress.setText("Av. Principal 123, Centro, Lima, Perú");

        // ✅ SOLUCIÓN: Forzar carga de servicios desde Firebase
        Log.d(TAG, "🔄 Forzando carga explícita de servicios básicos");
        firebaseServiceManager.getServicesByType("basic", new FirebaseServiceManager.ServicesListCallback() {
            @Override
            public void onSuccess(List<HotelServiceModel> services) {
                Log.d(TAG, "✅ Servicios básicos obtenidos: " + services.size());

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Actualizar lista de servicios básicos
                        basicServices.clear();
                        for (HotelServiceModel model : services) {
                            BasicService basicService = convertFirebaseToBasicService(model);
                            basicServices.add(basicService);
                            Log.d(TAG, "➕ Servicio básico cargado: " + basicService.getName());
                        }

                        // Forzar actualización del adapter
                        if (servicesAdapter != null) {
                            servicesAdapter.notifyDataSetChanged();
                            Log.d(TAG, "🔄 Adapter de servicios básicos actualizado");
                        }

                        // ✅ NUEVO: Actualizar visibilidad
                        updateBasicServicesVisibility();
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando servicios básicos: " + error);
            }
        });

        updatePhotosStatus();
        // ✅ NUEVO: Actualizar visibilidades iniciales
        updatePhotosVisibility();
        updateBasicServicesVisibility();
    }

    private void saveHotelProfile() {
        String name = etHotelName.getText().toString().trim();
        String address = etHotelAddress.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty()) {
            Toast.makeText(getContext(), "Por favor completa la información básica", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hotelPhotos.size() < 4) {
            Toast.makeText(getContext(), "Se requieren mínimo 4 fotos del hotel para continuar", Toast.LENGTH_LONG).show();
            return;
        }

        if (basicServices.isEmpty()) {
            Toast.makeText(getContext(), "Debe agregar al menos un servicio básico", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón mientras se guarda
        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Guardando...");

        // Subir fotos del hotel a AWS
        uploadHotelPhotos(name, address);
    }

    private void uploadHotelPhotos(String hotelName, String hotelAddress) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showSaveError("Usuario no autenticado");
            return;
        }

        String userId = currentUser.getUid();
        String folder = "hotel_profile/" + userId;

        List<String> uploadedPhotoUrls = new ArrayList<>();
        AtomicInteger uploadedCount = new AtomicInteger(0);
        AtomicInteger totalUploads = new AtomicInteger(hotelPhotos.size());

        for (int i = 0; i < hotelPhotos.size(); i++) {
            Uri photoUri = hotelPhotos.get(i);
            String fileName = idGenerator.generateUniqueFileName("hotel", hotelName + "_" + i + ".jpg");

            awsFileManager.uploadFile(photoUri, userId, folder, new AwsFileManager.UploadCallback() {
                @Override
                public void onSuccess(AwsFileManager.AwsFileInfo fileInfo) {
                    synchronized (uploadedPhotoUrls) {
                        uploadedPhotoUrls.add(fileInfo.fileUrl);
                        int completed = uploadedCount.incrementAndGet();

                        // Actualizar progreso
                        int progress = (completed * 100) / totalUploads.get();
                        btnSaveProfile.setText("Subiendo fotos... " + progress + "%");

                        if (completed == totalUploads.get()) {
                            // Todas las fotos subidas, guardar perfil
                            saveHotelProfileToFirebase(hotelName, hotelAddress, uploadedPhotoUrls);
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error subiendo foto del hotel: " + error);
                    showSaveError("Error subiendo fotos: " + error);
                }

                @Override
                public void onProgress(int percentage) {
                    // Progreso individual
                }
            });
        }
    }

    private void saveHotelProfileToFirebase(String hotelName, String hotelAddress, List<String> photoUrls) {
        // TODO: Implementar guardado del perfil del hotel en Firebase
        btnSaveProfile.setEnabled(true);
        btnSaveProfile.setText("💾 Guardar Cambios");

        Toast.makeText(getContext(), "✅ Perfil del hotel actualizado exitosamente", Toast.LENGTH_LONG).show();
        getParentFragmentManager().popBackStack();
    }

    private void showSaveError(String error) {
        btnSaveProfile.setEnabled(true);
        btnSaveProfile.setText("💾 Guardar Cambios");
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }

    // ========== MÉTODOS DE CONVERSIÓN ==========

    private BasicService convertFirebaseToBasicService(HotelServiceModel model) {
        BasicService basicService = new BasicService(
                model.getName(),
                model.getDescription(),
                model.getIconKey()
        );

        // Convertir URLs a URIs si hay fotos
        if (model.getPhotoUrls() != null && !model.getPhotoUrls().isEmpty()) {
            List<String> photoUrls = new ArrayList<>();
            for (String url : model.getPhotoUrls()) {
                photoUrls.add(url);
            }
            basicService.setPhotos(photoUrls);
        }

        return basicService;
    }

    private HotelServiceModel convertBasicServiceToFirebase(BasicService basicService) {
        Log.d(TAG, "🔄 Convirtiendo BasicService a Firebase: " + basicService.getName() +
                " con " + basicService.getPhotos().size() + " fotos");

        HotelServiceModel firebaseService = new HotelServiceModel(
                basicService.getName(),
                basicService.getDescription(),
                basicService.getIconKey(),
                "basic"
        );

        // ✅ Establecer las URLs de fotos directamente (ya subidas en AWS)
        firebaseService.setPhotoUrls(basicService.getPhotos());

        Log.d(TAG, "✅ URLs de fotos establecidas en Firebase model: " +
                firebaseService.getPhotoUrls().size());

        return firebaseService;
    }

    private String findFirebaseServiceId(BasicService basicService) {
        if (basicService.getFirebaseId() != null) {
            return basicService.getFirebaseId();
        }
        Log.w(TAG, "No se pudo encontrar ID de Firebase para: " + basicService.getName());
        return null;
    }
}