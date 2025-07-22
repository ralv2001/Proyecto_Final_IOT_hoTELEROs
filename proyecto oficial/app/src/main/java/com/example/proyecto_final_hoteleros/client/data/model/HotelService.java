// client/data/model/HotelService.java
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
    private List<String> imageUrls; // ✅ Lista de fotos del servicio
    private String iconResourceName;
    private boolean isConditional;
    private String conditionalDescription;
    private ServiceCategory category;
    private boolean isPopular;
    private int sortOrder;
    private String availability;
    private List<String> features;
    private boolean isIncludedInRoom; // ✅ Si viene incluido en la habitación específica

    // Estados del servicio
    private boolean isSelected;
    private boolean isEligibleForFree;
    private boolean isExpanded;
    private String serviceType; // "basic", "included", "paid", "conditional"
    private boolean isFree;

    public enum ServiceCategory {
        ROOM_INCLUDED("Incluidos en habitación", "room_included"), // ✅ NUEVO
        ESSENTIALS("Servicios básicos", "free"),
        COMFORT("Comodidad", "paid"),
        WELLNESS("Bienestar", "paid"),
        GASTRONOMY("Gastronomía", "paid"),
        BUSINESS("Negocios", "paid"),
        TRANSPORT("Transporte", "conditional");

        private String displayName;
        private String filterType;

        ServiceCategory(String displayName, String filterType) {
            this.displayName = displayName;
            this.filterType = filterType;
        }

        public String getDisplayName() { return displayName; }
        public String getFilterType() { return filterType; }
    }

    // Constructor actualizado
    public HotelService(String id, String name, String description, Double price,
                        String imageUrl, List<String> imageUrls, String iconResourceName,
                        boolean isConditional, String conditionalDescription,
                        ServiceCategory category, boolean isPopular, int sortOrder,
                        String availability, String[] features, boolean isIncludedInRoom) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.expandedDescription = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.imageUrls = imageUrls != null ? imageUrls : Arrays.asList();
        this.iconResourceName = iconResourceName;
        this.isConditional = isConditional;
        this.conditionalDescription = conditionalDescription;
        this.category = category != null ? category : ServiceCategory.COMFORT;
        this.isPopular = isPopular;
        this.sortOrder = sortOrder;
        this.availability = availability != null ? availability : "24/7";
        this.features = features != null ? Arrays.asList(features) : Arrays.asList();
        this.isIncludedInRoom = isIncludedInRoom;
        this.isSelected = false;
        this.isEligibleForFree = false;
        this.isExpanded = false;
    }

    // Constructor de compatibilidad
    public HotelService(String id, String name, String description, Double price,
                        String imageUrl, String iconResourceName, boolean isConditional,
                        String conditionalDescription) {
        this(id, name, description, price, imageUrl, null, iconResourceName,
                isConditional, conditionalDescription, ServiceCategory.COMFORT,
                false, 999, "24/7", null, false);
    }

    // ✅ ARREGLADO: Método isFree() con lógica correcta
    public boolean isFree() {
        // Si se estableció explícitamente como gratuito
        if (isFree) return true;

        // Si es básico, siempre es gratuito
        if ("basic".equals(serviceType)) return true;

        // Si es incluido en la habitación específica, es gratuito
        if ("included".equals(serviceType) && isIncludedInRoom) return true;

        // Si es condicional y está desbloqueado, es gratuito
        if ("conditional".equals(serviceType) && isEligibleForFree) return true;

        // Si no tiene precio o precio es 0
        if (price == null || price == 0.0) return true;

        return false;
    }

    /**
     * ✅ ARREGLADO: Determinar tipo de servicio con lógica mejorada
     */
    public String getServiceType() {
        // ✅ Si se seteó desde Firebase, usar ese valor
        if (serviceType != null && !serviceType.isEmpty()) {
            return serviceType;
        }

        // ✅ MANTENER lógica existente como fallback
        if (isIncludedInRoom) return "included";
        if (isFree() && !isConditional) return "basic";
        if (isConditional) return "conditional";
        return "paid";
    }

    public void setFree(boolean free) {
        this.isFree = free;
    }

    /**
     * ✅ ARREGLADO: Configurar tipo de servicio con auto-configuración
     */
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;

        // ✅ Auto-configurar propiedades según el tipo de Firebase
        switch (serviceType) {
            case "basic":
                this.isFree = true;
                this.isIncludedInRoom = false;
                this.isConditional = false;
                break;
            case "included":
                this.isFree = true;
                // isIncludedInRoom se determina por la habitación específica
                this.isConditional = false;
                break;
            case "paid":
                this.isFree = false;
                this.isIncludedInRoom = false;
                this.isConditional = false;
                break;
            case "conditional":
                // Para condicionales (taxi), se determina dinámicamente
                this.isConditional = true;
                this.isIncludedInRoom = false;
                // isFree se determina con isEligibleForFree
                break;
        }
    }

    // ✅ NUEVOS getters y setters
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public boolean isIncludedInRoom() { return isIncludedInRoom; }
    public void setIncludedInRoom(boolean includedInRoom) { isIncludedInRoom = includedInRoom; }

    public boolean hasMultipleImages() {
        return imageUrls != null && imageUrls.size() > 1;
    }

    public String getMainImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return imageUrl;
    }

    // ✅ ARREGLADO: Método getPriceDisplay con lógica mejorada
    public String getPriceDisplay() {
        if (isFree()) {
            if ("basic".equals(serviceType)) {
                return "✓ Básico";
            } else if ("included".equals(serviceType) && isIncludedInRoom) {
                return "✓ Incluido";
            } else if ("conditional".equals(serviceType) && isEligibleForFree) {
                return "¡DESBLOQUEADO!";
            } else {
                return "✓ Incluido";
            }
        }
        return String.format("S/. %.2f", price != null ? price : 0.0);
    }

    /**
     * ✅ ARREGLADO: Badge condicional con mensaje dinámico
     */
    public String getConditionalBadgeText() {
        if (!isConditional) return "";

        if (conditionalDescription != null && !conditionalDescription.isEmpty()) {
            return conditionalDescription;
        }

        if (isEligibleForFree) {
            return "🎉 ¡Incluido con tu reserva! (Ahorro: S/. " + String.format("%.2f", price != null ? price : 60.0) + ")";
        }

        return "💡 Se desbloquea con reservas mayores";
    }

    // Resto de getters y setters existentes...
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
    public void setConditionalDescription(String conditionalDescription) { this.conditionalDescription = conditionalDescription; }
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

    public Double getEffectivePrice() {
        if (isFree()) return 0.0;
        return price != null ? price : 0.0;
    }
}