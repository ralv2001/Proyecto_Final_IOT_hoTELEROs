package com.example.proyecto_final_hoteleros.adminhotel.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.proyecto_final_hoteleros.adminhotel.model.HotelProfile;
import com.example.proyecto_final_hoteleros.utils.AwsFileManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FirebaseHotelManager {
    private static final String TAG = "FirebaseHotelManager";
    private static final String HOTELS_COLLECTION = "hotels";

    private static FirebaseHotelManager instance;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Context context;
    private AwsFileManager awsFileManager;

    private List<OnHotelChangedListener> listeners = new ArrayList<>();
    private ListenerRegistration hotelListener;

    public interface OnHotelChangedListener {
        void onHotelLoaded(HotelProfile hotel);
        void onHotelActivated(boolean isActive);
        void onError(String error);
    }

    public interface HotelCallback {
        void onSuccess(HotelProfile hotel);
        void onError(String error);
    }

    public interface ActivationCallback {
        void onSuccess(boolean isActive);
        void onError(String error);
    }

    private FirebaseHotelManager(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.awsFileManager = new AwsFileManager(context);
    }

    public static synchronized FirebaseHotelManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseHotelManager(context);
        }
        return instance;
    }

    // ========== GESTIÓN DE LISTENERS ==========

    public void addListener(OnHotelChangedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }

        // Si es el primer listener, iniciar escucha de Firebase
        if (listeners.size() == 1) {
            startListeningToHotel();
        }

        // Forzar carga inicial inmediata
        forceInitialLoad();
    }

    public void removeListener(OnHotelChangedListener listener) {
        listeners.remove(listener);

        // Si no quedan listeners, parar escucha
        if (listeners.isEmpty() && hotelListener != null) {
            hotelListener.remove();
            hotelListener = null;
        }
    }

    private void startListeningToHotel() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            notifyError("Usuario no autenticado");
            return;
        }

        Log.d(TAG, "🔄 Iniciando escucha en tiempo real para hotel del admin: " + currentUserId);

        hotelListener = db.collection(HOTELS_COLLECTION)
                .whereEqualTo("hotelAdminId", currentUserId)
                .limit(1)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error escuchando hotel: " + e.getMessage());
                        notifyError("Error cargando hotel: " + e.getMessage());
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        try {
                            HotelProfile hotel = documentToHotelProfile(doc);
                            if (hotel != null) {
                                Log.d(TAG, "🔄 Hotel actualizado desde Firebase: " + hotel.getName());
                                notifyHotelLoaded(hotel);
                                notifyHotelActivated(hotel.isActive());
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, "Error parseando hotel: " + ex.getMessage());
                            notifyError("Error procesando hotel: " + ex.getMessage());
                        }
                    } else {
                        // No existe hotel para este admin
                        Log.d(TAG, "📭 No existe hotel para este admin");
                        HotelProfile emptyHotel = createEmptyHotel();
                        notifyHotelLoaded(emptyHotel);
                        notifyHotelActivated(false);
                    }
                });
    }

    private void forceInitialLoad() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return;

        Log.d(TAG, "🔄 Forzando carga inicial de hotel...");

        db.collection(HOTELS_COLLECTION)
                .whereEqualTo("hotelAdminId", currentUserId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        try {
                            HotelProfile hotel = documentToHotelProfile(doc);
                            if (hotel != null) {
                                notifyHotelLoaded(hotel);
                                notifyHotelActivated(hotel.isActive());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error en carga inicial: " + e.getMessage());
                            notifyError("Error cargando hotel: " + e.getMessage());
                        }
                    } else {
                        // Crear hotel vacío inicial
                        HotelProfile emptyHotel = createEmptyHotel();
                        notifyHotelLoaded(emptyHotel);
                        notifyHotelActivated(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error en carga inicial: " + e.getMessage());
                    notifyError("Error cargando hotel: " + e.getMessage());
                });
    }

    // ========== OPERACIONES CRUD ==========

    public void saveHotelProfile(String name, String address, List<Uri> photoUris, HotelCallback callback) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        Log.d(TAG, "💾 Guardando perfil del hotel: " + name + " con " +
                (photoUris != null ? photoUris.size() : 0) + " fotos");

        // Primero verificar si ya existe un hotel
        db.collection(HOTELS_COLLECTION)
                .whereEqualTo("hotelAdminId", currentUserId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Hotel existe - actualizar
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        updateExistingHotel(doc.getId(), name, address, photoUris, callback);
                    } else {
                        // Hotel no existe - crear nuevo
                        createNewHotel(name, address, photoUris, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error verificando hotel existente: " + e.getMessage());
                    callback.onError("Error verificando hotel: " + e.getMessage());
                });
    }

    private void createNewHotel(String name, String address, List<Uri> photoUris, HotelCallback callback) {
        String currentUserId = getCurrentUserId();

        // Crear hotel con fotos vacías inicialmente
        HotelProfile newHotel = new HotelProfile(
                null, // ID será generado por Firebase
                currentUserId,
                name,
                address,
                new ArrayList<>(), // photoUrls vacías inicialmente
                false, // No activo inicialmente
                System.currentTimeMillis(),
                null
        );

        Map<String, Object> hotelMap = hotelProfileToMap(newHotel, currentUserId);

        db.collection(HOTELS_COLLECTION)
                .add(hotelMap)
                .addOnSuccessListener(documentReference -> {
                    String hotelId = documentReference.getId();
                    newHotel.setId(hotelId);

                    Log.d(TAG, "✅ Hotel creado exitosamente: " + hotelId);

                    // Si hay fotos, subirlas
                    if (photoUris != null && !photoUris.isEmpty()) {
                        uploadPhotosAndUpdateHotel(hotelId, photoUris, newHotel, callback);
                    } else {
                        callback.onSuccess(newHotel);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error creando hotel: " + e.getMessage());
                    callback.onError("Error creando hotel: " + e.getMessage());
                });
    }

    private void updateExistingHotel(String hotelId, String name, String address, List<Uri> photoUris, HotelCallback callback) {
        // Obtener hotel actual para preservar datos
        db.collection(HOTELS_COLLECTION)
                .document(hotelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    try {
                        HotelProfile currentHotel = documentToHotelProfile(documentSnapshot);
                        if (currentHotel == null) {
                            callback.onError("Hotel no encontrado");
                            return;
                        }

                        // Actualizar solo los campos modificables
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("name", name);
                        updates.put("address", address);
                        updates.put("updatedAt", System.currentTimeMillis());

                        db.collection(HOTELS_COLLECTION)
                                .document(hotelId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "✅ Hotel actualizado exitosamente: " + hotelId);

                                    // Actualizar hotel en memoria
                                    currentHotel.setName(name);
                                    currentHotel.setAddress(address);

                                    // Si hay fotos nuevas, subirlas
                                    if (photoUris != null && !photoUris.isEmpty()) {
                                        uploadPhotosAndUpdateHotel(hotelId, photoUris, currentHotel, callback);
                                    } else {
                                        callback.onSuccess(currentHotel);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "❌ Error actualizando hotel: " + e.getMessage());
                                    callback.onError("Error actualizando hotel: " + e.getMessage());
                                });

                    } catch (Exception e) {
                        Log.e(TAG, "Error procesando hotel existente: " + e.getMessage());
                        callback.onError("Error procesando hotel: " + e.getMessage());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo hotel existente: " + e.getMessage());
                    callback.onError("Error obteniendo hotel: " + e.getMessage());
                });
    }

    private void uploadPhotosAndUpdateHotel(String hotelId, List<Uri> photoUris, HotelProfile hotel, HotelCallback callback) {
        Log.d(TAG, "📷 Subiendo " + photoUris.size() + " fotos para hotel: " + hotelId);

        AtomicInteger uploadedCount = new AtomicInteger(0);
        List<String> uploadedUrls = new ArrayList<>(hotel.getPhotoUrls()); // Preservar fotos existentes

        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        for (Uri photoUri : photoUris) {
            String folder = "hotels/" + hotelId + "/photos";

            // ✅ CORREGIDO: Usar el método uploadFile con los parámetros correctos
            awsFileManager.uploadFile(photoUri, currentUserId, folder, new AwsFileManager.UploadCallback() {
                @Override
                public void onProgress(int progress) {
                    // Opcional: mostrar progreso
                }

                @Override
                public void onSuccess(AwsFileManager.AwsFileInfo fileInfo) {
                    uploadedUrls.add(fileInfo.fileUrl);
                    int uploaded = uploadedCount.incrementAndGet();

                    Log.d(TAG, "📷 Foto subida " + uploaded + "/" + photoUris.size() + ": " + fileInfo.fileUrl);

                    if (uploaded == photoUris.size()) {
                        // Todas las fotos subidas, actualizar hotel
                        updateHotelPhotos(hotelId, uploadedUrls, hotel, callback);
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error subiendo foto: " + error);
                    // Continuar con las demás fotos
                    int uploaded = uploadedCount.incrementAndGet();
                    if (uploaded == photoUris.size()) {
                        updateHotelPhotos(hotelId, uploadedUrls, hotel, callback);
                    }
                }
            });
        }
    }

    private void updateHotelPhotos(String hotelId, List<String> photoUrls, HotelProfile hotel, HotelCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("photoUrls", photoUrls);
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection(HOTELS_COLLECTION)
                .document(hotelId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Fotos del hotel actualizadas: " + photoUrls.size() + " fotos");
                    hotel.setPhotoUrls(photoUrls);
                    callback.onSuccess(hotel);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error actualizando fotos del hotel: " + e.getMessage());
                    callback.onError("Error actualizando fotos: " + e.getMessage());
                });
    }

    // ========== ACTIVACIÓN DEL HOTEL ==========

    public void activateHotel(ActivationCallback callback) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        Log.d(TAG, "🚀 Activando hotel para publicación...");

        // Buscar hotel del admin actual
        db.collection(HOTELS_COLLECTION)
                .whereEqualTo("hotelAdminId", currentUserId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onError("No se encontró hotel para activar");
                        return;
                    }

                    DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                    String hotelId = doc.getId();

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("isActive", true);
                    updates.put("activatedAt", System.currentTimeMillis());
                    updates.put("updatedAt", System.currentTimeMillis());

                    db.collection(HOTELS_COLLECTION)
                            .document(hotelId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "✅ Hotel activado exitosamente: " + hotelId);
                                callback.onSuccess(true);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "❌ Error activando hotel: " + e.getMessage());
                                callback.onError("Error activando hotel: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error buscando hotel para activar: " + e.getMessage());
                    callback.onError("Error buscando hotel: " + e.getMessage());
                });
    }

    public void deactivateHotel(ActivationCallback callback) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        Log.d(TAG, "⏸️ Desactivando hotel...");

        db.collection(HOTELS_COLLECTION)
                .whereEqualTo("hotelAdminId", currentUserId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onError("No se encontró hotel para desactivar");
                        return;
                    }

                    DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                    String hotelId = doc.getId();

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("isActive", false);
                    updates.put("deactivatedAt", System.currentTimeMillis());
                    updates.put("updatedAt", System.currentTimeMillis());

                    db.collection(HOTELS_COLLECTION)
                            .document(hotelId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "✅ Hotel desactivado exitosamente: " + hotelId);
                                callback.onSuccess(false);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "❌ Error desactivando hotel: " + e.getMessage());
                                callback.onError("Error desactivando hotel: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error buscando hotel para desactivar: " + e.getMessage());
                    callback.onError("Error buscando hotel: " + e.getMessage());
                });
    }

    // ========== MÉTODOS DE CONVERSIÓN ==========

    private Map<String, Object> hotelProfileToMap(HotelProfile hotel, String hotelAdminId) {
        Map<String, Object> map = new HashMap<>();
        map.put("hotelAdminId", hotelAdminId);
        map.put("name", hotel.getName());
        map.put("address", hotel.getAddress());
        map.put("photoUrls", hotel.getPhotoUrls() != null ? hotel.getPhotoUrls() : new ArrayList<>());
        map.put("isActive", hotel.isActive());
        map.put("createdAt", hotel.getCreatedAt());
        map.put("updatedAt", System.currentTimeMillis());
        return map;
    }

    private HotelProfile documentToHotelProfile(DocumentSnapshot doc) {
        try {
            String id = doc.getId();
            String hotelAdminId = doc.getString("hotelAdminId");
            String name = doc.getString("name");
            String address = doc.getString("address");
            List<String> photoUrls = (List<String>) doc.get("photoUrls");
            Boolean isActive = doc.getBoolean("isActive");
            Long createdAt = doc.getLong("createdAt");
            Long activatedAt = doc.getLong("activatedAt");

            if (name == null) {
                name = "";
            }
            if (address == null) {
                address = "";
            }
            if (photoUrls == null) {
                photoUrls = new ArrayList<>();
            }
            if (isActive == null) {
                isActive = false;
            }
            if (createdAt == null) {
                createdAt = System.currentTimeMillis();
            }

            return new HotelProfile(
                    id,
                    hotelAdminId,
                    name,
                    address,
                    photoUrls,
                    isActive,
                    createdAt,
                    activatedAt
            );

        } catch (Exception e) {
            Log.e(TAG, "Error convirtiendo documento a HotelProfile: " + e.getMessage());
            return null;
        }
    }

    private HotelProfile createEmptyHotel() {
        String currentUserId = getCurrentUserId();
        return new HotelProfile(
                null,
                currentUserId,
                "",
                "",
                new ArrayList<>(),
                false,
                System.currentTimeMillis(),
                null
        );
    }

    // ========== MÉTODOS DE NOTIFICACIÓN ==========

    private void notifyHotelLoaded(HotelProfile hotel) {
        for (OnHotelChangedListener listener : listeners) {
            try {
                listener.onHotelLoaded(hotel);
            } catch (Exception e) {
                Log.e(TAG, "Error notificando hotel loaded: " + e.getMessage());
            }
        }
    }

    private void notifyHotelActivated(boolean isActive) {
        for (OnHotelChangedListener listener : listeners) {
            try {
                listener.onHotelActivated(isActive);
            } catch (Exception e) {
                Log.e(TAG, "Error notificando hotel activated: " + e.getMessage());
            }
        }
    }

    private void notifyError(String error) {
        for (OnHotelChangedListener listener : listeners) {
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
        if (hotelListener != null) {
            hotelListener.remove();
            hotelListener = null;
        }
        listeners.clear();
    }
}