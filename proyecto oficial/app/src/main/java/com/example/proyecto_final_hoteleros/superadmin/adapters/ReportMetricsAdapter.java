package com.example.proyecto_final_hoteleros.superadmin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.models.ReportMetric;

import java.util.List;

public class ReportMetricsAdapter extends RecyclerView.Adapter<ReportMetricsAdapter.MetricViewHolder> {

    private List<ReportMetric> metrics;

    public ReportMetricsAdapter(List<ReportMetric> metrics) {
        this.metrics = metrics;
    }

    @NonNull
    @Override
    public MetricViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report_metric, parent, false);
        return new MetricViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MetricViewHolder holder, int position) {
        ReportMetric metric = metrics.get(position);
        holder.bind(metric);
    }

    @Override
    public int getItemCount() {
        return metrics.size();
    }

    public void updateData(List<ReportMetric> newMetrics) {
        this.metrics.clear();
        this.metrics.addAll(newMetrics);
        notifyDataSetChanged();
    }

    static class MetricViewHolder extends RecyclerView.ViewHolder {
        private CardView cardMetric;
        private ImageView ivIcon;
        private TextView tvTitle, tvValue, tvChange;

        public MetricViewHolder(@NonNull View itemView) {
            super(itemView);
            cardMetric = itemView.findViewById(R.id.card_metric);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvValue = itemView.findViewById(R.id.tv_value);
            tvChange = itemView.findViewById(R.id.tv_change);
        }

        public void bind(ReportMetric metric) {
            ivIcon.setImageResource(metric.getIconResId());
            tvTitle.setText(metric.getTitle());
            tvValue.setText(metric.getValue());
            tvChange.setText(metric.getChange());
            tvChange.setTextColor(metric.getChangeColorInt());
        }
    }
}