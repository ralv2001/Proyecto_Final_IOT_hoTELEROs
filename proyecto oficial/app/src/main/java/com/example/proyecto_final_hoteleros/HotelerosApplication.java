package com.example.proyecto_final_hoteleros;

import android.app.Application;
import android.content.SharedPreferences;

public class HotelerosApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Solo limpiar los datos si la aplicación se está iniciando desde cero
        // (no durante una rotación de pantalla o reinicio de actividad)
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        boolean isRegistrationInProgress = prefs.contains("email");

        if (!isRegistrationInProgress) {
            prefs.edit()
                    .remove("photoPath")
                    .remove("photoUri")
                    .remove("pdfPath")
                    .remove("pdfUri")
                    .apply();
        }
    }
}