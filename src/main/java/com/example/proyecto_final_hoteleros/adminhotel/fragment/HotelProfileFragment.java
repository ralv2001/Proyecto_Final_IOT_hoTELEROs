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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.HotelPhotosAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.BasicServicesAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.ServicePhotoViewerDialog;
import com.example.proyecto_final_hoteleros.adminhotel.model.BasicService;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceModel;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseServiceManager;
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
    private MaterialButton btnSaveProfile, btnAddPhoto, btnManageServices;
    private ImageView ivBack;
    private TextView tvPhotosStatus, tvPhotosCounter;
    private LinearLayout emptyPhotosState, emptyServicesState;

    // Data
    private List<Uri> hotelPhotos;
    private List<BasicService> basicServices;

    // Adapters
    private HotelPhotosAdapter photosAdapter;
    private BasicServicesAdapter servicesAdapter;

    // Firebase
    private FirebaseServiceManager firebaseServiceManager;

    // Activity result launchers
    private ActivityResultLauncher<Intent> photoPickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Firebase manager
        firebaseServiceManager = FirebaseServiceManager.getInstance(getContext());

        setupActivityResultLaunchers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_hotel_fragment_profile_management, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupAdapters();
        setupClickListeners();
        loadHotelProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Registrar listener cuando el fragmento está visible
        if (firebaseServiceManager != null) {
            firebaseServiceManager.addListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Remover listener cuando el fragmento no está visible
        if (firebaseServiceManager != null) {
            firebaseServiceManager.removeListener(this);
        }
    }

    private void setupActivityResultLaunchers() {
        // ✅ Launcher para seleccionar fotos del hotel
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        if (data.getClipData() != null) {
                            // Múltiples fotos seleccionadas
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count && hotelPhotos.size() < 10; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                addHotelPhoto(imageUri);
                            }
                        } else if (data.getData() != null) {
                            // Una sola foto seleccionada
                            Uri selectedImageUri = data.getData();
                            if (selectedImageUri != null) {
                                addHotelPhoto(selectedImageUri);
                            }
                        }
                    }
                }
        );
    }

    private void initViews(View rootView) {
        etHotelName = rootView.findViewById(R.id.etHotelName);
        etHotelAddress = rootView.findViewById(R.id.etHotelAddress);
        rvHotelPhotos = rootView.findViewById(R.id.rvHotelPhotos);
        rvBasicServices = rootView.findViewById(R.id.rvBasicServices);
        btnSaveProfile = rootView.findViewById(R.id.btnSaveProfile);
        btnAddPhoto = rootView.findViewById(R.id.btnAddPhoto);
        btnManageServices = rootView.findViewById(R.id.btnManageServices);
        ivBack = rootView.findViewById(R.id.ivBack);
        tvPhotosStatus = rootView.findViewById(R.id.tvPhotosStatus);
        tvPhotosCounter = rootView.findViewById(R.id.tvPhotosCounter);

        // Inicializar estados vacíos
        emptyPhotosState = rootView.findViewById(R.id.emptyPhotosState);
        emptyServicesState = rootView.findViewById(R.id.emptyServicesState);

        // Inicializar listas
        hotelPhotos = new ArrayList<>();
        basicServices = new ArrayList<>();
    }

    private void setupAdapters() {
        // ✅ CORREGIDO: Adapter para fotos del hotel
        photosAdapter = new HotelPhotosAdapter(hotelPhotos, this::removePhoto);
        rvHotelPhotos.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvHotelPhotos.setAdapter(photosAdapter);

        // ✅ CORREGIDO: Adapter para servicios básicos con listener de fotos
        servicesAdapter = new BasicServicesAdapter(getContext(), basicServices, this::onServicePhotoClick);
        rvBasicServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBasicServices.setAdapter(servicesAdapter);
    }

    private void setupClickListeners() {
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> saveHotelProfile());
        }

        if (btnAddPhoto != null) {
            btnAddPhoto.setOnClickListener(v -> {
                if (hotelPhotos.size() < 10) {
                    selectPhoto();
                } else {
                    Toast.makeText(getContext(), "Máximo 10 fotos permitidas", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Navegar a gestión de servicios
        if (btnManageServices != null) {
            btnManageServices.setOnClickListener(v -> navigateToServiceManagement());
        }
    }

    // Método para navegar a gestión de servicios
    private void navigateToServiceManagement() {
        try {
            ServiceManagementFragment fragment = new ServiceManagementFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();

            Log.d(TAG, "✅ Navegando a gestión de servicios");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error navegando a gestión de servicios: " + e.getMessage());
            Toast.makeText(getContext(), "Error al abrir gestión de servicios", Toast.LENGTH_SHORT).show();
        }
    }

    // ========== GESTIÓN DE FOTOS DEL HOTEL ==========

    private void selectPhoto() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        photoPickerLauncher.launch(Intent.createChooser(intent, "Seleccionar fotos"));
    }

    private void addHotelPhoto(Uri photoUri) {
        if (hotelPhotos.size() < 10) {
            hotelPhotos.add(photoUri);
            photosAdapter.notifyItemInserted(hotelPhotos.size() - 1);
            updatePhotosStatus();
            updatePhotosVisibility();
            Log.d(TAG, "📷 Foto del hotel agregada. Total: " + hotelPhotos.size());
        }
    }

    private void removePhoto(int position) {
        if (position >= 0 && position < hotelPhotos.size()) {
            hotelPhotos.remove(position);
            photosAdapter.notifyItemRemoved(position);
            updatePhotosStatus();
            updatePhotosVisibility();
            Toast.makeText(getContext(), "📷 Foto eliminada", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "📷 Foto del hotel eliminada. Total restante: " + hotelPhotos.size());
        }
    }

    private void updatePhotosStatus() {
        int photoCount = hotelPhotos.size();

        if (tvPhotosCounter != null) {
            tvPhotosCounter.setText(photoCount + " fotos");
        }

        if (tvPhotosStatus != null) {
            if (photoCount == 0) {
                tvPhotosStatus.setText("Las fotos mejoran la visibilidad de tu hotel");
                if (btnAddPhoto != null) {
                    btnAddPhoto.setText("Agregar Fotos del Hotel");
                }
            } else if (photoCount < 4) {
                tvPhotosStatus.setText("Se recomiendan mínimo 4 fotos para mejor visibilidad");
                if (btnAddPhoto != null) {
                    btnAddPhoto.setText("Agregar Más Fotos (" + photoCount + "/10)");
                }
            } else {
                tvPhotosStatus.setText(photoCount + " de 10 fotos subidas - ¡Excelente galería!");
                if (btnAddPhoto != null) {
                    btnAddPhoto.setText("Agregar Más Fotos (" + photoCount + "/10)");
                }
            }
        }
    }

    private void updatePhotosVisibility() {
        boolean hasPhotos = !hotelPhotos.isEmpty();

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

        // Cargar servicios básicos desde Firebase
        if (firebaseServiceManager != null) {
            // El listener ya está registrado en onResume()
        }
    }

    // ✅ MÉTODO MEJORADO PARA CONVERTIR HotelServiceModel A BasicService
    private BasicService convertHotelServiceModelToBasicService(HotelServiceModel model) {
        Log.d(TAG, "🔄 Convirtiendo HotelServiceModel a BasicService: " + model.getName());

        BasicService basicService = new BasicService(
                model.getName(),
                model.getDescription(),
                model.getIconKey()
        );

        // Convertir URLs de fotos
        List<String> photoPaths = new ArrayList<>();
        if (model.getPhotoUrls() != null) {
            photoPaths.addAll(model.getPhotoUrls());
            Log.d(TAG, "📷 URLs de fotos transferidas: " + photoPaths.size());
        }
        basicService.setPhotos(photoPaths);
        basicService.setFirebaseId(model.getId());

        Log.d(TAG, "✅ BasicService creado con " + basicService.getPhotos().size() + " fotos");
        return basicService;
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

    // ✅ MÉTODO PARA MANEJAR CLICKS EN FOTOS DE SERVICIOS
    private void onServicePhotoClick(String photoUrl, int position, List<String> allPhotos) {
        // Encontrar el servicio al que pertenece esta foto
        String serviceName = "Servicio";
        for (BasicService service : basicServices) {
            if (service.getPhotos() != null && service.getPhotos().contains(photoUrl)) {
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

    // ========== IMPLEMENTACIÓN DE OnServicesChangedListener ==========

    @Override
    public void onBasicServicesUpdated(List<HotelServiceModel> basicServiceModels) {
        Log.d(TAG, "🔄 onBasicServicesUpdated llamado con " + basicServiceModels.size() + " servicios");

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                basicServices.clear();

                for (HotelServiceModel model : basicServiceModels) {
                    BasicService basicService = convertHotelServiceModelToBasicService(model);
                    basicServices.add(basicService);
                    Log.d(TAG, "➕ Agregado: " + basicService.getName() + " con " +
                            basicService.getPhotos().size() + " fotos");
                }

                // Actualizar adapter y visibilidad
                if (servicesAdapter != null) {
                    servicesAdapter.updateServices(basicServices);
                }
                updateServicesVisibility();

                Log.d(TAG, "✅ Servicios básicos actualizados en UI: " + basicServices.size());
            });
        }
    }

    @Override
    public void onAllServicesUpdated(List<HotelServiceModel> allServices) {
        Log.d(TAG, "🔄 onAllServicesUpdated llamado con " + allServices.size() + " servicios totales");
        // En este fragmento solo nos interesan los servicios básicos
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
        // El listener de servicios actualizará automáticamente la lista
    }

    @Override
    public void onServiceUpdated(HotelServiceModel service) {
        Log.d(TAG, "🔄 Servicio actualizado: " + service.getName());
        // El listener de servicios actualizará automáticamente la lista
    }

    @Override
    public void onServiceDeleted(String serviceId) {
        Log.d(TAG, "🗑️ Servicio eliminado: " + serviceId);
        // El listener de servicios actualizará automáticamente la lista
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

    // ========== GUARDAR PERFIL DEL HOTEL ==========

    private void saveHotelProfile() {
        String hotelName = etHotelName.getText().toString().trim();
        String hotelAddress = etHotelAddress.getText().toString().trim();

        if (hotelName.isEmpty()) {
            Toast.makeText(getContext(), "⚠️ Ingresa el nombre del hotel", Toast.LENGTH_SHORT).show();
            etHotelName.requestFocus();
            return;
        }

        if (hotelAddress.isEmpty()) {
            Toast.makeText(getContext(), "⚠️ Ingresa la dirección del hotel", Toast.LENGTH_SHORT).show();
            etHotelAddress.requestFocus();
            return;
        }

        // Aquí iría la lógica para guardar el perfil del hotel
        Toast.makeText(getContext(), "✅ Perfil del hotel guardado", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "💾 Perfil guardado - Hotel: " + hotelName + ", Dirección: " + hotelAddress);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (firebaseServiceManager != null) {
            firebaseServiceManager.removeListener(this);
        }
    }
}