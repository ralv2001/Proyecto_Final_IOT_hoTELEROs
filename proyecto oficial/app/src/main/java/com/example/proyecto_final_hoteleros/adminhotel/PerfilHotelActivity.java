package com.example.proyecto_final_hoteleros.adminhotel;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_final_hoteleros.R;

public class PerfilHotelActivity extends AppCompatActivity {
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
        setupBarraNavegacion();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_hotel_perfil);
    }
}
