package com.example.proyecto_final_hoteleros.client.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.client.activity.HomeActivity;
import com.example.proyecto_final_hoteleros.client.activity.HotelResultsActivity;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.adapters.CitiesAdapter;
import com.example.proyecto_final_hoteleros.client.adapters.HotelsAdapter;
import com.example.proyecto_final_hoteleros.client.adapters.PopularHotelsAdapter;
import com.example.proyecto_final_hoteleros.client.model.City;
import com.example.proyecto_final_hoteleros.client.model.Hotel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private static final int HOTEL_RESULTS_REQUEST_CODE = 1235;
    private static final int LOCATION_REQUEST_CODE = 1234;
    private List<Hotel> listaDeHoteles = new ArrayList<>();

    // Variables para el navegador inferior
    private LinearLayout navHome, navExplore, navChat, navProfile;
    private ImageView ivHome, ivExplore, ivChat, ivProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
// Añadir el listener para el avatar
        de.hdodenhof.circleimageview.CircleImageView avatarView = rootView.findViewById(R.id.iv_avatar);

        // Establece el nombre de transición
        ViewCompat.setTransitionName(avatarView, "avatar_transition");
        // Manejar clic en el icono de notificaciones
        setupNotificationClick(rootView);

        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar al fragmento de perfil con transición compartida
                navigateToProfileWithAnimation(v);
            }
        });
        // 1) Encuentra la fila de Ubicación (en tu view_search_panel.xml)
        View rowLocation = rootView.findViewById(R.id.rowLocation);

        // 2) Selector de fechas personalizado
        View rowDates = rootView.findViewById(R.id.rowDates);
        TextView tvDates = rootView.findViewById(R.id.tvDates);
        rowDates.setOnClickListener(v -> {
            CustomDatePickerBottomSheet datePicker = new CustomDatePickerBottomSheet();
            datePicker.setListener((startDate, endDate) -> {
                SimpleDateFormat fmt = new SimpleDateFormat("dd MMM", new Locale("es", "ES"));
                tvDates.setText(fmt.format(startDate) + " – " + fmt.format(endDate));
                tvDates.setTextColor(requireContext().getColor(R.color.black));
            });
            datePicker.show(getChildFragmentManager(), "DATE_PICKER");
        });

        // 3) Selector de huéspedes
        View rowGuests = rootView.findViewById(R.id.rowGuests);
        TextView tvGuests = rootView.findViewById(R.id.tvGuests);
        rowGuests.setOnClickListener(v -> {
            GuestCountBottomSheet sheet = new GuestCountBottomSheet();
            sheet.setListener((adults, children) -> {
                tvGuests.setText(adults + " adultos – " + children + " niños");
                tvGuests.setTextColor(requireContext().getColor(R.color.black));
            });
            sheet.show(getChildFragmentManager(), "GUEST_COUNT");
        });

        // Asocia el click listener que lanza la pantalla de selección
        rowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Desde un Fragment, usa getActivity() como contexto
                Intent intent = new Intent(getActivity(), LocationSelectorActivity.class);
                startActivityForResult(intent, LOCATION_REQUEST_CODE);
            }
        });

        // Referencia al TextView y lo haces VISIBLE antes de animar
        TextView tvTitle = rootView.findViewById(R.id.tv_title);
        tvTitle.setVisibility(View.VISIBLE);

        // Arranca la animación fade-in
        tvTitle.startAnimation(
                AnimationUtils.loadAnimation(getContext(), R.anim.fade_in)
        );

        // Ejemplo de creación de datos (estos datos pueden venir de una API o base de datos)
        listaDeHoteles.add(new Hotel("Belmond Miraflores Park",
                "Miraflores, frente al malecón, Lima",
                "drawable/belmond", // O el resource ID si usas imágenes locales
                "S/290", "4.9"));
        listaDeHoteles.add(new Hotel("Inkaterra Concepción",
                "Pesawaran, Lampung",
                "drawable/inkaterra",
                "S/300", "4.6"));
        listaDeHoteles.add(new Hotel("Skylodge",
                "Valle Sagrado, acantilado, Cusco ",
                "drawable/gocta",
                "S/310", "4.8"));
        listaDeHoteles.add(new Hotel("Arennas Máncora",
                "Jepara, Central Java",
                "drawable/cuzco",
                "S/275", "4.7"));
        listaDeHoteles.add(new Hotel("Pariwana Lima",
                "Cercado, Barrio Chino, Lima",
                "drawable/arequipa",
                "S/320", "4.9"));

        // Creamos una lista de hoteles populares (podrías usar datos diferentes si lo deseas)
        List<Hotel> listaHotelesPopulares = new ArrayList<>();
        listaHotelesPopulares.add(new Hotel("Gocta Lodge",
                "Chachapoyas, Gocta, Amazonas",
                "drawable/gocta",
                "S/300", "4.9"));
        listaHotelesPopulares.add(new Hotel("Arennas Máncora",
                "Máncora, Piura",
                "drawable/cuzco",
                "S/300", "4.9"));
        listaHotelesPopulares.add(new Hotel("Inkaterra Concepción",
                "Tambopata, Madre de Dios",
                "drawable/inkaterra",
                "S/300", "4.9"));
        listaHotelesPopulares.add(new Hotel("Skylodge",
                "Valle Sagrado, Cusco",
                "drawable/gocta",
                "S/300", "4.9"));


        // Configuración del RecyclerView
        RecyclerView rvHotels = rootView.findViewById(R.id.rvHotels);
        rvHotels.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

