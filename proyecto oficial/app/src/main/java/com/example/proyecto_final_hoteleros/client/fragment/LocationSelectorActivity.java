package com.example.proyecto_final_hoteleros.client.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adapters.LocationAdapter;
import com.example.proyecto_final_hoteleros.client.model.LocationItem;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationSelectorActivity extends AppCompatActivity implements LocationAdapter.OnLocationClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private RecyclerView rvLocations;
    private LocationAdapter adapter;
    private List<LocationItem> locations = new ArrayList<>();
    private EditText etSearchLocation;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_selector);

        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Configurar botón de retroceso
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Configurar RecyclerView
        rvLocations = findViewById(R.id.rvLocations);
        rvLocations.setLayoutManager(new LinearLayoutManager(this));

        // Añadir algunas ubicaciones populares iniciales
        locations.add(new LocationItem("Lima", "Ciudad capital"));
        locations.add(new LocationItem("Cusco", "Ciudad histórica"));
        locations.add(new LocationItem("Arequipa", "Ciudad blanca"));
        locations.add(new LocationItem("Trujillo", "Ciudad de la eterna primavera"));
        locations.add(new LocationItem("Ica", "Ciudad desértica"));

        adapter = new LocationAdapter(locations, this);
        rvLocations.setAdapter(adapter);

        // Configurar búsqueda
        etSearchLocation = findViewById(R.id.etSearchLocation);
        etSearchLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se necesita implementar
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No se necesita implementar
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterLocations(s.toString());
            }
        });

        // Configurar botón de "Usar ubicación actual"
        LinearLayout btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        btnCurrentLocation.setOnClickListener(v -> getCurrentLocation());
    }

    private void filterLocations(String query) {
        if (query.isEmpty()) {
            // Si la consulta está vacía, mostrar ubicaciones populares predefinidas
            List<LocationItem> defaultLocations = new ArrayList<>();
            defaultLocations.add(new LocationItem("Lima", "Ciudad capital"));
            defaultLocations.add(new LocationItem("Cusco", "Ciudad histórica"));
            defaultLocations.add(new LocationItem("Arequipa", "Ciudad blanca"));
            defaultLocations.add(new LocationItem("Trujillo", "Ciudad de la eterna primavera"));
            defaultLocations.add(new LocationItem("Ica", "Ciudad desértica"));
            adapter.updateLocations(defaultLocations);
        } else {
            // Buscar ubicaciones basadas en la consulta
            // En una implementación real, aquí se podría usar la API de Places de Google
            // Para este ejemplo, simplemente filtramos nuestras ubicaciones existentes
            List<LocationItem> filteredLocations = new ArrayList<>();

            // Simular búsqueda en tiempo real
            // Añadir algunas ubicaciones basadas en lo que el usuario escribió
            String queryLower = query.toLowerCase();

            // Conjunto básico de departamentos peruanos para filtrar
            String[] departamentos = {"Lima", "Arequipa", "Cusco", "Piura", "Loreto", "Puno",
                    "Tacna", "Tumbes", "Ica", "Junín", "Áncash", "Ayacucho",
                    "Cajamarca", "Huancavelica", "Lambayeque", "La Libertad","Pasco"};

            for (String departamento : departamentos) {
                if (departamento.toLowerCase().contains(queryLower)) {
                    filteredLocations.add(new LocationItem(departamento, "Departamento"));
                }
            }

            // Añadir algunos distritos de Lima como ejemplo
            String[] distritosLima = {"Miraflores", "San Isidro", "Barranco", "San Borja",
                    "La Molina", "Surco", "Chorrillos", "Jesús María",
                    "Pueblo Libre", "San Miguel", "Magdalena"};

            if ("lima".contains(queryLower)) {
                for (String distrito : distritosLima) {
                    filteredLocations.add(new LocationItem(distrito, "Distrito de Lima"));
                }
            } else {
                for (String distrito : distritosLima) {
                    if (distrito.toLowerCase().contains(queryLower)) {
                        filteredLocations.add(new LocationItem(distrito, "Distrito de Lima"));
                    }
                }
            }

            adapter.updateLocations(filteredLocations);
        }
    }

    private void getCurrentLocation() {
        // Verificar permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permisos si no están concedidos
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permisos ya concedidos, obtener ubicación
            requestLocation();
        }
    }

    private void requestLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Obtener dirección a partir de las coordenadas
                                getAddressFromLocation(location);
                            } else {
                                Toast.makeText(LocationSelectorActivity.this,
                                        "No se pudo obtener la ubicación. Intentando de nuevo...",
                                        Toast.LENGTH_SHORT).show();

                                // En el emulador a veces falla la primera vez, intentamos con una ubicación fija
                                if (isEmulator()) {
                                    // Coordenadas de Lima como fallback para el emulador
                                    Location limaLocation = new Location("");
                                    limaLocation.setLatitude(-12.046374);
                                    limaLocation.setLongitude(-77.042793);
                                    getAddressFromLocation(limaLocation);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(this, e -> {
                        Toast.makeText(LocationSelectorActivity.this,
                                "Error al obtener ubicación: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isEmulator() {
        return android.os.Build.PRODUCT.contains("sdk") ||
                android.os.Build.MODEL.contains("Emulator") ||
                android.os.Build.MODEL.contains("Android SDK");
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // Obtener información de localidad más específica posible
                String locality = address.getLocality(); // Ciudad/distrito
                String subAdminArea = address.getSubAdminArea(); // Provincia
                String adminArea = address.getAdminArea(); // Departamento
                String country = address.getCountryName(); // País

                StringBuilder locationName = new StringBuilder();

                // Construir el nombre de la ubicación con los detalles disponibles
                if (locality != null && !locality.isEmpty()) {
                    locationName.append(locality);
                }

                if (subAdminArea != null && !subAdminArea.isEmpty() &&
                        (locality == null || !locality.equals(subAdminArea))) {
                    if (locationName.length() > 0) locationName.append(", ");
                    locationName.append(subAdminArea);
                }

                if (adminArea != null && !adminArea.isEmpty()) {
                    if (locationName.length() > 0) locationName.append(", ");
                    locationName.append(adminArea);
                }

                String locationText = locationName.toString();

                // Devolver la ubicación seleccionada
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_location", locationText);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "No se pudo determinar la dirección", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error al obtener dirección: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, obtener ubicación
                requestLocation();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLocationClick(LocationItem location) {
        // Devolver la ubicación seleccionada
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_location", location.getName());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}