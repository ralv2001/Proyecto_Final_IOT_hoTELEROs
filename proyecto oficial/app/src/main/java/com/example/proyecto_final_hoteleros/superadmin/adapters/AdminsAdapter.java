package com.example.proyecto_final_hoteleros.superadmin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.models.AdminUser;

import java.util.List;

public class AdminsAdapter extends RecyclerView.Adapter<AdminsAdapter.AdminViewHolder> {

    private List<AdminUser> admins;
    private OnAdminActionListener actionListener;

    public interface OnAdminActionListener {
        void onAdminAction(AdminUser admin, String action);
    }

    public AdminsAdapter(List<AdminUser> admins, OnAdminActionListener actionListener) {
        this.admins = admins;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.superadmin_item_admin_user, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        AdminUser admin = admins.get(position);
        holder.bind(admin, actionListener);
    }

    @Override
    public int getItemCount() {
        return admins.size();
    }

    public void updateData(List<AdminUser> newAdmins) {
        this.admins.clear();
        this.admins.addAll(newAdmins);
        notifyDataSetChanged();
    }

    static class AdminViewHolder extends RecyclerView.ViewHolder {
        private CardView cardAdmin;
        private ImageView ivProfile, ivMore;
        private TextView tvName, tvEmail, tvAdminType, tvRegistrationDate, tvStatus; // ✅ CAMBIADO
        private MaterialButton btnToggleStatus;

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            cardAdmin = itemView.findViewById(R.id.card_admin);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            ivMore = itemView.findViewById(R.id.iv_more);
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvAdminType = itemView.findViewById(R.id.tv_admin_type); // ✅ CAMBIADO
            tvRegistrationDate = itemView.findViewById(R.id.tv_registration_date);
            tvStatus = itemView.findViewById(R.id.tv_status); // ✅ AGREGADO
            btnToggleStatus = itemView.findViewById(R.id.btn_toggle_status);
        }

        public void bind(AdminUser admin, OnAdminActionListener actionListener) {
            // Configurar datos básicos
            tvName.setText(admin.getName());
            tvEmail.setText(admin.getEmail());
            tvAdminType.setText("Admin Hotel"); // Texto fijo
            tvRegistrationDate.setText("Registrado: " + admin.getRegistrationDate());

            // ✅ CONFIGURAR ESTADO - Igual que usuarios
            tvStatus.setText(admin.getStatusText());
            tvStatus.setTextColor(admin.getStatusColor());

            // ✅ CONFIGURAR TIPO DE ADMIN CON COLOR
            tvAdminType.setTextColor(android.graphics.Color.parseColor("#FF9800")); // Naranja para admins

            // ✅ CONFIGURAR BOTÓN DE TOGGLE - Igual que usuarios
            btnToggleStatus.setText(admin.isActive() ? "DESACTIVAR" : "ACTIVAR");

            if (admin.isActive()) {
                // Botón rojo para desactivar
                btnToggleStatus.setBackgroundColor(android.graphics.Color.parseColor("#F44336"));
                btnToggleStatus.setTextColor(android.graphics.Color.WHITE);
            } else {
                // Botón verde para activar
                btnToggleStatus.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"));
                btnToggleStatus.setTextColor(android.graphics.Color.WHITE);
            }

            // Configurar imagen de perfil
            ivProfile.setImageResource(R.drawable.ic_person);

            // ✅ CLICK LISTENERS
            cardAdmin.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onAdminAction(admin, "view_info");
                }
            });

            btnToggleStatus.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onAdminAction(admin, "toggle_status");
                }
            });

            ivMore.setOnClickListener(v -> {
                showMoreOptions(v, admin, actionListener);
            });
        }

        private void showMoreOptions(View anchor, AdminUser admin, OnAdminActionListener actionListener) {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(anchor.getContext(), anchor);

            // ✅ SOLO UNA OPCIÓN: Ver información
            android.view.Menu menu = popup.getMenu();
            menu.add(0, 1001, 0, "Ver información")
                    .setIcon(R.drawable.ic_info);

            popup.setOnMenuItemClickListener(item -> {
                if (actionListener != null && item.getItemId() == 1001) {
                    actionListener.onAdminAction(admin, "view_info");
                    return true;
                }
                return false;
            });

            popup.show();
        }
    }
}