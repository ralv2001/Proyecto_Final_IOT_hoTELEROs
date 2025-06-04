package com.example.proyecto_final_hoteleros.client.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.model.HotelService;

/**
 * ViewHolder para elementos de vista previa de servicios
 */
public class ServicePreviewViewHolder extends RecyclerView.ViewHolder {
    private ImageView ivServiceIcon;
    private TextView tvServiceName;
    private TextView tvPriceBadge;
    private View iconContainer;

    public ServicePreviewViewHolder(@NonNull View itemView) {
        super(itemView);
        ivServiceIcon = itemView.findViewById(R.id.iv_service_icon);
        tvServiceName = itemView.findViewById(R.id.tv_service_name);
        tvPriceBadge = itemView.findViewById(R.id.tv_price_badge);
        iconContainer = itemView.findViewById(R.id.fl_service_icon_container);
    }

    /**
     * Configura la vista con los datos del servicio
     */
    public void bind(HotelService service) {
        tvServiceName.setText(service.getName());

        // Configurar icono
        if (service.getImageUrl() != null) {
            // Aquí usaríamos una biblioteca como Glide o Picasso para cargar la imagen desde URL
            // Por ejemplo: Glide.with(itemView.getContext()).load(service.getImageUrl()).into(ivServiceIcon);
        } else {
            // Usar icono por defecto
            int resourceId = itemView.getContext().getResources().getIdentifier(
                    service.getIconResourceName(), "drawable", itemView.getContext().getPackageName());

            if (resourceId > 0) {
                ivServiceIcon.setImageResource(resourceId);
            } else {
                // Fallback a un icono genérico
                ivServiceIcon.setImageResource(R.drawable.ic_hotel_service_default);
            }
        }

        // Mostrar badge de precio si es un servicio de pago
        if (service.getPrice() != null && service.getPrice() > 0) {
            tvPriceBadge.setVisibility(View.VISIBLE);
        } else {
            tvPriceBadge.setVisibility(View.GONE);
        }
    }
}