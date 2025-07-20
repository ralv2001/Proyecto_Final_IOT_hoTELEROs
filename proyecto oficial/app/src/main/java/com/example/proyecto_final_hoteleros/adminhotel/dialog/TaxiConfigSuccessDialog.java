package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;

import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Locale;

public class TaxiConfigSuccessDialog extends AppCompatDialog {

    private static final String TAG = "TaxiConfigSuccessDialog";

    private Context context;
    private TextView tvConfigDetails;
    private MaterialButton btnClose;
    private NumberFormat currencyFormat;

    // ✅ CONSTRUCTOR SIMPLE - Sin parámetros problemáticos
    public TaxiConfigSuccessDialog(Context context) {
        super(context, R.style.DialogTheme);
        this.context = context;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

        setupDialog();
    }

    // ✅ CONSTRUCTOR CON MONTO (opcional)
    public TaxiConfigSuccessDialog(Context context, double minAmount) {
        super(context, R.style.DialogTheme);
        this.context = context;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

        setupDialogWithAmount(minAmount);
    }

    // ✅ MÉTODO SIN PARÁMETROS
    private void setupDialog() {
        setContentView(R.layout.admin_hotel_dialog_taxi_config_success);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        initializeViews();
        setupDefaultContent();
        setupListeners();
    }

    // ✅ MÉTODO CON MONTO
    private void setupDialogWithAmount(double minAmount) {
        setContentView(R.layout.admin_hotel_dialog_taxi_config_success);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        initializeViews();
        setupContent(minAmount);
        setupListeners();
    }

    private void initializeViews() {
        tvConfigDetails = findViewById(R.id.tvConfigDetails);
        btnClose = findViewById(R.id.btnClose);
    }

    // ✅ CONTENIDO POR DEFECTO
    private void setupDefaultContent() {
        if (tvConfigDetails != null) {
            String details = "✅ Configuración de taxi guardada exitosamente\n\n" +
                    "• La configuración se aplicará a todas las futuras reservas\n" +
                    "• Los huéspedes serán notificados automáticamente\n" +
                    "• Puedes modificar la configuración en cualquier momento\n" +
                    "• Configuración sincronizada con Firebase";
            tvConfigDetails.setText(details);
        }
    }

    // ✅ CONTENIDO CON MONTO ESPECÍFICO
    private void setupContent(double minAmount) {
        if (tvConfigDetails != null) {
            String formattedAmount = currencyFormat.format(minAmount);
            String details = String.format(
                    "✅ Configuración de taxi guardada exitosamente\n\n" +
                            "• Monto mínimo para taxi gratuito: %s\n" +
                            "• Los huéspedes recibirán taxi gratis al superar este monto\n" +
                            "• La configuración se aplicará a todas las futuras reservas\n" +
                            "• Configuración guardada exitosamente en Firebase",
                    formattedAmount
            );
            tvConfigDetails.setText(details);
        }
    }

    private void setupListeners() {
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }
    }
}