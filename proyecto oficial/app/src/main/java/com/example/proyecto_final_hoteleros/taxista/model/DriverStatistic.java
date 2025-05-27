package com.example.proyecto_final_hoteleros.taxista.model;

public class DriverStatistic {
    private String title;
    private String value;
    private int iconResId;
    private String subtitle;

    public DriverStatistic(String title, String value, int iconResId, String subtitle) {
        this.title = title;
        this.value = value;
        this.iconResId = iconResId;
        this.subtitle = subtitle;
    }

    // Getters y setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
}