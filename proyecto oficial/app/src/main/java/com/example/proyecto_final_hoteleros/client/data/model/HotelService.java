package com.example.proyecto_final_hoteleros.client.data.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class HotelService implements Serializable {
    private String id;
    private String name;
    private String description;
    private String expandedDescription;
    private Double price;
    private String imageUrl;
    private String iconResourceName;
    private boolean isConditional;
    private String conditionalDescription;
    private ServiceCategory category;
    private boolean isPopular;
    private int sortOrder;
    private String availability;
    private List<String> features;

    // Estados del servicio
    private boolean isSelected;
    private boolean isEligibleForFree;
    private boolean isExpanded;

    public enum ServiceCategory {
        ESSENTIALS("Incluidos", "free"),
        COMFORT("De Pago", "paid"),
        WELLNESS("De Pago", "paid"),
        GASTRONOMY("De Pago", "paid"),
        BUSINESS("De Pago", "paid"),
        TRANSPORT("Especiales", "conditional");

        private String displayName;
        private String filterType;

        ServiceCategory(String displayName, String filterType) {
            this.displayName = displayName;
            this.filterType = filterType;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getFilterType() {
            return filterType;
        }
    }

    // Constructor completo
    public HotelService(String id, String name, String description, Double price,
                        String imageUrl, String iconResourceName, boolean isConditional,
                        String conditionalDescription, ServiceCategory category,
                        boolean isPopular, int sortOrder, String availability,
                        String[] features) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.expandedDescription = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.iconResourceName = iconResourceName;
        this.isConditional = isConditional;
        this.conditionalDescription = conditionalDescription;
        this.category = category != null ? category : ServiceCategory.COMFORT;
        this.isPopular = isPopular;
        this.sortOrder = sortOrder;
        this.availability = availability != null ? availability : "24/7";
        this.features = features != null ? Arrays.asList(features) : Arrays.asList();
        this.isSelected = false;
        this.isEligibleForFree = false;
        this.isExpanded = false;
    }

    // Constructor simplificado (compatible con código existente)
    public HotelService(String id, String name, String description, Double price,
                        String imageUrl, String iconResourceName, boolean isConditional,
                        String conditionalDescription) {
        this(id, name, description, price, imageUrl, iconResourceName,
                isConditional, conditionalDescription, ServiceCategory.COMFORT,
                false, 999, "24/7", null);
    }

    // Métodos auxiliares
    public boolean isFree() {
        return price == null || price == 0.0 || (isConditional && isEligibleForFree);
    }

    public Double getEffectivePrice() {
        if (isFree()) return 0.0;
        return price != null ? price : 0.0;
    }

    public String getPriceDisplay() {
        if (isFree()) {
            return isConditional && isEligibleForFree ? "¡GRATIS!" : "✓ Incluido";
        }
        return String.format("S/. %.2f", price);
    }

    public String getServiceType() {
        if (isFree() && !isConditional) return "free";
        if (isConditional) return "conditional";
        return "paid";
    }

    public String getConditionalBadgeText() {
        if (!isConditional) return "";
        if (isEligibleForFree) {
            return "🎉 ¡Incluido con tu reserva! (Ahorro: S/. " + String.format("%.2f", price) + ")";
        }
        return "💡 " + conditionalDescription;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExpandedDescription() { return expandedDescription; }
    public void setExpandedDescription(String expandedDescription) { this.expandedDescription = expandedDescription; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getIconResourceName() { return iconResourceName; }
    public void setIconResourceName(String iconResourceName) { this.iconResourceName = iconResourceName; }

    public boolean isConditional() { return isConditional; }
    public void setConditional(boolean conditional) { isConditional = conditional; }

    public String getConditionalDescription() { return conditionalDescription; }
    public void setConditionalDescription(String conditionalDescription) {
        this.conditionalDescription = conditionalDescription;
    }

    public ServiceCategory getCategory() { return category; }
    public void setCategory(ServiceCategory category) { this.category = category; }

    public boolean isPopular() { return isPopular; }
    public void setPopular(boolean popular) { isPopular = popular; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    public boolean isEligibleForFree() { return isEligibleForFree; }
    public void setEligibleForFree(boolean eligibleForFree) { isEligibleForFree = eligibleForFree; }

    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}