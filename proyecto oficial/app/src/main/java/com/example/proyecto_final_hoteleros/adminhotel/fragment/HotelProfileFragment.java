package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.HotelPhotosAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.BasicServicesAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.ServicePhotoViewerDialog;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceModel;
import com.example.proyecto_final_hoteleros.adminhotel.model.RoomType;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelProfile;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseServiceManager;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseRoomManager;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseHotelManager;
import com.example.proyecto_final_hoteleros.utils.AwsFileManager;
import com.example.proyecto_final_hoteleros.utils.UniqueIdGenerator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HotelProfileFragment extends Fragment implements
        FirebaseServiceManager.OnServicesChangedListener,
        FirebaseRoomManager.OnRoomsChangedListener,
        FirebaseHotelManager.OnHotelChangedListener {

    private static final String TAG = "HotelProfileFragment";

    // ========== CRITERIOS DE ACTIVACIÓN ==========
    private static final int MIN_PHOTOS_REQUIRED = 3;
    private static final int MIN_BASIC_SERVICES_REQUIRED = 4;
    private static final int MIN_ROOM_TYPES_REQUIRED = 3;

    // Views del formulario
    private TextInputEditText etHotelName;
    private TextInputEditText etHotelAddress;

    // Views de fotos del hotel
    private RecyclerView rvHotelPhotos;
    private LinearLayout emptyPhotosState;
    private MaterialButton btnAddPhoto;
    private TextView tvPhotosStatus;

    // Views de servicios básicos
    private RecyclerView rvBasicServices;
    private LinearLayout emptyServicesState;
    private MaterialButton btnManageServices;

    // ✅ NUEVAS Views para criterios de activación
    private ImageView ivCriteriaInfo, ivCriteriaServices, ivCriteriaRooms;
    private TextView tvCriteriaInfoStatus, tvCriteriaServicesStatus, tvCriteriaRoomsStatus;
    private ImageView ivHotelStatus;
    private TextView tvHotelStatusTitle, tvHotelStatusDescription;
    private MaterialButton btnSaveProfile, btnActivateHotel, btnManageRooms;

    // Datos
    private List<Uri> hotelPhotos;
    private List<HotelServiceModel> basicServices;
    private List<RoomType> roomTypes; // ✅ NUEVO: Lista de tipos de habitaciones

    // Adapters
    private HotelPhotosAdapter photosAdapter;
    private BasicServicesAdapter servicesAdapter;

    // Managers
    private FirebaseServiceManager firebaseServiceManager;
    private FirebaseRoomManager firebaseRoomManager; // ✅ NUEVO
    private FirebaseHotelManager firebaseHotelManager; // ✅ NUEVO
    private AwsFileManager awsFileManager;
    private UniqueIdGenerator idGenerator;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> photoPickerLauncher;

    // ✅ NUEVO: Variables de estado para criterios
    private boolean isBasicInfoComplete = false;
    private boolean hasEnoughServices = false;
    private boolean hasEnoughRoomTypes = false;
    private boolean isHotelActive = false;

    // ✅ NUEVO: Hotel profile actual
    private HotelProfile currentHotel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar managers
        firebaseServiceManager = FirebaseServiceManager.getInstance(getContext());
        firebaseRoomManager = FirebaseRoomManager.getInstance(getContext()); // ✅ NUEVO
        firebaseHotelManager = FirebaseHotelManager.getInstance(getContext()); // ✅ NUEVO
        awsFileManager = new AwsFileManager(getContext());
        idGenerator = UniqueIdGenerator.getInstance(getContext());

        // Configurar launchers
        setupActivityResultLaunchers();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_hotel_fragment_profile_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        initializeLists();
        setupRecyclerViews();
        setupListeners();
        setupTextWatchers(); // ✅ NUEVO
        loadHotelProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 onResume - Registrando listeners");

        // Registrar listeners para cambios en tiempo real
        if (firebaseServiceManager != null) {
            firebaseServiceManager.addListener(this);
        }
        if (firebaseRoomManager != null) {
            firebaseRoomManager.addListener(this);
        }
        if (firebaseHotelManager != null) {
            firebaseHotelManager.addListener(this);
        }

        // ✅ NUEVO: Validar criterios al reanudar
        validateAllCriteria();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "⏸️ onPause - Desregistrando listeners");

        // Desregistrar listeners para evitar memory leaks
        if (firebaseServiceManager != null) {
            firebaseServiceManager.removeListener(this);
        }
        if (firebaseRoomManager != null) {
            firebaseRoomManager.removeListener(this);
        }
        if (firebaseHotelManager != null) {
            firebaseHotelManager.removeListener(this);
        }
    }

    // ========== CONFIGURACIÓN INICIAL ==========

    private void setupActivityResultLaunchers() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();

                        if (data.getClipData() != null) {
                            // Múltiples fotos seleccionadas
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count && hotelPhotos.size() < 10; i++) {
                                Uri photoUri = data.getClipData().getItemAt(i).getUri();
                                addHotelPhoto(photoUri);
                            }
                        } else if (data.getData() != null) {
                            // Una sola foto seleccionada
                            addHotelPhoto(data.getData());
                        }
                    }
                }
        );
    }

    private void initializeViews(View view) {
        // Formulario
        etHotelName = view.findViewById(R.id.etHotelName);
        etHotelAddress = view.findViewById(R.id.etHotelAddress);

        // Fotos del hotel
        rvHotelPhotos = view.findViewById(R.id.rvHotelPhotos);
        emptyPhotosState = view.findViewById(R.id.emptyPhotosState);
        btnAddPhoto = view.findViewById(R.id.btnAddPhoto);
        tvPhotosStatus = view.findViewById(R.id.tvPhotosStatus);

        // Servicios básicos
        rvBasicServices = view.findViewById(R.id.rvBasicServices);
        emptyServicesState = view.findViewById(R.id.emptyServicesState);
        btnManageServices = view.findViewById(R.id.btnManageServices);

        // ✅ NUEVAS: Views de criterios de activación
        ivCriteriaInfo = view.findViewById(R.id.ivCriteriaInfo);
        ivCriteriaServices = view.findViewById(R.id.ivCriteriaServices);
        ivCriteriaRooms = view.findViewById(R.id.ivCriteriaRooms);
        tvCriteriaInfoStatus = view.findViewById(R.id.tvCriteriaInfoStatus);
        tvCriteriaServicesStatus = view.findViewById(R.id.tvCriteriaServicesStatus);
        tvCriteriaRoomsStatus = view.findViewById(R.id.tvCriteriaRoomsStatus);
        ivHotelStatus = view.findViewById(R.id.ivHotelStatus);
        tvHotelStatusTitle = view.findViewById(R.id.tvHotelStatusTitle);
        tvHotelStatusDescription = view.findViewById(R.id.tvHotelStatusDescription);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnActivateHotel = view.findViewById(R.id.btnActivateHotel);
        btnManageRooms = view.findViewById(R.id.btnManageRooms);
    }

    private void initializeLists() {
        hotelPhotos = new ArrayList<>();
        basicServices = new ArrayList<>();
        roomTypes = new ArrayList<>(); // ✅ NUEVO
    }

    private void setupRecyclerViews() {
        // Adapter de fotos del hotel
        photosAdapter = new HotelPhotosAdapter(hotelPhotos, this::removePhoto);
        rvHotelPhotos.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvHotelPhotos.setAdapter(photosAdapter);

        // Adapter de servicios básicos
        servicesAdapter = new BasicServicesAdapter(getContext(), basicServices, this::onServicePhotoClick);
        rvBasicServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBasicServices.setAdapter(servicesAdapter);
        rvBasicServices.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {
        // Agregar fotos del hotel
        if (btnAddPhoto != null) {
            btnAddPhoto.setOnClickListener(v -> openPhotoSelector());
        }

        // Gestión de servicios
        if (btnManageServices != null) {
            btnManageServices.setOnClickListener(v -> navigateToServiceManagement());
        }

        // ✅ NUEVO: Gestión de habitaciones
        if (btnManageRooms != null) {
            btnManageRooms.setOnClickListener(v -> navigateToRoomManagement());
        }

        // Guardar perfil
        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> saveHotelProfile());
        }

        // ✅ NUEVO: Activar hotel
        if (btnActivateHotel != null) {
            btnActivateHotel.setOnClickListener(v -> activateHotel());
        }
    }

    // ✅ NUEVO: TextWatchers para validación en tiempo real
    private void setupTextWatchers() {
        TextWatcher basicInfoWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateBasicInfoCriteria();
            }
        };

        if (etHotelName != null) {
            etHotelName.addTextChangedListener(basicInfoWatcher);
        }
        if (etHotelAddress != null) {
            etHotelAddress.addTextChangedListener(basicInfoWatcher);
        }
    }

    // ========== GESTIÓN DE FOTOS ==========

    private void openPhotoSelector() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        photoPickerLauncher.launch(intent);
    }

    private void addHotelPhoto(Uri photoUri) {
        if (hotelPhotos.size() >= 10) {
            Toast.makeText(getContext(), "Máximo 10 fotos permitidas", Toast.LENGTH_SHORT).show();
            return;
        }

        hotelPhotos.add(photoUri);
        photosAdapter.notifyDataSetChanged();
        updatePhotosStatus();
        updatePhotosVisibility();

        // ✅ NUEVO: Validar criterios después de agregar foto
        validateBasicInfoCriteria();

        Log.d(TAG, "📷 Foto agregada. Total: " + hotelPhotos.size());
    }

    private void removePhoto(int position) {
        if (position >= 0 && position < hotelPhotos.size()) {
            hotelPhotos.remove(position);
            photosAdapter.notifyItemRemoved(position);
            updatePhotosStatus();
            updatePhotosVisibility();

            // ✅ NUEVO: Validar criterios después de eliminar foto
            validateBasicInfoCriteria();

            Log.d(TAG, "🗑️ Foto eliminada. Total: " + hotelPhotos.size());
        }
    }

    private void updatePhotosStatus() {
        if (tvPhotosStatus != null) {
            int localPhotoCount = hotelPhotos.size();
            int totalPhotoCount = localPhotoCount;

            // ✅ NUEVO: Considerar fotos del hotel desde Firebase
            if (currentHotel != null && currentHotel.hasPhotos()) {
                totalPhotoCount = Math.max(localPhotoCount, currentHotel.getPhotoCount());
            }

            String status = totalPhotoCount + " fotos agregadas (mínimo " + MIN_PHOTOS_REQUIRED + " requeridas)";
            tvPhotosStatus.setText(status);
        }
    }

    private void updatePhotosVisibility() {
        boolean hasLocalPhotos = !hotelPhotos.isEmpty();
        boolean hasFirebasePhotos = currentHotel != null && currentHotel.hasPhotos();
        boolean hasPhotos = hasLocalPhotos || hasFirebasePhotos;

        if (rvHotelPhotos != null) {
            rvHotelPhotos.setVisibility(hasPhotos ? View.VISIBLE : View.GONE);
        }

        if (emptyPhotosState != null) {
            emptyPhotosState.setVisibility(hasPhotos ? View.GONE : View.VISIBLE);
        }
    }

    // ========== GESTIÓN DE SERVICIOS ==========

    private void loadHotelProfile() {
        Log.d(TAG, "🔄 Cargando perfil del hotel...");
        // Los listeners de Firebase se registran en onResume() y cargarán automáticamente los datos
        validateAllCriteria();
    }

    private void updateServicesVisibility() {
        boolean hasServices = !basicServices.isEmpty();

        if (rvBasicServices != null) {
            rvBasicServices.setVisibility(hasServices ? View.VISIBLE : View.GONE);
        }

        if (emptyServicesState != null) {
            emptyServicesState.setVisibility(hasServices ? View.GONE : View.VISIBLE);
        }
    }

    private void onServicePhotoClick(String photoUrl, int position, List<String> allPhotos) {
        // Encontrar el servicio al que pertenece esta foto
        String serviceName = "Servicio";
        for (HotelServiceModel service : basicServices) {
            if (service.getPhotoUrls() != null && service.getPhotoUrls().contains(photoUrl)) {
                serviceName = service.getName();
                break;
            }
        }

        // Mostrar diálogo de fotos
        ServicePhotoViewerDialog dialog = new ServicePhotoViewerDialog(
                getContext(),
                allPhotos,
                position,
                serviceName
        );
        dialog.show();
    }

    // ========== VALIDACIÓN DE CRITERIOS ==========

    private void validateAllCriteria() {
        validateBasicInfoCriteria();
        validateServicesCriteria();
        validateRoomsCriteria();
        updateHotelStatus();
    }

    private void validateBasicInfoCriteria() {
        String hotelName = etHotelName != null ? etHotelName.getText().toString().trim() : "";
        String hotelAddress = etHotelAddress != null ? etHotelAddress.getText().toString().trim() : "";

        // ✅ NUEVO: También considerar fotos del hotel desde Firebase
        int photoCount = hotelPhotos.size(); // Fotos locales
        if (currentHotel != null && currentHotel.hasPhotos()) {
            // Si hay hotel en Firebase, usar el mayor número de fotos
            photoCount = Math.max(photoCount, currentHotel.getPhotoCount());
        }

        boolean hasName = !hotelName.isEmpty();
        boolean hasAddress = !hotelAddress.isEmpty();
        boolean hasEnoughPhotos = photoCount >= MIN_PHOTOS_REQUIRED;

        isBasicInfoComplete = hasName && hasAddress && hasEnoughPhotos;

        // Actualizar UI
        if (ivCriteriaInfo != null) {
            ivCriteriaInfo.setImageResource(isBasicInfoComplete ? R.drawable.ic_check : R.drawable.ic_close);
            ivCriteriaInfo.setColorFilter(getResources().getColor(isBasicInfoComplete ? R.color.green : R.color.red));
        }

        if (tvCriteriaInfoStatus != null) {
            String status = String.format("Nombre: %s, Ubicación: %s, Fotos: %d/%d",
                    hasName ? "✓" : "✗",
                    hasAddress ? "✓" : "✗",
                    photoCount, MIN_PHOTOS_REQUIRED);
            tvCriteriaInfoStatus.setText(status);
        }

        Log.d(TAG, "🔍 Criterio información básica: " + (isBasicInfoComplete ? "✅ COMPLETO" : "❌ INCOMPLETO") +
                " (Fotos: " + photoCount + "/" + MIN_PHOTOS_REQUIRED + ")");
        updateHotelStatus();
    }

    private void validateServicesCriteria() {
        int servicesCount = basicServices.size();
        hasEnoughServices = servicesCount >= MIN_BASIC_SERVICES_REQUIRED;

        // Actualizar UI
        if (ivCriteriaServices != null) {
            ivCriteriaServices.setImageResource(hasEnoughServices ? R.drawable.ic_check : R.drawable.ic_close);
            ivCriteriaServices.setColorFilter(getResources().getColor(hasEnoughServices ? R.color.green : R.color.red));
        }

        if (tvCriteriaServicesStatus != null) {
            String status = String.format("Servicios básicos: %d/%d configurados",
                    servicesCount, MIN_BASIC_SERVICES_REQUIRED);
            tvCriteriaServicesStatus.setText(status);
        }

        Log.d(TAG, "🔍 Criterio servicios básicos: " + (hasEnoughServices ? "✅ COMPLETO" : "❌ INCOMPLETO"));
        updateHotelStatus();
    }

    private void validateRoomsCriteria() {
        int roomTypesCount = roomTypes.size();
        hasEnoughRoomTypes = roomTypesCount >= MIN_ROOM_TYPES_REQUIRED;

        // Actualizar UI
        if (ivCriteriaRooms != null) {
            ivCriteriaRooms.setImageResource(hasEnoughRoomTypes ? R.drawable.ic_check : R.drawable.ic_close);
            ivCriteriaRooms.setColorFilter(getResources().getColor(hasEnoughRoomTypes ? R.color.green : R.color.red));
        }

        if (tvCriteriaRoomsStatus != null) {
            String status = String.format("Tipos de habitaciones: %d/%d creados",
                    roomTypesCount, MIN_ROOM_TYPES_REQUIRED);
            tvCriteriaRoomsStatus.setText(status);
        }

        Log.d(TAG, "🔍 Criterio tipos de habitaciones: " + (hasEnoughRoomTypes ? "✅ COMPLETO" : "❌ INCOMPLETO"));
        updateHotelStatus();
    }

    private void updateHotelStatus() {
        boolean allCriteriaMet = isBasicInfoComplete && hasEnoughServices && hasEnoughRoomTypes;

        if (ivHotelStatus != null && tvHotelStatusTitle != null && tvHotelStatusDescription != null) {
            if (isHotelActive) {
                // Hotel ya está activo
                ivHotelStatus.setImageResource(R.drawable.ic_check_circle);
                ivHotelStatus.setColorFilter(getResources().getColor(R.color.green));
                tvHotelStatusTitle.setText("Hotel Activo");
                tvHotelStatusDescription.setText("Tu hotel está publicado y visible para los clientes");
            } else if (allCriteriaMet) {
                // Todos los criterios cumplidos - Listo para activar
                ivHotelStatus.setImageResource(R.drawable.ic_check_circle);
                ivHotelStatus.setColorFilter(getResources().getColor(R.color.green));
                tvHotelStatusTitle.setText("Listo para Activación");
                tvHotelStatusDescription.setText("Todos los criterios cumplidos. Activa tu hotel para hacerlo visible a los clientes");
            } else {
                // Criterios pendientes
                ivHotelStatus.setImageResource(R.drawable.ic_warning);
                ivHotelStatus.setColorFilter(getResources().getColor(R.color.orange));
                tvHotelStatusTitle.setText("Hotel en Configuración");
                tvHotelStatusDescription.setText("Completa todos los criterios para publicar tu hotel");
            }
        }

        // Mostrar/ocultar botón de activación
        if (btnActivateHotel != null) {
            btnActivateHotel.setVisibility(allCriteriaMet && !isHotelActive ? View.VISIBLE : View.GONE);
        }

        Log.d(TAG, "🏨 Estado del hotel: " + (isHotelActive ? "ACTIVO" : allCriteriaMet ? "LISTO PARA ACTIVAR" : "EN CONFIGURACIÓN"));
    }

    // ========== NAVEGACIÓN ==========

    private void navigateToServiceManagement() {
        ServiceManagementFragment fragment = new ServiceManagementFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToRoomManagement() {
        RoomManagementFragment fragment = new RoomManagementFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    // ========== GUARDAR Y ACTIVAR ==========

    private void saveHotelProfile() {
        String hotelName = etHotelName != null ? etHotelName.getText().toString().trim() : "";
        String hotelAddress = etHotelAddress != null ? etHotelAddress.getText().toString().trim() : "";

        if (hotelName.isEmpty()) {
            Toast.makeText(getContext(), "Por favor ingresa el nombre del hotel", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hotelAddress.isEmpty()) {
            Toast.makeText(getContext(), "Por favor ingresa la dirección del hotel", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón mientras se guarda
        if (btnSaveProfile != null) {
            btnSaveProfile.setEnabled(false);
            btnSaveProfile.setText("💾 Guardando...");
        }

        // ✅ USAR FIREBASE REAL
        if (firebaseHotelManager != null) {
            firebaseHotelManager.saveHotelProfile(hotelName, hotelAddress, hotelPhotos, new FirebaseHotelManager.HotelCallback() {
                @Override
                public void onSuccess(HotelProfile hotel) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "✅ Perfil guardado exitosamente", Toast.LENGTH_SHORT).show();

                            // Restaurar botón
                            if (btnSaveProfile != null) {
                                btnSaveProfile.setEnabled(true);
                                btnSaveProfile.setText("💾 Guardar Cambios");
                            }

                            // Actualizar hotel actual
                            currentHotel = hotel;

                            // Validar criterios después de guardar
                            validateBasicInfoCriteria();

                            Log.d(TAG, "💾 Perfil del hotel guardado: " + hotel.getName());
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "❌ Error guardando perfil: " + error, Toast.LENGTH_LONG).show();

                            // Restaurar botón
                            if (btnSaveProfile != null) {
                                btnSaveProfile.setEnabled(true);
                                btnSaveProfile.setText("💾 Guardar Cambios");
                            }

                            Log.e(TAG, "❌ Error guardando perfil: " + error);
                        });
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), "❌ Error: Servicio de hotel no disponible", Toast.LENGTH_SHORT).show();

            // Restaurar botón
            if (btnSaveProfile != null) {
                btnSaveProfile.setEnabled(true);
                btnSaveProfile.setText("💾 Guardar Cambios");
            }
        }
    }

    private void activateHotel() {
        if (!isBasicInfoComplete || !hasEnoughServices || !hasEnoughRoomTypes) {
            Toast.makeText(getContext(), "No se puede activar el hotel. Criterios incompletos", Toast.LENGTH_LONG).show();
            return;
        }

        // Deshabilitar botón mientras se activa
        if (btnActivateHotel != null) {
            btnActivateHotel.setEnabled(false);
            btnActivateHotel.setText("🚀 Activando...");
        }

        // ✅ USAR FIREBASE REAL
        if (firebaseHotelManager != null) {
            firebaseHotelManager.activateHotel(new FirebaseHotelManager.ActivationCallback() {
                @Override
                public void onSuccess(boolean isActive) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            isHotelActive = isActive;

                            Toast.makeText(getContext(), "🚀 ¡Hotel activado exitosamente! Ahora es visible para los clientes", Toast.LENGTH_LONG).show();

                            // Actualizar estado
                            updateHotelStatus();

                            Log.d(TAG, "🚀 Hotel activado para publicación");
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "❌ Error activando hotel: " + error, Toast.LENGTH_LONG).show();

                            // Restaurar botón
                            if (btnActivateHotel != null) {
                                btnActivateHotel.setEnabled(true);
                                btnActivateHotel.setText("🚀 Activar Hotel para Publicación");
                            }

                            Log.e(TAG, "❌ Error activando hotel: " + error);
                        });
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), "❌ Error: Servicio de hotel no disponible", Toast.LENGTH_SHORT).show();

            // Restaurar botón
            if (btnActivateHotel != null) {
                btnActivateHotel.setEnabled(true);
                btnActivateHotel.setText("🚀 Activar Hotel para Publicación");
            }
        }
    }

    // ========== IMPLEMENTACIÓN DE LISTENERS ==========

    // FirebaseServiceManager.OnServicesChangedListener
    @Override
    public void onBasicServicesUpdated(List<HotelServiceModel> basicServiceModels) {
        Log.d(TAG, "🔄 onBasicServicesUpdated llamado con " + basicServiceModels.size() + " servicios");

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                basicServices.clear();
                basicServices.addAll(basicServiceModels);

                if (servicesAdapter != null) {
                    servicesAdapter.updateServices(basicServices);
                }
                updateServicesVisibility();
                validateServicesCriteria(); // ✅ NUEVO

                Log.d(TAG, "✅ Servicios básicos actualizados en UI: " + basicServices.size());
            });
        }
    }

    @Override
    public void onAllServicesUpdated(List<HotelServiceModel> allServices) {
        Log.d(TAG, "🔄 onAllServicesUpdated llamado con " + allServices.size() + " servicios totales");

        List<HotelServiceModel> basicServiceModels = new ArrayList<>();
        for (HotelServiceModel service : allServices) {
            if ("basic".equals(service.getServiceType())) {
                basicServiceModels.add(service);
            }
        }
        onBasicServicesUpdated(basicServiceModels);
    }

    @Override
    public void onServiceAdded(HotelServiceModel service) {
        Log.d(TAG, "➕ Servicio agregado: " + service.getName());
        // onAllServicesUpdated actualizará automáticamente
    }

    @Override
    public void onServiceUpdated(HotelServiceModel service) {
        Log.d(TAG, "🔄 Servicio actualizado: " + service.getName());
        // onAllServicesUpdated actualizará automáticamente
    }

    @Override
    public void onServiceDeleted(String serviceId) {
        Log.d(TAG, "🗑️ Servicio eliminado: " + serviceId);
        // onAllServicesUpdated actualizará automáticamente
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

    // ✅ NUEVO: FirebaseRoomManager.OnRoomsChangedListener
    @Override
    public void onRoomsLoaded(List<RoomType> rooms) {
        Log.d(TAG, "🔄 onRoomsLoaded llamado con " + rooms.size() + " habitaciones");

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                roomTypes.clear();
                roomTypes.addAll(rooms);
                validateRoomsCriteria(); // ✅ Validar criterios

                Log.d(TAG, "✅ Habitaciones cargadas: " + roomTypes.size());
            });
        }
    }

    @Override
    public void onRoomAdded(RoomType room) {
        Log.d(TAG, "➕ Habitación agregada: " + room.getName());
        // onRoomsLoaded actualizará automáticamente
    }

    @Override
    public void onRoomUpdated(RoomType room) {
        Log.d(TAG, "🔄 Habitación actualizada: " + room.getName());
        // onRoomsLoaded actualizará automáticamente
    }

    @Override
    public void onRoomDeleted(String roomId) {
        Log.d(TAG, "🗑️ Habitación eliminada: " + roomId);
        // onRoomsLoaded actualizará automáticamente
    }

    // ✅ NUEVO: FirebaseHotelManager.OnHotelChangedListener
    @Override
    public void onHotelLoaded(HotelProfile hotel) {
        Log.d(TAG, "🔄 onHotelLoaded llamado: " + hotel.toString());

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                currentHotel = hotel;

                // Cargar datos en la UI
                if (etHotelName != null && hotel.getName() != null) {
                    etHotelName.setText(hotel.getName());
                }
                if (etHotelAddress != null && hotel.getAddress() != null) {
                    etHotelAddress.setText(hotel.getAddress());
                }

                // ✅ PENDIENTE: Cargar fotos desde URLs
                // TODO: Implementar carga de fotos desde hotel.getPhotoUrls()

                isHotelActive = hotel.isActive();
                validateBasicInfoCriteria(); // Validar con los nuevos datos

                Log.d(TAG, "✅ Hotel cargado en UI: " + hotel.getName() + " (Activo: " + hotel.isActive() + ")");
            });
        }
    }

    @Override
    public void onHotelActivated(boolean isActive) {
        Log.d(TAG, "🔄 onHotelActivated llamado: " + isActive);

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                isHotelActive = isActive;
                updateHotelStatus();

                Log.d(TAG, "✅ Estado de activación actualizado: " + (isActive ? "ACTIVO" : "INACTIVO"));
            });
        }
    }
}