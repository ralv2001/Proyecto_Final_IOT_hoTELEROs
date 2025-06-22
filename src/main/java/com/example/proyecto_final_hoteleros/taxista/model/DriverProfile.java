// DriverProfile.java
package com.example.proyecto_final_hoteleros.taxista.model;

import android.os.Parcel;
import android.os.Parcelable;

public class DriverProfile implements Parcelable {
    private String driverId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private String address;
    private String licenseNumber;
    private boolean isActive;
    private boolean isAvailable;

    // Estadísticas
    private float averageRating;
    private int totalTrips;
    private int completedTrips;
    private double monthlyEarnings;

    public DriverProfile(String driverId, String fullName, String email, String phoneNumber,
                         String profileImageUrl, String address, String licenseNumber,
                         boolean isActive, boolean isAvailable, float averageRating,
                         int totalTrips, int completedTrips, double monthlyEarnings) {
        this.driverId = driverId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
        this.address = address;
        this.licenseNumber = licenseNumber;
        this.isActive = isActive;
        this.isAvailable = isAvailable;
        this.averageRating = averageRating;
        this.totalTrips = totalTrips;
        this.completedTrips = completedTrips;
        this.monthlyEarnings = monthlyEarnings;
    }

    protected DriverProfile(Parcel in) {
        driverId = in.readString();
        fullName = in.readString();
        email = in.readString();
        phoneNumber = in.readString();
        profileImageUrl = in.readString();
        address = in.readString();
        licenseNumber = in.readString();
        isActive = in.readByte() != 0;
        isAvailable = in.readByte() != 0;
        averageRating = in.readFloat();
        totalTrips = in.readInt();
        completedTrips = in.readInt();
        monthlyEarnings = in.readDouble();
    }

    public static final Creator<DriverProfile> CREATOR = new Creator<DriverProfile>() {
        @Override
        public DriverProfile createFromParcel(Parcel in) {
            return new DriverProfile(in);
        }

        @Override
        public DriverProfile[] newArray(int size) {
            return new DriverProfile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(driverId);
        dest.writeString(fullName);
        dest.writeString(email);
        dest.writeString(phoneNumber);
        dest.writeString(profileImageUrl);
        dest.writeString(address);
        dest.writeString(licenseNumber);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeByte((byte) (isAvailable ? 1 : 0));
        dest.writeFloat(averageRating);
        dest.writeInt(totalTrips);
        dest.writeInt(completedTrips);
        dest.writeDouble(monthlyEarnings);
    }

    // Getters y setters
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

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

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public float getAverageRating() { return averageRating; }
    public void setAverageRating(float averageRating) { this.averageRating = averageRating; }

    public int getTotalTrips() { return totalTrips; }
    public void setTotalTrips(int totalTrips) { this.totalTrips = totalTrips; }

    public int getCompletedTrips() { return completedTrips; }
    public void setCompletedTrips(int completedTrips) { this.completedTrips = completedTrips; }

    public double getMonthlyEarnings() { return monthlyEarnings; }
    public void setMonthlyEarnings(double monthlyEarnings) { this.monthlyEarnings = monthlyEarnings; }
}