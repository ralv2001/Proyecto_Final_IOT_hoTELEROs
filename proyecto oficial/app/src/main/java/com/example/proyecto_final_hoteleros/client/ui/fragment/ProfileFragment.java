package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;


import com.example.proyecto_final_hoteleros.MainActivity;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.navigation.NavigationManager;
import com.example.proyecto_final_hoteleros.client.utils.UserDataManager;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends BaseBottomNavigationFragment {
    private static final String TAG = "ProfileFragment";

    // Variables para las opciones del perfil
    private LinearLayout layoutProfileOption, layoutPaymentOption, layoutNotificationOption;
    private TextView btnLogout;

    // Views para mostrar datos del usuario
    private TextView tvProfileName;

    @Override
    protected NavigationTab getCurrentTab() {
        return NavigationTab.PROFILE;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtener datos del usuario y guardarlos en el manager si vienen en argumentos
        if (getArguments() != null) {
            String userId = getArguments().getString("user_id");
            String userName = getArguments().getString("user_name");
            String userFullName = getArguments().getString("user_full_name");
            String userEmail = getArguments().getString("user_email");
            String userType = getArguments().getString("user_type");

            UserDataManager.getInstance().setUserData(userId, userName, userFullName, userEmail, userType);

            Log.d(TAG, "=== DATOS RECIBIDOS EN PROFILEFRAGMENT ===");
            Log.d(TAG, "User ID: " + userId);
            Log.d(TAG, "User Name: " + userName);
        }

        // Configurar transiciones
        setSharedElementEnterTransition(
                TransitionInflater.from(requireContext())
                        .inflateTransition(android.R.transition.move)
                        .setDuration(300)
        );

        setSharedElementReturnTransition(
                TransitionInflater.from(requireContext())
                        .inflateTransition(android.R.transition.move)
                        .setDuration(300)
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.client_fragment_profile, container, false);

        // Inicializar views
        initViews(rootView);

        // Configurar datos del usuario
        setupUserData();

        // Configurar el avatar con transición
        de.hdodenhof.circleimageview.CircleImageView profileAvatar =
                rootView.findViewById(R.id.ivProfileAvatar);
        ViewCompat.setTransitionName(profileAvatar, "avatar_transition");

        // Inicializar las opciones del perfil
        initializeProfileOptions(rootView);

        return rootView;
    }

    private void initViews(View view) {
        tvProfileName = view.findViewById(R.id.tvProfileName);
    }

    private void setupUserData() {
        if (tvProfileName != null) {
            String displayName = UserDataManager.getInstance().getUserFullName();
            tvProfileName.setText(displayName);
            Log.d(TAG, "Nombre de perfil configurado: " + displayName);
        } else {
            Log.e(TAG, "TextView tvProfileName no encontrado en el layout");
        }
    }

    private void initializeProfileOptions(View rootView) {
        // Referencias a los elementos de las opciones
        layoutProfileOption = rootView.findViewById(R.id.layoutProfileOption);
        layoutPaymentOption = rootView.findViewById(R.id.layoutPaymentOption);
        layoutNotificationOption = rootView.findViewById(R.id.layoutNotificationOption);
        btnLogout = rootView.findViewById(R.id.btnLogout);

        // Configurar listeners para las opciones
        layoutProfileOption.setOnClickListener(v -> navigateToEditProfile());

        layoutPaymentOption.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Métodos de pago", Toast.LENGTH_SHORT).show();
        });

        layoutNotificationOption.setOnClickListener(v -> navigateToNotifications());

        btnLogout.setOnClickListener(v -> performLogout());
    }

    private void navigateToEditProfile() {
        Bundle args = UserDataManager.getInstance().getUserBundle();
        String message = "Editar perfil de: " + UserDataManager.getInstance().getUserFullName();
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

        // Cuando tengas EditProfileFragment, usar NavigationManager:
        // NavigationManager.getInstance().navigateToEditProfile(args);
    }

    private void navigateToNotifications() {
        NavigationManager.getInstance().navigateToNotificationSettings(
                UserDataManager.getInstance().getUserBundle()
        );
    }

    private void performLogout() {
        try {
            Log.d(TAG, "Iniciando proceso de cierre de sesión...");

            FirebaseAuth.getInstance().signOut();
            Log.d(TAG, "Sesión de Firebase cerrada exitosamente");

            Toast.makeText(requireContext(), "Cerrando sesión...", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            requireActivity().finish();
            Log.d(TAG, "Navegación a MainActivity completada");

        } catch (Exception e) {
            Log.e(TAG, "Error al cerrar sesión: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error al cerrar sesión", Toast.LENGTH_SHORT).show();
        }
    }

    // Métodos públicos para acceder a los datos del usuario
    public String getUserName() {
        return UserDataManager.getInstance().getUserName();
    }

    public String getUserFullName() {
        return UserDataManager.getInstance().getUserFullName();
    }

    public String getUserId() {
        return UserDataManager.getInstance().getUserId();
    }

    public String getUserEmail() {
        return UserDataManager.getInstance().getUserEmail();
    }

    public String getUserType() {
        return UserDataManager.getInstance().getUserType();
    }
}