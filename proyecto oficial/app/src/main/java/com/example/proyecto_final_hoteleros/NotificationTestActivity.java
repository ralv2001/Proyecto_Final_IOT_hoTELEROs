package com.example.proyecto_final_hoteleros;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.NotificationService;

/**
 * Activity for testing notifications
 * You can use this during development to test your notifications
 */
public class NotificationTestActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private NotificationService notificationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_test);

        // Initialize notification service
        notificationService = new NotificationService(this);

        // Setup test buttons
        Button btnTestBooking = findViewById(R.id.btnTestBooking);
        Button btnTestCheckIn = findViewById(R.id.btnTestCheckIn);
        Button btnTestCheckOut = findViewById(R.id.btnTestCheckOut);

        // Check and request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }
        }

        // Set click listeners
        btnTestBooking.setOnClickListener(v -> {
            notificationService.testNotification(1);
            Toast.makeText(this, "Enviando notificación de reserva", Toast.LENGTH_SHORT).show();
        });

        btnTestCheckIn.setOnClickListener(v -> {
            notificationService.testNotification(2);
            Toast.makeText(this, "Enviando notificación de check-in", Toast.LENGTH_SHORT).show();
        });

        btnTestCheckOut.setOnClickListener(v -> {
            notificationService.testNotification(3);
            Toast.makeText(this, "Enviando notificación de check-out", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "El permiso de notificaciones es necesario para mostrar alertas",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}