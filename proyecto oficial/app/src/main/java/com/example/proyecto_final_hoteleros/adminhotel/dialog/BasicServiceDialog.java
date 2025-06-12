/*
package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.BasicService;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class BasicServiceDialog extends Dialog {

    public interface OnServiceAddedListener {
        void onServiceAdded(BasicService service);
    }

    private Context context;
    private OnServiceAddedListener listener;
    private TextInputEditText etServiceName, etServiceDescription;
    private MaterialButton btnSelectIcon, btnSave, btnCancel;
    private String selectedIconKey = "wifi";

    public BasicServiceDialog(Context context, OnServiceAddedListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        setupDialog();
    }

    private void setupDialog() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.admin_hotel_dialog_add_basic_service);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        initViews();
        setupClickListeners();
        updateIconButton();
    }

    private void initViews() {
        etServiceName = findViewById(R.id.etServiceName);
        etServiceDescription = findViewById(R.id.etServiceDescription);
        btnSelectIcon = findViewById(R.id.btnSelectIcon);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupClickListeners() {
        btnSelectIcon.setOnClickListener(v -> showIconSelector());
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

    private void saveService() {
        String name = etServiceName.getText().toString().trim();
        String description = etServiceDescription.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(context, "Ingresa el nombre del servicio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(context, "Ingresa la descripción del servicio", Toast.LENGTH_SHORT).show();
            return;
        }

        BasicService service = new BasicService(name, description, selectedIconKey);

        if (listener != null) {
            listener.onServiceAdded(service);
        }

        Toast.makeText(context, "✅ Servicio básico agregado", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}

 */