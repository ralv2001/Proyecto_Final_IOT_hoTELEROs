// PaymentMethodManager.java - Gestor de tarjetas de pago con Firebase
package com.example.proyecto_final_hoteleros.client.utils;

import android.content.Context;
import android.util.Log;
import com.example.proyecto_final_hoteleros.client.data.model.PaymentMethod;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PaymentMethodManager {

    private static final String TAG = "PaymentMethodManager";
    private static final String PAYMENT_METHODS_COLLECTION = "payment_methods";

    private static PaymentMethodManager instance;
    private FirebaseFirestore firestore;

    private PaymentMethodManager(Context context) {
        firestore = FirebaseFirestore.getInstance();
    }

    public static PaymentMethodManager getInstance(Context context) {
        if (instance == null) {
            instance = new PaymentMethodManager(context);
        }
        return instance;
    }

    // Interfaces para callbacks
    public interface PaymentMethodCallback {
        void onSuccess(PaymentMethod paymentMethod);
        void onError(String error);
    }

    public interface PaymentMethodsListCallback {
        void onSuccess(List<PaymentMethod> paymentMethods);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }

    /**
     * Guardar nueva tarjeta de pago
     */
    public void savePaymentMethod(PaymentMethod paymentMethod, PaymentMethodCallback callback) {
        if (paymentMethod == null || paymentMethod.getUserId() == null) {
            callback.onError("Datos de tarjeta inválidos");
            return;
        }

        Log.d(TAG, "💳 Guardando nueva tarjeta para usuario: " + paymentMethod.getUserId());

        // Si es la primera tarjeta, marcarla como predeterminada
        getUserPaymentMethods(paymentMethod.getUserId(), new PaymentMethodsListCallback() {
            @Override
            public void onSuccess(List<PaymentMethod> existingCards) {
                if (existingCards.isEmpty()) {
                    paymentMethod.setDefault(true);
                    Log.d(TAG, "✅ Primera tarjeta - marcada como predeterminada");
                }

                // Guardar en Firestore
                firestore.collection(PAYMENT_METHODS_COLLECTION)
                        .document(paymentMethod.getId())
                        .set(paymentMethod.toMap())
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "✅ Tarjeta guardada exitosamente: " + paymentMethod.getId());
                            callback.onSuccess(paymentMethod);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "❌ Error guardando tarjeta: " + e.getMessage());
                            callback.onError("Error guardando tarjeta: " + e.getMessage());
                        });
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "⚠️ Error verificando tarjetas existentes, guardando de todas formas");
                // Guardar de todas formas
                firestore.collection(PAYMENT_METHODS_COLLECTION)
                        .document(paymentMethod.getId())
                        .set(paymentMethod.toMap())
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "✅ Tarjeta guardada exitosamente (fallback): " + paymentMethod.getId());
                            callback.onSuccess(paymentMethod);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "❌ Error guardando tarjeta (fallback): " + e.getMessage());
                            callback.onError("Error guardando tarjeta: " + e.getMessage());
                        });
            }
        });
    }

    /**
     * Obtener todas las tarjetas de un usuario
     */
    public void getUserPaymentMethods(String userId, PaymentMethodsListCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onError("ID de usuario inválido");
            return;
        }

        Log.d(TAG, "🔍 Obteniendo tarjetas del usuario: " + userId);

        firestore.collection(PAYMENT_METHODS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .orderBy("isDefault", Query.Direction.DESCENDING)
                .orderBy("lastUsed", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<PaymentMethod> paymentMethods = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Map<String, Object> data = document.getData();
                            PaymentMethod paymentMethod = PaymentMethod.fromMap(data);
                            paymentMethods.add(paymentMethod);
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error parseando tarjeta: " + e.getMessage());
                        }
                    }

                    Log.d(TAG, "✅ Tarjetas obtenidas: " + paymentMethods.size());
                    callback.onSuccess(paymentMethods);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error obteniendo tarjetas: " + e.getMessage());
                    callback.onError("Error obteniendo tarjetas: " + e.getMessage());
                });
    }

    /**
     * Obtener la tarjeta predeterminada de un usuario
     */
    public void getDefaultPaymentMethod(String userId, PaymentMethodCallback callback) {
        Log.d(TAG, "🔍 Obteniendo tarjeta predeterminada del usuario: " + userId);

        firestore.collection(PAYMENT_METHODS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isDefault", true)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        PaymentMethod paymentMethod = PaymentMethod.fromMap(document.getData());
                        Log.d(TAG, "✅ Tarjeta predeterminada encontrada: " + paymentMethod.getId());
                        callback.onSuccess(paymentMethod);
                    } else {
                        Log.d(TAG, "ℹ️ No se encontró tarjeta predeterminada");
                        callback.onError("No se encontró tarjeta predeterminada");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error obteniendo tarjeta predeterminada: " + e.getMessage());
                    callback.onError("Error obteniendo tarjeta predeterminada: " + e.getMessage());
                });
    }

    /**
     * Marcar una tarjeta como predeterminada
     */
    public void setDefaultPaymentMethod(String userId, String cardId, SimpleCallback callback) {
        Log.d(TAG, "🔄 Estableciendo tarjeta predeterminada: " + cardId);

        // Primero, quitar el estado de predeterminada de todas las tarjetas del usuario
        firestore.collection(PAYMENT_METHODS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isDefault", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Usar batch para actualizar múltiples documentos
                    firestore.runBatch(batch -> {
                                // Quitar isDefault de las tarjetas existentes
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    batch.update(document.getReference(), "isDefault", false);
                                }

                                // Establecer la nueva tarjeta como predeterminada
                                batch.update(firestore.collection(PAYMENT_METHODS_COLLECTION).document(cardId),
                                        "isDefault", true);
                            })
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "✅ Tarjeta predeterminada actualizada exitosamente");
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "❌ Error actualizando tarjeta predeterminada: " + e.getMessage());
                                callback.onError("Error actualizando tarjeta predeterminada");
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error obteniendo tarjetas existentes: " + e.getMessage());
                    callback.onError("Error procesando tarjetas");
                });
    }

    /**
     * Marcar tarjeta como usada recientemente
     */
    public void markCardAsUsed(String cardId, SimpleCallback callback) {
        Log.d(TAG, "🔄 Marcando tarjeta como usada: " + cardId);

        firestore.collection(PAYMENT_METHODS_COLLECTION)
                .document(cardId)
                .update("lastUsed", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Tarjeta marcada como usada");
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error marcando tarjeta como usada: " + e.getMessage());
                    if (callback != null) callback.onError("Error actualizando tarjeta");
                });
    }

    /**
     * Eliminar (desactivar) una tarjeta
     */
    public void deletePaymentMethod(String cardId, SimpleCallback callback) {
        Log.d(TAG, "🗑️ Eliminando tarjeta: " + cardId);

        // No eliminar físicamente, solo desactivar
        firestore.collection(PAYMENT_METHODS_COLLECTION)
                .document(cardId)
                .update("isActive", false)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Tarjeta desactivada exitosamente");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error desactivando tarjeta: " + e.getMessage());
                    callback.onError("Error eliminando tarjeta: " + e.getMessage());
                });
    }

    /**
     * Validar si un usuario puede agregar más tarjetas
     */
    public void canAddMoreCards(String userId, CardLimitCallback callback) {
        final int MAX_CARDS_PER_USER = 5; // Límite de tarjetas por usuario

        getUserPaymentMethods(userId, new PaymentMethodsListCallback() {
            @Override
            public void onSuccess(List<PaymentMethod> paymentMethods) {
                boolean canAdd = paymentMethods.size() < MAX_CARDS_PER_USER;
                callback.onResult(canAdd, paymentMethods.size(), MAX_CARDS_PER_USER);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public interface CardLimitCallback {
        void onResult(boolean canAdd, int currentCount, int maxAllowed);
        void onError(String error);
    }

    /**
     * Determinar el tipo de tarjeta basado en el número
     */
    public static String determineCardType(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "UNKNOWN";
        }

        String cleanNumber = cardNumber.replaceAll("\\s", "");

        if (cleanNumber.startsWith("4")) {
            return "VISA";
        } else if (cleanNumber.startsWith("5") || cleanNumber.startsWith("2")) {
            return "MASTERCARD";
        } else if (cleanNumber.startsWith("3")) {
            return "AMEX";
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * Enmascarar número de tarjeta para mostrar solo los últimos 4 dígitos
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "**** **** **** ****";
        }

        String cleanNumber = cardNumber.replaceAll("\\s", "");
        if (cleanNumber.length() < 4) {
            return "**** **** **** ****";
        }

        String lastFour = cleanNumber.substring(cleanNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }
}