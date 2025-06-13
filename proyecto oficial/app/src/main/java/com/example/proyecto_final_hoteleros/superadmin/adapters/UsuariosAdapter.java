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

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.models.Usuario;

import java.util.List;

public class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder> {

    private List<Usuario> usuarios;
    private OnUsuarioActionListener actionListener;

    public interface OnUsuarioActionListener {
        void onUsuarioAction(Usuario usuario, String action);
    }

    public UsuariosAdapter(List<Usuario> usuarios, OnUsuarioActionListener actionListener) {
        this.usuarios = usuarios;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.superadmin_item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = usuarios.get(position);
        holder.bind(usuario, actionListener);
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    public void updateData(List<Usuario> newUsuarios) {
        this.usuarios.clear();
        this.usuarios.addAll(newUsuarios);
        notifyDataSetChanged();
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        private CardView cardUsuario;
        private ImageView ivProfile, ivMore;
        private TextView tvName, tvEmail, tvUserType, tvRegistrationDate, tvUserTypeIcon, tvStatus;
        private MaterialButton btnToggleStatus;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            cardUsuario = itemView.findViewById(R.id.card_usuario);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            ivMore = itemView.findViewById(R.id.iv_more);
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvUserType = itemView.findViewById(R.id.tv_user_type);
            tvRegistrationDate = itemView.findViewById(R.id.tv_registration_date);
            tvUserTypeIcon = itemView.findViewById(R.id.tv_user_type_icon);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnToggleStatus = itemView.findViewById(R.id.btn_toggle_status);
        }

        public void bind(Usuario usuario, OnUsuarioActionListener actionListener) {
            // Configurar datos básicos
            tvName.setText(usuario.getName());
            tvEmail.setText(usuario.getEmail());
            tvUserType.setText(usuario.getUserTypeText());
            tvRegistrationDate.setText("Registrado: " + usuario.getRegistrationDate());
            tvUserTypeIcon.setText(usuario.getUserTypeIcon());

            // Configurar estado
            tvStatus.setText(usuario.getStatusText());
            tvStatus.setTextColor(usuario.getStatusColor());

            // Configurar tipo de usuario con color
            tvUserType.setTextColor(usuario.getUserTypeColor());

            // Configurar botón de toggle
            btnToggleStatus.setText(usuario.isActive() ? "Desactivar" : "Activar");
            btnToggleStatus.setBackgroundColor(usuario.isActive() ?
                    android.graphics.Color.parseColor("#F44336") :
                    android.graphics.Color.parseColor("#4CAF50"));

            // Configurar imagen de perfil (placeholder por ahora)
            ivProfile.setImageResource(R.drawable.ic_person);

            // Click listeners
            cardUsuario.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onUsuarioAction(usuario, "view_details");
                }
            });

            btnToggleStatus.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onUsuarioAction(usuario, "toggle_status");
                }
            });

            ivMore.setOnClickListener(v -> {
                showMoreOptions(v, usuario, actionListener);
            });
        }

        private void showMoreOptions(View anchor, Usuario usuario, OnUsuarioActionListener actionListener) {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(anchor.getContext(), anchor);
            popup.inflate(R.menu.menu_usuario_options);

            popup.setOnMenuItemClickListener(item -> {
                if (actionListener != null) {
                    if (item.getItemId() == R.id.action_edit) {
                        actionListener.onUsuarioAction(usuario, "edit");
                        return true;
                    } else if (item.getItemId() == R.id.action_view_activity) {
                        actionListener.onUsuarioAction(usuario, "view_activity");
                        return true;
                    } else if (item.getItemId() == R.id.action_reset_password) {
                        actionListener.onUsuarioAction(usuario, "reset_password");
                        return true;
                    }
                }
                return false;
            });

            popup.show();
        }
    }
}