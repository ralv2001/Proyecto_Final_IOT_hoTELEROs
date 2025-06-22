package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.ServiceDetailAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.DamageDetailAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.model.CheckoutItem;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CheckoutInvoiceDialog extends Dialog {

    private Context context;
    private CheckoutItem checkoutItem;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    // Views
    private TextView tvInvoiceNumber, tvInvoiceDate;
    private TextView tvGuestName, tvRoomNumber, tvDates;
    private TextView tvRoomCost, tvServicesCost, tvDamagesCost, tvTotalCost;
    private TextView tvPaymentMethod, tvPaymentStatus;
    private RecyclerView rvServices, rvDamages;
    private TextView tvNoServices, tvNoDamages;
    private ImageView ivClose;

    public CheckoutInvoiceDialog(Context context, CheckoutItem checkoutItem) {
        super(context, android.R.style.Theme_Material_Dialog_NoActionBar);
        this.context = context;
        this.checkoutItem = checkoutItem;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        setupDialog();
        initViews();
        setupAdapters();
        loadData();
        setupClickListeners();
    }

    private void setupDialog() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.admin_hotel_dialog_checkout_invoice);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    private void initViews() {
        tvInvoiceNumber = findViewById(R.id.tvInvoiceNumber);
        tvInvoiceDate = findViewById(R.id.tvInvoiceDate);
        tvGuestName = findViewById(R.id.tvGuestName);
        tvRoomNumber = findViewById(R.id.tvRoomNumber);
        tvDates = findViewById(R.id.tvDates);
        tvRoomCost = findViewById(R.id.tvRoomCost);
        tvServicesCost = findViewById(R.id.tvServicesCost);
        tvDamagesCost = findViewById(R.id.tvDamagesCost);
        tvTotalCost = findViewById(R.id.tvTotalCost);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        rvServices = findViewById(R.id.rvServices);
        rvDamages = findViewById(R.id.rvDamages);
        tvNoServices = findViewById(R.id.tvNoServices);
        tvNoDamages = findViewById(R.id.tvNoDamages);
        ivClose = findViewById(R.id.ivClose);
    }

    private void setupAdapters() {
        // Services adapter
        ServiceDetailAdapter servicesAdapter = new ServiceDetailAdapter(context, checkoutItem.getServiciosAdicionales());
        rvServices.setLayoutManager(new LinearLayoutManager(context));
        rvServices.setAdapter(servicesAdapter);

        // Damages adapter
        DamageDetailAdapter damagesAdapter = new DamageDetailAdapter(context, checkoutItem.getDanos());
        rvDamages.setLayoutManager(new LinearLayoutManager(context));
        rvDamages.setAdapter(damagesAdapter);
    }

    private void loadData() {
        // Invoice info
        tvInvoiceNumber.setText("FACT-" + checkoutItem.getId());
        tvInvoiceDate.setText(dateFormat.format(new Date()));

        // Guest info
        tvGuestName.setText(checkoutItem.getNombreHuesped());
        tvRoomNumber.setText("Habitación " + checkoutItem.getNumeroHabitacion() +
                (checkoutItem.getTipoHabitacion() != null ? " • " + checkoutItem.getTipoHabitacion() : ""));
        tvDates.setText(checkoutItem.getFechaCheckIn() + " - " + checkoutItem.getFechaCheckOut() +
                " (" + checkoutItem.getNumeroNoches() + " noche" + (checkoutItem.getNumeroNoches() != 1 ? "s" : "") + ")");

        // Costs
        tvRoomCost.setText(currencyFormat.format(checkoutItem.getCostoHabitacion()));
        tvServicesCost.setText(currencyFormat.format(checkoutItem.getTotalServicios()));
        tvDamagesCost.setText(currencyFormat.format(checkoutItem.getTotalDanos()));
        tvTotalCost.setText(currencyFormat.format(checkoutItem.getTotalGeneral()));

        // Payment info
        tvPaymentMethod.setText(checkoutItem.getMetodoPago() != null ? checkoutItem.getMetodoPago() : "Efectivo");
        tvPaymentStatus.setText(checkoutItem.isPagado() ? "✅ PAGADO" : "❌ PENDIENTE");

        // Services visibility
        if (checkoutItem.getServiciosAdicionales().isEmpty()) {
            rvServices.setVisibility(View.GONE);
            tvNoServices.setVisibility(View.VISIBLE);
        } else {
            rvServices.setVisibility(View.VISIBLE);
            tvNoServices.setVisibility(View.GONE);
        }

        // Damages visibility
        if (checkoutItem.getDanos().isEmpty()) {
            rvDamages.setVisibility(View.GONE);
            tvNoDamages.setVisibility(View.VISIBLE);
        } else {
            rvDamages.setVisibility(View.VISIBLE);
            tvNoDamages.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        ivClose.setOnClickListener(v -> dismiss());
    }
}