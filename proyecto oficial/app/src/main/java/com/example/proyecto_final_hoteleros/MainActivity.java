package com.example.proyecto_final_hoteleros;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity;
import com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity;
import com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity;
import com.example.proyecto_final_hoteleros.utils.AwsFileManager;
import com.example.proyecto_final_hoteleros.utils.DatabaseTestHelper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_final_hoteleros.adminhotel.activity.AdminHotelActivity;
import com.example.proyecto_final_hoteleros.utils.FirebaseTestHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializar Firebase (añade esta línea)
        FirebaseApp.initializeApp(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.sistema_activity_main);

        // Configurar sistema de insets para pantallas con notch
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
                finish(); // Opcional: cerrar MainActivity
            }
        });
        LinearLayout layoutContinueAsSuperadmin = findViewById(R.id.layoutContinueAsSuperadmin);
        layoutContinueAsSuperadmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Continuando como superadmin...", Toast.LENGTH_SHORT).show();

                // Navegar a SuperAdminActivity
                Intent intent = new Intent(MainActivity.this, SuperAdminActivity.class);
                startActivity(intent);
            }
        });

        // Configuración de "Continuar como admin de hotel"
        LinearLayout layoutContinueAsAdminHotel = findViewById(R.id.layoutContinueAsAdminHotel);
        layoutContinueAsAdminHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Continuando como admin de hotel ...", Toast.LENGTH_SHORT).show();

                // Iniciar la HomeActivity que contiene el contenedor de fragmentos
                Intent intent = new Intent(MainActivity.this, AdminHotelActivity.class);
                startActivity(intent);
            }
        });
        LinearLayout layoutContinueAsTaxiDriver = findViewById(R.id.layoutContinueAsTaxiDriver);
        layoutContinueAsTaxiDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Continuando como taxista...", Toast.LENGTH_SHORT).show();

                // Iniciar la HomeActivity que contiene el contenedor de fragmentos
                Intent intent = new Intent(MainActivity.this, DriverActivity.class);
                startActivity(intent);
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


}