package com.example.proyecto_final_hoteleros.client.utils;

import android.util.Log;
import com.example.proyecto_final_hoteleros.client.data.model.Reservation;
import java.util.Date;
import java.util.List;

/**
 * ✅ MANAGER PARA TESTING Y DEBUG DE RESERVAS
 * Permite forzar estados para verificar el flujo completo sin esperar tiempos reales
 */
public class ReservationDebugManager {

    private static final String TAG = "ReservationDebugManager";
    private static ReservationDebugManager instance;

    // ✅ CONFIGURACIÓN PARA TESTING
    private boolean debugModeEnabled = true; // Cambiar a false en producción
    private double testTaxiMinAmount = 800.0; // Monto para testing (configurable por hotel)

    public static synchronized ReservationDebugManager getInstance() {
        if (instance == null) {
            instance = new ReservationDebugManager();
        }
        return instance;
    }

    private ReservationDebugManager() {
        Log.d(TAG, "🐛 ReservationDebugManager inicializado - Modo Debug: " + debugModeEnabled);
    }

    // ========== MÉTODOS PARA FORZAR ESTADOS ==========

    /**
     * ✅ FORZAR RESERVA A ESTADO PRÓXIMO
     */
    public void forceToUpcoming(Reservation reservation) {
        if (!debugModeEnabled) {
            Log.w(TAG, "⚠️ Debug mode deshabilitado");
            return;
        }

        Log.d(TAG, "🔄 Forzando a PRÓXIMO: " + reservation.getHotelName());

        reservation.setStatus(Reservation.STATUS_UPCOMING);
        reservation.setSubStatus(0);

        // Limpiar datos de estadía activa/completada
        reservation.setActualCheckInTime(null);
        reservation.setReviewSubmitted(false);

        logStateChange(reservation, "PRÓXIMO");
    }

    /**
     * ✅ FORZAR RESERVA A ESTADO ACTUAL (CHECK-IN)
     */
    public void forceToActive(Reservation reservation) {
        if (!debugModeEnabled) {
            Log.w(TAG, "⚠️ Debug mode deshabilitado");
            return;
        }

        Log.d(TAG, "🔄 Forzando a ACTUAL (Check-in): " + reservation.getHotelName());

        reservation.setStatus(Reservation.STATUS_ACTIVE);
        reservation.setSubStatus(Reservation.SUBSTATUS_CHECKED_IN);
        reservation.setActualCheckInTime(new Date());

        logStateChange(reservation, "ACTUAL (Check-in)");
    }

    /**
     * ✅ FORZAR RESERVA A ESTADÍA EN CURSO
     */
    public void forceToStaying(Reservation reservation) {
        if (!debugModeEnabled) {
            Log.w(TAG, "⚠️ Debug mode deshabilitado");
            return;
        }

        Log.d(TAG, "🔄 Forzando a ESTADÍA EN CURSO: " + reservation.getHotelName());

        reservation.setStatus(Reservation.STATUS_ACTIVE);
        reservation.setSubStatus(Reservation.SUBSTATUS_STAYING);
        reservation.setActualCheckInTime(new Date(System.currentTimeMillis() - 86400000)); // Ayer

        // Agregar algunos servicios de ejemplo
        reservation.addService("Desayuno buffet", 45.0, 2);
        reservation.addService("Spa relajante", 120.0, 1);

        logStateChange(reservation, "ESTADÍA EN CURSO");
    }

    /**
     * ✅ FORZAR RESERVA A CHECKOUT PENDIENTE
     */
    public void forceToCheckoutPending(Reservation reservation) {
        if (!debugModeEnabled) {
            Log.w(TAG, "⚠️ Debug mode deshabilitado");
            return;
        }

        Log.d(TAG, "🔄 Forzando a CHECKOUT PENDIENTE: " + reservation.getHotelName());

        reservation.setStatus(Reservation.STATUS_ACTIVE);
        reservation.setSubStatus(Reservation.SUBSTATUS_CHECKOUT_PENDING);

        // Agregar servicios para completar la estadía
        if (reservation.getServices().isEmpty()) {
            reservation.addService("Room service", 85.0, 2);
            reservation.addService("Minibar", 45.0, 1);
        }

        // Usar el método interno para solicitar checkout
        reservation.requestCheckout();

        logStateChange(reservation, "CHECKOUT PENDIENTE");
    }

    /**
     * ✅ FORZAR RESERVA A COMPLETADO (SIN TAXI)
     */
    public void forceToCompletedNoTaxi(Reservation reservation) {
        if (!debugModeEnabled) {
            Log.w(TAG, "⚠️ Debug mode deshabilitado");
            return;
        }

        Log.d(TAG, "🔄 Forzando a COMPLETADO (Sin taxi): " + reservation.getHotelName());

        reservation.setStatus(Reservation.STATUS_COMPLETED);
        reservation.setSubStatus(0);

        // Para asegurar que NO califique para taxi, mantenemos servicios mínimos
        // (No hay manera de limpiar servicios, así que usamos lo que hay)

        reservation.setReviewSubmitted(false);
        reservation.setHasTaxiService(false);

        logStateChange(reservation, "COMPLETADO (Sin taxi)");
        logTaxiEligibility(reservation);
    }

