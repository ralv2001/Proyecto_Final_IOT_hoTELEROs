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
    // ✅ SOLO 3 ESTADOS (sin checkout)
    public static final int ESTADO_PROXIMA = Reservation.STATUS_PROXIMA;
    public static final int ESTADO_ACTUAL = Reservation.STATUS_ACTUAL;
    public static final int ESTADO_COMPLETADA = Reservation.STATUS_COMPLETADA;

    private List<Reservation> reservations;
    private int currentState;
    private ReservationActionListener actionListener;

    public interface ReservationActionListener {
        void onActionButtonClicked(Reservation reservation, int position);
        void onReservationCardClicked(Reservation reservation, int position);

        // ✅ NUEVO: Callback específico para checkout
        void onCheckoutRequested(Reservation reservation, int position);
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
                .inflate(R.layout.client_item_reservation_card, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);
        Context context = holder.itemView.getContext();

        // Configurar datos básicos
        holder.tvHotelName.setText(reservation.getHotelName());
        holder.tvLocation.setText(reservation.getLocation());
        holder.tvDate.setText(reservation.getDate());
        holder.tvPrice.setText("S/" + reservation.getPrice());
        holder.tvRating.setText(String.valueOf(reservation.getRating()));
        holder.ivHotelImage.setImageResource(reservation.getImageResource());

        // Configurar estado
        holder.tvStatus.setText(reservation.getStatusText());
        holder.tvStatus.getBackground().setTint(reservation.getStatusBackgroundColor());

        // ✅ NUEVO: Configurar botón según estado y si está listo para checkout
        setupActionButton(holder, reservation, position, context);

        // Configurar información adicional
        String additionalInfo = reservation.getAdditionalInfo();
        if (!additionalInfo.isEmpty()) {
            holder.additionalInfoContainer.setVisibility(View.VISIBLE);
            holder.tvAdditionalInfo.setText(additionalInfo);
            setupAdditionalInfoTitle(holder, reservation);
        } else {
            holder.additionalInfoContainer.setVisibility(View.GONE);
        }

        // Click en toda la tarjeta
        holder.cardReservation.setOnClickListener(v -> {
            animateCardClick(v, () -> {
                if (actionListener != null) {
                    actionListener.onReservationCardClicked(reservation, holder.getAdapterPosition());
                } else {
                    Toast.makeText(context, "Ver detalles: " + reservation.getHotelName(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // ✅ NUEVO: Configurar botón de acción inteligente
    private void setupActionButton(ReservationViewHolder holder, Reservation reservation, int position, Context context) {
        String buttonText = reservation.getActionButtonText();
        holder.btnAction.setText(buttonText);

        holder.btnAction.setOnClickListener(v -> {
            animateButtonClick(v, () -> {
                if (actionListener != null) {
                    // Si es una reserva actual y está lista para checkout
                    if (reservation.getStatus() == Reservation.STATUS_ACTUAL && reservation.isReadyForCheckout()) {
                        actionListener.onCheckoutRequested(reservation, position);
                    } else {
                        actionListener.onActionButtonClicked(reservation, position);
                    }
                    return;
                }

                // Comportamiento por defecto
                handleDefaultAction(reservation, context);
            });
        });

        // ✅ Cambiar color del botón si es checkout
        if (reservation.getStatus() == Reservation.STATUS_ACTUAL && reservation.isReadyForCheckout()) {
            holder.btnAction.setBackgroundTintList(
                    context.getResources().getColorStateList(R.color.orange_primary, null));
        } else {
            holder.btnAction.setBackgroundTintList(
                    context.getResources().getColorStateList(R.color.blue_primary, null));
        }
    }

    private void setupAdditionalInfoTitle(ReservationViewHolder holder, Reservation reservation) {
        switch (reservation.getStatus()) {
            case ESTADO_PROXIMA:
                holder.tvAdditionalInfoTitle.setText("Información de check-in");
                break;
            case ESTADO_ACTUAL:
                if (reservation.isReadyForCheckout()) {
                    holder.tvAdditionalInfoTitle.setText("Resumen para checkout");
                } else {
                    holder.tvAdditionalInfoTitle.setText("Servicios adquiridos");
                }
                break;
            case ESTADO_COMPLETADA:
                holder.tvAdditionalInfoTitle.setText("Resumen de estadía");
                break;
        }
    }

    private void handleDefaultAction(Reservation reservation, Context context) {
        switch (reservation.getStatus()) {
            case ESTADO_PROXIMA:
                Toast.makeText(context, "Ver detalles de: " + reservation.getHotelName(), Toast.LENGTH_SHORT).show();
                break;
            case ESTADO_ACTUAL:
                if (reservation.isReadyForCheckout()) {
                    Toast.makeText(context, "Iniciando checkout: " + reservation.getHotelName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Ver servicios: " + reservation.getHotelName(), Toast.LENGTH_SHORT).show();
                }
                break;
            case ESTADO_COMPLETADA:
                Toast.makeText(context, "Ver factura: " + reservation.getHotelName(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // ✅ MEJORES ANIMACIONES
    private void animateButtonClick(View view, Runnable action) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .withEndAction(action);
                });
    }

    private void animateCardClick(View view, Runnable action) {
        view.animate()
                .alpha(0.8f)
                .setDuration(150)
                .withEndAction(() -> {
                    view.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .withEndAction(action);
                });
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        View cardReservation;
        TextView tvHotelName, tvLocation, tvDate, tvPrice, tvRating, tvStatus;
        ImageView ivHotelImage;
        Button btnAction;
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