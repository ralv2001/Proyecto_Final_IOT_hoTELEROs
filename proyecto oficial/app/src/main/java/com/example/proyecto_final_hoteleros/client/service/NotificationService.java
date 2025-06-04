package com.example.proyecto_final_hoteleros.client.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.proyecto_final_hoteleros.MainActivity;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para manejar las notificaciones push y locales
 */
public class NotificationService {

    private static final String CHANNEL_ID = "hotel_notifications";
    private static final String CHANNEL_NAME = "Notificaciones de Hotel";
    private static final String CHANNEL_DESC = "Notificaciones sobre reservas, check-ins y check-outs";

    private final Context context;
    private final NotificationManager notificationManager;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    /**
     * Constructor del servicio de notificaciones
     * @param context Contexto de la aplicación
     */
    public NotificationService(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();

        // Crear canal de notificaciones (requerido en Android 8.0+)
        createNotificationChannel();
    }

    /**
     * Crear el canal de notificaciones para Android 8.0+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);

            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Procesa y muestra una notificación a partir de datos recibidos (FCM o locales)
     * @param title Título de la notificación
     * @param message Mensaje de la notificación
     * @param type Tipo de notificación (booking, check-in, check-out)
     * @param actionData Datos adicionales para la acción (ID de reserva, etc.)
     */
    public void showNotification(String title, String message, int type, String actionData) {
        // Guardar en Firestore
        saveNotificationToFirestore(title, message, type, actionData);

        // Mostrar notificación local
        int notificationId = (int) System.currentTimeMillis();

        // Intent para abrir la app
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("notification_type", type);
        intent.putExtra("action_data", actionData);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Sonido por defecto
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Ícono según tipo
        int smallIcon;
        int color;

        switch (type) {
            case Notification.TYPE_BOOKING:
                smallIcon = R.drawable.ic_booking;
                color = Color.parseColor("#FF5722"); // Color naranja
                break;
            case Notification.TYPE_CHECK_IN:
                smallIcon = R.drawable.ic_check_in;
                color = Color.parseColor("#4CAF50"); // Color verde
                break;
            case Notification.TYPE_CHECK_OUT:
                smallIcon = R.drawable.ic_check_out;
                color = Color.parseColor("#2196F3"); // Color azul
                break;
            default:
                smallIcon = R.drawable.ic_notification;
                color = Color.parseColor("#FF5722"); // Color naranja por defecto
        }

        // Construir notificación
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setColor(color)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Mostrar la notificación
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    /**
     * Guarda la notificación en Firestore para el usuario actual
     */
    private void saveNotificationToFirestore(String title, String message, int type, String actionData) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) return; // No hay usuario autenticado

        // Datos de la notificación
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("type", type);
        notificationData.put("timestamp", new Date());
        notificationData.put("read", false);
        notificationData.put("actionData", actionData);

        // ID único para la notificación
        String notificationId = UUID.randomUUID().toString();

        // Guardar en Firestore
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .set(notificationData)
                .addOnFailureListener(e -> {
                    // Error al guardar (podría intentar de nuevo o registrar el error)
                });
    }

    /**
     * Método para enviar notificaciones de prueba (solo para desarrollo)
     * @param type Tipo de notificación a probar
     */
    public void testNotification(int type) {
        String title, message, actionData;

        switch (type) {
            case Notification.TYPE_BOOKING:
                title = "Reserva confirmada";
                message = "Tu reserva para Hotel Miraflores ha sido confirmada exitosamente. Te esperamos el 15 de Mayo.";
                actionData = "booking" + System.currentTimeMillis();
                break;
            case Notification.TYPE_CHECK_IN:
                title = "Check-in disponible";
                message = "Ya puedes realizar tu check-in para tu estadía en Hotel Lima Centro. Te esperamos mañana.";
                actionData = "booking" + System.currentTimeMillis();
                break;
            case Notification.TYPE_CHECK_OUT:
                title = "Check-out completado";
                message = "Tu check-out del Hotel San Isidro ha sido procesado. ¡Gracias por tu estadía!";
                actionData = "booking" + System.currentTimeMillis();
                break;
            default:
                title = "Notificación de prueba";
                message = "Esta es una notificación de prueba del sistema de hoteles.";
                actionData = "test";
        }

        showNotification(title, message, type, actionData);
    }
}