package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.proyecto_final_hoteleros.R;

import java.util.List;

public class HotelPhotosAdapter extends RecyclerView.Adapter<HotelPhotosAdapter.PhotoViewHolder> {

    private static final String TAG = "HotelPhotosAdapter";

    public interface OnPhotoRemovedListener {
        void onPhotoRemoved(int position);
    }

    // ✅ CAMBIADO: Ahora maneja Object (puede ser Uri o String)
    private List<Object> photos;
    private OnPhotoRemovedListener listener;
    private Context context;

    // ✅ CONSTRUCTOR ACTUALIZADO
    public HotelPhotosAdapter(Context context, List<Object> photos, OnPhotoRemovedListener listener) {
        this.context = context;
        this.photos = photos;
        this.listener = listener;
        Log.d(TAG, "🔧 Adapter creado con " + (photos != null ? photos.size() : 0) + " fotos");
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext(); // Asegurar contexto
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        if (photos != null && position < photos.size()) {
            Object photo = photos.get(position);
            holder.bind(photo, position);
        }
    }

    @Override
    public int getItemCount() {
        return photos != null ? photos.size() : 0;
    }

    // ✅ MÉTODO PARA ACTUALIZAR FOTOS
    public void updatePhotos(List<Object> newPhotos) {
        this.photos = newPhotos;
        notifyDataSetChanged();
        Log.d(TAG, "📷 Fotos actualizadas: " + (newPhotos != null ? newPhotos.size() : 0));
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private ImageView ivRemove;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            ivRemove = itemView.findViewById(R.id.ivRemove);
        }

        // ✅ MÉTODO ACTUALIZADO PARA MANEJAR URI Y STRING
        public void bind(Object photo, int position) {
            // Configurar opciones de Glide
            RequestOptions requestOptions = new RequestOptions()
                    .transform(new CenterCrop(), new RoundedCorners(16))
                    .placeholder(R.drawable.ic_photo)
                    .error(R.drawable.ic_photo)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            if (photo instanceof Uri) {
                // ✅ FOTO LOCAL (Uri)
                Uri photoUri = (Uri) photo;
                Log.d(TAG, "📷 Cargando foto local: " + photoUri.toString());

                Glide.with(itemView.getContext())
                        .load(photoUri)
                        .apply(requestOptions)
                        .into(ivPhoto);

            } else if (photo instanceof String) {
                // ✅ FOTO DE FIREBASE (URL)
                String photoUrl = (String) photo;
                Log.d(TAG, "📷 Cargando foto de Firebase: " + photoUrl);

                Glide.with(itemView.getContext())
                        .load(photoUrl)
                        .apply(requestOptions)
                        .into(ivPhoto);
            } else {
                // ✅ FALLBACK - Mostrar placeholder
                Log.w(TAG, "⚠️ Tipo de foto desconocido en posición " + position + ": " +
                        (photo != null ? photo.getClass().getSimpleName() : "null"));

                Glide.with(itemView.getContext())
                        .load(R.drawable.ic_photo)
                        .apply(requestOptions)
                        .into(ivPhoto);
            }

            // ✅ BOTÓN DE ELIMINAR
            ivRemove.setOnClickListener(v -> {
                if (listener != null) {
                    Log.d(TAG, "🗑️ Eliminando foto en posición " + position);
                    listener.onPhotoRemoved(position);
                }
            });

            // ✅ OPCIONAL: Click en la foto para ver en grande
            ivPhoto.setOnClickListener(v -> {
                // TODO: Implementar visor de foto en grande si es necesario
                Log.d(TAG, "👁️ Click en foto posición " + position);
            });
        }
    }
}