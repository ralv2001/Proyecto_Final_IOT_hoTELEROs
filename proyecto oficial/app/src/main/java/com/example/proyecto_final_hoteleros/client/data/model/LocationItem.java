package com.example.proyecto_final_hoteleros.client.data.model;

public class LocationItem {
    private String name;
    private String type;
    private String placeId;

    public LocationItem(String name, String type, String placeId) {
        this.name = name;
        this.type = type;
        this.placeId = placeId;
    }

    // Constructor antiguo para compatibilidad con código existente
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