package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelProfile;
import com.example.proyecto_final_hoteleros.adminhotel.model.BasicService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class HotelProfileFragment extends Fragment {

    private TextInputEditText etHotelName, etHotelAddress;
    private RecyclerView rvHotelPhotos, rvBasicServices;
    private MaterialButton btnSaveProfile, btnAddPhoto, btnAddBasicService;
    private ImageView ivBack;
    private TextView tvPhotosStatus, tvPhotosCounter;

    private HotelPhotosAdapter photosAdapter;
    private BasicServicesAdapter servicesAdapter;
    private List<Uri> hotelPhotos;
    private List<BasicService> basicServices;
    private ActivityResultLauncher<Intent> servicePhotoPickerLauncher; // NUEVA LÍNEA

    private ActivityResultLauncher<Intent> photoPickerLauncher;
    private BasicServiceDialog currentBasicServiceDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_profile_management, container, false);

        initViews(rootView);
        initLaunchers();
        setupRecyclerViews();
        setupClickListeners();

        // IMPORTANTE: Cargar datos después de que todo esté configurado
        // y usar post() para asegurar que el layout esté completo
        rootView.post(() -> {
            loadHotelProfile();
            forceRecyclerViewUpdate(); // Nueva función
        });

        return rootView;
    }
    // NUEVA FUNCIÓN: Forzar actualización del RecyclerView
    private void forceRecyclerViewUpdate() {
        if (servicesAdapter != null && rvBasicServices != null) {
            // Forzar recálculo de dimensiones
            servicesAdapter.notifyDataSetChanged();

            // Asegurar que el RecyclerView recalcule su altura
            rvBasicServices.post(() -> {
                if (rvBasicServices.getLayoutManager() != null) {
                    rvBasicServices.getLayoutManager().requestLayout();
                }
            });
        }
    }
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

        hotelPhotos = new ArrayList<>();
        basicServices = new ArrayList<>();
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

        // NUEVO: Launcher separado para fotos de servicios básicos
        servicePhotoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null && currentBasicServiceDialog != null) {
                            // Agregar foto al diálogo del servicio, NO al hotel
                            currentBasicServiceDialog.addPhoto(selectedImageUri);
                        }
                    }
                }
        );
    }

    private void setupRecyclerViews() {
        // RecyclerView para fotos del hotel
        photosAdapter = new HotelPhotosAdapter(hotelPhotos, this::removePhoto);
        rvHotelPhotos.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvHotelPhotos.setAdapter(photosAdapter);

        // RecyclerView para servicios básicos - CONFIGURACIÓN MEJORADA
        servicesAdapter = new BasicServicesAdapter(basicServices, this::removeBasicService);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvBasicServices.setLayoutManager(layoutManager);
        rvBasicServices.setAdapter(servicesAdapter);

        // IMPORTANTE: Configurar para mejor comportamiento en ScrollView
        rvBasicServices.setNestedScrollingEnabled(false);
        rvBasicServices.setHasFixedSize(false); // Permite que cambie de tamaño
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnSaveProfile.setOnClickListener(v -> saveHotelProfile());

        // CORRECCIÓN: Permitir subir fotos SIEMPRE que no se haya alcanzado el máximo
        btnAddPhoto.setOnClickListener(v -> {
            if (hotelPhotos.size() < 8) { // Máximo 8 fotos
                selectPhoto();
            } else {
                Toast.makeText(getContext(), "Máximo 8 fotos permitidas", Toast.LENGTH_SHORT).show();
            }
        });

        btnAddBasicService.setOnClickListener(v -> showAddBasicServiceDialog());
    }


    private void addHotelPhoto(Uri photoUri) {
        if (hotelPhotos.size() < 8) {
            hotelPhotos.add(photoUri);
            photosAdapter.notifyItemInserted(hotelPhotos.size() - 1);
            updatePhotosStatus();

            // AGREGAR: Refrescar el RecyclerView de servicios para evitar inconsistencias visuales
            forceRecyclerViewUpdate();

            Toast.makeText(getContext(), "Foto del hotel agregada (" + hotelPhotos.size() + "/8)", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Máximo 8 fotos permitidas", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePhotosStatus() {
        int photoCount = hotelPhotos.size();
        tvPhotosCounter.setText(photoCount + "/8 fotos");

        if (photoCount == 0) {
            tvPhotosStatus.setText("📷 No hay fotos subidas");
            tvPhotosStatus.setTextColor(getResources().getColor(R.color.text_secondary));
            btnAddPhoto.setText("📷 Subir Primera Foto");
        } else if (photoCount < 4) {
            tvPhotosStatus.setText("⚠️ Se requieren mínimo 4 fotos (" + (4 - photoCount) + " faltantes)");
            tvPhotosStatus.setTextColor(getResources().getColor(R.color.warning));
            btnAddPhoto.setText("📷 Agregar Foto (" + photoCount + "/4 requeridas)");
        } else if (photoCount >= 4 && photoCount < 8) {
            tvPhotosStatus.setText("✅ Fotos del hotel (" + photoCount + " fotos)");
            tvPhotosStatus.setTextColor(getResources().getColor(R.color.success));
            btnAddPhoto.setText("📷 Agregar Más Fotos");
        } else {
            tvPhotosStatus.setText("✅ Máximo de fotos alcanzado (8/8)");
            tvPhotosStatus.setTextColor(getResources().getColor(R.color.success));
            btnAddPhoto.setText("📷 Máximo Alcanzado");
            btnAddPhoto.setEnabled(false);
        }

        // Mostrar/ocultar RecyclerView según si hay fotos
        rvHotelPhotos.setVisibility(photoCount > 0 ? View.VISIBLE : View.GONE);

        // Habilitar/deshabilitar el botón según el límite
        btnAddPhoto.setEnabled(photoCount < 8);
    }

    private void selectPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        photoPickerLauncher.launch(intent);
    }

    private void showAddBasicServiceDialog() {
        // CAMBIO: Usar el launcher específico para servicios
        currentBasicServiceDialog = new BasicServiceDialog(getContext(), servicePhotoPickerLauncher, service -> {
            basicServices.add(service);
            servicesAdapter.notifyItemInserted(basicServices.size() - 1);

            // AGREGAR: Forzar actualización después de agregar servicio
            forceRecyclerViewUpdate();

            currentBasicServiceDialog = null;
            Toast.makeText(getContext(), "✅ Servicio básico agregado", Toast.LENGTH_SHORT).show();
        });
        currentBasicServiceDialog.show();

        currentBasicServiceDialog.setOnDismissListener(dialog -> {
            currentBasicServiceDialog = null;
        });
    }

    private void removePhoto(int position) {
        hotelPhotos.remove(position);
        photosAdapter.notifyItemRemoved(position);
        updatePhotosStatus();
    }

    private void removeBasicService(int position) {
        basicServices.remove(position);
        servicesAdapter.notifyItemRemoved(position);

        // AGREGAR: Forzar actualización después de remover servicio
        forceRecyclerViewUpdate();
    }

    private void loadHotelProfile() {
        // Cargar datos básicos del hotel
        etHotelName.setText("Hotel Belmond");
        etHotelAddress.setText("Av. Principal 123, Centro, Lima, Perú");

        // Limpiar lista existente
        basicServices.clear();

        // Agregar servicios básicos por defecto
        basicServices.add(new BasicService("WiFi Gratuito", "Internet de alta velocidad incluido en todas las habitaciones", "ic_wifi"));
        basicServices.add(new BasicService("Aire Acondicionado", "Climatización individual en cada habitación", "ic_ac"));
        basicServices.add(new BasicService("TV por Cable", "Televisión por cable con canales premium", "ic_tv"));
        basicServices.add(new BasicService("Agua Caliente 24h", "Agua caliente disponible las 24 horas", "ic_water"));
        basicServices.add(new BasicService("Servicio de Limpieza", "Limpieza diaria de habitaciones incluida", "ic_cleaning"));

        // MEJORAR: Notificar cambios después de agregar todos los elementos
        if (servicesAdapter != null) {
            servicesAdapter.notifyDataSetChanged();
        }

        updatePhotosStatus();
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

        // TODO: Guardar en Firebase/base de datos
        HotelProfile profile = new HotelProfile(name, address, hotelPhotos, basicServices);

        Toast.makeText(getContext(), "✅ Perfil del hotel actualizado exitosamente", Toast.LENGTH_LONG).show();
        getParentFragmentManager().popBackStack();
    }
}