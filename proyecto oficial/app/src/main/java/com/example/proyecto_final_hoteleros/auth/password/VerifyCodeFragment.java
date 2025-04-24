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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.button.MaterialButton;

public class VerifyCodeFragment extends Fragment {

    private EditText etCode1, etCode2, etCode3, etCode4, etCode5;
    private MaterialButton btnVerifyCode;
    private TextView tvEmailSent, tvResendCode;
    private String email;

    public static VerifyCodeFragment newInstance(String email) {
        VerifyCodeFragment fragment = new VerifyCodeFragment();
        Bundle args = new Bundle();
        args.putString("email", email);
        fragment.setArguments(args);
        return fragment;
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
        View view = inflater.inflate(R.layout.sistema_fragment_verify_code, container, false);

        // Obtener el email de los argumentos
        if (getArguments() != null) {
            email = getArguments().getString("email", "");
        }

        // Inicializar vistas
        etCode1 = view.findViewById(R.id.etCode1);
        etCode2 = view.findViewById(R.id.etCode2);
        etCode3 = view.findViewById(R.id.etCode3);
        etCode4 = view.findViewById(R.id.etCode4);
        etCode5 = view.findViewById(R.id.etCode5);
        btnVerifyCode = view.findViewById(R.id.btnVerifyCode);
        tvEmailSent = view.findViewById(R.id.tvEmailSent);
        tvResendCode = view.findViewById(R.id.tvResendCode);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        // Configurar el texto del email enmascarado
        if (!email.isEmpty()) {
            String maskedEmail = maskEmail(email);
            tvEmailSent.setText("Hemos enviado un código a " + maskedEmail);
        }

        // Configurar el focus automático
        setupCodeInputs();

        // Configurar el botón de verificación
        btnVerifyCode.setOnClickListener(v -> {
            String code = getCompleteCode();
            if (code.length() == 5) {
                verifyCode(code);
            } else {
                Toast.makeText(getContext(), "Por favor, ingrese el código completo", Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar el reenvío de código
        tvResendCode.setOnClickListener(v -> {
            resendCode();
        });

        // Configurar el botón de retroceso
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // NO restauramos las pestañas aquí, ya que podríamos estar navegando a otro fragmento sin pestañas
    }

    private void setupCodeInputs() {
        // Asignar TextWatchers a cada EditText para moverse al siguiente cuando se ingresa un dígito
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
                    // Si se ingresó un dígito y no es el último campo, mover al siguiente
                    if (s.length() == 1 && currentIndex < editTexts.length - 1) {
                        editTexts[currentIndex + 1].requestFocus();
                    }

                    // Verificar si todos los campos están llenos para habilitar el botón
                    checkAllFieldsFilled();
                }
            });

            // Para permitir borrar con tecla de retroceso y moverse al campo anterior
            editTexts[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && currentIndex > 0 &&
                        editTexts[currentIndex].getText().toString().isEmpty()) {
                    // Si el campo actual está vacío y se presiona borrar, ir al campo anterior
                    editTexts[currentIndex - 1].requestFocus();
                    editTexts[currentIndex - 1].setText("");
                    return true;
                }
                return false;
            });
        }

        // Enfocar el primer campo al iniciar
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

    private String maskEmail(String email) {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            return email;
        }

        int atIndex = email.indexOf('@');
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        // Si el nombre de usuario tiene menos de 3 caracteres, mostrar solo el primer carácter
        if (username.length() <= 3) {
            return username.charAt(0) + "*****" + domain;
        }
        // Si tiene más de 3 caracteres, mostrar el primero y el último
        else {
            return username.charAt(0) + "*****" + domain;
        }
    }

    private void verifyCode(String code) {
        // TODO: Aquí se implementaría la verificación real del código con el backend
        // Por ahora, simulamos que 12345 o 83600 son códigos válidos
        if (code.equals("12345") || code.equals("83600")) {
            Toast.makeText(getContext(), "Código verificado correctamente", Toast.LENGTH_SHORT).show();
            // Navegación a la siguiente pantalla (cambio de contraseña)
            // TODO: Implementar navegación a la pantalla de nueva contraseña
        } else {
            Toast.makeText(getContext(), "Código incorrecto. Por favor, inténtelo de nuevo.", Toast.LENGTH_SHORT).show();
        }
    }

    private void resendCode() {
        // TODO: Implementar lógica para reenviar el código
        Toast.makeText(getContext(), "Se ha reenviado el código a tu correo", Toast.LENGTH_SHORT).show();

        // Limpiar los campos de código
        etCode1.setText("");
        etCode2.setText("");
        etCode3.setText("");
        etCode4.setText("");
        etCode5.setText("");

        // Volver a poner el foco en el primer campo
        etCode1.requestFocus();
    }
}