package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.ReportItem;

import java.util.List;

public class ReportDetailAdapter extends RecyclerView.Adapter<ReportDetailAdapter.ReportViewHolder> {

    private List<ReportItem> reportItems;

    public ReportDetailAdapter(List<ReportItem> reportItems) {
        this.reportItems = reportItems;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_report_detail, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportItem item = reportItems.get(position);
        holder.bind(item, position + 1);
    }

    @Override
    public int getItemCount() {
        return reportItems.size();
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPosition;
        private TextView tvTitle;
        private TextView tvValue;
        private TextView tvSubtitle;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvValue = itemView.findViewById(R.id.tvValue);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
        }

        public void bind(ReportItem item, int position) {
            tvPosition.setText(String.valueOf(position));
            tvTitle.setText(item.getTitle());
            tvValue.setText(item.getValue());
            tvSubtitle.setText(item.getSubtitle());
        }
    }
}