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
import androidx.core.content.ContextCompat;
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
    private Context context;

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
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.client_item_reservation_card, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);

        // ✅ CONFIGURAR DATOS BÁSICOS
        setupBasicInfo(holder, reservation);

        // ✅ CONFIGURAR ESTADO PROFESIONAL CON INDICADORES VISUALES
        setupProfessionalStatus(holder, reservation);

        // ✅ CONFIGURAR PRECIOS SEGÚN ESTADO
        setupPriceDisplay(holder, reservation);

        // ✅ CONFIGURAR BOTÓN INTELIGENTE
        setupIntelligentActionButton(holder, reservation, position);

        // ✅ CONFIGURAR INFORMACIÓN CONTEXTUAL
        setupContextualInfo(holder, reservation);

        // ✅ CONFIGURAR INDICADORES VISUALES
        setupVisualIndicators(holder, reservation);

        // ✅ CONFIGURAR LISTENERS
        setupClickListeners(holder, reservation, position);
    }

    private void setupBasicInfo(ReservationViewHolder holder, Reservation reservation) {
        holder.tvHotelName.setText(reservation.getHotelName());
        holder.tvLocation.setText(reservation.getLocation());
        holder.tvDate.setText(reservation.getDate());
        holder.tvRating.setText(String.valueOf(reservation.getRating()));
        holder.ivHotelImage.setImageResource(reservation.getImageResource());
    }

    private void setupProfessionalStatus(ReservationViewHolder holder, Reservation reservation) {
        // ✅ CONFIGURAR ESTADO CON COLOR Y TEXTO INTELIGENTE
        String statusText = reservation.getStatusText();
        int statusColor = reservation.getStatusBackgroundColor();

        if (holder.tvStatus != null) {
            holder.tvStatus.setText(statusText);
            holder.tvStatus.setBackgroundColor(statusColor);
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white));

            // ✅ PADDING Y ESQUINAS REDONDEADAS
            int padding = (int) (8 * context.getResources().getDisplayMetrics().density);
            holder.tvStatus.setPadding(padding, padding/2, padding, padding/2);
        }
    }

    private void setupPriceDisplay(ReservationViewHolder holder, Reservation reservation) {
        DecimalFormat formatter = new DecimalFormat("0.00");

        switch (reservation.getStatus()) {
            case ESTADO_UPCOMING:
                // Mostrar precio base de la habitación
                if (holder.tvPrice != null) {
                    holder.tvPrice.setText("S/ " + formatter.format(reservation.getBasePrice()));
                }
                if (holder.tvPriceLabel != null) {
                    holder.tvPriceLabel.setText("Precio base");
                }
                break;

            case ESTADO_ACTIVE:
                // Mostrar total parcial (base + servicios)
                double partialTotal = reservation.getBasePrice() + reservation.getServicesTotal();
                if (holder.tvPrice != null) {
                    holder.tvPrice.setText("S/ " + formatter.format(partialTotal));
                }
                if (holder.tvPriceLabel != null) {
                    holder.tvPriceLabel.setText("Total parcial");
                }
                break;

            case ESTADO_COMPLETED:
                // Mostrar total final
                if (holder.tvPrice != null) {
                    holder.tvPrice.setText("S/ " + formatter.format(reservation.getFinalTotal()));
                }
                if (holder.tvPriceLabel != null) {
                    holder.tvPriceLabel.setText("Total pagado");
                }
                break;

            default:
                if (holder.tvPrice != null) {
                    holder.tvPrice.setText("S/ " + formatter.format(reservation.getBasePrice()));
                }
                if (holder.tvPriceLabel != null) {
                    holder.tvPriceLabel.setText("Precio");
                }
                break;
        }
    }

    private void setupIntelligentActionButton(ReservationViewHolder holder, Reservation reservation, int position) {
        if (holder.btnAction == null) return;

        String buttonText = reservation.getActionButtonText();
        holder.btnAction.setText(buttonText);

        // ✅ CONFIGURAR COLOR Y ESTILO DEL BOTÓN SEGÚN ESTADO
        switch (reservation.getStatus()) {
            case ESTADO_UPCOMING:
                if (reservation.canModify()) {
                    // Botón para modificar (naranja)
                    holder.btnAction.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.orange_primary));
                    holder.btnAction.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                } else {
                    // Botón para ver detalles (gris)
                    holder.btnAction.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.text_secondary));
                    holder.btnAction.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                }
                break;

            case ESTADO_ACTIVE:
                if (reservation.canRequestCheckout()) {
                    // Botón para checkout (verde)
                    holder.btnAction.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.success_green));
                    holder.btnAction.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                } else if (reservation.isCheckoutPending()) {
                    // Botón para ver estado (naranja)
                    holder.btnAction.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.orange_primary));
                    holder.btnAction.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                } else {
                    // Botón para ver detalles (azul)
                    holder.btnAction.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.blue_primary));
                    holder.btnAction.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                }
                break;

            case ESTADO_COMPLETED:
                if (reservation.isReviewSubmitted()) {
                    // Botón para ver factura (gris)
                    holder.btnAction.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.text_secondary));
                    holder.btnAction.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                } else {
                    // Botón para valorar (amarillo)
                    holder.btnAction.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.rating_yellow));
                    holder.btnAction.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                }
                break;

            default:
                holder.btnAction.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.text_secondary));
                holder.btnAction.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                break;
        }

        // Click listener
        holder.btnAction.setOnClickListener(v -> {
            animateButtonClick(v, () -> {
                if (actionListener != null) {
                    actionListener.onActionButtonClicked(reservation, holder.getAdapterPosition());
                } else {
                    handleDefaultAction(reservation, position);
                }
            });
        });
    }

    private void setupContextualInfo(ReservationViewHolder holder, Reservation reservation) {
        if (holder.additionalInfoContainer == null) return;

        String contextualInfo = reservation.getContextualInfo();

        if (contextualInfo != null && !contextualInfo.isEmpty()) {
            holder.additionalInfoContainer.setVisibility(View.VISIBLE);

            if (holder.tvAdditionalInfoTitle != null) {
                holder.tvAdditionalInfoTitle.setText("Información adicional");
            }

            if (holder.tvAdditionalInfo != null) {
                holder.tvAdditionalInfo.setText(contextualInfo);
            }
        } else {
            holder.additionalInfoContainer.setVisibility(View.GONE);
        }
    }

    private void setupVisualIndicators(ReservationViewHolder holder, Reservation reservation) {
        // ✅ INDICADOR DE NÚMERO DE HABITACIÓN
        if (reservation.getRoomNumber() != null && !reservation.getRoomNumber().isEmpty()) {
            if (holder.tvRoomNumber != null) {
                holder.tvRoomNumber.setVisibility(View.VISIBLE);
                holder.tvRoomNumber.setText("Habitación " + reservation.getRoomNumber());
            }
        } else {
            if (holder.tvRoomNumber != null) {
                holder.tvRoomNumber.setVisibility(View.GONE);
            }
        }

        // ✅ INDICADOR DE SERVICIOS ADICIONALES
        if (reservation.getServices() != null && reservation.getServices().size() > 0) {
            if (holder.tvServicesIndicator != null) {
                holder.tvServicesIndicator.setVisibility(View.VISIBLE);
                String servicesText = "🍽️ " + reservation.getServices().size() + " servicios";
                if (reservation.getServicesTotal() > 0) {
                    DecimalFormat formatter = new DecimalFormat("0.00");
                    servicesText += " (+S/ " + formatter.format(reservation.getServicesTotal()) + ")";
                }
                holder.tvServicesIndicator.setText(servicesText);

                // Color según estado
                switch (reservation.getStatus()) {
                    case ESTADO_ACTIVE:
                        holder.tvServicesIndicator.setTextColor(ContextCompat.getColor(context, R.color.blue_primary));
                        break;
                    case ESTADO_COMPLETED:
                        holder.tvServicesIndicator.setTextColor(ContextCompat.getColor(context, R.color.success_green));
                        break;
                    default:
                        holder.tvServicesIndicator.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                        break;
                }
            }
        } else {
            if (holder.tvServicesIndicator != null) {
                holder.tvServicesIndicator.setVisibility(View.GONE);
            }
        }

        // ✅ INDICADOR DE TAXI GRATUITO - SOLO PARA ELEGIBLES
        if (reservation.isEligibleForFreeTaxi()) {
            if (holder.tvTaxiIndicator != null) {
                holder.tvTaxiIndicator.setVisibility(View.VISIBLE);

                String taxiText;
                switch (reservation.getStatus()) {
                    case ESTADO_UPCOMING:
                    case ESTADO_ACTIVE:
                        taxiText = "🚖 Taxi gratis incluido";
                        holder.tvTaxiIndicator.setTextColor(ContextCompat.getColor(context, R.color.success_green));
                        break;
                    case ESTADO_COMPLETED:
                        if (reservation.hasTaxiService()) {
                            taxiText = "🚖 Taxi disponible hoy";
                            holder.tvTaxiIndicator.setTextColor(ContextCompat.getColor(context, R.color.orange_primary));
                        } else {
                            taxiText = "🚖 Taxi gratis (expirado)";
                            holder.tvTaxiIndicator.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                        }
                        break;
                    default:
                        taxiText = "🚖 Taxi gratis incluido";
                        holder.tvTaxiIndicator.setTextColor(ContextCompat.getColor(context, R.color.success_green));
                        break;
                }
                holder.tvTaxiIndicator.setText(taxiText);
            }
        } else {
            if (holder.tvTaxiIndicator != null) {
                holder.tvTaxiIndicator.setVisibility(View.GONE);
            }
        }

        // ✅ INDICADOR DE CARGOS ADICIONALES - SOLO PARA ACTIVAS Y COMPLETADAS
        if (reservation.getAdditionalCharges() > 0) {
            if (holder.tvAdditionalChargesIndicator != null) {
                holder.tvAdditionalChargesIndicator.setVisibility(View.VISIBLE);
                DecimalFormat formatter = new DecimalFormat("0.00");
                holder.tvAdditionalChargesIndicator.setText("⚠️ Cargos adicionales: +S/ " + formatter.format(reservation.getAdditionalCharges()));
                holder.tvAdditionalChargesIndicator.setTextColor(ContextCompat.getColor(context, R.color.warning_red));
            }
        } else {
            if (holder.tvAdditionalChargesIndicator != null) {
                holder.tvAdditionalChargesIndicator.setVisibility(View.GONE);
            }
        }
    }

    private void setupClickListeners(ReservationViewHolder holder, Reservation reservation, int position) {
        // Click en toda la tarjeta
        holder.cardReservation.setOnClickListener(v -> {
            animateCardClick(v, () -> {
                if (actionListener != null) {
                    actionListener.onReservationCardClicked(reservation, holder.getAdapterPosition());
                } else {
                    // Fallback: comportamiento por defecto
                    Toast.makeText(context, "Ver detalles: " + reservation.getHotelName(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void handleDefaultAction(Reservation reservation, int position) {
        switch (reservation.getStatus()) {
            case ESTADO_UPCOMING:
                String upcomingMessage = reservation.canModify() ?
                        "Modificar: " + reservation.getHotelName() :
                        "Ver detalles: " + reservation.getHotelName();
                Toast.makeText(context, upcomingMessage, Toast.LENGTH_SHORT).show();
                break;

            case ESTADO_ACTIVE:
                String activeMessage;
                if (reservation.canRequestCheckout()) {
                    activeMessage = "Solicitar checkout: " + reservation.getHotelName();
                } else if (reservation.isCheckoutPending()) {
                    activeMessage = "Ver estado del checkout: " + reservation.getHotelName();
                } else {
                    activeMessage = "Ver detalles: " + reservation.getHotelName();
                }
                Toast.makeText(context, activeMessage, Toast.LENGTH_SHORT).show();
                break;

            case ESTADO_COMPLETED:
                String completedMessage = reservation.isReviewSubmitted() ?
                        "Ver factura: " + reservation.getHotelName() :
                        "Valorar estadía: " + reservation.getHotelName();
                Toast.makeText(context, completedMessage, Toast.LENGTH_SHORT).show();
                break;

            default:
                Toast.makeText(context, "Ver detalles: " + reservation.getHotelName(), Toast.LENGTH_SHORT).show();
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
        return reservations != null ? reservations.size() : 0;
    }

    // ✅ MÉTODO PARA ACTUALIZAR DATOS ESPECÍFICOS
    public void updateReservation(int position, Reservation updatedReservation) {
        if (position >= 0 && position < getItemCount()) {
            reservations.set(position, updatedReservation);
            notifyItemChanged(position);
        }
    }

    // ✅ MÉTODO PARA OBTENER RESERVA EN POSICIÓN ESPECÍFICA
    public Reservation getReservation(int position) {
        if (position >= 0 && position < getItemCount()) {
            return reservations.get(position);
        }
        return null;
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

            // ✅ INICIALIZACIÓN BÁSICA
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvPriceLabel = itemView.findViewById(R.id.tvPriceLabel);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivHotelImage = itemView.findViewById(R.id.ivHotelImage);
            btnAction = itemView.findViewById(R.id.btnAction);

            // ✅ INFORMACIÓN ADICIONAL
            additionalInfoContainer = itemView.findViewById(R.id.additionalInfoContainer);
            tvAdditionalInfoTitle = itemView.findViewById(R.id.tvAdditionalInfoTitle);
            tvAdditionalInfo = itemView.findViewById(R.id.tvAdditionalInfo);

            // ✅ INDICADORES ESPECÍFICOS (pueden no existir en todos los layouts)
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
            tvServicesIndicator = itemView.findViewById(R.id.tvServicesIndicator);
            tvTaxiIndicator = itemView.findViewById(R.id.tvTaxiIndicator);
            tvAdditionalChargesIndicator = itemView.findViewById(R.id.tvAdditionalChargesIndicator);

            // ✅ FALLBACK: Si no existen algunos indicadores en el layout, crear dinámicamente
            ensureIndicatorsExist();
        }

        // ✅ ASEGURAR QUE EXISTAN INDICADORES MÍNIMOS
        private void ensureIndicatorsExist() {
            // Si no existe tvStatus, podemos usar tvPriceLabel como alternativa
            if (tvStatus == null && tvPriceLabel != null) {
                // Usar tvPriceLabel como indicador de estado secundario si es necesario
                // Esta es una medida de respaldo
            }

            // Log para debug (opcional)
            android.util.Log.d("ReservationAdapter", "Indicadores disponibles: " +
                    "Status=" + (tvStatus != null) +
                    ", Room=" + (tvRoomNumber != null) +
                    ", Services=" + (tvServicesIndicator != null) +
                    ", Taxi=" + (tvTaxiIndicator != null) +
                    ", Charges=" + (tvAdditionalChargesIndicator != null));
        }
    }
}