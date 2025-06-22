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
import java.util.Map;

public class HotelResultsActivity extends AppCompatActivity {

    private static final String TAG = "HotelResultsActivity";

    // Views
    private TextView tvSearchContext;
    private TextView tvSearchSummaryLine;
    private TextView tvResultsCountInline;
    private ImageView ivBack;
    private ImageView ivExpandDetails;
    private CardView cardSearchDetails;
    private ChipGroup chipGroupQuickFilters;
    private RecyclerView recyclerViewResults;
    private ModifySearchDialog currentModifyDialog;

    // Data
    private List<Hotel> allHotels;
    private List<Object> groupedItems;
    private GroupedHotelsAdapter adapter;
    private List<Object> originalGroupedItems;
    private String currentFilter = "";

    // Search parameters
    private SearchContext currentContext;
    private SearchContext originalContext; // ✅ NUEVO: Guardar contexto original
    private String searchLocation;
    private String searchDates;
    private String searchGuests;
    private String filterType;
    private boolean isDetailsExpanded = false;

    // Location manager
    private UserLocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_activity_hotel_results);

        locationManager = UserLocationManager.getInstance(this);

        initViews();
        getSearchParameters();
        determineSearchContext();

        // ✅ NUEVO: Guardar contexto original
        originalContext = currentContext;

        setupIntelligentHeader();
        loadHotelsIntelligently();
        setupSmartFilters();
        setupListeners();
    }

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
                        // ✅ CORREGIDO: NO cambiar contexto, solo actualizar datos
                        updateCompleteUI();
                    }
                }
            }
        }
    }

    private void initViews() {
        tvSearchContext = findViewById(R.id.tv_search_context);
        tvSearchSummaryLine = findViewById(R.id.tv_search_summary_line);
        tvResultsCountInline = findViewById(R.id.tv_results_count_inline);
        ivBack = findViewById(R.id.iv_back);
        ivExpandDetails = findViewById(R.id.iv_expand_details);
        cardSearchDetails = findViewById(R.id.card_search_details);
        chipGroupQuickFilters = findViewById(R.id.chip_group_quick_filters);
        recyclerViewResults = findViewById(R.id.recycler_view_results);

        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getSearchParameters() {
        Intent intent = getIntent();
        searchLocation = intent.getStringExtra("location");
        searchDates = intent.getStringExtra("dates");
        searchGuests = intent.getStringExtra("guests");
        filterType = intent.getStringExtra("filter_type");

        Log.d(TAG, "Parámetros recibidos:");
        Log.d(TAG, "Location: " + searchLocation);
        Log.d(TAG, "Dates: " + searchDates);
        Log.d(TAG, "Guests: " + searchGuests);
        Log.d(TAG, "FilterType: " + filterType);
    }

    private void determineSearchContext() {
        if (filterType == null || filterType.isEmpty()) {
            currentContext = SearchContext.ALL_DESTINATIONS;
        } else {
            switch (filterType) {
                case "nearby":
                    currentContext = SearchContext.NEARBY_HOTELS;
                    break;
                case "popular":
                    currentContext = SearchContext.POPULAR_DESTINATIONS;
                    break;
                case "city":
                    currentContext = SearchContext.CITY_SPECIFIC;
                    break;
                case "search":
                    if (searchLocation == null || searchLocation.trim().isEmpty()) {
                        currentContext = SearchContext.LOCATION_FREE;
                    } else {
                        currentContext = SearchContext.SEARCH_RESULTS;
                    }
                    break;
                default:
                    currentContext = SearchContext.ALL_DESTINATIONS;
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

        // ✅ MANTENER SIEMPRE EL TÍTULO ORIGINAL
        String contextTitle = getOriginalContextTitle();

        if (currentContext == SearchContext.NEARBY_HOTELS) {
            contextTitle += " 🏠";
        }

        tvSearchContext.setText(contextTitle);

        String summaryLine = buildIntelligentSummary();
        tvSearchSummaryLine.setText(summaryLine);

        Log.d(TAG, "Header configurado - Título: " + contextTitle);
    }

    // ✅ NUEVO: Obtener título original sin cambios
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
                // ✅ CORREGIDO: Si cambió la ubicación, mostrar ciudad específica
                if (searchLocation != null && !searchLocation.equals("Todas las ubicaciones")) {
                    String cityName = HotelGroupingUtils.extractCityFromLocation(searchLocation.toLowerCase());
                    if (cityName != null) {
                        // Capitalizar primera letra
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
                // ✅ CORREGIDO: Si cambió la ubicación, mostrar ciudad específica
                if (searchLocation != null && !searchLocation.equals("Los más buscados")) {
                    String cityName = HotelGroupingUtils.extractCityFromLocation(searchLocation.toLowerCase());
                    if (cityName != null) {
                        // Capitalizar primera letra
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

    // ✅ ELIMINADO: updateSearchContextForLocation - NO cambiar contexto nunca

    private void loadHotelsIntelligently() {
        Log.d(TAG, "loadHotelsIntelligently() - Contexto ORIGINAL: " + originalContext);

        allHotels = loadSampleHotels();

        // ✅ USAR SIEMPRE EL CONTEXTO ORIGINAL
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
        setupAdapter();
        updateResultsCount();

        Log.d(TAG, "Hoteles cargados: " + (groupedItems != null ? groupedItems.size() : 0) + " items");
    }

    // ✅ CORREGIR: loadAllDestinationsGrouped() - Lógica más robusta
    private void loadAllDestinationsGrouped() {
        Log.d(TAG, "loadAllDestinationsGrouped() - searchLocation: '" + searchLocation + "'");

        groupedItems = new ArrayList<>();

        // ✅ CONDICIÓN MÁS ESPECÍFICA: verificar si realmente cambió la ubicación
        boolean hasSpecificLocation = searchLocation != null &&
                !searchLocation.trim().isEmpty() &&
                !searchLocation.equals("Todas las ubicaciones") &&
                !searchLocation.equals("Destinos disponibles");

        Log.d(TAG, "hasSpecificLocation: " + hasSpecificLocation);

        if (hasSpecificLocation) {
            String cityName = HotelGroupingUtils.extractCityFromLocation(searchLocation.toLowerCase());
            Log.d(TAG, "Ciudad extraída: " + cityName);

            if (cityName != null) {
                // Buscar hoteles específicos de esa ciudad
                List<Hotel> cityHotels = HotelGroupingUtils.getHotelsForCity(allHotels, cityName);
                Log.d(TAG, "Hoteles encontrados en " + cityName + ": " + cityHotels.size());

                if (!cityHotels.isEmpty()) {
                    // Hay hoteles en esa ciudad
                    String capitalizedCity = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);
                    groupedItems.add(new CityHeader("Hoteles en " + capitalizedCity, cityHotels.size()));
                    groupedItems.addAll(cityHotels);
                    Log.d(TAG, "Mostrando hoteles de " + capitalizedCity);
                } else {
                    // ✅ NO HAY HOTELES EN ESA CIUDAD - NO MOSTRAR TODOS
                    String capitalizedCity = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);
                    groupedItems.add(new CityHeader("No hay hoteles disponibles en " + capitalizedCity, 0));
                    Log.d(TAG, "No hay hoteles en " + capitalizedCity + " - mostrando mensaje vacío");
                }
            } else {
                // ✅ NO SE PUDO EXTRAER CIUDAD - BUSCAR POR NOMBRE COMPLETO
                Log.d(TAG, "No se pudo extraer ciudad, buscando por nombre completo: " + searchLocation);
                List<Hotel> foundHotels = HotelGroupingUtils.searchHotels(allHotels, searchLocation);

                if (!foundHotels.isEmpty()) {
                    groupedItems.add(new CityHeader("Resultados para '" + searchLocation + "'", foundHotels.size()));
                    groupedItems.addAll(foundHotels);
                    Log.d(TAG, "Encontrados " + foundHotels.size() + " hoteles para '" + searchLocation + "'");
                } else {
                    groupedItems.add(new CityHeader("No se encontraron hoteles para '" + searchLocation + "'", 0));
                    Log.d(TAG, "No se encontraron hoteles para '" + searchLocation + "'");
                }
            }
        } else {
            // ✅ NO HAY UBICACIÓN ESPECÍFICA - MOSTRAR TODOS AGRUPADOS
            Log.d(TAG, "No hay ubicación específica, mostrando todos agrupados por ciudad");
            groupedItems = HotelGroupingUtils.groupHotelsByCity(allHotels);
        }

        Log.d(TAG, "Total items en groupedItems: " + groupedItems.size());
    }

    // ✅ NUEVO: Hoteles cercanos pero respetando ubicación modificada
    private void loadNearbyHotelsWithCurrentLocation() {
        String targetCity = locationManager.getCurrentCity();
        String displayCityName = targetCity;

        // Si se modificó la ubicación, usar la nueva
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
            // ✅ MEJORADO: Mostrar ciudad específica en el header
            if (!displayCityName.equals(locationManager.getCurrentCity())) {
                groupedItems.add(new CityHeader("Hoteles cercanos en " + displayCityName, nearbyHotels.size()));
            } else {
                groupedItems.add(new CityHeader("Hoteles cercanos", nearbyHotels.size()));
            }
            groupedItems.addAll(nearbyHotels);
        } else {
            // ✅ MEJORADO: Mensaje más específico
            groupedItems.add(new CityHeader("No hay hoteles cercanos en " + displayCityName, 0));
        }
    }

    // ✅ NUEVO: Destinos populares filtrados por ciudad si se especifica
    private void loadPopularDestinationsFiltered() {
        List<Hotel> popularHotels = HotelGroupingUtils.filterPopularHotels(allHotels);

        // Si se especificó una ciudad, filtrar solo los populares de esa ciudad
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

                // ✅ CORREGIDO: Si no hay populares en esa ciudad, no mostrar todos
                groupedItems = new ArrayList<>();
                if (!cityPopularHotels.isEmpty()) {
                    // Capitalizar primera letra
                    String capitalizedCity = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);
                    groupedItems.add(new CityHeader("Destinos populares en " + capitalizedCity, cityPopularHotels.size()));
                    groupedItems.addAll(cityPopularHotels);
                } else {
                    // ✅ NUEVO: Mostrar mensaje específico cuando no hay populares en esa ciudad
                    String capitalizedCity = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);
                    groupedItems.add(new CityHeader("No hay destinos populares en " + capitalizedCity, 0));

                    // ✅ OPCIONAL: Agregar mensaje explicativo
                    Log.d(TAG, "No se encontraron destinos populares en " + capitalizedCity);
                }
                return;
            }
        }

        // Si no se especificó ciudad o no se pudo extraer, mostrar todos los populares
        groupedItems = new ArrayList<>();
        if (!popularHotels.isEmpty()) {
            groupedItems.add(new CityHeader("Destinos populares", popularHotels.size()));
            groupedItems.addAll(popularHotels);
        } else {
            groupedItems.add(new CityHeader("No hay destinos populares", 0));
        }
    }

    private void loadCitySpecificHotels() {
        List<Hotel> cityHotels = HotelGroupingUtils.getHotelsForCity(allHotels, searchLocation);
        groupedItems = new ArrayList<>();

        if (!cityHotels.isEmpty()) {
            groupedItems.add(new CityHeader(searchLocation, cityHotels.size()));
            groupedItems.addAll(cityHotels);
        } else {
            groupedItems.add(new CityHeader("No se encontraron hoteles en " + searchLocation, 0));
        }
    }

    private void loadLocationFreeResults() {
        groupedItems = HotelGroupingUtils.groupHotelsByCity(allHotels);
    }

    private void loadSearchResults() {
        List<Hotel> searchResults;

        if (searchLocation != null && !searchLocation.trim().isEmpty()) {
            searchResults = HotelGroupingUtils.getHotelsForCitySpecific(allHotels, searchLocation);
            String cityName = HotelGroupingUtils.extractCityFromLocation(searchLocation.toLowerCase());

            groupedItems = new ArrayList<>();

            if (!searchResults.isEmpty()) {
                if (cityName != null) {
                    cityName = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);
                    groupedItems.add(new CityHeader(cityName, searchResults.size()));
                } else {
                    groupedItems.add(new CityHeader("Resultados de búsqueda", searchResults.size()));
                }
                groupedItems.addAll(searchResults);
            } else {
                groupedItems.add(new CityHeader("No se encontraron resultados", 0));
            }
        } else {
            searchResults = allHotels;
            groupedItems = new ArrayList<>();
            groupedItems.add(new CityHeader("Resultados de búsqueda", searchResults.size()));
            groupedItems.addAll(searchResults);
        }
    }

    private void setupSmartFilters() {
        chipGroupQuickFilters.removeAllViews();

        // ✅ USAR FILTROS DEL CONTEXTO ORIGINAL
        String[] filterOptions = originalContext.getContextualFilters();

        for (String filterText : filterOptions) {
            addFilterChip(filterText);
        }

        Log.d(TAG, "Filtros configurados: " + filterOptions.length + " filtros");
    }

    private void addFilterChip(String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCheckable(true);
        chip.setChipBackgroundColorResource(android.R.color.white);
        chip.setTextColor(getResources().getColor(android.R.color.black));
        chip.setChipStrokeWidth(2);
        chip.setChipStrokeColorResource(R.color.light_gray);

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                for (int i = 0; i < chipGroupQuickFilters.getChildCount(); i++) {
                    Chip otherChip = (Chip) chipGroupQuickFilters.getChildAt(i);
                    if (otherChip != chip) {
                        otherChip.setChecked(false);
                    }
                }

                chip.setChipBackgroundColorResource(R.color.orange);
                chip.setTextColor(getResources().getColor(android.R.color.white));
                chip.setChipStrokeWidth(0);
                applyFilter(text);
                currentFilter = text;
            } else {
                chip.setChipBackgroundColorResource(android.R.color.white);
                chip.setTextColor(getResources().getColor(android.R.color.black));
                chip.setChipStrokeWidth(2);
                removeFilter(text);
                currentFilter = "";
            }
        });

        chipGroupQuickFilters.addView(chip);
    }

    private void applyFilter(String filterText) {
        if (originalGroupedItems == null) return;

        Log.d(TAG, "Aplicando filtro: " + filterText);

        List<Object> filteredItems = new ArrayList<>();
        List<Hotel> hotels = extractHotels(originalGroupedItems);

        if (filterText.contains("Distancia")) {
            hotels = HotelGroupingUtils.sortByDistance(hotels, searchLocation, this);
        } else if (filterText.contains("Precio") || filterText.contains("precio") || filterText.contains("Económicos")) {
            boolean ascending = filterText.contains("Mejor precio") || filterText.contains("Económicos");
            hotels = HotelGroupingUtils.sortByPrice(hotels, ascending);
        } else if (filterText.contains("Rating") || filterText.contains("rating") ||
                filterText.contains("Top rated") || filterText.contains("Populares") ||
                filterText.contains("valorados")) {
            hotels = HotelGroupingUtils.sortByRating(hotels);
        } else if (filterText.contains("cercanos")) {
            hotels = HotelGroupingUtils.sortByDistance(hotels, locationManager.getCurrentCity(), this);
        }

        rebuildFilteredItems(filteredItems, hotels);

        adapter.updateItems(filteredItems);
        updateResultsCount();
        showFilterToast("Filtro aplicado: " + filterText.substring(2));

        Log.d(TAG, "Filtro aplicado. Items resultantes: " + filteredItems.size());
    }

    private void rebuildFilteredItems(List<Object> filteredItems, List<Hotel> sortedHotels) {
        switch (originalContext) {
            case ALL_DESTINATIONS:
                // ✅ LÓGICA MEJORADA: verificar si hay ciudad específica
                boolean hasSpecificLocation = searchLocation != null &&
                        !searchLocation.trim().isEmpty() &&
                        !searchLocation.equals("Todas las ubicaciones") &&
                        !searchLocation.equals("Destinos disponibles");

                if (hasSpecificLocation) {
                    String cityName = HotelGroupingUtils.extractCityFromLocation(searchLocation.toLowerCase());
                    if (cityName != null && !sortedHotels.isEmpty()) {
                        String capitalizedCity = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);
                        filteredItems.add(new CityHeader("Hoteles en " + capitalizedCity, sortedHotels.size()));
                        filteredItems.addAll(sortedHotels);
                    } else {
                        // ✅ NO HAY HOTELES O NO SE PUDO EXTRAER CIUDAD
                        String searchTerm = searchLocation;
                        if (cityName != null) {
                            searchTerm = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);
                        }
                        filteredItems.add(new CityHeader("No hay hoteles disponibles en " + searchTerm, 0));
                    }
                    return;
                }

                // ✅ NO HAY CIUDAD ESPECÍFICA - AGRUPAR POR CIUDADES
                Map<String, List<Hotel>> cityGroups = HotelGroupingUtils.groupHotelsByCityMap(sortedHotels);
                for (Map.Entry<String, List<Hotel>> entry : cityGroups.entrySet()) {
                    if (!entry.getValue().isEmpty()) {
                        filteredItems.add(new CityHeader(entry.getKey(), entry.getValue().size()));
                        filteredItems.addAll(entry.getValue());
                    }
                }
                break;

            // ... resto de casos sin cambios
            case POPULAR_DESTINATIONS:
                String headerText = "Destinos populares";
                if (searchLocation != null && !searchLocation.equals("Los más buscados")) {
                    String cityName = HotelGroupingUtils.extractCityFromLocation(searchLocation.toLowerCase());
                    if (cityName != null) {
                        String capitalizedCity = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);
                        headerText = "Destinos populares en " + capitalizedCity;
                    }
                }
                filteredItems.add(new CityHeader(headerText, sortedHotels.size()));
                filteredItems.addAll(sortedHotels);
                break;

            case NEARBY_HOTELS:
                String nearbyHeaderText = "Hoteles cercanos";
                if (searchLocation != null && !searchLocation.equals("Tu ciudad actual") &&
                        !searchLocation.equals(locationManager.getCurrentCity())) {
                    String cityName = HotelGroupingUtils.extractCityFromLocation(searchLocation.toLowerCase());
                    if (cityName != null) {
                        String capitalizedCity = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);
                        nearbyHeaderText = "Hoteles cercanos en " + capitalizedCity;
                    }
                }
                filteredItems.add(new CityHeader(nearbyHeaderText, sortedHotels.size()));
                filteredItems.addAll(sortedHotels);
                break;

            case SEARCH_RESULTS:
            case CITY_SPECIFIC:
            case LOCATION_FREE:
                for (Object item : originalGroupedItems) {
                    if (item instanceof CityHeader) {
                        CityHeader header = (CityHeader) item;
                        filteredItems.add(new CityHeader(header.getCityName(), sortedHotels.size()));
                        filteredItems.addAll(sortedHotels);
                        break;
                    }
                }
                break;
        }
    }

    private void removeFilter(String filterText) {
        if (originalGroupedItems != null) {
            adapter.updateItems(new ArrayList<>(originalGroupedItems));
            updateResultsCount();
            Log.d(TAG, "Filtro removido: " + filterText);
        }
    }

    private List<Hotel> extractHotels(List<Object> items) {
        List<Hotel> hotels = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Hotel) {
                hotels.add((Hotel) item);
            }
        }
        return hotels;
    }

    private void showFilterToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setupAdapter() {
        adapter = new GroupedHotelsAdapter(this, groupedItems);
        adapter.setOnHotelClickListener((hotel, position) -> {
            navigateToHotelDetail(hotel);
        });
        recyclerViewResults.setAdapter(adapter);

        if (recyclerViewResults.getItemAnimator() != null) {
            recyclerViewResults.getItemAnimator().setChangeDuration(250);
            recyclerViewResults.getItemAnimator().setMoveDuration(250);
            recyclerViewResults.getItemAnimator().setAddDuration(300);
            recyclerViewResults.getItemAnimator().setRemoveDuration(200);
        }

        Log.d(TAG, "Adapter configurado");
    }

    private void updateCompleteUI() {
        Log.d(TAG, "Actualizando UI completa - MANTENIENDO contexto original: " + originalContext);

        setupIntelligentHeader();

        if (isDetailsExpanded) {
            setupDetailedViews();
        }

        loadHotelsIntelligently();
        setupSmartFilters();

        if (tvSearchContext != null) {
            tvSearchContext.invalidate();
        }
        if (tvSearchSummaryLine != null) {
            tvSearchSummaryLine.invalidate();
        }
    }

    private void setupDetailedViews() {
        TextView tvLocationDetailed = findViewById(R.id.tv_location_detailed);
        TextView tvCheckInDetailed = findViewById(R.id.tv_check_in_detailed);
        TextView tvCheckOutDetailed = findViewById(R.id.tv_check_out_detailed);
        TextView tvGuestsDetailed = findViewById(R.id.tv_guests_detailed);
        LinearLayout btnModifySearch = findViewById(R.id.btn_modify_search);

        if (tvLocationDetailed != null) {
            tvLocationDetailed.setText(getLocationDisplayName());
        }

        if (tvCheckInDetailed != null && tvCheckOutDetailed != null) {
            if (hasSpecificDates()) {
                String[] dates = getDatesSummary().split(" - ");
                tvCheckInDetailed.setText(dates.length > 0 ? dates[0] : "Hoy");
                tvCheckOutDetailed.setText(dates.length > 1 ? dates[1] : "Mañana");
            } else {
                tvCheckInDetailed.setText("Hoy");
                tvCheckOutDetailed.setText("Mañana");
            }
        }

        if (tvGuestsDetailed != null) {
            tvGuestsDetailed.setText(getGuestsSummary());
        }

        if (btnModifySearch != null) {
            // ✅ USAR CONTEXTO ORIGINAL PARA RESTRICCIONES
            boolean canModifyLocation = originalContext.isLocationModifiable();

            if (canModifyLocation || originalContext.areDatesModifiable() || originalContext.areGuestsModifiable()) {
                btnModifySearch.setVisibility(View.VISIBLE);
                btnModifySearch.setOnClickListener(v -> {
                    v.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                v.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(100)
                                        .start();
                            })
                            .start();
                    openSearchModification();
                });
            } else {
                btnModifySearch.setVisibility(View.GONE);
            }
        }
    }

    private void openSearchModification() {
        Log.d(TAG, "Abriendo modificación de búsqueda");

        // ✅ USAR CONTEXTO ORIGINAL
        currentModifyDialog = new ModifySearchDialog(this, originalContext,
                searchLocation, searchDates, searchGuests);

        currentModifyDialog.setOnSearchModifiedListener((newLocation, newDates, newGuests) -> {
            Log.d(TAG, "Modificación recibida - Location: " + newLocation + ", Dates: " + newDates + ", Guests: " + newGuests);

            // Solo actualizar ubicación si se permite en el contexto ORIGINAL
            if (originalContext.isLocationModifiable()) {
                searchLocation = newLocation;
                // ✅ NO cambiar contexto, solo datos
            }

            searchDates = newDates;
            searchGuests = newGuests;
            updateCompleteUI();
        });

        if (originalContext.isLocationModifiable()) {
            currentModifyDialog.setOnLocationRequestListener(currentLocation -> {
                Intent intent = new Intent(this, LocationSelectorActivity.class);
                intent.putExtra("current_location", currentLocation);
                intent.putExtra("context_type", originalContext.name());
                startActivityForResult(intent, ModifySearchDialog.REQUEST_CODE_LOCATION);
            });
        }

        currentModifyDialog.show();
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        ivExpandDetails.setOnClickListener(v -> toggleDetailsVisibility());
    }

    private void toggleDetailsVisibility() {
        if (isDetailsExpanded) {
            cardSearchDetails.animate()
                    .alpha(0f)
                    .scaleY(0.8f)
                    .translationY(-cardSearchDetails.getHeight() / 2)
                    .setDuration(250)
                    .withEndAction(() -> cardSearchDetails.setVisibility(View.GONE))
                    .start();

            ivExpandDetails.animate()
                    .rotation(0)
                    .setDuration(250)
                    .start();

            isDetailsExpanded = false;
        } else {
            cardSearchDetails.setVisibility(View.VISIBLE);
            cardSearchDetails.setAlpha(0f);
            cardSearchDetails.setScaleY(0.8f);
            cardSearchDetails.setTranslationY(-cardSearchDetails.getHeight() / 2);

            cardSearchDetails.animate()
                    .alpha(1f)
                    .scaleY(1f)
                    .translationY(0)
                    .setDuration(250)
                    .start();

            ivExpandDetails.animate()
                    .rotation(180)
                    .setDuration(250)
                    .start();

            setupDetailedViews();
            isDetailsExpanded = true;
        }
    }

    private void updateResultsCount() {
        int totalCount = getTotalItemsCount();
        tvResultsCountInline.setText(String.valueOf(totalCount));
    }

    private int getTotalItemsCount() {
        int count = 0;
        for (Object item : groupedItems) {
            if (item instanceof Hotel) {
                count++;
            }
        }
        return count;
    }

    private void navigateToHotelDetail(Hotel hotel) {
        Intent intent = new Intent();
        intent.putExtra("hotel_name", hotel.getName());
        intent.putExtra("hotel_location", hotel.getLocation());
        intent.putExtra("hotel_price", hotel.getPrice());
        intent.putExtra("hotel_rating", hotel.getRating());
        intent.putExtra("hotel_image", hotel.getImageUrl());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentModifyDialog != null && currentModifyDialog.isShowing()) {
            currentModifyDialog.dismiss();
        }
        currentModifyDialog = null;
    }

    private List<Hotel> loadSampleHotels() {
        List<Hotel> hotels = new ArrayList<>();

        // LIMA (8 hoteles)
        hotels.add(new Hotel("Belmond Miraflores Park", "Miraflores, Lima", "belmond", "S/290", "4.9"));
        hotels.add(new Hotel("Westin Lima Hotel & Convention Center", "San Isidro, Lima", "belmond", "S/320", "4.7"));
        hotels.add(new Hotel("JW Marriott Hotel Lima", "Miraflores, Lima", "belmond", "S/380", "4.8"));
        hotels.add(new Hotel("Country Club Lima Hotel", "San Isidro, Lima", "belmond", "S/450", "4.9"));
        hotels.add(new Hotel("Hilton Lima Miraflores", "Miraflores, Lima", "belmond", "S/275", "4.6"));
        hotels.add(new Hotel("Swissotel Lima", "San Isidro, Lima", "belmond", "S/310", "4.7"));
        hotels.add(new Hotel("Hotel B", "Barranco, Lima", "belmond", "S/240", "4.8"));
        hotels.add(new Hotel("Costa del Sol Wyndham Lima", "Centro, Lima", "belmond", "S/180", "4.5"));

        // CUSCO (6 hoteles)
        hotels.add(new Hotel("Belmond Hotel Monasterio", "Centro Histórico, Cusco", "cuzco", "S/650", "4.9"));
        hotels.add(new Hotel("JW Marriott El Convento Cusco", "Centro Histórico, Cusco", "cuzco", "S/590", "4.8"));
        hotels.add(new Hotel("Palacio del Inka", "Centro Histórico, Cusco", "cuzco", "S/480", "4.7"));
        hotels.add(new Hotel("Aranwa Cusco Boutique Hotel", "Centro Histórico, Cusco", "cuzco", "S/420", "4.6"));
        hotels.add(new Hotel("Novotel Cusco", "Centro, Cusco", "cuzco", "S/280", "4.5"));
        hotels.add(new Hotel("Costa del Sol Ramada Cusco", "Centro, Cusco", "cuzco", "S/250", "4.4"));

        // AREQUIPA (4 hoteles)
        hotels.add(new Hotel("Casa Andina Premium Arequipa", "Centro, Arequipa", "arequipa", "S/320", "4.7"));
        hotels.add(new Hotel("Sonesta Posadas del Inca Arequipa", "Yanahuara, Arequipa", "arequipa", "S/280", "4.6"));
        hotels.add(new Hotel("Hampton by Hilton Arequipa", "Cayma, Arequipa", "arequipa", "S/240", "4.5"));
        hotels.add(new Hotel("Hotel Libertador Arequipa", "Centro, Arequipa", "arequipa", "S/200", "4.3"));

        // PIURA (3 hoteles)
        hotels.add(new Hotel("Arennas Mancora", "Máncora, Piura", "cuzco", "S/350", "4.8"));
        hotels.add(new Hotel("Casa Andina Select Tumbes", "Tumbes, Piura", "cuzco", "S/220", "4.4"));
        hotels.add(new Hotel("Costa del Sol Wyndham Tumbes", "Tumbes, Piura", "cuzco", "S/180", "4.2"));

        // AMAZONAS (2 hoteles)
        hotels.add(new Hotel("Gocta Lodge", "Chachapoyas, Amazonas", "gocta", "S/280", "4.8"));
        hotels.add(new Hotel("Casa Vieja Lodge", "Leymebamba, Amazonas", "gocta", "S/160", "4.5"));

        // MADRE DE DIOS (2 hoteles)
        hotels.add(new Hotel("Inkaterra Reserva Amazónica", "Tambopata, Madre de Dios", "inkaterra", "S/480", "4.9"));
        hotels.add(new Hotel("Corto Maltes Amazonia", "Puerto Maldonado, Madre de Dios", "inkaterra", "S/200", "4.4"));

        // VALLE SAGRADO (2 hoteles)
        hotels.add(new Hotel("Skylodge Adventure Suites", "Valle Sagrado, Cusco", "gocta", "S/680", "4.9"));
        hotels.add(new Hotel("Tambo del Inka Resort", "Valle Sagrado, Cusco", "gocta", "S/520", "4.8"));

        return hotels;
    }
}