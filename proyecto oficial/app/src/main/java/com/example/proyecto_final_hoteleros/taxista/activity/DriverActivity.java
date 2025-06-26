package com.example.proyecto_final_hoteleros.taxista.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.taxista.fragment.DriverHistorialFragment;
import com.example.proyecto_final_hoteleros.taxista.fragment.DriverMapFragment;
import com.example.proyecto_final_hoteleros.taxista.fragment.DriverPerfilFragment;
import com.example.proyecto_final_hoteleros.taxista.fragment.DriverViajesFragment;
import com.example.proyecto_final_hoteleros.taxista.utils.DriverPreferenceManager;
import com.example.proyecto_final_hoteleros.taxista.utils.NotificationHelper;

public class DriverActivity extends AppCompatActivity {

    private LinearLayout navMapa;
    private LinearLayout navViajes;
    private LinearLayout navHistorial;
    private LinearLayout navPerfil;

    private View lastSelectedNavItem;

    // 🔥 NUEVOS CAMPOS PARA DATOS DEL USUARIO LOGUEADO
    private String userId;
    private String userEmail;
    private String userName;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_activity_driver);

        // 🔥 AGREGAR: Recibir datos del intent
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("userId");
            userEmail = intent.getStringExtra("userEmail");
            userName = intent.getStringExtra("userName");
            userType = intent.getStringExtra("userType");

            android.util.Log.d("DriverActivity", "=== DATOS RECIBIDOS ===");
            android.util.Log.d("DriverActivity", "UserId: " + userId);
            android.util.Log.d("DriverActivity", "Email: " + userEmail);
            android.util.Log.d("DriverActivity", "Name: " + userName);
            android.util.Log.d("DriverActivity", "Type: " + userType);
        }

        // Inicializar las vistas de navegación
        navMapa = findViewById(R.id.nav_mapa);
        navViajes = findViewById(R.id.nav_viajes);
        navHistorial = findViewById(R.id.nav_historial);
        navPerfil = findViewById(R.id.nav_perfil);

        // Configurar listeners existentes...
        setupNavigationListeners();

        // Manejar intents de notificaciones
        handleNotificationIntent(getIntent());

        // Cargar fragment por defecto si no viene de notificación
        if (savedInstanceState == null && !handleNotificationIntent(getIntent())) {
            loadFragment(new DriverMapFragment());
            lastSelectedNavItem = navMapa;
            updateSelectedNavItem(navMapa);
        }
    }

    // 🔥 GETTERS PARA QUE LOS FRAGMENTS ACCEDAN A LOS DATOS
    public String getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public String getUserName() { return userName; }
    public String getUserType() { return userType; }

    private void setupNavigationListeners() {
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
    }

    private boolean handleNotificationIntent(Intent intent) {
        if (intent != null) {
            // Manejar fragmento específico desde notificación
            String openFragment = intent.getStringExtra("open_fragment");
            if (openFragment != null) {
                switch (openFragment) {
                    case "mapa":
                        loadFragment(new DriverMapFragment());
                        updateSelectedNavItem(navMapa);
                        return true;
                    case "viajes":
                        loadFragment(new DriverViajesFragment());
                        updateSelectedNavItem(navViajes);
                        return true;
                    case "perfil":
                        loadFragment(new DriverPerfilFragment());
                        updateSelectedNavItem(navPerfil);
                        return true;
                }
            }

            // Manejar acciones de notificaciones de viaje
            String tripAction = intent.getStringExtra("trip_action");
            if (tripAction != null) {
                handleTripAction(tripAction);
                return true;
            }

            // Manejar acciones de estado
            String statusAction = intent.getStringExtra("status_action");
            if (statusAction != null) {
                handleStatusAction(statusAction);
                return true;
            }
        }
        return false;
    }

    private void handleTripAction(String action) {
        // Cargar fragment de viajes y realizar acción
        loadFragment(new DriverViajesFragment());
        updateSelectedNavItem(navViajes);

        if ("accept".equals(action)) {
            // Lógica para aceptar viaje automáticamente
            // Puedes implementar esto más adelante
            Toast.makeText(this, "Viaje aceptado desde notificación", Toast.LENGTH_SHORT).show();
        } else if ("reject".equals(action)) {
            Toast.makeText(this, "Viaje rechazado desde notificación", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleStatusAction(String action) {
        if ("disconnect".equals(action)) {
            // Cambiar a fragment de mapa y desconectar
            loadFragment(new DriverMapFragment());
            updateSelectedNavItem(navMapa);

            // Desconectar conductor
            DriverPreferenceManager preferenceManager = new DriverPreferenceManager(this);
            preferenceManager.setDriverAvailable(false);

            NotificationHelper notificationHelper = new NotificationHelper(this);
            notificationHelper.hideOnlineStatusNotification();

            Toast.makeText(this, "Te has desconectado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNotificationIntent(intent);
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