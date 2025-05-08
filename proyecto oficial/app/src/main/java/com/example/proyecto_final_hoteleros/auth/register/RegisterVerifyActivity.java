package com.example.proyecto_final_hoteleros.auth.register;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
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
        setContentView(R.layout.sistema_activity_register_verify);

        // Obtener el email del intent directamente
        String email = "";
        if (getIntent() != null && getIntent().hasExtra("email")) {
            email = getIntent().getStringExtra("email");
            Log.d("RegisterVerify", "Email recibido en intent: " + email);
        } else {
            // Obtener el email del ViewModel como fallback
            RegisterViewModel viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
            email = viewModel.getEmail();
            Log.d("RegisterVerify", "Email obtenido del ViewModel: " + email);

            // Si todavía es null, intenta recuperarlo de SharedPreferences
            if (email == null || email.isEmpty()) {
                email = getSharedPreferences("UserData", MODE_PRIVATE)
                        .getString("email", "");
                Log.d("RegisterVerify", "Email recuperado de SharedPreferences: " + email);
            }
        }

        // En caso de que aún sea null, usar un valor por defecto SOLO para desarrollo
        if (email == null || email.isEmpty()) {
            email = "correo@example.com";
            Log.e("RegisterVerify", "Usando email por defecto: " + email);
        }

        // Cargar el fragmento de verificación de código con el email
        if (savedInstanceState == null) {
            Log.d("RegisterVerify", "Creando fragmento con email: " + email);
            RegisterVerifyCodeFragment fragment = RegisterVerifyCodeFragment.newInstance(email);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }
}