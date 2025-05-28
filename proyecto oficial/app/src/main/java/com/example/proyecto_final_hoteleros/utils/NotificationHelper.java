package com.example.proyecto_final_hoteleros.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.proyecto_final_hoteleros.MainActivity;
import com.example.proyecto_final_hoteleros.R;

public class NotificationHelper {

    private static final String CHANNEL_ID_REGISTRATION = "registration_channel";
    private static final String CHANNEL_ID_DRIVER_STATUS = "driver_status_channel";

    private final Context context;
    private final NotificationManagerCompat notificationManager;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal para notificaciones de registro
            NotificationChannel registrationChannel = new NotificationChannel(
                    CHANNEL_ID_REGISTRATION,
                    "Registro de Usuario",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            registrationChannel.setDescription("Notificaciones relacionadas con el registro de usuarios");

            // Canal para notificaciones de estado de taxistas
            NotificationChannel driverStatusChannel = new NotificationChannel(
                    CHANNEL_ID_DRIVER_STATUS,
                    "Estado de Taxista",
                    NotificationManager.IMPORTANCE_HIGH
            );
            driverStatusChannel.setDescription("Notificaciones sobre el estado de aprobación de taxistas");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(registrationChannel);
                manager.createNotificationChannel(driverStatusChannel);
            }
        }
    }

    // Notificación cuando se completa el registro exitosamente
    public void showRegistrationCompleteNotification(String userType, String userName) {
        String title = "¡Registro Completado!";
        String message;

        if ("driver".equals(userType)) {
            message = "Hola " + userName + ", tu registro como taxista ha sido enviado para revisión. " +
                    "Te notificaremos cuando sea aprobado.";
        } else {
            message = "Hola " + userName + ", tu registro como cliente ha sido completado exitosamente. " +
                    "¡Ya puedes empezar a usar la aplicación!";
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_REGISTRATION)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        safeNotify(1001, builder.build());
    }

    // Notificación cuando un taxista es aprobado
    public void showDriverApprovedNotification(String driverName) {
        String title = "¡Solicitud Aprobada! 🎉";
        String message = "Felicidades " + driverName + ", tu solicitud para ser taxista ha sido aprobada. " +
                "Ya puedes empezar a recibir viajes.";

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("approved_driver", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_DRIVER_STATUS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 100, 500});

        safeNotify(1002, builder.build());

    }

    // Notificación cuando un taxista es rechazado
    public void showDriverRejectedNotification(String driverName, String reason) {
        String title = "Solicitud No Aprobada";
        String message = "Hola " + driverName + ", lamentablemente tu solicitud para ser taxista no ha sido aprobada.";

        if (reason != null && !reason.trim().isEmpty()) {
            message += " Motivo: " + reason;
        }

        message += " Puedes intentar registrarte nuevamente.";

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("rejected_driver", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_DRIVER_STATUS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        safeNotify(1003, builder.build());


    }

    // Notificación para recordar completar el registro
    public void showRegistrationReminderNotification(String userType) {
        String title = "Completa tu registro";
        String message = "Tienes un registro de " +
                ("driver".equals(userType) ? "taxista" : "cliente") +
                " pendiente. ¡Complétalo para empezar a usar la aplicación!";

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("complete_registration", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_REGISTRATION)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        safeNotify(1004, builder.build());

    }

    // Notificación de error en el proceso de registro
    public void showRegistrationErrorNotification(String errorMessage) {
        String title = "Error en el Registro";
        String message = "Hubo un problema con tu registro: " + errorMessage +
                " Por favor, inténtalo de nuevo.";

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_REGISTRATION)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        safeNotify(1005, builder.build());

    }

    // Cancelar todas las notificaciones
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }

    // Cancelar notificación específica
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }

    private void safeNotify(int notificationId, android.app.Notification notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(notificationId, notification);
            }
        } else {
            notificationManager.notify(notificationId, notification);
        }
    }
}