package com.example.proyecto_final_hoteleros.adminhotel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.ListaUsuariosActivity;
import com.example.proyecto_final_hoteleros.superadmin.LogEventosActivity;
import com.example.proyecto_final_hoteleros.superadmin.RegistroAdministradorActivity;
import com.example.proyecto_final_hoteleros.superadmin.ReporteReservasActivity;
import com.example.proyecto_final_hoteleros.superadmin.SolicitudesTaxistasActivity;
import com.example.proyecto_final_hoteleros.superadmin.SuperadminActivity;
import com.google.android.material.button.MaterialButton;

public class AdminHotelActivity extends AppCompatActivity {
    private MaterialButton btnPerfilHotel;
    private MaterialButton btnHabitaciones;
    private MaterialButton btnServiciosHotel;
    private MaterialButton btnAsigTaxistas;
    private MaterialButton btnReportEventos;
    private MaterialButton btnCheckout;
    private MaterialButton btnCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_hotel); // Asegúrate de que tu archivo XML se llama así

        // Referencias a los botones
        btnPerfilHotel = findViewById(R.id.btn_perfil_hotel);
        btnHabitaciones = findViewById(R.id.btn_habitaciones);
        btnServiciosHotel = findViewById(R.id.btn_servicios_hotel);
        btnAsigTaxistas = findViewById(R.id.btn_asignacion_taxistas);
        btnReportEventos = findViewById(R.id.btn_reporte_ventas);
        btnCheckout = findViewById(R.id.btn_checkout);
        btnCerrarSesion = findViewById(R.id.btnLogin);

        // Acciones para cada botón
        btnPerfilHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminHotelActivity.this, "Ir a Pefil de Hotel", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AdminHotelActivity.this, ListaUsuariosActivity.class));
            }
        });

        btnHabitaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminHotelActivity.this, "Ir a Habitaciones", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AdminHotelActivity.this, SolicitudesTaxistasActivity.class));
            }
        });

        btnServiciosHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminHotelActivity.this, "Ir a Servicios de Hotel", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AdminHotelActivity.this, RegistroAdministradorActivity.class));
            }
        });

        btnAsigTaxistas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminHotelActivity.this, "Ir a Asingación de Taxistas", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AdminHotelActivity.this, ReporteReservasActivity.class));
            }
        });

        btnReportEventos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminHotelActivity.this, "Reporte de Ventas", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AdminHotelActivity.this, LogEventosActivity.class));
            }
        });

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminHotelActivity.this, "Checkout", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AdminHotelActivity.this, LogEventosActivity.class));
            }
        });

        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminHotelActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(MainActivity.this, LoginActivity.class));
                // finish();
            }
        });
    }
}
