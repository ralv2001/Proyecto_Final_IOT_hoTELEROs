package com.example.proyecto_final_hoteleros.adminhotel.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.proyecto_final_hoteleros.adminhotel.model.BasicService;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceItem;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceModel;

import java.util.ArrayList;
import java.util.List;

public class ServiceSyncManager implements FirebaseServiceManager.OnServicesChangedListener {

    private static final String TAG = "ServiceSyncManager";
    private static ServiceSyncManager instance;

    private final List<OnBasicServicesChangedListener> listeners = new ArrayList<>();
    private final FirebaseServiceManager firebaseServiceManager;
    private List<HotelServiceModel> currentBasicServices = new ArrayList<>();

    public interface OnBasicServicesChangedListener {
        void onBasicServicesUpdated(List<BasicService> basicServices);
    }

    private ServiceSyncManager(Context context) {
        this.firebaseServiceManager = FirebaseServiceManager.getInstance(context);
        this.firebaseServiceManager.addListener(this);

        // ✅ SOLUCIÓN: Forzar carga inicial de servicios básicos
        loadInitialBasicServices();

        // Inicializar servicios por defecto si es necesario
        firebaseServiceManager.initializeDefaultServices();
    }
    private void loadInitialBasicServices() {
        Log.d(TAG, "🔄 Cargando servicios básicos iniciales...");

        firebaseServiceManager.getServicesByType("basic", new FirebaseServiceManager.ServicesListCallback() {
            @Override
            public void onSuccess(List<HotelServiceModel> basicServices) {
                Log.d(TAG, "✅ Servicios básicos iniciales obtenidos: " + basicServices.size());
                currentBasicServices = new ArrayList<>(basicServices);

                // Convertir y notificar a listeners
                List<BasicService> convertedServices = new ArrayList<>();
                for (HotelServiceModel firebaseService : basicServices) {
                    BasicService basicService = convertFirebaseToBasic(firebaseService);
                    convertedServices.add(basicService);
                }

                notifyListeners(convertedServices);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando servicios básicos iniciales: " + error);
            }
        });
    }
    public static synchronized ServiceSyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new ServiceSyncManager(context);
        }
        return instance;
    }

    // ========== IMPLEMENTACIÓN DE FIREBASE LISTENER ==========

    @Override
    public void onBasicServicesUpdated(List<HotelServiceModel> basicServices) {
        Log.d(TAG, "Servicios básicos actualizados desde Firebase: " + basicServices.size());
        this.currentBasicServices = new ArrayList<>(basicServices);

        // Convertir a BasicService para compatibilidad
        List<BasicService> convertedServices = new ArrayList<>();
        for (HotelServiceModel firebaseService : basicServices) {
            BasicService basicService = convertFirebaseToBasic(firebaseService);
            convertedServices.add(basicService);
        }

        // Notificar a listeners locales
        notifyListeners(convertedServices);
    }

    @Override
    public void onAllServicesUpdated(List<HotelServiceModel> allServices) {
        // Filtrar solo los básicos
        List<HotelServiceModel> basics = new ArrayList<>();
        for (HotelServiceModel service : allServices) {
            if ("basic".equals(service.getServiceType())) {
                basics.add(service);
            }
        }
        onBasicServicesUpdated(basics);
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "Error desde Firebase: " + error);
    }

    // ========== MÉTODOS PÚBLICOS (COMPATIBILIDAD) ==========

    public void addBasicService(BasicService service) {
        Log.d(TAG, "Agregando servicio básico: " + service.getName());

        // Convertir a modelo de Firebase
        HotelServiceModel firebaseService = convertBasicToFirebase(service);

        // Obtener URIs de fotos
        List<Uri> photoUris = new ArrayList<>();
        for (String photoPath : service.getPhotos()) {
            if (photoPath.startsWith("content://") || photoPath.startsWith("file://")) {
                photoUris.add(Uri.parse(photoPath));
            }
        }

        // Crear en Firebase
        firebaseServiceManager.createService(firebaseService, photoUris, new FirebaseServiceManager.ServiceCallback() {
            @Override
            public void onSuccess(HotelServiceModel createdService) {
                Log.d(TAG, "✅ Servicio básico creado en Firebase: " + createdService.getId());
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error creando servicio básico: " + error);
            }
        });
    }

    public void removeBasicService(int position) {
        if (position >= 0 && position < currentBasicServices.size()) {
            HotelServiceModel serviceToDelete = currentBasicServices.get(position);

            Log.d(TAG, "Eliminando servicio básico: " + serviceToDelete.getName());

            firebaseServiceManager.deleteService(serviceToDelete.getId(), new FirebaseServiceManager.ServiceCallback() {
                @Override
                public void onSuccess(HotelServiceModel service) {
                    Log.d(TAG, "✅ Servicio básico eliminado de Firebase");
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error eliminando servicio básico: " + error);
                }
            });
        }
    }

    public void updateBasicService(int position, BasicService service) {
        if (position >= 0 && position < currentBasicServices.size()) {
            HotelServiceModel existingService = currentBasicServices.get(position);

            // Actualizar campos
            existingService.setName(service.getName());
            existingService.setDescription(service.getDescription());
            existingService.setIconKey(service.getIconKey());

            Log.d(TAG, "Actualizando servicio básico: " + service.getName());

            firebaseServiceManager.updateService(existingService, null, new FirebaseServiceManager.ServiceCallback() {
                @Override
                public void onSuccess(HotelServiceModel updatedService) {
                    Log.d(TAG, "✅ Servicio básico actualizado en Firebase");
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error actualizando servicio básico: " + error);
                }
            });
        }
    }

    public List<BasicService> getBasicServices() {
        List<BasicService> result = new ArrayList<>();
        for (HotelServiceModel firebaseService : currentBasicServices) {
            result.add(convertFirebaseToBasic(firebaseService));
        }
        return result;
    }

    public void addListener(OnBasicServicesChangedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(OnBasicServicesChangedListener listener) {
        listeners.remove(listener);
    }

    public int findBasicServicePosition(String name) {
        for (int i = 0; i < currentBasicServices.size(); i++) {
            if (currentBasicServices.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    // ========== MÉTODOS DE CONVERSIÓN ==========

    public static List<HotelServiceItem> convertToHotelServiceItems(List<BasicService> basicServices) {
        List<HotelServiceItem> hotelServiceItems = new ArrayList<>();
        for (BasicService basicService : basicServices) {
            // Convertir strings a Uri
            List<Uri> photoUris = new ArrayList<>();
            for (String photoPath : basicService.getPhotos()) {
                if (photoPath.startsWith("http") || photoPath.startsWith("content://") || photoPath.startsWith("file://")) {
                    photoUris.add(Uri.parse(photoPath));
                }
            }

            HotelServiceItem item = new HotelServiceItem(
                    basicService.getName(),
                    basicService.getDescription(),
                    0.0,
                    basicService.getIconKey(),
                    HotelServiceItem.ServiceType.BASIC,
                    photoUris
            );
            hotelServiceItems.add(item);
        }
        return hotelServiceItems;
    }

    public static BasicService convertToBasicService(HotelServiceItem hotelServiceItem) {
        BasicService basicService = new BasicService(
                hotelServiceItem.getName(),
                hotelServiceItem.getDescription(),
                hotelServiceItem.getIconKey()
        );

        // Convertir URIs a strings
        List<String> photoPaths = new ArrayList<>();
        for (Uri uri : hotelServiceItem.getPhotos()) {
            photoPaths.add(uri.toString());
        }
        basicService.setPhotos(photoPaths);

        return basicService;
    }

    private HotelServiceModel convertBasicToFirebase(BasicService basicService) {
        HotelServiceModel firebaseService = new HotelServiceModel(
                basicService.getName(),
                basicService.getDescription(),
                basicService.getIconKey(),
                "basic"
        );

        // Convertir fotos (si son URLs, mantenerlas)
        List<String> photoUrls = new ArrayList<>();
        for (String photoPath : basicService.getPhotos()) {
            if (photoPath.startsWith("http")) {
                photoUrls.add(photoPath);
            }
        }
        firebaseService.setPhotoUrls(photoUrls);

        return firebaseService;
    }

    private BasicService convertFirebaseToBasic(HotelServiceModel firebaseService) {
        BasicService basicService = new BasicService(
                firebaseService.getName(),
                firebaseService.getDescription(),
                firebaseService.getIconKey()
        );

        // Convertir URLs de fotos
        List<String> photoPaths = new ArrayList<>();
        if (firebaseService.getPhotoUrls() != null) {
            photoPaths.addAll(firebaseService.getPhotoUrls());
        }
        basicService.setPhotos(photoPaths);

        return basicService;
    }

    private void notifyListeners(List<BasicService> basicServices) {
        for (OnBasicServicesChangedListener listener : listeners) {
            try {
                listener.onBasicServicesUpdated(basicServices);
            } catch (Exception e) {
                Log.e(TAG, "Error notificando listener: " + e.getMessage());
            }
        }
    }

    public void cleanup() {
        if (firebaseServiceManager != null) {
            firebaseServiceManager.removeListener(this);
        }
        listeners.clear();
    }
}