// HistorialFragment.java
package com.example.proyecto_final_hoteleros.client.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.proyecto_final_hoteleros.R;

public class HistorialFragment extends Fragment {
    // Variables para el navegador inferior
    private LinearLayout navHome, navExplore, navChat, navProfile;
    private ImageView ivHome, ivExplore, ivChat, ivProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_historial, container, false);

        // Configuración del navegador inferior
        setupBottomNavigation(rootView);

        return rootView;
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

        // Marcar Explore como seleccionado
        setSelectedNavItem(navExplore, ivExplore);

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
                // Ya estamos en Historial, no necesitamos cambiar
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
                navigateToFragment(new ProfileFragment(), true);
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