package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.CheckoutItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ServiceDetailAdapter extends RecyclerView.Adapter<ServiceDetailAdapter.ServiceViewHolder> {

    private Context context;
    private List<CheckoutItem.ServicioAdicional> services;
    private NumberFormat currencyFormat;

    public ServiceDetailAdapter(Context context, List<CheckoutItem.ServicioAdicional> services) {
        this.context = context;
        this.services = services;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_hotel_item_service_detail, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        CheckoutItem.ServicioAdicional service = services.get(position);
        holder.bind(service);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivServiceIcon;
        private TextView tvServiceName;
        private TextView tvServiceDescription;
        private TextView tvServiceCost;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServiceIcon = itemView.findViewById(R.id.ivServiceIcon);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceDescription = itemView.findViewById(R.id.tvServiceDescription);
            tvServiceCost = itemView.findViewById(R.id.tvServiceCost);
        }

        public void bind(CheckoutItem.ServicioAdicional service) {
            tvServiceName.setText(service.getNombre());

            String description = service.getDescripcion();
            if (service.getCantidad() > 1) {
                description += " (x" + service.getCantidad() + ")";
            }
            description += " • " + service.getFecha();
            tvServiceDescription.setText(description);

            tvServiceCost.setText(currencyFormat.format(service.getTotal()));

            // Iconos según el tipo de servicio
            switch (service.getNombre().toLowerCase()) {
                case "spa & wellness":
                case "spa":
                    ivServiceIcon.setImageResource(R.drawable.ic_spa);
                    break;
                case "room service":
                case "servicio a la habitación":
                    ivServiceIcon.setImageResource(R.drawable.ic_room_service);
                    break;
                case "minibar":
                    ivServiceIcon.setImageResource(R.drawable.ic_minibar);
                    break;
                case "lavandería":
                case "laundry":
                    ivServiceIcon.setImageResource(R.drawable.ic_laundry);
                    break;
                case "gimnasio":
                case "gym":
                    ivServiceIcon.setImageResource(R.drawable.ic_gym);
                    break;
                case "transporte":
                case "transport":
                    ivServiceIcon.setImageResource(R.drawable.ic_car);
                    break;
                default:
                    ivServiceIcon.setImageResource(R.drawable.ic_hotel_service_default);
                    break;
            }
        }
    }
}