/*
package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceItem;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ServiceManagementAdapter extends RecyclerView.Adapter<ServiceManagementAdapter.ServiceViewHolder> {

    public interface OnServiceActionListener {
        void onEditService(HotelServiceItem service, int position);
        void onDeleteService(HotelServiceItem service, int position);
    }

    private List<HotelServiceItem> services;
    private OnServiceActionListener editListener;
    private OnServiceActionListener deleteListener;
    private NumberFormat currencyFormat;

    public ServiceManagementAdapter(List<HotelServiceItem> services, OnServiceActionListener editListener, OnServiceActionListener deleteListener) {
        this.services = services;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_service_management, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        HotelServiceItem service = services.get(position);
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
        private TextView tvServicePrice;
        private TextView tvServiceType;
        private ImageView ivEdit;
        private ImageView ivDelete;
        private View typeIndicator;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServiceIcon = itemView.findViewById(R.id.ivServiceIcon);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceDescription = itemView.findViewById(R.id.tvServiceDescription);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            typeIndicator = itemView.findViewById(R.id.typeIndicator);
        }

        public void bind(HotelServiceItem service, int position) {
            Context context = itemView.getContext();

            ivServiceIcon.setImageResource(IconHelper.getIconResource(service.getIconKey()));
            tvServiceName.setText(service.getName());
            tvServiceDescription.setText(service.getDescription());

            // Configurar según tipo de servicio
            switch (service.getType()) {
                case INCLUDED:
                    tvServicePrice.setText("INCLUIDO");
                    tvServicePrice.setTextColor(ContextCompat.getColor(context, R.color.green));
                    tvServiceType.setText("Básico");
                    tvServiceType.setTextColor(ContextCompat.getColor(context, R.color.green));
                    typeIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
                    break;

                case PAID:
                    tvServicePrice.setText(currencyFormat.format(service.getPrice()));
                    tvServicePrice.setTextColor(ContextCompat.getColor(context, R.color.blue));
                    tvServiceType.setText("Pagado");
                    tvServiceType.setTextColor(ContextCompat.getColor(context, R.color.blue));
                    typeIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
                    break;

                case SPECIAL:
                    tvServicePrice.setText("ESPECIAL");
                    tvServicePrice.setTextColor(ContextCompat.getColor(context, R.color.orange));
                    tvServiceType.setText("Promocional");
                    tvServiceType.setTextColor(ContextCompat.getColor(context, R.color.orange));
                    typeIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.orange));
                    break;
            }

            ivEdit.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onEditService(service, position);
                }
            });

            ivDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteService(service, position);
                }
            });
        }
    }
}




 */