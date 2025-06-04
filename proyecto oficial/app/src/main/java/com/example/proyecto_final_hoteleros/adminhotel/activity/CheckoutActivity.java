package com.example.proyecto_final_hoteleros.adminhotel.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_final_hoteleros.MainActivity;
import com.example.proyecto_final_hoteleros.R;

public class CheckoutActivity extends AppCompatActivity {

    private Button btnAgregarCargo;
    private Button btnConfirmarPago;
    private ImageView btnBack;
    private TextView tvTotal;
    private LinearLayout paymentMethodsContainer;

    private double totalAmount = 380.0;
    private String selectedPaymentMethod = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_hotel_checkout);

        initViews();
        setupClickListeners();
        setupPaymentMethods();
    }

    private void initViews() {
        btnAgregarCargo = findViewById(R.id.btnAgregarCargo);
        btnConfirmarPago = findViewById(R.id.btnConfirmarPago);
        btnBack = findViewById(R.id.btnBack);
        tvTotal = findViewById(R.id.tvTotal);

        // Inicializar otros views si es necesario
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnAgregarCargo.setOnClickListener(v -> showAddChargeDialog());

        btnConfirmarPago.setOnClickListener(v -> {
            if (selectedPaymentMethod.isEmpty()) {
                Toast.makeText(this, "Por favor selecciona un método de pago", Toast.LENGTH_SHORT).show();
            } else {
                processPayment();
            }
        });
    }

    private void setupPaymentMethods() {
        // Configurar listeners para métodos de pago
        LinearLayout visaMethod = findViewById(R.id.visaPaymentMethod);
        LinearLayout mastercardMethod = findViewById(R.id.mastercardPaymentMethod);
        LinearLayout paypalMethod = findViewById(R.id.paypalPaymentMethod);

        if (visaMethod != null) {
            visaMethod.setOnClickListener(v -> selectPaymentMethod("visa", visaMethod));
        }

        if (mastercardMethod != null) {
            mastercardMethod.setOnClickListener(v -> selectPaymentMethod("mastercard", mastercardMethod));
        }

        if (paypalMethod != null) {
            paypalMethod.setOnClickListener(v -> selectPaymentMethod("paypal", paypalMethod));
        }
    }

    private void selectPaymentMethod(String method, LinearLayout selectedView) {
        selectedPaymentMethod = method;

        // Reset all payment methods background
        resetPaymentMethodsBackground();

        // Highlight selected method
        selectedView.setBackgroundResource(R.drawable.payment_method_selected_background);

        Toast.makeText(this, "Método seleccionado: " + method.toUpperCase(), Toast.LENGTH_SHORT).show();
    }

    private void resetPaymentMethodsBackground() {
        LinearLayout visaMethod = findViewById(R.id.visaPaymentMethod);
        LinearLayout mastercardMethod = findViewById(R.id.mastercardPaymentMethod);
        LinearLayout paypalMethod = findViewById(R.id.paypalPaymentMethod);

        if (visaMethod != null) {
            visaMethod.setBackgroundResource(R.drawable.payment_method_background);
        }
        if (mastercardMethod != null) {
            mastercardMethod.setBackgroundResource(R.drawable.payment_method_background);
        }
        if (paypalMethod != null) {
            paypalMethod.setBackgroundResource(R.drawable.payment_method_background);
        }
    }

    private void showAddChargeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Crear layout del dialog
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 40, 50, 40);

        // Campo para monto
        TextView montoLabel = new TextView(this);
        montoLabel.setText("Monto a agregar:");
        montoLabel.setTextSize(16);
        montoLabel.setTextColor(getResources().getColor(android.R.color.black));
        dialogLayout.addView(montoLabel);

        EditText editMonto = new EditText(this);
        editMonto.setHint("S/ 0.00");
        editMonto.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        LinearLayout.LayoutParams montoParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        montoParams.setMargins(0, 10, 0, 20);
        editMonto.setLayoutParams(montoParams);
        dialogLayout.addView(editMonto);

        // Campo para motivo
        TextView motivoLabel = new TextView(this);
        motivoLabel.setText("Motivo:");
        motivoLabel.setTextSize(16);
        motivoLabel.setTextColor(getResources().getColor(android.R.color.black));
        dialogLayout.addView(motivoLabel);

        EditText editMotivo = new EditText(this);
        editMotivo.setHint("Ej. Servicio adicional");
        LinearLayout.LayoutParams motivoParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        motivoParams.setMargins(0, 10, 0, 0);
        editMotivo.setLayoutParams(motivoParams);
        dialogLayout.addView(editMotivo);

        builder.setView(dialogLayout);
        builder.setTitle("Agregar Cargo");

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String montoStr = editMonto.getText().toString().trim();
            String motivo = editMotivo.getText().toString().trim();

            if (!montoStr.isEmpty() && !motivo.isEmpty()) {
                try {
                    double monto = Double.parseDouble(montoStr);
                    addChargeToInvoice(motivo, monto);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addChargeToInvoice(String description, double amount) {
        // Agregar el cargo a la factura
        totalAmount += amount;

        // Actualizar la UI - Aquí puedes agregar el item a tu layout dinámicamente
        // o actualizar el total
        updateTotalAmount();

        Toast.makeText(this, "Cargo agregado: " + description + " - $" + amount, Toast.LENGTH_LONG).show();
    }

    private void updateTotalAmount() {
        if (tvTotal != null) {
            tvTotal.setText("$" + String.format("%.2f", totalAmount));
        }
    }

    private void processPayment() {
        // Mostrar dialog de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Pago");
        builder.setMessage("¿Confirmar pago de $" + String.format("%.2f", totalAmount) +
                " con " + selectedPaymentMethod.toUpperCase() + "?");

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            // Simular procesamiento de pago
            showProcessingDialog();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void showProcessingDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Procesando pago...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Simular tiempo de procesamiento
        new Handler().postDelayed(() -> {
            progressDialog.dismiss();
            showPaymentSuccess();
        }, 2000);
    }

    private void showPaymentSuccess() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¡Pago Exitoso!");
        builder.setMessage("El pago de $" + String.format("%.2f", totalAmount) + " ha sido procesado correctamente.");
        builder.setIcon(R.drawable.ic_success);

        builder.setPositiveButton("Continuar", (dialog, which) -> {
            // Regresar a la pantalla principal o mostrar resumen
            finishCheckout();
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void finishCheckout() {
        // Crear intent para la pantalla de resumen o principal
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("checkout_completed", true);
        intent.putExtra("total_paid", totalAmount);
        startActivity(intent);
        finish();
    }

    // Método para cargar datos del huésped (si vienen de otra activity)
    private void loadGuestData() {
        Intent intent = getIntent();
        if (intent != null) {
            String guestName = intent.getStringExtra("guest_name");
            String guestDni = intent.getStringExtra("guest_dni");
            String checkIn = intent.getStringExtra("check_in");
            String checkOut = intent.getStringExtra("check_out");

            // Actualizar UI con los datos
            if (guestName != null) {
                TextView tvName = findViewById(R.id.tvName);
                if (tvName != null) {
                    tvName.setText("Nombre completo: " + guestName);
                }
            }

            if (guestDni != null) {
                TextView tvDni = findViewById(R.id.tvDni);
                if (tvDni != null) {
                    tvDni.setText("DNI: " + guestDni);
                }
            }

            if (checkIn != null) {
                TextView tvCheckIn = findViewById(R.id.tvCheckIn);
                if (tvCheckIn != null) {
                    tvCheckIn.setText(checkIn);
                }
            }

            if (checkOut != null) {
                TextView tvCheckOut = findViewById(R.id.tvCheckOut);
                if (tvCheckOut != null) {
                    tvCheckOut.setText(checkOut);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGuestData();
    }

    // Método para formatear moneda
    private String formatCurrency(double amount) {
        return "$" + String.format("%.2f", amount);
    }

    // Método para validar campos
    private boolean validatePaymentData() {
        if (selectedPaymentMethod.isEmpty()) {
            Toast.makeText(this, "Selecciona un método de pago", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (totalAmount <= 0) {
            Toast.makeText(this, "El monto total debe ser mayor a cero", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Override del método onBackPressed para mostrar confirmación
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancelar Checkout");
        builder.setMessage("¿Estás seguro de que quieres cancelar el checkout?");

        builder.setPositiveButton("Sí, cancelar", (dialog, which) -> {
            super.onBackPressed();
        });

        builder.setNegativeButton("No, continuar", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }
}