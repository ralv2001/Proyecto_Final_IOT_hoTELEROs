package com.example.proyecto_final_hoteleros.adminhotel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.Habitacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HabitacionesActivity extends AppCompatActivity {

    private LinearLayout roomListContainer;
    private List<Habitacion> habitaciones;
    private static final int REQUEST_NUEVA = 1001;
    private static final int REQUEST_EDITAR = 1002;
    private int posicionEditar = -1;

    private void setupBarraNavegacion() {
        findViewById(R.id.nav_home).setOnClickListener(v ->
                startActivity(new Intent(this, AdminHotelActivity.class)));

        findViewById(R.id.nav_hotel).setOnClickListener(v ->
                startActivity(new Intent(this, HabitacionesActivity.class)));

        findViewById(R.id.nav_chat_center).setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class)));

        findViewById(R.id.nav_reports).setOnClickListener(v ->
                startActivity(new Intent(this, ReporteVentasActivity.class)));

        findViewById(R.id.nav_profile).setOnClickListener(v ->
                startActivity(new Intent(this, PerfilHotelActivity.class)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_hotel_fragment_habitaciones);
        setupBarraNavegacion();

        roomListContainer = findViewById(R.id.room_list_container);
        habitaciones = new ArrayList<>();

        // Datos de ejemplo
        habitaciones.add(new Habitacion("Habitación Matrimonial", Arrays.asList("Luminosa", "Jacuzzi")));
        habitaciones.add(new Habitacion("Habitación Triple", Arrays.asList("Balcón", "Minibar")));

        Button btnAgregar = findViewById(R.id.btn_agregar);
        btnAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(HabitacionesActivity.this, NuevaHabitacionActivity.class);
            startActivityForResult(intent, 1001);
        });

        renderHabitaciones();
    }

    private void renderHabitaciones() {
        roomListContainer.removeAllViews();

        for (int i = 0; i < habitaciones.size(); i++) {
            Habitacion hab = habitaciones.get(i);

            View card = LayoutInflater.from(this).inflate(R.layout.admin_hotel_item_habitacion_card, roomListContainer, false);

            TextView nombre = card.findViewById(R.id.tv_nombre_habitacion);
            TextView detalles = card.findViewById(R.id.tv_detalles_habitacion);
            Button btnEditar = card.findViewById(R.id.btn_editar);
            Button btnBorrar = card.findViewById(R.id.btn_borrar);

            nombre.setText(hab.getNombre());
            detalles.setText(TextUtils.join(" • ", hab.getCaracteristicas()));

            final int index = i;

            btnBorrar.setOnClickListener(v -> {
                habitaciones.remove(index);
                renderHabitaciones();
            });

            btnEditar.setOnClickListener(v -> {
                posicionEditar = index;
                Intent intent = new Intent(this, NuevaHabitacionActivity.class);
                intent.putExtra("nombre", hab.getNombre());
                intent.putStringArrayListExtra("caracteristicas", new ArrayList<>(hab.getCaracteristicas()));
                startActivityForResult(intent, REQUEST_EDITAR);
            });

            roomListContainer.addView(card);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            String nombre = data.getStringExtra("nombre");
            ArrayList<String> caracteristicas = data.getStringArrayListExtra("caracteristicas");

            if (requestCode == REQUEST_NUEVA) {
                habitaciones.add(new Habitacion(nombre, caracteristicas));
            } else if (requestCode == REQUEST_EDITAR && posicionEditar >= 0) {
                habitaciones.set(posicionEditar, new Habitacion(nombre, caracteristicas));
                posicionEditar = -1;
            }
            renderHabitaciones();
        }
    }
}

