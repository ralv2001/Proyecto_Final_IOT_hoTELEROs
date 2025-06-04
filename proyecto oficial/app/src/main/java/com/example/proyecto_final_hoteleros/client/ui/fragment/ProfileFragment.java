package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.proyecto_final_hoteleros.MainActivity;
import com.example.proyecto_final_hoteleros.R;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    // Variables para datos del usuario (Firebase)
    private String userId;
    private String userName;
    private String userFullName;
    private String userEmail;
    private String userType;

    // Variables para el navegador inferior
    private LinearLayout navHome, navExplore, navChat, navProfile;
    private ImageView ivHome, ivExplore, ivChat, ivProfile;

    // Variables para las opciones del perfil
    private LinearLayout layoutProfileOption, layoutPaymentOption, layoutNotificationOption;
    private TextView btnLogout;

    // Views para mostrar datos del usuario
    private TextView tvProfileName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtener datos del usuario desde los argumentos
        if (getArguments() != null) {
            userId = getArguments().getString("user_id");
            userName = getArguments().getString("user_name");
            userFullName = getArguments().getString("user_full_name");
            userEmail = getArguments().getString("user_email");
            userType = getArguments().getString("user_type");

            Log.d(TAG, "=== DATOS RECIBIDOS EN PROFILEFRAGMENT ===");
            Log.d(TAG, "User ID: " + userId);
            Log.d(TAG, "User Name: " + userName);
            Log.d(TAG, "User Full Name: " + userFullName);
            Log.d(TAG, "User Email: " + userEmail);
            Log.d(TAG, "User Type: " + userType);
        }

        // Configurar la transición de entrada compartida
        setSharedElementEnterTransition(
                TransitionInflater.from(requireContext())
                        .inflateTransition(android.R.transition.move)
                        .setDuration(300)
        );

        // Configurar transición de retroceso (cuando se presiona back)
        setSharedElementReturnTransition(
                TransitionInflater.from(requireContext())
                        .inflateTransition(android.R.transition.move)
                        .setDuration(300)
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inicializar views
        initViews(rootView);

        // Configurar datos del usuario
        setupUserData();

        // Configurar el nombre de transición para el avatar en el perfil
        de.hdodenhof.circleimageview.CircleImageView profileAvatar =
                rootView.findViewById(R.id.ivProfileAvatar);

        // Establecer el nombre de transición que coincide con el usado en HomeFragment
        ViewCompat.setTransitionName(profileAvatar, "avatar_transition");

        // Inicializar las opciones del perfil
        initializeProfileOptions(rootView);

        // Configuración del navegador inferior
        setupBottomNavigation(rootView);

        return rootView;
    }

    private void initViews(View view) {
        // Inicializar el TextView del nombre del perfil
        tvProfileName = view.findViewById(R.id.tvProfileName);
    }

    private void setupUserData() {
        // Configurar el nombre del usuario en el perfil
        if (tvProfileName != null) {
            String displayName = userFullName != null && !userFullName.isEmpty() ?
                    userFullName :
                    (userName != null && !userName.isEmpty() ? userName : "Usuario");

            tvProfileName.setText(displayName);

            Log.d(TAG, "Nombre de perfil configurado: " + displayName);
        } else {
            Log.e(TAG, "TextView tvProfileName no encontrado en el layout");
        }
    }

    // Método para inicializar las opciones del perfil
    private void initializeProfileOptions(View rootView) {
        // Referencias a los elementos de las opciones
        layoutProfileOption = rootView.findViewById(R.id.layoutProfileOption);
        layoutPaymentOption = rootView.findViewById(R.id.layoutPaymentOption);
        layoutNotificationOption = rootView.findViewById(R.id.layoutNotificationOption);
        btnLogout = rootView.findViewById(R.id.btnLogout);

        // Configurar listeners para las opciones
        layoutProfileOption.setOnClickListener(v -> {
            // Navegar a la pantalla de editar perfil pasando los datos del usuario
            navigateToEditProfile();
        });

        layoutPaymentOption.setOnClickListener(v -> {
            // Implementar la navegación a la pantalla de métodos de pago
            // Por ahora solo mostramos un mensaje
            Toast.makeText(requireContext(), "Métodos de pago", Toast.LENGTH_SHORT).show();
        });

        layoutNotificationOption.setOnClickListener(v -> {
            // Navegar a la pantalla de configuración de notificaciones
            navigateToNotifications();
        });

        btnLogout.setOnClickListener(v -> {
            // Implementar el cierre de sesión
            performLogout();
        });
    }

    // Método para navegar a editar perfil
    private void navigateToEditProfile() {
        // Crear una instancia del fragmento de editar perfil
        // EditProfileFragment editProfileFragment = new EditProfileFragment();

        // Pasar los datos del usuario
        Bundle args = new Bundle();
        args.putString("user_id", userId);
        args.putString("user_name", userName);
        args.putString("user_full_name", userFullName);
        args.putString("user_email", userEmail);
        args.putString("user_type", userType);
        // editProfileFragment.setArguments(args);

        // Por ahora solo mostramos un mensaje con los datos
        String message = "Editar perfil de: " + (userFullName != null ? userFullName : userName);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

        // Cuando tengas el EditProfileFragment, descomenta estas líneas:
        /*
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, editProfileFragment)
                .addToBackStack(null)
                .commit();
        */
    }

    // Método para navegar a la pantalla de notificaciones
    private void navigateToNotifications() {
        // Crear una instancia del fragmento de notificaciones
        NotificationSettingsFragment notificationFragment = new NotificationSettingsFragment();

        // Pasar los datos del usuario
        Bundle args = new Bundle();
        args.putString("user_id", userId);
        args.putString("user_name", userName);
        notificationFragment.setArguments(args);

        // Realizar la transacción del fragmento
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, notificationFragment)
                .addToBackStack(null)
                .commit();
    }

    // Método para realizar el cierre de sesión
    private void performLogout() {
        try {
            Log.d(TAG, "Iniciando proceso de cierre de sesión...");

            // Cerrar sesión en Firebase Auth (si está siendo usado)
            FirebaseAuth.getInstance().signOut();

            Log.d(TAG, "Sesión de Firebase cerrada exitosamente");

            // Mostrar mensaje de confirmación
            Toast.makeText(requireContext(), "Cerrando sesión...", Toast.LENGTH_SHORT).show();

            // Navegar a MainActivity (pantalla de inicio/login)
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Finalizar la actividad actual
            requireActivity().finish();

            Log.d(TAG, "Navegación a MainActivity completada");

        } catch (Exception e) {
            Log.e(TAG, "Error al cerrar sesión: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error al cerrar sesión", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para configurar el navegador inferior
    private void setupBottomNavigation(View rootView) {
        // Referencias a los elementos del navegador
        navHome = rootView.findViewById(R.id.nav_home);
        navExplore = rootView.findViewById(R.id.nav_explore);
        navChat = rootView.findViewById(R.id.nav_chat);
        navProfile = rootView.findViewById(R.id.nav_profile);
        ivHome = rootView.findViewById(R.id.iv_home);
        ivExplore = rootView.findViewById(R.id.iv_explore);
        ivChat = rootView.findViewById(R.id.iv_chat);
        ivProfile = rootView.findViewById(R.id.iv_profile);

        // Marcar Profile como seleccionado
        if (navProfile != null && ivProfile != null) {
            setSelectedNavItem(navProfile, ivProfile);
        }

        // Establecer listeners para cada elemento del navegador
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (!isCurrentFragment(HomeFragment.class)) {
                    setSelectedNavItem(navHome, ivHome);
                    HomeFragment homeFragment = new HomeFragment();
                    // Pasar datos del usuario de vuelta al HomeFragment
                    Bundle args = new Bundle();
                    args.putString("user_id", userId);
                    args.putString("user_name", userName);
                    args.putString("user_full_name", userFullName);
                    args.putString("user_email", userEmail);
                    args.putString("user_type", userType);
                    homeFragment.setArguments(args);
                    navigateToFragment(homeFragment, false);
                }
            });
        }

        if (navExplore != null) {
            navExplore.setOnClickListener(v -> {
                if (!isCurrentFragment(HistorialFragment.class)) {
                    setSelectedNavItem(navExplore, ivExplore);
                    HistorialFragment historialFragment = new HistorialFragment();
                    // Pasar datos del usuario
                    Bundle args = new Bundle();
                    args.putString("user_id", userId);
                    args.putString("user_name", userName);
                    historialFragment.setArguments(args);
                    navigateToFragment(historialFragment, true);
                }
            });
        }

        if (navChat != null) {
            navChat.setOnClickListener(v -> {
                if (!isCurrentFragment(ChatFragment.class)) {
                    setSelectedNavItem(navChat, ivChat);
                    ChatFragment chatFragment = new ChatFragment();
                    // Pasar datos del usuario
                    Bundle args = new Bundle();
                    args.putString("user_id", userId);
                    args.putString("user_name", userName);
                    chatFragment.setArguments(args);
                    navigateToFragment(chatFragment, true);
                }
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                if (!isCurrentFragment(ProfileFragment.class)) {
                    setSelectedNavItem(navProfile, ivProfile);
                    // Ya estamos en Profile, no necesitamos cambiar
                }
            });
        }
    }

    // Método para navegar a un fragmento con animación
    private void navigateToFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction =
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left,
                                R.anim.slide_in_left,
                                R.anim.slide_out_right
                        )
                        .replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    // Método para verificar si el fragmento actual es del tipo especificado
    private boolean isCurrentFragment(Class<? extends Fragment> fragmentClass) {
        Fragment currentFragment =
                requireActivity().getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);

        return currentFragment != null &&
                fragmentClass.isInstance(currentFragment);
    }

    // Método para resaltar el ítem seleccionado
    private void setSelectedNavItem(LinearLayout navItem, ImageView icon) {
        // Resetear todos los íconos a color blanco
        if (ivHome != null) ivHome.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        if (ivExplore != null) ivExplore.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        if (ivChat != null) ivChat.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        if (ivProfile != null) ivProfile.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));

        // Establecer el ícono seleccionado a color naranja
        if (icon != null) {
            icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.orange));
        }
    }

    // Métodos públicos para acceder a los datos del usuario
    public String getUserName() {
        return userName != null ? userName : "Usuario";
    }

    public String getUserFullName() {
        return userFullName != null ? userFullName : getUserName();
    }

    public String getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserType() {
        return userType;
    }
}