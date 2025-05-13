// ProfileFragment.java
package com.example.proyecto_final_hoteleros.client.fragment;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.proyecto_final_hoteleros.R;

public class ProfileFragment extends Fragment {
    // Variables para el navegador inferior
    private LinearLayout navHome, navExplore, navChat, navProfile;
    private ImageView ivHome, ivExplore, ivChat, ivProfile;

    // Variables para las opciones del perfil
    private LinearLayout layoutProfileOption, layoutPaymentOption, layoutNotificationOption;
    private TextView btnLogout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    // Método para inicializar las opciones del perfil
    private void initializeProfileOptions(View rootView) {
        // Referencias a los elementos de las opciones
        layoutProfileOption = rootView.findViewById(R.id.layoutProfileOption);
        layoutPaymentOption = rootView.findViewById(R.id.layoutPaymentOption);
        layoutNotificationOption = rootView.findViewById(R.id.layoutNotificationOption);
        btnLogout = rootView.findViewById(R.id.btnLogout);

        // Configurar listeners para las opciones
        layoutProfileOption.setOnClickListener(v -> {
            // Implementar la navegación a la pantalla de editar perfil
            // Por ahora solo mostramos un mensaje
            Toast.makeText(requireContext(), "Editar perfil", Toast.LENGTH_SHORT).show();
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
            // Por ahora solo mostramos un mensaje
            Toast.makeText(requireContext(), "Cerrando sesión...", Toast.LENGTH_SHORT).show();
        });
    }

    // Método para navegar a la pantalla de notificaciones
    private void navigateToNotifications() {
        // Crear una instancia del fragmento de notificaciones
        NotificationSettingsFragment notificationFragment = new NotificationSettingsFragment();

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
        setSelectedNavItem(navProfile, ivProfile);

        // Establecer listeners para cada elemento del navegador
        navHome.setOnClickListener(v -> {
            if (!isCurrentFragment(HomeFragment.class)) {
                setSelectedNavItem(navHome, ivHome);
                navigateToFragment(new HomeFragment(), false);
            }
        });

        navExplore.setOnClickListener(v -> {
            if (!isCurrentFragment(HistorialFragment.class)) {
                setSelectedNavItem(navExplore, ivExplore);
                navigateToFragment(new HistorialFragment(), true);
            }
        });

        navChat.setOnClickListener(v -> {
            if (!isCurrentFragment(ChatFragment.class)) {
                setSelectedNavItem(navChat, ivChat);
                navigateToFragment(new ChatFragment(), true);
            }
        });

        navProfile.setOnClickListener(v -> {
            if (!isCurrentFragment(ProfileFragment.class)) {
                setSelectedNavItem(navProfile, ivProfile);
                // Ya estamos en Profile, no necesitamos cambiar
            }
        });
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
        ivHome.setColorFilter(ContextCompat.getColor(requireContext(),
                android.R.color.white));
        ivExplore.setColorFilter(ContextCompat.getColor(requireContext(),
                android.R.color.white));
        ivChat.setColorFilter(ContextCompat.getColor(requireContext(),
                android.R.color.white));
        ivProfile.setColorFilter(ContextCompat.getColor(requireContext(),
                android.R.color.white));

        // Establecer el ícono seleccionado a color naranja
        icon.setColorFilter(ContextCompat.getColor(requireContext(),
                R.color.orange));
    }
}