package com.example.proyecto_final_hoteleros.superadmin.models;


public class QuickAccessItem {
    private String title;
    private int iconResId;
    private String action;

    public QuickAccessItem(String title, int iconResId, String action) {
        this.title = title;
        this.iconResId = iconResId;
        this.action = action;
    }

    // Getters
    public String getTitle() { return title; }
    public int getIconResId() { return iconResId; }
    public String getAction() { return action; }
}