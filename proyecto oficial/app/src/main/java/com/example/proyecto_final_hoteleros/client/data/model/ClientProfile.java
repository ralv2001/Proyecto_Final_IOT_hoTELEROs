package com.example.proyecto_final_hoteleros.client.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ClientProfile implements Parcelable {
    private String clientId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private String address;
    private boolean isActive;

    // Estadísticas del cliente
    private int totalReservations;
    private int completedStays;
    private float averageRating;
    private double totalSpent;

    public ClientProfile(String clientId, String fullName, String email, String phoneNumber,
                         String profileImageUrl, String address, boolean isActive,
                         int totalReservations, int completedStays, float averageRating, double totalSpent) {
        this.clientId = clientId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
        this.address = address;
        this.isActive = isActive;
        this.totalReservations = totalReservations;
        this.completedStays = completedStays;
        this.averageRating = averageRating;
        this.totalSpent = totalSpent;
    }

    protected ClientProfile(Parcel in) {
        clientId = in.readString();
        fullName = in.readString();
        email = in.readString();
        phoneNumber = in.readString();
        profileImageUrl = in.readString();
        address = in.readString();
        isActive = in.readByte() != 0;
        totalReservations = in.readInt();
        completedStays = in.readInt();
        averageRating = in.readFloat();
        totalSpent = in.readDouble();
    }

    public static final Creator<ClientProfile> CREATOR = new Creator<ClientProfile>() {
        @Override
        public ClientProfile createFromParcel(Parcel in) {
            return new ClientProfile(in);
        }

        @Override
        public ClientProfile[] newArray(int size) {
            return new ClientProfile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(clientId);
        dest.writeString(fullName);
        dest.writeString(email);
        dest.writeString(phoneNumber);
        dest.writeString(profileImageUrl);
        dest.writeString(address);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeInt(totalReservations);
        dest.writeInt(completedStays);
        dest.writeFloat(averageRating);
        dest.writeDouble(totalSpent);
    }

    // Getters y setters
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getTotalReservations() { return totalReservations; }
    public void setTotalReservations(int totalReservations) { this.totalReservations = totalReservations; }

    public int getCompletedStays() { return completedStays; }
    public void setCompletedStays(int completedStays) { this.completedStays = completedStays; }

    public float getAverageRating() { return averageRating; }
    public void setAverageRating(float averageRating) { this.averageRating = averageRating; }

    public double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }
}
