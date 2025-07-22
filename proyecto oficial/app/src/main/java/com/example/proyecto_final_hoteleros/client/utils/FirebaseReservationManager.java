package com.example.proyecto_final_hoteleros.client.utils;

import android.util.Log;
import com.example.proyecto_final_hoteleros.client.data.model.Reservation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FirebaseReservationManager {
    private static final String TAG = "FirebaseReservationManager";
    private static final String RESERVATIONS_COLLECTION = "client_reservations";

    private static FirebaseReservationManager instance;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM - dd MMM, yyyy", Locale.getDefault());
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public interface ReservationCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface ReservationsListCallback {
        void onSuccess(List<Reservation> upcomingList, List<Reservation> activeList, List<Reservation> completedList);
        void onError(String error);
    }

    public interface TaxiConfirmationCallback {
        void onConfirmed();
        void onDeclined();
        void onError(String error);
    }

    private FirebaseReservationManager() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseReservationManager getInstance() {
        if (instance == null) {
            instance = new FirebaseReservationManager();
        }
        return instance;
    }

    /**
     * ✅ GUARDAR NUEVA RESERVA EN FIREBASE
     */
    public void saveReservation(Reservation reservation, String userId, ReservationCallback callback) {
        Log.d(TAG, "💾 Guardando nueva reserva para usuario: " + userId);

        try {
            Map<String, Object> reservationData = convertReservationToMap(reservation, userId);

            firestore.collection(RESERVATIONS_COLLECTION)
                    .add(reservationData)
                    .addOnSuccessListener(documentReference -> {
                        String reservationId = documentReference.getId();
                        reservation.setId(reservationId);

                        Log.d(TAG, "✅ Reserva guardada exitosamente: " + reservationId);
                        Log.d(TAG, "📅 Hotel: " + reservation.getHotelName());
                        Log.d(TAG, "📅 Fechas: " + reservation.getDate());
                        Log.d(TAG, "💰 Total: S/ " + reservation.getFinalTotal());
                        Log.d(TAG, "🚖 Taxi incluido: " + reservation.isEligibleForFreeTaxi());

                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Error guardando reserva: " + e.getMessage());
                        callback.onError(e.getMessage());
                    });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error preparando datos de reserva: " + e.getMessage());
            callback.onError("Error preparando datos: " + e.getMessage());
        }
    }

    /**
     * ✅ CARGAR RESERVAS DEL USUARIO DESDE FIREBASE
     */
    public void loadUserReservations(String userId, ReservationsListCallback callback) {
        Log.d(TAG, "🔍 Cargando reservas del usuario: " + userId);

        firestore.collection(RESERVATIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Reservation> allReservations = new ArrayList<>();

                        for (DocumentSnapshot document : task.getResult()) {
                            try {
                                Reservation reservation = convertMapToReservation(document.getData(), document.getId());
                                if (reservation != null) {
                                    allReservations.add(reservation);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "❌ Error procesando reserva: " + e.getMessage());
                            }
                        }

                        Log.d(TAG, "📋 Total de reservas cargadas: " + allReservations.size());

                        // ✅ CLASIFICAR RESERVAS POR ESTADO BASADO EN FECHAS REALES
                        classifyReservationsByDate(allReservations, callback);

                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        Log.e(TAG, "❌ Error cargando reservas: " + error);
                        callback.onError(error);
                    }
                });
    }

    /**
     * ✅ CLASIFICAR RESERVAS POR FECHA ACTUAL
     */
    private void classifyReservationsByDate(List<Reservation> allReservations, ReservationsListCallback callback) {
        List<Reservation> upcomingList = new ArrayList<>();
        List<Reservation> activeList = new ArrayList<>();
        List<Reservation> completedList = new ArrayList<>();

        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date todayStart = cal.getTime();

        Log.d(TAG, "📅 Clasificando reservas basado en fecha actual: " + simpleDateFormat.format(todayStart));

        for (Reservation reservation : allReservations) {
            try {
                Date checkInDate = parseCheckInDate(reservation.getDate());
                Date checkOutDate = parseCheckOutDate(reservation.getDate());

                if (checkInDate != null && checkOutDate != null) {
                    if (todayStart.before(checkInDate)) {
                        // ✅ PRÓXIMAS: antes del check-in
                        reservation.setStatus(Reservation.STATUS_UPCOMING);
                        upcomingList.add(reservation);
                        Log.d(TAG, "📅 PRÓXIMA: " + reservation.getHotelName() + " - Check-in: " + simpleDateFormat.format(checkInDate));

                    } else if (!todayStart.before(checkInDate) && !todayStart.after(checkOutDate)) {
                        // ✅ ACTUALES: entre check-in y check-out
                        reservation.setStatus(Reservation.STATUS_ACTIVE);
                        if (reservation.getSubStatus() == 0) {
                            reservation.setSubStatus(Reservation.SUBSTATUS_STAYING);
                        }
                        activeList.add(reservation);
                        Log.d(TAG, "🏨 ACTUAL: " + reservation.getHotelName() + " - Estadía en curso");

                    } else {
                        // ✅ COMPLETADAS: después del check-out
                        reservation.setStatus(Reservation.STATUS_COMPLETED);
                        completedList.add(reservation);
                        Log.d(TAG, "✅ COMPLETADA: " + reservation.getHotelName() + " - Check-out: " + simpleDateFormat.format(checkOutDate));
                    }
                } else {
                    Log.w(TAG, "⚠️ No se pudo parsear fechas para: " + reservation.getHotelName());
                    // Por defecto, considerar como próxima si no se puede determinar
                    reservation.setStatus(Reservation.STATUS_UPCOMING);
                    upcomingList.add(reservation);
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Error clasificando reserva " + reservation.getHotelName() + ": " + e.getMessage());
                // Por defecto, considerar como próxima
                reservation.setStatus(Reservation.STATUS_UPCOMING);
                upcomingList.add(reservation);
            }
        }

        Log.d(TAG, "📊 Clasificación final:");
        Log.d(TAG, "   - Próximas: " + upcomingList.size());
        Log.d(TAG, "   - Actuales: " + activeList.size());
        Log.d(TAG, "   - Completadas: " + completedList.size());

        callback.onSuccess(upcomingList, activeList, completedList);
    }

    /**
     * ✅ PARSEAR FECHA DE CHECK-IN DESDE STRING
     */
    private Date parseCheckInDate(String dateString) {
        try {
            // Formato esperado: "10 May - 15 May, 2025"
            if (dateString.contains(" - ")) {
                String checkInPart = dateString.split(" - ")[0].trim();
                String year = dateString.substring(dateString.lastIndexOf(" ") + 1);
                String fullCheckInDate = checkInPart + ", " + year;

                SimpleDateFormat parser = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
                return parser.parse(fullCheckInDate);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parseando fecha de check-in: " + dateString + " - " + e.getMessage());
        }
        return null;
    }

    /**
     * ✅ PARSEAR FECHA DE CHECK-OUT DESDE STRING
     */
    private Date parseCheckOutDate(String dateString) {
        try {
            // Formato esperado: "10 May - 15 May, 2025"
            if (dateString.contains(" - ")) {
                String checkOutPart = dateString.split(" - ")[1].trim();

                SimpleDateFormat parser = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
                return parser.parse(checkOutPart);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parseando fecha de check-out: " + dateString + " - " + e.getMessage());
        }
        return null;
    }

    /**
     * ✅ CONVERTIR RESERVA A MAPA PARA FIREBASE
     */
    private Map<String, Object> convertReservationToMap(Reservation reservation, String userId) {
        Map<String, Object> data = new HashMap<>();

        // Datos básicos
        data.put("userId", userId);
        data.put("hotelName", reservation.getHotelName());
        data.put("location", reservation.getLocation());
        data.put("dateRange", reservation.getDate());
        data.put("basePrice", reservation.getBasePrice());
        data.put("rating", reservation.getRating());
        data.put("status", reservation.getStatus());
        data.put("subStatus", reservation.getSubStatus());
        data.put("roomType", reservation.getRoomType() != null ? reservation.getRoomType() : "Suite Estándar");
        data.put("roomNumber", reservation.getRoomNumber() != null ? reservation.getRoomNumber() : "");

        // Servicios y precios
        data.put("servicesTotal", reservation.getServicesTotal());
        data.put("additionalCharges", reservation.getAdditionalCharges());
        data.put("finalTotal", reservation.getFinalTotal());
        data.put("hasTaxiService", reservation.isEligibleForFreeTaxi());

        // Información adicional
        data.put("canModify", reservation.canModify());
        data.put("reviewSubmitted", reservation.isReviewSubmitted());
        data.put("specialRequests", reservation.getSpecialRequests() != null ? reservation.getSpecialRequests() : "");

        // Datos de pago si existen
        if (reservation.getGuaranteeCard() != null) {
            Map<String, Object> cardData = new HashMap<>();
            cardData.put("cardNumber", reservation.getGuaranteeCard().getCardNumber());
            cardData.put("cardType", reservation.getGuaranteeCard().getCardType());
            cardData.put("holderName", reservation.getGuaranteeCard().getHolderName());
            data.put("guaranteeCard", cardData);
        }

        // ✅ DATOS DE TAXI ESPECÍFICOS
        data.put("taxiStatus", reservation.isEligibleForFreeTaxi() ? "available" : "not_included");
        data.put("taxiConfirmed", false); // Por defecto no confirmado

        // Timestamps
        data.put("createdAt", System.currentTimeMillis());
        data.put("updatedAt", System.currentTimeMillis());

        return data;
    }

    /**
     * ✅ CONVERTIR MAPA DE FIREBASE A RESERVA
     */
    private Reservation convertMapToReservation(Map<String, Object> data, String documentId) {
        if (data == null) return null;

        try {
            String hotelName = (String) data.get("hotelName");
            String location = (String) data.get("location");
            String dateRange = (String) data.get("dateRange");
            Number basePrice = (Number) data.get("basePrice");
            Number rating = (Number) data.get("rating");

            Reservation reservation = new Reservation(
                    hotelName != null ? hotelName : "Hotel",
                    location != null ? location : "Ubicación",
                    dateRange != null ? dateRange : "Fechas no especificadas",
                    basePrice != null ? basePrice.doubleValue() : 0.0,
                    rating != null ? rating.floatValue() : 4.5f,
                    com.example.proyecto_final_hoteleros.R.drawable.belmond,
                    Reservation.STATUS_UPCOMING
            );

            // Configurar datos adicionales
            reservation.setId(documentId);

            Number status = (Number) data.get("status");
            if (status != null) {
                reservation.setStatus(status.intValue());
            }

            Number subStatus = (Number) data.get("subStatus");
            if (subStatus != null) {
                reservation.setSubStatus(subStatus.intValue());
            }

            reservation.setRoomType((String) data.get("roomType"));
            reservation.setRoomNumber((String) data.get("roomNumber"));
            reservation.setSpecialRequests((String) data.get("specialRequests"));

            Boolean reviewSubmitted = (Boolean) data.get("reviewSubmitted");
            if (reviewSubmitted != null) {
                reservation.setReviewSubmitted(reviewSubmitted);
            }

            // Los servicios se manejan automáticamente por la clase Reservation

            // ✅ CONFIGURAR INFORMACIÓN DE TAXI
            Boolean hasTaxiService = (Boolean) data.get("hasTaxiService");
            if (hasTaxiService != null && hasTaxiService) {
                reservation.setHasTaxiService(true);
            }

            // Restaurar tarjeta de garantía si existe
            @SuppressWarnings("unchecked")
            Map<String, Object> cardData = (Map<String, Object>) data.get("guaranteeCard");
            if (cardData != null) {
                String cardNumber = (String) cardData.get("cardNumber");
                String cardType = (String) cardData.get("cardType");
                String holderName = (String) cardData.get("holderName");

                Reservation.PaymentMethod card = new Reservation.PaymentMethod(cardNumber, cardType, holderName);
                reservation.setGuaranteeCard(card);
            }

            Log.d(TAG, "✅ Reserva convertida: " + hotelName + " | Estado: " + reservation.getStatusText());
            return reservation;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error convirtiendo datos de reserva: " + e.getMessage());
            return null;
        }
    }

    /**
     * ✅ ACTUALIZAR SUB-ESTADO DE RESERVA (checkout, etc.)
     */
    public void updateReservationSubStatus(String reservationId, int newSubStatus, ReservationCallback callback) {
        Log.d(TAG, "🔄 Actualizando sub-estado de reserva " + reservationId + " a: " + newSubStatus);

        Map<String, Object> updates = new HashMap<>();
        updates.put("subStatus", newSubStatus);
        updates.put("updatedAt", System.currentTimeMillis());

        firestore.collection(RESERVATIONS_COLLECTION)
                .document(reservationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Sub-estado actualizado exitosamente");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error actualizando sub-estado: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    /**
     * ✅ CONFIRMAR SERVICIO DE TAXI PARA RESERVA COMPLETADA
     */
    public void confirmTaxiService(String reservationId, boolean confirmed, TaxiConfirmationCallback callback) {
        Log.d(TAG, "🚖 Confirmando taxi para reserva " + reservationId + ": " + confirmed);

        Map<String, Object> updates = new HashMap<>();
        updates.put("taxiConfirmed", confirmed);
        updates.put("taxiConfirmedAt", System.currentTimeMillis());
        updates.put("updatedAt", System.currentTimeMillis());

        firestore.collection(RESERVATIONS_COLLECTION)
                .document(reservationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Confirmación de taxi guardada");
                    if (confirmed) {
                        callback.onConfirmed();
                    } else {
                        callback.onDeclined();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error guardando confirmación de taxi: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    /**
     * ✅ VERIFICAR SI RESERVA YA TIENE CONFIRMACIÓN DE TAXI
     */
    public void checkTaxiConfirmationStatus(String reservationId, TaxiStatusCallback callback) {
        firestore.collection(RESERVATIONS_COLLECTION)
                .document(reservationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean taxiConfirmed = documentSnapshot.getBoolean("taxiConfirmed");
                        Long taxiConfirmedAt = documentSnapshot.getLong("taxiConfirmedAt");

                        callback.onStatusChecked(
                                taxiConfirmed != null ? taxiConfirmed : false,
                                taxiConfirmedAt != null ? taxiConfirmedAt : 0L
                        );
                    } else {
                        callback.onError("Reserva no encontrada");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error verificando estado de taxi: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    public interface TaxiStatusCallback {
        void onStatusChecked(boolean isConfirmed, long confirmedAt);
        void onError(String error);
    }
}