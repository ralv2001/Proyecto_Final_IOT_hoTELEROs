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
import com.example.proyecto_final_hoteleros.adminhotel.model.CheckoutItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private List<CheckoutItem> checkoutList;
    private OnItemClickListener listener;
    private Context context;
    private NumberFormat currencyFormat;

    public interface OnItemClickListener {
        void onItemClick(CheckoutItem checkout);
        void onProcessClick(CheckoutItem checkout);
        void onViewDetailsClick(CheckoutItem checkout);
    }

    public CheckoutAdapter(List<CheckoutItem> checkoutList) {
        this.checkoutList = checkoutList;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
    }

    public CheckoutAdapter(Context context, List<CheckoutItem> checkoutList) {
        this.context = context;
        this.checkoutList = checkoutList;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.admin_hotel_item_checkout, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        CheckoutItem checkout = checkoutList.get(position);
        holder.bind(checkout);
    }

    @Override
    public int getItemCount() {
        return checkoutList.size();
    }

    class CheckoutViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNombreHuesped;
        private TextView tvHabitacion;
        private TextView tvFechas;
        private TextView tvEstado;
        private TextView tvTotalAmount;
        private TextView tvNoches;
        private TextView tvServiciosCount;
        private TextView tvDanosCount;
        private View btnProcessCheckout;
        private View btnViewDetails;
        private ImageView ivStatusIcon;
        private ImageView ivPriorityIndicator;
        private View statusBar;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);

            // Views básicos (siempre deben existir)
            tvNombreHuesped = itemView.findViewById(R.id.tvNombreHuesped);
            tvHabitacion = itemView.findViewById(R.id.tvHabitacion);
            tvFechas = itemView.findViewById(R.id.tvFechas);
            tvEstado = itemView.findViewById(R.id.tvEstado);

            // Views opcionales (pueden ser null)
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvNoches = itemView.findViewById(R.id.tvNoches);
            tvServiciosCount = itemView.findViewById(R.id.tvServiciosCount);
            tvDanosCount = itemView.findViewById(R.id.tvDanosCount);
            btnProcessCheckout = itemView.findViewById(R.id.btnProcessCheckout);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            ivStatusIcon = itemView.findViewById(R.id.ivStatusIcon);
            ivPriorityIndicator = itemView.findViewById(R.id.ivPriorityIndicator);
            statusBar = itemView.findViewById(R.id.statusBar);

            setupClickListeners();
        }

        private void setupClickListeners() {
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(checkoutList.get(position));
                }
            });

            if (btnProcessCheckout != null) {
                btnProcessCheckout.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onProcessClick(checkoutList.get(position));
                    }
                });
            }

            if (btnViewDetails != null) {
                btnViewDetails.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onViewDetailsClick(checkoutList.get(position));
                    }
                });
            }
        }

        public void bind(CheckoutItem checkout) {
            // Información básica
            tvNombreHuesped.setText(checkout.getNombreHuesped());
            tvHabitacion.setText("Habitación " + checkout.getNumeroHabitacion());
            tvFechas.setText(checkout.getFechaCheckIn() + " - " + checkout.getFechaCheckOut());

            // Información adicional si existe
            if (tvNoches != null) {
                tvNoches.setText(checkout.getNumeroNoches() + " noche" + (checkout.getNumeroNoches() != 1 ? "s" : ""));
            }

            if (tvServiciosCount != null) {
                int serviciosCount = checkout.getServiciosAdicionales().size();
                tvServiciosCount.setText(serviciosCount + " servicio" + (serviciosCount != 1 ? "s" : ""));
            }

            if (tvDanosCount != null) {
                int danosCount = checkout.getDanos().size();
                tvDanosCount.setText(danosCount + " daño" + (danosCount != 1 ? "s" : ""));
            }

            if (tvTotalAmount != null) {
                tvTotalAmount.setText(currencyFormat.format(checkout.getTotalGeneral()));
            }

            // Configurar estado
            configureStatus(checkout);

            // Configurar prioridad
            configurePriority(checkout);
        }

        private void configureStatus(CheckoutItem checkout) {
            String estado = checkout.getEstado();

            if (context == null) return;

            switch (estado) {
                case "Pendiente":
                    tvEstado.setText("Checkout Pendiente");
                    tvEstado.setTextColor(ContextCompat.getColor(context, R.color.orange));
                    if (statusBar != null) statusBar.setBackgroundColor(ContextCompat.getColor(context, R.color.orange));
                    if (ivStatusIcon != null) {
                        ivStatusIcon.setImageResource(R.drawable.ic_clock);
                        ivStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.orange));
                    }
                    if (btnProcessCheckout != null) btnProcessCheckout.setVisibility(View.VISIBLE);
                    break;

                case "En Proceso":
                    tvEstado.setText("Procesando Pago");
                    tvEstado.setTextColor(ContextCompat.getColor(context, R.color.blue));
                    if (statusBar != null) statusBar.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
                    if (ivStatusIcon != null) {
                        ivStatusIcon.setImageResource(R.drawable.ic_payment);
                        ivStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.blue));
                    }
                    if (btnProcessCheckout != null) btnProcessCheckout.setVisibility(View.VISIBLE);
                    break;

                case "Completado":
                    tvEstado.setText("Checkout Completado");
                    tvEstado.setTextColor(ContextCompat.getColor(context, R.color.green));
                    if (statusBar != null) statusBar.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
                    if (ivStatusIcon != null) {
                        ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
                        ivStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.green));
                    }
                    if (btnProcessCheckout != null) btnProcessCheckout.setVisibility(View.GONE);
                    break;

                default:
                    tvEstado.setText(estado);
                    tvEstado.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                    if (statusBar != null) statusBar.setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray));
                    if (ivStatusIcon != null) {
                        ivStatusIcon.setImageResource(R.drawable.ic_info);
                        ivStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary));
                    }
                    if (btnProcessCheckout != null) btnProcessCheckout.setVisibility(View.VISIBLE);
                    break;
            }
        }

        private void configurePriority(CheckoutItem checkout) {
            if (ivPriorityIndicator == null || context == null) return;

            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - checkout.getFechaCreacion();
            long hoursWaiting = timeDiff / (1000 * 60 * 60);

            if (hoursWaiting > 2) {
                ivPriorityIndicator.setVisibility(View.VISIBLE);
                ivPriorityIndicator.setColorFilter(ContextCompat.getColor(context, R.color.red));
            } else if (hoursWaiting > 1) {
                ivPriorityIndicator.setVisibility(View.VISIBLE);
                ivPriorityIndicator.setColorFilter(ContextCompat.getColor(context, R.color.orange));
            } else {
                ivPriorityIndicator.setVisibility(View.GONE);
            }
        }
    }

    public void updateItem(CheckoutItem updatedItem) {
        for (int i = 0; i < checkoutList.size(); i++) {
            if (checkoutList.get(i).getId().equals(updatedItem.getId())) {
                checkoutList.set(i, updatedItem);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeItem(CheckoutItem item) {
        int position = checkoutList.indexOf(item);
        if (position != -1) {
            checkoutList.remove(position);
            notifyItemRemoved(position);
        }
    }
}