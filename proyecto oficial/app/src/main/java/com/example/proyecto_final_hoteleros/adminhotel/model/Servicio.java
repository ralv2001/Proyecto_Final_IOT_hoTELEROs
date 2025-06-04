package com.example.proyecto_final_hoteleros.adminhotel.model;

public class Servicio {
    private String nombre;
    private int imagenResId;

    public Servicio(String nombre, int imagenResId) {
        this.nombre = nombre;
        this.imagenResId = imagenResId;
    }

    public String getNombre() {
        return nombre;
    }

    public int getImagenResId() {
        return imagenResId;
    }
}
