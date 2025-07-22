package com.example.proyecto_final_hoteleros.superadmin.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.adapters.ReportMetricsAdapter;
import com.example.proyecto_final_hoteleros.superadmin.adapters.ReportChartAdapter;
import com.example.proyecto_final_hoteleros.superadmin.models.ReportMetric;
import com.example.proyecto_final_hoteleros.superadmin.models.ChartData;

import java.util.ArrayList;
import java.util.List;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

public class ReportesFragment extends Fragment {

    private RecyclerView rvMetrics, rvCharts;
    private ReportMetricsAdapter metricsAdapter;
    private ReportChartAdapter chartAdapter;
    private TextView tvPeriodSelector;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.superadmin_fragment_reportes, container, false);

        initViews(view);
        setupRecyclerViews();
        loadData();

        return view;
    }

    // ✅ AGREGAR ESTE MÉTODO NUEVO DESPUÉS DE onCreateView
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ✅ CONFIGURAR WINDOW INSETS
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });
    }

    private void initViews(View view) {
        android.util.Log.d("ReportesFragment", "=== INICIO initViews ===");

        rvMetrics = view.findViewById(R.id.rv_metrics);
        rvCharts = view.findViewById(R.id.rv_charts);
        tvPeriodSelector = view.findViewById(R.id.tv_period_selector);

        // Debug: verificar que se encontraron las vistas
        android.util.Log.d("ReportesFragment", "rvMetrics: " + (rvMetrics != null ? "OK" : "NULL"));
        android.util.Log.d("ReportesFragment", "rvCharts: " + (rvCharts != null ? "OK" : "NULL"));
        android.util.Log.d("ReportesFragment", "tvPeriodSelector: " + (tvPeriodSelector != null ? "OK" : "NULL"));

        // Configurar botón de back
        ImageView ivBack = view.findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
            android.util.Log.d("ReportesFragment", "ivBack configurado correctamente");
        } else {
            android.util.Log.e("ReportesFragment", "ivBack es null!");
        }

        // Configurar selector de período
        if (tvPeriodSelector != null) {
            tvPeriodSelector.setOnClickListener(v -> showPeriodSelector());
            android.util.Log.d("ReportesFragment", "tvPeriodSelector configurado correctamente");
        } else {
            android.util.Log.e("ReportesFragment", "tvPeriodSelector es null!");
        }

        android.util.Log.d("ReportesFragment", "=== FIN initViews ===");
    }

    private void setupRecyclerViews() {
        android.util.Log.d("ReportesFragment", "Configurando RecyclerViews...");
        try {
            // RecyclerView de métricas (grid 2 columnas)
            rvMetrics.setLayoutManager(new GridLayoutManager(getContext(), 2));
            metricsAdapter = new ReportMetricsAdapter(new ArrayList<>());
            rvMetrics.setAdapter(metricsAdapter);

            // RecyclerView de gráficos (vertical)
            rvCharts.setLayoutManager(new LinearLayoutManager(getContext()));
            chartAdapter = new ReportChartAdapter(new ArrayList<>());
            rvCharts.setAdapter(chartAdapter);

            android.util.Log.d("ReportesFragment", "RecyclerViews configurados exitosamente");
        } catch (Exception e) {
            android.util.Log.e("ReportesFragment", "Error configurando RecyclerViews: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadData() {
        android.util.Log.d("ReportesFragment", "Cargando datos...");
        try {
            loadMetrics();
            loadCharts();
            android.util.Log.d("ReportesFragment", "Datos cargados exitosamente");
        } catch (Exception e) {
            android.util.Log.e("ReportesFragment", "Error cargando datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMetrics() {
        List<ReportMetric> metrics = new ArrayList<>();

        // Métricas financieras
        metrics.add(new ReportMetric("Ingresos Totales", "S/ 45,280", "+12.5%", "#4CAF50", R.drawable.ic_money));
        metrics.add(new ReportMetric("Reservas del Mes", "342", "+8.3%", "#2196F3", R.drawable.ic_calendar));
        metrics.add(new ReportMetric("Hoteles Activos", "28", "+2 nuevos", "#FF9800", R.drawable.ic_hotel));
        metrics.add(new ReportMetric("Usuarios Registrados", "1,245", "+15.7%", "#9C27B0", R.drawable.ic_people));
        metrics.add(new ReportMetric("Servicios de Taxi", "156", "+5.1%", "#4CAF50", R.drawable.ic_car));
        metrics.add(new ReportMetric("Satisfacción", "4.8★", "+0.2", "#FF5722", R.drawable.ic_star));

        if (metricsAdapter != null) {
            metricsAdapter.updateData(metrics);
        }
    }

    private void loadCharts() {
        List<ChartData> charts = new ArrayList<>();

        // Datos de ejemplo para los gráficos
        charts.add(new ChartData("Reservas por Mes", "BAR_CHART",
                createSampleBarData(), "Comparación de reservas mensuales"));

        charts.add(new ChartData("Distribución por Tipo de Usuario", "PIE_CHART",
                createSamplePieData(), "Porcentaje de usuarios por tipo"));

        charts.add(new ChartData("Ingresos por Hotel", "HORIZONTAL_BAR",
                createSampleHotelData(), "Top 5 hoteles con mayores ingresos"));

        if (chartAdapter != null) {
            chartAdapter.updateData(charts);
        }
    }

    private List<String> createSampleBarData() {
        List<String> data = new ArrayList<>();
        data.add("Enero: 245");
        data.add("Febrero: 312");
        data.add("Marzo: 298");
        data.add("Abril: 356");
        data.add("Mayo: 412");
        data.add("Junio: 378");
        return data;
    }

    private List<String> createSamplePieData() {
        List<String> data = new ArrayList<>();
        data.add("Clientes: 65%");
        data.add("Admins Hotel: 20%");
        data.add("Taxistas: 15%");
        return data;
    }

    private List<String> createSampleHotelData() {
        List<String> data = new ArrayList<>();
        data.add("Hotel Plaza: S/ 12,450");
        data.add("Hotel Seaside: S/ 9,840");
        data.add("Hotel Mountain: S/ 8,720");
        data.add("Hotel Downtown: S/ 7,950");
        data.add("Hotel Garden: S/ 6,320");
        return data;
    }

    private void showPeriodSelector() {
        String[] periods = {"Última semana", "Último mes", "Últimos 3 meses", "Último año", "Personalizado"};
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Seleccionar período")
                .setItems(periods, (dialog, which) -> {
                    tvPeriodSelector.setText(periods[which]);
                    // Aquí recargarías los datos según el período seleccionado
                    refreshDataForPeriod(periods[which]);
                    android.widget.Toast.makeText(getContext(), "Período: " + periods[which], android.widget.Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void refreshDataForPeriod(String period) {
        // Simular cambio de datos según el período
        android.util.Log.d("ReportesFragment", "Refrescando datos para período: " + period);
        // Aquí implementarías la lógica para cargar datos específicos del período
        loadData();
    }
}