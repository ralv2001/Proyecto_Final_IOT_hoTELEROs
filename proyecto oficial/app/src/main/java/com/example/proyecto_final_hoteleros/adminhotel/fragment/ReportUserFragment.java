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
import com.example.proyecto_final_hoteleros.adminhotel.adapters.UserReportAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.model.UserReport;

import java.util.ArrayList;
import java.util.List;

public class ReportUserFragment extends Fragment {

    private ImageView ivBack;
    private TextView tvTotalUsers, tvActiveUsers, tvInactiveUsers, tvTopSpender;
    private RecyclerView rvUserReports;
    private UserReportAdapter userAdapter;
    private List<UserReport> userReports;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_report_users, container, false);

        initViews(rootView);
        setupRecyclerView();
        loadUserReports();
        setupClickListeners();

        return rootView;
    }

    private void initViews(View rootView) {
        ivBack = rootView.findViewById(R.id.ivBack);
        tvTotalUsers = rootView.findViewById(R.id.tvTotalUsers);
        tvActiveUsers = rootView.findViewById(R.id.tvActiveUsers);
        tvInactiveUsers = rootView.findViewById(R.id.tvInactiveUsers);
        tvTopSpender = rootView.findViewById(R.id.tvTopSpender);
        rvUserReports = rootView.findViewById(R.id.rvUserReports);
    }

    private void setupRecyclerView() {
        userReports = new ArrayList<>();
        userAdapter = new UserReportAdapter(userReports);
        rvUserReports.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUserReports.setAdapter(userAdapter);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void loadUserReports() {
        // Datos simulados - métricas generales
        tvTotalUsers.setText("127");
        tvActiveUsers.setText("89");
        tvInactiveUsers.setText("38");
        tvTopSpender.setText("María García - S/ 2,450");

        // Datos detallados por usuario
        userReports.add(new UserReport("María García", "maria.garcia@email.com", 5, 2450.0, "VIP", true));
        userReports.add(new UserReport("Carlos Ruiz", "carlos.ruiz@email.com", 3, 1200.0, "Premium", true));
        userReports.add(new UserReport("Ana López", "ana.lopez@email.com", 2, 800.0, "Regular", true));
        userReports.add(new UserReport("Luis Mendoza", "luis.mendoza@email.com", 4, 1850.0, "Premium", false));
        userReports.add(new UserReport("Sofia Chen", "sofia.chen@email.com", 1, 350.0, "Regular", true));
        userReports.add(new UserReport("Roberto Silva", "roberto.silva@email.com", 6, 3200.0, "VIP", true));
        userReports.add(new UserReport("Patricia Morales", "patricia.morales@email.com", 2, 650.0, "Regular", false));

        userAdapter.notifyDataSetChanged();
    }
}