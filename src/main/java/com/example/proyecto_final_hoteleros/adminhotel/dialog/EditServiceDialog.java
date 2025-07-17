package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.ServicePhotosAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.IconSelectorDialog;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceItem;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;

public class EditServiceDialog extends AppCompatDialog {

    public interface OnServiceEditedListener {
        void onServiceEdited(HotelServiceItem service);
    }

    private Context context;
    private OnServiceEditedListener listener;
    private HotelServiceItem originalService;

    // Views
    private TextInputEditText etServiceName, etServiceDescription, etServicePrice, etConditionalAmount;
    private AutoCompleteTextView etServiceType;
    private TextInputLayout tilServicePrice, tilConditionalAmount;
    private ImageView ivSelectedIcon;
    private TextView tvSelectedIconName, tvPhotoCount;
    private LinearLayout layoutIconPreview;
    private MaterialCardView cardAddPhoto;
    private RecyclerView rvServicePhotos;
    private MaterialButton btnCancel, btnSave;

    // Data
    private String selectedIconKey;
    private List<Uri> servicePhotos;
    private ServicePhotosAdapter photosAdapter;

    // ✅ CORREGIDO: Solo 3 tipos - Taxi condicional tiene su propia sección
    private final String[] serviceTypes = {
            "Básico",
            "Incluido",
            "Pagado"
    };

    public EditServiceDialog(Context context, HotelServiceItem service, OnServiceEditedListener listener) {
        super(context, R.style.DialogTheme);
        this.context = context;
        this.listener = listener;
        this.originalService = service;
        this.selectedIconKey = service.getIconKey();
        this.servicePhotos = new ArrayList<>(service.getPhotos());
        setupDialog();
    }

    private void setupDialog() {
        setContentView(R.layout.admin_hotel_dialog_add_service);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        initViews();
        setupRecyclerView();
        setupServiceTypeDropdown();
        setupClickListeners();
        loadCurrentData();
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
        // ✅ CORREGIDO: Usar exactamente el mismo patrón que BasicServiceDialog
        photosAdapter = new ServicePhotosAdapter(servicePhotos, this::removePhoto);
        rvServicePhotos.setLayoutManager(new GridLayoutManager(context, 3));
        rvServicePhotos.setAdapter(photosAdapter);
    }

