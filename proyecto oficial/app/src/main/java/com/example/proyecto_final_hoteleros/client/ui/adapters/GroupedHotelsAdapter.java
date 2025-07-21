package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.Hotel;
import com.example.proyecto_final_hoteleros.client.data.model.CityHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class GroupedHotelsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "GroupedHotelsAdapter";
    private static final int TYPE_CITY_HEADER = 0;
    private static final int TYPE_HOTEL_ITEM = 1;

    private Context context;
    private List<Object> originalItems;
    private List<Object> currentItems;
    private OnHotelClickListener hotelClickListener;

    // ✅ ESTRUCTURA para manejar expansión de headers
    private Map<String, Boolean> cityExpansionState = new HashMap<>();
    private Map<String, List<Hotel>> cityHotelsMap = new HashMap<>();

    public interface OnHotelClickListener {
        void onHotelClick(Hotel hotel, int position);
    }

    public GroupedHotelsAdapter(Context context, List<Object> items) {
        this.context = context;
        this.originalItems = new ArrayList<>(items);
        this.currentItems = new ArrayList<>();
        initializeData();
        rebuildCurrentItems();
    }

    // ✅ INICIALIZAR estructura de datos para expansión
    private void initializeData() {
        cityExpansionState.clear();
        cityHotelsMap.clear();

        String currentCity = null;
        List<Hotel> currentCityHotels = new ArrayList<>();

        for (Object item : originalItems) {
            if (item instanceof CityHeader) {
                // Si había una ciudad anterior, guardar sus hoteles
                if (currentCity != null && !currentCityHotels.isEmpty()) {
                    cityHotelsMap.put(currentCity, new ArrayList<>(currentCityHotels));
                    cityExpansionState.put(currentCity, true); // Por defecto expandido
                }

                // Nueva ciudad
                CityHeader header = (CityHeader) item;
                currentCity = header.getCityName();
                currentCityHotels = new ArrayList<>();

                Log.d(TAG, "🏙️ Inicializando ciudad: " + currentCity);

            } else if (item instanceof Hotel && currentCity != null) {
                // Agregar hotel a la ciudad actual
                currentCityHotels.add((Hotel) item);
            }
        }

        // No olvidar la última ciudad
        if (currentCity != null && !currentCityHotels.isEmpty()) {
            cityHotelsMap.put(currentCity, new ArrayList<>(currentCityHotels));
            cityExpansionState.put(currentCity, true);
        }

        Log.d(TAG, "✅ Inicialización completada. Ciudades: " + cityHotelsMap.size());
    }

    // ✅ RECONSTRUIR lista actual basada en estados de expansión
    private void rebuildCurrentItems() {
        currentItems.clear();

        for (Object item : originalItems) {
            if (item instanceof CityHeader) {
                CityHeader header = (CityHeader) item;
                String cityName = header.getCityName();

                // Siempre agregar el header
                currentItems.add(header);

                // Agregar hoteles solo si está expandido
                Boolean isExpanded = cityExpansionState.get(cityName);
                if (isExpanded != null && isExpanded) {
                    List<Hotel> cityHotels = cityHotelsMap.get(cityName);
                    if (cityHotels != null) {
                        currentItems.addAll(cityHotels);
                    }
                }
            }
        }

        Log.d(TAG, "🔄 Lista reconstruida. Items actuales: " + currentItems.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= 0 && position < currentItems.size()) {
            return currentItems.get(position) instanceof CityHeader ? TYPE_CITY_HEADER : TYPE_HOTEL_ITEM;
        }
        return TYPE_HOTEL_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_CITY_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.client_item_city_header, parent, false);
            return new CityHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.client_item_popular_hotel_card, parent, false);
            return new HotelViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position >= currentItems.size()) return;

        if (holder instanceof CityHeaderViewHolder) {
            CityHeader cityHeader = (CityHeader) currentItems.get(position);
            ((CityHeaderViewHolder) holder).bind(cityHeader);
        } else if (holder instanceof HotelViewHolder) {
            Hotel hotel = (Hotel) currentItems.get(position);
            ((HotelViewHolder) holder).bind(hotel, position);
        }
    }

    @Override
    public int getItemCount() {
        return currentItems.size();
    }

    // ✅ VIEWHOLDER para headers con funcionalidad de expansión
    class CityHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvCityName, tvHotelCount;
        ImageView ivCityIcon, ivExpandCollapse;

        CityHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tv_city_name);
            tvHotelCount = itemView.findViewById(R.id.tv_hotel_count);
            ivCityIcon = itemView.findViewById(R.id.iv_city_icon);
            ivExpandCollapse = itemView.findViewById(R.id.iv_expand_collapse);
        }

        void bind(CityHeader cityHeader) {
            String cityName = cityHeader.getCityName();
            tvCityName.setText(cityName);

            // Mostrar información de hoteles
            List<Hotel> cityHotels = cityHotelsMap.get(cityName);
            int hotelCount = cityHotels != null ? cityHotels.size() : 0;
            String subtitle = cityHeader.getSubtitle();
            if (subtitle == null || subtitle.isEmpty()) {
                subtitle = hotelCount + " hoteles disponibles";
            }
            tvHotelCount.setText(subtitle);

            // Estado de expansión
            Boolean isExpanded = cityExpansionState.get(cityName);
            if (isExpanded == null) isExpanded = true;

            // ✅ ANIMACIÓN de rotación del ícono de expansión
            float targetRotation = isExpanded ? 180f : 0f;
            if (ivExpandCollapse != null) {
                ivExpandCollapse.animate()
                        .rotation(targetRotation)
                        .setDuration(200)
                        .start();
            }

            // ✅ CLICK LISTENER para expandir/colapsar
            itemView.setOnClickListener(v -> {
                Log.d(TAG, "🔄 Header clickeado: " + cityName);
                toggleCityExpansion(cityName);
            });

            Log.d(TAG, "🎭 Header vinculado - Ciudad: " + cityName + ", Expandido: " + isExpanded + ", Hoteles: " + hotelCount);
        }
    }

    // ✅ VIEWHOLDER para hoteles
    class HotelViewHolder extends RecyclerView.ViewHolder {
        TextView tvHotelName, tvLocation, tvPrice, tvRating;
        ImageView ivHotelImage;

        HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvRating = itemView.findViewById(R.id.tvRating);
            ivHotelImage = itemView.findViewById(R.id.ivHotelImage);
        }

        void bind(Hotel hotel, int position) {
            // Configurar datos del hotel
            tvHotelName.setText(hotel.getName());
            tvLocation.setText(hotel.getLocation());
            tvPrice.setText(hotel.getPrice());
            tvRating.setText(hotel.getRating());

            // ✅ CARGAR IMAGEN REAL O PLACEHOLDER
            loadHotelImage(ivHotelImage, hotel.getImageUrl(), hotel.getName());

            // Click listener
            itemView.setOnClickListener(v -> {
                if (hotelClickListener != null) {
                    hotelClickListener.onHotelClick(hotel, position);
                }
            });
        }
    }

    // ✅ MÉTODO MEJORADO: Cargar imagen desde URL o usar placeholder
    private void loadHotelImage(ImageView imageView, String imageUrl, String hotelName) {
        if (imageUrl != null && imageUrl.startsWith("http")) {
            // ✅ CARGAR IMAGEN REAL DESDE URL
            Log.d(TAG, "📸 Cargando imagen real para: " + hotelName);

            com.bumptech.glide.Glide.with(imageView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.hotel_placeholder) // Mientras carga
                    .error(R.drawable.hotel_placeholder)       // Si falla
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(imageView);
        } else {
            // ✅ USAR IMAGEN PLACEHOLDER O DRAWABLE
            Log.d(TAG, "🖼️ Usando placeholder para: " + hotelName);

            if (imageUrl != null && !imageUrl.equals("hotel_placeholder")) {
                // Intentar cargar como recurso drawable
                int resID = context.getResources().getIdentifier(
                        imageUrl, "drawable", context.getPackageName()
                );

                if (resID != 0) {
                    imageView.setImageResource(resID);
                } else {
                    imageView.setImageResource(R.drawable.hotel_placeholder);
                }
            } else {
                imageView.setImageResource(R.drawable.hotel_placeholder);
            }
        }
    }

    // ✅ MÉTODO para alternar expansión de ciudad con animaciones
    private void toggleCityExpansion(String cityName) {
        Boolean currentState = cityExpansionState.get(cityName);
        if (currentState == null) return;

        boolean newState = !currentState;
        cityExpansionState.put(cityName, newState);

        Log.d(TAG, "🔄 Alternando expansión - Ciudad: " + cityName + ", Nuevo estado: " + newState);

        // Encontrar posición del header
        int headerPosition = -1;
        for (int i = 0; i < currentItems.size(); i++) {
            Object item = currentItems.get(i);
            if (item instanceof CityHeader) {
                CityHeader header = (CityHeader) item;
                if (header.getCityName().equals(cityName)) {
                    headerPosition = i;
                    break;
                }
            }
        }

        if (headerPosition == -1) {
            Log.w(TAG, "❌ No se encontró header para ciudad: " + cityName);
            return;
        }

        List<Hotel> cityHotels = cityHotelsMap.get(cityName);
        if (cityHotels == null || cityHotels.isEmpty()) {
            Log.w(TAG, "❌ No hay hoteles para ciudad: " + cityName);
            return;
        }

        if (newState) {
            // EXPANDIR: insertar hoteles después del header
            Log.d(TAG, "➕ Expandiendo - Insertando " + cityHotels.size() + " hoteles");
            for (int i = 0; i < cityHotels.size(); i++) {
                currentItems.add(headerPosition + 1 + i, cityHotels.get(i));
                notifyItemInserted(headerPosition + 1 + i);
            }
        } else {
            // COLAPSAR: remover hoteles
            Log.d(TAG, "➖ Colapsando - Removiendo " + cityHotels.size() + " hoteles");
            for (int i = cityHotels.size() - 1; i >= 0; i--) {
                currentItems.remove(headerPosition + 1);
                notifyItemRemoved(headerPosition + 1);
            }
        }

        // Actualizar header (para animación del ícono)
        notifyItemChanged(headerPosition);

        Log.d(TAG, "✅ Expansión completada. Items actuales: " + currentItems.size());
    }

    // ✅ MÉTODOS PÚBLICOS

    public void setOnHotelClickListener(OnHotelClickListener listener) {
        this.hotelClickListener = listener;
    }

    public void updateItems(List<Object> newItems) {
        Log.d(TAG, "🔄 Actualizando items. Nuevos: " + newItems.size());

        this.originalItems = new ArrayList<>(newItems);
        initializeData();
        rebuildCurrentItems();
        notifyDataSetChanged();

        Log.d(TAG, "✅ Items actualizados. Total actual: " + currentItems.size());
    }

    // ✅ MÉTODO para obtener hoteles visibles (para filtros)
    public List<Hotel> getVisibleHotels() {
        List<Hotel> visibleHotels = new ArrayList<>();
        for (Object item : currentItems) {
            if (item instanceof Hotel) {
                visibleHotels.add((Hotel) item);
            }
        }
        return visibleHotels;
    }

    // ✅ MÉTODO para expandir/colapsar todas las ciudades
    public void expandAllCities() {
        boolean hasChanges = false;
        for (String cityName : cityExpansionState.keySet()) {
            if (!cityExpansionState.get(cityName)) {
                cityExpansionState.put(cityName, true);
                hasChanges = true;
            }
        }

        if (hasChanges) {
            rebuildCurrentItems();
            notifyDataSetChanged();
        }
    }

    public void collapseAllCities() {
        boolean hasChanges = false;
        for (String cityName : cityExpansionState.keySet()) {
            if (cityExpansionState.get(cityName)) {
                cityExpansionState.put(cityName, false);
                hasChanges = true;
            }
        }

        if (hasChanges) {
            rebuildCurrentItems();
            notifyDataSetChanged();
        }
    }

    // ✅ MÉTODO para obtener estadísticas
    public int getTotalCities() {
        return cityHotelsMap.size();
    }

    public int getTotalHotels() {
        int total = 0;
        for (List<Hotel> hotels : cityHotelsMap.values()) {
            total += hotels.size();
        }
        return total;
    }

    public int getExpandedCities() {
        int expanded = 0;
        for (Boolean isExpanded : cityExpansionState.values()) {
            if (isExpanded) expanded++;
        }
        return expanded;
    }
}