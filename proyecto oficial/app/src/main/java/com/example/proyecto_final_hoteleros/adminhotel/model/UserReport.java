package com.example.proyecto_final_hoteleros.adminhotel.model;

public class UserReport {
    private String name;
    private String email;
    private int totalReservations;
    private double totalSpent;
    private String category;
    private boolean isActive;

    public UserReport(String name, String email, int totalReservations, double totalSpent, String category, boolean isActive) {
        this.name = name;
        this.email = email;
        this.totalReservations = totalReservations;
        this.totalSpent = totalSpent;
        this.category = category;
        this.isActive = isActive;
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getTotalReservations() { return totalReservations; }
    public double getTotalSpent() { return totalSpent; }
    public String getCategory() { return category; }
    public boolean isActive() { return isActive; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setTotalReservations(int totalReservations) { this.totalReservations = totalReservations; }
    public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }
    public void setCategory(String category) { this.category = category; }
    public void setActive(boolean active) { isActive = active; }
}