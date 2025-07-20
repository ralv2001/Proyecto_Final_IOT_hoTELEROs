package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.RoomType;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoomTypeAdapter extends RecyclerView.Adapter<RoomTypeAdapter.RoomViewHolder> {

    private static final String TAG = "RoomTypeAdapter";

    public interface OnRoomActionListener {
        void onEditRoom(RoomType roomType, int position);
        void onDeleteRoom(RoomType roomType, int position);
    }

    // ✅ NUEVA interfaz para clicks en fotos
    public interface OnPhotoClickListener {
        void onPhotoClick(String photoUrl, int position, List<String> allPhotos);
    }

    private List<RoomType> roomTypes;
    private OnRoomActionListener editListener;
    private OnRoomActionListener deleteListener;
    private NumberFormat currencyFormat;
    private Context context; // ✅ NUEVO para manejo de fotos

    public RoomTypeAdapter(List<RoomType> roomTypes, OnRoomActionListener editListener, OnRoomActionListener deleteListener) {
        this.roomTypes = roomTypes;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext(); // ✅ Obtener contexto
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
        // Views principales
        private TextView tvRoomName;
        private TextView tvRoomArea;
        private TextView tvRoomPrice;
        private TextView tvRoomCapacity;
        private TextView tvAvailableRooms;
        private ImageView ivEdit;
        private ImageView ivDelete;
        private RecyclerView rvServices;

        // ✅ NUEVAS views para fotos
        private LinearLayout photosSection;
        private RecyclerView rvRoomPhotos;
        private TextView tvPhotoCount;
        private TextView tvServiceCount;

        // Adapters
        private ServiceChipAdapter servicesAdapter;
        private RoomPhotosAdapter photosAdapter; // ✅ NUEVO adapter para fotos

        RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            initializeViews();
        }

        private void initializeViews() {
            // Views principales
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvRoomArea = itemView.findViewById(R.id.tvRoomArea);
            tvRoomPrice = itemView.findViewById(R.id.tvRoomPrice);
            tvRoomCapacity = itemView.findViewById(R.id.tvRoomCapacity);
            tvAvailableRooms = itemView.findViewById(R.id.tvAvailableRooms);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            rvServices = itemView.findViewById(R.id.rvServices);

            // ✅ NUEVAS views para fotos
            photosSection = itemView.findViewById(R.id.photosSection);
            rvRoomPhotos = itemView.findViewById(R.id.rvRoomPhotos);
            tvPhotoCount = itemView.findViewById(R.id.tvPhotoCount);
            tvServiceCount = itemView.findViewById(R.id.tvServiceCount);
        }

        void bind(RoomType roomType, int position) {
            Log.d(TAG, "🏨 Binding habitación: " + roomType.getName() + " con " +
                    (roomType.getPhotoUrls() != null ? roomType.getPhotoUrls().size() : 0) + " fotos");

            // Información básica
            tvRoomName.setText(roomType.getName());
            tvRoomArea.setText(String.format("%.0f m²", roomType.getArea()));
            tvRoomPrice.setText(currencyFormat.format(roomType.getPricePerNight()));
            tvRoomCapacity.setText(roomType.getCapacity() + " personas");
            tvAvailableRooms.setText(roomType.getAvailableRooms() + " disponibles");

            // ✅ Configurar sección de fotos
            setupPhotosSection(roomType);

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

        // ✅ NUEVO método para configurar sección de fotos
        private void setupPhotosSection(RoomType roomType) {
            if (roomType.getPhotoUrls() == null || roomType.getPhotoUrls().isEmpty()) {
                photosSection.setVisibility(View.GONE);
                return;
            }

            photosSection.setVisibility(View.VISIBLE);

            // Actualizar contador de fotos
            int photoCount = roomType.getPhotoUrls().size();
            tvPhotoCount.setText(photoCount + (photoCount == 1 ? " foto" : " fotos"));

            // Configurar adapter para fotos
            if (photosAdapter == null) {
                List<Object> photoObjects = new ArrayList<>();
                for (String url : roomType.getPhotoUrls()) {
                    photoObjects.add(url); // Convertir String a Object
                }

                photosAdapter = new RoomPhotosAdapter(context, photoObjects, new RoomPhotosAdapter.OnPhotoActionListener() {
                    @Override
                    public void onRemovePhoto(int position) {
                        // No permitir eliminar en vista de lista, solo en edición
                    }

                    @Override
                    public void onPhotoClick(String photoUrl, int position, List<String> allPhotos) {
                        Log.d(TAG, "📸 Click en foto: " + photoUrl);
                        // Implementar visor de fotos si es necesario
                        // Por ahora solo log
                    }
                }, false); // false = no modo edición

                rvRoomPhotos.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                rvRoomPhotos.setAdapter(photosAdapter);
            } else {
                // Actualizar fotos existentes
                List<Object> photoObjects = new ArrayList<>();
                for (String url : roomType.getPhotoUrls()) {
                    photoObjects.add(url);
                }
                photosAdapter.updatePhotos(photoObjects);
            }

            Log.d(TAG, "📷 Sección de fotos configurada: " + photoCount + " fotos");
        }

        private void setupServicesRecyclerView(RoomType roomType) {
            if (roomType.getIncludedServices() == null || roomType.getIncludedServices().isEmpty()) {
                rvServices.setVisibility(View.GONE);
                // Mostrar mensaje de "sin servicios" si se desea
                return;
            }

            rvServices.setVisibility(View.VISIBLE);

            // ✅ Actualizar contador de servicios
            int serviceCount = roomType.getIncludedServices().size();
            if (tvServiceCount != null) {
                tvServiceCount.setText(serviceCount + (serviceCount == 1 ? " servicio" : " servicios"));
            }

            // Configurar adapter para chips de servicios
            if (servicesAdapter == null) {
                servicesAdapter = new ServiceChipAdapter(roomType.getIncludedServices());
                rvServices.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                rvServices.setAdapter(servicesAdapter);
            } else {
                servicesAdapter.updateServices(roomType.getIncludedServices());
            }

            Log.d(TAG, "🛎️ Servicios configurados: " + serviceCount + " servicios");
        }
    }

    // ========== MÉTODOS PÚBLICOS ==========

    public void updateRooms(List<RoomType> newRooms) {
        this.roomTypes.clear();
        this.roomTypes.addAll(newRooms);
        notifyDataSetChanged();
        Log.d(TAG, "🔄 Habitaciones actualizadas: " + newRooms.size());
    }

    public void addRoom(RoomType roomType) {
        this.roomTypes.add(roomType);
        notifyItemInserted(roomTypes.size() - 1);
        Log.d(TAG, "➕ Habitación añadida: " + roomType.getName());
    }

    public void updateRoom(int position, RoomType roomType) {
        if (position >= 0 && position < roomTypes.size()) {
            roomTypes.set(position, roomType);
            notifyItemChanged(position);
            Log.d(TAG, "🔄 Habitación actualizada en posición " + position + ": " + roomType.getName());
        }
    }

    public void removeRoom(int position) {
        if (position >= 0 && position < roomTypes.size()) {
            RoomType removedRoom = roomTypes.remove(position);
            notifyItemRemoved(position);
            Log.d(TAG, "🗑️ Habitación eliminada: " + (removedRoom != null ? removedRoom.getName() : "Desconocida"));
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