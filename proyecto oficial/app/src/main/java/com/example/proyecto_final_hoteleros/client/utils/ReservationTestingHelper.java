package com.example.proyecto_final_hoteleros.client.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.example.proyecto_final_hoteleros.client.data.model.Reservation;
import com.example.proyecto_final_hoteleros.client.ui.fragment.HistorialFragment;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ HELPER ESPECÍFICO PARA TESTING DE RESERVAS Y FLUJO COMPLETO
 * Facilita las pruebas del sistema de gestión de reservas con todos los estados
 */
public class ReservationTestingHelper {

    private static final String TAG = "ReservationTestingHelper";

    private Context context;
    private HistorialFragment historialFragment;
    private ReservationDebugManager debugManager;

    // ✅ CONFIGURACIÓN DE TESTING
    private boolean verboseLogging = true;
    private double[] testTaxiAmounts = {500.0, 800.0, 1000.0, 1200.0, 1500.0}; // Diferentes montos para probar

    public ReservationTestingHelper(Context context, HistorialFragment fragment) {
        this.context = context;
        this.historialFragment = fragment;
        this.debugManager = ReservationDebugManager.getInstance();

        Log.d(TAG, "🧪 ReservationTestingHelper inicializado");
    }

    // ========== MÉTODOS PARA CREAR ESCENARIOS DE TESTING ==========

    /**
     * ✅ CREAR SUITE COMPLETA DE TESTING CON TODOS LOS ESCENARIOS
     */
    public List<Reservation> createFullTestingSuite() {
        Log.d(TAG, "🧪 ========== CREANDO SUITE COMPLETA DE TESTING ==========");

        List<Reservation> testReservations = new ArrayList<>();

        // ✅ ESCENARIO 1: PRÓXIMAS - Diferentes precios para testing de taxi
        testReservations.add(createScenario_UpcomingLowPrice());
        testReservations.add(createScenario_UpcomingHighPrice());
        testReservations.add(createScenario_UpcomingMidPrice());

        // ✅ ESCENARIO 2: ACTUALES - Diferentes sub-estados
        testReservations.add(createScenario_ActiveJustCheckedIn());
        testReservations.add(createScenario_ActiveStaying());
        testReservations.add(createScenario_ActiveCheckoutPending());

        // ✅ ESCENARIO 3: COMPLETADAS - Diferentes estados de taxi y review
        testReservations.add(createScenario_CompletedWithTaxiAvailable());
        testReservations.add(createScenario_CompletedWithTaxiUsed());
        testReservations.add(createScenario_CompletedNoTaxiLowAmount());
        testReservations.add(createScenario_CompletedWithReviewSubmitted());

        logTestingSuiteCreated(testReservations);
        return testReservations;
    }

    // ========== ESCENARIOS ESPECÍFICOS ==========

    private Reservation createScenario_UpcomingLowPrice() {
        Reservation reservation = debugManager.createTestReservation("Hotel Básico Test", 400.0);
        debugManager.forceToUpcoming(reservation);

        if (verboseLogging) {
            Log.d(TAG, "📅 ESCENARIO: Próximo con precio bajo (No califica taxi)");
            Log.d(TAG, "   - Precio: S/ 400 (< mínimo para taxi)");
            Log.d(TAG, "   - Puede modificar: " + reservation.canModify());
        }

        return reservation;
    }

    private Reservation createScenario_UpcomingHighPrice() {
        Reservation reservation = debugManager.createTestReservation("Hotel Premium Test", 1200.0);
        debugManager.forceToUpcoming(reservation);

        // Agregar servicios para asegurar que califique para taxi
        reservation.addService("Spa Premium", 150.0, 1);
        reservation.addService("Room Service Gourmet", 100.0, 1);

        if (verboseLogging) {
            Log.d(TAG, "📅 ESCENARIO: Próximo con precio alto (Califica taxi)");
            Log.d(TAG, "   - Precio base: S/ 1200");
            Log.d(TAG, "   - Con servicios: S/ " + reservation.getFinalTotal());
            Log.d(TAG, "   - Califica taxi: " + (reservation.getFinalTotal() >= debugManager.getTestTaxiMinAmount()));
        }

        return reservation;
    }

