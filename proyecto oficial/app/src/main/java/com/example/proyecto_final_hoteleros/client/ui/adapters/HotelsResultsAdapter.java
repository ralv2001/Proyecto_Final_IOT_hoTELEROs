package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.Hotel;

import java.util.List;

public class HotelsResultsAdapter extends RecyclerView.Adapter<HotelsResultsAdapter.HotelResultViewHolder> {

    private Context context;
    private List<Hotel> hotelsList;
    private OnHotelClickListener listener;

    public interface OnHotelClickListener {
        void onHotelClick(Hotel hotel, int position);
    }

    public HotelsResultsAdapter(Context context, List<Hotel> hotelsList) {
        this.context = context;
        this.hotelsList = hotelsList;
    }

    public void setOnHotelClickListener(OnHotelClickListener listener) {
        this.listener = listener;
    }

    public void updateHotels(List<Hotel> newHotels) {
        this.hotelsList = newHotels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HotelResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // USAR EL MISMO LAYOUT QUE EN EL HOME (item_popular_hotel_card)
        View view = LayoutInflater.from(context).inflate(R.layout.item_popular_hotel_card, parent, false);
        return new HotelResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelResultViewHolder holder, int position) {
        Hotel hotel = hotelsList.get(position);

        // Configurar información básica (igual que PopularHotelsAdapter)
        holder.tvHotelName.setText(hotel.getName());
        holder.tvLocation.setText(hotel.getLocation());
        holder.tvPrice.setText(hotel.getPrice());
        holder.tvRating.setText(hotel.getRating());

        // Configurar imagen del hotel (igual que PopularHotelsAdapter)
        String imageName = hotel.getImageUrl();
        if (imageName.contains("/")) {
            imageName = imageName.substring(imageName.lastIndexOf("/") + 1);
        }
        int resID = context.getResources().getIdentifier(
                imageName,
                "drawable",
                context.getPackageName()
        );
        if (resID != 0) {
            holder.ivHotelImage.setImageResource(resID);
        } else {
            holder.ivHotelImage.setImageResource(R.drawable.belmond);
        }

        // Configurar listener de clic
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHotelClick(hotel, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return hotelsList.size();
    }

    static class HotelResultViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHotelImage;
        TextView tvHotelName;
        TextView tvLocation;
        TextView tvPrice;
        TextView tvPriceDescription;
        TextView tvRating;
        LinearLayout layoutRating;

        HotelResultViewHolder(@NonNull View itemView) {
            super(itemView);
            // USAR LOS MISMOS IDs QUE item_popular_hotel_card
            ivHotelImage = itemView.findViewById(R.id.ivHotelImage);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvPriceDescription = itemView.findViewById(R.id.tvPriceDescription);
            tvRating = itemView.findViewById(R.id.tvRating);
            layoutRating = itemView.findViewById(R.id.layoutRating);
        }
    }
}