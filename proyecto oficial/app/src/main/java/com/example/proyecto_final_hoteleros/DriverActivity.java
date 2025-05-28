package com.example.proyecto_final_hoteleros;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.proyecto_final_hoteleros.taxista.fragment.DriverHistorialFragment;
import com.example.proyecto_final_hoteleros.taxista.fragment.DriverMapFragment;
import com.example.proyecto_final_hoteleros.taxista.fragment.DriverPerfilFragment;
import com.example.proyecto_final_hoteleros.taxista.fragment.DriverViajesFragment;

public class DriverActivity extends AppCompatActivity {

    private LinearLayout navMapa;
    private LinearLayout navViajes;
    private LinearLayout navHistorial;
    private LinearLayout navPerfil;

    private View lastSelectedNavItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        // Inicializar las vistas de navegación
        navMapa = findViewById(R.id.nav_mapa);
        navViajes = findViewById(R.id.nav_viajes);
        navHistorial = findViewById(R.id.nav_historial);
        navPerfil = findViewById(R.id.nav_perfil);

        // Configurar listeners
        navMapa.setOnClickListener(v -> {
            updateSelectedNavItem(v);
            loadFragment(new DriverMapFragment());
        });

        navViajes.setOnClickListener(v -> {
            updateSelectedNavItem(v);
            loadFragment(new DriverViajesFragment());
        });

        navHistorial.setOnClickListener(v -> {
            updateSelectedNavItem(v);
            loadFragment(new DriverHistorialFragment());
        });

        navPerfil.setOnClickListener(v -> {
            updateSelectedNavItem(v);
            loadFragment(new DriverPerfilFragment());
        });

        // Cargar el fragment del mapa por defecto
        if (savedInstanceState == null) {
            loadFragment(new DriverMapFragment());
            // Marcar visualmente el ítem seleccionado
            lastSelectedNavItem = navMapa;
            updateSelectedNavItem(navMapa);
        }
    }

    private void updateSelectedNavItem(View selectedItem) {
        // Desmarcar el elemento anteriormente seleccionado
        if (lastSelectedNavItem != null) {
            lastSelectedNavItem.setBackground(null);
            // Aquí podrías también cambiar el color del texto e ícono a no seleccionado
        }

        // Marcar el nuevo elemento seleccionado
        selectedItem.setBackgroundResource(android.R.drawable.list_selector_background);
        // También podrías cambiar el color del texto e ícono a seleccionado

        lastSelectedNavItem = selectedItem;
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}