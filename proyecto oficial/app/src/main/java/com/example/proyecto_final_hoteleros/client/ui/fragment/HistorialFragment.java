package com.example.proyecto_final_hoteleros.client.ui.fragment;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.adapters.ReservationAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.Reservation;

import java.util.ArrayList;
import java.util.List;

public class HistorialFragment extends BaseBottomNavigationFragment {

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

    @Override
    protected NavigationTab getCurrentTab() {
        return NavigationTab.EXPLORE;
    }

    // Listener para acciones en las tarjetas de reserva
    private final ReservationAdapter.ReservationActionListener reservationActionListener =
            new ReservationAdapter.ReservationActionListener() {
                @Override
                public void onActionButtonClicked(Reservation reservation, int position) {
                    switch (reservation.getStatus()) {
                        case Reservation.STATUS_PROXIMA:
                            // Navegar a detalles de la reserva próxima
                            break;
                        case Reservation.STATUS_ACTUAL:
                            // Navegar a servicios adicionales
                            break;
                        case Reservation.STATUS_CHECKOUT:
                            // Iniciar proceso de checkout
                            break;
                        case Reservation.STATUS_COMPLETADA:
                            // Mostrar factura
                            break;
                    }
                }

                @Override
                public void onReservationCardClicked(Reservation reservation, int position) {
                    // Mostrar detalles de la reserva
                }
            };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.client_fragment_historial, container, false);

        // Configuración de los tabs y recyclerviews
        setupViews(rootView);

        // Configurar adaptadores con el listener definido
        setupRecyclerViews();

        // Cargar datos de ejemplo
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

        setupTabClickListeners();
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
        setupRecyclerView(recyclerProximas, proximasReservations,
                ReservationAdapter.ESTADO_PROXIMA, R.anim.item_animation_from_right);

        setupRecyclerView(recyclerActuales, actualesReservations,
                ReservationAdapter.ESTADO_ACTUAL, R.anim.item_animation_from_right);

        setupRecyclerView(recyclerCheckout, checkoutReservations,
                ReservationAdapter.ESTADO_CHECKOUT, R.anim.item_animation_from_right);

        setupRecyclerView(recyclerCompletadas, completadasReservations,
                ReservationAdapter.ESTADO_COMPLETADA, R.anim.item_animation_from_right);
    }

    private void setupRecyclerView(RecyclerView recyclerView, List<Reservation> dataList,
                                   int estado, int animResId) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);

        ReservationAdapter adapter = new ReservationAdapter(dataList, estado, reservationActionListener);
        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutAnimation(
                AnimationUtils.loadLayoutAnimation(requireContext(), animResId));

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

    private void updateSelectedTab(int selectedIndex) {
        resetTabsStyle();

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

        if (!isEmptyList(selectedIndex)) {
            viewFlipper.setDisplayedChild(selectedIndex);
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
        tab.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.tab_selected_background));
        tab.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));

        if (reservations.isEmpty()) {
            showEmptyState(emptyTitle, emptyMessage, emptyIcon);
        }
    }

    private void showEmptyState(String title, String message, int iconResId) {
        tvEmptyStateTitle.setText(title);
        tvEmptyStateMessage.setText(message);
        ivEmptyState.setImageResource(iconResId);

        boolean emptyStateExists = false;
        for (int i = 0; i < viewFlipper.getChildCount(); i++) {
            if (viewFlipper.getChildAt(i) == emptyStateView) {
                emptyStateExists = true;
                break;
            }
        }

        if (!emptyStateExists) {
            ViewGroup parent = (ViewGroup) emptyStateView.getParent();
            if (parent != null) {
                parent.removeView(emptyStateView);
            }
            viewFlipper.addView(emptyStateView);
        }

        emptyStateView.setVisibility(View.VISIBLE);
        viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(emptyStateView));
        emptyStateView.startAnimation(
                AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
    }

    private void loadMockData() {
        proximasReservations.clear();
        actualesReservations.clear();
        checkoutReservations.clear();
        completadasReservations.clear();

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

        Reservation primerReservaProxima = proximasReservations.get(0);
        primerReservaProxima.addService("Early check-in", 50.0, 1);

        actualesReservations.add(new Reservation(
                "Hotel Costa del Sol",
                "Chiclayo, Lambayeque",
                "25 Abr - 02 May, 2025",
                1200.0,
                4.8f,
                R.drawable.belmond,
                Reservation.STATUS_ACTUAL
        ));

        Reservation reservaActual = actualesReservations.get(0);
        reservaActual.addService("Desayuno buffet", 45.0, 2);
        reservaActual.addService("Spa", 120.0, 1);

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

        notifyAllAdapters();

        int currentTab = viewFlipper.getDisplayedChild();
        if (currentTab < 4) {
            updateSelectedTab(currentTab);
        } else {
            updateSelectedTab(0);
        }
    }

    private void notifyAllAdapters() {
        if (adapterProximas != null) adapterProximas.notifyDataSetChanged();
        if (adapterActuales != null) adapterActuales.notifyDataSetChanged();
        if (adapterCheckout != null) adapterCheckout.notifyDataSetChanged();
        if (adapterCompletadas != null) adapterCompletadas.notifyDataSetChanged();
    }
}