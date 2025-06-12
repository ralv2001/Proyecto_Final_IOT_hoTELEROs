package com.example.proyecto_final_hoteleros.superadmin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.models.QuickAccessItem;

import java.util.List;

public class QuickAccessAdapter extends RecyclerView.Adapter<QuickAccessAdapter.QuickAccessViewHolder> {

    private List<QuickAccessItem> quickAccessItems;
    private OnQuickAccessClickListener clickListener;

    public interface OnQuickAccessClickListener {
        void onQuickAccessClick(QuickAccessItem item);
    }

    public QuickAccessAdapter(List<QuickAccessItem> quickAccessItems, OnQuickAccessClickListener clickListener) {
        this.quickAccessItems = quickAccessItems;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public QuickAccessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quick_access, parent, false);
        return new QuickAccessViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuickAccessViewHolder holder, int position) {
        QuickAccessItem item = quickAccessItems.get(position);
        holder.bind(item, clickListener);
    }

    @Override
    public int getItemCount() {
        return quickAccessItems.size();
    }

    public void updateData(List<QuickAccessItem> newItems) {
        this.quickAccessItems.clear();
        this.quickAccessItems.addAll(newItems);
        notifyDataSetChanged();
    }

    static class QuickAccessViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView tvTitle;

        public QuickAccessViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_quick_access_icon);
            tvTitle = itemView.findViewById(R.id.tv_quick_access_title);
        }

        public void bind(QuickAccessItem item, OnQuickAccessClickListener clickListener) {
            ivIcon.setImageResource(item.getIconResId());
            tvTitle.setText(item.getTitle());

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onQuickAccessClick(item);
                }
            });
        }
    }
}
