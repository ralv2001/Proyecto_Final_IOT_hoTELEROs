package com.example.proyecto_final_hoteleros.taxista.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.taxista.model.AvailableHotel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class AvailableHotelsAdapter extends RecyclerView.Adapter<AvailableHotelsAdapter.HotelViewHolder> {

    private Context context;
    private List<AvailableHotel> hotelsList;
    private HotelListener listener;

    public interface HotelListener {
        void onHotelClick(AvailableHotel hotel);
        void onCallHotel(AvailableHotel hotel);
        void onGetDirections(AvailableHotel hotel);
    }

    public AvailableHotelsAdapter(Context context, List<AvailableHotel> hotelsList, HotelListener listener) {
        this.context = context;
        this.hotelsList = hotelsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.taxi_item_available_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        AvailableHotel hotel = hotelsList.get(position);

        // Información básica del hotel
        holder.tvHotelName.setText(hotel.getName());
        holder.tvHotelAddress.setText(hotel.getAddress());
        holder.tvRating.setText(String.valueOf(hotel.getRating()));
        holder.tvDistrict.setText(hotel.getDistrict());

        // Distancia
        holder.tvDistance.setText(hotel.getFormattedDistance());

        // Información de asociación
        holder.tvPartnershipInfo.setText(hotel.getPartnershipInfo());

        // Descripción
        holder.tvDescription.setText(hotel.getDescription());

        // Total de solicitudes
        holder.tvTotalRequests.setText(hotel.getTotalRequests() + " servicios");

        // Cargar imagen del hotel
        Glide.with(context)
                .load(hotel.getImageUrl())
                .placeholder(R.drawable.belmond)
                .error(R.drawable.belmond)
                .centerCrop()
                .into(holder.ivHotelImage);

        // Listeners
        holder.cardHotel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHotelClick(hotel);
            }
        });

        holder.btnCall.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCallHotel(hotel);
            }
        });

        holder.btnDirections.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGetDirections(hotel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return hotelsList.size();
    }

    static class HotelViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardHotel;
        ImageView ivHotelImage;
        TextView tvHotelName;
        TextView tvHotelAddress;
        TextView tvRating;
        TextView tvDistrict;
        TextView tvDistance;
        TextView tvPartnershipInfo;
        TextView tvDescription;
        TextView tvTotalRequests;
        MaterialButton btnCall;
        MaterialButton btnDirections;
        LinearLayout layoutRating;

        HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            cardHotel = itemView.findViewById(R.id.card_hotel);
            ivHotelImage = itemView.findViewById(R.id.iv_hotel_image);
            tvHotelName = itemView.findViewById(R.id.tv_hotel_name);
            tvHotelAddress = itemView.findViewById(R.id.tv_hotel_address);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvDistrict = itemView.findViewById(R.id.tv_district);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvPartnershipInfo = itemView.findViewById(R.id.tv_partnership_info);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvTotalRequests = itemView.findViewById(R.id.tv_total_requests);
            btnCall = itemView.findViewById(R.id.btn_call);
            btnDirections = itemView.findViewById(R.id.btn_directions);
            layoutRating = itemView.findViewById(R.id.layout_rating);
        }
    }
}