    /**
     * ✅ FORZAR RESERVA A COMPLETADO (CON TAXI DISPONIBLE)
     */
    public void forceToCompletedWithTaxi(Reservation reservation) {
        if (!debugModeEnabled) {
            Log.w(TAG, "⚠️ Debug mode deshabilitado");
            return;
        }

        Log.d(TAG, "🔄 Forzando a COMPLETADO (Con taxi): " + reservation.getHotelName());

        reservation.setStatus(Reservation.STATUS_COMPLETED);
        reservation.setSubStatus(0);

        // Asegurar que SÍ califique para taxi agregando servicios premium
        reservation.addService("Spa premium", 150.0, 2);
        reservation.addService("Room service gourmet", 120.0, 2);
        reservation.addService("Minibar premium", 80.0, 1);

        // Verificar elegibilidad
        double currentTotal = reservation.getBasePrice() + reservation.getServicesTotal();
        if (currentTotal >= testTaxiMinAmount) {
            reservation.setHasTaxiService(true);
            Log.d(TAG, "✅ Taxi habilitado - Total: S/ " + currentTotal + " ≥ S/ " + testTaxiMinAmount);
        } else {
            Log.w(TAG, "⚠️ Aún no califica para taxi - Total: S/ " + currentTotal + " < S/ " + testTaxiMinAmount);
        }

        reservation.setReviewSubmitted(false);

        logStateChange(reservation, "COMPLETADO (Con taxi)");
        logTaxiEligibility(reservation);
    }

    /**
     * ✅ FORZAR RESERVA A COMPLETADO (CON REVIEW ENVIADO)
     */
    public void forceToCompletedWithReview(Reservation reservation) {
        if (!debugModeEnabled) {
            Log.w(TAG, "⚠️ Debug mode deshabilitado");
            return;
        }

        Log.d(TAG, "🔄 Forzando a COMPLETADO (Review enviado): " + reservation.getHotelName());

        forceToCompletedWithTaxi(reservation); // Primero configurar como completado con taxi
        reservation.setReviewSubmitted(true);

        logStateChange(reservation, "COMPLETADO (Review enviado)");
    }

    // ========== MÉTODOS PARA TESTING DE FLUJO COMPLETO ==========

    /**
     * ✅ SIMULAR FLUJO COMPLETO DE RESERVA (AUTOMÁTICO)
     */
    public void simulateFullReservationFlow(Reservation reservation, FlowCallback callback) {
        if (!debugModeEnabled) {
            Log.w(TAG, "⚠️ Debug mode deshabilitado");
            return;
        }

        Log.d(TAG, "🎬 Iniciando simulación de flujo completo para: " + reservation.getHotelName());

        // Paso 1: Próximo
        forceToUpcoming(reservation);
        callback.onStateChanged("Próximo", reservation);

        // Paso 2: Check-in (después de 2 segundos)
        new android.os.Handler().postDelayed(() -> {
            forceToActive(reservation);
            callback.onStateChanged("Check-in", reservation);

            // Paso 3: Estadía en curso (después de 2 segundos)
            new android.os.Handler().postDelayed(() -> {
                forceToStaying(reservation);
                callback.onStateChanged("Estadía en curso", reservation);

                // Paso 4: Checkout pendiente (después de 2 segundos)
                new android.os.Handler().postDelayed(() -> {
                    forceToCheckoutPending(reservation);
                    callback.onStateChanged("Checkout pendiente", reservation);

                    // Paso 5: Completado con taxi (después de 2 segundos)
                    new android.os.Handler().postDelayed(() -> {
                        forceToCompletedWithTaxi(reservation);
                        callback.onStateChanged("Completado con taxi", reservation);
                        callback.onFlowCompleted();
                    }, 2000);
                }, 2000);
            }, 2000);
        }, 2000);
    }

    // ========== MÉTODOS DE CONFIGURACIÓN ==========

    /**
     * ✅ CONFIGURAR MONTO MÍNIMO DE TAXI PARA TESTING
     */
    public void setTestTaxiMinAmount(double amount) {
        this.testTaxiMinAmount = amount;
        Log.d(TAG, "🚕 Monto mínimo taxi actualizado para testing: S/ " + amount);
    }

    public double getTestTaxiMinAmount() {
        return testTaxiMinAmount;
    }

    /**
     * ✅ HABILITAR/DESHABILITAR MODO DEBUG
     */
    public void setDebugMode(boolean enabled) {
        this.debugModeEnabled = enabled;
        Log.d(TAG, "🐛 Modo debug: " + (enabled ? "HABILITADO" : "DESHABILITADO"));
    }

