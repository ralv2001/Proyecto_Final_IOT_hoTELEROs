package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelProfile;

import java.util.List;
import java.util.Random;

public class HotelCardAdapter extends RecyclerView.Adapter<HotelCardAdapter.HotelCardViewHolder> {

    private Context context;
    private List<HotelProfile> hotels;
    private OnHotelClickListener listener;

    public interface OnHotelClickListener {
        void onHotelClick(HotelProfile hotel);
    }

    public HotelCardAdapter(Context context, List<HotelProfile> hotels) {
        this.context = context;
        this.hotels = hotels;
    }

    public void setOnHotelClickListener(OnHotelClickListener listener) {
        this.listener = listener;
    }

    public void updateHotels(List<HotelProfile> newHotels) {
        this.hotels = newHotels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HotelCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.client_item_hotel_card, parent, false);
        return new HotelCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelCardViewHolder holder, int position) {
        HotelProfile hotel = hotels.get(position);
        holder.bind(hotel, position);
    }

    @Override
    public int getItemCount() {
        return hotels != null ? hotels.size() : 0;
    }

    public class HotelCardViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivHotelImage;
        private TextView tvHotelName;
        private TextView tvLocation;
        private TextView tvPrice;
        private TextView tvPriceDescription;
        private TextView tvRating;

        public HotelCardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHotelImage = itemView.findViewById(R.id.ivHotelImage);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvPriceDescription = itemView.findViewById(R.id.tvPriceDescription);
            tvRating = itemView.findViewById(R.id.tvRating);
        }

        public void bind(HotelProfile hotel, int position) {
            // Configurar información básica del hotel
            tvHotelName.setText(hotel.getName());

            // Usar locationName si está disponible, sino address
            String displayLocation = hotel.getLocationName();
            if (displayLocation == null || displayLocation.trim().isEmpty()) {
                displayLocation = hotel.getAddress();
            }
            tvLocation.setText(displayLocation);

            // Configurar precio (simulado por ahora)
            String price = generatePrice(hotel);
            tvPrice.setText(price);

            if (tvPriceDescription != null) {
                tvPriceDescription.setText("por noche");
            }

            // Configurar rating (simulado por ahora)
            String rating = generateRating(hotel);
            tvRating.setText(rating);

            // Configurar imagen del hotel
            loadHotelImage(hotel);

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHotelClick(hotel);
                }
            });
        }

        private void loadHotelImage(HotelProfile hotel) {
            if (hotel.getPhotoUrls() != null && !hotel.getPhotoUrls().isEmpty()) {
                // Cargar primera imagen desde Firebase Storage/URL
                String imageUrl = hotel.getPhotoUrls().get(0);

                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_hotel_icono)
                        .error(getDefaultHotelImage(hotel))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(new CenterCrop(), new RoundedCorners(16))
                        .into(ivHotelImage);
            } else {
                // Usar imagen por defecto basada en la ubicación
                ivHotelImage.setImageResource(getDefaultHotelImage(hotel));
            }
        }

        private int getDefaultHotelImage(HotelProfile hotel) {
            // Asignar imagen por defecto basada en la ciudad/departamento
            String location = hotel.getDepartamento();
            if (location == null) location = hotel.getAddress();
            if (location == null) location = "";

            location = location.toLowerCase();

            if (location.contains("lima")) {
                return R.drawable.belmond; // Imagen de Lima
            } else if (location.contains("cusco") || location.contains("cuzco")) {
                return R.drawable.cuzco; // Imagen de Cusco
            } else if (location.contains("arequipa")) {
                return R.drawable.arequipa; // Imagen de Arequipa
            } else if (location.contains("amazonas") || location.contains("iquitos")) {
                return R.drawable.gocta; // Imagen de Amazonas
            } else if (location.contains("piura") || location.contains("mancora")) {
                return R.drawable.inkaterra; // Imagen de costa norte
            } else {
                return R.drawable.ic_hotel_icono; // Imagen por defecto
            }
        }

        private String generatePrice(HotelProfile hotel) {
            // Generar precio basado en características del hotel
            // Puedes reemplazar esto con un campo real de precio cuando lo implementes

            Random random = new Random(hotel.getName().hashCode()); // Seed consistente
            int basePrice = 180 + random.nextInt(200); // Entre S/180 y S/380

            // Ajustar precio por ubicación
            String location = hotel.getDepartamento();
            if (location != null) {
                location = location.toLowerCase();
                if (location.contains("lima")) {
                    basePrice += 50; // Lima más caro
                } else if (location.contains("cusco")) {
                    basePrice += 30; // Cusco turístico
                }
            }

            return "S/" + basePrice;
        }

        private String generateRating(HotelProfile hotel) {
            // Generar rating basado en características del hotel
            // Puedes reemplazar esto con un campo real de rating cuando lo implementes

            Random random = new Random(hotel.getName().hashCode()); // Seed consistente
            double rating = 4.0 + (random.nextDouble() * 1.0); // Entre 4.0 y 5.0

            // Redondear a 1 decimal
            rating = Math.round(rating * 10.0) / 10.0;

            return String.valueOf(rating);
        }
    }
}