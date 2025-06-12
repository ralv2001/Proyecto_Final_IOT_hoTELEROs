package com.example.proyecto_final_hoteleros.adminhotel.model;

public class TaxiStatus {
    private String driverName;
    private String vehiclePlate;
    private String estado;
    private String clientName;
    private String eta;

    public TaxiStatus(String driverName, String vehiclePlate, String estado, String clientName, String eta) {
        this.driverName = driverName;
        this.vehiclePlate = vehiclePlate;
        this.estado = estado;
        this.clientName = clientName;
        this.eta = eta;
    }

    // Getters and setters
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getVehiclePlate() { return vehiclePlate; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }
}