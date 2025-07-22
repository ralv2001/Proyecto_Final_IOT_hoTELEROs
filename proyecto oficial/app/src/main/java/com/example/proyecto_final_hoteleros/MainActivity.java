package com.example.proyecto_final_hoteleros;

import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseServiceManager;
import com.google.firebase.auth.FirebaseAuth;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity;
import com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity;
import com.example.proyecto_final_hoteleros.utils.AwsFileManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_final_hoteleros.adminhotel.activity.AdminHotelActivity;
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;
import com.example.proyecto_final_hoteleros.utils.GitHubSignInHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth; // Agregar esta línea

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ CONFIGURAR EDGE-TO-EDGE
        enableEdgeToEdge();

        setContentView(R.layout.sistema_activity_main);

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

        // Inicializar Firebase (añade esta línea)
        FirebaseApp.initializeApp(this);

        // Inicializar Firebase Auth  <-- AGREGAR ESTA LÍNEA
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseServiceManager firebaseServiceManager = FirebaseServiceManager.getInstance(this);
        // Verificar si hay vinculación pendiente de GitHub
        //checkPendingGitHubLink();

        // En caso se necesite crear de nuevo el SuperAdmin porque se borró:
        //recreateSuperAdmin();

        // Configuración de botón de registro
        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAuth("register");
            }
        });

        // Configuración de botón de inicio de sesión
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAuth("login");
            }
        });

        // Configuración de "Continuar como huésped"
        LinearLayout layoutContinueAsGuest = findViewById(R.id.layoutContinueAsGuest);
        layoutContinueAsGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Continuando como huésped...", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        LinearLayout layoutContinueAsSuperadmin = findViewById(R.id.layoutContinueAsSuperadmin);
        layoutContinueAsSuperadmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear usuario superadmin si no existe
                com.example.proyecto_final_hoteleros.utils.FirebaseManager firebaseManager =
                        com.example.proyecto_final_hoteleros.utils.FirebaseManager.getInstance();

                firebaseManager.createSuperAdminUser(new com.example.proyecto_final_hoteleros.utils.FirebaseManager.DataCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this,
                                "Usuario superadmin creado. Email: delgadoaquinor@gmail.com, Password: SuperAdmin123!",
                                Toast.LENGTH_LONG).show();

                        // Navegar al login
                        goToAuth("login");
                    }

                    @Override
                    public void onError(String error) {
                        if (error.contains("email address is already in use")) {
                            Toast.makeText(MainActivity.this,
                                    "Superadmin ya existe. Ir a login",
                                    Toast.LENGTH_SHORT).show();
                            goToAuth("login");
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Error: " + error,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // Configuración de "Continuar como admin de hotel"
        // Configuración de "Continuar como admin de hotel"
        // Configuración de "Continuar como admin de hotel"
        LinearLayout layoutContinueAsAdminHotel = findViewById(R.id.layoutContinueAsAdminHotel);
        layoutContinueAsAdminHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Continuando como admin de hotel ...", Toast.LENGTH_SHORT).show();

                // Autenticar o crear usuario de prueba para admin de hotel
                authenticateTestHotelAdmin();
            }
        });

        LinearLayout layoutContinueAsTaxiDriver = findViewById(R.id.layoutContinueAsTaxiDriver);
        layoutContinueAsTaxiDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "🚗 Continuando como taxista...", Toast.LENGTH_SHORT).show();

                // Crear intent con datos simulados para testing
                Intent intent = new Intent(MainActivity.this, DriverActivity.class);

                // ⭐ AGREGAR ESTOS DATOS PARA QUE FUNCIONE CORRECTAMENTE
                intent.putExtra("userId", "taxista_testing_" + System.currentTimeMillis());
                intent.putExtra("userEmail", "taxista.testing@hoteleros.com");
                intent.putExtra("userName", "Taxista de Prueba");
                intent.putExtra("userType", "driver");

                startActivity(intent);
            }
        });
        // ========== TESTS DE CONCURRENCIA - COMENTADO PARA PRODUCCIÓN ==========
        /*
        findViewById(R.id.btnLogin).setOnLongClickListener(v -> {
            Log.d("MainActivity", "Iniciando test de concurrencia normal");
            ConcurrencyTestHelper.testConcurrentRegistrations(this);
            return true;
        });

        findViewById(R.id.btnRegister).setOnLongClickListener(v -> {
            Log.d("MainActivity", "Iniciando test de email duplicado");
            ConcurrencyTestHelper.testDuplicateEmailConcurrency(this);
            return true;
        });
        */

    }
    // ✅ MÉTODO PARA AUTENTICAR ADMIN DE HOTEL DE PRUEBA
    private void authenticateTestHotelAdmin() {
        String testEmail = "adminhotel@test.com";
        String testPassword = "AdminHotel123!";

        // Intentar hacer login primero
        firebaseAuth.signInWithEmailAndPassword(testEmail, testPassword)
                .addOnSuccessListener(authResult -> {
                    Log.d("MainActivity", "✅ Admin de hotel autenticado exitosamente");
                    Toast.makeText(MainActivity.this, "✅ Autenticado como Admin de Hotel", Toast.LENGTH_SHORT).show();

                    // Ir al AdminHotelActivity
                    Intent intent = new Intent(MainActivity.this, AdminHotelActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Log.d("MainActivity", "Usuario no existe, creándolo...");

                    // Si falla el login, crear el usuario
                    createTestHotelAdmin(testEmail, testPassword);
                });
    }
    private void createTestHotelAdmin(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Log.d("MainActivity", "✅ Usuario admin de hotel creado exitosamente");

                    // Crear perfil del usuario en Firestore
                    String userId = authResult.getUser().getUid();
                    createHotelAdminProfile(userId, email);
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "❌ Error creando admin de hotel: " + e.getMessage());

                    if (e.getMessage() != null && e.getMessage().contains("email address is already in use")) {
                        // Si el email ya existe, intentar login otra vez
                        Toast.makeText(MainActivity.this,
                                "Usuario ya existe, reintentando login...",
                                Toast.LENGTH_SHORT).show();
                        authenticateTestHotelAdmin();
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createHotelAdminProfile(String userId, String email) {
        FirebaseManager firebaseManager = FirebaseManager.getInstance();

        // Crear el modelo de usuario para admin de hotel
        com.example.proyecto_final_hoteleros.models.UserModel adminUser = new com.example.proyecto_final_hoteleros.models.UserModel();
        adminUser.setUserId(userId);  // ✅ AGREGAR ESTO
        adminUser.setEmail(email);
        adminUser.setNombres("Admin");
        adminUser.setApellidos("Hotel Test");
        adminUser.setUserType("hotel_admin");  // IMPORTANTE: Este es el rol que necesita!
        adminUser.setTelefono("999888777");
        adminUser.setDireccion("Hotel Central Lima");
        adminUser.setNumeroDocumento("12345678");
        adminUser.setTipoDocumento("DNI");
        adminUser.setFechaNacimiento("01/01/1985");
        adminUser.setActive(true);

        // Guardar en Firestore
        firebaseManager.saveUserData(userId, adminUser, new FirebaseManager.DataCallback() {
            @Override
            public void onSuccess() {
                Log.d("MainActivity", "✅ Perfil de admin hotel creado");
                Toast.makeText(MainActivity.this,
                        "✅ Admin de Hotel creado y autenticado",
                        Toast.LENGTH_LONG).show();

                // ✅ MEJORADO: Ir al AdminHotelActivity con datos
                Intent intent = new Intent(MainActivity.this, AdminHotelActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userName", "Admin Hotel Test");
                intent.putExtra("userEmail", email);
                intent.putExtra("userType", "hotel_admin");
                startActivity(intent);
                finish(); // ✅ AGREGAR para cerrar MainActivity
            }

            @Override
            public void onError(String error) {
                Log.e("MainActivity", "❌ Error creando perfil: " + error);
                Toast.makeText(MainActivity.this,
                        "Error creando perfil: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.d("MainActivity", "=== CONFIGURATION CHANGED ===");
        Log.d("MainActivity", "Orientation: " + (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? "LANDSCAPE" : "PORTRAIT"));
        Log.d("MainActivity", "Preservando estado de pantalla principal...");

        // El estado se mantiene automáticamente
        Log.d("MainActivity", "Estado después de rotación preservado");
    }

    // Método para navegar a la pantalla de autenticación
    private void goToAuth(String mode) {
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        intent.putExtra("mode", mode); // Para indicar qué pestaña mostrar (login o register)
        startActivity(intent);
    }

    // MÉTODO TEMPORAL PARA TESTING AWS - ELIMINAR EN PRODUCCIÓN
    private void testAwsUpload() {
        Log.d("AWSTest", "=== INICIANDO TEST DE AWS ===");

        AwsFileManager awsManager = new AwsFileManager(this);

        // Crear imagen de prueba pequeña
        android.graphics.Bitmap testBitmap = android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888);
        testBitmap.eraseColor(android.graphics.Color.RED);

        awsManager.uploadImage(testBitmap, "test_image.jpg", "test_user", "photos",
                new AwsFileManager.UploadCallback() {
                    @Override
                    public void onSuccess(AwsFileManager.AwsFileInfo fileInfo) {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this,
                                    "✅ AWS Upload exitoso: " + fileInfo.storedName,
                                    Toast.LENGTH_LONG).show();
                            Log.d("AWSTest", "✅ Archivo subido exitosamente:");
                            Log.d("AWSTest", "  - S3 Key: " + fileInfo.s3Key);
                            Log.d("AWSTest", "  - File URL: " + fileInfo.fileUrl);
                            Log.d("AWSTest", "  - Size: " + fileInfo.fileSizeMB + " MB");
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this,
                                    "❌ AWS Error: " + error,
                                    Toast.LENGTH_LONG).show();
                            Log.e("AWSTest", "❌ Error: " + error);
                        });
                    }

                    @Override
                    public void onProgress(int percentage) {
                        Log.d("AWSTest", "📊 Progreso: " + percentage + "%");
                    }
                });
    }

    // ✅ MÉTODO TEMPORAL PARA RECREAR SUPERADMIN
    private void recreateSuperAdmin() {
        FirebaseManager firebaseManager = FirebaseManager.getInstance();
        firebaseManager.createSuperAdminUser(new FirebaseManager.DataCallback() {
            @Override
            public void onSuccess() {
                Log.d("MainActivity", "✅ SuperAdmin recreado automáticamente");
                Toast.makeText(MainActivity.this, "✅ SuperAdmin restaurado", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                Log.e("MainActivity", "❌ Error recreando SuperAdmin: " + error);
                if (error.contains("email address is already in use")) {
                    Log.d("MainActivity", "SuperAdmin ya existe, todo OK");
                } else {
                    Toast.makeText(MainActivity.this, "❌ Error creando SuperAdmin: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void checkPendingGitHubLink() {
        SharedPreferences prefs = getSharedPreferences("github_pending", MODE_PRIVATE);
        String pendingEmail = prefs.getString("pending_email", null);

        if (pendingEmail != null && firebaseAuth.getCurrentUser() != null) {
            String currentEmail = firebaseAuth.getCurrentUser().getEmail();

            if (pendingEmail.equals(currentEmail)) {
                // Mostrar diálogo o snackbar
                new AlertDialog.Builder(this)
                        .setTitle("Vincular GitHub")
                        .setMessage("¿Deseas vincular tu cuenta de GitHub a este perfil?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            linkGitHubAccount();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // Limpiar pendiente
                            prefs.edit().clear().apply();
                        })
                        .show();
            }
        }
    }

    private void linkGitHubAccount() {
        GitHubSignInHelper gitHubHelper = new GitHubSignInHelper(this);
        gitHubHelper.linkGitHubToCurrentUser(new GitHubSignInHelper.GitHubSignInCallback() {
            @Override
            public void onSignInSuccess(FirebaseUser user) {
                Toast.makeText(MainActivity.this,
                        "✅ GitHub vinculado exitosamente!",
                        Toast.LENGTH_LONG).show();

                // Limpiar pendiente
                getSharedPreferences("github_pending", MODE_PRIVATE)
                        .edit().clear().apply();
            }

            @Override
            public void onSignInFailure(String error) {
                Toast.makeText(MainActivity.this,
                        "Error vinculando GitHub: " + error,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSignInCanceled() {
                // Usuario canceló
            }

            @Override
            public void onAccountCollision(String email) {
                // No debería pasar en vinculación
            }
        });
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