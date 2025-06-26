package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.UserReport;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class UserReportAdapter extends RecyclerView.Adapter<UserReportAdapter.UserReportViewHolder> {

    private List<UserReport> userReports;
    private NumberFormat currencyFormat;

    public UserReportAdapter(List<UserReport> userReports) {
        this.userReports = userReports;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
    }

    @NonNull
    @Override
    public UserReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_user_report, parent, false);
        return new UserReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserReportViewHolder holder, int position) {
        UserReport user = userReports.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userReports.size();
    }

    class UserReportViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName, tvUserEmail, tvReservations, tvTotalSpent, tvCategory;
        private ImageView ivStatus;
        private View statusIndicator;

        public UserReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvReservations = itemView.findViewById(R.id.tvReservations);
            tvTotalSpent = itemView.findViewById(R.id.tvTotalSpent);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }

        public void bind(UserReport user) {
            tvUserName.setText(user.getName());
            tvUserEmail.setText(user.getEmail());
            tvReservations.setText(user.getTotalReservations() + " reservas");
            tvTotalSpent.setText(currencyFormat.format(user.getTotalSpent()));
            tvCategory.setText(user.getCategory());

            // Status indicator
            if (user.isActive()) {
                statusIndicator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.green));
                ivStatus.setImageResource(R.drawable.ic_check_circle);
                ivStatus.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.green));
            } else {
                statusIndicator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.red));
                ivStatus.setImageResource(R.drawable.ic_cancel);
                ivStatus.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.red));
            }

            // Category styling
            int categoryColor;
            switch (user.getCategory()) {
                case "VIP":
                    categoryColor = R.color.gold;
                    break;
                case "Premium":
                    categoryColor = R.color.orange;
                    break;
                default:
                    categoryColor = R.color.blue;
                    break;
            }
            tvCategory.setTextColor(ContextCompat.getColor(itemView.getContext(), categoryColor));
        }
    }
}