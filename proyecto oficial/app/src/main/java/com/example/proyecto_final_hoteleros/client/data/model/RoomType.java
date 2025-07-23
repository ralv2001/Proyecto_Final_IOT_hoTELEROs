// client/data/model/RoomType.java - CORREGIDO: Sin servicios hardcodeados
package com.example.proyecto_final_hoteleros.client.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class RoomType implements Parcelable {
    private String name;
    private int size;
    private String price;
    private int imageResource; // Mantener para compatibilidad
    private List<String> includedServiceIds;
    private String description;
    private List<String> features;

    // ✅ NUEVO: Soporte para múltiples fotos reales desde Firebase
    private List<String> photoUrls;

    // ✅ Constructor básico SIN hardcoding de servicios
    public RoomType(String name, int size, String price, int imageResource) {
        this.name = name;
        this.size = size;
        this.price = price;
        this.imageResource = imageResource;
        this.includedServiceIds = new ArrayList<>();
        this.features = new ArrayList<>();
        this.description = "";
        this.photoUrls = new ArrayList<>();
        // ✅ ELIMINADO: setupDefaultServicesAndFeatures() - No más hardcoding
    }

    // Constructor con servicios incluidos (mantener compatibilidad)
    public RoomType(String name, int size, String price, int imageResource,
                    List<String> includedServiceIds, String description, List<String> features) {
        this.name = name;
        this.size = size;
        this.price = price;
        this.imageResource = imageResource;
        this.includedServiceIds = includedServiceIds != null ? includedServiceIds : new ArrayList<>();
        this.description = description != null ? description : "";
        this.features = features != null ? features : new ArrayList<>();
        this.photoUrls = new ArrayList<>();
        // ✅ ELIMINADO: No más configuración automática de servicios
    }

    // ✅ Constructor completo con fotos reales
    public RoomType(String name, int size, String price, int imageResource,
                    List<String> includedServiceIds, String description, List<String> features,
                    List<String> photoUrls) {
        this.name = name;
        this.size = size;
        this.price = price;
        this.imageResource = imageResource;
        this.includedServiceIds = includedServiceIds != null ? includedServiceIds : new ArrayList<>();
        this.description = description != null ? description : "";
        this.features = features != null ? features : new ArrayList<>();
        this.photoUrls = photoUrls != null ? photoUrls : new ArrayList<>();
        // ✅ ELIMINADO: No más configuración automática
    }

    // ✅ ELIMINADO COMPLETAMENTE: setupDefaultServicesAndFeatures()
    // Ahora los servicios vienen 100% desde Firebase

    // ✅ NUEVO: Métodos para gestión de fotos
    public boolean hasRealPhotos() {
        return photoUrls != null && !photoUrls.isEmpty();
    }

    public int getPhotoCount() {
        return photoUrls != null ? photoUrls.size() : 0;
    }

    public String getFirstPhotoUrl() {
        return hasRealPhotos() ? photoUrls.get(0) : null;
    }

    public List<String> getAllPhotoUrls() {
        return new ArrayList<>(photoUrls != null ? photoUrls : new ArrayList<>());
    }

    // Getters existentes
    public String getName() { return name; }
    public int getSize() { return size; }
    public int getArea() { return size; }
    public String getPrice() { return price; }
    public int getImageResource() { return imageResource; }
    public int getImageResId() { return imageResource; }

    // ✅ Getters y setters actualizados
    public List<String> getIncludedServiceIds() { return includedServiceIds; }
    public void setIncludedServiceIds(List<String> includedServiceIds) {
        this.includedServiceIds = includedServiceIds;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    // ✅ Getters y setters para fotos
    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls != null ? photoUrls : new ArrayList<>();
    }

    public boolean hasService(String serviceId) {
        return includedServiceIds.contains(serviceId);
    }

    public int getIncludedServicesCount() {
        return includedServiceIds.size();
    }

    // ✅ Implementación Parcelable actualizada con photoUrls
    protected RoomType(Parcel in) {
        name = in.readString();
        size = in.readInt();
        price = in.readString();
        imageResource = in.readInt();
        includedServiceIds = new ArrayList<>();
        in.readList(includedServiceIds, String.class.getClassLoader());
        description = in.readString();
        features = new ArrayList<>();
        in.readList(features, String.class.getClassLoader());
        // ✅ AGREGAR photoUrls al Parcelable
        photoUrls = new ArrayList<>();
        in.readList(photoUrls, String.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(size);
        dest.writeString(price);
        dest.writeInt(imageResource);
        dest.writeList(includedServiceIds);
        dest.writeString(description);
        dest.writeList(features);
        // ✅ AGREGAR photoUrls al Parcelable
        dest.writeList(photoUrls);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<RoomType> CREATOR = new Creator<RoomType>() {
        @Override
        public RoomType createFromParcel(Parcel in) { return new RoomType(in); }
        @Override
        public RoomType[] newArray(int size) { return new RoomType[size]; }
    };
}