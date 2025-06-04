package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.Reservation;

import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {
    // Constantes para los estados (duplicadas de Reservation para facilidad de uso)
    public static final int ESTADO_PROXIMA = Reservation.STATUS_PROXIMA;
    public static final int ESTADO_ACTUAL = Reservation.STATUS_ACTUAL;
    public static final int ESTADO_CHECKOUT = Reservation.STATUS_CHECKOUT;
    public static final int ESTADO_COMPLETADA = Reservation.STATUS_COMPLETADA;

    private List<Reservation> reservations;
    private int currentState;
    private ReservationActionListener actionListener;

    public interface ReservationActionListener {
        void onActionButtonClicked(Reservation reservation, int position);
        void onReservationCardClicked(Reservation reservation, int position);
    }

    public ReservationAdapter(List<Reservation> reservations, int currentState) {
        this.reservations = reservations;
        this.currentState = currentState;
    }

    public ReservationAdapter(List<Reservation> reservations, int currentState, ReservationActionListener listener) {
        this.reservations = reservations;
        this.currentState = currentState;
        this.actionListener = listener;
    }

    public void setActionListener(ReservationActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation_card, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);
        Context context = holder.itemView.getContext();

        // Configurar datos básicos de la reserva
        holder.tvHotelName.setText(reservation.getHotelName());
        holder.tvLocation.setText(reservation.getLocation());
        holder.tvDate.setText(reservation.getDate());
        holder.tvPrice.setText("S/" + reservation.getPrice());
        holder.tvRating.setText(String.valueOf(reservation.getRating()));
        holder.ivHotelImage.setImageResource(reservation.getImageResource());

        // Configurar el estado
        holder.tvStatus.setText(reservation.getStatusText());
        holder.tvStatus.getBackground().setTint(reservation.getStatusBackgroundColor());

        // Configurar el botón de acción principal
        holder.btnAction.setText(reservation.getActionButtonText());

        // Configurar información adicional si es necesario
        String additionalInfo = reservation.getAdditionalInfo();
        if (!additionalInfo.isEmpty()) {
            holder.additionalInfoContainer.setVisibility(View.VISIBLE);
            holder.tvAdditionalInfo.setText(additionalInfo);

            // Establecer título adecuado según el estado
            switch (reservation.getStatus()) {
                case ESTADO_PROXIMA:
                    holder.tvAdditionalInfoTitle.setText("Información de check-in");
                    break;
                case ESTADO_ACTUAL:
                    holder.tvAdditionalInfoTitle.setText("Servicios adquiridos");
                    break;
                case ESTADO_CHECKOUT:
                    holder.tvAdditionalInfoTitle.setText("Información de checkout");
                    break;
                case ESTADO_COMPLETADA:
                    holder.tvAdditionalInfoTitle.setText("Resumen de estadía");
                    break;
            }
        } else {
            holder.additionalInfoContainer.setVisibility(View.GONE);
        }

        // Configurar acciones para el botón principal con animación de feedback
        holder.btnAction.setOnClickListener(v -> {
            // Efecto visual de feedback
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100);

                // Si hay un listener definido, usarlo
                if (actionListener != null) {
                    actionListener.onActionButtonClicked(reservation, holder.getAdapterPosition());
                    return;
                }

                // Comportamiento por defecto si no hay listener
                switch (reservation.getStatus()) {
                    case ESTADO_PROXIMA:
                        // Abrir detalles de la reserva
                        Toast.makeText(context, "Ver detalles de: " + reservation.getHotelName(), Toast.LENGTH_SHORT).show();
                        break;
                    case ESTADO_ACTUAL:
                        // Abrir servicios adicionales
                        Toast.makeText(context, "Ver servicios para: " + reservation.getHotelName(), Toast.LENGTH_SHORT).show();
                        break;
                    case ESTADO_CHECKOUT:
                        // Iniciar proceso de checkout
                        Toast.makeText(context, "Iniciar checkout para: " + reservation.getHotelName(), Toast.LENGTH_SHORT).show();
                        break;
                    case ESTADO_COMPLETADA:
                        // Mostrar factura
                        Toast.makeText(context, "Ver factura de: " + reservation.getHotelName(), Toast.LENGTH_SHORT).show();
                        break;
                }
            });
        });

        // Configurar acción para toda la tarjeta con animación de feedback
        holder.cardReservation.setOnClickListener(v -> {
            // Efecto visual de feedback
            v.animate().alpha(0.7f).setDuration(100).withEndAction(() -> {
                v.animate().alpha(1f).setDuration(100);

                // Si hay un listener definido, usarlo
                if (actionListener != null) {
                    actionListener.onReservationCardClicked(reservation, holder.getAdapterPosition());
                    return;
                }

                // Comportamiento por defecto
                Toast.makeText(context, "Reserva seleccionada: " + reservation.getHotelName(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        // Elementos básicos de la tarjeta
        View cardReservation;
        TextView tvHotelName, tvLocation, tvDate, tvPrice, tvRating, tvStatus;
        ImageView ivHotelImage;
        Button btnAction;

        // Elementos para información adicional
        ConstraintLayout additionalInfoContainer;
        TextView tvAdditionalInfoTitle, tvAdditionalInfo;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardReservation = itemView;
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivHotelImage = itemView.findViewById(R.id.ivHotelImage);
            btnAction = itemView.findViewById(R.id.btnAction);
            additionalInfoContainer = itemView.findViewById(R.id.additionalInfoContainer);
            tvAdditionalInfoTitle = itemView.findViewById(R.id.tvAdditionalInfoTitle);
            tvAdditionalInfo = itemView.findViewById(R.id.tvAdditionalInfo);
        }
    }
}