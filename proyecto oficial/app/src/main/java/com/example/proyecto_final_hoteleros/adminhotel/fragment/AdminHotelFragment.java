package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.fragment.HotelProfileFragment;
import com.example.proyecto_final_hoteleros.adminhotel.fragment.RoomManagementFragment;
import com.example.proyecto_final_hoteleros.adminhotel.fragment.ServiceManagementFragment;
import com.google.android.material.card.MaterialCardView;

public class AdminHotelFragment extends Fragment {

    // Header TextViews
    private TextView tvMainTitle;
    private TextView tvSubtitle;

    // Management Cards - REMOVIDO cardEstadisticas
    private MaterialCardView cardHabitaciones;
    private MaterialCardView cardServicios;
    private MaterialCardView cardPerfilHotel;

    // Stats Cards
    private MaterialCardView cardOcupacion;
    private MaterialCardView cardIngresos;
    private MaterialCardView cardHuespedes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_hotel_management, container, false);

        initViews(rootView);
        setupClickListeners();
        startWelcomeAnimation();

        return rootView;
    }

    private void initViews(View rootView) {
        // HEADER
        tvMainTitle = rootView.findViewById(R.id.tvMainTitle);
        tvSubtitle = rootView.findViewById(R.id.tvSubtitle);

        // Management cards - REMOVIDO cardEstadisticas
        cardHabitaciones = rootView.findViewById(R.id.cardHabitaciones);
        cardServicios = rootView.findViewById(R.id.cardServicios);
        cardPerfilHotel = rootView.findViewById(R.id.cardPerfilHotel);

        // Stats cards
        cardOcupacion = rootView.findViewById(R.id.cardOcupacion);
        cardIngresos = rootView.findViewById(R.id.cardIngresos);
        cardHuespedes = rootView.findViewById(R.id.cardHuespedes);

        // Set dynamic greeting
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "Buenos días" : hour < 18 ? "Buenas tardes" : "Buenas noches";
        tvMainTitle.setText(greeting + ", Admin");
        tvSubtitle.setText("Hotel Belmond • Dashboard Administrativo");
    }

    private void setupClickListeners() {
        cardHabitaciones.setOnClickListener(v -> {
            animatePress(cardHabitaciones);
            navigateToRoomManagement();
        });

        cardServicios.setOnClickListener(v -> {
            animatePress(cardServicios);
            navigateToServiceManagement();
        });

        cardPerfilHotel.setOnClickListener(v -> {
            animatePress(cardPerfilHotel);
            navigateToHotelProfile();
        });

        // ❌ REMOVIDO: setupClickListeners para cardEstadisticas

        // Stats cards - solo muestran información
        cardOcupacion.setOnClickListener(v -> {
            animatePress(cardOcupacion);
            Toast.makeText(getContext(), "📈 Ocupación: 85% de habitaciones", Toast.LENGTH_SHORT).show();
        });

        cardIngresos.setOnClickListener(v -> {
            animatePress(cardIngresos);
            Toast.makeText(getContext(), "💰 Ingresos del día: S/ 12,450", Toast.LENGTH_SHORT).show();
        });

        cardHuespedes.setOnClickListener(v -> {
            animatePress(cardHuespedes);
            Toast.makeText(getContext(), "👥 34 huéspedes en el hotel", Toast.LENGTH_SHORT).show();
        });
    }

    private void navigateToRoomManagement() {
        RoomManagementFragment fragment = new RoomManagementFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToServiceManagement() {
        ServiceManagementFragment fragment = new ServiceManagementFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToHotelProfile() {
        HotelProfileFragment fragment = new HotelProfileFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void startWelcomeAnimation() {
        new Handler().postDelayed(() -> {
            if (getActivity() != null && isAdded()) {
                animateStatsCards();
                animateManagementCards();
            }
        }, 300);
    }

    private void animateStatsCards() {
        MaterialCardView[] statsCards = {cardOcupacion, cardIngresos, cardHuespedes};

        for (int i = 0; i < statsCards.length; i++) {
            if (statsCards[i] != null) {
                statsCards[i].setAlpha(0f);
                statsCards[i].setTranslationY(100f);

                statsCards[i].animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(800)
                        .setStartDelay(i * 150)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        }
    }

    private void animateManagementCards() {
        MaterialCardView[] managementCards = {cardHabitaciones, cardServicios, cardPerfilHotel};

        for (int i = 0; i < managementCards.length; i++) {
            if (managementCards[i] != null) {
                managementCards[i].setAlpha(0f);
                managementCards[i].setScaleX(0.8f);
                managementCards[i].setScaleY(0.8f);

                managementCards[i].animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(600)
                        .setStartDelay(800 + (i * 100))
                        .setInterpolator(new OvershootInterpolator())
                        .start();
            }
        }
    }

    private void animatePress(MaterialCardView card) {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(card, "scaleX", 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(card, "scaleY", 0.95f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);
        scaleDownX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleDownY.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(card, "scaleX", 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(card, "scaleY", 1f);
        scaleUpX.setDuration(100);
        scaleUpY.setDuration(100);
        scaleUpX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleUpY.setInterpolator(new AccelerateDecelerateInterpolator());

        animatorSet.play(scaleDownX).with(scaleDownY);
        animatorSet.play(scaleUpX).with(scaleUpY).after(scaleDownX);
        animatorSet.start();
    }
}