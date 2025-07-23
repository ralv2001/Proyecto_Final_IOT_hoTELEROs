package com.example.proyecto_final_hoteleros.client.utils;

import android.util.Log;
import com.example.proyecto_final_hoteleros.client.data.model.HotelService;
import java.util.List;
import java.util.Set;

/**
 * ✅ HERRAMIENTA DE DEBUG para verificar que los servicios se categorizan correctamente
 * y que el taxi funciona con el monto dinámico
 */
public class ServicesDebugHelper {

    private static final String TAG = "ServicesDebugHelper";

    /**
     * ✅ Debuggear categorización de servicios
     */
    public static void debugServiceCategories(List<HotelService> services, String[] includedServiceIds) {
        Log.d(TAG, "========== DEBUG CATEGORIZACIÓN DE SERVICIOS ==========");

        int basicCount = 0, includedInRoomCount = 0, includedNotInRoomCount = 0, paidCount = 0, conditionalCount = 0;

        for (HotelService service : services) {
            String serviceType = service.getServiceType();
            boolean isIncludedInRoom = service.isIncludedInRoom();
            boolean isFree = service.isFree();

            String category;
            if ("basic".equals(serviceType)) {
                category = "BÁSICO";
                basicCount++;
            } else if ("included".equals(serviceType)) {
                if (isIncludedInRoom) {
                    category = "INCLUIDO EN ESTE CUARTO";
                    includedInRoomCount++;
                } else {
                    category = "INCLUIDO PERO NO EN ESTE CUARTO";
                    includedNotInRoomCount++;
                }
            } else if ("paid".equals(serviceType)) {
                category = "DE PAGO";
                paidCount++;
            } else if ("conditional".equals(serviceType)) {
                category = "CONDICIONAL (TAXI)";
                conditionalCount++;
            } else {
                category = "SIN CATEGORÍA";
            }

            Log.d(TAG, String.format("📋 %s: %s | Tipo: %s | En cuarto: %s | Gratis: %s | Precio: %s",
                    service.getName(),
                    category,
                    serviceType,
                    isIncludedInRoom ? "SÍ" : "NO",
                    isFree ? "SÍ" : "NO",
                    service.getPrice() != null ? "S/. " + service.getPrice() : "N/A"));
        }

        Log.d(TAG, "========== RESUMEN ==========");
        Log.d(TAG, "📊 Básicos: " + basicCount);
        Log.d(TAG, "✅ Incluidos en este cuarto: " + includedInRoomCount);
        Log.d(TAG, "⏭️ Incluidos en otros cuartos: " + includedNotInRoomCount);
        Log.d(TAG, "💰 De pago: " + paidCount);
        Log.d(TAG, "🚕 Condicionales: " + conditionalCount);

        // Verificar servicios incluidos en el cuarto
        if (includedServiceIds != null) {
            Log.d(TAG, "🏨 Servicios que DEBEN estar incluidos en este cuarto:");
            for (String serviceId : includedServiceIds) {
                boolean found = false;
                for (HotelService service : services) {
                    if (service.getId().equals(serviceId)) {
                        found = true;
                        Log.d(TAG, "  ✅ " + service.getName() +
                                " (Marcado como incluido: " + service.isIncludedInRoom() + ")");
                        break;
                    }
                }
                if (!found) {
                    Log.w(TAG, "  ❌ Servicio " + serviceId + " NO ENCONTRADO en la lista");
                }
            }
        }
    }

    /**
     * ✅ Debuggear configuración del taxi
     */
    public static void debugTaxiConfiguration(List<HotelService> services, double currentTotal, double taxiMinAmount) {
        Log.d(TAG, "========== DEBUG CONFIGURACIÓN DE TAXI ==========");

        HotelService taxiService = null;
        for (HotelService service : services) {
            if ("taxi".equals(service.getId())) {
                taxiService = service;
                break;
            }
        }

        if (taxiService == null) {
            Log.w(TAG, "❌ TAXI NO ENCONTRADO en la lista de servicios");
            return;
        }

        boolean qualifies = TaxiConfigManager.qualifiesForFreeTaxi(currentTotal, taxiMinAmount);
        String message = TaxiConfigManager.getTaxiMessage(currentTotal, taxiMinAmount);
        double needed = TaxiConfigManager.getAmountNeededForFreeTaxi(currentTotal, taxiMinAmount);

        Log.d(TAG, "🚕 Taxi Service Debug:");
        Log.d(TAG, "  - ID: " + taxiService.getId());
        Log.d(TAG, "  - Nombre: " + taxiService.getName());
        Log.d(TAG, "  - Tipo: " + taxiService.getServiceType());
        Log.d(TAG, "  - Es condicional: " + taxiService.isConditional());
        Log.d(TAG, "  - Elegible para gratis: " + taxiService.isEligibleForFree());
        Log.d(TAG, "  - Precio referencia: " + taxiService.getPrice());

        Log.d(TAG, "💰 Cálculos:");
        Log.d(TAG, "  - Total actual: S/. " + currentTotal);
        Log.d(TAG, "  - Monto mínimo configurado: S/. " + taxiMinAmount);
        Log.d(TAG, "  - ¿Califica para gratis?: " + (qualifies ? "SÍ" : "NO"));
        Log.d(TAG, "  - Dinero faltante: S/. " + needed);

        Log.d(TAG, "📢 Mensajes:");
        Log.d(TAG, "  - Mensaje: " + message);
        Log.d(TAG, "  - Badge: " + taxiService.getConditionalBadgeText());
        Log.d(TAG, "  - Precio display: " + taxiService.getPriceDisplay());
    }

