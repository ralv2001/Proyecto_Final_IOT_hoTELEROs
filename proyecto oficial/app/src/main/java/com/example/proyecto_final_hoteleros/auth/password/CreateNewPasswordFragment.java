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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.auth.login.LoginFragment;
import com.google.android.material.button.MaterialButton;

public class CreateNewPasswordFragment extends Fragment {

    private static final String ARG_EMAIL = "email";

    // Variables para las vistas
    private EditText etContrasena, etConfirmarContrasena;
    private ImageButton ibTogglePassword, ibToggleConfirmPassword;
    private ImageView passwordStrengthBar;
    private ImageView ivReq1Icon, ivReq2Icon, ivReq3Icon;
    private TextView tvReq1, tvReq2, tvReq3;
    private MaterialButton btnContinuar;

    // Estados
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private String email;

    public CreateNewPasswordFragment() {
        // Required empty public constructor
    }

    public static CreateNewPasswordFragment newInstance(String email) {
        CreateNewPasswordFragment fragment = new CreateNewPasswordFragment();
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
        Log.d("PasswordSuccess", "onViewCreated called");
        // Asegurarnos de ocultar las pestañas cuando el fragmento es visible
        hideTabLayout();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Usamos el layout renombrado
        View view = inflater.inflate(R.layout.sistema_fragment_create_new_password, container, false);

        // Inicializar vistas
        etContrasena = view.findViewById(R.id.etContrasena);
        etConfirmarContrasena = view.findViewById(R.id.etConfirmarContrasena);
        ibTogglePassword = view.findViewById(R.id.ibTogglePassword);
        ibToggleConfirmPassword = view.findViewById(R.id.ibToggleConfirmPassword);
        passwordStrengthBar = view.findViewById(R.id.passwordStrengthBar);
        ivReq1Icon = view.findViewById(R.id.ivReq1Icon);
        ivReq2Icon = view.findViewById(R.id.ivReq2Icon);
        ivReq3Icon = view.findViewById(R.id.ivReq3Icon);
        tvReq1 = view.findViewById(R.id.tvReq1);
        tvReq2 = view.findViewById(R.id.tvReq2);
        tvReq3 = view.findViewById(R.id.tvReq3);
        btnContinuar = view.findViewById(R.id.btnContinuar);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        // Configurar listeners para mostrar/ocultar contraseña
        ibTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            togglePasswordVisibility(etContrasena, ibTogglePassword, isPasswordVisible);
        });

        ibToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            togglePasswordVisibility(etConfirmarContrasena, ibToggleConfirmPassword, isConfirmPasswordVisible);
        });

        // Añadir TextWatcher para validar contraseña
        etContrasena.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePassword(s.toString());
                // Verificar si las contraseñas coinciden
                validatePasswordMatch();
                // Verificar todos los campos para el botón continuar
                updateContinueButton();
            }
        });

        // Añadir TextWatcher para confirmar contraseña
        etConfirmarContrasena.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePasswordMatch();
                // Verificar todos los campos para el botón continuar
                updateContinueButton();
            }
        });

        // Configurar botón de volver
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Configurar botón continuar
        btnContinuar.setOnClickListener(v -> {
            if (areAllFieldsValid()) {
                resetPassword();
            } else {
                Toast.makeText(getContext(), "Por favor, complete todos los campos correctamente", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("PasswordSuccess", "onResume called");
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

    // Método para mostrar/ocultar contraseña
    private void togglePasswordVisibility(EditText editText, ImageButton button, boolean isVisible) {
        if (isVisible) {
            // Mostrar contraseña
            editText.setTransformationMethod(null);
            button.setImageResource(R.drawable.ic_visibility_off_custom);
        } else {
            // Ocultar contraseña
            editText.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
            button.setImageResource(R.drawable.ic_visibility_custom);
        }

        // Mover cursor al final del texto
        editText.setSelection(editText.getText().length());
    }

    // Método para validar los requisitos de la contraseña
    private void validatePassword(String password) {
        int progress = 0;

        // Requisito 1: entre 8 y 32 caracteres
        boolean req1Met = password.length() >= 8 && password.length() <= 32;
        if (req1Met) {
            ivReq1Icon.setImageResource(R.drawable.ic_check);
            tvReq1.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            tvReq1.setTypeface(ResourcesCompat.getFont(getContext(), R.font.roboto_regular));
            progress++;
        } else {
            ivReq1Icon.setImageResource(R.drawable.ic_sin_check);
            tvReq1.setTextColor(getResources().getColor(R.color.colorTextTertiary));
            tvReq1.setTypeface(ResourcesCompat.getFont(getContext(), R.font.roboto_bold));
        }

        // Requisito 2: un número y un símbolo
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        boolean req2Met = hasNumber && hasSymbol;
        if (req2Met) {
            ivReq2Icon.setImageResource(R.drawable.ic_check);
            tvReq2.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            tvReq2.setTypeface(ResourcesCompat.getFont(getContext(), R.font.roboto_regular));
            progress++;
        } else {
            ivReq2Icon.setImageResource(R.drawable.ic_sin_check);
            tvReq2.setTextColor(getResources().getColor(R.color.colorTextTertiary));
            tvReq2.setTypeface(ResourcesCompat.getFont(getContext(), R.font.roboto_bold));
        }

        // Requisito 3: una mayúscula
        boolean req3Met = password.matches(".*[A-Z].*");
        if (req3Met) {
            ivReq3Icon.setImageResource(R.drawable.ic_check);
            tvReq3.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            tvReq3.setTypeface(ResourcesCompat.getFont(getContext(), R.font.roboto_regular));
            progress++;
        } else {
            ivReq3Icon.setImageResource(R.drawable.ic_sin_check);
            tvReq3.setTextColor(getResources().getColor(R.color.colorTextTertiary));
            tvReq3.setTypeface(ResourcesCompat.getFont(getContext(), R.font.roboto_bold));
        }

        // Actualizar la barra de progreso según los requisitos cumplidos
        switch (progress) {
            case 0:
                passwordStrengthBar.setImageResource(R.drawable.progress_bar_0);
                break;
            case 1:
                passwordStrengthBar.setImageResource(R.drawable.progress_bar_1);
                break;
            case 2:
                passwordStrengthBar.setImageResource(R.drawable.progress_bar_2);
                break;
            case 3:
                passwordStrengthBar.setImageResource(R.drawable.progress_bar_3);
                break;
        }
    }

    // Método para validar si las contraseñas coinciden
    private void validatePasswordMatch() {
        String password = etContrasena.getText().toString();
        String confirmPassword = etConfirmarContrasena.getText().toString();

        if (!confirmPassword.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                etConfirmarContrasena.setError("Las contraseñas no coinciden");
            } else {
                etConfirmarContrasena.setError(null);
            }
        }
    }

    // Método para verificar si todos los requisitos de la contraseña se cumplen
    private boolean passwordRequirementsMet() {
        String password = etContrasena.getText().toString();

        // Requisito 1: entre 8 y 32 caracteres
        boolean req1Met = password.length() >= 8 && password.length() <= 32;

        // Requisito 2: un número y un símbolo
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        boolean req2Met = hasNumber && hasSymbol;

        // Requisito 3: una mayúscula
        boolean req3Met = password.matches(".*[A-Z].*");

        return req1Met && req2Met && req3Met;
    }

    // Método para verificar si todos los campos son válidos
    private boolean areAllFieldsValid() {
        // Verificar requisitos de contraseña
        boolean passwordValid = passwordRequirementsMet() &&
                !etContrasena.getText().toString().trim().isEmpty();

        // Verificar si las contraseñas coinciden
        boolean passwordsMatch = etContrasena.getText().toString().equals(
                etConfirmarContrasena.getText().toString()) &&
                !etConfirmarContrasena.getText().toString().trim().isEmpty();

        return passwordValid && passwordsMatch;
    }

    // Método para actualizar el estado del botón continuar
    private void updateContinueButton() {
        boolean enableButton = areAllFieldsValid();
        btnContinuar.setEnabled(enableButton);
        btnContinuar.setAlpha(enableButton ? 1.0f : 0.4f);
    }

    // Método para resetear la contraseña
    private void resetPassword() {
        // Aquí implementaríamos la lógica real para cambiar la contraseña en el backend
        // Por ahora, simularemos que fue exitoso
        Toast.makeText(getContext(), "Contraseña actualizada con éxito", Toast.LENGTH_SHORT).show();

        // Iniciar la actividad de éxito
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), SuccessActivity.class);
            startActivity(intent);

            // Opcionalmente, se puede cerrar la actividad actual para evitar que el usuario vuelva atrás (lo haremos XD)
            getActivity().finish();
        }
    }
}