package com.example.proyecto_final_hoteleros.adminhotel.model;

import android.net.Uri;
import java.util.List;

public class HotelServiceItem {

    public enum ServiceType {
        INCLUDED,  // Servicios incluidos (sin costo)
        PAID,      // Servicios pagados
        SPECIAL    // Servicio especial (taxi gratis)
    }

    private String name;
    private String description;
    private double price;
    private String iconKey;
    private ServiceType type;
    private List<Uri> photos;
    private boolean active;

    public HotelServiceItem(String name, String description, double price, String iconKey,
                            ServiceType type, List<Uri> photos) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.iconKey = iconKey;
        this.type = type;
        this.photos = photos;
        this.active = true;
    }

    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getIconKey() { return iconKey; }
    public void setIconKey(String iconKey) { this.iconKey = iconKey; }

    public ServiceType getType() { return type; }
    public void setType(ServiceType type) { this.type = type; }

    public List<Uri> getPhotos() { return photos; }
    public void setPhotos(List<Uri> photos) { this.photos = photos; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}