package com.example.proyecto_final_hoteleros.client.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.adapters.ReservationAdapter;
import com.example.proyecto_final_hoteleros.client.model.Reservation;

import java.util.ArrayList;
import java.util.List;

public class HistorialFragment extends Fragment {
    // Variables para el navegador inferior
    private LinearLayout navHome, navExplore, navChat, navProfile;
    private ImageView ivHome, ivExplore, ivChat, ivProfile;

    // Variables para los tabs y contenido
    private TextView tabProximas, tabActuales, tabCheckout, tabCompletadas;
    private ViewFlipper viewFlipper;
    private RecyclerView recyclerProximas, recyclerActuales, recyclerCheckout, recyclerCompletadas;
    private LinearLayout emptyStateView;
    private TextView tvEmptyStateTitle, tvEmptyStateMessage;
    private ImageView ivEmptyState;

    // Adaptadores para cada lista
    private ReservationAdapter adapterProximas, adapterActuales, adapterCheckout, adapterCompletadas;

    // Listas para almacenar las reservas según su estado
    private List<Reservation> proximasReservations = new ArrayList<>();
    private List<Reservation> actualesReservations = new ArrayList<>();
    private List<Reservation> checkoutReservations = new ArrayList<>();
    private List<Reservation> completadasReservations = new ArrayList<>();

