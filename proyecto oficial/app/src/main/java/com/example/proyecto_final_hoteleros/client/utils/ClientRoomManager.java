// client/utils/ClientRoomManager.java - NUEVO ARCHIVO
package com.example.proyecto_final_hoteleros.client.utils;

import android.content.Context;
import android.util.Log;

import com.example.proyecto_final_hoteleros.client.data.model.RoomType;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager para que el cliente obtenga habitaciones de hoteles específicos
 */
public class ClientRoomManager {

    private static final String TAG = "ClientRoomManager";
    private static final String ROOMS_COLLECTION = "hotel_rooms";

    private static ClientRoomManager instance;
    private FirebaseFirestore db;
    private Context context;

    public interface RoomsCallback {
        void onSuccess(List<RoomType> rooms);
        void onError(String error);
    }

    private ClientRoomManager(Context context) {
        this.context = context.getApplicationContext();
        this.db = FirebaseFirestore.getInstance();
    }

    public static synchronized ClientRoomManager getInstance(Context context) {
        if (instance == null) {
            instance = new ClientRoomManager(context);
        }
        return instance;
    }

    /**
     * Obtiene las habitaciones de un hotel específico usando su hotelAdminId
     */
    public void getHotelRooms(String hotelAdminId, RoomsCallback callback) {
        if (hotelAdminId == null || hotelAdminId.isEmpty()) {
            callback.onError("ID del hotel requerido");
            return;
        }

        Log.d(TAG, "🔍 Obteniendo habitaciones para hotel admin: " + hotelAdminId);

        db.collection(ROOMS_COLLECTION)
                .whereEqualTo("hotelAdminId", hotelAdminId)
                .whereEqualTo("active", true) // Solo habitaciones activas
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<RoomType> clientRooms = new ArrayList<>();

                    Log.d(TAG, "✅ Documentos obtenidos: " + queryDocumentSnapshots.size());

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            // Convertir de admin RoomType a client RoomType
                            RoomType clientRoom = convertToClientRoomType(doc);
                            if (clientRoom != null) {
                                clientRooms.add(clientRoom);
                                Log.d(TAG, "✅ Habitación convertida: " + clientRoom.getName() + " - " + clientRoom.getPrice());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error convirtiendo habitación: " + e.getMessage());
                        }
                    }

                    Log.d(TAG, "✅ Total habitaciones convertidas: " + clientRooms.size());
                    callback.onSuccess(clientRooms);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error obteniendo habitaciones: " + e.getMessage());
                    callback.onError("Error cargando habitaciones: " + e.getMessage());
                });
    }

    /**
     * Convierte una habitación del admin (Firebase) a habitación del cliente
     */
    private RoomType convertToClientRoomType(DocumentSnapshot doc) {
        try {
            // Extraer datos básicos del documento de Firebase
            String name = doc.getString("name");
            String description = doc.getString("description");
            Double area = doc.getDouble("area");
            Double pricePerNight = doc.getDouble("pricePerNight");
            Long availableRoomsLong = doc.getLong("availableRooms");
            Long capacityLong = doc.getLong("capacity");
            List<String> includedServices = (List<String>) doc.get("includedServices");
            List<String> photoUrls = (List<String>) doc.get("photoUrls");

            // Validar campos requeridos
            if (name == null || area == null || pricePerNight == null ||
                    availableRoomsLong == null || capacityLong == null) {
                Log.w(TAG, "❌ Habitación con campos faltantes: " + doc.getId());
                return null;
            }

            // Preparar datos para el modelo del cliente
            int size = area.intValue(); // Convertir área a size
            String price = "S/" + String.format("%.0f", pricePerNight); // Formatear precio
            int imageResource = getDefaultImageResource(name); // Imagen por defecto según tipo

            if (includedServices == null) {
                includedServices = new ArrayList<>();
            }

            // Crear características basadas en la descripción y tipo de habitación
            List<String> features = createFeaturesFromRoomData(name, description, area, includedServices);

            // Crear habitación del cliente con servicios incluidos
            RoomType clientRoom = new RoomType(
                    name,
                    size,
                    price,
                    imageResource,
                    includedServices, // IDs de servicios incluidos
                    description != null ? description : "",
                    features
            );

            Log.d(TAG, "🔄 Habitación convertida: " + name +
                    " - Área: " + area + "m² - Precio: " + price +
                    " - Servicios: " + includedServices.size());

            return clientRoom;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error en conversión: " + e.getMessage());
            return null;
        }
    }

    /**
     * Crea características basadas en los datos de la habitación
     */
    private List<String> createFeaturesFromRoomData(String name, String description, Double area, List<String> services) {
        List<String> features = new ArrayList<>();

        // Características básicas según el área
        if (area != null) {
            if (area >= 70) {
                features.add("Suite amplia de " + String.format("%.0f", area) + "m²");
                features.add("Sala de estar separada");
                features.add("TV de 65 pulgadas");
            } else if (area >= 50) {
                features.add("Suite de " + String.format("%.0f", area) + "m²");
                features.add("Área de estar independiente");
                features.add("TV de 50 pulgadas");
            } else if (area >= 40) {
                features.add("Habitación espaciosa de " + String.format("%.0f", area) + "m²");
                features.add("TV de 42 pulgadas");
            } else {
                features.add("Habitación de " + String.format("%.0f", area) + "m²");
                features.add("TV de 32 pulgadas");
            }
        }

        // Características básicas estándar
        features.add("Aire acondicionado");
        features.add("Baño privado");

        // Características específicas según servicios incluidos
        if (services != null) {
            for (String serviceId : services) {
                switch (serviceId) {
                    case "minibar":
                        features.add("Minibar incluido");
                        break;
                    case "room_service":
                        features.add("Room service 24/7");
                        break;
                    case "laundry":
                        features.add("Servicio de lavandería");
                        break;
                }
            }
        }

        // Características especiales según el tipo de habitación
        if (name != null) {
            if (name.toLowerCase().contains("suite")) {
                features.add("Vista panorámica");
                if (name.toLowerCase().contains("presidencial")) {
                    features.add("Balcón privado");
                    features.add("Servicio de mayordomía");
                }
            } else if (name.toLowerCase().contains("deluxe")) {
                features.add("Vista a la ciudad");
                features.add("Baño con bañera");
            }
        }

        return features;
    }

    /**
     * Obtiene el recurso de imagen por defecto según el tipo de habitación
     */
    private int getDefaultImageResource(String roomName) {
        // Usar la imagen por defecto de Belmond para todas las habitaciones
        // El usuario puede cambiar esto según sus recursos
        return com.example.proyecto_final_hoteleros.R.drawable.belmond;
    }
}