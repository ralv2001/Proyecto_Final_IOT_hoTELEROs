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

    public interface Listener { void onGuestCount(int adults, int children); }
    private Listener listener;
    public void setListener(Listener l) { listener = l; }

    private int adults = 1, children = 0;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup parent, Bundle b) {
        try {
            View v = inflater.inflate(R.layout.client_bottom_sheet_guest_count, parent, false);

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

            // OK
            btnOk.setOnClickListener(x -> {
                if (listener != null) listener.onGuestCount(adults, children);
                dismiss();
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
}