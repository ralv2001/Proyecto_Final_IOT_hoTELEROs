package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.content.Context;
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

public class BasicServicePhotosAdapter extends RecyclerView.Adapter<BasicServicePhotosAdapter.PhotoViewHolder> {

    private static final String TAG = "BasicServicePhotosAdapter";

    public interface OnPhotoClickListener {
        void onPhotoClick(String photoUrl, int position, List<String> allPhotos);
    }

    private List<String> photoUrls;
    private OnPhotoClickListener listener;
    private Context context;

    public BasicServicePhotosAdapter(Context context, List<String> photoUrls, OnPhotoClickListener listener) {
        this.context = context;
        this.photoUrls = photoUrls;
        this.listener = listener;
        Log.d(TAG, "🔧 Adapter creado con " + (photoUrls != null ? photoUrls.size() : 0) + " fotos");
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_hotel_item_basic_service_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        if (photoUrls != null && position < photoUrls.size()) {
            String photoUrl = photoUrls.get(position);
            holder.bind(photoUrl, position + 1);
        }
    }

    @Override
    public int getItemCount() {
        return photoUrls != null ? photoUrls.size() : 0;
    }

    public void updatePhotos(List<String> newPhotos) {
        this.photoUrls = newPhotos;
        notifyDataSetChanged();
        Log.d(TAG, "📷 Fotos actualizadas: " + (newPhotos != null ? newPhotos.size() : 0));
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivServicePhoto;
        private TextView tvPhotoIndex;
        private ProgressBar progressBarPhoto;
        private View photoOverlay;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServicePhoto = itemView.findViewById(R.id.ivServicePhoto);
            tvPhotoIndex = itemView.findViewById(R.id.tvPhotoIndex);
            progressBarPhoto = itemView.findViewById(R.id.progressBarPhoto);
            photoOverlay = itemView.findViewById(R.id.photoOverlay);
        }

        public void bind(String photoUrl, int photoNumber) {
            // Configurar el índice de la foto
            tvPhotoIndex.setText(String.valueOf(photoNumber));

            // Mostrar indicador de carga inicialmente
            progressBarPhoto.setVisibility(View.VISIBLE);

            // Configurar opciones de Glide
            RequestOptions options = new RequestOptions()
                    .transform(new CenterCrop(), new RoundedCorners(12))
                    .placeholder(R.drawable.ic_photo)
                    .error(R.drawable.ic_photo)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            // Cargar imagen con Glide
            Glide.with(context)
                    .load(photoUrl)
                    .apply(options)
                    .into(ivServicePhoto);

            // Ocultar indicador de carga después de un breve delay
            ivServicePhoto.post(() -> progressBarPhoto.setVisibility(View.GONE));

            // Click listener para ver la foto en pantalla completa
            photoOverlay.setOnClickListener(v -> {
                if (listener != null) {
                    Log.d(TAG, "📸 Click en foto: " + photoUrl + " posición: " + getAdapterPosition());
                    listener.onPhotoClick(photoUrl, getAdapterPosition(), photoUrls);
                }
            });

            Log.d(TAG, "📷 Foto cargada: " + photoNumber + " - URL: " + photoUrl);
        }
    }
}