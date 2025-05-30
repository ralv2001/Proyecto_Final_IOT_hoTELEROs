package com.example.proyecto_final_hoteleros.taxista.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PaymentMethod implements Parcelable {

    public enum PaymentType {
        CASH("Efectivo"),
        CARD("Tarjeta"),
        DIGITAL_WALLET("Billetera Digital"),
        BANK_TRANSFER("Transferencia");

        private final String displayName;

        PaymentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private String id;
    private PaymentType type;
    private String name;
    private String description;
    private String accountNumber; // Para tarjetas: **** **** **** 1234
    private String bankName;
    private boolean isEnabled;
    private boolean isDefault;
    private int iconResId;
    private String lastUsed; // Fecha del último uso
    private double totalReceived; // Total recibido por este método

    public PaymentMethod(String id, PaymentType type, String name, String description,
                         String accountNumber, String bankName, boolean isEnabled,
                         boolean isDefault, int iconResId, String lastUsed, double totalReceived) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.isEnabled = isEnabled;
        this.isDefault = isDefault;
        this.iconResId = iconResId;
        this.lastUsed = lastUsed;
        this.totalReceived = totalReceived;
    }

    protected PaymentMethod(Parcel in) {
        id = in.readString();
        type = PaymentType.valueOf(in.readString());
        name = in.readString();
        description = in.readString();
        accountNumber = in.readString();
        bankName = in.readString();
        isEnabled = in.readByte() != 0;
        isDefault = in.readByte() != 0;
        iconResId = in.readInt();
        lastUsed = in.readString();
        totalReceived = in.readDouble();
    }

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
        dest.writeString(type.name());
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(accountNumber);
        dest.writeString(bankName);
        dest.writeByte((byte) (isEnabled ? 1 : 0));
        dest.writeByte((byte) (isDefault ? 1 : 0));
        dest.writeInt(iconResId);
        dest.writeString(lastUsed);
        dest.writeDouble(totalReceived);
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public PaymentType getType() { return type; }
    public void setType(PaymentType type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }

    public String getLastUsed() { return lastUsed; }
    public void setLastUsed(String lastUsed) { this.lastUsed = lastUsed; }

    public double getTotalReceived() { return totalReceived; }
    public void setTotalReceived(double totalReceived) { this.totalReceived = totalReceived; }

    // Métodos de utilidad
    public String getFormattedTotalReceived() {
        return "S/ " + String.format("%.2f", totalReceived);
    }

    public String getDisplayAccountNumber() {
        if (accountNumber != null && accountNumber.length() > 4) {
            return "**** " + accountNumber.substring(accountNumber.length() - 4);
        }
        return accountNumber;
    }

    public boolean isCash() {
        return type == PaymentType.CASH;
    }

    public boolean isDigital() {
        return type == PaymentType.DIGITAL_WALLET || type == PaymentType.BANK_TRANSFER;
    }
}