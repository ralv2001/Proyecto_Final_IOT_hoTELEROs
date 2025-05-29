package com.example.proyecto_final_hoteleros.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.Huesped;

import java.util.List;

public class HuespedAdapter extends RecyclerView.Adapter<HuespedAdapter.HuespedViewHolder> {

    private List<Huesped> listaHuespedes;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Huesped huesped);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public HuespedAdapter(List<Huesped> listaHuespedes) {
        this.listaHuespedes = listaHuespedes;
    }

    @NonNull
    @Override
    public HuespedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_huesped, parent, false);
        return new HuespedViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull HuespedViewHolder holder, int position) {
        Huesped h = listaHuespedes.get(position);
        holder.tvNombre.setText(h.getNombre());
        holder.tvCheckIn.setText("Check-in: " + h.getCheckIn());
    }

    @Override
    public int getItemCount() {
        return listaHuespedes.size();
    }

    public class HuespedViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCheckIn;

        public HuespedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvCheckIn = itemView.findViewById(R.id.tvCheckIn);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(listaHuespedes.get(getAdapterPosition()));
                }
            });
        }
    }
}
