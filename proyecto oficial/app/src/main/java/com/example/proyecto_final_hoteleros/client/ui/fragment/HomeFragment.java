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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.client.ui.activity.HotelResultsActivity;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.activity.LocationSelectorActivity;
import com.example.proyecto_final_hoteleros.client.ui.adapters.CitiesAdapter;
import com.example.proyecto_final_hoteleros.client.ui.adapters.HotelsAdapter;
import com.example.proyecto_final_hoteleros.client.ui.adapters.PopularHotelsAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.City;
import com.example.proyecto_final_hoteleros.client.data.model.Hotel;
import com.example.proyecto_final_hoteleros.client.navigation.NavigationManager;
import com.example.proyecto_final_hoteleros.client.utils.UserDataManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends BaseBottomNavigationFragment {
    private static final String TAG = "HomeFragment";
    private static final int HOTEL_RESULTS_REQUEST_CODE = 1235;
    private static final int LOCATION_REQUEST_CODE = 1234;

    // Lista de hoteles
    private List<Hotel> listaDeHoteles = new ArrayList<>();

    // Views
    private TextView tvGreeting;

    @Override
    protected NavigationTab getCurrentTab() {
        return NavigationTab.HOME;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtener datos del usuario y guardarlos en el manager
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

        // Avatar con transición
        de.hdodenhof.circleimageview.CircleImageView avatarView = rootView.findViewById(R.id.iv_avatar);
        ViewCompat.setTransitionName(avatarView, "avatar_transition");

        // Manejar clic en el icono de notificaciones
        setupNotificationClick(rootView);

        avatarView.setOnClickListener(v -> navigateToProfileWithAnimation(v));

        // Selector de ubicación
        View rowLocation = rootView.findViewById(R.id.rowLocation);
        rowLocation.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LocationSelectorActivity.class);
            startActivityForResult(intent, LOCATION_REQUEST_CODE);
        });

        // Selector de fechas
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

        // Selector de huéspedes
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

        // Animación del título
        TextView tvTitle = rootView.findViewById(R.id.tv_title);
        if (tvTitle != null) {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
        }

        // Configurar datos y RecyclerViews
        setupHotelsData();
        setupRecyclerViews(rootView);
        setupCitiesSection(rootView);
        setupSeeAllButtons(rootView);

        return rootView;
    }

    private void initViews(View view) {
        tvGreeting = view.findViewById(R.id.tv_greeting);
    }

    private void setupUserData() {
        if (tvGreeting != null) {
            // 🔥 CARGAR DATOS REALES DEL USUARIO
            loadRealUserDataForHome();
        }
    }

    // 🔥 CARGAR DATOS REALES PARA EL HOME
    private void loadRealUserDataForHome() {
        if (getActivity() instanceof com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity) {
            com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity activity =
                    (com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity) getActivity();

            String userId = activity.getUserId();
            String userName = activity.getUserName();

            // 🔥 MOSTRAR NOMBRE BÁSICO INMEDIATAMENTE
            if (userName != null && !userName.isEmpty()) {
                String greetingText = "Hola, " + userName;
                tvGreeting.setText(greetingText);
                Log.d(TAG, "Greeting básico configurado: " + greetingText);
            }

            // 🔥 CARGAR DATOS COMPLETOS DESDE FIREBASE
            if (userId != null && !userId.isEmpty()) {
                loadCompleteUserDataFromFirebase(userId);
            }
        }
    }

    // 🔥 CARGAR DATOS COMPLETOS DESDE FIREBASE PARA EL HOME
    private void loadCompleteUserDataFromFirebase(String userId) {
        Log.d(TAG, "🔄 Cargando datos completos desde Firebase para el home");

        com.example.proyecto_final_hoteleros.utils.FirebaseManager firebaseManager =
                com.example.proyecto_final_hoteleros.utils.FirebaseManager.getInstance();

        firebaseManager.getUserDataFromAnyCollection(userId, new com.example.proyecto_final_hoteleros.utils.FirebaseManager.UserCallback() {
            @Override
            public void onUserFound(com.example.proyecto_final_hoteleros.models.UserModel user) {
                Log.d(TAG, "✅ Datos del usuario obtenidos para el home");
                Log.d(TAG, "Nombre completo: " + user.getFullName());

                // 🔥 ACTUALIZAR UI EN EL HILO PRINCIPAL
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        // Actualizar saludo con nombre completo
                        String greetingText = "Hola, " + user.getNombres();
                        tvGreeting.setText(greetingText);

                        Log.d(TAG, "✅ Home actualizado con datos reales: " + greetingText);
                    });
                }
            }

            @Override
            public void onUserNotFound() {
                Log.w(TAG, "⚠️ Usuario no encontrado para el home");
                // Mantener saludo básico
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando datos para el home: " + error);
                // Mantener saludo básico
            }
        });
    }

    private void setupHotelsData() {
        listaDeHoteles.add(new Hotel("Belmond Miraflores Park",
                "Miraflores, frente al malecón, Lima",
                "drawable/belmond", "S/290", "4.9"));
        listaDeHoteles.add(new Hotel("Inkaterra Concepción",
                "Pesawaran, Lampung",
                "drawable/inkaterra", "S/300", "4.6"));
        listaDeHoteles.add(new Hotel("Skylodge",
                "Valle Sagrado, acantilado, Cusco ",
                "drawable/gocta", "S/310", "4.8"));
        listaDeHoteles.add(new Hotel("Arennas Máncora",
                "Jepara, Central Java",
                "drawable/cuzco", "S/275", "4.7"));
        listaDeHoteles.add(new Hotel("Pariwana Lima",
                "Cercado, Barrio Chino, Lima",
                "drawable/arequipa", "S/320", "4.9"));
    }

    private void setupRecyclerViews(View rootView) {
        // RecyclerView horizontal de hoteles
        RecyclerView rvHotels = rootView.findViewById(R.id.rvHotels);
        if (rvHotels != null) {
            rvHotels.setLayoutManager(
                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
            );

            HotelsAdapter hotelsAdapter = new HotelsAdapter(listaDeHoteles);
            hotelsAdapter.setOnHotelClickListener((hotel, position) -> navigateToHotelDetail(hotel));
            rvHotels.setAdapter(hotelsAdapter);
        }

        // RecyclerView vertical de hoteles populares
        RecyclerView rvPopularHotels = rootView.findViewById(R.id.rvPopularHotels);
        if (rvPopularHotels != null) {
            rvPopularHotels.setLayoutManager(
                    new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
            );

            List<Hotel> listaHotelesPopulares = new ArrayList<>();
            listaHotelesPopulares.add(new Hotel("Gocta Lodge",
                    "Chachapoyas, Gocta, Amazonas", "drawable/gocta", "S/300", "4.9"));
            listaHotelesPopulares.add(new Hotel("Arennas Máncora",
                    "Máncora, Piura", "drawable/cuzco", "S/300", "4.9"));
            listaHotelesPopulares.add(new Hotel("Inkaterra Concepción",
                    "Tambopata, Madre de Dios", "drawable/inkaterra", "S/300", "4.9"));
            listaHotelesPopulares.add(new Hotel("Skylodge",
                    "Valle Sagrado, Cusco", "drawable/gocta", "S/300", "4.9"));

            PopularHotelsAdapter popularHotelsAdapter = new PopularHotelsAdapter(listaHotelesPopulares);
            popularHotelsAdapter.setOnHotelClickListener((hotel, position) -> navigateToHotelDetail(hotel));
            rvPopularHotels.setAdapter(popularHotelsAdapter);
        }
    }

    private void setupSeeAllButtons(View rootView) {
        TextView tvSeeAllNearby = rootView.findViewById(R.id.tv_see_all);
        if (tvSeeAllNearby != null) {
            tvSeeAllNearby.setOnClickListener(v -> navigateToNearbyHotels());
        }

        TextView tvSeeAllPopular = rootView.findViewById(R.id.tv_see_all_popular);
        if (tvSeeAllPopular != null) {
            tvSeeAllPopular.setOnClickListener(v -> navigateToPopularHotels());
        }

        Button btnSearch = rootView.findViewById(R.id.btnSearch);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> performSearch());
        }
    }

    private void setupNotificationClick(View view) {
        FrameLayout notificationContainer = view.findViewById(R.id.fl_notification_container);
        if (notificationContainer != null) {
            notificationContainer.setOnClickListener(v -> navigateToNotificationsFragment());
        }
    }

    private void setupCitiesSection(View rootView) {
        List<City> listaCiudades = new ArrayList<>();
        listaCiudades.add(new City("Lima", R.drawable.lima));
        listaCiudades.add(new City("Cusco", R.drawable.cuzco));
        listaCiudades.add(new City("Arequipa", R.drawable.arequipa));
        listaCiudades.add(new City("Piura", R.drawable.inkaterra));
        listaCiudades.add(new City("Trujillo", R.drawable.belmond));

        RecyclerView rvCities = rootView.findViewById(R.id.rvCities);
        if (rvCities != null) {
            rvCities.setLayoutManager(
                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
            );

            CitiesAdapter citiesAdapter = new CitiesAdapter(listaCiudades);
            citiesAdapter.setOnCityClickListener(city -> navigateToCityHotels(city.getName()));
            rvCities.setAdapter(citiesAdapter);
        }

        TextView tvSeeAllCities = rootView.findViewById(R.id.tv_see_all_cities);
        if (tvSeeAllCities != null) {
            tvSeeAllCities.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), HotelResultsActivity.class);
                intent.putExtra("filter_type", "all_cities");
                intent.putExtra("user_id", UserDataManager.getInstance().getUserId());
                startActivity(intent);
            });
        }
    }

    // === MÉTODOS DE NAVEGACIÓN USANDO NAVIGATIONMANAGER ===

    private void navigateToHotelDetail(Hotel hotel) {
        Log.d(TAG, "Navegando a detalle del hotel: " + hotel.getName());

        // ✅ CORRECTO - Usa NavigationManager con animación SCALE_UP automática
        NavigationManager.getInstance().navigateToHotelDetail(
                hotel.getName(),
                hotel.getLocation(),
                hotel.getPrice(),
                hotel.getRating(),
                hotel.getImageUrl(),
                UserDataManager.getInstance().getUserBundle()
        );
    }

    private void navigateToNotificationsFragment() {
        NavigationManager.getInstance().navigateToNotifications(
                UserDataManager.getInstance().getUserBundle()
        );
    }

    private void navigateToProfileWithAnimation(View sharedElement) {
        // Usar NavigationManager para navegación consistente
        NavigationManager.getInstance().navigateToProfile(
                UserDataManager.getInstance().getUserBundle()
        );
    }



    // === MÉTODOS DE NAVEGACIÓN A ACTIVIDADES (se mantienen igual) ===

    private void performSearch() {
        if (getView() != null) {
            TextView tvLocation = getView().findViewById(R.id.tvLocation);
            TextView tvDates = getView().findViewById(R.id.tvDates);
            TextView tvGuests = getView().findViewById(R.id.tvGuests);

            String location = tvLocation != null ? tvLocation.getText().toString() : "";
            String dates = tvDates != null ? tvDates.getText().toString() : "";
            String guests = tvGuests != null ? tvGuests.getText().toString() : "";

            Intent intent = new Intent(getActivity(), HotelResultsActivity.class);
            intent.putExtra("filter_type", "search");
            intent.putExtra("location", location);
            intent.putExtra("dates", dates);
            intent.putExtra("guests", guests);
            intent.putExtra("user_id", UserDataManager.getInstance().getUserId());
            startActivityForResult(intent, HOTEL_RESULTS_REQUEST_CODE);
        }
    }

    private void navigateToNearbyHotels() {
        Intent intent = new Intent(getActivity(), HotelResultsActivity.class);
        intent.putExtra("filter_type", "nearby");
        intent.putExtra("user_id", UserDataManager.getInstance().getUserId());
        startActivityForResult(intent, HOTEL_RESULTS_REQUEST_CODE);
    }

    private void navigateToPopularHotels() {
        Intent intent = new Intent(getActivity(), HotelResultsActivity.class);
        intent.putExtra("filter_type", "popular");
        intent.putExtra("user_id", UserDataManager.getInstance().getUserId());
        startActivityForResult(intent, HOTEL_RESULTS_REQUEST_CODE);
    }

    private void navigateToCityHotels(String cityName) {
        Intent intent = new Intent(getActivity(), HotelResultsActivity.class);
        intent.putExtra("filter_type", "city");
        intent.putExtra("location", cityName);
        intent.putExtra("user_id", UserDataManager.getInstance().getUserId());
        startActivityForResult(intent, HOTEL_RESULTS_REQUEST_CODE);
    }

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

        if (requestCode == HOTEL_RESULTS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String hotelName = data.getStringExtra("hotel_name");
                String hotelLocation = data.getStringExtra("hotel_location");
                String hotelPrice = data.getStringExtra("hotel_price");
                String hotelRating = data.getStringExtra("hotel_rating");
                String hotelImage = data.getStringExtra("hotel_image");

                Hotel selectedHotel = new Hotel(hotelName, hotelLocation, hotelImage, hotelPrice, hotelRating);
                navigateToHotelDetail(selectedHotel);
            }
        }
    }

    // === MÉTODOS PÚBLICOS PARA ACCESO A DATOS ===

    public String getUserName() {
        return UserDataManager.getInstance().getUserName();
    }

    public String getUserFullName() {
        return UserDataManager.getInstance().getUserFullName();
    }

    public String getUserId() {
        return UserDataManager.getInstance().getUserId();
    }

    public String getUserEmail() {
        return UserDataManager.getInstance().getUserEmail();
    }

    public String getUserType() {
        return UserDataManager.getInstance().getUserType();
    }

    public void updateUserData(String userId, String userName, String userFullName, String userEmail, String userType) {
        UserDataManager.getInstance().setUserData(userId, userName, userFullName, userEmail, userType);
        setupUserData();
        Log.d(TAG, "Datos de usuario actualizados");
    }
}