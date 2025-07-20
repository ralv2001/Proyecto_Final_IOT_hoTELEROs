package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.RoomPhotosAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.ServiceSelectionAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseServiceManager;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceModel;
import com.example.proyecto_final_hoteleros.adminhotel.model.RoomType;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AddRoomTypeDialog extends Dialog {

    private static final String TAG = "AddRoomTypeDialog";
    private static final int MAX_PHOTOS = 3;

    public interface OnRoomTypeAddedListener {
        void onRoomTypeAdded(RoomType roomType, List<Uri> photoUris);
    }

    // Views
    private AutoCompleteTextView spinnerRoomType;
    private TextInputEditText etRoomArea, etRoomPrice, etAvailableRooms, etRoomCapacity;
    private RecyclerView rvServices, rvRoomPhotos;
    private Button btnSave, btnCancel;
    private MaterialCardView cardAddPhoto;
    private TextView tvSelectedCount, tvServicesStatus, tvPhotoCount;

    // Data
    private OnRoomTypeAddedListener listener;
    private List<String> selectedServices;
    private List<String> basicServices;
    private List<HotelServiceModel> availableIncludedServices;
    private ServiceSelectionAdapter serviceAdapter;

    // ✅ NUEVOS campos para fotos
    private List<Object> selectedPhotos; // Object porque puede ser Uri o String
    private RoomPhotosAdapter photosAdapter;
    private ActivityResultLauncher<Intent> photoPickerLauncher;

    // Firebase
    private FirebaseServiceManager firebaseServiceManager;

    // Lista predefinida de tipos de habitación
    private final String[] roomTypesArray = {
            "Habitación Individual",
            "Habitación Doble",
            "Habitación Twin",
            "Habitación Triple",
            "Habitación Cuádruple",
            "Habitación Familiar",
            "Habitación Standard",
            "Habitación Superior",
            "Habitación Deluxe",
            "Habitación Premium",
            "Junior Suite",
            "Suite Ejecutiva",
            "Suite Familiar",
            "Suite Presidencial",
            "Suite Penthouse",
            "Habitación con Balcón",
            "Habitación con Vista al Mar",
            "Habitación con Vista a la Ciudad",
            "Habitación con Vista al Jardín",
            "Habitación Accesible",
            "Habitación Económica",
            "Habitación de Lujo",
            "Villa",
            "Bungalow",
            "Cabaña"
    };

    public AddRoomTypeDialog(Context context, FirebaseServiceManager firebaseServiceManager,
                             OnRoomTypeAddedListener listener, ActivityResultLauncher<Intent> photoLauncher) {
        super(context);
        this.listener = listener;
        this.firebaseServiceManager = firebaseServiceManager;
        this.photoPickerLauncher = photoLauncher; // ✅ Recibir launcher del Fragment padre
        this.selectedServices = new ArrayList<>();
        this.basicServices = new ArrayList<>();
        this.availableIncludedServices = new ArrayList<>();
        this.selectedPhotos = new ArrayList<>(); // ✅ Inicializar lista de fotos

        setupDialog();
        loadServicesFromFirebase();
    }

    private void setupDialog() {
        setContentView(R.layout.admin_hotel_dialog_add_room_type);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        initViews();
        setupSpinner();
        setupRecyclerViews(); // ✅ Configurar ambos RecyclerViews
        setupClickListeners();
    }

    private void initViews() {
        spinnerRoomType = findViewById(R.id.spinnerRoomType);
        etRoomArea = findViewById(R.id.etRoomArea);
        etRoomPrice = findViewById(R.id.etRoomPrice);
        etAvailableRooms = findViewById(R.id.etAvailableRooms);
        etRoomCapacity = findViewById(R.id.etRoomCapacity);
        rvServices = findViewById(R.id.rvServices);
        rvRoomPhotos = findViewById(R.id.rvRoomPhotos); // ✅ NUEVO
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        cardAddPhoto = findViewById(R.id.cardAddPhoto); // ✅ NUEVO
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        tvServicesStatus = findViewById(R.id.tvServicesStatus);
        tvPhotoCount = findViewById(R.id.tvPhotoCount); // ✅ NUEVO
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, roomTypesArray);
        spinnerRoomType.setAdapter(adapter);
    }

    // ✅ NUEVO método para configurar ambos RecyclerViews
    private void setupRecyclerViews() {
        // RecyclerView para servicios
        serviceAdapter = new ServiceSelectionAdapter(availableIncludedServices, selectedServices, new ServiceSelectionAdapter.OnServiceSelectedListener() {
            @Override
            public void onServiceSelected(String serviceName, boolean isSelected) {
                updateSelectedCount();
                Log.d(TAG, "Servicio " + (isSelected ? "seleccionado" : "deseleccionado") + ": " + serviceName);
            }
        });
        rvServices.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvServices.setAdapter(serviceAdapter);

        // ✅ RecyclerView para fotos
        photosAdapter = new RoomPhotosAdapter(getContext(), selectedPhotos, new RoomPhotosAdapter.OnPhotoActionListener() {
            @Override
            public void onRemovePhoto(int position) {
                removePhoto(position);
            }

            @Override
            public void onPhotoClick(String photoUrl, int position, List<String> allPhotos) {
                // Implementar visor de fotos si es necesario
                Log.d(TAG, "Click en foto: " + photoUrl);
            }
        }, true); // true = modo edición

        rvRoomPhotos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRoomPhotos.setAdapter(photosAdapter);
    }

    // ✅ NUEVO método para manejar resultado de fotos (llamado desde Fragment)
    public void handlePhotoResult(Intent data) {
        if (data == null) return;

        try {
            if (data.getClipData() != null) {
                // Múltiples fotos seleccionadas
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count && selectedPhotos.size() < MAX_PHOTOS; i++) {
                    Uri photoUri = data.getClipData().getItemAt(i).getUri();
                    addPhoto(photoUri);
                }
            } else if (data.getData() != null) {
                // Una sola foto seleccionada
                Uri photoUri = data.getData();
                addPhoto(photoUri);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error procesando fotos seleccionadas: " + e.getMessage());
            Toast.makeText(getContext(), "Error seleccionando fotos", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                createRoomType();
            }
        });

        // ✅ NUEVO click listener para añadir fotos
        cardAddPhoto.setOnClickListener(v -> openPhotoSelector());
    }

    // ========== MANEJO DE FOTOS ==========

    // ✅ NUEVO método para abrir selector de fotos
    private void openPhotoSelector() {
        if (selectedPhotos.size() >= MAX_PHOTOS) {
            Toast.makeText(getContext(), "⚠️ Máximo " + MAX_PHOTOS + " fotos permitidas", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoPickerLauncher != null) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            Intent chooser = Intent.createChooser(intent, "Seleccionar fotos de habitación");
            photoPickerLauncher.launch(chooser);
        } else {
            Toast.makeText(getContext(), "Funcionalidad de fotos no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ NUEVO método para añadir foto
    private void addPhoto(Uri photoUri) {
        if (photoUri != null && selectedPhotos.size() < MAX_PHOTOS) {
            selectedPhotos.add(photoUri);
            updatePhotoUI();
            Log.d(TAG, "📷 Foto añadida: " + photoUri + " Total: " + selectedPhotos.size());
        }
    }

    // ✅ NUEVO método para remover foto
    private void removePhoto(int position) {
        if (position >= 0 && position < selectedPhotos.size()) {
            Object removedPhoto = selectedPhotos.remove(position);
            updatePhotoUI();
            Log.d(TAG, "🗑️ Foto removida en posición: " + position + " Total: " + selectedPhotos.size());
        }
    }

    // ✅ NUEVO método para actualizar UI de fotos
    private void updatePhotoUI() {
        if (photosAdapter != null) {
            photosAdapter.updatePhotos(selectedPhotos);
        }

        if (tvPhotoCount != null) {
            tvPhotoCount.setText(selectedPhotos.size() + " / " + MAX_PHOTOS + " fotos seleccionadas");
        }

        // Mostrar/ocultar RecyclerView de fotos
        if (rvRoomPhotos != null) {
            rvRoomPhotos.setVisibility(selectedPhotos.isEmpty() ?
                    android.view.View.GONE : android.view.View.VISIBLE);
        }
    }

    // ========== FIREBASE INTEGRATION ==========

    private void loadServicesFromFirebase() {
        if (firebaseServiceManager == null) {
            Log.e(TAG, "❌ FirebaseServiceManager is null");
            showServicesError("Servicio no disponible");
            return;
        }

        showServicesLoading();
        Log.d(TAG, "🔄 Cargando servicios desde Firebase...");

        // Cargar servicios básicos
        firebaseServiceManager.getServicesByType("basic", new FirebaseServiceManager.ServicesListCallback() {
            @Override
            public void onSuccess(List<HotelServiceModel> services) {
                basicServices.clear();
                for (HotelServiceModel service : services) {
                    basicServices.add(service.getName());
                }
                Log.d(TAG, "✅ Servicios básicos cargados: " + basicServices.size());

                // Después cargar servicios incluidos
                loadIncludedServices();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando servicios básicos: " + error);
                showServicesError("Error cargando servicios básicos");
            }
        });
    }

    private void loadIncludedServices() {
        firebaseServiceManager.getServicesByType("included", new FirebaseServiceManager.ServicesListCallback() {
            @Override
            public void onSuccess(List<HotelServiceModel> services) {
                availableIncludedServices.clear();
                availableIncludedServices.addAll(services);

                Log.d(TAG, "✅ Servicios incluidos cargados: " + availableIncludedServices.size());

                // Actualizar UI
                updateServicesUI();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando servicios incluidos: " + error);
                showServicesError("Error cargando servicios incluidos");
            }
        });
    }

    // ========== UI UPDATES ==========

    private void showServicesLoading() {
        if (tvServicesStatus != null) {
            tvServicesStatus.setText("🔄 Cargando servicios...");
            tvServicesStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.orange));
        }
    }

    private void showServicesError(String error) {
        if (tvServicesStatus != null) {
            tvServicesStatus.setText("❌ " + error);
            tvServicesStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        }
    }

    private void updateServicesUI() {
        if (serviceAdapter != null) {
            serviceAdapter.notifyDataSetChanged();
        }

        // Mostrar información de servicios básicos
        if (tvServicesStatus != null) {
            String basicServicesText = "✅ Servicios básicos incluidos automáticamente:\n" +
                    String.join(", ", basicServices);
            tvServicesStatus.setText(basicServicesText);
            tvServicesStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
        }

        updateSelectedCount();
        Log.d(TAG, "✅ UI de servicios actualizada");
    }

    private void updateSelectedCount() {
        if (tvSelectedCount != null) {
            int totalServices = basicServices.size() + selectedServices.size();
            String countText = "Servicios: " + basicServices.size() + " básicos + " +
                    selectedServices.size() + " incluidos = " + totalServices + " total";
            tvSelectedCount.setText(countText);
        }
    }

    // ========== VALIDATION & CREATION ==========

    private boolean validateInputs() {
        String roomTypeName = spinnerRoomType.getText().toString().trim();
        String area = etRoomArea.getText().toString().trim();
        String price = etRoomPrice.getText().toString().trim();
        String available = etAvailableRooms.getText().toString().trim();
        String capacity = etRoomCapacity.getText().toString().trim();

        if (roomTypeName.isEmpty()) {
            Toast.makeText(getContext(), "❌ Selecciona un tipo de habitación", Toast.LENGTH_SHORT).show();
            spinnerRoomType.requestFocus();
            return false;
        }

        if (area.isEmpty()) {
            Toast.makeText(getContext(), "❌ Ingresa el área de la habitación", Toast.LENGTH_SHORT).show();
            etRoomArea.requestFocus();
            return false;
        }

        if (price.isEmpty()) {
            Toast.makeText(getContext(), "❌ Ingresa el precio por noche", Toast.LENGTH_SHORT).show();
            etRoomPrice.requestFocus();
            return false;
        }

        if (available.isEmpty()) {
            Toast.makeText(getContext(), "❌ Ingresa la cantidad disponible", Toast.LENGTH_SHORT).show();
            etAvailableRooms.requestFocus();
            return false;
        }

        if (capacity.isEmpty()) {
            Toast.makeText(getContext(), "❌ Ingresa la capacidad de personas", Toast.LENGTH_SHORT).show();
            etRoomCapacity.requestFocus();
            return false;
        }

        try {
            double areaValue = Double.parseDouble(area);
            double priceValue = Double.parseDouble(price);
            int availableValue = Integer.parseInt(available);
            int capacityValue = Integer.parseInt(capacity);

            if (areaValue <= 0) {
                Toast.makeText(getContext(), "❌ El área debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (priceValue <= 0) {
                Toast.makeText(getContext(), "❌ El precio debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (availableValue <= 0) {
                Toast.makeText(getContext(), "❌ La cantidad disponible debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (capacityValue <= 0 || capacityValue > 10) {
                Toast.makeText(getContext(), "❌ La capacidad debe ser entre 1 y 10 personas", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "❌ Verifica que los números sean válidos", Toast.LENGTH_SHORT).show();
            return false;
        }

        // ✅ Validación opcional de fotos
        if (selectedPhotos.isEmpty()) {
            Toast.makeText(getContext(), "⚠️ Recomendamos añadir al menos una foto", Toast.LENGTH_SHORT).show();
            // No bloquear, solo advertir
        }

        return true;
    }

    private void createRoomType() {
        // Deshabilitar botón para evitar doble envío
        btnSave.setEnabled(false);
        btnSave.setText("🔄 Creando...");

        try {
            String roomTypeName = spinnerRoomType.getText().toString().trim();
            double area = Double.parseDouble(etRoomArea.getText().toString().trim());
            double price = Double.parseDouble(etRoomPrice.getText().toString().trim());
            int available = Integer.parseInt(etAvailableRooms.getText().toString().trim());
            int capacity = Integer.parseInt(etRoomCapacity.getText().toString().trim());

            // ✅ COMBINAR SERVICIOS: Básicos + Incluidos seleccionados
            List<String> allServices = new ArrayList<>();
            allServices.addAll(basicServices); // Servicios básicos automáticos
            allServices.addAll(selectedServices); // Servicios incluidos seleccionados

            // ✅ Convertir fotos a URIs
            List<Uri> photoUris = new ArrayList<>();
            for (Object photo : selectedPhotos) {
                if (photo instanceof Uri) {
                    photoUris.add((Uri) photo);
                }
            }

            Log.d(TAG, "🏨 Creando habitación con " + allServices.size() + " servicios y " + photoUris.size() + " fotos:");
            Log.d(TAG, "   - Básicos: " + basicServices.size());
            Log.d(TAG, "   - Incluidos: " + selectedServices.size());
            Log.d(TAG, "   - Fotos: " + photoUris.size());

            RoomType roomType = new RoomType(
                    roomTypeName,
                    "", // Sin descripción por ahora
                    area,
                    price,
                    allServices,
                    available,
                    capacity
            );

            if (listener != null) {
                listener.onRoomTypeAdded(roomType, photoUris); // ✅ Pasar fotos al listener
            }

            dismiss();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error creando habitación: " + e.getMessage());
            Toast.makeText(getContext(), "❌ Error creando habitación", Toast.LENGTH_SHORT).show();

            // Restaurar botón
            btnSave.setEnabled(true);
            btnSave.setText("💾 Crear Habitación");
        }
    }
}