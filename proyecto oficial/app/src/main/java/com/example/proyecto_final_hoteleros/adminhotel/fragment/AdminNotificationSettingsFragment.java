package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.R;

public class AdminNotificationSettingsFragment extends Fragment {

    private Switch switchNewReservations;
    private Switch switchCheckouts;
    private Switch switchTaxiRequests;
    private Switch switchChatMessages;
    private Switch switchSystemAlerts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_notification_settings, container, false);

        initViews(rootView);
        setupSwitchListeners();
        loadSettings();

        return rootView;
    }

    private void initViews(View rootView) {
        switchNewReservations = rootView.findViewById(R.id.switchNewReservations);
        switchCheckouts = rootView.findViewById(R.id.switchCheckouts);
        switchTaxiRequests = rootView.findViewById(R.id.switchTaxiRequests);
        switchChatMessages = rootView.findViewById(R.id.switchChatMessages);
        switchSystemAlerts = rootView.findViewById(R.id.switchSystemAlerts);

        // Back button
        rootView.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
    }

    private void setupSwitchListeners() {
        switchNewReservations.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveNotificationSetting("new_reservations", isChecked);
        });

        switchCheckouts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveNotificationSetting("checkouts", isChecked);
        });

        switchTaxiRequests.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveNotificationSetting("taxi_requests", isChecked);
        });

        switchChatMessages.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveNotificationSetting("chat_messages", isChecked);
        });

        switchSystemAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveNotificationSetting("system_alerts", isChecked);
        });
    }

    private void loadSettings() {
        // Cargar configuraciones actuales
        switchNewReservations.setChecked(true);
        switchCheckouts.setChecked(true);
        switchTaxiRequests.setChecked(true);
        switchChatMessages.setChecked(true);
        switchSystemAlerts.setChecked(false);
    }

    private void saveNotificationSetting(String setting, boolean enabled) {
        // TODO: Save to SharedPreferences or Firebase
        Toast.makeText(getContext(), "Configuración guardada", Toast.LENGTH_SHORT).show();
    }
}