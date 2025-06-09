package com.example.proyecto_final_hoteleros.client.utils;

import com.example.proyecto_final_hoteleros.client.data.model.Hotel;
import java.util.HashMap;
import java.util.Map;

public class HotelDistanceCalculator {

    // Coordenadas reales de ciudades principales del Perú
    private static final Map<String, Coordinates> CITY_COORDINATES = new HashMap<>();
    private static final Map<String, Coordinates> DISTRICT_COORDINATES = new HashMap<>();

    static {
        // Ciudades principales
        CITY_COORDINATES.put("lima", new Coordinates(-12.046374, -77.042793));
        CITY_COORDINATES.put("cusco", new Coordinates(-13.531950, -71.967463));
        CITY_COORDINATES.put("arequipa", new Coordinates(-16.409047, -71.537451));
        CITY_COORDINATES.put("piura", new Coordinates(-5.194486, -80.632656));
        CITY_COORDINATES.put("trujillo", new Coordinates(-8.111691, -79.028740));
        CITY_COORDINATES.put("iquitos", new Coordinates(-3.749912, -73.250854));
        CITY_COORDINATES.put("chiclayo", new Coordinates(-6.777187, -79.840531));
        CITY_COORDINATES.put("huancayo", new Coordinates(-12.068768, -75.210030));
        CITY_COORDINATES.put("tacna", new Coordinates(-18.004668, -70.248718));
        CITY_COORDINATES.put("puno", new Coordinates(-15.840422, -70.028068));

        // Distritos de Lima
        DISTRICT_COORDINATES.put("miraflores", new Coordinates(-12.121332, -77.029968));
        DISTRICT_COORDINATES.put("san isidro", new Coordinates(-12.097778, -77.037222));
        DISTRICT_COORDINATES.put("barranco", new Coordinates(-12.140833, -77.020556));
        DISTRICT_COORDINATES.put("centro", new Coordinates(-12.046374, -77.042793));
        DISTRICT_COORDINATES.put("san borja", new Coordinates(-12.108611, -76.993056));
        DISTRICT_COORDINATES.put("surco", new Coordinates(-12.147500, -76.987222));
        DISTRICT_COORDINATES.put("la molina", new Coordinates(-12.079167, -76.948611));
        DISTRICT_COORDINATES.put("chorrillos", new Coordinates(-12.168611, -77.012778));

        // Áreas específicas de otras ciudades
        DISTRICT_COORDINATES.put("centro histórico", new Coordinates(-13.518750, -71.972222)); // Cusco
        DISTRICT_COORDINATES.put("yanahuara", new Coordinates(-16.396944, -71.544444)); // Arequipa
        DISTRICT_COORDINATES.put("cayma", new Coordinates(-16.372222, -71.542778)); // Arequipa
        DISTRICT_COORDINATES.put("máncora", new Coordinates(-4.103611, -81.045556)); // Piura
        DISTRICT_COORDINATES.put("tumbes", new Coordinates(-3.566667, -80.450000));
        DISTRICT_COORDINATES.put("chachapoyas", new Coordinates(-6.232222, -77.869167)); // Amazonas
        DISTRICT_COORDINATES.put("leymebamba", new Coordinates(-6.714722, -77.795278)); // Amazonas
        DISTRICT_COORDINATES.put("tambopata", new Coordinates(-12.733333, -69.183333)); // Madre de Dios
        DISTRICT_COORDINATES.put("puerto maldonado", new Coordinates(-12.593333, -69.189167));
        DISTRICT_COORDINATES.put("valle sagrado", new Coordinates(-13.316667, -72.083333)); // Cusco
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en kilómetros

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return distance;
    }

    public static double calculateDistanceToHotel(Hotel hotel, double userLat, double userLon) {
        Coordinates hotelCoords = getHotelCoordinates(hotel);
        if (hotelCoords != null) {
            return calculateDistance(userLat, userLon, hotelCoords.latitude, hotelCoords.longitude);
        }
        return 999.0; // Distancia máxima si no se puede calcular
    }

    public static Coordinates getHotelCoordinates(Hotel hotel) {
        String location = hotel.getLocation().toLowerCase();

        // Primero buscar por distrito específico
        for (Map.Entry<String, Coordinates> entry : DISTRICT_COORDINATES.entrySet()) {
            if (location.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Luego buscar por ciudad
        for (Map.Entry<String, Coordinates> entry : CITY_COORDINATES.entrySet()) {
            if (location.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    public static int getDistanceCategory(double distance) {
        if (distance < 1.0) return 1; // Muy cerca
        if (distance < 5.0) return 2; // Cerca
        if (distance < 15.0) return 3; // Moderado
        if (distance < 50.0) return 4; // Lejos
        return 5; // Muy lejos
    }

    public static class Coordinates {
        public final double latitude;
        public final double longitude;

        public Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}