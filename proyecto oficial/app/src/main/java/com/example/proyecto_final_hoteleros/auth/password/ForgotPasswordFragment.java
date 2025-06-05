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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
                                "Email de restablecimiento enviado a " + email,
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

                        String friendlyError = translateFirebaseError(error);
                        Toast.makeText(getContext(), friendlyError, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void navigateToVerifyCode(String email) {
        if (getActivity() != null) {
            VerifyCodeFragment verifyCodeFragment = VerifyCodeFragment.newInstance(email);

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            transaction.replace(R.id.fragmentContainer, verifyCodeFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
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
            return "No hay ninguna cuenta registrada con este correo electrónico.";
        } else if (error.contains("invalid-email")) {
            return "El formato del correo electrónico no es válido.";
        } else if (error.contains("too-many-requests")) {
            return "Demasiados intentos. Inténtalo más tarde.";
        } else if (error.contains("network-request-failed")) {
            return "Error de conexión. Verifica tu internet e inténtalo de nuevo.";
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
}