    // Listener para acciones en las tarjetas de reserva
    private final ReservationAdapter.ReservationActionListener reservationActionListener =
            new ReservationAdapter.ReservationActionListener() {
                @Override
                public void onActionButtonClicked(Reservation reservation, int position) {
                    // Implementar acciones según el estado de la reserva
                    switch (reservation.getStatus()) {
                        case Reservation.STATUS_PROXIMA:
                            // Navegar a detalles de la reserva próxima
                            // Por ejemplo: navigateToReservationDetails(reservation);
                            break;
                        case Reservation.STATUS_ACTUAL:
                            // Navegar a servicios adicionales
                            // Por ejemplo: navigateToServices(reservation);
                            break;
                        case Reservation.STATUS_CHECKOUT:
                            // Iniciar proceso de checkout
                            // Por ejemplo: startCheckoutProcess(reservation);
                            break;
                        case Reservation.STATUS_COMPLETADA:
                            // Mostrar factura
                            // Por ejemplo: showInvoice(reservation);
                            break;
                    }
                }

                @Override
                public void onReservationCardClicked(Reservation reservation, int position) {
                    // Por defecto, mostrar detalles de la reserva independientemente del estado
                    // Por ejemplo: navigateToReservationDetails(reservation);
                }
            };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_historial, container, false);

        // Configuración del navegador inferior
        setupBottomNavigation(rootView);

        // Configuración de los tabs y recyclerviews
        setupViews(rootView);

        // Configurar adaptadores con el listener definido
        setupRecyclerViews();

        // Cargar datos de ejemplo (en una app real, esto vendría de una API o base de datos)
        loadMockData();

        return rootView;
    }

    private void setupViews(View rootView) {
        // Referencias a los tabs
        tabProximas = rootView.findViewById(R.id.tabProximas);
        tabActuales = rootView.findViewById(R.id.tabActuales);
        tabCheckout = rootView.findViewById(R.id.tabCheckout);
        tabCompletadas = rootView.findViewById(R.id.tabCompletadas);
        viewFlipper = rootView.findViewById(R.id.viewFlipper);

        // Referencias a los RecyclerViews
        recyclerProximas = rootView.findViewById(R.id.recyclerProximas);
        recyclerActuales = rootView.findViewById(R.id.recyclerActuales);
        recyclerCheckout = rootView.findViewById(R.id.recyclerCheckout);
        recyclerCompletadas = rootView.findViewById(R.id.recyclerCompletadas);

        // Referencias a la vista de estado vacío
        emptyStateView = rootView.findViewById(R.id.emptyStateView);
        tvEmptyStateTitle = rootView.findViewById(R.id.tvEmptyStateTitle);
        tvEmptyStateMessage = rootView.findViewById(R.id.tvEmptyStateMessage);
        ivEmptyState = rootView.findViewById(R.id.ivEmptyState);

        // Establecer listeners para cada tab con animación de feedback
        setupTabClickListeners();

        // Establecer el tab "Próximas" como seleccionado por defecto
        updateSelectedTab(0);
    }

    private void setupTabClickListeners() {
        tabProximas.setOnClickListener(v -> {
            animateTabClick(v);
            updateSelectedTab(0);
        });

        tabActuales.setOnClickListener(v -> {
            animateTabClick(v);
            updateSelectedTab(1);
        });

        tabCheckout.setOnClickListener(v -> {
            animateTabClick(v);
            updateSelectedTab(2);
        });

        tabCompletadas.setOnClickListener(v -> {
            animateTabClick(v);
            updateSelectedTab(3);
        });
    }

    private void animateTabClick(View view) {
        // Animación sutil al hacer clic en un tab
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                );
    }

    private void setupRecyclerViews() {
        // Configurar RecyclerView para Próximas con animación
        setupRecyclerView(recyclerProximas, proximasReservations,
                ReservationAdapter.ESTADO_PROXIMA, R.anim.item_animation_from_right);

        // Configurar RecyclerView para Actuales con animación
        setupRecyclerView(recyclerActuales, actualesReservations,
                ReservationAdapter.ESTADO_ACTUAL, R.anim.item_animation_from_right);

        // Configurar RecyclerView para Checkout con animación
        setupRecyclerView(recyclerCheckout, checkoutReservations,
                ReservationAdapter.ESTADO_CHECKOUT, R.anim.item_animation_from_right);

        // Configurar RecyclerView para Completadas con animación
        setupRecyclerView(recyclerCompletadas, completadasReservations,
                ReservationAdapter.ESTADO_COMPLETADA, R.anim.item_animation_from_right);
    }

    private void setupRecyclerView(RecyclerView recyclerView, List<Reservation> dataList,
                                   int estado, int animResId) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);

        ReservationAdapter adapter = new ReservationAdapter(dataList, estado, reservationActionListener);
        recyclerView.setAdapter(adapter);

        // Aplicar animación al RecyclerView
        recyclerView.setLayoutAnimation(
                AnimationUtils.loadLayoutAnimation(requireContext(), animResId));

        // Guardar referencia al adaptador según el estado
        switch (estado) {
            case ReservationAdapter.ESTADO_PROXIMA:
                adapterProximas = adapter;
                break;
            case ReservationAdapter.ESTADO_ACTUAL:
                adapterActuales = adapter;
                break;
            case ReservationAdapter.ESTADO_CHECKOUT:
                adapterCheckout = adapter;
                break;
            case ReservationAdapter.ESTADO_COMPLETADA:
                adapterCompletadas = adapter;
                break;
        }
    }

    // Método para actualizar el tab seleccionado y su contenido
    private void updateSelectedTab(int selectedIndex) {
        // Resetear el estilo de todos los tabs
        resetTabsStyle();

        // Establecer el estilo del tab seleccionado y verificar el estado vacío
        switch (selectedIndex) {
            case 0:
                setSelectedTabStyle(tabProximas, proximasReservations,
                        "No tienes reservas próximas",
                        "¡Explora nuestros hoteles y planifica tu próxima estadía!",
                        R.drawable.ic_upcoming);
                break;
            case 1:
                setSelectedTabStyle(tabActuales, actualesReservations,
                        "No tienes reservas activas",
                        "Aquí verás tus reservas cuando inicies tu estadía.",
                        R.drawable.ic_active);
                break;
            case 2:
                setSelectedTabStyle(tabCheckout, checkoutReservations,
                        "No tienes reservas en checkout",
                        "Las reservas listas para finalizar aparecerán aquí.",
                        R.drawable.ic_checkout);
                break;
            case 3:
                setSelectedTabStyle(tabCompletadas, completadasReservations,
                        "No tienes reservas completadas",
                        "Tu historial de estadías anteriores aparecerá aquí.",
                        R.drawable.ic_completed);
                break;
        }

        // Cambiar la vista en el ViewFlipper si no está mostrando el estado vacío
        if (!isEmptyList(selectedIndex)) {
            viewFlipper.setDisplayedChild(selectedIndex);

            // Animar la aparición del contenido
            View currentView = viewFlipper.getCurrentView();
            currentView.startAnimation(
                    AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
        }
    }

    private boolean isEmptyList(int index) {
        switch (index) {
            case 0: return proximasReservations.isEmpty();
            case 1: return actualesReservations.isEmpty();
            case 2: return checkoutReservations.isEmpty();
            case 3: return completadasReservations.isEmpty();
            default: return true;
        }
    }

    private void resetTabsStyle() {
        tabProximas.setBackground(null);
        tabProximas.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        tabActuales.setBackground(null);
        tabActuales.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        tabCheckout.setBackground(null);
        tabCheckout.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        tabCompletadas.setBackground(null);
        tabCompletadas.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
    }

    private void setSelectedTabStyle(TextView tab, List<Reservation> reservations,
                                     String emptyTitle, String emptyMessage, int emptyIcon) {
        // Configurar estilo del tab seleccionado
        tab.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.tab_selected_background));
        tab.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));

        // Verificar estado vacío
        if (reservations.isEmpty()) {
            showEmptyState(emptyTitle, emptyMessage, emptyIcon);
        }
    }

    // Método para mostrar el estado vacío con los mensajes apropiados
    private void showEmptyState(String title, String message, int iconResId) {
        // Configurar contenido del estado vacío
        tvEmptyStateTitle.setText(title);
        tvEmptyStateMessage.setText(message);
        ivEmptyState.setImageResource(iconResId);

        // Asegurar que la vista de estado vacío exista en el ViewFlipper
        boolean emptyStateExists = false;
        for (int i = 0; i < viewFlipper.getChildCount(); i++) {
            if (viewFlipper.getChildAt(i) == emptyStateView) {
                emptyStateExists = true;
                break;
            }
        }

        if (!emptyStateExists) {
            // Si el emptyStateView está actualmente en algún otro contenedor, retirarlo primero
            ViewGroup parent = (ViewGroup) emptyStateView.getParent();
            if (parent != null) {
                parent.removeView(emptyStateView);
            }
            viewFlipper.addView(emptyStateView);
        }

        // Mostrar vista de estado vacío con animación
        emptyStateView.setVisibility(View.VISIBLE);
        viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(emptyStateView));
        emptyStateView.startAnimation(
                AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
    }

    // Método para cargar datos de ejemplo (en una aplicación real, esto vendría de una API o base de datos)
    private void loadMockData() {
        // Limpiar listas para evitar duplicados en caso de recarga
        proximasReservations.clear();
        actualesReservations.clear();
        checkoutReservations.clear();
        completadasReservations.clear();

        // Ejemplos de reservas próximas
        proximasReservations.add(new Reservation(
                "Gocta Lodge",
                "Chachapoyas, Gocta, Amazonas",
                "10 May - 15 May, 2025",
                900.0,
                4.9f,
                R.drawable.belmond,
                Reservation.STATUS_PROXIMA
        ));

        proximasReservations.add(new Reservation(
                "Casona del Patio",
                "Chiclayo, Lambayeque",
                "21 Jun - 25 Jun, 2025",
                750.0,
                4.7f,
                R.drawable.belmond,
                Reservation.STATUS_PROXIMA
        ));

        // Añadir un servicio adicional a la primera reserva próxima
        Reservation primerReservaProxima = proximasReservations.get(0);
        primerReservaProxima.addService("Early check-in", 50.0, 1);

        // Ejemplos de reservas actuales
        actualesReservations.add(new Reservation(
                "Hotel Costa del Sol",
                "Chiclayo, Lambayeque",
                "25 Abr - 02 May, 2025",
                1200.0,
                4.8f,
                R.drawable.belmond,
                Reservation.STATUS_ACTUAL
        ));

        // Añadir servicios a la reserva actual
        Reservation reservaActual = actualesReservations.get(0);
        reservaActual.addService("Desayuno buffet", 45.0, 2);
        reservaActual.addService("Spa", 120.0, 1);

        // Ejemplo de reserva en checkout (añadir una para mostrar cómo se ve)
        Reservation checkoutReservation = new Reservation(
                "Casa Andina Premium",
                "Miraflores, Lima",
                "20 Abr - 27 Abr, 2025",
                1350.0,
                4.5f,
                R.drawable.belmond,
                Reservation.STATUS_CHECKOUT
        );
        checkoutReservation.addService("Minibar", 85.0, 1);
        checkoutReservation.addService("Room service", 120.0, 2);
        checkoutReservation.addService("Lavandería", 45.0, 1);
        checkoutReservations.add(checkoutReservation);

        // Ejemplos de reservas completadas
        completadasReservations.add(new Reservation(
                "Belmond Hotel Monasterio",
                "Cusco, Centro Histórico",
                "05 Mar - 10 Mar, 2025",
                1500.0,
                5.0f,
                R.drawable.belmond,
                Reservation.STATUS_COMPLETADA
        ));

        completadasReservations.add(new Reservation(
                "Inkaterra Machu Picchu",
                "Aguas Calientes, Cusco",
                "10 Feb - 15 Feb, 2025",
                2000.0,
                4.9f,
                R.drawable.belmond,
                Reservation.STATUS_COMPLETADA
        ));

        // Notificar a los adaptadores sobre los cambios en los datos
        notifyAllAdapters();

        // Verificar estado inicial según el tab seleccionado
        int currentTab = viewFlipper.getDisplayedChild();
        if (currentTab < 4) { // Si es uno de los tabs normales (no el estado vacío)
            updateSelectedTab(currentTab);
        } else {
            // Por defecto, mostrar el primer tab
            updateSelectedTab(0);
        }
    }

    private void notifyAllAdapters() {
        if (adapterProximas != null) adapterProximas.notifyDataSetChanged();
        if (adapterActuales != null) adapterActuales.notifyDataSetChanged();
        if (adapterCheckout != null) adapterCheckout.notifyDataSetChanged();
        if (adapterCompletadas != null) adapterCompletadas.notifyDataSetChanged();
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

        // Marcar Explore como seleccionado
        setSelectedNavItem(navExplore, ivExplore);

        // Establecer listeners para cada elemento del navegador
        setupNavListeners();
    }

    private void setupNavListeners() {
        navHome.setOnClickListener(v -> {
            animateNavClick(v);
            if (!isCurrentFragment(HomeFragment.class)) {
                setSelectedNavItem(navHome, ivHome);
                navigateToFragment(new HomeFragment(), false);
            }
        });

        navExplore.setOnClickListener(v -> {
            animateNavClick(v);
            if (!isCurrentFragment(HistorialFragment.class)) {
                setSelectedNavItem(navExplore, ivExplore);
                // Ya estamos en Historial, no necesitamos cambiar
            }
        });

        navChat.setOnClickListener(v -> {
            animateNavClick(v);
            if (!isCurrentFragment(ChatFragment.class)) {
                setSelectedNavItem(navChat, ivChat);
                navigateToFragment(new ChatFragment(), true);
            }
        });

        navProfile.setOnClickListener(v -> {
            animateNavClick(v);
            if (!isCurrentFragment(ProfileFragment.class)) {
                setSelectedNavItem(navProfile, ivProfile);
                navigateToFragment(new ProfileFragment(), true);
            }
        });
    }

    private void animateNavClick(View view) {
        // Animación sutil al hacer clic en un elemento de navegación
        view.animate()
                .alpha(0.7f)
                .setDuration(100)
                .withEndAction(() ->
                        view.animate()
                                .alpha(1f)
                                .setDuration(100)
                );
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
}