package com.example.proyecto_final_hoteleros.adminhotel.model;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class CheckoutItem implements Serializable {
    private String id;
    private String nombreHuesped;
    private String numeroHabitacion;
    private String fechaCheckIn;
    private String fechaCheckOut;
    private String estado;
    private String emailHuesped;
    private String telefonoHuesped;
    private double costoHabitacion;
    private List<ServicioAdicional> serviciosAdicionales;
    private List<DanoHabitacion> danos;
    private double totalServicios;
    private double totalDanos;
    private double totalGeneral;
    private String metodoPago;
    private boolean pagado;
    private long fechaCreacion;
    private String observaciones;
    private int numeroNoches;
    private String tipoHabitacion;

    public CheckoutItem(String nombreHuesped, String numeroHabitacion, String fechaCheckIn, String fechaCheckOut) {
        this.id = String.valueOf(System.currentTimeMillis() + (int)(Math.random() * 1000));
        this.nombreHuesped = nombreHuesped;
        this.numeroHabitacion = numeroHabitacion;
        this.fechaCheckIn = fechaCheckIn;
        this.fechaCheckOut = fechaCheckOut;
        this.estado = "Pendiente";
        this.serviciosAdicionales = new ArrayList<>();
        this.danos = new ArrayList<>();
        this.pagado = false;
        this.fechaCreacion = System.currentTimeMillis();
        this.totalServicios = 0.0;
        this.totalDanos = 0.0;
        this.costoHabitacion = 0.0;
        this.numeroNoches = 1;
        calculateTotal();
    }

    // Clase interna para servicios adicionales
    public static class ServicioAdicional implements Serializable {
        private String nombre;
        private double precio;
        private int cantidad;
        private String fecha;
        private String descripcion;

        public ServicioAdicional(String nombre, double precio, int cantidad, String fecha, String descripcion) {
            this.nombre = nombre;
            this.precio = precio;
            this.cantidad = cantidad;
            this.fecha = fecha;
            this.descripcion = descripcion;
        }

        public double getTotal() {
            return precio * cantidad;
        }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public double getPrecio() { return precio; }
        public void setPrecio(double precio) { this.precio = precio; }
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }

    // Clase interna para daños
    public static class DanoHabitacion implements Serializable {
        private String descripcion;
        private double costo;
        private String gravedad;
        private String fecha;
        private boolean confirmado;

        public DanoHabitacion(String descripcion, double costo, String gravedad, String fecha) {
            this.descripcion = descripcion;
            this.costo = costo;
            this.gravedad = gravedad;
            this.fecha = fecha;
            this.confirmado = false;
        }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public double getCosto() { return costo; }
        public void setCosto(double costo) { this.costo = costo; }
        public String getGravedad() { return gravedad; }
        public void setGravedad(String gravedad) { this.gravedad = gravedad; }
        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }
        public boolean isConfirmado() { return confirmado; }
        public void setConfirmado(boolean confirmado) { this.confirmado = confirmado; }
    }

    public void calculateTotal() {
        totalServicios = 0.0;
        if (serviciosAdicionales != null) {
            for (ServicioAdicional servicio : serviciosAdicionales) {
                totalServicios += servicio.getTotal();
            }
        }

        totalDanos = 0.0;
        if (danos != null) {
            for (DanoHabitacion dano : danos) {
                if (dano.isConfirmado()) {
                    totalDanos += dano.getCosto();
                }
            }
        }

        totalGeneral = costoHabitacion + totalServicios + totalDanos;
    }

    public void addServicioAdicional(ServicioAdicional servicio) {
        if (serviciosAdicionales == null) {
            serviciosAdicionales = new ArrayList<>();
        }
        serviciosAdicionales.add(servicio);
        calculateTotal();
    }

    public void addDano(DanoHabitacion dano) {
        if (danos == null) {
            danos = new ArrayList<>();
        }
        danos.add(dano);
        calculateTotal();
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombreHuesped() { return nombreHuesped; }
    public void setNombreHuesped(String nombreHuesped) { this.nombreHuesped = nombreHuesped; }

    public String getNumeroHabitacion() { return numeroHabitacion; }
    public void setNumeroHabitacion(String numeroHabitacion) { this.numeroHabitacion = numeroHabitacion; }

    public String getFechaCheckIn() { return fechaCheckIn; }
    public void setFechaCheckIn(String fechaCheckIn) { this.fechaCheckIn = fechaCheckIn; }

    public String getFechaCheckOut() { return fechaCheckOut; }
    public void setFechaCheckOut(String fechaCheckOut) { this.fechaCheckOut = fechaCheckOut; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getEmailHuesped() { return emailHuesped; }
    public void setEmailHuesped(String emailHuesped) { this.emailHuesped = emailHuesped; }

    public String getTelefonoHuesped() { return telefonoHuesped; }
    public void setTelefonoHuesped(String telefonoHuesped) { this.telefonoHuesped = telefonoHuesped; }

    public double getCostoHabitacion() { return costoHabitacion; }
    public void setCostoHabitacion(double costoHabitacion) {
        this.costoHabitacion = costoHabitacion;
        calculateTotal();
    }

    public List<ServicioAdicional> getServiciosAdicionales() {
        if (serviciosAdicionales == null) {
            serviciosAdicionales = new ArrayList<>();
        }
        return serviciosAdicionales;
    }

    public void setServiciosAdicionales(List<ServicioAdicional> serviciosAdicionales) {
        this.serviciosAdicionales = serviciosAdicionales;
        calculateTotal();
    }

    public List<DanoHabitacion> getDanos() {
        if (danos == null) {
            danos = new ArrayList<>();
        }
        return danos;
    }

    public void setDanos(List<DanoHabitacion> danos) {
        this.danos = danos;
        calculateTotal();
    }

    public double getTotalServicios() { return totalServicios; }
    public double getTotalDanos() { return totalDanos; }
    public double getTotalGeneral() { return totalGeneral; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public boolean isPagado() { return pagado; }
    public void setPagado(boolean pagado) { this.pagado = pagado; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public int getNumeroNoches() { return numeroNoches; }
    public void setNumeroNoches(int numeroNoches) { this.numeroNoches = numeroNoches; }

    public String getTipoHabitacion() { return tipoHabitacion; }
    public void setTipoHabitacion(String tipoHabitacion) { this.tipoHabitacion = tipoHabitacion; }
}