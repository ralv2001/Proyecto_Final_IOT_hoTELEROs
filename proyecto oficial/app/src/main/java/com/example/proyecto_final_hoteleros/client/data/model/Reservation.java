package com.example.proyecto_final_hoteleros.client.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Reservation implements Parcelable {
    // ✅ SOLO 3 ESTADOS (eliminamos CHECKOUT)
    public static final int STATUS_PROXIMA = 1;
    public static final int STATUS_ACTUAL = 2;
    public static final int STATUS_COMPLETADA = 3;

    public static final double MONTO_MINIMO_TAXI_GRATIS = 1000.0;

    private String hotelName;
    private String location;
    private String date;
    private double price;
    private float rating;
    private int imageResource;
    private int status;
    private String roomType;
    private boolean hasTaxiService;
    private double servicesTotal;
    private String checkInDate;
    private String checkOutDate;
    private List<ServiceItem> services;
    private String reservationId;

    // ✅ NUEVO: Campo para saber si está listo para checkout
    private boolean readyForCheckout = false;

    // Campos para NotificationService
    private Date checkInDateObj;
    private Date checkOutDateObj;
    private String checkInTime;
    private String checkOutTime;

    public static class ServiceItem {
        private String name;
        private double price;
        private int quantity;

        public ServiceItem(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public double getTotal() { return price * quantity; }
    }

    // Constructor principal
    public Reservation(String hotelName, String location, String date, double price,
                       float rating, int imageResource, int status) {
        this.hotelName = hotelName;
        this.location = location;
        this.date = date;
        this.price = price;
        this.rating = rating;
        this.imageResource = imageResource;
        this.status = status;
        this.servicesTotal = 0;
        this.hasTaxiService = false;
        this.roomType = "Estándar";
        this.services = new ArrayList<>();
        this.reservationId = generateReservationId();
        this.readyForCheckout = false;

        // Inicializar campos de fecha
        this.checkInDateObj = new Date();
        this.checkOutDateObj = new Date();
        this.checkInTime = "14:00";
        this.checkOutTime = "12:00";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.checkInDate = sdf.format(this.checkInDateObj);
        this.checkOutDate = sdf.format(this.checkOutDateObj);
    }

    public Reservation() {
        this.services = new ArrayList<>();
        this.reservationId = generateReservationId();
        this.checkInDateObj = new Date();
        this.checkOutDateObj = new Date();
        this.checkInTime = "14:00";
        this.checkOutTime = "12:00";
        this.readyForCheckout = false;
    }

    private String generateReservationId() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        String hotelInitials = "";
        if (hotelName != null) {
            String[] words = hotelName.split(" ");
            for (String word : words) {
                if (!word.isEmpty()) {
                    hotelInitials += word.charAt(0);
                }
            }
        } else {
            hotelInitials = "RES";
        }
        return "RES-" + hotelInitials + "-" + timestamp;
    }

    // ✅ NUEVO: Método para marcar como listo para checkout
    public void setReadyForCheckout(boolean ready) {
        this.readyForCheckout = ready;
    }

    public boolean isReadyForCheckout() {
        return readyForCheckout;
    }

    // ✅ NUEVO: Realizar checkout (cambiar a completada)
    public void performCheckout() {
        this.status = STATUS_COMPLETADA;
        this.readyForCheckout = false;
    }

    public void addService(String name, double price, int quantity) {
        ServiceItem service = new ServiceItem(name, price, quantity);
        services.add(service);
        recalculateServicesTotal();
    }

    private void recalculateServicesTotal() {
        servicesTotal = 0;
        for (ServiceItem service : services) {
            servicesTotal += service.getTotal();
        }
    }

    public String getServicesBreakdown() {
        if (services.isEmpty()) {
            return "No hay servicios adicionales";
        }

        StringBuilder breakdown = new StringBuilder();
        for (ServiceItem service : services) {
            breakdown.append("• ").append(service.getName())
                    .append(" (x").append(service.getQuantity()).append("): S/")
                    .append(new DecimalFormat("0.00").format(service.getTotal()))
                    .append("\n");
        }
        return breakdown.toString();
    }

    // Getters y setters
    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public int getImageResource() { return imageResource; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public boolean hasTaxiService() { return hasTaxiService; }
    public void setHasTaxiService(boolean hasTaxiService) { this.hasTaxiService = hasTaxiService; }
    public double getServicesTotal() { return servicesTotal; }
    public void setServicesTotal(double servicesTotal) { this.servicesTotal = servicesTotal; }
    public String getReservationId() { return reservationId; }
    public String getId() { return reservationId; }
    public void setId(String id) { this.reservationId = id; }
    public List<ServiceItem> getServices() { return services; }
    public double getTotalPrice() { return price + servicesTotal; }
    public boolean isEligibleForFreeTaxi() { return getTotalPrice() >= MONTO_MINIMO_TAXI_GRATIS; }

    // ✅ ACTUALIZADO: Texto de estado sin CHECKOUT
    public String getStatusText() {
        switch (status) {
            case STATUS_PROXIMA:
                return "Próxima";
            case STATUS_ACTUAL:
                return "Actual";
            case STATUS_COMPLETADA:
                return "Completada";
            default:
                return "Desconocido";
        }
    }

    // ✅ ACTUALIZADO: Colores sin CHECKOUT
    public int getStatusBackgroundColor() {
        switch (status) {
            case STATUS_PROXIMA:
                return android.graphics.Color.parseColor("#4CAF50"); // Verde
            case STATUS_ACTUAL:
                return android.graphics.Color.parseColor("#2196F3"); // Azul
            case STATUS_COMPLETADA:
                return android.graphics.Color.parseColor("#9E9E9E"); // Gris
            default:
                return android.graphics.Color.parseColor("#757575");
        }
    }

    // ✅ ACTUALIZADO: Información adicional sin CHECKOUT
    public String getAdditionalInfo() {
        switch (status) {
            case STATUS_PROXIMA:
                return "Reserva confirmada. Se requiere presentar identificación al check-in.";
            case STATUS_ACTUAL:
                if (services.isEmpty()) {
                    return "Estancia en curso. No hay servicios adicionales.";
                } else {
                    return "Estancia en curso. Servicios adicionales: S/" +
                            new DecimalFormat("0.00").format(servicesTotal);
                }
            case STATUS_COMPLETADA:
                return "Reserva finalizada el " +
                        new SimpleDateFormat("dd MMM yyyy", new Locale("es", "PE")).format(new Date()) +
                        ". Gracias por su estadía.";
            default:
                return "";
        }
    }

    // ✅ ACTUALIZADO: Texto del botón sin CHECKOUT
    public String getActionButtonText() {
        switch (status) {
            case STATUS_PROXIMA:
                return "Ver detalles";
            case STATUS_ACTUAL:
                return readyForCheckout ? "Hacer Checkout" : "Ver servicios";
            case STATUS_COMPLETADA:
                return "Ver factura";
            default:
                return "Detalles";
        }
    }

    // Métodos para NotificationService
    public Date getCheckInDate() { return checkInDateObj; }
    public void setCheckInDate(Date checkInDate) {
        this.checkInDateObj = checkInDate;
        if (checkInDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            this.checkInDate = sdf.format(checkInDate);
        }
    }
    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }
    public Date getCheckOutDate() { return checkOutDateObj; }
    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDateObj = checkOutDate;
        if (checkOutDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            this.checkOutDate = sdf.format(checkOutDate);
        }
    }
    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }

    // Implementación Parcelable
    protected Reservation(Parcel in) {
        hotelName = in.readString();
        location = in.readString();
        date = in.readString();
        price = in.readDouble();
        rating = in.readFloat();
        imageResource = in.readInt();
        status = in.readInt();
        roomType = in.readString();
        hasTaxiService = in.readByte() != 0;
        servicesTotal = in.readDouble();
        readyForCheckout = in.readByte() != 0;
        reservationId = in.readString();
        checkInDate = in.readString();
        checkOutDate = in.readString();

        long tmpCheckInDateObj = in.readLong();
        checkInDateObj = tmpCheckInDateObj != 0 ? new Date(tmpCheckInDateObj) : null;
        long tmpCheckOutDateObj = in.readLong();
        checkOutDateObj = tmpCheckOutDateObj != 0 ? new Date(tmpCheckOutDateObj) : null;
        checkInTime = in.readString();
        checkOutTime = in.readString();

        services = new ArrayList<>();
        in.readList(services, ServiceItem.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hotelName);
        dest.writeString(location);
        dest.writeString(date);
        dest.writeDouble(price);
        dest.writeFloat(rating);
        dest.writeInt(imageResource);
        dest.writeInt(status);
        dest.writeString(roomType);
        dest.writeByte((byte) (hasTaxiService ? 1 : 0));
        dest.writeDouble(servicesTotal);
        dest.writeByte((byte) (readyForCheckout ? 1 : 0));
        dest.writeString(reservationId);
        dest.writeString(checkInDate);
        dest.writeString(checkOutDate);

        dest.writeLong(checkInDateObj != null ? checkInDateObj.getTime() : 0);
        dest.writeLong(checkOutDateObj != null ? checkOutDateObj.getTime() : 0);
        dest.writeString(checkInTime);
        dest.writeString(checkOutTime);

        dest.writeList(services);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<Reservation> CREATOR = new Creator<Reservation>() {
        @Override
        public Reservation createFromParcel(Parcel in) { return new Reservation(in); }
        @Override
        public Reservation[] newArray(int size) { return new Reservation[size]; }
    };
}