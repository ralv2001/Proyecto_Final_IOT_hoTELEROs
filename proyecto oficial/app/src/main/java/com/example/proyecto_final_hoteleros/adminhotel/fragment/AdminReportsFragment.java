package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.R;

public class AdminReportsFragment extends Fragment {

    private CardView cardReporteServicios;
    private CardView cardReporteUsuarios;
    private TextView tvTotalVentas;
    private TextView tvTotalReservas;
    private TextView tvServiciosMasSolicitados;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_reports, container, false);

        initViews(rootView);
        setupClickListeners();
        loadReportData();

        return rootView;
    }

    private void initViews(View rootView) {
        cardReporteServicios = rootView.findViewById(R.id.cardReporteServicios);
        cardReporteUsuarios = rootView.findViewById(R.id.cardReporteUsuarios);
        tvTotalVentas = rootView.findViewById(R.id.tvTotalVentas);
        tvTotalReservas = rootView.findViewById(R.id.tvTotalReservas);
        tvServiciosMasSolicitados = rootView.findViewById(R.id.tvServiciosMasSolicitados);
    }

    private void setupClickListeners() {
        cardReporteServicios.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Reporte por Servicios", Toast.LENGTH_SHORT).show();
        });

        cardReporteUsuarios.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Reporte por Usuarios", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadReportData() {
        // Simulando datos
        tvTotalVentas.setText("S/ 15,250.00");
        tvTotalReservas.setText("24");
        tvServiciosMasSolicitados.setText("WiFi, Desayuno, Spa");
    }
}