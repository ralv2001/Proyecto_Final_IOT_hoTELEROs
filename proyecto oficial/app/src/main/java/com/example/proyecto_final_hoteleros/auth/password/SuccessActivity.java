package com.example.proyecto_final_hoteleros.auth.password;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
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
            tvTitle.setText("¡Solicitud Enviada!");

            String customMessage = getIntent().getStringExtra("message");
            if (customMessage != null && !customMessage.isEmpty()) {
                tvDescription.setText(customMessage);
            } else {
                tvDescription.setText("Si tu correo está registrado en nuestro sistema, recibirás un enlace de restablecimiento.\n\nRevisa tu bandeja de entrada y spam.");
            }
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.d("SuccessActivity", "=== CONFIGURATION CHANGED ===");
        Log.d("SuccessActivity", "Orientation: " + (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? "LANDSCAPE" : "PORTRAIT"));
        Log.d("SuccessActivity", "Preservando estado de pantalla de éxito...");

        // Verificar que los datos importantes se mantienen
        String successType = getIntent().getStringExtra("success_type");
        String email = getIntent().getStringExtra("email");

        Log.d("SuccessActivity", "Estado después de rotación:");
        Log.d("SuccessActivity", "  - successType: " + successType);
        Log.d("SuccessActivity", "  - email: " + email);
        Log.d("SuccessActivity", "Estado preservado correctamente");
    }
}