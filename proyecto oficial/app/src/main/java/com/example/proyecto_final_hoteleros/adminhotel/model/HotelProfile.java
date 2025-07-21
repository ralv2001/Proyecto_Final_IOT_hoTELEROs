package com.example.proyecto_final_hoteleros.adminhotel.model;

import java.util.List;

public class HotelProfile {
    private String id;
    private String hotelAdminId;
    private String name;
    private String address;

    // ✅ NUEVOS CAMPOS DE UBICACIÓN INTEGRADOS
    private String locationName;        // Nombre corto de la ubicación
    private String fullAddress;         // Dirección completa
    private double latitude;            // Coordenada latitud
    private double longitude;           // Coordenada longitud
    private String departamento;        // Departamento/Estado
    private String provincia;           // Provincia/Ciudad
    private String distrito;            // Distrito/Área

    private List<String> photoUrls;
    private boolean isActive;
    private long createdAt;
    private Long activatedAt;

    // ✅ Constructor completo para Firebase - ACTUALIZADO
    public HotelProfile(String id, String hotelAdminId, String name, String address,
                        String locationName, String fullAddress, double latitude, double longitude,
                        String departamento, String provincia, String distrito,
                        List<String> photoUrls, boolean isActive, long createdAt, Long activatedAt) {
        this.id = id;
        this.hotelAdminId = hotelAdminId;
        this.name = name;
        this.address = address;
        this.locationName = locationName;
        this.fullAddress = fullAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.departamento = departamento;
        this.provincia = provincia;
        this.distrito = distrito;
        this.photoUrls = photoUrls;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.activatedAt = activatedAt;
    }

    // ✅ Constructor simplificado para crear nuevos hoteles - ACTUALIZADO
    public HotelProfile(String name, String address, List<String> photoUrls) {
        this.name = name;
        this.address = address;
        this.photoUrls = photoUrls;
        this.isActive = false;
        this.createdAt = System.currentTimeMillis();
        this.activatedAt = null;
        // Ubicación vacía inicialmente
        this.locationName = "";
        this.fullAddress = address; // Usar address como fallback
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.departamento = "";
        this.provincia = "";
        this.distrito = "";
    }

    // ✅ Constructor vacío para Firebase
    public HotelProfile() {
        this.isActive = false;
        this.createdAt = System.currentTimeMillis();
        this.activatedAt = null;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.locationName = "";
        this.fullAddress = "";
        this.departamento = "";
        this.provincia = "";
        this.distrito = "";
    }

    // ========== GETTERS Y SETTERS ORIGINALES ==========

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

    // ========== NUEVOS GETTERS Y SETTERS DE UBICACIÓN ==========

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    // ========== MÉTODOS DE UTILIDAD ORIGINALES ==========

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
     * Verifica si la información básica está completa - ACTUALIZADO
     */
    public boolean isBasicInfoComplete() {
        return name != null && !name.trim().isEmpty() &&
                address != null && !address.trim().isEmpty() &&
                hasValidLocation();
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

    // ========== NUEVOS MÉTODOS DE UTILIDAD DE UBICACIÓN ==========

    /**
     * Verifica si tiene una ubicación válida con coordenadas
     */
    public boolean hasValidLocation() {
        return latitude != 0.0 && longitude != 0.0 &&
                locationName != null && !locationName.trim().isEmpty() &&
                fullAddress != null && !fullAddress.trim().isEmpty();
    }

    /**
     * Verifica si tiene información de ubicación política (departamento, provincia, distrito)
     */
    public boolean hasLocationComponents() {
        return departamento != null && !departamento.trim().isEmpty() &&
                provincia != null && !provincia.trim().isEmpty() &&
                distrito != null && !distrito.trim().isEmpty();
    }

    /**
     * Actualiza toda la información de ubicación de una vez
     */
    public void updateLocation(String locationName, String fullAddress,
                               double latitude, double longitude) {
        this.locationName = locationName;
        this.fullAddress = fullAddress;
        this.latitude = latitude;
        this.longitude = longitude;

        // Actualizar también el address principal para compatibilidad
        this.address = fullAddress;

        // Parsear componentes de ubicación
        parseLocationComponents(fullAddress);
    }

    /**
     * Parsea los componentes de ubicación desde la dirección completa
     */
    private void parseLocationComponents(String address) {
        if (address == null || address.trim().isEmpty()) {
            return;
        }

        // Lógica para extraer departamento, provincia, distrito
        // Ejemplo: "Av. José Larco 123, Miraflores, Lima, Perú"
        String[] parts = address.split(", ");
        if (parts.length >= 3) {
            // Último elemento generalmente es el país, lo ignoramos
            this.distrito = parts[parts.length - 3].trim();
            this.provincia = parts[parts.length - 2].trim();

            // Para Perú, muchas veces provincia y departamento son lo mismo
            if (parts.length >= 4) {
                this.departamento = parts[parts.length - 3].trim();
            } else {
                this.departamento = this.provincia; // Fallback
            }
        }
    }

    /**
     * Obtiene una representación corta de la ubicación
     */
    public String getLocationSummary() {
        if (hasLocationComponents()) {
            return distrito + ", " + provincia;
        } else if (locationName != null && !locationName.trim().isEmpty()) {
            return locationName;
        } else {
            return address != null ? address : "Ubicación no disponible";
        }
    }

    /**
     * Calcula la distancia a otro punto (en kilómetros)
     */
    public double getDistanceTo(double otherLatitude, double otherLongitude) {
        if (!hasValidLocation()) {
            return Double.MAX_VALUE;
        }

        final int R = 6371; // Radio de la Tierra en kilómetros

        double latDistance = Math.toRadians(otherLatitude - this.latitude);
        double lonDistance = Math.toRadians(otherLongitude - this.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(otherLatitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    @Override
    public String toString() {
        return "HotelProfile{" +
                "id='" + id + '\'' +
                ", hotelAdminId='" + hotelAdminId + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", locationName='" + locationName + '\'' +
                ", coordinates=[" + latitude + ", " + longitude + "]" +
                ", photoCount=" + getPhotoCount() +
                ", isActive=" + isActive +
                ", status='" + getStatusString() + '\'' +
                ", hasValidLocation=" + hasValidLocation() +
                '}';
    }
}