package com.example.proyecto_final_hoteleros.superadmin.models;

public class AdminUser {
    private String id;
    private String name;
    private String email;
    private String hotelName;
    private boolean isActive;
    private String registrationDate;
    private String profileImageUrl;

    public AdminUser() {
        // Constructor vacío para Firebase
    }

    public AdminUser(String id, String name, String email, String hotelName, boolean isActive) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.hotelName = hotelName;
        this.isActive = isActive;
        this.registrationDate = getCurrentDate();
    }

    public AdminUser(String id, String name, String email, String hotelName, boolean isActive,
                     String registrationDate, String profileImageUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.hotelName = hotelName;
        this.isActive = isActive;
        this.registrationDate = registrationDate;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    // Métodos de utilidad
    public String getStatusText() {
        return isActive ? "Activo" : "Inactivo";
    }

    public int getStatusColor() {
        return isActive ? android.graphics.Color.parseColor("#4CAF50") :
                android.graphics.Color.parseColor("#F44336");
    }

    private String getCurrentDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }
}