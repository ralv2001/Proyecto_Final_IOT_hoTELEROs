// PaymentMethod.java - Modelo para manejar tarjetas de pago
package com.example.proyecto_final_hoteleros.client.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.firestore.PropertyName;
import java.util.HashMap;
import java.util.Map;

public class PaymentMethod implements Parcelable {

    @PropertyName("id")
    private String id;

    @PropertyName("userId")
    private String userId;

    @PropertyName("cardNumber")
    private String cardNumber; // Enmascarado: **** **** **** 1234

    @PropertyName("cardHolderName")
    private String cardHolderName;

    @PropertyName("cardType")
    private String cardType; // VISA, MASTERCARD, etc.

    @PropertyName("expiryDate")
    private String expiryDate; // MM/YY

    @PropertyName("isDefault")
    private boolean isDefault;

    @PropertyName("isActive")
    private boolean isActive;

    @PropertyName("createdAt")
    private long createdAt;

    @PropertyName("lastUsed")
    private long lastUsed;

    // Constructor vacío requerido para Firestore
    public PaymentMethod() {
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
        this.isDefault = false;
    }

    // Constructor completo
    public PaymentMethod(String userId, String cardNumber, String cardHolderName,
                         String cardType, String expiryDate) {
        this();
        this.userId = userId;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.cardType = cardType;
        this.expiryDate = expiryDate;
        this.id = generateCardId();
    }

    // Constructor desde Parcel
    protected PaymentMethod(Parcel in) {
        id = in.readString();
        userId = in.readString();
        cardNumber = in.readString();
        cardHolderName = in.readString();
        cardType = in.readString();
        expiryDate = in.readString();
        isDefault = in.readByte() != 0;
        isActive = in.readByte() != 0;
        createdAt = in.readLong();
        lastUsed = in.readLong();
    }

    // Generar ID único para la tarjeta
    private String generateCardId() {
        return "card_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    // Convertir a Map para Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("userId", userId);
        map.put("cardNumber", cardNumber);
        map.put("cardHolderName", cardHolderName);
        map.put("cardType", cardType);
        map.put("expiryDate", expiryDate);
        map.put("isDefault", isDefault);
        map.put("isActive", isActive);
        map.put("createdAt", createdAt);
        map.put("lastUsed", lastUsed);
        return map;
    }

    // Crear desde Map (para Firestore)
    public static PaymentMethod fromMap(Map<String, Object> map) {
        PaymentMethod paymentMethod = new PaymentMethod();

        if (map.get("id") != null) paymentMethod.id = (String) map.get("id");
        if (map.get("userId") != null) paymentMethod.userId = (String) map.get("userId");
        if (map.get("cardNumber") != null) paymentMethod.cardNumber = (String) map.get("cardNumber");
        if (map.get("cardHolderName") != null) paymentMethod.cardHolderName = (String) map.get("cardHolderName");
        if (map.get("cardType") != null) paymentMethod.cardType = (String) map.get("cardType");
        if (map.get("expiryDate") != null) paymentMethod.expiryDate = (String) map.get("expiryDate");
        if (map.get("isDefault") != null) paymentMethod.isDefault = (Boolean) map.get("isDefault");
        if (map.get("isActive") != null) paymentMethod.isActive = (Boolean) map.get("isActive");
        if (map.get("createdAt") != null) paymentMethod.createdAt = (Long) map.get("createdAt");
        if (map.get("lastUsed") != null) paymentMethod.lastUsed = (Long) map.get("lastUsed");

        return paymentMethod;
    }

    // Marcar como usada recientemente
    public void markAsUsed() {
        this.lastUsed = System.currentTimeMillis();
    }

    // Obtener últimos 4 dígitos de la tarjeta
    public String getLastFourDigits() {
        if (cardNumber != null && cardNumber.length() >= 4) {
            return cardNumber.substring(cardNumber.length() - 4);
        }
        return "0000";
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastUsed() { return lastUsed; }
    public void setLastUsed(long lastUsed) { this.lastUsed = lastUsed; }

    // Implementación de Parcelable
    public static final Creator<PaymentMethod> CREATOR = new Creator<PaymentMethod>() {
        @Override
        public PaymentMethod createFromParcel(Parcel in) {
            return new PaymentMethod(in);
        }

        @Override
        public PaymentMethod[] newArray(int size) {
            return new PaymentMethod[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userId);
        dest.writeString(cardNumber);
        dest.writeString(cardHolderName);
        dest.writeString(cardType);
        dest.writeString(expiryDate);
        dest.writeByte((byte) (isDefault ? 1 : 0));
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeLong(createdAt);
        dest.writeLong(lastUsed);
    }

    @Override
    public String toString() {
        return "PaymentMethod{" +
                "id='" + id + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", cardHolderName='" + cardHolderName + '\'' +
                ", cardType='" + cardType + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}