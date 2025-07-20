package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.example.proyecto_final_hoteleros.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LocationSelectorDialog extends DialogFragment {

    private static final String TAG = "LocationSelectorDialog";

    // Views
    private AutoCompleteTextView etLocationSearch;
    private MaterialButton btnCurrentLocation;
    private MaterialButton btnConfirmLocation;
    private MaterialCardView cardSelectedLocation;
    private TextView tvSelectedLocationName;
    private TextView tvSelectedLocationAddress;
    private LinearLayout layoutEmptyLocation;
    private View dialogView; // ✅ AGREGADO: Referencia a la vista principal

    // Google Places
    private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;

    // Callback
    private LocationSelectedListener listener;

    // Datos seleccionados
    private String selectedLocationName = "";
    private String selectedLocationAddress = "";
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private boolean hasValidLocation = false;

    // ✅ AGREGADO: Lista para autocompletado
    private List<AutocompletePrediction> currentPredictions = new ArrayList<>();
    private ArrayAdapter<String> autoCompleteAdapter;

    public interface LocationSelectedListener {
        void onLocationSelected(String locationName, String fullAddress, double latitude, double longitude);
    }

    public void setLocationSelectedListener(LocationSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        dialogView = getLayoutInflater().inflate(R.layout.admin_hotel_dialog_location_selector, null);

        initializeViews(dialogView);
        initializePlaces();
        setupClickListeners();
        setupAutoComplete();

        builder.setView(dialogView);
        return builder.create();
    }

    private void initializeViews(View view) {
        etLocationSearch = view.findViewById(R.id.etLocationSearch);
        btnCurrentLocation = view.findViewById(R.id.btnCurrentLocation);
        btnConfirmLocation = view.findViewById(R.id.btnConfirmLocation);
        cardSelectedLocation = view.findViewById(R.id.cardSelectedLocation);
        tvSelectedLocationName = view.findViewById(R.id.tvSelectedLocationName);
        tvSelectedLocationAddress = view.findViewById(R.id.tvSelectedLocationAddress);
        layoutEmptyLocation = view.findViewById(R.id.layoutEmptyLocation);
    }

    private void initializePlaces() {
        try {
            // Inicializar Google Places API
            if (!Places.isInitialized()) {
                Places.initialize(getContext(), getString(R.string.google_maps_key));
            }
            placesClient = Places.createClient(getContext());
            sessionToken = AutocompleteSessionToken.newInstance();

            Log.d(TAG, "✅ Google Places API inicializada");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error inicializando Google Places: " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        btnCurrentLocation.setOnClickListener(v -> getCurrentLocation());
        btnConfirmLocation.setOnClickListener(v -> confirmLocation());
        dialogView.findViewById(R.id.btnCancelLocation).setOnClickListener(v -> dismiss());
    }

    private void setupAutoComplete() {
        // ✅ IMPLEMENTACIÓN COMPLETA del AutoComplete

        // Crear adapter para el AutoCompleteTextView
        autoCompleteAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line);
        etLocationSearch.setAdapter(autoCompleteAdapter);

        // Configurar threshold para búsqueda
        etLocationSearch.setThreshold(3);

        // Listener para cuando se selecciona un item del dropdown
        etLocationSearch.setOnItemClickListener((parent, view, position, id) -> {
            if (position < currentPredictions.size()) {
                AutocompletePrediction prediction = currentPredictions.get(position);
                String placeId = prediction.getPlaceId();
                fetchPlaceDetails(placeId);

                Log.d(TAG, "🎯 Lugar seleccionado: " + prediction.getPrimaryText(null));
            }
        });

        // TextWatcher para búsqueda en tiempo real
        etLocationSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3) {
                    searchPlaces(s.toString());
                } else {
                    clearAutoCompleteResults();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchPlaces(String query) {
        if (placesClient == null) {
            Log.e(TAG, "❌ PlacesClient no inicializado");
            return;
        }

        // ✅ IMPLEMENTACIÓN COMPLETA de búsqueda con Google Places

        // Definir bounds para Perú
        LatLng southWest = new LatLng(-18.3479, -81.3867); // Esquina suroeste de Perú
        LatLng northEast = new LatLng(-0.0389, -68.6767);   // Esquina noreste de Perú
        RectangularBounds bounds = RectangularBounds.newInstance(southWest, northEast);

        // Crear request de autocompletado
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setLocationRestriction(bounds)
                .setCountries("PE") // Restringir a Perú
                .setTypeFilter(TypeFilter.ESTABLISHMENT) // Buscar establecimientos
                .setQuery(query)
                .setSessionToken(sessionToken)
                .build();

        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener((response) -> {
                    updateAutoCompleteResults(response.getAutocompletePredictions());
                    Log.d(TAG, "🔍 Encontrados " + response.getAutocompletePredictions().size() + " resultados para: " + query);
                })
                .addOnFailureListener((exception) -> {
                    Log.e(TAG, "❌ Error en búsqueda de lugares: " + exception.getMessage());
                    clearAutoCompleteResults();
                });
    }

    // ✅ MÉTODO IMPLEMENTADO: Actualizar resultados del autocompletado
    private void updateAutoCompleteResults(List<AutocompletePrediction> predictions) {
        currentPredictions.clear();
        currentPredictions.addAll(predictions);

        // Crear lista de strings para el adapter
        List<String> locationStrings = new ArrayList<>();
        for (AutocompletePrediction prediction : predictions) {
            String primaryText = prediction.getPrimaryText(null).toString();
            String secondaryText = prediction.getSecondaryText(null) != null ?
                    prediction.getSecondaryText(null).toString() : "";

            String displayText = primaryText;
            if (!secondaryText.isEmpty()) {
                displayText += " - " + secondaryText;
            }

            locationStrings.add(displayText);
        }

        // Actualizar adapter en el hilo principal
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                autoCompleteAdapter.clear();
                autoCompleteAdapter.addAll(locationStrings);
                autoCompleteAdapter.notifyDataSetChanged();
            });
        }
    }

    private void clearAutoCompleteResults() {
        currentPredictions.clear();
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                autoCompleteAdapter.clear();
                autoCompleteAdapter.notifyDataSetChanged();
            });
        }
    }

    private void fetchPlaceDetails(String placeId) {
        if (placesClient == null) {
            Log.e(TAG, "❌ PlacesClient no inicializado para obtener detalles");
            return;
        }

        // Definir campos que queremos obtener
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS_COMPONENTS
        );

        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, fields);

        placesClient.fetchPlace(request)
                .addOnSuccessListener((response) -> {
                    Place place = response.getPlace();

                    String locationName = place.getName() != null ? place.getName() : "Ubicación";
                    String fullAddress = place.getAddress() != null ? place.getAddress() : "";

                    if (place.getLatLng() != null) {
                        updateSelectedLocation(
                                locationName,
                                fullAddress,
                                place.getLatLng().latitude,
                                place.getLatLng().longitude
                        );

                        Log.d(TAG, "✅ Detalles del lugar obtenidos: " + locationName);
                    } else {
                        Log.e(TAG, "❌ No se pudieron obtener las coordenadas del lugar");
                    }
                })
                .addOnFailureListener((exception) -> {
                    Log.e(TAG, "❌ Error obteniendo detalles del lugar: " + exception.getMessage());
                });
    }

    private void getCurrentLocation() {
        // Verificar permisos
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        Log.d(TAG, "📍 Obteniendo ubicación actual...");

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Log.d(TAG, "✅ Ubicación obtenida: " + location.getLatitude() + ", " + location.getLongitude());
                        reverseGeocode(location.getLatitude(), location.getLongitude());
                    } else {
                        Log.e(TAG, "❌ No se pudo obtener la ubicación actual");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error obteniendo ubicación: " + e.getMessage());
                });
    }

    private void reverseGeocode(double latitude, double longitude) {
        // Convertir coordenadas a dirección usando Geocoder
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);

                // Construir nombre de ubicación
                String locationName = "";
                if (address.getThoroughfare() != null) {
                    locationName = address.getThoroughfare();
                    if (address.getSubThoroughfare() != null) {
                        locationName += " " + address.getSubThoroughfare();
                    }
                } else if (address.getFeatureName() != null) {
                    locationName = address.getFeatureName();
                } else {
                    locationName = "Ubicación Actual";
                }

                String fullAddress = address.getAddressLine(0);

                updateSelectedLocation(locationName, fullAddress, latitude, longitude);

                Log.d(TAG, "✅ Geocoding reverso exitoso: " + locationName);
            } else {
                Log.e(TAG, "❌ No se encontraron direcciones para las coordenadas");
            }
        } catch (IOException e) {
            Log.e(TAG, "❌ Error en geocoding reverso: " + e.getMessage());
        }
    }

    private void updateSelectedLocation(String locationName, String fullAddress, double latitude, double longitude) {
        selectedLocationName = locationName;
        selectedLocationAddress = fullAddress;
        selectedLatitude = latitude;
        selectedLongitude = longitude;
        hasValidLocation = true;

        // Actualizar UI en el hilo principal
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // Actualizar textos
                tvSelectedLocationName.setText(locationName);
                tvSelectedLocationAddress.setText(fullAddress);

                // Mostrar card de ubicación seleccionada
                cardSelectedLocation.setVisibility(View.VISIBLE);
                layoutEmptyLocation.setVisibility(View.GONE);

                // Habilitar botón de confirmar
                btnConfirmLocation.setEnabled(true);
                btnConfirmLocation.setAlpha(1.0f);

                // Limpiar campo de búsqueda
                etLocationSearch.setText("");

                Log.d(TAG, "✅ UI actualizada con ubicación: " + locationName);
            });
        }
    }

    private void confirmLocation() {
        if (hasValidLocation && listener != null) {
            listener.onLocationSelected(selectedLocationName, selectedLocationAddress, selectedLatitude, selectedLongitude);
            dismiss();

            Log.d(TAG, "✅ Ubicación confirmada y enviada al callback");
        } else {
            Log.e(TAG, "❌ No se puede confirmar: ubicación inválida o listener nulo");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Log.e(TAG, "❌ Permiso de ubicación denegado");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Limpiar referencias
        placesClient = null;
        sessionToken = null;
        currentPredictions.clear();
    }
}