package com.example.proyecto_final_hoteleros.adminhotel;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adapters.ServicioAdapter;

import java.util.ArrayList;
import java.util.List;

public class ServiciosHotelActivity extends AppCompatActivity {
    private RecyclerView recyclerServicios;
    private ServicioAdapter adapter;
    private List<Servicio> listaServicios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_hotel_servicios);

        recyclerServicios = findViewById(R.id.recyclerServicios);
        recyclerServicios.setLayoutManager(new LinearLayoutManager(this));

        // Lista de ejemplo
        listaServicios = new ArrayList<>();
        listaServicios.add(new Servicio("Sauna", R.drawable.sauna_sample));
        listaServicios.add(new Servicio("Piscina", R.drawable.sauna_sample));

        adapter = new ServicioAdapter(listaServicios, this);
        recyclerServicios.setAdapter(adapter);
    }
}