// Crear y configurar el adapter para hoteles horizontales
        HotelsAdapter hotelsAdapter = new HotelsAdapter(listaDeHoteles);
        hotelsAdapter.setOnHotelClickListener((hotel, position) -> {
            // Navegar al fragmento de detalle cuando se hace clic en un hotel
            navigateToHotelDetail(hotel);
        });
        rvHotels.setAdapter(hotelsAdapter);
        // Configuración del RecyclerView de hoteles populares (vertical)
        RecyclerView rvPopularHotels = rootView.findViewById(R.id.rvPopularHotels);
        rvPopularHotels.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
        );

// Crear y configurar el adapter para hoteles populares
        PopularHotelsAdapter popularHotelsAdapter = new PopularHotelsAdapter(listaHotelesPopulares); // Usar la lista correcta
        popularHotelsAdapter.setOnHotelClickListener((hotel, position) -> {
            // Navegar al fragmento de detalle cuando se hace clic en un hotel popular
            navigateToHotelDetail(hotel);
        });

// Configurar el adapter UNA SOLA VEZ
        rvPopularHotels.setAdapter(popularHotelsAdapter);
// Crear lista de ciudades
        setupCitiesSection(rootView);
        // Configuración del navegador inferior

        TextView tvSeeAllNearby = rootView.findViewById(R.id.tv_see_all );
        if (tvSeeAllNearby != null) {
            tvSeeAllNearby.setOnClickListener(v -> navigateToNearbyHotels());
        }

// Para hoteles populares (asumiendo que tienes un TextView tvSeeAllPopular)
        TextView tvSeeAllPopular = rootView.findViewById(R.id.tv_see_all_popular);
        if (tvSeeAllPopular != null) {
            tvSeeAllPopular.setOnClickListener(v -> navigateToPopularHotels());
        }

