// client/ui/adapters/RoomTypeAdapter.java - MODIFICACIÓN DEL ARCHIVO ORIGINAL
package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.fragment.RoomSelectionFragment;
import com.example.proyecto_final_hoteleros.client.data.model.RoomType;
import com.example.proyecto_final_hoteleros.client.data.repository.ServicesRepository;
import com.example.proyecto_final_hoteleros.client.data.model.HotelService;

import java.util.List;

public class RoomTypeAdapter extends RecyclerView.Adapter<RoomTypeAdapter.RoomViewHolder> {

    private final List<RoomType> roomTypes;
    private final RoomSelectionFragment.OnRoomSelectedListener listener;
    private int selectedPosition = -1;
    private ServicesRepository servicesRepository; // ✅ AGREGADO

    public RoomTypeAdapter(List<RoomType> roomTypes, RoomSelectionFragment.OnRoomSelectedListener listener) {
        this.roomTypes = roomTypes;
        this.listener = listener;
        this.servicesRepository = ServicesRepository.getInstance(); // ✅ AGREGADO
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.client_item_room_type, parent, false); // ✅ USA EL LAYOUT ORIGINAL
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        RoomType room = roomTypes.get(position);

        // TU CÓDIGO EXISTENTE se mantiene:
        holder.tvRoomName.setText(room.getName());
        holder.tvRoomArea.setText(room.getArea() + " m²");
        holder.tvRoomPrice.setText(room.getPrice());
        holder.ivRoomImage.setImageResource(room.getImageResId());

        // ✅ AGREGADO: Mostrar servicios incluidos específicos
        setupRoomServiceInfo(holder, room);

        // TU LÓGICA DE SELECCIÓN se mantiene igual:
        if (position == selectedPosition) {
            holder.btnSelect.setBackgroundResource(R.drawable.bg_orange_button);
            holder.btnSelect.setText("Seleccionado");
            holder.btnSelect.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
        } else {
            holder.btnSelect.setBackgroundResource(R.drawable.bg_white_button);
            holder.btnSelect.setText("Seleccionar");
            holder.btnSelect.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorPrimary));
        }

        // TU CLICK LISTENER se mantiene igual:
        holder.btnSelect.setOnClickListener(v -> {
            int oldSelectedPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldSelectedPosition);
            notifyItemChanged(selectedPosition);
            listener.onRoomSelected(selectedPosition);
        });
    }

    // ✅ MÉTODO AGREGADO: Configurar info de servicios
    private void setupRoomServiceInfo(RoomViewHolder holder, RoomType room) {
        // Buscar TextViews para servicios incluidos (ya existen en tu layout)
        TextView tvIncludedCount = holder.itemView.findViewById(R.id.tv_included_services_count);
        TextView tvExclusiveServices = holder.itemView.findViewById(R.id.tv_exclusive_services);

        // ✅ MOSTRAR servicios específicos en lugar de solo número
        if (tvIncludedCount != null && room != null) {
            List<String> includedIds = room.getIncludedServiceIds();

            // Crear texto con nombres de servicios
            StringBuilder servicesText = new StringBuilder("✓ Incluye: ");
            for (int i = 0; i < includedIds.size(); i++) {
                String serviceName = getServiceDisplayName(includedIds.get(i));
                servicesText.append(serviceName);
                if (i < includedIds.size() - 1) {
                    servicesText.append(" • ");
                }
            }

            tvIncludedCount.setText(servicesText.toString());
            tvIncludedCount.setVisibility(View.VISIBLE);
        }

        if (tvExclusiveServices != null && room != null) {
            List<String> exclusiveServices = room.getExclusiveServiceNames();
            if (!exclusiveServices.isEmpty()) {
                tvExclusiveServices.setText("+ " + String.join(" • ", exclusiveServices));
                tvExclusiveServices.setVisibility(View.VISIBLE);
            } else {
                tvExclusiveServices.setVisibility(View.GONE);
            }
        }
    }

    // ✅ MÉTODO AGREGADO: Obtener nombres de servicios
    private String getServiceDisplayName(String serviceId) {
        HotelService service = servicesRepository.getServiceById(serviceId);
        if (service != null) {
            return service.getName();
        }

        // Fallback por si no encuentra el servicio
        switch (serviceId) {
            case "wifi": return "WiFi";
            case "reception": return "Recepción 24h";
            case "pool": return "Piscina";
            case "parking": return "Estacionamiento";
            case "minibar": return "Minibar";
            case "room_service": return "Room Service";
            case "laundry": return "Lavandería";
            default: return "Servicio";
        }
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
        TextView tvIncludedServicesCount, tvExclusiveServices; // ✅ YA EXISTEN en tu código
        Button btnSelect;
        CardView cardRoom;

        RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            // ✅ TUS VIEWS EXISTENTES se mantienen
            ivRoomImage = itemView.findViewById(R.id.iv_room_image);
            tvRoomName = itemView.findViewById(R.id.tv_room_name);
            tvRoomArea = itemView.findViewById(R.id.tv_room_area);
            tvRoomPrice = itemView.findViewById(R.id.tv_room_price);
            btnSelect = itemView.findViewById(R.id.btn_select_room);
            cardRoom = itemView.findViewById(R.id.card_room);

            // ✅ ESTAS YA EXISTEN en tu código original
            tvIncludedServicesCount = itemView.findViewById(R.id.tv_included_services_count);
            tvExclusiveServices = itemView.findViewById(R.id.tv_exclusive_services);
        }
    }
}