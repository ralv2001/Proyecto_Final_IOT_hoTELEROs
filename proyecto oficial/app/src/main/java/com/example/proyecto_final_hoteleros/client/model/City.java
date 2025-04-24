package com.example.proyecto_final_hoteleros.client.model;

public class City {
    private String name;
    private int imageResourceId; // Si usas recursos locales
    private String imageUrl; // Si usas URLs

    public City(String name, int imageResourceId) {
        this.name = name;
        this.imageResourceId = imageResourceId;
    }

    public City(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}