package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.ServiceReportAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.model.ServiceReport;

import java.util.ArrayList;
import java.util.List;

public class ReportServiceFragment extends Fragment {

    private ImageView ivBack;
    private TextView tvTotalServices, tvMostRequested, tvHighestRevenue, tvAverageRating;
    private RecyclerView rvServiceReports;
    private ServiceReportAdapter serviceAdapter;
    private List<ServiceReport> serviceReports;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_report_services, container, false);

        initViews(rootView);
        setupRecyclerView();
        loadServiceReports();
        setupClickListeners();

        return rootView;
    }

    private void initViews(View rootView) {
        ivBack = rootView.findViewById(R.id.ivBack);
        tvTotalServices = rootView.findViewById(R.id.tvTotalServices);
        tvMostRequested = rootView.findViewById(R.id.tvMostRequested);
        tvHighestRevenue = rootView.findViewById(R.id.tvHighestRevenue);
        tvAverageRating = rootView.findViewById(R.id.tvAverageRating);
        rvServiceReports = rootView.findViewById(R.id.rvServiceReports);
    }

    private void setupRecyclerView() {
        serviceReports = new ArrayList<>();
        serviceAdapter = new ServiceReportAdapter(serviceReports);
        rvServiceReports.setLayoutManager(new LinearLayoutManager(getContext()));
        rvServiceReports.setAdapter(serviceAdapter);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void loadServiceReports() {
        // Datos simulados - métricas generales
        tvTotalServices.setText("12");
        tvMostRequested.setText("Spa & Wellness");
        tvHighestRevenue.setText("Room Service - S/ 3,200");
        tvAverageRating.setText("4.6 ⭐");

        // Datos detallados por servicio
        serviceReports.add(new ServiceReport("Spa & Wellness", "ic_spa", 45, 5400.0, 4.8f, "PAID"));
        serviceReports.add(new ServiceReport("Room Service", "ic_room_service", 38, 3200.0, 4.5f, "PAID"));
        serviceReports.add(new ServiceReport("WiFi Gratuito", "ic_wifi", 127, 0.0, 4.9f, "INCLUDED"));
        serviceReports.add(new ServiceReport("Minibar Premium", "ic_minibar", 22, 1760.0, 4.2f, "PAID"));
        serviceReports.add(new ServiceReport("Lavandería Express", "ic_laundry", 18, 630.0, 4.6f, "PAID"));
        serviceReports.add(new ServiceReport("Gimnasio VIP", "ic_gym", 15, 750.0, 4.4f, "PAID"));
        serviceReports.add(new ServiceReport("Taxi Aeropuerto VIP", "ic_taxi", 8, 0.0, 5.0f, "SPECIAL"));

        serviceAdapter.notifyDataSetChanged();
    }
}