package com.example.proyecto_final_hoteleros.superadmin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.example.proyecto_final_hoteleros.R;

public class ReporteReservasActivity extends AppCompatActivity {

    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superadmin_reporte_reservas);

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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Añadir animación de transición al retroceder
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
