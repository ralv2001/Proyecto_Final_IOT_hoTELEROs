package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.RoomType;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RoomTypeAdapter extends RecyclerView.Adapter<RoomTypeAdapter.RoomViewHolder> {

    public interface OnRoomActionListener {
        void onEditRoom(RoomType roomType, int position);
        void onDeleteRoom(RoomType roomType, int position);
    }

    private List<RoomType> roomTypes;
    private OnRoomActionListener editListener;
    private OnRoomActionListener deleteListener;
    private NumberFormat currencyFormat;

    public RoomTypeAdapter(List<RoomType> roomTypes, OnRoomActionListener editListener, OnRoomActionListener deleteListener) {
        this.roomTypes = roomTypes;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_room_type, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        RoomType roomType = roomTypes.get(position);
        holder.bind(roomType, position);
    }

    @Override
    public int getItemCount() {
        return roomTypes.size();
    }

    class RoomViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRoomName;
        private TextView tvRoomDescription;
        private TextView tvRoomArea;
        private TextView tvRoomPrice;
        private TextView tvAvailableRooms;
        private TextView tvServicesCount;
        private ImageView ivEdit;
        private ImageView ivDelete;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvRoomDescription = itemView.findViewById(R.id.tvRoomDescription);
            tvRoomArea = itemView.findViewById(R.id.tvRoomArea);
            tvRoomPrice = itemView.findViewById(R.id.tvRoomPrice);
            tvAvailableRooms = itemView.findViewById(R.id.tvAvailableRooms);
            tvServicesCount = itemView.findViewById(R.id.tvServicesCount);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }

        public void bind(RoomType roomType, int position) {
            tvRoomName.setText(roomType.getName());
            tvRoomDescription.setText(roomType.getDescription());
            tvRoomArea.setText(String.format("%.0f m²", roomType.getArea()));
            tvRoomPrice.setText(currencyFormat.format(roomType.getPricePerNight()) + "/noche");
            tvAvailableRooms.setText(roomType.getAvailableRooms() + " disponibles");
            tvServicesCount.setText(roomType.getIncludedServices().size() + " servicios incluidos");

            ivEdit.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onEditRoom(roomType, position);
                }
            });

            ivDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteRoom(roomType, position);
                }
            });
        }
    }
}