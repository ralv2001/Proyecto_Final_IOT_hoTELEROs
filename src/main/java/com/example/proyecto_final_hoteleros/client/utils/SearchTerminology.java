package com.example.proyecto_final_hoteleros.client.utils;

public class SearchTerminology {

    // Constantes para ubicaciones
    public static final String LOCATION_FREE = "Sin restricción geográfica";
    public static final String ALL_DESTINATIONS = "Todas las ubicaciones";
    public static final String METROPOLITAN_AREA = "Área metropolitana";
    public static final String CURATED_SELECTION = "Selección curada";
    public static final String CUSTOM_SEARCH = "Búsqueda personalizada";
    public static final String EXPANDED_SEARCH = "Búsqueda amplia";
    public static final String GEOGRAPHIC_FILTER_DISABLED = "Filtro geográfico deshabilitado";

    // Constantes para fechas
    public static final String FLEXIBLE_DATES = "Fechas flexibles";
    public static final String IMMEDIATE_BOOKING = "Reserva inmediata";
    public static final String EXTENDED_STAY = "Estadía prolongada";

    // Constantes para huéspedes
    public static final String STANDARD_OCCUPANCY = "Ocupación estándar";
    public static final String GROUP_BOOKING = "Reserva grupal";
    public static final String FAMILY_PACKAGE = "Paquete familiar";

    public static String getContextualLocationText(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return LOCATION_FREE;
        }

        String input = userInput.toLowerCase().trim();

        if (input.contains("todo") || input.equals("*") || input.contains("todas")) {
            return ALL_DESTINATIONS;
        }

        if (input.contains("cerca") || input.contains("cercano")) {
            return METROPOLITAN_AREA;
        }

        return userInput;
    }

    public static String getGuestsSummary(int adults, int children) {
        if (adults <= 0) adults = 1;

        StringBuilder summary = new StringBuilder();

        if (adults == 1 && children == 0) {
            return "1 huésped";
        }

        if (adults == 2 && children == 0) {
            return STANDARD_OCCUPANCY;
        }

        summary.append(adults).append(" adulto").append(adults > 1 ? "s" : "");

        if (children > 0) {
            summary.append(" • ").append(children).append(" niño").append(children > 1 ? "s" : "");

            if (adults >= 2 && children >= 2) {
                return FAMILY_PACKAGE;
            }
        }

        if (adults + children >= 6) {
            return GROUP_BOOKING;
        }

        return summary.toString();
    }

    public static String getDatesSummary(String checkIn, String checkOut) {
        if (checkIn == null || checkOut == null) {
            return FLEXIBLE_DATES;
        }

        // Calcular días entre fechas (implementación simple)
        // En un caso real usarías una librería de fechas
        return checkIn + " - " + checkOut;
    }

    public static String getStayDurationText(int nights) {
        if (nights <= 0) return "Estadía no especificada";
        if (nights == 1) return "1 noche";
        if (nights <= 3) return nights + " noches";
        if (nights <= 7) return nights + " noches";
        if (nights <= 14) return "Estadía " + nights + " noches";
        return EXTENDED_STAY + " (" + nights + " noches)";
    }
}