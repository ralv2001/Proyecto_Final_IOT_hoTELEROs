package com.example.proyecto_final_hoteleros.client.utils;

import android.content.Context;
import android.util.Log;

import com.example.proyecto_final_hoteleros.client.data.model.Hotel;
import com.example.proyecto_final_hoteleros.client.data.model.CityHeader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HotelGroupingUtils {

    private static final String TAG = "HotelGroupingUtils";

    // ✅ CIUDADES DEL PERÚ ACTUALIZADAS con Firebase
    private static final String[] PERU_CITIES = {
            "lima", "cusco", "cuzco", "arequipa", "piura", "trujillo", "iquitos",
            "chiclayo", "huancayo", "tacna", "puno", "ayacucho", "cajamarca",
            "tumbes", "chachapoyas", "leymebamba", "tambopata", "puerto maldonado",
            "madre de dios", "amazonas", "ancash", "apurimac", "huanuco", "junin",
            "lambayeque", "la libertad", "loreto", "moquegua", "pasco", "san martin",
            "ucayali", "callao"
    };

    /**
     * ✅ MÉTODO PRINCIPAL: Agrupa hoteles por ciudad/departamento
     */
    public static List<Object> groupHotelsByCity(List<Hotel> hotels) {
        if (hotels == null || hotels.isEmpty()) {
            Log.d(TAG, "Lista de hoteles vacía o nula");
            return new ArrayList<>();
        }

        Log.d(TAG, "Agrupando " + hotels.size() + " hoteles por ciudad");

        Map<String, List<Hotel>> cityGroups = new HashMap<>();

        for (Hotel hotel : hotels) {
            String city = extractCityFromLocation(hotel.getLocation());
            if (city == null) {
                city = "otros"; // Fallback
            }

            // Capitalizar primera letra
            String capitalizedCity = city.substring(0, 1).toUpperCase() + city.substring(1);

            if (!cityGroups.containsKey(capitalizedCity)) {
                cityGroups.put(capitalizedCity, new ArrayList<>());
            }
            cityGroups.get(capitalizedCity).add(hotel);
        }

        // Convertir a lista con headers
        List<Object> groupedItems = new ArrayList<>();

        // Ordenar ciudades alfabéticamente
        List<String> sortedCities = new ArrayList<>(cityGroups.keySet());
        Collections.sort(sortedCities);

        for (String city : sortedCities) {
            List<Hotel> cityHotels = cityGroups.get(city);
            if (!cityHotels.isEmpty()) {
                // Agregar header de ciudad
                groupedItems.add(new CityHeader(city, cityHotels.size()));

                // Ordenar hoteles de la ciudad por rating
                Collections.sort(cityHotels, (h1, h2) -> {
                    try {
                        double rating1 = Double.parseDouble(h1.getRating());
                        double rating2 = Double.parseDouble(h2.getRating());
                        return Double.compare(rating2, rating1); // Descendente
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                });

                // Agregar hoteles
                groupedItems.addAll(cityHotels);
            }
        }

        Log.d(TAG, "Agrupación completada: " + groupedItems.size() + " items en " + sortedCities.size() + " ciudades");
        return groupedItems;
    }

    /**
     * ✅ NUEVO: Crear mapa de hoteles por ciudad (para HomeFragment)
     */
    public static Map<String, List<Hotel>> groupHotelsByCityMap(List<Hotel> hotels) {
        Map<String, List<Hotel>> cityGroups = new HashMap<>();

        if (hotels == null) {
            return cityGroups;
        }

        for (Hotel hotel : hotels) {
            String city = extractCityFromLocation(hotel.getLocation());
            if (city == null) {
                city = "otros"; // Fallback
            }

            String capitalizedCity = city.substring(0, 1).toUpperCase() + city.substring(1);

            if (!cityGroups.containsKey(capitalizedCity)) {
                cityGroups.put(capitalizedCity, new ArrayList<>());
            }
            cityGroups.get(capitalizedCity).add(hotel);
        }

        return cityGroups;
    }

    /**
     * ✅ FILTRAR HOTELES CERCANOS - Optimizado para Firebase
     */
    public static List<Hotel> filterHotelsByProximity(List<Hotel> hotels, String userLocation, Context context) {
        List<Hotel> nearbyHotels = new ArrayList<>();

        if (hotels == null || hotels.isEmpty()) {
            Log.d(TAG, "No hay hoteles para filtrar proximidad");
            return nearbyHotels;
        }

        String userCity = userLocation != null ? userLocation.toLowerCase().trim() : "";

        // ✅ OBTENER CIUDAD ACTUAL DEL USUARIO DESDE LOCATIONMANAGER
        if (context != null) {
            UserLocationManager locationManager = UserLocationManager.getInstance(context);
            userCity = locationManager.getCurrentCity().toLowerCase().trim();
        }

        Log.d(TAG, "🔍 Filtrando hoteles cercanos para ciudad del usuario: '" + userCity + "'");
        Log.d(TAG, "📊 Total hoteles a revisar: " + hotels.size());

        for (Hotel hotel : hotels) {
            String hotelLocation = hotel.getLocation() != null ? hotel.getLocation().toLowerCase().trim() : "";
            String hotelCity = extractCityFromLocation(hotelLocation);

            Log.d(TAG, "🏨 Revisando hotel: " + hotel.getName());
            Log.d(TAG, "   📍 Ubicación hotel: '" + hotelLocation + "'");
            Log.d(TAG, "   🏙️ Ciudad extraída: '" + hotelCity + "'");

            boolean isNearby = false;

            // ✅ MEJORAR LÓGICA DE COMPARACIÓN
            if (hotelCity != null && hotelCity.equals(userCity)) {
                isNearby = true;
                Log.d(TAG, "   ✅ COINCIDENCIA POR CIUDAD EXTRAÍDA");
            }
            // ✅ VERIFICAR SI LA UBICACIÓN COMPLETA CONTIENE LA CIUDAD DEL USUARIO
            else if (hotelLocation.contains(userCity)) {
                isNearby = true;
                Log.d(TAG, "   ✅ COINCIDENCIA POR CONTIENE CIUDAD");
            }
            // ✅ VERIFICAR VARIACIONES COMUNES DE NOMBRES DE CIUDADES
            else if (isCityVariation(userCity, hotelLocation)) {
                isNearby = true;
                Log.d(TAG, "   ✅ COINCIDENCIA POR VARIACIÓN DE CIUDAD");
            }

            if (isNearby) {
                nearbyHotels.add(hotel);
                Log.d(TAG, "   🎯 HOTEL AGREGADO A CERCANOS");
            } else {
                Log.d(TAG, "   ❌ Hotel no está cerca");
            }
        }

        // ✅ ORDENAR POR RATING DESCENDENTE
        Collections.sort(nearbyHotels, (h1, h2) -> {
            try {
                double rating1 = Double.parseDouble(h1.getRating());
                double rating2 = Double.parseDouble(h2.getRating());
                return Double.compare(rating2, rating1);
            } catch (NumberFormatException e) {
                return 0;
            }
        });

        Log.d(TAG, "🏆 RESULTADO FINAL - Hoteles cercanos encontrados: " + nearbyHotels.size());
        for (Hotel hotel : nearbyHotels) {
            Log.d(TAG, "   ✅ " + hotel.getName() + " - " + hotel.getLocation());
        }

        return nearbyHotels;
    }
    private static boolean isCityVariation(String userCity, String hotelLocation) {
        // Manejar variaciones comunes
        if (userCity.equals("lima")) {
            return hotelLocation.contains("lima") ||
                    hotelLocation.contains("miraflores") ||
                    hotelLocation.contains("san isidro") ||
                    hotelLocation.contains("barranco") ||
                    hotelLocation.contains("surco") ||
                    hotelLocation.contains("callao");
        }

        if (userCity.equals("cusco") || userCity.equals("cuzco")) {
            return hotelLocation.contains("cusco") || hotelLocation.contains("cuzco");
        }

        // Agregar más variaciones según necesidad
        return false;
    }


    /**
     * ✅ FILTRAR HOTELES POPULARES - Basado en rating
     */
    public static List<Hotel> filterPopularHotels(List<Hotel> hotels) {
        if (hotels == null || hotels.isEmpty()) {
            Log.d(TAG, "No hay hoteles para filtrar populares");
            return new ArrayList<>();
        }

        Log.d(TAG, "Filtrando hoteles populares de " + hotels.size() + " hoteles");

        List<Hotel> popularHotels = new ArrayList<>(hotels);

        // Ordenar por rating descendente
        Collections.sort(popularHotels, (h1, h2) -> {
            try {
                double rating1 = Double.parseDouble(h1.getRating());
                double rating2 = Double.parseDouble(h2.getRating());
                return Double.compare(rating2, rating1);
            } catch (NumberFormatException e) {
                return 0;
            }
        });

        // Tomar solo los mejores (con rating >= 4.5)
        List<Hotel> topRatedHotels = new ArrayList<>();
        for (Hotel hotel : popularHotels) {
            try {
                double rating = Double.parseDouble(hotel.getRating());
                if (rating >= 4.5) {
                    topRatedHotels.add(hotel);
                }

                // Limitar a 15 hoteles populares
                if (topRatedHotels.size() >= 15) {
                    break;
                }
            } catch (NumberFormatException e) {
                // Si no se puede parsear el rating, agregarlo al final
                if (topRatedHotels.size() < 15) {
                    topRatedHotels.add(hotel);
                }
            }
        }

        Log.d(TAG, "Hoteles populares encontrados: " + topRatedHotels.size());
        return topRatedHotels;
    }

    /**
     * ✅ BUSCAR HOTELES POR CIUDAD ESPECÍFICA
     */
    public static List<Hotel> getHotelsForCity(List<Hotel> allHotels, String cityName) {
        List<Hotel> cityHotels = new ArrayList<>();

        if (allHotels == null || cityName == null) {
            Log.d(TAG, "Parámetros nulos en getHotelsForCity");
            return cityHotels;
        }

        String searchCity = cityName.toLowerCase().trim();
        Log.d(TAG, "Buscando hoteles para ciudad: '" + searchCity + "'");

        for (Hotel hotel : allHotels) {
            String hotelLocation = hotel.getLocation() != null ? hotel.getLocation().toLowerCase() : "";
            String hotelCity = extractCityFromLocation(hotelLocation);

            boolean matchesCity = false;

            // 1. Coincidencia exacta con ciudad extraída
            if (hotelCity != null && hotelCity.equals(searchCity)) {
                matchesCity = true;
            }
            // 2. La ubicación contiene la ciudad buscada
            else if (hotelLocation.contains(searchCity)) {
                matchesCity = true;
            }
            // 3. Verificar nombre del hotel
            else if (hotel.getName().toLowerCase().contains(searchCity)) {
                matchesCity = true;
            }

            if (matchesCity) {
                cityHotels.add(hotel);
                Log.d(TAG, "Hotel coincidente: " + hotel.getName() + " en " + hotel.getLocation());
            }
        }

        Log.d(TAG, "Total hoteles encontrados para '" + searchCity + "': " + cityHotels.size());
        return cityHotels;
    }

    /**
     * ✅ MÉTODO MEJORADO: getHotelsForCitySpecific
     */
    public static List<Hotel> getHotelsForCitySpecific(List<Hotel> allHotels, String searchLocation) {
        List<Hotel> results = new ArrayList<>();

        if (searchLocation == null || searchLocation.trim().isEmpty()) {
            return results;
        }

        String searchTerm = searchLocation.toLowerCase().trim();
        String cityFromLocation = extractCityFromLocation(searchTerm);

        Log.d(TAG, "Búsqueda específica para: '" + searchTerm + "', ciudad extraída: '" + cityFromLocation + "'");

        // Primero agregar hoteles de la ciudad específica
        for (Hotel hotel : allHotels) {
            String hotelLocation = hotel.getLocation() != null ? hotel.getLocation().toLowerCase() : "";
            String hotelCity = extractCityFromLocation(hotelLocation);

            if (cityFromLocation != null && cityFromLocation.equals(hotelCity)) {
                results.add(hotel);
            }
        }

        // Si no hay suficientes hoteles, agregar otros que coincidan
        if (results.size() < 3) {
            for (Hotel hotel : allHotels) {
                if (!results.contains(hotel)) {
                    String hotelLocation = hotel.getLocation() != null ? hotel.getLocation().toLowerCase() : "";
                    String hotelName = hotel.getName() != null ? hotel.getName().toLowerCase() : "";

                    if (hotelLocation.contains(searchTerm) || hotelName.contains(searchTerm)) {
                        results.add(hotel);
                    }
                }
            }
        }

        Log.d(TAG, "Resultados específicos encontrados: " + results.size());
        return results;
    }

    /**
     * ✅ BÚSQUEDA GENERAL DE HOTELES
     */
    public static List<Hotel> searchHotels(List<Hotel> allHotels, String searchTerm) {
        List<Hotel> results = new ArrayList<>();

        if (allHotels == null || searchTerm == null || searchTerm.trim().isEmpty()) {
            return results;
        }

        String search = searchTerm.toLowerCase().trim();
        Log.d(TAG, "Búsqueda general para: '" + search + "'");

        for (Hotel hotel : allHotels) {
            String hotelName = hotel.getName() != null ? hotel.getName().toLowerCase() : "";
            String hotelLocation = hotel.getLocation() != null ? hotel.getLocation().toLowerCase() : "";

            if (hotelName.contains(search) || hotelLocation.contains(search)) {
                results.add(hotel);
            }
        }

        // Ordenar por relevancia (los que contienen el término en el nombre primero)
        Collections.sort(results, (h1, h2) -> {
            boolean h1NameMatch = h1.getName().toLowerCase().contains(search);
            boolean h2NameMatch = h2.getName().toLowerCase().contains(search);

            if (h1NameMatch && !h2NameMatch) return -1;
            if (!h1NameMatch && h2NameMatch) return 1;

            // Si ambos coinciden en nombre o ubicación, ordenar por rating
            try {
                double rating1 = Double.parseDouble(h1.getRating());
                double rating2 = Double.parseDouble(h2.getRating());
                return Double.compare(rating2, rating1);
            } catch (NumberFormatException e) {
                return 0;
            }
        });

        Log.d(TAG, "Resultados de búsqueda encontrados: " + results.size());
        return results;
    }

    /**
     * ✅ EXTRAER CIUDAD DE UBICACIÓN - Mejorado
     */
    public static String extractCityFromLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return null;
        }

        String locationLower = location.toLowerCase().trim();

        Log.d(TAG, "🔍 Extrayendo ciudad de: '" + location + "'");

        // ✅ BÚSQUEDA MÁS PRECISA - ORDEN IMPORTA
        // Buscar ciudades más específicas primero
        if (locationLower.contains("san isidro")) return "lima";
        if (locationLower.contains("miraflores")) return "lima";
        if (locationLower.contains("barranco")) return "lima";
        if (locationLower.contains("surco")) return "lima";
        if (locationLower.contains("callao")) return "lima";

        // Luego ciudades principales
        for (String city : PERU_CITIES) {
            if (locationLower.contains(city)) {
                Log.d(TAG, "✅ Ciudad extraída: '" + city + "'");
                return city;
            }
        }

        Log.d(TAG, "❌ No se pudo extraer ciudad de: '" + location + "'");
        return null;
    }

    /**
     * ✅ CONTAR TOTAL DE HOTELES EN GRUPOS
     */
    public static int getTotalHotelsCount(List<Object> groupedItems) {
        int count = 0;
        for (Object item : groupedItems) {
            if (item instanceof Hotel) {
                count++;
            }
        }
        return count;
    }

    /**
     * ✅ VERIFICAR SI UN HOTEL ESTÁ EN UNA CIUDAD ESPECÍFICA
     */
    public static boolean isHotelInCity(Hotel hotel, String cityName) {
        if (hotel == null || cityName == null) {
            return false;
        }

        String targetCity = cityName.toLowerCase().trim();
        String hotelLocation = hotel.getLocation() != null ? hotel.getLocation().toLowerCase() : "";
        String hotelCity = extractCityFromLocation(hotelLocation);

        return (hotelCity != null && hotelCity.equals(targetCity)) ||
                hotelLocation.contains(targetCity);
    }
}