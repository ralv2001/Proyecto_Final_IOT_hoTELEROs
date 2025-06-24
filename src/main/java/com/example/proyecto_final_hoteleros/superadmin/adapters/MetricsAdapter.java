package com.example.proyecto_final_hoteleros.superadmin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        public MetricViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_metric_icon);
            tvValue = itemView.findViewById(R.id.tv_metric_value);
            tvTitle = itemView.findViewById(R.id.tv_metric_title);
        }

        public void bind(MetricItem metric) {
            ivIcon.setImageResource(metric.getIconResId());
            tvValue.setText(metric.getValue());
            tvTitle.setText(metric.getTitle());
        }
    }
}
