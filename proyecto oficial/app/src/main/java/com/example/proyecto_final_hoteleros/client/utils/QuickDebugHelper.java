package com.example.proyecto_final_hoteleros.client.utils;

import android.util.Log;
import com.example.proyecto_final_hoteleros.client.data.model.Reservation;

/**
 * ✅ HELPER SIMPLE PARA DEBUG RÁPIDO
 * Métodos estáticos para testing rápido sin problemas de compilación
 */
public class QuickDebugHelper {

    private static final String TAG = "QuickDebugHelper";

    /**
     * ✅ HABILITAR DEBUG MODE RÁPIDAMENTE
     */
    public static void enableDebugMode() {
        ReservationDebugManager.getInstance().setDebugMode(true);
        Log.d(TAG, "🐛 Debug mode HABILITADO");
    }

    /**
     * ✅ DESHABILITAR DEBUG MODE
     */
    public static void disableDebugMode() {
        ReservationDebugManager.getInstance().setDebugMode(false);
        Log.d(TAG, "🔒 Debug mode DESHABILITADO");
    }

    /**
     * ✅ CREAR RESERVA SIMPLE PARA TESTING CON ID ÚNICO
     */
    public static Reservation createSimpleTestReservation() {
        ReservationDebugManager debug = ReservationDebugManager.getInstance();
        String uniqueName = "Hotel Debug " + System.currentTimeMillis() % 10000;
        return debug.createTestReservation(uniqueName, 800.0);
    }

    /**
     * ✅ CREAR MÚLTIPLES RESERVAS PARA TESTING COMPLETO
     */
    public static Reservation[] createTestSuite() {
        ReservationDebugManager debug = ReservationDebugManager.getInstance();

        return new Reservation[] {
                debug.createTestReservation("Hotel Próximo Test", 500.0),
                debug.createTestReservation("Hotel Activo Test", 900.0),
                debug.createTestReservation("Hotel Completado Test", 1200.0)
        };
    }

    /**
     * ✅ TESTING ESPECÍFICO DE ACTUALIZACIÓN DE UI
     */
    public static void testUIUpdates() {
        Log.d(TAG, "🎨 ========== TESTING ACTUALIZACIÓN UI ==========");

        ReservationDebugManager debug = ReservationDebugManager.getInstance();
        if (!debug.isDebugModeEnabled()) {
            debug.setDebugMode(true);
            Log.d(TAG, "✅ Debug mode habilitado");
        }

        // Crear reservas para cada estado
        Reservation[] testSuite = createTestSuite();

        Log.d(TAG, "📋 Creadas " + testSuite.length + " reservas de prueba:");
        for (int i = 0; i < testSuite.length; i++) {
            Reservation r = testSuite[i];
            Log.d(TAG, "   " + (i+1) + ". " + r.getHotelName() + " (ID: " + r.getReservationId() + ")");
        }

        // Cambiar estados para probar actualización
        Log.d(TAG, "🔄 Cambiando estados para testing UI...");

        debug.forceToUpcoming(testSuite[0]);
        Log.d(TAG, "   ✅ " + testSuite[0].getHotelName() + " → PRÓXIMO");

        debug.forceToActive(testSuite[1]);
        Log.d(TAG, "   ✅ " + testSuite[1].getHotelName() + " → ACTIVO");

        debug.forceToCompletedWithTaxi(testSuite[2]);
        Log.d(TAG, "   ✅ " + testSuite[2].getHotelName() + " → COMPLETADO");

        Log.d(TAG, "🏁 Testing UI completado - Las cartas deberían cambiar ahora");
        Log.d(TAG, "💡 TIP: Haz long press en las cartas para abrir el diálogo debug");
        Log.d(TAG, "================================================");
    }

    /**
     * ✅ VERIFICAR SI UNA RESERVA CALIFICA PARA TAXI
     */
    public static void checkTaxiEligibility(Reservation reservation) {
        double total = reservation.getFinalTotal();
        double minAmount = ReservationDebugManager.getInstance().getTestTaxiMinAmount();
        boolean eligible = total >= minAmount;

        Log.d(TAG, "🚕 ========== VERIFICACIÓN TAXI ==========");
        Log.d(TAG, "🏨 Hotel: " + reservation.getHotelName());
        Log.d(TAG, "💰 Total: S/ " + total);
        Log.d(TAG, "🎯 Mínimo: S/ " + minAmount);
        Log.d(TAG, "✅ Califica: " + (eligible ? "SÍ" : "NO"));
        if (!eligible) {
            Log.d(TAG, "💡 Necesita: S/ " + (minAmount - total) + " más");
        }
        Log.d(TAG, "========================================");
    }

