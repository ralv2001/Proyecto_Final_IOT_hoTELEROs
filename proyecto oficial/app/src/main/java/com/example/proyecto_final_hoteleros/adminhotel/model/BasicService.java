package com.example.proyecto_final_hoteleros.adminhotel.model;

public class BasicService {
    private String name;
    private String description;
    private String iconKey;

    public BasicService(String name, String description, String iconKey) {
        this.name = name;
        this.description = description;
        this.iconKey = iconKey;
    }

    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconKey() { return iconKey; }
    public void setIconKey(String iconKey) { this.iconKey = iconKey; }
}