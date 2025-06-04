package com.example.proyecto_final_hoteleros.client.data.model;

public class ChatSummary {
    private String id;
    private String hotelId;
    private String hotelName;
    private String reservationId;
    private String reservationDates;
    private String lastMessage;
    private String hotelImageUrl;
    private ChatStatus status;

    // Enum for chat status
    public enum ChatStatus {
        AVAILABLE, ACTIVE, FINISHED
    }

    // Constructor
    public ChatSummary(String id, String hotelId, String hotelName,
                       String reservationId, String reservationDates,
                       ChatStatus status) {
        this.id = id;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.reservationId = reservationId;
        this.reservationDates = reservationDates;
        this.status = status;
    }

    // Getters and setters
    public String getId() {
        return id != null ? id : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHotelId() {
        return hotelId != null ? hotelId : "";
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public String getHotelName() {
        return hotelName != null ? hotelName : "Hotel";
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getReservationId() {
        return reservationId != null ? reservationId : "";
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservationDates() {
        return reservationDates != null ? reservationDates : "";
    }

    public void setReservationDates(String reservationDates) {
        this.reservationDates = reservationDates;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getHotelImageUrl() {
        return hotelImageUrl;
    }

    public void setHotelImageUrl(String hotelImageUrl) {
        this.hotelImageUrl = hotelImageUrl;
    }

    public ChatStatus getStatus() {
        return status != null ? status : ChatStatus.AVAILABLE;
    }

    public void setStatus(ChatStatus status) {
        this.status = status;
    }
}
