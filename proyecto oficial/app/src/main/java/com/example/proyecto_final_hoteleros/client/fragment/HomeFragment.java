package com.example.proyecto_final_hoteleros.client.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.model.Hotel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {


    private List<Hotel> listaDeHoteles = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Ejemplo de creación de datos (estos datos pueden venir de una API o base de datos)
        listaDeHoteles.add(new Hotel("Belmond Miraflores Park",
                "Miraflores, frente al malecón, Lima",
                "drawable/belmond", // O el resource ID si usas imágenes locales
                "S/290", "4.9"));
        listaDeHoteles.add(new Hotel("Inkaterra Concepción",
                "Pesawaran, Lampung",
                "drawable/inkaterra",
                "S/300", "4.6"));
        listaDeHoteles.add(new Hotel("Skylodge",
                "Valle Sagrado, acantilado, Cusco ",
                "drawable/gocta",
                "S/310", "4.8"));
        listaDeHoteles.add(new Hotel("Arennas Máncora",
                "Jepara, Central Java",
                "drawable/cuzco",
                "S/275", "4.7"));
        listaDeHoteles.add(new Hotel("Pariwana Lima",
                "Cercado, Barrio Chino, Lima",
                "drawable/arequipa",
                "S/320", "4.9"));


        // Configuración del RecyclerView
        RecyclerView rvHotels = rootView.findViewById(R.id.rvHotels);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvHotels.setLayoutManager(layoutManager);
        rvHotels.setAdapter(new HotelsAdapter(listaDeHoteles));

        return rootView;
    }

}
