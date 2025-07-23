package com.example.proyecto_final_hoteleros.adminhotel.model;

import java.util.List;
import java.util.ArrayList;

public class RoomType {
    private String id; // Campo para Firebase
    private String name;
    private String description;
    private double area; // en m²
    private double pricePerNight;
    private List<String> includedServices;
    private int availableRooms;
    private int capacity;
    private List<String> photoUrls; // ✅ NUEVO CAMPO para fotos

    // Constructor principal
    public RoomType(String name, String description, double area, double pricePerNight,
                    List<String> includedServices, int availableRooms, int capacity) {
        this.name = name;
        this.description = description;
        this.area = area;
        this.pricePerNight = pricePerNight;
        this.includedServices = includedServices;
        this.availableRooms = availableRooms;
        this.capacity = capacity;
        this.photoUrls = new ArrayList<>(); // ✅ Inicializar lista de fotos
    }

    // Constructor con ID (para Firebase)
    public RoomType(String id, String name, String description, double area, double pricePerNight,
                    List<String> includedServices, int availableRooms, int capacity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.area = area;
        this.pricePerNight = pricePerNight;
        this.includedServices = includedServices;
        this.availableRooms = availableRooms;
        this.capacity = capacity;
        this.photoUrls = new ArrayList<>(); // ✅ Inicializar lista de fotos
    }

    // ✅ Constructor completo con fotos
    public RoomType(String id, String name, String description, double area, double pricePerNight,
                    List<String> includedServices, int availableRooms, int capacity, List<String> photoUrls) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.area = area;
        this.pricePerNight = pricePerNight;
        this.includedServices = includedServices;
        this.availableRooms = availableRooms;
        this.capacity = capacity;
        this.photoUrls = photoUrls != null ? photoUrls : new ArrayList<>();
    }

    // ========== GETTERS Y SETTERS ==========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }

    public List<String> getIncludedServices() { return includedServices; }
    public void setIncludedServices(List<String> includedServices) { this.includedServices = includedServices; }

    public int getAvailableRooms() { return availableRooms; }
    public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    // ✅ NUEVOS getters/setters para fotos
    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls != null ? photoUrls : new ArrayList<>(); }

    // ========== MÉTODOS UTILITARIOS ==========

    // ✅ Método para verificar si tiene fotos
    public boolean hasPhotos() {
        return photoUrls != null && !photoUrls.isEmpty();
    }

    // ✅ Método para obtener número de fotos
    public int getPhotoCount() {
        return photoUrls != null ? photoUrls.size() : 0;
    }

    // ✅ Método para añadir foto
    public void addPhoto(String photoUrl) {
        if (photoUrls == null) {
            photoUrls = new ArrayList<>();
        }
        if (photoUrl != null && !photoUrl.trim().isEmpty() && photoUrls.size() < 3) {
            photoUrls.add(photoUrl);
        }
    }

    // ✅ Método para remover foto
    public void removePhoto(String photoUrl) {
        if (photoUrls != null) {
            photoUrls.remove(photoUrl);
        }
    }

    @Override
    public String toString() {
        return "RoomType{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", area=" + area +
                ", pricePerNight=" + pricePerNight +
                ", capacity=" + capacity +
                ", availableRooms=" + availableRooms +
                ", includedServices=" + (includedServices != null ? includedServices.size() : 0) + " services" +
                ", photoUrls=" + (photoUrls != null ? photoUrls.size() : 0) + " photos" +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        RoomType roomType = (RoomType) obj;
        return id != null ? id.equals(roomType.id) : roomType.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}