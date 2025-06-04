package com.example.proyecto_final_hoteleros.client.ui.fragment;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity;
import com.example.proyecto_final_hoteleros.client.ui.activity.HotelResultsActivity;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.activity.LocationSelectorActivity;
import com.example.proyecto_final_hoteleros.client.ui.adapters.CitiesAdapter;
import com.example.proyecto_final_hoteleros.client.ui.adapters.HotelsAdapter;
import com.example.proyecto_final_hoteleros.client.ui.adapters.PopularHotelsAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.City;
import com.example.proyecto_final_hoteleros.client.data.model.Hotel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static final int HOTEL_RESULTS_REQUEST_CODE = 1235;
    private static final int LOCATION_REQUEST_CODE = 1234;

    // Lista de hoteles
    private List<Hotel> listaDeHoteles = new ArrayList<>();

    // Variables para datos del usuario (Firebase)
    private String userId;
    private String userName;
    private String userFullName;
    private String userEmail;
    private String userType;

    // Variables para el navegador inferior
    private LinearLayout navHome, navExplore, navChat, navProfile;
    private ImageView ivHome, ivExplore, ivChat, ivProfile;

    // Views
    private TextView tvGreeting;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtener datos del usuario desde los argumentos
        if (getArguments() != null) {
            userId = getArguments().getString("user_id");
            userName = getArguments().getString("user_name");
            userFullName = getArguments().getString("user_full_name");
            userEmail = getArguments().getString("user_email");
            userType = getArguments().getString("user_type");

            Log.d(TAG, "=== DATOS RECIBIDOS EN HOMEFRAGMENT ===");
            Log.d(TAG, "User ID: " + userId);
            Log.d(TAG, "User Name: " + userName);
            Log.d(TAG, "User Full Name: " + userFullName);
            Log.d(TAG, "User Email: " + userEmail);
            Log.d(TAG, "User Type: " + userType);
        }

        // También intentar obtener datos desde la actividad padre como fallback
        if ((userName == null || userName.isEmpty()) && getActivity() instanceof HomeActivity) {
            HomeActivity homeActivity = (HomeActivity) getActivity();
            userName = homeActivity.getUserName();
            userFullName = homeActivity.getUserFullName();
            userId = homeActivity.getUserId();
            userEmail = homeActivity.getUserEmail();
            userType = homeActivity.getUserType();

            Log.d(TAG, "Datos obtenidos desde HomeActivity como fallback");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicializar views (incluye el saludo)
        initViews(rootView);

        // Configurar datos del usuario
        setupUserData();

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
        if (tvTitle != null) {
            tvTitle.setVisibility(View.VISIBLE);
            // Arranca la animación fade-in
            tvTitle.startAnimation(
                    AnimationUtils.loadAnimation(getContext(), R.anim.fade_in)
            );
        }

        // Configurar datos de hoteles
        setupHotelsData();

        // Configuración de RecyclerViews
        setupRecyclerViews(rootView);

        // Crear lista de ciudades
        setupCitiesSection(rootView);

        // Configurar botones de "Ver todo"
        setupSeeAllButtons(rootView);

        // Configuración del navegador inferior
        setupBottomNavigation(rootView);

        return rootView;
    }

    private void initViews(View view) {
        // Inicializar el TextView de saludo (este ID SÍ existe en tu layout)
        tvGreeting = view.findViewById(R.id.tv_greeting);
    }

    private void setupUserData() {
        // Configurar el saludo personalizado
        if (tvGreeting != null) {
            String greetingText = "Hola, " + (userName != null && !userName.isEmpty() ? userName : "Huésped");
            tvGreeting.setText(greetingText);

            Log.d(TAG, "Greeting configurado: " + greetingText);
        } else {
            Log.e(TAG, "TextView tv_greeting no encontrado en el layout");
        }
    }

    private void setupHotelsData() {
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
    }

    private void setupRecyclerViews(View rootView) {
        // Configuración del RecyclerView de hoteles horizontales
        RecyclerView rvHotels = rootView.findViewById(R.id.rvHotels);
        if (rvHotels != null) {
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
        }

        // Configuración del RecyclerView de hoteles populares (vertical)
        RecyclerView rvPopularHotels = rootView.findViewById(R.id.rvPopularHotels);
        if (rvPopularHotels != null) {
            rvPopularHotels.setLayoutManager(
                    new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
            );

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

            // Crear y configurar el adapter para hoteles populares
            PopularHotelsAdapter popularHotelsAdapter = new PopularHotelsAdapter(listaHotelesPopulares);
            popularHotelsAdapter.setOnHotelClickListener((hotel, position) -> {
                // Navegar al fragmento de detalle cuando se hace clic en un hotel popular
                navigateToHotelDetail(hotel);
            });

            // Configurar el adapter UNA SOLA VEZ
            rvPopularHotels.setAdapter(popularHotelsAdapter);
        }
    }

    private void setupSeeAllButtons(View rootView) {
        TextView tvSeeAllNearby = rootView.findViewById(R.id.tv_see_all);
        if (tvSeeAllNearby != null) {
            tvSeeAllNearby.setOnClickListener(v -> navigateToNearbyHotels());
        }

        // Para hoteles populares (asumiendo que tienes un TextView tvSeeAllPopular)
        TextView tvSeeAllPopular = rootView.findViewById(R.id.tv_see_all_popular);
        if (tvSeeAllPopular != null) {
            tvSeeAllPopular.setOnClickListener(v -> navigateToPopularHotels());
        }

        // Si tienes un botón de búsqueda, agregar este listener:
        Button btnSearch = rootView.findViewById(R.id.btnSearch);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> performSearch());
        }
    }

    // Método para navegar al fragmento de detalle del hotel
    private void setupNotificationClick(View view) {
        FrameLayout notificationContainer = view.findViewById(R.id.fl_notification_container);
        if (notificationContainer != null) {
            notificationContainer.setOnClickListener(v -> {
                // Navegar al fragmento de notificaciones
                navigateToNotificationsFragment();
            });
        }
    }

    private void performSearch() {
        // Obtener los valores de búsqueda actuales
        if (getView() != null) {
            TextView tvLocation = getView().findViewById(R.id.tvLocation);
            TextView tvDates = getView().findViewById(R.id.tvDates);
            TextView tvGuests = getView().findViewById(R.id.tvGuests);

            String location = tvLocation != null ? tvLocation.getText().toString() : "";
            String dates = tvDates != null ? tvDates.getText().toString() : "";
            String guests = tvGuests != null ? tvGuests.getText().toString() : "";

            // Navegar a la actividad de resultados con parámetros completos
            Intent intent = new Intent(getActivity(), HotelResultsActivity.class);
            intent.putExtra("filter_type", "search");
            intent.putExtra("location", location);
            intent.putExtra("dates", dates);
            intent.putExtra("guests", guests);
            // Pasar también datos del usuario si es necesario
            intent.putExtra("user_id", userId);
            intent.putExtra("user_name", userName);
            startActivityForResult(intent, HOTEL_RESULTS_REQUEST_CODE);
        }
    }

    private void navigateToNearbyHotels() {
        Intent intent = new Intent(getActivity(), HotelResultsActivity.class);
        intent.putExtra("filter_type", "nearby");
        intent.putExtra("user_id", userId);
        startActivityForResult(intent, HOTEL_RESULTS_REQUEST_CODE);
    }

    private void navigateToPopularHotels() {
        Intent intent = new Intent(getActivity(), HotelResultsActivity.class);
        intent.putExtra("filter_type", "popular");
        intent.putExtra("user_id", userId);
        startActivityForResult(intent, HOTEL_RESULTS_REQUEST_CODE);
    }

    private void navigateToCityHotels(String cityName) {
        Intent intent = new Intent(getActivity(), HotelResultsActivity.class);
        intent.putExtra("filter_type", "city");
        intent.putExtra("location", cityName);
        intent.putExtra("user_id", userId);
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

        // Crear el fragmento de notificaciones y pasar datos del usuario
        NotificationFragment notificationFragment = new NotificationFragment();
        Bundle args = new Bundle();
        args.putString("user_id", userId);
        args.putString("user_name", userName);
        notificationFragment.setArguments(args);

        // Reemplazar el fragmento actual con el de notificaciones
        transaction.replace(R.id.fragment_container, notificationFragment);

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

            // 6. Crear argumentos (incluir datos del usuario)
            Bundle args = new Bundle();
            args.putString("hotel_name", hotel.getName());
            args.putString("hotel_location", hotel.getLocation());
            args.putString("hotel_price", hotel.getPrice());
            args.putString("hotel_rating", hotel.getRating());
            args.putString("hotel_image", hotel.getImageUrl());
            // Agregar datos del usuario
            args.putString("user_id", userId);
            args.putString("user_name", userName);
            args.putString("user_email", userEmail);
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

    // Llama este método antes de navigateToHotelDetail
    // Por ejemplo, en el onClick del hotel:
    private void onHotelClick(Hotel hotel) {
        checkCurrentContext(); // Agregar esta línea
        navigateToHotelDetail(hotel);
    }

    // Método para navegar a ProfileFragment con animación personalizada
    private void navigateToProfileWithAnimation(View sharedElement) {
        // Crear instancia del fragment de perfil
        ProfileFragment profileFragment = new ProfileFragment();

        // Pasar datos del usuario al fragmento de perfil
        Bundle args = new Bundle();
        args.putString("user_id", userId);
        args.putString("user_name", userName);
        args.putString("user_full_name", userFullName);
        args.putString("user_email", userEmail);
        args.putString("user_type", userType);
        profileFragment.setArguments(args);

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
        if (navHome != null && ivHome != null) {
            setSelectedNavItem(navHome, ivHome);
        }

        // Establecer listeners para cada elemento del navegador
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (!isCurrentFragment(HomeFragment.class)) {
                    setSelectedNavItem(navHome, ivHome);
                    // Ya estamos en Home, no necesitamos cambiar de fragmento
                    // Pero si venimos de otro fragmento, regresamos a HomeFragment
                    navigateToFragment(new HomeFragment(), false);
                }
            });
        }

        if (navExplore != null) {
            navExplore.setOnClickListener(v -> {
                if (!isCurrentFragment(HistorialFragment.class)) {
                    setSelectedNavItem(navExplore, ivExplore);
                    HistorialFragment historialFragment = new HistorialFragment();
                    // Pasar datos del usuario
                    Bundle args = new Bundle();
                    args.putString("user_id", userId);
                    args.putString("user_name", userName);
                    historialFragment.setArguments(args);
                    navigateToFragment(historialFragment, true);
                }
            });
        }

        if (navChat != null) {
            navChat.setOnClickListener(v -> {
                if (!isCurrentFragment(ChatFragment.class)) {
                    setSelectedNavItem(navChat, ivChat);
                    ChatFragment chatFragment = new ChatFragment();
                    // Pasar datos del usuario
                    Bundle args = new Bundle();
                    args.putString("user_id", userId);
                    args.putString("user_name", userName);
                    chatFragment.setArguments(args);
                    navigateToFragment(chatFragment, true);
                }
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                if (!isCurrentFragment(ProfileFragment.class)) {
                    setSelectedNavItem(navProfile, ivProfile);
                    ProfileFragment profileFragment = new ProfileFragment();
                    // Pasar datos del usuario
                    Bundle args = new Bundle();
                    args.putString("user_id", userId);
                    args.putString("user_name", userName);
                    args.putString("user_full_name", userFullName);
                    args.putString("user_email", userEmail);
                    args.putString("user_type", userType);
                    profileFragment.setArguments(args);
                    navigateToFragment(profileFragment, true);
                }
            });
        }
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
        if (rvCities != null) {
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
        }

        // Ver todo - ciudades
        TextView tvSeeAllCities = rootView.findViewById(R.id.tv_see_all_cities);
        if (tvSeeAllCities != null) {
            tvSeeAllCities.setOnClickListener(v -> {
                // Navegar a la página de resultados mostrando todas las ciudades
                Intent intent = new Intent(getActivity(), HotelResultsActivity.class);
                intent.putExtra("filter_type", "all_cities");
                intent.putExtra("user_id", userId);
                startActivity(intent);
            });
        }
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
        if (ivHome != null) ivHome.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        if (ivExplore != null) ivExplore.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        if (ivChat != null) ivChat.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
        if (ivProfile != null) ivProfile.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));

        // Establecer el ícono seleccionado a color naranja
        if (icon != null) {
            icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.orange));
        }
    }

    // Capturar el resultado que devuelva LocationSelectorActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && getView() != null) {
                String location = data.getStringExtra("selected_location");
                TextView tvLocation = getView().findViewById(R.id.tvLocation);
                if (tvLocation != null) {
                    tvLocation.setText(location);
                    tvLocation.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                }
            }
        }

        // NUEVO: Manejar resultado de HotelResultsActivity
        if (requestCode == HOTEL_RESULTS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
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

    // Métodos públicos para acceder a los datos del usuario desde otras partes del fragmento
    public String getUserName() {
        return userName != null ? userName : "Huésped";
    }

    public String getUserFullName() {
        return userFullName != null ? userFullName : getUserName();
    }

    public String getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserType() {
        return userType;
    }

    // Método para actualizar los datos del usuario (útil si se actualizan desde Firebase)
    public void updateUserData(String userId, String userName, String userFullName, String userEmail, String userType) {
        this.userId = userId;
        this.userName = userName;
        this.userFullName = userFullName;
        this.userEmail = userEmail;
        this.userType = userType;

        // Actualizar la UI si el fragmento ya está creado
        setupUserData();

        Log.d(TAG, "Datos de usuario actualizados");
    }
}