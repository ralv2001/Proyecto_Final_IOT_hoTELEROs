package com.example.proyecto_final_hoteleros.client.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.adapters.HotelsResultsAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.Hotel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class HotelResultsActivity extends AppCompatActivity {

    // Variables para las vistas
    private TextView tvSearchSummary;
    private LinearLayout layoutSearchParams;
    private TextView tvLocation, tvCheckInDate, tvCheckInDay, tvCheckOutDate, tvCheckOutDay, tvGuests, tvStayDuration;
    private ChipGroup chipGroupFilters;
    private RecyclerView recyclerViewResults;
    private TextView tvResultsCount;
    private ImageView ivBack, ivFilter;

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
        tvCheckInDate = findViewById(R.id.tv_check_in_date);
        tvCheckInDay = findViewById(R.id.tv_check_in_day);
        tvCheckOutDate = findViewById(R.id.tv_check_out_date);
        tvCheckOutDay = findViewById(R.id.tv_check_out_day);
        tvGuests = findViewById(R.id.tv_guests);
        tvStayDuration = findViewById(R.id.tv_stay_duration);
        chipGroupFilters = findViewById(R.id.chip_group_filters);
        recyclerViewResults = findViewById(R.id.recycler_view_results);
        tvResultsCount = findViewById(R.id.tv_results_count);
        ivBack = findViewById(R.id.iv_back);
        ivFilter = findViewById(R.id.iv_filter);

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

        // Configurar botón de filtro (la flecha hacia abajo) con animación suave
        ivFilter.setOnClickListener(v -> {
            if (layoutSearchParams.getVisibility() == View.VISIBLE) {
                // Ocultar con animación
                layoutSearchParams.animate()
                        .alpha(0f)
                        .translationY(-layoutSearchParams.getHeight())
                        .setDuration(300)
                        .withEndAction(() -> layoutSearchParams.setVisibility(View.GONE))
                        .start();

                // Rotar flecha hacia abajo
                ivFilter.animate().rotation(0).setDuration(300).start();
            } else {
                // Mostrar con animación
                layoutSearchParams.setVisibility(View.VISIBLE);
                layoutSearchParams.setAlpha(0f);
                layoutSearchParams.setTranslationY(-layoutSearchParams.getHeight());
                layoutSearchParams.animate()
                        .alpha(1f)
                        .translationY(0)
                        .setDuration(300)
                        .start();

                // Rotar flecha hacia arriba
                ivFilter.animate().rotation(180).setDuration(300).start();
            }
        });

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

        // Mostrar todos los parámetros con formato mejorado
        tvLocation.setText(searchLocation != null ? searchLocation : "Ubicación no especificada");

        // Procesar y mostrar fechas de forma profesional
        if (searchDates != null && !searchDates.isEmpty()) {
            String[] dates = searchDates.split("–");
            if (dates.length == 2) {
                tvCheckInDate.setText(dates[0].trim());
                tvCheckOutDate.setText(dates[1].trim());
                tvCheckInDay.setText("Lunes"); // Podrías calcular el día real
                tvCheckOutDay.setText("Sábado"); // Podrías calcular el día real

                // Calcular duración
                tvStayDuration.setText("5 noches"); // Podrías calcular las noches reales
            }
        }

        tvGuests.setText(searchGuests != null ? searchGuests : "Huéspedes no especificados");

        // Agregar filtros adicionales
        addFilterChips(true, true, true); // Todos los filtros disponibles
    }

    private void setupNearbyFilter() {
        tvSearchSummary.setText("Hoteles cerca de ti");
        layoutSearchParams.setVisibility(View.VISIBLE);

        // Configurar valores por defecto para "Ver todo"
        tvLocation.setText("Todas las ubicaciones");
        setupDefaultDatesAndGuests();

        // Solo mostrar filtros relevantes (sin "Cerca de ti" ya que está activo)
        addFilterChips(false, true, true); // Popular, precio, calificación

        // Activar chip "Cerca de ti" por defecto
        // activateNearbyChip(); // Se activará automáticamente en addFilterChips
    }

    private void setupPopularFilter() {
        tvSearchSummary.setText("Hoteles populares");
        layoutSearchParams.setVisibility(View.VISIBLE);

        // Configurar valores por defecto para "Ver todo"
        tvLocation.setText("Todas las ubicaciones");
        setupDefaultDatesAndGuests();

        // Solo mostrar filtros relevantes (sin "Popular" ya que está activo)
        addFilterChips(true, false, true); // Cerca de ti, precio, calificación

        // Activar chip "Popular" por defecto
        // activatePopularChip(); // Se activará automáticamente en addFilterChips
    }

    private void setupDefaultDatesAndGuests() {
        // Configurar fechas por defecto (hoy + 2 días)
        java.util.Calendar today = java.util.Calendar.getInstance();
        java.util.Calendar checkout = java.util.Calendar.getInstance();
        checkout.add(java.util.Calendar.DAY_OF_MONTH, 2);

        java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("dd MMM", new java.util.Locale("es", "ES"));
        java.text.SimpleDateFormat dayNameFormat = new java.text.SimpleDateFormat("EEEE", new java.util.Locale("es", "ES"));

        tvCheckInDate.setText(dayFormat.format(today.getTime()));
        tvCheckInDay.setText(capitalize(dayNameFormat.format(today.getTime())));
        tvCheckOutDate.setText(dayFormat.format(checkout.getTime()));
        tvCheckOutDay.setText(capitalize(dayNameFormat.format(checkout.getTime())));

        // Configurar huéspedes por defecto
        tvGuests.setText("2 adultos · 0 niños");

        // Duración de estadía
        tvStayDuration.setText("2 noches");
    }

    private String capitalize(String str) {
        if (str == null || str.length() == 0) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private void setupCityFilter() {
        tvSearchSummary.setText("Hoteles en " + searchLocation);
        layoutSearchParams.setVisibility(View.VISIBLE);

        // Mostrar solo ubicación
        tvLocation.setText(searchLocation);

        // Ocultar fechas y huéspedes para búsqueda por ciudad
        findViewById(R.id.layout_dates).setVisibility(View.GONE);
        findViewById(R.id.layout_guests).setVisibility(View.GONE);
        tvStayDuration.setVisibility(View.GONE);

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

        // Mejorar el estilo de los chips con diseño premium
        if (isChecked) {
            chip.setChipBackgroundColorResource(R.color.orange);
            chip.setTextColor(getResources().getColor(android.R.color.white));
            chip.setChipStrokeWidth(0);
        } else {
            chip.setChipBackgroundColor(getResources().getColorStateList(android.R.color.white));
            chip.setTextColor(getResources().getColor(android.R.color.black));
            chip.setChipStrokeWidth(2);
            chip.setChipStrokeColor(getResources().getColorStateList(R.color.light_gray));
        }

        // Configurar padding y tamaño
        chip.setChipMinHeight(44);
        chip.setTextSize(13);
        chip.setTypeface(null, android.graphics.Typeface.BOLD);

        chip.setOnCheckedChangeListener((buttonView, isCheckedNow) -> {
            // Actualizar colores cuando cambie el estado
            if (isCheckedNow) {
                chip.setChipBackgroundColorResource(R.color.orange);
                chip.setTextColor(getResources().getColor(android.R.color.white));
                chip.setChipStrokeWidth(0);
            } else {
                chip.setChipBackgroundColor(getResources().getColorStateList(android.R.color.white));
                chip.setTextColor(getResources().getColor(android.R.color.black));
                chip.setChipStrokeWidth(2);
                chip.setChipStrokeColor(getResources().getColorStateList(R.color.light_gray));
            }

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
        // Configurar listener para el botón de ordenar
        LinearLayout tvSortBy = findViewById(R.id.tv_sort_by);
        if (tvSortBy != null) {
            tvSortBy.setOnClickListener(v -> {
                showSortOptions();
            });
        }
    }

    private void showSortOptions() {
        String[] sortOptions = {
                "⭐ Mejor calificación",
                "💰 Precio: menor a mayor",
                "💸 Precio: mayor a menor",
                "🔤 Nombre A-Z",
                "📍 Más cercanos"
        };

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Ordenar por")
                .setItems(sortOptions, (dialog, which) -> {
                    switch (which) {
                        case 0: // Mejor calificación
                            filterByRating();
                            showToast("Ordenado por mejor calificación");
                            break;
                        case 1: // Precio menor a mayor
                            filterByPriceLowToHigh();
                            showToast("Ordenado por precio: menor a mayor");
                            break;
                        case 2: // Precio mayor a menor
                            filterByPriceHighToLow();
                            showToast("Ordenado por precio: mayor a menor");
                            break;
                        case 3: // Nombre A-Z
                            sortByName();
                            showToast("Ordenado alfabéticamente");
                            break;
                        case 4: // Más cercanos
                            filterByProximity();
                            showToast("Ordenado por proximidad");
                            break;
                    }
                    adapter.updateHotels(filteredHotels);
                    updateResultsCount();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void sortByName() {
        filteredHotels.sort((h1, h2) -> h1.getName().compareToIgnoreCase(h2.getName()));
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    private void navigateToHotelDetail(Hotel hotel) {
        // Navegar al HotelDetailFragment (no Activity)
        android.content.Intent intent = new android.content.Intent();
        intent.putExtra("hotel_name", hotel.getName());
        intent.putExtra("hotel_location", hotel.getLocation());
        intent.putExtra("hotel_price", hotel.getPrice());
        intent.putExtra("hotel_rating", hotel.getRating());
        intent.putExtra("hotel_image", hotel.getImageUrl());

        // Devolver resultado para que HomeActivity maneje la navegación al fragment
        setResult(RESULT_OK, intent);
        finish();
    }
}