    // ✅ NUEVO: Método para remover foto (como en BasicServiceDialog)
    private void removePhoto(int position) {
        if (position >= 0 && position < servicePhotos.size()) {
            servicePhotos.remove(position);
            photosAdapter.notifyItemRemoved(position);
            updatePhotoCount();
            Toast.makeText(context, "📷 Foto eliminada", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ CORREGIDO: Configurar dropdown con todos los tipos de servicio
    private void setupServiceTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, serviceTypes);
        etServiceType.setAdapter(adapter);

        // ✅ Listener para mostrar/ocultar campos según el tipo
        etServiceType.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = serviceTypes[position];
            updateFieldsVisibility(selectedType);
        });
    }

    // ✅ NUEVO: Método para mostrar/ocultar campos según el tipo de servicio
    private void updateFieldsVisibility(String serviceType) {
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

            case "Condicional":
                tilServicePrice.setVisibility(View.VISIBLE);
                tilConditionalAmount.setVisibility(View.VISIBLE);
                if (etServicePrice.getText().toString().trim().equals("0")) {
                    etServicePrice.setText("");
                }
                break;
        }
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveChanges());

        layoutIconPreview.setOnClickListener(v -> {
            IconSelectorDialog iconDialog = new IconSelectorDialog(context, selectedIconKey, new IconSelectorDialog.OnIconSelectedListener() {
                @Override
                public void onIconSelected(String iconKey, String iconName) {
                    selectedIconKey = iconKey;
                    updateIconDisplay();
                }
            });
            iconDialog.show();
        });

        cardAddPhoto.setOnClickListener(v -> {
            if (servicePhotos.size() < 5) {
                // Lógica para agregar foto
                Toast.makeText(context, "Funcionalidad de fotos en desarrollo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Máximo 5 fotos permitidas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCurrentData() {
        etServiceName.setText(originalService.getName());
        etServiceDescription.setText(originalService.getDescription());

        // ✅ CORREGIDO: Configurar tipo de servicio correctamente
        String serviceTypeText = getServiceTypeText(originalService.getType());
        etServiceType.setText(serviceTypeText, false);

        // Configurar precio
        if (originalService.getPrice() > 0) {
            etServicePrice.setText(String.valueOf(originalService.getPrice()));
        }

        // ✅ NUEVO: Configurar monto condicional
        if (originalService.getConditionalAmount() > 0) {
            etConditionalAmount.setText(String.valueOf(originalService.getConditionalAmount()));
        }

        // Actualizar visibilidad de campos
        updateFieldsVisibility(serviceTypeText);
    }

    // ✅ CORREGIDO: Solo para los 3 tipos disponibles
    private String getServiceTypeText(HotelServiceItem.ServiceType serviceType) {
        switch (serviceType) {
            case BASIC:
                return "Básico";
            case INCLUDED:
                return "Incluido";
            case PAID:
                return "Pagado";
            case CONDITIONAL:
                // ✅ Si es condicional, mostrar como "Incluido" (no se puede editar desde aquí)
                return "Incluido";
            default:
                return "Incluido";
        }
    }

    // ✅ CORREGIDO: Solo para los 3 tipos disponibles
    private HotelServiceItem.ServiceType convertTextToServiceType(String serviceTypeText) {
        switch (serviceTypeText) {
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

    private void updateIconDisplay() {
        if (ivSelectedIcon != null && tvSelectedIconName != null) {
            int iconResource = IconHelper.getIconResource(selectedIconKey);
            ivSelectedIcon.setImageResource(iconResource);
            tvSelectedIconName.setText(IconHelper.getIconName(selectedIconKey));
        }
    }

    private void updatePhotoCount() {
        if (tvPhotoCount != null) {
            tvPhotoCount.setText(servicePhotos.size() + "/5 fotos");
        }
        if (rvServicePhotos != null) {
            rvServicePhotos.setVisibility(servicePhotos.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void saveChanges() {
        String name = etServiceName.getText().toString().trim();
        String description = etServiceDescription.getText().toString().trim();
        String serviceTypeText = etServiceType.getText().toString().trim();
        String priceStr = etServicePrice.getText().toString().trim();
        String conditionalAmountStr = etConditionalAmount.getText().toString().trim();

        // ✅ Validaciones
        if (name.isEmpty()) {
            etServiceName.setError("Ingresa el nombre del servicio");
            return;
        }

        if (description.isEmpty()) {
            etServiceDescription.setError("Ingresa la descripción");
            return;
        }

        if (serviceTypeText.isEmpty()) {
            Toast.makeText(context, "Selecciona el tipo de servicio", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Validar precio solo para servicios pagados
        double price = 0.0;
        if (serviceTypeText.equals("Pagado")) {
            if (priceStr.isEmpty()) {
                etServicePrice.setError("Ingresa el precio");
                return;
            }
            try {
                price = Double.parseDouble(priceStr);
                if (price < 0) {
                    etServicePrice.setError("El precio no puede ser negativo");
                    return;
                }
            } catch (NumberFormatException e) {
                etServicePrice.setError("Precio inválido");
                return;
            }
        }

        // ✅ No hay servicios condicionales desde el dialog (solo taxi en su sección)
        double conditionalAmount = 0.0;

        // ✅ Crear servicio editado
        HotelServiceItem.ServiceType serviceType = convertTextToServiceType(serviceTypeText);

        HotelServiceItem editedService = new HotelServiceItem(
                name,
                description,
                price,
                selectedIconKey,
                serviceType,
                servicePhotos,
                conditionalAmount
        );
        editedService.setFirebaseId(originalService.getFirebaseId());

        // ✅ Notificar al listener
        if (listener != null) {
            listener.onServiceEdited(editedService);
        }

        Toast.makeText(context, "✅ Servicio actualizado: " + name, Toast.LENGTH_SHORT).show();
        dismiss();
    }
}