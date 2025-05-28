package com.example.proyecto_final_hoteleros.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.taxista.model.SolicitudViaje;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ViajesAdapter extends RecyclerView.Adapter<ViajesAdapter.ViajesViewHolder> {

    private List<SolicitudViaje> solicitudesList;
    private Context context;
    private ViajeListener listener;

    // Interface para manejar eventos
    public interface ViajeListener {
        void onAcceptClick(SolicitudViaje solicitud);
        void onRejectClick(SolicitudViaje solicitud);
        void onDetailsClick(SolicitudViaje solicitud);
    }

    public ViajesAdapter(Context context, List<SolicitudViaje> solicitudesList, ViajeListener listener) {
        this.context = context;
        this.solicitudesList = solicitudesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViajesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_solicitud_viaje, parent, false);
        return new ViajesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViajesViewHolder holder, int position) {
        SolicitudViaje solicitud = solicitudesList.get(position);

        // Configurar datos del hotel
        holder.tvHotelName.setText(solicitud.getHotelName());
        holder.tvRating.setText(String.format("%.1f", solicitud.getRating()));

        // Nombre del cliente (NUEVO)
        holder.tvClientName.setText("Cliente: " + solicitud.getClientName());

        // Estado, fecha y ubicación
        holder.tvStatus.setText(solicitud.getStatus());
        holder.tvDate.setText(solicitud.getDateRange());
        holder.tvLocation.setText(solicitud.getLocation());

        // Origen y destino (NUEVOS)
        holder.tvOriginAddress.setText(solicitud.getOriginAddress());
        holder.tvDestinationAddress.setText(solicitud.getDestinationAddress());

        // Tiempo estimado (NUEVO)
        holder.tvEstimatedTime.setText(solicitud.getEstimatedTime() + " min");

        // Cargar imagen
        if (solicitud.getImageUrl() != null && !solicitud.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(solicitud.getImageUrl())
                    .placeholder(R.drawable.belmond)
                    .error(R.drawable.belmond)
                    .centerCrop()
                    .into(holder.ivHotelImage);
        }

        // Formatear precio
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
        format.setMaximumFractionDigits(1);
        String formattedPrice = format.format(solicitud.getPrice()).replace("PEN", "S/");
        holder.tvPrice.setText(formattedPrice);

        // Configurar el botón de acción
        holder.btnAction.setText("Aceptar viaje");
        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAcceptClick(solicitud);
            }
        });
        holder.btnAction.setText("Aceptar viaje");
        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAcceptClick(solicitud);
            }
        });

        // Configurar el enlace "Ver detalles"
        holder.tvViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailsClick(solicitud);
            }
        });

    }

    @Override
    public int getItemCount() {
        return solicitudesList != null ? solicitudesList.size() : 0;
    }

    public static class ViajesViewHolder extends RecyclerView.ViewHolder {
        TextView tvHotelName, tvRating, tvStatus, tvClientName;
        TextView tvDate, tvLocation, tvPrice, tvEstimatedTime;
        TextView tvOriginAddress, tvDestinationAddress;
        TextView tvViewDetails; // Nuevo TextView para "Ver detalles"
        ImageView ivHotelImage;
        MaterialButton btnAction;
        CardView cardReservation;

        public ViajesViewHolder(@NonNull View itemView) {
            super(itemView);

            cardReservation = itemView.findViewById(R.id.cardReservation);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvEstimatedTime = itemView.findViewById(R.id.tvEstimatedTime);
            tvOriginAddress = itemView.findViewById(R.id.tvOriginAddress);
            tvDestinationAddress = itemView.findViewById(R.id.tvDestinationAddress);
            tvViewDetails = itemView.findViewById(R.id.tvViewDetails); // Inicializar el nuevo TextView
            ivHotelImage = itemView.findViewById(R.id.ivHotelImage);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}