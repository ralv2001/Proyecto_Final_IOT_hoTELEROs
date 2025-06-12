package com.example.proyecto_final_hoteleros.superadmin.models;

import java.io.Serializable;

public class TaxistaUser implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String apellidos;
    private String email;
    private String licensePlate;
    private String status; // PENDING, APPROVED, REJECTED
    private String registrationDate;
    private String profileImageUrl;
    private String carImageUrl;
    private String phoneNumber;
    private String documentNumber;

    // Nuevos campos agregados
    private String tipoDocumento; // DNI, Pasaporte, Carnet de extranjería
    private String fechaNacimiento;
    private String domicilio;
    private String breveteImageUrl;

    public TaxistaUser() {
        // Constructor vacío para Firebase
    }

    public TaxistaUser(String id, String name, String email, String licensePlate,
                       String status, String registrationDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.licensePlate = licensePlate;
        this.status = status;
        this.registrationDate = registrationDate;
    }

    // Constructor completo
    public TaxistaUser(String id, String name, String apellidos, String email, String licensePlate,
                       String status, String registrationDate, String profileImageUrl, String carImageUrl,
                       String phoneNumber, String documentNumber, String tipoDocumento,
                       String fechaNacimiento, String domicilio, String breveteImageUrl) {
        this.id = id;
        this.name = name;
        this.apellidos = apellidos;
        this.email = email;
        this.licensePlate = licensePlate;
        this.status = status;
        this.registrationDate = registrationDate;
        this.profileImageUrl = profileImageUrl;
        this.carImageUrl = carImageUrl;
        this.phoneNumber = phoneNumber;
        this.documentNumber = documentNumber;
        this.tipoDocumento = tipoDocumento;
        this.fechaNacimiento = fechaNacimiento;
        this.domicilio = domicilio;
        this.breveteImageUrl = breveteImageUrl;
    }

    // Getters y Setters existentes
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getCarImageUrl() { return carImageUrl; }
    public void setCarImageUrl(String carImageUrl) { this.carImageUrl = carImageUrl; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

    // Nuevos getters y setters
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }

    public String getBreveteImageUrl() { return breveteImageUrl; }
    public void setBreveteImageUrl(String breveteImageUrl) { this.breveteImageUrl = breveteImageUrl; }

    // Métodos de utilidad existentes
    public String getStatusText() {
        switch (status) {
            case "PENDING":
                return "Pendiente";
            case "APPROVED":
                return "Aprobado";
            case "REJECTED":
                return "Rechazado";
            default:
                return "Desconocido";
        }
    }

    public int getStatusColor() {
        switch (status) {
            case "PENDING":
                return android.graphics.Color.parseColor("#FF9800"); // Naranja
            case "APPROVED":
                return android.graphics.Color.parseColor("#4CAF50"); // Verde
            case "REJECTED":
                return android.graphics.Color.parseColor("#F44336"); // Rojo
            default:
                return android.graphics.Color.parseColor("#9E9E9E"); // Gris
        }
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isApproved() {
        return "APPROVED".equals(status);
    }

    public boolean isRejected() {
        return "REJECTED".equals(status);
    }

    // Método para obtener nombre completo
    public String getFullName() {
        if (apellidos != null && !apellidos.isEmpty()) {
            return name + " " + apellidos;
        }
        return name;
    }
}