package com.example.proyecto_final_hoteleros.adminhotel.utils;

import com.example.proyecto_final_hoteleros.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IconHelper {

    public static class IconItem {
        private String name;
        private String key;
        private int resourceId; // Para iconos locales
        private String imageUrl; // Para iconos personalizados (URL)
        private String category;
        private boolean isCustomUrl; // true = URL personalizada, false = local

        // Constructor para iconos locales
        public IconItem(String name, String key, int resourceId, String category) {
            this.name = name;
            this.key = key;
            this.resourceId = resourceId;
            this.category = category;
            this.isCustomUrl = false;
            this.imageUrl = null;
        }

        // Constructor para iconos con URL personalizada
        public IconItem(String name, String key, String imageUrl, String category) {
            this.name = name;
            this.key = key;
            this.imageUrl = imageUrl;
            this.category = category;
            this.isCustomUrl = true;
            this.resourceId = 0;
        }

        // Getters
        public String getName() { return name; }
        public String getKey() { return key; }
        public int getResourceId() { return resourceId; }
        public String getImageUrl() { return imageUrl; }
        public String getCategory() { return category; }
        public boolean isCustomUrl() { return isCustomUrl; }
    }

    // MAPA DE ICONOS LOCALES DISPONIBLES
    private static final Map<String, Integer> ICON_MAP = new HashMap<String, Integer>() {{
        // Servicios Básicos
        put("wifi", R.drawable.ic_wifi);
        put("ac", R.drawable.ic_service_wifi);
        put("tv", R.drawable.ic_service_wifi);
        put("phone", R.drawable.ic_phone);

        // Servicios de Hotel
        put("spa", R.drawable.ic_spa);
        put("room_service", R.drawable.ic_room_service);
        put("minibar", R.drawable.ic_minibar);
        put("laundry", R.drawable.ic_laundry);
        put("gym", R.drawable.ic_gym);
        put("pool", R.drawable.ic_pool);
        put("parking", R.drawable.ic_parking);
        put("restaurant", R.drawable.ic_restaurant);
        put("breakfast", R.drawable.ic_breakfast);
        put("meeting", R.drawable.ic_meeting);
        put("reception", R.drawable.ic_reception);

        // Transporte
        put("taxi", R.drawable.ic_taxi);
        put("car", R.drawable.ic_car);
        put("airport", R.drawable.ic_aiport);

        // Habitación
        put("room", R.drawable.ic_room);
        put("hotel", R.drawable.ic_hotel);
        put("bed", R.drawable.ic_room);

        // Otros
        put("security", R.drawable.ic_security);
        put("tour", R.drawable.ic_tour);
        put("promo", R.drawable.ic_promo);
    }};

    public static int getIconResource(String iconKey) {
        return ICON_MAP.getOrDefault(iconKey, R.drawable.ic_hotel_service_default);
    }

    public static List<IconItem> getAllIcons() {
        List<IconItem> icons = new ArrayList<>();

        // Servicios Básicos
        icons.add(new IconItem("WiFi", "wifi", R.drawable.ic_wifi, "Básicos"));
        icons.add(new IconItem("Aire Acondicionado", "ac", R.drawable.ic_service_wifi, "Básicos"));
        icons.add(new IconItem("Televisión", "tv", R.drawable.ic_service_wifi, "Básicos"));
        icons.add(new IconItem("Teléfono", "phone", R.drawable.ic_phone, "Básicos"));

        // Bienestar
        icons.add(new IconItem("Spa", "spa", R.drawable.ic_spa, "Bienestar"));
        icons.add(new IconItem("Gimnasio", "gym", R.drawable.ic_gym, "Bienestar"));
        icons.add(new IconItem("Piscina", "pool", R.drawable.ic_pool, "Bienestar"));

        // Servicios de Habitación
        icons.add(new IconItem("Room Service", "room_service", R.drawable.ic_room_service, "Habitación"));
        icons.add(new IconItem("Minibar", "minibar", R.drawable.ic_minibar, "Habitación"));
        icons.add(new IconItem("Lavandería", "laundry", R.drawable.ic_laundry, "Habitación"));

        // Alimentación
        icons.add(new IconItem("Restaurante", "restaurant", R.drawable.ic_restaurant, "Alimentación"));
        icons.add(new IconItem("Desayuno", "breakfast", R.drawable.ic_breakfast, "Alimentación"));

        // Transporte
        icons.add(new IconItem("Taxi", "taxi", R.drawable.ic_taxi, "Transporte"));
        icons.add(new IconItem("Automóvil", "car", R.drawable.ic_car, "Transporte"));
        icons.add(new IconItem("Aeropuerto", "airport", R.drawable.ic_aiport, "Transporte"));
        icons.add(new IconItem("Estacionamiento", "parking", R.drawable.ic_parking, "Transporte"));

        // Instalaciones
        icons.add(new IconItem("Habitación", "room", R.drawable.ic_room, "Instalaciones"));
        icons.add(new IconItem("Hotel", "hotel", R.drawable.ic_hotel, "Instalaciones"));
        icons.add(new IconItem("Recepción", "reception", R.drawable.ic_reception, "Instalaciones"));
        icons.add(new IconItem("Sala de Reuniones", "meeting", R.drawable.ic_meeting, "Instalaciones"));

        // Otros
        icons.add(new IconItem("Seguridad", "security", R.drawable.ic_security, "Otros"));
        icons.add(new IconItem("Tours", "tour", R.drawable.ic_tour, "Otros"));
        icons.add(new IconItem("Promociones", "promo", R.drawable.ic_promo, "Otros"));

        return icons;
    }

    public static List<IconItem> getIconsByCategory(String category) {
        List<IconItem> allIcons = getAllIcons();
        List<IconItem> filteredIcons = new ArrayList<>();

        for (IconItem icon : allIcons) {
            if (icon.getCategory().equals(category)) {
                filteredIcons.add(icon);
            }
        }

        return filteredIcons;
    }

    public static List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("Básicos");
        categories.add("Bienestar");
        categories.add("Habitación");
        categories.add("Alimentación");
        categories.add("Transporte");
        categories.add("Instalaciones");
        categories.add("Otros");
        return categories;
    }

    public static String getIconName(String iconKey) {
        List<IconItem> allIcons = getAllIcons();
        for (IconItem icon : allIcons) {
            if (icon.getKey().equals(iconKey)) {
                return icon.getName();
            }
        }
        return "Servicio";
    }

    public static List<IconItem> searchIcons(String query) {
        List<IconItem> allIcons = getAllIcons();
        List<IconItem> filteredIcons = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (IconItem icon : allIcons) {
            if (icon.getName().toLowerCase().contains(lowerQuery) ||
                    icon.getKey().toLowerCase().contains(lowerQuery) ||
                    icon.getCategory().toLowerCase().contains(lowerQuery)) {
                filteredIcons.add(icon);
            }
        }

        if (filteredIcons.isEmpty()) {
            return getIconsByCategory("Básicos");
        }

        return filteredIcons;
    }
}