package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.ServicePhotosAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.IconSelectorDialog;
import com.example.proyecto_final_hoteleros.adminhotel.model.BasicService;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;
import com.example.proyecto_final_hoteleros.utils.AwsFileManager;
import com.example.proyecto_final_hoteleros.utils.UniqueIdGenerator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BasicServiceDialog extends Dialog {
    private TextInputEditText etServiceName, etServiceDescription;
    private ImageView ivSelectedIcon;
    private TextView tvSelectedIconName, tvPhotoCount;
    private MaterialButton btnAddPhoto, btnAddMorePhotos, btnSave, btnCancel;
    private RecyclerView rvServicePhotos;
    private LinearLayout layoutIconPreview, layoutPhotoPlaceholder, layoutPhotosContainer;
    private AwsFileManager awsFileManager;
    private UniqueIdGenerator idGenerator;
    private List<String> uploadedPhotoUrls; // ✅ URLs de fotos ya subidas a AWS
    private Context context;
    private String selectedIconKey = "ic_service_default";
    private List<Uri> servicePhotos; // ✅ URIs locales para mostrar en UI
    private ServicePhotosAdapter photosAdapter;
    private ActivityResultLauncher<Intent> photoPickerLauncher;
    private OnServiceAddedListener listener;

    public interface OnServiceAddedListener {
        void onServiceAdded(BasicService service);
    }

    public BasicServiceDialog(Context context, ActivityResultLauncher<Intent> photoLauncher, OnServiceAddedListener listener) {
        super(context, R.style.DialogTheme);
        this.context = context;
        this.listener = listener;
        this.photoPickerLauncher = photoLauncher;
        this.servicePhotos = new ArrayList<>();
        this.awsFileManager = new AwsFileManager(context);
        this.idGenerator = UniqueIdGenerator.getInstance(context);
        this.uploadedPhotoUrls = new ArrayList<>(); // ✅ Inicializar lista de URLs
        setupDialog();
    }

    private void setupDialog() {
        setContentView(R.layout.admin_hotel_dialog_add_basic_service);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        if (getWindow() != null) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

            params.width = (int) (displayMetrics.widthPixels * 0.9);
            int maxHeight = Math.min(
                    (int) (displayMetrics.heightPixels * 0.85),
                    (int) (700 * displayMetrics.density)
            );
            params.height = maxHeight;

            getWindow().setAttributes(params);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        initViews();
        setupRecyclerView();
        setupClickListeners();
        updateIconDisplay();
        updatePhotoCount();
        updatePhotosVisibility();
    }

    private void initViews() {
        etServiceName = findViewById(R.id.etServiceName);
        etServiceDescription = findViewById(R.id.etServiceDescription);
        ivSelectedIcon = findViewById(R.id.ivSelectedIcon);
        tvSelectedIconName = findViewById(R.id.tvSelectedIconName);
        tvPhotoCount = findViewById(R.id.tvPhotoCount);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnAddMorePhotos = findViewById(R.id.btnAddMorePhotos);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        rvServicePhotos = findViewById(R.id.rvServicePhotos);
        layoutIconPreview = findViewById(R.id.layoutIconPreview);
        layoutPhotoPlaceholder = findViewById(R.id.layoutPhotoPlaceholder);
        layoutPhotosContainer = findViewById(R.id.layoutPhotosContainer);
    }

    private void setupRecyclerView() {
        photosAdapter = new ServicePhotosAdapter(servicePhotos, this::removePhoto);
        rvServicePhotos.setLayoutManager(new GridLayoutManager(context, 3));
        rvServicePhotos.setAdapter(photosAdapter);
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveService());

        layoutIconPreview.setOnClickListener(v -> showIconSelector());

        btnAddPhoto.setOnClickListener(v -> selectPhoto());
        btnAddMorePhotos.setOnClickListener(v -> selectPhoto());
        layoutPhotoPlaceholder.setOnClickListener(v -> selectPhoto());

        // Validación en tiempo real
        etServiceName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etServiceDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void validateForm() {
        boolean isValid = !etServiceName.getText().toString().trim().isEmpty() &&
                !etServiceDescription.getText().toString().trim().isEmpty();
        btnSave.setEnabled(isValid);
        btnSave.setAlpha(isValid ? 1.0f : 0.5f);
    }

    private void showIconSelector() {
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
        tvSelectedIconName.setText(IconHelper.getIconName(selectedIconKey));
    }

    private void selectPhoto() {
        if (servicePhotos.size() >= 3) {
            Toast.makeText(context, "Máximo 3 fotos permitidas", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        photoPickerLauncher.launch(intent);
    }

    public void addPhoto(Uri photoUri) {
        if (servicePhotos.size() < 3) {
            servicePhotos.add(photoUri);
            photosAdapter.notifyItemInserted(servicePhotos.size() - 1);
            updatePhotoCount();
            updatePhotosVisibility();
            Toast.makeText(context, "Foto agregada (" + servicePhotos.size() + "/3)", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePhotoCount() {
        tvPhotoCount.setText(servicePhotos.size() + "/3 fotos");
    }

    private void updatePhotosVisibility() {
        boolean hasPhotos = !servicePhotos.isEmpty();

        if (rvServicePhotos != null) {
            rvServicePhotos.setVisibility(hasPhotos ? View.VISIBLE : View.GONE);
        }

        if (layoutPhotoPlaceholder != null) {
            layoutPhotoPlaceholder.setVisibility(hasPhotos ? View.GONE : View.VISIBLE);
        }

        if (btnAddPhoto != null && btnAddMorePhotos != null) {
            if (servicePhotos.size() == 0) {
                btnAddPhoto.setVisibility(View.VISIBLE);
                btnAddMorePhotos.setVisibility(View.GONE);
            } else if (servicePhotos.size() < 3) {
                btnAddPhoto.setVisibility(View.GONE);
                btnAddMorePhotos.setVisibility(View.VISIBLE);
                btnAddMorePhotos.setEnabled(true);
            } else {
                btnAddPhoto.setVisibility(View.GONE);
                btnAddMorePhotos.setVisibility(View.VISIBLE);
                btnAddMorePhotos.setEnabled(false);
                btnAddMorePhotos.setText("Máximo alcanzado");
            }
        }
    }

    private void removePhoto(int position) {
        if (position >= 0 && position < servicePhotos.size()) {
            servicePhotos.remove(position);
            // ✅ También remover de URLs subidas si existe
            if (position < uploadedPhotoUrls.size()) {
                uploadedPhotoUrls.remove(position);
            }
            photosAdapter.notifyItemRemoved(position);
            updatePhotoCount();
            updatePhotosVisibility();
            Toast.makeText(context, "Foto eliminada", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveService() {
        String name = etServiceName.getText().toString().trim();
        String description = etServiceDescription.getText().toString().trim();

        // Validaciones
        if (name.isEmpty()) {
            etServiceName.setError("El nombre es obligatorio");
            etServiceName.requestFocus();
            return;
        }

        if (name.length() < 3) {
            etServiceName.setError("Mínimo 3 caracteres");
            etServiceName.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etServiceDescription.setError("La descripción es obligatoria");
            etServiceDescription.requestFocus();
            return;
        }

        if (description.length() < 10) {
            etServiceDescription.setError("Mínimo 10 caracteres");
            etServiceDescription.requestFocus();
            return;
        }

        // Deshabilitar botón de guardar
        btnSave.setEnabled(false);
        btnSave.setText("Guardando...");

        if (!servicePhotos.isEmpty()) {
            // ✅ Subir fotos primero a AWS, luego crear servicio con URLs
            uploadPhotosToAws(name, description);
        } else {
            // Sin fotos, crear servicio directamente
            createBasicService(name, description, new ArrayList<>());
        }
    }

    private void uploadPhotosToAws(String serviceName, String serviceDescription) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showError("Usuario no autenticado");
            return;
        }

        String userId = currentUser.getUid();
        String folder = "hotel_services/" + userId;

        uploadedPhotoUrls.clear(); // ✅ Limpiar URLs anteriores
        AtomicInteger uploadedCount = new AtomicInteger(0);
        AtomicInteger totalUploads = new AtomicInteger(servicePhotos.size());

        Log.d("BasicServiceDialog", "🔄 Iniciando subida de " + servicePhotos.size() + " fotos a AWS");

        for (int i = 0; i < servicePhotos.size(); i++) {
            Uri photoUri = servicePhotos.get(i);
            String fileName = idGenerator.generateUniqueFileName("service", serviceName + "_" + i + ".jpg");

            awsFileManager.uploadFile(photoUri, userId, folder, new AwsFileManager.UploadCallback() {
                @Override
                public void onSuccess(AwsFileManager.AwsFileInfo fileInfo) {
                    synchronized (uploadedPhotoUrls) {
                        uploadedPhotoUrls.add(fileInfo.fileUrl); // ✅ Guardar URL de AWS
                        int completed = uploadedCount.incrementAndGet();

                        Log.d("BasicServiceDialog", "✅ Foto subida " + completed + "/" + totalUploads.get() +
                                " - URL: " + fileInfo.fileUrl);

                        // ✅ EJECUTAR EN UI THREAD
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            int progress = (completed * 100) / totalUploads.get();
                            btnSave.setText("Subiendo fotos... " + progress + "%");

                            if (completed == totalUploads.get()) {
                                // ✅ Todas las fotos subidas, crear servicio con URLs
                                Log.d("BasicServiceDialog", "✅ Todas las fotos subidas. Creando servicio con " +
                                        uploadedPhotoUrls.size() + " URLs");
                                createBasicService(serviceName, serviceDescription, uploadedPhotoUrls);
                            }
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e("BasicServiceDialog", "❌ Error subiendo foto: " + error);
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

    private void createBasicService(String name, String description, List<String> photoUrls) {
        Log.d("BasicServiceDialog", "🔧 Creando servicio básico: " + name + " con " + photoUrls.size() + " fotos");

        // ✅ Crear servicio con URLs de fotos de AWS
        BasicService service = new BasicService(name, description, selectedIconKey);
        service.setPhotos(photoUrls); // ✅ Establecer URLs directamente

        if (listener != null) {
            listener.onServiceAdded(service);
        }

        String photoText = photoUrls.isEmpty() ? "" : " con " + photoUrls.size() + " foto(s)";
        Toast.makeText(context, "✅ Servicio '" + name + "' agregado" + photoText, Toast.LENGTH_LONG).show();
        dismiss();
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