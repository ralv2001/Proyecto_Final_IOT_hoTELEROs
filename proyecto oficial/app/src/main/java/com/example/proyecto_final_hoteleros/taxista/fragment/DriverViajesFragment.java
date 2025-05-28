package com.example.proyecto_final_hoteleros.taxista.fragment;

import static android.content.ContentValues.TAG;

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

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adapters.ViajesAdapter;
import com.example.proyecto_final_hoteleros.taxista.model.SolicitudViaje;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.proyecto_final_hoteleros.taxista.fragment.TripDetailsFragment;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class DriverViajesFragment extends Fragment implements ViajesAdapter.ViajeListener {

    private RecyclerView recyclerViajes;
    private ViajesAdapter adapter;
    private List<SolicitudViaje> solicitudesList;

    private LinearLayout emptyState;
    private LinearLayout loadingState;
    private TextView tvSolicitudesCount;
    private FloatingActionButton btnRefresh;

    public DriverViajesFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_viajes, container, false);

        // Inicializar vistas
        recyclerViajes = view.findViewById(R.id.recyclerViajes);
        emptyState = view.findViewById(R.id.emptyState);
        loadingState = view.findViewById(R.id.loadingState);
        tvSolicitudesCount = view.findViewById(R.id.tvSolicitudesCount);
        btnRefresh = view.findViewById(R.id.btnRefresh);

        // Configurar RecyclerView
        recyclerViajes.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar lista y adaptador
        solicitudesList = new ArrayList<>();
        adapter = new ViajesAdapter(getContext(), solicitudesList, this);
        recyclerViajes.setAdapter(adapter);

        // Configurar botón de actualizar
        btnRefresh.setOnClickListener(v -> cargarSolicitudes());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cargar datos al iniciar
        cargarSolicitudes();
    }

    private void cargarSolicitudes() {
        // Mostrar estado de carga
        showLoadingState();

        // Simulamos una carga de datos de la base de datos (reemplazar con Firebase)
        new Handler().postDelayed(() -> {
            // Aquí cargarías los datos reales
            List<SolicitudViaje> nuevasSolicitudes = generarDatosEjemplo();

            // Actualizar la lista
            solicitudesList.clear();
            solicitudesList.addAll(nuevasSolicitudes);
            adapter.notifyDataSetChanged();

            // Actualizar contador
            actualizarContador();

            // Mostrar estado correspondiente
            updateVisibility();

        }, 1500); // Simular tiempo de carga
    }

    private void actualizarContador() {
        int count = solicitudesList.size();
        tvSolicitudesCount.setText("Tienes " + count + " solicitude" + (count == 1 ? "" : "s") + " pendiente" + (count == 1 ? "" : "s"));
    }

    private void updateVisibility() {
        if (solicitudesList.isEmpty()) {
            recyclerViajes.setVisibility(View.GONE);
            loadingState.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViajes.setVisibility(View.VISIBLE);
            loadingState.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private void showLoadingState() {
        recyclerViajes.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        loadingState.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAcceptClick(SolicitudViaje solicitud) {
        // Implementar lógica para aceptar solicitud
        Toast.makeText(getContext(), "Viendo detalles del servicio para " + solicitud.getHotelName(), Toast.LENGTH_SHORT).show();

        // Aquí podrías abrir un nuevo fragment o activity con detalles
        // Por ejemplo: mostrar mapa con ruta al hotel
    }

    @Override
    public void onRejectClick(SolicitudViaje solicitud) {
        // Implementar lógica para rechazar solicitud
        Toast.makeText(getContext(), "Solicitud rechazada", Toast.LENGTH_SHORT).show();

        // En una implementación real, aquí actualizarías la base de datos

        // Para el ejemplo, simplemente eliminamos la solicitud de la lista
        solicitudesList.remove(solicitud);
        adapter.notifyDataSetChanged();
        actualizarContador();
        updateVisibility();
    }

    @Override
    public void onDetailsClick(SolicitudViaje solicitud) {
        Log.d(TAG, "onDetailsClick: Navegando a detalles para " + solicitud.getId());

        try {
            TripDetailsFragment detailsFragment = TripDetailsFragment.newInstance(solicitud);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailsFragment)
                    .addToBackStack("trip_details")
                    .commit();

            Log.d(TAG, "onDetailsClick: Navegación completada");
        } catch (Exception e) {
            Log.e(TAG, "Error en navegación: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Método para generar datos de ejemplo
    private List<SolicitudViaje> generarDatosEjemplo() {
        List<SolicitudViaje> ejemplos = new ArrayList<>();

        // Crear algunos ejemplos usando imágenes locales de drawable
        ejemplos.add(new SolicitudViaje(
                "viaje1",
                "Hotel Gran Plaza",
                4.8f,
                "Próxima",
                "15 - 18 Mar",
                "15 Mar - 18 Mar, 2025",
                "San Miguel",
                "Av. La Marina 123, San Miguel",
                "https://cf.bstatic.com/xdata/images/hotel/max1024x768/237363319.jpg",
                900.0,
                "Cliente esperando en lobby del hotel a las 14:30.",
                true,
                "Juan Pérez", // Nombre del cliente
                "Hotel Gran Plaza, Av. La Marina 123", // Origen
                "Aeropuerto Jorge Chávez", // Destino
                20 // Tiempo estimado en minutos
        ));

        ejemplos.add(new SolicitudViaje(
                "viaje2",
                "Hotel Gran Plaza",
                4.8f,
                "Próxima",
                "15 - 18 Mar",
                "15 Mar - 18 Mar, 2025",
                "San Miguel",
                "Av. La Marina 123, San Miguel",
                "https://cf.bstatic.com/xdata/images/hotel/max1024x768/237363319.jpg",
                900.0,
                "Cliente esperando en lobby del hotel a las 14:30.",
                true,
                "Juan Pérez", // Nombre del cliente
                "Hotel Gran Plaza, Av. La Marina 123", // Origen
                "Aeropuerto Jorge Chávez", // Destino
                20 // Tiempo estimado en minutos
        ));

        ejemplos.add(new SolicitudViaje(
                "viaje3",
                "Hotel Gran Plaza",
                4.8f,
                "Próxima",
                "15 - 18 Mar",
                "15 Mar - 18 Mar, 2025",
                "San Miguel",
                "Av. La Marina 123, San Miguel",
                "https://cf.bstatic.com/xdata/images/hotel/max1024x768/237363319.jpg",
                900.0,
                "Cliente esperando en lobby del hotel a las 14:30.",
                true,
                "Juan Pérez", // Nombre del cliente
                "Hotel Gran Plaza, Av. La Marina 123", // Origen
                "Aeropuerto Jorge Chávez", // Destino
                20 // Tiempo estimado en minutos
        ));

        ejemplos.add(new SolicitudViaje(
                "viaje4",
                "Hotel Gran Plaza",
                4.8f,
                "Próxima",
                "15 - 18 Mar",
                "15 Mar - 18 Mar, 2025",
                "San Miguel",
                "Av. La Marina 123, San Miguel",
                "https://cf.bstatic.com/xdata/images/hotel/max1024x768/237363319.jpg",
                900.0,
                "Cliente esperando en lobby del hotel a las 14:30.",
                true,
                "Juan Pérez", // Nombre del cliente
                "Hotel Gran Plaza, Av. La Marina 123", // Origen
                "Aeropuerto Jorge Chávez", // Destino
                20 // Tiempo estimado en minutos
        ));

        return ejemplos;
    }
}