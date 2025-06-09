package com.example.proyecto_final_hoteleros.client.utils;

import android.content.Context;
import android.util.Log;

import com.example.proyecto_final_hoteleros.client.data.model.Hotel;
import com.example.proyecto_final_hoteleros.client.data.model.HotelGroup;
import com.example.proyecto_final_hoteleros.client.data.model.CityHeader;

import java.util.*;

public class HotelGroupingUtils {

    public static List<Object> groupHotelsByCity(List<Hotel> allHotels) {
        List<Object> groupedItems = new ArrayList<>();
        Map<String, List<Hotel>> cityGroups = new LinkedHashMap<>();

        String[] priorityCities = {"Lima", "Cusco", "Arequipa", "Piura", "Amazonas", "Madre de Dios", "Valle Sagrado"};

        for (String city : priorityCities) {
            cityGroups.put(city, new ArrayList<>());
        }

        List<Hotel> otherCities = new ArrayList<>();

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

        for (Map.Entry<String, List<Hotel>> entry : cityGroups.entrySet()) {
            List<Hotel> cityHotels = entry.getValue();
            if (!cityHotels.isEmpty()) {
                groupedItems.add(new CityHeader(entry.getKey(), cityHotels.size()));
                groupedItems.addAll(cityHotels);
            }
        }

        if (!otherCities.isEmpty()) {
            groupedItems.add(new CityHeader("Otros destinos", otherCities.size()));
            groupedItems.addAll(otherCities);
        }

        return groupedItems;
    }

    public static Map<String, List<Hotel>> groupHotelsByCityMap(List<Hotel> allHotels) {
        Map<String, List<Hotel>> cityGroups = new LinkedHashMap<>();
        String[] priorityCities = {"Lima", "Cusco", "Arequipa", "Piura", "Amazonas", "Madre de Dios", "Valle Sagrado"};

        for (String city : priorityCities) {
            cityGroups.put(city, new ArrayList<>());
        }

        List<Hotel> otherCities = new ArrayList<>();

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

        cityGroups.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        if (!otherCities.isEmpty()) {
            cityGroups.put("Otros destinos", otherCities);
        }

        return cityGroups;
    }

    public static List<Hotel> sortByDistance(List<Hotel> hotels, String referenceLocation) {
        return sortByDistance(hotels, referenceLocation, null);
    }

    public static List<Hotel> sortByDistance(List<Hotel> hotels, String referenceLocation, Context context) {
        List<Hotel> sortedHotels = new ArrayList<>(hotels);

        double userLat = -12.046374; // Lima por defecto
        double userLon = -77.042793;

        if (context != null) {
            UserLocationManager locationManager = UserLocationManager.getInstance(context);
            userLat = locationManager.getCurrentLatitude();
            userLon = locationManager.getCurrentLongitude();
        }

        final double finalUserLat = userLat;
        final double finalUserLon = userLon;

        sortedHotels.sort((h1, h2) -> {
            double distance1 = HotelDistanceCalculator.calculateDistanceToHotel(h1, finalUserLat, finalUserLon);
            double distance2 = HotelDistanceCalculator.calculateDistanceToHotel(h2, finalUserLat, finalUserLon);
            return Double.compare(distance1, distance2);
        });

        return sortedHotels;
    }

    public static List<Hotel> sortByPrice(List<Hotel> hotels, boolean ascending) {
        List<Hotel> sortedHotels = new ArrayList<>(hotels);

        sortedHotels.sort((h1, h2) -> {
            int price1 = extractPrice(h1.getPrice());
            int price2 = extractPrice(h2.getPrice());
            return ascending ? Integer.compare(price1, price2) : Integer.compare(price2, price1);
        });

        return sortedHotels;
    }

    public static List<Hotel> sortByRating(List<Hotel> hotels) {
        List<Hotel> sortedHotels = new ArrayList<>(hotels);

        sortedHotels.sort((h1, h2) -> {
            float rating1 = parseRating(h1.getRating());
            float rating2 = parseRating(h2.getRating());
            return Float.compare(rating2, rating1); // Descendente (mejor rating primero)
        });

        return sortedHotels;
    }

    private static int extractPrice(String priceText) {
        try {
            String cleanPrice = priceText.replace("S/", "").replace(",", "").trim();
            return Integer.parseInt(cleanPrice);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE; // Si no se puede parsear, va al final
        }
    }

