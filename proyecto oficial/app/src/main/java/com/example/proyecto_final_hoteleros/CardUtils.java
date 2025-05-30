package com.example.proyecto_final_hoteleros;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import java.util.Calendar;

/**
 * Utilidad para detectar y manejar únicamente tarjetas Visa y Mastercard
 * Diseñado específicamente para el sistema de reservas de hoteles
 */
public class CardUtils {
    public static final int UNKNOWN = 0;
    public static final int VISA = 1;
    public static final int MASTERCARD = 2;

    /**
     * Detecta el tipo de tarjeta basado en el número (Solo Visa y Mastercard)
     * @param cardNumber Número de tarjeta
     * @return Constante que representa el tipo de tarjeta
     */
    public static int getCardType(String cardNumber) {
        if (TextUtils.isEmpty(cardNumber)) {
            return UNKNOWN;
        }

        // Eliminar espacios y caracteres especiales
        cardNumber = cardNumber.replaceAll("[\\s-]", "");

        // Validar que solo contenga dígitos
        if (!cardNumber.matches("\\d+")) {
            return UNKNOWN;
        }

        // VISA: Comienza con 4 (longitud 13, 16 o 19)
        if (cardNumber.startsWith("4")) {
            int length = cardNumber.length();
            if (length == 13 || length == 16 || length == 19) {
                return VISA;
            }
        }

        // Mastercard: Rangos específicos actualizados
        if (cardNumber.length() >= 2) {
            String prefix2 = cardNumber.substring(0, 2);
            String prefix4 = cardNumber.length() >= 4 ? cardNumber.substring(0, 4) : "";

            try {
                int prefix2Int = Integer.parseInt(prefix2);

                // Mastercard: 51-55
                if (prefix2Int >= 51 && prefix2Int <= 55) {
                    return MASTERCARD;
                }

                // Mastercard: 2221-2720 (nuevo rango)
                if (!prefix4.isEmpty()) {
                    int prefix4Int = Integer.parseInt(prefix4);
                    if (prefix4Int >= 2221 && prefix4Int <= 2720) {
                        return MASTERCARD;
                    }
                }
            } catch (NumberFormatException e) {
                return UNKNOWN;
            }
        }

        return UNKNOWN;
    }

    /**
     * Valida la longitud de la tarjeta según su tipo
     * @param cardNumber Número de tarjeta
     * @param cardType Tipo de tarjeta
     * @return true si la longitud es correcta
     */
    public static boolean isValidCardLength(String cardNumber, int cardType) {
        if (TextUtils.isEmpty(cardNumber)) {
            return false;
        }

        cardNumber = cardNumber.replaceAll("[\\s-]", "");
        int length = cardNumber.length();

        switch (cardType) {
            case VISA:
                return length == 13 || length == 16 || length == 19;
            case MASTERCARD:
                return length == 16;
            default:
                return false;
        }
    }

    /**
     * Valida el CVV según el tipo de tarjeta
     * @param cvv Código CVV
     * @param cardType Tipo de tarjeta
     * @return true si el CVV es válido
     */
    public static boolean isValidCvv(String cvv, int cardType) {
        if (TextUtils.isEmpty(cvv) || !TextUtils.isDigitsOnly(cvv)) {
            return false;
        }

        // Para Visa y Mastercard siempre son 3 dígitos
        return cvv.length() == 3;
    }

    /**
     * Actualiza el ícono de la tarjeta según el tipo detectado (Solo Visa y Mastercard)
     * @param cardType Tipo de tarjeta
     * @param cardTypeView ImageView para mostrar el ícono
     */
    public static void updateCardTypeIcon(int cardType, ImageView cardTypeView) {
        if (cardTypeView == null) {
            return;
        }

        int resourceId;

        switch (cardType) {
            case VISA:
                resourceId = R.drawable.ic_card_visa;
                break;
            case MASTERCARD:
                resourceId = R.drawable.ic_card_mastercard;
                break;
            default:
                resourceId = R.drawable.ic_card_visa; // Default fallback
                break;
        }

        try {
            cardTypeView.setImageResource(resourceId);
        } catch (Exception e) {
            // Fallback en caso de error
            cardTypeView.setImageResource(R.drawable.ic_card_visa);
        }
    }

