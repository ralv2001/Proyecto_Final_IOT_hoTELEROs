package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelProfile;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseHotelManager;
import com.example.proyecto_final_hoteleros.client.data.model.Hotel;
import com.example.proyecto_final_hoteleros.client.data.model.City;
import com.example.proyecto_final_hoteleros.client.ui.activity.HotelResultsActivity;
import com.example.proyecto_final_hoteleros.client.ui.activity.LocationSelectorActivity;
import com.example.proyecto_final_hoteleros.client.ui.adapters.CitiesAdapter;
import com.example.proyecto_final_hoteleros.client.ui.adapters.HotelsAdapter;
import com.example.proyecto_final_hoteleros.client.ui.adapters.PopularHotelsAdapter;
import com.example.proyecto_final_hoteleros.client.ui.fragment.CustomDatePickerBottomSheet;
import com.example.proyecto_final_hoteleros.client.ui.fragment.GuestCountBottomSheet;
import com.example.proyecto_final_hoteleros.client.utils.HotelGroupingUtils;
import com.example.proyecto_final_hoteleros.client.utils.UserDataManager;
import com.example.proyecto_final_hoteleros.client.utils.UserLocationManager;
import com.example.proyecto_final_hoteleros.client.navigation.NavigationManager;
import com.example.proyecto_final_hoteleros.client.utils.HotelPriceUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends BaseBottomNavigationFragment {  // ✅ CAMBIADO

    private static final String TAG = "HomeFragment";
    private static final int LOCATION_REQUEST_CODE = 1001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    // Views principales
    private TextView tvUserName;
    private TextView tvLocation;
    private TextView tvDates;
    private TextView tvGuests;
    private CircleImageView ivAvatar;
    private ImageView ivNotifications;

    // RecyclerViews para las 3 secciones ORIGINALES
    private RecyclerView rvHotels; // Hoteles cerca de ti
    private RecyclerView rvPopularHotels; // Hoteles populares
    private RecyclerView rvCities; // Ciudades (layout original)

    // Adapters ORIGINALES
    private HotelsAdapter nearbyAdapter;
    private PopularHotelsAdapter popularAdapter;
    private CitiesAdapter citiesAdapter;

    // Estados de carga
    private LinearLayout loadingNearby;
    private LinearLayout loadingPopular;
    private LinearLayout loadingCities;
    private TextView tvEmptyNearby;
    private TextView tvEmptyPopular;
    private TextView tvEmptyCities;

    // Datos
    private List<HotelProfile> allHotels = new ArrayList<>();
    private UserLocationManager locationManager;
    private FirebaseHotelManager hotelManager;
    private FusedLocationProviderClient fusedLocationClient;

    // Estado de búsqueda
    private String selectedDates = "Hoy - Mañana";
    private int adults = 2;
    private int children = 0;

    // ✅ NUEVO: Implementar método requerido por BaseBottomNavigationFragment
    @Override
    protected NavigationTab getCurrentTab() {
        return NavigationTab.HOME;  // ✅ AGREGAR ESTE MÉTODO
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar managers
        locationManager = UserLocationManager.getInstance(requireContext());
        hotelManager = FirebaseHotelManager.getInstance(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Obtener datos del usuario si vienen en argumentos
        if (getArguments() != null) {
            String userId = getArguments().getString("user_id");
            String userName = getArguments().getString("user_name");
            String userFullName = getArguments().getString("user_full_name");
            String userEmail = getArguments().getString("user_email");
            String userType = getArguments().getString("user_type");

            UserDataManager.getInstance().setUserData(userId, userName, userFullName, userEmail, userType);
            Log.d(TAG, "=== DATOS RECIBIDOS EN HOMEFRAGMENT ===");
            Log.d(TAG, "User ID: " + userId);
            Log.d(TAG, "User Name: " + userName);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.client_fragment_home, container, false);

        // Inicializar views
        initViews(rootView);

        // Configurar datos del usuario
        setupUserData();

        // Configurar listeners
        setupClickListeners(rootView);

        // Avatar con transición
        ViewCompat.setTransitionName(ivAvatar, "avatar_transition");

        // Verificar ubicación y cargar hoteles
        checkLocationAndLoadHotels();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ✅ CONFIGURAR WINDOW INSETS PARA STATUS BAR EN ESTE FRAGMENT
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 🎯 SOLO TOP PADDING para status bar en HomeFragment
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    private void initViews(View rootView) {
        // User info views
        tvUserName = rootView.findViewById(R.id.tv_user_name);
        ivAvatar = rootView.findViewById(R.id.iv_avatar);
        ivNotifications = rootView.findViewById(R.id.iv_notifications);

        // Search panel views
        tvLocation = rootView.findViewById(R.id.tvLocation);
        tvDates = rootView.findViewById(R.id.tvDates);
        tvGuests = rootView.findViewById(R.id.tvGuests);

        // RecyclerViews ORIGINALES
        rvHotels = rootView.findViewById(R.id.rvHotels);
        rvPopularHotels = rootView.findViewById(R.id.rvPopularHotels);
        rvCities = rootView.findViewById(R.id.rvCities);

        // Estados de carga
        loadingNearby = rootView.findViewById(R.id.loading_nearby);
        loadingPopular = rootView.findViewById(R.id.loading_popular);
        loadingCities = rootView.findViewById(R.id.loading_cities);

        tvEmptyNearby = rootView.findViewById(R.id.tv_empty_nearby);
        tvEmptyPopular = rootView.findViewById(R.id.tv_empty_popular);
        tvEmptyCities = rootView.findViewById(R.id.tv_empty_cities);

        // Configurar RecyclerViews ORIGINALES
        setupRecyclerViews();

        // Configurar valores iniciales del panel de búsqueda
        updateSearchPanelDisplay();
    }

    private void setupRecyclerViews() {
        // Hoteles cercanos - Horizontal
        LinearLayoutManager nearbyLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvHotels.setLayoutManager(nearbyLayoutManager);

        // Hoteles populares - Horizontal
        LinearLayoutManager popularLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvPopularHotels.setLayoutManager(popularLayoutManager);

        // Ciudades - Horizontal (como era originalmente)
        LinearLayoutManager cityLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvCities.setLayoutManager(cityLayoutManager);
    }

    private void setupUserData() {
        UserDataManager userManager = UserDataManager.getInstance();

        if (tvUserName != null) {
            String userName = userManager.getUserName();
            if (userName != null && !userName.isEmpty()) {
                tvUserName.setText("Hola, " + userName);
            } else {
                tvUserName.setText("Hola, Huésped");
            }
        }

        // Configurar ubicación inicial
        if (tvLocation != null) {
            String locationDisplay = locationManager.getLocationDisplayName();
            tvLocation.setText(locationDisplay);
        }
    }

    private void setupClickListeners(View rootView) {
        // Avatar click
        if (ivAvatar != null) {
            ivAvatar.setOnClickListener(v -> navigateToProfileWithAnimation(v));
        }

        // Notifications click
        if (ivNotifications != null) {
            ivNotifications.setOnClickListener(v -> {
                NavigationManager.getInstance().navigateToNotifications(
                        UserDataManager.getInstance().getUserBundle()
                );
            });
        }

        // Location selector
        View rowLocation = rootView.findViewById(R.id.rowLocation);
        if (rowLocation != null) {
            rowLocation.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LocationSelectorActivity.class);
                startActivityForResult(intent, LOCATION_REQUEST_CODE);
            });
        }

        // Date selector
        View rowDates = rootView.findViewById(R.id.rowDates);
        if (rowDates != null) {
            rowDates.setOnClickListener(v -> openDateSelector());
        }

        // Guest selector
        View rowGuests = rootView.findViewById(R.id.rowGuests);
        if (rowGuests != null) {
            rowGuests.setOnClickListener(v -> openGuestSelector());
        }

        // "Ver todo" buttons
        TextView tvSeeAll = rootView.findViewById(R.id.tv_see_all);
        if (tvSeeAll != null) {
            tvSeeAll.setOnClickListener(v -> navigateToNearbyHotels());
        }

        TextView tvSeeAllPopular = rootView.findViewById(R.id.tv_see_all_popular);
        if (tvSeeAllPopular != null) {
            tvSeeAllPopular.setOnClickListener(v -> navigateToPopularHotels());
        }

        TextView tvSeeAllCities = rootView.findViewById(R.id.tv_see_all_cities);
        if (tvSeeAllCities != null) {
            tvSeeAllCities.setOnClickListener(v -> navigateToAllDestinations());
        }
    }

    // ========== GESTIÓN DE UBICACIÓN (mantenida igual) ==========

    private void checkLocationAndLoadHotels() {
        if (locationManager.hasValidLocation()) {
            Log.d(TAG, "✅ Ubicación válida encontrada: " + locationManager.getCurrentCity());
            loadHotelsFromFirebase();
        } else {
            Log.d(TAG, "❌ No hay ubicación válida, solicitando ubicación GPS automáticamente");
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "📱 Solicitando permisos de ubicación...");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "✅ Permisos de ubicación ya concedidos");
            getCurrentLocationAutomatically();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✅ Permiso de ubicación concedido, obteniendo ubicación...");
                getCurrentLocationAutomatically();
            } else {
                Log.w(TAG, "❌ Permiso de ubicación denegado, usando Lima por defecto");
                Toast.makeText(getContext(), "Se usará Lima como ubicación por defecto", Toast.LENGTH_LONG).show();
                setDefaultLocation();
                loadHotelsFromFirebase();
            }
        }
    }

    private void getCurrentLocationAutomatically() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Sin permisos para obtener ubicación");
            setDefaultLocation();
            loadHotelsFromFirebase();
            return;
        }

        Log.d(TAG, "📍 Obteniendo ubicación GPS automáticamente...");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Log.d(TAG, "✅ Ubicación obtenida: " + location.getLatitude() + ", " + location.getLongitude());
                        convertLocationToAddress(location.getLatitude(), location.getLongitude());
                    } else {
                        Log.w(TAG, "⚠️ Ubicación GPS nula, usando ubicación por defecto");
                        setDefaultLocation();
                        loadHotelsFromFirebase();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error obteniendo ubicación GPS: " + e.getMessage());
                    setDefaultLocation();
                    loadHotelsFromFirebase();
                });
    }

    private void convertLocationToAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String city = extractCityFromAddress(address);
                String district = extractDistrictFromAddress(address);

                Log.d(TAG, "✅ Dirección convertida - Ciudad: " + city + ", Distrito: " + district);

                locationManager.saveCurrentLocation(city, district, latitude, longitude);

                if (tvLocation != null) {
                    tvLocation.setText(locationManager.getLocationDisplayName());
                }

                loadHotelsFromFirebase();
            } else {
                Log.w(TAG, "⚠️ No se pudo convertir coordenadas a dirección");
                setDefaultLocation();
                loadHotelsFromFirebase();
            }

        } catch (IOException e) {
            Log.e(TAG, "❌ Error en geocoding: " + e.getMessage());
            setDefaultLocation();
            loadHotelsFromFirebase();
        }
    }

    private String extractCityFromAddress(Address address) {
        if (address.getLocality() != null && !address.getLocality().isEmpty()) {
            return address.getLocality();
        }
        if (address.getAdminArea() != null && !address.getAdminArea().isEmpty()) {
            return address.getAdminArea();
        }
        if (address.getSubAdminArea() != null && !address.getSubAdminArea().isEmpty()) {
            return address.getSubAdminArea();
        }
        return "Lima";
    }

    private String extractDistrictFromAddress(Address address) {
        if (address.getSubLocality() != null && !address.getSubLocality().isEmpty()) {
            return address.getSubLocality();
        }
        if (address.getThoroughfare() != null && !address.getThoroughfare().isEmpty()) {
            return address.getThoroughfare();
        }
        if (address.getFeatureName() != null && !address.getFeatureName().isEmpty()) {
            return address.getFeatureName();
        }
        return "Centro";
    }

    private void setDefaultLocation() {
        String defaultCity = "Lima";
        String defaultDistrict = "Centro";
        double defaultLat = -12.046374;
        double defaultLon = -77.042793;

        Log.d(TAG, "📍 Estableciendo ubicación por defecto: " + defaultCity);

        locationManager.saveCurrentLocation(defaultCity, defaultDistrict, defaultLat, defaultLon);

        if (tvLocation != null) {
            tvLocation.setText(locationManager.getLocationDisplayName());
        }
    }

    // ========== CARGA DE HOTELES DESDE FIREBASE ==========

    private void loadHotelsFromFirebase() {
        Log.d(TAG, "🔄 Cargando hoteles desde Firebase...");
        showLoadingStates();

        hotelManager.findHotelsNearLocation(0, 0, 999999, new FirebaseHotelManager.HotelsCallback() {
            @Override
            public void onSuccess(List<HotelProfile> hotels) {
                Log.d(TAG, "✅ Hoteles cargados desde Firebase: " + hotels.size());
                allHotels = hotels;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        hideLoadingStates();
                        convertAndDisplayHotels();
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando hoteles: " + error);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        hideLoadingStates();
                        showErrorStates();
                        Toast.makeText(getContext(), "Error cargando hoteles: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void convertAndDisplayHotels() {
        // Convertir HotelProfile a Hotel para usar con la lógica existente
        List<Hotel> convertedHotels = convertHotelProfilesToHotels(allHotels);

        // Aplicar lógica existente para cada sección
        loadNearbyHotelsSection(convertedHotels);
        loadPopularHotelsSection(convertedHotels);
        loadCitiesSection(convertedHotels);
    }

    // ✅ MÉTODO ACTUALIZADO: Usar fotos reales de Firebase y precios de habitaciones
    private List<Hotel> convertHotelProfilesToHotels(List<HotelProfile> hotelProfiles) {
        List<Hotel> hotels = new ArrayList<>();
        AtomicInteger processedHotels = new AtomicInteger(0);
        int totalHotels = hotelProfiles.size();

        Log.d(TAG, "🔄 Convirtiendo " + totalHotels + " hoteles con precios reales...");

        for (HotelProfile profile : hotelProfiles) {
            if (profile != null && profile.isActive()) {
                String city = extractCityFromProfile(profile);

                // ✅ EXTRAER PRIMERA FOTO REAL DEL HOTEL
                String imageUrl = getFirstPhotoFromProfile(profile);

                // ✅ OBTENER PRECIO REAL DE HABITACIONES
                HotelPriceUtils.getMinimumRoomPrice(profile, getContext(), new HotelPriceUtils.PriceCallback() {
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
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Log.d(TAG, "✅ Todos los hoteles procesados con precios reales");
                                        updateHotelSections(hotels);
                                    });
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.w(TAG, "⚠️ Error obteniendo precio para " + profile.getName() + ": " + error);

                        // Usar precio por defecto en caso de error
                        String defaultPrice = HotelPriceUtils.generatePriceSync(profile);

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
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Log.d(TAG, "✅ Todos los hoteles procesados (algunos con precios por defecto)");
                                        updateHotelSections(hotels);
                                    });
                                }
                            }
                        }
                    }
                });

                // ✅ LOG PARA VER QUE ESTÁ FUNCIONANDO
                Log.d(TAG, "🏨 Procesando hotel: " + profile.getName() + " - Foto: " +
                        (imageUrl.startsWith("http") ? "URL_REAL" : "PLACEHOLDER"));
            }
        }

        // Si no hay hoteles activos, retornar lista vacía inmediatamente
        if (totalHotels == 0) {
            Log.d(TAG, "❌ No hay hoteles activos para procesar");
            updateHotelSections(hotels);
        }

        return hotels; // Esta lista se irá llenando asíncronamente
    }

    // ✅ NUEVO MÉTODO: Actualizar secciones cuando todos los precios estén listos
    private void updateHotelSections(List<Hotel> hotels) {
        Log.d(TAG, "🔄 Actualizando secciones con " + hotels.size() + " hoteles con precios reales");

        // Aplicar lógica existente para cada sección
        loadNearbyHotelsSection(hotels);
        loadPopularHotelsSection(hotels);
        loadCitiesSection(hotels);
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

    private String generateRatingFromProfile(HotelProfile profile) {
        double rating = 4.0 + (Math.random() * 1.0);
        return String.format(Locale.US, "%.1f", rating);
    }

    // ========== SECCIONES DE HOTELES (mantenidas igual) ==========

    private void loadNearbyHotelsSection(List<Hotel> allHotels) {
        Log.d(TAG, "🏠 Cargando hoteles cercanos...");

        String userCity = locationManager.getCurrentCity().toLowerCase();
        List<Hotel> nearbyHotels = HotelGroupingUtils.filterHotelsByProximity(allHotels, userCity, getContext());

        Log.d(TAG, "✅ Hoteles cercanos encontrados: " + nearbyHotels.size() + " para ciudad: " + userCity);

        if (nearbyHotels.isEmpty()) {
            showEmptyState(tvEmptyNearby, "No hay hoteles cerca en " + locationManager.getCurrentCity());
            hideRecyclerView(rvHotels);
        } else {
            hideEmptyState(tvEmptyNearby);
            showRecyclerView(rvHotels);

            nearbyAdapter = new HotelsAdapter(nearbyHotels);
            nearbyAdapter.setOnHotelClickListener(this::onHotelClick);
            rvHotels.setAdapter(nearbyAdapter);
        }
    }

    private void loadPopularHotelsSection(List<Hotel> allHotels) {
        Log.d(TAG, "⭐ Cargando hoteles populares...");

        List<Hotel> popularHotels = HotelGroupingUtils.filterPopularHotels(allHotels);

        Log.d(TAG, "✅ Hoteles populares encontrados: " + popularHotels.size());

        if (popularHotels.isEmpty()) {
            showEmptyState(tvEmptyPopular, "No hay hoteles populares disponibles");
            hideRecyclerView(rvPopularHotels);
        } else {
            hideEmptyState(tvEmptyPopular);
            showRecyclerView(rvPopularHotels);

            popularAdapter = new PopularHotelsAdapter(popularHotels);
            popularAdapter.setOnHotelClickListener(this::onHotelClick);
            rvPopularHotels.setAdapter(popularAdapter);
        }
    }

    private void loadCitiesSection(List<Hotel> allHotels) {
        Log.d(TAG, "🏙️ Cargando ciudades disponibles...");

        // Agrupar hoteles por ciudad usando la utilidad existente
        Map<String, List<Hotel>> cityGroups = HotelGroupingUtils.groupHotelsByCityMap(allHotels);

        // Crear lista de ciudades para el CitiesAdapter original
        List<City> cityList = new ArrayList<>();

        // Imágenes predefinidas para ciudades conocidas
        Map<String, Integer> cityImages = getCityImageMap();

        for (String cityName : cityGroups.keySet()) {
            List<Hotel> cityHotels = cityGroups.get(cityName);
            if (cityHotels != null && !cityHotels.isEmpty()) {
                Integer imageRes = cityImages.get(cityName.toLowerCase());
                if (imageRes == null) {
                    imageRes = R.drawable.lima; // Imagen por defecto
                }

                City city = new City(cityName, imageRes);
                cityList.add(city);
            }
        }

        Log.d(TAG, "✅ Ciudades creadas: " + cityList.size());

        if (cityList.isEmpty()) {
            showEmptyState(tvEmptyCities, "No hay ciudades disponibles");
            hideRecyclerView(rvCities);
        } else {
            hideEmptyState(tvEmptyCities);
            showRecyclerView(rvCities);

            citiesAdapter = new CitiesAdapter(cityList);
            citiesAdapter.setOnCityClickListener(city -> navigateToCityHotels(city.getName()));
            rvCities.setAdapter(citiesAdapter);
        }
    }

    private Map<String, Integer> getCityImageMap() {
        Map<String, Integer> cityImages = new java.util.HashMap<>();
        cityImages.put("lima", R.drawable.lima);
        cityImages.put("cusco", R.drawable.cusco);
        cityImages.put("arequipa", R.drawable.arequipa);
        cityImages.put("trujillo", R.drawable.trujillo);
        cityImages.put("piura", R.drawable.piura);
        cityImages.put("iquitos", R.drawable.iquitos);
        cityImages.put("chiclayo", R.drawable.chiclayo);
        return cityImages;
    }

    // ========== EVENTOS DE CLICK Y NAVEGACIÓN (mantenidos igual) ==========

    private void onHotelClick(Hotel hotel, int position) {
        Log.d(TAG, "🏨 Hotel clickeado: " + hotel.getName());

        // ✅ ARREGLADO: Pasar fechas y huéspedes del HomeFragment
        String currentDates = selectedDates; // "Hoy - Mañana" por defecto
        String currentGuests = adults + " adultos" + (children > 0 ? " • " + children + " niños" : "");

        Log.d(TAG, "📅 Navegando con fechas: " + currentDates);
        Log.d(TAG, "👥 Navegando con huéspedes: " + currentGuests);

        // ✅ USAR EL NUEVO MÉTODO QUE INCLUYE FECHAS Y HUÉSPEDES
        NavigationManager.getInstance().navigateToHotelDetail(
                hotel.getName(),
                hotel.getLocation(),
                hotel.getPrice(),
                hotel.getRating(),
                hotel.getImageUrl(),
                UserDataManager.getInstance().getUserBundle(),
                currentDates,  // ✅ FECHAS DEL HOME
                currentGuests  // ✅ HUÉSPEDES DEL HOME
        );
    }

    private void navigateToCityHotels(String cityName) {
        Log.d(TAG, "🏙️ Navegando a hoteles de ciudad: " + cityName);

        Intent intent = new Intent(getContext(), HotelResultsActivity.class);
        intent.putExtra("search_location", cityName);
        intent.putExtra("search_dates", selectedDates);
        intent.putExtra("search_guests", adults + " adultos" + (children > 0 ? " • " + children + " niños" : ""));
        intent.putExtra("filter_type", "city_specific");
        startActivity(intent);
    }

    private void navigateToNearbyHotels() {
        Intent intent = new Intent(getContext(), HotelResultsActivity.class);
        intent.putExtra("search_location", "Tu ciudad actual");
        intent.putExtra("search_dates", selectedDates);
        intent.putExtra("search_guests", adults + " adultos" + (children > 0 ? " • " + children + " niños" : ""));
        intent.putExtra("filter_type", "nearby_hotels");
        startActivity(intent);
    }

    private void navigateToPopularHotels() {
        Intent intent = new Intent(getContext(), HotelResultsActivity.class);
        intent.putExtra("search_location", "Los más buscados");
        intent.putExtra("search_dates", selectedDates);
        intent.putExtra("search_guests", adults + " adultos" + (children > 0 ? " • " + children + " niños" : ""));
        intent.putExtra("filter_type", "popular_destinations");
        startActivity(intent);
    }


    private void navigateToAllDestinations() {
        Intent intent = new Intent(getContext(), HotelResultsActivity.class);
        intent.putExtra("search_location", "Todas las ubicaciones");
        intent.putExtra("search_dates", selectedDates);
        intent.putExtra("search_guests", adults + " adultos" + (children > 0 ? " • " + children + " niños" : ""));
        intent.putExtra("filter_type", "all_destinations");
        startActivity(intent);
    }

    private void navigateToProfileWithAnimation(View view) {
        NavigationManager.getInstance().navigateToProfile(
                UserDataManager.getInstance().getUserBundle()
        );
    }

    // ========== SELECCIÓN DE FECHAS Y HUÉSPEDES (mantenidos igual) ==========

    private void openDateSelector() {
        CustomDatePickerBottomSheet datePicker = new CustomDatePickerBottomSheet();
        datePicker.setListener((startDate, endDate) -> {
            SimpleDateFormat format = new SimpleDateFormat("dd MMM", Locale.getDefault());
            selectedDates = format.format(startDate) + " - " + format.format(endDate);
            updateSearchPanelDisplay();

            Log.d(TAG, "📅 Fechas actualizadas en HomeFragment: " + selectedDates);
        });
        datePicker.show(getParentFragmentManager(), "DatePicker");
    }

    private void openGuestSelector() {
        GuestCountBottomSheet guestSelector = new GuestCountBottomSheet();
        guestSelector.setListener((selectedAdults, selectedChildren) -> {
            adults = selectedAdults;
            children = selectedChildren;
            updateSearchPanelDisplay();

            Log.d(TAG, "👥 Huéspedes actualizados en HomeFragment: " + adults + " adultos, " + children + " niños");
        });
        guestSelector.show(getParentFragmentManager(), "GuestSelector");
    }

    private void updateSearchPanelDisplay() {
        if (tvDates != null) {
            tvDates.setText(selectedDates);
        }

        if (tvGuests != null) {
            String guestsText = adults + " adultos" + (children > 0 ? " • " + children + " niños" : "");
            tvGuests.setText(guestsText);
        }
    }

    // ========== ESTADOS DE UI (mantenidos igual) ==========

    private void showLoadingStates() {
        if (loadingNearby != null) loadingNearby.setVisibility(View.VISIBLE);
        if (loadingPopular != null) loadingPopular.setVisibility(View.VISIBLE);
        if (loadingCities != null) loadingCities.setVisibility(View.VISIBLE);

        hideAllRecyclerViews();
        hideAllEmptyStates();
    }

    private void hideLoadingStates() {
        if (loadingNearby != null) loadingNearby.setVisibility(View.GONE);
        if (loadingPopular != null) loadingPopular.setVisibility(View.GONE);
        if (loadingCities != null) loadingCities.setVisibility(View.GONE);
    }

    private void hideAllRecyclerViews() {
        hideRecyclerView(rvHotels);
        hideRecyclerView(rvPopularHotels);
        hideRecyclerView(rvCities);
    }

    private void hideAllEmptyStates() {
        hideEmptyState(tvEmptyNearby);
        hideEmptyState(tvEmptyPopular);
        hideEmptyState(tvEmptyCities);
    }

    private void showRecyclerView(RecyclerView recyclerView) {
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void hideRecyclerView(RecyclerView recyclerView) {
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void showErrorStates() {
        showEmptyState(tvEmptyNearby, "Error cargando hoteles cercanos");
        showEmptyState(tvEmptyPopular, "Error cargando hoteles populares");
        showEmptyState(tvEmptyCities, "Error cargando ciudades");
        hideAllRecyclerViews();
    }

    private void showEmptyState(TextView emptyView, String message) {
        if (emptyView != null) {
            emptyView.setText(message);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmptyState(TextView emptyView) {
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }

    // ========== LIFECYCLE ==========

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            if (data != null) {
                String selectedLocation = data.getStringExtra("selected_location");
                Log.d(TAG, "📍 Nueva ubicación seleccionada manualmente: " + selectedLocation);

                if (tvLocation != null) {
                    tvLocation.setText(selectedLocation);
                }

                loadHotelsFromFirebase();
            }
        }
    }
}