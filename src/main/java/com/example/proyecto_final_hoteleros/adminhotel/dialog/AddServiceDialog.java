package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.ServicePhotosAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.IconSelectorDialog;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceItem;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceModel;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseServiceManager;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;
import com.example.proyecto_final_hoteleros.utils.AwsFileManager;
import com.example.proyecto_final_hoteleros.utils.UniqueIdGenerator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AddServiceDialog extends AppCompatDialog {

    private static final String TAG = "AddServiceDialog";

    public interface OnServiceAddedListener {
        void onServiceAdded(HotelServiceItem service);
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

    // ✅ Managers para Firebase y AWS
    private FirebaseServiceManager firebaseServiceManager;
    private AwsFileManager awsFileManager;
    private UniqueIdGenerator idGenerator;
    private List<String> uploadedPhotoUrls;

    // Tipos de servicio disponibles
    private final String[] serviceTypes = {
            "Incluido",
            "Pagado",
            "Condicional"
    };

    public AddServiceDialog(Context context, ActivityResultLauncher<Intent> photoLauncher, OnServiceAddedListener listener) {
        super(context, R.style.DialogTheme);
        this.context = context;
        this.listener = listener;
        this.photoPickerLauncher = photoLauncher;
        this.servicePhotos = new ArrayList<>();
        this.uploadedPhotoUrls = new ArrayList<>();

        // ✅ Inicializar managers
        this.firebaseServiceManager = FirebaseServiceManager.getInstance(context);
        this.awsFileManager = new AwsFileManager(context);
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

        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupServiceTypeDropdown();
        updateIconDisplay();
        updatePhotoCount();
    }

    private void initViews() {
        etServiceName = findViewById(R.id.etServiceName);
        etServiceDescription = findViewById(R.id.etServiceDescription);
        etServiceType = findViewById(R.id.etServiceType);
        etServicePrice = findViewById(R.id.etServicePrice);
        etConditionalAmount = findViewById(R.id.etConditionalAmount);

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
    }

    private void setupRecyclerView() {
        photosAdapter = new ServicePhotosAdapter(servicePhotos, position -> {
            servicePhotos.remove(position);
            photosAdapter.notifyItemRemoved(position);
            updatePhotoCount();
            updatePhotosVisibility();
        });

        rvServicePhotos.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        rvServicePhotos.setAdapter(photosAdapter);
    }

    private void setupServiceTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, serviceTypes);
        etServiceType.setAdapter(adapter);

        etServiceType.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = serviceTypes[position];
            updateFieldsVisibilityByType(selectedType);
        });
    }

    private void updateFieldsVisibilityByType(String serviceType) {
        switch (serviceType) {
            case "Incluido":
                tilServicePrice.setVisibility(View.GONE);
                tilConditionalAmount.setVisibility(View.GONE);
                break;
            case "Pagado":
                tilServicePrice.setVisibility(View.VISIBLE);
                tilConditionalAmount.setVisibility(View.GONE);
                break;
            case "Condicional":
                tilServicePrice.setVisibility(View.GONE);
                tilConditionalAmount.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setupClickListeners() {
        layoutIconPreview.setOnClickListener(v -> showIconSelectorDialog());

        cardAddPhoto.setOnClickListener(v -> {
            if (servicePhotos.size() >= 3) {
                Toast.makeText(context, "⚠️ Máximo 3 fotos permitidas", Toast.LENGTH_SHORT).show();
                return;
            }
            openPhotoSelector();
        });

        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveService());
    }

    private void showIconSelectorDialog() {
        IconSelectorDialog iconDialog = new IconSelectorDialog(context, selectedIconKey, new IconSelectorDialog.OnIconSelectedListener() {
            @Override
            public void onIconSelected(String iconKey, String iconName) {
                selectedIconKey = iconKey;
                updateIconDisplay();
            }
        });
        iconDialog.show();
    }

    private void updateIconDisplay() {
        int iconResource = IconHelper.getIconResource(selectedIconKey);
        ivSelectedIcon.setImageResource(iconResource);

        String iconName = IconHelper.getIconName(selectedIconKey);
        tvSelectedIconName.setText(iconName);
    }

    private void openPhotoSelector() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        photoPickerLauncher.launch(Intent.createChooser(intent, "Seleccionar fotos"));
    }

    public void handlePhotoResult(Intent data) {
        if (data.getClipData() != null) {
            // Múltiples fotos seleccionadas
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count && servicePhotos.size() < 3; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                servicePhotos.add(imageUri);
            }
        } else if (data.getData() != null) {
            // Una sola foto seleccionada
            if (servicePhotos.size() < 3) {
                servicePhotos.add(data.getData());
            }
        }

        photosAdapter.notifyDataSetChanged();
        updatePhotoCount();
        updatePhotosVisibility();

        if (servicePhotos.size() >= 3) {
            Toast.makeText(context, "✅ Máximo de fotos alcanzado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "📷 Foto agregada (" + servicePhotos.size() + "/3)", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePhotoCount() {
        tvPhotoCount.setText(servicePhotos.size() + " / 3 fotos");
    }

    private void updatePhotosVisibility() {
        if (servicePhotos.isEmpty()) {
            rvServicePhotos.setVisibility(View.GONE);
        } else {
            rvServicePhotos.setVisibility(View.VISIBLE);
        }
    }

    // ========== ✅ MÉTODO PRINCIPAL PARA GUARDAR SERVICIO ==========
    private void saveService() {
        String name = etServiceName.getText().toString().trim();
        String description = etServiceDescription.getText().toString().trim();
        String typeStr = etServiceType.getText().toString().trim();

        // Validaciones básicas
        if (name.isEmpty()) {
            Toast.makeText(context, "⚠️ Ingresa el nombre del servicio", Toast.LENGTH_SHORT).show();
            etServiceName.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(context, "⚠️ Ingresa la descripción del servicio", Toast.LENGTH_SHORT).show();
            etServiceDescription.requestFocus();
            return;
        }

        if (typeStr.isEmpty()) {
            Toast.makeText(context, "⚠️ Selecciona el tipo de servicio", Toast.LENGTH_SHORT).show();
            etServiceType.requestFocus();
            return;
        }

        // Determinar tipo de servicio y validar campos correspondientes
        HotelServiceItem.ServiceType serviceType;
        double price = 0.0;
        double conditionalAmount = 0.0;

        switch (typeStr) {
            case "Incluido":
                serviceType = HotelServiceItem.ServiceType.INCLUDED;
                break;
            case "Pagado":
                serviceType = HotelServiceItem.ServiceType.PAID;
                String priceStr = etServicePrice.getText().toString().trim();
                if (priceStr.isEmpty()) {
                    Toast.makeText(context, "⚠️ Ingresa el precio del servicio", Toast.LENGTH_SHORT).show();
                    etServicePrice.requestFocus();
                    return;
                }
                try {
                    price = Double.parseDouble(priceStr);
                    if (price <= 0) {
                        Toast.makeText(context, "⚠️ El precio debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                        etServicePrice.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "⚠️ Ingresa un precio válido", Toast.LENGTH_SHORT).show();
                    etServicePrice.requestFocus();
                    return;
                }
                break;
            case "Condicional":
                serviceType = HotelServiceItem.ServiceType.CONDITIONAL;
                String amountStr = etConditionalAmount.getText().toString().trim();
                if (amountStr.isEmpty()) {
                    Toast.makeText(context, "⚠️ Ingresa el monto mínimo para activar", Toast.LENGTH_SHORT).show();
                    etConditionalAmount.requestFocus();
                    return;
                }
                try {
                    conditionalAmount = Double.parseDouble(amountStr);
                    if (conditionalAmount <= 0) {
                        Toast.makeText(context, "⚠️ El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                        etConditionalAmount.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "⚠️ Ingresa un monto válido", Toast.LENGTH_SHORT).show();
                    etConditionalAmount.requestFocus();
                    return;
                }
                break;
            default:
                Toast.makeText(context, "⚠️ Tipo de servicio no válido", Toast.LENGTH_SHORT).show();
                return;
        }

        // ✅ Deshabilitar botón mientras se guarda
        btnSave.setEnabled(false);
        btnSave.setText("Guardando...");

        Log.d(TAG, "🔧 Creando servicio: " + name + " (Tipo: " + typeStr + ")");

        if (!servicePhotos.isEmpty()) {
            // ✅ Subir fotos primero, luego crear servicio
            uploadPhotosToAws(name, description, serviceType, price, conditionalAmount);
        } else {
            // ✅ Sin fotos, crear servicio directamente
            createServiceInFirebase(name, description, serviceType, price, conditionalAmount, new ArrayList<>());
        }
    }

    // ✅ SUBIR FOTOS A AWS
    private void uploadPhotosToAws(String serviceName, String serviceDescription,
                                   HotelServiceItem.ServiceType serviceType, double price, double conditionalAmount) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showError("Usuario no autenticado");
            return;
        }

        String userId = currentUser.getUid();
        String folder = "hotel_services/" + userId;

        uploadedPhotoUrls.clear();
        AtomicInteger uploadedCount = new AtomicInteger(0);
        AtomicInteger totalUploads = new AtomicInteger(servicePhotos.size());

        Log.d(TAG, "🔄 Subiendo " + servicePhotos.size() + " fotos a AWS para servicio: " + serviceName);

        for (int i = 0; i < servicePhotos.size(); i++) {
            Uri photoUri = servicePhotos.get(i);
            String fileName = idGenerator.generateUniqueFileName("service", serviceName + "_" + i + ".jpg");

            awsFileManager.uploadFile(photoUri, userId, folder, new AwsFileManager.UploadCallback() {
                @Override
                public void onSuccess(AwsFileManager.AwsFileInfo fileInfo) {
                    synchronized (uploadedPhotoUrls) {
                        uploadedPhotoUrls.add(fileInfo.fileUrl);
                        int completed = uploadedCount.incrementAndGet();

                        Log.d(TAG, "✅ Foto subida " + completed + "/" + totalUploads.get() + " - URL: " + fileInfo.fileUrl);

                        // Ejecutar en UI Thread
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            int progress = (completed * 100) / totalUploads.get();
                            btnSave.setText("Subiendo fotos... " + progress + "%");

                            if (completed == totalUploads.get()) {
                                // ✅ Todas las fotos subidas, crear servicio
                                Log.d(TAG, "✅ Todas las fotos subidas. Creando servicio con " + uploadedPhotoUrls.size() + " URLs");
                                createServiceInFirebase(serviceName, serviceDescription, serviceType, price, conditionalAmount, uploadedPhotoUrls);
                            }
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error subiendo foto: " + error);
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        showError("Error subiendo fotos: " + error);
                    });
                }

                @Override
                public void onProgress(int percentage) {
                    // Progreso individual
                }
            });
        }
    }

    // ✅ CREAR SERVICIO EN FIREBASE
    private void createServiceInFirebase(String name, String description, HotelServiceItem.ServiceType serviceType,
                                         double price, double conditionalAmount, List<String> photoUrls) {
        Log.d(TAG, "🔧 Creando servicio en Firebase: " + name + " con " + photoUrls.size() + " fotos");

        // ✅ Crear modelo de Firebase
        String firebaseServiceType = convertServiceTypeToFirebase(serviceType);
        HotelServiceModel firebaseService = new HotelServiceModel(name, description, selectedIconKey, firebaseServiceType);
        firebaseService.setPrice(price);
        firebaseService.setConditionalAmount(conditionalAmount);
        firebaseService.setPhotoUrls(photoUrls); // ✅ URLs de AWS
        firebaseService.setActive(true);

        // ✅ Crear en Firebase (sin URIs adicionales porque ya están subidas)
        firebaseServiceManager.createService(firebaseService, null, new FirebaseServiceManager.ServiceCallback() {
            @Override
            public void onSuccess(HotelServiceModel createdService) {
                Log.d(TAG, "✅ Servicio creado exitosamente en Firebase: " + createdService.getId());

                ((android.app.Activity) context).runOnUiThread(() -> {
                    // ✅ Crear HotelServiceItem para callback
                    List<Uri> photoUris = new ArrayList<>();
                    for (String url : photoUrls) {
                        photoUris.add(Uri.parse(url));
                    }

                    HotelServiceItem serviceItem = new HotelServiceItem(
                            name, description, price, selectedIconKey, serviceType, photoUris, conditionalAmount
                    );
                    serviceItem.setFirebaseId(createdService.getId());

                    // ✅ Notificar al listener
                    if (listener != null) {
                        listener.onServiceAdded(serviceItem);
                    }

                    String photoText = photoUrls.isEmpty() ? "" : " con " + photoUrls.size() + " foto(s)";
                    Toast.makeText(context, "✅ Servicio '" + name + "' creado exitosamente" + photoText, Toast.LENGTH_LONG).show();
                    dismiss();
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error creando servicio en Firebase: " + error);
                ((android.app.Activity) context).runOnUiThread(() -> {
                    showError("Error creando servicio: " + error);
                });
            }
        });
    }

    // ✅ CONVERTIR TIPO DE SERVICIO
    private String convertServiceTypeToFirebase(HotelServiceItem.ServiceType serviceType) {
        switch (serviceType) {
            case BASIC: return "basic";
            case INCLUDED: return "included";
            case PAID: return "paid";
            case CONDITIONAL: return "conditional";
            default: return "included";
        }
    }

    private void showError(String error) {
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).runOnUiThread(() -> {
                btnSave.setEnabled(true);
                btnSave.setText("✅ Guardar Servicio");
                Toast.makeText(context, error, Toast.LENGTH_LONG).show();
            });
        }
    }
}