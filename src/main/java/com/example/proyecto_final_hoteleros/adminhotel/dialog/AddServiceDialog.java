package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.HotelPhotosAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.IconSelectorDialog;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceItem;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AddServiceDialog extends Dialog {

    public interface OnServiceAddedListener {
        void onServiceAdded(HotelServiceItem service);
    }

    private Context context;
    private OnServiceAddedListener listener;
    private ActivityResultLauncher<Intent> photoPickerLauncher; // MODIFICADO - Recibido como parámetro
    private TextInputEditText etServiceName, etServiceDescription, etServicePrice;
    private AutoCompleteTextView etServiceType;
    private AppCompatButton btnSelectIcon, btnAddPhoto, btnSave, btnCancel;
    private RecyclerView rvServicePhotos;

    private String selectedIconKey = "wifi";
    private List<Uri> servicePhotos;
    private HotelPhotosAdapter photosAdapter;

    // NUEVO CONSTRUCTOR con launcher
    public AddServiceDialog(Context context, ActivityResultLauncher<Intent> photoPickerLauncher, OnServiceAddedListener listener) {
        super(context);
        this.context = context;
        this.photoPickerLauncher = photoPickerLauncher;
        this.listener = listener;
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
        setupServiceTypes();
        setupPhotosRecyclerView();
        setupClickListeners();
    }

    private void initViews() {
        etServiceName = findViewById(R.id.etServiceName);
        etServiceDescription = findViewById(R.id.etServiceDescription);
        etServicePrice = findViewById(R.id.etServicePrice);
        etServiceType = findViewById(R.id.etServiceType);
        btnSelectIcon = findViewById(R.id.btnSelectIcon);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        rvServicePhotos = findViewById(R.id.rvServicePhotos);

        servicePhotos = new ArrayList<>();
    }

    private void setupServiceTypes() {
        String[] types = {"Incluido", "Pagado", "Especial"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, types);
        etServiceType.setAdapter(adapter);
        etServiceType.setText("Pagado");
    }

    private void setupPhotosRecyclerView() {
        photosAdapter = new HotelPhotosAdapter(servicePhotos, this::removePhoto);
        rvServicePhotos.setLayoutManager(new GridLayoutManager(context, 3));
        rvServicePhotos.setAdapter(photosAdapter);
    }

    private void setupClickListeners() {
        btnSelectIcon.setOnClickListener(v -> showIconSelector());

        btnAddPhoto.setOnClickListener(v -> {
            if (servicePhotos.size() >= 5) {
                Toast.makeText(context, "Máximo 5 fotos permitidas", Toast.LENGTH_SHORT).show();
                return;
            }
            // Usar el launcher pasado como parámetro
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            photoPickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> saveService());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    // NUEVO MÉTODO para recibir la foto desde el Fragment
    public void addPhoto(Uri photoUri) {
        servicePhotos.add(photoUri);
        photosAdapter.notifyDataSetChanged();
    }

    private void showIconSelector() {
        IconSelectorDialog iconDialog = new IconSelectorDialog(context, selectedIconKey, (iconKey, iconName) -> {
            selectedIconKey = iconKey;
            updateIconButton();
        });
        iconDialog.show();
    }

    private void updateIconButton() {
        if (selectedIconKey != null && !selectedIconKey.isEmpty()) {
            try {
                int iconResource = IconHelper.getIconResource(selectedIconKey);
                String iconName = IconHelper.getIconName(selectedIconKey);

                btnSelectIcon.setCompoundDrawablesWithIntrinsicBounds(iconResource, 0, 0, 0);
                btnSelectIcon.setText("✅ " + iconName);

                // Importar ColorStateList
                btnSelectIcon.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.success)
                ));

                Log.d("AddServiceDialog", "✅ Icono actualizado: " + selectedIconKey);

            } catch (Exception e) {
                Log.e("AddServiceDialog", "❌ Error: " + e.getMessage());
                btnSelectIcon.setText("🎨 Icono seleccionado");
            }
        }
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

        if (name.isEmpty()) {
            Toast.makeText(context, "Ingresa el nombre del servicio", Toast.LENGTH_SHORT).show();
            etServiceName.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(context, "Ingresa la descripción del servicio", Toast.LENGTH_SHORT).show();
            etServiceDescription.requestFocus();
            return;
        }

        if (typeStr.isEmpty()) {
            Toast.makeText(context, "Selecciona el tipo de servicio", Toast.LENGTH_SHORT).show();
            etServiceType.requestFocus();
            return;
        }

        double price = 0.0;
        if (!typeStr.equals("Incluido") && !priceStr.isEmpty()) {
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Ingresa un precio válido", Toast.LENGTH_SHORT).show();
                etServicePrice.requestFocus();
                return;
            }
        }

        // Determinar tipo de servicio
        HotelServiceItem.ServiceType serviceType;
        switch (typeStr) {
            case "Incluido":
                serviceType = HotelServiceItem.ServiceType.INCLUDED;
                break;
            case "Especial":
                serviceType = HotelServiceItem.ServiceType.SPECIAL;
                break;
            default:
                serviceType = HotelServiceItem.ServiceType.PAID;
                break;
        }

        // Crear servicio
        HotelServiceItem service = new HotelServiceItem(name, description, price, selectedIconKey, serviceType, servicePhotos);

        if (listener != null) {
            listener.onServiceAdded(service);
        }

        Toast.makeText(context, "✅ Servicio agregado exitosamente", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}