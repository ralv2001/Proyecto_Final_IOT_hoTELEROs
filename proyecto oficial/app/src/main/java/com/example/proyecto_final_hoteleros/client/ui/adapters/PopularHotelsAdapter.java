package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
        Log.d(TAG, "🔍 Intentando cargar imagen para: " + hotelName);
        Log.d(TAG, "📸 URL: " + (imageUrl != null ? imageUrl.substring(0, Math.min(50, imageUrl.length())) + "..." : "null"));

        if (imageUrl != null && imageUrl.startsWith("http")) {
            // ✅ CONFIGURACIÓN ROBUSTA DE GLIDE PARA DISPOSITIVOS REALES
            Glide.with(imageView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.hotel_placeholder) // Mientras carga
                    .error(R.drawable.hotel_placeholder)       // Si falla
                    .fallback(R.drawable.hotel_placeholder)    // Si URL es null

                    // ✅ CONFIGURACIÓN DE CACHE OPTIMIZADA
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache en disco
                    .skipMemoryCache(false)                   // Usar cache de memoria

                    // ✅ TIMEOUTS EXTENDIDOS PARA DISPOSITIVOS LENTOS
                    .timeout(30000) // 30 segundos timeout

                    // ✅ CONFIGURACIÓN DE RESIZE PARA AHORRAR MEMORIA
                    .override(800, 600) // Redimensionar a máximo 800x600
                    .centerCrop()

                    // ✅ CONFIGURACIÓN DE FORMATO
                    .format(DecodeFormat.PREFER_RGB_565) // Menor uso de memoria

                    // ✅ LISTENER PARA DEBUG EN DISPOSITIVOS REALES
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "❌ Error cargando imagen para " + hotelName + ": " + e);
                            if (e != null && e.getCauses() != null) {
                                for (Throwable cause : e.getCauses()) {
                                    Log.e(TAG, "   Causa: " + cause.getMessage());
                                }
                            }
                            // Intentar cargar URL alternativa o placeholder
                            imageView.setImageResource(R.drawable.hotel_placeholder);
                            return true; // true = manejo el error, no mostrar error adicional
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG, "✅ Imagen cargada exitosamente para " + hotelName + " desde: " + dataSource);
                            return false; // false = continuar con el flujo normal
                        }
                    })
                    .into(imageView);

            Log.d(TAG, "🚀 Glide iniciado para: " + hotelName);
        } else {
            // ✅ MANEJAR IMAGEN PLACEHOLDER O DRAWABLE LOCAL
            Log.d(TAG, "🖼️ Usando imagen local para: " + hotelName);

            if (imageUrl != null && !imageUrl.equals("hotel_placeholder")) {
                // Intentar cargar como recurso drawable
                int resID = imageView.getContext().getResources().getIdentifier(
                        imageUrl, "drawable", imageView.getContext().getPackageName()
                );

                if (resID != 0) {
                    imageView.setImageResource(resID);
                    Log.d(TAG, "✅ Imagen drawable cargada: " + imageUrl);
                } else {
                    imageView.setImageResource(R.drawable.hotel_placeholder);
                    Log.d(TAG, "⚠️ Drawable no encontrado, usando placeholder");
                }
            } else {
                imageView.setImageResource(R.drawable.hotel_placeholder);
                Log.d(TAG, "📷 Usando placeholder por defecto");
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