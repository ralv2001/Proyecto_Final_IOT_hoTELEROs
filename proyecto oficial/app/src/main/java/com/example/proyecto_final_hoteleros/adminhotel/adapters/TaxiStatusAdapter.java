package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.TaxiStatus;

import java.util.List;

public class TaxiStatusAdapter extends RecyclerView.Adapter<TaxiStatusAdapter.TaxiStatusViewHolder> {

    private List<TaxiStatus> taxiStatusList;

    public TaxiStatusAdapter(List<TaxiStatus> taxiStatusList) {
        this.taxiStatusList = taxiStatusList;
    }

    @NonNull
    @Override
    public TaxiStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_taxi_status, parent, false);
        return new TaxiStatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxiStatusViewHolder holder, int position) {
        TaxiStatus taxiStatus = taxiStatusList.get(position);
        holder.bind(taxiStatus);
    }

    @Override
    public int getItemCount() {
        return taxiStatusList.size();
    }

    class TaxiStatusViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDriverName;
        private TextView tvVehiclePlate;
        private TextView tvEstado;
        private TextView tvClientName;
        private TextView tvETA;
        private View statusIndicator;

        public TaxiStatusViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDriverName = itemView.findViewById(R.id.tvDriverName);
            tvVehiclePlate = itemView.findViewById(R.id.tvVehiclePlate);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvETA = itemView.findViewById(R.id.tvETA);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }

        public void bind(TaxiStatus taxiStatus) {
            tvDriverName.setText(taxiStatus.getDriverName());
            tvVehiclePlate.setText(taxiStatus.getVehiclePlate());
            tvEstado.setText(taxiStatus.getEstado());
            tvClientName.setText("Cliente: " + taxiStatus.getClientName());
            tvETA.setText(taxiStatus.getEta());

            // Set status color
            int statusColor;
            switch (taxiStatus.getEstado()) {
                case "Asignado":
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.orange);
                    break;
                case "En camino":
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.blue);
                    break;
                case "Llegó al destino":
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.green);
                    break;
                default:
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.light_gray);
                    break;
            }

            statusIndicator.setBackgroundColor(statusColor);
            tvEstado.setTextColor(statusColor);
        }
    }
}