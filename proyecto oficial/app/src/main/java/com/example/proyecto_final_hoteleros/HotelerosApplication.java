package com.example.proyecto_final_hoteleros;

import android.app.Application;
import android.content.SharedPreferences;

public class HotelerosApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Limpiar las fotos temporales al iniciar la aplicación
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        prefs.edit()
                .remove("photoPath")
                .remove("photoUri")
                .apply();
    }
}