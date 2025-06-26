package com.example.proyecto_final_hoteleros.adminhotel.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceModel;
import com.example.proyecto_final_hoteleros.utils.AwsFileManager;
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;
import com.example.proyecto_final_hoteleros.utils.UniqueIdGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FirebaseServiceManager {

    private static final String TAG = "FirebaseServiceManager";
    private static final String SERVICES_COLLECTION = "hotel_services";

    private static FirebaseServiceManager instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final Context context;
    private final AwsFileManager awsFileManager;
    private final UniqueIdGenerator idGenerator;

    // Listeners para cambios en tiempo real
    private final List<OnServicesChangedListener> listeners = new ArrayList<>();
    private ListenerRegistration servicesListener;

    public interface OnServicesChangedListener {
        void onBasicServicesUpdated(List<HotelServiceModel> basicServices);
        void onAllServicesUpdated(List<HotelServiceModel> allServices);
        void onError(String error);
    }

    public interface ServiceCallback {
        void onSuccess(HotelServiceModel service);
        void onError(String error);
    }

    public interface ServicesListCallback {
        void onSuccess(List<HotelServiceModel> services);
        void onError(String error);
    }

    public interface UploadCallback {
        void onProgress(int percentage);
        void onSuccess();
        void onError(String error);
    }

    private FirebaseServiceManager(Context context) {
        this.context = context.getApplicationContext();
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.awsFileManager = new AwsFileManager(context);
        this.idGenerator = UniqueIdGenerator.getInstance(context);
    }

    public static synchronized FirebaseServiceManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseServiceManager(context);
        }
        return instance;
    }

    // ========== GESTIÓN DE LISTENERS ==========

    public void addListener(OnServicesChangedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }

        // Si es el primer listener, iniciar escucha de Firebase
        if (listeners.size() == 1) {
            startListeningToServices();
        }

        // ✅ SOLUCIÓN: Forzar carga inicial inmediata
        forceInitialLoad();
    }
    private void forceInitialLoad() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return;

        Log.d(TAG, "🔄 Forzando carga inicial de servicios...");

        // Obtener servicios existentes de Firebase inmediatamente
        db.collection(SERVICES_COLLECTION)
                .whereEqualTo("hotelAdminId", currentUserId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        List<HotelServiceModel> allServices = new ArrayList<>();
                        List<HotelServiceModel> basicServices = new ArrayList<>();

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                HotelServiceModel service = HotelServiceModel.fromMap(doc.getData(), doc.getId());
                                allServices.add(service);

                                if ("basic".equals(service.getServiceType())) {
                                    basicServices.add(service);
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "Error parseando servicio en carga inicial: " + ex.getMessage());
                            }
                        }

                        Log.d(TAG, "✅ Carga inicial completada: " + allServices.size() + " servicios (Básicos: " + basicServices.size() + ")");

                        // Notificar inmediatamente a los listeners
                        notifyAllServicesUpdated(allServices);
                        notifyBasicServicesUpdated(basicServices);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error en carga inicial: " + e.getMessage());
                    notifyError("Error cargando servicios: " + e.getMessage());
                });
    }
    public void removeListener(OnServicesChangedListener listener) {
        listeners.remove(listener);

        // Si no quedan listeners, parar escucha
        if (listeners.isEmpty() && servicesListener != null) {
            servicesListener.remove();
            servicesListener = null;
        }
    }

    private void startListeningToServices() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return;

        Log.d(TAG, "Iniciando escucha de servicios para usuario: " + currentUserId);

        servicesListener = db.collection(SERVICES_COLLECTION)
                .whereEqualTo("hotelAdminId", currentUserId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error escuchando servicios: " + e.getMessage());
                        notifyError("Error cargando servicios: " + e.getMessage());
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<HotelServiceModel> allServices = new ArrayList<>();
                        List<HotelServiceModel> basicServices = new ArrayList<>();

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                HotelServiceModel service = HotelServiceModel.fromMap(doc.getData(), doc.getId());
                                allServices.add(service);

                                if ("basic".equals(service.getServiceType())) {
                                    basicServices.add(service);
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "Error parseando servicio: " + ex.getMessage());
                            }
                        }

                        Log.d(TAG, "Servicios cargados: " + allServices.size() + " (Básicos: " + basicServices.size() + ")");
                        notifyAllServicesUpdated(allServices);
                        notifyBasicServicesUpdated(basicServices);
                    }
                });
    }

    private void notifyBasicServicesUpdated(List<HotelServiceModel> basicServices) {
        for (OnServicesChangedListener listener : listeners) {
            try {
                listener.onBasicServicesUpdated(basicServices);
            } catch (Exception e) {
                Log.e(TAG, "Error notificando listener: " + e.getMessage());
            }
        }
    }

    private void notifyAllServicesUpdated(List<HotelServiceModel> allServices) {
        for (OnServicesChangedListener listener : listeners) {
            try {
                listener.onAllServicesUpdated(allServices);
            } catch (Exception e) {
                Log.e(TAG, "Error notificando listener: " + e.getMessage());
            }
        }
    }

    private void notifyError(String error) {
        for (OnServicesChangedListener listener : listeners) {
            try {
                listener.onError(error);
            } catch (Exception e) {
                Log.e(TAG, "Error notificando error: " + e.getMessage());
            }
        }
    }

    // ========== OPERACIONES CRUD ==========

    public void createService(HotelServiceModel service, List<Uri> photoUris, ServiceCallback callback) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        Log.d(TAG, "Creando servicio: " + service.getName());
        service.setHotelAdminId(currentUserId);

        // Si hay fotos, subirlas primero
        if (photoUris != null && !photoUris.isEmpty()) {
            uploadServicePhotos(service, photoUris, new UploadCallback() {
                @Override
                public void onProgress(int percentage) {
                    // Mostrar progreso si es necesario
                }

                @Override
                public void onSuccess() {
                    // Fotos subidas, ahora guardar servicio
                    saveServiceToFirestore(service, callback);
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error subiendo fotos: " + error);
                    callback.onError("Error subiendo fotos: " + error);
                }
            });
        } else {
            // Sin fotos, guardar directamente
            saveServiceToFirestore(service, callback);
        }
    }

    private void saveServiceToFirestore(HotelServiceModel service, ServiceCallback callback) {
        db.collection(SERVICES_COLLECTION)
                .add(service.toMap())
                .addOnSuccessListener(documentReference -> {
                    service.setId(documentReference.getId());
                    Log.d(TAG, "✅ Servicio creado con ID: " + service.getId());
                    callback.onSuccess(service);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error creando servicio: " + e.getMessage());
                    callback.onError("Error creando servicio: " + e.getMessage());
                });
    }

    public void updateService(HotelServiceModel service, List<Uri> newPhotoUris, ServiceCallback callback) {
        if (service.getId() == null) {
            callback.onError("ID de servicio requerido para actualizar");
            return;
        }

        Log.d(TAG, "Actualizando servicio: " + service.getId());

        // Si hay fotos nuevas, subirlas primero
        if (newPhotoUris != null && !newPhotoUris.isEmpty()) {
            uploadServicePhotos(service, newPhotoUris, new UploadCallback() {
                @Override
                public void onProgress(int percentage) {
                    // Progreso
                }

                @Override
                public void onSuccess() {
                    updateServiceInFirestore(service, callback);
                }

                @Override
                public void onError(String error) {
                    callback.onError("Error subiendo fotos: " + error);
                }
            });
        } else {
            updateServiceInFirestore(service, callback);
        }
    }

    private void updateServiceInFirestore(HotelServiceModel service, ServiceCallback callback) {
        db.collection(SERVICES_COLLECTION)
                .document(service.getId())
                .set(service.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Servicio actualizado: " + service.getId());
                    callback.onSuccess(service);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error actualizando servicio: " + e.getMessage());
                    callback.onError("Error actualizando servicio: " + e.getMessage());
                });
    }

    public void deleteService(String serviceId, ServiceCallback callback) {
        if (serviceId == null || serviceId.isEmpty()) {
            callback.onError("ID de servicio requerido");
            return;
        }

        Log.d(TAG, "Eliminando servicio: " + serviceId);

        db.collection(SERVICES_COLLECTION)
                .document(serviceId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Servicio eliminado: " + serviceId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error eliminando servicio: " + e.getMessage());
                    callback.onError("Error eliminando servicio: " + e.getMessage());
                });
    }

    // ========== SUBIDA DE FOTOS ==========

    private void uploadServicePhotos(HotelServiceModel service, List<Uri> photoUris, UploadCallback callback) {
        if (photoUris == null || photoUris.isEmpty()) {
            callback.onSuccess();
            return;
        }

        Log.d(TAG, "Subiendo " + photoUris.size() + " fotos para servicio: " + service.getName());

        List<String> uploadedUrls = new ArrayList<>();
        AtomicInteger uploadedCount = new AtomicInteger(0);
        AtomicInteger totalUploads = new AtomicInteger(photoUris.size());

        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        for (int i = 0; i < photoUris.size(); i++) {
            Uri photoUri = photoUris.get(i);
            String fileName = idGenerator.generateUniqueFileName("service", service.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_" + i + ".jpg");
            String folder = "hotel_services/" + currentUserId;

            awsFileManager.uploadFile(photoUri, currentUserId, folder, new AwsFileManager.UploadCallback() {
                @Override
                public void onSuccess(AwsFileManager.AwsFileInfo fileInfo) {
                    synchronized (uploadedUrls) {
                        uploadedUrls.add(fileInfo.fileUrl);
                        int completed = uploadedCount.incrementAndGet();

                        // Calcular progreso
                        int progress = (completed * 100) / totalUploads.get();
                        callback.onProgress(progress);

                        if (completed == totalUploads.get()) {
                            // Todas las fotos subidas
                            service.setPhotoUrls(uploadedUrls);
                            Log.d(TAG, "✅ Todas las fotos subidas exitosamente");
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

    // ========== MÉTODOS DE CONSULTA ==========

    public void getServicesByType(String serviceType, ServicesListCallback callback) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        db.collection(SERVICES_COLLECTION)
                .whereEqualTo("hotelAdminId", currentUserId)
                .whereEqualTo("serviceType", serviceType)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<HotelServiceModel> services = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            HotelServiceModel service = HotelServiceModel.fromMap(doc.getData(), doc.getId());
                            services.add(service);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parseando servicio: " + e.getMessage());
                        }
                    }
                    Log.d(TAG, "✅ " + services.size() + " servicios tipo " + serviceType + " obtenidos");
                    callback.onSuccess(services);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error obteniendo servicios: " + e.getMessage());
                    callback.onError("Error obteniendo servicios: " + e.getMessage());
                });
    }

    public void getAllServices(ServicesListCallback callback) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        db.collection(SERVICES_COLLECTION)
                .whereEqualTo("hotelAdminId", currentUserId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<HotelServiceModel> services = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            HotelServiceModel service = HotelServiceModel.fromMap(doc.getData(), doc.getId());
                            services.add(service);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parseando servicio: " + e.getMessage());
                        }
                    }
                    Log.d(TAG, "✅ " + services.size() + " servicios totales obtenidos");
                    callback.onSuccess(services);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error obteniendo servicios: " + e.getMessage());
                    callback.onError("Error obteniendo servicios: " + e.getMessage());
                });
    }

    // ========== MÉTODOS DE INICIALIZACIÓN ==========

    public void initializeDefaultServices() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return;

        Log.d(TAG, "Inicializando servicios por defecto para usuario: " + currentUserId);

        // Verificar si ya tiene servicios básicos
        getServicesByType("basic", new ServicesListCallback() {
            @Override
            public void onSuccess(List<HotelServiceModel> services) {
                if (services.isEmpty()) {
                    // No tiene servicios básicos, crear los por defecto
                    createDefaultBasicServices();
                } else {
                    Log.d(TAG, "Usuario ya tiene " + services.size() + " servicios básicos");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error verificando servicios básicos: " + error);
            }
        });
    }

    private void createDefaultBasicServices() {
        Log.d(TAG, "Creando servicios básicos por defecto");

        List<HotelServiceModel> defaultServices = new ArrayList<>();

        defaultServices.add(new HotelServiceModel("WiFi Gratuito", "Internet de alta velocidad en todas las habitaciones", "wifi", "basic"));
        defaultServices.add(new HotelServiceModel("Aire Acondicionado", "Climatización individual en cada habitación", "ac", "basic"));
        defaultServices.add(new HotelServiceModel("TV por Cable", "Televisión por cable con canales premium", "tv", "basic"));
        defaultServices.add(new HotelServiceModel("Recepción 24h", "Atención al cliente las 24 horas del día", "reception", "basic"));

        AtomicInteger createdCount = new AtomicInteger(0);
        int totalServices = defaultServices.size();

        for (HotelServiceModel service : defaultServices) {
            createService(service, null, new ServiceCallback() {
                @Override
                public void onSuccess(HotelServiceModel createdService) {
                    int completed = createdCount.incrementAndGet();
                    Log.d(TAG, "✅ Servicio básico creado: " + createdService.getName() + " (" + completed + "/" + totalServices + ")");

                    if (completed == totalServices) {
                        Log.d(TAG, "✅ Todos los servicios básicos por defecto creados");
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error creando servicio básico por defecto: " + error);
                }
            });
        }
    }

    // ========== UTILIDADES ==========

    private String getCurrentUserId() {
        FirebaseUser currentUser = auth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    public void cleanup() {
        if (servicesListener != null) {
            servicesListener.remove();
            servicesListener = null;
        }
        listeners.clear();
    }
}