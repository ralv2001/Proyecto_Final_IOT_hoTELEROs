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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.button.MaterialButton;

public class ForgotPasswordFragment extends Fragment {

    private EditText etEmail;
    private MaterialButton btnResetPassword;

    public static ForgotPasswordFragment newInstance() {
        return new ForgotPasswordFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Asegurarnos de ocultar las pestañas cuando el fragmento es visible
        hideTabLayout();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sistema_fragment_forgot_password, container, false);

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
                // Simulamos envío del código por correo
                Toast.makeText(getContext(), "Código de verificación enviado a: " + email, Toast.LENGTH_SHORT).show();

                // Navegar a la pantalla de verificación de código
                navigateToVerifyCode(email);
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

    private void navigateToVerifyCode(String email) {
        if (getActivity() != null) {
            // Crear instancia del fragmento de verificación con el email como argumento
            VerifyCodeFragment verifyCodeFragment = VerifyCodeFragment.newInstance(email);

            // Iniciar la transacción
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            // Reemplazar el fragmento actual con el de verificación
            transaction.replace(R.id.fragmentContainer, verifyCodeFragment);
            transaction.addToBackStack(null);

            // Ejecutar la transacción
            transaction.commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // NO restauramos las pestañas aquí, ya que podríamos estar navegando a otro fragmento sin pestañas
        // La responsabilidad de mostrar las pestañas la dejamos en los fragmentos que sí las necesitan
    }
}