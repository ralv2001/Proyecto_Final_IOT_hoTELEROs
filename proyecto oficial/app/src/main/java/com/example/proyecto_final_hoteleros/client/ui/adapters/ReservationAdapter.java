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
import java.text.DecimalFormat;
import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {
    // ✅ CONSTANTES ACTUALIZADAS (solo 3 estados)
    public static final int ESTADO_UPCOMING = Reservation.STATUS_UPCOMING;
    public static final int ESTADO_ACTIVE = Reservation.STATUS_ACTIVE;
    public static final int ESTADO_COMPLETED = Reservation.STATUS_COMPLETED;

    private List<Reservation> reservations;
    private int currentState;
    private ReservationActionListener actionListener;

    public interface ReservationActionListener {
        void onActionButtonClicked(Reservation reservation, int position);
        void onReservationCardClicked(Reservation reservation, int position);

        // ✅ CALLBACKS ESPECÍFICOS PROFESIONALES
        void onModifyReservation(Reservation reservation, int position);
        void onCheckoutRequested(Reservation reservation, int position);
        void onViewBill(Reservation reservation, int position);
        void onSubmitReview(Reservation reservation, int position);
        void onViewInvoice(Reservation reservation, int position);
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

        // ✅ CONFIGURAR DATOS BÁSICOS
        holder.tvHotelName.setText(reservation.getHotelName());
        holder.tvLocation.setText(reservation.getLocation());
        holder.tvDate.setText(reservation.getDate());

        // ✅ MOSTRAR PRECIO SEGÚN ESTADO
        setupPriceDisplay(holder, reservation);

        holder.tvRating.setText(String.valueOf(reservation.getRating()));
        holder.ivHotelImage.setImageResource(reservation.getImageResource());

        // ✅ CONFIGURAR ESTADO PROFESIONAL
        setupProfessionalStatus(holder, reservation);

        // ✅ CONFIGURAR BOTÓN INTELIGENTE
        setupIntelligentActionButton(holder, reservation, position, context);

        // ✅ CONFIGURAR INFORMACIÓN CONTEXTUAL
        setupContextualInfo(holder, reservation);

        // ✅ CONFIGURAR INDICADORES VISUALES
        setupVisualIndicators(holder, reservation);

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

    // ✅ CONFIGURAR PRECIO SEGÚN ESTADO
    private void setupPriceDisplay(ReservationViewHolder holder, Reservation reservation) {
        DecimalFormat formatter = new DecimalFormat("0.00");

        switch (reservation.getStatus()) {
            case ESTADO_UPCOMING:
                holder.tvPriceLabel.setText("Precio estimado");
                holder.tvPrice.setText("S/" + formatter.format(reservation.getBasePrice()));
                break;
            case ESTADO_ACTIVE:
                if (reservation.isCheckoutPending()) {
                    holder.tvPriceLabel.setText("Total pendiente");
                    holder.tvPrice.setText("S/" + formatter.format(reservation.getFinalTotal()));
                } else {
                    holder.tvPriceLabel.setText("Total actual");
                    double currentTotal = reservation.getBasePrice() + reservation.getServicesTotal();
                    holder.tvPrice.setText("S/" + formatter.format(currentTotal));
                }
                break;
            case ESTADO_COMPLETED:
                holder.tvPriceLabel.setText("Total pagado");
                holder.tvPrice.setText("S/" + formatter.format(reservation.getFinalTotal()));
                break;
        }
    }

    // ✅ CONFIGURAR ESTADO PROFESIONAL CON SUB-ESTADOS
    private void setupProfessionalStatus(ReservationViewHolder holder, Reservation reservation) {
        holder.tvStatus.setText(reservation.getStatusText());
        holder.tvStatus.getBackground().setTint(reservation.getStatusBackgroundColor());

        // ✅ AGREGAR ÍCONOS AL ESTADO
        int statusIcon = getStatusIcon(reservation);
        if (statusIcon != 0) {
            holder.tvStatus.setCompoundDrawablesWithIntrinsicBounds(statusIcon, 0, 0, 0);
            holder.tvStatus.setCompoundDrawablePadding(8);
        }
    }

    private int getStatusIcon(Reservation reservation) {
        switch (reservation.getStatus()) {
            case ESTADO_UPCOMING:
                return R.drawable.ic_upcoming;
            case ESTADO_ACTIVE:
                switch (reservation.getSubStatus()) {
                    case Reservation.SUBSTATUS_CHECKED_IN:
                        return R.drawable.ic_active;
                    case Reservation.SUBSTATUS_STAYING:
                        return R.drawable.ic_active;
                    case Reservation.SUBSTATUS_CHECKOUT_PENDING:
                        return R.drawable.ic_clock;
                    default:
                        return R.drawable.ic_active;
                }
            case ESTADO_COMPLETED:
                return R.drawable.ic_completedgozu;
            default:
                return 0;
        }
    }

    // ✅ CONFIGURAR BOTÓN INTELIGENTE
    private void setupIntelligentActionButton(ReservationViewHolder holder, Reservation reservation, int position, Context context) {
        String buttonText = reservation.getActionButtonText();
        holder.btnAction.setText(buttonText);

        // ✅ COLORES SEGÚN ACCIÓN
        int buttonColor = getButtonColor(reservation, context);
        holder.btnAction.setBackgroundTintList(context.getResources().getColorStateList(buttonColor, null));

        holder.btnAction.setOnClickListener(v -> {
            animateButtonClick(v, () -> {
                if (actionListener != null) {
                    handleIntelligentAction(reservation, position);
                    return;
                }

                // Comportamiento por defecto
                handleDefaultAction(reservation, context);
            });
        });
    }

    private int getButtonColor(Reservation reservation, Context context) {
        switch (reservation.getStatus()) {
            case ESTADO_UPCOMING:
                return reservation.canModify() ? R.color.blue_primary : R.color.gray_500;
            case ESTADO_ACTIVE:
                if (reservation.isCheckoutPending()) {
                    return R.color.amber_600; // Pendiente
                } else {
                    return R.color.orange_primary; // Checkout
                }
            case ESTADO_COMPLETED:
                return reservation.isReviewSubmitted() ? R.color.gray_500 : R.color.green_500;
            default:
                return R.color.blue_primary;
        }
    }

    // ✅ MANEJAR ACCIONES INTELIGENTES
    private void handleIntelligentAction(Reservation reservation, int position) {
        switch (reservation.getStatus()) {
            case ESTADO_UPCOMING:
                if (reservation.canModify()) {
                    actionListener.onModifyReservation(reservation, position);
                } else {
                    actionListener.onActionButtonClicked(reservation, position);
                }
                break;
            case ESTADO_ACTIVE:
                if (reservation.canRequestCheckout()) {
                    actionListener.onCheckoutRequested(reservation, position);
                } else if (reservation.isCheckoutPending()) {
                    actionListener.onViewBill(reservation, position);
                } else {
                    actionListener.onActionButtonClicked(reservation, position);
                }
                break;
            case ESTADO_COMPLETED:
                if (reservation.isReviewSubmitted()) {
                    actionListener.onViewInvoice(reservation, position);
                } else {
                    actionListener.onSubmitReview(reservation, position);
                }
                break;
            default:
                actionListener.onActionButtonClicked(reservation, position);
                break;
        }
    }

    // ✅ CONFIGURAR INFORMACIÓN CONTEXTUAL
    private void setupContextualInfo(ReservationViewHolder holder, Reservation reservation) {
        String contextualInfo = reservation.getContextualInfo();

        if (!contextualInfo.isEmpty()) {
            holder.additionalInfoContainer.setVisibility(View.VISIBLE);
            holder.tvAdditionalInfo.setText(contextualInfo);
            setupContextualInfoTitle(holder, reservation);
        } else {
            holder.additionalInfoContainer.setVisibility(View.GONE);
        }
    }

    private void setupContextualInfoTitle(ReservationViewHolder holder, Reservation reservation) {
        switch (reservation.getStatus()) {
            case ESTADO_UPCOMING:
                holder.tvAdditionalInfoTitle.setText("📋 Información de check-in");
                break;
            case ESTADO_ACTIVE:
                if (reservation.isCheckoutPending()) {
                    holder.tvAdditionalInfoTitle.setText("⏳ Checkout en proceso");
                } else {
                    holder.tvAdditionalInfoTitle.setText("🏨 Estado actual");
                }
                break;
            case ESTADO_COMPLETED:
                holder.tvAdditionalInfoTitle.setText("✅ Estadía completada");
                break;
        }
    }

    // ✅ CONFIGURAR INDICADORES VISUALES
    private void setupVisualIndicators(ReservationViewHolder holder, Reservation reservation) {
        // Mostrar número de habitación si está disponible
        if (reservation.getRoomNumber() != null && !reservation.getRoomNumber().isEmpty()) {
            if (holder.tvRoomNumber != null) {
                holder.tvRoomNumber.setVisibility(View.VISIBLE);
                holder.tvRoomNumber.setText("Hab. " + reservation.getRoomNumber());
            }
        } else {
            if (holder.tvRoomNumber != null) {
                holder.tvRoomNumber.setVisibility(View.GONE);
            }
        }

        // ✅ INDICADOR DE SERVICIOS
        if (reservation.getServicesTotal() > 0) {
            if (holder.tvServicesIndicator != null) {
                holder.tvServicesIndicator.setVisibility(View.VISIBLE);
                holder.tvServicesIndicator.setText("🍽️ +" + reservation.getServices().size() + " servicios");
            }
        } else {
            if (holder.tvServicesIndicator != null) {
                holder.tvServicesIndicator.setVisibility(View.GONE);
            }
        }

        // ✅ INDICADOR DE TAXI GRATIS
        if (reservation.isEligibleForFreeTaxi()) {
            if (holder.tvTaxiIndicator != null) {
                holder.tvTaxiIndicator.setVisibility(View.VISIBLE);
                holder.tvTaxiIndicator.setText("🚖 Taxi gratis incluido");
            }
        } else {
            if (holder.tvTaxiIndicator != null) {
                holder.tvTaxiIndicator.setVisibility(View.GONE);
            }
        }

        // ✅ INDICADOR DE CARGOS ADICIONALES
        if (reservation.getAdditionalCharges() > 0) {
            if (holder.tvAdditionalChargesIndicator != null) {
                holder.tvAdditionalChargesIndicator.setVisibility(View.VISIBLE);
                holder.tvAdditionalChargesIndicator.setText("⚠️ Cargos adicionales");
            }
        } else {
            if (holder.tvAdditionalChargesIndicator != null) {
                holder.tvAdditionalChargesIndicator.setVisibility(View.GONE);
            }
        }
    }

    private void handleDefaultAction(Reservation reservation, Context context) {
        switch (reservation.getStatus()) {
            case ESTADO_UPCOMING:
                Toast.makeText(context,
                        reservation.canModify() ? "Modificar: " + reservation.getHotelName() : "Ver detalles: " + reservation.getHotelName(),
                        Toast.LENGTH_SHORT).show();
                break;
            case ESTADO_ACTIVE:
                if (reservation.canRequestCheckout()) {
                    Toast.makeText(context, "Solicitar checkout: " + reservation.getHotelName(), Toast.LENGTH_SHORT).show();
                } else if (reservation.isCheckoutPending()) {
                    Toast.makeText(context, "Ver factura pendiente: " + reservation.getHotelName(), Toast.LENGTH_SHORT).show();
                }
                break;
            case ESTADO_COMPLETED:
                Toast.makeText(context,
                        reservation.isReviewSubmitted() ? "Ver factura: " + reservation.getHotelName() : "Valorar estadía: " + reservation.getHotelName(),
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // ✅ ANIMACIONES MEJORADAS
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
        TextView tvHotelName, tvLocation, tvDate, tvPrice, tvPriceLabel, tvRating, tvStatus;
        TextView tvRoomNumber, tvServicesIndicator, tvTaxiIndicator, tvAdditionalChargesIndicator;
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
            tvPriceLabel = itemView.findViewById(R.id.tvPriceLabel);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivHotelImage = itemView.findViewById(R.id.ivHotelImage);
            btnAction = itemView.findViewById(R.id.btnAction);
            additionalInfoContainer = itemView.findViewById(R.id.additionalInfoContainer);
            tvAdditionalInfoTitle = itemView.findViewById(R.id.tvAdditionalInfoTitle);
            tvAdditionalInfo = itemView.findViewById(R.id.tvAdditionalInfo);

            // ✅ INICIALIZAR INDICADORES (pueden no existir en layout actual)
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
            tvServicesIndicator = itemView.findViewById(R.id.tvServicesIndicator);
            tvTaxiIndicator = itemView.findViewById(R.id.tvTaxiIndicator);
            tvAdditionalChargesIndicator = itemView.findViewById(R.id.tvAdditionalChargesIndicator);
        }
    }
}