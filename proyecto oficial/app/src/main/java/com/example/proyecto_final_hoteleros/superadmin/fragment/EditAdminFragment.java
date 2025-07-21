package com.example.proyecto_final_hoteleros.superadmin.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity;
import com.example.proyecto_final_hoteleros.superadmin.models.AdminUser;
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class EditAdminFragment extends Fragment {

    private static final String TAG = "EditAdminFragment";
    private static final String ARG_ADMIN = "admin";

    private AdminUser admin;

    // Views
    private MaterialToolbar toolbar;
    private TextInputEditText etNombres, etApellidos, etEmail, etHotel;
    private SwitchMaterial switchActive;
    private MaterialButton btnSave, btnCancel;

    public static EditAdminFragment newInstance(AdminUser admin) {
        EditAdminFragment fragment = new EditAdminFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ADMIN, admin);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.superadmin_fragment_edit_admin, container, false);

        initViews(view);
        loadAdminData();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        etNombres = view.findViewById(R.id.et_nombres);
        etApellidos = view.findViewById(R.id.et_apellidos);
        etEmail = view.findViewById(R.id.et_email);
        etHotel = view.findViewById(R.id.et_hotel);
        switchActive = view.findViewById(R.id.switch_active);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);

        // Configurar toolbar
        toolbar.setTitle("Editar Administrador");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> navigateBack());
    }

    private void loadAdminData() {
        if (getArguments() != null) {
            admin = (AdminUser) getArguments().getSerializable(ARG_ADMIN);
            if (admin != null) {
                populateForm();
            }
        }
    }

    private void populateForm() {
        // Separar nombre completo
        String fullName = admin.getName();
        String[] nameParts = fullName.split(" ", 2);

        etNombres.setText(nameParts.length > 0 ? nameParts[0] : "");
        etApellidos.setText(nameParts.length > 1 ? nameParts[1] : "");
        etEmail.setText(admin.getEmail());
        etHotel.setText(admin.getHotelName());
        switchActive.setChecked(admin.isActive());

        Log.d(TAG, "Formulario poblado para: " + admin.getName());
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveChanges());
        btnCancel.setOnClickListener(v -> navigateBack());
    }

    private void saveChanges() {
        if (!validateForm()) return;

        // Mostrar loading
        btnSave.setEnabled(false);
        btnSave.setText("Guardando...");

        // Obtener datos del formulario
        String nombres = etNombres.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String hotel = etHotel.getText().toString().trim();
        boolean isActive = switchActive.isChecked();

        // Preparar datos para Firebase
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombres", nombres);
        updates.put("apellidos", apellidos);
        updates.put("active", isActive);

        Log.d(TAG, "Guardando cambios para admin: " + admin.getId());

        // Actualizar en Firebase
        FirebaseManager.getInstance().updateHotelAdmin(admin.getId(), updates, new FirebaseManager.DataCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d(TAG, "✅ Admin actualizado exitosamente");

                        // Actualizar modelo local
                        admin.setName(nombres + " " + apellidos);
                        admin.setHotelName(hotel);
                        admin.setActive(isActive);

                        // Mostrar éxito y regresar
                        Toast.makeText(getContext(),
                                "✅ Administrador actualizado correctamente",
                                Toast.LENGTH_LONG).show();

                        navigateBack();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e(TAG, "❌ Error actualizando admin: " + error);

                        Toast.makeText(getContext(),
                                "❌ Error actualizando: " + error,
                                Toast.LENGTH_LONG).show();

                        // Restaurar botón
                        btnSave.setEnabled(true);
                        btnSave.setText("💾 Guardar Cambios");
                    });
                }
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validar nombres
        String nombres = etNombres.getText().toString().trim();
        if (nombres.isEmpty()) {
            etNombres.setError("Los nombres son requeridos");
            isValid = false;
        } else {
            etNombres.setError(null);
        }

        // Validar apellidos
        String apellidos = etApellidos.getText().toString().trim();
        if (apellidos.isEmpty()) {
            etApellidos.setError("Los apellidos son requeridos");
            isValid = false;
        } else {
            etApellidos.setError(null);
        }

        // Validar hotel
        String hotel = etHotel.getText().toString().trim();
        if (hotel.isEmpty()) {
            etHotel.setError("El hotel es requerido");
            isValid = false;
        } else {
            etHotel.setError(null);
        }

        return isValid;
    }

    private void navigateBack() {
        if (getActivity() instanceof SuperAdminActivity) {
            getActivity().onBackPressed();
        }
    }
}