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
        private int resourceId;
        private String imageUrl;
        private String category;
        private boolean isCustomUrl;

        public IconItem(String name, String key, int resourceId, String category) {
            this.name = name;
            this.key = key;
            this.resourceId = resourceId;
            this.category = category;
            this.isCustomUrl = false;
            this.imageUrl = null;
        }

        public IconItem(String name, String key, String imageUrl, String category) {
            this.name = name;
            this.key = key;
            this.imageUrl = imageUrl;
            this.category = category;
            this.isCustomUrl = true;
            this.resourceId = 0;
        }

        public String getName() { return name; }
        public String getKey() { return key; }
        public int getResourceId() { return resourceId; }
        public String getImageUrl() { return imageUrl; }
        public String getCategory() { return category; }
        public boolean isCustomUrl() { return isCustomUrl; }
    }

    // MAPA EXPANDIDO DE ICONOS LOCALES
    private static final Map<String, Integer> ICON_MAP = new HashMap<String, Integer>() {{
        // === SERVICIOS BÁSICOS Y CONECTIVIDAD ===
        put("wifi", R.drawable.ic_wifi);
        put("internet", R.drawable.ic_wifi);
        put("phone", R.drawable.ic_phone);
        put("telephone", R.drawable.ic_phone);
        put("mobile_signal", R.drawable.ic_phone);
        put("computer", R.drawable.ic_laptop);
        put("laptop", R.drawable.ic_laptop);
        put("printer", R.drawable.ic_printer);
        put("fax", R.drawable.ic_fax);
        put("scanner", R.drawable.ic_scanner);

        // === CLIMATIZACIÓN ===
        put("ac", R.drawable.ic_air_conditioning);
        put("air_conditioning", R.drawable.ic_air_conditioning);
        put("heating", R.drawable.ic_heating);
        put("ventilation", R.drawable.ic_fan);
        put("climate_control", R.drawable.ic_thermostat);
        put("fan", R.drawable.ic_fan);
        put("thermostat", R.drawable.ic_thermostat);

        // === ENTRETENIMIENTO ===
        put("tv", R.drawable.ic_tv);
        put("television", R.drawable.ic_tv);
        put("cable_tv", R.drawable.ic_cable_tv);
        put("satellite_tv", R.drawable.ic_satellite);
        put("netflix", R.drawable.ic_streaming);
        put("streaming", R.drawable.ic_streaming);
        put("music", R.drawable.ic_music);
        put("radio", R.drawable.ic_radio);
        put("game_console", R.drawable.ic_gaming);
        put("gaming", R.drawable.ic_gaming);
        put("entertainment", R.drawable.ic_entertainment);

        // === SERVICIOS DE AGUA Y BAÑO ===
        put("hot_water", R.drawable.ic_hot_water);
        put("water_24h", R.drawable.ic_water_drop);
        put("shower", R.drawable.ic_shower);
        put("bathtub", R.drawable.ic_bathtub);
        put("jacuzzi", R.drawable.ic_spa);
        put("bathroom", R.drawable.ic_bathroom);
        put("toiletries", R.drawable.ic_soap);
        put("towels", R.drawable.ic_towel);
        put("hairdryer", R.drawable.ic_hairdryer);

        // === SERVICIOS DE HABITACIÓN ===
        put("room_service", R.drawable.ic_room_service);
        put("housekeeping", R.drawable.ic_cleaning);
        put("cleaning", R.drawable.ic_cleaning);
        put("daily_cleaning", R.drawable.ic_cleaning);
        put("turndown_service", R.drawable.ic_bed_service);
        put("wake_up_call", R.drawable.ic_alarm_clock);
        put("minibar", R.drawable.ic_minibar);
        put("safe", R.drawable.ic_security);
        put("iron", R.drawable.ic_iron);
        put("ironing_board", R.drawable.ic_ironing_board);
        put("closet", R.drawable.ic_wardrobe);
        put("wardrobe", R.drawable.ic_wardrobe);
        put("desk", R.drawable.ic_desk);
        put("chair", R.drawable.ic_chair);
        put("balcony", R.drawable.ic_balcony);
        put("terrace", R.drawable.ic_terrace);

        // === ALIMENTACIÓN ===
        put("restaurant", R.drawable.ic_restaurant);
        put("dining", R.drawable.ic_dining);
        put("breakfast", R.drawable.ic_breakfast);
        put("lunch", R.drawable.ic_lunch);
        put("dinner", R.drawable.ic_dinner);
        put("buffet", R.drawable.ic_buffet);
        put("continental_breakfast", R.drawable.ic_continental);
        put("american_breakfast", R.drawable.ic_american_breakfast);
        put("coffee", R.drawable.ic_coffee);
        put("tea", R.drawable.ic_tea);
        put("bar", R.drawable.ic_bar);
        put("cocktails", R.drawable.ic_cocktail);
        put("wine", R.drawable.ic_wine);
        put("beer", R.drawable.ic_beer);
        put("catering", R.drawable.ic_catering);
        put("menu", R.drawable.ic_menu);
        put("chef", R.drawable.ic_chef);
        put("kitchen", R.drawable.ic_kitchen);
        put("snacks", R.drawable.ic_snacks);
        put("room_dining", R.drawable.ic_room_dining);

        // === BIENESTAR Y SPA ===
        put("spa", R.drawable.ic_spa);
        put("massage", R.drawable.ic_massage);
        put("wellness", R.drawable.ic_wellness);
        put("beauty", R.drawable.ic_beauty);
        put("facial", R.drawable.ic_facial);
        put("manicure", R.drawable.ic_manicure);
        put("pedicure", R.drawable.ic_pedicure);
        put("sauna", R.drawable.ic_sauna);
        put("steam_room", R.drawable.ic_steam);
        put("aromatherapy", R.drawable.ic_aromatherapy);
        put("reflexology", R.drawable.ic_reflexology);
        put("yoga", R.drawable.ic_yoga);
        put("meditation", R.drawable.ic_meditation);
        put("relaxation", R.drawable.ic_relax);

        // === FITNESS Y ACTIVIDADES ===
        put("gym", R.drawable.ic_gym);

        put("pool", R.drawable.ic_pool);


        // === TRANSPORTE ===
        put("taxi", R.drawable.ic_taxi);
        put("car", R.drawable.ic_car);

        put("airport", R.drawable.ic_aiport);

        put("parking", R.drawable.ic_parking);


        // === LAVANDERÍA ===
        put("laundry", R.drawable.ic_laundry);


        // === SERVICIOS DE NEGOCIOS ===
        put("business", R.drawable.ic_meeting);
        put("meeting", R.drawable.ic_meeting);


        // === ENTRETENIMIENTO Y EVENTOS ===


        // === SERVICIOS DE TURISMO ===
        put("tour", R.drawable.ic_tour);


        // === SERVICIOS DE SEGURIDAD ===
        put("security", R.drawable.ic_security);

        // === SERVICIOS DE RECEPCIÓN ===
        put("reception", R.drawable.ic_reception);

        put("check_in", R.drawable.ic_check_in);
        put("check_out", R.drawable.ic_check_out);

        // === INSTALACIONES GENERALES ===
        put("room", R.drawable.ic_room);

        put("bed", R.drawable.ic_bed);

        put("hotel", R.drawable.ic_hotel);


        // === SERVICIOS ESPECIALES Y PROMOCIONALES ===
        put("promo", R.drawable.ic_promo);


        // === SERVICIOS PARA FAMILIAS Y NIÑOS ===


        // === SERVICIOS PARA MASCOTAS ===


        // === SERVICIOS DE TEMPORADA ===


        // === SERVICIOS GENERALES ===
        put("default", R.drawable.ic_service_default);
        put("service", R.drawable.ic_service_default);
        put("general", R.drawable.ic_service_default);
        put("other", R.drawable.ic_service_default);
        put("misc", R.drawable.ic_service_default);
        put("custom", R.drawable.ic_service_default);
    }};

    // Lista de todos los iconos disponibles organizados por categoría
    private static List<IconItem> getAllIcons() {
        List<IconItem> icons = new ArrayList<>();

        // === CONECTIVIDAD ===
        icons.add(new IconItem("WiFi", "wifi", R.drawable.ic_wifi, "Conectividad"));
        icons.add(new IconItem("Internet", "internet", R.drawable.ic_wifi, "Conectividad"));
        icons.add(new IconItem("Teléfono", "phone", R.drawable.ic_phone, "Conectividad"));
        icons.add(new IconItem("Portátil", "laptop", R.drawable.ic_laptop, "Conectividad"));
        icons.add(new IconItem("Impresora", "printer", R.drawable.ic_printer, "Conectividad"));
        icons.add(new IconItem("Fax", "fax", R.drawable.ic_fax, "Conectividad"));
        icons.add(new IconItem("Escáner", "scanner", R.drawable.ic_scanner, "Conectividad"));

        // === CLIMATIZACIÓN ===
        icons.add(new IconItem("Aire Acondicionado", "ac", R.drawable.ic_air_conditioning, "Climatización"));
        icons.add(new IconItem("Calefacción", "heating", R.drawable.ic_heating, "Climatización"));
        icons.add(new IconItem("Ventilador", "fan", R.drawable.ic_fan, "Climatización"));
        icons.add(new IconItem("Termostato", "thermostat", R.drawable.ic_thermostat, "Climatización"));

        // === ENTRETENIMIENTO ===
        icons.add(new IconItem("Televisión", "tv", R.drawable.ic_tv, "Entretenimiento"));
        icons.add(new IconItem("TV por Cable", "cable_tv", R.drawable.ic_cable_tv, "Entretenimiento"));
        icons.add(new IconItem("TV Satelital", "satellite_tv", R.drawable.ic_satellite, "Entretenimiento"));
        icons.add(new IconItem("Streaming", "streaming", R.drawable.ic_streaming, "Entretenimiento"));
        icons.add(new IconItem("Música", "music", R.drawable.ic_music, "Entretenimiento"));
        icons.add(new IconItem("Radio", "radio", R.drawable.ic_radio, "Entretenimiento"));
        icons.add(new IconItem("Videojuegos", "gaming", R.drawable.ic_gaming, "Entretenimiento"));

        // === BAÑO ===
        icons.add(new IconItem("Agua Caliente", "hot_water", R.drawable.ic_hot_water, "Baño"));
        icons.add(new IconItem("Ducha", "shower", R.drawable.ic_shower, "Baño"));
        icons.add(new IconItem("Bañera", "bathtub", R.drawable.ic_bathtub, "Baño"));
        icons.add(new IconItem("Jacuzzi", "jacuzzi", R.drawable.ic_spa, "Baño"));
        icons.add(new IconItem("Artículos de Baño", "toiletries", R.drawable.ic_soap, "Baño"));
        icons.add(new IconItem("Toallas", "towels", R.drawable.ic_towel, "Baño"));
        icons.add(new IconItem("Secador de Pelo", "hairdryer", R.drawable.ic_hairdryer, "Baño"));

        // === HABITACIÓN ===
        icons.add(new IconItem("Room Service", "room_service", R.drawable.ic_room_service, "Habitación"));
        icons.add(new IconItem("Limpieza Diaria", "daily_cleaning", R.drawable.ic_cleaning, "Habitación"));
        icons.add(new IconItem("Servicio de Limpieza", "housekeeping", R.drawable.ic_cleaning, "Habitación"));
        icons.add(new IconItem("Minibar", "minibar", R.drawable.ic_minibar, "Habitación"));
        icons.add(new IconItem("Caja Fuerte", "safe", R.drawable.ic_security, "Habitación"));
        icons.add(new IconItem("Plancha", "iron", R.drawable.ic_iron, "Habitación"));
        icons.add(new IconItem("Tabla de Planchar", "ironing_board", R.drawable.ic_ironing_board, "Habitación"));
        icons.add(new IconItem("Escritorio", "desk", R.drawable.ic_desk, "Habitación"));
        icons.add(new IconItem("Balcón", "balcony", R.drawable.ic_balcony, "Habitación"));
        icons.add(new IconItem("Terraza", "terrace", R.drawable.ic_terrace, "Habitación"));
        icons.add(new IconItem("Closet", "wardrobe", R.drawable.ic_wardrobe, "Habitación"));

        // === ALIMENTACIÓN ===
        icons.add(new IconItem("Restaurante", "restaurant", R.drawable.ic_restaurant, "Alimentación"));
        icons.add(new IconItem("Desayuno", "breakfast", R.drawable.ic_breakfast, "Alimentación"));
        icons.add(new IconItem("Almuerzo", "lunch", R.drawable.ic_lunch, "Alimentación"));
        icons.add(new IconItem("Cena", "dinner", R.drawable.ic_dinner, "Alimentación"));
        icons.add(new IconItem("Buffet", "buffet", R.drawable.ic_buffet, "Alimentación"));
        icons.add(new IconItem("Café", "coffee", R.drawable.ic_coffee, "Alimentación"));
        icons.add(new IconItem("Té", "tea", R.drawable.ic_tea, "Alimentación"));
        icons.add(new IconItem("Bar", "bar", R.drawable.ic_bar, "Alimentación"));
        icons.add(new IconItem("Cócteles", "cocktails", R.drawable.ic_cocktail, "Alimentación"));
        icons.add(new IconItem("Vino", "wine", R.drawable.ic_wine, "Alimentación"));
        icons.add(new IconItem("Cerveza", "beer", R.drawable.ic_beer, "Alimentación"));
        icons.add(new IconItem("Snacks", "snacks", R.drawable.ic_snacks, "Alimentación"));

        // === BIENESTAR ===
        icons.add(new IconItem("Spa", "spa", R.drawable.ic_spa, "Bienestar"));
        icons.add(new IconItem("Masajes", "massage", R.drawable.ic_massage, "Bienestar"));
        icons.add(new IconItem("Bienestar", "wellness", R.drawable.ic_wellness, "Bienestar"));
        icons.add(new IconItem("Tratamientos de Belleza", "beauty", R.drawable.ic_beauty, "Bienestar"));
        icons.add(new IconItem("Facial", "facial", R.drawable.ic_facial, "Bienestar"));
        icons.add(new IconItem("Manicure", "manicure", R.drawable.ic_manicure, "Bienestar"));
        icons.add(new IconItem("Pedicure", "pedicure", R.drawable.ic_pedicure, "Bienestar"));
        icons.add(new IconItem("Sauna", "sauna", R.drawable.ic_sauna, "Bienestar"));
        icons.add(new IconItem("Baño de Vapor", "steam_room", R.drawable.ic_steam, "Bienestar"));
        icons.add(new IconItem("Aromaterapia", "aromatherapy", R.drawable.ic_aromatherapy, "Bienestar"));
        icons.add(new IconItem("Yoga", "yoga", R.drawable.ic_yoga, "Bienestar"));
        icons.add(new IconItem("Meditación", "meditation", R.drawable.ic_meditation, "Bienestar"));

        // === FITNESS ===
        icons.add(new IconItem("Gimnasio", "gym", R.drawable.ic_gym, "Fitness"));


        // === TRANSPORTE ===
        icons.add(new IconItem("Taxi", "taxi", R.drawable.ic_taxi, "Transporte"));
        icons.add(new IconItem("Automóvil", "car", R.drawable.ic_car, "Transporte"));
        icons.add(new IconItem("Aeropuerto", "airport", R.drawable.ic_aiport, "Transporte"));


        // === LAVANDERÍA ===
        icons.add(new IconItem("Lavandería", "laundry", R.drawable.ic_laundry, "Lavandería"));

        // === EVENTOS ===


        // === TURISMO ===
        icons.add(new IconItem("Tours", "tour", R.drawable.ic_tour, "Turismo"));


        // === SEGURIDAD ===
        icons.add(new IconItem("Seguridad", "security", R.drawable.ic_security, "Seguridad"));


        // === INSTALACIONES ===
        icons.add(new IconItem("Habitación", "room", R.drawable.ic_room, "Instalaciones"));

        icons.add(new IconItem("Hotel", "hotel", R.drawable.ic_hotel, "Instalaciones"));


        // === ESPECIALES ===
        icons.add(new IconItem("Promociones", "promo", R.drawable.ic_promo, "Especiales"));


        // === FAMILIA ===


        // === MASCOTAS ===


        // === TEMPORADA ===


        // === GENERALES ===
        icons.add(new IconItem("Servicio General", "default", R.drawable.ic_service_default, "Generales"));
        icons.add(new IconItem("Servicio", "service", R.drawable.ic_service_default, "Generales"));
        icons.add(new IconItem("Otros", "other", R.drawable.ic_service_default, "Generales"));

        return icons;
    }

    // Resto de métodos sin cambios
    public static int getIconResource(String iconKey) {
        Integer resourceId = ICON_MAP.get(iconKey);
        return resourceId != null ? resourceId : R.drawable.ic_service_default;
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

    public static List<IconItem> getIconsByCategory(String category) {
        List<IconItem> categoryIcons = new ArrayList<>();
        List<IconItem> allIcons = getAllIcons();

        for (IconItem icon : allIcons) {
            if (icon.getCategory().equals(category)) {
                categoryIcons.add(icon);
            }
        }
        return categoryIcons;
    }

    public static List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("Conectividad");
        categories.add("Climatización");
        categories.add("Entretenimiento");
        categories.add("Baño");
        categories.add("Habitación");
        categories.add("Alimentación");
        categories.add("Bienestar");
        categories.add("Fitness");
        categories.add("Transporte");
        categories.add("Lavandería");
        categories.add("Negocios");
        categories.add("Eventos");
        categories.add("Turismo");
        categories.add("Seguridad");
        categories.add("Recepción");
        categories.add("Instalaciones");
        categories.add("Especiales");
        categories.add("Familia");
        categories.add("Mascotas");
        categories.add("Temporada");
        categories.add("Generales");
        return categories;
    }

    public static List<IconItem> searchIcons(String query) {
        List<IconItem> searchResults = new ArrayList<>();
        List<IconItem> allIcons = getAllIcons();
        String lowerQuery = query.toLowerCase();

        for (IconItem icon : allIcons) {
            if (icon.getName().toLowerCase().contains(lowerQuery) ||
                    icon.getKey().toLowerCase().contains(lowerQuery) ||
                    icon.getCategory().toLowerCase().contains(lowerQuery)) {
                searchResults.add(icon);
            }
        }
        return searchResults;
    }

    public static List<IconItem> getAllIconsList() {
        return getAllIcons();
    }
}