package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;

import java.util.List;

public class ServiceChipAdapter extends RecyclerView.Adapter<ServiceChipAdapter.ChipViewHolder> {

    private List<String> services;

    public ServiceChipAdapter(List<String> services) {
        this.services = services;
    }

    @NonNull
    @Override
    public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_service_chip, parent, false);
        return new ChipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder holder, int position) {
        String service = services.get(position);
        holder.tvServiceName.setText(service);
    }

    @Override
    public int getItemCount() {
        return services != null ? services.size() : 0;
    }

    static class ChipViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName;

        ChipViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
        }
    }
}