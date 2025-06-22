package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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

    private static final int TYPE_CITY_HEADER = 0;
    private static final int TYPE_HOTEL_ITEM = 1;

    private Context context;
    private List<Object> originalItems;
    private List<Object> currentItems;
    private OnHotelClickListener hotelClickListener;

    // ✅ NUEVA estructura para manejar expansión
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

    // ✅ INICIALIZAR estructura de datos
    private void initializeData() {
        cityExpansionState.clear();
        cityHotelsMap.clear();

        String currentCity = null;
        List<Hotel> currentCityHotels = new ArrayList<>();

        for (Object item : originalItems) {
            if (item instanceof CityHeader) {
                // Guardar hoteles de ciudad anterior
                if (currentCity != null && !currentCityHotels.isEmpty()) {
                    cityHotelsMap.put(currentCity, new ArrayList<>(currentCityHotels));
                }

                // Nueva ciudad
                CityHeader header = (CityHeader) item;
                currentCity = header.getCityName();
                cityExpansionState.put(currentCity, true); // Por defecto expandido
                currentCityHotels.clear();

            } else if (item instanceof Hotel && currentCity != null) {
                currentCityHotels.add((Hotel) item);
            }
        }

        // Guardar última ciudad
        if (currentCity != null && !currentCityHotels.isEmpty()) {
            cityHotelsMap.put(currentCity, new ArrayList<>(currentCityHotels));
        }
    }

    // ✅ RECONSTRUIR lista actual según estados de expansión
    private void rebuildCurrentItems() {
        currentItems.clear();

        String currentCity = null;

        for (Object item : originalItems) {
            if (item instanceof CityHeader) {
                CityHeader header = (CityHeader) item;
                currentCity = header.getCityName();

                // Siempre agregar header
                currentItems.add(header);

                // Agregar hoteles solo si está expandido
                Boolean isExpanded = cityExpansionState.get(currentCity);
                if (isExpanded != null && isExpanded) {
                    List<Hotel> hotels = cityHotelsMap.get(currentCity);
                    if (hotels != null && !hotels.isEmpty()) {
                        currentItems.addAll(hotels);
                    }
                }
            }
        }
    }

    // ✅ TOGGLE mejorado con animación suave
    private void toggleCityExpansion(String cityName) {
        Boolean currentState = cityExpansionState.get(cityName);
        if (currentState == null) return;

        boolean newState = !currentState;
        cityExpansionState.put(cityName, newState);

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

        if (headerPosition == -1) return;

        List<Hotel> cityHotels = cityHotelsMap.get(cityName);
        if (cityHotels == null || cityHotels.isEmpty()) return;

        if (newState) {
            // EXPANDIR: insertar hoteles después del header
            for (int i = 0; i < cityHotels.size(); i++) {
                currentItems.add(headerPosition + 1 + i, cityHotels.get(i));
                notifyItemInserted(headerPosition + 1 + i);
            }
        } else {
            // COLAPSAR: remover hoteles
            for (int i = cityHotels.size() - 1; i >= 0; i--) {
                currentItems.remove(headerPosition + 1);
                notifyItemRemoved(headerPosition + 1);
            }
        }

        // Actualizar header
        notifyItemChanged(headerPosition);
    }

    public void setOnHotelClickListener(OnHotelClickListener listener) {
        this.hotelClickListener = listener;
    }

    public void updateItems(List<Object> newItems) {
        this.originalItems = new ArrayList<>(newItems);
        initializeData();
        rebuildCurrentItems();
        notifyDataSetChanged();
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

    // ✅ VIEWHOLDER para headers con animación igual a la de detalles
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
            tvHotelCount.setText(cityHeader.getSubtitle());

            // Estado de expansión
            Boolean isExpanded = cityExpansionState.get(cityName);
            if (isExpanded == null) isExpanded = true;

            // ✅ ANIMACIÓN IGUAL A LA DE DETALLES DE BÚSQUEDA
            float targetRotation = isExpanded ? 180f : 0f;
            ivExpandCollapse.setRotation(targetRotation);

            // Click con feedback visual
            itemView.setOnClickListener(v -> {
                // ✅ MISMA ANIMACIÓN QUE EN toggleDetailsVisibility()
                v.animate()
                        .scaleX(0.98f)
                        .scaleY(0.98f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start();
                        })
                        .start();

                // Animar rotación del chevron
                Boolean currentExpanded = cityExpansionState.get(cityName);
                float newRotation = (currentExpanded != null && currentExpanded) ? 0f : 180f;

                ivExpandCollapse.animate()
                        .rotation(newRotation)
                        .setDuration(250)
                        .start();

                // Toggle con delay para sincronizar con animación
                itemView.postDelayed(() -> toggleCityExpansion(cityName), 100);
            });
        }
    }

    // ✅ VIEWHOLDER para hoteles (sin cambios)
    class HotelViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHotelImage;
        TextView tvHotelName, tvLocation, tvPrice, tvRating;

        HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHotelImage = itemView.findViewById(R.id.ivHotelImage);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvRating = itemView.findViewById(R.id.tvRating);
        }

        void bind(Hotel hotel, int position) {
            tvHotelName.setText(hotel.getName());
            tvLocation.setText(hotel.getLocation());
            tvPrice.setText(hotel.getPrice());
            tvRating.setText(hotel.getRating());

            // Configurar imagen
            String imageName = hotel.getImageUrl();
            if (imageName.contains("/")) {
                imageName = imageName.substring(imageName.lastIndexOf("/") + 1);
            }
            int resID = context.getResources().getIdentifier(
                    imageName, "drawable", context.getPackageName()
            );
            if (resID != 0) {
                ivHotelImage.setImageResource(resID);
            } else {
                ivHotelImage.setImageResource(R.drawable.belmond);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .withEndAction(() -> {
                                        if (hotelClickListener != null) {
                                            hotelClickListener.onHotelClick(hotel, position);
                                        }
                                    })
                                    .start();
                        })
                        .start();
            });
        }
    }
}