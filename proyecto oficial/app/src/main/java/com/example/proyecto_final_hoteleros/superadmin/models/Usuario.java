package com.example.proyecto_final_hoteleros.superadmin.models;

public class Usuario {
    private String id;
    private String name;
    private String email;
    private String userType; // CLIENTE, ADMIN_HOTEL, TAXISTA, SUPERADMIN
    private boolean isActive;
    private String registrationDate;
    private String profileImageUrl;
    private String phoneNumber;
    private String lastLoginDate;

    public Usuario() {
        // Constructor vacío para Firebase
    }

    public Usuario(String id, String name, String email, String userType, boolean isActive, String registrationDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.userType = userType;
        this.isActive = isActive;
        this.registrationDate = registrationDate;
    }

    public Usuario(String id, String name, String email, String userType, boolean isActive,
                   String registrationDate, String profileImageUrl, String phoneNumber, String lastLoginDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.userType = userType;
        this.isActive = isActive;
        this.registrationDate = registrationDate;
        this.profileImageUrl = profileImageUrl;
        this.phoneNumber = phoneNumber;
        this.lastLoginDate = lastLoginDate;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(String lastLoginDate) { this.lastLoginDate = lastLoginDate; }

    // Métodos de utilidad
    public String getUserTypeText() {
        switch (userType) {
            case "CLIENTE":
                return "Cliente";
            case "ADMIN_HOTEL":
                return "Admin Hotel";
            case "TAXISTA":
                return "Taxista";
            case "SUPERADMIN":
                return "Superadmin";
            default:
                return "Desconocido";
        }
    }

    public int getUserTypeColor() {
        switch (userType) {
            case "CLIENTE":
                return android.graphics.Color.parseColor("#2196F3"); // Azul
            case "ADMIN_HOTEL":
                return android.graphics.Color.parseColor("#FF9800"); // Naranja
            case "TAXISTA":
                return android.graphics.Color.parseColor("#4CAF50"); // Verde
            case "SUPERADMIN":
                return android.graphics.Color.parseColor("#9C27B0"); // Púrpura
            default:
                return android.graphics.Color.parseColor("#757575"); // Gris
        }
    }

    public String getStatusText() {
        return isActive ? "Activo" : "Inactivo";
    }

    public int getStatusColor() {
        return isActive ? android.graphics.Color.parseColor("#4CAF50") :
                android.graphics.Color.parseColor("#F44336");
    }

    public String getUserTypeIcon() {
        switch (userType) {
            case "CLIENTE":
                return "👤";
            case "ADMIN_HOTEL":
                return "🏨";
            case "TAXISTA":
                return "🚗";
            case "SUPERADMIN":
                return "⚙️";
            default:
                return "❓";
        }
    }
}