    /**
     * ✅ Debuggear servicios seleccionados y cálculo del carrito
     */
    public static void debugCartCalculation(List<HotelService> services, Set<String> selectedServiceIds,
                                            double roomPrice, double taxiMinAmount) {
        Log.d(TAG, "========== DEBUG CÁLCULO DE CARRITO ==========");

        double totalAdditionalCost = 0.0;
        int paidServicesCount = 0;
        int freeServicesCount = 0;

        Log.d(TAG, "🛒 Servicios seleccionados: " + selectedServiceIds.size());

        for (HotelService service : services) {
            if (selectedServiceIds.contains(service.getId())) {
                String serviceType = service.getServiceType();
                boolean isFree = service.isFree();
                boolean isIncluded = service.isIncludedInRoom();

                Log.d(TAG, String.format("  📝 %s | Tipo: %s | Gratis: %s | En cuarto: %s | Precio: %s",
                        service.getName(),
                        serviceType,
                        isFree ? "SÍ" : "NO",
                        isIncluded ? "SÍ" : "NO",
                        service.getPrice() != null ? "S/. " + service.getPrice() : "N/A"));

                // Determinar si suma al total
                boolean shouldAddToTotal = false;

                if ("paid".equals(serviceType) && service.getPrice() != null && service.getPrice() > 0) {
                    shouldAddToTotal = true;
                    totalAdditionalCost += service.getPrice();
                    paidServicesCount++;
                } else if ("conditional".equals(serviceType)) {
                    // Taxi nunca suma al total (siempre gratis cuando se puede agregar)
                    Log.d(TAG, "    🚕 Taxi seleccionado pero NO suma al total (gratis)");
                    freeServicesCount++;
                } else {
                    freeServicesCount++;
                }

                Log.d(TAG, "    💰 Suma al total: " + (shouldAddToTotal ? "SÍ" : "NO"));
            }
        }

        double finalTotal = roomPrice + totalAdditionalCost;

        Log.d(TAG, "💰 Cálculo final:");
        Log.d(TAG, "  - Precio del cuarto: S/. " + roomPrice);
        Log.d(TAG, "  - Servicios adicionales: S/. " + totalAdditionalCost);
        Log.d(TAG, "  - Total final: S/. " + finalTotal);
        Log.d(TAG, "  - Servicios de pago: " + paidServicesCount);
        Log.d(TAG, "  - Servicios gratuitos: " + freeServicesCount);

        // Verificar si el taxi se desbloquea con este total
        boolean taxiUnlocked = TaxiConfigManager.qualifiesForFreeTaxi(finalTotal, taxiMinAmount);
        Log.d(TAG, "🚕 Con este total, ¿se desbloquea el taxi?: " + (taxiUnlocked ? "SÍ" : "NO"));
    }

    /**
     * ✅ Debuggear filtros de servicios
     */
    public static void debugServiceFilters(List<HotelService> allServices, String filterType) {
        Log.d(TAG, "========== DEBUG FILTRO: " + filterType.toUpperCase() + " ==========");

        int matchingServices = 0;

        for (HotelService service : allServices) {
            boolean shouldShow = false;
            String serviceType = service.getServiceType();

            switch (filterType) {
                case "all":
                    shouldShow = true;
                    break;
                case "basic":
                    shouldShow = "basic".equals(serviceType);
                    break;
                case "included":
                    shouldShow = "included".equals(serviceType) && service.isIncludedInRoom();
                    break;
                case "paid":
                    shouldShow = "paid".equals(serviceType);
                    break;
                case "conditional":
                    shouldShow = "conditional".equals(serviceType);
                    break;
            }

            if (shouldShow) {
                matchingServices++;
                Log.d(TAG, "  ✅ " + service.getName() + " (Tipo: " + serviceType +
                        ", En cuarto: " + service.isIncludedInRoom() + ")");
            }
        }

        Log.d(TAG, "📊 Servicios que coinciden con filtro '" + filterType + "': " + matchingServices + "/" + allServices.size());
    }

    /**
     * ✅ Verificar configuración completa del sistema
     */
    public static void debugCompleteSystem(List<HotelService> services, String[] includedServiceIds,
                                           Set<String> selectedServiceIds, double roomPrice, double taxiMinAmount) {
        Log.d(TAG, "========== DEBUG SISTEMA COMPLETO ==========");

        debugServiceCategories(services, includedServiceIds);
        debugTaxiConfiguration(services, roomPrice, taxiMinAmount);
        debugCartCalculation(services, selectedServiceIds, roomPrice, taxiMinAmount);

        Log.d(TAG, "========== FIN DEBUG SISTEMA COMPLETO ==========");
    }
}