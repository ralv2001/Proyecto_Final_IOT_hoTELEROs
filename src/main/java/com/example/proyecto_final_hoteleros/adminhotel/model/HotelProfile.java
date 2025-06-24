package com.example.proyecto_final_hoteleros.adminhotel.model;

import android.net.Uri;
import java.util.List;

public class HotelProfile {
    private String name;
    private String address;  // Solo dirección completa, se removió description y phone
    private List<Uri> photos;
    private List<BasicService> basicServices;

    public HotelProfile(String name, String address, List<Uri> photos, List<BasicService> basicServices) {
        this.name = name;
        this.address = address;
        this.photos = photos;
        this.basicServices = basicServices;
    }

    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<Uri> getPhotos() { return photos; }
    public void setPhotos(List<Uri> photos) { this.photos = photos; }

    public List<BasicService> getBasicServices() { return basicServices; }
    public void setBasicServices(List<BasicService> basicServices) { this.basicServices = basicServices; }
}