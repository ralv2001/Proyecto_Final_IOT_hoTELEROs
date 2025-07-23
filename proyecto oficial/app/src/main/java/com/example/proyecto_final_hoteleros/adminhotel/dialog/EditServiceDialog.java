package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.ServicePhotosAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.IconSelectorDialog;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceModel;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseServiceManager;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class EditServiceDialog extends AppCompatDialog {

    private static final String TAG = "EditServiceDialog";

    // ✅ INTERFACE OPTIMIZADA: Usar HotelServiceModel directamente
    public interface OnServiceEditedListener {
        void onServiceEdited(HotelServiceModel service);
        void onError(String error);
    }

    private Context context;
    private OnServiceEditedListener listener;
    private HotelServiceModel originalService; // ✅ CAMBIADO: Usar HotelServiceModel directamente

    // Views
    private TextInputEditText etServiceName, etServiceDescription, etServicePrice;
    private AutoCompleteTextView etServiceType;
    private TextInputLayout tilServicePrice;
    private ImageView ivSelectedIcon;
    private TextView tvSelectedIconName, tvPhotoCount;
    private LinearLayout layoutIconPreview;
    private MaterialCardView cardAddPhoto;
    private RecyclerView rvServicePhotos;
    private MaterialButton btnCancel, btnSave;

    // Variables de estado
    private String selectedIconKey;
    private List<Uri> servicePhotos;
    private ServicePhotosAdapter photosAdapter;
    private ActivityResultLauncher<Intent> photoPickerLauncher;
    private FirebaseServiceManager firebaseServiceManager;

    // ✅ Solo 3 tipos - Taxi condicional tiene su propia sección
    private final String[] serviceTypes = {
            "Básico",
            "Incluido",
            "Pagado"
    };

    public EditServiceDialog(Context context, HotelServiceModel service, OnServiceEditedListener listener, ActivityResultLauncher<Intent> photoLauncher) {
        super(context, R.style.DialogTheme);
        this.context = context;
        this.originalService = service;
        this.listener = listener;
        this.photoPickerLauncher = photoLauncher;
        this.servicePhotos = new ArrayList<>();
        this.selectedIconKey = service.getIconKey() != null ? service.getIconKey() : "service";

        // ✅ Inicializar manager
        this.firebaseServiceManager = FirebaseServiceManager.getInstance(context);

        // ✅ CONVERTIR URLs DE FOTOS A Uri
        if (service.getPhotoUrls() != null) {
            for (String url : service.getPhotoUrls()) {
                if (url != null && !url.trim().isEmpty()) {
                    try {
                        servicePhotos.add(Uri.parse(url));
                    } catch (Exception e) {
                        Log.w(TAG, "Error parseando URL de foto: " + url);
                    }
                }
            }
        }

        setupDialog();
    }

    private void setupDialog() {
        setContentView(R.layout.admin_hotel_dialog_add_service);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        initializeViews();
        setupServiceTypeDropdown();
        setupPhotoRecyclerView();
        setupListeners();
        loadCurrentData();
    }

    private void initializeViews() {
        etServiceName = findViewById(R.id.etServiceName);
        etServiceDescription = findViewById(R.id.etServiceDescription);
        etServicePrice = findViewById(R.id.etServicePrice);
        etServiceType = findViewById(R.id.etServiceType);
        tilServicePrice = findViewById(R.id.tilServicePrice);
        ivSelectedIcon = findViewById(R.id.ivSelectedIcon);
        tvSelectedIconName = findViewById(R.id.tvSelectedIconName);
        tvPhotoCount = findViewById(R.id.tvPhotoCount);
        layoutIconPreview = findViewById(R.id.layoutIconPreview);
        cardAddPhoto = findViewById(R.id.cardAddPhoto);
        rvServicePhotos = findViewById(R.id.rvServicePhotos);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);

        // Cambiar texto del botón
        if (btnSave != null) {
            btnSave.setText("💾 Actualizar Servicio");
        }

        updateIconPreview();
        updatePhotosVisibility();

        // ✅ Ocultar campos condicionales ya que no se usan más
        View tilConditionalAmount = findViewById(R.id.tilConditionalAmount);
        if (tilConditionalAmount != null) {
            tilConditionalAmount.setVisibility(View.GONE);
        }
    }

    private void setupServiceTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, serviceTypes);
        etServiceType.setAdapter(adapter);

        // ✅ Listener para mostrar/ocultar campos según el tipo
        etServiceType.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = serviceTypes[position];
            updateFieldsVisibility(selectedType);
        });
    }

    private void updateFieldsVisibility(String serviceType) {
        if (tilServicePrice == null) return;

        switch (serviceType) {
            case "Básico":
                tilServicePrice.setVisibility(View.GONE);
                etServicePrice.setText("0");
                break;

            case "Incluido":
                tilServicePrice.setVisibility(View.GONE);
                etServicePrice.setText("0");
                break;

            case "Pagado":
                tilServicePrice.setVisibility(View.VISIBLE);
                if (etServicePrice.getText().toString().trim().equals("0")) {
                    etServicePrice.setText("");
                }
                break;
        }
    }

    private void setupPhotoRecyclerView() {
        if (rvServicePhotos != null) {
            rvServicePhotos.setLayoutManager(new GridLayoutManager(context, 3));
            photosAdapter = new ServicePhotosAdapter(servicePhotos, position -> removePhoto(position));
            rvServicePhotos.setAdapter(photosAdapter);
        }
    }

    private void setupListeners() {
        // Botón cancelar
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dismiss());
        }

        // Botón guardar
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveChanges());
        }

        // Selector de iconos
        if (layoutIconPreview != null) {
            layoutIconPreview.setOnClickListener(v -> showIconSelector());
        }

        // Agregar fotos
        if (cardAddPhoto != null) {
            cardAddPhoto.setOnClickListener(v -> selectPhotos());
        }
    }

    private void loadCurrentData() {
        // ✅ CARGAR DATOS DEL SERVICIO ORIGINAL
        etServiceName.setText(originalService.getName());
        etServiceDescription.setText(originalService.getDescription());

        // ✅ CONFIGURAR TIPO DE SERVICIO - Solo 3 tipos
        String serviceTypeText = getServiceTypeText(originalService.getServiceType());
        etServiceType.setText(serviceTypeText, false);
        updateFieldsVisibility(serviceTypeText);

        // Configurar precio
        if (originalService.getPrice() > 0) {
            etServicePrice.setText(String.valueOf(originalService.getPrice()));
        }

        Log.d(TAG, "✅ Datos cargados para edición: " + originalService.getName() +
                " (Tipo: " + originalService.getServiceType() +
                ", Precio: " + originalService.getPrice() +
                ", Fotos: " + servicePhotos.size() + ")");
    }

    // ✅ MÉTODO OPTIMIZADO: Convertir tipo de servicio para mostrar
    private String getServiceTypeText(String serviceType) {
        if (serviceType == null) return "Incluido";

        switch (serviceType.toLowerCase()) {
            case "basic":
            case "básico":
                return "Básico";
            case "included":
            case "incluido":
                return "Incluido";
            case "paid":
            case "pagado":
                return "Pagado";
            default:
                return "Incluido";
        }
    }

    private void showIconSelector() {
        IconSelectorDialog iconDialog = new IconSelectorDialog(context, selectedIconKey,
                new IconSelectorDialog.OnIconSelectedListener() {
                    @Override
                    public void onIconSelected(String iconKey, String iconName) {
                        selectedIconKey = iconKey;
                        updateIconPreview();
                    }
                });
        iconDialog.show();
    }

    private void updateIconPreview() {
        if (ivSelectedIcon != null && tvSelectedIconName != null) {
            int iconResId = IconHelper.getIconResource(selectedIconKey);
            if (iconResId != 0) {
                ivSelectedIcon.setImageResource(iconResId);
                tvSelectedIconName.setText(IconHelper.getIconName(selectedIconKey));
            } else {
                ivSelectedIcon.setImageResource(R.drawable.ic_service_default);
                tvSelectedIconName.setText("Icono por defecto");
            }
        }
    }

    private void selectPhotos() {
        if (servicePhotos.size() >= 5) {
            Toast.makeText(context, "Máximo 5 fotos permitidas", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoPickerLauncher != null) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            photoPickerLauncher.launch(Intent.createChooser(intent, "Seleccionar fotos"));
        } else {
            Toast.makeText(context, "Funcionalidad de fotos no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    public void handlePhotoResult(Intent data) {
        if (data == null) return;

        try {
            if (data.getClipData() != null) {
                // Múltiples fotos seleccionadas
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count && servicePhotos.size() < 5; i++) {
                    Uri photoUri = data.getClipData().getItemAt(i).getUri();
                    addPhoto(photoUri);
                }
            } else if (data.getData() != null) {
                // Una sola foto seleccionada
                addPhoto(data.getData());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error procesando fotos seleccionadas: " + e.getMessage());
            Toast.makeText(context, "Error seleccionando fotos", Toast.LENGTH_SHORT).show();
        }
    }

    private void addPhoto(Uri photoUri) {
        if (servicePhotos.size() < 5) {
            servicePhotos.add(photoUri);
            if (photosAdapter != null) {
                photosAdapter.notifyItemInserted(servicePhotos.size() - 1);
            }
            updatePhotosVisibility();
            Log.d(TAG, "📷 Foto agregada. Total: " + servicePhotos.size());
        }
    }

    private void removePhoto(int position) {
        if (position >= 0 && position < servicePhotos.size()) {
            servicePhotos.remove(position);
            if (photosAdapter != null) {
                photosAdapter.notifyItemRemoved(position);
            }
            updatePhotosVisibility();
            Log.d(TAG, "📷 Foto eliminada. Total: " + servicePhotos.size());
        }
    }

    private void updatePhotosVisibility() {
        if (tvPhotoCount != null) {
            tvPhotoCount.setText(servicePhotos.size() + "/5");
        }

        if (rvServicePhotos != null) {
            rvServicePhotos.setVisibility(servicePhotos.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    // ✅ MÉTODO PRINCIPAL OPTIMIZADO: Guardar cambios directamente
    private void saveChanges() {
        Log.d(TAG, "💾 Iniciando actualización de servicio...");

        // Validaciones
        String name = etServiceName.getText().toString().trim();
        if (name.isEmpty()) {
            etServiceName.setError("Campo requerido");
            etServiceName.requestFocus();
            return;
        }

        String description = etServiceDescription.getText().toString().trim();
        if (description.isEmpty()) {
            etServiceDescription.setError("Campo requerido");
            etServiceDescription.requestFocus();
            return;
        }

        String serviceTypeStr = etServiceType.getText().toString().trim();
        if (serviceTypeStr.isEmpty()) {
            etServiceType.setError("Selecciona un tipo");
            etServiceType.requestFocus();
            return;
        }

        // Validar precio si es necesario
        double price = 0.0;
        if ("Pagado".equals(serviceTypeStr)) {
            String priceText = etServicePrice.getText().toString().trim();
            if (priceText.isEmpty()) {
                etServicePrice.setError("Campo requerido para servicios pagados");
                etServicePrice.requestFocus();
                return;
            }

            try {
                price = Double.parseDouble(priceText);
                if (price <= 0) {
                    etServicePrice.setError("El precio debe ser mayor a 0");
                    etServicePrice.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etServicePrice.setError("Precio inválido");
                etServicePrice.requestFocus();
                return;
            }
        }

        // ✅ Deshabilitar botón mientras se guarda
        btnSave.setEnabled(false);
        btnSave.setText("💾 Actualizando...");

        // ✅ ACTUALIZAR SERVICIO DIRECTAMENTE
        updateServiceInFirebase(name, description, price, serviceTypeStr);
    }

    // ✅ MÉTODO OPTIMIZADO: Actualizar servicio directamente en Firebase
    private void updateServiceInFirebase(String name, String description, double price, String serviceTypeStr) {
        Log.d(TAG, "🔄 Actualizando servicio en Firebase: " + name + " (Tipo: " + serviceTypeStr + ")");

        // ✅ ACTUALIZAR EL SERVICIO ORIGINAL
        originalService.setName(name);
        originalService.setDescription(description);
        originalService.setIconKey(selectedIconKey);
        originalService.setServiceType(convertCategoryToType(serviceTypeStr));
        originalService.setPrice(price);
        // Mantener otros campos como están (ID, hotelAdminId, createdAt, etc.)

        // ✅ ACTUALIZAR EN FIREBASE
        if (firebaseServiceManager != null) {
            firebaseServiceManager.updateService(originalService, servicePhotos, new FirebaseServiceManager.ServiceCallback() {
                @Override
                public void onSuccess(HotelServiceModel updatedService) {
                    Log.d(TAG, "✅ Servicio actualizado exitosamente: " + updatedService.getName());

                    if (listener != null) {
                        listener.onServiceEdited(updatedService);
                    }

                    dismiss();
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error actualizando servicio: " + error);
                    restoreButton();

                    if (listener != null) {
                        listener.onError(error);
                    }
                }
            });
        } else {
            restoreButton();
            if (listener != null) {
                listener.onError("Error: FirebaseServiceManager no disponible");
            }
        }
    }

    // ✅ MÉTODO OPTIMIZADO: Convertir categoría a tipo
    private String convertCategoryToType(String category) {
        switch (category) {
            case "Básico":
                return "basic";
            case "Incluido":
                return "included";
            case "Pagado":
                return "paid";
            default:
                return "included"; // Por defecto
        }
    }

    private void restoreButton() {
        if (btnSave != null) {
            btnSave.setEnabled(true);
            btnSave.setText("💾 Actualizar Servicio");
        }
    }
}