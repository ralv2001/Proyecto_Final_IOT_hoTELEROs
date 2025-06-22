package com.example.proyecto_final_hoteleros.adminhotel.model;

import android.net.Uri;
import java.util.List;

public class HotelProfile {
    private String name;
    private String description;
    private String address;
    private String phone;
    private List<Uri> photos;
    private List<BasicService> basicServices;

    public HotelProfile(String name, String description, String address, String phone,
                        List<Uri> photos, List<BasicService> basicServices) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.photos = photos;
        this.basicServices = basicServices;
    }

    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<Uri> getPhotos() { return photos; }
    public void setPhotos(List<Uri> photos) { this.photos = photos; }

    public List<BasicService> getBasicServices() { return basicServices; }
    public void setBasicServices(List<BasicService> basicServices) { this.basicServices = basicServices; }
}