package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.Servicio;

import java.util.ArrayList;
import java.util.List;

public class ServicioAdapter extends RecyclerView.Adapter<ServicioAdapter.ViewHolder> {

    private List<Servicio> servicios;
    private Context context;

    public ServicioAdapter(List<Servicio> servicios, Context context) {
        this.servicios = servicios;
        this.context = context;
    }

    public ServicioAdapter(ArrayList<String> servicios) {
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        ImageView ivImagen;
        Button btnEditar, btnBorrar;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreServicio);
            ivImagen = itemView.findViewById(R.id.ivImagenServicio);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnBorrar = itemView.findViewById(R.id.btnBorrar);
        }
    }

    @NonNull
    @Override
    public ServicioAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_hotel_item_servicio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicioAdapter.ViewHolder holder, int position) {
        Servicio servicio = servicios.get(position);
        holder.tvNombre.setText(servicio.getNombre());
        holder.ivImagen.setImageResource(servicio.getImagenResId());

        holder.btnEditar.setOnClickListener(v ->
                Toast.makeText(context, "Editar: " + servicio.getNombre(), Toast.LENGTH_SHORT).show()
        );

        holder.btnBorrar.setOnClickListener(v ->
                Toast.makeText(context, "Borrar: " + servicio.getNombre(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return servicios.size();
    }
}
