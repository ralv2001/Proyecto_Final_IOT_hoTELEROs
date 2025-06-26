package com.example.proyecto_final_hoteleros.adminhotel.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class BasicService {
    private String name;
    private String description;
    private String iconKey;
    private List<String> photos; // Cambiado de List<Uri> a List<String>
    private String firebaseId; // ID del documento en Firebase

    public BasicService(String name, String description, String iconKey) {
        this.name = name;
        this.description = description;
        this.iconKey = iconKey;
        this.photos = new ArrayList<>();
    }

    // Getter y Setter
    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }

    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconKey() { return iconKey; }
    public void setIconKey(String iconKey) { this.iconKey = iconKey; }

    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }

    // Métodos de utilidad para conversión con Uri
    public List<Uri> getPhotosAsUri() {
        List<Uri> uris = new ArrayList<>();
        for (String photoPath : photos) {
            uris.add(Uri.parse(photoPath));
        }
        return uris;
    }

    public void setPhotosFromUri(List<Uri> photoUris) {
        this.photos = new ArrayList<>();
        for (Uri uri : photoUris) {
            this.photos.add(uri.toString());
        }
    }
}