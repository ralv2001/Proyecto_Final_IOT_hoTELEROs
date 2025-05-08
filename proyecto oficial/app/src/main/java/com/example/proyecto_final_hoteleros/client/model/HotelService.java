package com.example.proyecto_final_hoteleros.client.model;


public class HotelService {
    private String id;
    private String name;
    private String description;
    private Double price; // null si es gratuito
    private String imageUrl; // URL de imagen o null si usa icono por defecto
    private String iconResourceName; // Nombre del recurso de icono por defecto
    private boolean isConditional; // Si el servicio depende de una condición (como el taxi)
    private String conditionalDescription; // Descripción de la condición

    public HotelService(String id, String name, String description, Double price,
                        String imageUrl, String iconResourceName,
                        boolean isConditional, String conditionalDescription) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.iconResourceName = iconResourceName;
        this.isConditional = isConditional;
        this.conditionalDescription = conditionalDescription;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getIconResourceName() { return iconResourceName; }
    public boolean isConditional() { return isConditional; }
    public String getConditionalDescription() { return conditionalDescription; }

    public boolean isFree() {
        return price == null;
    }
}
