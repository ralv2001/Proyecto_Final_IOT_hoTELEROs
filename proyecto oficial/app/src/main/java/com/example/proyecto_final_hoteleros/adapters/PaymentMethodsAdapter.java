package com.example.proyecto_final_hoteleros.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.taxista.model.PaymentMethod;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PaymentMethodsAdapter extends RecyclerView.Adapter<PaymentMethodsAdapter.PaymentViewHolder> {

    private static final String TAG = "PaymentMethodsAdapter";
    private Context context;
    private List<PaymentMethod> paymentMethods;
    private PaymentMethodListener listener;

    public interface PaymentMethodListener {
        void onPaymentMethodClick(PaymentMethod paymentMethod);
        void onToggleEnabled(PaymentMethod paymentMethod, boolean enabled);
        void onSetAsDefault(PaymentMethod paymentMethod);
        void onDeletePaymentMethod(PaymentMethod paymentMethod);
    }

    public PaymentMethodsAdapter(Context context, List<PaymentMethod> paymentMethods, PaymentMethodListener listener) {
        this.context = context;
        this.paymentMethods = paymentMethods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment_method, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        try {
            PaymentMethod paymentMethod = paymentMethods.get(position);
            if (paymentMethod == null) return;

            // Información básica
            holder.tvPaymentName.setText(paymentMethod.getName());
            holder.tvPaymentDescription.setText(paymentMethod.getDescription());
            holder.ivPaymentIcon.setImageResource(paymentMethod.getIconResId());

            // Número de cuenta (si aplica)
            if (paymentMethod.getAccountNumber() != null && !paymentMethod.getAccountNumber().isEmpty()) {
                holder.tvAccountNumber.setVisibility(View.VISIBLE);
                holder.tvAccountNumber.setText(paymentMethod.getDisplayAccountNumber());
            } else {
                holder.tvAccountNumber.setVisibility(View.GONE);
            }

            // Banco (si aplica)
            if (paymentMethod.getBankName() != null && !paymentMethod.getBankName().isEmpty()) {
                holder.tvBankName.setVisibility(View.VISIBLE);
                holder.tvBankName.setText(paymentMethod.getBankName());
            } else {
                holder.tvBankName.setVisibility(View.GONE);
            }

            // Total recibido
            holder.tvTotalReceived.setText(paymentMethod.getFormattedTotalReceived());

            // Último uso
            if (paymentMethod.getLastUsed() != null && !paymentMethod.getLastUsed().isEmpty()) {
                holder.tvLastUsed.setVisibility(View.VISIBLE);
                holder.tvLastUsed.setText("Último uso: " + paymentMethod.getLastUsed());
            } else {
                holder.tvLastUsed.setVisibility(View.GONE);
            }

            // Estado habilitado/deshabilitado
            holder.switchEnabled.setChecked(paymentMethod.isEnabled());
            holder.switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggleEnabled(paymentMethod, isChecked);
                }
            });

            // Indicador de método por defecto
            if (paymentMethod.isDefault()) {
                holder.tvDefaultIndicator.setVisibility(View.VISIBLE);
                holder.btnSetDefault.setVisibility(View.GONE);
                holder.cardPaymentMethod.setStrokeColor(context.getResources().getColor(R.color.colorPrimary, null));
                holder.cardPaymentMethod.setStrokeWidth(3);
            } else {
                holder.tvDefaultIndicator.setVisibility(View.GONE);
                holder.btnSetDefault.setVisibility(View.VISIBLE);
                holder.cardPaymentMethod.setStrokeWidth(1);
                holder.cardPaymentMethod.setStrokeColor(context.getResources().getColor(R.color.light_gray, null));
            }

            // Configurar colores según el tipo de pago
            configurePaymentTypeColors(holder, paymentMethod);

            // Configurar listeners
            setupClickListeners(holder, paymentMethod);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configurePaymentTypeColors(PaymentViewHolder holder, PaymentMethod paymentMethod) {
        int backgroundColor, iconColor;

        switch (paymentMethod.getType()) {
            case CASH:
                backgroundColor = 0xFFF3E5F5; // Púrpura claro
                iconColor = 0xFF9C27B0; // Púrpura
                break;
            case CARD:
                backgroundColor = 0xFFE3F2FD; // Azul claro
                iconColor = 0xFF2196F3; // Azul
                break;
            case DIGITAL_WALLET:
                backgroundColor = 0xFFE8F5E8; // Verde claro
                iconColor = 0xFF4CAF50; // Verde
                break;
            case BANK_TRANSFER:
                backgroundColor = 0xFFFFF3E0; // Naranja claro
                iconColor = 0xFFFF9800; // Naranja
                break;
            default:
                backgroundColor = 0xFFF5F5F5; // Gris claro
                iconColor = 0xFF757575; // Gris
                break;
        }

        holder.cardIconBackground.setCardBackgroundColor(backgroundColor);
        holder.ivPaymentIcon.setColorFilter(iconColor);
    }

    private void setupClickListeners(PaymentViewHolder holder, PaymentMethod paymentMethod) {
        // Click en toda la tarjeta
        holder.cardPaymentMethod.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentMethodClick(paymentMethod);
            }
        });

        // Botón establecer como predeterminado
        holder.btnSetDefault.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSetAsDefault(paymentMethod);
            }
        });


        // Botón eliminar
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeletePaymentMethod(paymentMethod);
            }
        });
    }

    @Override
    public int getItemCount() {
        return paymentMethods != null ? paymentMethods.size() : 0;
    }

    public void updatePaymentMethods(List<PaymentMethod> newPaymentMethods) {
        this.paymentMethods = newPaymentMethods;
        notifyDataSetChanged();
    }

    public void updatePaymentMethod(PaymentMethod updatedPaymentMethod) {
        for (int i = 0; i < paymentMethods.size(); i++) {
            if (paymentMethods.get(i).getId().equals(updatedPaymentMethod.getId())) {
                paymentMethods.set(i, updatedPaymentMethod);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removePaymentMethod(String paymentMethodId) {
        for (int i = 0; i < paymentMethods.size(); i++) {
            if (paymentMethods.get(i).getId().equals(paymentMethodId)) {
                paymentMethods.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardPaymentMethod;
        MaterialCardView cardIconBackground;
        ImageView ivPaymentIcon;
        TextView tvPaymentName;
        TextView tvPaymentDescription;
        TextView tvAccountNumber;
        TextView tvBankName;
        TextView tvTotalReceived;
        TextView tvLastUsed;
        TextView tvDefaultIndicator;
        Switch switchEnabled;
        MaterialButton btnSetDefault;
        MaterialButton btnEdit;
        MaterialButton btnDelete;

        PaymentViewHolder(@NonNull View itemView) {
            super(itemView);

            cardPaymentMethod = itemView.findViewById(R.id.card_payment_method);
            cardIconBackground = itemView.findViewById(R.id.card_icon_background);
            ivPaymentIcon = itemView.findViewById(R.id.iv_payment_icon);
            tvPaymentName = itemView.findViewById(R.id.tv_payment_name);
            tvPaymentDescription = itemView.findViewById(R.id.tv_payment_description);
            tvAccountNumber = itemView.findViewById(R.id.tv_account_number);
            tvBankName = itemView.findViewById(R.id.tv_bank_name);
            tvTotalReceived = itemView.findViewById(R.id.tv_total_received);
            tvLastUsed = itemView.findViewById(R.id.tv_last_used);
            tvDefaultIndicator = itemView.findViewById(R.id.tv_default_indicator);
            switchEnabled = itemView.findViewById(R.id.switch_enabled);
            btnSetDefault = itemView.findViewById(R.id.btn_set_default);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}