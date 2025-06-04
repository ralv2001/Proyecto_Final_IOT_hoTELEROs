package com.example.proyecto_final_hoteleros.client.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;

import java.util.List;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder> {

    private final List<Integer> images;
    private int selectedPosition = 0;
    private final OnThumbnailClickListener listener;

    public interface OnThumbnailClickListener {
        void onThumbnailClick(int position);
    }

    public ThumbnailAdapter(List<Integer> images, OnThumbnailClickListener listener) {
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_thumbnail, parent, false);
        return new ThumbnailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailViewHolder holder, int position) {
        holder.thumbnailImageView.setImageResource(images.get(position));

        // Aplicar el estilo seleccionado o normal
        if (position == selectedPosition) {
            holder.thumbnailOverlay.setBackgroundResource(R.drawable.bg_thumbnail_selected);
        } else {
            holder.thumbnailOverlay.setBackgroundResource(R.drawable.bg_thumbnail_normal);
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Actualizar los items modificados
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);

            // Notificar al listener
            if (listener != null) {
                listener.onThumbnailClick(selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void updateSelectedPosition(int position) {
        if (position != selectedPosition && position >= 0 && position < images.size()) {
            int previousSelected = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
        }
    }

    static class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImageView;
        View thumbnailOverlay;

        ThumbnailViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.iv_thumbnail);
            thumbnailOverlay = itemView.findViewById(R.id.view_thumbnail_overlay);
        }
    }
}