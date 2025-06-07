package com.example.proyecto_final_hoteleros.client.utils;

import com.example.proyecto_final_hoteleros.client.data.model.Hotel;
import com.example.proyecto_final_hoteleros.client.data.model.HotelGroup;
import com.example.proyecto_final_hoteleros.client.data.model.CityHeader;

import java.util.*;

public class HotelGroupingUtils {

    public static List<Object> groupHotelsByCity(List<Hotel> allHotels) {
        List<Object> groupedItems = new ArrayList<>();

        // Mapa para agrupar hoteles por ciudad
        Map<String, List<Hotel>> cityGroups = new LinkedHashMap<>();

        // Ciudades principales en orden de prioridad
        String[] priorityCities = {"Lima", "Cusco", "Arequipa", "Piura", "Amazonas", "Madre de Dios", "Valle Sagrado"};

        // Inicializar grupos de ciudades prioritarias
        for (String city : priorityCities) {
            cityGroups.put(city, new ArrayList<>());
        }

        // Grupo para otras ciudades
        List<Hotel> otherCities = new ArrayList<>();

        // Clasificar hoteles
        for (Hotel hotel : allHotels) {
            String location = hotel.getLocation();
            boolean foundCity = false;

            for (String city : priorityCities) {
                if (location.toLowerCase().contains(city.toLowerCase())) {
                    cityGroups.get(city).add(hotel);
                    foundCity = true;
                    break;
                }
            }

            if (!foundCity) {
                otherCities.add(hotel);
            }
        }

        // Agregar grupos con hoteles al resultado final
        for (Map.Entry<String, List<Hotel>> entry : cityGroups.entrySet()) {
            List<Hotel> cityHotels = entry.getValue();
            if (!cityHotels.isEmpty()) {
                // Agregar header de ciudad
                groupedItems.add(new CityHeader(entry.getKey(), cityHotels.size()));
                // Agregar hoteles de la ciudad
                groupedItems.addAll(cityHotels);
            }
        }

        // Agregar otras ciudades si existen
        if (!otherCities.isEmpty()) {
            groupedItems.add(new CityHeader("Otros destinos", otherCities.size()));
            groupedItems.addAll(otherCities);
        }

        return groupedItems;
    }

    public static List<Hotel> getHotelsForCity(List<Hotel> allHotels, String cityName) {
        List<Hotel> cityHotels = new ArrayList<>();

        if (cityName == null || cityName.trim().isEmpty()) {
            return cityHotels;
        }

        String searchCity = cityName.toLowerCase().trim();

        for (Hotel hotel : allHotels) {
            String hotelLocation = hotel.getLocation().toLowerCase();
            String hotelName = hotel.getName().toLowerCase();

            // Buscar en ubicación y nombre
            if (hotelLocation.contains(searchCity) || hotelName.contains(searchCity)) {
                cityHotels.add(hotel);
            }
        }

        return cityHotels;
    }

    public static int getTotalHotelsCount(List<HotelGroup> groups) {
        int total = 0;
        for (HotelGroup group : groups) {
            total += group.getTotalHotels();
        }
        return total;
    }

    public static List<Hotel> filterHotelsByProximity(List<Hotel> hotels, String userLocation) {
        List<Hotel> nearbyHotels = new ArrayList<>();

        // ✅ MEJORADO: Filtro más inteligente
        String[] nearbyAreas = {"Lima", "Miraflores", "San Isidro", "Barranco", "Centro"};

        for (Hotel hotel : hotels) {
            String location = hotel.getLocation().toLowerCase();
            for (String area : nearbyAreas) {
                if (location.contains(area.toLowerCase())) {
                    nearbyHotels.add(hotel);
                    break;
                }
            }
        }

        // Si no hay hoteles cercanos, devolver los primeros 5
        if (nearbyHotels.isEmpty() && !hotels.isEmpty()) {
            int limit = Math.min(5, hotels.size());
            nearbyHotels.addAll(hotels.subList(0, limit));
        }

        return nearbyHotels;
    }

    public static List<Hotel> filterPopularHotels(List<Hotel> hotels) {
        List<Hotel> popularHotels = new ArrayList<>();

        // ✅ MEJORADO: Filtro más permisivo
        for (Hotel hotel : hotels) {
            try {
                float rating = Float.parseFloat(hotel.getRating());
                if (rating >= 4.5) {
                    popularHotels.add(hotel);
                }
            } catch (NumberFormatException e) {
                // Si no tiene rating válido, incluirlo si es un hotel conocido
                String name = hotel.getName().toLowerCase();
                if (name.contains("belmond") || name.contains("marriott") ||
                        name.contains("hilton") || name.contains("inkaterra")) {
                    popularHotels.add(hotel);
                }
            }
        }

        // Si hay pocos hoteles populares, agregar algunos con rating >= 4.0
        if (popularHotels.size() < 5) {
            for (Hotel hotel : hotels) {
                if (popularHotels.contains(hotel)) continue;

                try {
                    float rating = Float.parseFloat(hotel.getRating());
                    if (rating >= 4.0) {
                        popularHotels.add(hotel);
                        if (popularHotels.size() >= 8) break;
                    }
                } catch (NumberFormatException e) {
                    // Ignorar
                }
            }
        }

        return popularHotels;
    }

    public static List<Hotel> searchHotels(List<Hotel> hotels, String query) {
        List<Hotel> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(hotels);
        }

        String searchQuery = query.toLowerCase().trim();

        for (Hotel hotel : hotels) {
            String name = hotel.getName().toLowerCase();
            String location = hotel.getLocation().toLowerCase();

            if (name.contains(searchQuery) || location.contains(searchQuery)) {
                results.add(hotel);
            }
        }

        return results;
    }
}