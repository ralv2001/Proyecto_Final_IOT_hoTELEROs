package com.example.proyecto_final_hoteleros.client.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelProfile;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseHotelManager;
import com.example.proyecto_final_hoteleros.client.data.model.Hotel;
import com.example.proyecto_final_hoteleros.client.data.model.SearchContext;
import com.example.proyecto_final_hoteleros.client.data.model.CityHeader;
import com.example.proyecto_final_hoteleros.client.ui.adapters.GroupedHotelsAdapter;
import com.example.proyecto_final_hoteleros.client.ui.dialog.ModifySearchDialog;
import com.example.proyecto_final_hoteleros.client.utils.SearchTerminology;
import com.example.proyecto_final_hoteleros.client.utils.HotelGroupingUtils;
import com.example.proyecto_final_hoteleros.client.utils.UserLocationManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HotelResultsActivity extends AppCompatActivity {

    private static final String TAG = "HotelResultsActivity";

    // Views principales
    private TextView tvSearchContext;
    private TextView tvSearchSummaryLine;
    private TextView tvResultsCountInline;
    private ImageView ivBack;
    private ImageView ivExpandDetails;
    private CardView cardSearchDetails;
    private ChipGroup chipGroupQuickFilters;
    private RecyclerView recyclerViewResults;
    private ModifySearchDialog currentModifyDialog;

    // ✅ RESTAURADO: Views de detalles expandibles
    private TextView tvLocationDetailed;
    private TextView tvCheckInDetailed;
    private TextView tvCheckOutDetailed;
    private TextView tvGuestsDetailed;
    private LinearLayout btnModifySearch;
    private LinearLayout layoutDatesDetailed;

    // Loading y error states
    private LinearLayout loadingContainer;
    private TextView tvEmptyState;

    // Data
    private List<Hotel> allHotels;
    private List<HotelProfile> allHotelProfiles;
    private List<Object> groupedItems;
    private GroupedHotelsAdapter adapter;
    private List<Object> originalGroupedItems;
    private String currentFilter = "";

    // Search parameters
    private SearchContext currentContext;
    private SearchContext originalContext;
    private String searchLocation;
    private String searchDates;
    private String searchGuests;
    private String filterType;
    private boolean isDetailsExpanded = false;

    // Location manager y Firebase
    private UserLocationManager locationManager;
    private FirebaseHotelManager hotelManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_activity_hotel_results);

        // Inicializar Firebase
        hotelManager = FirebaseHotelManager.getInstance(this);
        locationManager = UserLocationManager.getInstance(this);

        initViews();
        getSearchParameters();
        determineSearchContext();

        originalContext = currentContext;

        setupIntelligentHeader();
        loadHotelsFromFirebase();
        setupSmartFilters();
        setupListeners();
    }

    // ========== INICIALIZACIÓN ==========

    private void initViews() {
        // Views principales
        tvSearchContext = findViewById(R.id.tv_search_context);
        tvSearchSummaryLine = findViewById(R.id.tv_search_summary_line);
        tvResultsCountInline = findViewById(R.id.tv_results_count_inline);
        ivBack = findViewById(R.id.iv_back);
        ivExpandDetails = findViewById(R.id.iv_expand_details);
        cardSearchDetails = findViewById(R.id.card_search_details);
        chipGroupQuickFilters = findViewById(R.id.chip_group_quick_filters);
        recyclerViewResults = findViewById(R.id.recycler_view_results);

        // ✅ RESTAURADO: Views de detalles expandibles
        tvLocationDetailed = findViewById(R.id.tv_location_detailed);
        tvCheckInDetailed = findViewById(R.id.tv_check_in_detailed);
        tvCheckOutDetailed = findViewById(R.id.tv_check_out_detailed);
        tvGuestsDetailed = findViewById(R.id.tv_guests_detailed);
        btnModifySearch = findViewById(R.id.btn_modify_search);
        layoutDatesDetailed = findViewById(R.id.layout_dates_detailed);

        // Estados de carga
        loadingContainer = findViewById(R.id.loading_container);
        tvEmptyState = findViewById(R.id.tv_empty_state);

        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getSearchParameters() {
        Intent intent = getIntent();
        searchLocation = intent.getStringExtra("search_location");
        searchDates = intent.getStringExtra("search_dates");
        searchGuests = intent.getStringExtra("search_guests");
        filterType = intent.getStringExtra("filter_type");

        Log.d(TAG, "=== PARÁMETROS DE BÚSQUEDA ===");
        Log.d(TAG, "searchLocation: " + searchLocation);
        Log.d(TAG, "searchDates: " + searchDates);
        Log.d(TAG, "searchGuests: " + searchGuests);
        Log.d(TAG, "filterType: " + filterType);
    }

    private void determineSearchContext() {
        if (filterType != null) {
            switch (filterType) {
                case "nearby_hotels":
                    currentContext = SearchContext.NEARBY_HOTELS;
                    break;
                case "popular_destinations":
                    currentContext = SearchContext.POPULAR_DESTINATIONS;
                    break;
                case "all_destinations":
                    currentContext = SearchContext.ALL_DESTINATIONS;
                    break;
                case "city_specific":
                    currentContext = SearchContext.CITY_SPECIFIC;
                    break;
                default:
                    if (searchLocation == null || searchLocation.equals("Sin restricción geográfica")) {
                        currentContext = SearchContext.LOCATION_FREE;
                    } else {
                        currentContext = SearchContext.SEARCH_RESULTS;
                    }
                    break;
            }
        }

        Log.d(TAG, "Contexto determinado: " + currentContext);
    }

    private void setupIntelligentHeader() {
        Log.d(TAG, "setupIntelligentHeader() - Contexto: " + currentContext);

        SearchContext.DefaultSearchValues defaults = currentContext.getDefaultValues();

        if (searchLocation == null || searchLocation.trim().isEmpty()) {
            searchLocation = defaults.location;
        }
        if (searchDates == null || searchDates.trim().isEmpty()) {
            searchDates = defaults.dates;
        }
        if (searchGuests == null || searchGuests.trim().isEmpty()) {
            searchGuests = defaults.guests;
        }

        String contextTitle = getOriginalContextTitle();

        if (currentContext == SearchContext.NEARBY_HOTELS) {
            contextTitle += " 🏠";
        }

        tvSearchContext.setText(contextTitle);

        String summaryLine = buildIntelligentSummary();
        tvSearchSummaryLine.setText(summaryLine);

        // ✅ CONFIGURAR detalles expandibles
        setupDetailedViews();

        Log.d(TAG, "Header configurado - Título: " + contextTitle);
    }

    // ✅ RESTAURADO: Configurar vistas detalladas
    private void setupDetailedViews() {
        if (tvLocationDetailed != null) {
            tvLocationDetailed.setText(getLocationDisplayName());
        }

        // ✅ PARSEAR FECHAS para mostrar check-in y check-out
        if (searchDates != null && tvCheckInDetailed != null && tvCheckOutDetailed != null) {
            String[] datesParts = parseDates(searchDates);
            tvCheckInDetailed.setText(datesParts[0]);
            tvCheckOutDetailed.setText(datesParts[1]);
        }

        // ✅ CONFIGURAR huéspedes detallados
        if (tvGuestsDetailed != null) {
            String guestsDetailed = parseGuests(searchGuests);
            tvGuestsDetailed.setText(guestsDetailed);
        }
    }

    // ✅ NUEVO: Parsear fechas para mostrar por separado
    private String[] parseDates(String dates) {
        if (dates == null || dates.isEmpty()) {
            return new String[]{"Hoy", "Mañana"};
        }

        if (dates.contains("–") || dates.contains("-")) {
            String separator = dates.contains("–") ? "–" : "-";
            String[] parts = dates.split(separator);
            if (parts.length == 2) {
                return new String[]{parts[0].trim(), parts[1].trim()};
            }
        }

        // Si no se puede parsear, usar valores por defecto
        return new String[]{dates, "Mañana"};
    }

    // ✅ NUEVO: Parsear huéspedes para mostrar detallado
    private String parseGuests(String guests) {
        if (guests == null || guests.isEmpty()) {
            return "2 adultos · 0 niños";
        }

        // Si ya tiene el formato correcto, devolverlo
        if (guests.contains("adultos")) {
            return guests;
        }

        // Si es un número simple, asumir que son adultos
        try {
            int guestCount = Integer.parseInt(guests.trim());
            return guestCount + " adultos · 0 niños";
        } catch (NumberFormatException e) {
            return guests;
        }
    }

    private void setupListeners() {
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        // ✅ RESTAURADO: Listener de expansión de detalles
        if (ivExpandDetails != null) {
            ivExpandDetails.setOnClickListener(v -> toggleDetailsExpansion());
        }

        // ✅ RESTAURADO: Listener de modificar búsqueda
        if (btnModifySearch != null) {
            btnModifySearch.setOnClickListener(v -> openModifySearchDialog());
        }
    }

    // ✅ RESTAURADO: Alternar expansión de detalles
    private void toggleDetailsExpansion() {
        isDetailsExpanded = !isDetailsExpanded;

        if (cardSearchDetails != null) {
            cardSearchDetails.setVisibility(isDetailsExpanded ? View.VISIBLE : View.GONE);
        }

        // Animar rotación del ícono
        if (ivExpandDetails != null) {
            float targetRotation = isDetailsExpanded ? 180f : 0f;
            ivExpandDetails.animate()
                    .rotation(targetRotation)
                    .setDuration(300)
                    .start();
        }

        Log.d(TAG, "Detalles " + (isDetailsExpanded ? "expandidos" : "colapsados"));
    }

    // ✅ RESTAURADO: Abrir diálogo de modificación
    private void openModifySearchDialog() {
        if (currentContext == null) {
            Toast.makeText(this, "Error: contexto no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        currentModifyDialog = new ModifySearchDialog(
                this,
                currentContext,
                searchLocation,
                searchDates,
                searchGuests
        );

        currentModifyDialog.setOnSearchModifiedListener((newLocation, newDates, newGuests) -> {
            Log.d(TAG, "Búsqueda modificada: " + newLocation + ", " + newDates + ", " + newGuests);

            searchLocation = newLocation;
            searchDates = newDates;
            searchGuests = newGuests;

            setupIntelligentHeader();
            processHotelsIntelligently();
            updateResultsCount();

            Toast.makeText(this, "Búsqueda actualizada", Toast.LENGTH_SHORT).show();
        });

        currentModifyDialog.setOnLocationRequestListener(currentLocation -> {
            Intent intent = new Intent(this, LocationSelectorActivity.class);
            intent.putExtra("current_location", currentLocation);
            startActivityForResult(intent, ModifySearchDialog.REQUEST_CODE_LOCATION);
        });

        currentModifyDialog.show();
    }

    // ========== CARGA DE HOTELES ==========

    private void loadHotelsFromFirebase() {
        Log.d(TAG, "🔄 Cargando hoteles desde Firebase para contexto: " + currentContext);
        showLoadingState();

        hotelManager.findHotelsNearLocation(0, 0, 999999, new FirebaseHotelManager.HotelsCallback() {
            @Override
            public void onSuccess(List<HotelProfile> hotelProfiles) {
                Log.d(TAG, "✅ Hoteles cargados desde Firebase: " + hotelProfiles.size());

                allHotelProfiles = hotelProfiles;

                // ✅ CAMBIO IMPORTANTE: No llamar directamente a processHotelsIntelligently
                // porque convertHotelProfilesToHotels ahora es asíncrono y maneja la UI internamente
                convertHotelProfilesToHotels(hotelProfiles);

                // Ocultar loading inmediatamente ya que el procesamiento asíncrono se encarga
                runOnUiThread(() -> {
                    hideLoadingState();
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando hoteles desde Firebase: " + error);

                runOnUiThread(() -> {
                    hideLoadingState();
                    showErrorState("Error cargando hoteles: " + error);
                    Toast.makeText(HotelResultsActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    // ✅ MÉTODO ACTUALIZADO: Usar fotos reales de Firebase
    // ✅ MÉTODO ACTUALIZADO: Usar fotos reales de Firebase y precios reales de habitaciones
    private List<Hotel> convertHotelProfilesToHotels(List<HotelProfile> hotelProfiles) {
        List<Hotel> hotels = new ArrayList<>();
        AtomicInteger processedHotels = new AtomicInteger(0);
        int totalHotels = hotelProfiles.size();

        Log.d(TAG, "🔄 Convirtiendo " + totalHotels + " hoteles con precios reales en HotelResultsActivity...");

        for (HotelProfile profile : hotelProfiles) {
            if (profile != null && profile.isActive()) {
                String city = extractCityFromProfile(profile);

                // ✅ EXTRAER PRIMERA FOTO REAL DEL HOTEL
                String imageUrl = getFirstPhotoFromProfile(profile);

                // ✅ OBTENER PRECIO REAL DE HABITACIONES
                com.example.proyecto_final_hoteleros.client.utils.HotelPriceUtils.getMinimumRoomPrice(profile, this, new com.example.proyecto_final_hoteleros.client.utils.HotelPriceUtils.PriceCallback() {
                    @Override
                    public void onPriceObtained(String formattedPrice, double rawPrice) {
                        Hotel hotel = new Hotel(
                                profile.getName(),
                                profile.getFullAddress() != null ? profile.getFullAddress() : profile.getAddress(),
                                imageUrl,
                                formattedPrice, // ✅ PRECIO REAL DE HABITACIÓN MÁS BARATA
                                generateRatingFromProfile(profile)
                        );

                        synchronized (hotels) {
                            hotels.add(hotel);

                            int completed = processedHotels.incrementAndGet();
                            Log.d(TAG, "💰 Hotel " + completed + "/" + totalHotels + ": " + profile.getName() + " - " + formattedPrice);

                            // Cuando todos los hoteles estén procesados, actualizar UI
                            if (completed == totalHotels) {
                                runOnUiThread(() -> {
                                    Log.d(TAG, "✅ Todos los hoteles procesados con precios reales en HotelResults");

                                    // Actualizar allHotels con la lista completa
                                    allHotels = hotels;

                                    // Procesar hoteles y actualizar UI
                                    processHotelsIntelligently();
                                    setupAdapter();
                                    updateResultsCount();
                                });
                            }
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.w(TAG, "⚠️ Error obteniendo precio para " + profile.getName() + ": " + error);

                        // Usar precio por defecto en caso de error
                        String defaultPrice = com.example.proyecto_final_hoteleros.client.utils.HotelPriceUtils.generatePriceSync(profile);

                        Hotel hotel = new Hotel(
                                profile.getName(),
                                profile.getFullAddress() != null ? profile.getFullAddress() : profile.getAddress(),
                                imageUrl,
                                defaultPrice,
                                generateRatingFromProfile(profile)
                        );

                        synchronized (hotels) {
                            hotels.add(hotel);

                            int completed = processedHotels.incrementAndGet();
                            Log.d(TAG, "⚠️ Hotel " + completed + "/" + totalHotels + " (precio por defecto): " + profile.getName() + " - " + defaultPrice);

                            if (completed == totalHotels) {
                                runOnUiThread(() -> {
                                    Log.d(TAG, "✅ Todos los hoteles procesados en HotelResults (algunos con precios por defecto)");

                                    // Actualizar allHotels con la lista completa
                                    allHotels = hotels;

                                    // Procesar hoteles y actualizar UI
                                    processHotelsIntelligently();
                                    setupAdapter();
                                    updateResultsCount();
                                });
                            }
                        }
                    }
                });

                // ✅ LOG PARA VER QUE ESTÁ FUNCIONANDO
                Log.d(TAG, "🏨 Procesando hotel en HotelResults: " + profile.getName() + " - Foto: " +
                        (imageUrl.startsWith("http") ? "URL_REAL" : "PLACEHOLDER"));
            }
        }

        // Si no hay hoteles activos, retornar lista vacía inmediatamente
        if (totalHotels == 0) {
            Log.d(TAG, "❌ No hay hoteles activos para procesar en HotelResults");
            runOnUiThread(() -> {
                allHotels = hotels;
                processHotelsIntelligently();
                setupAdapter();
                updateResultsCount();
            });
        }

        return hotels; // Esta lista se irá llenando asíncronamente
    }

    // ✅ NUEVO MÉTODO: Extraer primera foto del perfil del hotel
    private String getFirstPhotoFromProfile(HotelProfile profile) {
        // ✅ USAR UTILIDAD ROBUSTA para obtener fotos
        return com.example.proyecto_final_hoteleros.client.utils.HotelPhotoUtils.getFirstPhotoFromProfile(profile);
    }

    private String extractCityFromProfile(HotelProfile profile) {
        if (profile.getDepartamento() != null && !profile.getDepartamento().isEmpty()) {
            return profile.getDepartamento();
        }

        if (profile.getProvincia() != null && !profile.getProvincia().isEmpty()) {
            return profile.getProvincia();
        }

        String address = profile.getFullAddress() != null ? profile.getFullAddress() : profile.getAddress();
        String extractedCity = HotelGroupingUtils.extractCityFromLocation(address);

        return extractedCity != null ? extractedCity : "Lima";
    }

    private String generatePriceFromProfile(HotelProfile profile) {
        int basePrice = 120 + (int)(Math.random() * 350);
        return "S/" + basePrice;
    }

    private String generateRatingFromProfile(HotelProfile profile) {
        double rating = 4.0 + (Math.random() * 1.0);
        return String.format(Locale.US, "%.1f", rating);
    }

    // ========== PROCESAMIENTO DE HOTELES ==========

    private void processHotelsIntelligently() {
        Log.d(TAG, "processHotelsIntelligently() - Contexto ORIGINAL: " + originalContext);

        if (allHotels == null || allHotels.isEmpty()) {
            showEmptyState("No se encontraron hoteles disponibles");
            return;
        }

        switch (originalContext) {
            case ALL_DESTINATIONS:
                loadAllDestinationsGrouped();
                break;
            case NEARBY_HOTELS:
                loadNearbyHotelsWithCurrentLocation();
                break;
            case CITY_SPECIFIC:
                loadCitySpecificHotels();
                break;
            case POPULAR_DESTINATIONS:
                loadPopularDestinationsFiltered();
                break;
            case LOCATION_FREE:
                loadLocationFreeResults();
                break;
            case SEARCH_RESULTS:
                loadSearchResults();
                break;
        }

        originalGroupedItems = new ArrayList<>(groupedItems);
        Log.d(TAG, "Hoteles procesados: " + (groupedItems != null ? groupedItems.size() : 0) + " items");
    }

    // ========== MÉTODOS DE CARGA POR CONTEXTO ==========

    private void loadAllDestinationsGrouped() {
        Log.d(TAG, "loadAllDestinationsGrouped() - searchLocation: '" + searchLocation + "'");

        groupedItems = new ArrayList<>();

        boolean hasSpecificLocation = searchLocation != null &&
                !searchLocation.trim().isEmpty() &&
                !searchLocation.equals("Todas las ubicaciones") &&
                !searchLocation.equals("Destinos disponibles");

        if (hasSpecificLocation) {
            String cityName = HotelGroupingUtils.extractCityFromLocation(searchLocation.toLowerCase());

            if (cityName != null) {
                List<Hotel> cityHotels = HotelGroupingUtils.getHotelsForCity(allHotels, cityName);

                if (!cityHotels.isEmpty()) {
                    String capitalizedCity = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);
                    groupedItems.add(new CityHeader("Hoteles en " + capitalizedCity, cityHotels.size()));
                    groupedItems.addAll(cityHotels);
                } else {
                    String capitalizedCity = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);
                    groupedItems.add(new CityHeader("No hay hoteles disponibles en " + capitalizedCity, 0));
                }
            } else {
                List<Hotel> foundHotels = HotelGroupingUtils.searchHotels(allHotels, searchLocation);

                if (!foundHotels.isEmpty()) {
                    groupedItems.add(new CityHeader("Resultados para '" + searchLocation + "'", foundHotels.size()));
                    groupedItems.addAll(foundHotels);
                } else {
                    groupedItems.add(new CityHeader("No se encontraron hoteles para '" + searchLocation + "'", 0));
                }
            }
        } else {
            groupedItems = HotelGroupingUtils.groupHotelsByCity(allHotels);
        }
    }

    private void loadNearbyHotelsWithCurrentLocation() {
        String targetCity = locationManager.getCurrentCity();
        String displayCityName = targetCity;

        if (searchLocation != null && !searchLocation.equals("Tu ciudad actual") &&
                !searchLocation.equals(locationManager.getCurrentCity())) {
            String newCity = HotelGroupingUtils.extractCityFromLocation(searchLocation.toLowerCase());
            if (newCity != null) {
                targetCity = newCity;
                displayCityName = newCity.substring(0, 1).toUpperCase() + newCity.substring(1);
            }
        }

        List<Hotel> nearbyHotels = HotelGroupingUtils.filterHotelsByProximity(allHotels, targetCity, this);
        groupedItems = new ArrayList<>();

        if (!nearbyHotels.isEmpty()) {
            if (!displayCityName.equals(locationManager.getCurrentCity())) {
                groupedItems.add(new CityHeader("Hoteles cercanos en " + displayCityName, nearbyHotels.size()));
            } else {
                groupedItems.add(new CityHeader("Hoteles cercanos", nearbyHotels.size()));
            }
            groupedItems.addAll(nearbyHotels);
        } else {
            groupedItems.add(new CityHeader("No hay hoteles cercanos en " + displayCityName, 0));
        }
    }

    private void loadPopularDestinationsFiltered() {
        List<Hotel> popularHotels = HotelGroupingUtils.filterPopularHotels(allHotels);

        if (searchLocation != null && !searchLocation.equals("Los más buscados")) {
            String cityName = HotelGroupingUtils.extractCityFromLocation(searchLocation.toLowerCase());
            if (cityName != null) {
                List<Hotel> cityPopularHotels = new ArrayList<>();
                for (Hotel hotel : popularHotels) {
                    String hotelCity = HotelGroupingUtils.extractCityFromLocation(hotel.getLocation().toLowerCase());
                    if (cityName.equals(hotelCity)) {
                        cityPopularHotels.add(hotel);
                    }
                }

                groupedItems = new ArrayList<>();
                if (!cityPopularHotels.isEmpty()) {
                    String capitalizedCity = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);
                    groupedItems.add(new CityHeader("Destinos populares en " + capitalizedCity, cityPopularHotels.size()));
                    groupedItems.addAll(cityPopularHotels);
                } else {
                    String capitalizedCity = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);
                    groupedItems.add(new CityHeader("No hay destinos populares en " + capitalizedCity, 0));
                }
                return;
            }
        }

        groupedItems = new ArrayList<>();
        if (!popularHotels.isEmpty()) {
            groupedItems.add(new CityHeader("Destinos populares", popularHotels.size()));
            groupedItems.addAll(popularHotels);
        } else {
            groupedItems.add(new CityHeader("No hay destinos populares disponibles", 0));
        }
    }

    private void loadCitySpecificHotels() {
        groupedItems = new ArrayList<>();

        if (searchLocation != null && !searchLocation.trim().isEmpty()) {
            List<Hotel> cityHotels = HotelGroupingUtils.getHotelsForCitySpecific(allHotels, searchLocation);

            if (!cityHotels.isEmpty()) {
                String cityName = HotelGroupingUtils.extractCityFromLocation(searchLocation.toLowerCase());
                String headerText = cityName != null ?
                        "Hoteles en " + cityName.substring(0, 1).toUpperCase() + cityName.substring(1) :
                        "Hoteles encontrados";

                groupedItems.add(new CityHeader(headerText, cityHotels.size()));
                groupedItems.addAll(cityHotels);
            } else {
                groupedItems.add(new CityHeader("No se encontraron hoteles en " + searchLocation, 0));
            }
        } else {
            groupedItems = HotelGroupingUtils.groupHotelsByCity(allHotels);
        }
    }

    private void loadLocationFreeResults() {
        groupedItems = HotelGroupingUtils.groupHotelsByCity(allHotels);
    }

    private void loadSearchResults() {
        groupedItems = new ArrayList<>();

        if (searchLocation != null && !searchLocation.trim().isEmpty()) {
            List<Hotel> searchResults = HotelGroupingUtils.searchHotels(allHotels, searchLocation);

            if (!searchResults.isEmpty()) {
                groupedItems.add(new CityHeader("Resultados de búsqueda", searchResults.size()));
                groupedItems.addAll(searchResults);
            } else {
                groupedItems.add(new CityHeader("Sin resultados para '" + searchLocation + "'", 0));
            }
        } else {
            groupedItems = HotelGroupingUtils.groupHotelsByCity(allHotels);
        }
    }

    // ========== ADAPTER Y UI ==========

    private void setupAdapter() {
        if (groupedItems == null || groupedItems.isEmpty()) {
            showEmptyState("No hay hoteles disponibles");
            return;
        }

        hideEmptyState();
        adapter = new GroupedHotelsAdapter(this, groupedItems);
        adapter.setOnHotelClickListener((hotel, position) -> {
            navigateToHotelDetail(hotel);
        });
        recyclerViewResults.setAdapter(adapter);
    }

    private void updateResultsCount() {
        if (groupedItems != null && tvResultsCountInline != null) {
            int hotelCount = 0;
            for (Object item : groupedItems) {
                if (item instanceof Hotel) {
                    hotelCount++;
                }
            }

            String countText = hotelCount + " hoteles encontrados";
            tvResultsCountInline.setText(countText);
        }
    }

    private void setupSmartFilters() {
        if (chipGroupQuickFilters != null && currentContext != null) {
            String[] filters = currentContext.getContextualFilters();

            for (String filter : filters) {
                Chip chip = new Chip(this);
                chip.setText(filter);
                chip.setCheckable(true);
                chipGroupQuickFilters.addView(chip);
            }
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    private String getOriginalContextTitle() {
        switch (originalContext) {
            case ALL_DESTINATIONS:
                return "Destinos disponibles";
            case NEARBY_HOTELS:
                return "Hoteles cercanos";
            case POPULAR_DESTINATIONS:
                return "Destinos populares";
            case SEARCH_RESULTS:
                return "Resultados de búsqueda";
            case CITY_SPECIFIC:
                return "Hoteles en " + (searchLocation != null ? searchLocation : "ciudad");
            case LOCATION_FREE:
                return "Búsqueda amplia";
            default:
                return "Destinos disponibles";
        }
    }

    private String getLocationDisplayName() {
        switch (originalContext) {
            case ALL_DESTINATIONS:
                if (searchLocation != null && !searchLocation.equals("Todas las ubicaciones")) {
                    String cityName = HotelGroupingUtils.extractCityFromLocation(searchLocation.toLowerCase());
                    if (cityName != null) {
                        cityName = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);
                        return "Todos los hoteles de " + cityName;
                    }
                }
                return SearchTerminology.ALL_DESTINATIONS;

            case NEARBY_HOTELS:
                return locationManager.getCurrentCity();

            case LOCATION_FREE:
                return SearchTerminology.LOCATION_FREE;

            case CITY_SPECIFIC:
                return searchLocation != null ? searchLocation : "Ciudad seleccionada";

            case POPULAR_DESTINATIONS:
                if (searchLocation != null && !searchLocation.equals("Los más buscados")) {
                    String cityName = HotelGroupingUtils.extractCityFromLocation(searchLocation.toLowerCase());
                    if (cityName != null) {
                        cityName = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);
                        return "Los más buscados (" + cityName + ")";
                    }
                }
                return "Los más buscados";

            case SEARCH_RESULTS:
                return SearchTerminology.getContextualLocationText(searchLocation);

            default:
                return SearchTerminology.ALL_DESTINATIONS;
        }
    }

    private String buildIntelligentSummary() {
        StringBuilder summary = new StringBuilder();
        String locationText = getLocationDisplayName();
        summary.append(locationText);

        if (hasSpecificDates()) {
            summary.append(" • ").append(getDatesSummary());
        }

        if (hasNonDefaultGuests()) {
            summary.append(" • ").append(getGuestsSummary());
        }

        return summary.toString();
    }

    private boolean hasSpecificDates() {
        return searchDates != null && !searchDates.trim().isEmpty() &&
                !searchDates.equals("Hoy - Mañana") && !searchDates.equals("Fechas flexibles");
    }

    private boolean hasNonDefaultGuests() {
        return searchGuests != null && !searchGuests.equals("2 adultos") &&
                !searchGuests.equals("2 adultos · 0 niños");
    }

    private String getDatesSummary() {
        if (searchDates == null) return "Fechas flexibles";
        if (searchDates.contains("–")) {
            String[] dates = searchDates.split("–");
            if (dates.length == 2) {
                return dates[0].trim() + " - " + dates[1].trim();
            }
        }
        return searchDates;
    }

    private String getGuestsSummary() {
        if (searchGuests == null || searchGuests.trim().isEmpty()) {
            return "2 adultos";
        }
        return searchGuests;
    }

    // ========== ESTADOS DE UI ==========

    private void showLoadingState() {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.VISIBLE);
        }
        if (recyclerViewResults != null) {
            recyclerViewResults.setVisibility(View.GONE);
        }
        hideEmptyState();
    }

    private void hideLoadingState() {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.GONE);
        }
        if (recyclerViewResults != null) {
            recyclerViewResults.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState(String message) {
        if (tvEmptyState != null) {
            tvEmptyState.setText(message);
            tvEmptyState.setVisibility(View.VISIBLE);
        }
        if (recyclerViewResults != null) {
            recyclerViewResults.setVisibility(View.GONE);
        }
    }

    private void showErrorState(String error) {
        showEmptyState("Error: " + error);
    }

    private void hideEmptyState() {
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    // ========== NAVEGACIÓN ==========

    private void navigateToHotelDetail(Hotel hotel) {
        Log.d(TAG, "🏨 Navegar a detalles del hotel: " + hotel.getName());
        Toast.makeText(this, "Ver detalles: " + hotel.getName(), Toast.LENGTH_SHORT).show();
    }

    // ========== LIFECYCLE ==========

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == ModifySearchDialog.REQUEST_CODE_LOCATION) {
                String selectedLocation = data.getStringExtra("selected_location");
                if (selectedLocation != null) {
                    Log.d(TAG, "Nueva ubicación recibida: " + selectedLocation);

                    if (currentModifyDialog != null && currentModifyDialog.isShowing()) {
                        currentModifyDialog.updateLocation(selectedLocation);
                    } else {
                        searchLocation = selectedLocation;
                        updateCompleteUI();
                    }
                }
            }
        }
    }

    private void updateCompleteUI() {
        setupIntelligentHeader();
        loadHotelsFromFirebase();
    }
}