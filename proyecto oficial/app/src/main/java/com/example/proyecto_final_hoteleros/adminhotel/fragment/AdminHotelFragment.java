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
import com.google.android.material.card.MaterialCardView;

public class AdminHotelFragment extends Fragment {

    // Header TextViews - IDs CORREGIDOS
    private TextView tvMainTitle;
    private TextView tvSubtitle;

    // Management Cards - USANDO MaterialCardView
    private MaterialCardView cardHabitaciones;
    private MaterialCardView cardServicios;
    private MaterialCardView cardPerfilHotel;
    private MaterialCardView cardEstadisticas;

    // Stats Cards - USANDO MaterialCardView
    private MaterialCardView cardOcupacion;
    private MaterialCardView cardIngresos;
    private MaterialCardView cardHuespedes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // USANDO EL NUEVO LAYOUT
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_hotel_management, container, false);

        initViews(rootView);
        setupClickListeners();
        startWelcomeAnimation();

        return rootView;
    }

    private void initViews(View rootView) {
        // HEADER - IDs ACTUALIZADOS
        tvMainTitle = rootView.findViewById(R.id.tvMainTitle);
        tvSubtitle = rootView.findViewById(R.id.tvSubtitle);

        // Management cards
        cardHabitaciones = rootView.findViewById(R.id.cardHabitaciones);
        cardServicios = rootView.findViewById(R.id.cardServicios);
        cardPerfilHotel = rootView.findViewById(R.id.cardPerfilHotel);
        cardEstadisticas = rootView.findViewById(R.id.cardEstadisticas);

        // Stats cards
        cardOcupacion = rootView.findViewById(R.id.cardOcupacion);
        cardIngresos = rootView.findViewById(R.id.cardIngresos);
        cardHuespedes = rootView.findViewById(R.id.cardHuespedes);

        // Set dynamic greeting
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "Buenos días" : hour < 18 ? "Buenas tardes" : "Buenas noches";
        tvMainTitle.setText(greeting + ", Admin");

        // Actualizar subtitle también
        tvSubtitle.setText("Hotel Belmond • Dashboard Administrativo");
    }

    private void setupClickListeners() {
        cardHabitaciones.setOnClickListener(v -> {
            animatePress(cardHabitaciones);
            Toast.makeText(getContext(), "Gestión de Habitaciones", Toast.LENGTH_SHORT).show();
        });

        cardServicios.setOnClickListener(v -> {
            animatePress(cardServicios);
            Toast.makeText(getContext(), "Gestión de Servicios", Toast.LENGTH_SHORT).show();
        });

        cardPerfilHotel.setOnClickListener(v -> {
            animatePress(cardPerfilHotel);
            Toast.makeText(getContext(), "Perfil del Hotel", Toast.LENGTH_SHORT).show();
        });

        cardEstadisticas.setOnClickListener(v -> {
            animatePress(cardEstadisticas);
            Toast.makeText(getContext(), "Estadísticas del Hotel", Toast.LENGTH_SHORT).show();
        });

        cardOcupacion.setOnClickListener(v -> {
            animatePress(cardOcupacion);
            Toast.makeText(getContext(), "Ocupación: 85% de habitaciones", Toast.LENGTH_SHORT).show();
        });

        cardIngresos.setOnClickListener(v -> {
            animatePress(cardIngresos);
            Toast.makeText(getContext(), "Ingresos del día: S/ 12,450", Toast.LENGTH_SHORT).show();
        });

        cardHuespedes.setOnClickListener(v -> {
            animatePress(cardHuespedes);
            Toast.makeText(getContext(), "34 huéspedes en el hotel", Toast.LENGTH_SHORT).show();
        });
        cardHabitaciones.setOnClickListener(v -> {
            animatePress(cardHabitaciones);

        });

        cardServicios.setOnClickListener(v -> {
            animatePress(cardServicios);

        });

        cardPerfilHotel.setOnClickListener(v -> {
            animatePress(cardPerfilHotel);
            navigateToHotelProfile();
        });

    }





    private void navigateToHotelProfile() {
        HotelProfileFragment fragment = new HotelProfileFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void startWelcomeAnimation() {
        // Ocultar todo inicialmente - USANDO IDs CORRECTOS
        tvMainTitle.setAlpha(0f);
        tvMainTitle.setTranslationY(-50f);
        tvMainTitle.setScaleX(0.8f);
        tvMainTitle.setScaleY(0.8f);

        tvSubtitle.setAlpha(0f);
        tvSubtitle.setTranslationX(-100f);

        // Animación chévere para el título principal
        new Handler().postDelayed(() -> {
            // Animación de entrada con bounce
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(tvMainTitle, "alpha", 0f, 1f);
            ObjectAnimator slideDown = ObjectAnimator.ofFloat(tvMainTitle, "translationY", -50f, 0f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(tvMainTitle, "scaleX", 0.8f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(tvMainTitle, "scaleY", 0.8f, 1.1f, 1f);

            AnimatorSet welcomeAnim = new AnimatorSet();
            welcomeAnim.playTogether(fadeIn, slideDown, scaleX, scaleY);
            welcomeAnim.setDuration(800);
            welcomeAnim.setInterpolator(new OvershootInterpolator(1.2f));
            welcomeAnim.start();

            // Efecto de typewriter para el subtitle
            new Handler().postDelayed(() -> {
                ObjectAnimator subtitleFade = ObjectAnimator.ofFloat(tvSubtitle, "alpha", 0f, 1f);
                ObjectAnimator subtitleSlide = ObjectAnimator.ofFloat(tvSubtitle, "translationX", -100f, 0f);

                AnimatorSet subtitleAnim = new AnimatorSet();
                subtitleAnim.playTogether(subtitleFade, subtitleSlide);
                subtitleAnim.setDuration(600);
                subtitleAnim.setInterpolator(new DecelerateInterpolator());
                subtitleAnim.start();
            }, 400);

        }, 200);

        // Animar cards después del saludo
        new Handler().postDelayed(() -> {
            animateCards();
        }, 1000);
    }

    private void animateCards() {
        MaterialCardView[] cards = {cardOcupacion, cardIngresos, cardHuespedes,
                cardHabitaciones, cardServicios, cardPerfilHotel, cardEstadisticas};

        for (int i = 0; i < cards.length; i++) {
            if (cards[i] != null) { // Verificación de seguridad
                cards[i].setAlpha(0f);
                cards[i].setTranslationY(100f);
                cards[i].setScaleX(0.8f);
                cards[i].setScaleY(0.8f);

                final int index = i;
                new Handler().postDelayed(() -> {
                    if (cards[index] != null) {
                        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(cards[index], "alpha", 0f, 1f);
                        ObjectAnimator slideUp = ObjectAnimator.ofFloat(cards[index], "translationY", 100f, 0f);
                        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cards[index], "scaleX", 0.8f, 1f);
                        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cards[index], "scaleY", 0.8f, 1f);

                        AnimatorSet cardAnim = new AnimatorSet();
                        cardAnim.playTogether(fadeIn, slideUp, scaleX, scaleY);
                        cardAnim.setDuration(500);
                        cardAnim.setInterpolator(new DecelerateInterpolator());
                        cardAnim.start();
                    }
                }, i * 100);
            }
        }
    }

    private void animatePress(MaterialCardView card) {
        if (card == null) return; // Verificación de seguridad

        // Animación de pulso al tocar - MEJORADA para MaterialCardView
        ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(card, "scaleY", 1f, 0.95f);
        ObjectAnimator elevation1 = ObjectAnimator.ofFloat(card, "cardElevation", 12f, 6f);

        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(card, "scaleX", 0.95f, 1.05f);
        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(card, "scaleY", 0.95f, 1.05f);
        ObjectAnimator elevation2 = ObjectAnimator.ofFloat(card, "cardElevation", 6f, 16f);

        ObjectAnimator scaleX3 = ObjectAnimator.ofFloat(card, "scaleX", 1.05f, 1f);
        ObjectAnimator scaleY3 = ObjectAnimator.ofFloat(card, "scaleY", 1.05f, 1f);
        ObjectAnimator elevation3 = ObjectAnimator.ofFloat(card, "cardElevation", 16f, 12f);

        AnimatorSet press = new AnimatorSet();
        press.playTogether(scaleX1, scaleY1, elevation1);
        press.setDuration(100);

        AnimatorSet bounce = new AnimatorSet();
        bounce.playTogether(scaleX2, scaleY2, elevation2);
        bounce.setDuration(150);
        bounce.setInterpolator(new BounceInterpolator());

        AnimatorSet normal = new AnimatorSet();
        normal.playTogether(scaleX3, scaleY3, elevation3);
        normal.setDuration(100);

        AnimatorSet fullAnim = new AnimatorSet();
        fullAnim.playSequentially(press, bounce, normal);
        fullAnim.start();
    }
}