    public boolean isDebugModeEnabled() {
        return debugModeEnabled;
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    private void logStateChange(Reservation reservation, String newState) {
        Log.d(TAG, "📋 ========== CAMBIO DE ESTADO ==========");
        Log.d(TAG, "🏨 Hotel: " + reservation.getHotelName());
        Log.d(TAG, "📅 Estado: " + newState);
        Log.d(TAG, "🔧 Sub-estado: " + reservation.getSubStatus());
        Log.d(TAG, "💰 Precio base: S/ " + reservation.getBasePrice());
        Log.d(TAG, "🛎️ Servicios: " + reservation.getServices().size() + " (S/ " + reservation.getServicesTotal() + ")");
        Log.d(TAG, "💵 Total: S/ " + reservation.getFinalTotal());
        Log.d(TAG, "✏️ Puede modificar: " + reservation.canModify());
        Log.d(TAG, "⭐ Review enviado: " + reservation.isReviewSubmitted());
        Log.d(TAG, "=====================================");
    }

    private void logTaxiEligibility(Reservation reservation) {
        double total = reservation.getFinalTotal();
        boolean eligible = total >= testTaxiMinAmount;

        Log.d(TAG, "🚕 ========== ELEGIBILIDAD TAXI ==========");
        Log.d(TAG, "💰 Total reserva: S/ " + total);
        Log.d(TAG, "🎯 Mínimo requerido: S/ " + testTaxiMinAmount);
        Log.d(TAG, "✅ Califica: " + (eligible ? "SÍ" : "NO"));
        if (!eligible) {
            Log.d(TAG, "💡 Necesita: S/ " + (testTaxiMinAmount - total) + " más");
        }
        Log.d(TAG, "========================================");
    }

    /**
     * ✅ CREAR RESERVA DE TESTING COMPLETA CON ID ÚNICO
     */
    public Reservation createTestReservation(String hotelName, double basePrice) {
        Reservation reservation = new Reservation(
                hotelName,
                "Lima, Miraflores",
                "22 Jul - 25 Jul, 2025",
                basePrice,
                4.8f,
                com.example.proyecto_final_hoteleros.R.drawable.belmond,
                Reservation.STATUS_UPCOMING
        );

        // ✅ ASEGURAR ID ÚNICO para identificación en debug
        String uniqueId = "DEBUG_" + System.currentTimeMillis() + "_" + Math.random();
        reservation.setId(uniqueId);

        reservation.setRoomType("Suite Test");
        reservation.setRoomNumber("TEST" + System.currentTimeMillis() % 1000);
        reservation.setSpecialRequests("Reserva de testing - Debug mode");

        // Configurar tarjeta de prueba
        Reservation.PaymentMethod testCard = new Reservation.PaymentMethod("1234", "Visa", "Usuario Test");
        reservation.setGuaranteeCard(testCard);

        Log.d(TAG, "🆕 Reserva de testing creada: " + hotelName + " (S/ " + basePrice + ") - ID: " + uniqueId);
        return reservation;
    }

    /**
     * ✅ NUEVO: Crear reserva con monto específico para testing de taxi
     */
    public Reservation createTestReservationForTaxiAmount(String hotelName, double targetTotal) {
        // Calcular precio base para que el total sea exactamente el targetTotal
        double basePrice = targetTotal * 0.7; // 70% base, 30% servicios
        double servicesNeeded = targetTotal - basePrice;

        Reservation reservation = createTestReservation(hotelName, basePrice);

        // Agregar servicios para alcanzar el total exacto
        if (servicesNeeded > 0) {
            // Distribuir los servicios de manera realista
            double service1 = servicesNeeded * 0.6;
            double service2 = servicesNeeded * 0.4;

            reservation.addService("Servicio Premium", service1, 1);
            reservation.addService("Servicio Adicional", service2, 1);
        }

        Log.d(TAG, "🎯 Reserva para monto específico: " + hotelName + " (Total objetivo: S/ " + targetTotal + ", Real: S/ " + reservation.getFinalTotal() + ")");
        return reservation;
    }

    // ========== INTERFACES PARA CALLBACKS ==========

    public interface FlowCallback {
        void onStateChanged(String stateName, Reservation reservation);
        void onFlowCompleted();
    }

    // ========== MÉTODOS PARA VERIFICAR ESTADO ACTUAL ==========

    public String getDetailedStatus(Reservation reservation) {
        String status = "DESCONOCIDO";
        String details = "";

        switch (reservation.getStatus()) {
            case Reservation.STATUS_UPCOMING:
                status = "PRÓXIMO";
                details = "Esperando check-in";
                break;

            case Reservation.STATUS_ACTIVE:
                status = "ACTUAL";
                switch (reservation.getSubStatus()) {
                    case Reservation.SUBSTATUS_CHECKED_IN:
                        details = "Recién llegado";
                        break;
                    case Reservation.SUBSTATUS_STAYING:
                        details = "Estadía en curso";
                        break;
                    case Reservation.SUBSTATUS_CHECKOUT_PENDING:
                        details = "Checkout pendiente";
                        break;
                    default:
                        details = "Estado activo";
                        break;
                }
                break;

            case Reservation.STATUS_COMPLETED:
                status = "COMPLETADO";
                if (reservation.isReviewSubmitted()) {
                    details = "Review enviado";
                } else if (reservation.isEligibleForFreeTaxi()) {
                    details = "Taxi disponible";
                } else {
                    details = "Estadía finalizada";
                }
                break;
        }

        return status + " - " + details;
    }
}