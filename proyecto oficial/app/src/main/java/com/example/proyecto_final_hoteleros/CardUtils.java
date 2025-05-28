package com.example.proyecto_final_hoteleros;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

/**
 * Utilidad para detectar y manejar tipos de tarjeta
 */
public class CardUtils {
    public static final int UNKNOWN = 0;
    public static final int VISA = 1;
    public static final int MASTERCARD = 2;
    public static final int AMEX = 3;
    public static final int DISCOVER = 4;

    /**
     * Detecta el tipo de tarjeta basado en el número
     *
     * @param cardNumber Número de tarjeta
     * @return Constante que representa el tipo de tarjeta
     */
    // En CardUtils.java
    public static int getCardType(String cardNumber) {
        if (TextUtils.isEmpty(cardNumber)) {
            return UNKNOWN;
        }

        // Elimina espacios
        cardNumber = cardNumber.replaceAll("\\s+", "");

        // VISA: Comienza con 4
        if (cardNumber.startsWith("4")) {
            return VISA;
        }

        // Mastercard: Comienza con 51-55 o 2221-2720
        if ((cardNumber.startsWith("51") || cardNumber.startsWith("52") ||
                cardNumber.startsWith("53") || cardNumber.startsWith("54") ||
                cardNumber.startsWith("55")) ||
                (cardNumber.length() >= 4 &&
                        Long.parseLong(cardNumber.substring(0, 4)) >= 2221 &&
                        Long.parseLong(cardNumber.substring(0, 4)) <= 2720))
        {
            return MASTERCARD;
        }

        return UNKNOWN;
    }

    // Agregar este método para validar longitud según el tipo de tarjeta
    public static boolean isValidCardLength(String cardNumber, int cardType) {
        if (TextUtils.isEmpty(cardNumber)) {
            return false;
        }

        cardNumber = cardNumber.replaceAll("\\s+", "");
        int length = cardNumber.length();

        switch (cardType) {
            case VISA:
            case MASTERCARD:
                return length == 16;
            default:
                return length >= 13 && length <= 19; // Longitud genérica
        }
    }
    // Modificar para validar el CVV (3 dígitos)
    public static boolean isValidCvv(String cvv, int cardType) {
        if (TextUtils.isEmpty(cvv)) {
            return false;
        }

        if (!TextUtils.isDigitsOnly(cvv)) {
            return false;
        }

        return cvv.length() == 3;
    }
    /**
     * Actualiza el ícono de la tarjeta según el tipo detectado
     *
     * @param cardType Tipo de tarjeta
     * @param cardTypeView ImageView para mostrar el ícono
     */
    public static void updateCardTypeIcon(int cardType, ImageView cardTypeView) {
        if (cardTypeView == null) {
            return;
        }

        Context context = cardTypeView.getContext();
        int resourceId;

        switch (cardType) {
            case VISA:
                resourceId = context.getResources().getIdentifier(
                        "ic_card_visa", "drawable",
                        context.getPackageName());
                break;
            case MASTERCARD:
                resourceId = context.getResources().getIdentifier(
                        "ic_card_mastercard", "drawable",
                        context.getPackageName());
                break;
            case AMEX:
                resourceId = context.getResources().getIdentifier(
                        "ic_card_amex", "drawable",
                        context.getPackageName());
                break;
            case DISCOVER:
                resourceId = context.getResources().getIdentifier(
                        "ic_card_discover", "drawable",
                        context.getPackageName());
                break;
            default:
                resourceId = context.getResources().getIdentifier(
                        "ic_card_generic", "drawable",
                        context.getPackageName());
                break;
        }

        // Si el recurso existe, establecerlo
        if (resourceId != 0) {
            cardTypeView.setImageResource(resourceId);
        }
    }

    /**
     * Valida el número de tarjeta usando el algoritmo de Luhn
     *
     * @param cardNumber Número de tarjeta sin espacios
     * @return true si la tarjeta es válida según el algoritmo de Luhn
     */
    public static boolean isValidCardNumber(String cardNumber) {
        if (TextUtils.isEmpty(cardNumber)) {
            return false;
        }

        cardNumber = cardNumber.replaceAll("\\s+", "");
        if (!TextUtils.isDigitsOnly(cardNumber)) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }

        return sum % 10 == 0;
    }

    /**
     * Valida el formato de la fecha de expiración (MM/YY)
     *
     * @param expiryDate Fecha de expiración en formato MM/YY
     * @return true si el formato es válido
     */
    public static boolean isValidExpiryDate(String expiryDate) {
        if (TextUtils.isEmpty(expiryDate) || expiryDate.length() != 5) {
            return false;
        }

        String[] parts = expiryDate.split("/");
        if (parts.length != 2) {
            return false;
        }

        try {
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);
            return month >= 1 && month <= 12;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}