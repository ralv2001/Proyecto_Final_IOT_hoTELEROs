package com.example.proyecto_final_hoteleros.client.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;


import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adapters.CitiesAdapter;
import com.example.proyecto_final_hoteleros.adapters.HotelImageAdapter;
import com.example.proyecto_final_hoteleros.client.model.City;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HotelDetailFragment extends Fragment {

    private ViewPager2 viewPagerImages;
    private TabLayout tabLayoutIndicator;
    private RecyclerView rvNearbyPlaces;
    private TextView tvHotelName, tvHotelLocation, tvHotelPrice, tvPriceDescription;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hotel_selection, container, false);

        // Inicializar vistas
        viewPagerImages = view.findViewById(R.id.view_pager_hotel_images);
        tabLayoutIndicator = view.findViewById(R.id.tab_layout_indicator);
        rvNearbyPlaces = view.findViewById(R.id.rv_nearby_places);
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        tvHotelLocation = view.findViewById(R.id.tv_hotel_location);
        tvHotelPrice = view.findViewById(R.id.tv_hotel_price);
        tvPriceDescription = view.findViewById(R.id.tv_price_description);


        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener argumentos si existen
        if (getArguments() != null) {
            String hotelName = getArguments().getString("hotel_name", "Belmond Miraflores Park");
            String hotelLocation = getArguments().getString("hotel_location", "Miraflores, frente al malecón, Lima");
            String hotelPrice = getArguments().getString("hotel_price", "S/2,050");
            String hotelRating = getArguments().getString("hotel_rating", "4.7");

            // Actualizar la UI con los datos
            tvHotelName.setText(hotelName);
            tvHotelLocation.setText(hotelLocation);
            tvHotelPrice.setText(hotelPrice);
        }

        // Configurar galería de imágenes
        setupImageGallery();

        // Configurar lugares turísticos cercanos
        setupNearbyPlaces();

        // Configurar botones y eventos
        setupActions(view);
    }
    private void setupImageGallery() {
        // Lista de imágenes para la galería (deberías usar tus propias imágenes)
        List<Integer> images = Arrays.asList(
                R.drawable.belmond,
                R.drawable.belmond,
                R.drawable.belmond,
                R.drawable.belmond
        );

        // Configurar adaptador del ViewPager
        HotelImageAdapter adapter = new HotelImageAdapter(images);
        viewPagerImages.setAdapter(adapter);

        // Configurar indicadores con TabLayout
        new TabLayoutMediator(tabLayoutIndicator, viewPagerImages,
                (tab, position) -> {
                    // No necesitas hacer nada aquí, solo crear los tabs
                }
        ).attach();
    }

    private void setupNearbyPlaces() {
        // Crear datos de ejemplo para lugares cercanos
        List<City> nearbyPlaces = new ArrayList<>();
        nearbyPlaces.add(new City("Parque del Amor", R.drawable.lima));
        nearbyPlaces.add(new City("Larcomar", R.drawable.lima));
        nearbyPlaces.add(new City("Kennedy Park", R.drawable.lima));

        // Configurar RecyclerView
        CitiesAdapter adapter = new CitiesAdapter(nearbyPlaces);
        rvNearbyPlaces.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvNearbyPlaces.setAdapter(adapter);
    }

    private void setupHotelData() {
        // Estos datos podrían venir de argumentos, bundles o un ViewModel
        tvHotelName.setText("Belmond Miraflores Park");
        tvHotelLocation.setText("Miraflores, frente al malecón, Lima");
        tvHotelPrice.setText("S/2,050");
        tvPriceDescription.setText("por 4 noches");
    }

    private void setupActions(View view) {
        // Configurar botón de retroceso
        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        // Configurar botón de favoritos
        view.findViewById(R.id.btn_favorite).setOnClickListener(v -> {
            // Implementar lógica para añadir a favoritos
        });

        // Configurar botón de elegir habitación
        view.findViewById(R.id.btn_choose_room).setOnClickListener(v -> {
            // Implementar navegación a la pantalla de selección de habitaciones
            navigateToRoomSelection();
        });

        // Ver todas las reseñas
        view.findViewById(R.id.tv_see_all_reviews).setOnClickListener(v -> {
            // Implementar navegación a la pantalla de todas las reseñas
        });
    }

    private void navigateToRoomSelection() {
        // Crear el fragmento de selección de habitación
        RoomSelectionFragment roomSelectionFragment = new RoomSelectionFragment();

        // Pasar datos del hotel al fragmento
        Bundle args = new Bundle();
        args.putString("hotel_name", tvHotelName.getText().toString());
        args.putString("hotel_price", tvHotelPrice.getText().toString());
        roomSelectionFragment.setArguments(args);

        // Navegar al fragmento
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, roomSelectionFragment) // Asegúrate de que el ID del contenedor sea correcto
                .addToBackStack(null)
                .commit();
    }
}