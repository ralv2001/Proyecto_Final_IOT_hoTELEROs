package com.example.proyecto_final_hoteleros.client.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.SearchContext;
import com.example.proyecto_final_hoteleros.client.ui.fragment.CustomDatePickerBottomSheet;
import com.example.proyecto_final_hoteleros.client.ui.fragment.GuestCountBottomSheet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ModifySearchDialog extends Dialog {

    private static final String TAG = "ModifySearchDialog";
    public static final int REQUEST_CODE_LOCATION = 1001;

    private AppCompatActivity activity;
    private SearchContext searchContext;
    private String currentLocation, currentDates, currentGuests;
    private OnSearchModifiedListener listener;
    private OnLocationRequestListener locationRequestListener;

    // Views
    private LinearLayout layoutLocationModify, layoutDatesModify, layoutGuestsModify;
    private TextView tvLocationModify, tvDatesModify, tvGuestsModify;
    private TextView tvLocationStatus;
    private Button btnApplyModify, btnCancelModify;
    private ImageButton btnClose;
    private ImageView ivChevronLocation, ivChevronDates, ivChevronGuests;

    public interface OnSearchModifiedListener {
        void onSearchModified(String newLocation, String newDates, String newGuests);
    }

    public interface OnLocationRequestListener {
        void onLocationRequested(String currentLocation);
    }

    public ModifySearchDialog(@NonNull AppCompatActivity activity, SearchContext searchContext,
                              String location, String dates, String guests) {
        super(activity);
        this.activity = activity;
        this.searchContext = searchContext;
        this.currentLocation = location != null ? location : "Todas las ubicaciones";
        this.currentDates = dates != null ? dates : "Fechas flexibles";
        this.currentGuests = guests != null ? guests : "2 adultos";

        Log.d(TAG, "Dialog creado con: location=" + this.currentLocation +
                ", dates=" + this.currentDates + ", guests=" + this.currentGuests);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.client_dialog_modify_search);

        setupWindow();
        initViews();
        setupViews();
        setupListeners();
        setupEntranceAnimation();
    }

    // ✅ MEJORADO: Ventana al centro con mejor configuración
    private void setupWindow() {
        Window window = getWindow();
        if (window != null) {
            // ✅ CAMBIAR: Centrar en lugar de abajo
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER; // ✅ CENTRO en lugar de BOTTOM
            params.width = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.9);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;

            // ✅ AGREGAR: Animaciones personalizadas
            window.setAttributes(params);
            window.setBackgroundDrawableResource(android.R.color.transparent);

            // ✅ NUEVO: Animación de entrada/salida más elegante
            window.setWindowAnimations(R.style.DialogSlideAnimation);

            // ✅ AGREGAR: Sombra suave alrededor
            window.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.6f); // Fondo semi-transparente
        }
    }

    // ✅ NUEVO: Animación de entrada personalizada
    private void setupEntranceAnimation() {
        View dialogContent = findViewById(R.id.dialog_content);
        if (dialogContent != null) {
            // Iniciar invisible y con escala pequeña
            dialogContent.setScaleX(0.8f);
            dialogContent.setScaleY(0.8f);
            dialogContent.setAlpha(0f);

            // Animar entrada
            dialogContent.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(1.1f))
                    .start();
        }
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
        btnCancelModify = findViewById(R.id.btn_cancel_modify);
        btnClose = findViewById(R.id.btn_close_modify);

        ivChevronLocation = findViewById(R.id.iv_chevron_location);
        ivChevronDates = findViewById(R.id.iv_chevron_dates);
        ivChevronGuests = findViewById(R.id.iv_chevron_guests);
    }

    private void setupViews() {
        setupColors();

        tvLocationModify.setText(currentLocation);
        tvDatesModify.setText(currentDates);
        tvGuestsModify.setText(currentGuests);

        if (!searchContext.isLocationModifiable()) {
            disableSection(layoutLocationModify, ivChevronLocation);
            if (searchContext == SearchContext.NEARBY_HOTELS) {
                if (tvLocationStatus != null) {
                    tvLocationStatus.setVisibility(View.VISIBLE);
                    tvLocationStatus.setText("📍 Mostrando hoteles cerca de tu ubicación actual");
                    tvLocationStatus.setTextColor(ContextCompat.getColor(activity, R.color.orange));
                }
            }
        }

        if (!searchContext.areDatesModifiable()) {
            disableSection(layoutDatesModify, ivChevronDates);
        }

        if (!searchContext.areGuestsModifiable()) {
            disableSection(layoutGuestsModify, ivChevronGuests);
        }
    }

    private void setupColors() {
        btnApplyModify.setBackgroundTintList(
                ContextCompat.getColorStateList(activity, R.color.orange));
        btnApplyModify.setTextColor(ContextCompat.getColor(activity, android.R.color.white));

        btnCancelModify.setBackgroundTintList(
                ContextCompat.getColorStateList(activity, android.R.color.transparent));
        btnCancelModify.setTextColor(ContextCompat.getColor(activity, R.color.gray));
    }

    private void disableSection(LinearLayout layout, ImageView chevron) {
        layout.setAlpha(0.5f);
        layout.setEnabled(false);
        layout.setClickable(false);
        chevron.setVisibility(View.GONE);
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismissWithAnimation());
        btnCancelModify.setOnClickListener(v -> dismissWithAnimation());

        btnApplyModify.setOnClickListener(v -> {
            Log.d(TAG, "Aplicar cambios presionado. Valores: " +
                    currentLocation + ", " + currentDates + ", " + currentGuests);
            confirmChanges();
        });

        if (searchContext.isLocationModifiable()) {
            layoutLocationModify.setOnClickListener(v -> {
                Log.d(TAG, "Ubicación clickeada");
                requestLocationChange();
            });
        }

        if (searchContext.areDatesModifiable()) {
            layoutDatesModify.setOnClickListener(v -> {
                Log.d(TAG, "Fechas clickeadas");
                openDatePicker();
            });
        }

        if (searchContext.areGuestsModifiable()) {
            layoutGuestsModify.setOnClickListener(v -> {
                Log.d(TAG, "Huéspedes clickeados");
                openGuestSelector();
            });
        }
    }

    // ✅ NUEVO: Animación de salida personalizada
    private void dismissWithAnimation() {
        View dialogContent = findViewById(R.id.dialog_content);
        if (dialogContent != null) {
            dialogContent.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        ModifySearchDialog.super.dismiss();
                    })
                    .start();
        } else {
            super.dismiss();
        }
    }

    @Override
    public void dismiss() {
        dismissWithAnimation();
    }

    // ✅ RESTO DE MÉTODOS SIN CAMBIOS (openDatePicker, openGuestSelector, etc.)
    private void requestLocationChange() {
        if (locationRequestListener != null) {
            showStatusMessage("Abriendo selector...", ContextCompat.getColor(activity, R.color.orange));
            locationRequestListener.onLocationRequested(currentLocation);
        } else {
            Log.e(TAG, "Location listener es null!");
        }
    }

    private void openDatePicker() {
        try {
            Log.d(TAG, "Abriendo selector de fechas...");
            CustomDatePickerBottomSheet datePickerBottomSheet = new CustomDatePickerBottomSheet();

            Bundle args = new Bundle();
            args.putString("current_dates", currentDates);
            datePickerBottomSheet.setArguments(args);

            datePickerBottomSheet.setListener(new CustomDatePickerBottomSheet.DateRangeListener() {
                @Override
                public void onDateRangeSelected(Date startDate, Date endDate) {
                    Log.d(TAG, "Fechas seleccionadas: " + startDate + " - " + endDate);

                    SimpleDateFormat format = new SimpleDateFormat("dd MMM", new Locale("es", "ES"));
                    currentDates = format.format(startDate) + " - " + format.format(endDate);
                    tvDatesModify.setText(currentDates);
                    showStatusMessage("✓ Fechas actualizadas", ContextCompat.getColor(activity, R.color.green));

                    Log.d(TAG, "Fechas formateadas: " + currentDates);
                }
            });

            datePickerBottomSheet.show(activity.getSupportFragmentManager(), "date_picker");

        } catch (Exception e) {
            Log.e(TAG, "Error al abrir selector de fechas", e);
            showStatusMessage("Error al abrir fechas", ContextCompat.getColor(activity, R.color.red));
        }
    }

    private void openGuestSelector() {
        try {
            Log.d(TAG, "Abriendo selector de huéspedes...");
            GuestCountBottomSheet guestBottomSheet = new GuestCountBottomSheet();

            Bundle args = new Bundle();
            args.putString("current_guests", currentGuests);
            guestBottomSheet.setArguments(args);

            guestBottomSheet.setListener(new GuestCountBottomSheet.Listener() {
                @Override
                public void onGuestCount(int adults, int children) {
                    Log.d(TAG, "Huéspedes seleccionados: " + adults + " adultos, " + children + " niños");

                    currentGuests = formatGuestsString(adults, children);
                    tvGuestsModify.setText(currentGuests);
                    showStatusMessage("✓ Huéspedes actualizados", ContextCompat.getColor(activity, R.color.green));

                    Log.d(TAG, "Huéspedes formateados: " + currentGuests);
                }
            });

            guestBottomSheet.show(activity.getSupportFragmentManager(), "guest_count");

        } catch (Exception e) {
            Log.e(TAG, "Error al abrir selector de huéspedes", e);
            showStatusMessage("Error al abrir huéspedes", ContextCompat.getColor(activity, R.color.red));
        }
    }

    private String formatGuestsString(int adults, int children) {
        if (adults == 1 && children == 0) {
            return "1 huésped";
        } else if (adults == 2 && children == 0) {
            return "2 adultos";
        } else {
            StringBuilder guestString = new StringBuilder();
            guestString.append(adults).append(" adulto").append(adults > 1 ? "s" : "");
            if (children > 0) {
                guestString.append(" • ").append(children).append(" niño").append(children > 1 ? "s" : "");
            }
            return guestString.toString();
        }
    }

    private void confirmChanges() {
        Log.d(TAG, "Confirmando cambios: " + currentLocation + ", " + currentDates + ", " + currentGuests);

        if (listener != null) {
            listener.onSearchModified(currentLocation, currentDates, currentGuests);
            showStatusMessage("✓ Cambios aplicados correctamente", ContextCompat.getColor(activity, R.color.green));

            tvLocationStatus.postDelayed(() -> {
                Log.d(TAG, "Cerrando diálogo después de aplicar cambios");
                dismissWithAnimation();
            }, 1000);
        } else {
            Log.e(TAG, "Listener es null!");
            showStatusMessage("Error: No se pudo aplicar", ContextCompat.getColor(activity, R.color.red));
            tvLocationStatus.postDelayed(() -> dismissWithAnimation(), 2000);
        }
    }

    private void showStatusMessage(String message, int color) {
        tvLocationStatus.setVisibility(View.VISIBLE);
        tvLocationStatus.setText(message);
        tvLocationStatus.setTextColor(color);

        tvLocationStatus.postDelayed(() -> {
            if (tvLocationStatus != null) {
                tvLocationStatus.setVisibility(View.GONE);
            }
        }, 3000);
    }

    public void updateLocation(String newLocation) {
        if (newLocation != null && !newLocation.equals(currentLocation)) {
            currentLocation = newLocation;
            tvLocationModify.setText(newLocation);
            showStatusMessage("✓ Ubicación actualizada", ContextCompat.getColor(activity, R.color.green));
            Log.d(TAG, "Ubicación actualizada a: " + newLocation);
        }
    }

    public void setOnSearchModifiedListener(OnSearchModifiedListener listener) {
        this.listener = listener;
        Log.d(TAG, "Listener configurado: " + (listener != null));
    }

    public void setOnLocationRequestListener(OnLocationRequestListener listener) {
        this.locationRequestListener = listener;
        Log.d(TAG, "Location request listener configurado: " + (listener != null));
    }
}