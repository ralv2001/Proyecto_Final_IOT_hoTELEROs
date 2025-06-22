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
    // ✅ SOLO 3 ESTADOS PRINCIPALES
    public static final int STATUS_UPCOMING = 1;     // Próximas
    public static final int STATUS_ACTIVE = 2;       // Actuales
    public static final int STATUS_COMPLETED = 3;    // Completadas

    // ✅ SUB-ESTADOS PARA ACTIVE (mostrados con UI)
    public static final int SUBSTATUS_CHECKED_IN = 1;        // Recién llegó
    public static final int SUBSTATUS_STAYING = 2;           // Estadía normal
    public static final int SUBSTATUS_CHECKOUT_PENDING = 3;  // Checkout solicitado

    public static final double MONTO_MINIMO_TAXI_GRATIS = 1000.0;

    private String hotelName;
    private String location;
    private String date;
    private double basePrice;
    private float rating;
    private int imageResource;
    private int status;
    private int subStatus; // ✅ NUEVO: Sub-estado para ACTIVE
    private String roomType;
    private String roomNumber;
    private boolean hasTaxiService;
    private double servicesTotal;
    private double additionalCharges;
    private String checkInDate;
    private String checkOutDate;
    private List<ServiceItem> services;
    private List<ChargeItem> additionalChargesList;
    private String reservationId;
    private String confirmationCode;

    // ✅ CAMPOS PROFESIONALES
    private boolean canModify;
    private boolean hasOutstandingBill;
    private boolean reviewSubmitted;
    private Date actualCheckInTime;
    private Date checkoutRequestTime;
    private String specialRequests;
    private PaymentMethod guaranteeCard;

    // Campos para NotificationService
    private Date checkInDateObj;
    private Date checkOutDateObj;
    private String checkInTime;
    private String checkOutTime;

    // ✅ CLASES INTERNAS
    public static class ChargeItem implements Parcelable {
        private String description;
        private double amount;
        private String reason;
        private Date chargeDate;

        public ChargeItem(String description, double amount, String reason) {
            this.description = description;
            this.amount = amount;
            this.reason = reason;
            this.chargeDate = new Date();
        }

        protected ChargeItem(Parcel in) {
            description = in.readString();
            amount = in.readDouble();
            reason = in.readString();
            long tmpChargeDate = in.readLong();
            chargeDate = tmpChargeDate != 0 ? new Date(tmpChargeDate) : null;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(description);
            dest.writeDouble(amount);
            dest.writeString(reason);
            dest.writeLong(chargeDate != null ? chargeDate.getTime() : 0);
        }

        @Override
        public int describeContents() { return 0; }

        public static final Creator<ChargeItem> CREATOR = new Creator<ChargeItem>() {
            @Override
            public ChargeItem createFromParcel(Parcel in) { return new ChargeItem(in); }
            @Override
            public ChargeItem[] newArray(int size) { return new ChargeItem[size]; }
        };

        public String getDescription() { return description; }
        public double getAmount() { return amount; }
        public String getReason() { return reason; }
        public Date getChargeDate() { return chargeDate; }
    }

    public static class ServiceItem implements Parcelable {
        private String name;
        private double price;
        private int quantity;
        private Date orderDate;
        private boolean isPaid;

        public ServiceItem(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.orderDate = new Date();
            this.isPaid = false;
        }

        protected ServiceItem(Parcel in) {
            name = in.readString();
            price = in.readDouble();
            quantity = in.readInt();
            long tmpOrderDate = in.readLong();
            orderDate = tmpOrderDate != 0 ? new Date(tmpOrderDate) : null;
            isPaid = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeDouble(price);
            dest.writeInt(quantity);
            dest.writeLong(orderDate != null ? orderDate.getTime() : 0);
            dest.writeByte((byte) (isPaid ? 1 : 0));
        }

        @Override
        public int describeContents() { return 0; }

        public static final Creator<ServiceItem> CREATOR = new Creator<ServiceItem>() {
            @Override
            public ServiceItem createFromParcel(Parcel in) { return new ServiceItem(in); }
            @Override
            public ServiceItem[] newArray(int size) { return new ServiceItem[size]; }
        };

        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public double getTotal() { return price * quantity; }
        public Date getOrderDate() { return orderDate; }
        public boolean isPaid() { return isPaid; }
        public void setPaid(boolean paid) { this.isPaid = paid; }
    }

    public static class PaymentMethod implements Parcelable {
        private String cardNumber; // Solo últimos 4 dígitos
        private String cardType;   // Visa, Mastercard, etc.
        private String holderName;

        public PaymentMethod(String cardNumber, String cardType, String holderName) {
            this.cardNumber = cardNumber;
            this.cardType = cardType;
            this.holderName = holderName;
        }

        protected PaymentMethod(Parcel in) {
            cardNumber = in.readString();
            cardType = in.readString();
            holderName = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(cardNumber);
            dest.writeString(cardType);
            dest.writeString(holderName);
        }

        @Override
        public int describeContents() { return 0; }

        public static final Creator<PaymentMethod> CREATOR = new Creator<PaymentMethod>() {
            @Override
            public PaymentMethod createFromParcel(Parcel in) { return new PaymentMethod(in); }
            @Override
            public PaymentMethod[] newArray(int size) { return new PaymentMethod[size]; }
        };

        public String getCardNumber() { return cardNumber; }
        public String getCardType() { return cardType; }
        public String getHolderName() { return holderName; }
    }

    // Constructor principal
    public Reservation(String hotelName, String location, String date, double price,
                       float rating, int imageResource, int status) {
        this.hotelName = hotelName;
        this.location = location;
        this.date = date;
        this.basePrice = price;
        this.rating = rating;
        this.imageResource = imageResource;
        this.status = status;
        this.subStatus = (status == STATUS_ACTIVE) ? SUBSTATUS_STAYING : 0;
        this.servicesTotal = 0;
        this.additionalCharges = 0;
        this.hasTaxiService = false;
        this.roomType = "Estándar";
        this.services = new ArrayList<>();
        this.additionalChargesList = new ArrayList<>();
        this.reservationId = generateReservationId();
        this.confirmationCode = generateConfirmationCode();

        updateModificationPermissions();

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
        this.additionalChargesList = new ArrayList<>();
        this.reservationId = generateReservationId();
        this.confirmationCode = generateConfirmationCode();
        this.checkInDateObj = new Date();
        this.checkOutDateObj = new Date();
        this.checkInTime = "14:00";
        this.checkOutTime = "12:00";
        updateModificationPermissions();
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
            hotelInitials = "HTL";
        }
        return "RES-" + hotelInitials + "-" + timestamp;
    }

    private String generateConfirmationCode() {
        return "CNF" + System.currentTimeMillis() % 1000000;
    }

    // ✅ ACTUALIZAR PERMISOS SEGÚN ESTADO Y SUB-ESTADO
    private void updateModificationPermissions() {
        switch (status) {
            case STATUS_UPCOMING:
                canModify = true;
                hasOutstandingBill = false;
                break;
            case STATUS_ACTIVE:
                canModify = (subStatus == SUBSTATUS_CHECKED_IN); // Solo primeras horas
                hasOutstandingBill = (subStatus == SUBSTATUS_CHECKOUT_PENDING);
                break;
            case STATUS_COMPLETED:
                canModify = false;
                hasOutstandingBill = false;
                break;
        }
    }

    // ✅ SOLICITAR CHECKOUT (cambiar sub-estado)
    public void requestCheckout() {
        if (status == STATUS_ACTIVE && subStatus != SUBSTATUS_CHECKOUT_PENDING) {
            this.subStatus = SUBSTATUS_CHECKOUT_PENDING;
            this.checkoutRequestTime = new Date();
            updateModificationPermissions();
        }
    }

    // ✅ APROBAR CHECKOUT (mover a completado)
    public void approveCheckout() {
        if (status == STATUS_ACTIVE && subStatus == SUBSTATUS_CHECKOUT_PENDING) {
            this.status = STATUS_COMPLETED;
            this.subStatus = 0;
            updateModificationPermissions();
        }
    }

    // ✅ AGREGAR CARGO ADICIONAL
    public void addAdditionalCharge(String description, double amount, String reason) {
        ChargeItem charge = new ChargeItem(description, amount, reason);
        additionalChargesList.add(charge);
        recalculateAdditionalCharges();
    }

    private void recalculateAdditionalCharges() {
        additionalCharges = 0;
        for (ChargeItem charge : additionalChargesList) {
            additionalCharges += charge.getAmount();
        }
    }

    public void addService(String name, double price, int quantity) {
        if (status == STATUS_ACTIVE) {
            ServiceItem service = new ServiceItem(name, price, quantity);
            services.add(service);
            recalculateServicesTotal();
        }
    }

    private void recalculateServicesTotal() {
        servicesTotal = 0;
        for (ServiceItem service : services) {
            servicesTotal += service.getTotal();
        }
    }

    public double getFinalTotal() {
        return basePrice + servicesTotal + additionalCharges;
    }

    // ✅ TEXTO DE ESTADO CON SUB-ESTADOS
    public String getStatusText() {
        switch (status) {
            case STATUS_UPCOMING:
                return "Próxima";
            case STATUS_ACTIVE:
                switch (subStatus) {
                    case SUBSTATUS_CHECKED_IN:
                        return "Recién llegado";
                    case SUBSTATUS_STAYING:
                        return "En estadía";
                    case SUBSTATUS_CHECKOUT_PENDING:
                        return "Checkout pendiente";
                    default:
                        return "Activa";
                }
            case STATUS_COMPLETED:
                return "Completada";
            default:
                return "Desconocido";
        }
    }

    // ✅ COLORES SEGÚN SUB-ESTADO
    public int getStatusBackgroundColor() {
        switch (status) {
            case STATUS_UPCOMING:
                return android.graphics.Color.parseColor("#4CAF50"); // Verde
            case STATUS_ACTIVE:
                switch (subStatus) {
                    case SUBSTATUS_CHECKED_IN:
                        return android.graphics.Color.parseColor("#2196F3"); // Azul
                    case SUBSTATUS_STAYING:
                        return android.graphics.Color.parseColor("#2196F3"); // Azul
                    case SUBSTATUS_CHECKOUT_PENDING:
                        return android.graphics.Color.parseColor("#FF9800"); // Naranja
                    default:
                        return android.graphics.Color.parseColor("#2196F3");
                }
            case STATUS_COMPLETED:
                return android.graphics.Color.parseColor("#9E9E9E"); // Gris
            default:
                return android.graphics.Color.parseColor("#757575");
        }
    }

    // ✅ INFORMACIÓN CONTEXTUAL INTELIGENTE
    public String getContextualInfo() {
        switch (status) {
            case STATUS_UPCOMING:
                String daysUntil = getDaysUntilCheckIn();
                return "Check-in disponible a partir de las " + checkInTime +
                        ". " + daysUntil + ". Puedes modificar hasta 24h antes.";

            case STATUS_ACTIVE:
                switch (subStatus) {
                    case SUBSTATUS_CHECKED_IN:
                        return "¡Bienvenido! Ya puedes disfrutar de todos nuestros servicios. Check-out antes de las " + checkOutTime + ".";
                    case SUBSTATUS_STAYING:
                        if (services.isEmpty()) {
                            return "Estadía en curso. Explora nuestros servicios adicionales disponibles.";
                        } else {
                            return "Servicios consumidos: S/" + new DecimalFormat("0.00").format(servicesTotal) +
                                    ". Check-out disponible cuando desees.";
                        }
                    case SUBSTATUS_CHECKOUT_PENDING:
                        return "Checkout solicitado. El hotel está revisando tu estadía y posibles cargos adicionales.";
                    default:
                        return "Estadía en curso.";
                }

            case STATUS_COMPLETED:
                return "Estadía finalizada. " +
                        (reviewSubmitted ? "Gracias por tu valoración." : "¿Te gustaría valorar tu experiencia?");

            default:
                return "";
        }
    }

    private String getDaysUntilCheckIn() {
        long diffInMillies = checkInDateObj.getTime() - new Date().getTime();
        long days = diffInMillies / (1000 * 60 * 60 * 24);

        if (days == 0) return "Es hoy";
        if (days == 1) return "Mañana";
        if (days > 1) return "En " + days + " días";
        return "Check-in disponible";
    }

    // ✅ TEXTO DEL BOTÓN INTELIGENTE
    public String getActionButtonText() {
        switch (status) {
            case STATUS_UPCOMING:
                return canModify ? "Modificar" : "Ver detalles";
            case STATUS_ACTIVE:
                switch (subStatus) {
                    case SUBSTATUS_CHECKED_IN:
                    case SUBSTATUS_STAYING:
                        return "Hacer checkout";
                    case SUBSTATUS_CHECKOUT_PENDING:
                        return "Ver estado";
                    default:
                        return "Ver detalles";
                }
            case STATUS_COMPLETED:
                return reviewSubmitted ? "Ver factura" : "Valorar estadía";
            default:
                return "Ver detalles";
        }
    }

    // ✅ MÉTODO PARA SABER SI PUEDE HACER CHECKOUT
    public boolean canRequestCheckout() {
        return status == STATUS_ACTIVE && (subStatus == SUBSTATUS_CHECKED_IN || subStatus == SUBSTATUS_STAYING);
    }

    public boolean isCheckoutPending() {
        return status == STATUS_ACTIVE && subStatus == SUBSTATUS_CHECKOUT_PENDING;
    }

    // ✅ FACTURA DETALLADA
    public String getDetailedBill() {
        StringBuilder bill = new StringBuilder();
        DecimalFormat formatter = new DecimalFormat("0.00");

        bill.append("🏨 ").append(hotelName).append("\n");
        bill.append("📅 ").append(date).append("\n");
        bill.append("🏠 ").append(roomType);
        if (roomNumber != null && !roomNumber.isEmpty()) {
            bill.append(" (").append(roomNumber).append(")");
        }
        bill.append("\n\n");

        bill.append("💰 DESGLOSE:\n");
        bill.append("• Habitación: S/").append(formatter.format(basePrice)).append("\n");

        if (!services.isEmpty()) {
            bill.append("• Servicios adicionales:\n");
            for (ServiceItem service : services) {
                bill.append("  - ").append(service.getName())
                        .append(" (x").append(service.getQuantity()).append("): S/")
                        .append(formatter.format(service.getTotal())).append("\n");
            }
        }

        if (!additionalChargesList.isEmpty()) {
            bill.append("• Cargos adicionales:\n");
            for (ChargeItem charge : additionalChargesList) {
                bill.append("  - ").append(charge.getDescription())
                        .append(": S/").append(formatter.format(charge.getAmount()))
                        .append("\n");
            }
        }

        bill.append("\n💳 TOTAL: S/").append(formatter.format(getFinalTotal()));

        return bill.toString();
    }

    // Getters y setters
    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    @Deprecated
    public double getPrice() { return basePrice; }
    @Deprecated
    public void setPrice(double price) { this.basePrice = price; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public int getImageResource() { return imageResource; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }
    public int getStatus() { return status; }

    public void setStatus(int status) {
        this.status = status;
        if (status == STATUS_ACTIVE && subStatus == 0) {
            subStatus = SUBSTATUS_STAYING;
        }
        updateModificationPermissions();
    }

    public int getSubStatus() { return subStatus; }
    public void setSubStatus(int subStatus) {
        this.subStatus = subStatus;
        updateModificationPermissions();
    }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public boolean hasTaxiService() { return hasTaxiService; }
    public void setHasTaxiService(boolean hasTaxiService) { this.hasTaxiService = hasTaxiService; }
    public double getServicesTotal() { return servicesTotal; }
    public double getAdditionalCharges() { return additionalCharges; }
    public String getReservationId() { return reservationId; }
    public String getConfirmationCode() { return confirmationCode; }
    public String getId() { return reservationId; }
    public void setId(String id) { this.reservationId = id; }
    public List<ServiceItem> getServices() { return services; }
    public List<ChargeItem> getAdditionalChargesList() { return additionalChargesList; }

    public boolean canModify() { return canModify; }
    public boolean hasOutstandingBill() { return hasOutstandingBill; }
    public boolean isReviewSubmitted() { return reviewSubmitted; }
    public void setReviewSubmitted(boolean reviewSubmitted) { this.reviewSubmitted = reviewSubmitted; }
    public Date getActualCheckInTime() { return actualCheckInTime; }
    public void setActualCheckInTime(Date actualCheckInTime) { this.actualCheckInTime = actualCheckInTime; }
    public Date getCheckoutRequestTime() { return checkoutRequestTime; }
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
    public PaymentMethod getGuaranteeCard() { return guaranteeCard; }
    public void setGuaranteeCard(PaymentMethod guaranteeCard) { this.guaranteeCard = guaranteeCard; }

    public double getTotalPrice() { return getFinalTotal(); }
    public boolean isEligibleForFreeTaxi() { return getFinalTotal() >= MONTO_MINIMO_TAXI_GRATIS; }

    public boolean isCompleted() {
        return status == STATUS_COMPLETED;
    }

    // Métodos para NotificationService (sin cambios)
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

    // Implementación Parcelable COMPLETA
    protected Reservation(Parcel in) {
        hotelName = in.readString();
        location = in.readString();
        date = in.readString();
        basePrice = in.readDouble();
        rating = in.readFloat();
        imageResource = in.readInt();
        status = in.readInt();
        subStatus = in.readInt();
        roomType = in.readString();
        roomNumber = in.readString();
        hasTaxiService = in.readByte() != 0;
        servicesTotal = in.readDouble();
        additionalCharges = in.readDouble();
        reservationId = in.readString();
        confirmationCode = in.readString();
        canModify = in.readByte() != 0;
        hasOutstandingBill = in.readByte() != 0;
        reviewSubmitted = in.readByte() != 0;
        checkInDate = in.readString();
        checkOutDate = in.readString();
        specialRequests = in.readString();

        long tmpCheckInDateObj = in.readLong();
        checkInDateObj = tmpCheckInDateObj != 0 ? new Date(tmpCheckInDateObj) : null;
        long tmpCheckOutDateObj = in.readLong();
        checkOutDateObj = tmpCheckOutDateObj != 0 ? new Date(tmpCheckOutDateObj) : null;
        long tmpActualCheckInTime = in.readLong();
        actualCheckInTime = tmpActualCheckInTime != 0 ? new Date(tmpActualCheckInTime) : null;
        long tmpCheckoutRequestTime = in.readLong();
        checkoutRequestTime = tmpCheckoutRequestTime != 0 ? new Date(tmpCheckoutRequestTime) : null;

        checkInTime = in.readString();
        checkOutTime = in.readString();

        services = new ArrayList<>();
        additionalChargesList = new ArrayList<>();
        in.readList(services, ServiceItem.class.getClassLoader());
        in.readList(additionalChargesList, ChargeItem.class.getClassLoader());

        // Leer PaymentMethod
        if (in.readByte() != 0) {
            guaranteeCard = PaymentMethod.CREATOR.createFromParcel(in);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hotelName);
        dest.writeString(location);
        dest.writeString(date);
        dest.writeDouble(basePrice);
        dest.writeFloat(rating);
        dest.writeInt(imageResource);
        dest.writeInt(status);
        dest.writeInt(subStatus);
        dest.writeString(roomType);
        dest.writeString(roomNumber);
        dest.writeByte((byte) (hasTaxiService ? 1 : 0));
        dest.writeDouble(servicesTotal);
        dest.writeDouble(additionalCharges);
        dest.writeString(reservationId);
        dest.writeString(confirmationCode);
        dest.writeByte((byte) (canModify ? 1 : 0));
        dest.writeByte((byte) (hasOutstandingBill ? 1 : 0));
        dest.writeByte((byte) (reviewSubmitted ? 1 : 0));
        dest.writeString(checkInDate);
        dest.writeString(checkOutDate);
        dest.writeString(specialRequests);

        dest.writeLong(checkInDateObj != null ? checkInDateObj.getTime() : 0);
        dest.writeLong(checkOutDateObj != null ? checkOutDateObj.getTime() : 0);
        dest.writeLong(actualCheckInTime != null ? actualCheckInTime.getTime() : 0);
        dest.writeLong(checkoutRequestTime != null ? checkoutRequestTime.getTime() : 0);
        dest.writeString(checkInTime);
        dest.writeString(checkOutTime);

        dest.writeList(services);
        dest.writeList(additionalChargesList);

        // Escribir PaymentMethod
        if (guaranteeCard != null) {
            dest.writeByte((byte) 1);
            guaranteeCard.writeToParcel(dest, flags);
        } else {
            dest.writeByte((byte) 0);
        }
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