package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.R;

public class AdminReportsDetailFragment extends Fragment {

    private static final String ARG_REPORT_TYPE = "report_type";
    private String reportType;

    public static AdminReportsDetailFragment newInstance(String reportType) {
        AdminReportsDetailFragment fragment = new AdminReportsDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REPORT_TYPE, reportType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Por ahora usar un layout simple
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_reports, container, false);

        if (getArguments() != null) {
            reportType = getArguments().getString(ARG_REPORT_TYPE);
        }

        // Mostrar mensaje temporal
        Toast.makeText(getContext(), "Reporte detallado: " + reportType, Toast.LENGTH_SHORT).show();

        return rootView;
    }
}