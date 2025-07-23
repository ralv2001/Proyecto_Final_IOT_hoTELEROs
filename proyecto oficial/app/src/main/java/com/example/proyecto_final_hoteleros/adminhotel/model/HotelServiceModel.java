package com.example.proyecto_final_hoteleros.adminhotel.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class HotelServiceModel {

    private String id;
    private String name;
    private String description;
    private String iconKey;
    private String serviceType; // "basic", "included", "paid", "conditional"
    private double price;
    private double conditionalAmount;
    private boolean active;
    private List<String> photoUrls;
    private String hotelAdminId; // ID del admin del hotel que lo creó

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date updatedAt;

    // Constructor vacío para Firebase
    public HotelServiceModel() {
        this.photoUrls = new ArrayList<>();
        this.active = true;
        this.price = 0.0;
        this.conditionalAmount = 0.0;
    }

    public HotelServiceModel(String name, String description, String iconKey, String serviceType) {
        this();
        this.name = name;
        this.description = description;
        this.iconKey = iconKey;
        this.serviceType = serviceType;
    }

    // Convertir a Map para Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("description", description);
        map.put("iconKey", iconKey);
        map.put("serviceType", serviceType);
        map.put("price", price);
        map.put("conditionalAmount", conditionalAmount);
        map.put("active", active);
        map.put("photoUrls", photoUrls != null ? photoUrls : new ArrayList<>());
        map.put("hotelAdminId", hotelAdminId);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }

    // ✅ MÉTODO CORREGIDO: Crear desde Map de Firebase con manejo de Timestamp
    public static HotelServiceModel fromMap(Map<String, Object> map, String documentId) {
        HotelServiceModel service = new HotelServiceModel();
        service.id = documentId;
        service.name = (String) map.get("name");
        service.description = (String) map.get("description");
        service.iconKey = (String) map.get("iconKey");
        service.serviceType = (String) map.get("serviceType");
        service.price = map.get("price") != null ? ((Number) map.get("price")).doubleValue() : 0.0;
        service.conditionalAmount = map.get("conditionalAmount") != null ? ((Number) map.get("conditionalAmount")).doubleValue() : 0.0;
        service.active = map.get("active") != null ? (Boolean) map.get("active") : true;
        service.photoUrls = (List<String>) map.get("photoUrls");
        service.hotelAdminId = (String) map.get("hotelAdminId");

        // ✅ CORREGIDO: Manejar Timestamp y Date correctamente
        service.createdAt = convertToDate(map.get("createdAt"));
        service.updatedAt = convertToDate(map.get("updatedAt"));

        if (service.photoUrls == null) {
            service.photoUrls = new ArrayList<>();
        }

        return service;
    }

    // ✅ NUEVO MÉTODO: Convertir Timestamp o Date a Date
    private static Date convertToDate(Object dateObject) {
        if (dateObject == null) {
            return new Date(); // Fecha actual por defecto
        }

        if (dateObject instanceof Timestamp) {
            // Convertir Firebase Timestamp a Date
            return ((Timestamp) dateObject).toDate();
        } else if (dateObject instanceof Date) {
            // Ya es un Date
            return (Date) dateObject;
        } else if (dateObject instanceof Long) {
            // Timestamp en millisegundos
            return new Date((Long) dateObject);
        } else {
            // Tipo desconocido, usar fecha actual
            return new Date();
        }
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconKey() { return iconKey; }
    public void setIconKey(String iconKey) { this.iconKey = iconKey; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getConditionalAmount() { return conditionalAmount; }
    public void setConditionalAmount(double conditionalAmount) { this.conditionalAmount = conditionalAmount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }

    public String getHotelAdminId() { return hotelAdminId; }
    public void setHotelAdminId(String hotelAdminId) { this.hotelAdminId = hotelAdminId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}