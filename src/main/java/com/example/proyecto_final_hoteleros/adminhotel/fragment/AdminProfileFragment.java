package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.AuthActivity;

public class AdminProfileFragment extends Fragment {

    private ImageView ivProfileImage;
    private TextView tvAdminName;
    private TextView tvHotelName;
    private CardView cardEditProfile;
    private CardView cardHotelSettings;
    private CardView cardNotifications;
    private CardView cardHelp;
    private CardView cardLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_profile, container, false);

        initViews(rootView);
        setupClickListeners();
        loadProfileData();

        return rootView;
    }

    private void initViews(View rootView) {
        ivProfileImage = rootView.findViewById(R.id.ivProfileImage);
        tvAdminName = rootView.findViewById(R.id.tvAdminName);
        tvHotelName = rootView.findViewById(R.id.tvHotelName);
        cardEditProfile = rootView.findViewById(R.id.cardEditProfile);
        cardHotelSettings = rootView.findViewById(R.id.cardHotelSettings);
        cardNotifications = rootView.findViewById(R.id.cardNotifications);
        cardHelp = rootView.findViewById(R.id.cardHelp);
        cardLogout = rootView.findViewById(R.id.cardLogout);
    }

    private void setupClickListeners() {
        cardEditProfile.setOnClickListener(v -> {
            navigateToEditProfile();
        });

        cardHotelSettings.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Configuración del Hotel", Toast.LENGTH_SHORT).show();
        });

        cardNotifications.setOnClickListener(v -> {
            navigateToNotificationSettings();
        });

        cardHelp.setOnClickListener(v -> {
            navigateToHelp();
        });

        cardLogout.setOnClickListener(v -> {
            showLogoutDialog();
        });
    }

    private void loadProfileData() {
        // Simulando datos
        tvAdminName.setText("Admin Hotel");
        tvHotelName.setText("Hotel Belmond");
        ivProfileImage.setImageResource(R.drawable.ic_profile_placeholder);
    }

    private void navigateToEditProfile() {
        AdminEditProfileFragment editFragment = new AdminEditProfileFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editFragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToNotificationSettings() {
        AdminNotificationSettingsFragment notificationFragment = new AdminNotificationSettingsFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, notificationFragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToHelp() {
        AdminHelpFragment helpFragment = new AdminHelpFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, helpFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showLogoutDialog() {
        // Logout directo por ahora
        Intent intent = new Intent(getActivity(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}