    /**
     * Valida el número de tarjeta usando el algoritmo de Luhn
     * @param cardNumber Número de tarjeta sin espacios
     * @return true si la tarjeta es válida según el algoritmo de Luhn
     */
    public static boolean isValidCardNumber(String cardNumber) {
        if (TextUtils.isEmpty(cardNumber)) {
            return false;
        }

        // Limpiar el número
        cardNumber = cardNumber.replaceAll("[\\s-]", "");

        // Validar que solo contenga dígitos
        if (!TextUtils.isDigitsOnly(cardNumber)) {
            return false;
        }

        // Validar longitud (13-19 para Visa, 16 para Mastercard)
        int length = cardNumber.length();
        if (length < 13 || length > 19) {
            return false;
        }

        // Validar que sea Visa o Mastercard
        int cardType = getCardType(cardNumber);
        if (cardType != VISA && cardType != MASTERCARD) {
            return false;
        }

        // Algoritmo de Luhn
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (digit < 0 || digit > 9) {
                return false;
            }

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10) == 0;
    }

    /**
     * Valida el formato y la validez de la fecha de expiración
     * @param expiryDate Fecha de expiración en formato MM/YY
     * @return true si el formato es válido y la fecha no ha expirado
     */
    public static boolean isValidExpiryDate(String expiryDate) {
        if (TextUtils.isEmpty(expiryDate) || expiryDate.length() != 5) {
            return false;
        }

        if (!expiryDate.contains("/")) {
            return false;
        }

        String[] parts = expiryDate.split("/");
        if (parts.length != 2) {
            return false;
        }

        try {
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);

            // Validar mes
            if (month < 1 || month > 12) {
                return false;
            }

            // Validar año (asumiendo formato YY)
            Calendar now = Calendar.getInstance();
            int currentYear = now.get(Calendar.YEAR) % 100; // Últimos 2 dígitos
            int currentMonth = now.get(Calendar.MONTH) + 1; // Calendar.MONTH es 0-based

            // Si el año es menor al actual, asumimos que es del próximo siglo
            if (year < currentYear) {
                year += 100;
            }

            // Validar que no haya expirado
            if (year < currentYear || (year == currentYear && month < currentMonth)) {
                return false;
            }

            // Validar que no sea muy lejano en el futuro (máximo 20 años)
            if (year > currentYear + 20) {
                return false;
            }

            return true;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Formatea el número de tarjeta con espacios apropiados
     * @param cardNumber Número de tarjeta sin formatear
     * @param cardType Tipo de tarjeta
     * @return Número formateado con espacios
     */
    public static String formatCardNumber(String cardNumber, int cardType) {
        if (TextUtils.isEmpty(cardNumber)) {
            return "";
        }

        cardNumber = cardNumber.replaceAll("[\\s-]", "");
        StringBuilder formatted = new StringBuilder();

        // Para Visa y Mastercard usar formato: XXXX XXXX XXXX XXXX
        for (int i = 0; i < cardNumber.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(cardNumber.charAt(i));
        }

        return formatted.toString();
    }

    /**
     * Enmascara el número de tarjeta para mostrar solo los últimos 4 dígitos
     * @param cardNumber Número completo de la tarjeta
     * @return Número enmascarado
     */
    public static String maskCardNumber(String cardNumber) {
        if (TextUtils.isEmpty(cardNumber)) {
            return "";
        }

        cardNumber = cardNumber.replaceAll("[\\s-]", "");

        if (cardNumber.length() < 4) {
            return cardNumber;
        }

        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }

    /**
     * Obtiene el nombre del tipo de tarjeta (Solo Visa y Mastercard)
     * @param cardType Tipo de tarjeta
     * @return Nombre del tipo de tarjeta
     */
    public static String getCardTypeName(int cardType) {
        switch (cardType) {
            case VISA:
                return "Visa";
            case MASTERCARD:
                return "Mastercard";
            default:
                return "Desconocida";
        }
    }

    /**
     * Verifica si el tipo de tarjeta está soportado (solo Visa y Mastercard)
     * @param cardType Tipo de tarjeta
     * @return true si está soportado
     */
    public static boolean isSupportedCardType(int cardType) {
        return cardType == VISA || cardType == MASTERCARD;
    }

    /**
     * Obtiene los colores de la tarjeta según el tipo
     * @param cardType Tipo de tarjeta
     * @return Array con colores [startColor, endColor]
     */
    public static int[] getCardColors(int cardType) {
        switch (cardType) {
            case VISA:
                return new int[]{0xFF1565C0, 0xFF1976D2}; // Azul Visa
            case MASTERCARD:
                return new int[]{0xFFD32F2F, 0xFFF44336}; // Rojo Mastercard
            default:
                return new int[]{0xFF1565C0, 0xFF1976D2}; // Azul por defecto
        }
    }
}