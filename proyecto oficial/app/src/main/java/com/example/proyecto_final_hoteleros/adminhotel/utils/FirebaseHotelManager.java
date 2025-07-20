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

        // ✅ USAR CONSTRUCTOR VACÍO Y SETTERS
        HotelProfile newHotel = new HotelProfile();
        newHotel.setHotelAdminId(currentUserId);
        newHotel.setName(name);
        newHotel.setAddress(address);

        // Inicializar ubicación vacía
        newHotel.setLocationName("");
        newHotel.setFullAddress(address); // Usar address como fallback inicial
        newHotel.setLatitude(0.0);
        newHotel.setLongitude(0.0);
        newHotel.setDepartamento("");
        newHotel.setProvincia("");
        newHotel.setDistrito("");

        newHotel.setPhotoUrls(new ArrayList<>());
        newHotel.setActive(false);
        newHotel.setCreatedAt(System.currentTimeMillis());
        newHotel.setActivatedAt(null);

        Map<String, Object> hotelMap = hotelProfileToMapWithLocation(newHotel, currentUserId);

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
            if (!doc.exists()) {
                return null;
            }

            HotelProfile hotel = new HotelProfile();

            // Datos básicos
            hotel.setId(doc.getId());
            hotel.setHotelAdminId(doc.getString("hotelAdminId"));
            hotel.setName(doc.getString("name"));
            hotel.setAddress(doc.getString("address"));

            // ✅ DATOS DE UBICACIÓN
            hotel.setLocationName(doc.getString("locationName"));
            hotel.setFullAddress(doc.getString("fullAddress"));

            // Coordenadas con valores por defecto
            hotel.setLatitude(doc.getDouble("latitude") != null ? doc.getDouble("latitude") : 0.0);
            hotel.setLongitude(doc.getDouble("longitude") != null ? doc.getDouble("longitude") : 0.0);

            hotel.setDepartamento(doc.getString("departamento"));
            hotel.setProvincia(doc.getString("provincia"));
            hotel.setDistrito(doc.getString("distrito"));

            // Datos del hotel
            List<String> photoUrls = (List<String>) doc.get("photoUrls");
            hotel.setPhotoUrls(photoUrls != null ? photoUrls : new ArrayList<>());

            hotel.setActive(Boolean.TRUE.equals(doc.getBoolean("isActive")));
            hotel.setCreatedAt(doc.getLong("createdAt") != null ? doc.getLong("createdAt") : System.currentTimeMillis());
            hotel.setActivatedAt(doc.getLong("activatedAt"));

            return hotel;

        } catch (Exception e) {
            Log.e(TAG, "Error convirtiendo documento a HotelProfile: " + e.getMessage());
            return null;
        }
    }

    /**
     * ✅ NUEVO: Busca hoteles por ubicación (para búsquedas geográficas)
     */
    public void findHotelsNearLocation(double latitude, double longitude, double radiusKm, HotelsCallback callback) {
        // Para implementar búsqueda geográfica, necesitarías usar GeoFirestore
        // Por ahora, implementamos una búsqueda básica por departamento/provincia

        Log.d(TAG, "🔍 Buscando hoteles cerca de: " + latitude + ", " + longitude);

        db.collection(HOTELS_COLLECTION)
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<HotelProfile> nearbyHotels = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        HotelProfile hotel = documentToHotelProfile(doc);
                        if (hotel != null && hotel.hasValidLocation()) {
                            double distance = hotel.getDistanceTo(latitude, longitude);
                            if (distance <= radiusKm) {
                                nearbyHotels.add(hotel);
                            }
                        }
                    }

                    Log.d(TAG, "✅ Encontrados " + nearbyHotels.size() + " hoteles cerca de la ubicación");
                    callback.onSuccess(nearbyHotels);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error buscando hoteles por ubicación: " + e.getMessage());
                    callback.onError("Error en búsqueda por ubicación: " + e.getMessage());
                });
    }

    public interface HotelsCallback {
        void onSuccess(List<HotelProfile> hotels);
        void onError(String error);
    }

    private HotelProfile createEmptyHotel() {
        HotelProfile emptyHotel = new HotelProfile();
        emptyHotel.setName("");
        emptyHotel.setAddress("");

        // ✅ INICIALIZAR CAMPOS DE UBICACIÓN
        emptyHotel.setLocationName("");
        emptyHotel.setFullAddress("");
        emptyHotel.setLatitude(0.0);
        emptyHotel.setLongitude(0.0);
        emptyHotel.setDepartamento("");
        emptyHotel.setProvincia("");
        emptyHotel.setDistrito("");

        emptyHotel.setPhotoUrls(new ArrayList<>());
        emptyHotel.setActive(false);
        emptyHotel.setCreatedAt(System.currentTimeMillis());
        emptyHotel.setActivatedAt(null);

        return emptyHotel;
    }
    /**
     * ✅ NUEVO: Obtiene hoteles por departamento
     */
    public void getHotelsByDepartamento(String departamento, HotelsCallback callback) {
        db.collection(HOTELS_COLLECTION)
                .whereEqualTo("departamento", departamento)
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<HotelProfile> hotels = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        HotelProfile hotel = documentToHotelProfile(doc);
                        if (hotel != null) {
                            hotels.add(hotel);
                        }
                    }
                    callback.onSuccess(hotels);
                })
                .addOnFailureListener(e -> callback.onError("Error buscando por departamento: " + e.getMessage()));
    }

    /**
     * ✅ NUEVO: Obtiene hoteles por provincia
     */
    public void getHotelsByProvincia(String provincia, HotelsCallback callback) {
        db.collection(HOTELS_COLLECTION)
                .whereEqualTo("provincia", provincia)
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<HotelProfile> hotels = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        HotelProfile hotel = documentToHotelProfile(doc);
                        if (hotel != null) {
                            hotels.add(hotel);
                        }
                    }
                    callback.onSuccess(hotels);
                })
                .addOnFailureListener(e -> callback.onError("Error buscando por provincia: " + e.getMessage()));
    }
    /**
     * ✅ NUEVO: Guarda el perfil del hotel con información de ubicación completa
     */
    public void saveHotelProfileWithLocation(String name, String address,
                                             String locationName, String fullAddress,
                                             double latitude, double longitude,
                                             List<Uri> photoUris, HotelCallback callback) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        Log.d(TAG, "💾 Guardando perfil del hotel con ubicación: " + name +
                " en " + locationName + " (" + latitude + ", " + longitude + ")");

        // Primero verificar si ya existe un hotel
        db.collection(HOTELS_COLLECTION)
                .whereEqualTo("hotelAdminId", currentUserId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Hotel existe - actualizar con ubicación
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        updateExistingHotelWithLocation(doc.getId(), name, address,
                                locationName, fullAddress, latitude, longitude, photoUris, callback);
                    } else {
                        // Hotel no existe - crear nuevo con ubicación
                        createNewHotelWithLocation(name, address,
                                locationName, fullAddress, latitude, longitude, photoUris, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error verificando hotel existente: " + e.getMessage());
                    callback.onError("Error verificando hotel: " + e.getMessage());
                });
    }

    /**
     * ✅ NUEVO: Crea un nuevo hotel con información de ubicación
     */
    private void createNewHotelWithLocation(String name, String address,
                                            String locationName, String fullAddress,
                                            double latitude, double longitude,
                                            List<Uri> photoUris, HotelCallback callback) {
        String currentUserId = getCurrentUserId();

        // Crear hotel con información de ubicación completa
        HotelProfile newHotel = new HotelProfile();
        newHotel.setHotelAdminId(currentUserId);
        newHotel.setName(name);
        newHotel.setAddress(address);

        // ✅ CONFIGURAR UBICACIÓN
        newHotel.setLocationName(locationName);
        newHotel.setFullAddress(fullAddress);
        newHotel.setLatitude(latitude);
        newHotel.setLongitude(longitude);
        // Los componentes de ubicación se parsearán automáticamente en HotelProfile

        newHotel.setPhotoUrls(new ArrayList<>());
        newHotel.setActive(false);
        newHotel.setCreatedAt(System.currentTimeMillis());
        newHotel.setActivatedAt(null);

        Map<String, Object> hotelMap = hotelProfileToMapWithLocation(newHotel, currentUserId);

        db.collection(HOTELS_COLLECTION)
                .add(hotelMap)
                .addOnSuccessListener(documentReference -> {
                    String hotelId = documentReference.getId();
                    newHotel.setId(hotelId);

                    Log.d(TAG, "✅ Hotel con ubicación creado exitosamente: " + hotelId);

                    // Si hay fotos, subirlas
                    if (photoUris != null && !photoUris.isEmpty()) {
                        uploadPhotosAndUpdateHotel(hotelId, photoUris, newHotel, callback);
                    } else {
                        callback.onSuccess(newHotel);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creando hotel: " + e.getMessage());
                    callback.onError("Error creando hotel: " + e.getMessage());
                });
    }

    /**
     * ✅ NUEVO: Actualiza un hotel existente con información de ubicación
     */
    private void updateExistingHotelWithLocation(String hotelId, String name, String address,
                                                 String locationName, String fullAddress,
                                                 double latitude, double longitude,
                                                 List<Uri> photoUris, HotelCallback callback) {
        // Crear map con datos actualizados incluyendo ubicación
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("address", address);

        // ✅ CAMPOS DE UBICACIÓN
        updates.put("locationName", locationName);
        updates.put("fullAddress", fullAddress);
        updates.put("latitude", latitude);
        updates.put("longitude", longitude);

        // Parsear componentes de ubicación
        parseAndAddLocationComponents(fullAddress, updates);

        db.collection(HOTELS_COLLECTION)
                .document(hotelId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Hotel actualizado con ubicación exitosamente: " + hotelId);

                    // Obtener hotel actualizado
                    db.collection(HOTELS_COLLECTION)
                            .document(hotelId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                HotelProfile updatedHotel = documentToHotelProfile(documentSnapshot);

                                if (updatedHotel != null) {
                                    // Si hay fotos nuevas, subirlas
                                    if (photoUris != null && !photoUris.isEmpty()) {
                                        uploadPhotosAndUpdateHotel(hotelId, photoUris, updatedHotel, callback);
                                    } else {
                                        callback.onSuccess(updatedHotel);
                                    }
                                } else {
                                    callback.onError("Error procesando hotel actualizado");
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando hotel: " + e.getMessage());
                    callback.onError("Error actualizando hotel: " + e.getMessage());
                });
    }

    /**
     * ✅ NUEVO: Parsea y agrega componentes de ubicación al Map
     */
    private void parseAndAddLocationComponents(String fullAddress, Map<String, Object> updates) {
        if (fullAddress == null || fullAddress.trim().isEmpty()) {
            updates.put("departamento", "");
            updates.put("provincia", "");
            updates.put("distrito", "");
            return;
        }

        // Lógica para extraer departamento, provincia, distrito
        String[] parts = fullAddress.split(", ");
        if (parts.length >= 3) {
            updates.put("distrito", parts[parts.length - 3].trim());
            updates.put("provincia", parts[parts.length - 2].trim());

            if (parts.length >= 4) {
                updates.put("departamento", parts[parts.length - 3].trim());
            } else {
                updates.put("departamento", parts[parts.length - 2].trim()); // Fallback
            }
        } else {
            updates.put("departamento", "");
            updates.put("provincia", "");
            updates.put("distrito", "");
        }
    }
    /**
     * ✅ ACTUALIZADO: Convierte HotelProfile a Map incluyendo ubicación
     */
    private Map<String, Object> hotelProfileToMapWithLocation(HotelProfile hotel, String currentUserId) {
        Map<String, Object> map = new HashMap<>();

        // Datos básicos
        map.put("hotelAdminId", currentUserId);
        map.put("name", hotel.getName() != null ? hotel.getName() : "");
        map.put("address", hotel.getAddress() != null ? hotel.getAddress() : "");

        // ✅ DATOS DE UBICACIÓN
        map.put("locationName", hotel.getLocationName() != null ? hotel.getLocationName() : "");
        map.put("fullAddress", hotel.getFullAddress() != null ? hotel.getFullAddress() : "");
        map.put("latitude", hotel.getLatitude());
        map.put("longitude", hotel.getLongitude());
        map.put("departamento", hotel.getDepartamento() != null ? hotel.getDepartamento() : "");
        map.put("provincia", hotel.getProvincia() != null ? hotel.getProvincia() : "");
        map.put("distrito", hotel.getDistrito() != null ? hotel.getDistrito() : "");

        // Datos del hotel
        map.put("photoUrls", hotel.getPhotoUrls() != null ? hotel.getPhotoUrls() : new ArrayList<>());
        map.put("isActive", hotel.isActive());
        map.put("createdAt", hotel.getCreatedAt());
        map.put("activatedAt", hotel.getActivatedAt());

        return map;
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