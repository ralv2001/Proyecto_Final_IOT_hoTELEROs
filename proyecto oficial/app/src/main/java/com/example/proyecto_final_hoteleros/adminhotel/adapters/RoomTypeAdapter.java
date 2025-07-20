package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
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
        private TextView tvRoomArea;
        private TextView tvRoomPrice;
        private TextView tvRoomCapacity;
        private TextView tvAvailableRooms;
        private ImageView ivEdit;
        private ImageView ivDelete;
        private RecyclerView rvServices;

        // Adapter para servicios
        private ServiceChipAdapter servicesAdapter;

        RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvRoomArea = itemView.findViewById(R.id.tvRoomArea);
            tvRoomPrice = itemView.findViewById(R.id.tvRoomPrice);
            tvRoomCapacity = itemView.findViewById(R.id.tvRoomCapacity);
            tvAvailableRooms = itemView.findViewById(R.id.tvAvailableRooms);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            rvServices = itemView.findViewById(R.id.rvServices);
        }

        void bind(RoomType roomType, int position) {
            // Información básica
            tvRoomName.setText(roomType.getName());
            tvRoomArea.setText(String.format("%.0f m²", roomType.getArea()));
            tvRoomPrice.setText(currencyFormat.format(roomType.getPricePerNight()));
            tvRoomCapacity.setText(roomType.getCapacity() + " personas");
            tvAvailableRooms.setText(roomType.getAvailableRooms() + " disponibles");

            // Configurar RecyclerView de servicios
            setupServicesRecyclerView(roomType);

            // Click listeners
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

        private void setupServicesRecyclerView(RoomType roomType) {
            if (roomType.getIncludedServices() == null || roomType.getIncludedServices().isEmpty()) {
                rvServices.setVisibility(View.GONE);
                return;
            }

            rvServices.setVisibility(View.VISIBLE);

            // Configurar adapter para chips de servicios
            if (servicesAdapter == null) {
                servicesAdapter = new ServiceChipAdapter(roomType.getIncludedServices());
                rvServices.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                rvServices.setAdapter(servicesAdapter);
            } else {
                servicesAdapter.updateServices(roomType.getIncludedServices());
            }
        }
    }

    // ========== MÉTODOS PÚBLICOS ==========

    public void updateRooms(List<RoomType> newRooms) {
        this.roomTypes.clear();
        this.roomTypes.addAll(newRooms);
        notifyDataSetChanged();
    }

    public void addRoom(RoomType roomType) {
        this.roomTypes.add(roomType);
        notifyItemInserted(roomTypes.size() - 1);
    }

    public void updateRoom(int position, RoomType roomType) {
        if (position >= 0 && position < roomTypes.size()) {
            roomTypes.set(position, roomType);
            notifyItemChanged(position);
        }
    }

    public void removeRoom(int position) {
        if (position >= 0 && position < roomTypes.size()) {
            roomTypes.remove(position);
            notifyItemRemoved(position);
        }
    }

    // ========== ADAPTER PARA CHIPS DE SERVICIOS ==========

    private static class ServiceChipAdapter extends RecyclerView.Adapter<ServiceChipAdapter.ChipViewHolder> {

        private List<String> services;

        ServiceChipAdapter(List<String> services) {
            this.services = services;
        }

        @NonNull
        @Override
        public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_service_chip, parent, false);
            return new ChipViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChipViewHolder holder, int position) {
            String service = services.get(position);
            holder.bind(service);
        }

        @Override
        public int getItemCount() {
            return services.size();
        }

        void updateServices(List<String> newServices) {
            this.services = newServices;
            notifyDataSetChanged();
        }

        static class ChipViewHolder extends RecyclerView.ViewHolder {
            private TextView tvServiceName;

            ChipViewHolder(@NonNull View itemView) {
                super(itemView);
                tvServiceName = itemView.findViewById(R.id.tvServiceName);
            }

            void bind(String serviceName) {
                tvServiceName.setText(serviceName);
            }
        }
    }
}