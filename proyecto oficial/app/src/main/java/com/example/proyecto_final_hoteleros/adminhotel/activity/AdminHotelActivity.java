package com.example.proyecto_final_hoteleros.adminhotel.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.HuespedAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.model.Huesped;

import java.util.ArrayList;
import java.util.List;

public class AdminHotelActivity extends AppCompatActivity {

    private RecyclerView recyclerHuespedes;
    private HuespedAdapter adapter;
    private List<Huesped> listaHuespedes;

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
        setContentView(R.layout.admin_hotel);
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


}
