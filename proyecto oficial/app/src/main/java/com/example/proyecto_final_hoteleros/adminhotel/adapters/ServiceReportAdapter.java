package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.ServiceReport;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ServiceReportAdapter extends RecyclerView.Adapter<ServiceReportAdapter.ServiceReportViewHolder> {

    private List<ServiceReport> serviceReports;
    private NumberFormat currencyFormat;

    public ServiceReportAdapter(List<ServiceReport> serviceReports) {
        this.serviceReports = serviceReports;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
    }

    @NonNull
    @Override
    public ServiceReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_service_report, parent, false);
        return new ServiceReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceReportViewHolder holder, int position) {
        ServiceReport service = serviceReports.get(position);
        holder.bind(service);
    }

    @Override
    public int getItemCount() {
        return serviceReports.size();
    }

    class ServiceReportViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivServiceIcon;
        private TextView tvServiceName, tvRequests, tvRevenue, tvRating, tvType;
        private View typeIndicator;

        public ServiceReportViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServiceIcon = itemView.findViewById(R.id.ivServiceIcon);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvRequests = itemView.findViewById(R.id.tvRequests);
            tvRevenue = itemView.findViewById(R.id.tvRevenue);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvType = itemView.findViewById(R.id.tvType);
            typeIndicator = itemView.findViewById(R.id.typeIndicator);
        }

        public void bind(ServiceReport service) {
            ivServiceIcon.setImageResource(IconHelper.getIconResource(service.getIconKey()));
            tvServiceName.setText(service.getName());
            tvRequests.setText(service.getTotalRequests() + " solicitudes");
            tvRating.setText(String.format("%.1f ⭐", service.getAverageRating()));
            tvType.setText(service.getType());

            if (service.getTotalRevenue() > 0) {
                tvRevenue.setText(currencyFormat.format(service.getTotalRevenue()));
                tvRevenue.setVisibility(View.VISIBLE);
            } else {
                tvRevenue.setText("Gratuito");
                tvRevenue.setVisibility(View.VISIBLE);
            }

            // Type indicator color
            int typeColor;
            switch (service.getType()) {
                case "PAID":
                    typeColor = R.color.orange;
                    break;
                case "SPECIAL":
                    typeColor = R.color.purple;
                    break;
                default: // INCLUDED
                    typeColor = R.color.green;
                    break;
            }
            typeIndicator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), typeColor));
            tvType.setTextColor(ContextCompat.getColor(itemView.getContext(), typeColor));
        }
    }
}