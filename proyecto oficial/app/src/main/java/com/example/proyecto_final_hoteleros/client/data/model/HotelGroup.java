package com.example.proyecto_final_hoteleros.client.data.model;

import java.util.List;

public class HotelGroup {
    private String cityName;
    private List<Hotel> hotels;
    private int totalHotels;
    private String regionCode;

    public HotelGroup(String cityName, List<Hotel> hotels) {
        this.cityName = cityName;
        this.hotels = hotels;
        this.totalHotels = hotels != null ? hotels.size() : 0;
        this.regionCode = generateRegionCode(cityName);
    }

    public HotelGroup(String cityName, List<Hotel> hotels, String regionCode) {
        this.cityName = cityName;
        this.hotels = hotels;
        this.totalHotels = hotels != null ? hotels.size() : 0;
        this.regionCode = regionCode;
    }

    private String generateRegionCode(String cityName) {
        if (cityName == null || cityName.length() < 2) return "XX";
        return cityName.substring(0, 2).toUpperCase();
    }

    // Getters y setters
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public List<Hotel> getHotels() { return hotels; }
    public void setHotels(List<Hotel> hotels) {
        this.hotels = hotels;
        this.totalHotels = hotels != null ? hotels.size() : 0;
    }

    public int getTotalHotels() { return totalHotels; }
    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }

    public String getDisplayText() {
        return cityName + " (" + totalHotels + " hoteles)";
    }

    public boolean hasHotels() {
        return hotels != null && !hotels.isEmpty();
    }
}