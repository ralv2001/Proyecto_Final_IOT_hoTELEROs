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

import com.bumptech.glide.Glide;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.taxista.model.DriverProfile;
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
import com.google.android.gms.maps.model.MapStyleOptions;
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

    private Marker driverMarker;
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
    private List<Marker> hotelMarkers;
    private CircleImageView ivProfilePhoto;
    private TextView tvDriverName;
    private ImageView ivNotifications;
    private int notificationCount = 0;
    private TextView tvNotificationBadge;
    private DriverPreferenceManager preferenceManager;
    private NotificationHelper notificationHelper;
    private MockNotificationService mockNotificationService;

    public DriverMapFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.taxi_fragment_driver_map, container, false);

        // Inicializar vistas
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

        // Configurar listeners
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

        FloatingActionButton btnTestNotification = view.findViewById(R.id.btnTestNotification);
        btnTestNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mockNotificationService != null) {
                    showNotificationTestMenu();
                }
            }
        });

        return view;
    }

    // MÉTODO MOVIDO FUERA DE onCreateView() - ESTA ES LA CORRECCIÓN PRINCIPAL
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
                        case 0: // Solicitud de viaje
                            if (mockNotificationService != null) {
                                mockNotificationService.sendRandomTripNotification();
                            }
                            break;
                        case 1: // Viaje completado
                            addNotification("Viaje completado exitosamente");
                            break;
                        case 2: // Ganancias
                            if (mockNotificationService != null) {
                                mockNotificationService.sendTestEarningsNotification();
                            }
                            break;
                        case 3: // General
                            if (mockNotificationService != null) {
                                mockNotificationService.sendTestGeneralNotification();
                            }
                            break;
                    }

                    // Agregar notificación local también
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
    }

    private void addNearbyHotels(LatLng driverLocation) {
        if (hotelMarkers != null) {
            for (Marker marker : hotelMarkers) {
                marker.remove();
            }
        }

        hotelMarkers = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 5; i++) {
            double r = SERVICE_RADIUS_METERS * 0.8 * Math.sqrt(random.nextDouble());
            double theta = random.nextDouble() * 2 * Math.PI;

            double lat = driverLocation.latitude + r * Math.cos(theta) / 111000;
            double lng = driverLocation.longitude + r * Math.sin(theta) / (111000 * Math.cos(driverLocation.latitude * Math.PI/180));

            LatLng hotelLocation = new LatLng(lat, lng);

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

        preferenceManager = new DriverPreferenceManager(requireContext());
        notificationHelper = new NotificationHelper(requireContext());
        mockNotificationService = new MockNotificationService(notificationHelper, preferenceManager);

        loadSavedState();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        createLocationRequest();

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

        checkLocationPermission();
    }

    private void loadSavedState() {
        boolean isAvailable = preferenceManager.isDriverAvailable();
        switchDisponible.setChecked(isAvailable);
        updateDriverStatus(isAvailable);

        int savedNotificationCount = preferenceManager.getNotificationCount();
        notificationCount = savedNotificationCount;
        updateNotificationBadge();

        // 🔥 CARGAR DATOS REALES DEL USUARIO LOGUEADO
        loadRealDriverDataForMap();
    }

    // 🔥 NUEVO MÉTODO PARA CARGAR DATOS REALES EN EL MAPA
    private void loadRealDriverDataForMap() {
        if (getActivity() instanceof com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity) {
            com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity activity =
                    (com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity) getActivity();

            String userId = activity.getUserId();
            String userName = activity.getUserName();

            Log.d("DriverMapFragment", "=== CARGANDO DATOS REALES EN MAPA ===");
            Log.d("DriverMapFragment", "UserId: " + userId);
            Log.d("DriverMapFragment", "Name: " + userName);

            // 🔥 SI TENEMOS DATOS BÁSICOS, MOSTRARLOS INMEDIATAMENTE
            if (userName != null && !userName.isEmpty()) {
                tvDriverName.setText(userName);
            } else {
                tvDriverName.setText("Conductor");
            }

            // 🔥 CARGAR DATOS COMPLETOS DESDE FIREBASE
            if (userId != null && !userId.isEmpty()) {
                loadCompleteDriverDataFromFirebase(userId);
            } else {
                // Si no hay userId, usar datos por defecto
                ivProfilePhoto.setImageResource(R.drawable.perfil);
            }
        } else {
            // Fallback si no podemos obtener datos del Activity
            tvDriverName.setText("Conductor");
            ivProfilePhoto.setImageResource(R.drawable.perfil);
        }
    }

    // 🔥 CARGAR DATOS COMPLETOS DESDE FIREBASE PARA EL MAPA
    private void loadCompleteDriverDataFromFirebase(String userId) {
        Log.d("DriverMapFragment", "🔄 Cargando datos completos desde Firebase para el mapa");

        com.example.proyecto_final_hoteleros.utils.FirebaseManager firebaseManager =
                com.example.proyecto_final_hoteleros.utils.FirebaseManager.getInstance();

        firebaseManager.getUserDataFromAnyCollection(userId, new com.example.proyecto_final_hoteleros.utils.FirebaseManager.UserCallback() {
            @Override
            public void onUserFound(com.example.proyecto_final_hoteleros.models.UserModel user) {
                Log.d("DriverMapFragment", "✅ Datos del usuario obtenidos para el mapa");
                Log.d("DriverMapFragment", "Nombre: " + user.getFullName());
                Log.d("DriverMapFragment", "Foto: " + user.getPhotoUrl());

                // 🔥 ACTUALIZAR UI EN EL HILO PRINCIPAL
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        // Actualizar nombre
                        tvDriverName.setText(user.getFullName());

                        // Cargar foto de perfil con Glide
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
                // Mantener datos por defecto
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        ivProfilePhoto.setImageResource(R.drawable.perfil);
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e("DriverMapFragment", "❌ Error cargando datos para el mapa: " + error);
                // Mantener datos por defecto
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
    }

    private void enableMyLocation() {
        if (mMap == null) {
            return;
        }

        try {
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

            if (preferenceManager != null) {
                preferenceManager.saveDriverLocation(location.getLatitude(), location.getLongitude());
            }

            if (driverMarker == null) {
                try {
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(currentLatLng)
                            .title("Mi ubicación");

                    try {
                        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_hotelitos_20);
                        markerOptions.icon(icon);
                    } catch (Exception e) {
                        Log.e("DriverMapFragment", "Error al cargar icono personalizado: " + e.getMessage());
                    }

                    driverMarker = mMap.addMarker(markerOptions);
                } catch (Exception e) {
                    Log.e("DriverMapFragment", "Error al añadir marcador: " + e.getMessage());
                }
            } else {
                driverMarker.setPosition(currentLatLng);

                if (location.hasBearing()) {
                    driverMarker.setRotation(location.getBearing());
                }
            }

            if (shouldCenterCamera) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f));
                shouldCenterCamera = false;
            }
        }
    }

    private void updateDriverLocationInDatabase(LatLng location) {
        // Implementar actualización en base de datos
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
    }
}