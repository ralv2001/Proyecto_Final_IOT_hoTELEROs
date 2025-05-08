package com.example.proyecto_final_hoteleros.auth.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_final_hoteleros.AuthActivity;
import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.button.MaterialButton;

public class RegisterSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sistema_activity_password_success);

        // Actualizar textos para el registro exitoso
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("¡Éxito!");

        TextView tvDescription = findViewById(R.id.tvDescription);
        tvDescription.setText("¡Felicidades!, su cuenta ha sido creada exitosamente.\nHaga clic en Continuar para ir al menú principal.");

        // Configurar el botón Continuar
        MaterialButton btnContinuar = findViewById(R.id.btnContinuar);
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Volver a la pantalla de login
                Intent intent = new Intent(RegisterSuccessActivity.this, AuthActivity.class);
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