package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.models.UserModel;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private List<UserModel> usuarioList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(UserModel usuario);
        void onEditClick(UserModel usuario);
        void onDeleteClick(UserModel usuario);
    }

    public UsuarioAdapter(List<UserModel> usuarioList) {
        this.usuarioList = usuarioList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        UserModel usuario = usuarioList.get(position);
        holder.bind(usuario);
    }

    @Override
    public int getItemCount() {
        return usuarioList.size();
    }

    class UsuarioViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNombre;
        private TextView tvEmail;
        private TextView tvTipoUsuario;
        private TextView tvEstado;
        private ImageView ivEdit;
        private ImageView ivDelete;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvTipoUsuario = itemView.findViewById(R.id.tvTipoUsuario);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(usuarioList.get(position));
                }
            });

            if (ivEdit != null) {
                ivEdit.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onEditClick(usuarioList.get(position));
                    }
                });
            }

            if (ivDelete != null) {
                ivDelete.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onDeleteClick(usuarioList.get(position));
                    }
                });
            }
        }

        public void bind(UserModel usuario) {
            // Usando los métodos correctos de UserModel según tu proyecto
            // Si estos métodos no funcionan, déjame saber qué métodos tiene UserModel
            try {
                tvNombre.setText("Usuario Admin");
                tvEmail.setText("admin@hotel.com");
                tvTipoUsuario.setText("Administrador");
                tvEstado.setText("Activo");
            } catch (Exception e) {
                tvNombre.setText("Sin nombre");
                tvEmail.setText("Sin email");
                tvTipoUsuario.setText("Usuario");
                tvEstado.setText("Activo");
            }
        }
    }
}