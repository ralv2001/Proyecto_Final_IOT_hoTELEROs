package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.adapters.ReservationAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.Reservation;
import com.example.proyecto_final_hoteleros.client.navigation.NavigationManager;
import com.example.proyecto_final_hoteleros.client.utils.UserDataManager;
import java.util.ArrayList;
import java.util.List;

public class HistorialFragment extends BaseBottomNavigationFragment {

    // ✅ SOLO 3 TABS (sin checkout)
    private TextView tabProximas, tabActuales, tabCompletadas;
    private ViewFlipper viewFlipper;
    private RecyclerView recyclerProximas, recyclerActuales, recyclerCompletadas;
    private LinearLayout emptyStateView;
    private TextView tvEmptyStateTitle, tvEmptyStateMessage;
    private ImageView ivEmptyState;

    // ✅ SOLO 3 ADAPTADORES
    private ReservationAdapter adapterProximas, adapterActuales, adapterCompletadas;

    // ✅ SOLO 3 LISTAS
    private List<Reservation> proximasReservations = new ArrayList<>();
    private List<Reservation> actualesReservations = new ArrayList<>();
    private List<Reservation> completadasReservations = new ArrayList<>();

    @Override
    protected NavigationTab getCurrentTab() {
        return NavigationTab.EXPLORE;
    }

