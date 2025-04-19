package com.example.proyecto_final_hoteleros.client.model;

public class Message {

    public enum MessageType {
        USER,
        HOTEL
    }

    private String id;
    private String senderId;
    private String receiverId;
    private String text;
    private long timestamp;
    private MessageType type;

    // Constructor vacío para Firebase
    public Message() {
    }

    // Constructor para uso local
    public Message(String id, String senderId, String receiverId, String text, long timestamp, MessageType type) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.timestamp = timestamp;
        this.type = type;
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

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}