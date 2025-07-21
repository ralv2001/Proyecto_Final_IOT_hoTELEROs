package com.example.proyecto_final_hoteleros.client.utils;

import android.util.Log;

import com.example.proyecto_final_hoteleros.adminhotel.model.HotelProfile;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class HotelPhotoUtils {

    private static final String TAG = "HotelPhotoUtils";

    /**
     * ✅ MÉTODO ROBUSTO: Extraer primera foto del HotelProfile
     * Usa múltiples estrategias para obtener las fotos:
     * 1. Método getPhotoUrls() si existe
     * 2. Campo photoUrls usando reflection
     * 3. Otros nombres de campo posibles
     */
    public static String getFirstPhotoFromProfile(HotelProfile profile) {
        if (profile == null) {
            return "hotel_placeholder";
        }

        try {
            // ✅ ESTRATEGIA 1: Método getPhotoUrls()
            List<String> photoUrls = tryGetPhotoUrlsMethod(profile);
            if (photoUrls != null && !photoUrls.isEmpty()) {
                String firstPhoto = photoUrls.get(0);
                Log.d(TAG, "📸 Primera foto obtenida (método): " +
                        firstPhoto.substring(0, Math.min(50, firstPhoto.length())) + "...");
                return firstPhoto;
            }

            // ✅ ESTRATEGIA 2: Campo photoUrls usando reflection
            photoUrls = tryGetPhotoUrlsField(profile);
            if (photoUrls != null && !photoUrls.isEmpty()) {
                String firstPhoto = photoUrls.get(0);
                Log.d(TAG, "📸 Primera foto obtenida (campo): " +
                        firstPhoto.substring(0, Math.min(50, firstPhoto.length())) + "...");
                return firstPhoto;
            }

            // ✅ ESTRATEGIA 3: Otros nombres de campo posibles
            photoUrls = tryAlternativeFields(profile);
            if (photoUrls != null && !photoUrls.isEmpty()) {
                String firstPhoto = photoUrls.get(0);
                Log.d(TAG, "📸 Primera foto obtenida (alternativo): " +
                        firstPhoto.substring(0, Math.min(50, firstPhoto.length())) + "...");
                return firstPhoto;
            }

        } catch (Exception e) {
            Log.w(TAG, "⚠️ Error obteniendo fotos del hotel: " + profile.getName() + " - " + e.getMessage());
        }

        // ✅ FALLBACK: Si no hay fotos, usar placeholder
        Log.d(TAG, "📷 No se encontraron fotos para hotel: " + profile.getName() + ", usando placeholder");
        return "hotel_placeholder";
    }

    /**
     * ✅ ESTRATEGIA 1: Intentar usar método getPhotoUrls()
     */
    @SuppressWarnings("unchecked")
    private static List<String> tryGetPhotoUrlsMethod(HotelProfile profile) {
        try {
            Method method = profile.getClass().getMethod("getPhotoUrls");
            Object result = method.invoke(profile);
            if (result instanceof List) {
                return (List<String>) result;
            }
        } catch (Exception e) {
            Log.d(TAG, "Método getPhotoUrls() no disponible: " + e.getMessage());
        }
        return null;
    }

    /**
     * ✅ ESTRATEGIA 2: Intentar usar campo photoUrls
     */
    @SuppressWarnings("unchecked")
    private static List<String> tryGetPhotoUrlsField(HotelProfile profile) {
        try {
            Field field = profile.getClass().getDeclaredField("photoUrls");
            field.setAccessible(true);
            Object result = field.get(profile);
            if (result instanceof List) {
                return (List<String>) result;
            }
        } catch (Exception e) {
            Log.d(TAG, "Campo photoUrls no disponible: " + e.getMessage());
        }
        return null;
    }

    /**
     * ✅ ESTRATEGIA 3: Intentar nombres alternativos de campos
     */
    @SuppressWarnings("unchecked")
    private static List<String> tryAlternativeFields(HotelProfile profile) {
        String[] alternativeNames = {
                "photos",
                "imageUrls",
                "images",
                "hotelPhotos",
                "photoList"
        };

        for (String fieldName : alternativeNames) {
            try {
                Field field = profile.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object result = field.get(profile);
                if (result instanceof List) {
                    List<String> photos = (List<String>) result;
                    if (!photos.isEmpty()) {
                        Log.d(TAG, "✅ Fotos encontradas en campo: " + fieldName);
                        return photos;
                    }
                }
            } catch (Exception e) {
                // Continuar con el siguiente nombre
            }
        }
        return null;
    }

    /**
     * ✅ MÉTODO ADICIONAL: Verificar si un hotel tiene fotos
     */
    public static boolean hasPhotos(HotelProfile profile) {
        String firstPhoto = getFirstPhotoFromProfile(profile);
        return firstPhoto != null && !firstPhoto.equals("hotel_placeholder");
    }

    /**
     * ✅ MÉTODO ADICIONAL: Obtener todas las fotos del hotel
     */
    @SuppressWarnings("unchecked")
    public static List<String> getAllPhotosFromProfile(HotelProfile profile) {
        if (profile == null) {
            return null;
        }

        try {
            // Intentar los mismos métodos que getFirstPhotoFromProfile
            List<String> photoUrls = tryGetPhotoUrlsMethod(profile);
            if (photoUrls != null && !photoUrls.isEmpty()) {
                return photoUrls;
            }

            photoUrls = tryGetPhotoUrlsField(profile);
            if (photoUrls != null && !photoUrls.isEmpty()) {
                return photoUrls;
            }

            photoUrls = tryAlternativeFields(profile);
            if (photoUrls != null && !photoUrls.isEmpty()) {
                return photoUrls;
            }

        } catch (Exception e) {
            Log.w(TAG, "Error obteniendo todas las fotos: " + e.getMessage());
        }

        return null;
    }

    /**
     * ✅ MÉTODO ADICIONAL: Debug - Listar todos los campos del HotelProfile
     */
    public static void debugHotelProfileFields(HotelProfile profile) {
        if (profile == null) return;

        Log.d(TAG, "=== DEBUG: Campos de HotelProfile ===");
        Field[] fields = profile.getClass().getDeclaredFields();

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(profile);
                String type = field.getType().getSimpleName();

                Log.d(TAG, "Campo: " + field.getName() + " | Tipo: " + type + " | Valor: " +
                        (value != null ? value.toString().substring(0, Math.min(50, value.toString().length())) : "null"));

            } catch (Exception e) {
                Log.d(TAG, "Campo: " + field.getName() + " | Error: " + e.getMessage());
            }
        }
        Log.d(TAG, "=== FIN DEBUG ===");
    }
}