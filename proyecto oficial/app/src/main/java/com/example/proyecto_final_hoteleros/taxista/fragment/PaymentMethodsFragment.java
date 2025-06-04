package com.example.proyecto_final_hoteleros.taxista.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.taxista.adapters.PaymentMethodsAdapter;
import com.example.proyecto_final_hoteleros.taxista.model.PaymentMethod;
import com.example.proyecto_final_hoteleros.taxista.utils.DriverPreferenceManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodsFragment extends Fragment implements PaymentMethodsAdapter.PaymentMethodListener {

    private static final String TAG = "PaymentMethodsFragment";

    private MaterialToolbar toolbar;
    private RecyclerView recyclerPaymentMethods;
    private PaymentMethodsAdapter adapter;
    private List<PaymentMethod> paymentMethodsList;
    private DriverPreferenceManager preferenceManager;

    private LinearLayout emptyState;
    private LinearLayout loadingState;
    private TextView tvPaymentMethodsCount;
    private TextView tvTotalBalance;
    private FloatingActionButton btnAddPaymentMethod;

    public PaymentMethodsFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_methods, container, false);

        try {
            initializeViews(view);
            setupToolbar();
            setupRecyclerView();
            preferenceManager = new DriverPreferenceManager(requireContext());

        } catch (Exception e) {
            Log.e(TAG, "Error en onCreateView: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error al cargar métodos de pago", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadPaymentMethods();
    }

    private void initializeViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        recyclerPaymentMethods = view.findViewById(R.id.recycler_payment_methods);
        emptyState = view.findViewById(R.id.empty_state);
        loadingState = view.findViewById(R.id.loading_state);
        tvPaymentMethodsCount = view.findViewById(R.id.tv_payment_methods_count);
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        btnAddPaymentMethod = view.findViewById(R.id.btn_add_payment_method);

        btnAddPaymentMethod.setOnClickListener(v -> showAddPaymentMethodDialog());
    }

    private void setupToolbar() {
        toolbar.setTitle("Métodos de Pago");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void setupRecyclerView() {
        if (recyclerPaymentMethods != null && getContext() != null) {
            recyclerPaymentMethods.setLayoutManager(new LinearLayoutManager(getContext()));

            paymentMethodsList = new ArrayList<>();
            adapter = new PaymentMethodsAdapter(getContext(), paymentMethodsList, this);
            recyclerPaymentMethods.setAdapter(adapter);
        }
    }

    private void loadPaymentMethods() {
        try {
            showLoadingState();

            // Simular carga de datos
            new Handler().postDelayed(() -> {
                try {
                    List<PaymentMethod> newPaymentMethods = generatePaymentMethodsData();

                    paymentMethodsList.clear();
                    paymentMethodsList.addAll(newPaymentMethods);

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }

                    updateStatistics();
                    updateVisibility();

                } catch (Exception e) {
                    Log.e(TAG, "Error al cargar métodos de pago: " + e.getMessage(), e);
                    showErrorState();
                }
            }, 1000);

        } catch (Exception e) {
            Log.e(TAG, "Error en loadPaymentMethods: " + e.getMessage(), e);
            showErrorState();
        }
    }

    private void updateStatistics() {
        if (paymentMethodsList == null) return;

        int totalMethods = paymentMethodsList.size();
        double totalBalance = 0.0;

        for (PaymentMethod method : paymentMethodsList) {
            if (method != null && method.isEnabled()) {
                totalBalance += method.getTotalReceived();
            }
        }

        if (tvPaymentMethodsCount != null) {
            tvPaymentMethodsCount.setText("Tienes " + totalMethods + " método" +
                    (totalMethods == 1 ? "" : "s") + " de pago configurado" +
                    (totalMethods == 1 ? "" : "s"));
        }

        if (tvTotalBalance != null) {
            tvTotalBalance.setText("Balance total: S/ " + String.format("%.2f", totalBalance));
        }
    }

    private void updateVisibility() {
        if (paymentMethodsList == null || paymentMethodsList.isEmpty()) {
            setViewVisibility(recyclerPaymentMethods, View.GONE);
            setViewVisibility(loadingState, View.GONE);
            setViewVisibility(emptyState, View.VISIBLE);
        } else {
            setViewVisibility(recyclerPaymentMethods, View.VISIBLE);
            setViewVisibility(loadingState, View.GONE);
            setViewVisibility(emptyState, View.GONE);
        }
    }

    private void showLoadingState() {
        setViewVisibility(recyclerPaymentMethods, View.GONE);
        setViewVisibility(emptyState, View.GONE);
        setViewVisibility(loadingState, View.VISIBLE);
    }

    private void showErrorState() {
        setViewVisibility(recyclerPaymentMethods, View.GONE);
        setViewVisibility(loadingState, View.GONE);
        setViewVisibility(emptyState, View.VISIBLE);

        if (getContext() != null && isAdded()) {
            Toast.makeText(getContext(), "Error al cargar métodos de pago", Toast.LENGTH_SHORT).show();
        }
    }

    private void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    // Implementación de PaymentMethodListener
    @Override
    public void onPaymentMethodClick(PaymentMethod paymentMethod) {
        Log.d(TAG, "PaymentMethod clicked: " + paymentMethod.getName());
        Toast.makeText(getContext(), "Método: " + paymentMethod.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onToggleEnabled(PaymentMethod paymentMethod, boolean enabled) {
        Log.d(TAG, "Toggle enabled for " + paymentMethod.getName() + ": " + enabled);

        paymentMethod.setEnabled(enabled);

        String message = enabled ?
                paymentMethod.getName() + " activado" :
                paymentMethod.getName() + " desactivado";

        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

        updateStatistics();
    }

    @Override
    public void onSetAsDefault(PaymentMethod paymentMethod) {
        Log.d(TAG, "Set as default: " + paymentMethod.getName());

        // Remover el estado de predeterminado de todos los métodos
        for (PaymentMethod method : paymentMethodsList) {
            method.setDefault(false);
        }

        // Establecer el nuevo método como predeterminado
        paymentMethod.setDefault(true);

        adapter.notifyDataSetChanged();

        Toast.makeText(getContext(), paymentMethod.getName() + " establecido como predeterminado",
                Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDeletePaymentMethod(PaymentMethod paymentMethod) {
        Log.d(TAG, "Delete payment method: " + paymentMethod.getName());

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Método de Pago")
                .setMessage("¿Estás seguro de que deseas eliminar " + paymentMethod.getName() + "?")
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    paymentMethodsList.remove(paymentMethod);
                    adapter.notifyDataSetChanged();
                    updateStatistics();
                    updateVisibility();

                    Toast.makeText(getContext(), paymentMethod.getName() + " eliminado",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showAddPaymentMethodDialog() {
        String[] paymentTypes = {
                "Efectivo",
                "Tarjeta de Débito/Crédito",
                "Yape",
                "Plin",
                "Transferencia Bancaria"
        };

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Agregar Método de Pago")
                .setIcon(R.drawable.ic_payment)
                .setItems(paymentTypes, (dialog, which) -> {
                    String selectedType = paymentTypes[which];
                    showPaymentMethodForm(selectedType, which);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showPaymentMethodForm(String paymentType, int typeIndex) {
        View formView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_payment_method, null);

        TextView tvTitle = formView.findViewById(R.id.tv_form_title);
        com.google.android.material.textfield.TextInputEditText etName = formView.findViewById(R.id.et_payment_name);
        com.google.android.material.textfield.TextInputEditText etAccount = formView.findViewById(R.id.et_account_number);
        com.google.android.material.textfield.TextInputEditText etBank = formView.findViewById(R.id.et_bank_name);
        LinearLayout layoutAccount = formView.findViewById(R.id.layout_account_number);
        LinearLayout layoutBank = formView.findViewById(R.id.layout_bank_name);

        tvTitle.setText("Agregar " + paymentType);

        // Configurar campos según el tipo
        switch (typeIndex) {
            case 0: // Efectivo
                etName.setText("Efectivo");
                etName.setEnabled(false);
                layoutAccount.setVisibility(View.GONE);
                layoutBank.setVisibility(View.GONE);
                break;
            case 1: // Tarjeta
                etName.setHint("Ej: Visa Débito");
                etAccount.setHint("Últimos 4 dígitos");
                etBank.setHint("Nombre del banco");
                break;
            case 2: // Yape
                etName.setText("Yape");
                etName.setEnabled(false);
                etAccount.setHint("Número de teléfono");
                layoutBank.setVisibility(View.GONE);
                break;
            case 3: // Plin
                etName.setText("Plin");
                etName.setEnabled(false);
                etAccount.setHint("Número de teléfono");
                layoutBank.setVisibility(View.GONE);
                break;
            case 4: // Transferencia
                etName.setHint("Ej: Cuenta BCP");
                etAccount.setHint("Número de cuenta");
                etBank.setHint("Nombre del banco");
                break;
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Nuevo Método de Pago")
                .setView(formView)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String account = etAccount.getText().toString().trim();
                    String bank = etBank.getText().toString().trim();

                    if (validatePaymentMethodForm(name, account, bank, typeIndex)) {
                        addNewPaymentMethod(paymentType, name, account, bank, typeIndex);
                    } else {
                        Toast.makeText(getContext(), "Por favor completa todos los campos requeridos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private boolean validatePaymentMethodForm(String name, String account, String bank, int typeIndex) {
        switch (typeIndex) {
            case 0: // Efectivo - no requiere validación adicional
                return true;
            case 1: // Tarjeta - requiere todos los campos
            case 4: // Transferencia - requiere todos los campos
                return !name.isEmpty() && !account.isEmpty() && !bank.isEmpty();
            case 2: // Yape - requiere número
            case 3: // Plin - requiere número
                return !account.isEmpty() && account.length() >= 9;
            default:
                return false;
        }
    }

    private void addNewPaymentMethod(String paymentType, String name, String account, String bank, int typeIndex) {
        // Generar ID único
        String id = "payment_" + System.currentTimeMillis();

        // Determinar tipo de pago
        PaymentMethod.PaymentType type;
        switch (typeIndex) {
            case 0: type = PaymentMethod.PaymentType.CASH; break;
            case 1: type = PaymentMethod.PaymentType.CARD; break;
            case 2:
            case 3: type = PaymentMethod.PaymentType.DIGITAL_WALLET; break;
            case 4: type = PaymentMethod.PaymentType.BANK_TRANSFER; break;
            default: type = PaymentMethod.PaymentType.CASH;
        }

        // Crear nuevo método de pago
        PaymentMethod newMethod = new PaymentMethod(
                id,
                type,
                name,
                getDescriptionForType(paymentType),
                account,
                bank,
                true, // habilitado por defecto
                paymentMethodsList.isEmpty(), // es predeterminado si es el primero
                R.drawable.ic_payment,
                "Recién agregado",
                0.0 // sin ganancias iniciales
        );

        // Agregar a la lista
        paymentMethodsList.add(newMethod);
        adapter.notifyItemInserted(paymentMethodsList.size() - 1);

        // Actualizar estadísticas y visibilidad
        updateStatistics();
        updateVisibility();

        Toast.makeText(getContext(), name + " agregado exitosamente", Toast.LENGTH_SHORT).show();
    }

    private String getDescriptionForType(String paymentType) {
        switch (paymentType) {
            case "Efectivo": return "Pagos en efectivo directo";
            case "Tarjeta de Débito/Crédito": return "Pagos con tarjeta";
            case "Yape": return "Billetera digital Yape";
            case "Plin": return "Billetera digital Plin";
            case "Transferencia Bancaria": return "Transferencias bancarias";
            default: return "Método de pago";
        }
    }

    private List<PaymentMethod> generatePaymentMethodsData() {
        List<PaymentMethod> methods = new ArrayList<>();

        // Efectivo
        methods.add(new PaymentMethod(
                "cash_001",
                PaymentMethod.PaymentType.CASH,
                "Efectivo",
                "Pagos en efectivo directo",
                "",
                "",
                true,
                true, // Por defecto
                R.drawable.ic_payment,
                "Hace 2 horas",
                1850.50
        ));

        // Yape
        methods.add(new PaymentMethod(
                "yape_001",
                PaymentMethod.PaymentType.DIGITAL_WALLET,
                "Yape",
                "Billetera digital Yape",
                "987654321",
                "",
                true,
                false,
                R.drawable.ic_payment,
                "Ayer",
                650.00
        ));

        // Plin
        methods.add(new PaymentMethod(
                "plin_001",
                PaymentMethod.PaymentType.DIGITAL_WALLET,
                "Plin",
                "Billetera digital Plin",
                "987654321",
                "",
                true,
                false,
                R.drawable.ic_payment,
                "Hace 3 días",
                420.75
        ));

        // Tarjeta Visa
        methods.add(new PaymentMethod(
                "card_001",
                PaymentMethod.PaymentType.CARD,
                "Visa Débito",
                "Tarjeta de débito principal",
                "4532123456781234",
                "BCP",
                false, // Deshabilitada
                false,
                R.drawable.ic_payment,
                "Hace 1 semana",
                0.00
        ));

        // Transferencia BCP
        methods.add(new PaymentMethod(
                "transfer_001",
                PaymentMethod.PaymentType.BANK_TRANSFER,
                "Cuenta BCP",
                "Cuenta de ahorros",
                "19412345678901",
                "Banco de Crédito del Perú",
                true,
                false,
                R.drawable.ic_payment,
                "Hace 5 días",
                200.00
        ));

        return methods;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerPaymentMethods = null;
        adapter = null;
        paymentMethodsList = null;
        preferenceManager = null;
        Log.d(TAG, "Vista destruida y referencias limpiadas");
    }
}