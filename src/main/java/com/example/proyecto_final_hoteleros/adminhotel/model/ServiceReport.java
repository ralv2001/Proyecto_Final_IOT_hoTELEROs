package com.example.proyecto_final_hoteleros.adminhotel.model;

public class ServiceReport {
    private String name;
    private String iconKey;
    private int totalRequests;
    private double totalRevenue;
    private float averageRating;
    private String type;

    public ServiceReport(String name, String iconKey, int totalRequests, double totalRevenue, float averageRating, String type) {
        this.name = name;
        this.iconKey = iconKey;
        this.totalRequests = totalRequests;
        this.totalRevenue = totalRevenue;
        this.averageRating = averageRating;
        this.type = type;
    }

    // Getters
    public String getName() { return name; }
    public String getIconKey() { return iconKey; }
    public int getTotalRequests() { return totalRequests; }
    public double getTotalRevenue() { return totalRevenue; }
    public float getAverageRating() { return averageRating; }
    public String getType() { return type; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setIconKey(String iconKey) { this.iconKey = iconKey; }
    public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    public void setAverageRating(float averageRating) { this.averageRating = averageRating; }
    public void setType(String type) { this.type = type; }
}