// Si tienes un botón de búsqueda, agregar este listener:
        Button btnSearch = rootView.findViewById(R.id.btnSearch );
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> performSearch());
        }
        setupBottomNavigation(rootView);

        return rootView;
    }

    // Método para navegar al fragmento de detalle del hotel
    private void setupNotificationClick(View view) {
        FrameLayout notificationContainer = view.findViewById(R.id.fl_notification_container);
        notificationContainer.setOnClickListener(v -> {
            // Navegar al fragmento de notificaciones
            navigateToNotificationsFragment();
        });
    }

    private void performSearch() {
        // Obtener los valores de búsqueda actuales
        TextView tvLocation = getView().findViewById(R.id.tvLocation);
        TextView tvDates = getView().findViewById(R.id.tvDates);
        TextView tvGuests = getView().findViewById(R.id.tvGuests);

        String location = tvLocation.getText().toString();
        String dates = tvDates.getText().toString();
        String guests = tvGuests.getText().toString();

        // Navegar a la actividad de resultados con parámetros completos
        Intent intent = new Intent(getActivity(), HotelResultsActivity.class);
        intent.putExtra("filter_type", "search");
        intent.putExtra("location", location);
        intent.putExtra("dates", dates);
        intent.putExtra("guests", guests);
        startActivityForResult(intent, HOTEL_RESULTS_REQUEST_CODE);
    }
    private void navigateToNearbyHotels() {
        Intent intent = new Intent(getActivity(), HotelResultsActivity.class);
        intent.putExtra("filter_type", "nearby");
        startActivityForResult(intent, HOTEL_RESULTS_REQUEST_CODE);
    }

    private void navigateToPopularHotels() {
        Intent intent = new Intent(getActivity(), HotelResultsActivity.class);
        intent.putExtra("filter_type", "popular");
        startActivityForResult(intent, HOTEL_RESULTS_REQUEST_CODE);
    }

    private void navigateToCityHotels(String cityName) {
        Intent intent = new Intent(getActivity(), HotelResultsActivity.class);
        intent.putExtra("filter_type", "city");
        intent.putExtra("location", cityName);
        startActivityForResult(intent, HOTEL_RESULTS_REQUEST_CODE);
    }



    /**
     * Navega al fragmento de notificaciones
     */
    private void navigateToNotificationsFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Transición con animación
        transaction.setCustomAnimations(
                R.anim.slide_in_right,  // Entrada
                R.anim.slide_out_left,   // Salida
                R.anim.slide_in_left,    // Entrada al volver atrás
                R.anim.slide_out_right   // Salida al volver atrás
        );

        // Reemplazar el fragmento actual con el de notificaciones
        transaction.replace(R.id.fragment_container, new NotificationFragment());

        // Añadir a la pila de retroceso para poder volver con el botón atrás
        transaction.addToBackStack(null);

        // Commit la transacción
        transaction.commit();
    }
    private void navigateToHotelDetail(Hotel hotel) {
        Log.d("HomeFragment", "=== INICIANDO NAVEGACIÓN ===");

        try {
            // 1. Verificar actividad
            if (getActivity() == null) {
                Log.e("HomeFragment", "getActivity() retorna null");
                Toast.makeText(getContext(), "Error: Activity es null", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("HomeFragment", "Activity OK: " + getActivity().getClass().getSimpleName());

            // 2. Verificar que estamos en el fragmento correcto
            Log.d("HomeFragment", "Fragment actual: " + this.getClass().getSimpleName());

            // 3. Verificar el contenedor en la actividad
            View fragmentContainer = getActivity().findViewById(R.id.fragment_container);
            if (fragmentContainer == null) {
                Log.e("HomeFragment", "fragment_container NO ENCONTRADO en activity");

                // Listar todos los IDs disponibles en la actividad
                Log.d("HomeFragment", "Contenido de la actividad:");
                View rootView = getActivity().findViewById(android.R.id.content);
                if (rootView instanceof ViewGroup) {
                    logViewHierarchy((ViewGroup) rootView, 0);
                }

                Toast.makeText(getContext(), "Error: contenedor no encontrado", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("HomeFragment", "fragment_container ENCONTRADO: " + fragmentContainer.getClass().getSimpleName());

            // 4. Verificar FragmentManager
            androidx.fragment.app.FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            Log.d("HomeFragment", "FragmentManager OK, fragmentos actuales: " + fragmentManager.getFragments().size());

            // 5. Crear el fragmento
            Log.d("HomeFragment", "Creando HotelDetailFragment...");
            HotelDetailFragment detailFragment = new HotelDetailFragment();

            // 6. Crear argumentos
            Bundle args = new Bundle();
            args.putString("hotel_name", hotel.getName());
            args.putString("hotel_location", hotel.getLocation());
            args.putString("hotel_price", hotel.getPrice());
            args.putString("hotel_rating", hotel.getRating());
            args.putString("hotel_image", hotel.getImageUrl());
            detailFragment.setArguments(args);
            Log.d("HomeFragment", "Argumentos establecidos");

            // 7. Realizar la transacción
            Log.d("HomeFragment", "Iniciando transacción de fragmento...");

            androidx.fragment.app.FragmentTransaction transaction = fragmentManager.beginTransaction();

            transaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
            );

            transaction.replace(R.id.fragment_container, detailFragment);
            transaction.addToBackStack(null);

            Log.d("HomeFragment", "Ejecutando commit...");
            transaction.commit();

            Log.d("HomeFragment", "=== NAVEGACIÓN COMPLETADA ===");

        } catch (Exception e) {
            Log.e("HomeFragment", "=== ERROR EN NAVEGACIÓN ===");
            Log.e("HomeFragment", "Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();

            if (getContext() != null) {
                Toast.makeText(getContext(), "Error detallado: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    // Método auxiliar para debuggear la jerarquía de vistas
    private void logViewHierarchy(ViewGroup viewGroup, int depth) {
        String indent = new String(new char[depth]).replace('\0', ' ');
        Log.d("HomeFragment", indent + "ViewGroup: " + viewGroup.getClass().getSimpleName() + " ID: " + viewGroup.getId());

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                logViewHierarchy((ViewGroup) child, depth + 2);
            } else {
                Log.d("HomeFragment", indent + "  View: " + child.getClass().getSimpleName() + " ID: " + child.getId());
            }
        }
    }

    // Agregar este método en HomeFragment.java para verificar el contexto

    private void checkCurrentContext() {
        Log.d("HomeFragment", "=== VERIFICANDO CONTEXTO ===");

        // Verificar la actividad actual
        if (getActivity() != null) {
            Log.d("HomeFragment", "Activity class: " + getActivity().getClass().getName());
            Log.d("HomeFragment", "Activity simple name: " + getActivity().getClass().getSimpleName());

            // Verificar si es HomeActivity
            if (getActivity() instanceof HomeActivity) {
                Log.d("HomeFragment", "✅ Estamos en HomeActivity");
            } else {
                Log.e("HomeFragment", "❌ NO estamos en HomeActivity, estamos en: " + getActivity().getClass().getSimpleName());
            }

            // Verificar el layout de la actividad
            try {
                View contentView = getActivity().findViewById(android.R.id.content);
                if (contentView != null) {
                    Log.d("HomeFragment", "Content view encontrado: " + contentView.getClass().getSimpleName());

                    // Buscar fragment_container
                    View fragmentContainer = getActivity().findViewById(R.id.fragment_container);
                    if (fragmentContainer != null) {
                        Log.d("HomeFragment", "✅ fragment_container encontrado en actividad");
                    } else {
                        Log.e("HomeFragment", "❌ fragment_container NO encontrado en actividad");

                        // Intentar buscar otros contenedores comunes
                        View mainContainer = getActivity().findViewById(R.id.main);
                        View mainContentContainer = getActivity().findViewById(R.id.main_container);

                        Log.d("HomeFragment", "main container: " + (mainContainer != null ? "✅" : "❌"));
                        Log.d("HomeFragment", "main_container: " + (mainContentContainer != null ? "✅" : "❌"));
                    }
                }
            } catch (Exception e) {
                Log.e("HomeFragment", "Error verificando content view: " + e.getMessage());
            }
        } else {
            Log.e("HomeFragment", "❌ getActivity() retorna null");
        }

        // Verificar el contexto
        if (getContext() != null) {
            Log.d("HomeFragment", "Context class: " + getContext().getClass().getName());
        } else {
            Log.e("HomeFragment", "❌ getContext() retorna null");
        }

        Log.d("HomeFragment", "=== FIN VERIFICACIÓN CONTEXTO ===");
    }

    // Llama este método antes de navegateToHotelDetail
// Por ejemplo, en el onClick del hotel:
    private void onHotelClick(Hotel hotel) {
        checkCurrentContext(); // Agregar esta línea
        navigateToHotelDetail(hotel);
    }
    // Método para navegar a ProfileFragment con animación personalizada
    // Método para navegar a ProfileFragment con animación personalizada
    private void navigateToProfileWithAnimation(View sharedElement) {
        // Crear instancia del fragment de perfil
        ProfileFragment profileFragment = new ProfileFragment();

        // Configurar los elementos compartidos para la transición
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true) // Optimiza la transición
                .addSharedElement(sharedElement, "avatar_transition");

        // Aplicar animaciones personalizadas para las vistas no compartidas
        transaction.setCustomAnimations(
                R.anim.fade_in2,
                R.anim.fade_out,
                R.anim.fade_in2,
                R.anim.fade_out);

        // Reemplazar el fragmento actual y añadir a la pila
        transaction.replace(R.id.fragment_container, profileFragment)
                .addToBackStack(null)
                .commit();

        // Actualizar la barra de navegación
        setSelectedNavItem(navProfile, ivProfile);
    }
    // Método para configurar el navegador inferior
    private void setupBottomNavigation(View rootView) {
        // Referencias a los elementos del navegador
        navHome = rootView.findViewById(R.id.nav_home);
        navExplore = rootView.findViewById(R.id.nav_explore);
        navChat = rootView.findViewById(R.id.nav_chat);
        navProfile = rootView.findViewById(R.id.nav_profile);

        ivHome = rootView.findViewById(R.id.iv_home);
        ivExplore = rootView.findViewById(R.id.iv_explore);
        ivChat = rootView.findViewById(R.id.iv_chat);
        ivProfile = rootView.findViewById(R.id.iv_profile);

        // Marcar Home como seleccionado por defecto
        setSelectedNavItem(navHome, ivHome);

        // Establecer listeners para cada elemento del navegador
        navHome.setOnClickListener(v -> {
            if (!isCurrentFragment(HomeFragment.class)) {
                setSelectedNavItem(navHome, ivHome);
                // Ya estamos en Home, no necesitamos cambiar de fragmento
                // Pero si venimos de otro fragmento, regresamos a HomeFragment
                navigateToFragment(new HomeFragment(), false);
            }
        });

        navExplore.setOnClickListener(v -> {
            if (!isCurrentFragment(HistorialFragment.class)) {
                setSelectedNavItem(navExplore, ivExplore);
                navigateToFragment(new HistorialFragment(), true);
            }
        });

        navChat.setOnClickListener(v -> {
            if (!isCurrentFragment(ChatFragment.class)) {
                setSelectedNavItem(navChat, ivChat);
                navigateToFragment(new ChatFragment(), true);
            }
        });

        navProfile.setOnClickListener(v -> {
            if (!isCurrentFragment(ProfileFragment.class)) {
                setSelectedNavItem(navProfile, ivProfile);
                navigateToFragment(new ProfileFragment(), true);
            }
        });
    }

    private void setupCitiesSection(View rootView) {
        // Crear lista de ciudades
        List<City> listaCiudades = new ArrayList<>();
        listaCiudades.add(new City("Lima", R.drawable.lima));
        listaCiudades.add(new City("Cusco", R.drawable.cuzco));
        listaCiudades.add(new City("Arequipa", R.drawable.arequipa));
        listaCiudades.add(new City("Piura", R.drawable.inkaterra));
        listaCiudades.add(new City("Trujillo", R.drawable.belmond));

        // Configuración del RecyclerView de ciudades
        RecyclerView rvCities = rootView.findViewById(R.id.rvCities);
        rvCities.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        // Modificar el CitiesAdapter para incluir el listener de clic
        CitiesAdapter citiesAdapter = new CitiesAdapter(listaCiudades);
        citiesAdapter.setOnCityClickListener(city -> {
            // Navegar a la página de resultados con filtro por ciudad
            navigateToCityHotels(city.getName());
        });
        rvCities.setAdapter(citiesAdapter);

        // Ver todo - ciudades
        TextView tvSeeAllCities = rootView.findViewById(R.id.tv_see_all_cities);
        tvSeeAllCities.setOnClickListener(v -> {
            // Navegar a la página de resultados mostrando todas las ciudades
            Intent intent = new Intent(getActivity(), HotelResultsActivity.class);
            intent.putExtra("filter_type", "all_cities");
            startActivity(intent);
        });
    }


    // Método para navegar a un fragmento con animación
    private void navigateToFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    // Método para verificar si el fragmento actual es del tipo especificado
    private boolean isCurrentFragment(Class<? extends Fragment> fragmentClass) {
        Fragment currentFragment = requireActivity().getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        return currentFragment != null && fragmentClass.isInstance(currentFragment);
    }

    // Método para resaltar el ítem seleccionado
    private void setSelectedNavItem(LinearLayout navItem, ImageView icon) {
        // Resetear todos los íconos a color blanco
        ivHome.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        ivExplore.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        ivChat.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        ivProfile.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));

        // Establecer el ícono seleccionado a color naranja
        icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.orange));
    }

    // Capturar el resultado que devuelva LocationSelectorActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String location = data.getStringExtra("selected_location");
            TextView tvLocation = getView().findViewById(R.id.tvLocation);
            tvLocation.setText(location);
            tvLocation.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        }

        // NUEVO: Manejar resultado de HotelResultsActivity
        if (requestCode == HOTEL_RESULTS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // El usuario seleccionó un hotel, navegar al detalle
            String hotelName = data.getStringExtra("hotel_name");
            String hotelLocation = data.getStringExtra("hotel_location");
            String hotelPrice = data.getStringExtra("hotel_price");
            String hotelRating = data.getStringExtra("hotel_rating");
            String hotelImage = data.getStringExtra("hotel_image");

            // Crear objeto Hotel y navegar al detalle
            Hotel selectedHotel = new Hotel(hotelName, hotelLocation, hotelImage, hotelPrice, hotelRating);
            navigateToHotelDetail(selectedHotel);
        }
    }
}