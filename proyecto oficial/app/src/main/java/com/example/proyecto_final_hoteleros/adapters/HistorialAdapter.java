package com.example.proyecto_final_hoteleros.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.taxista.model.CompletedTrip;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder> {

    private static final String TAG = "HistorialAdapter";
    private Context context;
    private List<CompletedTrip> historialList;
    private HistorialListener listener;

    public interface HistorialListener {
        void onDetailsClick(CompletedTrip trip);
        void onRepeatTripClick(CompletedTrip trip);
    }

    public HistorialAdapter(Context context, List<CompletedTrip> historialList, HistorialListener listener) {
        this.context = context;
        this.historialList = historialList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("HistorialAdapter", "🏗️ Inflando layout: item_historial_viaje");
        View view = LayoutInflater.from(context).inflate(R.layout.item_historial_viaje, parent, false);

        // Debug: Buscar el botón directamente en la vista inflada
        View btnDetailsTest = view.findViewById(R.id.btn_details);
        Log.d("HistorialAdapter", "🔍 Botón encontrado directamente en vista: " + (btnDetailsTest != null));

        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        try {
            CompletedTrip trip = historialList.get(position);
            if (trip == null) return;

            // Información básica
            if (holder.tvHotelName != null) {
                holder.tvHotelName.setText(trip.getHotelName());
            }
            if (holder.tvClientName != null) {
                holder.tvClientName.setText("Cliente: " + trip.getClientName());
            }
            if (holder.tvTripType != null) {
                holder.tvTripType.setText(trip.getTripType());
            }

            // Fecha y hora
            if (holder.tvCompletedDate != null) {
                holder.tvCompletedDate.setText(trip.getCompletedDate());
            }
            if (holder.tvCompletedTime != null) {
                holder.tvCompletedTime.setText(trip.getCompletedTime());
            }

            // Estado del viaje
            if (holder.tvStatus != null) {
                holder.tvStatus.setText(trip.getStatus());
                try {
                    if (trip.isCompleted()) {
                        holder.tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
                        holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.white));
                    } else if (trip.isCancelled()) {
                        holder.tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                        holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.white));
                    }
                } catch (Exception e) {
                    // Si falla el drawable, usar colores directos
                    if (trip.isCompleted()) {
                        holder.tvStatus.setBackgroundColor(0xFF4CAF50);
                        holder.tvStatus.setTextColor(0xFFFFFFFF);
                    } else if (trip.isCancelled()) {
                        holder.tvStatus.setBackgroundColor(0xFFF44336);
                        holder.tvStatus.setTextColor(0xFFFFFFFF);
                    }
                }
            }

            // Calificación del cliente
            if (holder.tvClientRating != null) {
                holder.tvClientRating.setText(String.valueOf(trip.getClientRating()));
            }

            // Duración y distancia
            if (holder.tvDuration != null) {
                holder.tvDuration.setText(trip.getFormattedDuration());
            }
            if (holder.tvDistance != null) {
                holder.tvDistance.setText(trip.getFormattedDistance());
            }

            // Precio y ganancias
            try {
                NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
                String formattedTotal = format.format(trip.getTotalAmount()).replace("PEN", "S/");
                String formattedEarnings = format.format(trip.getEarnings()).replace("PEN", "S/");

                if (holder.tvTotalAmount != null) {
                    holder.tvTotalAmount.setText(formattedTotal);
                }
                if (holder.tvEarnings != null) {
                    holder.tvEarnings.setText("Ganancia: " + formattedEarnings);
                }
            } catch (Exception e) {
                // Fallback si falla el formato
                if (holder.tvTotalAmount != null) {
                    holder.tvTotalAmount.setText("S/ " + String.format("%.2f", trip.getTotalAmount()));
                }
                if (holder.tvEarnings != null) {
                    holder.tvEarnings.setText("Ganancia: S/ " + String.format("%.2f", trip.getEarnings()));
                }
            }

            // Método de pago
            if (holder.tvPaymentMethod != null) {
                holder.tvPaymentMethod.setText(trip.getPaymentMethod());
            }

            // Direcciones
            if (holder.tvOriginAddress != null) {
                holder.tvOriginAddress.setText(trip.getOriginAddress());
            }
            if (holder.tvDestinationAddress != null) {
                holder.tvDestinationAddress.setText(trip.getDestinationAddress());
            }

            // Cargar imagen del hotel
            if (holder.ivHotelImage != null) {
                try {
                    Glide.with(context)
                            .load(trip.getHotelImageUrl())
                            .placeholder(R.drawable.belmond)
                            .error(R.drawable.belmond)
                            .centerCrop()
                            .into(holder.ivHotelImage);
                } catch (Exception e) {
                    // Si Glide falla, usar imagen por defecto
                    holder.ivHotelImage.setImageResource(R.drawable.belmond);
                }
            }

            // Configurar listeners de forma segura
            setupClickListeners(holder, trip);

        } catch (Exception e) {
            // Log error pero no crashes
            e.printStackTrace();
        }
    }

    private void setupClickListeners(HistorialViewHolder holder, CompletedTrip trip) {
        // Listener para el botón de detalles
        if (holder.btnDetails != null) {
            Log.d("HistorialAdapter", "✅ btnDetails encontrado, configurando listener");
            holder.btnDetails.setOnClickListener(v -> {
                Log.d("HistorialAdapter", "🔥 BOTÓN PRESIONADO!");
                Log.d("HistorialAdapter", "listener != null: " + (listener != null));
                Log.d("HistorialAdapter", "trip != null: " + (trip != null));

                if (trip != null) {
                    Log.d("HistorialAdapter", "Trip ID: " + trip.getId());
                }

                if (listener != null && trip != null) {
                    Log.d("HistorialAdapter", "✅ Llamando listener.onDetailsClick()");
                    listener.onDetailsClick(trip);
                } else {
                    Log.e("HistorialAdapter", "❌ Error: listener=" + listener + ", trip=" + trip);
                }
            });
        } else {
            Log.e("HistorialAdapter", "❌ btnDetails es NULL!");
        }
    }

    @Override
    public int getItemCount() {
        return historialList != null ? historialList.size() : 0;
    }

    static class HistorialViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardTrip;
        ImageView ivHotelImage;
        TextView tvHotelName;
        TextView tvClientName;
        TextView tvTripType;
        TextView tvCompletedDate;
        TextView tvCompletedTime;
        TextView tvStatus;
        TextView tvClientRating;
        TextView tvDuration;
        TextView tvDistance;
        TextView tvTotalAmount;
        TextView tvEarnings;
        TextView tvPaymentMethod;
        TextView tvOriginAddress;
        TextView tvDestinationAddress;
        MaterialButton btnDetails;
        MaterialButton btnRepeatTrip;

        HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                Log.d("HistorialAdapter", "🔍 Inicializando ViewHolder...");
                View btnTest = itemView.findViewById(R.id.btn_details);
                Log.d("HistorialAdapter", "🎯 Botón encontrado directamente: " + (btnTest != null));

                // Inicializar vistas de forma segura
                cardTrip = findViewSafely(itemView, R.id.card_trip);
                ivHotelImage = findViewSafely(itemView, R.id.iv_hotel_image);
                tvHotelName = findViewSafely(itemView, R.id.tv_hotel_name);
                tvClientName = findViewSafely(itemView, R.id.tv_client_name);
                tvTripType = findViewSafely(itemView, R.id.tv_trip_type);
                tvCompletedDate = findViewSafely(itemView, R.id.tv_completed_date);
                tvCompletedTime = findViewSafely(itemView, R.id.tv_completed_time);
                tvStatus = findViewSafely(itemView, R.id.tv_status);
                tvClientRating = findViewSafely(itemView, R.id.tv_client_rating);
                tvDuration = findViewSafely(itemView, R.id.tv_duration);
                tvDistance = findViewSafely(itemView, R.id.tv_distance);
                tvTotalAmount = findViewSafely(itemView, R.id.tv_total_amount);
                tvEarnings = findViewSafely(itemView, R.id.tv_earnings);
                tvPaymentMethod = findViewSafely(itemView, R.id.tv_payment_method);
                tvOriginAddress = findViewSafely(itemView, R.id.tv_origin_address);
                tvDestinationAddress = findViewSafely(itemView, R.id.tv_destination_address);

                // Los más importantes para debug
                btnDetails = findViewSafely(itemView, R.id.btn_details);
                btnRepeatTrip = findViewSafely(itemView, R.id.btn_repeat_trip);

                Log.d("HistorialAdapter", "btnDetails encontrado: " + (btnDetails != null));
                Log.d("HistorialAdapter", "btnRepeatTrip encontrado: " + (btnRepeatTrip != null));

                Log.d("HistorialAdapter", "✅ btnDetails después de findViewSafely: " + (btnDetails != null));

            } catch (Exception e) {
                Log.e("HistorialAdapter", "❌ Error en ViewHolder: " + e.getMessage(), e);
                e.printStackTrace();
            }
        }

        @SuppressWarnings("unchecked")
        private <T extends View> T findViewSafely(View parent, int id) {
            try {
                return (T) parent.findViewById(id);
            } catch (Exception e) {
                return null;
            }
        }
    }
}