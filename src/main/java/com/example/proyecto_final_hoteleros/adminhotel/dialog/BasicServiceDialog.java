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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class BasicServiceDialog extends Dialog {
    private TextInputEditText etServiceName, etServiceDescription;
    private ImageView ivSelectedIcon;
    private TextView tvSelectedIconName, tvPhotoCount;
    private MaterialButton btnAddPhoto, btnSave, btnCancel;
    private RecyclerView rvServicePhotos;
    private LinearLayout layoutIconPreview;

    private Context context;
    private String selectedIconKey = "ic_service_default";
    private List<Uri> servicePhotos;
    private ServicePhotosAdapter photosAdapter;
    private ActivityResultLauncher<Intent> photoPickerLauncher;
    private OnServiceAddedListener listener;

    public interface OnServiceAddedListener {
        void onServiceAdded(BasicService service);
    }

    // CONSTRUCTOR MODIFICADO - Recibe el launcher desde el Fragment/Activity
    public BasicServiceDialog(Context context, ActivityResultLauncher<Intent> photoLauncher, OnServiceAddedListener listener) {
        super(context, R.style.DialogTheme);
        this.context = context;
        this.listener = listener;
        this.photoPickerLauncher = photoLauncher; // Usar el launcher pasado desde fuera
        this.servicePhotos = new ArrayList<>();
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
    }

    private void initViews() {
        etServiceName = findViewById(R.id.etServiceName);
        etServiceDescription = findViewById(R.id.etServiceDescription);
        ivSelectedIcon = findViewById(R.id.ivSelectedIcon);
        tvSelectedIconName = findViewById(R.id.tvSelectedIconName);
        tvPhotoCount = findViewById(R.id.tvPhotoCount);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        rvServicePhotos = findViewById(R.id.rvServicePhotos);
        layoutIconPreview = findViewById(R.id.layoutIconPreview);
    }

    private void setupRecyclerView() {
        photosAdapter = new ServicePhotosAdapter(servicePhotos, this::removePhoto);
        rvServicePhotos.setLayoutManager(new GridLayoutManager(context, 3));
        rvServicePhotos.setAdapter(photosAdapter);
        updatePhotosVisibility();
    }

    private void setupClickListeners() {
        // Botón seleccionar icono
        layoutIconPreview.setOnClickListener(v -> openIconSelector());

        // Botón agregar foto - usar el launcher externo
        btnAddPhoto.setOnClickListener(v -> {
            if (servicePhotos.size() < 3) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                photoPickerLauncher.launch(intent);
            } else {
                Toast.makeText(context, "Máximo 3 fotos permitidas", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveService());
    }

    // Método para agregar foto desde el launcher externo
    public void addPhoto(Uri photoUri) {
        if (servicePhotos.size() < 3) {
            servicePhotos.add(photoUri);
            photosAdapter.notifyItemInserted(servicePhotos.size() - 1);
            updatePhotoCount();
            updatePhotosVisibility();
            Toast.makeText(context, "Foto agregada (" + servicePhotos.size() + "/3)", Toast.LENGTH_SHORT).show();
        }
    }

    private void openIconSelector() {
        IconSelectorDialog iconDialog = new IconSelectorDialog(context, selectedIconKey,
                (iconKey, iconName) -> {
                    selectedIconKey = iconKey;
                    updateIconDisplay();
                    Toast.makeText(context, "✅ Icono seleccionado: " + iconName, Toast.LENGTH_SHORT).show();
                });
        iconDialog.show();
    }

    private void updateIconDisplay() {
        try {
            int iconResource = IconHelper.getIconResource(selectedIconKey);
            ivSelectedIcon.setImageResource(iconResource);

            String iconName = IconHelper.getIconName(selectedIconKey);
            tvSelectedIconName.setText(iconName);
            tvSelectedIconName.setTextColor(ContextCompat.getColor(context, R.color.text_primary));

        } catch (Exception e) {
            ivSelectedIcon.setImageResource(R.drawable.ic_service_default);
            tvSelectedIconName.setText("Icono por defecto");
        }
    }

    private void updatePhotoCount() {
        tvPhotoCount.setText(servicePhotos.size() + "/3");
        btnAddPhoto.setEnabled(servicePhotos.size() < 3);
        btnAddPhoto.setAlpha(servicePhotos.size() < 3 ? 1.0f : 0.6f);
    }

    private void updatePhotosVisibility() {
        rvServicePhotos.setVisibility(servicePhotos.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void removePhoto(int position) {
        servicePhotos.remove(position);
        photosAdapter.notifyItemRemoved(position);
        updatePhotoCount();
        updatePhotosVisibility();
        Toast.makeText(context, "Foto eliminada", Toast.LENGTH_SHORT).show();
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

        // Crear servicio con fotos
        BasicService service = new BasicService(name, description, selectedIconKey);
        service.setPhotos(new ArrayList<>(servicePhotos)); // Agregar fotos al servicio

        if (listener != null) {
            listener.onServiceAdded(service);
        }

        String photoText = servicePhotos.isEmpty() ? "" : " con " + servicePhotos.size() + " foto(s)";
        Toast.makeText(context, "✅ Servicio '" + name + "' agregado" + photoText, Toast.LENGTH_LONG).show();
        dismiss();
    }
}