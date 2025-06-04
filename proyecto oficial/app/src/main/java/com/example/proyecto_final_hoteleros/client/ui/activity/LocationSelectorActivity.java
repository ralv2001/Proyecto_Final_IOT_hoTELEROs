package com.example.proyecto_final_hoteleros.client.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.adapters.LocationAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.LocationItem;
import com.example.proyecto_final_hoteleros.client.utils.LocationPreferences;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocationSelectorActivity extends AppCompatActivity implements LocationAdapter.OnLocationClickListener {

    private static final String TAG = "LocationSelector";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1002;
    private RecyclerView rvLocations;
    private LocationAdapter adapter;
    private List<LocationItem> locations = new ArrayList<>();
    private EditText etSearchLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient placesClient;
    private TextView tvRecentLocationsTitle;
    private boolean isSearching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_activity_location_selector);

        Log.d(TAG, "Iniciando onCreate");

        // Inicializar vista del título de ubicaciones recientes
        tvRecentLocationsTitle = findViewById(R.id.tvRecentLocationsTitle);

        // 1. Inicializar Google Places API primero
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(this);

        // 2. Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 3. Configurar botones y elementos de UI que no dependen del adaptador
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());



        // 4. Configurar RecyclerView y inicializar el adaptador ANTES de usarlo
        rvLocations = findViewById(R.id.rvLocations);
        rvLocations.setLayoutManager(new LinearLayoutManager(this));

        // Crear lista de ubicaciones por defecto primero (departamentos del Perú)
        List<LocationItem> defaultLocations = new ArrayList<>();
        defaultLocations.add(new LocationItem("Lima", "Departamento", ""));
        defaultLocations.add(new LocationItem("Cusco", "Departamento", ""));
        defaultLocations.add(new LocationItem("Arequipa", "Departamento", ""));
        defaultLocations.add(new LocationItem("Trujillo", "Provincia, La Libertad", ""));
        defaultLocations.add(new LocationItem("Ica", "Departamento", ""));
        defaultLocations.add(new LocationItem("Piura", "Departamento", ""));
        defaultLocations.add(new LocationItem("Loreto", "Departamento", ""));
        defaultLocations.add(new LocationItem("Lambayeque", "Departamento", ""));
        defaultLocations.add(new LocationItem("Ayacucho", "Departamento", ""));
        defaultLocations.add(new LocationItem("Junín", "Departamento", ""));
        defaultLocations.add(new LocationItem("Cajamarca", "Departamento", ""));
        defaultLocations.add(new LocationItem("Puno", "Departamento", ""));
        defaultLocations.add(new LocationItem("Tacna", "Departamento", ""));

        // Inicializar el adaptador con la lista predeterminada
        Log.d(TAG, "Inicializando adaptador");
        adapter = new LocationAdapter(defaultLocations, this);
        rvLocations.setAdapter(adapter);
        Log.d(TAG, "Adaptador inicializado correctamente");

        // 5. AHORA que el adaptador ya está inicializado, puedes usar las ubicaciones recientes
        LocationPreferences locationPreferences = new LocationPreferences(this);
        List<LocationItem> recentLocations = locationPreferences.getRecentLocations();

        if (!recentLocations.isEmpty()) {
            Log.d(TAG, "Mostrando ubicaciones recientes: " + recentLocations.size());
            // Si hay ubicaciones recientes, mostrarlas - ahora el adaptador ya existe
            adapter.updateLocations(recentLocations);
            tvRecentLocationsTitle.setText("Ubicaciones recientes");
        } else {
            Log.d(TAG, "No hay ubicaciones recientes, mostrando predeterminadas");
            tvRecentLocationsTitle.setText("Departamentos y provincias");
        }

        // 6. Configurar búsqueda y el resto de elementos
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
                if (s.length() > 0) {
                    // Usar Google Places para autocompletado
                    isSearching = true;
                    tvRecentLocationsTitle.setText("Resultados de búsqueda");
                    getPlacePredictions(s.toString());
                } else {
                    // Mostrar ubicaciones recientes o predeterminadas
                    isSearching = false;
                    LocationPreferences prefs = new LocationPreferences(LocationSelectorActivity.this);
                    List<LocationItem> recent = prefs.getRecentLocations();

                    if (!recent.isEmpty()) {
                        tvRecentLocationsTitle.setText("Ubicaciones recientes");
                        adapter.updateLocations(recent);
                    } else {
                        tvRecentLocationsTitle.setText("Departamentos y provincias");
                        showDefaultLocations();
                    }
                }
            }
        });

        // 7. Configurar botón de "Usar ubicación actual"
        LinearLayout btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        // Añadir tabs para seleccionar entre diferentes vistas
        LinearLayout tabDestinations = findViewById(R.id.tabDestinations);
        LinearLayout tabRegions = findViewById(R.id.tabRegions);
        LinearLayout tabPopular = findViewById(R.id.tabPopular);
        btnCurrentLocation.setOnClickListener(v -> getCurrentLocation());
