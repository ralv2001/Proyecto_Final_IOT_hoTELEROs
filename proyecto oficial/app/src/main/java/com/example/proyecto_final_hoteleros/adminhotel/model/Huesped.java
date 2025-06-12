package com.example.proyecto_final_hoteleros.adminhotel.model;

import java.util.List;

public class Huesped {
    private String nombre;
    private String checkIn;
    private String checkOut;
    private String numeroHabitacion;
    private String estado;
    private List<String> servicios;

    public Huesped(String nombre, String checkIn) {
        this.nombre = nombre;
        this.checkIn = checkIn;
        this.estado = "Activo";
    }

    public Huesped(String nombre, String checkIn, String checkOut) {
        this.nombre = nombre;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.estado = "Activo";
    }

    public Huesped(String nombre, String checkIn, String checkOut, String numeroHabitacion) {
        this.nombre = nombre;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.numeroHabitacion = numeroHabitacion;
        this.estado = "Activo";
    }

    // Getters and setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }

    public String getNumeroHabitacion() {
        return numeroHabitacion;
    }

    public void setNumeroHabitacion(String numeroHabitacion) {
        this.numeroHabitacion = numeroHabitacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<String> getServicios() {
        return servicios;
    }

    public void setServicios(List<String> servicios) {
        this.servicios = servicios;
    }
}