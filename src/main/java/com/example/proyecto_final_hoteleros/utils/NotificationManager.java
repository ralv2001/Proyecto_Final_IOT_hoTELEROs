package com.example.proyecto_final_hoteleros.utils;

import android.app.NotificationChannel;
import android.util.Log;

import com.example.proyecto_final_hoteleros.client.data.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase para gestionar las notificaciones de la aplicación
 */
public class NotificationManager {
    private static final String TAG = "NotificationManager";
    private static NotificationManager instance;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private NotificationManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Obtener la instancia única (Singleton)
     */
    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    /**
     * Enviar una nueva notificación al usuario actual
     */
    public void sendNotification(String title, String message, int type, String actionData) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (userId.isEmpty()) {
            Log.e(TAG, "No hay usuario autenticado para enviar notificación");
            return;
        }

        sendNotificationToUser(userId, title, message, type, actionData);
    }

    /**
     * Enviar una notificación a un usuario específico
     */
    public void sendNotificationToUser(String userId, String title, String message, int type, String actionData) {
        // Crear notificación
        Notification notification = new Notification(
                null,  // ID se generará en Firestore
                title,
                message,
                type,
                new Date(),  // Timestamp actual
                false,  // No leída por defecto
                actionData
        );

        // Convertir a mapa para Firestore
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", notification.getTitle());
        notificationData.put("message", notification.getMessage());
        notificationData.put("type", notification.getType());
        notificationData.put("timestamp", notification.getTimestamp());
        notificationData.put("read", notification.isRead());
        notificationData.put("actionData", notification.getActionData());

        // Guardar en Firestore
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .add(notificationData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Notificación enviada con ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al enviar notificación: " + e.getMessage());
                });
    }

    /**
     * Marcar una notificación específica como leída
     */
    public void markAsRead(String notificationId, NotificationCallback callback) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (userId.isEmpty() || notificationId == null || notificationId.isEmpty()) {
            if (callback != null) {
                callback.onFailure("Usuario o notificación no válidos");
            }
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .update("read", true)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Marcar todas las notificaciones del usuario como leídas
     */
    public void markAllAsRead(NotificationCallback callback) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (userId.isEmpty()) {
            if (callback != null) {
                callback.onFailure("Usuario no autenticado");
            }
            return;
        }

        // Primero obtener todas las notificaciones no leídas
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Si no hay notificaciones sin leer, terminar
                    if (queryDocumentSnapshots.isEmpty()) {
                        if (callback != null) {
                            callback.onSuccess();
                        }
                        return;
                    }

                    // Crear un batch para actualizar múltiples documentos
                    com.google.firebase.firestore.WriteBatch batch = db.batch();

                    // Agregar cada documento al batch
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.update(doc.getReference(), "read", true);
                    }

                    // Ejecutar el batch
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                if (callback != null) {
                                    callback.onSuccess();
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) {
                                    callback.onFailure(e.getMessage());
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Eliminar una notificación específica
     */
    public void deleteNotification(String notificationId, NotificationCallback callback) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (userId.isEmpty() || notificationId == null || notificationId.isEmpty()) {
            if (callback != null) {
                callback.onFailure("Usuario o notificación no válidos");
            }
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    public void createNotificationChannel(NotificationChannel channel) {

    }

    /**
     * Interface para callbacks de operaciones con notificaciones
     */
    public interface NotificationCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}