package com.example.proyecto_final_hoteleros.taxista.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.taxista.adapters.HistorialAdapter;
import com.example.proyecto_final_hoteleros.taxista.model.CompletedTrip;
import com.example.proyecto_final_hoteleros.taxista.utils.DriverPreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class DriverHistorialFragment extends Fragment implements HistorialAdapter.HistorialListener {

    private static final String TAG = "DriverHistorialFragment";

    private RecyclerView recyclerHistorial;
    private HistorialAdapter adapter;
    private List<CompletedTrip> historialList;
    private DriverPreferenceManager preferenceManager;

    private LinearLayout emptyState;
    private LinearLayout loadingState;
    private TextView tvHistorialCount;
    private TextView tvTotalEarnings;
    private TextView tvTotalTrips;
    //private FloatingActionButton btnRefresh;
    private SwipeRefreshLayout swipeRefreshLayout;

    public DriverHistorialFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.taxi_fragment_driver_historial, container, false);

        try {
            // Inicializar vistas
            initializeViews(view);

            // Inicializar PreferenceManager
            preferenceManager = new DriverPreferenceManager(requireContext());

            // Configurar RecyclerView
            setupRecyclerView();

            // Configurar botón de actualizar

        } catch (Exception e) {
            Log.e(TAG, "Error en onCreateView: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error al cargar historial: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return view;
    }

    private void initializeViews(View view) {
        recyclerHistorial = view.findViewById(R.id.recycler_historial);
        emptyState = view.findViewById(R.id.empty_state);
        loadingState = view.findViewById(R.id.loading_state);
        tvHistorialCount = view.findViewById(R.id.tv_historial_count);
        tvTotalEarnings = view.findViewById(R.id.tv_total_earnings);
        tvTotalTrips = view.findViewById(R.id.tv_total_trips);

        // NUEVO: Inicializar SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        // btnRefresh = view.findViewById(R.id.btn_refresh);
    }

    private void setupRecyclerView() {
        if (recyclerHistorial != null && getContext() != null) {
            recyclerHistorial.setLayoutManager(new LinearLayoutManager(getContext()));

            // Inicializar lista y adaptador
            historialList = new ArrayList<>();
            adapter = new HistorialAdapter(getContext(), historialList, this);
            recyclerHistorial.setAdapter(adapter);

            Log.d(TAG, "✅ RecyclerView configurado con listener: " + this);
        }

        // NUEVO: Configurar SwipeRefreshLayout
        setupSwipeRefresh();
    }
    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            // Configurar colores del refresh
            swipeRefreshLayout.setColorSchemeColors(
                    getResources().getColor(R.color.colorPrimary, null),
                    getResources().getColor(R.color.colorAccent, null),
                    getResources().getColor(android.R.color.holo_orange_light, null)
            );

            // Configurar listener para el refresh
            swipeRefreshLayout.setOnRefreshListener(() -> {
                Log.d(TAG, "🔄 Pull-to-refresh activado");
                cargarHistorial();
            });

            Log.d(TAG, "✅ SwipeRefreshLayout configurado");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Cargar datos al iniciar
            cargarHistorial();
        } catch (Exception e) {
            Log.e(TAG, "Error en onViewCreated: " + e.getMessage(), e);
            showErrorState();
        }
    }

    private void cargarHistorial() {
        try {
            // Mostrar estado de carga
            showLoadingState();

            // Simular carga de datos del historial
            new Handler().postDelayed(() -> {
                try {
                    // Generar datos de ejemplo del historial
                    List<CompletedTrip> nuevoHistorial = generarHistorialEjemplo();

                    // Actualizar la lista
                    historialList.clear();
                    historialList.addAll(nuevoHistorial);

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }

                    // Actualizar estadísticas
                    actualizarEstadisticas();

                    // Mostrar estado correspondiente
                    updateVisibility();

                } catch (Exception e) {
                    Log.e(TAG, "Error al cargar historial: " + e.getMessage(), e);
                    showErrorState();
                }
            }, 1500); // Simular tiempo de carga

        } catch (Exception e) {
            Log.e(TAG, "Error en cargarHistorial: " + e.getMessage(), e);
            showErrorState();
        }
    }

    private void actualizarEstadisticas() {
        if (historialList == null) return;

        int totalTrips = historialList.size();
        int completedTrips = 0;
        double totalEarnings = 0.0;

        for (CompletedTrip trip : historialList) {
            if (trip != null && trip.isCompleted()) {
                completedTrips++;
                totalEarnings += trip.getEarnings();
            }
        }

        // Actualizar contadores de forma segura
        if (tvHistorialCount != null) {
            tvHistorialCount.setText("Tienes " + totalTrips + " viaje" + (totalTrips == 1 ? "" : "s") + " en tu historial");
        }

        if (tvTotalTrips != null) {
            tvTotalTrips.setText(String.valueOf(completedTrips));
        }

        if (tvTotalEarnings != null) {
            tvTotalEarnings.setText("S/ " + String.format("%.2f", totalEarnings));
        }

        // Guardar estadísticas en local storage
        if (preferenceManager != null) {
            preferenceManager.updateCompletedTrips(completedTrips);
            preferenceManager.updateEarnings(totalEarnings, totalEarnings);
        }
    }

    private void updateVisibility() {
        if (historialList == null || historialList.isEmpty()) {
            setViewVisibility(recyclerHistorial, View.GONE);
            setViewVisibility(loadingState, View.GONE);
            setViewVisibility(emptyState, View.VISIBLE);
            setViewVisibility(swipeRefreshLayout, View.GONE); // NUEVO
        } else {
            setViewVisibility(recyclerHistorial, View.VISIBLE);
            setViewVisibility(loadingState, View.GONE);
            setViewVisibility(emptyState, View.GONE);
            setViewVisibility(swipeRefreshLayout, View.VISIBLE); // NUEVO
        }

        // NUEVO: Detener el refresh si está activo
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showLoadingState() {
        // Solo mostrar loading inicial si no hay datos
        if (historialList == null || historialList.isEmpty()) {
            setViewVisibility(recyclerHistorial, View.GONE);
            setViewVisibility(emptyState, View.GONE);
            setViewVisibility(loadingState, View.VISIBLE);
            setViewVisibility(swipeRefreshLayout, View.GONE);
        } else {
            // Si ya hay datos, solo mostrar el refresh indicator
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(true);
            }
        }
    }

    private void showErrorState() {
        setViewVisibility(recyclerHistorial, View.GONE);
        setViewVisibility(loadingState, View.GONE);
        setViewVisibility(emptyState, View.VISIBLE);

        if (getContext() != null && isAdded()) {
            Toast.makeText(getContext(), "Error al cargar el historial", Toast.LENGTH_SHORT).show();
        }
    }

    private void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    @Override
    public void onDetailsClick(CompletedTrip trip) {
        Log.d(TAG, "🎯 onDetailsClick() LLAMADO!");

        if (trip == null) {
            Log.w(TAG, "❌ CompletedTrip es null");
            return;
        }

        Log.d(TAG, "✅ Navegando a detalles para trip: " + trip.getId());

        try {
            // Crear fragmento de detalles del viaje completado
            TripHistoryDetailsFragment detailsFragment = TripHistoryDetailsFragment.newInstance(trip);

            if (getParentFragmentManager() != null) {
                Log.d(TAG, "✅ FragmentManager disponible, iniciando transacción");
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detailsFragment)
                        .addToBackStack("trip_history_details")
                        .commit();

                Log.d(TAG, "✅ Navegación completada");
            } else {
                Log.e(TAG, "❌ FragmentManager es null");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error en navegación: " + e.getMessage(), e);
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), "Error al abrir detalles: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRepeatTripClick(CompletedTrip trip) {
        if (trip == null) {
            Log.w(TAG, "onRepeatTripClick: CompletedTrip es null");
            return;
        }

        Log.d(TAG, "onRepeatTripClick: Repitiendo viaje " + trip.getId());

        try {
            // Mostrar diálogo de confirmación
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Repetir Viaje")
                    .setMessage("¿Deseas solicitar un viaje similar desde " + trip.getOriginAddress() + " hasta " + trip.getDestinationAddress() + "?")
                    .setPositiveButton("Sí, solicitar", (dialog, which) -> {
                        // Aquí implementarías la lógica para crear una nueva solicitud
                        // basada en los datos del viaje anterior
                        if (getContext() != null && isAdded()) {
                            Toast.makeText(getContext(), "Funcionalidad de repetir viaje será implementada", Toast.LENGTH_SHORT).show();
                        }

                        // Podrías navegar a la pantalla de solicitudes o crear una nueva solicitud automáticamente
                        // Por ejemplo:
                        // createNewTripRequest(trip);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error en onRepeatTripClick: " + e.getMessage(), e);
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), "Error al procesar solicitud", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para generar datos de ejemplo del historial
    private List<CompletedTrip> generarHistorialEjemplo() {
        List<CompletedTrip> historial = new ArrayList<>();

        try {
            // Generar algunas fechas recientes
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            Random random = new Random();

            String[] hotelNames = {
                    "Hotel Gran Plaza", "Hotel Miraflores Park", "Hotel Lima Centro",
                    "Hotel San Isidro Business", "Hotel Barranco Boutique"
            };

            String[] clientNames = {
                    "Carlos Mendoza", "Ana García", "Luis Rodríguez",
                    "María Torres", "José Hernández", "Carmen López"
            };

            String[] origins = {
                    "Hotel Gran Plaza, Av. La Marina 123, San Miguel",
                    "Hotel Miraflores Park, Av. Malecón 456, Miraflores",
                    "Hotel Lima Centro, Jr. De la Unión 789, Lima Centro"
            };

            String[] destinations = {
                    "Aeropuerto Internacional Jorge Chávez, Callao",
                    "Terminal Terrestre Plaza Norte, Los Olivos",
                    "Estación Central del Metropolitano, Lima"
            };

            String[] tripTypes = {
                    "Hotel-Aeropuerto", "Aeropuerto-Hotel", "Hotel-Terminal", "Otros"
            };

            String[] paymentMethods = {
                    "Efectivo", "Tarjeta", "Yape", "Plin"
            };

            // Generar viajes de los últimos 30 días
            for (int i = 0; i < 15; i++) {
                calendar.add(Calendar.DAY_OF_YEAR, -random.nextInt(30));

                String date = dateFormat.format(calendar.getTime());
                String time = timeFormat.format(calendar.getTime());

                double totalAmount = 50.0 + (random.nextDouble() * 100.0);
                double earnings = totalAmount * 0.8; // 80% para el conductor
                int duration = 15 + random.nextInt(45); // 15-60 minutos
                double distance = 5.0 + (random.nextDouble() * 20.0); // 5-25 km
                float rating = 4.0f + (random.nextFloat() * 1.0f); // 4.0-5.0

                String status = random.nextBoolean() ? "Completado" : (random.nextInt(10) > 8 ? "Cancelado" : "Completado");

                historial.add(new CompletedTrip(
                        "trip_" + (i + 1),
                        hotelNames[random.nextInt(hotelNames.length)],
                        clientNames[random.nextInt(clientNames.length)],
                        origins[random.nextInt(origins.length)],
                        destinations[random.nextInt(destinations.length)],
                        date,
                        time,
                        totalAmount,
                        duration,
                        distance,
                        rating,
                        paymentMethods[random.nextInt(paymentMethods.length)],
                        status,
                        status.equals("Completado") ? "Viaje completado exitosamente" : "Viaje cancelado por el cliente",
                        "https://cf.bstatic.com/xdata/images/hotel/max1024x768/237363319.jpg",
                        status.equals("Completado") ? earnings : 0.0,
                        tripTypes[random.nextInt(tripTypes.length)]
                ));

                // Resetear calendario para la próxima iteración
                calendar = Calendar.getInstance();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error al generar historial de ejemplo: " + e.getMessage(), e);
        }

        return historial;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias para evitar memory leaks
        recyclerHistorial = null;
        adapter = null;
        historialList = null;
        preferenceManager = null;
        swipeRefreshLayout = null; // NUEVO
        // btnRefresh = null; // REMOVER esta línea
        Log.d(TAG, "Vista destruida y referencias limpiadas");
    }
}