package com.example.proyecto_final_hoteleros.adminhotel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.ServicioAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.model.Servicio;

import java.util.ArrayList;
import java.util.List;

public class ServiciosHotelActivity extends AppCompatActivity {
    private RecyclerView recyclerServicios;
    private ServicioAdapter adapter;
    private List<Servicio> listaServicios;
    private static final int NUEVO_SERVICIO_REQUEST = 1;

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

        Button btnAgregar = findViewById(R.id.btnAgregar);
        btnAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(ServiciosHotelActivity.this, NuevoServicioActivity.class);
            startActivity(intent);
        });



        btnAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(ServiciosHotelActivity.this, NuevoServicioActivity.class);
            startActivityForResult(intent, NUEVO_SERVICIO_REQUEST);
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NUEVO_SERVICIO_REQUEST && resultCode == RESULT_OK && data != null) {
            String nombre = data.getStringExtra("nombre");
            String descripcion = data.getStringExtra("descripcion");
            int imagen = data.getIntExtra("imagen", R.drawable.sauna_sample);

            Servicio nuevo = new Servicio(nombre, imagen); // podrías añadir descripción al modelo también
            listaServicios.add(nuevo);
            adapter.notifyItemInserted(listaServicios.size() - 1);
        }
    }
}
