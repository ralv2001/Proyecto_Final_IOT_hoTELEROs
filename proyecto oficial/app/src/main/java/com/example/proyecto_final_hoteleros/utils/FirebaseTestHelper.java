package com.example.proyecto_final_hoteleros.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.example.proyecto_final_hoteleros.models.UserModel;

public class FirebaseTestHelper {

    private static final String TAG = "FirebaseTestHelper";
    private final Context context;
    private final FirebaseManager firebaseManager;

    public FirebaseTestHelper(Context context) {
        this.context = context;
        this.firebaseManager = FirebaseManager.getInstance();
    }

    public void runAllTests() {
        Log.d(TAG, "=== INICIANDO TESTS DE FIREBASE ===");

        // Test 1: Probar conexión
        testConnection();

        // Test 2: Probar registro de usuario (después de 3 segundos)
        new android.os.Handler().postDelayed(() -> {
            testUserRegistration();
        }, 3000);
    }

    public void testConnection() {
        Log.d(TAG, "--- TEST 1: CONEXIÓN A FIREBASE ---");

        firebaseManager.testConnection(new FirebaseManager.DataCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Conexión a Firebase exitosa");
                showToast("✅ Firebase conectado correctamente");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error de conexión: " + error);
                showToast("❌ Error conectando a Firebase: " + error);
            }
        });
    }

    public void testUserRegistration() {
        Log.d(TAG, "--- TEST 2: REGISTRO DE USUARIO ---");

        String testEmail = "test" + System.currentTimeMillis() + "@hoteleros.com";
        String testPassword = "Test123!";

        Log.d(TAG, "Registrando usuario de prueba: " + testEmail);

        firebaseManager.registerUser(testEmail, testPassword, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                Log.d(TAG, "✅ Usuario de prueba registrado: " + userId);

                // Crear datos de prueba
                UserModel testUser = new UserModel();
                testUser.setUserId(userId);
                testUser.setUserType("client");
                testUser.setNombres("Usuario");
                testUser.setApellidos("De Prueba");
                testUser.setEmail(testEmail);
                testUser.setFechaNacimiento("01/01/1990");
                testUser.setTelefono("999-888-777");
                testUser.setTipoDocumento("DNI");
                testUser.setNumeroDocumento("12345678");
                testUser.setDireccion("Dirección de prueba 123");

                // Guardar en Firestore
                testSaveUserData(testUser);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error registrando usuario de prueba: " + error);
                showToast("❌ Error en registro de prueba: " + error);
            }
        });
    }

    private void testSaveUserData(UserModel testUser) {
        Log.d(TAG, "--- TEST 3: GUARDAR DATOS EN FIRESTORE ---");

        firebaseManager.saveUserData(testUser.getUserId(), testUser, new FirebaseManager.DataCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Datos guardados en Firestore");
                showToast("✅ Datos guardados en Firestore");

                // Test final: obtener datos
                testGetUserData(testUser.getUserId());
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error guardando en Firestore: " + error);
                showToast("❌ Error guardando en Firestore: " + error);
            }
        });
    }

    private void testGetUserData(String userId) {
        Log.d(TAG, "--- TEST 4: OBTENER DATOS DE FIRESTORE ---");

        firebaseManager.getUserData(userId, new FirebaseManager.UserCallback() {
            @Override
            public void onUserFound(UserModel user) {
                Log.d(TAG, "✅ Datos obtenidos de Firestore:");
                Log.d(TAG, "   Nombre: " + user.getFullName());
                Log.d(TAG, "   Email: " + user.getEmail());
                Log.d(TAG, "   Tipo: " + user.getUserType());
                showToast("✅ Todos los tests completados exitosamente!");
            }

            @Override
            public void onUserNotFound() {
                Log.e(TAG, "❌ Usuario no encontrado en Firestore");
                showToast("❌ Usuario no encontrado en Firestore");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error obteniendo datos: " + error);
                showToast("❌ Error obteniendo datos: " + error);
            }
        });
    }

    public void testDriverRegistration() {
        Log.d(TAG, "--- TEST ESPECIAL: REGISTRO DE TAXISTA ---");

        String testEmail = "driver" + System.currentTimeMillis() + "@hoteleros.com";
        String testPassword = "Driver123!";

        firebaseManager.registerUser(testEmail, testPassword, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                Log.d(TAG, "✅ Taxista de prueba registrado: " + userId);

                // Crear datos de taxista
                UserModel testDriver = new UserModel();
                testDriver.setUserId(userId);
                testDriver.setUserType("driver");
                testDriver.setNombres("Taxista");
                testDriver.setApellidos("De Prueba");
                testDriver.setEmail(testEmail);
                testDriver.setFechaNacimiento("01/01/1985");
                testDriver.setTelefono("999-777-555");
                testDriver.setTipoDocumento("DNI");
                testDriver.setNumeroDocumento("87654321");
                testDriver.setDireccion("Dirección taxista 456");
                testDriver.setPlacaVehiculo("ABC123");

                // Guardar como taxista pendiente
                firebaseManager.savePendingDriver(testDriver.getUserId(), testDriver, new FirebaseManager.DataCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Taxista guardado en pending_drivers");
                        showToast("✅ Test de taxista completado!");
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "❌ Error guardando taxista: " + error);
                        showToast("❌ Error en test de taxista: " + error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error registrando taxista: " + error);
                showToast("❌ Error registrando taxista: " + error);
            }
        });
    }

    private void showToast(String message) {
        if (context != null) {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            });
        }
    }
}