package com.example.proyecto_final_hoteleros.utils;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utilidad para formatear precios y montos monetarios
 */
public class PriceFormatter {

    private static final Locale LOCALE_PERU = new Locale("es", "PE");

    /**
     * Formatea un monto en soles con el formato "S/. XX.XX"
     * @param amount El monto a formatear
     * @return String formateado
     */
    public static String formatSoles(double amount) {
        return String.format("S/. %.2f", amount);
    }

    /**
     * Formatea un monto con el formato de moneda peruano
     * Ejemplo: "S/ 1,234.56"
     * @param amount El monto a formatear
     * @return String formateado con el formato de moneda peruana
     */
    public static String formatCurrencyPeru(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(LOCALE_PERU);
        return formatter.format(amount);
    }

    /**
     * Convierte un precio opcional (que puede ser null) a un texto formateado
     * @param price El precio que puede ser null
     * @param defaultText Texto a mostrar si el precio es null
     * @return Precio formateado o texto por defecto
     */
    public static String formatOptionalPrice(Double price, String defaultText) {
        if (price == null) {
            return defaultText;
        }
        return formatSoles(price);
    }

    /**
     * Formatea un precio con descuento, mostrando el precio original tachado
     * @param originalPrice Precio original
     * @param discountedPrice Precio con descuento
     * @return String con formato HTML que muestra el precio original tachado y el precio con descuento
     */
    public static String formatDiscountedPrice(double originalPrice, double discountedPrice) {
        String original = formatSoles(originalPrice);
        String discounted = formatSoles(discountedPrice);
        return String.format("<strike>%s</strike> %s", original, discounted);
    }
}