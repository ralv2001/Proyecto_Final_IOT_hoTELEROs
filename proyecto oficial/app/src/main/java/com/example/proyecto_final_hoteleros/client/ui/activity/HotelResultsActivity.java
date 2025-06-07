package com.example.proyecto_final_hoteleros.client.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class HotelResultsActivity extends AppCompatActivity {

    // Views
    private TextView tvSearchContext;
    private TextView tvSearchSummaryLine;
    private TextView tvResultsCountInline;
    private ImageView ivBack;
    private ImageView ivExpandDetails;
    private CardView cardSearchDetails;
    private ChipGroup chipGroupQuickFilters;
    private RecyclerView recyclerViewResults; // ✅ ESTA ES LA QUE TE FALTA

    // Data
    private List<Hotel> allHotels;
    private List<Object> groupedItems;
    private GroupedHotelsAdapter adapter;

    // Search parameters
    private SearchContext currentContext;
    private String searchLocation;
    private String searchDates;
    private String searchGuests;
    private String filterType;
    private boolean isDetailsExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_activity_hotel_results);

        initViews();
        getSearchParameters();
        determineSearchContext();
        setupIntelligentHeader();
        loadHotelsIntelligently();
        setupSmartFilters();
        setupListeners();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ModifySearchDialog.REQUEST_CODE_LOCATION && resultCode == RESULT_OK) {
            if (data != null) {
                String selectedLocation = data.getStringExtra("selected_location");
                if (selectedLocation != null) {
                    // Actualizar la ubicación actual
                    searchLocation = selectedLocation;

                    // Determinar nuevo contexto según la ubicación seleccionada
                    updateSearchContextForLocation(selectedLocation);

                    // Recargar header y resultados
                    setupIntelligentHeader();
                    loadHotelsIntelligently();

                    // Mostrar feedback
                    showFilterToast("Ubicación actualizada: " + selectedLocation);
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

    private void updateSearchContextForLocation(String location) {
        String locationLower = location.toLowerCase();

        if (locationLower.contains("lima") || locationLower.contains("miraflores") ||
                locationLower.contains("san isidro") || locationLower.contains("barranco")) {
            currentContext = SearchContext.NEARBY_HOTELS;
        } else if (isPopularDestination(locationLower)) {
            currentContext = SearchContext.POPULAR_DESTINATIONS;
        } else {
            currentContext = SearchContext.CITY_SPECIFIC;
        }
    }

    private boolean isPopularDestination(String location) {
        String[] popularDestinations = {"cusco", "machu picchu", "arequipa", "máncora", "paracas", "iquitos"};
        for (String destination : popularDestinations) {
            if (location.contains(destination)) {
                return true;
            }
        }
        return false;
    }
    private void getSearchParameters() {
        Intent intent = getIntent();
        searchLocation = intent.getStringExtra("location");
        searchDates = intent.getStringExtra("dates");
        searchGuests = intent.getStringExtra("guests");
        filterType = intent.getStringExtra("filter_type");
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
    }

    // Método mejorado para configurar header inteligente
    private void setupIntelligentHeader() {
        // ✅ APLICAR VALORES POR DEFECTO SOLO SI SON NULOS
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

        // ✅ CONTEXTO INTELIGENTE con modificabilidad
        String contextTitle = currentContext.getFormattedTitle(getLocationDisplayName());

        // ✅ AGREGAR INDICADOR SI HAY RESTRICCIONES
        if (!currentContext.isLocationModifiable() || !currentContext.areDatesModifiable() || !currentContext.areGuestsModifiable()) {
            contextTitle += " 🔒"; // Indicador de opciones fijas
        }

        tvSearchContext.setText(contextTitle);

        // Resumen en una línea
        String summaryLine = buildIntelligentSummary();
        tvSearchSummaryLine.setText(summaryLine);
    }

    private String buildIntelligentSummary() {
        StringBuilder summary = new StringBuilder();

        // Ubicación inteligente
        String locationText = getLocationDisplayName();
        summary.append(locationText);

        // Fechas (solo si es relevante)
        if (hasSpecificDates()) {
            summary.append(" • ").append(getDatesSummary());
        }

        // Huéspedes (solo si no es default)
        if (hasNonDefaultGuests()) {
            summary.append(" • ").append(getGuestsSummary());
        }

        return summary.toString();
    }

    private String getLocationDisplayName() {
        switch (currentContext) {
            case ALL_DESTINATIONS:
                return SearchTerminology.ALL_DESTINATIONS;
            case NEARBY_HOTELS:
                return SearchTerminology.METROPOLITAN_AREA;
            case LOCATION_FREE:
                return SearchTerminology.LOCATION_FREE;
            case CITY_SPECIFIC:
                return searchLocation != null ? searchLocation : "Ciudad seleccionada";
            case POPULAR_DESTINATIONS:
                return SearchTerminology.CURATED_SELECTION;
            case SEARCH_RESULTS:
                return SearchTerminology.getContextualLocationText(searchLocation);
            default:
                return SearchTerminology.ALL_DESTINATIONS;
        }
    }

    private boolean hasSpecificDates() {
        return searchDates != null && !searchDates.trim().isEmpty();
    }

    private boolean hasNonDefaultGuests() {
        return searchGuests != null && !searchGuests.equals("2 adultos · 0 niños");
    }

    private String getDatesSummary() {
        if (searchDates == null) return "Fechas flexibles";

        // Procesar formato de fechas
        if (searchDates.contains("–")) {
            String[] dates = searchDates.split("–");
            if (dates.length == 2) {
                return dates[0].trim() + " - " + dates[1].trim();
            }
        }

        return searchDates;
    }

    // ✅ Método mejorado para resumen de huéspedes con valores por defecto
    private String getGuestsSummary() {
        if (searchGuests == null || searchGuests.trim().isEmpty()) {
            return "2 adultos"; // Valor por defecto específico
        }
        return searchGuests;
    }

    private void loadHotelsIntelligently() {
        // Cargar datos base
        allHotels = loadSampleHotels();

        switch (currentContext) {
            case ALL_DESTINATIONS:
                loadAllDestinationsGrouped();
                break;
            case NEARBY_HOTELS:
                loadNearbyHotels();
                break;
            case CITY_SPECIFIC:
                loadCitySpecificHotels();
                break;
            case POPULAR_DESTINATIONS:
                loadPopularDestinations();
                break;
            case LOCATION_FREE:
                loadLocationFreeResults();
                break;
            case SEARCH_RESULTS:
                loadSearchResults();
                break;
        }

        setupAdapter();
        updateResultsCount();
    }

    private void loadAllDestinationsGrouped() {
        groupedItems = HotelGroupingUtils.groupHotelsByCity(allHotels);
    }


    private void loadNearbyHotels() {
        // ✅ CORREGIDO: Filtrar hoteles cercanos y convertir a lista plana
        List<Hotel> nearbyHotels = HotelGroupingUtils.filterHotelsByProximity(allHotels, "Lima");
        groupedItems = new ArrayList<>();

        if (!nearbyHotels.isEmpty()) {
            // Agregar header
            groupedItems.add(new CityHeader("Hoteles cercanos", nearbyHotels.size()));
            // Agregar hoteles
            groupedItems.addAll(nearbyHotels);
        } else {
            // Si no hay hoteles cercanos, mostrar algunos de Lima
            List<Hotel> limaHotels = HotelGroupingUtils.getHotelsForCity(allHotels, "Lima");
            if (!limaHotels.isEmpty()) {
                groupedItems.add(new CityHeader("Área metropolitana", limaHotels.size()));
                groupedItems.addAll(limaHotels);
            }
        }
    }

    private void loadCitySpecificHotels() {
        List<Hotel> cityHotels = HotelGroupingUtils.getHotelsForCity(allHotels, searchLocation);
        groupedItems = new ArrayList<>();

        if (!cityHotels.isEmpty()) {
            groupedItems.add(new CityHeader(searchLocation, cityHotels.size()));
            groupedItems.addAll(cityHotels);
        } else {
            // Si no se encuentra la ciudad específica, mostrar mensaje
            groupedItems.add(new CityHeader("No se encontraron hoteles en " + searchLocation, 0));
        }
    }

    private void loadPopularDestinations() {
        // ✅ CORREGIDO: Filtrar hoteles populares
        List<Hotel> popularHotels = HotelGroupingUtils.filterPopularHotels(allHotels);
        groupedItems = new ArrayList<>();

        if (!popularHotels.isEmpty()) {
            groupedItems.add(new CityHeader("Destinos populares", popularHotels.size()));
            groupedItems.addAll(popularHotels);
        } else {
            // Fallback: mostrar hoteles con rating >= 4.0
            List<Hotel> goodHotels = new ArrayList<>();
            for (Hotel hotel : allHotels) {
                try {
                    float rating = Float.parseFloat(hotel.getRating());
                    if (rating >= 4.0) {
                        goodHotels.add(hotel);
                    }
                } catch (NumberFormatException e) {
                    // Ignorar
                }
            }
            if (!goodHotels.isEmpty()) {
                groupedItems.add(new CityHeader("Hoteles recomendados", goodHotels.size()));
                groupedItems.addAll(goodHotels);
            }
        }
    }

    private void loadLocationFreeResults() {
        groupedItems = HotelGroupingUtils.groupHotelsByCity(allHotels);
    }

    private void loadSearchResults() {
        // ✅ MEJORADO: Búsqueda más inteligente
        List<Hotel> searchResults = new ArrayList<>();

        if (searchLocation != null && !searchLocation.trim().isEmpty()) {
            String searchTerm = searchLocation.toLowerCase().trim();

            for (Hotel hotel : allHotels) {
                String hotelName = hotel.getName().toLowerCase();
                String hotelLocation = hotel.getLocation().toLowerCase();

                // Buscar en nombre y ubicación
                if (hotelName.contains(searchTerm) || hotelLocation.contains(searchTerm)) {
                    searchResults.add(hotel);
                }
            }
        } else {
            searchResults = allHotels;
        }

        groupedItems = new ArrayList<>();
        if (!searchResults.isEmpty()) {
            groupedItems.add(new CityHeader("Resultados de búsqueda", searchResults.size()));
            groupedItems.addAll(searchResults);
        } else {
            groupedItems.add(new CityHeader("No se encontraron resultados", 0));
        }
    }

    private void setupAdapter() {
        adapter = new GroupedHotelsAdapter(this, groupedItems);

        adapter.setOnHotelClickListener((hotel, position) -> {
            navigateToHotelDetail(hotel);
        });

        recyclerViewResults.setAdapter(adapter);

        // ✅ CONFIGURAR ANIMACIONES SUAVES
        if (recyclerViewResults.getItemAnimator() != null) {
            recyclerViewResults.getItemAnimator().setChangeDuration(250);
            recyclerViewResults.getItemAnimator().setMoveDuration(250);
            recyclerViewResults.getItemAnimator().setAddDuration(300);
            recyclerViewResults.getItemAnimator().setRemoveDuration(200);
        }

        // ✅ ANIMACIÓN DE ENTRADA MEJORADA
        recyclerViewResults.setAlpha(0f);
        recyclerViewResults.setTranslationY(50f);
        recyclerViewResults.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(200)
                .start();
    }
    private void setupDetailedViews() {
        TextView tvLocationDetailed = findViewById(R.id.tv_location_detailed);
        TextView tvCheckInDetailed = findViewById(R.id.tv_check_in_detailed);
        TextView tvCheckOutDetailed = findViewById(R.id.tv_check_out_detailed);
        TextView tvGuestsDetailed = findViewById(R.id.tv_guests_detailed);
        LinearLayout btnModifySearch = findViewById(R.id.btn_modify_search);

        // Configurar valores con defaults inteligentes
        tvLocationDetailed.setText(getLocationDisplayName());

        if (hasSpecificDates()) {
            String[] dates = getDatesSummary().split(" - ");
            tvCheckInDetailed.setText(dates.length > 0 ? dates[0] : "Hoy");
            tvCheckOutDetailed.setText(dates.length > 1 ? dates[1] : "Mañana");
        } else {
            // ✅ Usar valores por defecto más naturales
            tvCheckInDetailed.setText("Hoy");
            tvCheckOutDetailed.setText("Mañana");
        }

        tvGuestsDetailed.setText(getGuestsSummary());

        // ✅ NUEVO: Verificar si la modificación está disponible según contexto
        if (currentContext.isLocationModifiable() || currentContext.areDatesModifiable() ||
                currentContext.areGuestsModifiable()) {
            btnModifySearch.setVisibility(View.VISIBLE);
            btnModifySearch.setOnClickListener(v -> {
                // Animación de feedback
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

    private void openSearchModification() {
        ModifySearchDialog dialog = new ModifySearchDialog(this, currentContext,
                searchLocation, searchDates, searchGuests);

        dialog.setOnSearchModifiedListener(new ModifySearchDialog.OnSearchModifiedListener() {
            @Override
            public void onSearchModified(String newLocation, String newDates, String newGuests) {
                // Actualizar parámetros solo si son modificables
                if (currentContext.isLocationModifiable()) {
                    searchLocation = newLocation;
                }
                if (currentContext.areDatesModifiable()) {
                    searchDates = newDates;
                }
                if (currentContext.areGuestsModifiable()) {
                    searchGuests = newGuests;
                }

                // Recargar resultados
                setupIntelligentHeader();
                loadHotelsIntelligently();

                // Mostrar feedback
                showFilterToast("Búsqueda actualizada");
            }
        });

        dialog.show();
    }



    private void setupSmartFilters() {
        chipGroupQuickFilters.removeAllViews();

        // Filtros contextuales inteligentes
        String[] filterOptions = getContextualFilters();

        for (String filterText : filterOptions) {
            addFilterChip(filterText);
        }
    }

    private String[] getContextualFilters() {
        switch (currentContext) {
            case ALL_DESTINATIONS:
                return new String[]{"🏨 Por ciudad", "⭐ Mejor valorados", "💰 Mejor precio"};
            case NEARBY_HOTELS:
                return new String[]{"📍 Más cercanos", "🚗 Con parking", "⭐ Recomendados"};
            case CITY_SPECIFIC:
                return new String[]{"🌟 Populares", "💰 Económicos", "🏪 Centro", "🏖️ Costa"};
            case LOCATION_FREE:
                return new String[]{"🌎 Por región", "⭐ Destacados", "🎯 Ofertas"};
            case POPULAR_DESTINATIONS:
                return new String[]{"⭐ Top rated", "💎 Lujo", "🏆 Premiados"};
            case SEARCH_RESULTS:
                return new String[]{"🔍 Relevancia", "📍 Distancia", "💰 Precio"};
            default:
                return new String[]{"⭐ Populares", "💰 Precio", "📍 Ubicación"};
        }
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
                chip.setChipBackgroundColorResource(R.color.orange);
                chip.setTextColor(getResources().getColor(android.R.color.white));
                chip.setChipStrokeWidth(0);
                applyFilter(text);
            } else {
                chip.setChipBackgroundColorResource(android.R.color.white);
                chip.setTextColor(getResources().getColor(android.R.color.black));
                chip.setChipStrokeWidth(2);
                removeFilter(text);
            }
        });

        chipGroupQuickFilters.addView(chip);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        ivExpandDetails.setOnClickListener(v -> toggleDetailsVisibility());
    }

    private void toggleDetailsVisibility() {
        if (isDetailsExpanded) {
            // COLAPSAR CON ANIMACIÓN MEJORADA
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
            // EXPANDIR CON ANIMACIÓN MEJORADA
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

            // Configurar views detallados
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

    private void applyFilter(String filterText) {
        List<Object> filteredItems = new ArrayList<>();

        if (filterText.contains("💰 Mejor precio")) {
            // Ordenar por precio
            List<Hotel> hotels = extractHotels(groupedItems);
            hotels.sort((h1, h2) -> {
                int price1 = extractPrice(h1.getPrice());
                int price2 = extractPrice(h2.getPrice());
                return Integer.compare(price1, price2);
            });
            filteredItems.addAll(hotels);
        } else if (filterText.contains("⭐ Mejor valorados")) {
            // Ordenar por rating
            List<Hotel> hotels = extractHotels(groupedItems);
            hotels.sort((h1, h2) -> Float.compare(Float.parseFloat(h2.getRating()), Float.parseFloat(h1.getRating())));
            filteredItems.addAll(hotels);
        } else if (filterText.contains("🏨 Por ciudad")) {
            // Mantener agrupación por ciudad
            filteredItems = new ArrayList<>(groupedItems);
        } else {
            // Filtro por defecto
            filteredItems = new ArrayList<>(groupedItems);
        }

        adapter.updateItems(filteredItems);
        updateResultsCount();

        // Mostrar feedback
        showFilterToast("Filtro aplicado: " + filterText.substring(2));
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

    private int extractPrice(String priceText) {
        try {
            return Integer.parseInt(priceText.replace("S/", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showFilterToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
    private void removeFilter(String filterText) {
        // Remover filtro aplicado
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