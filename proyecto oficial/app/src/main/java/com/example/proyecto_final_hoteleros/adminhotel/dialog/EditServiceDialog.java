package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.ViewGroup;
import android.view.Window;
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
    private ActivityResultLauncher<Intent> photoPickerLauncher;

    // Service types
    private final String[] serviceTypes = {
            "Incluido",
            "Pagado",
            "Condicional"
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
                tilServicePrice.setVisibility(LinearLayout.GONE);
                tilConditionalAmount.setVisibility(LinearLayout.GONE);
                break;
            case "Pagado":
                tilServicePrice.setVisibility(LinearLayout.VISIBLE);
                tilConditionalAmount.setVisibility(LinearLayout.GONE);
                break;
            case "Condicional":
                tilServicePrice.setVisibility(LinearLayout.GONE);
                tilConditionalAmount.setVisibility(LinearLayout.VISIBLE);
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

    private void loadCurrentData() {
        etServiceName.setText(originalService.getName());
        etServiceDescription.setText(originalService.getDescription());

        // Set service type
        String typeText = getServiceTypeText(originalService.getType());
        etServiceType.setText(typeText, false);
        updateFieldsVisibilityByType(typeText);

        // Set price if applicable
        if (originalService.getPrice() > 0) {
            etServicePrice.setText(String.valueOf(originalService.getPrice()));
        }

        // Set conditional amount if applicable
        if (originalService.getConditionalAmount() > 0) {
            etConditionalAmount.setText(String.valueOf(originalService.getConditionalAmount()));
        }

        updatePhotosVisibility();
    }

    private String getServiceTypeText(HotelServiceItem.ServiceType type) {
        switch (type) {
            case BASIC:
            case INCLUDED:
                return "Incluido";
            case PAID:
                return "Pagado";
            case CONDITIONAL:
                return "Condicional";
            default:
                return "Incluido";
        }
    }

    private void showIconSelectorDialog() {
        IconSelectorDialog iconDialog = new IconSelectorDialog(context, selectedIconKey, (iconKey, iconName) -> {
            selectedIconKey = iconKey;
            updateIconDisplay();
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
        // Note: Since we don't have photoPickerLauncher in edit mode, we'll skip photo functionality for now
        Toast.makeText(context, "Funcionalidad de fotos disponible solo al crear servicios nuevos", Toast.LENGTH_SHORT).show();
    }

    private void updatePhotoCount() {
        tvPhotoCount.setText(servicePhotos.size() + " / 3 fotos");
    }

    private void updatePhotosVisibility() {
        if (servicePhotos.isEmpty()) {
            rvServicePhotos.setVisibility(LinearLayout.GONE);
        } else {
            rvServicePhotos.setVisibility(LinearLayout.VISIBLE);
        }
    }

    private void saveService() {
        String name = etServiceName.getText().toString().trim();
        String description = etServiceDescription.getText().toString().trim();
        String typeStr = etServiceType.getText().toString().trim();

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

        // Determine service type and validate corresponding fields
        HotelServiceItem.ServiceType serviceType;
        double price = 0.0;
        double conditionalAmount = 0.0;

        switch (typeStr) {
            case "Incluido":
                serviceType = originalService.getType() == HotelServiceItem.ServiceType.BASIC ?
                        HotelServiceItem.ServiceType.BASIC : HotelServiceItem.ServiceType.INCLUDED;
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

        // Create updated service
        HotelServiceItem updatedService = new HotelServiceItem(
                name,
                description,
                price,
                selectedIconKey,
                serviceType,
                new ArrayList<>(servicePhotos),
                conditionalAmount
        );

        if (listener != null) {
            listener.onServiceEdited(updatedService);
        }

        Toast.makeText(context, "✅ Servicio actualizado exitosamente", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}