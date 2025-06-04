package com.example.proyecto_final_hoteleros.client.data.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class FirebaseChatSummary {
    private String id;
    private String hotelId;
    private String hotelName;
    private String reservationId;
    private String reservationDates;
    private String lastMessage;
    private String hotelImageUrl;
    private String status; // "AVAILABLE", "ACTIVE", "FINISHED"
    private long lastUpdated;
    private String userId;

    // Constructor vacío requerido para Firebase
    public FirebaseChatSummary() {
    }

    public FirebaseChatSummary(String id, String hotelId, String hotelName, String reservationId,
                               String reservationDates, String status, String userId) {
        this.id = id;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.reservationId = reservationId;
        this.reservationDates = reservationDates;
        this.status = status;
        this.lastUpdated = System.currentTimeMillis();
        this.userId = userId;
    }

    // Convertidor para ChatSummary
    public static FirebaseChatSummary fromChatSummary(ChatSummary chatSummary, String userId) {
        String status;
        switch (chatSummary.getStatus()) {
            case AVAILABLE:
                status = "AVAILABLE";
                break;
            case ACTIVE:
                status = "ACTIVE";
                break;
            case FINISHED:
                status = "FINISHED";
                break;
            default:
                status = "AVAILABLE";
        }

        FirebaseChatSummary fbChat = new FirebaseChatSummary(
                chatSummary.getId(),
                chatSummary.getHotelId(),
                chatSummary.getHotelName(),
                chatSummary.getReservationId(),
                chatSummary.getReservationDates(),
                status,
                userId
        );

        fbChat.setLastMessage(chatSummary.getLastMessage());
        fbChat.setHotelImageUrl(chatSummary.getHotelImageUrl());

        return fbChat;
    }

    // Convertidor a ChatSummary
    // Convertidor a ChatSummary
    public ChatSummary toChatSummary() {
        ChatSummary.ChatStatus chatStatus;

        // MODIFICADO: Manejar el caso donde status es null
        if (this.status == null) {
            // Definir un valor predeterminado si status es null
            chatStatus = ChatSummary.ChatStatus.AVAILABLE;
        } else {
            // Conversión normal
            switch (this.status) {
                case "AVAILABLE":
                    chatStatus = ChatSummary.ChatStatus.AVAILABLE;
                    break;
                case "ACTIVE":
                    chatStatus = ChatSummary.ChatStatus.ACTIVE;
                    break;
                case "FINISHED":
                    chatStatus = ChatSummary.ChatStatus.FINISHED;
                    break;
                default:
                    chatStatus = ChatSummary.ChatStatus.AVAILABLE;
            }
        }

        // MODIFICADO: Verificar también otros campos para evitar NullPointerException
        String safeId = this.id != null ? this.id : "";
        String safeHotelId = this.hotelId != null ? this.hotelId : "";
        String safeHotelName = this.hotelName != null ? this.hotelName : "Hotel";
        String safeReservationId = this.reservationId != null ? this.reservationId : "";
        String safeReservationDates = this.reservationDates != null ? this.reservationDates : "";

        ChatSummary chatSummary = new ChatSummary(
                safeId,
                safeHotelId,
                safeHotelName,
                safeReservationId,
                safeReservationDates,
                chatStatus
        );

        // También verifica los otros campos antes de asignarlos
        if (this.lastMessage != null) {
            chatSummary.setLastMessage(this.lastMessage);
        }

        if (this.hotelImageUrl != null) {
            chatSummary.setHotelImageUrl(this.hotelImageUrl);
        }

        return chatSummary;
    }

    // Para guardar en Firebase Database
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("hotelId", hotelId);
        result.put("hotelName", hotelName);
        result.put("reservationId", reservationId);
        result.put("reservationDates", reservationDates);
        result.put("lastMessage", lastMessage);
        result.put("hotelImageUrl", hotelImageUrl);
        result.put("status", status);
        result.put("lastUpdated", lastUpdated);
        result.put("userId", userId);

        return result;
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

    public String getHotelImageUrl() {
        return hotelImageUrl;
    }

    public void setHotelImageUrl(String hotelImageUrl) {
        this.hotelImageUrl = hotelImageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}