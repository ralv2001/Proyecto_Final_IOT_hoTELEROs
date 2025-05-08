package com.example.proyecto_final_hoteleros.client.model;





public class User {
    private String nombres;
    private String apellidos;
    private String email;
    private String fechaNacimiento;
    private String telefono;
    private String numeroDocumento;
    private String direccion;
    private String contrasena;
    private String confirmarContrasena;
    private String userType;
    private String docType;
    private String countryCode;

    public User() {}

    public User(String nombres, String apellidos, String email, String fechaNacimiento, String telefono, String numeroDocumento, String direccion, String contrasena, String confirmarContrasena, String userType, String docType, String countryCode) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.numeroDocumento = numeroDocumento;
        this.direccion = direccion;
        this.contrasena = contrasena;
        this.confirmarContrasena = confirmarContrasena;
        this.userType = userType;
        this.docType = docType;
        this.countryCode = countryCode;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getConfirmarContrasena() {
        return confirmarContrasena;
    }

    public void setConfirmarContrasena(String confirmarContrasena) {
        this.confirmarContrasena = confirmarContrasena;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
