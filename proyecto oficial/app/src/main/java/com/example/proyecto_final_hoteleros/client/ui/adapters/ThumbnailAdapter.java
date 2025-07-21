package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
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
 * Adapter para las miniaturas de imágenes del hotel
 * Muestra thumbnails pequeños que al hacer clic cambian la imagen principal
 */
public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder> {

    private static final String TAG = "ThumbnailAdapter";

    public interface OnThumbnailClickListener {
        void onThumbnailClick(int position);
    }

    private List<String> imageUrls; // URLs o nombres de recursos
    private OnThumbnailClickListener clickListener;
    private int selectedPosition = 0; // Posición actualmente seleccionada

    public ThumbnailAdapter(List<String> imageUrls, OnThumbnailClickListener clickListener) {
        this.imageUrls = imageUrls;
        this.clickListener = clickListener;
        Log.d(TAG, "🖼️ ThumbnailAdapter creado con " + (imageUrls != null ? imageUrls.size() : 0) + " imágenes");
    }

    @NonNull
    @Override
    public ThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.client_item_image_thumbnail, parent, false);
        return new ThumbnailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailViewHolder holder, int position) {
        if (imageUrls != null && position < imageUrls.size()) {
            String imageUrl = imageUrls.get(position);
            boolean isSelected = (position == selectedPosition);
            holder.bind(imageUrl, position, isSelected);
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    /**
     * Actualiza la posición seleccionada y refresca el adapter
     */
    public void updateSelectedPosition(int newPosition) {
        int oldPosition = selectedPosition;
        selectedPosition = newPosition;

        // Notificar cambios solo en las posiciones afectadas
        notifyItemChanged(oldPosition);
        notifyItemChanged(newPosition);

        Log.d(TAG, "📍 Thumbnail seleccionado actualizado: " + newPosition);
    }

    /**
     * Actualiza la lista de imágenes
     */
    public void updateImages(List<String> newImageUrls) {
        this.imageUrls = newImageUrls;
        this.selectedPosition = 0; // Resetear selección
        notifyDataSetChanged();
        Log.d(TAG, "📷 Thumbnails actualizados: " + (newImageUrls != null ? newImageUrls.size() : 0));
    }

    class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        private CardView cardContainer;
        private ImageView imageView;
        private View borderIndicator; // ✅ NUEVO: Para borde visible

        public ThumbnailViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_thumbnail_container);
            imageView = itemView.findViewById(R.id.iv_thumbnail);
            borderIndicator = itemView.findViewById(R.id.border_indicator); // ✅ NUEVO
        }

        public void bind(String imageUrl, int position, boolean isSelected) {
            if (imageUrl == null) {
                Log.w(TAG, "⚠️ URL de thumbnail nula en posición: " + position);
                imageView.setImageResource(R.drawable.hotel_placeholder);
                return;
            }

            Log.d(TAG, "🔄 Cargando thumbnail " + (position + 1) + " (seleccionado: " + isSelected + ")");

            // ✅ CONFIGURAR ESTADO VISUAL DE SELECCIÓN
            updateSelectionState(isSelected);

            if (imageUrl.startsWith("http")) {
                // ✅ CARGAR THUMBNAIL DESDE URL
                loadThumbnailFromUrl(imageUrl, position);
            } else {
                // ✅ CARGAR THUMBNAIL DESDE RECURSOS
                loadThumbnailFromResources(imageUrl, position);
            }

            // Configurar click listener
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    Log.d(TAG, "👆 Thumbnail " + (position + 1) + " clickeado");
                    clickListener.onThumbnailClick(position);
                } else {
                    Log.e(TAG, "❌ clickListener es null para thumbnail " + position);
                }
            });

            // ✅ TAMBIÉN AGREGAR CLICK AL CARDVIEW POR SI ACASO
            if (cardContainer != null) {
                cardContainer.setOnClickListener(v -> {
                    if (clickListener != null) {
                        Log.d(TAG, "👆 CardView thumbnail " + (position + 1) + " clickeado");
                        clickListener.onThumbnailClick(position);
                    }
                });
            }
        }

        private void updateSelectionState(boolean isSelected) {
            if (cardContainer != null) {
                // ✅ ENFOQUE COMPATIBLE: Usar elevation y alpha para indicar selección
                if (isSelected) {
                    cardContainer.setCardElevation(8f);
                    cardContainer.setAlpha(1.0f);

                    // ✅ MOSTRAR BORDE VISIBLE si existe
                    if (borderIndicator != null) {
                        borderIndicator.setVisibility(View.VISIBLE);
                    } else {
                        // ✅ FALLBACK: Cambiar background para simular borde
                        try {
                            cardContainer.setCardBackgroundColor(
                                    ContextCompat.getColor(itemView.getContext(), R.color.accent_light));
                        } catch (Exception e) {
                            // Fallback si no existe el color
                            Log.d(TAG, "Color accent_light no encontrado, usando estado por defecto");
                        }
                    }
                } else {
                    cardContainer.setCardElevation(2f);
                    cardContainer.setAlpha(0.7f);

                    // ✅ OCULTAR BORDE VISIBLE si existe
                    if (borderIndicator != null) {
                        borderIndicator.setVisibility(View.GONE);
                    } else {
                        // ✅ FALLBACK: Restaurar background normal
                        cardContainer.setCardBackgroundColor(
                                ContextCompat.getColor(itemView.getContext(), android.R.color.white));
                    }
                }
            }
        }

        private void loadThumbnailFromUrl(String imageUrl, int position) {
            Log.d(TAG, "📸 Cargando thumbnail desde URL para posición: " + position);

            Glide.with(imageView.getContext())
                    .load(imageUrl)

                    // ✅ PLACEHOLDERS PARA THUMBNAILS
                    .placeholder(R.drawable.hotel_placeholder)
                    .error(R.drawable.hotel_placeholder)
                    .fallback(R.drawable.hotel_placeholder)

                    // ✅ CONFIGURACIÓN OPTIMIZADA PARA THUMBNAILS
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)

                    // ✅ TAMAÑO PEQUEÑO PARA THUMBNAILS (mejor rendimiento)
                    .override(150, 100) // Thumbnails pequeños
                    .centerCrop()

                    // ✅ FORMATO OPTIMIZADO PARA THUMBNAILS
                    .format(DecodeFormat.PREFER_RGB_565) // Menor uso de memoria para thumbnails

                    // ✅ TIMEOUT MÁS CORTO PARA THUMBNAILS
                    .timeout(15000) // 15 segundos para thumbnails

                    // ✅ LISTENER PARA DEBUG
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "❌ Error cargando thumbnail " + (position + 1) + ": " +
                                    (e != null ? e.getMessage() : "Error desconocido"));

                            // Cargar placeholder
                            imageView.setImageResource(R.drawable.hotel_placeholder);
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            Log.d(TAG, "✅ Thumbnail " + (position + 1) + " cargado desde: " + dataSource);
                            return false;
                        }
                    })
                    .into(imageView);
        }

        private void loadThumbnailFromResources(String imageName, int position) {
            Log.d(TAG, "🖼️ Cargando thumbnail desde recursos: " + imageName);

            try {
                int resID = imageView.getContext().getResources().getIdentifier(
                        imageName, "drawable", imageView.getContext().getPackageName());

                if (resID != 0) {
                    // ✅ USAR GLIDE TAMBIÉN PARA RECURSOS (consistencia)
                    Glide.with(imageView.getContext())
                            .load(resID)
                            .placeholder(R.drawable.hotel_placeholder)
                            .error(R.drawable.hotel_placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .override(150, 100) // Tamaño thumbnail
                            .centerCrop()
                            .into(imageView);

                    Log.d(TAG, "✅ Thumbnail recurso cargado: " + imageName);
                } else {
                    Log.w(TAG, "⚠️ Recurso thumbnail no encontrado: " + imageName);
                    imageView.setImageResource(R.drawable.hotel_placeholder);
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error cargando thumbnail recurso " + imageName + ": " + e.getMessage());
                imageView.setImageResource(R.drawable.hotel_placeholder);
            }
        }
    }
}