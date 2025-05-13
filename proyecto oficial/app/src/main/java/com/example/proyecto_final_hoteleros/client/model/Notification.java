package com.example.proyecto_final_hoteleros.client.model;

import android.graphics.Color;
import com.example.proyecto_final_hoteleros.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Modelo de datos para una notificación.
 */
public class Notification {
    // Constantes para los tipos de notificación
    public static final int TYPE_BOOKING = 1;
    public static final int TYPE_CHECK_IN = 2;
    public static final int TYPE_CHECK_OUT = 3;
    public static final int TYPE_PROMO = 4;
    public static final int TYPE_REMINDER = 5;
    public static final int TYPE_SYSTEM = 6;

    private String id;
    private String title;
    private String message;
    private int type;
    private Date timestamp;
    private boolean read;
    private String actionData; // datos adicionales para la acción (ID de reserva, etc.)
    private String imageUrl; // URL de imagen opcional para notificaciones

    // Constructor vacío para Firebase
    public Notification() {
        // Constructor vacío necesario para Firebase
    }

    // Constructor completo
    public Notification(String id, String title, String message, int type,
                        Date timestamp, boolean read, String actionData) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
        this.read = read;
        this.actionData = actionData;
        this.imageUrl = null; // Por defecto sin imagen
    }

    // Constructor con imagen
    public Notification(String id, String title, String message, int type,
                        Date timestamp, boolean read, String actionData, String imageUrl) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
        this.read = read;
        this.actionData = actionData;
        this.imageUrl = imageUrl;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getActionData() {
        return actionData;
    }

    public void setActionData(String actionData) {
        this.actionData = actionData;
    }

    // Getter y setter para imageUrl
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Método para obtener el recurso de icono según el tipo (mejorado)
    public int getIconResource() {
        switch (type) {
            case TYPE_BOOKING:
                return R.drawable.ic_booking;
            case TYPE_CHECK_IN:
                return R.drawable.ic_check_in;
            case TYPE_CHECK_OUT:
                return R.drawable.ic_check_out;
            case TYPE_PROMO:
                return R.drawable.ic_promo;
            case TYPE_REMINDER:
                return R.drawable.ic_reminder;
            case TYPE_SYSTEM:
                return R.drawable.ic_system;
            default:
                return R.drawable.ic_notification;
        }
    }

    // Método para formatear el tiempo transcurrido (mejorado)
    public String getTimeAgo() {
        long timeDiff = System.currentTimeMillis() - timestamp.getTime();
        long seconds = timeDiff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 7) {
            // Formato de fecha para notificaciones antiguas
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
            return sdf.format(timestamp);
        } else if (days > 0) {
            return days + "d";
        } else if (hours > 0) {
            return hours + "h";
        } else if (minutes > 0) {
            return minutes + "m";
        } else {
            return "Ahora";
        }
    }

    // Método para obtener el color según el tipo
    public int getTypeColor() {
        switch (type) {
            case TYPE_BOOKING:
                return Color.parseColor("#FF5722"); // Naranja
            case TYPE_CHECK_IN:
                return Color.parseColor("#4CAF50"); // Verde
            case TYPE_CHECK_OUT:
                return Color.parseColor("#2196F3"); // Azul
            case TYPE_PROMO:
                return Color.parseColor("#9C27B0"); // Morado
            case TYPE_REMINDER:
                return Color.parseColor("#FFC107"); // Ámbar
            case TYPE_SYSTEM:
                return Color.parseColor("#607D8B"); // Gris azulado
            default:
                return Color.parseColor("#757575"); // Gris
        }
    }
}