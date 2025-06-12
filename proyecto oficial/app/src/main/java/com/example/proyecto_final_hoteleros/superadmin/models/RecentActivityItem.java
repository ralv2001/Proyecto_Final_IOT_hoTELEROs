package com.example.proyecto_final_hoteleros.superadmin.models;


public class RecentActivityItem {
    private String title;
    private String description;
    private String time;
    private int iconResId;

    public RecentActivityItem(String title, String description, String time, int iconResId) {
        this.title = title;
        this.description = description;
        this.time = time;
        this.iconResId = iconResId;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTime() { return time; }
    public int getIconResId() { return iconResId; }
}
