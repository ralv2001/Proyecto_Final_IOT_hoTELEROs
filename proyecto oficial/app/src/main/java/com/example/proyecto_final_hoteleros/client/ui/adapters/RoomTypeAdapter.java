package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.fragment.RoomSelectionFragment;
import com.example.proyecto_final_hoteleros.client.data.model.RoomType;

import java.util.List;

public class RoomTypeAdapter extends RecyclerView.Adapter<RoomTypeAdapter.RoomViewHolder> {

    private final List<RoomType> roomTypes;
    private final RoomSelectionFragment.OnRoomSelectedListener listener;
    private int selectedPosition = -1;

    public RoomTypeAdapter(List<RoomType> roomTypes, RoomSelectionFragment.OnRoomSelectedListener listener) {
        this.roomTypes = roomTypes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room_type, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        RoomType room = roomTypes.get(position);
        holder.tvRoomName.setText(room.getName());
        holder.tvRoomArea.setText(room.getArea() + " m²");
        holder.tvRoomPrice.setText(room.getPrice());
        holder.ivRoomImage.setImageResource(room.getImageResId());

        // Configurar el estado de selección
        if (position == selectedPosition) {
            holder.btnSelect.setBackgroundResource(R.drawable.bg_orange_button);
            holder.btnSelect.setText("Seleccionado");
            holder.btnSelect.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
        } else {
            holder.btnSelect.setBackgroundResource(R.drawable.bg_white_button);
            holder.btnSelect.setText("Seleccionar");
            holder.btnSelect.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorPrimary));
        }

        // Configurar el click en el botón
        holder.btnSelect.setOnClickListener(v -> {
            int oldSelectedPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldSelectedPosition);
            notifyItemChanged(selectedPosition);
            listener.onRoomSelected(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return roomTypes.size();
    }
    public List<RoomType> getRoomTypes() {
        return roomTypes;
    }
    public int getSelectedPosition() {
        return selectedPosition;
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRoomImage;
        TextView tvRoomName, tvRoomArea, tvRoomPrice;
        Button btnSelect;
        CardView cardRoom;

        RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRoomImage = itemView.findViewById(R.id.iv_room_image);
            tvRoomName = itemView.findViewById(R.id.tv_room_name);
            tvRoomArea = itemView.findViewById(R.id.tv_room_area);
            tvRoomPrice = itemView.findViewById(R.id.tv_room_price);
            btnSelect = itemView.findViewById(R.id.btn_select_room);
            cardRoom = itemView.findViewById(R.id.card_room);
        }
    }
}