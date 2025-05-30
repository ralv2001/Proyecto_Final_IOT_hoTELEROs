package com.example.proyecto_final_hoteleros.adminhotel;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adapters.ServicioAdapter;
import com.example.proyecto_final_hoteleros.adapters.ServicioSimpleAdapter;

import java.util.ArrayList;

public class DatosHuespedActivity extends AppCompatActivity {

    TextView tvName, tvDni, tvEmail, tvPhone, tvBirthDate, tvCheckIn, tvCheckOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_hotel_datos_huesped);

        // Referencias al layout
        tvName = findViewById(R.id.tvName);
        tvDni = findViewById(R.id.tvDni);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvBirthDate = findViewById(R.id.tvBirthDate);
        tvCheckIn = findViewById(R.id.tvCheckIn);
        tvCheckOut = findViewById(R.id.tvCheckOut);

        // Obtener los datos enviados
        String nombre = getIntent().getStringExtra("nombre");
        String dni = getIntent().getStringExtra("dni");
        String email = getIntent().getStringExtra("email");
        String telefono = getIntent().getStringExtra("telefono");
        String nacimiento = getIntent().getStringExtra("nacimiento");
        String checkIn = getIntent().getStringExtra("checkIn");
        String checkOut = getIntent().getStringExtra("checkOut");
        ArrayList<String> servicios = getIntent().getStringArrayListExtra("servicios");


        // Mostrar los datos
        tvName.setText("Nombre completo: " + nombre);
        tvDni.setText("DNI: " + dni);
        tvEmail.setText("Correo electrónico: " + email);
        tvPhone.setText("Teléfono: " + telefono);
        tvBirthDate.setText("Fecha de nacimiento: " + nacimiento);
        tvCheckIn.setText(checkIn);
        tvCheckOut.setText(checkOut);

        RecyclerView recyclerView = findViewById(R.id.recyclerServices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ServicioSimpleAdapter adapter = new ServicioSimpleAdapter(servicios);
        recyclerView.setAdapter(adapter);
    }
}