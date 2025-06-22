// client/ui/fragment/AllReviewsFragment.java
package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.adapters.ReviewsAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.Review;

import java.util.ArrayList;
import java.util.List;

public class AllReviewsFragment extends Fragment {

    private RecyclerView rvReviews;
    private TextView tvHotelName, tvTotalReviews, tvAverageRating;
    private ImageButton btnBack;
    private ReviewsAdapter adapter;
    private List<Review> reviewsList = new ArrayList<>();
    private String hotelName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_fragment_all_reviews, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        getArgumentsData();
        setupRecyclerView();
        loadReviews();
        setupActions();
    }

    private void initViews(View view) {
        rvReviews = view.findViewById(R.id.rv_reviews);
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        tvTotalReviews = view.findViewById(R.id.tv_total_reviews);
        tvAverageRating = view.findViewById(R.id.tv_average_rating);
        btnBack = view.findViewById(R.id.btn_back);
    }

    private void getArgumentsData() {
        if (getArguments() != null) {
            hotelName = getArguments().getString("hotel_name", "Hotel");
            tvHotelName.setText(hotelName);
        }
    }

    private void setupRecyclerView() {
        adapter = new ReviewsAdapter(reviewsList);
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setAdapter(adapter);
    }

    private void loadReviews() {
        // Datos de ejemplo - en producción vendrían de una API
        reviewsList.clear();

        reviewsList.add(new Review("1", "Adrián López", "perfil",
                "El servicio es excelente y me encantaron las instalaciones. ¡Buen trabajo! La atención fue muy personalizada y el hotel tiene una ubicación perfecta.",
                4.9f, "2 días atrás", "Lima, Perú", true));

        reviewsList.add(new Review("2", "Ximena Sánchez", "perfil",
                "Muy buena experiencia, definitivamente regresaría. El desayuno buffet es increíble y las vistas desde la habitación son espectaculares.",
                4.0f, "5 días atrás", "Arequipa, Perú", true));

        reviewsList.add(new Review("3", "Carlos Mendoza", "perfil",
                "Hotel excepcional con un servicio de primera clase. El spa y la piscina son increíbles. Sin duda uno de los mejores hoteles en los que me he hospedado.",
                5.0f, "1 semana atrás", "Cusco, Perú", true));

        reviewsList.add(new Review("4", "María García", "perfil",
                "Muy buena ubicación y excelente atención al cliente. Las habitaciones son amplias y cómodas. El personal siempre estuvo dispuesto a ayudar.",
                4.5f, "2 semanas atrás", "Trujillo, Perú", false));

        reviewsList.add(new Review("5", "Roberto Silva", "perfil",
                "Una experiencia inolvidable. La gastronomía del restaurante es excepcional y el servicio de concierge nos ayudó a planificar toda nuestra estadía.",
                4.8f, "3 semanas atrás", "Chiclayo, Perú", true));

        // Calcular estadísticas
        updateStatistics();
        adapter.notifyDataSetChanged();
    }

    private void updateStatistics() {
        if (reviewsList.isEmpty()) return;

        float totalRating = 0;
        for (Review review : reviewsList) {
            totalRating += review.getRating();
        }

        float averageRating = totalRating / reviewsList.size();
        tvAverageRating.setText(String.format("%.1f", averageRating));
        tvTotalReviews.setText("Basado en " + reviewsList.size() + " reseñas");
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
    }
}