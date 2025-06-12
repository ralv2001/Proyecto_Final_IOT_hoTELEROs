package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.Huesped;

import java.util.List;

public class HuespedAdapter extends RecyclerView.Adapter<HuespedAdapter.HuespedViewHolder> {

    private List<Huesped> huespedList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Huesped huesped);
    }

    public HuespedAdapter(List<Huesped> huespedList) {
        this.huespedList = huespedList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public HuespedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_huesped, parent, false);
        return new HuespedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HuespedViewHolder holder, int position) {
        Huesped huesped = huespedList.get(position);
        holder.bind(huesped);
    }

    @Override
    public int getItemCount() {
        return huespedList.size();
    }

    class HuespedViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNombre;
        private TextView tvFechaCheckIn;
        private TextView tvEstado;

        public HuespedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvFechaCheckIn = itemView.findViewById(R.id.tvFechaCheckIn);
            tvEstado = itemView.findViewById(R.id.tvEstado);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(huespedList.get(position));
                }
            });
        }

        public void bind(Huesped huesped) {
            tvNombre.setText(huesped.getNombre());
            tvFechaCheckIn.setText(huesped.getCheckIn());
            tvEstado.setText("Pendiente checkout");
        }
    }
}