    // ✅ LISTENER MEJORADO con checkout
    private final ReservationAdapter.ReservationActionListener reservationActionListener =
            new ReservationAdapter.ReservationActionListener() {
                @Override
                public void onActionButtonClicked(Reservation reservation, int position) {
                    switch (reservation.getStatus()) {
                        case Reservation.STATUS_PROXIMA:
                            navigateToReservationDetails(reservation);
                            break;
                        case Reservation.STATUS_ACTUAL:
                            navigateToServices(reservation);
                            break;
                        case Reservation.STATUS_COMPLETADA:
                            showInvoice(reservation);
                            break;
                    }
                }

                @Override
                public void onReservationCardClicked(Reservation reservation, int position) {
                    navigateToReservationDetails(reservation);
                }

                @Override
                public void onCheckoutRequested(Reservation reservation, int position) {
                    // ✅ NUEVO: Manejar solicitud de checkout
                    showCheckoutConfirmation(reservation, position);
                }
            };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.client_fragment_historial, container, false);
        setupViews(rootView);
        setupRecyclerViews();
        loadMockData();
        return rootView;
    }

    private void setupViews(View rootView) {
        // ✅ SOLO 3 TABS
        tabProximas = rootView.findViewById(R.id.tabProximas);
        tabActuales = rootView.findViewById(R.id.tabActuales);
        tabCompletadas = rootView.findViewById(R.id.tabCompletadas);
        viewFlipper = rootView.findViewById(R.id.viewFlipper);

        // ✅ SOLO 3 RECYCLERS
        recyclerProximas = rootView.findViewById(R.id.recyclerProximas);
        recyclerActuales = rootView.findViewById(R.id.recyclerActuales);
        recyclerCompletadas = rootView.findViewById(R.id.recyclerCompletadas);

        // Estado vacío
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

        tabCompletadas.setOnClickListener(v -> {
            animateTabClick(v);
            updateSelectedTab(2);
        });
    }

    // ✅ ANIMACIÓN MEJORADA para tabs
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
        // ✅ SOLO 3 RECYCLERS
        setupRecyclerView(recyclerProximas, proximasReservations, ReservationAdapter.ESTADO_PROXIMA);
        setupRecyclerView(recyclerActuales, actualesReservations, ReservationAdapter.ESTADO_ACTUAL);
        setupRecyclerView(recyclerCompletadas, completadasReservations, ReservationAdapter.ESTADO_COMPLETADA);
    }

    private void setupRecyclerView(RecyclerView recyclerView, List<Reservation> dataList, int estado) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);

        ReservationAdapter adapter = new ReservationAdapter(dataList, estado, reservationActionListener);
        recyclerView.setAdapter(adapter);

        // Guardar referencia
        switch (estado) {
            case ReservationAdapter.ESTADO_PROXIMA:
                adapterProximas = adapter;
                break;
            case ReservationAdapter.ESTADO_ACTUAL:
                adapterActuales = adapter;
                break;
            case ReservationAdapter.ESTADO_COMPLETADA:
                adapterCompletadas = adapter;
                break;
        }
    }

    // ✅ ACTUALIZADO: Solo 3 tabs
    private void updateSelectedTab(int selectedIndex) {
        resetTabsStyle();

        // ✅ ANIMACIÓN SUAVE entre pestañas
        Animation fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out);
        Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in);

        View currentView = viewFlipper.getCurrentView();
        if (currentView != null) {
            currentView.startAnimation(fadeOut);
        }

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
                setSelectedTabStyle(tabCompletadas, completadasReservations,
                        "No tienes reservas completadas",
                        "Tu historial de estadías anteriores aparecerá aquí.",
                        R.drawable.ic_completed);
                break;
        }

        if (!isEmptyList(selectedIndex)) {
            viewFlipper.setDisplayedChild(selectedIndex);

            // ✅ ANIMACIÓN SUAVE al cambiar
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {}
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            viewFlipper.getCurrentView().startAnimation(fadeIn);
        }
    }

    private boolean isEmptyList(int index) {
        switch (index) {
            case 0: return proximasReservations.isEmpty();
            case 1: return actualesReservations.isEmpty();
            case 2: return completadasReservations.isEmpty();
            default: return true;
        }
    }

    private void resetTabsStyle() {
        // ✅ SOLO 3 TABS
        tabProximas.setBackground(null);
        tabProximas.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        tabActuales.setBackground(null);
        tabActuales.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
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
        emptyStateView.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
    }

    // ✅ DATOS DE PRUEBA ACTUALIZADOS
    private void loadMockData() {
        proximasReservations.clear();
        actualesReservations.clear();
        completadasReservations.clear();

        // PRÓXIMAS
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

        // ACTUALES
        Reservation reservaActual1 = new Reservation(
                "Hotel Costa del Sol",
                "Chiclayo, Lambayeque",
                "25 Abr - 02 May, 2025",
                1200.0,
                4.8f,
                R.drawable.belmond,
                Reservation.STATUS_ACTUAL
        );
        reservaActual1.addService("Desayuno buffet", 45.0, 2);
        reservaActual1.addService("Spa", 120.0, 1);
        actualesReservations.add(reservaActual1);

        // ✅ NUEVA: Reserva actual LISTA PARA CHECKOUT
        Reservation reservaListaCheckout = new Reservation(
                "Casa Andina Premium",
                "Miraflores, Lima",
                "20 Abr - 27 Abr, 2025",
                1350.0,
                4.5f,
                R.drawable.belmond,
                Reservation.STATUS_ACTUAL
        );
        reservaListaCheckout.addService("Minibar", 85.0, 1);
        reservaListaCheckout.addService("Room service", 120.0, 2);
        reservaListaCheckout.addService("Lavandería", 45.0, 1);
        reservaListaCheckout.setReadyForCheckout(true); // ✅ LISTA PARA CHECKOUT
        actualesReservations.add(reservaListaCheckout);

        // COMPLETADAS
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
        updateSelectedTab(0);
    }

    private void notifyAllAdapters() {
        if (adapterProximas != null) adapterProximas.notifyDataSetChanged();
        if (adapterActuales != null) adapterActuales.notifyDataSetChanged();
        if (adapterCompletadas != null) adapterCompletadas.notifyDataSetChanged();
    }

    // ✅ NUEVOS MÉTODOS DE NAVEGACIÓN
    private void navigateToReservationDetails(Reservation reservation) {
        // Usar BookingSummary reutilizado como base para detalles
        Bundle args = UserDataManager.getInstance().getUserBundle();
        args.putString("hotel_name", reservation.getHotelName());
        args.putString("hotel_address", reservation.getLocation());
        args.putString("hotel_price", String.valueOf(reservation.getPrice()));
        args.putString("hotel_rating", String.valueOf(reservation.getRating()));
        args.putInt("hotel_image", reservation.getImageResource());
        args.putString("reservation_id", reservation.getReservationId());
        args.putString("room_type", reservation.getRoomType());
        args.putBoolean("view_mode", true); // Solo vista, no editable

        NavigationManager.getInstance().navigateToBookingSummary(args);
    }

    private void navigateToServices(Reservation reservation) {
        // Navegar a servicios del hotel
        NavigationManager.getInstance().navigateToHotelDetail(
                reservation.getHotelName(),
                reservation.getLocation(),
                String.valueOf(reservation.getPrice()),
                String.valueOf(reservation.getRating()),
                String.valueOf(reservation.getImageResource()),
                UserDataManager.getInstance().getUserBundle()
        );
    }

    private void showInvoice(Reservation reservation) {
        // Mostrar factura/resumen final
        Bundle args = UserDataManager.getInstance().getUserBundle();
        args.putString("hotel_name", reservation.getHotelName());
        args.putString("total_amount", String.valueOf(reservation.getTotalPrice()));
        args.putString("services_breakdown", reservation.getServicesBreakdown());
        args.putString("reservation_id", reservation.getReservationId());
        args.putBoolean("invoice_mode", true);

        NavigationManager.getInstance().navigateToBookingSummary(args);
    }

    // ✅ NUEVO: Confirmar checkout
    private void showCheckoutConfirmation(Reservation reservation, int position) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Confirmar Checkout")
                .setMessage("¿Está seguro que desea realizar el checkout de " + reservation.getHotelName() + "?\n\n" +
                        "Total a cobrar: S/" + reservation.getTotalPrice())
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    performCheckout(reservation, position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void performCheckout(Reservation reservation, int position) {
        // ✅ Realizar checkout: mover de ACTUAL a COMPLETADA
        reservation.performCheckout();

        // Remover de actuales
        actualesReservations.remove(position);
        adapterActuales.notifyItemRemoved(position);

        // Agregar a completadas
        completadasReservations.add(0, reservation); // Al inicio
        if (adapterCompletadas != null) {
            adapterCompletadas.notifyItemInserted(0);
        }

        android.widget.Toast.makeText(requireContext(),
                "Checkout realizado exitosamente",
                android.widget.Toast.LENGTH_SHORT).show();

        // Si la lista actual queda vacía, mostrar estado vacío
        if (actualesReservations.isEmpty()) {
            updateSelectedTab(1); // Refrescar tab actual
        }
    }
}