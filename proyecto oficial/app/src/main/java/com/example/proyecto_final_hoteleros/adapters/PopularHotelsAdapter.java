package com.example.proyecto_final_hoteleros.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.model.Hotel;

import java.util.List;

public class PopularHotelsAdapter extends RecyclerView.Adapter<PopularHotelsAdapter.PopularHotelViewHolder> {

    private List<Hotel> hotelList;

    public PopularHotelsAdapter(List<Hotel> hotelList) {
        this.hotelList = hotelList;
    }

    @NonNull
    @Override
    public PopularHotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el layout del ítem (item_popular_hotel_card.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_popular_hotel_card, parent, false);
        return new PopularHotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularHotelViewHolder holder, int position) {
        Hotel hotel = hotelList.get(position);

        // Asigna los textos
        holder.tvHotelName.setText(hotel.getName());
        holder.tvLocation.setText(hotel.getLocation());
        holder.tvPrice.setText(hotel.getPrice());
        holder.tvRating.setText(hotel.getRating());

        // Asigna dinámica la imagen
        String imageName = hotel.getImageUrl();  // Ej: "drawable/inkaterra"
        if (imageName.contains("/")) {
            imageName = imageName.substring(imageName.lastIndexOf("/") + 1);
        }
        int resID = holder.itemView.getContext().getResources().getIdentifier(
                imageName,
                "drawable",
                holder.itemView.getContext().getPackageName()
        );
        // Si resID es válido, se asigna la imagen; en caso contrario, se asigna una imagen por defecto.
        if (resID != 0) {
            holder.ivHotelImage.setImageResource(resID);
        } else {
            holder.ivHotelImage.setImageResource(R.drawable.belmond);
        }
    }

    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    public static class PopularHotelViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHotelImage;
        TextView tvHotelName, tvLocation, tvPrice, tvPriceDescription, tvRating;

        public PopularHotelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHotelImage = itemView.findViewById(R.id.ivHotelImage);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvPriceDescription = itemView.findViewById(R.id.tvPriceDescription);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }
}