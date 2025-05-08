package com.example.proyecto_final_hoteleros.superadmin;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.example.proyecto_final_hoteleros.R;

public class ListaSolicitudesActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private AutoCompleteTextView dropdownSolicitudes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superadmin_lista_solicitudes);

        // Vincular el botón de retroceso
        btnBack = findViewById(R.id.btn_back);

        // Configurar el evento de clic para el botón de retroceso
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Llamar al método para regresar a la actividad anterior
                onBackPressed();
            }
        });

        // Vincular el dropdown
        dropdownSolicitudes = findViewById(R.id.dropdown_hoteles);

        String[] hoteles = new String[] {
                "Todos los Hoteles",
                "Belmond Miraflores Park",
                "JW Marriott Hotel",
                "Hilton Lima Miraflores",
                "Country Club Lima Hotel",
                "Sheraton Lima Hotel & Convention Center"
        };

        // Adaptador para el AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, hoteles);

        // Asignar el adaptador al dropdown
        dropdownSolicitudes.setAdapter(adapter);

        // Mostrar el dropdown al hacer clic
        dropdownSolicitudes.setOnClickListener(v -> dropdownSolicitudes.showDropDown());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Añadir animación de transición al retroceder
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
