package com.example.proyecto_final_hoteleros.auth.password;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_final_hoteleros.AuthActivity;
import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.button.MaterialButton;

public class SuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sistema_activity_password_success);

        // Limpiar datos temporales del formulario de registro
        getSharedPreferences("UserData", MODE_PRIVATE)
                .edit()
                .remove("photoPath")
                .remove("photoUri")
                .remove("email")
                .remove("photoSkipped")
                .apply();

        // Configurar el botón Continuar
        MaterialButton btnContinuar = findViewById(R.id.btnContinuar);
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Volver a la pantalla de login
                Intent intent = new Intent(SuccessActivity.this, AuthActivity.class);
                // Añadir un extra para indicar que debemos mostrar la pestaña de login
                intent.putExtra("mode", "login");
                // Limpiar el stack de actividades para que el usuario no pueda volver atrás
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}