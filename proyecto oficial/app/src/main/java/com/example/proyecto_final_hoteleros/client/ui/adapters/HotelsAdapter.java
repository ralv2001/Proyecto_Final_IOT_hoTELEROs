package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.Hotel;

import java.util.List;

public class HotelsAdapter extends RecyclerView.Adapter<HotelsAdapter.HotelViewHolder> {

    private List<Hotel> hotelList;
    private OnHotelClickListener listener;
    public HotelsAdapter(List<Hotel> hotelList) {
        this.hotelList = hotelList;
    }
    // Agregar setter para el listener
    public void setOnHotelClickListener(OnHotelClickListener listener) {
        this.listener = listener;
    }
    // Interfaz para manejar clics
    public interface OnHotelClickListener {
        void onHotelClick(Hotel hotel, int position);
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el layout del ítem (item_hotel_card.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.client_item_hotel_card, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotelList.get(position);

        // Asigna los textos
        holder.tvHotelName.setText(hotel.getName());
        holder.tvLocation.setText(hotel.getLocation());
        holder.tvPrice.setText(hotel.getPrice());
        holder.tvRating.setText(hotel.getRating());

        // Asigna dinámica la imagen:
        // Suponiendo que en el modelo guardas una cadena como "drawable/belmond" o "drawable/inkaterra"
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

        // Agregar listener de clic a toda la vista
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHotelClick(hotel, position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    public static class HotelViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHotelImage;
        TextView tvHotelName, tvLocation, tvPrice, tvPriceDescription, tvRating;

        public HotelViewHolder(@NonNull View itemView) {
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
