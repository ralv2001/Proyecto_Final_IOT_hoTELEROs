package com.example.proyecto_final_hoteleros.client.data.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.proyecto_final_hoteleros.MainActivity;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.Notification;
import com.example.proyecto_final_hoteleros.utils.NotificationManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FirebaseNotificationService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseNotificationSvc";
    private static final String CHANNEL_ID = "hotel_notifications";
    private static final int NOTIFICATION_ID = 100;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Verificar si el mensaje contiene datos
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }

        // Verificar si el mensaje contiene una notificación
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            String title = remoteMessage.getNotification().getTitle();
            String message = remoteMessage.getNotification().getBody();

            // Crear notificación Android
            showNotification(title, message);

            // Guardar en Firestore (asumiendo tipo genérico)
            saveNotificationToFirestore(title, message);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // Enviar el token al servidor (opcional)
        sendTokenToServer(token);
    }

    /**
     * Manejar mensaje de datos
     */
    private void handleDataMessage(Map<String, String> data) {
        String title = data.get("title");
        String message = data.get("message");
        String type = data.get("type");
        String actionData = data.get("actionData");

        if (title == null || message == null) {
            Log.e(TAG, "Datos de notificación incompletos");
            return;
        }

        // Determinar el tipo de notificación
        int notificationType = Notification.TYPE_BOOKING; // Por defecto
        if (type != null) {
            try {
                notificationType = Integer.parseInt(type);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Tipo de notificación no válido: " + type);
            }
        }

        // Mostrar notificación Android
        showNotification(title, message);

        // Guardar en Firestore
        saveNotificationToFirestore(title, message, notificationType, actionData);
    }

    /**
     * Mostrar notificación Android
     */
    private void showNotification(String title, String message) {
        // Crear canal de notificación para Android 8.0+
        createNotificationChannel();

        // Intent para abrir la app al hacer clic en la notificación
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        // Construir notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Mostrar notificación
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    /**
     * Crear canal de notificación (requerido para Android 8.0+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Hotel Notifications";
            String description = "Channel for hotel app notifications";
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Registrar el canal
            @SuppressLint("ServiceCast") NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Guardar notificación en Firestore (versión simple)
     */
    private void saveNotificationToFirestore(String title, String message) {
        saveNotificationToFirestore(title, message, Notification.TYPE_BOOKING, null);
    }

    /**
     * Guardar notificación en Firestore (versión completa)
     */
    private void saveNotificationToFirestore(String title, String message, int type, String actionData) {
        com.example.proyecto_final_hoteleros.utils.NotificationManager.getInstance().sendNotification(title, message, type, actionData);
    }

    /**
     * Enviar token al servidor
     */
    private void sendTokenToServer(String token) {
        // Implementar según necesidades
        Log.d(TAG, "Token enviado al servidor: " + token);
    }
}