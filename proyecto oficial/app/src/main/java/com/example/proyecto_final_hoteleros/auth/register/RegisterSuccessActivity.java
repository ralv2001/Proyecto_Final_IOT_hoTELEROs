package com.example.proyecto_final_hoteleros.auth.register;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import androidx.annotation.NonNull;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_final_hoteleros.AuthActivity;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.utils.NotificationHelper;
import com.google.android.material.button.MaterialButton;

public class RegisterSuccessActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 200; // NUEVA CONSTANTE
    private NotificationHelper notificationHelper;
    private String userType;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ CONFIGURAR EDGE-TO-EDGE
        enableEdgeToEdge();

        setContentView(R.layout.sistema_activity_password_success);

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

        // Limpiar datos temporales del formulario de registro
        getSharedPreferences("UserData", MODE_PRIVATE)
                .edit()
                .remove("photoPath")
                .remove("photoUri")
                .remove("email")
                .remove("photoSkipped")
                .remove("pdfPath")
                .remove("pdfUri")
                .apply();

        // Obtener el tipo de usuario del intent
        userType = getIntent().getStringExtra("userType");
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
        }

        // CONFIGURAR NOTIFICACIÓN AQUÍ
        userName = getIntent().getStringExtra("userName");
        if (userName == null || userName.isEmpty()) {
            userName = "Usuario"; // Valor por defecto
        }

        Log.d("RegisterSuccess", "UserName recibido: " + userName);
        Log.d("RegisterSuccess", "Iniciando proceso de notificación...");

        notificationHelper = new NotificationHelper(this);

        // Verificar y solicitar permisos de notificación antes de enviar
        checkAndRequestNotificationPermission();

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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.d("RegisterSuccess", "=== CONFIGURATION CHANGED ===");
        Log.d("RegisterSuccess", "Orientation: " + (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? "LANDSCAPE" : "PORTRAIT"));
        Log.d("RegisterSuccess", "Preservando estado de éxito...");

        // Verificar que los datos importantes se mantienen
        Log.d("RegisterSuccess", "Estado después de rotación:");
        Log.d("RegisterSuccess", "  - userType: " + userType);
        Log.d("RegisterSuccess", "  - userName: " + userName);
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                Log.d("RegisterSuccess", "Solicitando permisos de notificación...");

                // Mostrar explicación antes de solicitar el permiso
                showNotificationPermissionExplanation();
            } else {
                Log.d("RegisterSuccess", "✅ Permisos de notificación ya concedidos");
                // Ya tenemos permisos, enviar notificación
                sendNotification();
            }
        } else {
            Log.d("RegisterSuccess", "Android < 13, no se requieren permisos de notificación");
            // En versiones anteriores a Android 13, no se requieren permisos especiales
            sendNotification();
        }
    }

    private void showNotificationPermissionExplanation() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Recibir Notificaciones")
                .setMessage("¿Te gustaría recibir notificaciones importantes sobre tu cuenta y el estado de tu registro?")
                .setPositiveButton("Sí, activar", (dialog, which) -> {
                    // Solicitar permisos
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                REQUEST_NOTIFICATION_PERMISSION);
                    }
                })
                .setNegativeButton("No, gracias", (dialog, which) -> {
                    Log.d("RegisterSuccess", "Usuario declinó notificaciones");
                    Toast.makeText(this, "Puedes activar las notificaciones después en Configuración", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false) // No permitir cancelar tocando fuera
                .show();
    }

    private void sendNotification() {
        Log.d("RegisterSuccess", "=== ENVIANDO NOTIFICACIÓN ===");
        Log.d("RegisterSuccess", "NotificationHelper: " + (notificationHelper != null ? "OK" : "NULL"));
        Log.d("RegisterSuccess", "UserType: " + userType);
        Log.d("RegisterSuccess", "UserName: " + userName);

        if (notificationHelper != null && userType != null && userName != null) {
            try {
                notificationHelper.showRegistrationCompleteNotification(userType, userName);
                Log.d("RegisterSuccess", "✅ Notificación enviada exitosamente para " + userType + ": " + userName);
                Toast.makeText(this, "¡Notificación enviada!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("RegisterSuccess", "❌ Error enviando notificación: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.e("RegisterSuccess", "❌ Error: Datos faltantes para notificación");
            Log.e("RegisterSuccess", "  - NotificationHelper: " + (notificationHelper != null));
            Log.e("RegisterSuccess", "  - UserType: " + userType);
            Log.e("RegisterSuccess", "  - UserName: " + userName);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("RegisterSuccess", "✅ Permisos de notificación concedidos");
                sendNotification();
                Toast.makeText(this, "¡Genial! Recibirás notificaciones importantes", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("RegisterSuccess", "❌ Permisos de notificación denegados");
                Toast.makeText(this, "Puedes activar las notificaciones después en Configuración", Toast.LENGTH_LONG).show();

                // Opcional: Ofrecer ir a configuración
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    showNotificationSettingsDialog();
                }
            }
        }
    }

    private void showNotificationSettingsDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Activar Notificaciones")
                .setMessage("Para recibir actualizaciones importantes, puedes activar las notificaciones en Configuración.")
                .setPositiveButton("Ir a Configuración", (dialog, which) -> {
                    // Abrir configuración de notificaciones de la app
                    Intent intent = new Intent();
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
                    startActivity(intent);
                })
                .setNegativeButton("Más tarde", null)
                .show();
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