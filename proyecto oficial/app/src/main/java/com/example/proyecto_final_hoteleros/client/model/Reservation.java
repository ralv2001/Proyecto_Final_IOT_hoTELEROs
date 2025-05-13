package com.example.proyecto_final_hoteleros.client.model;


import android.os.Parcel;
import android.os.Parcelable;


import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class Reservation implements Parcelable {
    // Constantes para los estados de la reserva
    public static final int STATUS_PROXIMA = 1;
    public static final int STATUS_ACTUAL = 2;
    public static final int STATUS_CHECKOUT = 3;
    public static final int STATUS_COMPLETADA = 4;


    // Constante para el monto mínimo para taxi gratuito
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
    private boolean readyForCheckout;
    private String reservationId;

    // Campos necesarios para NotificationService
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


        public String getName() {
            return name;
        }


        public double getPrice() {
            return price;
        }


        public int getQuantity() {
            return quantity;
        }


        public double getTotal() {
            return price * quantity;
        }
    }


    public Reservation(String hotelName, String location, String date, double price, float rating, int imageResource, int status) {
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
        this.readyForCheckout = status == STATUS_CHECKOUT;
        this.reservationId = generateReservationId();

        // Inicializar los campos para NotificationService
        this.checkInDateObj = new Date(); // Fecha actual como predeterminada
        this.checkOutDateObj = new Date(); // Fecha actual como predeterminada
        this.checkInTime = "14:00"; // Hora de check-in predeterminada
        this.checkOutTime = "12:00"; // Hora de check-out predeterminada

        // Convertir fechas a formato String para los campos originales
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.checkInDate = sdf.format(this.checkInDateObj);
        this.checkOutDate = sdf.format(this.checkOutDateObj);
    }


    // Constructor sin parámetros necesario para NotificationService
    public Reservation() {
        this.services = new ArrayList<>();
        this.reservationId = generateReservationId();
        this.checkInDateObj = new Date();
        this.checkOutDateObj = new Date();
        this.checkInTime = "14:00";
        this.checkOutTime = "12:00";
    }


    private String generateReservationId() {
        // Crear un ID único para la reserva basado en el nombre del hotel y fecha actual
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


    // Método para agregar un servicio
    public void addService(String name, double price, int quantity) {
        ServiceItem service = new ServiceItem(name, price, quantity);
        services.add(service);
        recalculateServicesTotal();
    }


    // Método para recalcular el total de servicios
    private void recalculateServicesTotal() {
        servicesTotal = 0;
        for (ServiceItem service : services) {
            servicesTotal += service.getTotal();
        }
    }


    // Método para generar un resumen de servicios
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


    // Getters y setters originales
    public String getHotelName() {
        return hotelName;
    }


    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }


    public String getLocation() {
        return location;
    }


    public void setLocation(String location) {
        this.location = location;
    }


    public String getDate() {
        return date;
    }


    public void setDate(String date) {
        this.date = date;
    }


    public double getPrice() {
        return price;
    }


    public void setPrice(double price) {
        this.price = price;
    }


    public float getRating() {
        return rating;
    }


    public void setRating(float rating) {
        this.rating = rating;
    }


    public int getImageResource() {
        return imageResource;
    }


    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }


    public int getStatus() {
        return status;
    }


    public void setStatus(int status) {
        this.status = status;
        // Si cambia a checkout, actualizar la bandera
        this.readyForCheckout = status == STATUS_CHECKOUT;
    }


    public String getRoomType() {
        return roomType;
    }


    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }


    public boolean hasTaxiService() {
        return hasTaxiService;
    }


    public void setHasTaxiService(boolean hasTaxiService) {
        this.hasTaxiService = hasTaxiService;
    }


    public double getServicesTotal() {
        return servicesTotal;
    }


    public void setServicesTotal(double servicesTotal) {
        this.servicesTotal = servicesTotal;
    }


    public boolean isReadyForCheckout() {
        return readyForCheckout;
    }


    public void setReadyForCheckout(boolean readyForCheckout) {
        this.readyForCheckout = readyForCheckout;
    }


    public String getReservationId() {
        return reservationId;
    }

    // Método necesario para NotificationService
    public String getId() {
        return reservationId;
    }

    // Método necesario para NotificationService
    public void setId(String id) {
        this.reservationId = id;
    }


    public List<ServiceItem> getServices() {
        return services;
    }


    public double getTotalPrice() {
        return price + servicesTotal;
    }


    public boolean isEligibleForFreeTaxi() {
        // Verificar si el gasto total supera un umbral para obtener taxi gratis
        return getTotalPrice() >= MONTO_MINIMO_TAXI_GRATIS;
    }


    // Método para obtener el texto de estado según el código
    public String getStatusText() {
        switch (status) {
            case STATUS_PROXIMA:
                return "Próxima";
            case STATUS_ACTUAL:
                return "Actual";
            case STATUS_CHECKOUT:
                return "En Checkout";
            case STATUS_COMPLETADA:
                return "Completada";
            default:
                return "Desconocido";
        }
    }


    // Método para obtener el color de fondo según el estado
    public int getStatusBackgroundColor() {
        switch (status) {
            case STATUS_PROXIMA:
                return android.graphics.Color.parseColor("#4CAF50"); // Verde
            case STATUS_ACTUAL:
                return android.graphics.Color.parseColor("#2196F3"); // Azul
            case STATUS_CHECKOUT:
                return android.graphics.Color.parseColor("#FF9800"); // Naranja
            case STATUS_COMPLETADA:
                return android.graphics.Color.parseColor("#9E9E9E"); // Gris
            default:
                return android.graphics.Color.parseColor("#757575"); // Gris oscuro
        }
    }


    // Método para obtener información adicional según el estado
    public String getAdditionalInfo() {
        switch (status) {
            case STATUS_PROXIMA:
                return "Reserva confirmada. Se requiere presentar identificación al check-in.";
            case STATUS_ACTUAL:
                if (services.isEmpty()) {
                    return "Estancia en curso. No hay servicios adicionales.";
                } else {
                    return "Estancia en curso. Servicios adicionales: S/" + new DecimalFormat("0.00").format(servicesTotal);
                }
            case STATUS_CHECKOUT:
                StringBuilder checkoutInfo = new StringBuilder();
                checkoutInfo.append("Total estimado: S/").append(new DecimalFormat("0.00").format(getTotalPrice())).append("\n");
                checkoutInfo.append("• Habitación: S/").append(new DecimalFormat("0.00").format(price)).append("\n");


                if (servicesTotal > 0) {
                    checkoutInfo.append("• Servicios: S/").append(new DecimalFormat("0.00").format(servicesTotal)).append("\n");
                }


                checkoutInfo.append(isEligibleForFreeTaxi() ?
                        "✅ Califica para servicio de taxi gratuito." :
                        "❌ No califica para servicio de taxi gratuito.");


                return checkoutInfo.toString();
            case STATUS_COMPLETADA:
                return "Reserva finalizada el " +
                        new SimpleDateFormat("dd MMM yyyy", new Locale("es", "PE")).format(new Date()) +
                        ". Gracias por su estadía.";
            default:
                return "";
        }
    }


    // Método para obtener el texto del botón de acción principal según el estado
    public String getActionButtonText() {
        switch (status) {
            case STATUS_PROXIMA:
                return "Ver detalles";
            case STATUS_ACTUAL:
                return "Servicios";
            case STATUS_CHECKOUT:
                return "Checkout";
            case STATUS_COMPLETADA:
                return "Ver factura";
            default:
                return "Detalles";
        }
    }

    // Métodos necesarios para NotificationService

    /**
     * Obtiene la fecha de check-in como objeto Date
     * @return Fecha de check-in
     */
    public Date getCheckInDate() {
        return checkInDateObj;
    }

    /**
     * Establece la fecha de check-in
     * @param checkInDate Fecha de check-in
     */
    public void setCheckInDate(Date checkInDate) {
        this.checkInDateObj = checkInDate;
        // Actualizar también el String
        if (checkInDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            this.checkInDate = sdf.format(checkInDate);
        }
    }

    /**
     * Obtiene la hora de check-in
     * @return Hora de check-in (formato HH:mm)
     */
    public String getCheckInTime() {
        return checkInTime;
    }

    /**
     * Establece la hora de check-in
     * @param checkInTime Hora de check-in (formato HH:mm)
     */
    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    /**
     * Obtiene la fecha de check-out como objeto Date
     * @return Fecha de check-out
     */
    public Date getCheckOutDate() {
        return checkOutDateObj;
    }

    /**
     * Establece la fecha de check-out
     * @param checkOutDate Fecha de check-out
     */
    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDateObj = checkOutDate;
        // Actualizar también el String
        if (checkOutDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            this.checkOutDate = sdf.format(checkOutDate);
        }
    }

    /**
     * Obtiene la hora de check-out
     * @return Hora de check-out (formato HH:mm)
     */
    public String getCheckOutTime() {
        return checkOutTime;
    }

    /**
     * Establece la hora de check-out
     * @param checkOutTime Hora de check-out (formato HH:mm)
     */
    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }


    // Implementación de Parcelable para pasar fácilmente reservas entre activities/fragments
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

        // Leer los nuevos campos para NotificationService
        long tmpCheckInDateObj = in.readLong();
        checkInDateObj = tmpCheckInDateObj != 0 ? new Date(tmpCheckInDateObj) : null;
        long tmpCheckOutDateObj = in.readLong();
        checkOutDateObj = tmpCheckOutDateObj != 0 ? new Date(tmpCheckOutDateObj) : null;
        checkInTime = in.readString();
        checkOutTime = in.readString();

        // Reconstruir la lista de servicios
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

        // Escribir los nuevos campos para NotificationService
        dest.writeLong(checkInDateObj != null ? checkInDateObj.getTime() : 0);
        dest.writeLong(checkOutDateObj != null ? checkOutDateObj.getTime() : 0);
        dest.writeString(checkInTime);
        dest.writeString(checkOutTime);

        // Escribir la lista de servicios
        dest.writeList(services);
    }


    @Override
    public int describeContents() {
        return 0;
    }


    public static final Creator<Reservation> CREATOR = new Creator<Reservation>() {
        @Override
        public Reservation createFromParcel(Parcel in) {
            return new Reservation(in);
        }


        @Override
        public Reservation[] newArray(int size) {
            return new Reservation[size];
        }
    };
}