package com.example.proyecto_final_hoteleros.auth.password;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

public class ForgotPasswordFragment extends Fragment {

    private static final String TAG = "ForgotPasswordFragment";

    private EditText etEmail;
    private MaterialButton btnResetPassword;
    private FirebaseManager firebaseManager;

    private LinearLayout layoutGeneralError;
    private TextView tvGeneralError;

    public static ForgotPasswordFragment newInstance() {
        return new ForgotPasswordFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideTabLayout();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sistema_fragment_forgot_password, container, false);

        // Inicializar servicios
        firebaseManager = FirebaseManager.getInstance();

        // Inicializar vistas
        etEmail = view.findViewById(R.id.etEmail);
        btnResetPassword = view.findViewById(R.id.btnResetPassword);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        // Nuevo
        layoutGeneralError = view.findViewById(R.id.layoutGeneralError);
        tvGeneralError = view.findViewById(R.id.tvGeneralError);

        // Configurar botón de retroceso
        btnBack.setOnClickListener(v -> {
            etEmail.setError(null);
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Configurar botón de reset
        btnResetPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (!email.isEmpty() && etEmail.getError() == null) {
                verifyEmailAndSendCode(email);
            } else {
                if (etEmail.getError() != null) {
                    Toast.makeText(getContext(), "Por favor, ingrese un correo electrónico válido", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Por favor, ingrese su correo electrónico", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Configurar validación de email
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateEmail(s.toString());
                updateButtonState();

                // Limpiar errores cuando el usuario empiece a escribir
                clearForgotPasswordErrors();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        hideTabLayout();
    }

    private void verifyEmailAndSendCode(String email) {
        Log.d(TAG, "=== ENVIANDO EMAIL DE RESET CON FIREBASE ===");

        // Limpiar errores previos
        clearForgotPasswordErrors();

        // Deshabilitar botón
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Enviando email...");

        // Usar Firebase para enviar email de reset
        firebaseManager.sendPasswordResetEmail(email, new FirebaseManager.DataCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d(TAG, "✅ Email de reset enviado exitosamente");
                        Toast.makeText(getContext(),
                                "Si el correo existe en nuestro sistema, recibirás un enlace de restablecimiento",
                                Toast.LENGTH_LONG).show();

                        // Navegar directamente a pantalla de éxito
                        navigateToSuccess(email);
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnResetPassword.setEnabled(true);
                        btnResetPassword.setText("Restablecer contraseña");

                        // Mostrar error visual
                        showForgotPasswordError(error);
                    });
                }
            }
        });
    }

    private void updateButtonState() {
        boolean isNotEmpty = !etEmail.getText().toString().trim().isEmpty();
        boolean isValid = etEmail.getError() == null;
        btnResetPassword.setEnabled(isNotEmpty && isValid);
        btnResetPassword.setAlpha((isNotEmpty && isValid) ? 1.0f : 0.4f);
    }

    private void hideTabLayout() {
        if (getActivity() != null) {
            View tabLayout = getActivity().findViewById(R.id.tabLayout);
            View viewTabIndicatorLogin = getActivity().findViewById(R.id.viewTabIndicatorLogin);
            ViewGroup indicatorLayout = null;

            if (viewTabIndicatorLogin != null) {
                indicatorLayout = (ViewGroup) viewTabIndicatorLogin.getParent();
            }

            if (tabLayout != null) {
                tabLayout.setVisibility(View.GONE);
            }

            if (indicatorLayout != null) {
                indicatorLayout.setVisibility(View.GONE);
            }
        }
    }

    private void validateEmail(String email) {
        if (email.isEmpty()) {
            etEmail.setError(null);
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Formato de correo electrónico inválido");
            return;
        }

        List<String> validDomains = Arrays.asList(
                "gmail.com", "hotmail.com", "yahoo.es", "pucp.edu.pe", "outlook.com",
                "icloud.com", "yahoo.com", "live.com", "msn.com", "protonmail.com",
                "yahoo.com.mx", "hotmail.es", "me.com", "aol.com", "mail.com"
        );

        String domain = email.substring(email.lastIndexOf("@") + 1).toLowerCase();
        if (!validDomains.contains(domain)) {
            etEmail.setError("Dominio de correo no reconocido");
            return;
        }

        etEmail.setError(null);
    }

    private String translateFirebaseError(String error) {
        if (error == null) return "Error desconocido";

        if (error.contains("user-not-found")) {
            return "El correo introducido no coincide con ninguno registrado en nuestra base de datos";
        } else if (error.contains("invalid-email")) {
            return "El formato del correo electrónico no es válido";
        } else if (error.contains("too-many-requests")) {
            return "Demasiados intentos. Inténtalo más tarde";
        } else if (error.contains("network-request-failed")) {
            return "Error de conexión. Verifica tu internet e inténtalo de nuevo";
        } else {
            return "Error: " + error;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getActivity() != null && !getActivity().isFinishing()) {
            View tabLayout = getActivity().findViewById(R.id.tabLayout);
            View viewTabIndicatorLogin = getActivity().findViewById(R.id.viewTabIndicatorLogin);
            ViewGroup indicatorLayout = null;

            if (viewTabIndicatorLogin != null) {
                indicatorLayout = (ViewGroup) viewTabIndicatorLogin.getParent();
            }

            if (tabLayout != null) {
                tabLayout.setVisibility(View.VISIBLE);
            }

            if (indicatorLayout != null) {
                indicatorLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void navigateToSuccess(String email) {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), SuccessActivity.class);
            intent.putExtra("success_type", "password_reset");
            intent.putExtra("email", email);
            intent.putExtra("message", "Te hemos enviado un enlace de restablecimiento a tu correo electrónico. Haz clic en el enlace para crear una nueva contraseña.");
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void showForgotPasswordError(String error) {
        // Cambiar el borde del campo de email a rojo
        etEmail.setBackgroundResource(R.drawable.sistema_se_ff0000_sw2cr12);

        // Mostrar mensaje general de error
        String userFriendlyError = translateFirebaseError(error);
        tvGeneralError.setText("¡Ups! " + userFriendlyError);
        layoutGeneralError.setVisibility(View.VISIBLE);

        Log.d(TAG, "Mostrando error de forgot password: " + userFriendlyError);
    }

    private void clearForgotPasswordErrors() {
        // Restaurar borde normal del campo de email
        etEmail.setBackgroundResource(R.drawable.se1e1e1sw2cr12);

        // Ocultar mensajes de error
        layoutGeneralError.setVisibility(View.GONE);

        Log.d(TAG, "Errores de forgot password limpiados");
    }
}