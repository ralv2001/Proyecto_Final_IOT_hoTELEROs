package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.proyecto_final_hoteleros.R;

import java.util.List;

public class RoomPhotosAdapter extends RecyclerView.Adapter<RoomPhotosAdapter.PhotoViewHolder> {

    private static final String TAG = "RoomPhotosAdapter";

    public interface OnPhotoActionListener {
        void onRemovePhoto(int position);
        void onPhotoClick(String photoUrl, int position, List<String> allPhotos);
    }

    private List<Object> photos; // Object porque puede ser Uri o String
    private OnPhotoActionListener listener;
    private Context context;
    private boolean isEditMode; // Para mostrar botón de eliminar

    public RoomPhotosAdapter(Context context, List<Object> photos, OnPhotoActionListener listener, boolean isEditMode) {
        this.context = context;
        this.photos = photos;
        this.listener = listener;
        this.isEditMode = isEditMode;
        Log.d(TAG, "🔧 Adapter creado con " + (photos != null ? photos.size() : 0) + " fotos");
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_hotel_item_room_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        if (photos != null && position < photos.size()) {
            Object photo = photos.get(position);
            holder.bind(photo, position + 1);
        }
    }

    @Override
    public int getItemCount() {
        return photos != null ? photos.size() : 0;
    }

    public void updatePhotos(List<Object> newPhotos) {
        this.photos = newPhotos;
        notifyDataSetChanged();
        Log.d(TAG, "📷 Fotos actualizadas: " + (newPhotos != null ? newPhotos.size() : 0));
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivRoomPhoto;
        private ImageView ivRemove;
        private TextView tvPhotoIndex;
        private ProgressBar progressBarPhoto;
        private View photoOverlay;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRoomPhoto = itemView.findViewById(R.id.ivRoomPhoto);
            ivRemove = itemView.findViewById(R.id.ivRemove);
            tvPhotoIndex = itemView.findViewById(R.id.tvPhotoIndex);
            progressBarPhoto = itemView.findViewById(R.id.progressBarPhoto);
            photoOverlay = itemView.findViewById(R.id.photoOverlay);
        }

        public void bind(Object photo, int photoNumber) {
            // Configurar el índice de la foto
            tvPhotoIndex.setText(String.valueOf(photoNumber));

            // Configurar visibilidad del botón eliminar
            ivRemove.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

            // Mostrar indicador de carga inicialmente
            progressBarPhoto.setVisibility(View.VISIBLE);

            // Configurar opciones de Glide
            RequestOptions options = new RequestOptions()
                    .transform(new CenterCrop(), new RoundedCorners(16))
                    .placeholder(R.drawable.ic_photo)
                    .error(R.drawable.ic_photo)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            // Cargar imagen dependiendo del tipo
            if (photo instanceof Uri) {
                // Es una imagen local (URI)
                Uri photoUri = (Uri) photo;
                Glide.with(context)
                        .load(photoUri)
                        .apply(options)
                        .into(ivRoomPhoto);
                Log.d(TAG, "📷 Cargando foto local: " + photoNumber + " - URI: " + photoUri);
            } else if (photo instanceof String) {
                // Es una URL de imagen remota
                String photoUrl = (String) photo;
                Glide.with(context)
                        .load(photoUrl)
                        .apply(options)
                        .into(ivRoomPhoto);
                Log.d(TAG, "📷 Cargando foto remota: " + photoNumber + " - URL: " + photoUrl);
            }

            // Ocultar indicador de carga después de un breve delay
            ivRoomPhoto.post(() -> progressBarPhoto.setVisibility(View.GONE));

            // Click listener para remover foto (solo en modo edición)
            ivRemove.setOnClickListener(v -> {
                if (listener != null && isEditMode) {
                    Log.d(TAG, "🗑️ Removiendo foto en posición: " + getAdapterPosition());
                    listener.onRemovePhoto(getAdapterPosition());
                }
            });

            // Click listener para ver la foto en pantalla completa
            photoOverlay.setOnClickListener(v -> {
                if (listener != null) {
                    // Convertir todas las fotos a String para el visor
                    java.util.List<String> allPhotosAsString = new java.util.ArrayList<>();
                    for (Object p : photos) {
                        if (p instanceof String) {
                            allPhotosAsString.add((String) p);
                        } else if (p instanceof Uri) {
                            allPhotosAsString.add(p.toString());
                        }
                    }

                    String currentPhotoUrl = photo instanceof String ?
                            (String) photo : photo.toString();

                    Log.d(TAG, "📸 Click en foto: " + currentPhotoUrl + " posición: " + getAdapterPosition());
                    listener.onPhotoClick(currentPhotoUrl, getAdapterPosition(), allPhotosAsString);
                }
            });
        }
    }
}