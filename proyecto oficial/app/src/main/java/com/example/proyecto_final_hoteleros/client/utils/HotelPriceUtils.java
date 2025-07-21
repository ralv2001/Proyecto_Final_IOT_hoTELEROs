package com.example.proyecto_final_hoteleros.client.utils;

import android.util.Log;

import com.example.proyecto_final_hoteleros.adminhotel.model.HotelProfile;
import com.example.proyecto_final_hoteleros.adminhotel.model.RoomType;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseRoomManager;

import java.util.List;
import java.util.Locale;

/**
 * Utilidad para obtener precios de hoteles basados en sus habitaciones más baratas
 */
public class HotelPriceUtils {

    private static final String TAG = "HotelPriceUtils";

    public interface PriceCallback {
        void onPriceObtained(String formattedPrice, double rawPrice);
        void onError(String error);
    }

    /**
     * Obtiene el precio mínimo de las habitaciones de un hotel específico
     */
    public static void getMinimumRoomPrice(HotelProfile hotel, android.content.Context context, PriceCallback callback) {
        if (hotel == null || hotel.getHotelAdminId() == null) {
            Log.w(TAG, "Hotel o hotelAdminId es null");
            callback.onError("Información del hotel incompleta");
            return;
        }

        Log.d(TAG, "🔍 Obteniendo precio mínimo para hotel: " + hotel.getName());

        // Usar Firebase directamente para obtener habitaciones del hotel específico
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("hotel_rooms")
                .whereEqualTo("hotelAdminId", hotel.getHotelAdminId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "✅ Habitaciones obtenidas: " + queryDocumentSnapshots.size());

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.w(TAG, "❌ No se encontraron habitaciones para el hotel: " + hotel.getName());
                        // Precio por defecto si no hay habitaciones
                        double defaultPrice = generateDefaultPrice(hotel);
                        String formattedPrice = formatPrice(defaultPrice);
                        callback.onPriceObtained(formattedPrice, defaultPrice);
                        return;
                    }

                    // Encontrar precio mínimo directamente de los documentos
                    double minPrice = Double.MAX_VALUE;
                    boolean foundValidPrice = false;

                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        Double pricePerNight = doc.getDouble("pricePerNight");
                        if (pricePerNight != null && pricePerNight > 0) {
                            minPrice = Math.min(minPrice, pricePerNight);
                            foundValidPrice = true;
                        }
                    }

                    if (!foundValidPrice) {
                        minPrice = generateDefaultPrice(hotel);
                    }

                    String formattedPrice = formatPrice(minPrice);

                    Log.d(TAG, "💰 Precio mínimo encontrado para " + hotel.getName() + ": " + formattedPrice);
                    callback.onPriceObtained(formattedPrice, minPrice);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error obteniendo habitaciones: " + e.getMessage());

                    // Fallback a precio generado
                    double defaultPrice = generateDefaultPrice(hotel);
                    String formattedPrice = formatPrice(defaultPrice);

                    Log.d(TAG, "🔄 Usando precio por defecto para " + hotel.getName() + ": " + formattedPrice);
                    callback.onPriceObtained(formattedPrice, defaultPrice);
                });
    }



    /**
     * Formatea el precio en el formato de la moneda local
     */
    private static String formatPrice(double price) {
        return String.format(Locale.US, "S/%.0f", price);
    }

    /**
     * Genera un precio por defecto basado en características del hotel
     */
    private static double generateDefaultPrice(HotelProfile hotel) {
        // Usar hash del nombre para generar precio consistente
        int basePrice = 120 + (Math.abs(hotel.getName().hashCode()) % 280); // Entre S/120 y S/400

        // Ajustar por ubicación si está disponible
        String location = hotel.getDepartamento();
        if (location != null) {
            location = location.toLowerCase();
            if (location.contains("lima")) {
                basePrice += 50; // Lima más caro
            } else if (location.contains("cusco")) {
                basePrice += 30; // Cusco turístico
            } else if (location.contains("arequipa")) {
                basePrice += 20; // Arequipa
            }
        }

        return basePrice;
    }

    /**
     * Obtiene precio mínimo de manera síncrona (para uso en adapters existentes como fallback)
     */
    public static String generatePriceSync(HotelProfile hotel) {
        double price = generateDefaultPrice(hotel);
        return formatPrice(price);
    }

    /**
     * Calcula precio para múltiples noches
     */
    public static String calculatePriceForNights(double pricePerNight, int nights) {
        double totalPrice = pricePerNight * nights;
        return formatPrice(totalPrice);
    }

    /**
     * Obtiene información de precio para mostrar en detalles
     */
    public static String getPriceDescription(int nights) {
        if (nights == 1) {
            return "por noche";
        } else {
            return "por " + nights + " noches";
        }
    }
}