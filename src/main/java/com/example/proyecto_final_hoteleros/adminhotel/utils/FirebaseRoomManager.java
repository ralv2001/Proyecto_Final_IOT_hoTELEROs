package com.example.proyecto_final_hoteleros.adminhotel.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.proyecto_final_hoteleros.adminhotel.model.RoomType;
import com.example.proyecto_final_hoteleros.utils.AwsFileManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FirebaseRoomManager {
    private static final String TAG = "FirebaseRoomManager";
    private static final String ROOMS_COLLECTION = "hotel_rooms";

    private static FirebaseRoomManager instance;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Context context;
    private AwsFileManager awsFileManager; // ✅ NUEVO para manejo de fotos

    private List<OnRoomsChangedListener> listeners = new ArrayList<>();
    private ListenerRegistration roomsListener;

    public interface OnRoomsChangedListener {
        void onRoomsLoaded(List<RoomType> rooms);
        void onRoomAdded(RoomType room);
        void onRoomUpdated(RoomType room);
        void onRoomDeleted(String roomId);
        void onError(String error);
    }

    public interface RoomCallback {
        void onSuccess(RoomType room);
        void onError(String error);
    }

    public interface RoomsListCallback {
        void onSuccess(List<RoomType> rooms);
        void onError(String error);
    }

    // ✅ NUEVA interfaz para callbacks de subida de fotos
    public interface UploadCallback {
        void onProgress(int percentage);
        void onSuccess();
        void onError(String error);
    }

    private FirebaseRoomManager(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.awsFileManager = new AwsFileManager(context); // ✅ Inicializar AWS manager
    }

    public static synchronized FirebaseRoomManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseRoomManager(context);
        }
        return instance;
    }

    // ========== GESTIÓN DE LISTENERS ==========

    public void addListener(OnRoomsChangedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }

        // Si es el primer listener, iniciar escucha de Firebase
        if (listeners.size() == 1) {
            startListeningToRooms();
        }

        // Forzar carga inicial inmediata
        forceInitialLoad();
    }

    public void removeListener(OnRoomsChangedListener listener) {
        listeners.remove(listener);

        // Si no quedan listeners, parar escucha
        if (listeners.isEmpty() && roomsListener != null) {
            roomsListener.remove();
            roomsListener = null;
        }
    }

    private void forceInitialLoad() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return;

        Log.d(TAG, "🔄 Forzando carga inicial de habitaciones...");

        db.collection(ROOMS_COLLECTION)
                .whereEqualTo("hotelAdminId", currentUserId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        List<RoomType> rooms = new ArrayList<>();

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                RoomType room = documentToRoomType(doc);
                                if (room != null) {
                                    rooms.add(room);
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "Error parseando habitación en carga inicial: " + ex.getMessage());
                            }
                        }

                        Log.d(TAG, "✅ Carga inicial completada: " + rooms.size() + " habitaciones");
                        notifyRoomsLoaded(rooms);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error en carga inicial: " + e.getMessage());
                    notifyError("Error cargando habitaciones: " + e.getMessage());
                });
    }

    private void startListeningToRooms() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return;

        Log.d(TAG, "Iniciando escucha de habitaciones para usuario: " + currentUserId);

        roomsListener = db.collection(ROOMS_COLLECTION)
                .whereEqualTo("hotelAdminId", currentUserId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error escuchando habitaciones: " + e.getMessage());
                        notifyError("Error cargando habitaciones: " + e.getMessage());
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<RoomType> rooms = new ArrayList<>();

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                RoomType room = documentToRoomType(doc);
                                if (room != null) {
                                    rooms.add(room);
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "Error parseando habitación: " + ex.getMessage());
                            }
                        }

                        Log.d(TAG, "🔄 Habitaciones actualizadas desde Firebase: " + rooms.size());
                        notifyRoomsLoaded(rooms);
                    }
                });
    }

    // ========== CRUD OPERATIONS CON FOTOS ==========

    // ✅ NUEVO método para crear habitación con fotos
    public void createRoom(RoomType roomType, List<Uri> photoUris, RoomCallback callback) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        Log.d(TAG, "Creando habitación: " + roomType.getName() + " con " +
                (photoUris != null ? photoUris.size() : 0) + " fotos");

        // Si hay fotos, subirlas primero
        if (photoUris != null && !photoUris.isEmpty()) {
            uploadRoomPhotos(roomType, photoUris, new UploadCallback() {
                @Override
                public void onProgress(int percentage) {
                    // Mostrar progreso si es necesario
                }

                @Override
                public void onSuccess() {
                    // Fotos subidas, ahora guardar habitación
                    saveRoomToFirestore(roomType, currentUserId, callback);
                }

                @Override
                public void onError(String error) {
                    callback.onError("Error subiendo fotos: " + error);
                }
            });
        } else {
            // Sin fotos, guardar directamente
            saveRoomToFirestore(roomType, currentUserId, callback);
        }
    }

    // Método original para compatibilidad
    public void createRoom(RoomType roomType, RoomCallback callback) {
        createRoom(roomType, null, callback);
    }

    // ✅ NUEVO método para actualizar habitación con fotos
    public void updateRoom(RoomType roomType, List<Uri> newPhotoUris, RoomCallback callback) {
        if (roomType.getId() == null || roomType.getId().isEmpty()) {
            callback.onError("ID de habitación no válido");
            return;
        }

        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        Log.d(TAG, "Actualizando habitación: " + roomType.getId() + " con " +
                (newPhotoUris != null ? newPhotoUris.size() : 0) + " fotos nuevas");

        // Si hay fotos nuevas, subirlas primero
        if (newPhotoUris != null && !newPhotoUris.isEmpty()) {
            uploadRoomPhotos(roomType, newPhotoUris, new UploadCallback() {
                @Override
                public void onProgress(int percentage) {
                    // Mostrar progreso si es necesario
                }

                @Override
                public void onSuccess() {
                    // Fotos subidas, ahora actualizar habitación
                    updateRoomInFirestore(roomType, currentUserId, callback);
                }

                @Override
                public void onError(String error) {
                    callback.onError("Error subiendo fotos: " + error);
                }
            });
        } else {
            // Sin fotos nuevas, actualizar directamente
            updateRoomInFirestore(roomType, currentUserId, callback);
        }
    }

    // Método original para compatibilidad
    public void updateRoom(RoomType roomType, RoomCallback callback) {
        updateRoom(roomType, null, callback);
    }

    // ========== MÉTODOS AUXILIARES PARA FIREBASE ==========

    private void saveRoomToFirestore(RoomType roomType, String currentUserId, RoomCallback callback) {
        Map<String, Object> roomData = roomTypeToMap(roomType, currentUserId);

        db.collection(ROOMS_COLLECTION)
                .add(roomData)
                .addOnSuccessListener(documentReference -> {
                    String roomId = documentReference.getId();
                    roomType.setId(roomId);

                    Log.d(TAG, "✅ Habitación creada con ID: " + roomId);
                    callback.onSuccess(roomType);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error creando habitación: " + e.getMessage());
                    callback.onError("Error creando habitación: " + e.getMessage());
                });
    }

    private void updateRoomInFirestore(RoomType roomType, String currentUserId, RoomCallback callback) {
        Map<String, Object> roomData = roomTypeToMap(roomType, currentUserId);
        roomData.put("updatedAt", System.currentTimeMillis());

        db.collection(ROOMS_COLLECTION)
                .document(roomType.getId())
                .update(roomData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Habitación actualizada: " + roomType.getId());
                    callback.onSuccess(roomType);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error actualizando habitación: " + e.getMessage());
                    callback.onError("Error actualizando habitación: " + e.getMessage());
                });
    }

    // ========== SUBIDA DE FOTOS ==========

    // ✅ NUEVO método para subir fotos de habitación
    private void uploadRoomPhotos(RoomType roomType, List<Uri> photoUris, UploadCallback callback) {
        if (photoUris == null || photoUris.isEmpty()) {
            callback.onSuccess();
            return;
        }

        // Limitar a máximo 3 fotos
        List<Uri> photosToUpload = photoUris.size() > 3 ?
                photoUris.subList(0, 3) : new ArrayList<>(photoUris);

        Log.d(TAG, "📷 Subiendo " + photosToUpload.size() + " fotos para habitación: " + roomType.getName());

        List<String> uploadedUrls = new ArrayList<>();
        AtomicInteger uploadedCount = new AtomicInteger(0);
        AtomicInteger totalUploads = new AtomicInteger(photosToUpload.size());

        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        for (int i = 0; i < photosToUpload.size(); i++) {
            Uri photoUri = photosToUpload.get(i);
            String folder = "hotel_rooms/" + currentUserId;

            awsFileManager.uploadFile(photoUri, currentUserId, folder, new AwsFileManager.UploadCallback() {
                @Override
                public void onSuccess(AwsFileManager.AwsFileInfo fileInfo) {
                    synchronized (uploadedUrls) {
                        uploadedUrls.add(fileInfo.fileUrl);
                        int completed = uploadedCount.incrementAndGet();

                        // Calcular progreso
                        int progress = (completed * 100) / totalUploads.get();
                        callback.onProgress(progress);

                        Log.d(TAG, "📷 Foto subida (" + completed + "/" + totalUploads.get() + "): " + fileInfo.fileUrl);

                        if (completed == totalUploads.get()) {
                            // Todas las fotos subidas, añadir a las existentes
                            List<String> currentPhotos = roomType.getPhotoUrls();
                            if (currentPhotos == null) {
                                currentPhotos = new ArrayList<>();
                            }

                            // Añadir nuevas fotos pero mantener límite de 3
                            for (String newUrl : uploadedUrls) {
                                if (currentPhotos.size() < 3) {
                                    currentPhotos.add(newUrl);
                                }
                            }

                            roomType.setPhotoUrls(currentPhotos);
                            Log.d(TAG, "✅ Todas las fotos subidas exitosamente: " + uploadedUrls.size());
                            callback.onSuccess();
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error subiendo foto: " + error);
                    callback.onError("Error subiendo foto: " + error);
                }

                @Override
                public void onProgress(int percentage) {
                    // Progreso individual de cada foto
                }
            });
        }
    }

    public void deleteRoom(String roomId, RoomCallback callback) {
        if (roomId == null || roomId.isEmpty()) {
            callback.onError("ID de habitación no válido");
            return;
        }

        Log.d(TAG, "Eliminando habitación: " + roomId);

        db.collection(ROOMS_COLLECTION)
                .document(roomId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Habitación eliminada: " + roomId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error eliminando habitación: " + e.getMessage());
                    callback.onError("Error eliminando habitación: " + e.getMessage());
                });
    }

    public void getAllRooms(RoomsListCallback callback) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        db.collection(ROOMS_COLLECTION)
                .whereEqualTo("hotelAdminId", currentUserId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<RoomType> rooms = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            RoomType room = documentToRoomType(doc);
                            if (room != null) {
                                rooms.add(room);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parseando habitación: " + e.getMessage());
                        }
                    }
                    Log.d(TAG, "✅ " + rooms.size() + " habitaciones obtenidas");
                    callback.onSuccess(rooms);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error obteniendo habitaciones: " + e.getMessage());
                    callback.onError("Error obteniendo habitaciones: " + e.getMessage());
                });
    }

    // ========== MÉTODOS DE CONVERSIÓN ==========

    private Map<String, Object> roomTypeToMap(RoomType roomType, String hotelAdminId) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", roomType.getName());
        map.put("description", roomType.getDescription());
        map.put("area", roomType.getArea());
        map.put("pricePerNight", roomType.getPricePerNight());
        map.put("includedServices", roomType.getIncludedServices());
        map.put("availableRooms", roomType.getAvailableRooms());
        map.put("capacity", roomType.getCapacity());
        map.put("photoUrls", roomType.getPhotoUrls() != null ? roomType.getPhotoUrls() : new ArrayList<>()); // ✅ NUEVO
        map.put("hotelAdminId", hotelAdminId);
        map.put("createdAt", System.currentTimeMillis());
        map.put("active", true);
        return map;
    }

    private RoomType documentToRoomType(DocumentSnapshot doc) {
        try {
            String id = doc.getId();
            String name = doc.getString("name");
            String description = doc.getString("description");
            Double area = doc.getDouble("area");
            Double pricePerNight = doc.getDouble("pricePerNight");
            List<String> includedServices = (List<String>) doc.get("includedServices");
            List<String> photoUrls = (List<String>) doc.get("photoUrls"); // ✅ NUEVO
            Long availableRoomsLong = doc.getLong("availableRooms");
            Long capacityLong = doc.getLong("capacity");

            if (name == null || area == null || pricePerNight == null ||
                    availableRoomsLong == null || capacityLong == null) {
                Log.w(TAG, "Documento de habitación con campos faltantes: " + id);
                return null;
            }

            int availableRooms = availableRoomsLong.intValue();
            int capacity = capacityLong.intValue();

            if (includedServices == null) {
                includedServices = new ArrayList<>();
            }
            if (photoUrls == null) { // ✅ NUEVO
                photoUrls = new ArrayList<>();
            }

            RoomType roomType = new RoomType(
                    id,
                    name,
                    description != null ? description : "",
                    area,
                    pricePerNight,
                    includedServices,
                    availableRooms,
                    capacity,
                    photoUrls // ✅ NUEVO parámetro
            );

            return roomType;

        } catch (Exception e) {
            Log.e(TAG, "Error convirtiendo documento a RoomType: " + e.getMessage());
            return null;
        }
    }

    // ========== MÉTODOS DE NOTIFICACIÓN ==========

    private void notifyRoomsLoaded(List<RoomType> rooms) {
        for (OnRoomsChangedListener listener : listeners) {
            try {
                listener.onRoomsLoaded(rooms);
            } catch (Exception e) {
                Log.e(TAG, "Error notificando rooms loaded: " + e.getMessage());
            }
        }
    }

    private void notifyRoomAdded(RoomType room) {
        for (OnRoomsChangedListener listener : listeners) {
            try {
                listener.onRoomAdded(room);
            } catch (Exception e) {
                Log.e(TAG, "Error notificando room added: " + e.getMessage());
            }
        }
    }

    private void notifyRoomUpdated(RoomType room) {
        for (OnRoomsChangedListener listener : listeners) {
            try {
                listener.onRoomUpdated(room);
            } catch (Exception e) {
                Log.e(TAG, "Error notificando room updated: " + e.getMessage());
            }
        }
    }

    private void notifyRoomDeleted(String roomId) {
        for (OnRoomsChangedListener listener : listeners) {
            try {
                listener.onRoomDeleted(roomId);
            } catch (Exception e) {
                Log.e(TAG, "Error notificando room deleted: " + e.getMessage());
            }
        }
    }

    private void notifyError(String error) {
        for (OnRoomsChangedListener listener : listeners) {
            try {
                listener.onError(error);
            } catch (Exception e) {
                Log.e(TAG, "Error notificando error: " + e.getMessage());
            }
        }
    }

    // ========== UTILIDADES ==========

    private String getCurrentUserId() {
        FirebaseUser currentUser = auth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    public void cleanup() {
        if (roomsListener != null) {
            roomsListener.remove();
            roomsListener = null;
        }
        listeners.clear();
    }
}