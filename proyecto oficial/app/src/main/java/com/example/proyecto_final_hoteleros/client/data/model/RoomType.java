// client/data/model/RoomType.java
package com.example.proyecto_final_hoteleros.client.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoomType implements Parcelable {
    private String name;
    private int size;
    private String price;
    private int imageResource;
    private List<String> includedServiceIds; // ✅ NUEVO: IDs de servicios incluidos
    private String description;
    private List<String> features; // ✅ NUEVO: Características específicas del cuarto

    public RoomType(String name, int size, String price, int imageResource) {
        this.name = name;
        this.size = size;
        this.price = price;
        this.imageResource = imageResource;
        this.includedServiceIds = new ArrayList<>();
        this.features = new ArrayList<>();
        this.description = "";
        setupDefaultServicesAndFeatures();
    }

    public RoomType(String name, int size, String price, int imageResource,
                    List<String> includedServiceIds, String description, List<String> features) {
        this.name = name;
        this.size = size;
        this.price = price;
        this.imageResource = imageResource;
        this.includedServiceIds = includedServiceIds != null ? includedServiceIds : new ArrayList<>();
        this.description = description != null ? description : "";
        this.features = features != null ? features : new ArrayList<>();
    }

    private void setupDefaultServicesAndFeatures() {
        // ✅ SERVICIOS BÁSICOS QUE TODOS LOS CUARTOS TIENEN (ya existe en tu código)
        includedServiceIds.add("wifi");
        includedServiceIds.add("reception");
        includedServiceIds.add("parking");
        includedServiceIds.add("pool"); // ✅ AGREGAR pool como básico

        // ✅ MEJORAR las características según el tipo (tu estructura ya existe)
        switch (name) {
            case "Habitación Estándar":
                // Solo servicios básicos
                features.add("TV de 32 pulgadas");
                features.add("Aire acondicionado");
                features.add("Baño privado");
                features.add("Escritorio de trabajo");
                description = "Habitación cómoda con servicios esenciales incluidos.";
                break;

            case "Habitación Deluxe":
                // ✅ AGREGAR servicios específicos de Deluxe
                includedServiceIds.add("minibar");
                features.add("TV de 42 pulgadas");
                features.add("Aire acondicionado");
                features.add("Baño privado con bañera");
                features.add("Vista a la ciudad");
                features.add("Minibar premium incluido"); // ✅ ACLARAR que está incluido
                description = "Habitación espaciosa con minibar incluido y vista espectacular.";
                break;

            case "Suite Junior":
                // ✅ SERVICIOS ESPECÍFICOS de Suite Junior
                includedServiceIds.add("minibar");
                includedServiceIds.add("room_service");
                features.add("TV de 50 pulgadas");
                features.add("Sala de estar separada");
                features.add("Baño con jacuzzi");
                features.add("Minibar premium incluido");
                features.add("Room service 24/7 incluido"); // ✅ ACLARAR que está incluido
                description = "Suite elegante con servicios de lujo incluidos.";
                break;

            case "Suite Presidencial":
                // ✅ TODOS LOS SERVICIOS PREMIUM
                includedServiceIds.add("minibar");
                includedServiceIds.add("room_service");
                includedServiceIds.add("laundry");
                features.add("TV de 65 pulgadas");
                features.add("Sala de estar amplia");
                features.add("Baño master con jacuzzi");
                features.add("Todos los servicios premium incluidos");
                features.add("Servicio de mayordomía personal");
                description = "La suite más exclusiva con servicios de lujo completos.";
                break;
        }
    }

    public List<String> getExclusiveServiceNames() {
        List<String> basicServices = Arrays.asList("wifi", "reception", "parking", "pool");
        List<String> exclusiveNames = new ArrayList<>();

        for (String serviceId : includedServiceIds) {
            if (!basicServices.contains(serviceId)) {
                switch (serviceId) {
                    case "minibar":
                        exclusiveNames.add("Minibar Premium");
                        break;
                    case "room_service":
                        exclusiveNames.add("Room Service 24/7");
                        break;
                    case "laundry":
                        exclusiveNames.add("Lavandería Express");
                        break;
                }
            }
        }
        return exclusiveNames;
    }

    // Getters existentes
    public String getName() { return name; }
    public int getSize() { return size; }
    public int getArea() { return size; }
    public String getPrice() { return price; }
    public int getImageResource() { return imageResource; }
    public int getImageResId() { return imageResource; }

    // ✅ NUEVOS getters y setters
    public List<String> getIncludedServiceIds() { return includedServiceIds; }
    public void setIncludedServiceIds(List<String> includedServiceIds) {
        this.includedServiceIds = includedServiceIds;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    public boolean hasService(String serviceId) {
        return includedServiceIds.contains(serviceId);
    }

    public int getIncludedServicesCount() {
        return includedServiceIds.size();
    }

    // Implementación Parcelable actualizada
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