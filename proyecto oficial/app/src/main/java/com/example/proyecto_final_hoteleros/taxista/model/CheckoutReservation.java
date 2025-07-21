package com.example.proyecto_final_hoteleros.taxista.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.Map;

public class CheckoutReservation implements Parcelable {
    private String id;
    private String hotelName;
    private String hotelAddress;
    private String hotelPhone;
    private String clientName;
    private String clientPhone;
    private String clientEmail;
    private String checkoutDate;
    private String checkoutTime;
    private String roomNumber;
    private String roomType;
    private boolean freeTransport;
    private String assignedDriverId;
    private String taxiStatus; // "pending", "assigned", "in_progress", "completed", "cancelled"
    private double estimatedDistance;
    private int estimatedDuration;
    private long createdAt;
    private long assignedAt;
    private long serviceStartTime;
    private long serviceCompletedTime;
    private String destinationAddress;
    private String notes;

    // Constructor vacío (requerido para Firebase)
    public CheckoutReservation() {
        this.destinationAddress = "Aeropuerto Internacional Jorge Chávez, Callao";
        this.freeTransport = true;
        this.taxiStatus = "pending";
        this.createdAt = System.currentTimeMillis();
    }

    // Constructor completo
    public CheckoutReservation(String id, String hotelName, String hotelAddress, String hotelPhone,
                               String clientName, String clientPhone, String clientEmail,
                               String checkoutDate, String checkoutTime, String roomNumber,
                               String roomType, boolean freeTransport) {
        this();
        this.id = id;
        this.hotelName = hotelName;
        this.hotelAddress = hotelAddress;
        this.hotelPhone = hotelPhone;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.clientEmail = clientEmail;
        this.checkoutDate = checkoutDate;
        this.checkoutTime = checkoutTime;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.freeTransport = freeTransport;
    }

    // Constructor para Parcelable
    protected CheckoutReservation(Parcel in) {
        id = in.readString();
        hotelName = in.readString();
        hotelAddress = in.readString();
        hotelPhone = in.readString();
        clientName = in.readString();
        clientPhone = in.readString();
        clientEmail = in.readString();
        checkoutDate = in.readString();
        checkoutTime = in.readString();
        roomNumber = in.readString();
        roomType = in.readString();
        freeTransport = in.readByte() != 0;
        assignedDriverId = in.readString();
        taxiStatus = in.readString();
        estimatedDistance = in.readDouble();
        estimatedDuration = in.readInt();
        createdAt = in.readLong();
        assignedAt = in.readLong();
        serviceStartTime = in.readLong();
        serviceCompletedTime = in.readLong();
        destinationAddress = in.readString();
        notes = in.readString();
    }

    // Método para crear desde DocumentSnapshot de Firebase
    public static CheckoutReservation fromDocumentSnapshot(DocumentSnapshot document) {
        CheckoutReservation reservation = new CheckoutReservation();
        reservation.setId(document.getId());

        Map<String, Object> data = document.getData();
        if (data != null) {
            reservation.setHotelName((String) data.get("hotelName"));
            reservation.setHotelAddress((String) data.get("hotelAddress"));
            reservation.setHotelPhone((String) data.get("hotelPhone"));
            reservation.setClientName((String) data.get("clientName"));
            reservation.setClientPhone((String) data.get("clientPhone"));
            reservation.setClientEmail((String) data.get("clientEmail"));
            reservation.setCheckoutDate((String) data.get("checkoutDate"));
            reservation.setCheckoutTime((String) data.get("checkoutTime"));
            reservation.setRoomNumber((String) data.get("roomNumber"));
            reservation.setRoomType((String) data.get("roomType"));
            reservation.setFreeTransport(Boolean.TRUE.equals(data.get("freeTransport")));
            reservation.setAssignedDriverId((String) data.get("assignedDriverId"));
            reservation.setTaxiStatus((String) data.get("taxiStatus"));
            reservation.setNotes((String) data.get("notes"));

            // Campos numéricos con valores por defecto
            Object distance = data.get("estimatedDistance");
            reservation.setEstimatedDistance(distance instanceof Number ? ((Number) distance).doubleValue() : 15.5);

            Object duration = data.get("estimatedDuration");
            reservation.setEstimatedDuration(duration instanceof Number ? ((Number) duration).intValue() : 25);

            Object created = data.get("createdAt");
            reservation.setCreatedAt(created instanceof Number ? ((Number) created).longValue() : System.currentTimeMillis());

            Object assigned = data.get("assignedAt");
            reservation.setAssignedAt(assigned instanceof Number ? ((Number) assigned).longValue() : 0);

            Object startTime = data.get("serviceStartTime");
            reservation.setServiceStartTime(startTime instanceof Number ? ((Number) startTime).longValue() : 0);

            Object completedTime = data.get("serviceCompletedTime");
            reservation.setServiceCompletedTime(completedTime instanceof Number ? ((Number) completedTime).longValue() : 0);
        }

        return reservation;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public String getHotelAddress() { return hotelAddress; }
    public void setHotelAddress(String hotelAddress) { this.hotelAddress = hotelAddress; }

    public String getHotelPhone() { return hotelPhone; }
    public void setHotelPhone(String hotelPhone) { this.hotelPhone = hotelPhone; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }

    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }

