package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.BasicService;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;

import java.util.List;

public class BasicServicesAdapter extends RecyclerView.Adapter<BasicServicesAdapter.ServiceViewHolder> {

    public interface OnServiceRemovedListener {
        void onServiceRemoved(int position);
    }

    private List<BasicService> services;
    private OnServiceRemovedListener listener;

    public BasicServicesAdapter(List<BasicService> services, OnServiceRemovedListener listener) {
        this.services = services;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_basic_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        BasicService service = services.get(position);
        holder.bind(service, position);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivServiceIcon;
        private TextView tvServiceName;
        private TextView tvServiceDescription;
        private ImageView ivRemove;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServiceIcon = itemView.findViewById(R.id.ivServiceIcon);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceDescription = itemView.findViewById(R.id.tvServiceDescription);
            ivRemove = itemView.findViewById(R.id.ivRemove);
        }

        public void bind(BasicService service, int position) {
            ivServiceIcon.setImageResource(IconHelper.getIconResource(service.getIconKey()));
            tvServiceName.setText(service.getName());
            tvServiceDescription.setText(service.getDescription());

            ivRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onServiceRemoved(position);
                }
            });
        }
    }
}