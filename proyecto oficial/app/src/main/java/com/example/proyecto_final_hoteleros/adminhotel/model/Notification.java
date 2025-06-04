package com.example.proyecto_final_hoteleros.adminhotel.model;

public class Notification {
    private String text;
    private boolean isRead;
    private String timestamp;
    private String type; // "checkout", "arrival", "service", etc.

    public Notification(String text, boolean isRead, String timestamp, String type) {
        this.text = text;
        this.isRead = isRead;
        this.timestamp = timestamp;
        this.type = type;
    }

    // Getters
    public String getText() { return text; }
    public boolean isRead() { return isRead; }
    public String getTimestamp() { return timestamp; }
    public String getType() { return type; }

    // Setters
    public void setText(String text) { this.text = text; }
    public void setRead(boolean read) { isRead = read; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setType(String type) { this.type = type; }
}
