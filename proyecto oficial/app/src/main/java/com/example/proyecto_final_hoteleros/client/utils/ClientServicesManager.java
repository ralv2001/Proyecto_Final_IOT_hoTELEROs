package com.example.proyecto_final_hoteleros.client.utils;

import android.content.Context;
import android.util.Log;

import com.example.proyecto_final_hoteleros.client.data.model.HotelService;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ✅ Manager ARREGLADO con comparación de IDs corregida
 */
public class ClientServicesManager {

    private static final String TAG = "ClientServicesManager";
    private static final String SERVICES_COLLECTION = "hotel_services";

    private static ClientServicesManager instance;
    private FirebaseFirestore db;
    private Context context;

    public interface ServicesCallback {
        void onSuccess(List<HotelService> services);
        void onError(String error);
    }

    private ClientServicesManager(Context context) {
        this.context = context.getApplicationContext();
        this.db = FirebaseFirestore.getInstance();
    }

    public static synchronized ClientServicesManager getInstance(Context context) {
        if (instance == null) {
            instance = new ClientServicesManager(context);
        }
        return instance;
    }

    /**
     * ✅ CARGAR servicios para selección categorizados correctamente
     */
    public void loadServicesForSelection(String hotelAdminId, String[] includedServiceIds, ServicesCallback callback) {

        if (hotelAdminId == null || hotelAdminId.isEmpty()) {
            Log.w(TAG, "⚠️ No hay hotelAdminId, usando servicios por defecto");
            callback.onSuccess(createDefaultServicesWithTaxi(350.0));
            return;
        }

        Log.d(TAG, "🔄 Cargando servicios categorizados para hotel: " + hotelAdminId);
        Log.d(TAG, "📋 Servicios incluidos en este cuarto: " + (includedServiceIds != null ? includedServiceIds.length : 0));

        // ✅ DEBUG MEJORADO: Mostrar lista de servicios incluidos
        if (includedServiceIds != null) {
            Log.d(TAG, "🏨 Lista EXACTA de servicios incluidos:");
            for (int i = 0; i < includedServiceIds.length; i++) {
                String serviceId = includedServiceIds[i];
                Log.d(TAG, String.format("   [%d] = '%s' (length: %d)", i, serviceId, serviceId.length()));
            }
        } else {
            Log.d(TAG, "❌ includedServiceIds es NULL");
        }

        // ✅ CONSULTA SIMPLIFICADA
        db.collection(SERVICES_COLLECTION)
                .whereEqualTo("hotelAdminId", hotelAdminId)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    List<HotelService> allCategorizedServices = new ArrayList<>();

                    // ✅ PROCESAR servicios desde Firebase
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            HotelService service = convertFirebaseToHotelService(doc);
                            if (service != null) {

                                // ✅ CATEGORIZAR CORRECTAMENTE según tipo y si está incluido en el cuarto
                                categorizeServiceCorrectly(service, includedServiceIds);

                                // ✅ AGREGAR TODOS LOS SERVICIOS (el filtro se hace en la UI)
                                allCategorizedServices.add(service);

                                Log.d(TAG, "   ✅ Servicio procesado: " + service.getName() +
                                        " - Tipo: " + service.getServiceType() +
                                        " - Incluido en cuarto: " + service.isIncludedInRoom());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parseando servicio: " + e.getMessage());
                        }
                    }

