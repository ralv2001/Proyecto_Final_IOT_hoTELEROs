package com.example.proyecto_final_hoteleros.auth.password;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.button.MaterialButton;

public class ForgotPasswordFragment extends Fragment {

    private EditText etEmail;
    private MaterialButton btnResetPassword;

    public static ForgotPasswordFragment newInstance() {
        return new ForgotPasswordFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        // Inicializar vistas
        etEmail = view.findViewById(R.id.etEmail);
        btnResetPassword = view.findViewById(R.id.btnResetPassword);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        // Configurar el botón de retroceso
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Configurar el botón de restablecimiento de contraseña
        btnResetPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                // Aquí se implementaría la lógica para enviar el correo de recuperación
                Toast.makeText(getContext(), "Correo de recuperación enviado a: " + email, Toast.LENGTH_SHORT).show();
                // Volver a la pantalla de inicio de sesión
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        // Configurar el listener para el campo de correo electrónico
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se necesita implementación
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No se necesita implementación
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Habilitar o deshabilitar el botón según si hay texto
                boolean isNotEmpty = !s.toString().trim().isEmpty();
                btnResetPassword.setEnabled(isNotEmpty);
                btnResetPassword.setAlpha(isNotEmpty ? 1.0f : 0.4f);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Restaurar las pestañas cuando se cierre este fragmento
        if (getActivity() != null) {
            View tabLayout = getActivity().findViewById(R.id.tabLayout);
            View viewTabIndicatorLogin = getActivity().findViewById(R.id.viewTabIndicatorLogin);
            ViewGroup indicatorLayout = null;

            if (viewTabIndicatorLogin != null) {
                indicatorLayout = (ViewGroup) viewTabIndicatorLogin.getParent();
            }

            // Restaurar la visibilidad
            if (tabLayout != null) tabLayout.setVisibility(View.VISIBLE);
            if (indicatorLayout != null) indicatorLayout.setVisibility(View.VISIBLE);

            // Restaurar el scroll
            ScrollView scrollView = getActivity().findViewById(R.id.scrollViewLogin);
            if (scrollView != null) {
                scrollView.scrollTo(0, 0);
                scrollView.requestLayout();
            }
        }
    }
}