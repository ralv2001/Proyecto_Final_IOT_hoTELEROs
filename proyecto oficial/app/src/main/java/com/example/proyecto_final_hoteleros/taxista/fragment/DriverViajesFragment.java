package com.example.proyecto_final_hoteleros.taxista.fragment;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
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
import com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity;
import com.example.proyecto_final_hoteleros.taxista.adapters.ViajesAdapter;
import com.example.proyecto_final_hoteleros.taxista.fragment.DriverMapFragment;
import com.example.proyecto_final_hoteleros.taxista.model.CheckoutReservation;
import com.example.proyecto_final_hoteleros.taxista.model.SolicitudViaje;
import com.example.proyecto_final_hoteleros.taxista.utils.DriverPreferenceManager;
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class DriverViajesFragment extends Fragment implements ViajesAdapter.ViajeListener {

    private static final String TAG = "DriverViajesFragment";

    // Vistas
    private RecyclerView recyclerViajes;
    private ViajesAdapter adapter;
    private List<SolicitudViaje> solicitudesList;
    private LinearLayout emptyState;
    private LinearLayout loadingState;
    private TextView tvSolicitudesCount;
    private FloatingActionButton btnRefresh;

    // Managers
    private DriverPreferenceManager preferenceManager;
    private FirebaseManager firebaseManager;
    private ListenerRegistration checkoutListener;

    // Estados
    private boolean isLoadingData = false;

    public DriverViajesFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Creando vista del fragment");
        View view = inflater.inflate(R.layout.taxi_fragment_driver_viajes, container, false);

        // Inicializar vistas
        initViews(view);

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar listeners
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        recyclerViajes = view.findViewById(R.id.recyclerViajes);
        emptyState = view.findViewById(R.id.emptyState);
        loadingState = view.findViewById(R.id.loadingState);
        tvSolicitudesCount = view.findViewById(R.id.tvSolicitudesCount);
        btnRefresh = view.findViewById(R.id.btnRefresh);
    }

    private void setupRecyclerView() {
        recyclerViajes.setLayoutManager(new LinearLayoutManager(getContext()));
        solicitudesList = new ArrayList<>();
        adapter = new ViajesAdapter(getContext(), solicitudesList, this);
        recyclerViajes.setAdapter(adapter);
    }

    private void setupListeners() {
        btnRefresh.setOnClickListener(v -> {
            Log.d(TAG, "Botón refresh presionado");
            cargarSolicitudes();
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Configurando fragment");

        // Inicializar managers
        preferenceManager = new DriverPreferenceManager(getContext());
        firebaseManager = FirebaseManager.getInstance();

        // ✅ VERIFICAR ESTADO ANTES DE CARGAR DATOS:
        verificarEstadoYCargarDatos();

        // ✅ NO configurar listener automáticamente
        // Solo cargar cuando esté disponible
    }

    // ✅ NUEVO MÉTODO:
    private void verificarEstadoYCargarDatos() {
        if (isDriverAvailable()) {
            Log.d(TAG, "✅ Taxista disponible, cargando solicitudes...");
            cargarSolicitudes();
            setupRealtimeListener();
        } else {
            Log.d(TAG, "⚠️ Taxista no disponible, mostrando mensaje...");
            mostrarMensajeNoDisponible();
        }
    }

    // ✅ NUEVO MÉTODO:
    private void mostrarMensajeNoDisponible() {
        // Limpiar lista
        solicitudesList.clear();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // Mostrar estado vacío
        updateVisibility();

        // Mostrar mensaje
        if (tvSolicitudesCount != null) {
            tvSolicitudesCount.setText("⚠️ Activa 'En servicio' en el mapa para recibir solicitudes");
        }
    }

    /**
     * Cargar solicitudes de viaje desde Firebase
     */
    private void cargarSolicitudes() {
        if (isLoadingData) {
            Log.d(TAG, "Ya se están cargando datos, ignorando solicitud");
            return;
        }
        // ✅ VERIFICAR ESTADO DEL TAXISTA PRIMERO:
        if (!isDriverAvailable()) {
            Log.d(TAG, "⚠️ Taxista no disponible, no cargar solicitudes");

            // Mostrar mensaje y lista vacía
            solicitudesList.clear();
            adapter.notifyDataSetChanged();
            updateVisibility();

            if (tvSolicitudesCount != null) {
                tvSolicitudesCount.setText("Activa el estado 'En servicio' para recibir solicitudes");
            }

            return;
        }

        if (isLoadingData) {
            Log.d(TAG, "Ya se están cargando datos, ignorando solicitud");
            return;
        }

        Log.d(TAG, "🔄 Cargando solicitudes de checkout...");
        isLoadingData = true;
        showLoadingState();

        firebaseManager.getCheckoutReservations(new FirebaseManager.CheckoutCallback() {
            @Override
            public void onSuccess(List<CheckoutReservation> reservations) {
                Log.d(TAG, "✅ " + reservations.size() + " reservas obtenidas de Firebase");

                // Convertir CheckoutReservation a SolicitudViaje
                List<SolicitudViaje> nuevasSolicitudes = new ArrayList<>();
                for (CheckoutReservation reservation : reservations) {
                    if (reservation.isPending()) { // Solo mostrar pendientes
                        SolicitudViaje solicitud = SolicitudViaje.fromCheckoutReservation(reservation);
                        nuevasSolicitudes.add(solicitud);
                        Log.d(TAG, "📋 Convertida: " + solicitud.getHotelName() + " - " + solicitud.getClientName());
                    }
                }

                // Actualizar UI en el hilo principal
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        solicitudesList.clear();
                        solicitudesList.addAll(nuevasSolicitudes);
                        adapter.notifyDataSetChanged();
                        actualizarContador();
                        updateVisibility();
                        isLoadingData = false;

                        Log.d(TAG, "🎯 UI actualizada con " + solicitudesList.size() + " solicitudes");
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando reservas: " + error);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // En caso de error, cargar datos de ejemplo para testing
                        cargarDatosEjemplo();
                        isLoadingData = false;

                        Toast.makeText(getContext(),
                                "Error conectando con servidor. Mostrando datos de ejemplo.",
                                Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
    private boolean isDriverAvailable() {
        if (preferenceManager != null) {
            return preferenceManager.isDriverAvailable();
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "🔄 Fragment resumed, verificando estado del taxista...");

        // ✅ VERIFICAR ESTADO CADA VEZ QUE SE REGRESE:
        verificarEstadoYCargarDatos();
    }

    /**
     * Configurar listener en tiempo real para nuevas reservas
     */
    private void setupRealtimeListener() {
        Log.d(TAG, "🔄 Configurando listener en tiempo real...");

        // ✅ DETENER LISTENER ANTERIOR SI EXISTE:
        if (checkoutListener != null) {
            checkoutListener.remove();
            checkoutListener = null;
            Log.d(TAG, "🗑️ Listener anterior removido");
        }

        // ✅ SOLO CREAR LISTENER SI ESTÁ DISPONIBLE:
        if (!isDriverAvailable()) {
            Log.d(TAG, "⚠️ Taxista no disponible, no se configura listener");
            return;
        }

        checkoutListener = firebaseManager.listenToCheckoutReservations(
                new FirebaseManager.RealtimeCheckoutCallback() {
                    @Override
                    public void onNewReservation(CheckoutReservation reservation) {
                        // ✅ VERIFICAR ESTADO ANTES DE AGREGAR:
                        if (!isDriverAvailable()) {
                            Log.d(TAG, "⚠️ Nueva reserva ignorada - taxista no disponible");
                            return;
                        }

                        Log.d(TAG, "🆕 Nueva reserva detectada: " + reservation.getHotelName());

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                SolicitudViaje nuevaSolicitud = SolicitudViaje.fromCheckoutReservation(reservation);
                                solicitudesList.add(0, nuevaSolicitud);
                                adapter.notifyItemInserted(0);
                                actualizarContador();
                                updateVisibility();

                                Toast.makeText(getContext(),
                                        "Nueva solicitud: " + reservation.getHotelName(),
                                        Toast.LENGTH_LONG).show();
                            });
                        }
                    }

                    @Override
                    public void onReservationUpdated(CheckoutReservation reservation) {
                        Log.d(TAG, "🔄 Reserva actualizada: " + reservation.getId());
                        if (!reservation.isPending()) {
                            removeReservationFromList(reservation.getId());
                        }
                    }

                    @Override
                    public void onReservationRemoved(String reservationId) {
                        Log.d(TAG, "🗑️ Reserva removida: " + reservationId);
                        removeReservationFromList(reservationId);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "❌ Error en listener: " + error);
                    }
                });
    }

    private void removeReservationFromList(String reservationId) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                for (int i = 0; i < solicitudesList.size(); i++) {
                    SolicitudViaje solicitud = solicitudesList.get(i);
                    if (reservationId.equals(solicitud.getReservationId())) {
                        solicitudesList.remove(i);
                        adapter.notifyItemRemoved(i);
                        actualizarContador();
                        updateVisibility();
                        break;
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // ✅ LIMPIAR LISTENER SIEMPRE:
        if (checkoutListener != null) {
            checkoutListener.remove();
            checkoutListener = null;
            Log.d(TAG, "🧹 Listener de checkout removido en onDestroy");
        }
    }

    /**
     * Actualizar contador de solicitudes
     */
    private void actualizarContador() {
        if (tvSolicitudesCount != null) {
            int count = solicitudesList.size();
            tvSolicitudesCount.setText("Tienes " + count + " solicitud" +
                    (count == 1 ? "" : "es") + " pendiente" + (count == 1 ? "" : "s"));
        }
    }

    /**
     * Actualizar visibilidad de estados
     */
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

    /**
     * Mostrar estado de carga
     */
    private void showLoadingState() {
        recyclerViajes.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        loadingState.setVisibility(View.VISIBLE);
    }

    // ========== IMPLEMENTACIÓN DE ViajesAdapter.ViajeListener ==========

    @Override
    public void onAcceptClick(SolicitudViaje solicitud) {
        Log.d(TAG, "onAcceptClick: " + solicitud.getHotelName());

        // ✅ SIEMPRE NAVEGAR AL MAPA CON ARGUMENTOS (sin importar el tipo):
        navegarAMapaConRuta(solicitud);
    }

    // ✅ MANTENER ESTE MÉTODO PERO ASEGURAR QUE FUNCIONE:
    private void navegarAMapaConRuta(SolicitudViaje solicitud) {
        Log.d(TAG, "🗺️ Navegando al mapa con ruta para: " + solicitud.getHotelName());

        // Crear el fragment del mapa con argumentos
        DriverMapFragment mapFragment = new DriverMapFragment();

        // Pasar datos del servicio como argumentos
        Bundle args = new Bundle();
        args.putString("destination_address", solicitud.getDestinationAddress());
        args.putString("client_name", solicitud.getClientName());
        args.putString("destination_name", solicitud.getHotelName());
        args.putString("client_phone", solicitud.getClientPhone());
        args.putString("service_type", "checkout_gratuito");
        mapFragment.setArguments(args);

        // Navegar al mapa
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mapFragment)
                    .addToBackStack("map_with_route")
                    .commit();

            Toast.makeText(getContext(),
                    "🚕 Servicio aceptado!\n📍 Dirígete a: " + solicitud.getHotelName(),
                    Toast.LENGTH_LONG).show();

            Log.d(TAG, "✅ Navegación al mapa desde DriverViajesFragment completada");
        } else {
            Log.e(TAG, "❌ ParentFragmentManager es null");
            Toast.makeText(getContext(), "Error de navegación", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Aceptar servicio de checkout gratuito

    private void aceptarServicioCheckout(SolicitudViaje solicitud) {
        Log.d(TAG, "🚕 Aceptando servicio de checkout para: " + solicitud.getClientName());

        // Obtener ID del taxista
        String driverId = getDriverId();
        if (driverId == null) {
            Toast.makeText(getContext(), "Error: ID de taxista no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar indicador de carga
        showLoadingState();

        // Asignar taxista a la reserva
        firebaseManager.assignDriverToReservation(
                solicitud.getReservationId(),
                driverId,
                new FirebaseManager.DataCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Servicio aceptado exitosamente");

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(),
                                        "¡Servicio aceptado! Dirígete al hotel",
                                        Toast.LENGTH_LONG).show();

                                // Abrir navegación al hotel
                                abrirNavegacionAlHotel(solicitud);

                                // Remover de la lista local
                                solicitudesList.remove(solicitud);
                                adapter.notifyDataSetChanged();
                                actualizarContador();
                                updateVisibility();
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "❌ Error aceptando servicio: " + error);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                updateVisibility(); // Quitar loading
                                Toast.makeText(getContext(),
                                        "Error: " + error,
                                        Toast.LENGTH_LONG).show();
                            });
                        }
                    }
                });
    }
     */
    /**
     * Abrir navegación al hotel

    private void abrirNavegacionAlHotel(SolicitudViaje solicitud) {
        Log.d(TAG, "🗺️ Abriendo navegación al hotel: " + solicitud.getHotelName());

        // OPCIÓN 1: Navegar al mapa sin parámetros especiales
        DriverMapFragment mapFragment = new DriverMapFragment();

        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mapFragment)
                    .addToBackStack("navigation_service")
                    .commit();

            // Mostrar información del servicio
            Toast.makeText(getContext(),
                    "🚕 Dirígete a: " + solicitud.getHotelName() + "\n" +
                            "📍 " + solicitud.getOriginAddress() + "\n" +
                            "👤 Cliente: " + solicitud.getClientName(),
                    Toast.LENGTH_LONG).show();

            Log.d(TAG, "✅ Navegación al mapa iniciada");
        }
    }
     */
    @Override
    public void onRejectClick(SolicitudViaje solicitud) {
        Log.d(TAG, "onRejectClick: " + solicitud.getHotelName());

        Toast.makeText(getContext(), "Solicitud rechazada", Toast.LENGTH_SHORT).show();

        // Eliminar de la lista local
        solicitudesList.remove(solicitud);
        adapter.notifyDataSetChanged();
        actualizarContador();
        updateVisibility();

        // Guardar en preferencias locales
        if (preferenceManager != null) {
            preferenceManager.saveTripRequests(solicitudesList);
        }
    }

    @Override
    public void onDetailsClick(SolicitudViaje solicitud) {
        Log.d(TAG, "onDetailsClick: Navegando a detalles para " + solicitud.getId());

        try {
            // SIEMPRE usar TripDetailsFragment, tanto para checkout como viajes normales
            TripDetailsFragment detailsFragment = TripDetailsFragment.newInstance(solicitud);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailsFragment)
                    .addToBackStack("trip_details")
                    .commit();

            Log.d(TAG, "✅ Navegación a TripDetailsFragment completada");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error en navegación: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }





    /**
     * Obtener ID del taxista logueado
     */
    private String getDriverId() {
        DriverActivity activity = (DriverActivity) getActivity();
        return activity != null ? activity.getUserId() : null;
    }

    /**
     * Cargar datos de ejemplo para testing
     */
    private void cargarDatosEjemplo() {
        Log.d(TAG, "🧪 Cargando datos de ejemplo...");

        solicitudesList.clear();
        solicitudesList.addAll(generarDatosEjemplo());
        adapter.notifyDataSetChanged();
        actualizarContador();
        updateVisibility();
    }

    /**
     * Generar datos de ejemplo
     */
    private List<SolicitudViaje> generarDatosEjemplo() {
        List<SolicitudViaje> ejemplos = new ArrayList<>();

        // Ejemplo 1: Servicio de checkout
        SolicitudViaje checkout1 = new SolicitudViaje();
        checkout1.setId("checkout_001");
        checkout1.setReservationId("res_001");
        checkout1.setHotelName("Hotel Gran Plaza");
        checkout1.setClientName("Juan Pérez");
        checkout1.setClientPhone("+51 987 654 321");
        checkout1.setOriginAddress("Av. La Marina 123, San Miguel");
        checkout1.setDestinationAddress("Aeropuerto Jorge Chávez");
        checkout1.setCheckoutTime("11:30");
        checkout1.setEstimatedTime(25);
        checkout1.setTipoServicio("checkout_gratuito");
        checkout1.setStatus("Checkout Pendiente");
        checkout1.setLocation("San Miguel");
        checkout1.setNotes("Cliente esperando en lobby, equipaje pesado");
        checkout1.setUrgent(true);
        checkout1.setPrice(0.0);
        checkout1.setRating(4.8f);
        checkout1.setImageUrl("https://cf.bstatic.com/xdata/images/hotel/max1024x768/237363319.jpg");
        ejemplos.add(checkout1);

        // Ejemplo 2: Otro servicio de checkout
        SolicitudViaje checkout2 = new SolicitudViaje();
        checkout2.setId("checkout_002");
        checkout2.setReservationId("res_002");
        checkout2.setHotelName("Hotel Miraflores Park");
        checkout2.setClientName("María García");
        checkout2.setClientPhone("+51 987 123 456");
        checkout2.setOriginAddress("Av. Malecón 456, Miraflores");
        checkout2.setDestinationAddress("Aeropuerto Jorge Chávez");
        checkout2.setCheckoutTime("14:15");
        checkout2.setEstimatedTime(30);
        checkout2.setTipoServicio("checkout_gratuito");
        checkout2.setStatus("Checkout Pendiente");
        checkout2.setLocation("Miraflores");
        checkout2.setNotes("Vuelo internacional, llegar 3 horas antes");
        checkout2.setUrgent(true);
        checkout2.setPrice(0.0);
        checkout2.setRating(4.5f);
        checkout2.setImageUrl("");
        ejemplos.add(checkout2);

        return ejemplos;
    }
}