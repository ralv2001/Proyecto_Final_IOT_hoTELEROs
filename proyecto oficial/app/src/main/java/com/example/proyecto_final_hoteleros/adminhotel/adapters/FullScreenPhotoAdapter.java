package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.proyecto_final_hoteleros.R;

import java.util.List;

public class FullScreenPhotoAdapter extends RecyclerView.Adapter<FullScreenPhotoAdapter.PhotoViewHolder> {

    private Context context;
    private List<String> photoUrls;

    public FullScreenPhotoAdapter(Context context, List<String> photoUrls) {
        this.context = context;
        this.photoUrls = photoUrls;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_hotel_item_fullscreen_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        holder.bind(photoUrls.get(position));
    }

    @Override
    public int getItemCount() {
        return photoUrls.size();
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private ProgressBar progressBar;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            progressBar = itemView.findViewById(R.id.progressBar);
        }

        public void bind(String photoUrl) {
            progressBar.setVisibility(View.VISIBLE);

            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.ic_photo)
                    .error(R.drawable.ic_photo)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            Glide.with(context)
                    .load(photoUrl)
                    .apply(options)
                    .into(ivPhoto);

            progressBar.setVisibility(View.GONE);
        }
    }
}