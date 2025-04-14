package com.example.proyecto_final_hoteleros;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private ImageButton ibTogglePassword;
    private TextView tvErrorMessage;
    private TextView tvForgotPassword;
    private Button btnContinue;
    private Button btnFacebookLogin;
    private Button btnGoogleLogin;
    private TextView tvRegisterPrompt;
    private TextView tvLoginTab;
    private TextView tvRegisterTab;
    private View viewTabIndicator;


    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ibTogglePassword = findViewById(R.id.ibTogglePassword);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnContinue = findViewById(R.id.btnContinue);
        btnFacebookLogin = findViewById(R.id.btnFacebookLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        tvLoginTab = findViewById(R.id.tvLoginTab);
        tvRegisterTab = findViewById(R.id.tvRegisterTab);

        // Ocultar mensaje de error inicialmente
        tvErrorMessage.setVisibility(View.INVISIBLE);

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
                Toast.makeText(LoginActivity.this, "Recuperar contraseña", Toast.LENGTH_SHORT).show();
                // Implementar navegación a pantalla de recuperación de contraseña
            }
        });

        btnFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Iniciar sesión con Facebook", Toast.LENGTH_SHORT).show();
                // Implementar inicio de sesión con Facebook
            }
        });

        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Iniciar sesión con Google", Toast.LENGTH_SHORT).show();
                // Implementar inicio de sesión con Google
            }
        });

        tvRegisterPrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Ir a pantalla de registro", Toast.LENGTH_SHORT).show();
                // Implementar navegación a pantalla de registro
            }
        });

        tvRegisterTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cambiar a pestaña de registro
                tvLoginTab.setTextColor(getResources().getColor(android.R.color.darker_gray));
                tvRegisterTab.setTextColor(getResources().getColor(R.color.colorAccent));
                // Implementar lógica para mostrar pantalla de registro
            }
        });
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

        // Validación básica
        if (email.isEmpty()) {
            Toast.makeText(this, "Ingrese su correo electrónico", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Ingrese su contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simulación de login (para demostración)
        if (email.equals("luchito@stuardiño.com") && password.equals("password")) {
            Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
            tvErrorMessage.setVisibility(View.INVISIBLE);
            // Implementar navegación a la siguiente pantalla
        } else {
            tvErrorMessage.setVisibility(View.VISIBLE);
        }
    }
}