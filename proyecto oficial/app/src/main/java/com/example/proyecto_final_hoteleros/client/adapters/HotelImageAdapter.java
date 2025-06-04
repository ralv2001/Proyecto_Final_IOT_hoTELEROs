package com.example.proyecto_final_hoteleros.client.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;

import java.util.List;

public class HotelImageAdapter extends RecyclerView.Adapter<HotelImageAdapter.ImageViewHolder> {

    private final List<Integer> images;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    public HotelImageAdapter(List<Integer> images) {
        this.images = images;
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hotel_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.imageView.setImageResource(images.get(position));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_hotel_image);
        }
    }
}