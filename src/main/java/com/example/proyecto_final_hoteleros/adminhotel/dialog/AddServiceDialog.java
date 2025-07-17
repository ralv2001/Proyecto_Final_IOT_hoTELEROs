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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.ServicePhotosAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.IconSelectorDialog;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceItem;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceModel;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseServiceManager;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;
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

    // ✅ CORREGIDO: Solo FirebaseServiceManager y UniqueIdGenerator
    private FirebaseServiceManager firebaseServiceManager;
    private UniqueIdGenerator idGenerator;

    // ✅ Solo 3 tipos - Taxi condicional tiene su propia sección
    private final String[] serviceTypes = {
            "Básico",
            "Incluido",
            "Pagado"
    };

    public AddServiceDialog(Context context, ActivityResultLauncher<Intent> photoLauncher, OnServiceAddedListener listener) {
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

        // Configurar campos iniciales
        updateFieldsVisibility("Incluido");
    }

    private void updateFieldsVisibility(String serviceType) {
        Log.d(TAG, "🎛️ Actualizando visibilidad para tipo: " + serviceType);

        switch (serviceType) {
            case "Básico":
                tilServicePrice.setVisibility(View.GONE);
                tilConditionalAmount.setVisibility(View.GONE);
                etServicePrice.setText("0");
                break;

            case "Incluido":
                tilServicePrice.setVisibility(View.GONE);
                tilConditionalAmount.setVisibility(View.GONE);
                etServicePrice.setText("0");
                break;

            case "Pagado":
                tilServicePrice.setVisibility(View.VISIBLE);
                tilConditionalAmount.setVisibility(View.GONE);
                if (etServicePrice.getText().toString().trim().equals("0")) {
                    etServicePrice.setText("");
                }
                break;
        }
    }

    private void setupPhotoRecyclerView() {
        // ✅ Usar patrón simple que funciona
        photosAdapter = new ServicePhotosAdapter(servicePhotos, this::removePhoto);
        rvServicePhotos.setLayoutManager(new GridLayoutManager(context, 3));
        rvServicePhotos.setAdapter(photosAdapter);
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveService());
        layoutIconPreview.setOnClickListener(v -> openIconSelector());

        cardAddPhoto.setOnClickListener(v -> {
            if (servicePhotos.size() < 5) {
                selectPhoto();
            } else {
                Toast.makeText(context, "Máximo 5 fotos permitidas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openIconSelector() {
        IconSelectorDialog iconDialog = new IconSelectorDialog(context, selectedIconKey, new IconSelectorDialog.OnIconSelectedListener() {
            @Override
            public void onIconSelected(String iconKey, String iconName) {
                selectedIconKey = iconKey;
                updateIconPreview();
                Log.d(TAG, "🎨 Icono seleccionado: " + iconName + " (" + iconKey + ")");
            }
        });
        iconDialog.show();
    }

    private void updateIconPreview() {
        if (ivSelectedIcon != null && tvSelectedIconName != null) {
            int iconResource = IconHelper.getIconResource(selectedIconKey);
            ivSelectedIcon.setImageResource(iconResource);
            tvSelectedIconName.setText(IconHelper.getIconName(selectedIconKey));
        }
    }

    private void selectPhoto() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        photoPickerLauncher.launch(Intent.createChooser(intent, "Seleccionar fotos"));
    }

    // ✅ MÉTODO PÚBLICO PARA MANEJAR RESULTADO DE FOTOS
    public void handlePhotoResult(Intent data) {
        if (data != null) {
            if (data.getClipData() != null) {
                // Múltiples fotos
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count && servicePhotos.size() < 5; i++) {
                    Uri photoUri = data.getClipData().getItemAt(i).getUri();
                    addPhoto(photoUri);
                }
            } else if (data.getData() != null) {
                // Una sola foto
                if (servicePhotos.size() < 5) {
                    addPhoto(data.getData());
                }
            }

            photosAdapter.notifyDataSetChanged();
            updatePhotosVisibility();
            Log.d(TAG, "📷 Fotos agregadas. Total: " + servicePhotos.size());
        }
    }

    private void addPhoto(Uri photoUri) {
        if (servicePhotos.size() < 5 && photoUri != null) {
            servicePhotos.add(photoUri);

            if (photosAdapter != null) {
                photosAdapter.notifyItemInserted(servicePhotos.size() - 1);
            }

            updatePhotosVisibility();
            Log.d(TAG, "📷 Foto agregada: " + photoUri.toString() + " (Total: " + servicePhotos.size() + ")");
        }
    }

    private void removePhoto(int position) {
        if (position >= 0 && position < servicePhotos.size()) {
            servicePhotos.remove(position);
            photosAdapter.notifyItemRemoved(position);
            updatePhotosVisibility();
            Toast.makeText(context, "📷 Foto eliminada", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "📷 Foto eliminada. Total: " + servicePhotos.size());
        }
    }

    private void updatePhotosVisibility() {
        if (tvPhotoCount != null) {
            tvPhotoCount.setText(servicePhotos.size() + "/5 fotos");
        }

        if (rvServicePhotos != null) {
            rvServicePhotos.setVisibility(servicePhotos.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void saveService() {
        String name = etServiceName.getText().toString().trim();
        String description = etServiceDescription.getText().toString().trim();
        String serviceTypeStr = etServiceType.getText().toString().trim();
        String priceStr = etServicePrice.getText().toString().trim();

        // ✅ Validaciones
        if (name.isEmpty()) {
            etServiceName.setError("Ingresa el nombre del servicio");
            etServiceName.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etServiceDescription.setError("Ingresa la descripción");
            etServiceDescription.requestFocus();
            return;
        }

        if (serviceTypeStr.isEmpty()) {
            Toast.makeText(context, "Selecciona el tipo de servicio", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Validar precio solo para servicios pagados
        double price = 0.0;
        if (serviceTypeStr.equals("Pagado")) {
            if (priceStr.isEmpty()) {
                etServicePrice.setError("Ingresa el precio");
                etServicePrice.requestFocus();
                return;
            }
            try {
                price = Double.parseDouble(priceStr);
                if (price < 0) {
                    etServicePrice.setError("El precio no puede ser negativo");
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

        // ✅ CORREGIDO: Crear servicio exactamente como BasicServiceDialog
        createServiceInFirebase(name, description, price, serviceTypeStr);
    }

    // ✅ COMPLETAMENTE REDISEÑADO: Seguir exactamente el patrón de BasicServiceDialog
    private void createServiceInFirebase(String name, String description, double price, String serviceTypeStr) {
        Log.d(TAG, "🚀 Creando servicio en Firebase: " + name + " (Tipo: " + serviceTypeStr + ")");

        // ✅ CORREGIDO: Crear servicio exactamente como BasicServiceDialog
        HotelServiceModel service = new HotelServiceModel();
        // ✅ NO establecer ID - Firebase lo generará automáticamente
        service.setName(name);
        service.setDescription(description);
        service.setIconKey(selectedIconKey);
        service.setServiceType(convertCategoryToType(serviceTypeStr));
        service.setPrice(price);
        service.setConditionalAmount(0.0); // No hay condicionales desde este dialog
        service.setActive(true);
        service.setCreatedAt(new Date()); // ✅ Firebase manejará el timestamp

        // Obtener usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            service.setHotelAdminId(currentUser.getUid());
        }

        // ✅ CORREGIDO: Usar el MISMO patrón que BasicServiceDialog
        if (firebaseServiceManager != null) {
            firebaseServiceManager.createService(service, servicePhotos, new FirebaseServiceManager.ServiceCallback() {
                @Override
                public void onSuccess(HotelServiceModel savedService) {
                    Log.d(TAG, "✅ Servicio creado exitosamente: " + savedService.getName());

                    // ✅ Crear HotelServiceItem para el listener
                    HotelServiceItem.ServiceType serviceType = convertStringToServiceType(serviceTypeStr);
                    HotelServiceItem serviceItem = new HotelServiceItem(
                            savedService.getName(),
                            savedService.getDescription(),
                            savedService.getPrice(),
                            savedService.getIconKey(),
                            serviceType,
                            servicePhotos,
                            0.0 // No conditional amount
                    );
                    serviceItem.setFirebaseId(savedService.getId());

                    // ✅ Notificar al listener
                    if (listener != null) {
                        listener.onServiceAdded(serviceItem);
                    }

                    String photoText = servicePhotos.isEmpty() ? "" : " con " + servicePhotos.size() + " foto(s)";
                    Toast.makeText(context, "✅ Servicio '" + name + "' creado exitosamente" + photoText, Toast.LENGTH_LONG).show();
                    dismiss();
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error creando servicio en Firebase: " + error);

                    // ✅ Reactivar botón
                    btnSave.setEnabled(true);
                    btnSave.setText("✅ Guardar Servicio");

                    Toast.makeText(context, "❌ Error creando servicio: " + error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Log.e(TAG, "❌ FirebaseServiceManager es null");

            // ✅ Reactivar botón
            btnSave.setEnabled(true);
            btnSave.setText("✅ Guardar Servicio");

            Toast.makeText(context, "❌ Error: No se pudo conectar con Firebase", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ Conversión de string a ServiceType
    private HotelServiceItem.ServiceType convertStringToServiceType(String serviceTypeStr) {
        switch (serviceTypeStr) {
            case "Básico":
                return HotelServiceItem.ServiceType.BASIC;
            case "Incluido":
                return HotelServiceItem.ServiceType.INCLUDED;
            case "Pagado":
                return HotelServiceItem.ServiceType.PAID;
            default:
                return HotelServiceItem.ServiceType.INCLUDED;
        }
    }

    // ✅ Conversión para Firebase
    private String convertCategoryToType(String category) {
        switch (category) {
            case "Básico":
                return "basic";
            case "Incluido":
                return "included";
            case "Pagado":
                return "paid";
            default:
                return "included";
        }
    }
}