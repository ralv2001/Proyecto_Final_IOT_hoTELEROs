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

    // Datos
    private List<Uri> hotelPhotos;
    private List<HotelServiceModel> basicServices; // ✅ CAMBIADO: Usar HotelServiceModel directamente

    // Adapters
    private HotelPhotosAdapter photosAdapter;
    private BasicServicesAdapter servicesAdapter;

    // Managers
    private FirebaseServiceManager firebaseServiceManager;
    private AwsFileManager awsFileManager;
    private UniqueIdGenerator idGenerator;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> photoPickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar managers
        firebaseServiceManager = FirebaseServiceManager.getInstance(getContext());
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
        loadHotelProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 onResume - Registrando listener de servicios");

        // Registrar listener para cambios en tiempo real
        if (firebaseServiceManager != null) {
            firebaseServiceManager.addListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "⏸️ onPause - Desregistrando listener de servicios");

        // Desregistrar listener para evitar memory leaks
        if (firebaseServiceManager != null) {
            firebaseServiceManager.removeListener(this);
        }
    }

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
    }

    private void initializeLists() {
        hotelPhotos = new ArrayList<>();
        basicServices = new ArrayList<>(); // ✅ CAMBIADO: Lista de HotelServiceModel
    }

    private void setupRecyclerViews() {
        // ✅ ADAPTER DE FOTOS DEL HOTEL
        photosAdapter = new HotelPhotosAdapter(hotelPhotos, this::removePhoto);
        rvHotelPhotos.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvHotelPhotos.setAdapter(photosAdapter);

        // ✅ ADAPTER DE SERVICIOS BÁSICOS - SIMPLIFICADO
        servicesAdapter = new BasicServicesAdapter(getContext(), basicServices, this::onServicePhotoClick);
        rvBasicServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBasicServices.setAdapter(servicesAdapter);
        rvBasicServices.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {
        // Agregar fotos del hotel
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
            Log.d(TAG, "📷 Foto del hotel eliminada. Total: " + hotelPhotos.size());
        }
    }

    private void updatePhotosStatus() {
        if (tvPhotosStatus != null) {
            int photoCount = hotelPhotos.size();
            if (photoCount == 0) {
                tvPhotosStatus.setText("Sin fotos");
                if (btnAddPhoto != null) {
                    btnAddPhoto.setText("Agregar Fotos (0/10)");
                }
            } else {
                tvPhotosStatus.setText(photoCount + " foto" + (photoCount != 1 ? "s" : ""));
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

    // ========== IMPLEMENTACIÓN DE OnServicesChangedListener ==========

    @Override
    public void onBasicServicesUpdated(List<HotelServiceModel> basicServiceModels) {
        Log.d(TAG, "🔄 onBasicServicesUpdated llamado con " + basicServiceModels.size() + " servicios");

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // ✅ SIMPLIFICADO: Usar directamente HotelServiceModel
                basicServices.clear();
                basicServices.addAll(basicServiceModels);

                // ✅ Actualizar adapter y visibilidad
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

        // ✅ FILTRAR SOLO SERVICIOS BÁSICOS
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
}