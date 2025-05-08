package com.example.proyecto_final_hoteleros;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_final_hoteleros.superadmin.AdminHotelActivity;
import com.example.proyecto_final_hoteleros.superadmin.SuperadminActivity;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

                // Iniciar la HomeActivity que contiene el contenedor de fragmentos
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        // Configuración de "Continuar como superadmin"
        LinearLayout layoutContinueAsSuperadmin = findViewById(R.id.layoutContinueAsSuperadmin);
        layoutContinueAsSuperadmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Continuando como superadmin...", Toast.LENGTH_SHORT).show();

                // Iniciar la HomeActivity que contiene el contenedor de fragmentos
                Intent intent = new Intent(MainActivity.this, SuperadminActivity.class);
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
    }

    // Método para navegar a la pantalla de autenticación
    private void goToAuth(String mode) {
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        intent.putExtra("mode", mode); // Para indicar qué pestaña mostrar (login o register)
        startActivity(intent);
    }


}