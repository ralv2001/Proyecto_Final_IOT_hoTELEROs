package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class GuestCountBottomSheet extends BottomSheetDialogFragment {
    private static final String TAG = "GuestCountBottomSheet";

    // ✅ INTERFACE ORIGINAL (mantener compatibilidad)
    public interface Listener {
        void onGuestCount(int adults, int children);
    }

    // ✅ NUEVA INTERFACE para ModifySearchDialog
    public interface OnGuestsSelectedListener {
        void onGuestsSelected(int adults, int children);
    }

    // ✅ Ambos listeners
    private Listener listener;
    private OnGuestsSelectedListener guestsSelectedListener;

    private int adults = 1, children = 0;

    // ✅ Setters para ambos tipos de listener
    public void setListener(Listener l) {
        listener = l;
    }

    public void setOnGuestsSelectedListener(OnGuestsSelectedListener listener) {
        this.guestsSelectedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup parent, Bundle b) {
        try {
            View v = inflater.inflate(R.layout.client_bottom_sheet_guest_count, parent, false);

            // ✅ NUEVO: Leer huéspedes actuales de argumentos si existen
            Bundle args = getArguments();
            if (args != null && args.containsKey("current_guests")) {
                String currentGuests = args.getString("current_guests");
                parseCurrentGuests(currentGuests);
            }

            // Encontrar todas las vistas
            TextView tvAdults = v.findViewById(R.id.tvAdultsCount);
            TextView tvChildren = v.findViewById(R.id.tvChildrenCount);
            ImageButton btnAddAdult = v.findViewById(R.id.btnAddAdult);
            ImageButton btnSubAdult = v.findViewById(R.id.btnSubAdult);
            ImageButton btnAddChild = v.findViewById(R.id.btnAddChild);
            ImageButton btnSubChild = v.findViewById(R.id.btnSubChild);
            View btnOk = v.findViewById(R.id.btnGuestOk);

            // Configurar valores iniciales
            tvAdults.setText(String.valueOf(adults));
            tvChildren.setText(String.valueOf(children));

            // Listeners de + / –
            btnAddAdult.setOnClickListener(x -> {
                adults++;
                tvAdults.setText(String.valueOf(adults));
            });

            btnSubAdult.setOnClickListener(x -> {
                if (adults > 1) {
                    adults--;
                    tvAdults.setText(String.valueOf(adults));
                }
            });

            btnAddChild.setOnClickListener(x -> {
                children++;
                tvChildren.setText(String.valueOf(children));
            });

            btnSubChild.setOnClickListener(x -> {
                if (children > 0) {
                    children--;
                    tvChildren.setText(String.valueOf(children));
                }
            });

            // ✅ MODIFICADO: OK usa método unificado
            btnOk.setOnClickListener(x -> {
                confirmGuests(); // ✅ Usar método unificado
            });

            return v;
        } catch (Exception e) {
            Log.e(TAG, "Error al crear la vista: " + e.getMessage(), e);
            // En caso de error, devolver una vista mínima para evitar crash
            TextView errorView = new TextView(getContext());
            errorView.setText("Error al cargar. Intente de nuevo.");
            return errorView;
        }
    }

    // ✅ NUEVO: Parsear huéspedes actuales desde string
    private void parseCurrentGuests(String currentGuests) {
        try {
            if (currentGuests != null) {
                if (currentGuests.equals("1 huésped")) {
                    adults = 1;
                    children = 0;
                } else if (currentGuests.equals("2 adultos")) {
                    adults = 2;
                    children = 0;
                } else {
                    // Parsear formato "X adultos • Y niños"
                    String[] parts = currentGuests.split(" • ");

                    // Parsear adultos
                    if (parts.length >= 1) {
                        String adultPart = parts[0].trim();
                        if (adultPart.contains(" adulto")) {
                            String numberStr = adultPart.replace(" adultos", "").replace(" adulto", "").trim();
                            adults = Integer.parseInt(numberStr);
                        }
                    }

                    // Parsear niños
                    if (parts.length >= 2) {
                        String childPart = parts[1].trim();
                        if (childPart.contains(" niño")) {
                            String numberStr = childPart.replace(" niños", "").replace(" niño", "").trim();
                            children = Integer.parseInt(numberStr);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error parsing guests, using defaults: " + e.getMessage());
            // Usar valores por defecto si hay error
            adults = 2;
            children = 0;
        }
    }

    // ✅ NUEVO: Método unificado para confirmar huéspedes
    private void confirmGuests() {
        // ✅ Llamar al listener original si existe
        if (listener != null) {
            listener.onGuestCount(adults, children);
        }

        // ✅ Llamar al nuevo listener si existe
        if (guestsSelectedListener != null) {
            guestsSelectedListener.onGuestsSelected(adults, children);
        }

        dismiss();
    }
}