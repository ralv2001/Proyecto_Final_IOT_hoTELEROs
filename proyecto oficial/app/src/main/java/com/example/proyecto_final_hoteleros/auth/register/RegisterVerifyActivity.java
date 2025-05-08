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
        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putString("email", email);
            args.putString("userType", userType);

            RegisterVerifyCodeFragment fragment = RegisterVerifyCodeFragment.newInstance(email);
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }
}