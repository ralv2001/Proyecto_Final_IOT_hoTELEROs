package com.example.proyecto_final_hoteleros.client.ui.fragment;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.adapters.ReservationAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.Reservation;
import com.example.proyecto_final_hoteleros.client.navigation.AnimationDirection;
import com.example.proyecto_final_hoteleros.client.navigation.NavigationManager;
import com.example.proyecto_final_hoteleros.client.ui.fragment.ReservationDetailFragment;
import com.example.proyecto_final_hoteleros.client.utils.UserDataManager;
import com.example.proyecto_final_hoteleros.client.utils.ReservationDebugManager;
import com.example.proyecto_final_hoteleros.client.ui.dialog.ReservationDebugDialog;

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

    // ✅ NUEVO: SwipeRefreshLayout para actualización manual
    private SwipeRefreshLayout swipeRefreshLayout;

    // ✅ NUEVO: Botón de debug (solo visible en modo debug)
    private TextView tvDebugMode;

    // ✅ SOLO 3 ADAPTADORES
    private ReservationAdapter adapterProximas, adapterActuales, adapterCompletadas;

    // ✅ SOLO 3 LISTAS
    private List<Reservation> proximasReservations = new ArrayList<>();
    private List<Reservation> actualesReservations = new ArrayList<>();
    private List<Reservation> completadasReservations = new ArrayList<>();

    // ✅ ESTADO DE CARGA
    private boolean isLoading = false;
    private int currentSelectedTab = 0;

    // ✅ NUEVO: Debug manager
    private ReservationDebugManager debugManager;

    @Override
    protected NavigationTab getCurrentTab() {
        return NavigationTab.EXPLORE;
    }

    // ✅ LISTENER SIMPLIFICADO CON DEBUG - TODAS LAS ACCIONES VAN AL FRAGMENTO DE DETALLES
    private final ReservationAdapter.ReservationActionListener reservationActionListener =
            new ReservationAdapter.ReservationActionListener() {
                @Override
                public void onActionButtonClicked(Reservation reservation, int position) {
                    handleActionButtonClick(reservation, position);
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

        // ✅ INICIALIZAR DEBUG MANAGER
        debugManager = ReservationDebugManager.getInstance();

        setupViews(rootView);
        setupRecyclerViews();
        setupSwipeRefresh(rootView);
        setupDebugMode(rootView);

        // ✅ CARGAR RESERVAS CON DELAY PARA ASEGURAR QUE LA UI ESTÉ LISTA
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            loadReservationsFromFirebase();
        }, 300);

        return rootView;
    }

    // ✅ NUEVO: Configurar SwipeRefreshLayout
    private void setupSwipeRefresh(View rootView) {
        // Buscar SwipeRefreshLayout si existe en el layout
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::refreshReservationsManually);
            swipeRefreshLayout.setColorSchemeColors(
                    ContextCompat.getColor(requireContext(), R.color.orange_primary)
            );
        }
    }

    // ✅ NUEVO: Configurar modo debug
    private void setupDebugMode(View rootView) {
        tvDebugMode = rootView.findViewById(R.id.tv_debug_mode);

        if (debugManager.isDebugModeEnabled()) {
            if (tvDebugMode != null) {
                tvDebugMode.setVisibility(View.VISIBLE);
                tvDebugMode.setText("🐛 DEBUG MODE");
                tvDebugMode.setOnClickListener(v -> showDebugOptions());
            }

            Log.d(TAG, "🐛 Debug mode habilitado - Long press en reservas para debug");
        } else {
            if (tvDebugMode != null) {
                tvDebugMode.setVisibility(View.GONE);
            }
        }
    }

    // ✅ NUEVO: Mostrar opciones de debug
    private void showDebugOptions() {
        if (!debugManager.isDebugModeEnabled()) {
            android.widget.Toast.makeText(requireContext(),
                    "❌ Modo debug deshabilitado",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear una reserva de testing rápida
        Reservation testReservation = debugManager.createTestReservation("Hotel Debug Test", 500.0);
        showDebugDialog(testReservation);
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

        // ✅ NUEVO: Configurar long click para debug
        if (debugManager.isDebugModeEnabled()) {
            setupDebugLongClickListener(recyclerView, dataList);
        }

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

    // ✅ NUEVO: Configurar long click listener para debug
    private void setupDebugLongClickListener(RecyclerView recyclerView, List<Reservation> dataList) {
        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            private android.view.GestureDetector gestureDetector = new android.view.GestureDetector(
                    requireContext(),
                    new android.view.GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public void onLongPress(android.view.MotionEvent e) {
                            View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                            if (child != null) {
                                int position = recyclerView.getChildAdapterPosition(child);
                                if (position >= 0 && position < dataList.size()) {
                                    Reservation reservation = dataList.get(position);
                                    Log.d(TAG, "🐛 Long press en reserva: " + reservation.getHotelName());
                                    showDebugDialog(reservation);
                                }
                            }
                        }
                    }
            );

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, android.view.MotionEvent e) {
                gestureDetector.onTouchEvent(e);
                return false;
            }
        });
    }

    // ✅ NUEVO: Mostrar diálogo de debug
    private void showDebugDialog(Reservation reservation) {
        if (!debugManager.isDebugModeEnabled()) {
            return;
        }

        if (getChildFragmentManager().isStateSaved()) {
            Log.w(TAG, "⚠️ FragmentManager en estado guardado, no mostrar diálogo debug");
            return;
        }

        Log.d(TAG, "🐛 Mostrando diálogo de debug para: " + reservation.getHotelName());

        ReservationDebugDialog dialog = ReservationDebugDialog.newInstance(reservation);
        dialog.setDebugActionListener(new ReservationDebugDialog.DebugActionListener() {
            @Override
            public void onReservationStateChanged(Reservation reservation) {
                Log.d(TAG, "🔄 Estado cambiado via debug: " + debugManager.getDetailedStatus(reservation));

                // ✅ USAR MÉTODO MEJORADO DE ACTUALIZACIÓN
                updateReservationInAllLists(reservation);

                // ✅ FORZAR REFRESH COMPLETO DE LA UI
                forceUIRefresh();

                android.widget.Toast.makeText(requireContext(),
                        "🐛 Estado actualizado: " + debugManager.getDetailedStatus(reservation),
                        android.widget.Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFlowSimulationStarted(Reservation reservation) {
                Log.d(TAG, "🎬 Iniciando simulación de flujo para: " + reservation.getHotelName());
                android.widget.Toast.makeText(requireContext(),
                        "🎬 Iniciando simulación...",
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        try {
            dialog.show(getChildFragmentManager(), "ReservationDebugDialog");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error mostrando diálogo de debug: " + e.getMessage());
        }
    }

    // ✅ NUEVO: Actualizar reserva en todas las listas con mejor lógica
    private void updateReservationInAllLists(Reservation updatedReservation) {
        Log.d(TAG, "🔄 Actualizando reserva: " + updatedReservation.getHotelName() + " a estado: " + updatedReservation.getStatusText());

        // Buscar y remover de todas las listas primero
        boolean foundAndRemoved = false;
        int oldPosition = -1;
        List<Reservation> oldList = null;
        ReservationAdapter oldAdapter = null;

        // Buscar en próximas
        for (int i = 0; i < proximasReservations.size(); i++) {
            if (isSameReservation(proximasReservations.get(i), updatedReservation)) {
                oldList = proximasReservations;
                oldAdapter = adapterProximas;
                oldPosition = i;
                proximasReservations.remove(i);
                foundAndRemoved = true;
                Log.d(TAG, "   📤 Removida de PRÓXIMAS en posición " + i);
                break;
            }
        }

        // Buscar en actuales
        if (!foundAndRemoved) {
            for (int i = 0; i < actualesReservations.size(); i++) {
                if (isSameReservation(actualesReservations.get(i), updatedReservation)) {
                    oldList = actualesReservations;
                    oldAdapter = adapterActuales;
                    oldPosition = i;
                    actualesReservations.remove(i);
                    foundAndRemoved = true;
                    Log.d(TAG, "   📤 Removida de ACTUALES en posición " + i);
                    break;
                }
            }
        }

        // Buscar en completadas
        if (!foundAndRemoved) {
            for (int i = 0; i < completadasReservations.size(); i++) {
                if (isSameReservation(completadasReservations.get(i), updatedReservation)) {
                    oldList = completadasReservations;
                    oldAdapter = adapterCompletadas;
                    oldPosition = i;
                    completadasReservations.remove(i);
                    foundAndRemoved = true;
                    Log.d(TAG, "   📤 Removida de COMPLETADAS en posición " + i);
                    break;
                }
            }
        }

        // Agregar a la lista correcta según el nuevo estado
        List<Reservation> newList = null;
        ReservationAdapter newAdapter = null;
        String newListName = "";

        switch (updatedReservation.getStatus()) {
            case Reservation.STATUS_UPCOMING:
                newList = proximasReservations;
                newAdapter = adapterProximas;
                newListName = "PRÓXIMAS";
                break;
            case Reservation.STATUS_ACTIVE:
                newList = actualesReservations;
                newAdapter = adapterActuales;
                newListName = "ACTUALES";
                break;
            case Reservation.STATUS_COMPLETED:
                newList = completadasReservations;
                newAdapter = adapterCompletadas;
                newListName = "COMPLETADAS";
                break;
        }

        // Agregar a la nueva lista
        if (newList != null) {
            newList.add(0, updatedReservation); // Agregar al inicio
            Log.d(TAG, "   📥 Agregada a " + newListName + " en posición 0");
        }

        // Notificar cambios a los adaptadores
        if (foundAndRemoved && oldAdapter != null) {
            oldAdapter.notifyItemRemoved(oldPosition);
            Log.d(TAG, "   🔔 Notificado removal en adaptador anterior");
        }

        if (newAdapter != null) {
            newAdapter.notifyItemInserted(0);
            Log.d(TAG, "   🔔 Notificado insertion en adaptador nuevo");
        }

        // Actualizar la UI para mostrar la lista correcta
        updateSelectedTab(currentSelectedTab);

        if (!foundAndRemoved) {
            Log.w(TAG, "   ⚠️ No se encontró la reserva en ninguna lista, agregando directamente");
        }

        Log.d(TAG, "✅ Actualización completada");
    }

    // ✅ NUEVO: Método mejorado para identificar si es la misma reserva
    private boolean isSameReservation(Reservation reservation1, Reservation reservation2) {
        if (reservation1 == reservation2) return true;
        if (reservation1 == null || reservation2 == null) return false;

        // Método 1: Por ID si existe
        if (reservation1.getReservationId() != null && reservation2.getReservationId() != null) {
            if (reservation1.getReservationId().equals(reservation2.getReservationId())) {
                return true;
            }
        }

        // Método 2: Por combinación de hotel + fecha + precio (para reservas de testing)
        boolean sameHotel = reservation1.getHotelName().equals(reservation2.getHotelName());
        boolean sameDate = reservation1.getDate().equals(reservation2.getDate());
        boolean samePrice = Math.abs(reservation1.getBasePrice() - reservation2.getBasePrice()) < 0.01;

        return sameHotel && sameDate && samePrice;
    }

    private void updateSelectedTab(int selectedIndex) {
        currentSelectedTab = selectedIndex;
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

    // ✅ NUEVO: Manejar clicks del botón de acción de manera inteligente
    private void handleActionButtonClick(Reservation reservation, int position) {
        switch (reservation.getStatus()) {
            case Reservation.STATUS_UPCOMING:
                if (reservation.canModify()) {
                    // Ir a detalles para modificar
                    navigateToReservationDetails(reservation);
                } else {
                    // Solo ver detalles
                    navigateToReservationDetails(reservation);
                }
                break;

            case Reservation.STATUS_ACTIVE:
                if (reservation.canRequestCheckout()) {
                    // Solicitar checkout
                    performCheckoutRequest(reservation, position);
                } else if (reservation.isCheckoutPending()) {
                    // Ver estado del checkout
                    navigateToReservationDetails(reservation);
                } else {
                    // Ver detalles
                    navigateToReservationDetails(reservation);
                }
                break;

            case Reservation.STATUS_COMPLETED:
                if (reservation.isReviewSubmitted()) {
                    // Ver factura
                    navigateToReservationDetails(reservation);
                } else {
                    // Valorar estadía
                    navigateToReservationDetails(reservation);
                }
                break;

            default:
                navigateToReservationDetails(reservation);
                break;
        }
    }

    // ✅ DATOS DE PRUEBA PROFESIONALES CON SUB-ESTADOS (Mantenido para testing)
    private void loadProfessionalMockData() {
        clearAllData();
        Log.d(TAG, "📋 Cargando datos mock profesionales para testing");

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
                1250.0, // ✅ Monto mayor para taxi (configurable por hotel)
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
        active1.setSubStatus(Reservation.SUBSTATUS_CHECKED_IN);
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
        active2.setSubStatus(Reservation.SUBSTATUS_STAYING);
        active2.addService("Desayuno buffet", 45.0, 3);
        active2.addService("Spa relajante", 120.0, 1);
        active2.addService("Minibar", 35.0, 2);
        actualesReservations.add(active2);

        // 3. ✅ CHECKOUT PENDIENTE
        Reservation activePending = createActiveReservation(
                "Casa Andina Premium",
                "Miraflores, Lima",
                "20 Abr - 27 Abr, 2025",
                1350.0,
                4.5f
        );
        activePending.setSubStatus(Reservation.SUBSTATUS_CHECKOUT_PENDING);
        activePending.addService("Room service", 120.0, 2);
        activePending.addService("Lavandería express", 45.0, 1);
        activePending.addService("Minibar premium", 85.0, 1);
        activePending.addAdditionalCharge("Reparación menor de mueble", 150.0, "Daño accidental reportado");
        actualesReservations.add(activePending);

        // ✅ COMPLETADAS (Con y sin reviews, algunas con taxi disponible)
        Reservation completed1 = createCompletedReservation(
                "Belmond Hotel Monasterio",
                "Cusco, Centro Histórico",
                "05 Mar - 10 Mar, 2025",
                1500.0, // ✅ Elegible para taxi (configurable por hotel)
                5.0f
        );
        completed1.setReviewSubmitted(true);
        completed1.setHasTaxiService(true); // ✅ Taxi disponible
        completadasReservations.add(completed1);

        Reservation completed2 = createCompletedReservation(
                "Inkaterra Machu Picchu",
                "Aguas Calientes, Cusco",
                "10 Feb - 15 Feb, 2025",
                2000.0, // ✅ Elegible para taxi (configurable por hotel)
                4.9f
        );
        completed2.setHasTaxiService(true); // ✅ Taxi disponible
        completadasReservations.add(completed2);

        Reservation completed3 = createCompletedReservation(
                "JW Marriott Lima",
                "Miraflores, Lima",
                "15 Ene - 20 Ene, 2025",
                800.0, // ✅ NO elegible para taxi (menor al monto configurado)
                4.7f
        );
        completed3.setReviewSubmitted(true);
        completadasReservations.add(completed3);

        notifyAllAdapters();
        updateSelectedTab(currentSelectedTab);

        // ✅ NUEVO: Log de debug si está habilitado
        if (debugManager.isDebugModeEnabled()) {
            Log.d(TAG, "🐛 Datos mock cargados - Long press en cualquier reserva para debug");
        }
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

        // ✅ ACTUALIZAR EN FIREBASE
        com.example.proyecto_final_hoteleros.client.utils.FirebaseReservationManager
                .getInstance()
                .updateReservationSubStatus(reservation.getReservationId(),
                        Reservation.SUBSTATUS_CHECKOUT_PENDING,
                        new com.example.proyecto_final_hoteleros.client.utils.FirebaseReservationManager.ReservationCallback() {
                            @Override
                            public void onSuccess() {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Log.d(TAG, "✅ Checkout solicitado y guardado en Firebase");
                                    });
                                }
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "❌ Error actualizando checkout en Firebase: " + error);
                            }
                        });

        // ✅ NOTIFICAR CAMBIO EN LA POSICIÓN ESPECÍFICA
        if (adapterActuales != null) {
            adapterActuales.notifyItemChanged(position);
        }

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
                if (adapterActuales != null) {
                    adapterActuales.notifyItemRemoved(i);
                }

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

    // ✅ NUEVO: Refrescar manualmente con SwipeRefresh
    private void refreshReservationsManually() {
        Log.d(TAG, "🔄 Refrescando reservas manualmente");
        loadReservationsFromFirebase();
    }

    public void refreshReservations() {
        loadReservationsFromFirebase();
    }

    // ✅ CARGAR RESERVAS REALES DESDE FIREBASE - OPTIMIZADO
    private void loadReservationsFromFirebase() {
        if (isLoading) {
            Log.d(TAG, "⚠️ Carga ya en progreso, saltando");
            return;
        }

        isLoading = true;

        // ✅ MOSTRAR INDICADOR DE CARGA
        if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        clearAllData();

        // ✅ OBTENER ID DEL USUARIO ACTUAL
        UserDataManager userManager = UserDataManager.getInstance();
        String userId = userManager.getUserId();

        if (userId == null) {
            Log.w(TAG, "⚠️ Usuario no identificado, mostrando datos mock");
            finishLoading();
            loadProfessionalMockData();
            return;
        }

        Log.d(TAG, "🔍 Cargando reservas desde Firebase para usuario: " + userId);

        // ✅ CARGAR DESDE FIREBASE CON TIMEOUT
        com.example.proyecto_final_hoteleros.client.utils.FirebaseReservationManager
                .getInstance()
                .loadUserReservations(userId, new com.example.proyecto_final_hoteleros.client.utils.FirebaseReservationManager.ReservationsListCallback() {
                    @Override
                    public void onSuccess(List<Reservation> upcomingList, List<Reservation> activeList, List<Reservation> completedList) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                try {
                                    Log.d(TAG, "✅ Reservas cargadas desde Firebase:");
                                    Log.d(TAG, "   - Próximas: " + upcomingList.size());
                                    Log.d(TAG, "   - Actuales: " + activeList.size());
                                    Log.d(TAG, "   - Completadas: " + completedList.size());

                                    // ✅ ASIGNAR LISTAS CON VALIDACIÓN
                                    proximasReservations.clear();
                                    if (upcomingList != null) {
                                        proximasReservations.addAll(upcomingList);
                                    }

                                    actualesReservations.clear();
                                    if (activeList != null) {
                                        actualesReservations.addAll(activeList);
                                    }

                                    completadasReservations.clear();
                                    if (completedList != null) {
                                        completadasReservations.addAll(completedList);
                                    }

                                    // ✅ VERIFICAR TAXI EN COMPLETADAS - CON DELAY PARA UI
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        checkTaxiDialogsForCompletedReservations();
                                    }, 500);

                                    // ✅ NOTIFICAR ADAPTADORES
                                    notifyAllAdapters();
                                    updateSelectedTab(currentSelectedTab);

                                    finishLoading();

                                } catch (Exception e) {
                                    Log.e(TAG, "❌ Error procesando reservas cargadas: " + e.getMessage());
                                    finishLoading();
                                    loadProfessionalMockData();
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.e(TAG, "❌ Error cargando reservas: " + error);
                                finishLoading();

                                // ✅ FALLBACK A DATOS MOCK SI HAY ERROR
                                android.widget.Toast.makeText(requireContext(),
                                        "⚠️ Error de conexión. Mostrando datos de ejemplo.",
                                        android.widget.Toast.LENGTH_SHORT).show();

                                loadProfessionalMockData();
                            });
                        }
                    }
                });

        // ✅ TIMEOUT DE SEGURIDAD
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isLoading) {
                Log.w(TAG, "⏰ Timeout cargando desde Firebase, usando datos mock");
                finishLoading();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        android.widget.Toast.makeText(requireContext(),
                                "⏰ Conexión lenta. Mostrando datos de ejemplo.",
                                android.widget.Toast.LENGTH_SHORT).show();
                        loadProfessionalMockData();
                    });
                }
            }
        }, 10000); // 10 segundos timeout
    }

    // ✅ NUEVO: Finalizar estado de carga
    private void finishLoading() {
        isLoading = false;
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * ✅ VERIFICAR SI HAY RESERVAS COMPLETADAS CON TAXI QUE REQUIERAN CONFIRMACIÓN - OPTIMIZADO
     */
    private void checkTaxiDialogsForCompletedReservations() {
        if (completadasReservations == null || completadasReservations.isEmpty()) {
            Log.d(TAG, "ℹ️ No hay reservas completadas para verificar taxi");
            return;
        }

        Log.d(TAG, "🚖 Verificando " + completadasReservations.size() + " reservas completadas para diálogos de taxi");

        for (Reservation reservation : completadasReservations) {
            if (shouldShowTaxiDialog(reservation)) {
                Log.d(TAG, "🚖 Taxi disponible para: " + reservation.getHotelName() + " (S/ " + reservation.getFinalTotal() + ")");

                // ✅ MOSTRAR DIÁLOGO DESPUÉS DE UN BREVE DELAY PARA QUE LA UI SE ESTABILICE
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    showTaxiConfirmationDialog(reservation);
                }, 1000);
                break; // Solo mostrar uno a la vez
            }
        }
    }

    /**
     * ✅ VERIFICAR SI DEBE MOSTRARSE EL DIÁLOGO DE TAXI PARA UNA RESERVA - MEJORADO
     */
    private boolean shouldShowTaxiDialog(Reservation reservation) {
        if (reservation == null) {
            Log.d(TAG, "🚖 Reserva null, no mostrar diálogo");
            return false;
        }

        if (reservation.getStatus() != Reservation.STATUS_COMPLETED) {
            Log.d(TAG, "🚖 Reserva no completada: " + reservation.getHotelName());
            return false;
        }

        if (!reservation.isEligibleForFreeTaxi()) {
            Log.d(TAG, "🚖 No elegible para taxi gratis: " + reservation.getHotelName() + " (S/ " + reservation.getFinalTotal() + ")");
            return false;
        }

        // ✅ VERIFICAR SI AÚN ES VÁLIDO (mismo día del checkout)
        boolean shouldShow = com.example.proyecto_final_hoteleros.client.ui.dialog.TaxiConfirmationDialog
                .shouldShowForReservation(reservation);

        Log.d(TAG, "🚖 Validación final para " + reservation.getHotelName() + ": " + shouldShow);
        return shouldShow;
    }

    /**
     * ✅ MOSTRAR DIÁLOGO DE CONFIRMACIÓN DE TAXI - MEJORADO
     */
    private void showTaxiConfirmationDialog(Reservation reservation) {
        if (getChildFragmentManager().isStateSaved()) {
            Log.w(TAG, "⚠️ FragmentManager en estado guardado, no mostrar diálogo");
            return;
        }

        Log.d(TAG, "🚖 Mostrando diálogo de confirmación de taxi para: " + reservation.getHotelName());

        com.example.proyecto_final_hoteleros.client.ui.dialog.TaxiConfirmationDialog dialog =
                com.example.proyecto_final_hoteleros.client.ui.dialog.TaxiConfirmationDialog.newInstance(reservation);

        dialog.setTaxiConfirmationListener(new com.example.proyecto_final_hoteleros.client.ui.dialog.TaxiConfirmationDialog.TaxiConfirmationListener() {
            @Override
            public void onTaxiConfirmed(Reservation reservation) {
                Log.d(TAG, "✅ Usuario confirmó taxi para: " + reservation.getHotelName());

                // ✅ GUARDAR CONFIRMACIÓN EN FIREBASE
                com.example.proyecto_final_hoteleros.client.utils.FirebaseReservationManager
                        .getInstance()
                        .confirmTaxiService(reservation.getReservationId(), true,
                                new com.example.proyecto_final_hoteleros.client.utils.FirebaseReservationManager.TaxiConfirmationCallback() {
                                    @Override
                                    public void onConfirmed() {
                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(() -> {
                                                android.widget.Toast.makeText(requireContext(),
                                                        "🚖 Taxi confirmado para " + reservation.getHotelName(),
                                                        android.widget.Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                    }

                                    @Override
                                    public void onDeclined() {
                                        // No debería llegar aquí
                                    }

                                    @Override
                                    public void onError(String error) {
                                        Log.e(TAG, "❌ Error guardando confirmación de taxi: " + error);
                                    }
                                });
            }

            @Override
            public void onTaxiDeclined(Reservation reservation) {
                Log.d(TAG, "❌ Usuario declinó taxi para: " + reservation.getHotelName());

                // ✅ GUARDAR DECLINACIÓN EN FIREBASE
                com.example.proyecto_final_hoteleros.client.utils.FirebaseReservationManager
                        .getInstance()
                        .confirmTaxiService(reservation.getReservationId(), false,
                                new com.example.proyecto_final_hoteleros.client.utils.FirebaseReservationManager.TaxiConfirmationCallback() {
                                    @Override
                                    public void onConfirmed() {
                                        // No debería llegar aquí
                                    }

                                    @Override
                                    public void onDeclined() {
                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(() -> {
                                                android.widget.Toast.makeText(requireContext(),
                                                        "Taxi declinado para " + reservation.getHotelName(),
                                                        android.widget.Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                    }

                                    @Override
                                    public void onError(String error) {
                                        Log.e(TAG, "❌ Error guardando declinación de taxi: " + error);
                                    }
                                });
            }

            @Override
            public void onNavigateToTaxiFlow(Reservation reservation) {
                Log.d(TAG, "🚖 Navegando al flujo de taxi para: " + reservation.getHotelName());
                navigateToTaxiFlow(reservation);
            }
        });

        try {
            dialog.show(getChildFragmentManager(), "TaxiConfirmationDialog");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error mostrando diálogo de taxi: " + e.getMessage());
        }
    }

    /**
     * ✅ NAVEGAR AL FLUJO DE TAXI (TAXISTA)
     */
    private void navigateToTaxiFlow(Reservation reservation) {
        Log.d(TAG, "🚖 Navegando al flujo de taxi para reserva: " + reservation.getReservationId());

        // ✅ CREAR FRAGMENTO DE SOLICITUD DE TAXI
        com.example.proyecto_final_hoteleros.client.ui.fragment.TaxiRequestFragment taxiFragment =
                com.example.proyecto_final_hoteleros.client.ui.fragment.TaxiRequestFragment.newInstance(reservation);

        // ✅ NAVEGAR CON ANIMACIÓN BOTTOM_TO_TOP (emerge desde abajo como modal)
        NavigationManager.getInstance().navigateWithCustomAnimation(
                taxiFragment,
                AnimationDirection.BOTTOM_TO_TOP,
                true
        );
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

    // ✅ NUEVO: Forzar refresh completo de la UI
    private void forceUIRefresh() {
        Log.d(TAG, "🔄 Forzando refresh completo de UI");

        // Notificar todos los adaptadores
        notifyAllAdapters();

        // Actualizar el tab actual
        updateSelectedTab(currentSelectedTab);

        // Log del estado actual
        Log.d(TAG, "📊 Estado actual de listas:");
        Log.d(TAG, "   - Próximas: " + proximasReservations.size());
        Log.d(TAG, "   - Actuales: " + actualesReservations.size());
        Log.d(TAG, "   - Completadas: " + completadasReservations.size());

        // Si estamos en una pestaña vacía, mostrar estado vacío
        if (isEmptyList(currentSelectedTab)) {
            Log.d(TAG, "📋 Tab actual está vacío, actualizando estado vacío");
        }
    }

    // ✅ NUEVO: Método público para debug externo
    public void debugRefreshUI() {
        forceUIRefresh();
    }

    // ✅ NUEVO: Método para obtener estadísticas rápidas
    public String getReservationsStats() {
        return String.format("Próximas: %d | Actuales: %d | Completadas: %d",
                proximasReservations.size(),
                actualesReservations.size(),
                completadasReservations.size());
    }

    @Override
    public void onResume() {
        super.onResume();
        // ✅ Recargar solo si han pasado más de 5 minutos desde la última carga
        // o si no hay datos cargados
        if (proximasReservations.isEmpty() && actualesReservations.isEmpty() && completadasReservations.isEmpty()) {
            Log.d(TAG, "🔄 onResume: Recargando datos porque están vacíos");
            loadReservationsFromFirebase();
        }
    }
}