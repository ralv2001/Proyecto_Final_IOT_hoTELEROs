package com.example.proyecto_final_hoteleros.client.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.proyecto_final_hoteleros.R;

public class NotificationSettingsFragment extends Fragment {

    // Constants for SharedPreferences
    public static final String PREFS_NAME = "NotificationPrefs";
    public static final String PREF_BOOKING_NOTIFICATION = "booking_notification";
    public static final String PREF_CHECKIN_NOTIFICATION = "checkin_notification";
    public static final String PREF_CHECKOUT_NOTIFICATION = "checkout_notification";

    // UI Elements
    private Switch switchBooking;
    private Switch switchCheckIn;
    private Switch switchCheckOut;
    private Button btnSaveNotifications;
    private ImageButton btnBack;

    // SharedPreferences object
    private SharedPreferences preferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize SharedPreferences
        preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_notification_settings, container, false);

        // Initialize UI elements
        initializeViews(rootView);

        // Load saved preferences
        loadSavedPreferences();

        // Set up click listeners
        setupClickListeners();

        return rootView;
    }

    private void initializeViews(View rootView) {
        switchBooking = rootView.findViewById(R.id.switchBooking);
        switchCheckIn = rootView.findViewById(R.id.switchCheckIn);
        switchCheckOut = rootView.findViewById(R.id.switchCheckOut);
        btnSaveNotifications = rootView.findViewById(R.id.btnSaveNotifications);
        btnBack = rootView.findViewById(R.id.btnBack);
    }

    private void loadSavedPreferences() {
        // Load saved preferences with default values of true
        switchBooking.setChecked(preferences.getBoolean(PREF_BOOKING_NOTIFICATION, true));
        switchCheckIn.setChecked(preferences.getBoolean(PREF_CHECKIN_NOTIFICATION, true));
        switchCheckOut.setChecked(preferences.getBoolean(PREF_CHECKOUT_NOTIFICATION, true));
    }

    private void setupClickListeners() {
        // Save button click listener
        btnSaveNotifications.setOnClickListener(v -> saveNotificationPreferences());

        // Back button click listener
        btnBack.setOnClickListener(v -> goBack());
    }

    private void saveNotificationPreferences() {
        // Get the SharedPreferences editor
        SharedPreferences.Editor editor = preferences.edit();

        // Save the current state of switches
        editor.putBoolean(PREF_BOOKING_NOTIFICATION, switchBooking.isChecked());
        editor.putBoolean(PREF_CHECKIN_NOTIFICATION, switchCheckIn.isChecked());
        editor.putBoolean(PREF_CHECKOUT_NOTIFICATION, switchCheckOut.isChecked());

        // Apply changes
        editor.apply();

        // Show confirmation to user
        Toast.makeText(requireContext(), "Preferencias guardadas", Toast.LENGTH_SHORT).show();

        // Go back to profile
        goBack();
    }

    private void goBack() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}