    /**
     * ✅ IMPRIMIR ESTADO ACTUAL DE UNA RESERVA
     */
    public static void printReservationStatus(Reservation reservation) {
        Log.d(TAG, "📋 ========== ESTADO RESERVA ==========");
        Log.d(TAG, "🏨 Hotel: " + reservation.getHotelName());
        Log.d(TAG, "📅 Estado: " + reservation.getStatusText());
        Log.d(TAG, "🔧 Sub-estado: " + reservation.getSubStatus());
        Log.d(TAG, "💰 Precio base: S/ " + reservation.getBasePrice());
        Log.d(TAG, "🛎️ Servicios: " + reservation.getServices().size() + " (S/ " + reservation.getServicesTotal() + ")");
        Log.d(TAG, "💵 Total: S/ " + reservation.getFinalTotal());
        Log.d(TAG, "✏️ Puede modificar: " + reservation.canModify());
        Log.d(TAG, "⭐ Review enviado: " + reservation.isReviewSubmitted());
        Log.d(TAG, "🚕 Taxi elegible: " + reservation.isEligibleForFreeTaxi());
        Log.d(TAG, "=====================================");
    }

    /**
     * ✅ TESTING RÁPIDO DE ESTADOS
     */
    public static void quickStateTest(Reservation reservation) {
        Log.d(TAG, "🚀 ========== TESTING RÁPIDO ==========");

        ReservationDebugManager debug = ReservationDebugManager.getInstance();
        if (!debug.isDebugModeEnabled()) {
            Log.w(TAG, "⚠️ Debug mode deshabilitado, habilitando...");
            debug.setDebugMode(true);
        }

        // Test estado próximo
        Log.d(TAG, "1️⃣ Probando estado PRÓXIMO...");
        debug.forceToUpcoming(reservation);
        printReservationStatus(reservation);

        // Pequeña pausa para logs
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Test estado activo
        Log.d(TAG, "2️⃣ Probando estado ACTIVO...");
        debug.forceToActive(reservation);
        printReservationStatus(reservation);

        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Test completado con taxi
        Log.d(TAG, "3️⃣ Probando estado COMPLETADO...");
        debug.forceToCompletedWithTaxi(reservation);
        printReservationStatus(reservation);
        checkTaxiEligibility(reservation);

        Log.d(TAG, "🏁 ========== TESTING COMPLETADO ==========");
    }

    /**
     * ✅ CONFIGURAR MONTO DE TAXI PARA TESTING
     */
    public static void setTaxiAmountForTesting(double amount) {
        ReservationDebugManager.getInstance().setTestTaxiMinAmount(amount);
        Log.d(TAG, "🚕 Monto mínimo taxi configurado: S/ " + amount);
    }

    /**
     * ✅ VERIFICAR QUE FUNCIONE EL SISTEMA DE TESTING
     */
    public static boolean verifyTestingSystem() {
        Log.d(TAG, "🔍 ========== VERIFICANDO SISTEMA ==========");

        try {
            // Crear reserva de prueba
            Reservation testReservation = createSimpleTestReservation();
            if (testReservation == null) {
                Log.e(TAG, "❌ No se pudo crear reserva de prueba");
                return false;
            }

            Log.d(TAG, "✅ Reserva creada: " + testReservation.getHotelName() + " (ID: " + testReservation.getReservationId() + ")");

            // Probar cambio de estado
            ReservationDebugManager debug = ReservationDebugManager.getInstance();
            debug.setDebugMode(true);

            int initialStatus = testReservation.getStatus();
            debug.forceToUpcoming(testReservation);

            if (testReservation.getStatus() != Reservation.STATUS_UPCOMING) {
                Log.e(TAG, "❌ No se pudo cambiar estado a UPCOMING");
                return false;
            }

            debug.forceToCompletedWithTaxi(testReservation);

            if (testReservation.getStatus() != Reservation.STATUS_COMPLETED) {
                Log.e(TAG, "❌ No se pudo cambiar estado a COMPLETED");
                return false;
            }

            Log.d(TAG, "✅ Sistema de testing funciona correctamente");
            Log.d(TAG, "✅ Estados cambian correctamente: " + initialStatus + " → " + testReservation.getStatus());
            Log.d(TAG, "========================================");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error verificando sistema: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}