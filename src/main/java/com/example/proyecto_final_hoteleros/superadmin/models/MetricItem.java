package com.example.proyecto_final_hoteleros.superadmin.models;

import com.example.proyecto_final_hoteleros.R;

public class MetricItem {
    private String title;
    private String value;
    private int iconResId;
    private String colorTheme; // 🎨 NUEVO CAMPO
    private String changeText; // 🎨 NUEVO CAMPO

    // Constructor anterior (mantener compatibilidad)
    public MetricItem(String title, String value, int iconResId) {
        this.title = title;
        this.value = value;
        this.iconResId = iconResId;
        this.colorTheme = "blue"; // Default azul
        this.changeText = "";
    }

    // 🎨 NUEVO CONSTRUCTOR con tema de color
    public MetricItem(String title, String value, int iconResId, String colorTheme) {
        this.title = title;
        this.value = value;
        this.iconResId = iconResId;
        this.colorTheme = colorTheme;
        this.changeText = "";
    }

    // 🎨 CONSTRUCTOR COMPLETO con cambio
    public MetricItem(String title, String value, int iconResId, String colorTheme, String changeText) {
        this.title = title;
        this.value = value;
        this.iconResId = iconResId;
        this.colorTheme = colorTheme;
        this.changeText = changeText;
    }

    // Getters existentes
    public String getTitle() { return title; }
    public String getValue() { return value; }
    public int getIconResId() { return iconResId; }

    // 🎨 NUEVOS GETTERS
    public String getColorTheme() { return colorTheme; }
    public String getChangeText() { return changeText; }

    // 🎨 MÉTODO PARA OBTENER DRAWABLE DEL GRADIENTE
    public int getGradientDrawable() {
        switch (colorTheme) {
            case "green":
                return R.drawable.gradient_green;
            case "orange":
                return R.drawable.gradient_orange;
            case "purple":
                return R.drawable.gradient_purple;
            case "blue":
            default:
                return R.drawable.gradient_blue_default;
        }
    }

    // 🎨 MÉTODO PARA VERIFICAR SI TIENE CAMBIO
    public boolean hasChange() {
        return changeText != null && !changeText.isEmpty();
    }
}