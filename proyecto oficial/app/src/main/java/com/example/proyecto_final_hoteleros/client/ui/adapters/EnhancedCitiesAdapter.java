package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.NearbyPlace;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;

public class EnhancedCitiesAdapter extends RecyclerView.Adapter<EnhancedCitiesAdapter.ViewHolder> {
    // ✅ DECLARAR LA VARIABLE PLACES
    private List<NearbyPlace> places;
    private OnPlaceClickListener listener;
    private String apiKey;
    private LatLng hotelLocation;

    public interface OnPlaceClickListener {
        void onPlaceClick(NearbyPlace place, int position);
    }

    public EnhancedCitiesAdapter(List<NearbyPlace> places, String apiKey, LatLng hotelLocation) {
        this.places = places; // ✅ INICIALIZAR AQUÍ
        this.apiKey = apiKey;
        this.hotelLocation = hotelLocation;
    }

    public void setOnPlaceClickListener(OnPlaceClickListener listener) {
        this.listener = listener;
    }

    // ✅ MÉTODO updatePlaces CON LOGS
    public void updatePlaces(List<NearbyPlace> newPlaces) {
        Log.d("EnhancedCitiesAdapter", "📱 updatePlaces llamado con " + newPlaces.size() + " lugares");
        this.places = newPlaces;
        notifyDataSetChanged();
        Log.d("EnhancedCitiesAdapter", "📱 notifyDataSetChanged() ejecutado");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("EnhancedCitiesAdapter", "🏗️ onCreateViewHolder llamado");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.client_item_nearby_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("EnhancedCitiesAdapter", "🎨 Binding posición " + position);
        if (position < places.size()) {
            NearbyPlace place = places.get(position);
            Log.d("EnhancedCitiesAdapter", "🎨 Mostrando: " + place.getName());
            holder.bind(place);
        }
    }

    @Override
    public int getItemCount() {
        int count = places != null ? places.size() : 0;
        Log.d("EnhancedCitiesAdapter", "📊 getItemCount: " + count);
        return count;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCityImage;
        private TextView tvCityName;
        private TextView tvDistrict;
        private TextView tvDistanceMinutes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d("EnhancedCitiesAdapter", "🔍 ViewHolder creado");

            // BUSCAR LOS ELEMENTOS PERO NO FALLAR SI NO EXISTEN
            ivCityImage = itemView.findViewById(R.id.iv_city_image);
            tvCityName = itemView.findViewById(R.id.tv_city_name);
            tvDistrict = itemView.findViewById(R.id.tv_district);
            tvDistanceMinutes = itemView.findViewById(R.id.tv_distance_minutes);

            Log.d("EnhancedCitiesAdapter", "🔍 Elementos encontrados - Imagen: " + (ivCityImage != null) +
                    ", Nombre: " + (tvCityName != null) + ", Distrito: " + (tvDistrict != null) +
                    ", Distancia: " + (tvDistanceMinutes != null));
        }

        public void bind(NearbyPlace place) {
            Log.d("EnhancedCitiesAdapter", "🎭 Bind llamado para: " + place.getName());

            // VERIFICAR QUE LOS ELEMENTOS EXISTAN ANTES DE USARLOS
            if (tvCityName != null) {
                tvCityName.setText(place.getName());
                Log.d("EnhancedCitiesAdapter", "✅ Nombre asignado: " + place.getName());
            } else {
                Log.e("EnhancedCitiesAdapter", "❌ tvCityName es null");
            }

            if (tvDistrict != null) {
                String district = extractDistrict(place.getVicinity());
                tvDistrict.setText(district);
                Log.d("EnhancedCitiesAdapter", "✅ Distrito asignado: " + district);
            }

            if (tvDistanceMinutes != null) {
                String distanceText = calculateDistanceInMinutes(place);
                tvDistanceMinutes.setText(distanceText);
                Log.d("EnhancedCitiesAdapter", "✅ Distancia asignada: " + distanceText);
            }

            // Cargar imagen
            if (ivCityImage != null) {
                loadPlaceImage(place);
                Log.d("EnhancedCitiesAdapter", "✅ Imagen cargada");
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlaceClick(place, getAdapterPosition());
                }
            });
        }

        private String extractDistrict(String vicinity) {
            if (vicinity == null || vicinity.isEmpty()) {
                return "Lima";
            }

            String[] parts = vicinity.split(",");
            if (parts.length > 1) {
                return parts[parts.length - 1].trim();
            }

            String[] words = vicinity.trim().split(" ");
            if (words.length > 2) {
                return words[words.length - 1];
            }

            return vicinity;
        }

        private String calculateDistanceInMinutes(NearbyPlace place) {
            if (hotelLocation == null) {
                return "-- min";
            }

            try {
                double distanceKm = calculateDistance(
                        hotelLocation.latitude, hotelLocation.longitude,
                        place.getLatitude(), place.getLongitude()
                );

                double walkingSpeedKmH = 5.0;
                int minutes = (int) Math.ceil((distanceKm / walkingSpeedKmH) * 60);

                if (minutes < 1) {
                    return "1 min";
                } else if (minutes <= 60) {
                    return minutes + " min";
                } else {
                    int hours = minutes / 60;
                    int remainingMinutes = minutes % 60;
                    if (remainingMinutes == 0) {
                        return hours + "h";
                    } else {
                        return hours + "h " + remainingMinutes + "m";
                    }
                }
            } catch (Exception e) {
                return "-- min";
            }
        }

        private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
            final int R = 6371;
            double latDistance = Math.toRadians(lat2 - lat1);
            double lonDistance = Math.toRadians(lon2 - lon1);

            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            return R * c;
        }

        private void loadPlaceImage(NearbyPlace place) {
            if (ivCityImage == null) return;

            String imageUrl = place.getImageUrl(apiKey);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.lima)
                        .error(R.drawable.lima)
                        .into(ivCityImage);
            } else {
                ivCityImage.setImageResource(R.drawable.lima);
            }
        }
    }
}