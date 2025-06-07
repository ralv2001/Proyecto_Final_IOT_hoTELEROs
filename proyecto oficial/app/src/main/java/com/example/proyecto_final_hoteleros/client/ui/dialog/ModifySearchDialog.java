package com.example.proyecto_final_hoteleros.client.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.SearchContext;
import com.example.proyecto_final_hoteleros.client.ui.activity.HotelResultsActivity;
import com.example.proyecto_final_hoteleros.client.ui.activity.LocationSelectorActivity;
import com.example.proyecto_final_hoteleros.client.ui.fragment.CustomDatePickerBottomSheet;
import com.example.proyecto_final_hoteleros.client.ui.fragment.GuestCountBottomSheet;

public class ModifySearchDialog extends Dialog {

    private SearchContext searchContext;
    private String currentLocation, currentDates, currentGuests;
    private OnSearchModifiedListener listener;

    // Views
    private LinearLayout layoutLocationModify, layoutDatesModify, layoutGuestsModify;
    private TextView tvLocationModify, tvDatesModify, tvGuestsModify;
    private TextView tvLocationStatus;
    private Button btnApplyModify;
    public static final int REQUEST_CODE_LOCATION = 1001;


    public interface OnSearchModifiedListener {
        void onSearchModified(String newLocation, String newDates, String newGuests);
    }

    public ModifySearchDialog(@NonNull Context context, SearchContext searchContext,
                              String location, String dates, String guests) {
        super(context);
        this.searchContext = searchContext;
        this.currentLocation = location;
        this.currentDates = dates;
        this.currentGuests = guests;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.client_dialog_modify_search);

