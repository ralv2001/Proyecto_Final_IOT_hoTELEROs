package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.proyecto_final_hoteleros.adminhotel.utils.ImageCompressor;
import com.example.proyecto_final_hoteleros.utils.UniqueIdGenerator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddServiceDialog extends AppCompatDialog {

    private static final String TAG = "AddServiceDialog";

    // ✅ INTERFACE OPTIMIZADA: Usar HotelServiceModel directamente
    public interface OnServiceAddedListener {
        void onServiceAdded(HotelServiceModel service);
        void onError(String error);
    }

    private Context context;
    private TextInputEditText etServiceName, etServiceDescription, etServicePrice, etConditionalAmount;
    private AutoCompleteTextView etServiceType;
    private TextInputLayout tilServicePrice, tilConditionalAmount;
    private ImageView ivSelectedIcon;
    private TextView tvSelectedIconName, tvPhotoCount;
    private LinearLayout layoutIconPreview;
    private MaterialCardView cardAddPhoto;
    private RecyclerView rvServicePhotos;
    private MaterialButton btnCancel, btnSave;

    private String selectedIconKey = "service";
    private List<Uri> servicePhotos;
    private ServicePhotosAdapter photosAdapter;
    private ActivityResultLauncher<Intent> photoPickerLauncher;
    private OnServiceAddedListener listener;

    // ✅ MANAGERS OPTIMIZADOS
    private FirebaseServiceManager firebaseServiceManager;
    private UniqueIdGenerator idGenerator;

    // ✅ Solo 3 tipos - Taxi condicional tiene su propia sección
    private final String[] serviceTypes = {
            "Básico",
            "Incluido",
            "Pagado"
    };

    public AddServiceDialog(Context context, OnServiceAddedListener listener, ActivityResultLauncher<Intent> photoLauncher) {
        super(context, R.style.DialogTheme);
        this.context = context;
        this.listener = listener;
        this.photoPickerLauncher = photoLauncher;
        this.servicePhotos = new ArrayList<>();

        // ✅ Inicializar managers
        this.firebaseServiceManager = FirebaseServiceManager.getInstance(context);
        this.idGenerator = UniqueIdGenerator.getInstance(context);

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
    }

    private void initializeViews() {
        etServiceName = findViewById(R.id.etServiceName);
        etServiceDescription = findViewById(R.id.etServiceDescription);
        etServicePrice = findViewById(R.id.etServicePrice);
        etConditionalAmount = findViewById(R.id.etConditionalAmount);
        etServiceType = findViewById(R.id.etServiceType);
        tilServicePrice = findViewById(R.id.tilServicePrice);
        tilConditionalAmount = findViewById(R.id.tilConditionalAmount);
        ivSelectedIcon = findViewById(R.id.ivSelectedIcon);
        tvSelectedIconName = findViewById(R.id.tvSelectedIconName);
        tvPhotoCount = findViewById(R.id.tvPhotoCount);
        layoutIconPreview = findViewById(R.id.layoutIconPreview);
        cardAddPhoto = findViewById(R.id.cardAddPhoto);
        rvServicePhotos = findViewById(R.id.rvServicePhotos);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);

        // Configurar icono por defecto
        updateIconPreview();
        updatePhotosVisibility();

        // ✅ Ocultar campos condicionales ya que no se usan más
        if (tilConditionalAmount != null) {
            tilConditionalAmount.setVisibility(View.GONE);
        }
    }

    private void setupServiceTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, serviceTypes);
        etServiceType.setAdapter(adapter);

        // ✅ Valor por defecto
        etServiceType.setText("Incluido", false);

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
            btnSave.setOnClickListener(v -> saveService());
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

    // ✅ MÉTODO PRINCIPAL OPTIMIZADO: Guardar servicio directamente como HotelServiceModel
    private void saveService() {
        Log.d(TAG, "💾 Iniciando guardado de servicio...");

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
        btnSave.setText("💾 Guardando...");

        // ✅ CREAR SERVICIO DIRECTAMENTE COMO HotelServiceModel
        createServiceInFirebase(name, description, price, serviceTypeStr);
    }

    // ✅ MÉTODO OPTIMIZADO: Crear servicio directamente en Firebase
    private void createServiceInFirebase(String name, String description, double price, String serviceTypeStr) {
        Log.d(TAG, "🚀 Creando servicio en Firebase: " + name + " (Tipo: " + serviceTypeStr + ")");

        // ✅ CREAR SERVICIO DIRECTAMENTE COMO HotelServiceModel
        HotelServiceModel service = new HotelServiceModel();
        service.setName(name);
        service.setDescription(description);
        service.setIconKey(selectedIconKey);
        service.setServiceType(convertCategoryToType(serviceTypeStr));
        service.setPrice(price);
        service.setConditionalAmount(0.0);
        service.setActive(true);
        service.setCreatedAt(new Date());

        // Obtener usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            restoreButton();
            if (listener != null) {
                listener.onError("Usuario no autenticado");
            }
            return;
        }
        service.setHotelAdminId(currentUser.getUid());

        // ✅ COMPRIMIR FOTOS ANTES DE SUBIR (SI HAY FOTOS)
        if (servicePhotos != null && !servicePhotos.isEmpty()) {
            Log.d(TAG, "📷 Comprimiendo " + servicePhotos.size() + " fotos antes de subir...");

            // Comprimir en hilo separado para no bloquear UI
            new Thread(() -> {
                try {
                    List<Uri> compressedPhotos = ImageCompressor.compressImages(context, servicePhotos);

                    // Volver al hilo principal para continuar
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> {
                            uploadServiceWithCompressedPhotos(service, compressedPhotos);
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error comprimiendo fotos: " + e.getMessage());
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> {
                            restoreButton();
                            if (listener != null) {
                                listener.onError("Error procesando imágenes: " + e.getMessage());
                            }
                        });
                    }
                }
            }).start();
        } else {
            // ✅ SIN FOTOS: Subir servicio directamente
            uploadServiceWithCompressedPhotos(service, new ArrayList<>());
        }
    }

    private void uploadServiceWithCompressedPhotos(HotelServiceModel service, List<Uri> compressedPhotos) {
        Log.d(TAG, "📤 Subiendo servicio con " + compressedPhotos.size() + " fotos comprimidas");

        if (firebaseServiceManager != null) {
            firebaseServiceManager.createService(service, compressedPhotos, new FirebaseServiceManager.ServiceCallback() {
                @Override
                public void onSuccess(HotelServiceModel createdService) {
                    Log.d(TAG, "✅ Servicio creado exitosamente: " + createdService.getName());

                    // ✅ LIMPIAR ARCHIVOS TEMPORALES
                    ImageCompressor.cleanupTempFiles(context);

                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> {
                            if (listener != null) {
                                listener.onServiceAdded(createdService);
                            }
                            dismiss();
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error creando servicio: " + error);

                    // ✅ LIMPIAR ARCHIVOS TEMPORALES INCLUSO SI FALLA
                    ImageCompressor.cleanupTempFiles(context);

                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> {
                            restoreButton();
                            if (listener != null) {
                                listener.onError(error);
                            }
                        });
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
        // ✅ ASEGURAR QUE SE EJECUTE EN EL HILO PRINCIPAL
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> {
                if (btnSave != null) {
                    btnSave.setEnabled(true);
                    btnSave.setText("💾 Guardar Servicio");
                }
            });
        } else {
            // ✅ ALTERNATIVA: Usar Handler si no es Activity
            new Handler(Looper.getMainLooper()).post(() -> {
                if (btnSave != null) {
                    btnSave.setEnabled(true);
                    btnSave.setText("💾 Guardar Servicio");
                }
            });
        }
    }
}