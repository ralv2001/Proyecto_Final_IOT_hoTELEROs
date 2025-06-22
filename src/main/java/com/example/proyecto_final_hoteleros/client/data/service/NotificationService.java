package com.example.proyecto_final_hoteleros.client.data.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.proyecto_final_hoteleros.MainActivity;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para manejar las notificaciones push y locales usando Firebase Realtime Database
 */
public class NotificationService {

    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID = "hotel_notifications";
    private static final String CHANNEL_NAME = "Notificaciones de Hotel";
    private static final String CHANNEL_DESC = "Notificaciones sobre reservas, check-ins y check-outs";

    private final Context context;
    private final NotificationManager notificationManager;
    private final DatabaseReference databaseRef;
    private final FirebaseAuth auth;

    /**
     * Constructor del servicio de notificaciones
     */
    public NotificationService(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
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
     * Procesa y muestra una notificación a partir de datos recibidos
     */
    public void showNotification(String title, String message, int type, String actionData) {
        // Guardar en Firebase Realtime Database
        saveNotificationToDatabase(title, message, type, actionData);

        // Mostrar notificación local
        showLocalNotification(title, message, type, actionData);
    }

    /**
     * Mostrar notificación local en el dispositivo
     */
    private void showLocalNotification(String title, String message, int type, String actionData) {
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

        // Ícono y color según tipo
        int smallIcon;
        int color;

        switch (type) {
            case Notification.TYPE_BOOKING:
                smallIcon = R.drawable.ic_booking;
                color = Color.parseColor("#FF5722");
                break;
            case Notification.TYPE_CHECK_IN:
                smallIcon = R.drawable.ic_check_in;
                color = Color.parseColor("#4CAF50");
                break;
            case Notification.TYPE_CHECK_OUT:
                smallIcon = R.drawable.ic_check_out;
                color = Color.parseColor("#2196F3");
                break;
            case Notification.TYPE_PROMO:
                smallIcon = R.drawable.ic_promo;
                color = Color.parseColor("#9C27B0");
                break;
            case Notification.TYPE_REMINDER:
                smallIcon = R.drawable.ic_reminder;
                color = Color.parseColor("#FFC107");
                break;
            default:
                smallIcon = R.drawable.ic_notification;
                color = Color.parseColor("#FF5722");
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
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        // Mostrar la notificación
        notificationManager.notify(notificationId, notificationBuilder.build());

        Log.d(TAG, "Notificación local mostrada: " + title);
    }

    /**
     * Guarda la notificación en Firebase Realtime Database
     */
    private void saveNotificationToDatabase(String title, String message, int type, String actionData) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "user_demo";
        String notificationId = UUID.randomUUID().toString();

        // Datos de la notificación
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("id", notificationId);
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("type", type);
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("read", false);
        notificationData.put("actionData", actionData);

        // Guardar en Firebase Realtime Database
        databaseRef.child("notifications").child(userId).child(notificationId).setValue(notificationData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notificación guardada en Database: " + notificationId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error guardando notificación: " + e.getMessage());
                });
    }

    /**
     * Método para enviar notificaciones de prueba
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

    /**
     * Método público para crear notificaciones desde otras partes de la app
     */
    public static void createNotification(Context context, String userId, String title, String message, int type, String actionData) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        String notificationId = UUID.randomUUID().toString();

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("id", notificationId);
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("type", type);
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("read", false);
        notificationData.put("actionData", actionData);

        databaseRef.child("notifications").child(userId).child(notificationId).setValue(notificationData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notificación creada para usuario: " + userId);

                    // También mostrar notificación local si es el usuario actual
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "user_demo";
                    if (userId.equals(currentUserId)) {
                        NotificationService service = new NotificationService(context);
                        service.showLocalNotification(title, message, type, actionData);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creando notificación: " + e.getMessage());
                });
    }
}