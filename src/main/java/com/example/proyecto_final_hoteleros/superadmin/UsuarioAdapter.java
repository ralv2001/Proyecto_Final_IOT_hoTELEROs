package com.example.proyecto_final_hoteleros.superadmin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.Usuario;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private List<Usuario> usuarioList;

    // Constructor del adaptador
    public UsuarioAdapter(List<Usuario> usuarioList) {
        this.usuarioList = usuarioList;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = usuarioList.get(position);
        holder.nombreTextView.setText(usuario.getNombres());
        holder.emailTextView.setText(usuario.getEmail());
    }

    @Override
    public int getItemCount() {
        return usuarioList.size();
    }

    // ViewHolder para los elementos de la lista
    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView nombreTextView;
        TextView emailTextView;

        public UsuarioViewHolder(View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.nombre_usuario);
            emailTextView = itemView.findViewById(R.id.email_usuario);
        }
    }
}
