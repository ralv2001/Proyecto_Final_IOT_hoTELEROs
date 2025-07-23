package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

import java.util.List;

/**
 * Adapter para ViewPager2 que muestra imágenes del hotel
 * Maneja tanto URLs reales como recursos drawable
 */
public class HotelImageAdapter extends RecyclerView.Adapter<HotelImageAdapter.ImageViewHolder> {

    private static final String TAG = "HotelImageAdapter";

    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    private List<String> imageUrls; // URLs o nombres de recursos
    private OnImageClickListener clickListener;

    public HotelImageAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
        Log.d(TAG, "🖼️ Adapter creado con " + (imageUrls != null ? imageUrls.size() : 0) + " imágenes");
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.client_item_hotel_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        if (imageUrls != null && position < imageUrls.size()) {
            String imageUrl = imageUrls.get(position);
            holder.bind(imageUrl, position);
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    public void updateImages(List<String> newImageUrls) {
        this.imageUrls = newImageUrls;
        notifyDataSetChanged();
        Log.d(TAG, "📷 Imágenes actualizadas: " + (newImageUrls != null ? newImageUrls.size() : 0));
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_hotel_image);
        }

        public void bind(String imageUrl, int position) {
            if (imageUrl == null) {
                Log.w(TAG, "⚠️ URL de imagen nula en posición: " + position);
                imageView.setImageResource(R.drawable.hotel_placeholder);
                return;
            }

            Log.d(TAG, "🔄 Cargando imagen " + (position + 1) + ": " +
                    (imageUrl.length() > 50 ? imageUrl.substring(0, 50) + "..." : imageUrl));

            if (imageUrl.startsWith("http")) {
                // ✅ CARGAR IMAGEN REAL DESDE URL
                loadImageFromUrl(imageUrl, position);
            } else {
                // ✅ CARGAR IMAGEN DESDE RECURSOS LOCALES
                loadImageFromResources(imageUrl, position);
            }

            // Configurar click listener
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onImageClick(position);
                }
            });
        }

        private void loadImageFromUrl(String imageUrl, int position) {
            Log.d(TAG, "📸 Cargando imagen real desde URL para posición: " + position);

            Glide.with(imageView.getContext())
                    .load(imageUrl)

                    // ✅ PLACEHOLDERS
                    .placeholder(R.drawable.hotel_placeholder) // Mientras carga
                    .error(R.drawable.hotel_placeholder)       // Si falla
                    .fallback(R.drawable.hotel_placeholder)    // Si URL es null

                    // ✅ CONFIGURACIÓN DE CACHE OPTIMIZADA
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache en disco
                    .skipMemoryCache(false)                   // Usar cache de memoria

                    // ✅ TIMEOUTS Y OPTIMIZACIÓN
                    .timeout(30000) // 30 segundos timeout
                    .override(1200, 800) // Redimensionar para pantalla completa
                    .centerCrop()

                    // ✅ CONFIGURACIÓN DE FORMATO PARA AHORRAR MEMORIA
                    .format(DecodeFormat.PREFER_ARGB_8888) // Mejor calidad para galería principal

                    // ✅ LISTENER PARA DEBUG Y MANEJO DE ERRORES
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "❌ Error cargando imagen " + (position + 1) + ": " +
                                    (e != null ? e.getMessage() : "Error desconocido"));

                            if (e != null && e.getCauses() != null) {
                                for (Throwable cause : e.getCauses()) {
                                    Log.e(TAG, "   Causa: " + cause.getMessage());
                                }
                            }

                            // Intentar cargar placeholder explícitamente
                            imageView.setImageResource(R.drawable.hotel_placeholder);
                            return true; // true = manejamos el error
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            Log.d(TAG, "✅ Imagen " + (position + 1) + " cargada exitosamente desde: " + dataSource);
                            return false; // false = continuar con flujo normal
                        }
                    })
                    .into(imageView);
        }

        private void loadImageFromResources(String imageName, int position) {
            Log.d(TAG, "🖼️ Cargando imagen desde recursos para posición: " + position + " (" + imageName + ")");

            try {
                // Intentar cargar como recurso drawable
                int resID = imageView.getContext().getResources().getIdentifier(
                        imageName, "drawable", imageView.getContext().getPackageName());

                if (resID != 0) {
                    // ✅ USAR GLIDE TAMBIÉN PARA RECURSOS LOCALES (mejor manejo de memoria)
                    Glide.with(imageView.getContext())
                            .load(resID)
                            .placeholder(R.drawable.hotel_placeholder)
                            .error(R.drawable.hotel_placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .centerCrop()
                            .into(imageView);

                    Log.d(TAG, "✅ Recurso drawable cargado: " + imageName);
                } else {
                    Log.w(TAG, "⚠️ Recurso drawable no encontrado: " + imageName + ", usando placeholder");
                    imageView.setImageResource(R.drawable.hotel_placeholder);
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error cargando recurso " + imageName + ": " + e.getMessage());
                imageView.setImageResource(R.drawable.hotel_placeholder);
            }
        }
    }
}