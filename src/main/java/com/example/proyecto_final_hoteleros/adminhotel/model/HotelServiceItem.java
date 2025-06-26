package com.example.proyecto_final_hoteleros.adminhotel.model;

import android.net.Uri;
import java.util.List;

public class HotelServiceItem {

    public enum ServiceType {
        BASIC,       // Servicios básicos creados desde el perfil del hotel
        INCLUDED,    // Servicios incluidos creados desde gestión de servicios
        PAID,        // Servicios pagados
        CONDITIONAL  // Servicios condicionales (como taxi gratis por monto)
    }

    private String name;
    private String description;
    private double price;
    private String iconKey;
    private ServiceType type;
    private List<Uri> photos;
    private boolean active;
    private double conditionalAmount; // Para servicios condicionales
    private String firebaseId; // ID del documento en Firebase

    public HotelServiceItem(String name, String description, double price, String iconKey,
                            ServiceType type, List<Uri> photos) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.iconKey = iconKey;
        this.type = type;
        this.photos = photos;
        this.active = true;
        this.conditionalAmount = 0.0;
    }

    // Constructor para servicios condicionales
    public HotelServiceItem(String name, String description, double price, String iconKey,
                            ServiceType type, List<Uri> photos, double conditionalAmount) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.iconKey = iconKey;
        this.type = type;
        this.photos = photos;
        this.active = true;
        this.conditionalAmount = conditionalAmount;
    }
    // Getter y Setter
    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }
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

    public double getConditionalAmount() { return conditionalAmount; }
    public void setConditionalAmount(double conditionalAmount) { this.conditionalAmount = conditionalAmount; }

    // Método para obtener la etiqueta del tipo de servicio
    public String getTypeLabel() {
        switch (type) {
            case BASIC:
                return "Incluido - Básico";
            case INCLUDED:
                return "Incluido";
            case PAID:
                return "Pagado";
            case CONDITIONAL:
                return "Condicional";
            default:
                return "Servicio";
        }
    }

    // Método para verificar si es un servicio gratuito
    public boolean isFree() {
        return type == ServiceType.BASIC || type == ServiceType.INCLUDED ||
                (type == ServiceType.CONDITIONAL && price == 0.0);
    }
}