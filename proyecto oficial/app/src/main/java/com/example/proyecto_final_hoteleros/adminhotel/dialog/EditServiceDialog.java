/*
package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.HotelPhotosAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceItem;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class EditServiceDialog extends Dialog {

    public interface OnServiceEditedListener {
        void onServiceEdited(HotelServiceItem service);
    }

    private Context context;
    private OnServiceEditedListener listener;
    private HotelServiceItem originalService;
    private TextInputEditText etServiceName, etServiceDescription, etServicePrice;
    private AutoCompleteTextView etServiceType;
    private MaterialButton btnSelectIcon, btnAddPhoto, btnSave, btnCancel;
    private RecyclerView rvServicePhotos;

    private String selectedIconKey;
    private List<Uri> servicePhotos;
    private HotelPhotosAdapter photosAdapter;
    private ActivityResultLauncher<Intent> photoPickerLauncher;

    public EditServiceDialog(Context context, HotelServiceItem service, OnServiceEditedListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.originalService = service;
        this.selectedIconKey = service.getIconKey();
        this.servicePhotos = new ArrayList<>(service.getPhotos());
        setupDialog();
    }

    private void setupDialog() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.admin_hotel_dialog_add_service);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        initViews();
        loadCurrentData();
        setupPhotoLauncher();
        setupServiceTypes();
        setupClickListeners();
        updateIconButton();
    }

    private void initViews() {
        etServiceName = findViewById(R.id.etServiceName);
        etServiceDescription = findViewById(R.id.etServiceDescription);
        etServicePrice = findViewById(R.id.etServicePrice);
        etServiceType = findViewById(R.id.etServiceType);
        btnSelectIcon = findViewById(R.id.btnSelectIcon);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        rvServicePhotos = findViewById(R.id.rvServicePhotos);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Setup photos RecyclerView
        photosAdapter = new HotelPhotosAdapter(servicePhotos, this::removePhoto);
        rvServicePhotos.setLayoutManager(new GridLayoutManager(context, 3));
        rvServicePhotos.setAdapter(photosAdapter);
    }

    private void loadCurrentData() {
        etServiceName.setText(originalService.getName());
        etServiceDescription.setText(originalService.getDescription());

        if (originalService.getType() == HotelServiceItem.ServiceType.PAID) {
            etServicePrice.setText(String.valueOf(originalService.getPrice()));
        }

        String typeText = "";
        switch (originalService.getType()) {
            case INCLUDED:
                typeText = "Incluido";
                break;
            case PAID:
                typeText = "Pagado";
                break;
            case SPECIAL:
                typeText = "Especial";
                break;
        }
        etServiceType.setText(typeText, false);
    }

    private void setupPhotoLauncher() {
        photoPickerLauncher = ((FragmentActivity) context).registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == ((FragmentActivity) context).RESULT_OK && result.getData() != null) {
                        Uri photoUri = result.getData().getData();
                        if (photoUri != null && servicePhotos.size() < 5) {
                            servicePhotos.add(photoUri);
                            photosAdapter.notifyDataSetChanged();
                        } else if (servicePhotos.size() >= 5) {
                            Toast.makeText(context, "Máximo 5 fotos permitidas", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void setupServiceTypes() {
        String[] types = {"Incluido", "Pagado", "Especial"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, types);
        etServiceType.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnSelectIcon.setOnClickListener(v -> showIconSelector());

        btnAddPhoto.setOnClickListener(v -> {
            if (servicePhotos.size() >= 5) {
                Toast.makeText(context, "Máximo 5 fotos permitidas", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            photoPickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> saveService());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void showIconSelector() {
        IconSelectorDialog iconDialog = new IconSelectorDialog(context, selectedIconKey, (iconKey, iconName) -> {
            selectedIconKey = iconKey;
            updateIconButton();
        });
        iconDialog.show();
    }

    private void updateIconButton() {
        btnSelectIcon.setCompoundDrawablesWithIntrinsicBounds(IconHelper.getIconResource(selectedIconKey), 0, 0, 0);
        btnSelectIcon.setText(IconHelper.getIconName(selectedIconKey));
    }

    private void removePhoto(int position) {
        servicePhotos.remove(position);
        photosAdapter.notifyItemRemoved(position);
    }

    private void saveService() {
        String name = etServiceName.getText().toString().trim();
        String description = etServiceDescription.getText().toString().trim();
        String priceStr = etServicePrice.getText().toString().trim();
        String typeStr = etServiceType.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || typeStr.isEmpty()) {
            Toast.makeText(context, "Por favor completa todos los campos requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        HotelServiceItem.ServiceType serviceType;
        double price = 0.0;

        switch (typeStr) {
            case "Incluido":
                serviceType = HotelServiceItem.ServiceType.INCLUDED;
                break;
            case "Especial":
                serviceType = HotelServiceItem.ServiceType.SPECIAL;
                break;
            default: // "Pagado"
                serviceType = HotelServiceItem.ServiceType.PAID;
                if (priceStr.isEmpty()) {
                    Toast.makeText(context, "Ingresa el precio del servicio", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    price = Double.parseDouble(priceStr);
                    if (price <= 0) {
                        Toast.makeText(context, "El precio debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Ingresa un precio válido", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }

        HotelServiceItem editedService = new HotelServiceItem(name, description, price, selectedIconKey, serviceType, new ArrayList<>(servicePhotos));

        if (listener != null) {
            listener.onServiceEdited(editedService);
        }

        dismiss();
    }
}

 */