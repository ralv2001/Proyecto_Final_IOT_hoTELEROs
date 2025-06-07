package com.example.proyecto_final_hoteleros.client.data.model;

public enum SearchContext {
    ALL_DESTINATIONS("Destinos disponibles", "Todas las ubicaciones", "🌎", true, true, true),
    NEARBY_HOTELS("Hoteles cercanos", "Área metropolitana", "📍", false, true, true),
    CITY_SPECIFIC("Hoteles en %s", "%s", "🏙️", false, true, true),
    POPULAR_DESTINATIONS("Destinos populares", "Recomendados", "⭐", true, true, true),
    SEARCH_RESULTS("Resultados de búsqueda", "Búsqueda personalizada", "🔍", true, true, true),
    LOCATION_FREE("Búsqueda amplia", "Sin restricción geográfica", "🌐", true, true, true);

    private final String title;
    private final String locationDisplay;
    private final String icon;
    private final boolean locationModifiable;
    private final boolean datesModifiable;
    private final boolean guestsModifiable;

    SearchContext(String title, String locationDisplay, String icon,
                  boolean locationModifiable, boolean datesModifiable, boolean guestsModifiable) {
        this.title = title;
        this.locationDisplay = locationDisplay;
        this.icon = icon;
        this.locationModifiable = locationModifiable;
        this.datesModifiable = datesModifiable;
        this.guestsModifiable = guestsModifiable;
    }

    // Getters existentes
    public String getTitle() { return title; }
    public String getLocationDisplay() { return locationDisplay; }
    public String getIcon() { return icon; }

    // ✅ NUEVOS getters para modificabilidad
    public boolean isLocationModifiable() { return locationModifiable; }
    public boolean areDatesModifiable() { return datesModifiable; }
    public boolean areGuestsModifiable() { return guestsModifiable; }

    public String getFormattedTitle(String cityName) {
        if (this == CITY_SPECIFIC && cityName != null) {
            return String.format(title, cityName);
        }
        return title;
    }

    public String getFormattedLocationDisplay(String location) {
        if (this == CITY_SPECIFIC && location != null) {
            return location;
        }
        return locationDisplay;
    }

    // ✅ NUEVO: Método para obtener valores por defecto según contexto
    public DefaultSearchValues getDefaultValues() {
        switch (this) {
            case NEARBY_HOTELS:
                return new DefaultSearchValues("Área metropolitana", "Hoy - Mañana", "2 adultos");
            case POPULAR_DESTINATIONS:
                return new DefaultSearchValues("Recomendados", "Hoy - Mañana", "2 adultos");
            case CITY_SPECIFIC:
                return new DefaultSearchValues("Ciudad seleccionada", "Hoy - Mañana", "2 adultos");
            case ALL_DESTINATIONS:
                return new DefaultSearchValues("Todas las ubicaciones", "Hoy - Mañana", "2 adultos");
            case SEARCH_RESULTS:
                return new DefaultSearchValues("Búsqueda personalizada", "Hoy - Mañana", "2 adultos");
            case LOCATION_FREE:
                return new DefaultSearchValues("Sin restricción geográfica", "Hoy - Mañana", "2 adultos");
            default:
                return new DefaultSearchValues("Todas las ubicaciones", "Hoy - Mañana", "2 adultos");
        }
    }

    // ✅ NUEVA clase para valores por defecto
    public static class DefaultSearchValues {
        public final String location;
        public final String dates;
        public final String guests;

        public DefaultSearchValues(String location, String dates, String guests) {
            this.location = location;
            this.dates = dates;
            this.guests = guests;
        }
    }
}