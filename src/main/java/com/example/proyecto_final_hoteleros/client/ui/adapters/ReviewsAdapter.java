// client/ui/adapters/ReviewsAdapter.java
package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.Review;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private List<Review> reviews;

    public ReviewsAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.client_item_review_card, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivReviewerAvatar;
        private TextView tvReviewerName;
        private TextView tvReviewDate;
        private TextView tvReviewText;
        private TextView tvRating;
        private LinearLayout ratingStarsContainer;
        private TextView tvReviewerLocation;
        private ImageView ivVerifiedBadge;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivReviewerAvatar = itemView.findViewById(R.id.iv_reviewer_avatar);
            tvReviewerName = itemView.findViewById(R.id.tv_reviewer_name);
            tvReviewDate = itemView.findViewById(R.id.tv_review_date);
            tvReviewText = itemView.findViewById(R.id.tv_review_text);
            tvRating = itemView.findViewById(R.id.tv_rating);
            ratingStarsContainer = itemView.findViewById(R.id.rating_stars_container);
            tvReviewerLocation = itemView.findViewById(R.id.tv_reviewer_location);
            ivVerifiedBadge = itemView.findViewById(R.id.iv_verified_badge);
        }

        public void bind(Review review) {
            tvReviewerName.setText(review.getReviewerName());
            tvReviewDate.setText(review.getDate());
            tvReviewText.setText(review.getReviewText());
            tvRating.setText(String.format("%.1f", review.getRating()));
            tvReviewerLocation.setText(review.getReviewerLocation());

            // Configurar avatar
            int avatarResource = itemView.getContext().getResources()
                    .getIdentifier(review.getReviewerAvatar(), "drawable",
                            itemView.getContext().getPackageName());
            if (avatarResource > 0) {
                ivReviewerAvatar.setImageResource(avatarResource);
            } else {
                ivReviewerAvatar.setImageResource(R.drawable.perfil);
            }

            // Mostrar badge de verificado
            ivVerifiedBadge.setVisibility(review.isVerified() ? View.VISIBLE : View.GONE);

            // Crear estrellas dinámicamente
            createStars(review.getRating());
        }

        private void createStars(float rating) {
            ratingStarsContainer.removeAllViews();

            for (int i = 1; i <= 5; i++) {
                ImageView star = new ImageView(itemView.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        dpToPx(16), dpToPx(16));
                params.setMargins(dpToPx(1), 0, dpToPx(1), 0);
                star.setLayoutParams(params);

                if (i <= rating) {
                    star.setImageResource(R.drawable.ic_star);
                    star.setColorFilter(itemView.getContext().getColor(R.color.star_yellow));
                } else if (i - 0.5f <= rating) {
                    star.setImageResource(R.drawable.ic_star);
                    star.setColorFilter(itemView.getContext().getColor(R.color.star_yellow));
                    star.setAlpha(0.5f);
                } else {
                    star.setImageResource(R.drawable.ic_star);
                    star.setColorFilter(itemView.getContext().getColor(R.color.light_gray));
                }

                ratingStarsContainer.addView(star);
            }
        }

        private int dpToPx(int dp) {
            float density = itemView.getContext().getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}