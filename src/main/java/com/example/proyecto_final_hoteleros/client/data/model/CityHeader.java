package com.example.proyecto_final_hoteleros.client.data.model;

public class CityHeader {
    private String cityName;
    private int hotelCount;
    private String subtitle;
    private boolean isExpanded;
    private String uniqueId; // ✅ NUEVO: ID único

    public CityHeader(String cityName, int hotelCount) {
        this.cityName = cityName;
        this.hotelCount = hotelCount;
        this.subtitle = generateSubtitle();
        this.isExpanded = true; // Por defecto expandido
        this.uniqueId = "city_" + cityName.toLowerCase().replace(" ", "_"); // ✅ NUEVO
    }

    public CityHeader(String cityName, int hotelCount, String subtitle) {
        this.cityName = cityName;
        this.hotelCount = hotelCount;
        this.subtitle = subtitle;
        this.isExpanded = true;
        this.uniqueId = "city_" + cityName.toLowerCase().replace(" ", "_"); // ✅ NUEVO
    }

    private String generateSubtitle() {
        if (hotelCount == 1) {
            return "1 hotel disponible";
        } else {
            return hotelCount + " hoteles disponibles";
        }
    }

    // Getters y setters existentes...
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) {
        this.cityName = cityName;
        this.uniqueId = "city_" + cityName.toLowerCase().replace(" ", "_"); // ✅ Actualizar ID
    }

    public int getHotelCount() { return hotelCount; }
    public void setHotelCount(int hotelCount) {
        this.hotelCount = hotelCount;
        this.subtitle = generateSubtitle();
    }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }

    // ✅ NUEVO getter
    public String getUniqueId() { return uniqueId; }

    public String getDisplayTitle() {
        return cityName;
    }
}