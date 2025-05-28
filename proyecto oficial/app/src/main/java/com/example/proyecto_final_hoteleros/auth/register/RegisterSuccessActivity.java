package com.example.proyecto_final_hoteleros.auth.register;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_final_hoteleros.AuthActivity;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.utils.NotificationHelper;
import com.google.android.material.button.MaterialButton;

public class RegisterSuccessActivity extends AppCompatActivity {

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
                .remove("pdfPath")  // Añadir esta línea para limpiar la ruta del PDF
                .remove("pdfUri")   // Añadir esta línea para limpiar el URI del PDF
                .apply();

        // Obtener el tipo de usuario del intent
        String userType = getIntent().getStringExtra("userType");
        Log.d("RegisterSuccess", "UserType recibido: " + userType);

        boolean isDriver = "driver".equals(userType);
        Log.d("RegisterSuccess", "Is driver: " + isDriver);

        // Actualizar textos para el registro exitoso
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("¡Éxito!");

        TextView tvDescription = findViewById(R.id.tvDescription);

        if (isDriver) {
            // Mensaje para taxistas
            Log.d("RegisterSuccess", "Mostrando mensaje para taxistas");
            tvDescription.setText("¡Felicidades!, su cuenta ha sido creada y será evaluada por un administrador para ser habilitada. Se le notificará por correo electrónico la decisión del administrador.\n\nHaga clic en Continuar para ir al menú principal.");
        } else {
            // Mensaje para clientes
            Log.d("RegisterSuccess", "Mostrando mensaje para clientes");
            tvDescription.setText("¡Felicidades!, su cuenta ha sido creada exitosamente.\nHaga clic en Continuar para ir al menú principal.");
            // AGREGAR NOTIFICACIÓN AQUÍ - después de configurar los textos
            String userName = getIntent().getStringExtra("userName");
            if (userName == null || userName.isEmpty()) {
                userName = "Usuario"; // Valor por defecto
            }

            NotificationHelper notificationHelper = new NotificationHelper(this);
            notificationHelper.showRegistrationCompleteNotification(userType, userName);
            Log.d("RegisterSuccess", "Notificación enviada para " + userType + ": " + userName);
        }

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