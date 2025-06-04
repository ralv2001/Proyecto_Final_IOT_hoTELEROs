package com.example.proyecto_final_hoteleros.client.data.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class FirebaseMessage {
    private String id;
    private String senderId;
    private String receiverId;
    private String text;
    private long timestamp;
    private String type; // "USER", "HOTEL", "SYSTEM"
    private boolean read;

    // Constructor vacío requerido para Firebase
    public FirebaseMessage() {
    }

    public FirebaseMessage(String id, String senderId, String receiverId, String text,
                           long timestamp, String type) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.timestamp = timestamp;
        this.type = type;
        this.read = false;
    }

    // Convertidor para Message
    public static FirebaseMessage fromMessage(Message message) {
        String type;
        switch (message.getType()) {
            case USER:
                type = "USER";
                break;
            case HOTEL:
                type = "HOTEL";
                break;
            case SYSTEM:
                type = "SYSTEM";
                break;
            default:
                type = "USER";
        }

        return new FirebaseMessage(
                message.getId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getText(),
                message.getTimestamp(),
                type
        );
    }

    // Convertidor a Message
    public Message toMessage() {
        Message.MessageType messageType;
        switch (this.type) {
            case "USER":
                messageType = Message.MessageType.USER;
                break;
            case "HOTEL":
                messageType = Message.MessageType.HOTEL;
                break;
            case "SYSTEM":
                messageType = Message.MessageType.SYSTEM;
                break;
            default:
                messageType = Message.MessageType.USER;
        }

        return new Message(
                this.id,
                this.senderId,
                this.receiverId,
                this.text,
                this.timestamp,
                messageType
        );
    }

    // Para guardar en Firebase Database
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("senderId", senderId);
        result.put("receiverId", receiverId);
        result.put("text", text);
        result.put("timestamp", timestamp);
        result.put("type", type);
        result.put("read", read);

        return result;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}