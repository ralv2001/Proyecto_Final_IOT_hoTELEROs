package com.example.proyecto_final_hoteleros.adminhotel.model;

import java.util.List;

public class HotelProfile {
    private String id;
    private String hotelAdminId;
    private String name;
    private String address;
    private List<String> photoUrls; // ✅ CAMBIADO: URLs de fotos ya subidas, no URIs
    private boolean isActive;
    private long createdAt;
    private Long activatedAt; // Puede ser null si nunca se ha activado

    // ✅ Constructor completo para Firebase
    public HotelProfile(String id, String hotelAdminId, String name, String address,
                        List<String> photoUrls, boolean isActive, long createdAt, Long activatedAt) {
        this.id = id;
        this.hotelAdminId = hotelAdminId;
        this.name = name;
        this.address = address;
        this.photoUrls = photoUrls;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.activatedAt = activatedAt;
    }

    // ✅ Constructor simplificado para crear nuevos hoteles
    public HotelProfile(String name, String address, List<String> photoUrls) {
        this.name = name;
        this.address = address;
        this.photoUrls = photoUrls;
        this.isActive = false; // Por defecto no activo
        this.createdAt = System.currentTimeMillis();
        this.activatedAt = null;
    }

    // ✅ Constructor vacío para Firebase
    public HotelProfile() {
        this.isActive = false;
        this.createdAt = System.currentTimeMillis();
        this.activatedAt = null;
    }

    // ========== GETTERS Y SETTERS ==========

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHotelAdminId() {
        return hotelAdminId;
    }

    public void setHotelAdminId(String hotelAdminId) {
        this.hotelAdminId = hotelAdminId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(Long activatedAt) {
        this.activatedAt = activatedAt;
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Verifica si el hotel tiene fotos
     */
    public boolean hasPhotos() {
        return photoUrls != null && !photoUrls.isEmpty();
    }

    /**
     * Obtiene el número de fotos
     */
    public int getPhotoCount() {
        return photoUrls != null ? photoUrls.size() : 0;
    }

    /**
     * Verifica si la información básica está completa
     */
    public boolean isBasicInfoComplete() {
        return name != null && !name.trim().isEmpty() &&
                address != null && !address.trim().isEmpty();
    }

    /**
     * Verifica si el hotel tiene suficientes fotos para activación
     */
    public boolean hasEnoughPhotos(int minRequired) {
        return getPhotoCount() >= minRequired;
    }

    /**
     * Verifica si alguna vez ha sido activado
     */
    public boolean hasBeenActivated() {
        return activatedAt != null;
    }

    /**
     * Obtiene el estado del hotel como string
     */
    public String getStatusString() {
        if (isActive) {
            return "Activo";
        } else if (hasBeenActivated()) {
            return "Desactivado";
        } else {
            return "En Configuración";
        }
    }

    @Override
    public String toString() {
        return "HotelProfile{" +
                "id='" + id + '\'' +
                ", hotelAdminId='" + hotelAdminId + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", photoCount=" + getPhotoCount() +
                ", isActive=" + isActive +
                ", status='" + getStatusString() + '\'' +
                '}';
    }
}