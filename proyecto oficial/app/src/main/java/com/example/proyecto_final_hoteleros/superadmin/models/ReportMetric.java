package com.example.proyecto_final_hoteleros.superadmin.models;

public class ReportMetric {
    private String title;
    private String value;
    private String change;
    private String changeColor;
    private int iconResId;

    public ReportMetric(String title, String value, String change, String changeColor, int iconResId) {
        this.title = title;
        this.value = value;
        this.change = change;
        this.changeColor = changeColor;
        this.iconResId = iconResId;
    }

    // Getters
    public String getTitle() { return title; }
    public String getValue() { return value; }
    public String getChange() { return change; }
    public String getChangeColor() { return changeColor; }
    public int getIconResId() { return iconResId; }

    public boolean isPositiveChange() {
        return change.startsWith("+");
    }

    public int getChangeColorInt() {
        try {
            return android.graphics.Color.parseColor(changeColor);
        } catch (Exception e) {
            return isPositiveChange() ?
                    android.graphics.Color.parseColor("#4CAF50") :
                    android.graphics.Color.parseColor("#F44336");
        }
    }
}