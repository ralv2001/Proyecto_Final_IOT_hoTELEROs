package com.example.proyecto_final_hoteleros.adminhotel.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class HotelProfile implements Parcelable {
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

    // ✅ IMPLEMENTACIÓN PARCELABLE
    protected HotelProfile(Parcel in) {
        id = in.readString();
        hotelAdminId = in.readString();
        name = in.readString();
        address = in.readString();
        locationName = in.readString();
        fullAddress = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        departamento = in.readString();
        provincia = in.readString();
        distrito = in.readString();
        photoUrls = new ArrayList<>();
        in.readList(photoUrls, String.class.getClassLoader());
        isActive = in.readByte() != 0;
        createdAt = in.readLong();
        Long tmpActivatedAt = in.readLong();
        activatedAt = tmpActivatedAt == -1 ? null : tmpActivatedAt;
    }

    public static final Creator<HotelProfile> CREATOR = new Creator<HotelProfile>() {
        @Override
        public HotelProfile createFromParcel(Parcel in) {
            return new HotelProfile(in);
        }

        @Override
        public HotelProfile[] newArray(int size) {
            return new HotelProfile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(hotelAdminId);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(locationName);
        dest.writeString(fullAddress);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(departamento);
        dest.writeString(provincia);
        dest.writeString(distrito);
        dest.writeList(photoUrls);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeLong(createdAt);
        dest.writeLong(activatedAt != null ? activatedAt : -1);
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
     * Obtiene la primera foto como imagen de preview
     */
    public String getPreviewImageUrl() {
        return hasPhotos() ? photoUrls.get(0) : null;
    }

    // ========== NUEVOS MÉTODOS DE UTILIDAD PARA UBICACIÓN ==========

    /**
     * Verifica si el hotel tiene coordenadas válidas
     */
    public boolean hasValidLocation() {
        return latitude != 0.0 && longitude != 0.0;
    }

    /**
     * Obtiene la dirección para mostrar (prioriza fullAddress)
     */
    public String getDisplayAddress() {
        if (fullAddress != null && !fullAddress.trim().isEmpty()) {
            return fullAddress;
        }
        return address != null ? address : "";
    }

    /**
     * Obtiene la ubicación completa en formato jerárquico
     */
    public String getHierarchicalLocation() {
        StringBuilder location = new StringBuilder();

        if (distrito != null && !distrito.trim().isEmpty()) {
            location.append(distrito);
        }

        if (provincia != null && !provincia.trim().isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(provincia);
        }

        if (departamento != null && !departamento.trim().isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(departamento);
        }

        return location.length() > 0 ? location.toString() : getDisplayAddress();
    }

    /**
     * Verifica si el hotel está completamente configurado
     */
    public boolean isFullyConfigured() {
        return name != null && !name.trim().isEmpty() &&
                hasValidLocation() &&
                hasPhotos() &&
                hotelAdminId != null && !hotelAdminId.trim().isEmpty();
    }

    /**
     * Obtiene un resumen del estado del hotel
     */
    public String getConfigurationStatus() {
        if (isFullyConfigured()) {
            return isActive ? "Hotel activo y completamente configurado" : "Hotel configurado, pendiente activación";
        }

        List<String> missing = new ArrayList<>();
        if (name == null || name.trim().isEmpty()) missing.add("nombre");
        if (!hasValidLocation()) missing.add("ubicación");
        if (!hasPhotos()) missing.add("fotos");
        if (hotelAdminId == null) missing.add("administrador");

        return "Configuración incompleta: falta " + String.join(", ", missing);
    }

    /**
     * ✅ NUEVO: Calcula la distancia entre el hotel y unas coordenadas dadas
     * Utiliza la fórmula de Haversine para calcular distancias geográficas
     * @param lat Latitud de destino
     * @param lon Longitud de destino
     * @return Distancia en kilómetros
     */
    public double getDistanceTo(double lat, double lon) {
        if (!hasValidLocation()) {
            return Double.MAX_VALUE; // Retorna distancia máxima si no tiene ubicación válida
        }

        final int R = 6371; // Radio de la Tierra en kilómetros

        double latDistance = Math.toRadians(lat - this.latitude);
        double lonDistance = Math.toRadians(lon - this.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(lat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distancia en kilómetros
    }

    /**
     * ✅ NUEVO: Obtiene la distancia formateada como texto
     * @param lat Latitud de destino
     * @param lon Longitud de destino
     * @return Distancia formateada (ej: "2.5 km", "850 m")
     */
    public String getFormattedDistanceTo(double lat, double lon) {
        double distance = getDistanceTo(lat, lon);

        if (distance == Double.MAX_VALUE) {
            return "Ubicación no disponible";
        }

        if (distance < 1.0) {
            return String.format("%.0f m", distance * 1000);
        } else {
            return String.format("%.1f km", distance);
        }
    }

    @Override
    public String toString() {
        return "HotelProfile{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", isActive=" + isActive +
                ", location=" + getHierarchicalLocation() +
                ", photos=" + getPhotoCount() +
                '}';
    }
}