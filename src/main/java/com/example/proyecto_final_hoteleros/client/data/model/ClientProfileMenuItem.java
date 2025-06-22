package com.example.proyecto_final_hoteleros.client.data.model;

public class ClientProfileMenuItem {
    private String title;
    private int iconResId;
    private String subtitle;
    private boolean hasArrow;
    private ProfileMenuType type;

    public enum ProfileMenuType {
        HEADER,
        MENU_ITEM
    }

    public ClientProfileMenuItem(String title, int iconResId, String subtitle, boolean hasArrow, ProfileMenuType type) {
        this.title = title;
        this.iconResId = iconResId;
        this.subtitle = subtitle;
        this.hasArrow = hasArrow;
        this.type = type;
    }

    // Getters y setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public boolean hasArrow() { return hasArrow; }
    public void setHasArrow(boolean hasArrow) { this.hasArrow = hasArrow; }

    public ProfileMenuType getType() { return type; }
    public void setType(ProfileMenuType type) { this.type = type; }
}