    private static float parseRating(String ratingText) {
        try {
            return Float.parseFloat(ratingText);
        } catch (NumberFormatException e) {
            return 0.0f; // Si no se puede parsear, rating mínimo
        }
    }

    public static List<Hotel> getHotelsForCity(List<Hotel> allHotels, String cityName) {
        List<Hotel> cityHotels = new ArrayList<>();

        if (cityName == null || cityName.trim().isEmpty()) {
            return cityHotels;
        }

        String searchCity = cityName.toLowerCase().trim();
        Log.d("HotelGroupingUtils", "Buscando hoteles en ciudad: '" + searchCity + "'");

        for (Hotel hotel : allHotels) {
            String hotelLocation = hotel.getLocation().toLowerCase();
            String hotelCity = extractCityFromLocation(hotelLocation);

            // ✅ BÚSQUEDA MÁS PRECISA
            boolean matchesCity = false;

            // 1. Coincidencia exacta con ciudad extraída
            if (hotelCity != null && hotelCity.equals(searchCity)) {
                matchesCity = true;
            }
            // 2. La ubicación contiene la ciudad buscada
            else if (hotelLocation.contains(searchCity)) {
                matchesCity = true;
            }
            // 3. El nombre del hotel contiene la ciudad
            else if (hotel.getName().toLowerCase().contains(searchCity)) {
                matchesCity = true;
            }

            if (matchesCity) {
                cityHotels.add(hotel);
                Log.d("HotelGroupingUtils", "Hotel coincidente: " + hotel.getName() + " en " + hotel.getLocation());
            }
        }

        Log.d("HotelGroupingUtils", "Total hoteles encontrados para '" + searchCity + "': " + cityHotels.size());
        return cityHotels;
    }
    public static List<Hotel> getHotelsForCitySpecific(List<Hotel> allHotels, String searchLocation) {
        List<Hotel> results = new ArrayList<>();

        if (searchLocation == null || searchLocation.trim().isEmpty()) {
            return results;
        }

        String searchTerm = searchLocation.toLowerCase().trim();
        String cityFromLocation = extractCityFromLocation(searchTerm);

        // Primero agregar hoteles de la ciudad específica
        for (Hotel hotel : allHotels) {
            String hotelLocation = hotel.getLocation().toLowerCase();
            String hotelCity = extractCityFromLocation(hotelLocation);

            if (cityFromLocation != null && cityFromLocation.equals(hotelCity)) {
                results.add(hotel);
            }
        }

        // Si no hay suficientes hoteles, agregar otros que coincidan
        if (results.size() < 3) {
            for (Hotel hotel : allHotels) {
                if (!results.contains(hotel)) {
                    String hotelLocation = hotel.getLocation().toLowerCase();
                    String hotelName = hotel.getName().toLowerCase();

                    if (hotelLocation.contains(searchTerm) || hotelName.contains(searchTerm)) {
                        results.add(hotel);
                    }
                }
            }
        }

        return results;
    }

    public static String extractCityFromLocation(String location) {
        if (location == null) return null;

        String[] cities = {"lima", "cusco", "arequipa", "piura", "trujillo", "iquitos",
                "chiclayo", "huancayo", "tacna", "puno", "ayacucho", "cajamarca",
                "tumbes", "chachapoyas", "leymebamba", "tambopata", "puerto maldonado"};
        String locationLower = location.toLowerCase();

        // ✅ BÚSQUEDA MÁS ESPECÍFICA - priorizar coincidencias exactas
        for (String city : cities) {
            if (locationLower.contains(city)) {
                Log.d("HotelGroupingUtils", "Ciudad extraída: '" + city + "' de ubicación: '" + location + "'");
                return city;
            }
        }

        Log.d("HotelGroupingUtils", "No se pudo extraer ciudad de: '" + location + "'");
        return null;
    }

    public static int getTotalHotelsCount(List<HotelGroup> groups) {
        int total = 0;
        for (HotelGroup group : groups) {
            total += group.getTotalHotels();
        }
        return total;
    }

    public static List<Hotel> filterHotelsByProximity(List<Hotel> hotels, String userLocation) {
        return filterHotelsByProximity(hotels, userLocation, null);
    }

