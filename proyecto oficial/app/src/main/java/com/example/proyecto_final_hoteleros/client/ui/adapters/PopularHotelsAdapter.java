package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.Hotel;

import java.util.List;

public class PopularHotelsAdapter extends RecyclerView.Adapter<PopularHotelsAdapter.PopularHotelViewHolder> {

    private static final String TAG = "PopularHotelsAdapter";

    private List<Hotel> hotelList;
    private OnHotelClickListener listener;

    public interface OnHotelClickListener {
        void onHotelClick(Hotel hotel, int position);
    }

    public PopularHotelsAdapter(List<Hotel> hotelList) {
        this.hotelList = hotelList;
    }

    public void setOnHotelClickListener(OnHotelClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PopularHotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.client_item_popular_hotel_card, parent, false);
        return new PopularHotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularHotelViewHolder holder, int position) {
        Hotel hotel = hotelList.get(position);

        // Configurar datos básicos
        holder.tvHotelName.setText(hotel.getName());
        holder.tvLocation.setText(hotel.getLocation());
        holder.tvPrice.setText(hotel.getPrice());
        holder.tvRating.setText(hotel.getRating());

        // ✅ CARGAR IMAGEN REAL O PLACEHOLDER
        loadHotelImage(holder.ivHotelImage, hotel.getImageUrl(), hotel.getName());

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHotelClick(hotel, position);
            }
        });
    }

    // ✅ MÉTODO MEJORADO: Cargar imagen desde URL o usar placeholder
    private void loadHotelImage(ImageView imageView, String imageUrl, String hotelName) {
        if (imageUrl != null && imageUrl.startsWith("http")) {
            // ✅ CARGAR IMAGEN REAL DESDE URL
            Log.d(TAG, "📸 Cargando imagen real para hotel popular: " + hotelName);

            Glide.with(imageView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.hotel_placeholder) // Mientras carga
                    .error(R.drawable.hotel_placeholder)       // Si falla
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache para mejor rendimiento
                    .centerCrop()
                    .into(imageView);
        } else {
            // ✅ USAR IMAGEN PLACEHOLDER O DRAWABLE
            Log.d(TAG, "🖼️ Usando placeholder para hotel popular: " + hotelName);

            if (imageUrl != null && !imageUrl.equals("hotel_placeholder")) {
                // Intentar cargar como recurso drawable
                int resID = imageView.getContext().getResources().getIdentifier(
                        imageUrl, "drawable", imageView.getContext().getPackageName()
                );

                if (resID != 0) {
                    imageView.setImageResource(resID);
                } else {
                    imageView.setImageResource(R.drawable.hotel_placeholder);
                }
            } else {
                imageView.setImageResource(R.drawable.hotel_placeholder);
            }
        }
    }

    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    class PopularHotelViewHolder extends RecyclerView.ViewHolder {
        TextView tvHotelName, tvLocation, tvPrice, tvRating;
        ImageView ivHotelImage;

        public PopularHotelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvRating = itemView.findViewById(R.id.tvRating);
            ivHotelImage = itemView.findViewById(R.id.ivHotelImage);
        }
    }
}