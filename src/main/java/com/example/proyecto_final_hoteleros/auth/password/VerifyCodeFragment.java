package com.example.proyecto_final_hoteleros.auth.password;

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
import com.example.proyecto_final_hoteleros.utils.PasswordResetManager;
import com.google.android.material.button.MaterialButton;

public class VerifyCodeFragment extends Fragment {

    private static final String TAG = "VerifyCodeFragment";
    private static final String ARG_EMAIL = "email";

    private EditText etCode1, etCode2, etCode3, etCode4, etCode5;
    private MaterialButton btnVerifyCode;
    private TextView tvEmailSent, tvResendCode;
    private String email;
    private PasswordResetManager resetManager;

    public static VerifyCodeFragment newInstance(String email) {
        VerifyCodeFragment fragment = new VerifyCodeFragment();
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
        hideTabLayout();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sistema_fragment_verify_code, container, false);

        // Inicializar reset manager
        resetManager = new PasswordResetManager(getActivity());

        // Inicializar vistas
        etCode1 = view.findViewById(R.id.etCode1);
        etCode2 = view.findViewById(R.id.etCode2);
        etCode3 = view.findViewById(R.id.etCode3);
        etCode4 = view.findViewById(R.id.etCode4);
        etCode5 = view.findViewById(R.id.etCode5);
        btnVerifyCode = view.findViewById(R.id.btnVerifyEmail);
        tvEmailSent = view.findViewById(R.id.tvEmailSent);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        // Configurar texto del email
        if (email != null && !email.isEmpty()) {
            String maskedEmail = maskEmail(email);
            tvEmailSent.setText("Hemos enviado un código a " + maskedEmail);
        }

        // Configurar focus automático entre campos
        setupCodeInputs();

        // Configurar botón de verificación
        btnVerifyCode.setOnClickListener(v -> {
            String code = getCompleteCode();
            if (code.length() == 5) {
                verifyCode(code);
            } else {
                Toast.makeText(getContext(), "Por favor ingrese el código completo", Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar reenvío de código
        tvResendCode.setOnClickListener(v -> {
            resendCode();
        });

        // Configurar botón de retroceso
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
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

            if (tabLayout != null) {
                tabLayout.setVisibility(View.GONE);
            }

            if (indicatorLayout != null) {
                indicatorLayout.setVisibility(View.GONE);
            }
        }
    }

    private void setupCodeInputs() {
        EditText[] editTexts = new EditText[]{etCode1, etCode2, etCode3, etCode4, etCode5};

        for (int i = 0; i < editTexts.length; i++) {
            final int currentIndex = i;
            editTexts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && currentIndex < editTexts.length - 1) {
                        editTexts[currentIndex + 1].requestFocus();
                    }
                    checkAllFieldsFilled();
                }
            });

            editTexts[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && currentIndex > 0 &&
                        editTexts[currentIndex].getText().toString().isEmpty()) {
                    editTexts[currentIndex - 1].requestFocus();
                    editTexts[currentIndex - 1].setText("");
                    return true;
                }
                return false;
            });
        }

        etCode1.requestFocus();
    }

    private void checkAllFieldsFilled() {
        boolean allFilled = !etCode1.getText().toString().isEmpty() &&
                !etCode2.getText().toString().isEmpty() &&
                !etCode3.getText().toString().isEmpty() &&
                !etCode4.getText().toString().isEmpty() &&
                !etCode5.getText().toString().isEmpty();

        btnVerifyCode.setEnabled(allFilled);
        btnVerifyCode.setAlpha(allFilled ? 1.0f : 0.4f);
    }

    private String getCompleteCode() {
        return etCode1.getText().toString() +
                etCode2.getText().toString() +
                etCode3.getText().toString() +
                etCode4.getText().toString() +
                etCode5.getText().toString();
    }

    private void verifyCode(String code) {
        Log.d(TAG, "=== VERIFICANDO CÓDIGO ===");
        Log.d(TAG, "Código ingresado: " + code);

        // Deshabilitar botón
        btnVerifyCode.setEnabled(false);
        btnVerifyCode.setText("Verificando...");

        resetManager.validateCode(code, new PasswordResetManager.ValidationCallback() {
            @Override
            public void onValidCode(String email) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d(TAG, "✅ Código válido, navegando a crear nueva contraseña");
                        Toast.makeText(getContext(), "Código verificado correctamente", Toast.LENGTH_SHORT).show();

                        // Navegar a crear nueva contraseña
                        navigateToCreateNewPassword(email);
                    });
                }
            }

            @Override
            public void onInvalidCode() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnVerifyCode.setEnabled(true);
                        btnVerifyCode.setText("Verificar código");

                        Toast.makeText(getContext(), "Código incorrecto. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show();
                        clearCodeFields();
                    });
                }
            }

            @Override
            public void onExpiredCode() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnVerifyCode.setEnabled(true);
                        btnVerifyCode.setText("Verificar código");

                        Toast.makeText(getContext(), "El código ha expirado. Solicita uno nuevo.", Toast.LENGTH_LONG).show();
                        clearCodeFields();
                    });
                }
            }

            @Override
            public void onMaxAttemptsReached() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Máximo de intentos alcanzado. Regresando...", Toast.LENGTH_LONG).show();

                        // Regresar a la pantalla anterior
                        getActivity().getSupportFragmentManager().popBackStack();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnVerifyCode.setEnabled(true);
                        btnVerifyCode.setText("Verificar código");

                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void navigateToCreateNewPassword(String email) {
        if (getActivity() != null) {
            CreateNewPasswordFragment newPasswordFragment = CreateNewPasswordFragment.newInstance(email);

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            transaction.replace(R.id.fragmentContainer, newPasswordFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void resendCode() {
        Log.d(TAG, "=== REENVIANDO CÓDIGO ===");

        if (email != null && !email.isEmpty()) {
            resetManager.startPasswordReset(email, new PasswordResetManager.ResetCallback() {
                @Override
                public void onCodeSent(String maskedEmail) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Nuevo código enviado a " + maskedEmail, Toast.LENGTH_SHORT).show();
                            clearCodeFields();
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error reenviando código: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        }
    }

    private void clearCodeFields() {
        etCode1.setText("");
        etCode2.setText("");
        etCode3.setText("");
        etCode4.setText("");
        etCode5.setText("");
        etCode1.requestFocus();
    }

    private String maskEmail(String email) {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            return email;
        }

        int atIndex = email.indexOf('@');
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (username.length() > 0) {
            return username.charAt(0) + "*****" + domain;
        } else {
            return "*****" + domain;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (resetManager != null) {
            resetManager.cleanup();
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
}