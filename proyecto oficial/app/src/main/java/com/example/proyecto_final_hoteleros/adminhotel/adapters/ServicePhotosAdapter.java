package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.proyecto_final_hoteleros.R;
import java.util.List;

public class ServicePhotosAdapter extends RecyclerView.Adapter<ServicePhotosAdapter.PhotoViewHolder> {

    private static final String TAG = "ServicePhotosAdapter";

    public interface OnPhotoActionListener {
        void onRemovePhoto(int position);
    }

    private List<Uri> photos;
    private OnPhotoActionListener listener;

    public ServicePhotosAdapter(List<Uri> photos, OnPhotoActionListener listener) {
        this.photos = photos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_service_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        holder.bind(photos.get(position), position);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto, ivRemove;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            ivRemove = itemView.findViewById(R.id.ivRemove);

            // ✅ SOLO ESTE LOG PARA VERIFICAR EL PROBLEMA
            if (ivPhoto == null) {
                Log.e(TAG, "❌ ERROR: ivPhoto es NULL! Layout: admin_hotel_item_service_photo");
            }
            if (ivRemove == null) {
                Log.e(TAG, "❌ ERROR: ivRemove es NULL! Layout: admin_hotel_item_service_photo");
            }
        }

        public void bind(Uri photoUri, int position) {
            // ✅ SOLO AGREGAMOS ESTA VALIDACIÓN MÍNIMA
            if (ivPhoto == null) {
                Log.e(TAG, "❌ ivPhoto es null en bind() - No se puede cargar imagen");
                return;
            }

            // El resto del código EXACTAMENTE IGUAL que antes
            Glide.with(itemView.getContext())
                    .load(photoUri)
                    .transform(new CenterCrop(), new RoundedCorners(16))
                    .placeholder(R.drawable.ic_photo)
                    .error(R.drawable.ic_photo)
                    .into(ivPhoto);

            // Click para remover
            if (ivRemove != null) {
                ivRemove.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRemovePhoto(position);
                    }
                });
            }
        }
    }
}