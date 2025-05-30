package com.example.proyecto_final_hoteleros.taxista.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.proyecto_final_hoteleros.taxista.utils.NotificationHelper;
import com.example.proyecto_final_hoteleros.DriverActivity;
import com.example.proyecto_final_hoteleros.R;

public class NotificationHelper {

    private static final String CHANNEL_ID_TRIP_REQUESTS = "trip_requests";
    private static final String CHANNEL_ID_DRIVER_STATUS = "driver_status";
    private static final String CHANNEL_ID_EARNINGS = "earnings";
    private static final String CHANNEL_ID_GENERAL = "general";

    private Context context;
    private NotificationManagerCompat notificationManager;
    private DriverPreferenceManager preferenceManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        this.preferenceManager = new DriverPreferenceManager(context);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal para solicitudes de viaje (alta prioridad)
            NotificationChannel tripChannel = new NotificationChannel(
                    CHANNEL_ID_TRIP_REQUESTS,
                    "Solicitudes de Viaje",
                    NotificationManager.IMPORTANCE_HIGH
            );
            tripChannel.setDescription("Notificaciones de nuevas solicitudes de viaje");
            tripChannel.enableVibration(true);
            tripChannel.setVibrationPattern(new long[]{0, 500, 200, 500});
            tripChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);

            // Canal para estado del conductor (prioridad media)
            NotificationChannel statusChannel = new NotificationChannel(
                    CHANNEL_ID_DRIVER_STATUS,
                    "Estado del Conductor",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            statusChannel.setDescription("Notificaciones sobre el estado del conductor");

            // Canal para ganancias (prioridad baja)
            NotificationChannel earningsChannel = new NotificationChannel(
                    CHANNEL_ID_EARNINGS,
                    "Ganancias",
                    NotificationManager.IMPORTANCE_LOW
            );
            earningsChannel.setDescription("Notificaciones sobre ganancias y estadísticas");

            // Canal general (prioridad media)
            NotificationChannel generalChannel = new NotificationChannel(
                    CHANNEL_ID_GENERAL,
                    "General",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            generalChannel.setDescription("Notificaciones generales de la aplicación");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(tripChannel);
            manager.createNotificationChannel(statusChannel);
            manager.createNotificationChannel(earningsChannel);
            manager.createNotificationChannel(generalChannel);
        }
    }
    // === NUEVAS NOTIFICACIONES ===

    // Notificación de documentación por vencer
    public void showDocumentExpirationNotification(String documentType, int daysRemaining) {
        Intent intent = new Intent(context, DriverActivity.class);
        intent.putExtra("open_fragment", "perfil");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );

        String title = "⚠️ Documentación por vencer";
        String message = "Tu " + documentType + " vence en " + daysRemaining + " día" +
                (daysRemaining == 1 ? "" : "s") + ". Actualízala a tiempo.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message + "\n\nEs importante mantener tus documentos al día para seguir ofreciendo servicios."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_profile, "Ver Perfil", pendingIntent);

        notificationManager.notify(2001, builder.build());

        // Incrementar contador
        int currentCount = preferenceManager.getNotificationCount();
        preferenceManager.setNotificationCount(currentCount + 1);
    }

    // Notificación de sugerencia de descanso
    public void showRestSuggestionNotification(int hoursActive) {
        Intent intent = new Intent(context, DriverActivity.class);
        intent.putExtra("status_action", "take_break");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );

        String title = "😴 Hora de descansar";
        String message = "Has estado activo por más de " + hoursActive + " horas. Te recomendamos tomar un descanso.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_DRIVER_STATUS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message + "\n\n💡 Tu seguridad y la de tus pasajeros es importante. Un conductor descansado es un conductor seguro."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_close, "Descansar Ahora", createStatusActionIntent("go_offline"));

        notificationManager.notify(2002, builder.build());

        // Incrementar contador
        int currentCount = preferenceManager.getNotificationCount();
        preferenceManager.setNotificationCount(currentCount + 1);
    }

    // Notificación de viaje completado con calificación
    public void showTripCompletedNotification(String clientName, double earnings, float rating) {
        Intent intent = new Intent(context, DriverActivity.class);
        intent.putExtra("open_fragment", "historial");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );

        String title = "🎉 Viaje completado";
        String message = "Viaje con " + clientName + " finalizado. Ganaste S/ " + String.format("%.2f", earnings);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_EARNINGS)
                .setSmallIcon(R.drawable.ic_check_circle)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message + "\n⭐ Calificación recibida: " + rating + "/5.0\n¡Excelente trabajo!"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(2003, builder.build());

        // Incrementar contador
        int currentCount = preferenceManager.getNotificationCount();
        preferenceManager.setNotificationCount(currentCount + 1);
    }

    // Notificación de meta de ganancias alcanzada
    public void showEarningsGoalNotification(double goalAmount, double currentAmount) {
        Intent intent = new Intent(context, DriverActivity.class);
        intent.putExtra("open_fragment", "perfil");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );

        String title = "🎯 ¡Meta alcanzada!";
        String message = "Has alcanzado tu meta de S/ " + String.format("%.2f", goalAmount) + " del día";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_EARNINGS)
                .setSmallIcon(R.drawable.ic_payment)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message + "\n💰 Ganancias actuales: S/ " + String.format("%.2f", currentAmount) +
                                "\n¡Felicitaciones por tu dedicación!"))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(2004, builder.build());

        // Incrementar contador
        int currentCount = preferenceManager.getNotificationCount();
        preferenceManager.setNotificationCount(currentCount + 1);
    }

    // Notificación de zona de alta demanda
    public void showHighDemandZoneNotification(String zoneName, String estimatedEarnings) {
        Intent intent = new Intent(context, DriverActivity.class);
        intent.putExtra("open_fragment", "mapa");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );

        String title = "🔥 Zona de alta demanda";
        String message = "Alta demanda en " + zoneName + ". Ganancias estimadas: " + estimatedEarnings;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
                .setSmallIcon(R.drawable.ic_location)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message + "\n\n📍 Dirígete a esta zona para maximizar tus ganancias."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(2005, builder.build());

        // Incrementar contador
        int currentCount = preferenceManager.getNotificationCount();
        preferenceManager.setNotificationCount(currentCount + 1);
    }

    // Notificación de promoción especial
    public void showSpecialPromoNotification(String promoTitle, String promoDetails, int bonusPercentage) {
        Intent intent = new Intent(context, DriverActivity.class);
        intent.putExtra("open_fragment", "viajes");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );

        String title = "🎁 " + promoTitle;
        String message = "Gana " + bonusPercentage + "% extra. " + promoDetails;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message + "\n\n⏰ Promoción válida por tiempo limitado. ¡Aprovéchala ahora!"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(2006, builder.build());

        // Incrementar contador
        int currentCount = preferenceManager.getNotificationCount();
        preferenceManager.setNotificationCount(currentCount + 1);
    }

    // Notificación de feedback de cliente
    public void showClientFeedbackNotification(String clientName, String feedback, float rating) {
        Intent intent = new Intent(context, DriverActivity.class);
        intent.putExtra("open_fragment", "historial");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );

        String title = "💬 Nuevo comentario";
        String message = clientName + " te dejó una reseña de " + rating + " estrellas";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
                .setSmallIcon(R.drawable.ic_star)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message + "\n\n\"" + feedback + "\""))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(2007, builder.build());

        // Incrementar contador
        int currentCount = preferenceManager.getNotificationCount();
        preferenceManager.setNotificationCount(currentCount + 1);
    }
    // === NOTIFICACIONES DE SOLICITUDES DE VIAJE ===
    public void showTripRequestNotification(String hotelName, String clientName, String pickup, double price) {
        Intent intent = new Intent(context, DriverActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("open_fragment", "viajes");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_TRIP_REQUESTS)
                .setSmallIcon(R.drawable.ic_taxi)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notification_active))
                .setContentTitle("🚗 Nueva Solicitud de Viaje")
                .setContentText("Cliente: " + clientName + " • Hotel: " + hotelName)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Cliente: " + clientName + "\n" +
                                "Hotel: " + hotelName + "\n" +
                                "Recogida: " + pickup + "\n" +
                                "Precio: S/ " + String.format("%.2f", price)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .addAction(R.drawable.ic_check_circle, "Aceptar", createTripActionIntent("accept"))
                .addAction(R.drawable.ic_close, "Rechazar", createTripActionIntent("reject"));

        notificationManager.notify(1001, builder.build());

        // Incrementar contador de notificaciones
        int currentCount = preferenceManager.getNotificationCount();
        preferenceManager.setNotificationCount(currentCount + 1);
    }

    private PendingIntent createTripActionIntent(String action) {
        Intent intent = new Intent(context, DriverActivity.class);
        intent.putExtra("trip_action", action);
        return PendingIntent.getActivity(
                context, action.hashCode(), intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    // === NOTIFICACIONES DE ESTADO ===
    public void showDriverStatusNotification(boolean isOnline) {
        String title = isOnline ? "¡Estás en línea!" : "Has salido de línea";
        String message = isOnline ?
                "Ahora puedes recibir solicitudes de viaje" :
                "No recibirás nuevas solicitudes hasta que te conectes";

        Intent intent = new Intent(context, DriverActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_DRIVER_STATUS)
                .setSmallIcon(isOnline ? R.drawable.ic_check_circle : R.drawable.ic_close)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(1002, builder.build());
    }

    // === NOTIFICACIONES DE GANANCIAS ===
    public void showEarningsNotification(double todayEarnings, int tripsCompleted) {
        Intent intent = new Intent(context, DriverActivity.class);
        intent.putExtra("open_fragment", "perfil");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_EARNINGS)
                .setSmallIcon(R.drawable.ic_payment)
                .setContentTitle("💰 Resumen de Ganancias")
                .setContentText("Hoy: S/ " + String.format("%.2f", todayEarnings) + " • " + tripsCompleted + " viajes")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Ganancias de hoy: S/ " + String.format("%.2f", todayEarnings) + "\n" +
                                "Viajes completados: " + tripsCompleted + "\n" +
                                "¡Sigue así, excelente trabajo!"))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(1003, builder.build());
    }

    // === NOTIFICACIONES GENERALES ===
    public void showGeneralNotification(String title, String message) {
        Intent intent = new Intent(context, DriverActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());

        // Incrementar contador de notificaciones
        int currentCount = preferenceManager.getNotificationCount();
        preferenceManager.setNotificationCount(currentCount + 1);
    }

    // === NOTIFICACIÓN PERSISTENTE DE ESTADO EN LÍNEA ===
    public void showOnlineStatusNotification() {
        Intent intent = new Intent(context, DriverActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_DRIVER_STATUS)
                .setSmallIcon(R.drawable.ic_taxi)
                .setContentTitle("🟢 Conductores App - En línea")
                .setContentText("Esperando solicitudes de viaje...")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true) // Hace que la notificación sea persistente
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_close, "Desconectar", createStatusActionIntent("disconnect"));

        notificationManager.notify(1004, builder.build());
    }

    private PendingIntent createStatusActionIntent(String action) {
        Intent intent = new Intent(context, DriverActivity.class);
        intent.putExtra("status_action", action);
        return PendingIntent.getActivity(
                context, action.hashCode(), intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    public void hideOnlineStatusNotification() {
        notificationManager.cancel(1004);
    }

    // === LIMPIAR NOTIFICACIONES ===
    public void clearTripNotifications() {
        notificationManager.cancel(1001);
    }

    public void clearAllNotifications() {
        notificationManager.cancelAll();
        preferenceManager.setNotificationCount(0);
    }
}