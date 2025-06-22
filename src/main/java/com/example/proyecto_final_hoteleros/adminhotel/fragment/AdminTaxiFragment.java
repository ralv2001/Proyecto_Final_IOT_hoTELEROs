package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.TaxiStatusAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.model.TaxiStatus;

import java.util.ArrayList;
import java.util.List;

public class AdminTaxiFragment extends Fragment {

    private RecyclerView recyclerTaxiStatus;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TaxiStatusAdapter adapter;
    private List<TaxiStatus> listaTaxiStatus;
    private View emptyStateView;
    private TextView tvActiveTaxis;
    private TextView tvPendingRequests;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_taxi, container, false);

        initViews(rootView);
        setupRecyclerView();
        loadTaxiStatus();

        return rootView;
    }

    private void initViews(View rootView) {
        recyclerTaxiStatus = rootView.findViewById(R.id.recyclerTaxiStatus);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        emptyStateView = rootView.findViewById(R.id.emptyStateView);
        tvActiveTaxis = rootView.findViewById(R.id.tvActiveTaxis);
        tvPendingRequests = rootView.findViewById(R.id.tvPendingRequests);

        swipeRefreshLayout.setOnRefreshListener(this::loadTaxiStatus);
    }

    private void setupRecyclerView() {
        recyclerTaxiStatus.setLayoutManager(new LinearLayoutManager(getContext()));
        listaTaxiStatus = new ArrayList<>();
        adapter = new TaxiStatusAdapter(listaTaxiStatus);
        recyclerTaxiStatus.setAdapter(adapter);
    }

    private void loadTaxiStatus() {
        swipeRefreshLayout.setRefreshing(true);

        // Simulando data
        listaTaxiStatus.clear();
        listaTaxiStatus.add(new TaxiStatus("Carlos Mendoza", "ABC-123", "En camino", "Ana Palacios", "15 min"));
        listaTaxiStatus.add(new TaxiStatus("Luis Rodriguez", "XYZ-789", "Asignado", "María González", "Esperando"));
        listaTaxiStatus.add(new TaxiStatus("Pedro Vargas", "DEF-456", "Llegó al destino", "Renato Sulca", "Completado"));

        adapter.notifyDataSetChanged();
        updateEmptyState();
        updateStats();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void updateEmptyState() {
        if (listaTaxiStatus.isEmpty()) {
            recyclerTaxiStatus.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            recyclerTaxiStatus.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private void updateStats() {
        int activeTaxis = 0;
        int pendingRequests = 0;

        for (TaxiStatus status : listaTaxiStatus) {
            if (status.getEstado().equals("En camino") || status.getEstado().equals("Asignado")) {
                activeTaxis++;
            }
            if (status.getEstado().equals("Asignado")) {
                pendingRequests++;
            }
        }

        tvActiveTaxis.setText(String.valueOf(activeTaxis));
        tvPendingRequests.setText(String.valueOf(pendingRequests));
    }
}