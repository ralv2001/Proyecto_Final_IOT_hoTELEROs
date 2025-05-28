package com.example.proyecto_final_hoteleros.taxista.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SolicitudViaje implements Parcelable {
    private String id;
    private String hotelName;
    private String location;
    private float rating;
    private String status;
    private String dates;
    private String dateRange;
    private String district;
    private String hotelAddress;
    private String imageUrl;
    private double price;
    private String notes;
    private boolean isUrgent;
    private String clientName;
    private String originAddress;
    private String destinationAddress;
    private int estimatedTime;

    // Constructor existente
    public SolicitudViaje(String id, String hotelName, float rating, String status, String dates,
                          String dateRange, String district, String hotelAddress, String imageUrl,
                          double price, String notes, boolean isUrgent, String clientName,
                          String origin, String destination, int estimatedTime) {
        this.id = id;
        this.hotelName = hotelName;
        this.rating = rating;
        this.status = status;
        this.dates = dates;
        this.dateRange = dateRange;
        this.district = district;
        this.location = district; // Inicializar location con district
        this.hotelAddress = hotelAddress;
        this.imageUrl = imageUrl;
        this.price = price;
        this.notes = notes;
        this.isUrgent = isUrgent;
        this.clientName = clientName;
        this.originAddress = origin;
        this.destinationAddress = destination;
        this.estimatedTime = estimatedTime;
    }

    // Constructor para Parcelable
    protected SolicitudViaje(Parcel in) {
        id = in.readString();
        hotelName = in.readString();
        location = in.readString();
        rating = in.readFloat();
        status = in.readString();
        dates = in.readString();
        dateRange = in.readString();
        district = in.readString();
        hotelAddress = in.readString();
        imageUrl = in.readString();
        price = in.readDouble();
        notes = in.readString();
        isUrgent = in.readByte() != 0;
        clientName = in.readString();
        originAddress = in.readString();
        destinationAddress = in.readString();
        estimatedTime = in.readInt();
    }

    // Implementación del Creator para Parcelable
    public static final Creator<SolicitudViaje> CREATOR = new Creator<SolicitudViaje>() {
        @Override
        public SolicitudViaje createFromParcel(Parcel in) {
            return new SolicitudViaje(in);
        }

        @Override
        public SolicitudViaje[] newArray(int size) {
            return new SolicitudViaje[size];
        }
    };

    // Método para describir el contenido del objeto Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    // Método para escribir los datos en el Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(hotelName);
        dest.writeString(location);
        dest.writeFloat(rating);
        dest.writeString(status);
        dest.writeString(dates);
        dest.writeString(dateRange);
        dest.writeString(district);
        dest.writeString(hotelAddress);
        dest.writeString(imageUrl);
        dest.writeDouble(price);
        dest.writeString(notes);
        dest.writeByte((byte) (isUrgent ? 1 : 0));
        dest.writeString(clientName);
        dest.writeString(originAddress);
        dest.writeString(destinationAddress);
        dest.writeInt(estimatedTime);
    }

    // Getters y setters completos
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDates() {
        return dates;
    }

    public void setDates(String dates) {
        this.dates = dates;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getHotelAddress() {
        return hotelAddress;
    }

    public void setHotelAddress(String hotelAddress) {
        this.hotelAddress = hotelAddress;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isUrgent() {
        return isUrgent;
    }

    public void setUrgent(boolean urgent) {
        isUrgent = urgent;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getOriginAddress() {
        return originAddress;
    }

    public void setOriginAddress(String originAddress) {
        this.originAddress = originAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public int getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(int estimatedTime) {
        this.estimatedTime = estimatedTime;
    }
}