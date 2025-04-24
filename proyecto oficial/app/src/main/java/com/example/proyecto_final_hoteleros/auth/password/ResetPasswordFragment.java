package com.example.proyecto_final_hoteleros.auth.password;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.button.MaterialButton;

public class ResetPasswordFragment extends Fragment {

    private static final String ARG_EMAIL = "email";

    private String email;
    private MaterialButton btnContinuar;

    public ResetPasswordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param email Email del usuario.
     * @return A new instance of fragment ResetPasswordFragment.
     */
    public static ResetPasswordFragment newInstance(String email) {
        ResetPasswordFragment fragment = new ResetPasswordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            email = getArguments().getString(ARG_EMAIL);
        }
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
        View view = inflater.inflate(R.layout.sistema_fragment_reset_password, container, false);

        // Inicializar vistas
        btnContinuar = view.findViewById(R.id.btnContinuar);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        // Configurar botón de volver
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Configurar botón continuar
        btnContinuar.setOnClickListener(v -> {
            // Navegar a la pantalla de crear nueva contraseña
            navigateToCreateNewPassword();
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

    // Método para navegar a la pantalla de crear nueva contraseña
    private void navigateToCreateNewPassword() {
        if (getActivity() != null) {
            // Crear instancia del fragmento para crear nueva contraseña
            CreateNewPasswordFragment newPasswordFragment = CreateNewPasswordFragment.newInstance(email);

            // Realizar la transacción de fragmentos
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, newPasswordFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}