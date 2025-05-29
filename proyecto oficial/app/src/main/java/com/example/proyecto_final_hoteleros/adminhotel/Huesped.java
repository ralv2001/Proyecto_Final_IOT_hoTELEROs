package com.example.proyecto_final_hoteleros.adminhotel;

public class Huesped {
    private String nombre;
    private String checkIn;

    public Huesped(String nombre, String checkIn) {
        this.nombre = nombre;
        this.checkIn = checkIn;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCheckIn() {
        return checkIn;
    }
}
