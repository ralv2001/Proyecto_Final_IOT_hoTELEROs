package com.example.proyecto_final_hoteleros.taxista.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SolicitudViaje implements Parcelable {
    // Campos existentes
    private String id;
    private String hotelName;
    private String location;
    private float rating;
    private String status;
    private String dates;
    private String dateRange;
    private String district;
    private String hotelAddress;
    private String imageUrl;
    private double price;
    private String notes;
    private boolean isUrgent;
    private String clientName;
    private String originAddress;
    private String destinationAddress;
    private int estimatedTime;

    // CAMPOS PARA TAXI SERVICE
    private String tipoServicio; // "checkout_gratuito", "viaje_normal", etc.
    private String checkoutTime;
    private String clientPhone;
    private String reservationId; // ID de la reserva original

    // ✅ NUEVOS CAMPOS NECESARIOS
    private double estimatedDistance; // Para distancia estimada
    private long createdAt; // Para timestamp de creación

    // Constructor vacío
    public SolicitudViaje() {
        this.tipoServicio = "viaje_normal";
        this.estimatedDistance = 15.0; // Default 15km
        this.createdAt = System.currentTimeMillis();
    }

    // Constructor existente (mantener tal como está)
    public SolicitudViaje(String id, String hotelName, float rating, String status, String dates,
                          String dateRange, String district, String hotelAddress, String imageUrl,
                          double price, String notes, boolean isUrgent, String clientName,
                          String origin, String destination, int estimatedTime) {
        this.id = id;
        this.hotelName = hotelName;
        this.rating = rating;
        this.status = status;
        this.dates = dates;
        this.dateRange = dateRange;
        this.district = district;
        this.location = district;
        this.hotelAddress = hotelAddress;
        this.imageUrl = imageUrl;
        this.price = price;
        this.notes = notes;
        this.isUrgent = isUrgent;
        this.clientName = clientName;
        this.originAddress = origin;
        this.destinationAddress = destination;
        this.estimatedTime = estimatedTime;
        this.tipoServicio = "viaje_normal";
        this.estimatedDistance = 15.0; // Default
        this.createdAt = System.currentTimeMillis();
    }

    // NUEVO: Constructor para servicios de checkout
    public static SolicitudViaje fromCheckoutReservation(CheckoutReservation reservation) {
        SolicitudViaje solicitud = new SolicitudViaje();
        solicitud.setId(reservation.getId());
        solicitud.setReservationId(reservation.getId());
        solicitud.setHotelName(reservation.getHotelName());
        solicitud.setClientName(reservation.getClientName());
        solicitud.setClientPhone(reservation.getClientPhone());
        solicitud.setOriginAddress(reservation.getHotelAddress());
        solicitud.setDestinationAddress("Aeropuerto Internacional Jorge Chávez");
        solicitud.setCheckoutTime(reservation.getCheckoutTime());
        solicitud.setEstimatedTime(reservation.getEstimatedDuration());
        solicitud.setTipoServicio("checkout_gratuito");
        solicitud.setStatus("Checkout Pendiente");
        solicitud.setLocation(getDistrictFromAddress(reservation.getHotelAddress()));
        solicitud.setNotes(reservation.getNotes());
        solicitud.setUrgent(true); // Los checkouts son urgentes
        solicitud.setPrice(0.0); // Es gratuito
        solicitud.setRating(4.5f); // Rating por defecto
        solicitud.setImageUrl("https://cf.bstatic.com/xdata/images/hotel/max1024x768/237363319.jpg");
        solicitud.setEstimatedDistance(reservation.getEstimatedDistance());
        solicitud.setCreatedAt(reservation.getCreatedAt());
        return solicitud;
    }

    private static String getDistrictFromAddress(String address) {
        if (address == null) return "Lima";
        if (address.contains("San Miguel")) return "San Miguel";
        if (address.contains("Miraflores")) return "Miraflores";
        if (address.contains("San Isidro")) return "San Isidro";
        if (address.contains("Barranco")) return "Barranco";
        if (address.contains("Surco")) return "Surco";
        return "Lima";
    }

    // Constructor para Parcelable (ACTUALIZADO)
    protected SolicitudViaje(Parcel in) {
        id = in.readString();
        hotelName = in.readString();
        location = in.readString();
        rating = in.readFloat();
        status = in.readString();
        dates = in.readString();
        dateRange = in.readString();
        district = in.readString();
        hotelAddress = in.readString();
        imageUrl = in.readString();
        price = in.readDouble();
        notes = in.readString();
        isUrgent = in.readByte() != 0;
        clientName = in.readString();
        originAddress = in.readString();
        destinationAddress = in.readString();
        estimatedTime = in.readInt();
        // CAMPOS EXISTENTES
        tipoServicio = in.readString();
        checkoutTime = in.readString();
        clientPhone = in.readString();
        reservationId = in.readString();
        // ✅ NUEVOS CAMPOS
        estimatedDistance = in.readDouble();
        createdAt = in.readLong();
    }

    // ========== GETTERS Y SETTERS EXISTENTES (mantener todos) ==========

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }

    public String getCheckoutTime() { return checkoutTime; }
    public void setCheckoutTime(String checkoutTime) { this.checkoutTime = checkoutTime; }

    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    // ✅ NUEVOS GETTERS Y SETTERS NECESARIOS
    public double getEstimatedDistance() { return estimatedDistance; }
    public void setEstimatedDistance(double estimatedDistance) { this.estimatedDistance = estimatedDistance; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // ✅ MÉTODOS ALIAS PARA COMPATIBILIDAD
    public String getServiceType() { return tipoServicio; }
    public void setServiceType(String serviceType) { this.tipoServicio = serviceType; }

    public int getEstimatedDuration() { return estimatedTime; }
    public void setEstimatedDuration(int estimatedDuration) { this.estimatedTime = estimatedDuration; }

    // Métodos de utilidad existentes
    public boolean isCheckoutService() {
        return "checkout_gratuito".equals(tipoServicio);
    }

    public boolean isFreeService() {
        return isCheckoutService() || price == 0.0;
    }

    public String getServiceTypeLabel() {
        switch (tipoServicio) {
            case "checkout_gratuito":
                return "🏨 Checkout Gratuito";
            case "viaje_normal":
                return "🚕 Viaje Regular";
            default:
                return "🚗 Servicio de Taxi";
        }
    }

    // Getters y setters existentes (todos los que ya tenías)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDates() { return dates; }
    public void setDates(String dates) { this.dates = dates; }

    public String getDateRange() { return dateRange; }
    public void setDateRange(String dateRange) { this.dateRange = dateRange; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getHotelAddress() { return hotelAddress; }
    public void setHotelAddress(String hotelAddress) { this.hotelAddress = hotelAddress; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isUrgent() { return isUrgent; }
    public void setUrgent(boolean urgent) { isUrgent = urgent; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getOriginAddress() { return originAddress; }
    public void setOriginAddress(String originAddress) { this.originAddress = originAddress; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public int getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(int estimatedTime) { this.estimatedTime = estimatedTime; }

    // Actualizar writeToParcel para incluir nuevos campos
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(hotelName);
        dest.writeString(location);
        dest.writeFloat(rating);
        dest.writeString(status);
        dest.writeString(dates);
        dest.writeString(dateRange);
        dest.writeString(district);
        dest.writeString(hotelAddress);
        dest.writeString(imageUrl);
        dest.writeDouble(price);
        dest.writeString(notes);
        dest.writeByte((byte) (isUrgent ? 1 : 0));
        dest.writeString(clientName);
        dest.writeString(originAddress);
        dest.writeString(destinationAddress);
        dest.writeInt(estimatedTime);
        // CAMPOS EXISTENTES
        dest.writeString(tipoServicio);
        dest.writeString(checkoutTime);
        dest.writeString(clientPhone);
        dest.writeString(reservationId);
        // ✅ NUEVOS CAMPOS
        dest.writeDouble(estimatedDistance);
        dest.writeLong(createdAt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SolicitudViaje> CREATOR = new Creator<SolicitudViaje>() {
        @Override
        public SolicitudViaje createFromParcel(Parcel in) {
            return new SolicitudViaje(in);
        }

        @Override
        public SolicitudViaje[] newArray(int size) {
            return new SolicitudViaje[size];
        }
    };

    @Override
    public String toString() {
        return "SolicitudViaje{" +
                "id='" + id + '\'' +
                ", hotelName='" + hotelName + '\'' +
                ", clientName='" + clientName + '\'' +
                ", tipoServicio='" + tipoServicio + '\'' +
                ", estimatedTime=" + estimatedTime +
                '}';
    }
}