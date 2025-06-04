package com.example.proyecto_final_hoteleros.client.ui.fragment;

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

public class BarraFragment extends Fragment {

    private LinearLayout navHome, navExplore, navChat, navProfile;
    private ImageView ivHome, ivExplore, ivChat, ivProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.barra_admin, container, false);
        setupBottomNavigation(view);
        return view;
    }

    private void setupBottomNavigation(View rootView) {
        navHome = rootView.findViewById(R.id.nav_home);
        navExplore = rootView.findViewById(R.id.nav_explore);
        navChat = rootView.findViewById(R.id.nav_chat);
        navProfile = rootView.findViewById(R.id.nav_profile);

        ivHome = rootView.findViewById(R.id.iv_home);
        ivExplore = rootView.findViewById(R.id.iv_explore);
        ivChat = rootView.findViewById(R.id.iv_chat);
        ivProfile = rootView.findViewById(R.id.iv_profile);

        // Establece el ítem actual como seleccionado si lo deseas
        setSelectedNavItem(navHome, ivHome);

        navHome.setOnClickListener(v -> {
            if (!isCurrentFragment(HomeFragment.class)) {
                setSelectedNavItem(navHome, ivHome);
                navigateToFragment(new HomeFragment());
            }
        });

        navExplore.setOnClickListener(v -> {
            if (!isCurrentFragment(HistorialFragment.class)) {
                setSelectedNavItem(navExplore, ivExplore);
                navigateToFragment(new HistorialFragment());
            }
        });

        navChat.setOnClickListener(v -> {
            if (!isCurrentFragment(ChatFragment.class)) {
                setSelectedNavItem(navChat, ivChat);
                navigateToFragment(new ChatFragment());
            }
        });

        navProfile.setOnClickListener(v -> {
            if (!isCurrentFragment(ProfileFragment.class)) {
                setSelectedNavItem(navProfile, ivProfile);
                navigateToFragment(new ProfileFragment());
            }
        });
    }

    private void setSelectedNavItem(LinearLayout navItem, ImageView icon) {
        ivHome.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        ivExplore.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        ivChat.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        ivProfile.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));

        icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.orange));
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null);
        transaction.commit();
    }

    private boolean isCurrentFragment(Class<? extends Fragment> fragmentClass) {
        Fragment current = requireActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        return current != null && fragmentClass.isInstance(current);
    }
}
