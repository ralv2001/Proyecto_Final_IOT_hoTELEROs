package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_final_hoteleros.R;
import java.util.List;

public class ServiceGalleryAdapter extends RecyclerView.Adapter<ServiceGalleryAdapter.GalleryViewHolder> {

    private List<String> imageNames;
    private Context context;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(String imageName, int position);
    }

    public ServiceGalleryAdapter(List<String> imageNames, OnImageClickListener listener) {
        this.imageNames = imageNames;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.superadmin_item_service_gallery_image, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        String imageName = imageNames.get(position);
        holder.bind(imageName, position);
    }

    @Override
    public int getItemCount() {
        return imageNames != null ? imageNames.size() : 0;
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivGalleryImage;
        private TextView tvImageNumber;

        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGalleryImage = itemView.findViewById(R.id.iv_gallery_image);
            tvImageNumber = itemView.findViewById(R.id.tv_image_number);
        }

        public void bind(String imageName, int position) {
            // Mostrar número de imagen
            tvImageNumber.setText((position + 1) + "/" + imageNames.size());

            // Cargar imagen desde drawable
            try {
                int resourceId = context.getResources().getIdentifier(
                        imageName, "drawable", context.getPackageName());

                if (resourceId > 0) {
                    ivGalleryImage.setImageResource(resourceId);
                } else {
                    ivGalleryImage.setImageResource(R.drawable.ic_hotel_service_default);
                }
            } catch (Exception e) {
                ivGalleryImage.setImageResource(R.drawable.ic_hotel_service_default);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageClick(imageName, position);
                }
            });
        }
    }
}