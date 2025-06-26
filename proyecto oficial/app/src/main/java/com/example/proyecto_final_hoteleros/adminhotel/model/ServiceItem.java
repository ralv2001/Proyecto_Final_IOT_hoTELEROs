package com.example.proyecto_final_hoteleros.adminhotel.model;

public class ServiceItem {
    private String name;
    private String category;
    private String iconKey;
    private boolean isNew;
    private boolean isSelected;

    public ServiceItem(String name, String category, String iconKey) {
        this.name = name;
        this.category = category;
        this.iconKey = iconKey;
        this.isNew = false;
        this.isSelected = false;
    }

    public ServiceItem(String name, String category, String iconKey, boolean isNew) {
        this.name = name;
        this.category = category;
        this.iconKey = iconKey;
        this.isNew = isNew;
        this.isSelected = false;
    }

    // Getters y Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIconKey() {
        return iconKey;
    }

    public void setIconKey(String iconKey) {
        this.iconKey = iconKey;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }
}