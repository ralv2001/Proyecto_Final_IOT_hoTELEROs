package com.example.proyecto_final_hoteleros.auth.password;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_final_hoteleros.AuthActivity;
import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.button.MaterialButton;

public class SuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sistema_activity_password_success);

        // Limpiar datos temporales
        getSharedPreferences("UserData", MODE_PRIVATE)
                .edit()
                .remove("photoPath")
                .remove("photoUri")
                .remove("email")
                .remove("photoSkipped")
                .apply();

        // Verificar si es password reset o registro normal
        String successType = getIntent().getStringExtra("success_type");
        String email = getIntent().getStringExtra("email");

        // Configurar textos según el tipo de éxito
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvDescription = findViewById(R.id.tvDescription);

        if ("password_reset".equals(successType)) {
            tvTitle.setText("¡Contraseña Actualizada!");
            tvDescription.setText("Tu nueva contraseña ha sido configurada exitosamente.\n\n" +
                    "Ya puedes iniciar sesión con tu nueva contraseña en " + email);
        } else {
            tvTitle.setText("¡Éxito!");
            tvDescription.setText("¡Felicidades!, su contraseña ha sido restablecida.\nHaga clic en Continuar para iniciar sesión.");
        }

        // Configurar el botón Continuar
        MaterialButton btnContinuar = findViewById(R.id.btnContinuar);
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Volver a la pantalla de login
                Intent intent = new Intent(SuccessActivity.this, AuthActivity.class);
                intent.putExtra("mode", "login");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}