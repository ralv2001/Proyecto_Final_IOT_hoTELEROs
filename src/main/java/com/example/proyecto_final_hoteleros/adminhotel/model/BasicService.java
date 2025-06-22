package com.example.proyecto_final_hoteleros.adminhotel.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class BasicService {
    private String name;
    private String description;
    private String iconKey;
    private List<Uri> photos;
    public BasicService(String name, String description, String iconKey) {
        this.name = name;
        this.description = description;
        this.iconKey = iconKey;
        this.photos = new ArrayList<>();
    }
    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconKey() { return iconKey; }
    public void setIconKey(String iconKey) { this.iconKey = iconKey; }
    public List<Uri> getPhotos() { return photos; }
    public void setPhotos(List<Uri> photos) { this.photos = photos; }
}