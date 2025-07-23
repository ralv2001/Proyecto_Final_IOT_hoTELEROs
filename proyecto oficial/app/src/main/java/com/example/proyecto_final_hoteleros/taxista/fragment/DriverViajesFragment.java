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

    // Listeners
    private ListenerRegistration checkoutListener;
    private ListenerRegistration realtimeListener;

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

            // ✅ AGREGAR OPCIÓN DE CREAR DATOS REALES
            if (solicitudesList.isEmpty()) {
                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                        .setTitle("Crear Solicitudes")
                        .setMessage("No hay solicitudes. ¿Crear algunas con hoteles reales de Firebase?")
                        .setPositiveButton("Sí, crear", (dialog, which) -> {
                            crearReservasRealesYRecargar();
                        })
                        .setNegativeButton("Solo recargar", (dialog, which) -> {
                            cargarSolicitudes();
                        })
                        .show();
            } else {
                cargarSolicitudes();
            }
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
            solicitudesList.clear();
            adapter.notifyDataSetChanged();
            updateVisibility();
            if (tvSolicitudesCount != null) {
                tvSolicitudesCount.setText("⚠️ Activa 'En servicio' en el mapa para recibir solicitudes");
            }
            return;
        }

        Log.d(TAG, "🔄 Cargando solicitudes de checkout desde Firebase...");
        isLoadingData = true;
        showLoadingState();

        // ✅ USAR EL NUEVO MÉTODO CORRECTO
        firebaseManager.getCheckoutRequests(new FirebaseManager.CheckoutCallback() {
            @Override
            public void onSuccess(List<CheckoutReservation> reservations) {
                Log.d(TAG, "✅ " + reservations.size() + " reservas obtenidas de Firebase");

                // Convertir CheckoutReservation a SolicitudViaje
                List<SolicitudViaje> nuevasSolicitudes = new ArrayList<>();
                for (CheckoutReservation reservation : reservations) {
                    // ✅ FILTRAR SOLO RESERVAS PENDIENTES
                    if ("pending".equals(reservation.getTaxiStatus())) {
                        SolicitudViaje solicitud = convertirReservaASolicitud(reservation);
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

                        // ✅ MENSAJE DE ESTADO AL USUARIO
                        if (nuevasSolicitudes.isEmpty()) {
                            Toast.makeText(getContext(),
                                    "📭 No hay solicitudes pendientes por el momento",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(),
                                    "✅ " + nuevasSolicitudes.size() + " solicitud(es) encontrada(s)",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando reservas: " + error);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // ✅ CAMBIAR ESTA PARTE - NO usar datos hardcodeados
                        // En lugar de cargarDatosEjemplo(), crear reservas reales
                        Log.w(TAG, "🏨 Creando reservas reales de hoteles de Firebase...");
                        crearReservasRealesYRecargar();

                        Toast.makeText(getContext(),
                                "⚠️ Creando solicitudes con hoteles reales...\nPor favor espera un momento.",
                                Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    /**
     * Crear reservas reales usando hoteles de Firebase
     */
    private void crearReservasRealesYRecargar() {
        Log.d(TAG, "🏨 Creando reservas con hoteles reales de Firebase...");

        firebaseManager.createSampleCheckoutReservations(new FirebaseManager.DataCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Reservas reales creadas exitosamente");

                // Esperar 2 segundos y recargar
                new Handler().postDelayed(() -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            cargarSolicitudes(); // Recargar con datos reales
                            Toast.makeText(getContext(),
                                    "✅ Solicitudes creadas con hoteles reales",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }, 2000);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error creando reservas reales: " + error);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Solo si falla todo, usar datos mínimos de ejemplo
                        solicitudesList.clear();
                        updateVisibility();
                        isLoadingData = false;

                        Toast.makeText(getContext(),
                                "❌ Error: " + error + "\nIntenta refrescar más tarde",
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
     * Configurar listener en tiempo real MEJORADO
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

        checkoutListener = firebaseManager.setupCheckoutRealtimeListener(
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
                                SolicitudViaje nuevaSolicitud = convertirReservaASolicitud(reservation);
                                solicitudesList.add(0, nuevaSolicitud);
                                adapter.notifyItemInserted(0);
                                actualizarContador();
                                updateVisibility();

                                // ✅ NOTIFICACIÓN VISUAL Y SONORA
                                Toast.makeText(getContext(),
                                        "🆕 Nueva solicitud: " + reservation.getHotelName(),
                                        Toast.LENGTH_LONG).show();

                                // ✅ VIBRACIÓN OPCIONAL
                                try {
                                    android.os.Vibrator vibrator = (android.os.Vibrator)
                                            getContext().getSystemService(android.content.Context.VIBRATOR_SERVICE);
                                    if (vibrator != null) {
                                        vibrator.vibrate(500); // 500ms vibración
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, "No se pudo vibrar: " + e.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onReservationUpdated(CheckoutReservation reservation) {
                        Log.d(TAG, "🔄 Reserva actualizada: " + reservation.getId());
                        // ✅ VERIFICAR STATUS CORRECTO:
                        if (!"pending".equals(reservation.getTaxiStatus())) {
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
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(),
                                        "Error de conexión: " + error,
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });

        Log.d(TAG, "✅ Listener en tiempo real configurado");
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        // ✅ LIMPIAR TODOS LOS LISTENERS CORRECTAMENTE
        if (checkoutListener != null) {
            checkoutListener.remove();
            checkoutListener = null;
            Log.d(TAG, "🧹 checkoutListener removido en onDestroy");
        }

        if (realtimeListener != null) {
            realtimeListener.remove();
            realtimeListener = null;
            Log.d(TAG, "🧹 realtimeListener removido en onDestroy");
        }

        // ✅ LIMPIAR REFERENCIAS PARA EVITAR MEMORY LEAKS
        solicitudesList = null;
        adapter = null;
        preferenceManager = null;
        firebaseManager = null;

        Log.d(TAG, "🧹 DriverViajesFragment destruido y recursos limpiados");
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
     * Generar datos mínimos de ejemplo (solo para casos extremos)
     */
    private List<SolicitudViaje> generarDatosEjemplo() {
        Log.d(TAG, "⚠️ Usando datos mínimos de emergencia");

        // Lista vacía - forzar que use datos reales de Firebase
        return new ArrayList<>();
    }

    // ========== MÉTODOS PARA CONVERSIÓN Y LISTENER ==========

    /**
     * Convertir CheckoutReservation a SolicitudViaje (SIMPLIFICADO PARA TU CLASE)
     */
    private SolicitudViaje convertirReservaASolicitud(CheckoutReservation reservation) {
        SolicitudViaje solicitud = new SolicitudViaje();

        // ✅ DATOS ESENCIALES (usando tus métodos existentes)
        solicitud.setId(reservation.getId());
        solicitud.setReservationId(reservation.getId());
        solicitud.setHotelName(reservation.getHotelName());
        solicitud.setClientName(reservation.getClientName());
        solicitud.setClientPhone(reservation.getClientPhone());
        solicitud.setOriginAddress(reservation.getHotelAddress());
        solicitud.setDestinationAddress("Aeropuerto Internacional Jorge Chávez");
        solicitud.setCheckoutTime(reservation.getCheckoutTime());

        // ✅ USAR TUS MÉTODOS EXISTENTES
        solicitud.setTipoServicio("checkout_gratuito"); // Tu método existente
        solicitud.setEstimatedTime(reservation.getEstimatedDuration()); // Tu método existente
        solicitud.setEstimatedDistance(reservation.getEstimatedDistance()); // Nuevo método
        solicitud.setCreatedAt(reservation.getCreatedAt()); // Nuevo método

        // ✅ DATOS FIJOS
        solicitud.setStatus("Checkout Pendiente");
        solicitud.setPrice(0.0); // Gratuito
        solicitud.setLocation(extractLocationFromAddress(reservation.getHotelAddress()));
        solicitud.setNotes(reservation.getNotes());
        solicitud.setUrgent(false);
        solicitud.setRating(4.5f);
        solicitud.setImageUrl("");

        return solicitud;
    }

    /**
     * Extraer distrito/zona de la dirección completa
     */
    private String extractLocationFromAddress(String fullAddress) {
        if (fullAddress == null) return "Lima";

        // Buscar patrones comunes de distritos de Lima
        String[] districts = {"Miraflores", "San Isidro", "Barranco", "San Miguel",
                "Cercado de Lima", "Lima Centro", "Surco", "La Molina",
                "Callao", "Jesús María", "Magdalena", "Pueblo Libre"};

        for (String district : districts) {
            if (fullAddress.toLowerCase().contains(district.toLowerCase())) {
                return district;
            }
        }

        return "Lima"; // Valor por defecto
    }


    /**
     * Remover reserva de la lista local
     */
    private void removeReservationFromList(String reservationId) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                for (int i = 0; i < solicitudesList.size(); i++) {
                    SolicitudViaje solicitud = solicitudesList.get(i);
                    if (reservationId.equals(solicitud.getReservationId()) ||
                            reservationId.equals(solicitud.getId())) {
                        solicitudesList.remove(i);
                        adapter.notifyItemRemoved(i);
                        actualizarContador();
                        updateVisibility();

                        Toast.makeText(getContext(),
                                "Solicitud actualizada: " + solicitud.getHotelName(),
                                Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            });
        }
    }
}