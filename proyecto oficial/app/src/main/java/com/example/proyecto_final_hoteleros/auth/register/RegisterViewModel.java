package com.example.proyecto_final_hoteleros.auth.register;

import android.net.Uri;

import androidx.lifecycle.ViewModel;

public class RegisterViewModel extends ViewModel {
    // Datos de usuario para el registro
    private String nombres;
    private String apellidos;
    private String email;
    private String fechaNacimiento;
    private String telefono;
    private String tipoDocumento;
    private String numeroDocumento;
    private String direccion;
    private String password;
    private String userType;
    private Uri profilePhotoUri;
    private boolean hasProfilePhoto = false;

    // Getters y setters
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

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Uri getProfilePhotoUri() {
        return profilePhotoUri;
    }

    public void setProfilePhotoUri(Uri profilePhotoUri) {
        this.profilePhotoUri = profilePhotoUri;
        this.hasProfilePhoto = (profilePhotoUri != null);
    }

    public boolean hasProfilePhoto() {
        return hasProfilePhoto;
    }

    public void setHasProfilePhoto(boolean hasProfilePhoto) {
        this.hasProfilePhoto = hasProfilePhoto;
    }
}