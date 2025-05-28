package com.example.proyecto_final_hoteleros.taxista.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.R;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.LinearInterpolator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;


public class DriverMapFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final double SERVICE_RADIUS_METERS = 5000;
    private Marker driverMarker; // Declarar como variable de clase
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private boolean shouldCenterCamera = true;
    private CardView cardEstadoTaxista;
    private TextView tvEstadoServicio;
    private Switch switchDisponible;
    private FloatingActionButton btnMyLocation;
    private boolean isLocationUpdatesActive = false;
    private boolean hasLocationPermission = false;
    private FloatingActionButton btnZoomIn;
    private FloatingActionButton btnZoomOut;
    // Agregar la lista de marcadores de hoteles
    private List<Marker> hotelMarkers;
    private CircleImageView ivProfilePhoto;
    private TextView tvDriverName;
    private ImageView ivNotifications;
    private int notificationCount = 0;
    private TextView tvNotificationBadge;

    public DriverMapFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_map, container, false);

        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        tvDriverName = view.findViewById(R.id.tvDriverName);
        ivNotifications = view.findViewById(R.id.ivNotifications);

        // Inicializar vistas
        cardEstadoTaxista = view.findViewById(R.id.cardEstadoTaxista);
        tvEstadoServicio = view.findViewById(R.id.tvEstadoServicio);
        switchDisponible = view.findViewById(R.id.switchDisponible);
        btnMyLocation = view.findViewById(R.id.btnMyLocation);

        btnZoomIn = view.findViewById(R.id.btnZoomIn);
        btnZoomOut = view.findViewById(R.id.btnZoomOut);

        tvNotificationBadge = view.findViewById(R.id.tvNotificationBadge);
        notificationCount = 3;
        updateNotificationBadge();

        // Configurar listeners
        switchDisponible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateDriverStatus(isChecked);
            }
        });
        // Configurar listener para notificaciones
        ivNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mostrar panel de notificaciones o marcar como leídas
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

        FloatingActionButton btnTestNotification = view.findViewById(R.id.btnTestNotification);
        btnTestNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNotification("Prueba de notificación");
            }
        });

        return view;
    }
    private void showNotificationsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Notificaciones");

        if (notificationCount > 0) {
            // Crear una lista de notificaciones de ejemplo
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

        // Mostrar una notificación Toast al usuario
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

        // También actualizamos el ícono si es necesario
        if (notificationCount > 0) {
            ivNotifications.setImageResource(R.drawable.ic_notification_active);
        } else {
            ivNotifications.setImageResource(R.drawable.ic_notification);
        }
    }

    private void updateDriverStatus(boolean isAvailable) {
        if (isAvailable) {
            tvEstadoServicio.setText("En servicio");
            tvEstadoServicio.setTextColor(Color.parseColor("#4CAF50")); // Verde
            btnMyLocation.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));

            // Si está disponible, asegurarse de que las actualizaciones de ubicación están activas
        } else {
            tvEstadoServicio.setText("Fuera de servicio");
            tvEstadoServicio.setTextColor(Color.parseColor("#E53935")); // Rojo
            btnMyLocation.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E53935")));
        }
    }
    private void addNearbyHotels(LatLng driverLocation) {
        // En una implementación real, estos datos vendrían de tu base de datos o API
        // Aquí los simularemos con ubicaciones aleatorias cercanas

        // Limpiar marcadores de hoteles anteriores si existen
        if (hotelMarkers != null) {
            for (Marker marker : hotelMarkers) {
                marker.remove();
            }
        }

        hotelMarkers = new ArrayList<>();
        Random random = new Random();

        // Crear algunos hoteles cercanos aleatorios
        for (int i = 0; i < 5; i++) {
            // Generar ubicación aleatoria dentro del radio de servicio
            double r = SERVICE_RADIUS_METERS * 0.8 * Math.sqrt(random.nextDouble());
            double theta = random.nextDouble() * 2 * Math.PI;

            double lat = driverLocation.latitude + r * Math.cos(theta) / 111000;
            double lng = driverLocation.longitude + r * Math.sin(theta) / (111000 * Math.cos(driverLocation.latitude * Math.PI/180));

            LatLng hotelLocation = new LatLng(lat, lng);

            // Crear marcador para el hotel
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(hotelLocation)
                    .title("Hotel " + (i+1))
                    .snippet("Hotel asociado a la app")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hotelitos_20));

            Marker hotelMarker = mMap.addMarker(markerOptions);
            hotelMarkers.add(hotelMarker);
        }
    }




    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Inicializar el proveedor de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Crear solicitud de ubicación
        createLocationRequest();

        // Configurar callback de ubicación
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocationOnMap(location);
                }
            }
        };

        // Verificar permisos de ubicación
        checkLocationPermission();
    }

    // Dentro de onMapReady en DriverMapFragment
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Personalizar el estilo del mapa
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

        // Resto de tu código...
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        // Si ya tenemos permiso, configurar el mapa para mostrar la ubicación
        if (hasLocationPermission) {
            enableMyLocation();
        }
    }

    private void enableMyLocation() {
        if (mMap == null) {
            return;
        }

        try {
            // Habilitar el botón de "Mi ubicación" del mapa
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false); // Usamos nuestro botón personalizado

            // Obtener ubicación inicial
            getCurrentLocation();

            // Iniciar actualizaciones de ubicación
            startLocationUpdates();
        } catch (SecurityException e) {
            // Este bloque no debería ejecutarse si hasLocationPermission es true,
            // pero lo añadimos por seguridad
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
                            // Mover cámara a la ubicación actual con zoom adecuado
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                            addNearbyHotels(currentLatLng);
                        } else {
                            // Si no hay última ubicación conocida, forzar actualizaciones para obtenerla
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
        final long duration = 500; // duración en milisegundos
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
                    // Reprogramar la animación
                    handler.postDelayed(this, 16); // 16ms = ~60fps
                }
            }
        });
    }


    private void updateLocationOnMap(Location location) {
        if (mMap != null && location != null) {
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            // Actualizar o crear el marcador del taxista
            if (driverMarker == null) {
                try {
                    // Usar un icono por defecto en caso de error
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(currentLatLng)
                            .title("Mi ubicación");

                    // Intentar cargar el icono personalizado
                    try {
                        // Asegúrate de que este recurso exista y sea un bitmap válido
                        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_hotelitos_20);
                        markerOptions.icon(icon);
                    } catch (Exception e) {
                        // Si falla, usar el icono por defecto
                        Log.e("DriverMapFragment", "Error al cargar icono personalizado: " + e.getMessage());
                    }

                    driverMarker = mMap.addMarker(markerOptions);
                } catch (Exception e) {
                    Log.e("DriverMapFragment", "Error al añadir marcador: " + e.getMessage());
                }
            } else {
                // Solo actualizar la posición del marcador existente
                driverMarker.setPosition(currentLatLng);

                // Si tienes la dirección del taxista, puedes rotar el ícono
                if (location.hasBearing()) {
                    driverMarker.setRotation(location.getBearing());
                }
            }

            // Centrar el mapa en la ubicación actual si es necesario
            if (shouldCenterCamera) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f));
                shouldCenterCamera = false;
            }
        }
    }

    // Agregar una variable de clase para controlar cuándo centrar la cámara


