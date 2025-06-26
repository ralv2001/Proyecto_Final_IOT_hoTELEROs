package com.example.proyecto_final_hoteleros.superadmin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.models.MetricItem;

import java.util.List;

public class MetricsAdapter extends RecyclerView.Adapter<MetricsAdapter.MetricViewHolder> {

    private List<MetricItem> metrics;

    public MetricsAdapter(List<MetricItem> metrics) {
        this.metrics = metrics;
    }

    @NonNull
    @Override
    public MetricViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.superadmin_item_metric_card, parent, false);
        return new MetricViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MetricViewHolder holder, int position) {
        MetricItem metric = metrics.get(position);
        holder.bind(metric);
    }

    @Override
    public int getItemCount() {
        return metrics.size();
    }

    public void updateData(List<MetricItem> newMetrics) {
        this.metrics.clear();
        this.metrics.addAll(newMetrics);
        notifyDataSetChanged();
    }

    static class MetricViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView tvValue, tvTitle;
        // 🎨 NUEVAS VARIABLES (las otras pueden existir pero no se usan)

        public MetricViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_metric_icon);
            tvValue = itemView.findViewById(R.id.tv_metric_value);
            tvTitle = itemView.findViewById(R.id.tv_metric_title);
            // Las referencias a gradient_container y tv_metric_change se obtienen dinámicamente
        }

        public void bind(MetricItem metric) {
            // 🎨 APLICAR GRADIENTE TEMÁTICO
            FrameLayout gradientContainer = itemView.findViewById(R.id.gradient_container);
            if (gradientContainer != null) {
                gradientContainer.setBackgroundResource(metric.getGradientDrawable());
            }

            // Configurar ícono y textos
            ivIcon.setImageResource(metric.getIconResId());
            tvValue.setText(metric.getValue());
            tvTitle.setText(metric.getTitle());

            // 🎨 MOSTRAR/OCULTAR INDICADOR DE CAMBIO
            TextView tvChange = itemView.findViewById(R.id.tv_metric_change);
            if (tvChange != null) {
                if (metric.hasChange()) {
                    tvChange.setText(metric.getChangeText());
                    tvChange.setVisibility(android.view.View.VISIBLE);
                } else {
                    tvChange.setVisibility(android.view.View.GONE);
                }
            }

            // ⚡ ANIMACIÓN DE ENTRADA ESPECTACULAR
            itemView.setAlpha(0f);
            itemView.setTranslationY(50f);
            itemView.setScaleX(0.8f);
            itemView.setScaleY(0.8f);

            // Delay basado en posición para efecto cascada
            int position = getAdapterPosition();
            long delay = position * 100L; // 100ms entre cada card

            itemView.animate()
                    .alpha(1.0f)
                    .translationY(0f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(400)
                    .setStartDelay(delay)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();

            // ⚡ EFECTO HOVER MEJORADO
            itemView.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        v.animate()
                                .scaleX(0.95f)
                                .scaleY(0.95f)
                                .setDuration(100)
                                .start();
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        v.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start();
                        break;
                }
                return false;
            });
        }
    }
}
