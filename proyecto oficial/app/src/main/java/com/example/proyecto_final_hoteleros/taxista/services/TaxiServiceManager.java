package com.example.proyecto_final_hoteleros.taxista.services;

import android.util.Log;
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;
import com.example.proyecto_final_hoteleros.taxista.model.CheckoutReservation;

import java.util.List;

public class TaxiServiceManager {
    private static final String TAG = "TaxiServiceManager";
    private static TaxiServiceManager instance;
    private FirebaseManager firebaseManager;

    public static synchronized TaxiServiceManager getInstance() {
        if (instance == null) {
            instance = new TaxiServiceManager();
        }
        return instance;
    }

    private TaxiServiceManager() {
        firebaseManager = FirebaseManager.getInstance();
    }

    // Interfaces
    public interface TaxiServiceCallback {
        void onSuccess();
        void onError(String error);
        default void onStatusUpdate(String status) {}
    }

    public interface ServiceValidationCallback {
        void onValidationComplete(boolean isValid, String message);
    }

    /**
     * Iniciar servicio de taxi
     */
    public void startTaxiService(String reservationId, String driverId, TaxiServiceCallback callback) {
        Log.d(TAG, "🚕 Iniciando servicio de taxi para reserva: " + reservationId);

        firebaseManager.updateTaxiServiceStatus(reservationId, "in_progress",
                new FirebaseManager.TaxiServiceCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Servicio iniciado exitosamente");
                        callback.onSuccess();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "❌ Error iniciando servicio: " + error);
                        callback.onError(error);
                    }

                    @Override
                    public void onStatusUpdate(String status) {
                        Log.d(TAG, "📊 Estado actualizado a: " + status);
                        callback.onStatusUpdate(status);
                    }
                });
    }

    /**
     * Completar servicio de taxi
     */
    public void completeTaxiService(String reservationId, String driverId,
                                    double distance, int duration, String notes,
                                    TaxiServiceCallback callback) {
        Log.d(TAG, "✅ Completando servicio de taxi...");

        // Primero actualizar el estado de la reserva
        firebaseManager.updateTaxiServiceStatus(reservationId, "completed",
                new FirebaseManager.TaxiServiceCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Estado actualizado a completado");

                        // Crear registro del servicio
                        firebaseManager.createTaxiServiceRecord(reservationId, driverId,
                                distance, duration, notes, new FirebaseManager.DataCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "✅ Registro de servicio creado");
                                        callback.onSuccess();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        Log.e(TAG, "❌ Error creando registro: " + error);
                                        callback.onError("Error guardando registro: " + error);
                                    }
                                });
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "❌ Error completando servicio: " + error);
                        callback.onError(error);
                    }

                    @Override
                    public void onStatusUpdate(String status) {
                        callback.onStatusUpdate(status);
                    }
                });
    }

    /**
     * Cancelar servicio de taxi
     */
    public void cancelTaxiService(String reservationId, String reason, TaxiServiceCallback callback) {
        Log.d(TAG, "❌ Cancelando servicio de taxi. Motivo: " + reason);

        firebaseManager.updateTaxiServiceStatus(reservationId, "cancelled",
                new FirebaseManager.TaxiServiceCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Servicio cancelado exitosamente");
                        callback.onSuccess();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "❌ Error cancelando servicio: " + error);
                        callback.onError(error);
                    }

                    @Override
                    public void onStatusUpdate(String status) {
                        callback.onStatusUpdate(status);
                    }
                });
    }

    /**
     * Validar si un taxista puede tomar un servicio
     */
    public void validateDriverCanTakeService(String driverId, ServiceValidationCallback callback) {
        Log.d(TAG, "🔍 Validando disponibilidad del taxista: " + driverId);

        // Verificar si el taxista ya tiene servicios activos
        firebaseManager.getDriverAssignedReservations(driverId,
                new FirebaseManager.CheckoutCallback() {
                    @Override
                    public void onSuccess(List<CheckoutReservation> reservations) {
                        boolean hasActiveService = false;

                        for (CheckoutReservation reservation : reservations) {
                            if ("assigned".equals(reservation.getTaxiStatus()) ||
                                    "in_progress".equals(reservation.getTaxiStatus())) {
                                hasActiveService = true;
                                break;
                            }
                        }

                        if (hasActiveService) {
                            Log.w(TAG, "⚠️ Taxista ya tiene un servicio activo");
                            callback.onValidationComplete(false,
                                    "Ya tienes un servicio activo. Complétalo antes de tomar otro.");
                        } else {
                            Log.d(TAG, "✅ Taxista disponible para tomar servicio");
                            callback.onValidationComplete(true, "Disponible");
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "❌ Error validando disponibilidad: " + error);
                        callback.onValidationComplete(false, "Error verificando disponibilidad");
                    }
                });
    }

    /**
     * Obtener servicios activos de un taxista
     */
    public void getActiveServices(String driverId, FirebaseManager.CheckoutCallback callback) {
        Log.d(TAG, "📋 Obteniendo servicios activos del taxista: " + driverId);
        firebaseManager.getDriverAssignedReservations(driverId, callback);
    }

    /**
     * Calcular estimaciones de tiempo y distancia
     */
    public ServiceEstimation calculateEstimations(String origin, String destination) {
        Log.d(TAG, "📊 Calculando estimaciones para ruta");

        // Implementación básica - en producción usar Google Maps API
        double estimatedDistance = 15.5; // km
        int estimatedTime = 25; // minutos

        // Ajustes básicos según destino
        if (destination.toLowerCase().contains("aeropuerto")) {
            estimatedDistance = 18.0;
            estimatedTime = 30;
        }

        return new ServiceEstimation(estimatedDistance, estimatedTime);
    }

    /**
     * Clase para estimaciones de servicio
     */
    public static class ServiceEstimation {
        public final double distance;
        public final int timeMinutes;

        public ServiceEstimation(double distance, int timeMinutes) {
            this.distance = distance;
            this.timeMinutes = timeMinutes;
        }

        public String getFormattedDistance() {
            return String.format("%.1f km", distance);
        }

        public String getFormattedTime() {
            return timeMinutes + " min";
        }
    }

    /**
     * Estados de servicio
     */
    public static class ServiceStatus {
        public static final String PENDING = "pending";
        public static final String ASSIGNED = "assigned";
        public static final String IN_PROGRESS = "in_progress";
        public static final String COMPLETED = "completed";
        public static final String CANCELLED = "cancelled";

        public static String getDisplayName(String status) {
            switch (status) {
                case PENDING:
                    return "Pendiente";
                case ASSIGNED:
                    return "Asignado";
                case IN_PROGRESS:
                    return "En Progreso";
                case COMPLETED:
                    return "Completado";
                case CANCELLED:
                    return "Cancelado";
                default:
                    return "Desconocido";
            }
        }

        public static boolean isActive(String status) {
            return ASSIGNED.equals(status) || IN_PROGRESS.equals(status);
        }
    }
}