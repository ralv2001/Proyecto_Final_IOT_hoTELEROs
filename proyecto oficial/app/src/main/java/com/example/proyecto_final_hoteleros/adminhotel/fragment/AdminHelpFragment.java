package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.R;

public class AdminHelpFragment extends Fragment {

    private CardView cardFAQ;
    private CardView cardContactSupport;
    private CardView cardUserGuide;
    private CardView cardTechnicalSupport;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_help, container, false);

        initViews(rootView);
        setupClickListeners();

        return rootView;
    }

    private void initViews(View rootView) {
        cardFAQ = rootView.findViewById(R.id.cardFAQ);
        cardContactSupport = rootView.findViewById(R.id.cardContactSupport);
        cardUserGuide = rootView.findViewById(R.id.cardUserGuide);
        cardTechnicalSupport = rootView.findViewById(R.id.cardTechnicalSupport);

        // Back button
        rootView.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
    }

    private void setupClickListeners() {
        cardFAQ.setOnClickListener(v -> {
            // TODO: Navigate to FAQ section
        });

        cardContactSupport.setOnClickListener(v -> {
            // Open email client
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:soporte@hotelitos.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Consulta - Admin Hotel");
            startActivity(Intent.createChooser(emailIntent, "Enviar email"));
        });

        cardUserGuide.setOnClickListener(v -> {
            // TODO: Open user guide PDF or web page
        });

        cardTechnicalSupport.setOnClickListener(v -> {
            // Open phone dialer
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
            phoneIntent.setData(Uri.parse("tel:+51987654321"));
            startActivity(phoneIntent);
        });
    }
}