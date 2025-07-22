package com.example.proyecto_final_hoteleros.auth.register;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyecto_final_hoteleros.R;

public class RegisterVerifyActivity extends AppCompatActivity {

    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ CONFIGURAR EDGE-TO-EDGE
        enableEdgeToEdge();

        setContentView(R.layout.sistema_activity_register_verify);

        // ✅ CONFIGURAR WINDOW INSETS - VERSIÓN CORREGIDA (SIN TOP PADDING)
        View rootLayout = findViewById(android.R.id.content).getRootView();
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            boolean isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            int bottomPadding = Math.max(systemBars.bottom, ime.bottom);

            View mainLayout = findViewById(android.R.id.content);
            if (mainLayout != null) {
                mainLayout.setPadding(
                        mainLayout.getPaddingLeft(),
                        0,               // 🎯 SIN top padding - el XML maneja el margen
                        mainLayout.getPaddingRight(),
                        bottomPadding    // 🎯 Solo bottom padding dinámico
                );
            }

            return insets;
        });

        // Obtener el email y userType del intent
        String email = "";
        String userType = "client"; // Valor por defecto

        if (getIntent() != null) {
            if (getIntent().hasExtra("email")) {
                email = getIntent().getStringExtra("email");
                Log.d("RegisterVerify", "Email recibido en intent: " + email);
            }

            if (getIntent().hasExtra("userType")) {
                userType = getIntent().getStringExtra("userType");
                Log.d("RegisterVerify", "UserType recibido en intent: " + userType);
            }
        }

        // Si email aún es null, intenta recuperarlo de SharedPreferences
        if (email == null || email.isEmpty()) {
            email = getSharedPreferences("UserData", MODE_PRIVATE)
                    .getString("email", "");
            Log.d("RegisterVerify", "Email recuperado de SharedPreferences: " + email);
        }

        // En caso de que aún sea null, usar un valor por defecto SOLO para desarrollo
        if (email == null || email.isEmpty()) {
            email = "correo@example.com";
            Log.e("RegisterVerify", "Usando email por defecto: " + email);
        }

        // Guardar el userType en SharedPreferences para asegurar persistencia
        getSharedPreferences("UserData", MODE_PRIVATE)
                .edit()
                .putString("userType", userType)
                .apply();

        // Cargar el fragmento de verificación de código con el email y userType
        // Obtener registrationId del intent si está disponible
        int registrationId = getIntent().getIntExtra("registrationId", -1);

        // Cargar el fragmento de verificación de código con el email, userType y registrationId
        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putString("email", email);
            args.putString("userType", userType);
            args.putInt("registrationId", registrationId);

            RegisterVerifyCodeFragment fragment = RegisterVerifyCodeFragment.newInstance(email);
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.d("RegisterVerify", "=== CONFIGURATION CHANGED ===");
        Log.d("RegisterVerify", "Orientation: " + (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? "LANDSCAPE" : "PORTRAIT"));
        Log.d("RegisterVerify", "Preservando estado de verificación...");

        // El estado del fragmento se mantiene automáticamente
        Log.d("RegisterVerify", "Estado después de rotación preservado");
    }

    // ✅ MÉTODO PARA HABILITAR EDGE-TO-EDGE CON ICONOS OSCUROS (VERSIÓN SEGURA)
    private void enableEdgeToEdge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );

        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }
}