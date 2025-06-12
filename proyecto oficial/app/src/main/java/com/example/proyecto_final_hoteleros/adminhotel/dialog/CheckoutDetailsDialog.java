package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.DamageDetailAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.ServiceDetailAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.model.CheckoutItem;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Locale;

public class CheckoutDetailsDialog extends Dialog implements DamageDetailAdapter.OnDamageClickListener {

    private Context context;
    private CheckoutItem checkoutItem;
    private OnCheckoutProcessedListener listener;
    private NumberFormat currencyFormat;

    // Views
    private TextView tvGuestName, tvRoomNumber, tvDates, tvStatus;
    private TextView tvRoomCost, tvServicesCost, tvDamagesCost, tvTotalCost;
    private RecyclerView rvServices, rvDamages;
    private TextView tvNoDamages;
    private Button btnAddDamage, btnProcessCheckout, btnCancel;
    private ImageView ivClose;

    // Adapters
    private ServiceDetailAdapter servicesAdapter;
    private DamageDetailAdapter damagesAdapter;

    public interface OnCheckoutProcessedListener {
        void onCheckoutProcessed(CheckoutItem checkout);
        void onCheckoutCancelled();
        void onDamageAdded(CheckoutItem updatedCheckout);
        void onDamageUpdated(CheckoutItem updatedCheckout);
    }

    public CheckoutDetailsDialog(Context context, CheckoutItem checkoutItem) {
        super(context, android.R.style.Theme_Material_Dialog_NoActionBar);
        this.context = context;
        this.checkoutItem = checkoutItem;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

        setupDialog();
        initViews();
        setupAdapters();
        loadData();
        setupClickListeners();
    }

