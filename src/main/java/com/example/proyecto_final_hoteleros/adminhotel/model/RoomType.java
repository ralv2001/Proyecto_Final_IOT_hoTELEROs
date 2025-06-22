package com.example.proyecto_final_hoteleros.adminhotel.model;

import java.util.List;

public class RoomType {
    private String name;
    private String description;
    private double area; // en m²
    private double pricePerNight;
    private List<String> includedServices;
    private int availableRooms;

    public RoomType(String name, String description, double area, double pricePerNight,
                    List<String> includedServices, int availableRooms) {
        this.name = name;
        this.description = description;
        this.area = area;
        this.pricePerNight = pricePerNight;
        this.includedServices = includedServices;
        this.availableRooms = availableRooms;
    }

    // Getters y Setters
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
}