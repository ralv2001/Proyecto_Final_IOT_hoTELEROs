package com.example.proyecto_final_hoteleros.client.data.model;

public class NearbyPlace {
    private String placeId;
    private String name;
    private String vicinity;
    private double rating;
    private String photoReference;
    private double latitude;
    private double longitude;
    private String type;
    private boolean isOpen;
    private int priceLevel;

    public NearbyPlace(String placeId, String name, String vicinity, double rating,
                       String photoReference, double latitude, double longitude, String type) {
        this.placeId = placeId;
        this.name = name;
        this.vicinity = vicinity;
        this.rating = rating;
        this.photoReference = photoReference;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.isOpen = true;
        this.priceLevel = 0;
    }

    // Getters y setters
    public String getPlaceId() { return placeId; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVicinity() { return vicinity; }
    public void setVicinity(String vicinity) { this.vicinity = vicinity; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getPhotoReference() { return photoReference; }
    public void setPhotoReference(String photoReference) { this.photoReference = photoReference; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isOpen() { return isOpen; }
    public void setOpen(boolean open) { isOpen = open; }

    public int getPriceLevel() { return priceLevel; }
    public void setPriceLevel(int priceLevel) { this.priceLevel = priceLevel; }

    // Método para obtener URL de imagen
    public String getImageUrl(String apiKey) {
        if (photoReference != null && !photoReference.isEmpty()) {
            return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="
                    + photoReference + "&key=" + apiKey;
        }
        return null;
    }

    // Método para obtener distancia aproximada (puedes mejorarlo)
    public String getDistance() {
        // Aquí podrías calcular la distancia real si tienes las coordenadas del hotel
        return "500m"; // Placeholder
    }
}