// Modificar el método del botón para activar el centrado


    private void updateDriverLocationInDatabase(LatLng location) {
        // Aquí implementarías el código para actualizar la ubicación en tu base de datos
        // Por ejemplo, si usas Firebase:
        /*
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference()
                .child("driversAvailable").child(driverId);

        HashMap<String, Object> driverMap = new HashMap<>();
        driverMap.put("latitude", location.latitude);
        driverMap.put("longitude", location.longitude);

        driverLocationRef.updateChildren(driverMap);
        */

        // Como no has compartido tu implementación de base de datos,
        // este método queda como un placeholder para que implementes tu lógica específica
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 segundos
        locationRequest.setFastestInterval(5000); // 5 segundos
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
                    Looper.getMainLooper() // Usar el Looper principal para recibir callbacks en el hilo principal
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

            // Solicitar permisos
            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        } else {
            hasLocationPermission = true;
            // Si el mapa ya está listo, habilitar la ubicación
            if (mMap != null) {
                enableMyLocation();
            }
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
                // Si el mapa ya está listo, habilitar la ubicación
                if (mMap != null) {
                    enableMyLocation();
                }

                // Iniciar actualizaciones de ubicación si es necesario
                startLocationUpdates();
            } else {
                hasLocationPermission = false;
                // El usuario ha rechazado el permiso, explicar por qué se necesita
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Asegurarse de que se detienen las actualizaciones de ubicación
        stopLocationUpdates();
    }
}