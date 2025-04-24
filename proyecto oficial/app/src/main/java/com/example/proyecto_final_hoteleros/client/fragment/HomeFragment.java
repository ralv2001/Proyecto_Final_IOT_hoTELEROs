package com.example.proyecto_final_hoteleros.client.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adapters.CitiesAdapter;
import com.example.proyecto_final_hoteleros.adapters.HotelsAdapter;
import com.example.proyecto_final_hoteleros.adapters.PopularHotelsAdapter;
import com.example.proyecto_final_hoteleros.client.model.City;
import com.example.proyecto_final_hoteleros.client.model.Hotel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
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
        rvHotels.setAdapter(new HotelsAdapter(listaDeHoteles));

        // Configuración del RecyclerView de hoteles populares (vertical)
        RecyclerView rvPopularHotels = rootView.findViewById(R.id.rvPopularHotels);
        rvPopularHotels.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
        );
        rvPopularHotels.setAdapter(new PopularHotelsAdapter(listaDeHoteles));
// Crear lista de ciudades
        List<City> listaCiudades = new ArrayList<>();
        listaCiudades.add(new City("Lima", R.drawable.lima));
        listaCiudades.add(new City("Cusco", R.drawable.cuzco));
        listaCiudades.add(new City("Arequipa", R.drawable.arequipa));
        listaCiudades.add(new City("Piura", R.drawable.inkaterra)); // Usar una imagen apropiada para Piura
        listaCiudades.add(new City("Trujillo", R.drawable.belmond)); // Usar una imagen apropiada para Trujillo

// Configuración del RecyclerView de ciudades
        RecyclerView rvCities = rootView.findViewById(R.id.rvCities);
        rvCities.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        rvCities.setAdapter(new CitiesAdapter(listaCiudades));

// Ver todo - ciudades
        TextView tvSeeAllCities = rootView.findViewById(R.id.tv_see_all_cities);
        tvSeeAllCities.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Ver todas las ciudades", Toast.LENGTH_SHORT).show();
            // Implementar navegación a una actividad o fragmento que muestre todas las ciudades
        });
        // Configuración del navegador inferior
        setupBottomNavigation(rootView);

        return rootView;
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
    }
}