    public String getCheckoutDate() { return checkoutDate; }
    public void setCheckoutDate(String checkoutDate) { this.checkoutDate = checkoutDate; }

    public String getCheckoutTime() { return checkoutTime; }
    public void setCheckoutTime(String checkoutTime) { this.checkoutTime = checkoutTime; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public boolean isFreeTransport() { return freeTransport; }
    public void setFreeTransport(boolean freeTransport) { this.freeTransport = freeTransport; }

    public String getAssignedDriverId() { return assignedDriverId; }
    public void setAssignedDriverId(String assignedDriverId) { this.assignedDriverId = assignedDriverId; }

    public String getTaxiStatus() { return taxiStatus; }
    public void setTaxiStatus(String taxiStatus) { this.taxiStatus = taxiStatus; }

    public double getEstimatedDistance() { return estimatedDistance; }
    public void setEstimatedDistance(double estimatedDistance) { this.estimatedDistance = estimatedDistance; }

    public int getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(int estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getAssignedAt() { return assignedAt; }
    public void setAssignedAt(long assignedAt) { this.assignedAt = assignedAt; }

    public long getServiceStartTime() { return serviceStartTime; }
    public void setServiceStartTime(long serviceStartTime) { this.serviceStartTime = serviceStartTime; }

    public long getServiceCompletedTime() { return serviceCompletedTime; }
    public void setServiceCompletedTime(long serviceCompletedTime) { this.serviceCompletedTime = serviceCompletedTime; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Métodos de utilidad
    public boolean isPending() {
        return "pending".equals(taxiStatus);
    }

    public boolean isAssigned() {
        return "assigned".equals(taxiStatus);
    }

    public boolean isInProgress() {
        return "in_progress".equals(taxiStatus);
    }

    public boolean isCompleted() {
        return "completed".equals(taxiStatus);
    }

    public String getFormattedDateTime() {
        return checkoutDate + " a las " + checkoutTime;
    }

    // Implementación Parcelable
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(hotelName);
        dest.writeString(hotelAddress);
        dest.writeString(hotelPhone);
        dest.writeString(clientName);
        dest.writeString(clientPhone);
        dest.writeString(clientEmail);
        dest.writeString(checkoutDate);
        dest.writeString(checkoutTime);
        dest.writeString(roomNumber);
        dest.writeString(roomType);
        dest.writeByte((byte) (freeTransport ? 1 : 0));
        dest.writeString(assignedDriverId);
        dest.writeString(taxiStatus);
        dest.writeDouble(estimatedDistance);
        dest.writeInt(estimatedDuration);
        dest.writeLong(createdAt);
        dest.writeLong(assignedAt);
        dest.writeLong(serviceStartTime);
        dest.writeLong(serviceCompletedTime);
        dest.writeString(destinationAddress);
        dest.writeString(notes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CheckoutReservation> CREATOR = new Creator<CheckoutReservation>() {
        @Override
        public CheckoutReservation createFromParcel(Parcel in) {
            return new CheckoutReservation(in);
        }

        @Override
        public CheckoutReservation[] newArray(int size) {
            return new CheckoutReservation[size];
        }
    };

    @Override
    public String toString() {
        return "CheckoutReservation{" +
                "id='" + id + '\'' +
                ", hotelName='" + hotelName + '\'' +
                ", clientName='" + clientName + '\'' +
                ", checkoutTime='" + checkoutTime + '\'' +
                ", taxiStatus='" + taxiStatus + '\'' +
                '}';
    }
}