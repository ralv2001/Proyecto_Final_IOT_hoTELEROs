package com.example.proyecto_final_hoteleros.adminhotel;

import java.util.List;

public class Habitacion {
    private String nombre;
    private List<String> caracteristicas;

    public Habitacion(String nombre, List<String> caracteristicas) {
        this.nombre = nombre;
        this.caracteristicas = caracteristicas;
    }

    public String getNombre() { return nombre; }

    public List<String> getCaracteristicas() { return caracteristicas; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public void setCaracteristicas(List<String> caracteristicas) { this.caracteristicas = caracteristicas; }
}

