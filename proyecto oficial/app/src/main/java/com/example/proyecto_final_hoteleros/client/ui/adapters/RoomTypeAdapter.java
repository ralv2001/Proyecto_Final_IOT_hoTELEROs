// client/ui/adapters/RoomTypeAdapter.java - SIMPLIFICADO: Lógica directa sin complicaciones
package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelProfile;
import com.example.proyecto_final_hoteleros.client.ui.fragment.RoomSelectionFragment;
import com.example.proyecto_final_hoteleros.client.data.model.RoomType;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomTypeAdapter extends RecyclerView.Adapter<RoomTypeAdapter.RoomViewHolder> {

    private static final String TAG = "RoomTypeAdapter";

    private final List<RoomType> roomTypes;
    private final RoomSelectionFragment.OnRoomSelectedListener listener;
    private final Context context;
    private int selectedPosition = -1;

    // ✅ SIMPLE: Solo necesitamos estos datos
    private HotelProfile currentHotel;
    private List<String> basicServicesNames = new ArrayList<>();  // Servicios básicos del hotel
    private Map<String, ServiceInfo> allServicesMap = new HashMap<>(); // ID -> Info completa
    private Map<String, ServiceInfo> servicesByName = new HashMap<>(); // ✅ NUEVO: Nombre -> Info completa

    // Clase simple para info de servicio
    private static class ServiceInfo {
        String name;
        String type;

        ServiceInfo(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    public RoomTypeAdapter(List<RoomType> roomTypes, RoomSelectionFragment.OnRoomSelectedListener listener,
                           Context context, HotelProfile currentHotel) {
        this.roomTypes = roomTypes;
        this.listener = listener;
        this.context = context;
        this.currentHotel = currentHotel;

        // ✅ SIMPLE: Cargar servicios directamente
        if (currentHotel != null && currentHotel.getHotelAdminId() != null) {
            loadHotelServices();
        }
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.client_item_room_type, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        RoomType room = roomTypes.get(position);

        // Datos básicos de la habitación
        holder.tvRoomName.setText(room.getName());
        holder.tvRoomArea.setText(room.getArea() + " m²");
        holder.tvRoomPrice.setText(room.getPrice());

        // Cargar foto real desde Firebase o usar fallback
        loadRoomImage(holder.ivRoomImage, room);

        // ✅ SIMPLE: Mostrar servicios
        showServices(holder, room);

        // Lógica de selección
        if (position == selectedPosition) {
            holder.btnSelect.setBackgroundResource(R.drawable.bg_orange_button);
            holder.btnSelect.setText("Seleccionado");
            holder.btnSelect.setTextColor(context.getResources().getColor(android.R.color.white));
        } else {
            holder.btnSelect.setBackgroundResource(R.drawable.bg_white_button);
            holder.btnSelect.setText("Seleccionar");
            holder.btnSelect.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }

        // Click listener
        holder.btnSelect.setOnClickListener(v -> {
            int oldSelectedPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldSelectedPosition);
            notifyItemChanged(selectedPosition);
            listener.onRoomSelected(selectedPosition);
        });
    }

    // ✅ SIMPLE: Una sola consulta para obtener todos los servicios del hotel
    private void loadHotelServices() {
        String hotelAdminId = currentHotel.getHotelAdminId();
        Log.d(TAG, "🔄 Cargando servicios del hotel: " + hotelAdminId);

        FirebaseFirestore.getInstance()
                .collection("hotel_services")
                .whereEqualTo("hotelAdminId", hotelAdminId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    basicServicesNames.clear();
                    allServicesMap.clear();
                    servicesByName.clear(); // ✅ NUEVO: Limpiar mapa por nombre

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String serviceId = doc.getId();
                        String serviceName = doc.getString("name");
                        String serviceType = doc.getString("serviceType");

                        Log.d(TAG, "   📄 Documento: ID='" + serviceId + "', Nombre='" + serviceName + "', Tipo='" + serviceType + "'");

                        if (serviceName != null && serviceType != null) {
                            ServiceInfo serviceInfo = new ServiceInfo(serviceName, serviceType);

                            // ✅ Guardar en AMBOS mapas
                            allServicesMap.put(serviceId, serviceInfo);
                            servicesByName.put(serviceName, serviceInfo); // ✅ NUEVO: Mapa por nombre

                            // ✅ Si es básico, agregarlo a la lista de básicos
                            if ("basic".equals(serviceType)) {
                                basicServicesNames.add(serviceName);
                                Log.d(TAG, "✅ Básico: " + serviceName + " (ID del doc: " + serviceId + ")");
                            } else {
                                Log.d(TAG, "   📝 Servicio tipo '" + serviceType + "': " + serviceName + " (ID: " + serviceId + ")");
                            }
                        } else {
                            Log.w(TAG, "   ⚠️ Servicio con datos incompletos: ID=" + serviceId + ", Nombre=" + serviceName + ", Tipo=" + serviceType);
                        }
                    }

                    Log.d(TAG, "✅ Servicios cargados - Básicos: " + basicServicesNames.size() +
                            ", Total: " + allServicesMap.size());

                    // ✅ NUEVO: Mostrar contenido completo del mapa por ID
                    Log.d(TAG, "📋 MAPA POR ID:");
                    for (Map.Entry<String, ServiceInfo> entry : allServicesMap.entrySet()) {
                        ServiceInfo info = entry.getValue();
                        Log.d(TAG, "   🔑 ID: '" + entry.getKey() + "' -> Nombre: '" + info.name + "', Tipo: '" + info.type + "'");
                    }

                    // ✅ NUEVO: Mostrar contenido completo del mapa por NOMBRE
                    Log.d(TAG, "📋 MAPA POR NOMBRE:");
                    for (Map.Entry<String, ServiceInfo> entry : servicesByName.entrySet()) {
                        ServiceInfo info = entry.getValue();
                        Log.d(TAG, "   🔑 Nombre: '" + entry.getKey() + "' -> Tipo: '" + info.type + "'");
                    }

                    // Actualizar vista
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error cargando servicios: " + e.getMessage());
                });
    }

    // ✅ SIMPLE: Mostrar servicios sin complicaciones
    private void showServices(RoomViewHolder holder, RoomType room) {
        TextView tvBasicos = holder.itemView.findViewById(R.id.tv_included_services_count);
        TextView tvIncluidos = holder.itemView.findViewById(R.id.tv_exclusive_services);

        // ✅ 1. SERVICIOS BÁSICOS - Siempre mostrar todos los del hotel
        if (tvBasicos != null) {
            if (!basicServicesNames.isEmpty()) {
                String basicText = "Servicios básicos: " + String.join(" • ", basicServicesNames);
                tvBasicos.setText(basicText);
                tvBasicos.setTextColor(ContextCompat.getColor(context, R.color.success_green));
                tvBasicos.setVisibility(View.VISIBLE);
                Log.d(TAG, "✅ Básicos mostrados para " + room.getName() + ": " + basicText);
            } else {
                tvBasicos.setVisibility(View.GONE);
            }
        }

        // ✅ 2. SERVICIOS INCLUIDOS - Solo los de tipo "included" asignados a esta habitación
        if (tvIncluidos != null) {
            List<String> includedServicesForRoom = getIncludedServicesForRoom(room);

            if (!includedServicesForRoom.isEmpty()) {
                String includedText = "Servicios incluidos: " + String.join(" • ", includedServicesForRoom);
                tvIncluidos.setText(includedText);
                tvIncluidos.setTextColor(ContextCompat.getColor(context, R.color.orange_primary));
                tvIncluidos.setVisibility(View.VISIBLE);
                Log.d(TAG, "🌟 Incluidos mostrados para " + room.getName() + ": " + includedText);
            } else {
                tvIncluidos.setVisibility(View.GONE);
                Log.d(TAG, "❌ No hay servicios incluidos para " + room.getName());
            }
        }
    }

    // ✅ CORREGIDO: Usar mapa por NOMBRE (las habitaciones guardan nombres como IDs)
    private List<String> getIncludedServicesForRoom(RoomType room) {
        List<String> result = new ArrayList<>();

        if (room.getIncludedServiceIds() == null || room.getIncludedServiceIds().isEmpty()) {
            Log.d(TAG, "❌ Habitación " + room.getName() + " sin servicios asignados");
            return result;
        }

        Log.d(TAG, "🔍 Analizando servicios para " + room.getName() + ":");
        Log.d(TAG, "   📋 IDs asignados: " + room.getIncludedServiceIds());

        // ✅ CORREGIDO: Buscar por NOMBRE en lugar de por ID de Firebase
        for (String serviceName : room.getIncludedServiceIds()) {
            ServiceInfo serviceInfo = servicesByName.get(serviceName); // ✅ USAR MAPA POR NOMBRE

            if (serviceInfo != null) {
                Log.d(TAG, "   🔍 " + serviceName + " -> " + serviceInfo.name + " (tipo: " + serviceInfo.type + ")");

                if ("included".equals(serviceInfo.type)) {
                    result.add(serviceInfo.name);
                    Log.d(TAG, "   ✅ INCLUIDO: " + serviceInfo.name);
                } else {
                    Log.d(TAG, "   ❌ NO incluido (tipo " + serviceInfo.type + "): " + serviceInfo.name);
                }
            } else {
                Log.w(TAG, "   ⚠️ Servicio '" + serviceName + "' NO encontrado en mapa por nombre");
            }
        }

        Log.d(TAG, "📊 Total servicios incluidos para " + room.getName() + ": " + result.size());
        if (!result.isEmpty()) {
            Log.d(TAG, "   📝 Lista: " + result);
        }

        return result;
    }

    // ✅ Cargar imagen (mantenido igual)
    private void loadRoomImage(ImageView imageView, RoomType room) {
        if (room.hasRealPhotos()) {
            String photoUrl = room.getFirstPhotoUrl();
            Glide.with(context)
                    .load(photoUrl)
                    .transform(new RoundedCorners(16))
                    .placeholder(room.getImageResource())
                    .error(room.getImageResource())
                    .into(imageView);

            // Mostrar indicador de múltiples fotos
            showMultiplePhotosIndicator(imageView, room.getPhotoCount());
        } else {
            imageView.setImageResource(room.getImageResource());
            hideMultiplePhotosIndicator(imageView);
        }
    }

    // ✅ Indicador de múltiples fotos (mantenido igual)
    private void showMultiplePhotosIndicator(ImageView imageView, int photoCount) {
        if (photoCount > 1) {
            ViewGroup parent = (ViewGroup) imageView.getParent();
            TextView indicator = parent.findViewWithTag("photo_indicator");

            if (indicator == null) {
                indicator = new TextView(context);
                indicator.setTag("photo_indicator");
                indicator.setText("📷 " + photoCount);
                indicator.setTextSize(10);
                indicator.setTextColor(context.getResources().getColor(android.R.color.white));
                indicator.setBackgroundColor(0x80000000);
                indicator.setPadding(8, 4, 8, 4);

                if (parent instanceof androidx.constraintlayout.widget.ConstraintLayout) {
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                            new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT);
                    params.topToTop = imageView.getId();
                    params.endToEnd = imageView.getId();
                    params.setMargins(0, 8, 8, 0);
                    indicator.setLayoutParams(params);
                    parent.addView(indicator);
                }
            } else {
                indicator.setText("📷 " + photoCount);
                indicator.setVisibility(View.VISIBLE);
            }
        }
    }

    private void hideMultiplePhotosIndicator(ImageView imageView) {
        ViewGroup parent = (ViewGroup) imageView.getParent();
        TextView indicator = parent.findViewWithTag("photo_indicator");
        if (indicator != null) {
            indicator.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return roomTypes.size();
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