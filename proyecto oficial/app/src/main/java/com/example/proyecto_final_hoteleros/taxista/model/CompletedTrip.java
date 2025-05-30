package com.example.proyecto_final_hoteleros.taxista.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CompletedTrip implements Parcelable {
    private String id;
    private String hotelName;
    private String clientName;
    private String originAddress;
    private String destinationAddress;
    private String completedDate;
    private String completedTime;
    private double totalAmount;
    private int duration; // en minutos
    private double distance; // en kilómetros
    private float clientRating;
    private String paymentMethod;
    private String status; // "Completado", "Cancelado"
    private String notes;
    private String hotelImageUrl;
    private double earnings; // Ganancia del conductor
    private String tripType; // "Hotel-Aeropuerto", "Aeropuerto-Hotel", "Otros"

    public CompletedTrip(String id, String hotelName, String clientName, String originAddress,
                         String destinationAddress, String completedDate, String completedTime,
                         double totalAmount, int duration, double distance, float clientRating,
                         String paymentMethod, String status, String notes, String hotelImageUrl,
                         double earnings, String tripType) {
        this.id = id;
        this.hotelName = hotelName;
        this.clientName = clientName;
        this.originAddress = originAddress;
        this.destinationAddress = destinationAddress;
        this.completedDate = completedDate;
        this.completedTime = completedTime;
        this.totalAmount = totalAmount;
        this.duration = duration;
        this.distance = distance;
        this.clientRating = clientRating;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.notes = notes;
        this.hotelImageUrl = hotelImageUrl;
        this.earnings = earnings;
        this.tripType = tripType;
    }

    protected CompletedTrip(Parcel in) {
        id = in.readString();
        hotelName = in.readString();
        clientName = in.readString();
        originAddress = in.readString();
        destinationAddress = in.readString();
        completedDate = in.readString();
        completedTime = in.readString();
        totalAmount = in.readDouble();
        duration = in.readInt();
        distance = in.readDouble();
        clientRating = in.readFloat();
        paymentMethod = in.readString();
        status = in.readString();
        notes = in.readString();
        hotelImageUrl = in.readString();
        earnings = in.readDouble();
        tripType = in.readString();
    }

    public static final Creator<CompletedTrip> CREATOR = new Creator<CompletedTrip>() {
        @Override
        public CompletedTrip createFromParcel(Parcel in) {
            return new CompletedTrip(in);
        }

        @Override
        public CompletedTrip[] newArray(int size) {
            return new CompletedTrip[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(hotelName);
        dest.writeString(clientName);
        dest.writeString(originAddress);
        dest.writeString(destinationAddress);
        dest.writeString(completedDate);
        dest.writeString(completedTime);
        dest.writeDouble(totalAmount);
        dest.writeInt(duration);
        dest.writeDouble(distance);
        dest.writeFloat(clientRating);
        dest.writeString(paymentMethod);
        dest.writeString(status);
        dest.writeString(notes);
        dest.writeString(hotelImageUrl);
        dest.writeDouble(earnings);
        dest.writeString(tripType);
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getOriginAddress() { return originAddress; }
    public void setOriginAddress(String originAddress) { this.originAddress = originAddress; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public String getCompletedDate() { return completedDate; }
    public void setCompletedDate(String completedDate) { this.completedDate = completedDate; }

    public String getCompletedTime() { return completedTime; }
    public void setCompletedTime(String completedTime) { this.completedTime = completedTime; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public float getClientRating() { return clientRating; }
    public void setClientRating(float clientRating) { this.clientRating = clientRating; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getHotelImageUrl() { return hotelImageUrl; }
    public void setHotelImageUrl(String hotelImageUrl) { this.hotelImageUrl = hotelImageUrl; }

    public double getEarnings() { return earnings; }
    public void setEarnings(double earnings) { this.earnings = earnings; }

    public String getTripType() { return tripType; }
    public void setTripType(String tripType) { this.tripType = tripType; }

    // Métodos de utilidad
    public String getFormattedDistance() {
        return String.format("%.1f km", distance);
    }

    public String getFormattedDuration() {
        int hours = duration / 60;
        int minutes = duration % 60;
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%d min", minutes);
        }
    }

    public String getFormattedDateTime() {
        return completedDate + " - " + completedTime;
    }

    public boolean isCompleted() {
        return "Completado".equals(status);
    }

    public boolean isCancelled() {
        return "Cancelado".equals(status);
    }
}