    private Reservation createScenario_UpcomingMidPrice() {
        Reservation reservation = debugManager.createTestReservation("Hotel Medio Test", 750.0);
        debugManager.forceToUpcoming(reservation);

        // Agregar algunos servicios
        reservation.addService("Desayuno Continental", 45.0, 2);

        if (verboseLogging) {
            Log.d(TAG, "📅 ESCENARIO: Próximo precio medio (Borderline taxi)");
            Log.d(TAG, "   - Total: S/ " + reservation.getFinalTotal());
        }

        return reservation;
    }

    private Reservation createScenario_ActiveJustCheckedIn() {
        Reservation reservation = debugManager.createTestReservation("Hotel Check-in Test", 900.0);
        debugManager.forceToActive(reservation);

        if (verboseLogging) {
            Log.d(TAG, "🏨 ESCENARIO: Activo - Recién llegado");
            Log.d(TAG, "   - Sub-estado: " + reservation.getSubStatus());
            Log.d(TAG, "   - Puede modificar: " + reservation.canModify());
        }

        return reservation;
    }

    private Reservation createScenario_ActiveStaying() {
        Reservation reservation = debugManager.createTestReservation("Hotel Estadía Test", 1100.0);
        debugManager.forceToStaying(reservation);

        if (verboseLogging) {
            Log.d(TAG, "🛏️ ESCENARIO: Activo - Estadía en curso");
            Log.d(TAG, "   - Sub-estado: " + reservation.getSubStatus());
            Log.d(TAG, "   - Servicios: " + reservation.getServices().size());
            Log.d(TAG, "   - Puede modificar: " + reservation.canModify());
        }

        return reservation;
    }

    private Reservation createScenario_ActiveCheckoutPending() {
        Reservation reservation = debugManager.createTestReservation("Hotel Checkout Test", 1300.0);
        debugManager.forceToCheckoutPending(reservation);

        if (verboseLogging) {
            Log.d(TAG, "⏳ ESCENARIO: Activo - Checkout pendiente");
            Log.d(TAG, "   - Sub-estado: " + reservation.getSubStatus());
            Log.d(TAG, "   - Esperando aprobación del hotel");
        }

        return reservation;
    }

    private Reservation createScenario_CompletedWithTaxiAvailable() {
        Reservation reservation = debugManager.createTestReservation("Hotel Taxi Disponible", 1400.0);
        debugManager.forceToCompletedWithTaxi(reservation);

        if (verboseLogging) {
            Log.d(TAG, "✅ ESCENARIO: Completado - Taxi disponible");
            Log.d(TAG, "   - Total: S/ " + reservation.getFinalTotal());
            Log.d(TAG, "   - Califica taxi: " + reservation.isEligibleForFreeTaxi());
            Log.d(TAG, "   - Review enviado: " + reservation.isReviewSubmitted());
        }

        return reservation;
    }

    private Reservation createScenario_CompletedWithTaxiUsed() {
        Reservation reservation = debugManager.createTestReservation("Hotel Taxi Usado", 1600.0);
        debugManager.forceToCompletedWithTaxi(reservation);
        reservation.setHasTaxiService(true); // Simular que ya usó el taxi

        if (verboseLogging) {
            Log.d(TAG, "🚖 ESCENARIO: Completado - Taxi ya usado");
            Log.d(TAG, "   - Taxi service: " + reservation.hasTaxiService());
        }

        return reservation;
    }

    private Reservation createScenario_CompletedNoTaxiLowAmount() {
        Reservation reservation = debugManager.createTestReservation("Hotel Sin Taxi", 600.0);
        debugManager.forceToCompletedNoTaxi(reservation);

        if (verboseLogging) {
            Log.d(TAG, "❌ ESCENARIO: Completado - No califica taxi");
            Log.d(TAG, "   - Total: S/ " + reservation.getFinalTotal());
            Log.d(TAG, "   - No califica para taxi por monto insuficiente");
        }

        return reservation;
    }

    private Reservation createScenario_CompletedWithReviewSubmitted() {
        Reservation reservation = debugManager.createTestReservation("Hotel Review Test", 1100.0);
        debugManager.forceToCompletedWithReview(reservation);

        if (verboseLogging) {
            Log.d(TAG, "⭐ ESCENARIO: Completado - Review enviado");
            Log.d(TAG, "   - Review enviado: " + reservation.isReviewSubmitted());
            Log.d(TAG, "   - Botón debe mostrar 'Ver factura'");
        }

        return reservation;
    }

