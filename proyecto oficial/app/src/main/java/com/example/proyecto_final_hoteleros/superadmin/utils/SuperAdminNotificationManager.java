package com.example.proyecto_final_hoteleros.superadmin.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity;

public class SuperAdminNotificationManager {

    private static final String TAG = "SuperAdminNotifications";
    private static final String CHANNEL_ID = "superadmin_alerts";
    private static final String CHANNEL_NAME = "SuperAdmin Alertas";

    private final Context context;
    private final NotificationManagerCompat notificationManager;

    public SuperAdminNotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Alertas importantes para el SuperAdmin");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 250, 250, 250});

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // ✅ NOTIFICACIÓN: Nuevos taxistas pendientes
    public void showNewPendingDriverNotification(int count) {
        if (count <= 0) return;

        String title = "🚗 Nuevos taxistas pendientes";
        String message = count == 1 ?
                "1 taxista necesita aprobación" :
                count + " taxistas necesitan aprobación";

        Intent intent = new Intent(context, SuperAdminActivity.class);
        intent.putExtra("navigate_to", "taxistas");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_car)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setNumber(count);

        safeNotify(1001, builder.build());
        Log.d(TAG, "📢 Notificación enviada: " + count + " taxistas pendientes");
    }

    // ✅ NOTIFICACIÓN: Sistema actualizado
    public void showSystemUpdateNotification() {
        String title = "📊 Sistema actualizado";
        String message = "Los datos han sido refrescados automáticamente";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_refresh)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true);

        safeNotify(1002, builder.build());
        Log.d(TAG, "📢 Notificación de actualización enviada");
    }

    // ✅ NOTIFICACIÓN: Actividad alta en el sistema
    public void showHighActivityNotification(int newUsers) {
        if (newUsers < 5) return; // Solo si hay bastante actividad

        String title = "🔥 Alta actividad en el sistema";
        String message = newUsers + " nuevos usuarios registrados hoy";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_people)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        safeNotify(1003, builder.build());
        Log.d(TAG, "📢 Notificación de alta actividad enviada");
    }

    private void safeNotify(int notificationId, android.app.Notification notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(notificationId, notification);
                }
            } else {
                notificationManager.notify(notificationId, notification);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error enviando notificación: " + e.getMessage());
        }
    }

    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}