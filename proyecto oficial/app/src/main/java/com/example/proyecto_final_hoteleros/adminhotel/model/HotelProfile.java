package com.example.proyecto_final_hoteleros.adminhotel.model;

import android.net.Uri;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceModel;
import java.util.List;

public class HotelProfile {
    private String name;
    private String address;  // Solo dirección completa, se removió description y phone
    private List<Uri> photos;
    private List<HotelServiceModel> basicServices; // ✅ CAMBIADO: Usar HotelServiceModel directamente

    public HotelProfile(String name, String address, List<Uri> photos, List<HotelServiceModel> basicServices) {
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

    public List<HotelServiceModel> getBasicServices() { return basicServices; }
    public void setBasicServices(List<HotelServiceModel> basicServices) { this.basicServices = basicServices; }
}