    public static List<Hotel> filterHotelsByProximity(List<Hotel> hotels, String userLocation, Context context) {
        List<Hotel> nearbyHotels = new ArrayList<>();

        double userLat = -12.046374; // Lima por defecto
        double userLon = -77.042793;

        if (context != null) {
            UserLocationManager locationManager = UserLocationManager.getInstance(context);
            userLat = locationManager.getCurrentLatitude();
            userLon = locationManager.getCurrentLongitude();
            userLocation = locationManager.getCurrentCity().toLowerCase();
        }

        // Filtrar por ciudad y cercanía
        for (Hotel hotel : hotels) {
            String hotelLocation = hotel.getLocation().toLowerCase();

            // Si está en la misma ciudad
            if (hotelLocation.contains(userLocation.toLowerCase())) {
                double distance = HotelDistanceCalculator.calculateDistanceToHotel(hotel, userLat, userLon);
                if (distance < 20.0) { // Menos de 20km
                    nearbyHotels.add(hotel);
                }
            }
        }

        // Si no hay suficientes hoteles cercanos, incluir más de la misma ciudad
        if (nearbyHotels.size() < 5) {
            for (Hotel hotel : hotels) {
                if (!nearbyHotels.contains(hotel)) {
                    String hotelLocation = hotel.getLocation().toLowerCase();
                    if (hotelLocation.contains(userLocation.toLowerCase())) {
                        nearbyHotels.add(hotel);
                        if (nearbyHotels.size() >= 8) break;
                    }
                }
            }
        }

        // Ordenar por distancia
        return sortByDistance(nearbyHotels, userLocation, context);
    }

    public static List<Hotel> filterPopularHotels(List<Hotel> hotels) {
        List<Hotel> popularHotels = new ArrayList<>();

        // Primero filtrar por rating alto
        for (Hotel hotel : hotels) {
            try {
                float rating = Float.parseFloat(hotel.getRating());
                if (rating >= 4.5) {
                    popularHotels.add(hotel);
                }
            } catch (NumberFormatException e) {
                // Ignorar hoteles sin rating válido
            }
        }

        // Si no hay suficientes, incluir hoteles de marcas reconocidas
        if (popularHotels.size() < 5) {
            String[] premiumBrands = {"belmond", "marriott", "hilton", "inkaterra",
                    "westin", "country club", "skylodge", "tambo"};

            for (Hotel hotel : hotels) {
                if (popularHotels.contains(hotel)) continue;

                String name = hotel.getName().toLowerCase();
                for (String brand : premiumBrands) {
                    if (name.contains(brand)) {
                        popularHotels.add(hotel);
                        break;
                    }
                }

                if (popularHotels.size() >= 10) break;
            }
        }

        // Si aún no hay suficientes, incluir hoteles con rating >= 4.0
        if (popularHotels.size() < 8) {
            for (Hotel hotel : hotels) {
                if (popularHotels.contains(hotel)) continue;

                try {
                    float rating = Float.parseFloat(hotel.getRating());
                    if (rating >= 4.0) {
                        popularHotels.add(hotel);
                        if (popularHotels.size() >= 12) break;
                    }
                } catch (NumberFormatException e) {
                    // Ignorar
                }
            }
        }

        // Ordenar por rating
        return sortByRating(popularHotels);
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

    public static List<Hotel> filterHotelsByRatingRange(List<Hotel> hotels, float minRating, float maxRating) {
        List<Hotel> filteredHotels = new ArrayList<>();

        for (Hotel hotel : hotels) {
            try {
                float rating = Float.parseFloat(hotel.getRating());
                if (rating >= minRating && rating <= maxRating) {
                    filteredHotels.add(hotel);
                }
            } catch (NumberFormatException e) {
                // Si no tiene rating válido, no incluir
            }
        }

        return filteredHotels;
    }

    public static List<Hotel> filterHotelsByPriceRange(List<Hotel> hotels, int minPrice, int maxPrice) {
        List<Hotel> filteredHotels = new ArrayList<>();

        for (Hotel hotel : hotels) {
            int price = extractPrice(hotel.getPrice());
            if (price >= minPrice && price <= maxPrice) {
                filteredHotels.add(hotel);
            }
        }

        return filteredHotels;
    }
}