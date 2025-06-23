package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;

import java.util.List;

public class ServiceSelectionAdapter extends RecyclerView.Adapter<ServiceSelectionAdapter.ServiceViewHolder> {

    private List<String> availableServices;
    private List<String> selectedServices;

    public ServiceSelectionAdapter(List<String> availableServices, List<String> selectedServices) {
        this.availableServices = availableServices;
        this.selectedServices = selectedServices;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_service_selection, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        String service = availableServices.get(position);
        holder.bind(service);
    }

    @Override
    public int getItemCount() {
        return availableServices.size();
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbService;
        private TextView tvServiceName;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            cbService = itemView.findViewById(R.id.cbService);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
        }

        public void bind(String service) {
            tvServiceName.setText(service);
            cbService.setChecked(selectedServices.contains(service));

            cbService.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedServices.contains(service)) {
                        selectedServices.add(service);
                    }
                } else {
                    selectedServices.remove(service);
                }
            });

            itemView.setOnClickListener(v -> cbService.setChecked(!cbService.isChecked()));
        }
    }
}