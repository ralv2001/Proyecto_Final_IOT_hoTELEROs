package com.example.proyecto_final_hoteleros.client.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.model.LocationItem;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<LocationItem> locations;
    private OnLocationClickListener listener;

    public interface OnLocationClickListener {
        void onLocationClick(LocationItem location);
    }

    public LocationAdapter(List<LocationItem> locations, OnLocationClickListener listener) {
        this.locations = locations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationItem location = locations.get(position);
        holder.tvLocationName.setText(location.getName());
        holder.tvLocationType.setText(location.getType());

        // Configurar color del ícono según el tipo de ubicación
        int iconColor;
        if (location.getType().contains("Departamento")) {
            iconColor = holder.itemView.getContext().getResources().getColor(R.color.departamento_color);
        } else if (location.getType().contains("Provincia")) {
            iconColor = holder.itemView.getContext().getResources().getColor(R.color.provincia_color);
        } else if (location.getType().contains("Distrito")) {
            iconColor = holder.itemView.getContext().getResources().getColor(R.color.distrito_color);
        } else if (location.getType().contains("Popular") || location.getType().contains("turística")) {
            iconColor = holder.itemView.getContext().getResources().getColor(R.color.popular_color);
        } else {
            iconColor = holder.itemView.getContext().getResources().getColor(R.color.location_default_color);
        }

        holder.ivLocationIcon.setColorFilter(iconColor);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLocationClick(location);
            }
        });
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public void updateLocations(List<LocationItem> newLocations) {
        this.locations = newLocations;
        notifyDataSetChanged();
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView tvLocationName;
        TextView tvLocationType;
        ImageView ivLocationIcon;
        LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLocationName = itemView.findViewById(R.id.tvLocationName);
            tvLocationType = itemView.findViewById(R.id.tvLocationType);
            ivLocationIcon  = itemView.findViewById(R.id.ivLocationIcon);
        }
    }
}
