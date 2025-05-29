package com.example.proyecto_final_hoteleros;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.adapters.HotelsResultsAdapter;
import com.example.proyecto_final_hoteleros.client.model.Hotel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class HotelResultsActivity extends AppCompatActivity {

    // Views
    private TextView tvSearchSummary;
    private LinearLayout layoutSearchParams;
    private TextView tvLocation, tvDates, tvGuests;
    private ChipGroup chipGroupFilters;
    private RecyclerView recyclerViewResults;
    private TextView tvResultsCount;
    private ImageView ivBack;

    // Datos
    private List<Hotel> allHotels;
    private List<Hotel> filteredHotels;
    private HotelsResultsAdapter adapter;

    // Parámetros de búsqueda
    private String searchLocation;
    private String searchDates;
    private String searchGuests;
    private String filterType; // "nearby", "popular", "city", "search"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_results);

        initViews();
        getSearchParameters();
        setupViews();
        loadHotels();
        setupFilters();
    }

    private void initViews() {
        tvSearchSummary = findViewById(R.id.tv_search_summary);
        layoutSearchParams = findViewById(R.id.layout_search_params);
        tvLocation = findViewById(R.id.tv_location);
        tvDates = findViewById(R.id.tv_dates);
        tvGuests = findViewById(R.id.tv_guests);
        chipGroupFilters = findViewById(R.id.chip_group_filters);
        recyclerViewResults = findViewById(R.id.recycler_view_results);
        tvResultsCount = findViewById(R.id.tv_results_count);
        ivBack = findViewById(R.id.iv_back);

        // Configurar RecyclerView
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getSearchParameters() {
        Intent intent = getIntent();
        searchLocation = intent.getStringExtra("location");
        searchDates = intent.getStringExtra("dates");
        searchGuests = intent.getStringExtra("guests");
        filterType = intent.getStringExtra("filter_type"); // "nearby", "popular", "city", "search"
    }

    private void setupViews() {
        // Configurar botón de regreso
        ivBack.setOnClickListener(v -> finish());

        // Configurar parámetros de búsqueda según el tipo
        switch (filterType) {
            case "search":
                setupCompleteSearch();
                break;
            case "nearby":
                setupNearbyFilter();
                break;
            case "popular":
                setupPopularFilter();
                break;
            case "city":
                setupCityFilter();
                break;
            default:
                setupDefaultView();
                break;
        }
    }

    private void setupCompleteSearch() {
        tvSearchSummary.setText("Resultados de búsqueda");
        layoutSearchParams.setVisibility(View.VISIBLE);

        // Mostrar todos los parámetros
        tvLocation.setText(searchLocation != null ? searchLocation : "Ubicación no especificada");
        tvDates.setText(searchDates != null ? searchDates : "Fechas no especificadas");
        tvGuests.setText(searchGuests != null ? searchGuests : "Huéspedes no especificados");

        // Agregar filtros adicionales
        addFilterChips(true, true, true); // Todos los filtros disponibles
    }

    private void setupNearbyFilter() {
        tvSearchSummary.setText("Hoteles cerca de ti");
        layoutSearchParams.setVisibility(View.GONE);

        // Solo mostrar filtros relevantes (sin "Cerca de ti" ya que está activo)
        addFilterChips(false, true, true); // Popular, precio, calificación

        // Activar chip "Cerca de ti" por defecto
        activateNearbyChip();
    }

    private void setupPopularFilter() {
        tvSearchSummary.setText("Hoteles populares");
        layoutSearchParams.setVisibility(View.GONE);

        // Solo mostrar filtros relevantes (sin "Popular" ya que está activo)
        addFilterChips(true, false, true); // Cerca de ti, precio, calificación

        // Activar chip "Popular" por defecto
        activatePopularChip();
    }

    private void setupCityFilter() {
        tvSearchSummary.setText("Hoteles en " + searchLocation);
        layoutSearchParams.setVisibility(View.VISIBLE);

        // Mostrar solo ubicación
        tvLocation.setText(searchLocation);
        tvDates.setVisibility(View.GONE);
        tvGuests.setVisibility(View.GONE);

        // Mostrar todos los filtros
        addFilterChips(true, true, true);
    }

    private void setupDefaultView() {
        tvSearchSummary.setText("Todos los hoteles");
        layoutSearchParams.setVisibility(View.GONE);
        addFilterChips(true, true, true);
    }

    private void addFilterChips(boolean includeNearby, boolean includePopular, boolean includeOthers) {
        chipGroupFilters.removeAllViews();

        if (includeNearby) {
            addChip("🏨 Cerca de ti", "nearby", false);
        }

        if (includePopular) {
            addChip("⭐ Popular", "popular", false);
        }

        if (includeOthers) {
            addChip("💰 Mejor precio", "price_low", false);
            addChip("💸 Mayor precio", "price_high", false);
            addChip("⭐ Mejor calificación", "rating", false);
            addChip("🏊 Más servicios", "services", false);
        }
    }

    private void addChip(String text, String tag, boolean isChecked) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setTag(tag);
        chip.setCheckable(true);
        chip.setChecked(isChecked);

        // Mejorar el estilo de los chips
        chip.setChipBackgroundColorResource(isChecked ? R.color.orange : R.color.light_gray);
        chip.setTextColor(getResources().getColor(isChecked ? android.R.color.white : android.R.color.black));
        chip.setChipStrokeWidth(0);
        chip.setTextSize(12);

        chip.setOnCheckedChangeListener((buttonView, isCheckedNow) -> {
            // Actualizar colores cuando cambie el estado
            chip.setChipBackgroundColorResource(isCheckedNow ? R.color.orange : R.color.light_gray);
            chip.setTextColor(getResources().getColor(isCheckedNow ? android.R.color.white : android.R.color.black));

            if (isCheckedNow) {
                applyFilter(tag);
            } else {
                removeFilter(tag);
            }
        });
        chipGroupFilters.addView(chip);
    }

    private void activateNearbyChip() {
        for (int i = 0; i < chipGroupFilters.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupFilters.getChildAt(i);
            if ("nearby".equals(chip.getTag())) {
                chip.setChecked(true);
                break;
            }
        }
    }

    private void activatePopularChip() {
        for (int i = 0; i < chipGroupFilters.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupFilters.getChildAt(i);
            if ("popular".equals(chip.getTag())) {
                chip.setChecked(true);
                break;
            }
        }
    }

    private void loadHotels() {
        // Aquí cargarías los hoteles desde tu fuente de datos
        // Por ahora uso datos de ejemplo
        allHotels = new ArrayList<>();

        // Agregar hoteles de ejemplo (puedes usar los mismos de HomeFragment)
        allHotels.add(new Hotel("Belmond Miraflores Park", "Miraflores, Lima", "drawable/belmond", "S/290", "4.9"));
        allHotels.add(new Hotel("Inkaterra Concepción", "Tambopata, Madre de Dios", "drawable/inkaterra", "S/300", "4.6"));
        allHotels.add(new Hotel("Skylodge", "Valle Sagrado, Cusco", "drawable/gocta", "S/310", "4.8"));
        allHotels.add(new Hotel("Arennas Máncora", "Máncora, Piura", "drawable/cuzco", "S/275", "4.7"));
        allHotels.add(new Hotel("Pariwana Lima", "Cercado, Lima", "drawable/arequipa", "S/320", "4.9"));
        allHotels.add(new Hotel("Gocta Lodge", "Chachapoyas, Amazonas", "drawable/gocta", "S/280", "4.8"));

        // Filtrar según el tipo inicial
        applyInitialFilter();

        // Configurar adapter
        adapter = new HotelsResultsAdapter(this, filteredHotels);
        adapter.setOnHotelClickListener((hotel, position) -> {
            // Navegar al detalle del hotel
            navigateToHotelDetail(hotel);
        });
        recyclerViewResults.setAdapter(adapter);

        updateResultsCount();
    }

    private void applyInitialFilter() {
        filteredHotels = new ArrayList<>();

        switch (filterType) {
            case "nearby":
                // Filtrar hoteles cercanos (por ejemplo, los que están en Lima)
                for (Hotel hotel : allHotels) {
                    if (hotel.getLocation().contains("Lima")) {
                        filteredHotels.add(hotel);
                    }
                }
                break;

            case "popular":
                // Filtrar hoteles populares (por ejemplo, con calificación >= 4.8)
                for (Hotel hotel : allHotels) {
                    if (Float.parseFloat(hotel.getRating()) >= 4.8) {
                        filteredHotels.add(hotel);
                    }
                }
                break;

            case "city":
                // Filtrar por ciudad seleccionada
                for (Hotel hotel : allHotels) {
                    if (hotel.getLocation().contains(searchLocation)) {
                        filteredHotels.add(hotel);
                    }
                }
                break;

            default:
                // Mostrar todos los hoteles
                filteredHotels.addAll(allHotels);
                break;
        }
    }

    private void applyFilter(String filterTag) {
        // Aquí implementarías la lógica de filtrado
        // Por ejemplo:
        switch (filterTag) {
            case "nearby":
                filterByProximity();
                break;
            case "popular":
                filterByPopularity();
                break;
            case "price_low":
                filterByPriceLowToHigh();
                break;
            case "price_high":
                filterByPriceHighToLow();
                break;
            case "rating":
                filterByRating();
                break;
            case "services":
                filterByServices();
                break;
        }

        adapter.updateHotels(filteredHotels);
        updateResultsCount();
    }

    private void removeFilter(String filterTag) {
        // Recargar la lista base y aplicar filtros activos
        applyInitialFilter();

        // Reaplicar otros filtros activos
        for (int i = 0; i < chipGroupFilters.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupFilters.getChildAt(i);
            if (chip.isChecked() && !filterTag.equals(chip.getTag())) {
                applyFilter((String) chip.getTag());
            }
        }
    }

    private void filterByProximity() {
        // Implementar filtro por proximidad
        filteredHotels.clear();
        for (Hotel hotel : allHotels) {
            if (hotel.getLocation().contains("Lima")) {
                filteredHotels.add(hotel);
            }
        }
    }

    private void filterByPopularity() {
        // Implementar filtro por popularidad
        filteredHotels.clear();
        for (Hotel hotel : allHotels) {
            if (Float.parseFloat(hotel.getRating()) >= 4.8) {
                filteredHotels.add(hotel);
            }
        }
    }

    private void filterByPriceLowToHigh() {
        // Ordenar por precio de menor a mayor
        filteredHotels.sort((h1, h2) -> {
            int price1 = Integer.parseInt(h1.getPrice().replace("S/", ""));
            int price2 = Integer.parseInt(h2.getPrice().replace("S/", ""));
            return Integer.compare(price1, price2);
        });
    }

    private void filterByPriceHighToLow() {
        // Ordenar por precio de mayor a menor
        filteredHotels.sort((h1, h2) -> {
            int price1 = Integer.parseInt(h1.getPrice().replace("S/", ""));
            int price2 = Integer.parseInt(h2.getPrice().replace("S/", ""));
            return Integer.compare(price2, price1);
        });
    }

    private void filterByRating() {
        // Ordenar por calificación de mayor a menor
        filteredHotels.sort((h1, h2) -> Float.compare(Float.parseFloat(h2.getRating()), Float.parseFloat(h1.getRating())));
    }

    private void filterByServices() {
        // Aquí podrías filtrar por cantidad de servicios
        // Por ahora solo como ejemplo
    }

    private void updateResultsCount() {
        tvResultsCount.setText(filteredHotels.size() + " hoteles encontrados");
    }

    private void setupFilters() {
        // Configurar listeners para los filtros adicionales si los tienes
        // Por ejemplo, si tienes un botón de "Ordenar por"
        TextView tvSortBy = findViewById(R.id.tv_sort_by);
        if (tvSortBy != null) {
            tvSortBy.setOnClickListener(v -> {
                // Mostrar opciones de ordenamiento
                showSortOptions();
            });
        }
    }

    private void showSortOptions() {
        // Implementar diálogo de opciones de ordenamiento
        // Por ahora solo un toast de ejemplo
        android.widget.Toast.makeText(this, "Opciones de ordenamiento", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void navigateToHotelDetail(Hotel hotel) {
        // Implementar navegación al detalle del hotel
        // Similar a como lo tienes en HomeFragment
        android.content.Intent intent = new android.content.Intent(this, com.example.proyecto_final_hoteleros.client.fragment.HotelDetailFragment.class);
        intent.putExtra("hotel_name", hotel.getName());
        intent.putExtra("hotel_location", hotel.getLocation());
        intent.putExtra("hotel_price", hotel.getPrice());
        intent.putExtra("hotel_rating", hotel.getRating());
        intent.putExtra("hotel_image", hotel.getImageUrl());
        startActivity(intent);
    }
}