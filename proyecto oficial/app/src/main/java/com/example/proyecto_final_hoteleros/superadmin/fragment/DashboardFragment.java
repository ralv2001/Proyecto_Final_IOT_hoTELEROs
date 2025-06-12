package com.example.proyecto_final_hoteleros.superadmin.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity;
import com.example.proyecto_final_hoteleros.superadmin.adapters.MetricsAdapter;
import com.example.proyecto_final_hoteleros.superadmin.adapters.QuickAccessAdapter;
import com.example.proyecto_final_hoteleros.superadmin.adapters.RecentActivityAdapter;
import com.example.proyecto_final_hoteleros.superadmin.models.MetricItem;
import com.example.proyecto_final_hoteleros.superadmin.models.QuickAccessItem;
import com.example.proyecto_final_hoteleros.superadmin.models.RecentActivityItem;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private RecyclerView rvMetrics, rvQuickAccess, rvRecentActivity;
    private MetricsAdapter metricsAdapter;
    private QuickAccessAdapter quickAccessAdapter;
    private RecentActivityAdapter recentActivityAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        initViews(view);
        setupRecyclerViews();
        loadData();

        return view;
    }

    private void initViews(View view) {
        rvMetrics = view.findViewById(R.id.rv_metrics);
        rvQuickAccess = view.findViewById(R.id.rv_quick_access);
        rvRecentActivity = view.findViewById(R.id.rv_recent_activity);
    }

    private void setupRecyclerViews() {
        // Configurar RecyclerView de métricas (horizontal)
        rvMetrics.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        metricsAdapter = new MetricsAdapter(new ArrayList<>());
        rvMetrics.setAdapter(metricsAdapter);

        // Configurar RecyclerView de accesos rápidos (grid 2 columnas)
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvQuickAccess.setLayoutManager(gridLayoutManager);

        // Limitar la altura del RecyclerView a 3 filas máximo
        ViewGroup.LayoutParams layoutParams = rvQuickAccess.getLayoutParams();
        layoutParams.height = (int) (getResources().getDisplayMetrics().density * 390); // 3 filas x 130dp
        rvQuickAccess.setLayoutParams(layoutParams);

        quickAccessAdapter = new QuickAccessAdapter(new ArrayList<>(), this::onQuickAccessClick);
        rvQuickAccess.setAdapter(quickAccessAdapter);

        // Configurar RecyclerView de actividad reciente (vertical)
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(getContext()));
        recentActivityAdapter = new RecentActivityAdapter(new ArrayList<>());
        rvRecentActivity.setAdapter(recentActivityAdapter);
    }

    private void loadData() {
        loadMetrics();
        loadQuickAccess();
        loadRecentActivity();
    }

    private void loadMetrics() {
        List<MetricItem> metrics = new ArrayList<>();
        metrics.add(new MetricItem("Hoteles", "15", R.drawable.ic_hotel));
        metrics.add(new MetricItem("Taxistas Pendientes", "3", R.drawable.ic_car));
        metrics.add(new MetricItem("Reservas Hoy", "28", R.drawable.ic_calendar));
        metrics.add(new MetricItem("Usuarios Activos", "142", R.drawable.ic_people));

        metricsAdapter.updateData(metrics);
    }

    private void loadQuickAccess() {
        List<QuickAccessItem> quickAccess = new ArrayList<>();
        quickAccess.add(new QuickAccessItem("Gestionar\nAdministradores", R.drawable.ic_profile, "admins"));
        quickAccess.add(new QuickAccessItem("Aprobar\nTaxistas", R.drawable.ic_car, "taxistas"));
        quickAccess.add(new QuickAccessItem("Ver Todos\nlos Usuarios", R.drawable.ic_people, "usuarios"));
        quickAccess.add(new QuickAccessItem("Reportes de\nReservas", R.drawable.ic_historial, "reportes"));
        quickAccess.add(new QuickAccessItem("Logs del\nSistema", R.drawable.ic_active, "logs"));
        quickAccess.add(new QuickAccessItem("Registrar\nAdmin Hotel", R.drawable.ic_add, "add_admin"));

        quickAccessAdapter.updateData(quickAccess);
    }

    private void loadRecentActivity() {
        List<RecentActivityItem> activities = new ArrayList<>();
        activities.add(new RecentActivityItem("Nuevo taxista aprobado", "Carlos Mendoza - Placa ABC-123", "Hace 2h", R.drawable.ic_check));
        activities.add(new RecentActivityItem("Admin de hotel registrado", "Hotel Plaza - María García", "Hace 4h", R.drawable.ic_profile));
        activities.add(new RecentActivityItem("Usuario desactivado", "Cliente Juan Pérez", "Hace 6h", R.drawable.ic_exclamacioncampoerroneo));

        recentActivityAdapter.updateData(activities);
    }

    private void onQuickAccessClick(QuickAccessItem item) {
        if (getActivity() instanceof SuperAdminActivity) {
            ((SuperAdminActivity) getActivity()).handleQuickAccessClick(item.getAction());
        }
    }
}