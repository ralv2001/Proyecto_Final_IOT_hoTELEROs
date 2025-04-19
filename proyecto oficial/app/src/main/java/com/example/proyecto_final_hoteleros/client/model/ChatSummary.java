package com.example.proyecto_final_hoteleros.client.model;

public class ChatSummary {
    public enum ChatStatus {
        AVAILABLE,    // Chat disponible para iniciar
        ACTIVE,       // Chat activo
        FINISHED      // Chat finalizado
    }

    private String id;
    private String hotelId;
    private String hotelName;
    private String hotelImageUrl;
    private String reservationId;
    private String reservationDates;
    private String lastMessage;
    private long lastMessageTimestamp;
    private int unreadCount;
    private ChatStatus status;

    // Constructor vacío para Firebase
    public ChatSummary() {
    }

    // Constructor para uso local
    public ChatSummary(String id, String hotelId, String hotelName, String reservationId,
                       String reservationDates, ChatStatus status) {
        this.id = id;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.reservationId = reservationId;
        this.reservationDates = reservationDates;
        this.status = status;
        this.lastMessage = "";
        this.lastMessageTimestamp = System.currentTimeMillis();
        this.unreadCount = 0;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getHotelImageUrl() {
        return hotelImageUrl;
    }

    public void setHotelImageUrl(String hotelImageUrl) {
        this.hotelImageUrl = hotelImageUrl;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservationDates() {
        return reservationDates;
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

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public ChatStatus getStatus() {
        return status;
    }

    public void setStatus(ChatStatus status) {
        this.status = status;
    }
}