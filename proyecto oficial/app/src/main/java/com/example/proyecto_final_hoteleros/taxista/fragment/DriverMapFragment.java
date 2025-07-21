package com.example.proyecto_final_hoteleros.taxista.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity;
import com.example.proyecto_final_hoteleros.taxista.model.DriverProfile;
import com.example.proyecto_final_hoteleros.taxista.services.DirectionsService;
import com.example.proyecto_final_hoteleros.taxista.services.MockNotificationService;
import com.example.proyecto_final_hoteleros.taxista.utils.DriverPreferenceManager;
import com.example.proyecto_final_hoteleros.taxista.utils.NotificationHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "DriverMapFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final double SERVICE_RADIUS_METERS = 5000;

    // Mapa y ubicación (existente)
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private boolean shouldCenterCamera = true;
    private boolean isLocationUpdatesActive = false;
    private boolean hasLocationPermission = false;

    // Marcadores (existente + nuevo)
    private Marker driverMarker;
    private List<Marker> hotelMarkers;
    private Marker destinationMarker; // 🆕 NUEVO
    private Polyline currentRoute; // 🆕 NUEVO

    // Servicios (existente + nuevo)
    private DriverPreferenceManager preferenceManager;
    private NotificationHelper notificationHelper;
    private MockNotificationService mockNotificationService;
    private DirectionsService directionsService; // 🆕 NUEVO

    // Vistas existentes
    private CardView cardEstadoTaxista;
    private TextView tvEstadoServicio;
    private Switch switchDisponible;
    private FloatingActionButton btnMyLocation;
    private FloatingActionButton btnZoomIn;
    private FloatingActionButton btnZoomOut;
    private CircleImageView ivProfilePhoto;
    private TextView tvDriverName;
    private ImageView ivNotifications;
    private TextView tvNotificationBadge;

    // 🆕 NUEVAS VISTAS PARA SERVICIO ACTIVO
    private MaterialCardView cardServicioActivo;
    private TextView tvClienteName;
    private TextView tvHotelName;
    private TextView tvDestinationAddress;
    private TextView tvDistanceTime;
    private TextView tvServiceStatus;
    private MaterialButton btnIniciarNavegacion;
    private MaterialButton btnCompletarServicio;
    private LinearLayout headerServicioActivo;
    private LinearLayout contenidoExpandible;
    private ImageView iconExpandCollapse;
    private MaterialButton btnMostrarRuta;
    private boolean isCardExpanded = false;

    // Estado (existente + nuevo)
    private int notificationCount = 0;
    private LatLng currentDriverLocation; // 🆕 NUEVO
    private String activeServiceDestination; // 🆕 NUEVO
    private String activeServiceClientName; // 🆕 NUEVO
    private String activeServiceHotelName; // 🆕 NUEVO
    private String activeServiceClientPhone; // 🆕 NUEVO

    public DriverMapFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.taxi_fragment_driver_map, container, false);

        // Inicializar vistas existentes
        initializeExistingViews(view);

        // 🆕 INICIALIZAR NUEVAS VISTAS
        initializeNewViews(view);

        // Configurar listeners existentes
        setupExistingListeners(view); // ✅ PASAR VIEW COMO PARÁMETRO

        // 🆕 CONFIGURAR NUEVOS LISTENERS
        setupNewListeners();

        return view;
    }

    private void initializeExistingViews(View view) {
        // Vistas existentes
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        tvDriverName = view.findViewById(R.id.tvDriverName);
        ivNotifications = view.findViewById(R.id.ivNotifications);
        cardEstadoTaxista = view.findViewById(R.id.cardEstadoTaxista);
        tvEstadoServicio = view.findViewById(R.id.tvEstadoServicio);
        switchDisponible = view.findViewById(R.id.switchDisponible);
        btnMyLocation = view.findViewById(R.id.btnMyLocation);
        btnZoomIn = view.findViewById(R.id.btnZoomIn);
        btnZoomOut = view.findViewById(R.id.btnZoomOut);
        tvNotificationBadge = view.findViewById(R.id.tvNotificationBadge);

        notificationCount = 3;
        updateNotificationBadge();
    }

    // 🆕 INICIALIZAR NUEVAS VISTAS
    private void initializeNewViews(View view) {
        cardServicioActivo = view.findViewById(R.id.cardServicioActivo);
        tvClienteName = view.findViewById(R.id.tvClienteName);
        tvHotelName = view.findViewById(R.id.tvHotelName);
        tvDestinationAddress = view.findViewById(R.id.tvDestinationAddress);
        tvDistanceTime = view.findViewById(R.id.tvDistanceTime);
        tvServiceStatus = view.findViewById(R.id.tvServiceStatus);
        btnIniciarNavegacion = view.findViewById(R.id.btnIniciarNavegacion);
        btnCompletarServicio = view.findViewById(R.id.btnCompletarServicio);
        headerServicioActivo = view.findViewById(R.id.headerServicioActivo);
        contenidoExpandible = view.findViewById(R.id.contenidoExpandible);
        iconExpandCollapse = view.findViewById(R.id.iconExpandCollapse);
        tvDestinationAddress = view.findViewById(R.id.tvDestinationAddress);
        btnMostrarRuta = view.findViewById(R.id.btnMostrarRuta);

    }

    private void setupExistingListeners(View view) { // ⭐ AGREGAR PARÁMETRO VIEW
        // Listeners existentes
        switchDisponible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateDriverStatus(isChecked);
            }
        });

        ivNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotificationsDialog();
            }
        });

        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });

        btnZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.zoomIn());
                }
            }
        });

        btnZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.zoomOut());
                }
            }
        });

        // ✅ AHORA CON VIEW DISPONIBLE:
        FloatingActionButton btnTestNotification = view.findViewById(R.id.btnTestNotification);
        if (btnTestNotification != null) {
            btnTestNotification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mockNotificationService != null) {
                        showNotificationTestMenu();
                    }
                }
            });
        }
    }

    // 🆕 CONFIGURAR NUEVOS LISTENERS
    private void setupNewListeners() {
        if (btnIniciarNavegacion != null) {
            btnIniciarNavegacion.setOnClickListener(v -> startExternalNavigation());
        }
        if (btnMostrarRuta != null) {
            btnMostrarRuta.setOnClickListener(v -> mostrarRutaEnMapa());
        }
        if (headerServicioActivo != null) {
            headerServicioActivo.setOnClickListener(v -> toggleCardExpansion());
        }


        if (btnCompletarServicio != null) {
            btnCompletarServicio.setOnClickListener(v -> openQRScanner());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "🗺️ Inicializando DriverMapFragment con funcionalidad completa");

        // Inicializar servicios existentes
        preferenceManager = new DriverPreferenceManager(requireContext());
        notificationHelper = new NotificationHelper(requireContext());
        mockNotificationService = new MockNotificationService(notificationHelper, preferenceManager);

        // 🆕 INICIALIZAR NUEVO SERVICIO
        directionsService = new DirectionsService(requireContext());

        loadSavedState();

        // Configurar mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Configurar ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        createLocationRequest();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    updateLocationOnMap(location);
                    // 🆕 ACTUALIZAR UBICACIÓN ACTUAL
                    currentDriverLocation = new LatLng(location.getLatitude(), location.getLongitude());
                }
            }
        };

        // 🆕 VERIFICAR ARGUMENTOS DE SERVICIO
        checkServiceArguments();

        checkLocationPermission();

        Log.d(TAG, "✅ DriverMapFragment inicializado completamente");
    }

    // 🆕 VERIFICAR ARGUMENTOS DE SERVICIO
    private void checkServiceArguments() {
        Bundle args = getArguments();
        if (args != null) {
            activeServiceDestination = args.getString("destination_address");
            activeServiceClientName = args.getString("client_name");
            activeServiceHotelName = args.getString("destination_name");
            activeServiceClientPhone = args.getString("client_phone");
            String serviceType = args.getString("service_type");

            Log.d(TAG, "🔍 ARGUMENTOS RECIBIDOS:");
            Log.d(TAG, "destination_address: " + activeServiceDestination);
            Log.d(TAG, "client_name: " + activeServiceClientName);
            Log.d(TAG, "destination_name: " + activeServiceHotelName);
            Log.d(TAG, "service_type: " + serviceType);

            if (activeServiceDestination != null && serviceType != null) {
                Log.d(TAG, "🎯 Servicio activo detectado: " + activeServiceHotelName);
                showActiveService();

                if (mMap != null) {
                    geocodeAndShowRoute();
                }
            } else {
                Log.w(TAG, "⚠️ No hay argumentos de servicio o están incompletos");
                hideActiveService(); // ✅ AGREGAR ESTA LÍNEA
            }
        } else {
            Log.w(TAG, "⚠️ No hay argumentos en el fragment");
            hideActiveService(); // ✅ AGREGAR ESTA LÍNEA
        }
    }
    private void hideActiveService() {
        if (cardServicioActivo != null) {
            cardServicioActivo.setVisibility(View.GONE);
            Log.d(TAG, "🫥 Card de servicio activo ocultada");
        }

        // Limpiar variables de servicio
        activeServiceDestination = null;
        activeServiceClientName = null;
        activeServiceHotelName = null;
        activeServiceClientPhone = null;

        // Limpiar ruta del mapa
        clearServiceFromMap();
    }

    // 🆕 MOSTRAR SERVICIO ACTIVO
    private void showActiveService() {
        if (cardServicioActivo != null) {
            cardServicioActivo.setVisibility(View.VISIBLE);

            if (tvClienteName != null && activeServiceClientName != null) {
                tvClienteName.setText("🚕 " + activeServiceClientName +
                        (activeServiceHotelName != null ? " - " + activeServiceHotelName : ""));
            }

            if (tvHotelName != null && activeServiceHotelName != null) {
                tvHotelName.setText("🏨 " + activeServiceHotelName);
            }

            if (tvDestinationAddress != null && activeServiceDestination != null) {
                tvDestinationAddress.setText("📍 " + activeServiceDestination);
            }

            if (tvServiceStatus != null) {
                tvServiceStatus.setText("● EN CURSO");
            }

            if (tvDistanceTime != null) {
                tvDistanceTime.setText("📏 Calculando ruta...");
            }

            // Empezar colapsada
            isCardExpanded = false;
            contenidoExpandible.setVisibility(View.GONE);
            if (iconExpandCollapse != null) {
                iconExpandCollapse.setRotation(0);
            }

            Log.d(TAG, "✅ Card desplegable mostrada (colapsada)");
        }
    }

    // 🆕 GEOCODIFICAR Y MOSTRAR RUTA
    private void geocodeAndShowRoute() {
        if (directionsService == null || activeServiceDestination == null) {
            Log.w(TAG, "No se puede geocodificar: servicio o destino nulo");
            return;
        }

        Log.d(TAG, "🔍 Geocodificando destino: " + activeServiceDestination);

        directionsService.geocodeAddress(activeServiceDestination, new DirectionsService.GeocodeCallback() {
            @Override
            public void onSuccess(LatLng destinationLocation) {
                Log.d(TAG, "✅ Destino geocodificado: " + destinationLocation);
                showDestinationMarker(destinationLocation);

                if (currentDriverLocation != null) {
                    calculateAndShowRoute(currentDriverLocation, destinationLocation);
                } else {
                    // Usar ubicación por defecto (Lima centro) hasta obtener ubicación real
                    LatLng defaultLocation = new LatLng(-12.0464, -77.0428);
                    calculateAndShowRoute(defaultLocation, destinationLocation);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error geocodificando: " + error);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error encontrando ubicación: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // 🆕 MOSTRAR MARCADOR DE DESTINO
    private void showDestinationMarker(LatLng location) {
        if (mMap != null) {
            if (destinationMarker != null) {
                destinationMarker.remove();
            }

            destinationMarker = mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(activeServiceHotelName != null ? activeServiceHotelName : "Destino")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            Log.d(TAG, "📍 Marcador de destino agregado");
        }
    }

    // 🆕 CALCULAR Y MOSTRAR RUTA
    private void calculateAndShowRoute(LatLng origin, LatLng destination) {
        Log.d(TAG, "🛣️ Calculando ruta desde " + origin + " hasta " + destination);

        directionsService.getDirections(origin, destination, new DirectionsService.DirectionsCallback() {
            @Override
            public void onSuccess(List<LatLng> route, String distance, String duration) {
                Log.d(TAG, "✅ Ruta calculada: " + distance + ", " + duration);

                // Mostrar ruta en el mapa
                showRouteOnMap(route);

                // Actualizar información de distancia/tiempo en la card
                updateDistanceTime(distance, duration);

                // Ajustar zoom para mostrar toda la ruta
                zoomToFitRoute(origin, destination);

                // Mensaje de éxito
                if (getContext() != null) {
                    Toast.makeText(getContext(),
                            "✅ Ruta calculada: " + distance + " en " + duration,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error calculando ruta: " + error);
                if (getContext() != null) {
                    Toast.makeText(getContext(),
                            "❌ Error calculando ruta: " + error,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // 🆕 MOSTRAR RUTA EN EL MAPA
    private void showRouteOnMap(List<LatLng> route) {
        if (mMap != null && route != null && !route.isEmpty()) {
            // Remover ruta anterior
            if (currentRoute != null) {
                currentRoute.remove();
            }

            // ✅ RUTA MÁS VISIBLE Y ATRACTIVA:
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(route)
                    .width(12) // ✅ Más gruesa
                    .color(Color.parseColor("#2196F3")) // ✅ Azul más vibrante
                    .geodesic(true)
                    .pattern(null); // ✅ Línea sólida

            currentRoute = mMap.addPolyline(polylineOptions);

            Log.d(TAG, "🗺️ Ruta dibujada en el mapa con " + route.size() + " puntos (línea azul gruesa)");

            // ✅ AGREGAR ANIMACIÓN SUTIL AL MAPA:
            if (getContext() != null) {
                // Pequeña vibración para feedback táctil
                android.os.Vibrator vibrator = (android.os.Vibrator) getContext().getSystemService(android.content.Context.VIBRATOR_SERVICE);
                if (vibrator != null) {
                    vibrator.vibrate(100); // 100ms de vibración
                }
            }
        }
    }

    // 🆕 ACTUALIZAR DISTANCIA Y TIEMPO
    private void updateDistanceTime(String distance, String duration) {
        if (tvDistanceTime != null) {
            tvDistanceTime.setText("📏 " + distance + " • ⏱️ " + duration);
        }
    }

    // 🆕 AJUSTAR ZOOM PARA MOSTRAR LA RUTA COMPLETA
    private void zoomToFitRoute(LatLng origin, LatLng destination) {
        if (mMap != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(origin);
            builder.include(destination);

            LatLngBounds bounds = builder.build();
            int padding = 150; // Padding en píxeles

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        }
    }

    // 🆕 INICIAR NAVEGACIÓN EXTERNA
    private void startExternalNavigation() {
        if (activeServiceDestination != null) {
            try {
                String uri = "google.navigation:q=" + activeServiceDestination.replace(" ", "+");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");

                if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                    startActivity(intent);
                    Toast.makeText(getContext(), "🧭 Navegación iniciada en Google Maps", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Google Maps no está instalado", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error abriendo navegación externa: " + e.getMessage());
                Toast.makeText(getContext(), "Error abriendo navegación", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void mostrarRutaEnMapa() {
        if (activeServiceDestination != null) {
            Log.d(TAG, "🛣️ Mostrando ruta completa en el mapa...");

            // Colapsar la card después de presionar el botón
            if (isCardExpanded) {
                toggleCardExpansion();
            }

            // Geocodificar destino si no tenemos la ubicación
            directionsService.geocodeAddress(activeServiceDestination, new DirectionsService.GeocodeCallback() {
                @Override
                public void onSuccess(LatLng destinationLocation) {
                    Log.d(TAG, "✅ Destino geocodificado: " + destinationLocation);

                    // Si tenemos ubicación actual, calcular ruta
                    if (currentDriverLocation != null) {
                        calculateAndShowRoute(currentDriverLocation, destinationLocation);
                    } else {
                        // Usar ubicación por defecto de Lima centro
                        LatLng limaCenter = new LatLng(-12.0464, -77.0428);
                        calculateAndShowRoute(limaCenter, destinationLocation);
                    }

                    // Mostrar mensaje al usuario
                    Toast.makeText(getContext(),
                            "🛣️ Ruta mostrada en el mapa\n📍 Sigue la línea azul",
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error geocodificando: " + error);
                    Toast.makeText(getContext(),
                            "❌ Error mostrando ruta: " + error,
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.w(TAG, "⚠️ No hay destino configurado");
            Toast.makeText(getContext(),
                    "⚠️ Destino no disponible",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private void toggleCardExpansion() {
        if (isCardExpanded) {
            // Colapsar
            contenidoExpandible.setVisibility(View.GONE);
            if (iconExpandCollapse != null) {
                iconExpandCollapse.setRotation(0);
            }
            isCardExpanded = false;
            Log.d(TAG, "📦 Card colapsada");
        } else {
            // Expandir
            contenidoExpandible.setVisibility(View.VISIBLE);
            if (iconExpandCollapse != null) {
                iconExpandCollapse.setRotation(180);
            }
            isCardExpanded = true;
            Log.d(TAG, "📂 Card expandida");
        }
    }


    /**
     * Abrir escáner de QR para finalizar servicio
     */
    private void openQRScanner() {
        if (activeServiceDestination != null && activeServiceClientName != null && activeServiceHotelName != null) {
            Log.d(TAG, "📱 Abriendo escáner QR para finalizar servicio...");

            // Crear fragment de finalización con QR
            ServiceCompletionFragment completionFragment = ServiceCompletionFragment.newInstance(
                    "temp_reservation_id", // En una implementación completa, esto sería el ID real
                    activeServiceClientName,
                    activeServiceHotelName
            );

            // Navegar al escáner
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, completionFragment)
                        .addToBackStack("qr_scanner")
                        .commit();

                Log.d(TAG, "✅ Navegación al escáner QR completada");
            }
        } else {
            Log.w(TAG, "⚠️ No hay servicio activo para finalizar");
            Toast.makeText(getContext(),
                    "⚠️ No hay servicio activo para finalizar",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // 🆕 COMPLETAR SERVICIO
    private void completeService() {
        Toast.makeText(getContext(), "✅ Servicio completado exitosamente", Toast.LENGTH_SHORT).show();

        // Limpiar mapa
        clearServiceFromMap();

        // Ocultar card de servicio activo
        if (cardServicioActivo != null) {
            cardServicioActivo.setVisibility(View.GONE);
        }

        // Limpiar variables de servicio
        activeServiceDestination = null;
        activeServiceClientName = null;
        activeServiceHotelName = null;
        activeServiceClientPhone = null;

        Log.d(TAG, "🧹 Servicio completado y limpiado");
    }

    // 🆕 LIMPIAR SERVICIO DEL MAPA
    private void clearServiceFromMap() {
        if (mMap != null) {
            if (destinationMarker != null) {
                destinationMarker.remove();
                destinationMarker = null;
            }

            if (currentRoute != null) {
                currentRoute.remove();
                currentRoute = null;
            }
        }
    }

    // MÉTODOS EXISTENTES (mantener exactamente como están)

    private void showNotificationTestMenu() {
        String[] options = {
                "🚗 Solicitud de viaje",
                "✅ Viaje completado",
                "💰 Resumen de ganancias",
                "📢 Notificación general"
        };

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Probar Notificaciones")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            if (mockNotificationService != null) {
                                mockNotificationService.sendRandomTripNotification();
                            }
                            break;
                        case 1:
                            addNotification("Viaje completado exitosamente");
                            break;
                        case 2:
                            if (mockNotificationService != null) {
                                mockNotificationService.sendTestEarningsNotification();
                            }
                            break;
                        case 3:
                            if (mockNotificationService != null) {
                                mockNotificationService.sendTestGeneralNotification();
                            }
                            break;
                    }
                    addNotification("Nueva notificación de prueba: " + options[which]);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showNotificationsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Notificaciones");

        if (notificationCount > 0) {
            String[] notifications = new String[notificationCount];
            for (int i = 0; i < notificationCount; i++) {
                notifications[i] = "Notificación #" + (i+1) + ": Nuevo viaje disponible cerca de tu ubicación.";
            }

            builder.setItems(notifications, null);
            builder.setPositiveButton("Marcar como leídas", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    notificationCount = 0;
                    updateNotificationBadge();
                    Toast.makeText(requireContext(), "Notificaciones marcadas como leídas", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            builder.setMessage("No tienes notificaciones nuevas.");
        }

        builder.setNegativeButton("Cerrar", null);
        builder.show();
    }

    public void addNotification(String message) {
        notificationCount++;
        updateNotificationBadge();

        if (notificationHelper != null) {
            if (message.contains("viaje") || message.contains("solicitud")) {
                notificationHelper.showTripRequestNotification(
                        "Hotel Gran Plaza",
                        "Carlos Mendoza",
                        "Av. La Marina 123, San Miguel",
                        85.50
                );
            } else {
                notificationHelper.showGeneralNotification("Nueva Notificación", message);
            }
        }

        if (isAdded() && getContext() != null) {
            Toast.makeText(requireContext(), "Nueva notificación: " + message, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNotificationBadge() {
        if (notificationCount > 0) {
            tvNotificationBadge.setVisibility(View.VISIBLE);
            tvNotificationBadge.setText(String.valueOf(notificationCount));
        } else {
            tvNotificationBadge.setVisibility(View.GONE);
        }

        if (notificationCount > 0) {
            ivNotifications.setImageResource(R.drawable.ic_notification_active);
        } else {
            ivNotifications.setImageResource(R.drawable.ic_notification);
        }
    }

    private void updateDriverStatus(boolean isAvailable) {
        if (isAvailable) {
            tvEstadoServicio.setText("En servicio");
            tvEstadoServicio.setTextColor(Color.parseColor("#4CAF50"));
            btnMyLocation.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));

            if (notificationHelper != null) {
                notificationHelper.showDriverStatusNotification(true);
                notificationHelper.showOnlineStatusNotification();
                mockNotificationService.startMockNotifications();
            }
        } else {
            tvEstadoServicio.setText("Fuera de servicio");
            tvEstadoServicio.setTextColor(Color.parseColor("#E53935"));
            btnMyLocation.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E53935")));

            if (notificationHelper != null) {
                notificationHelper.showDriverStatusNotification(false);
                notificationHelper.hideOnlineStatusNotification();
                mockNotificationService.stopMockNotifications();
            }
        }

        if (preferenceManager != null) {
            preferenceManager.setDriverAvailable(isAvailable);
            preferenceManager.setDriverStatus(isAvailable ? "En servicio" : "Fuera de servicio");
        }
        // Notificar a otros fragments sobre el cambio de estado
        if (getActivity() instanceof DriverActivity) {
            DriverActivity activity = (DriverActivity) getActivity();

            // Si cambió a disponible, refrescar viajes
            if (isAvailable) {
                activity.refreshViajesFragment();
            }
        }
    }

    private void addNearbyHotels(LatLng driverLocation) {
        // Limpiar hoteles anteriores
        if (hotelMarkers != null) {
            for (Marker marker : hotelMarkers) {
                marker.remove();
            }
        }

        hotelMarkers = new ArrayList<>();

        // ✅ HOTELES REALES DE LIMA CON UBICACIONES ESTÁTICAS
        String[][] hotelesLima = {
                {"Country Club Lima Hotel", "Golf de San Isidro 1570, San Isidro", "-12.0942", "-77.0364"},
                {"JW Marriott Hotel Lima", "Malecón de la Reserva 615, Miraflores", "-12.1196", "-77.0434"},
                {"Belmond Miraflores Park", "Av. Malecón de la Reserva 1035, Miraflores", "-12.1214", "-77.0436"},
                {"Hotel Maury", "Jr. Ucayali 201, Cercado de Lima", "-12.0458", "-77.0311"},
                {"Westin Lima Hotel", "Calle Las Begonias 450, San Isidro", "-12.0968", "-77.0365"},
                {"Hotel B", "Av. Sáenz Peña 204, Barranco", "-12.1401", "-77.0203"},
                {"Swissôtel Lima", "Vía Central 150, Centro Empresarial Real, San Isidro", "-12.0891", "-77.0427"},
                {"AC Hotel Lima Miraflores", "Av. Malecón Balta 1298, Miraflores", "-12.1231", "-77.0319"},
                {"Casa Andina Premium Miraflores", "Av. La Paz 463, Miraflores", "-12.1178", "-77.0286"},
                {"Hilton Lima Miraflores", "Av. La Paz 1099, Miraflores", "-12.1205", "-77.0234"}
        };

        // Agregar cada hotel al mapa
        for (int i = 0; i < hotelesLima.length; i++) {
            String nombre = hotelesLima[i][0];
            String direccion = hotelesLima[i][1];
            double lat = Double.parseDouble(hotelesLima[i][2]);
            double lng = Double.parseDouble(hotelesLima[i][3]);

            LatLng hotelLocation = new LatLng(lat, lng);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(hotelLocation)
                    .title("🏨 " + nombre)
                    .snippet("📍 " + direccion)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hotelitos_20));

            Marker hotelMarker = mMap.addMarker(markerOptions);
            hotelMarkers.add(hotelMarker);

            Log.d(TAG, "🏨 Hotel agregado: " + nombre + " en " + hotelLocation);
        }

        Log.d(TAG, "✅ " + hotelesLima.length + " hoteles reales de Lima agregados al mapa");
    }

    private void loadSavedState() {
        boolean isAvailable = preferenceManager.isDriverAvailable();
        switchDisponible.setChecked(isAvailable);
        updateDriverStatus(isAvailable);

        int savedNotificationCount = preferenceManager.getNotificationCount();
        notificationCount = savedNotificationCount;
        updateNotificationBadge();

        loadRealDriverDataForMap();
    }

    private void loadRealDriverDataForMap() {
        if (getActivity() instanceof com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity) {
            com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity activity =
                    (com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity) getActivity();

            String userId = activity.getUserId();
            String userName = activity.getUserName();

            Log.d("DriverMapFragment", "=== CARGANDO DATOS REALES EN MAPA ===");
            Log.d("DriverMapFragment", "UserId: " + userId);
            Log.d("DriverMapFragment", "Name: " + userName);

            if (userName != null && !userName.isEmpty()) {
                tvDriverName.setText(userName);
            } else {
                tvDriverName.setText("Conductor");
            }

            if (userId != null && !userId.isEmpty()) {
                loadCompleteDriverDataFromFirebase(userId);
            } else {
                ivProfilePhoto.setImageResource(R.drawable.perfil);
            }
        } else {
            tvDriverName.setText("Conductor");
            ivProfilePhoto.setImageResource(R.drawable.perfil);
        }
    }

    private void loadCompleteDriverDataFromFirebase(String userId) {
        Log.d("DriverMapFragment", "🔄 Cargando datos completos desde Firebase para el mapa");

        com.example.proyecto_final_hoteleros.utils.FirebaseManager firebaseManager =
                com.example.proyecto_final_hoteleros.utils.FirebaseManager.getInstance();

        firebaseManager.getUserDataFromAnyCollection(userId, new com.example.proyecto_final_hoteleros.utils.FirebaseManager.UserCallback() {
            @Override
            public void onUserFound(com.example.proyecto_final_hoteleros.models.UserModel user) {
                Log.d("DriverMapFragment", "✅ Datos del usuario obtenidos para el mapa");

                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        tvDriverName.setText(user.getFullName());

                        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                            com.bumptech.glide.Glide.with(DriverMapFragment.this)
                                    .load(user.getPhotoUrl())
                                    .placeholder(R.drawable.perfil)
                                    .error(R.drawable.perfil)
                                    .circleCrop()
                                    .into(ivProfilePhoto);
                        } else {
                            ivProfilePhoto.setImageResource(R.drawable.perfil);
                        }

                        Log.d("DriverMapFragment", "✅ UI del mapa actualizada con datos reales");
                    });
                }
            }

            @Override
            public void onUserNotFound() {
                Log.w("DriverMapFragment", "⚠️ Usuario no encontrado para el mapa");
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        ivProfilePhoto.setImageResource(R.drawable.perfil);
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e("DriverMapFragment", "❌ Error cargando datos para el mapa: " + error);
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        ivProfilePhoto.setImageResource(R.drawable.perfil);
                    });
                }
            }
        });
    }

    private void saveCurrentState() {
        preferenceManager.setDriverAvailable(switchDisponible.isChecked());
        preferenceManager.setDriverStatus(tvEstadoServicio.getText().toString());
        preferenceManager.setNotificationCount(notificationCount);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "🗺️ Mapa listo, configurando...");

        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            requireContext(), R.raw.map_style));

            if (!success) {
                Log.e("MapsActivity", "Falló la aplicación del estilo del mapa");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivity", "No se puede encontrar el estilo del mapa. Error: ", e);
        }

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        if (hasLocationPermission) {
            enableMyLocation();
        }

        // 🆕 SI HAY SERVICIO ACTIVO, MOSTRAR RUTA
        if (activeServiceDestination != null) {
            geocodeAndShowRoute();
        }
    }

    private void enableMyLocation() {
        if (mMap == null) return;

        try {
            // ✅ HABILITAR EL PUNTO AZUL PREDETERMINADO DE GOOGLE MAPS
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            getCurrentLocation();
            startLocationUpdates();
        } catch (SecurityException e) {
            hasLocationPermission = false;
            requestLocationPermission();
        }
    }

    private void getCurrentLocation() {
        if (!hasLocationPermission) {
            requestLocationPermission();
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            updateLocationOnMap(location);
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            currentDriverLocation = currentLatLng; // 🆕 GUARDAR UBICACIÓN ACTUAL
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                            addNearbyHotels(currentLatLng);
                        } else {
                            Toast.makeText(requireContext(), "Obteniendo ubicación...", Toast.LENGTH_SHORT).show();
                            if (!isLocationUpdatesActive) {
                                startLocationUpdates();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error al obtener ubicación: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } catch (SecurityException e) {
            hasLocationPermission = false;
            requestLocationPermission();
        }
    }

    private void animateMarker(final Marker marker, final LatLng toPosition) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final LatLng startLatLng = marker.getPosition();
        final long duration = 500;
        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private void updateLocationOnMap(Location location) {
        if (mMap != null && location != null) {
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            // Guardar ubicación
            if (preferenceManager != null) {
                preferenceManager.saveDriverLocation(location.getLatitude(), location.getLongitude());
            }

            // ✅ ACTUALIZAR UBICACIÓN ACTUAL (para otros usos como rutas)
            currentDriverLocation = currentLatLng;

            // ✅ NO CREAR MARCADORES PERSONALIZADOS
            // El punto azul predeterminado de Google Maps ya muestra tu ubicación

            // Centrar cámara solo la primera vez
            if (shouldCenterCamera) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f));
                shouldCenterCamera = false;
            }
        }
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        if (!hasLocationPermission) {
            requestLocationPermission();
            return;
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
            isLocationUpdatesActive = true;
        } catch (SecurityException e) {
            hasLocationPermission = false;
            Toast.makeText(requireContext(), "Error al iniciar actualizaciones de ubicación: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            requestLocationPermission();
        }
    }

    private void stopLocationUpdates() {
        if (isLocationUpdatesActive) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            isLocationUpdatesActive = false;
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        } else {
            hasLocationPermission = true;
            if (mMap != null) {
                enableMyLocation();
            }
        }
    }

    // Solo agregar este método:
    private void openServiceCompletion() {
        if (activeServiceClientName != null && activeServiceHotelName != null) {
            Log.d(TAG, "🎯 Abriendo escáner de QR...");

            ServiceCompletionFragment completionFragment = ServiceCompletionFragment.newInstance(
                    "temp_reservation_id",
                    activeServiceClientName,
                    activeServiceHotelName
            );

            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, completionFragment)
                        .addToBackStack("qr_scan")
                        .commit();
            }
        } else {
            Toast.makeText(getContext(), "No hay servicio activo", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestLocationPermission() {
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasLocationPermission = true;
                if (mMap != null) {
                    enableMyLocation();
                }
                startLocationUpdates();
            } else {
                hasLocationPermission = false;
                Toast.makeText(requireContext(),
                        "Se necesitan permisos de ubicación para mostrar tu posición en el mapa",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMap != null && !isLocationUpdatesActive && hasLocationPermission) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();

        if (preferenceManager != null) {
            saveCurrentState();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();

        // 🆕 LIMPIAR RECURSOS
        if (currentRoute != null) {
            currentRoute.remove();
        }
        if (destinationMarker != null) {
            destinationMarker.remove();
        }

        Log.d(TAG, "🧹 DriverMapFragment destruido y recursos limpiados");
    }
}