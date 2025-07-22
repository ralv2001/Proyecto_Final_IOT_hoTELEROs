package com.example.proyecto_final_hoteleros.client.utils;

import android.util.Log;

/**
 * ✅ Manager MEJORADO para manejar lógica del taxi con monto configurable por hotel
 * CON ACTUALIZACIÓN DINÁMICA Y MENSAJES CLAROS
 */
public class TaxiConfigManager {

    private static final String TAG = "TaxiConfigManager";

    /**
     * ✅ Verifica si un cliente califica para taxi gratuito
     * @param totalReservation Total actual de la reserva (habitación + servicios)
     * @param minAmount Monto mínimo configurado por el admin del hotel
     */
    public static boolean qualifiesForFreeTaxi(double totalReservation, double minAmount) {
        boolean qualifies = totalReservation >= minAmount;
        Log.d(TAG, String.format("🚕 Taxi check - Total: S/. %.2f, Mínimo: S/. %.2f, Califica: %s",
                totalReservation, minAmount, qualifies ? "SÍ" : "NO"));
        return qualifies;
    }

    /**
     * ✅ MEJORADO: Genera mensaje descriptivo para el servicio de taxi
     * @param currentTotal Total actual de la reserva
     * @param minAmount Monto mínimo configurado por el admin
     */
    public static String getTaxiMessage(double currentTotal, double minAmount) {
        if (qualifiesForFreeTaxi(currentTotal, minAmount)) {
            return String.format("🎉 ¡DESBLOQUEADO! Total: S/. %.0f (Ahorro: S/. 60)", currentTotal);
        } else {
            double needed = minAmount - currentTotal;
            return String.format("💡 Total actual: S/. %.0f - Agrega S/. %.0f más para desbloquearlo GRATIS",
                    currentTotal, needed);
        }
    }

    /**
     * ✅ NUEVO: Obtiene mensaje específico para el badge en la UI
     */
    public static String getTaxiBadgeText(double currentTotal, double minAmount) {
        if (qualifiesForFreeTaxi(currentTotal, minAmount)) {
            return "¡DESBLOQUEADO!";
        } else {
            double needed = minAmount - currentTotal;
            return String.format("Necesitas S/. %.0f más", needed);
        }
    }

    /**
     * ✅ NUEVO: Obtiene mensaje para el precio en la UI
     */
    public static String getTaxiPriceText(double currentTotal, double minAmount) {
        if (qualifiesForFreeTaxi(currentTotal, minAmount)) {
            return "¡DESBLOQUEADO!";
        } else {
            return String.format("Mínimo: S/. %.0f", minAmount);
        }
    }

    /**
     * ✅ NUEVO: Obtiene mensaje completo para el botón
     */
    public static String getTaxiButtonText(double currentTotal, double minAmount, boolean isSelected) {
        if (qualifiesForFreeTaxi(currentTotal, minAmount)) {
            return isSelected ? "✓ Agregado" : "+ Agregar GRATIS";
        } else {
            return String.format("Bloqueado (S/. %.0f)", minAmount);
        }
    }

    /**
     * ✅ Verifica si el taxi debe mostrarse como disponible para agregar
     */
    public static boolean isTaxiAvailableToAdd(double currentTotal, double minAmount) {
        return qualifiesForFreeTaxi(currentTotal, minAmount);
    }

    /**
     * ✅ NUEVO: Calcula cuánto dinero falta para desbloquear el taxi
     */
    public static double getAmountNeededForFreeTaxi(double currentTotal, double minAmount) {
        if (qualifiesForFreeTaxi(currentTotal, minAmount)) {
            return 0.0; // Ya califica
        }
        return minAmount - currentTotal;
    }

    /**
     * ✅ NUEVO: Obtiene mensaje de progreso hacia el taxi gratuito
     */
    public static String getTaxiProgressMessage(double currentTotal, double minAmount) {
        if (qualifiesForFreeTaxi(currentTotal, minAmount)) {
            double excess = currentTotal - minAmount;
            return String.format("✅ Taxi desbloqueado con S/. %.0f de exceso", excess);
        } else {
            double needed = minAmount - currentTotal;
            double percentage = (currentTotal / minAmount) * 100;
            return String.format("📊 Progreso: %.0f%% - Faltan S/. %.0f", percentage, needed);
        }
    }

    /**
     * ✅ NUEVO: Obtiene color sugerido para la UI según el estado del taxi
     */
    public static String getTaxiUIColorState(double currentTotal, double minAmount) {
        if (qualifiesForFreeTaxi(currentTotal, minAmount)) {
            return "green"; // Verde - desbloqueado
        } else {
            double percentage = (currentTotal / minAmount) * 100;
            if (percentage >= 80) {
                return "orange"; // Naranja - muy cerca
            } else if (percentage >= 50) {
                return "yellow"; // Amarillo - a mitad de camino
            } else {
                return "gray"; // Gris - lejos del objetivo
            }
        }
    }

    /**
     * ✅ NUEVO: Simula el cálculo dinámico del taxi cuando se agrega un servicio
     * @param currentTotal Total actual sin el nuevo servicio
     * @param servicePrice Precio del servicio que se va a agregar
     * @param minAmount Monto mínimo para taxi gratuito
     * @return true si agregando este servicio se desbloquea el taxi
     */
    public static boolean wouldUnlockTaxiWithService(double currentTotal, double servicePrice, double minAmount) {
        double newTotal = currentTotal + servicePrice;
        boolean currentlyQualifies = qualifiesForFreeTaxi(currentTotal, minAmount);
        boolean wouldQualify = qualifiesForFreeTaxi(newTotal, minAmount);

        // Solo retorna true si actualmente NO califica pero SÍ calificaría con el nuevo servicio
        return !currentlyQualifies && wouldQualify;
    }

    /**
     * ✅ NUEVO: Obtiene mensaje de incentivo para agregar un servicio específico
     */
    public static String getTaxiIncentiveMessage(double currentTotal, double servicePrice, double minAmount, String serviceName) {
        if (wouldUnlockTaxiWithService(currentTotal, servicePrice, minAmount)) {
            return String.format("🎉 ¡Agregando %s desbloquearás el taxi GRATIS!", serviceName);
        } else if (!qualifiesForFreeTaxi(currentTotal + servicePrice, minAmount)) {
            double stillNeeded = minAmount - (currentTotal + servicePrice);
            return String.format("📈 Con %s estarás S/. %.0f más cerca del taxi gratuito", serviceName, stillNeeded);
        } else {
            return ""; // Ya califica o el servicio no cambia nada
        }
    }
}