        // Configurar ventana
        Window window = getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            window.getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        }

        initViews();
        setupViews();
        setupListeners();
    }

    private void initViews() {
        layoutLocationModify = findViewById(R.id.layout_location_modify);
        layoutDatesModify = findViewById(R.id.layout_dates_modify);
        layoutGuestsModify = findViewById(R.id.layout_guests_modify);

        tvLocationModify = findViewById(R.id.tv_location_modify);
        tvDatesModify = findViewById(R.id.tv_dates_modify);
        tvGuestsModify = findViewById(R.id.tv_guests_modify);
        tvLocationStatus = findViewById(R.id.tv_location_status);
        btnApplyModify = findViewById(R.id.btn_apply_modify);
    }

    private void setupViews() {
        // Ubicación
        tvLocationModify.setText(currentLocation != null ? currentLocation : "Todas las ubicaciones");

        if (!searchContext.isLocationModifiable()) {
            layoutLocationModify.setAlpha(0.6f);
            layoutLocationModify.setEnabled(false);
            tvLocationStatus.setVisibility(View.VISIBLE);
            tvLocationStatus.setText("Fijo para este contexto");
            findViewById(R.id.iv_chevron_location).setVisibility(View.GONE);
        } else {
            layoutLocationModify.setAlpha(1.0f);
            layoutLocationModify.setEnabled(true);
            tvLocationStatus.setVisibility(View.GONE);
        }

        // Fechas
        tvDatesModify.setText(currentDates != null ? currentDates : "Fechas flexibles");
        if (!searchContext.areDatesModifiable()) {
            layoutDatesModify.setAlpha(0.6f);
            layoutDatesModify.setEnabled(false);
            findViewById(R.id.iv_chevron_dates).setVisibility(View.GONE);
        }

        // Huéspedes
        tvGuestsModify.setText(currentGuests != null ? currentGuests : "2 adultos");
        if (!searchContext.areGuestsModifiable()) {
            layoutGuestsModify.setAlpha(0.6f);
            layoutGuestsModify.setEnabled(false);
            findViewById(R.id.iv_chevron_guests).setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        // Botón cerrar
        ImageButton btnClose = findViewById(R.id.btn_close_modify);
        btnClose.setOnClickListener(v -> dismiss());

        // Botón cancelar
        findViewById(R.id.btn_cancel_modify).setOnClickListener(v -> dismiss());

        // Botón aplicar
        btnApplyModify.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSearchModified(currentLocation, currentDates, currentGuests);
            }
            dismiss();
        });

        // ✅ CLICKS SOLO EN SECCIONES MODIFICABLES
        if (searchContext.isLocationModifiable()) {
            layoutLocationModify.setOnClickListener(v -> {
                addClickFeedback(v);
                openLocationSelector();
                // ✅ MOSTRAR MENSAJE INFORMATIVO (temporalmente)
                tvLocationStatus.setVisibility(View.VISIBLE);
                tvLocationStatus.setText("Función en desarrollo");
                v.postDelayed(() -> tvLocationStatus.setVisibility(View.GONE), 2000);
            });
        }

        if (searchContext.areDatesModifiable()) {
            layoutDatesModify.setOnClickListener(v -> {
                addClickFeedback(v);
                openDatePicker();
            });
        }

        if (searchContext.areGuestsModifiable()) {
            layoutGuestsModify.setOnClickListener(v -> {
                addClickFeedback(v);
                openGuestSelector();
            });
        }
    }

    // ✅ IMPLEMENTACIÓN COMPLETA: Abrir selector de fechas
    private void openDatePicker() {
        CustomDatePickerBottomSheet datePickerBottomSheet = new CustomDatePickerBottomSheet();

        Bundle args = new Bundle();
        args.putString("current_dates", currentDates);
        datePickerBottomSheet.setArguments(args);

        datePickerBottomSheet.setOnDatesSelectedListener(new CustomDatePickerBottomSheet.OnDatesSelectedListener() {
            @Override
            public void onDatesSelected(String startDate, String endDate) {
                currentDates = startDate + " - " + endDate;
                tvDatesModify.setText(currentDates);
                updateApplyButtonState();
            }
        });

        if (getContext() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getContext();
            datePickerBottomSheet.show(activity.getSupportFragmentManager(), "date_picker_dialog");
        }
    }

    // ✅ IMPLEMENTACIÓN COMPLETA: Abrir selector de huéspedes
    private void openGuestSelector() {
        GuestCountBottomSheet guestBottomSheet = new GuestCountBottomSheet();

        Bundle args = new Bundle();
        args.putString("current_guests", currentGuests);
        guestBottomSheet.setArguments(args);

        guestBottomSheet.setOnGuestsSelectedListener(new GuestCountBottomSheet.OnGuestsSelectedListener() {
            @Override
            public void onGuestsSelected(int adults, int children) {
                if (adults == 1 && children == 0) {
                    currentGuests = "1 huésped";
                } else if (adults == 2 && children == 0) {
                    currentGuests = "2 adultos";
                } else {
                    StringBuilder guestString = new StringBuilder();
                    guestString.append(adults).append(" adulto").append(adults > 1 ? "s" : "");
                    if (children > 0) {
                        guestString.append(" • ").append(children).append(" niño").append(children > 1 ? "s" : "");
                    }
                    currentGuests = guestString.toString();
                }

                tvGuestsModify.setText(currentGuests);
                updateApplyButtonState();
            }
        });

        if (getContext() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getContext();
            guestBottomSheet.show(activity.getSupportFragmentManager(), "guest_count_dialog");
        }
    }

    private void addClickFeedback(View view) {
        view.animate()
                .scaleX(0.98f)
                .scaleY(0.98f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    private void updateApplyButtonState() {
        btnApplyModify.setAlpha(1.0f);
        btnApplyModify.setEnabled(true);
    }

    public void setOnSearchModifiedListener(OnSearchModifiedListener listener) {
        this.listener = listener;
    }
    private void openLocationSelector() {
        Intent intent = new Intent(getContext(), LocationSelectorActivity.class);
        intent.putExtra("current_location", currentLocation);
        intent.putExtra("context_type", searchContext.name());

        if (getContext() instanceof HotelResultsActivity) {
            HotelResultsActivity activity = (HotelResultsActivity) getContext();
            activity.startActivityForResult(intent, REQUEST_CODE_LOCATION);
        }

        dismiss(); // Cerrar el diálogo mientras se abre la actividad
    }
}