package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;

import java.util.List;

public class HotelPhotosAdapter extends RecyclerView.Adapter<HotelPhotosAdapter.PhotoViewHolder> {

    public interface OnPhotoRemovedListener {
        void onPhotoRemoved(int position);
    }

    private List<Uri> photos;
    private OnPhotoRemovedListener listener;

    public HotelPhotosAdapter(List<Uri> photos, OnPhotoRemovedListener listener) {
        this.photos = photos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Uri photoUri = photos.get(position);
        holder.bind(photoUri, position);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private ImageView ivRemove;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            ivRemove = itemView.findViewById(R.id.ivRemove);
        }

        public void bind(Uri photoUri, int position) {
            ivPhoto.setImageURI(photoUri);

            ivRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPhotoRemoved(position);
                }
            });
        }
    }
}