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
import com.example.proyecto_final_hoteleros.client.navigation.AnimationDirection;
import com.example.proyecto_final_hoteleros.client.navigation.NavigationManager;
import com.example.proyecto_final_hoteleros.client.ui.fragment.ReservationDetailFragment;
import java.util.ArrayList;
import java.util.List;

public class HistorialFragment extends BaseBottomNavigationFragment {

    // ✅ SOLO 3 TABS (manteniendo diseño original)
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

    // ✅ LISTENER SIMPLIFICADO - TODAS LAS ACCIONES VAN AL FRAGMENTO DE DETALLES
    private final ReservationAdapter.ReservationActionListener reservationActionListener =
            new ReservationAdapter.ReservationActionListener() {
                @Override
                public void onActionButtonClicked(Reservation reservation, int position) {
                    navigateToReservationDetails(reservation);
                }

                @Override
                public void onReservationCardClicked(Reservation reservation, int position) {
                    navigateToReservationDetails(reservation);
                }

                @Override
                public void onModifyReservation(Reservation reservation, int position) {
                    navigateToReservationDetails(reservation);
                }

                @Override
                public void onCheckoutRequested(Reservation reservation, int position) {
                    // ✅ CAMBIAR SUB-ESTADO SIN MOVER DE LISTA
                    performCheckoutRequest(reservation, position);
                }

                @Override
                public void onViewBill(Reservation reservation, int position) {
                    navigateToReservationDetails(reservation);
                }

                @Override
                public void onSubmitReview(Reservation reservation, int position) {
                    navigateToReservationDetails(reservation);
                }

                @Override
                public void onViewInvoice(Reservation reservation, int position) {
                    navigateToReservationDetails(reservation);
                }
            };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.client_fragment_historial, container, false);
        setupViews(rootView);
        setupRecyclerViews();
        loadProfessionalMockData();
        return rootView;
    }

    private void setupViews(View rootView) {
        // ✅ SOLO 3 TABS (manteniendo nombres originales)
        tabProximas = rootView.findViewById(R.id.tabProximas);
        tabActuales = rootView.findViewById(R.id.tabActuales);
        tabCompletadas = rootView.findViewById(R.id.tabCompletadas);
        viewFlipper = rootView.findViewById(R.id.viewFlipper);

        // ✅ SOLO 3 RECYCLERS (manteniendo nombres originales)
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
        // ✅ SOLO 3 RECYCLERS (usando constantes actualizadas)
        setupRecyclerView(recyclerProximas, proximasReservations, ReservationAdapter.ESTADO_UPCOMING);
        setupRecyclerView(recyclerActuales, actualesReservations, ReservationAdapter.ESTADO_ACTIVE);
        setupRecyclerView(recyclerCompletadas, completadasReservations, ReservationAdapter.ESTADO_COMPLETED);
    }

    private void setupRecyclerView(RecyclerView recyclerView, List<Reservation> dataList, int estado) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);

        ReservationAdapter adapter = new ReservationAdapter(dataList, estado, reservationActionListener);
        recyclerView.setAdapter(adapter);

        // Guardar referencia
        switch (estado) {
            case ReservationAdapter.ESTADO_UPCOMING:
                adapterProximas = adapter;
                break;
            case ReservationAdapter.ESTADO_ACTIVE:
                adapterActuales = adapter;
                break;
            case ReservationAdapter.ESTADO_COMPLETED:
                adapterCompletadas = adapter;
                break;
        }
    }

    private void updateSelectedTab(int selectedIndex) {
        resetTabsStyle();

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
                        "No tienes estadías activas",
                        "Aquí verás tus reservas cuando hagas check-in.",
                        R.drawable.ic_active);
                break;
            case 2:
                setSelectedTabStyle(tabCompletadas, completadasReservations,
                        "No tienes estadías completadas",
                        "Tu historial de estadías anteriores aparecerá aquí.",
                        R.drawable.ic_completed);
                break;
        }

        if (!isEmptyList(selectedIndex)) {
            viewFlipper.setDisplayedChild(selectedIndex);
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

    // ✅ DATOS DE PRUEBA PROFESIONALES CON SUB-ESTADOS
    private void loadProfessionalMockData() {
        clearAllData();

        // ✅ PRÓXIMAS
        Reservation upcoming1 = createUpcomingReservation(
                "Gocta Lodge",
                "Chachapoyas, Gocta, Amazonas",
                "10 May - 15 May, 2025",
                900.0,
                4.9f
        );
        upcoming1.setSpecialRequests("Vista al bosque, llegada tardía");
        proximasReservations.add(upcoming1);

        Reservation upcoming2 = createUpcomingReservation(
                "Casona del Patio",
                "Chiclayo, Lambayeque",
                "21 Jun - 25 Jun, 2025",
                750.0,
                4.7f
        );
        proximasReservations.add(upcoming2);

        Reservation upcoming3 = createUpcomingReservation(
                "Hotel Presidente",
                "Arequipa, Centro Histórico",
                "05 Jul - 08 Jul, 2025",
                650.0,
                4.4f
        );
        proximasReservations.add(upcoming3);

        // ✅ ACTUALES CON DIFERENTES SUB-ESTADOS

        // 1. Estadía normal (recién llegado)
        Reservation active1 = createActiveReservation(
                "Hotel Costa del Sol",
                "Chiclayo, Lambayeque",
                "25 Abr - 02 May, 2025",
                1200.0,
                4.8f
        );
        active1.setSubStatus(Reservation.SUBSTATUS_CHECKED_IN); // Recién llegó
        active1.addService("Welcome drink", 25.0, 2);
        actualesReservations.add(active1);

        // 2. Estadía en curso con servicios
        Reservation active2 = createActiveReservation(
                "Hotel Libertador",
                "Cusco, San Blas",
                "28 Abr - 03 May, 2025",
                980.0,
                4.6f
        );
        active2.setSubStatus(Reservation.SUBSTATUS_STAYING); // Estadía normal
        active2.addService("Desayuno buffet", 45.0, 3);
        active2.addService("Spa relajante", 120.0, 1);
        active2.addService("Minibar", 35.0, 2);
        actualesReservations.add(active2);

        // 3. ✅ CHECKOUT PENDIENTE (SUB-ESTADO, NO LISTA SEPARADA)
        Reservation activePending = createActiveReservation(
                "Casa Andina Premium",
                "Miraflores, Lima",
                "20 Abr - 27 Abr, 2025",
                1350.0,
                4.5f
        );
        activePending.setSubStatus(Reservation.SUBSTATUS_CHECKOUT_PENDING); // ✅ Checkout solicitado
        activePending.addService("Room service", 120.0, 2);
        activePending.addService("Lavandería express", 45.0, 1);
        activePending.addService("Minibar premium", 85.0, 1);
        // ✅ SIMULAR CARGO ADICIONAL POR DAÑO
        activePending.addAdditionalCharge("Reparación menor de mueble", 150.0, "Daño accidental reportado");
        actualesReservations.add(activePending);

        // ✅ COMPLETADAS (Con y sin reviews)
        Reservation completed1 = createCompletedReservation(
                "Belmond Hotel Monasterio",
                "Cusco, Centro Histórico",
                "05 Mar - 10 Mar, 2025",
                1500.0,
                5.0f
        );
        completed1.setReviewSubmitted(true); // Ya tiene review
        completadasReservations.add(completed1);

        Reservation completed2 = createCompletedReservation(
                "Inkaterra Machu Picchu",
                "Aguas Calientes, Cusco",
                "10 Feb - 15 Feb, 2025",
                2000.0,
                4.9f
        );
        // Esta aún no tiene review
        completadasReservations.add(completed2);

        Reservation completed3 = createCompletedReservation(
                "JW Marriott Lima",
                "Miraflores, Lima",
                "15 Ene - 20 Ene, 2025",
                1800.0,
                4.7f
        );
        completed3.setReviewSubmitted(true);
        completadasReservations.add(completed3);

        notifyAllAdapters();
        updateSelectedTab(0);
    }

    // ✅ MÉTODOS DE CREACIÓN DE RESERVAS PROFESIONALES
    private Reservation createUpcomingReservation(String name, String location, String date, double price, float rating) {
        Reservation reservation = new Reservation(name, location, date, price, rating, R.drawable.belmond, Reservation.STATUS_UPCOMING);
        reservation.setRoomType("Suite Deluxe");
        reservation.setRoomNumber("20" + (int)(Math.random() * 99));

        // Simular tarjeta de garantía
        Reservation.PaymentMethod card = new Reservation.PaymentMethod("4589", "Visa", "Juan Pérez");
        reservation.setGuaranteeCard(card);

        return reservation;
    }

    private Reservation createActiveReservation(String name, String location, String date, double price, float rating) {
        Reservation reservation = new Reservation(name, location, date, price, rating, R.drawable.belmond, Reservation.STATUS_ACTIVE);
        reservation.setRoomType("Suite Junior");
        reservation.setRoomNumber("15" + (int)(Math.random() * 99));
        reservation.setActualCheckInTime(new java.util.Date(System.currentTimeMillis() - 86400000)); // Ayer

        // Tarjeta de garantía
        Reservation.PaymentMethod card = new Reservation.PaymentMethod("7854", "Mastercard", "Juan Pérez");
        reservation.setGuaranteeCard(card);

        return reservation;
    }

    private Reservation createCompletedReservation(String name, String location, String date, double price, float rating) {
        Reservation reservation = new Reservation(name, location, date, price, rating, R.drawable.belmond, Reservation.STATUS_COMPLETED);
        reservation.setRoomType("Suite Ejecutiva");
        reservation.setRoomNumber("10" + (int)(Math.random() * 99));

        // Tarjeta usada
        Reservation.PaymentMethod card = new Reservation.PaymentMethod("2341", "Visa", "Juan Pérez");
        reservation.setGuaranteeCard(card);

        return reservation;
    }

    private void clearAllData() {
        proximasReservations.clear();
        actualesReservations.clear();
        completadasReservations.clear();
    }

    private void notifyAllAdapters() {
        if (adapterProximas != null) adapterProximas.notifyDataSetChanged();
        if (adapterActuales != null) adapterActuales.notifyDataSetChanged();
        if (adapterCompletadas != null) adapterCompletadas.notifyDataSetChanged();
    }

    // ✅ MÉTODO PRINCIPAL DE NAVEGACIÓN - TODO VA AQUÍ
    private void navigateToReservationDetails(Reservation reservation) {
        Bundle args = new Bundle();
        args.putParcelable("reservation", reservation); // ✅ PASAR OBJETO COMPLETO
        args.putBoolean("view_mode", true);

        ReservationDetailFragment fragment = new ReservationDetailFragment();
        fragment.setArguments(args);

        // ✅ USAR NAVIGATIONMANAGER CON ANIMACIÓN SCALE_UP (como si fuera modal)
        NavigationManager.getInstance().navigateWithCustomAnimation(
                fragment,
                AnimationDirection.SCALE_UP,
                true
        );
    }

    // ✅ MÉTODO PARA CHECKOUT SIN CAMBIAR DE TAB
    private void performCheckoutRequest(Reservation reservation, int position) {
        // ✅ CAMBIAR SUB-ESTADO EN LUGAR DE MOVER DE LISTA
        reservation.requestCheckout();

        // ✅ NOTIFICAR CAMBIO EN LA POSICIÓN ESPECÍFICA
        adapterActuales.notifyItemChanged(position);

        // ✅ MOSTRAR MENSAJE DE CONFIRMACIÓN
        android.widget.Toast.makeText(requireContext(),
                "Checkout solicitado. El hotel lo revisará pronto y te notificará.",
                android.widget.Toast.LENGTH_LONG).show();
    }

    // ✅ MÉTODO PARA ACTUALIZAR UNA RESERVA ESPECÍFICA (útil para callbacks del fragment de detalles)
    public void updateReservation(Reservation updatedReservation) {
        updateReservationInList(proximasReservations, updatedReservation, adapterProximas);
        updateReservationInList(actualesReservations, updatedReservation, adapterActuales);
        updateReservationInList(completadasReservations, updatedReservation, adapterCompletadas);
    }

    private void updateReservationInList(List<Reservation> list, Reservation updatedReservation, ReservationAdapter adapter) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getReservationId().equals(updatedReservation.getReservationId())) {
                list.set(i, updatedReservation);
                if (adapter != null) {
                    adapter.notifyItemChanged(i);
                }
                break;
            }
        }
    }

    // ✅ MÉTODO PARA MOVER RESERVA ENTRE LISTAS (cuando admin aprueba checkout)
    public void moveReservationToCompleted(String reservationId) {
        for (int i = 0; i < actualesReservations.size(); i++) {
            Reservation reservation = actualesReservations.get(i);
            if (reservation.getReservationId().equals(reservationId)) {
                // Remover de actuales
                actualesReservations.remove(i);
                adapterActuales.notifyItemRemoved(i);

                // Agregar a completadas
                reservation.approveCheckout(); // Cambiar estado
                completadasReservations.add(0, reservation);
                if (adapterCompletadas != null) {
                    adapterCompletadas.notifyItemInserted(0);
                }

                android.widget.Toast.makeText(requireContext(),
                        "Checkout aprobado. Estadía completada exitosamente.",
                        android.widget.Toast.LENGTH_LONG).show();

                if (actualesReservations.isEmpty()) {
                    updateSelectedTab(1);
                }
                break;
            }
        }
    }

    public void refreshReservations() {
        loadProfessionalMockData();
    }

    public Reservation getReservationById(String reservationId) {
        for (Reservation reservation : proximasReservations) {
            if (reservation.getReservationId().equals(reservationId)) {
                return reservation;
            }
        }
        for (Reservation reservation : actualesReservations) {
            if (reservation.getReservationId().equals(reservationId)) {
                return reservation;
            }
        }
        for (Reservation reservation : completadasReservations) {
            if (reservation.getReservationId().equals(reservationId)) {
                return reservation;
            }
        }
        return null;
    }
}