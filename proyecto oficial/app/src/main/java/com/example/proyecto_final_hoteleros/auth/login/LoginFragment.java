package com.example.proyecto_final_hoteleros.auth.login;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;

import com.example.proyecto_final_hoteleros.AuthActivity;
import com.example.proyecto_final_hoteleros.R;

import java.util.Arrays;
import java.util.List;

public class LoginFragment extends Fragment {

    private LoginViewModel mViewModel;

    private EditText etEmail;
    private EditText etPassword;
    private ImageButton ibTogglePassword;
    private TextView tvErrorMessage;
    private TextView tvForgotPassword;
    private Button btnContinue;
    private Button btnFacebookLogin;
    private Button btnGoogleLogin;
    private TextView tvRegisterPrompt;

    private boolean isPasswordVisible = false;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sistema_fragment_login, container, false);
        // Inicializar vistas
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        ibTogglePassword = view.findViewById(R.id.ibTogglePassword);
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        btnContinue = view.findViewById(R.id.btnContinue);
        btnFacebookLogin = view.findViewById(R.id.btnFacebookLogin);
        btnGoogleLogin = view.findViewById(R.id.btnGoogleLogin);
        tvRegisterPrompt = view.findViewById(R.id.tvRegisterPrompt);

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
                validateEmail(s.toString());
            }
        });

        // Configurar TextWatcher para validación de campos
        TextWatcher loginFieldsWatcher = new TextWatcher() {
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
                // Habilitar o deshabilitar el botón según si ambos campos tienen texto
                boolean emailNotEmpty = !etEmail.getText().toString().trim().isEmpty();
                boolean passwordNotEmpty = !etPassword.getText().toString().trim().isEmpty();
                boolean enableButton = emailNotEmpty && passwordNotEmpty;

                btnContinue.setEnabled(enableButton);
                btnContinue.setAlpha(enableButton ? 1.0f : 0.4f);
            }
        };

        // Aplicar el TextWatcher a ambos campos
        etEmail.addTextChangedListener(loginFieldsWatcher);
        etPassword.addTextChangedListener(loginFieldsWatcher);

        // Ocultar mensaje de error inicialmente
        tvErrorMessage.setVisibility(View.GONE);

        // Configurar listeners
        ibTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AuthActivity) getActivity()).goToForgotPassword();
            }
        });

        btnFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Iniciar sesión con Facebook", Toast.LENGTH_SHORT).show();
                // Implementar inicio de sesión con Facebook
            }
        });

        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Iniciar sesión con Google", Toast.LENGTH_SHORT).show();
                // Implementar inicio de sesión con Google
            }
        });

        tvRegisterPrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cambiar a la pestaña de registro
                if (getActivity() instanceof AuthActivity) {
                    ((AuthActivity) getActivity()).goToRegister();
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        // TODO: Use the ViewModel
    }

    // Si necesitas manejar el botón de retroceso, hazlo de manera muy simple:
    @Override
    public void onResume() {
        super.onResume();
        // Cualquier lógica adicional que necesites
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar cualquier estado persistente si es necesario
    }

    // Añade también este métodito para asegurar que el callback se limpia apropiadamente
    @Override
    public void onDetach() {
        super.onDetach();
        // El callback se eliminará automáticamente ya que está asociado al ciclo de vida del fragmento
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Ocultar contraseña
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ibTogglePassword.setImageResource(R.drawable.ic_visibility_custom);
        } else {
            // Mostrar contraseña
            etPassword.setTransformationMethod(null);
            ibTogglePassword.setImageResource(R.drawable.ic_visibility_off_custom);
        }
        isPasswordVisible = !isPasswordVisible;

        // Mover cursor al final del texto
        etPassword.setSelection(etPassword.getText().length());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Las validaciones básicas ya se hacen con el TextWatcher,
        // pero podemos agregar validaciones más específicas aquí
        boolean isValid = true;

        // Verificar si hay errores en el campo de email
        if (etEmail.getError() != null) {
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // El resto del métodito se mantiene igual
        // Simulación de login (para demostración)
        if (email.equals("luchito@stuardiño.com") && password.equals("password")) {
            Toast.makeText(getContext(), "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
            tvErrorMessage.setVisibility(View.INVISIBLE);
            // Implementar navegación a la siguiente pantalla
        } else {
            tvErrorMessage.setVisibility(View.VISIBLE);
        }
    }

    // Añadir justo antes del último corchete de cierre de la clase
    private void validateEmail(String email) {
        if (email.isEmpty()) {
            etEmail.setError(null);
            return;
        }

        // Validar formato básico con Patterns de Android
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Formato de correo electrónico inválido");
            return;
        }

        // Lista de dominios válidos
        List<String> validDomains = Arrays.asList(
                "gmail.com", "hotmail.com", "yahoo.es", "pucp.edu.pe", "outlook.com",
                "icloud.com", "yahoo.com", "live.com", "msn.com", "protonmail.com",
                "yahoo.com.mx", "hotmail.es", "me.com", "aol.com", "mail.com"
        );

        // Obtener el dominio del correo y validar si está en nuestra lista
        String domain = email.substring(email.lastIndexOf("@") + 1).toLowerCase();
        if (!validDomains.contains(domain)) {
            etEmail.setError("Dominio de correo no reconocido");
            return;
        }

        // Si pasa todas las validaciones, eliminar error
        etEmail.setError(null);
    }

}