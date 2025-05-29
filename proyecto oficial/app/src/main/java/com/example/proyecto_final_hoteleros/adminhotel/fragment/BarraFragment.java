package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.HomeActivity;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.AdminHotelActivity;
import com.example.proyecto_final_hoteleros.adminhotel.ChatActivity;
import com.example.proyecto_final_hoteleros.adminhotel.HabitacionesActivity;
import com.example.proyecto_final_hoteleros.adminhotel.PerfilHotelActivity;
import com.example.proyecto_final_hoteleros.adminhotel.ReporteVentasActivity;

public class BarraFragment extends Fragment {

    public BarraFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.barra_admin, container, false);

        LinearLayout navHome = view.findViewById(R.id.nav_home);
        LinearLayout navExplore = view.findViewById(R.id.nav_hotel);
        LinearLayout navChat = view.findViewById(R.id.nav_chat_center);
        LinearLayout navReports = view.findViewById(R.id.nav_reports);
        LinearLayout navProfile = view.findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AdminHotelActivity.class)));

        navExplore.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), HabitacionesActivity.class)));

        navChat.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ChatActivity.class)));

        navReports.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ReporteVentasActivity.class)));

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), PerfilHotelActivity.class)));

        return view;
    }
}