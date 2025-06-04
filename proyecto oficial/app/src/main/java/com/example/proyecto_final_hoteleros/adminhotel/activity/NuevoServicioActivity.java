package com.example.proyecto_final_hoteleros.adminhotel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_final_hoteleros.R;

public class NuevoServicioActivity extends AppCompatActivity {

    private EditText etNombre, etDescripcion;
    private ImageView ivImagen;
    private Button btnSeleccionarImagen, btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_hotel_agregar_editar_servicios); // usa tu layout aquí

        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        ivImagen = findViewById(R.id.ivImagen);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        btnGuardar = findViewById(R.id.btnGuardarServicio);

        btnSeleccionarImagen.setOnClickListener(view -> {
            Toast.makeText(this, "Aquí puedes abrir galería o cámara", Toast.LENGTH_SHORT).show();
            // Pendiente: lógica de selección de imagen
        });

        btnGuardar.setOnClickListener(view -> {
            String nombre = etNombre.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();

            if (nombre.isEmpty() || descripcion.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Servicio guardado: " + nombre, Toast.LENGTH_SHORT).show();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("nombre", nombre);
            resultIntent.putExtra("descripcion", descripcion);
// puedes enviar una imagen fija por ahora
            resultIntent.putExtra("imagen", R.drawable.sauna_sample);

            setResult(RESULT_OK, resultIntent);
            finish(); // cerrar pantalla
        });
    }
}

