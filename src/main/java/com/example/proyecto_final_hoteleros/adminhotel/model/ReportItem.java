package com.example.proyecto_final_hoteleros.adminhotel.model;

public class ReportItem {
    private String title;
    private String value;
    private String subtitle;

    public ReportItem(String title, String value, String subtitle) {
        this.title = title;
        this.value = value;
        this.subtitle = subtitle;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
}