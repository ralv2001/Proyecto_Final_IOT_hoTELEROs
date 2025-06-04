package com.example.proyecto_final_hoteleros.adminhotel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_final_hoteleros.R;

import java.util.ArrayList;

public class NuevaHabitacionActivity extends AppCompatActivity {

    EditText etNombre;
    CheckBox cbLuminosa, cbBalcon, cbJacuzzi, cbMinibar, cbDesayuno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_hotel_nueva_habitacion);


        etNombre = findViewById(R.id.et_nombre_habtacion);
        cbLuminosa = findViewById(R.id.cb_luminosa);
        cbBalcon = findViewById(R.id.cb_balcon);
        cbJacuzzi = findViewById(R.id.cb_jacuzzi);
        cbMinibar = findViewById(R.id.cb_minibar);
        cbDesayuno = findViewById(R.id.cb_desayuno);

        if (getIntent() != null) {
            etNombre.setText(getIntent().getStringExtra("nombre"));

            ArrayList<String> recibidas = getIntent().getStringArrayListExtra("caracteristicas");
            if (recibidas != null) {
                cbLuminosa.setChecked(recibidas.contains("Luminosa"));
                cbBalcon.setChecked(recibidas.contains("Balcón"));
                cbJacuzzi.setChecked(recibidas.contains("Jacuzzi"));
                cbMinibar.setChecked(recibidas.contains("Minibar y snack"));
                cbDesayuno.setChecked(recibidas.contains("Desayuno incluido"));
            }
        }


        Button btnGuardar = findViewById(R.id.btn_guardar);
        btnGuardar.setOnClickListener(v -> guardarHabitacion());
    }

    private void guardarHabitacion() {
        String nombre = etNombre.getText().toString().trim();
        ArrayList<String> caracteristicas = new ArrayList<>();

        if (cbLuminosa.isChecked()) caracteristicas.add("Luminosa");
        if (cbBalcon.isChecked()) caracteristicas.add("Balcón");
        if (cbJacuzzi.isChecked()) caracteristicas.add("Jacuzzi");
        if (cbMinibar.isChecked()) caracteristicas.add("Minibar y snack");
        if (cbDesayuno.isChecked()) caracteristicas.add("Desayuno incluido");

        Intent result = new Intent();
        result.putExtra("nombre", nombre);
        result.putStringArrayListExtra("caracteristicas", caracteristicas);
        setResult(RESULT_OK, result);
        finish();
    }
}
