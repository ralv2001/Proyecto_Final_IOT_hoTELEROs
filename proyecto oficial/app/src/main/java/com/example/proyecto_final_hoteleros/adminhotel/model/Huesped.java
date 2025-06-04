package com.example.proyecto_final_hoteleros.adminhotel.model;

import java.util.ArrayList;
import java.util.List;

public class Huesped {
    private String nombre;
    private String checkIn;
    private  String checkOut;
    private List<String> servicios;

    public List<String> getServicios() {
        return servicios;
    }

    public void setServicios(List<String> servicios) {
        this.servicios = servicios;
    }

    public Huesped(String nombre, String checkIn) {
        this.nombre = nombre;
        this.checkIn = checkIn;
        this.checkOut= checkOut;
        this.servicios = new ArrayList<>();

    }

    public String getNombre() {
        return nombre;
    }

    public String getCheckIn() {
        return checkIn;
    }
    public String getCheckOut(){return checkOut;}
}