                    // ✅ AGREGAR TAXI después de obtener su configuración
                    loadTaxiConfigAndAddService(hotelAdminId, allCategorizedServices, callback);

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error cargando servicios: " + e.getMessage());
                    callback.onSuccess(createDefaultServicesWithTaxi(350.0));
                });
    }

    /**
     * ✅ CARGAR todos los servicios para navegación (modo browse_only)
     */
    public void loadAllServicesForBrowsing(String hotelAdminId, ServicesCallback callback) {
        if (hotelAdminId == null || hotelAdminId.isEmpty()) {
            callback.onSuccess(createDefaultServicesWithTaxi(350.0));
            return;
        }

        Log.d(TAG, "🔄 Cargando todos los servicios para navegación. Hotel: " + hotelAdminId);

        db.collection(SERVICES_COLLECTION)
                .whereEqualTo("hotelAdminId", hotelAdminId)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    List<HotelService> allServices = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            HotelService service = convertFirebaseToHotelService(doc);
                            if (service != null) {
                                // En modo browsing, marcar todos como no incluidos en cuarto específico
                                service.setIncludedInRoom(false);
                                allServices.add(service);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parseando servicio: " + e.getMessage());
                        }
                    }

                    // ✅ AGREGAR TAXI con su configuración
                    loadTaxiConfigAndAddService(hotelAdminId, allServices, callback);

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error cargando servicios: " + e.getMessage());
                    callback.onSuccess(createDefaultServicesWithTaxi(350.0));
                });
    }

    /**
     * ✅ CARGAR configuración de taxi y agregar el servicio
     */
    private void loadTaxiConfigAndAddService(String hotelAdminId, List<HotelService> servicesList, ServicesCallback callback) {
        Log.d(TAG, "🚕 Cargando configuración de taxi para hotel: " + hotelAdminId);

        db.collection("taxi_config")
                .document(hotelAdminId)
                .get()
                .addOnSuccessListener(configDoc -> {
                    double taxiMinAmount = 350.0; // Valor por defecto

                    if (configDoc.exists()) {
                        Double minAmount = configDoc.getDouble("minAmount");
                        if (minAmount != null) {
                            taxiMinAmount = minAmount;
                            Log.d(TAG, "✅ Configuración de taxi cargada: monto mínimo = " + taxiMinAmount);
                        }
                    } else {
                        Log.d(TAG, "ℹ️ No existe configuración de taxi, usando valor por defecto: " + taxiMinAmount);
                    }

                    // ✅ AGREGAR taxi con el monto configurado
                    addTaxiServiceWithConfig(servicesList, taxiMinAmount);

                    Log.d(TAG, "✅ Servicios cargados con taxi: " + servicesList.size());
                    logServicesByCategory(servicesList);

                    callback.onSuccess(servicesList);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error cargando configuración de taxi: " + e.getMessage());
                    // ✅ CONTINUAR con valor por defecto
                    addTaxiServiceWithConfig(servicesList, 350.0);
                    callback.onSuccess(servicesList);
                });
    }

    /**
     * ✅ CONVERTIR documento Firebase a HotelService
     */
    private HotelService convertFirebaseToHotelService(DocumentSnapshot doc) {
        try {
            String id = doc.getId();
            String name = doc.getString("name");
            String description = doc.getString("description");
            String iconKey = doc.getString("iconKey");
            String serviceType = doc.getString("serviceType"); // ✅ basic, included, paid

            // ✅ PRECIO según tipo
            Double priceValue = null;
            if ("paid".equals(serviceType)) {
                Number priceNumber = (Number) doc.get("price");
                if (priceNumber != null) {
                    priceValue = priceNumber.doubleValue();
                }
            }

            // URLs de fotos
            List<String> photoUrls = (List<String>) doc.get("photoUrls");
            if (photoUrls == null) {
                photoUrls = new ArrayList<>();
            }

            // ✅ CREAR HotelService
            HotelService service = new HotelService(
                    id,
                    name != null ? name : "Servicio",
                    description != null ? description : "",
                    priceValue,
                    null,
                    photoUrls,
                    iconKey != null ? iconKey : "ic_service",
                    false,                                       // isConditional - solo taxi
                    null,
                    determineServiceCategory(serviceType),
                    false,
                    0,
                    "24/7",
                    new String[0],
                    false
            );

            // ✅ CONFIGURAR según tipo de servicio
            service.setServiceType(serviceType);

            return service;

        } catch (Exception e) {
            Log.e(TAG, "Error convirtiendo servicio Firebase: " + e.getMessage());
            return null;
        }
    }

    /**
     * ✅ CORREGIDO: CATEGORIZAR servicio correctamente - SOLO modificar isIncludedInRoom, NO el tipo
     */
    private void categorizeServiceCorrectly(HotelService service, String[] includedServiceIds) {
        String serviceType = service.getServiceType(); // ✅ RESPETAR tipo original de Firebase
        String serviceName = service.getName();
        String serviceId = service.getId();

        // ✅ BUSCAR POR NOMBRE si está incluido en este cuarto específico
        boolean isIncludedInThisRoom = isServiceIncludedInRoomByName(serviceName, includedServiceIds);

        Log.d(TAG, "🔍 ===== DEBUG CATEGORIZACIÓN =====");
        Log.d(TAG, String.format("   📝 Servicio: '%s'", serviceName));
        Log.d(TAG, String.format("   🆔 ID FB: '%s' (length: %d)", serviceId, serviceId.length()));
        Log.d(TAG, String.format("   📋 Tipo FB: '%s'", serviceType));
        Log.d(TAG, String.format("   ✅ Incluido en cuarto (por NOMBRE): %s", isIncludedInThisRoom));

        // ✅ LÓGICA CORREGIDA: RESPETAR el tipo Firebase, solo cambiar isIncludedInRoom
        if ("basic".equals(serviceType)) {
            // ✅ SERVICIOS BÁSICOS: Siempre gratis, pero NO específicos del cuarto
            service.setFree(true);
            service.setIncludedInRoom(false); // ✅ Los básicos NO son específicos del cuarto
            service.setCategory(HotelService.ServiceCategory.ESSENTIALS);
            // ✅ NO CAMBIAR EL TIPO - mantener "basic"
            Log.d(TAG, "   📋 RESULTADO: BÁSICO (todos los cuartos) - Tipo mantenido");

        } else if ("included".equals(serviceType)) {
            // ✅ SERVICIOS INCLUIDOS: Verificar si están en ESTE cuarto específico
            service.setFree(true);
            service.setIncludedInRoom(isIncludedInThisRoom); // ✅ CLAVE: Solo true si está en ESTE cuarto
            service.setCategory(HotelService.ServiceCategory.COMFORT);
            // ✅ NO CAMBIAR EL TIPO - mantener "included"

            if (isIncludedInThisRoom) {
                Log.d(TAG, "   ⭐ RESULTADO: INCLUIDO EN ESTE CUARTO - Tipo mantenido");
            } else {
                Log.d(TAG, "   ⏭️ RESULTADO: INCLUIDO PERO NO EN ESTE CUARTO - Tipo mantenido");
            }

        } else if ("paid".equals(serviceType)) {
            // ✅ SERVICIOS DE PAGO: Siempre pagados
            service.setFree(false);
            service.setIncludedInRoom(false);
            service.setCategory(determineServiceCategory(serviceType));
            // ✅ NO CAMBIAR EL TIPO - mantener "paid"
            Log.d(TAG, String.format("   💰 RESULTADO: DE PAGO - S/. %.2f - Tipo mantenido", service.getPrice()));

        } else {
            // ✅ Fallback para otros tipos (conditional, etc.)
            service.setFree(service.getPrice() == null || service.getPrice() <= 0);
            service.setIncludedInRoom(false);
            service.setCategory(HotelService.ServiceCategory.ESSENTIALS);
            // ✅ NO CAMBIAR EL TIPO - mantener el original
            Log.d(TAG, "   🔧 RESULTADO: TIPO " + serviceType + " mantenido");
        }

        Log.d(TAG, "=====================================");
    }

    /**
     * ✅ MANTENER método de búsqueda por nombre (este está correcto)
     */
    private boolean isServiceIncludedInRoomByName(String serviceName, String[] includedServiceIds) {
        if (includedServiceIds == null) {
            Log.d(TAG, "🔍 isServiceIncludedInRoomByName(" + serviceName + "): Lista NULL");
            return false;
        }

        String cleanServiceName = serviceName != null ? serviceName.trim() : "";

        Log.d(TAG, "🔍 === COMPARACIÓN POR NOMBRE ===");
        Log.d(TAG, String.format("   🎯 Buscando NOMBRE: '%s' (length: %d)", cleanServiceName, cleanServiceName.length()));
        Log.d(TAG, String.format("   📋 En lista de %d elementos:", includedServiceIds.length));

        for (int i = 0; i < includedServiceIds.length; i++) {
            String includedName = includedServiceIds[i];
            String cleanIncludedName = includedName != null ? includedName.trim() : "";

            boolean match = cleanServiceName.equals(cleanIncludedName);

            Log.d(TAG, String.format("   [%d] '%s' (len:%d) == '%s' (len:%d) = %s",
                    i, cleanIncludedName, cleanIncludedName.length(),
                    cleanServiceName, cleanServiceName.length(), match));

            if (match) {
                Log.d(TAG, "   ✅ MATCH POR NOMBRE ENCONTRADO en posición " + i);
                return true;
            }
        }

        Log.d(TAG, "   ❌ NO ENCONTRADO por nombre en ninguna posición");
        Log.d(TAG, "===============================");
        return false;
    }
    /**
     * ✅ ARREGLADO: AGREGAR taxi con configuración dinámica
     */
    private void addTaxiServiceWithConfig(List<HotelService> servicesList, double taxiMinAmount) {
        HotelService taxiService = new HotelService(
                "taxi",
                "Taxi Premium al Aeropuerto",
                String.format("Se desbloquea GRATIS al superar S/. %.0f en tu reserva", taxiMinAmount),
                null,
                null,
                new ArrayList<>(),
                "ic_taxi",
                true,
                String.format("GRATIS si tu reserva supera S/. %.0f", taxiMinAmount),
                HotelService.ServiceCategory.TRANSPORT,
                false,
                0,
                "Según horario de vuelo",
                new String[]{"Mercedes-Benz", "Chofer bilingüe", "WiFi a bordo"},
                false
        );

        taxiService.setServiceType("conditional");
        taxiService.setEligibleForFree(false);
        taxiService.setPrice(taxiMinAmount);

        servicesList.add(taxiService);

        Log.d(TAG, "✅ Taxi agregado - Monto mínimo: S/. " + taxiMinAmount);
    }

    /**
     * ✅ CREAR servicios por defecto con taxi configurable
     */
    private List<HotelService> createDefaultServicesWithTaxi(double taxiMinAmount) {
        List<HotelService> defaultServices = new ArrayList<>();

        HotelService wifi = new HotelService(
                "wifi", "WiFi Gratuito", "Internet de alta velocidad en todas las habitaciones",
                null, null, new ArrayList<>(), "ic_wifi", false, null,
                HotelService.ServiceCategory.ESSENTIALS, false, 0, "24/7", new String[0], false
        );
        wifi.setServiceType("basic");
        wifi.setFree(true);

        HotelService ac = new HotelService(
                "ac", "Aire Acondicionado", "Climatización individual en cada habitación",
                null, null, new ArrayList<>(), "ic_ac", false, null,
                HotelService.ServiceCategory.ESSENTIALS, false, 0, "24/7", new String[0], false
        );
        ac.setServiceType("basic");
        ac.setFree(true);

        HotelService breakfast = new HotelService(
                "breakfast", "Desayuno Gourmet", "Desayuno continental premium",
                45.0, null, new ArrayList<>(), "ic_breakfast", false, null,
                HotelService.ServiceCategory.GASTRONOMY, false, 0, "6:00 AM - 10:00 AM", new String[0], false
        );
        breakfast.setServiceType("paid");

        HotelService spa = new HotelService(
                "spa", "Spa & Wellness", "Experiencia completa de relajación",
                120.0, null, new ArrayList<>(), "ic_spa", false, null,
                HotelService.ServiceCategory.WELLNESS, false, 0, "9:00 AM - 8:00 PM", new String[0], false
        );
        spa.setServiceType("paid");

        defaultServices.add(wifi);
        defaultServices.add(ac);
        defaultServices.add(breakfast);
        defaultServices.add(spa);

        addTaxiServiceWithConfig(defaultServices, taxiMinAmount);

        Log.d(TAG, "✅ Servicios por defecto creados con taxi (mínimo: S/. " + taxiMinAmount + ")");
        return defaultServices;
    }

    private HotelService.ServiceCategory determineServiceCategory(String serviceType) {
        switch (serviceType) {
            case "basic":
                return HotelService.ServiceCategory.ESSENTIALS;
            case "included":
                return HotelService.ServiceCategory.COMFORT;
            case "paid":
                return HotelService.ServiceCategory.COMFORT;
            default:
                return HotelService.ServiceCategory.ESSENTIALS;
        }
    }

    /**
     * ✅ ARREGLADO: VERIFICAR servicio incluido con limpieza de strings
     */
    private boolean isServiceIncludedInRoom(String serviceId, String[] includedServiceIds) {
        if (includedServiceIds == null) {
            Log.d(TAG, "🔍 isServiceIncludedInRoom(" + serviceId + "): Lista NULL");
            return false;
        }

        // ✅ LIMPIAR el serviceId de entrada
        String cleanServiceId = serviceId != null ? serviceId.trim() : "";

        Log.d(TAG, "🔍 === COMPARACIÓN DETALLADA ===");
        Log.d(TAG, String.format("   🎯 Buscando: '%s' (length: %d)", cleanServiceId, cleanServiceId.length()));
        Log.d(TAG, String.format("   📋 En lista de %d elementos:", includedServiceIds.length));

        for (int i = 0; i < includedServiceIds.length; i++) {
            String includedId = includedServiceIds[i];
            String cleanIncludedId = includedId != null ? includedId.trim() : "";

            boolean match = cleanServiceId.equals(cleanIncludedId);

            Log.d(TAG, String.format("   [%d] '%s' (len:%d) == '%s' (len:%d) = %s",
                    i, cleanIncludedId, cleanIncludedId.length(),
                    cleanServiceId, cleanServiceId.length(), match));

            if (match) {
                Log.d(TAG, "   ✅ MATCH ENCONTRADO en posición " + i);
                return true;
            }
        }

        Log.d(TAG, "   ❌ NO ENCONTRADO en ninguna posición");
        Log.d(TAG, "===============================");
        return false;
    }

    /**
     * ✅ LOG de servicios por categoría con más detalle
     */
    private void logServicesByCategory(List<HotelService> services) {
        int basicCount = 0, includedInRoomCount = 0, includedNotInRoomCount = 0, paidCount = 0, conditionalCount = 0;

        Log.d(TAG, "========== RESUMEN FINAL DE SERVICIOS ==========");

        for (HotelService service : services) {
            String type = service.getServiceType();
            boolean isIncludedInRoom = service.isIncludedInRoom();

            String status;
            if (type != null) {
                switch (type) {
                    case "basic":
                        basicCount++;
                        status = "BÁSICO (todos los cuartos)";
                        break;
                    case "included":
                        if (isIncludedInRoom) {
                            includedInRoomCount++;
                            status = "⭐ INCLUIDO EN ESTE CUARTO";
                        } else {
                            includedNotInRoomCount++;
                            status = "⏭️ INCLUIDO EN OTROS CUARTOS";
                        }
                        break;
                    case "paid":
                        paidCount++;
                        status = "💰 DE PAGO";
                        break;
                    case "conditional":
                        conditionalCount++;
                        status = "🚕 CONDICIONAL (TAXI)";
                        break;
                    default:
                        status = "❓ SIN TIPO";
                }
            } else {
                status = "❓ SIN TIPO";
            }

            Log.d(TAG, String.format("📋 %-20s (%s) - %s",
                    service.getName(), service.getId(), status));
        }

        Log.d(TAG, "📊 TOTALES POR CATEGORÍA:");
        Log.d(TAG, "   - Básicos (todos los cuartos): " + basicCount);
        Log.d(TAG, "   - ⭐ Incluidos EN ESTE CUARTO: " + includedInRoomCount + " ⭐");
        Log.d(TAG, "   - Incluidos NO en este cuarto: " + includedNotInRoomCount);
        Log.d(TAG, "   - De pago: " + paidCount);
        Log.d(TAG, "   - Condicionales (taxi): " + conditionalCount);
        Log.d(TAG, "=================================================");

        // ✅ MENSAJE ESPECIAL si no hay incluidos en este cuarto
        if (includedInRoomCount == 0) {
            Log.w(TAG, "⚠️ WARNING: No hay servicios incluidos en este cuarto específico!");
            Log.w(TAG, "   La pestaña 'Incluidos' podría estar vacía.");
            Log.w(TAG, "   Verificar que los IDs en includedServiceIds coincidan con los de Firebase.");
        } else {
            Log.d(TAG, "✅ SUCCESS: " + includedInRoomCount + " servicios incluidos en este cuarto - La pestaña 'Incluidos' tendrá contenido");
        }
    }
}