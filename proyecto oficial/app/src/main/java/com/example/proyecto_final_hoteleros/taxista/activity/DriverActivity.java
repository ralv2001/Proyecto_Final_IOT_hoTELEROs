package com.example.proyecto_final_hoteleros.taxista.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.proyecto_final_hoteleros.taxista.fragment.TripDetailsFragment;
import com.example.proyecto_final_hoteleros.taxista.model.CheckoutReservation;
import com.example.proyecto_final_hoteleros.taxista.model.SolicitudViaje;
import com.example.proyecto_final_hoteleros.taxista.utils.DriverPreferenceManager;
import com.example.proyecto_final_hoteleros.taxista.utils.NotificationHelper;
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class DriverActivity extends AppCompatActivity {

    private static final String TAG = "DriverActivity";

    // Navegación
    private LinearLayout navMapa;
    private LinearLayout navViajes;
    private LinearLayout navHistorial;
    private LinearLayout navPerfil;
    private View lastSelectedNavItem;

    // Datos del usuario logueado
    private String userId;
    private String userEmail;
    private String userName;
    private String userType;

    // Testing (solo en debug)
    private LinearLayout testButtonsContainer;
    private MaterialButton btnCreateTestData;
    private MaterialButton btnClearTestData;

    // Managers
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_activity_driver);

        Log.d(TAG, "=== INICIANDO DRIVER ACTIVITY ===");

        // Inicializar managers
        firebaseManager = FirebaseManager.getInstance();

        // Recibir datos del intent
        recibirDatosIntent();

        // Inicializar vistas
        inicializarVistas();

        // Configurar listeners
        configurarListeners();

        // Configurar botones de testing (solo en debug)
        configurarTestingButtons();

        // Manejar intents de notificaciones
        handleNotificationIntent(getIntent());

        // Cargar fragment por defecto si no viene de notificación
        if (savedInstanceState == null && !handleNotificationIntent(getIntent())) {
            loadFragment(new DriverMapFragment());
            lastSelectedNavItem = navMapa;
            updateSelectedNavItem(navMapa);
        }

        Log.d(TAG, "✅ DriverActivity inicializada correctamente");
    }

    private void recibirDatosIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("userId");
            userEmail = intent.getStringExtra("userEmail");
            userName = intent.getStringExtra("userName");
            userType = intent.getStringExtra("userType");

            Log.d(TAG, "=== DATOS RECIBIDOS ===");
            Log.d(TAG, "UserID: " + userId);
            Log.d(TAG, "Email: " + userEmail);
            Log.d(TAG, "Name: " + userName);
            Log.d(TAG, "Type: " + userType);
        }
    }

    private void inicializarVistas() {
        // Navegación
        navMapa = findViewById(R.id.nav_mapa);
        navViajes = findViewById(R.id.nav_viajes);
        navHistorial = findViewById(R.id.nav_historial);
        navPerfil = findViewById(R.id.nav_perfil);

        // Testing buttons
        testButtonsContainer = findViewById(R.id.test_buttons_container);
        btnCreateTestData = findViewById(R.id.btnCreateTestData);
        btnClearTestData = findViewById(R.id.btnClearTestData);
    }

    private void configurarListeners() {
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


    /**
     * Configurar botones de testing (siempre visibles para testing)
     */
    private void configurarTestingButtons() {
        // Mostrar siempre los botones (puedes cambiar esto más tarde)
        testButtonsContainer.setVisibility(View.VISIBLE);

        btnCreateTestData.setOnClickListener(v -> {
            Log.d(TAG, "🧪 Creando datos de prueba...");
            crearDatosDePrueba();
        });

        btnClearTestData.setOnClickListener(v -> {
            Log.d(TAG, "🗑️ Limpiando datos de prueba...");
            limpiarDatosDePrueba();
        });
    }

    /**
     * Crear datos de prueba para checkout con verificación previa
     */
    private void crearDatosDePrueba() {
        Log.d(TAG, "🧪 Iniciando creación de datos de prueba...");

        // ✅ VERIFICAR SI YA EXISTEN DATOS ANTES DE CREAR
        Log.d(TAG, "🔍 Verificando si ya existen reservas...");

        firebaseManager.getCheckoutReservations(new FirebaseManager.CheckoutCallback() {
            @Override
            public void onSuccess(List<CheckoutReservation> reservations) {
                Log.d(TAG, "📊 Verificación completada. Reservas existentes: " + reservations.size());

                if (!reservations.isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(DriverActivity.this,
                                "ℹ️ Ya existen " + reservations.size() + " reservas.\n" +
                                        "Ve a la pestaña 'Viajes' para verlas.",
                                Toast.LENGTH_LONG).show();

                        // Cambiar automáticamente a la pestaña de viajes
                        goToViajesTab();
                    });
                    return;
                }

                // ✅ SI NO HAY DATOS, PROCEDER CON LA CREACIÓN
                Log.d(TAG, "✅ No hay reservas existentes, procediendo con la creación...");
                proceedWithDataCreation();
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "⚠️ Error verificando reservas existentes: " + error);
                Log.d(TAG, "🔄 Procediendo con la creación de datos de todos modos...");

                // Si hay error al verificar, proceder con la creación
                runOnUiThread(() -> {
                    Toast.makeText(DriverActivity.this,
                            "⚠️ No se pudieron verificar datos existentes.\nCreando datos nuevos...",
                            Toast.LENGTH_SHORT).show();
                });

                proceedWithDataCreation();
            }
        });
    }

    /**
     * Proceder con la creación de datos después de verificación
     */
    private void proceedWithDataCreation() {
        Log.d(TAG, "🚀 Iniciando creación efectiva de datos...");

        // ✅ DESHABILITAR BOTÓN Y MOSTRAR ESTADO
        btnCreateTestData.setEnabled(false);
        btnCreateTestData.setText("🔄 Creando...");

        // ✅ MOSTRAR PROGRESO AL USUARIO
        runOnUiThread(() -> {
            Toast.makeText(this,
                    "🔄 Creando datos de prueba...\nEspera un momento.",
                    Toast.LENGTH_SHORT).show();
        });

        firebaseManager.createSampleCheckoutReservations(new FirebaseManager.DataCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Datos de prueba creados exitosamente");

                runOnUiThread(() -> {
                    Toast.makeText(DriverActivity.this,
                            "✅ ¡Datos de prueba creados!\n" +
                                    "🚕 Ve a la pestaña 'Viajes' para ver las solicitudes de checkout.",
                            Toast.LENGTH_LONG).show();

                    // ✅ CAMBIAR AUTOMÁTICAMENTE A LA PESTAÑA DE VIAJES
                    goToViajesTab();

                    // ✅ REHABILITAR BOTÓN
                    btnCreateTestData.setEnabled(true);
                    btnCreateTestData.setText("🧪 Crear Datos");

                    Log.d(TAG, "🎯 Usuario dirigido a la pestaña de viajes");
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error creando datos de prueba: " + error);

                runOnUiThread(() -> {
                    Toast.makeText(DriverActivity.this,
                            "❌ Error creando datos de prueba:\n" + error +
                                    "\n\nInténtalo de nuevo.",
                            Toast.LENGTH_LONG).show();

                    // ✅ REHABILITAR BOTÓN EN CASO DE ERROR
                    btnCreateTestData.setEnabled(true);
                    btnCreateTestData.setText("🧪 Crear Datos");
                });
            }
        });

        // ✅ TIMEOUT DE SEGURIDAD PARA EL BOTÓN
        new android.os.Handler().postDelayed(() -> {
            if (!btnCreateTestData.isEnabled()) {
                Log.w(TAG, "⏰ Timeout de seguridad - rehabilitando botón");
                runOnUiThread(() -> {
                    btnCreateTestData.setEnabled(true);
                    btnCreateTestData.setText("🧪 Crear Datos");
                    Toast.makeText(DriverActivity.this,
                            "⏰ Operación tomó demasiado tiempo. Inténtalo de nuevo.",
                            Toast.LENGTH_SHORT).show();
                });
            }
        }, 15000); // 15 segundos timeout
    }

    /**
     * Método para ir directamente a la pestaña de viajes (útil después de crear datos)
     */
    public void goToViajesTab() {
        Log.d(TAG, "🎯 Navegando a la pestaña de viajes...");

        try {
            updateSelectedNavItem(navViajes);
            loadFragment(new DriverViajesFragment());

            Log.d(TAG, "✅ Navegación a viajes completada");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error navegando a viajes: " + e.getMessage(), e);
        }
    }

    /**
     * Limpiar datos de prueba
     */
    private void limpiarDatosDePrueba() {
        Log.d(TAG, "🗑️ Limpiando datos de prueba...");

        // Por ahora, solo simular la limpieza
        // En producción, podrías implementar un método en FirebaseManager para eliminar datos de test

        btnClearTestData.setEnabled(false);
        btnClearTestData.setText("Limpiando...");

        // Simular operación asíncrona
        new android.os.Handler().postDelayed(() -> {
            runOnUiThread(() -> {
                Toast.makeText(this,
                        "🗑️ Datos de prueba limpiados\n(Funcionalidad en desarrollo)",
                        Toast.LENGTH_SHORT).show();

                btnClearTestData.setEnabled(true);
                btnClearTestData.setText("🗑️ Limpiar");

                // Refrescar la pestaña de viajes si está activa
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof DriverViajesFragment) {
                    loadFragment(new DriverViajesFragment());
                }
            });
        }, 1000);
    }

    // GETTERS PARA QUE LOS FRAGMENTS ACCEDAN A LOS DATOS
    public String getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public String getUserName() { return userName; }
    public String getUserType() { return userType; }

    private void loadFragment(Fragment fragment) {
        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            Log.d(TAG, "✅ Fragment cargado: " + fragment.getClass().getSimpleName());
        } catch (Exception e) {
            Log.e(TAG, "❌ Error cargando fragment: " + e.getMessage(), e);
        }
    }

    private void updateSelectedNavItem(View selectedItem) {
        // Reset todos los items
        resetNavigationItems();

        // Destacar el seleccionado
        selectedItem.setSelected(true);
        selectedItem.setAlpha(1.0f);

        lastSelectedNavItem = selectedItem;
    }

    private void resetNavigationItems() {
        navMapa.setSelected(false);
        navViajes.setSelected(false);
        navHistorial.setSelected(false);
        navPerfil.setSelected(false);

        navMapa.setAlpha(0.6f);
        navViajes.setAlpha(0.6f);
        navHistorial.setAlpha(0.6f);
        navPerfil.setAlpha(0.6f);
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
        if ("show_details".equals(action)) {
            // ✅ IR DIRECTAMENTE A TRIPDETAILSFRAGMENT:
            loadFragment(new DriverViajesFragment());
            updateSelectedNavItem(navViajes);

            // Crear datos de ejemplo para mostrar detalles
            SolicitudViaje ejemploNotificacion = new SolicitudViaje();
            ejemploNotificacion.setId("notification_trip");
            ejemploNotificacion.setHotelName(getIntent().getStringExtra("hotel_name"));
            ejemploNotificacion.setClientName(getIntent().getStringExtra("client_name"));
            ejemploNotificacion.setOriginAddress(getIntent().getStringExtra("hotel_address"));
            ejemploNotificacion.setDestinationAddress("Aeropuerto Jorge Chávez");
            ejemploNotificacion.setEstimatedTime(25);
            ejemploNotificacion.setPrice(0.0);
            ejemploNotificacion.setRating(4.5f);
            ejemploNotificacion.setTipoServicio("checkout_gratuito");

            // Navegar a detalles
            TripDetailsFragment detailsFragment = TripDetailsFragment.newInstance(ejemploNotificacion);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailsFragment)
                    .addToBackStack("notification_details")
                    .commit();

            Toast.makeText(this, "📱 Solicitud desde notificación", Toast.LENGTH_SHORT).show();

        } else if ("accept".equals(action)) {
            // Mantener lógica original
            loadFragment(new DriverViajesFragment());
            updateSelectedNavItem(navViajes);
            Toast.makeText(this, "Viaje aceptado desde notificación", Toast.LENGTH_SHORT).show();
        } else if ("reject".equals(action)) {
            Toast.makeText(this, "Viaje rechazado desde notificación", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleStatusAction(String action) {
        if ("go_online".equals(action)) {
            // Cargar fragment de mapa y activar estado online
            loadFragment(new DriverMapFragment());
            updateSelectedNavItem(navMapa);
            Toast.makeText(this, "Estado cambiado a disponible", Toast.LENGTH_SHORT).show();
        } else if ("go_offline".equals(action)) {
            loadFragment(new DriverMapFragment());
            updateSelectedNavItem(navMapa);
            Toast.makeText(this, "Estado cambiado a no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "DriverActivity resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "DriverActivity paused");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "DriverActivity destroyed");
    }

    /**
     * Método para testing - forzar refresh de viajes
     */
    public void refreshViajesFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof DriverViajesFragment) {
            loadFragment(new DriverViajesFragment());
        }
    }

    /**
     * Método para mostrar mensaje desde fragments
     */
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}