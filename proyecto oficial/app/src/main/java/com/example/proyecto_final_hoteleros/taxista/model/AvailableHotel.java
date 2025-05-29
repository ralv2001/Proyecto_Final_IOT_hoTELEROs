package com.example.proyecto_final_hoteleros.taxista.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AvailableHotel implements Parcelable {
    private String id;
    private String name;
    private String address;
    private String district;
    private float rating;
    private String imageUrl;
    private double distanceKm;
    private String phoneNumber;
    private String partnershipInfo;
    private String description;
    private int totalRequests;
    private String status;
    private double latitude;
    private double longitude;

    public AvailableHotel(String id, String name, String address, String district, float rating,
                          String imageUrl, double distanceKm, String phoneNumber, String partnershipInfo,
                          String description, int totalRequests, String status, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.district = district;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.distanceKm = distanceKm;
        this.phoneNumber = phoneNumber;
        this.partnershipInfo = partnershipInfo;
        this.description = description;
        this.totalRequests = totalRequests;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected AvailableHotel(Parcel in) {
        id = in.readString();
        name = in.readString();
        address = in.readString();
        district = in.readString();
        rating = in.readFloat();
        imageUrl = in.readString();
        distanceKm = in.readDouble();
        phoneNumber = in.readString();
        partnershipInfo = in.readString();
        description = in.readString();
        totalRequests = in.readInt();
        status = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<AvailableHotel> CREATOR = new Creator<AvailableHotel>() {
        @Override
        public AvailableHotel createFromParcel(Parcel in) {
            return new AvailableHotel(in);
        }

        @Override
        public AvailableHotel[] newArray(int size) {
            return new AvailableHotel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(district);
        dest.writeFloat(rating);
        dest.writeString(imageUrl);
        dest.writeDouble(distanceKm);
        dest.writeString(phoneNumber);
        dest.writeString(partnershipInfo);
        dest.writeString(description);
        dest.writeInt(totalRequests);
        dest.writeString(status);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPartnershipInfo() { return partnershipInfo; }
    public void setPartnershipInfo(String partnershipInfo) { this.partnershipInfo = partnershipInfo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTotalRequests() { return totalRequests; }
    public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    // Método para obtener distancia formateada
    public String getFormattedDistance() {
        if (distanceKm < 1.0) {
            return String.format("%.0f m", distanceKm * 1000);
        } else {
            return String.format("%.1f km", distanceKm);
        }
    }

    @Override
    public String toString() {
        return "AvailableHotel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", district='" + district + '\'' +
                ", rating=" + rating +
                ", distanceKm=" + distanceKm +
                ", totalRequests=" + totalRequests +
                '}';
    }
}