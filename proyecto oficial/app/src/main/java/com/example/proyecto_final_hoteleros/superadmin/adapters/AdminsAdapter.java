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
        private TextView tvName, tvEmail, tvHotel, tvRegistrationDate;
        private Chip chipStatus;
        private MaterialButton btnToggleStatus;

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            cardAdmin = itemView.findViewById(R.id.card_admin);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            ivMore = itemView.findViewById(R.id.iv_more);
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvHotel = itemView.findViewById(R.id.tv_hotel);
            tvRegistrationDate = itemView.findViewById(R.id.tv_registration_date);
            chipStatus = itemView.findViewById(R.id.chip_status);
            btnToggleStatus = itemView.findViewById(R.id.btn_toggle_status);
        }

        public void bind(AdminUser admin, OnAdminActionListener actionListener) {
            // Configurar datos básicos
            tvName.setText(admin.getName());
            tvEmail.setText(admin.getEmail());
            tvHotel.setText(admin.getHotelName());
            tvRegistrationDate.setText("Registrado: " + admin.getRegistrationDate());

            // Configurar estado con TextView personalizado más visible
            if (admin.isActive()) {
                chipStatus.setText("✅ ACTIVO");
                chipStatus.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50")); // Verde
                chipStatus.setTextColor(android.graphics.Color.BLACK);
            } else {
                chipStatus.setText("❌ INACTIVO");
                chipStatus.setBackgroundColor(android.graphics.Color.parseColor("#F44336")); // Rojo
                chipStatus.setTextColor(android.graphics.Color.BLACK);
            }

            // Estilo mejorado
            chipStatus.setTextSize(11);
            chipStatus.setPadding(12, 6, 12, 6);
            chipStatus.setTypeface(null, android.graphics.Typeface.BOLD);

            // Esquinas redondeadas
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
                shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                shape.setCornerRadius(20f); // Esquinas redondeadas
                if (admin.isActive()) {
                    shape.setColor(android.graphics.Color.parseColor("#4CAF50"));
                } else {
                    shape.setColor(android.graphics.Color.parseColor("#F44336"));
                }
                chipStatus.setBackground(shape);
            }

            // Configurar botón de toggle con mejor UX
            if (admin.isActive()) {
                btnToggleStatus.setText("🔒 Desactivar");
                btnToggleStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#F44336"))); // Rojo
            } else {
                btnToggleStatus.setText("✅ Activar");
                btnToggleStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#4CAF50"))); // Verde
            }

            // Mejorar estilo del botón
            btnToggleStatus.setTextColor(android.graphics.Color.WHITE);
            btnToggleStatus.setAllCaps(false); // Texto normal, no mayúsculas

            // Configurar imagen de perfil (placeholder por ahora)
            ivProfile.setImageResource(R.drawable.ic_person);

            // Click listeners
            cardAdmin.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onAdminAction(admin, "view_details");
                }
            });

            cardAdmin.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        v.setAlpha(0.7f);
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        v.setAlpha(1.0f);
                        break;
                }
                return false;
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
            popup.inflate(R.menu.menu_admin_options);

            popup.setOnMenuItemClickListener(item -> {
                if (actionListener != null) {
                    if (item.getItemId() == R.id.action_edit) {
                        actionListener.onAdminAction(admin, "edit");
                        return true;
                    } else if (item.getItemId() == R.id.action_view_hotel) {
                        actionListener.onAdminAction(admin, "view_hotel");
                        return true;
                    } else if (item.getItemId() == R.id.action_reset_password) {
                        actionListener.onAdminAction(admin, "reset_password");
                        return true;
                    }
                }
                return false;
            });

            popup.show();
        }
    }
}