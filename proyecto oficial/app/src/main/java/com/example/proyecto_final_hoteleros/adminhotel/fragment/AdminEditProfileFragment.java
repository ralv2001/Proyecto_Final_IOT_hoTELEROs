package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.R;

public class AdminEditProfileFragment extends Fragment {

    private ImageView ivProfileImage;
    private EditText etAdminName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etHotelName;
    private Button btnSaveProfile;
    private Button btnChangePassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_edit_profile, container, false);

        initViews(rootView);
        setupClickListeners();
        loadCurrentData();

        return rootView;
    }

    private void initViews(View rootView) {
        ivProfileImage = rootView.findViewById(R.id.ivProfileImage);
        etAdminName = rootView.findViewById(R.id.etAdminName);
        etEmail = rootView.findViewById(R.id.etEmail);
        etPhone = rootView.findViewById(R.id.etPhone);
        etHotelName = rootView.findViewById(R.id.etHotelName);
        btnSaveProfile = rootView.findViewById(R.id.btnSaveProfile);
        btnChangePassword = rootView.findViewById(R.id.btnChangePassword);

        // Back button
        rootView.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
    }

    private void setupClickListeners() {
        ivProfileImage.setOnClickListener(v -> {
            // TODO: Implement image picker
            Toast.makeText(getContext(), "Cambiar imagen de perfil", Toast.LENGTH_SHORT).show();
        });

        btnSaveProfile.setOnClickListener(v -> {
            saveProfile();
        });

        btnChangePassword.setOnClickListener(v -> {
            // TODO: Navigate to change password
            Toast.makeText(getContext(), "Cambiar contraseña", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadCurrentData() {
        // Cargar datos actuales del perfil
        etAdminName.setText("Admin Hotel");
        etEmail.setText("admin@hotelbelmond.com");
        etPhone.setText("+51 987 654 321");
        etHotelName.setText("Hotel Belmond");
    }

    private void saveProfile() {
        String name = etAdminName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String hotelName = etHotelName.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || hotelName.isEmpty()) {
            Toast.makeText(getContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Save to Firebase/API
        Toast.makeText(getContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }
}