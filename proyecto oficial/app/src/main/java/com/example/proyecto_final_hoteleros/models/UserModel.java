package com.example.proyecto_final_hoteleros.models;

import java.util.HashMap;
import java.util.Map;

public class UserModel {

    private String userId;
    private String userType;
    private String nombres;
    private String apellidos;
    private String email;
    private String fechaNacimiento;
    private String telefono;
    private String tipoDocumento;
    private String numeroDocumento;
    private String direccion;
    private String placaVehiculo; // Solo para taxistas
    private String photoUrl;      // URL de la foto de perfil en AWS
    private String documentUrl;
    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    // Constructor vacío (requerido por Firestore)
    public UserModel() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isActive = true;
    }

    // Constructor completo
    public UserModel(String userId, String userType, String nombres, String apellidos,
                     String email, String fechaNacimiento, String telefono,
                     String tipoDocumento, String numeroDocumento, String direccion,
                     String placaVehiculo) {
        this();
        this.userId = userId;
        this.userType = userType;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.direccion = direccion;
        this.placaVehiculo = placaVehiculo;
    }

    // Método para convertir a Map (para Firestore)
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("userType", userType);
        map.put("nombres", nombres);
        map.put("apellidos", apellidos);
        map.put("email", email);
        map.put("fechaNacimiento", fechaNacimiento);
        map.put("telefono", telefono);
        map.put("tipoDocumento", tipoDocumento);
        map.put("numeroDocumento", numeroDocumento);
        map.put("direccion", direccion);
        map.put("placaVehiculo", placaVehiculo);
        map.put("photoUrl", photoUrl);
        map.put("documentUrl", documentUrl);
        map.put("isActive", isActive);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }

    // Método para crear desde Map (para leer de Firestore)
    public static UserModel fromMap(Map<String, Object> map) {
        UserModel user = new UserModel();
        user.userId = (String) map.get("userId");
        user.userType = (String) map.get("userType");
        user.nombres = (String) map.get("nombres");
        user.apellidos = (String) map.get("apellidos");
        user.email = (String) map.get("email");
        user.fechaNacimiento = (String) map.get("fechaNacimiento");
        user.telefono = (String) map.get("telefono");
        user.tipoDocumento = (String) map.get("tipoDocumento");
        user.numeroDocumento = (String) map.get("numeroDocumento");
        user.direccion = (String) map.get("direccion");
        user.placaVehiculo = (String) map.get("placaVehiculo");
        user.photoUrl = (String) map.get("photoUrl");
        user.documentUrl = (String) map.get("documentUrl");
        // Mejorar la lectura del campo isActive
        if (map.containsKey("isActive") && map.get("isActive") != null) {
            user.isActive = (Boolean) map.get("isActive");
        } else if (map.containsKey("active") && map.get("active") != null) {
            // Fallback para usuarios antiguos con campo "active"
            user.isActive = (Boolean) map.get("active");
        } else {
            // Default: true para usuarios sin el campo (usuarios antiguos)
            user.isActive = true;
        }
        user.createdAt = map.get("createdAt") != null ? (Long) map.get("createdAt") : System.currentTimeMillis();
        user.updatedAt = map.get("updatedAt") != null ? (Long) map.get("updatedAt") : System.currentTimeMillis();
        return user;
    }

    // Getters y Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getPlacaVehiculo() { return placaVehiculo; }
    public void setPlacaVehiculo(String placaVehiculo) { this.placaVehiculo = placaVehiculo; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }

    // Método helper para nombre completo
    public String getFullName() {
        return nombres + " " + apellidos;
    }

    // Método helper para verificar si es taxista
    public boolean isDriver() {
        return "driver".equals(userType);
    }

    // Método helper para verificar si es cliente
    public boolean isClient() {
        return "client".equals(userType);
    }
}