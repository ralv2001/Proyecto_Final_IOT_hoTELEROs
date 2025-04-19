package com.example.proyecto_final_hoteleros.client.model;

public class LocationItem {
    private String name;
    private String type;
    private String placeId; // Añadido para Places API

    public LocationItem(String name, String type) {
        this.name = name;
        this.type = type;
        this.placeId = "";
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
}