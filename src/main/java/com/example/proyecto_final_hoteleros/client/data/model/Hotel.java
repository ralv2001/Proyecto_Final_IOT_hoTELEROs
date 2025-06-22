package com.example.proyecto_final_hoteleros.client.data.model;

public class Hotel {
    private String name;
    private String location;
    private String imageUrl; // o resource ID si usas imágenes locales
    private String price;
    private String rating;

    // Constructor
    public Hotel(String name, String location, String imageUrl, String price, String rating) {
        this.name = name;
        this.location = location;
        this.imageUrl = imageUrl;
        this.price = price;
        this.rating = rating;
    }

    // Getters y setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPrice() {
        return price;
    }
    public void setPrice(String price) {
        this.price = price;
    }

    public String getRating() {
        return rating;
    }
    public void setRating(String rating) {
        this.rating = rating;
    }
}