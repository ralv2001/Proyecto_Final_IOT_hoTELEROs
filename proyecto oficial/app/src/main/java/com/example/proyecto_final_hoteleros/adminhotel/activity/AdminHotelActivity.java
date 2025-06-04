package com.example.proyecto_final_hoteleros.adminhotel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.HuespedAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.fragment.AdminChatFragment; // AÑADIR ESTE IMPORT
import com.example.proyecto_final_hoteleros.adminhotel.model.Huesped;

import java.util.ArrayList;
import java.util.List;

public class AdminHotelActivity extends AppCompatActivity {

    private RecyclerView recyclerHuespedes;
    private HuespedAdapter adapter;
    private List<Huesped> listaHuespedes;

    // AÑADIR ESTAS VARIABLES:
    private View mainContent;
    private View fragmentContainer;

    private void setupBarraNavegacion() {
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            showMainContent(); // Mostrar contenido principal
            // startActivity(new Intent(this, AdminHotelActivity.class)); // Opcional: comentar esta línea
        });

        findViewById(R.id.nav_hotel).setOnClickListener(v ->
                startActivity(new Intent(this, HabitacionesActivity.class)));

        // MODIFICAR ESTA LÍNEA:
        findViewById(R.id.nav_chat_center).setOnClickListener(v -> {
            AdminChatFragment chatFragment = new AdminChatFragment();
            replaceFragment(chatFragment);
        });

        findViewById(R.id.nav_reports).setOnClickListener(v ->
                startActivity(new Intent(this, ReporteVentasActivity.class)));

        findViewById(R.id.nav_profile).setOnClickListener(v ->
                startActivity(new Intent(this, PerfilHotelActivity.class)));
    }

    // AÑADIR ESTOS MÉTODOS:
    private void replaceFragment(Fragment fragment) {
        // Ocultar contenido principal
        hideMainContent();

        // Mostrar fragment container
        fragmentContainer.setVisibility(View.VISIBLE);

        // Cargar fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showMainContent() {
        // Mostrar contenido principal
        findViewById(R.id.header).setVisibility(View.VISIBLE);
        findViewById(R.id.label_huespedes).setVisibility(View.VISIBLE);
        findViewById(R.id.recycler_huespedes).setVisibility(View.VISIBLE);

        // Ocultar fragment container
        fragmentContainer.setVisibility(View.GONE);

        // Limpiar back stack
        getSupportFragmentManager().popBackStack();
    }

    private void hideMainContent() {
        // Ocultar contenido principal
        findViewById(R.id.header).setVisibility(View.GONE);
        findViewById(R.id.label_huespedes).setVisibility(View.GONE);
        findViewById(R.id.recycler_huespedes).setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_hotel);

        // AÑADIR ESTAS LÍNEAS:
        fragmentContainer = findViewById(R.id.fragment_container);

        setupBarraNavegacion();

        recyclerHuespedes = findViewById(R.id.recycler_huespedes);
        recyclerHuespedes.setLayoutManager(new LinearLayoutManager(this));

        listaHuespedes = new ArrayList<>();
        listaHuespedes.add(new Huesped("Renato Sulca", "Lun 27, 14:45"));
        listaHuespedes.add(new Huesped("Carlos Ramos", "Lun 27, 15:10"));
        listaHuespedes.add(new Huesped("Ana Palacios", "Dom 26, 11:00"));

        adapter = new HuespedAdapter(listaHuespedes);
        recyclerHuespedes.setAdapter(adapter);
        adapter.setOnItemClickListener(new HuespedAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Huesped huesped) {
                Intent intent = new Intent(AdminHotelActivity.this, DatosHuespedActivity.class);
                List<String> servicios = huesped.getServicios() != null ? huesped.getServicios() : new ArrayList<>();
                intent.putExtra("nombre", huesped.getNombre());
                intent.putExtra("checkIn", huesped.getCheckIn());
                intent.putExtra("checkOut", huesped.getCheckOut());
                intent.putStringArrayListExtra("servicios", new ArrayList<>(servicios));
                startActivity(intent);
            }
        });
    }

    // AÑADIR ESTE MÉTODO para manejar el botón atrás:
    @Override
    public void onBackPressed() {
        if (fragmentContainer.getVisibility() == View.VISIBLE) {
            // Si hay un fragment visible, volver al contenido principal
            showMainContent();
        } else {
            // Si no, comportamiento normal
            super.onBackPressed();
        }
    }
}