    // ========== MÉTODOS DE VERIFICACIÓN ==========

    /**
     * ✅ VERIFICAR QUE TODOS LOS ESTADOS SEAN CORRECTOS
     */
    public boolean verifyAllScenarios(List<Reservation> reservations) {
        Log.d(TAG, "🔍 ========== VERIFICANDO TODOS LOS ESCENARIOS ==========");

        boolean allValid = true;
        int scenarioNumber = 1;

        for (Reservation reservation : reservations) {
            boolean isValid = verifyReservationState(reservation, scenarioNumber);
            if (!isValid) {
                allValid = false;
                Log.e(TAG, "❌ Escenario " + scenarioNumber + " FALLÓ la verificación");
            } else {
                Log.d(TAG, "✅ Escenario " + scenarioNumber + " verificado correctamente");
            }
            scenarioNumber++;
        }

        Log.d(TAG, "🏁 RESULTADO FINAL: " + (allValid ? "TODOS LOS ESCENARIOS VÁLIDOS" : "ALGUNOS ESCENARIOS FALLARON"));

        if (context != null) {
            String message = allValid ?
                    "✅ Todos los escenarios verificados correctamente" :
                    "⚠️ Algunos escenarios requieren revisión";
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        return allValid;
    }

    private boolean verifyReservationState(Reservation reservation, int scenarioNumber) {
        Log.d(TAG, "🔍 Verificando escenario " + scenarioNumber + ": " + reservation.getHotelName());

        // Verificar que el estado sea coherente
        boolean stateValid = reservation.getStatus() >= Reservation.STATUS_UPCOMING &&
                reservation.getStatus() <= Reservation.STATUS_COMPLETED;

        if (!stateValid) {
            Log.e(TAG, "   ❌ Estado inválido: " + reservation.getStatus());
            return false;
        }

        // Verificar coherencia de taxi según monto
        double total = reservation.getFinalTotal();
        double minTaxi = debugManager.getTestTaxiMinAmount();
        boolean shouldQualifyForTaxi = total >= minTaxi;

        if (reservation.getStatus() == Reservation.STATUS_COMPLETED) {
            if (shouldQualifyForTaxi && !reservation.isEligibleForFreeTaxi()) {
                Log.w(TAG, "   ⚠️ Debería calificar para taxi pero no califica (Total: S/ " + total + " >= S/ " + minTaxi + ")");
            }
        }

        // Verificar permisos de modificación según estado
        switch (reservation.getStatus()) {
            case Reservation.STATUS_UPCOMING:
                if (!reservation.canModify()) {
                    Log.w(TAG, "   ⚠️ Reserva próxima debería permitir modificación");
                }
                break;
            case Reservation.STATUS_COMPLETED:
                if (reservation.canModify()) {
                    Log.w(TAG, "   ⚠️ Reserva completada NO debería permitir modificación");
                }
                break;
        }

        Log.d(TAG, "   ✅ Estado verificado: " + debugManager.getDetailedStatus(reservation));
        return true;
    }

    // ========== MÉTODOS DE TESTING AUTOMATIZADO ==========

    /**
     * ✅ EJECUTAR TESTING AUTOMATIZADO DE FLUJO COMPLETO
     */
    public void runAutomatedFlowTest() {
        Log.d(TAG, "🤖 ========== TESTING AUTOMATIZADO DE FLUJO ==========");

        if (!debugManager.isDebugModeEnabled()) {
            Log.w(TAG, "⚠️ Debug mode deshabilitado, no se puede ejecutar testing automatizado");
            if (context != null) {
                Toast.makeText(context, "⚠️ Debug mode deshabilitado", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Crear reserva de testing
        Reservation testReservation = debugManager.createTestReservation("Hotel Flujo Automático", 900.0);

        // Ejecutar flujo automatizado
        debugManager.simulateFullReservationFlow(testReservation, new ReservationDebugManager.FlowCallback() {
            @Override
            public void onStateChanged(String stateName, Reservation reservation) {
                Log.d(TAG, "🔄 Flujo automático - Estado: " + stateName);
                if (context != null) {
                    Toast.makeText(context, "🤖 " + stateName, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFlowCompleted() {
                Log.d(TAG, "🏁 Flujo automático completado exitosamente");
                if (context != null) {
                    Toast.makeText(context, "✅ Flujo automático completado", Toast.LENGTH_LONG).show();
                }

                // Verificar estado final
                verifyReservationState(testReservation, 999); // Número especial para test automático
            }
        });
    }

    /**
     * ✅ TESTING DE DIFERENTES MONTOS DE TAXI
     */
    public void testTaxiAmounts() {
        Log.d(TAG, "🚕 ========== TESTING DE MONTOS DE TAXI ==========");

        for (double taxiAmount : testTaxiAmounts) {
            Log.d(TAG, "🧪 Probando monto mínimo taxi: S/ " + taxiAmount);

            debugManager.setTestTaxiMinAmount(taxiAmount);

            // Crear reserva con monto justo por debajo del mínimo
            double testAmount = taxiAmount - 50.0;
            Reservation reservationBelow = debugManager.createTestReservation("Test Below " + taxiAmount, testAmount);
            boolean shouldNotQualify = testAmount < taxiAmount;

            Log.d(TAG, "   📊 Monto S/ " + testAmount + " < S/ " + taxiAmount + " = " + shouldNotQualify);

            // Crear reserva con monto justo por encima del mínimo
            testAmount = taxiAmount + 50.0;
            Reservation reservationAbove = debugManager.createTestReservation("Test Above " + taxiAmount, testAmount);
            boolean shouldQualify = testAmount >= taxiAmount;

            Log.d(TAG, "   📊 Monto S/ " + testAmount + " >= S/ " + taxiAmount + " = " + shouldQualify);
        }

        // Restaurar monto original
        debugManager.setTestTaxiMinAmount(800.0); // Valor por defecto
        Log.d(TAG, "🔄 Monto de taxi restaurado a valor por defecto: S/ 800");
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    private void logTestingSuiteCreated(List<Reservation> reservations) {
        Log.d(TAG, "📋 ========== SUITE DE TESTING CREADA ==========");
        Log.d(TAG, "Total de escenarios: " + reservations.size());

        int upcoming = 0, active = 0, completed = 0;
        for (Reservation r : reservations) {
            switch (r.getStatus()) {
                case Reservation.STATUS_UPCOMING: upcoming++; break;
                case Reservation.STATUS_ACTIVE: active++; break;
                case Reservation.STATUS_COMPLETED: completed++; break;
            }
        }

        Log.d(TAG, "Distribución:");
        Log.d(TAG, "  - Próximas: " + upcoming);
        Log.d(TAG, "  - Actuales: " + active);
        Log.d(TAG, "  - Completadas: " + completed);
        Log.d(TAG, "==========================================");
    }

    public void setVerboseLogging(boolean verbose) {
        this.verboseLogging = verbose;
        Log.d(TAG, "🔊 Verbose logging: " + (verbose ? "ENABLED" : "DISABLED"));
    }

    /**
     * ✅ CREAR REPORTE COMPLETO DE TESTING
     */
    public String generateTestingReport(List<Reservation> reservations) {
        StringBuilder report = new StringBuilder();
        report.append("🧪 REPORTE DE TESTING DE RESERVAS\n");
        report.append("=====================================\n\n");

        report.append("📊 RESUMEN:\n");
        report.append("- Total de escenarios: ").append(reservations.size()).append("\n");
        report.append("- Modo debug: ").append(debugManager.isDebugModeEnabled() ? "HABILITADO" : "DESHABILITADO").append("\n");
        report.append("- Monto mínimo taxi: S/ ").append(debugManager.getTestTaxiMinAmount()).append("\n\n");

        report.append("📋 DETALLE DE ESCENARIOS:\n");
        for (int i = 0; i < reservations.size(); i++) {
            Reservation r = reservations.get(i);
            report.append(i + 1).append(". ").append(r.getHotelName()).append("\n");
            report.append("   Estado: ").append(debugManager.getDetailedStatus(r)).append("\n");
            report.append("   Total: S/ ").append(r.getFinalTotal()).append("\n");
            report.append("   Taxi: ").append(r.isEligibleForFreeTaxi() ? "✅ Califica" : "❌ No califica").append("\n\n");
        }

        return report.toString();
    }
}