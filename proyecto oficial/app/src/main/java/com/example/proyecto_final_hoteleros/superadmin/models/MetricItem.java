package com.example.proyecto_final_hoteleros.superadmin.models;


public class MetricItem {
    private String title;
    private String value;
    private int iconResId;

    public MetricItem(String title, String value, int iconResId) {
        this.title = title;
        this.value = value;
        this.iconResId = iconResId;
    }

    // Getters
    public String getTitle() { return title; }
    public String getValue() { return value; }
    public int getIconResId() { return iconResId; }
}
