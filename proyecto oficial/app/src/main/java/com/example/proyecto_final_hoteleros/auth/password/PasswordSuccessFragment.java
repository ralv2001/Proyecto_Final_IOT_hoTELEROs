package com.example.proyecto_final_hoteleros.auth.password;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.auth.login.LoginFragment;
import com.google.android.material.button.MaterialButton;

public class PasswordSuccessFragment extends Fragment {

    public static PasswordSuccessFragment newInstance() {
        return new PasswordSuccessFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Asegurarnos de ocultar las pestañas cuando el fragmento es visible
        hideTabLayout();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.sistema_fragment_password_success, container, false);

        MaterialButton btnContinuar = view.findViewById(R.id.btnContinuar);

        // Configurar botón continuar
        btnContinuar.setOnClickListener(v -> {
            // Navegar de vuelta a la pantalla de login
            if (getActivity() != null) {
                // Limpiar todo el back stack
                FragmentManager fm = getActivity().getSupportFragmentManager();
                for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
                    fm.popBackStack();
                }

                // Reemplazar con la pantalla de login
                fm.beginTransaction()
                        .replace(R.id.fragmentContainer, new LoginFragment())
                        .commit();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Asegurarnos de que las pestañas estén ocultas cuando el fragmento retoma el foco
        hideTabLayout();
    }

    private void hideTabLayout() {
        if (getActivity() != null) {
            View tabLayout = getActivity().findViewById(R.id.tabLayout);
            View viewTabIndicatorLogin = getActivity().findViewById(R.id.viewTabIndicatorLogin);
            ViewGroup indicatorLayout = null;

            if (viewTabIndicatorLogin != null) {
                indicatorLayout = (ViewGroup) viewTabIndicatorLogin.getParent();
            }

            // Ocultar elementos
            if (tabLayout != null) {
                tabLayout.setVisibility(View.GONE);
            }

            if (indicatorLayout != null) {
                indicatorLayout.setVisibility(View.GONE);
            }
        }
    }
}