package com.example.proyecto_final_hoteleros.superadmin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.button.MaterialButton;
public class HomeActivity extends AppCompatActivity{
    private MaterialButton btnListaUsuarios;
    private MaterialButton btn_lista_solicitudes_taxistas;
    private MaterialButton btnRegistroAdministrador;
    private MaterialButton btnReporteReservas;
    private MaterialButton btnLogEventos;
    private MaterialButton btnCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superadmin_home); // Asegúrate de que tu archivo XML se llama así

        // Referencias a los botones
        btnListaUsuarios = findViewById(R.id.btn_lista_usuarios);
        btn_lista_solicitudes_taxistas = findViewById(R.id.btn_lista_solicitudes_taxistas);
        btnRegistroAdministrador = findViewById(R.id.btn_registro_administrador);
        btnReporteReservas = findViewById(R.id.btn_reporte_reservas);
        btnLogEventos = findViewById(R.id.btn_log_eventos);
        btnCerrarSesion = findViewById(R.id.btnLogin);

        // Acciones para cada botón
        btnListaUsuarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Ir a Lista de Usuarios", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(MainActivity.this, ListaUsuariosActivity.class));
            }
        });

        btn_lista_solicitudes_taxistas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Ir a Solicitudes de Taxistas", Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(MainActivity.this, SolicitudesTaxistasActivity.class));
            }
        });

        btnRegistroAdministrador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Ir a Registro de Administrador", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(MainActivity.this, RegistroAdministradorActivity.class));
            }
        });

        btnReporteReservas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Ir a Reporte de Reservas", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(MainActivity.this, ReporteReservasActivity.class));
            }
        });

        btnLogEventos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Ir a Log de Eventos", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(MainActivity.this, LogEventosActivity.class));
            }
        });

        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(MainActivity.this, LoginActivity.class));
                // finish();
            }
        });
    }

}