    private void setupDialog() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.admin_hotel_dialog_checkout_details);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // CORRECCIÓN PRINCIPAL: Configurar dimensiones correctas
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenHeight = displayMetrics.heightPixels;
            int screenWidth = displayMetrics.widthPixels;

            // Usar 90% del ancho y 80% de la altura máxima
            window.setLayout(
                    (int) (screenWidth * 0.9),
                    (int) (screenHeight * 0.8)
            );

            // Centrar el diálogo
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.CENTER;
            window.setAttributes(layoutParams);
        }

        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    private void initViews() {
        tvGuestName = findViewById(R.id.tvGuestName);
        ivClose = findViewById(R.id.ivClose);
        tvRoomNumber = findViewById(R.id.tvRoomNumber);
        tvDates = findViewById(R.id.tvDates);
        tvStatus = findViewById(R.id.tvStatus);
        tvRoomCost = findViewById(R.id.tvRoomCost);
        tvServicesCost = findViewById(R.id.tvServicesCost);
        tvDamagesCost = findViewById(R.id.tvDamagesCost);
        tvTotalCost = findViewById(R.id.tvTotalCost);
        rvServices = findViewById(R.id.rvServices);
        rvDamages = findViewById(R.id.rvDamages);
        tvNoDamages = findViewById(R.id.tvNoDamages);
        btnAddDamage = findViewById(R.id.btnAddDamage);
        btnProcessCheckout = findViewById(R.id.btnProcessCheckout);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupAdapters() {
        servicesAdapter = new ServiceDetailAdapter(context, checkoutItem.getServiciosAdicionales());
        rvServices.setLayoutManager(new LinearLayoutManager(context));
        rvServices.setAdapter(servicesAdapter);

        damagesAdapter = new DamageDetailAdapter(context, checkoutItem.getDanos());
        damagesAdapter.setOnDamageClickListener(this);
        rvDamages.setLayoutManager(new LinearLayoutManager(context));
        rvDamages.setAdapter(damagesAdapter);
    }

    private void loadData() {
        tvGuestName.setText(checkoutItem.getNombreHuesped());
        tvRoomNumber.setText("Hab. " + checkoutItem.getNumeroHabitacion() +
                (checkoutItem.getTipoHabitacion() != null ? " • " + checkoutItem.getTipoHabitacion() : ""));
        tvDates.setText(checkoutItem.getFechaCheckIn() + " - " + checkoutItem.getFechaCheckOut() +
                " (" + checkoutItem.getNumeroNoches() + " noche" + (checkoutItem.getNumeroNoches() != 1 ? "s" : "") + ")");
        tvStatus.setText(checkoutItem.getEstado());

        updateCostDisplay();
        updateDamagesVisibility();

        // Si está completado, ocultar botones de acción
        if (checkoutItem.getEstado().equals("Completado")) {
            btnAddDamage.setVisibility(View.GONE);
            btnProcessCheckout.setVisibility(View.GONE);
            btnCancel.setText("Cerrar");
        }
    }

    private void updateCostDisplay() {
        tvRoomCost.setText(currencyFormat.format(checkoutItem.getCostoHabitacion()));
        tvServicesCost.setText(currencyFormat.format(checkoutItem.getTotalServicios()));
        tvDamagesCost.setText(currencyFormat.format(checkoutItem.getTotalDanos()));
        tvTotalCost.setText(currencyFormat.format(checkoutItem.getTotalGeneral()));
    }

    private void updateDamagesVisibility() {
        if (checkoutItem.getDanos().isEmpty()) {
            rvDamages.setVisibility(View.GONE);
            if (tvNoDamages != null) {
                tvNoDamages.setVisibility(View.VISIBLE);
            }
        } else {
            rvDamages.setVisibility(View.VISIBLE);
            if (tvNoDamages != null) {
                tvNoDamages.setVisibility(View.GONE);
            }
        }
    }

    private void setupClickListeners() {
        ivClose.setOnClickListener(v -> dismiss());

        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCheckoutCancelled();
            }
            dismiss();
        });

        btnProcessCheckout.setOnClickListener(v -> processCheckout());
        btnAddDamage.setOnClickListener(v -> showAddDamageDialog());
    }

    private void processCheckout() {
        if (checkoutItem.getEstado().equals("Completado")) {
            Toast.makeText(context, "Este checkout ya fue completado", Toast.LENGTH_SHORT).show();
            return;
        }

        String mensaje = "¿Confirmar checkout?\n\n" +
                "👤 " + checkoutItem.getNombreHuesped() + "\n" +
                "🏨 Habitación " + checkoutItem.getNumeroHabitacion() + "\n" +
                "💰 TOTAL: " + currencyFormat.format(checkoutItem.getTotalGeneral());

        new AlertDialog.Builder(context)
                .setTitle("🏨 Procesar Checkout")
                .setMessage(mensaje)
                .setPositiveButton("✅ PROCESAR", (dialog, which) -> {
                    checkoutItem.setEstado("Completado");
                    checkoutItem.setPagado(true);
                    checkoutItem.setMetodoPago("Efectivo");

                    Toast.makeText(context, "🎉 ¡Checkout procesado exitosamente!", Toast.LENGTH_LONG).show();

                    if (listener != null) {
                        listener.onCheckoutProcessed(checkoutItem);
                    }
                    dismiss();
                })
                .setNegativeButton("❌ Cancelar", null)
                .show();
    }

    private void showAddDamageDialog() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.admin_hotel_dialog_add_damage, null);

        TextInputEditText etDescription = dialogView.findViewById(R.id.etDamageDescription);
        AutoCompleteTextView etSeverity = dialogView.findViewById(R.id.etDamageSeverity);
        TextInputEditText etCost = dialogView.findViewById(R.id.etDamageCost);

        String[] severityOptions = {"Leve", "Moderado", "Severo", "Crítico"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, severityOptions);
        etSeverity.setAdapter(adapter);

        // CORRECCIÓN: Usar AlertDialog.Builder sin tema específico
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        // CORRECCIÓN: Configurar ventana correctamente
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Usar WRAP_CONTENT para ambas dimensiones
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            // Configurar posicionamiento
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.CENTER;
            layoutParams.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9); // 90% del ancho
            window.setAttributes(layoutParams);
        }

        dialogView.findViewById(R.id.btnCancelDamage).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnSaveDamage).setOnClickListener(v -> {
            String description = etDescription.getText().toString().trim();
            String severity = etSeverity.getText().toString().trim();
            String costStr = etCost.getText().toString().trim();

            if (description.isEmpty() || severity.isEmpty() || costStr.isEmpty()) {
                Toast.makeText(context, "❌ Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double cost = Double.parseDouble(costStr);

                if (cost <= 0) {
                    Toast.makeText(context, "❌ El costo debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                CheckoutItem.DanoHabitacion newDamage = new CheckoutItem.DanoHabitacion(
                        description, cost, severity, getCurrentDate()
                );
                newDamage.setConfirmado(true);

                checkoutItem.addDano(newDamage);
                damagesAdapter.notifyDataSetChanged();
                updateCostDisplay();
                updateDamagesVisibility();

                Toast.makeText(context, "✅ Daño agregado: " + currencyFormat.format(cost), Toast.LENGTH_LONG).show();

                if (listener != null) {
                    listener.onDamageAdded(checkoutItem);
                }

                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(context, "❌ Ingresa un costo válido", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
    private String getCurrentDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }

    @Override
    public void onEditDamage(CheckoutItem.DanoHabitacion damage, int position) {
        // Implementar editar daño si es necesario
    }

    @Override
    public void onDeleteDamage(CheckoutItem.DanoHabitacion damage, int position) {
        new AlertDialog.Builder(context)
                .setTitle("🗑️ Eliminar Daño")
                .setMessage("¿Eliminar este daño?\n\n📝 " + damage.getDescripcion() + "\n💰 " + currencyFormat.format(damage.getCosto()))
                .setPositiveButton("🗑️ Eliminar", (dialog, which) -> {
                    checkoutItem.getDanos().remove(position);
                    checkoutItem.calculateTotal();
                    damagesAdapter.notifyItemRemoved(position);
                    updateCostDisplay();
                    updateDamagesVisibility();

                    if (listener != null) {
                        listener.onDamageUpdated(checkoutItem);
                    }

                    Toast.makeText(context, "🗑️ Daño eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("❌ Cancelar", null)
                .show();
    }

    public void setOnCheckoutProcessedListener(OnCheckoutProcessedListener listener) {
        this.listener = listener;
    }
}