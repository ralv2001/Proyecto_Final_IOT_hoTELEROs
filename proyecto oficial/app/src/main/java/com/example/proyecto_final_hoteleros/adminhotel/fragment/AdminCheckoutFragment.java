package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.CheckoutAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.CheckoutDetailsDialog;
import com.example.proyecto_final_hoteleros.adminhotel.model.CheckoutItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminCheckoutFragment extends Fragment implements CheckoutAdapter.OnItemClickListener {

    private static final String TAG = "AdminCheckoutFragment";

    private RecyclerView recyclerCheckouts;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CheckoutAdapter adapter;
    private List<CheckoutItem> listaCheckouts;
    private View emptyStateView;

    // Stats views
    private TextView tvPendingCount;
    private TextView tvTotalRevenue;
    private TextView tvAverageStay;

    private NumberFormat currencyFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_checkout, container, false);

        Log.d(TAG, "Iniciando AdminCheckoutFragment");

        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

        initViews(rootView);
        setupRecyclerView();
        loadCheckouts();

        return rootView;
    }

    private void initViews(View rootView) {
        recyclerCheckouts = rootView.findViewById(R.id.recyclerHuespedes);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        emptyStateView = rootView.findViewById(R.id.emptyStateView);

        // Stats views (pueden ser null si no están en el layout)
        tvPendingCount = rootView.findViewById(R.id.tvPendingCount);
        tvTotalRevenue = rootView.findViewById(R.id.tvTotalRevenue);
        tvAverageStay = rootView.findViewById(R.id.tvAverageStay);

        // Configurar SwipeRefreshLayout
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadCheckouts);
            try {
                swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.blue, R.color.green);
            } catch (Exception e) {
                Log.w(TAG, "No se pudieron establecer colores de refresh");
            }
        }

        Log.d(TAG, "Views inicializadas correctamente");
    }

    private void setupRecyclerView() {
        if (recyclerCheckouts == null) {
            Log.e(TAG, "RecyclerView es null!");
            return;
        }

        recyclerCheckouts.setLayoutManager(new LinearLayoutManager(getContext()));
        listaCheckouts = new ArrayList<>();

        adapter = new CheckoutAdapter(getContext(), listaCheckouts);
        recyclerCheckouts.setAdapter(adapter);

        adapter.setOnItemClickListener(this);

        Log.d(TAG, "RecyclerView configurado exitosamente");
    }

    private void loadCheckouts() {
        Log.d(TAG, "Cargando checkouts...");

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        // Limpiar lista existente
        listaCheckouts.clear();

        try {
            // Crear datos de ejemplo mejorados
            createBetterSampleData();

            Log.d(TAG, "Se crearon " + listaCheckouts.size() + " checkouts de ejemplo");

            // Notificar al adapter
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error creando datos de ejemplo", e);
            Toast.makeText(getContext(), "Error cargando checkouts", Toast.LENGTH_SHORT).show();
        }

        updateEmptyState();
        updateStats();

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        Log.d(TAG, "Carga completada");
    }

    private void createBetterSampleData() {
        // Checkout 1 - Pendiente SIN DAÑOS (limpio)
        CheckoutItem checkout1 = new CheckoutItem("María González", "301", "15/06/2024", "18/06/2024");
        checkout1.setEmailHuesped("maria.gonzalez@gmail.com");
        checkout1.setTelefonoHuesped("+51 987 654 321");
        checkout1.setCostoHabitacion(750.0);
        checkout1.setNumeroNoches(3);
        checkout1.setTipoHabitacion("Suite Ejecutiva");
        checkout1.addServicioAdicional(new CheckoutItem.ServicioAdicional("Spa & Wellness", 180.0, 2, "16/06/2024", "Masajes de relajación"));
        checkout1.addServicioAdicional(new CheckoutItem.ServicioAdicional("Room Service", 95.0, 3, "15/06/2024", "Cenas premium"));
        checkout1.addServicioAdicional(new CheckoutItem.ServicioAdicional("Minibar", 65.0, 1, "17/06/2024", "Bebidas importadas"));
        // NO AGREGAR DAÑOS - Se agregarán durante el procesamiento si es necesario

        // Checkout 2 - Pendiente SIN DAÑOS (limpio)
        CheckoutItem checkout2 = new CheckoutItem("Carlos Mendoza", "205", "12/06/2024", "15/06/2024");
        checkout2.setEmailHuesped("carlos.mendoza@email.com");
        checkout2.setTelefonoHuesped("+51 987 123 456");
        checkout2.setCostoHabitacion(900.0);
        checkout2.setNumeroNoches(3);
        checkout2.setTipoHabitacion("Suite Premium");
        checkout2.addServicioAdicional(new CheckoutItem.ServicioAdicional("Gimnasio VIP", 120.0, 2, "13/06/2024", "Entrenador personal"));
        checkout2.addServicioAdicional(new CheckoutItem.ServicioAdicional("Transporte", 150.0, 1, "15/06/2024", "Aeropuerto - Hotel"));
        // NO AGREGAR DAÑOS - Se agregarán durante el procesamiento si es necesario

        // Checkout 3 - Pendiente SIN DAÑOS (limpio)
        CheckoutItem checkout3 = new CheckoutItem("Ana Palacios", "150", "10/06/2024", "12/06/2024");
        checkout3.setEmailHuesped("ana.palacios@outlook.com");
        checkout3.setTelefonoHuesped("+51 987 789 123");
        checkout3.setCostoHabitacion(400.0);
        checkout3.setNumeroNoches(2);
        checkout3.setTipoHabitacion("Habitación Deluxe");
        // NO AGREGAR DAÑOS - Se agregarán durante el procesamiento si es necesario

        // Checkout 4 - COMPLETADO CON DAÑOS (ya procesado anteriormente)
        CheckoutItem checkout4 = new CheckoutItem("Renato Sulca", "412", "08/06/2024", "11/06/2024");
        checkout4.setEstado("Completado");
        checkout4.setPagado(true);
        checkout4.setMetodoPago("Tarjeta de Crédito");
        checkout4.setEmailHuesped("renato.sulca@gmail.com");
        checkout4.setTelefonoHuesped("+51 987 456 789");
        checkout4.setCostoHabitacion(1200.0);
        checkout4.setNumeroNoches(3);
        checkout4.setTipoHabitacion("Suite Presidencial");

        // Servicios múltiples
        checkout4.addServicioAdicional(new CheckoutItem.ServicioAdicional("Spa Premium", 200.0, 2, "09/06/2024", "Tratamiento completo"));
        checkout4.addServicioAdicional(new CheckoutItem.ServicioAdicional("Room Service Gourmet", 180.0, 3, "08/06/2024", "Chef privado"));
        checkout4.addServicioAdicional(new CheckoutItem.ServicioAdicional("Minibar Premium", 95.0, 2, "09/06/2024", "Licores premium"));

        // DAÑOS que se agregaron DURANTE el procesamiento anterior
        CheckoutItem.DanoHabitacion dano1 = new CheckoutItem.DanoHabitacion("Daño en mueble de baño", 200.0, "Moderado", "10/06/2024");
        dano1.setConfirmado(true);
        checkout4.addDano(dano1);

        CheckoutItem.DanoHabitacion dano2 = new CheckoutItem.DanoHabitacion("Quemadura en alfombra", 150.0, "Leve", "11/06/2024");
        dano2.setConfirmado(true);
        checkout4.addDano(dano2);

        // Checkout 5 - COMPLETADO SIN DAÑOS (checkout limpio procesado anteriormente)
        CheckoutItem checkout5 = new CheckoutItem("Pedro Martín", "101", "05/06/2024", "07/06/2024");
        checkout5.setEstado("Completado");
        checkout5.setPagado(true);
        checkout5.setMetodoPago("Tarjeta de Crédito");
        checkout5.setCostoHabitacion(500.0);
        checkout5.setNumeroNoches(2);
        checkout5.setTipoHabitacion("Habitación Standard");
        checkout5.addServicioAdicional(new CheckoutItem.ServicioAdicional("Desayuno", 60.0, 2, "06/06/2024", "Buffet continental"));
        // SIN DAÑOS - Habitación estaba en perfecto estado

        // Checkout 6 - EN PROCESO SIN DAÑOS (en proceso de pago)
        CheckoutItem checkout6 = new CheckoutItem("Luis Rodríguez", "250", "13/06/2024", "16/06/2024");
        checkout6.setEstado("En Proceso");
        checkout6.setEmailHuesped("luis.rodriguez@yahoo.com");
        checkout6.setTelefonoHuesped("+51 987 321 654");
        checkout6.setCostoHabitacion(600.0);
        checkout6.setNumeroNoches(3);
        checkout6.setTipoHabitacion("Suite Junior");
        checkout6.addServicioAdicional(new CheckoutItem.ServicioAdicional("Piscina VIP", 80.0, 2, "14/06/2024", "Área privada"));
        // SIN DAÑOS - Se agregarán durante el procesamiento si es necesario

        // Agregar todos a la lista
        listaCheckouts.add(checkout1);
        listaCheckouts.add(checkout2);
        listaCheckouts.add(checkout3);
        listaCheckouts.add(checkout4);
        listaCheckouts.add(checkout5);
        listaCheckouts.add(checkout6);
    }
    private void updateStats() {
        int pendingCount = 0;
        double totalRevenue = 0.0;
        double totalNights = 0.0;
        int completedCheckouts = 0;

        for (CheckoutItem checkout : listaCheckouts) {
            if (checkout.getEstado().equals("Pendiente") || checkout.getEstado().equals("En Proceso")) {
                pendingCount++;
            }
            if (checkout.isPagado()) {
                totalRevenue += checkout.getTotalGeneral();
                totalNights += checkout.getNumeroNoches();
                completedCheckouts++;
            }
        }

        // Actualizar stats si existen
        if (tvPendingCount != null) {
            tvPendingCount.setText(String.valueOf(pendingCount));
        }
        if (tvTotalRevenue != null) {
            tvTotalRevenue.setText(currencyFormat.format(totalRevenue));
        }
        if (tvAverageStay != null) {
            if (completedCheckouts > 0) {
                double averageStay = totalNights / completedCheckouts;
                tvAverageStay.setText(String.format("%.1f", averageStay));
            } else {
                tvAverageStay.setText("0");
            }
        }
    }

    private void updateEmptyState() {
        if (recyclerCheckouts == null) return;

        if (listaCheckouts.isEmpty()) {
            recyclerCheckouts.setVisibility(View.GONE);
            if (emptyStateView != null) {
                emptyStateView.setVisibility(View.VISIBLE);
            }
            Log.d(TAG, "Mostrando estado vacío");
        } else {
            recyclerCheckouts.setVisibility(View.VISIBLE);
            if (emptyStateView != null) {
                emptyStateView.setVisibility(View.GONE);
            }
            Log.d(TAG, "Mostrando lista con " + listaCheckouts.size() + " elementos");
        }
    }

    // Implementación de la interfaz OnItemClickListener
    @Override
    public void onItemClick(CheckoutItem checkout) {
        onViewDetailsClick(checkout);
    }

    @Override
    public void onProcessClick(CheckoutItem checkout) {
        if (checkout.getEstado().equals("Completado")) {
            Toast.makeText(getContext(), "Este checkout ya fue completado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar diálogo de detalles para procesamiento
        showCheckoutDetailsDialog(checkout, true);
    }

    @Override
    public void onViewDetailsClick(CheckoutItem checkout) {
        showCheckoutDetailsDialog(checkout, false);
    }

    private void showCheckoutDetailsDialog(CheckoutItem checkout, boolean autoProcess) {
        if (getContext() == null) {
            Log.e(TAG, "Context es null, no se puede mostrar el diálogo");
            return;
        }

        try {
            CheckoutDetailsDialog dialog = new CheckoutDetailsDialog(getContext(), checkout);
            dialog.setOnCheckoutProcessedListener(new CheckoutDetailsDialog.OnCheckoutProcessedListener() {
                @Override
                public void onCheckoutProcessed(CheckoutItem processedCheckout) {
                    // Actualizar el item en el adapter
                    if (adapter != null) {
                        adapter.updateItem(processedCheckout);
                    }
                    // Actualizar las estadísticas
                    updateStats();

                    Toast.makeText(getContext(),
                            "✅ Checkout de " + processedCheckout.getNombreHuesped() + " completado exitosamente",
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCheckoutCancelled() {
                    Log.d(TAG, "Checkout cancelado por el usuario");
                }

                @Override
                public void onDamageAdded(CheckoutItem updatedCheckout) {
                    // Actualizar el item cuando se agreguen daños
                    if (adapter != null) {
                        adapter.updateItem(updatedCheckout);
                    }
                    Toast.makeText(getContext(), "Daño agregado correctamente", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDamageUpdated(CheckoutItem updatedCheckout) {
                    // Actualizar el item cuando se modifiquen daños
                    if (adapter != null) {
                        adapter.updateItem(updatedCheckout);
                    }
                    Toast.makeText(getContext(), "Daño actualizado correctamente", Toast.LENGTH_SHORT).show();
                }
            });

            dialog.show();

            // Si es auto-process, mostrar el mensaje inicial
            if (autoProcess) {
                Toast.makeText(getContext(),
                        "💡 Revisa los detalles y daños antes de procesar el checkout",
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error mostrando diálogo de checkout", e);
            Toast.makeText(getContext(), "Error abriendo detalles del checkout", Toast.LENGTH_SHORT).show();
        }
    }

    public void onCheckoutCompleted(CheckoutItem completedCheckout) {
        if (adapter != null) {
            adapter.updateItem(completedCheckout);
        }
        updateStats();

        Toast.makeText(getContext(),
                "Checkout de " + completedCheckout.getNombreHuesped() + " completado exitosamente",
                Toast.LENGTH_LONG).show();
    }
}