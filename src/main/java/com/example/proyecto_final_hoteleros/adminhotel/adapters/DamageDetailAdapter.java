package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.CheckoutItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DamageDetailAdapter extends RecyclerView.Adapter<DamageDetailAdapter.DamageViewHolder> {

    private Context context;
    private List<CheckoutItem.DanoHabitacion> damages;
    private NumberFormat currencyFormat;
    private OnDamageClickListener listener;

    public interface OnDamageClickListener {
        void onEditDamage(CheckoutItem.DanoHabitacion damage, int position);
        void onDeleteDamage(CheckoutItem.DanoHabitacion damage, int position);
    }

    public DamageDetailAdapter(Context context, List<CheckoutItem.DanoHabitacion> damages) {
        this.context = context;
        this.damages = damages;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
    }

    public void setOnDamageClickListener(OnDamageClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DamageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_hote_item_damage_detail, parent, false);
        return new DamageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DamageViewHolder holder, int position) {
        CheckoutItem.DanoHabitacion damage = damages.get(position);
        holder.bind(damage, position);
    }

    @Override
    public int getItemCount() {
        return damages.size();
    }

    class DamageViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivDamageIcon;
        private TextView tvDamageDescription;
        private TextView tvDamageDetails;
        private TextView tvDamageStatus;
        private TextView tvDamageCost;
        private ImageView ivEditDamage;

        public DamageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDamageIcon = itemView.findViewById(R.id.ivDamageIcon);
            tvDamageDescription = itemView.findViewById(R.id.tvDamageDescription);
            tvDamageDetails = itemView.findViewById(R.id.tvDamageDetails);
            tvDamageStatus = itemView.findViewById(R.id.tvDamageStatus);
            tvDamageCost = itemView.findViewById(R.id.tvDamageCost);
            ivEditDamage = itemView.findViewById(R.id.ivEditDamage);
        }

        public void bind(CheckoutItem.DanoHabitacion damage, int position) {
            tvDamageDescription.setText(damage.getDescripcion());
            tvDamageDetails.setText("Gravedad: " + damage.getGravedad() + " • Reportado: " + damage.getFecha());
            tvDamageCost.setText(currencyFormat.format(damage.getCosto()));

            if (damage.isConfirmado()) {
                tvDamageStatus.setText("✅ Confirmado por administración");
                tvDamageStatus.setTextColor(context.getResources().getColor(R.color.green));
            } else {
                tvDamageStatus.setText("⏳ Pendiente de confirmación");
                tvDamageStatus.setTextColor(context.getResources().getColor(R.color.orange));
            }

            ivEditDamage.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditDamage(damage, position);
                }
            });

            // Long click para eliminar
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteDamage(damage, position);
                }
                return true;
            });
        }
    }
}