// Configurar los listeners de los tabs
        tabDestinations.setOnClickListener(v -> {
            // Mostrar ubicaciones recientes o predeterminadas si no hay
            LocationPreferences prefs = new LocationPreferences(this);
            List<LocationItem> recent = prefs.getRecentLocations();

            selectTab(tabDestinations);

            if (!recent.isEmpty()) {
                tvRecentLocationsTitle.setText("Ubicaciones recientes");
                adapter.updateLocations(recent);
            } else {
                tvRecentLocationsTitle.setText("Departamentos y provincias");
                showDefaultLocations();
            }
        });

        tabRegions.setOnClickListener(v -> {
            selectTab(tabRegions);
            tvRecentLocationsTitle.setText("Regiones por ubicación");
            showDefaultLocations();
        });

        tabPopular.setOnClickListener(v -> {
            selectTab(tabPopular);
            showPopularTouristDestinations();
        });

// Seleccionar el primer tab por defecto
        selectTab(tabDestinations);
        Log.d(TAG, "onCreate completado");
    }
    private void selectTab(View selectedTab) {
        // Reiniciar todos los tabs a estado no seleccionado
        findViewById(R.id.tabDestinations).setBackgroundResource(R.drawable.tab_unselected_bg);
        findViewById(R.id.tabRegions).setBackgroundResource(R.drawable.tab_unselected_bg);
        findViewById(R.id.tabPopular).setBackgroundResource(R.drawable.tab_unselected_bg);

        // Resaltar el tab seleccionado
        selectedTab.setBackgroundResource(R.drawable.tab_selected_bg);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.d(TAG, "Lugar seleccionado: " + place.getName());

                // Obtener más detalles sobre el lugar
                String address = place.getAddress();
                String description = "";

                // Determinar si es departamento, provincia o distrito
                if (address != null && !address.isEmpty()) {
                    if (address.contains(",")) {
                        String[] parts = address.split(",");
                        if (parts.length >= 3) {
                            description = "Distrito, " + parts[1].trim();
                        } else if (parts.length == 2) {
                            description = "Provincia, " + parts[1].trim();
                        } else {
                            description = "Departamento";
                        }
                    } else {
                        description = "Departamento";
                    }
                } else {
                    description = "Departamento";
                }

                // Crear un nuevo LocationItem
                LocationItem locationItem = new LocationItem(
                        place.getName(),
                        description,
                        place.getId()
                );

                // Guardar en recientes
                LocationPreferences locationPreferences = new LocationPreferences(this);
                locationPreferences.saveRecentLocation(locationItem);

                // Devolver resultado a la actividad anterior
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_location", place.getName());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.e(TAG, "Error en autocompletado: " + status.getStatusMessage());
                Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // El usuario canceló la operación
                Log.d(TAG, "Autocompletado cancelado por el usuario");
            }
        }
    }

    // Método para mostrar ubicaciones por defecto (departamentos del Perú) - con verificación de null
    private void showDefaultLocations() {
        if (adapter == null) {
            Log.w(TAG, "El adaptador es null en showDefaultLocations, inicializando");
            adapter = new LocationAdapter(new ArrayList<>(), this);
            if (rvLocations != null) {
                rvLocations.setAdapter(adapter);
            }
        }

        // Mostrar departamentos agrupados por región
        List<LocationItem> defaultLocations = new ArrayList<>();

        // Región Costa (ordenada de norte a sur)
        defaultLocations.add(new LocationItem("Piura", "Departamento, Costa Norte", ""));
        defaultLocations.add(new LocationItem("Lambayeque", "Departamento, Costa Norte", ""));
        defaultLocations.add(new LocationItem("La Libertad", "Departamento, Costa Norte", ""));
        defaultLocations.add(new LocationItem("Áncash", "Departamento, Costa Central", ""));
        defaultLocations.add(new LocationItem("Lima", "Departamento, Costa Central", ""));
        defaultLocations.add(new LocationItem("Ica", "Departamento, Costa Sur", ""));
        defaultLocations.add(new LocationItem("Arequipa", "Departamento, Costa Sur", ""));
        defaultLocations.add(new LocationItem("Tacna", "Departamento, Costa Sur", ""));

        // Región Sierra
        defaultLocations.add(new LocationItem("Cajamarca", "Departamento, Sierra Norte", ""));
        defaultLocations.add(new LocationItem("Junín", "Departamento, Sierra Central", ""));
        defaultLocations.add(new LocationItem("Huancavelica", "Departamento, Sierra Central", ""));
        defaultLocations.add(new LocationItem("Ayacucho", "Departamento, Sierra Central", ""));
        defaultLocations.add(new LocationItem("Cusco", "Departamento, Sierra Sur", ""));
        defaultLocations.add(new LocationItem("Puno", "Departamento, Sierra Sur", ""));

        // Región Selva
        defaultLocations.add(new LocationItem("Loreto", "Departamento, Selva", ""));
        defaultLocations.add(new LocationItem("Ucayali", "Departamento, Selva", ""));
        defaultLocations.add(new LocationItem("Madre de Dios", "Departamento, Selva", ""));

        Log.d(TAG, "Actualizando adaptador con ubicaciones predeterminadas");
        adapter.updateLocations(defaultLocations);
    }
    private void showPopularTouristDestinations() {
        if (adapter == null) return;

        List<LocationItem> popularDestinations = new ArrayList<>();

        // Destinos turísticos más populares del Perú
        popularDestinations.add(new LocationItem("Cusco", "Ciudad turística principal", ""));
        popularDestinations.add(new LocationItem("Machu Picchu", "Santuario histórico, Cusco", ""));
        popularDestinations.add(new LocationItem("Valle Sagrado", "Región turística, Cusco", ""));
        popularDestinations.add(new LocationItem("Lima", "Capital, Centro histórico", ""));
        popularDestinations.add(new LocationItem("Arequipa", "Ciudad Blanca, Centro histórico", ""));
        popularDestinations.add(new LocationItem("Puno", "Lago Titicaca", ""));
        popularDestinations.add(new LocationItem("Ica", "Huacachina, Oasis", ""));
        popularDestinations.add(new LocationItem("Máncora", "Playa, Piura", ""));
        popularDestinations.add(new LocationItem("Chachapoyas", "Kuelap, Amazonas", ""));
        popularDestinations.add(new LocationItem("Puerto Maldonado", "Selva, Madre de Dios", ""));

        tvRecentLocationsTitle.setText("Destinos turísticos populares");
        adapter.updateLocations(popularDestinations);
    }

    // Método para mostrar distritos de Lima (por ejemplo)
    private void showLimaDistricts() {
        if (adapter == null) return;

        List<LocationItem> limaDistricts = new ArrayList<>();
        limaDistricts.add(new LocationItem("Miraflores", "Distrito, Lima", ""));
        limaDistricts.add(new LocationItem("San Isidro", "Distrito, Lima", ""));
        limaDistricts.add(new LocationItem("Barranco", "Distrito, Lima", ""));
        limaDistricts.add(new LocationItem("San Borja", "Distrito, Lima", ""));
        limaDistricts.add(new LocationItem("Surco", "Distrito, Lima", ""));
        limaDistricts.add(new LocationItem("La Molina", "Distrito, Lima", ""));
        limaDistricts.add(new LocationItem("Jesús María", "Distrito, Lima", ""));
        limaDistricts.add(new LocationItem("Pueblo Libre", "Distrito, Lima", ""));
        limaDistricts.add(new LocationItem("Lince", "Distrito, Lima", ""));
        limaDistricts.add(new LocationItem("Magdalena", "Distrito, Lima", ""));
        limaDistricts.add(new LocationItem("San Miguel", "Distrito, Lima", ""));
        limaDistricts.add(new LocationItem("Chorrillos", "Distrito, Lima", ""));

        tvRecentLocationsTitle.setText("Distritos de Lima");
        adapter.updateLocations(limaDistricts);
    }

    private void getPlacePredictions(String query) {
        if (adapter == null) {
            Log.w(TAG, "El adaptador es null en getPlacePredictions, inicializando");
            adapter = new LocationAdapter(new ArrayList<>(), this);
            if (rvLocations != null) {
                rvLocations.setAdapter(adapter);
            }
        }

        Log.d(TAG, "Buscando predicciones para: " + query);

        // Actualizar título mientras se busca
        tvRecentLocationsTitle.setText("Buscando...");

        // Crear una solicitud de autocompletado con Google Places
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                // Usamos TypeFilter.REGIONS para obtener departamentos, provincias y distritos
                .setTypeFilter(TypeFilter.REGIONS)
                .setQuery(query)
                // Limitar a Perú
                .setCountries("PE")
                .build();

        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener((response) -> {
                    List<LocationItem> predictions = new ArrayList<>();

                    // Procesar y categorizar los resultados
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                        String name = prediction.getPrimaryText(null).toString();
                        String description = prediction.getSecondaryText(null).toString();
                        String placeId = prediction.getPlaceId();

                        // Analizar la descripción para determinar el tipo de lugar
                        String type;
                        if (description.equals("Perú")) {
                            // Si solo dice "Perú", es un departamento
                            type = "Departamento";
                        } else if (description.contains(", Perú") && !description.contains(", ")) {
                            // Si es "X, Perú", probablemente sea un departamento o ciudad principal
                            type = "Ciudad principal";
                        } else if (description.contains(", ")) {
                            // Si contiene comas, probablemente sea un distrito o localidad
                            String[] parts = description.split(", ");
                            if (parts.length >= 3) {
                                type = "Distrito";
                            } else {
                                type = "Localidad";
                            }
                        } else {
                            type = "Lugar";
                        }

                        predictions.add(new LocationItem(name, description, placeId));
                    }

                    // Actualizar la lista de ubicaciones
                    Log.d(TAG, "Predicciones obtenidas: " + predictions.size());

                    if (predictions.isEmpty()) {
                        tvRecentLocationsTitle.setText("No se encontraron resultados");
                        showLocalSearchResults(query);
                    } else {
                        tvRecentLocationsTitle.setText("Resultados de búsqueda");
                        adapter.updateLocations(predictions);
                    }
                })
                .addOnFailureListener((exception) -> {
                    Log.e(TAG, "Error al obtener predicciones: " + exception.getMessage());
                    Toast.makeText(this, "Error al obtener predicciones. Mostrando resultados locales.",
                            Toast.LENGTH_SHORT).show();
                    showLocalSearchResults(query);
                });
    }

    // Método para mostrar resultados de búsqueda local cuando Google Places falla
    private void showLocalSearchResults(String query) {
        String queryLower = query.toLowerCase();
        List<LocationItem> filteredLocations = new ArrayList<>();

        // Base de datos local de departamentos peruanos
        String[] departamentos = {"Lima", "Arequipa", "Cusco", "Piura", "Loreto", "Puno",
                "Tacna", "Tumbes", "Ica", "Junín", "Áncash", "Ayacucho",
                "Cajamarca", "Huancavelica", "Lambayeque", "La Libertad", "Pasco",
                "Amazonas", "Apurímac", "Huánuco", "Madre de Dios", "Moquegua",
                "San Martín", "Ucayali"};

        for (String departamento : departamentos) {
            if (departamento.toLowerCase().contains(queryLower)) {
                filteredLocations.add(new LocationItem(departamento, "Departamento", ""));
            }
        }

        // Distritos principales de Lima
        String[] distritosLima = {"Miraflores", "San Isidro", "Barranco", "San Borja",
                "La Molina", "Surco", "Chorrillos", "Jesús María", "Surquillo",
                "Pueblo Libre", "San Miguel", "Magdalena", "Lince", "Breña",
                "Lima (Cercado)", "Rímac", "La Victoria", "San Luis", "San Juan de Lurigancho",
                "San Juan de Miraflores", "Villa El Salvador", "Villa María del Triunfo",
                "Independencia", "Los Olivos", "San Martín de Porres", "Comas"};

        // Si la búsqueda parece estar relacionada con Lima, incluir distritos de Lima
        if ("lima".contains(queryLower) || queryLower.equals("l") || queryLower.equals("li")) {
            for (String distrito : distritosLima) {
                filteredLocations.add(new LocationItem(distrito, "Distrito, Lima", ""));
            }
        } else {
            // De lo contrario, buscar coincidencias en distritos
            for (String distrito : distritosLima) {
                if (distrito.toLowerCase().contains(queryLower)) {
                    filteredLocations.add(new LocationItem(distrito, "Distrito, Lima", ""));
                }
            }
        }

        // Algunas provincias importantes
        String[][] provincias = {
                {"Callao", "Lima"},
                {"Arequipa", "Arequipa"},
                {"Trujillo", "La Libertad"},
                {"Chiclayo", "Lambayeque"},
                {"Piura", "Piura"},
                {"Huancayo", "Junín"},
                {"Cusco", "Cusco"},
                {"Iquitos", "Loreto"},
                {"Chimbote", "Áncash"},
                {"Tacna", "Tacna"},
                {"Puno", "Puno"},
                {"Ayacucho", "Ayacucho"},
                {"Huánuco", "Huánuco"},
                {"Cajamarca", "Cajamarca"}
        };

        for (String[] provincia : provincias) {
            if (provincia[0].toLowerCase().contains(queryLower)) {
                filteredLocations.add(new LocationItem(provincia[0], "Provincia, " + provincia[1], ""));
            }
        }

        if (filteredLocations.isEmpty()) {
            tvRecentLocationsTitle.setText("No se encontraron resultados");
            // Mostrar mensaje de que no se encontraron resultados
            List<LocationItem> noResults = new ArrayList<>();
            noResults.add(new LocationItem("No se encontraron resultados", "Intenta con otra búsqueda", ""));
            adapter.updateLocations(noResults);
        } else {
            tvRecentLocationsTitle.setText("Resultados de búsqueda");
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            // Mostrar un indicador de carga
            View progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            Log.d(TAG, "Solicitando ubicación actual");

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            progressBar.setVisibility(View.GONE);

                            if (location != null) {
                                Log.d(TAG, "Ubicación obtenida: " + location.getLatitude() + ", " + location.getLongitude());
                                // Obtener dirección a partir de las coordenadas
                                getAddressFromLocation(location);
                            } else {
                                Log.w(TAG, "No se pudo obtener ubicación actual");
                                Toast.makeText(LocationSelectorActivity.this,
                                        "No se pudo obtener la ubicación. Intentando de nuevo...",
                                        Toast.LENGTH_SHORT).show();

                                // En el emulador a veces falla la primera vez, intentamos con una ubicación fija
                                if (isEmulator()) {
                                    Log.d(TAG, "Usando ubicación por defecto para emulador");
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
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Error al obtener ubicación: " + e.getMessage());
                        Toast.makeText(LocationSelectorActivity.this,
                                "Error al obtener ubicación: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Excepción al solicitar ubicación: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isEmulator() {
        return android.os.Build.PRODUCT.contains("sdk") ||
                android.os.Build.MODEL.contains("Emulator") ||
                android.os.Build.MODEL.contains("Android SDK");
    }

    private void getAddressFromLocation(Location location) {
        // Mostrar progress bar
        View progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        Log.d(TAG, "Obteniendo dirección para la ubicación: " + location.getLatitude() + ", " + location.getLongitude());

        // Construir la URL para la API de Geocoding de Google
        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());
        String apiKey = getString(R.string.google_maps_key);
        String url = "https://maps.googleapis.com/maps/api/geocode/json" +
                "?latlng=" + latitude + "," + longitude +
                "&key=" + apiKey +
                "&language=es"; // Para resultados en español

        // Usar un thread separado para la petición de red
        new Thread(() -> {
            try {
                URL requestUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                connection.setRequestMethod("GET");

                // Leer la respuesta
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parsear la respuesta JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray results = jsonResponse.getJSONArray("results");

                if (results.length() > 0) {
                    // Obtener el primer resultado
                    JSONObject result = results.getJSONObject(0);

                    // Obtener el nombre formateado de la ubicación
                    final String formattedAddress = result.getString("formatted_address");
                    Log.d(TAG, "Dirección formateada: " + formattedAddress);

                    // Obtener componentes individuales de la dirección
                    JSONArray addressComponents = result.getJSONArray("address_components");
                    String sublocality = ""; // Distrito
                    String locality = ""; // Ciudad/Localidad
                    String administrative_area_level_2 = ""; // Provincia
                    String administrative_area_level_1 = ""; // Departamento
                    String country = ""; // País

                    for (int i = 0; i < addressComponents.length(); i++) {
                        JSONObject component = addressComponents.getJSONObject(i);
                        JSONArray types = component.getJSONArray("types");

                        for (int j = 0; j < types.length(); j++) {
                            String type = types.getString(j);
                            switch (type) {
                                case "sublocality_level_1":
                                case "sublocality":
                                    sublocality = component.getString("long_name");
                                    break;
                                case "locality":
                                    locality = component.getString("long_name");
                                    break;
                                case "administrative_area_level_2":
                                    administrative_area_level_2 = component.getString("long_name");
                                    break;
                                case "administrative_area_level_1":
                                    administrative_area_level_1 = component.getString("long_name");
                                    break;
                                case "country":
                                    country = component.getString("long_name");
                                    break;
                            }
                        }
                    }

                    // Construir texto de ubicación más amigable
                    final StringBuilder locationBuilder = new StringBuilder();
                    String description = "";

                    // Priorizar distrito > localidad > provincia > departamento
                    if (!sublocality.isEmpty()) {
                        locationBuilder.append(sublocality);
                        description = "Distrito";
                        if (!locality.isEmpty() && !locality.equals(sublocality)) {
                            description += ", " + locality;
                        } else if (!administrative_area_level_1.isEmpty()) {
                            description += ", " + administrative_area_level_1;
                        }
                    } else if (!locality.isEmpty()) {
                        locationBuilder.append(locality);
                        description = "Ciudad";
                        if (!administrative_area_level_1.isEmpty()) {
                            description += ", " + administrative_area_level_1;
                        }
                    } else if (!administrative_area_level_2.isEmpty()) {
                        locationBuilder.append(administrative_area_level_2);
                        description = "Provincia";
                        if (!administrative_area_level_1.isEmpty()) {
                            description += ", " + administrative_area_level_1;
                        }
                    } else if (!administrative_area_level_1.isEmpty()) {
                        locationBuilder.append(administrative_area_level_1);
                        description = "Departamento";
                    }

                    final String locationText = locationBuilder.toString();
                    final String locationDescription = description;
                    Log.d(TAG, "Ubicación procesada: " + locationText + " (" + locationDescription + ")");

                    // Actualizar UI en el thread principal
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);

                        // Crear LocationItem para guardar en recientes
                        LocationItem locationItem = new LocationItem(
                                locationText,
                                locationDescription,
                                ""
                        );

                        // Guardar en recientes
                        LocationPreferences locationPreferences = new LocationPreferences(LocationSelectorActivity.this);
                        locationPreferences.saveRecentLocation(locationItem);

                        // Devolver resultado
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("selected_location", locationText);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    });
                } else {
                    Log.w(TAG, "No se obtuvieron resultados del geocoding");
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LocationSelectorActivity.this,
                                "No se pudo determinar la dirección",
                                Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error en geocoding: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LocationSelectorActivity.this,
                            "Error al obtener dirección: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, obtener ubicación
                Log.d(TAG, "Permiso de ubicación concedido");
                requestLocation();
            } else {
                Log.w(TAG, "Permiso de ubicación denegado");
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLocationClick(LocationItem location) {
        Log.d(TAG, "Ubicación seleccionada: " + location.getName());
        String placeId = location.getPlaceId();
        String type = location.getType();

        // Si seleccionamos Lima y no estamos en búsqueda, mostrar distritos
        if (location.getName().equals("Lima") && type.equals("Departamento") && !isSearching) {
            showLimaDistricts();
            return;
        }

        // Guardar en ubicaciones recientes
        LocationPreferences locationPreferences = new LocationPreferences(this);
        locationPreferences.saveRecentLocation(location);

        if (placeId != null && !placeId.isEmpty()) {
            // Si tenemos un placeId de Google Places, obtenemos los detalles
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.ADDRESS_COMPONENTS);

            com.google.android.libraries.places.api.net.FetchPlaceRequest request =
                    com.google.android.libraries.places.api.net.FetchPlaceRequest.builder(placeId, placeFields)
                            .build();

            placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                Place place = response.getPlace();
                String locationName = place.getName();
                Log.d(TAG, "Detalles de lugar obtenidos: " + locationName);

                // Devolver la ubicación seleccionada
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_location", locationName);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }).addOnFailureListener((exception) -> {
                Log.e(TAG, "Error al obtener detalles del lugar: " + exception.getMessage());
                // Usar el nombre directamente si falla la obtención de detalles
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_location", location.getName());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            });
        } else {
            // Para ubicaciones que no tienen placeId (como las predeterminadas)
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_location", location.getName());
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }
}