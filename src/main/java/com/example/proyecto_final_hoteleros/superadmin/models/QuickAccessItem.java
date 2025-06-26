package com.example.proyecto_final_hoteleros.superadmin.models;

import com.example.proyecto_final_hoteleros.R;

public class QuickAccessItem {
    private String title;
    private int iconResId;
    private String action;
    private String colorTheme; // 🎨 NUEVO CAMPO

    // Constructor anterior (mantener compatibilidad)
    public QuickAccessItem(String title, int iconResId, String action) {
        this.title = title;
        this.iconResId = iconResId;
        this.action = action;
        this.colorTheme = "theme_orange"; // Default naranja
    }

    // 🎨 NUEVO CONSTRUCTOR con tema de color
    public QuickAccessItem(String title, int iconResId, String action, String colorTheme) {
        this.title = title;
        this.iconResId = iconResId;
        this.action = action;
        this.colorTheme = colorTheme;
    }

    // Getters existentes
    public String getTitle() { return title; }
    public int getIconResId() { return iconResId; }
    public String getAction() { return action; }

    // 🎨 NUEVO GETTER
    public String getColorTheme() { return colorTheme; }

    // 🎨 MÉTODOS PARA OBTENER COLORES ESPECÍFICOS
    public int getPrimaryColor(android.content.Context context) {
        switch (colorTheme) {
            case "theme_blue":
                return context.getResources().getColor(R.color.theme_blue_primary);
            case "theme_green":
                return context.getResources().getColor(R.color.theme_green_primary);
            case "theme_purple":
                return context.getResources().getColor(R.color.theme_purple_primary);
            case "theme_yellow":
                return context.getResources().getColor(R.color.theme_yellow_primary);
            case "theme_red":
                return context.getResources().getColor(R.color.theme_red_primary);
            case "theme_orange":
            default:
                return context.getResources().getColor(R.color.theme_orange_primary);
        }
    }

    public int getLightColor(android.content.Context context) {
        switch (colorTheme) {
            case "theme_blue":
                return context.getResources().getColor(R.color.theme_blue_light);
            case "theme_green":
                return context.getResources().getColor(R.color.theme_green_light);
            case "theme_purple":
                return context.getResources().getColor(R.color.theme_purple_light);
            case "theme_yellow":
                return context.getResources().getColor(R.color.theme_yellow_light);
            case "theme_red":
                return context.getResources().getColor(R.color.theme_red_light);
            case "theme_orange":
            default:
                return context.getResources().getColor(R.color.theme_orange_light);
        }
    }
}