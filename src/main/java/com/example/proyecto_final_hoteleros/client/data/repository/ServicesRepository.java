// Actualización de ServicesRepository.java
package com.example.proyecto_final_hoteleros.client.data.repository;

import android.util.Log;

import com.example.proyecto_final_hoteleros.client.data.model.HotelService;
import com.example.proyecto_final_hoteleros.client.data.model.HotelService.ServiceCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServicesRepository {
    private static ServicesRepository instance;
    private List<HotelService> allServices;

    private ServicesRepository() {
        initializeServices();
    }

    public static ServicesRepository getInstance() {
        if (instance == null) {
            instance = new ServicesRepository();
        }
        return instance;
    }

    private void initializeServices() {
        allServices = new ArrayList<>();

        // ✅ SERVICIOS BÁSICOS INCLUIDOS
        allServices.add(new HotelService(
                "wifi", "WiFi Premium",
                "Conexión de alta velocidad en todas las áreas del hotel.",
                null, // ✅ GRATIS
                null,
                Arrays.asList("wifi_lobby", "wifi_room"),
                "ic_wifi", false, null,
                ServiceCategory.ESSENTIALS, true, 0, "24/7",
                new String[]{"Velocidad hasta 100 Mbps", "Cobertura completa"},
                false // ✅ NO está marcado como incluido en habitación por defecto
        ));
        allServices.add(new HotelService(
                "taxi", "Taxi Premium al Aeropuerto",
                "Traslado ejecutivo al aeropuerto Jorge Chávez.",
                60.0,
                null,
                Arrays.asList("mercedes_exterior", "driver_professional"),
                "ic_taxi", true,
                "GRATIS si tu reserva total supera S/. 350",
                ServiceCategory.TRANSPORT, true, 1, "Según horario",
                new String[]{"Mercedes-Benz", "Chofer bilingüe", "WiFi a bordo"},
                false
        ));
        allServices.add(new HotelService(
                "reception", "Recepción 24/7",
                "Atención personalizada las 24 horas.",
                null,
                null,
                Arrays.asList("reception_desk"),
                "ic_reception", false, null,
                ServiceCategory.ESSENTIALS, false, 0, "24/7",
                new String[]{"Check-in/out express", "Conserjería especializada"},
                false
        ));

        allServices.add(new HotelService(
                "pool", "Piscina Infinity",
                "Piscina climatizada con vista panorámica.",
                null,
                null,
                Arrays.asList("pool_day", "pool_night"),
                "ic_pool", false, null,
                ServiceCategory.ESSENTIALS, true, 0, "6:00 AM - 10:00 PM",
                new String[]{"Climatizada", "Vista panorámica", "Camastros"},
                false
        ));

        allServices.add(new HotelService(
                "parking", "Estacionamiento Vigilado",
                "Estacionamiento seguro las 24 horas.",
                null,
                null,
                Arrays.asList("parking_entrance.jpg"),
                "ic_parking", false, null,
                ServiceCategory.ESSENTIALS, false, 0, "24/7",
                new String[]{"Vigilancia 24h", "Valet parking"},
                false
        ));

        // ✅ SERVICIO CONDICIONAL - TAXI
        allServices.add(new HotelService(
                "taxi", "Taxi Premium al Aeropuerto",
                "Traslado ejecutivo al aeropuerto Jorge Chávez.",
                60.0,
                null,
                Arrays.asList("mercedes_exterior.jpg", "driver_professional.jpg"),
                "ic_taxi", true,
                "GRATIS si tu reserva total supera S/. 350",
                ServiceCategory.TRANSPORT, true, 1, "Según horario",
                new String[]{"Mercedes-Benz", "Chofer bilingüe", "WiFi a bordo"},
                false
        ));

        // ✅ SERVICIOS DE PAGO CON FOTOS
        allServices.add(new HotelService(
                "breakfast", "Desayuno Gourmet en Habitación",
                "Desayuno continental premium servido en la privacidad de tu habitación.",
                45.0,
                null, // ✅ AGREGAR: imageUrl (String)
                Arrays.asList("breakfast_setup.jpg", "breakfast_variety.jpg", "room_service_delivery.jpg"),
                "ic_breakfast", false, null,
                ServiceCategory.GASTRONOMY, true, 2, "6:30 AM - 11:00 AM",
                new String[]{"Menú personalizable", "Jugos recién exprimidos", "Pan artesanal horneado", "Café de especialidad", "Opciones sin gluten y veganas"},
                false
        ));

        allServices.add(new HotelService(
                "spa", "Spa & Wellness Experience",
                "Experiencia completa de relajación con masaje de 45 minutos incluido.",
                120.0,
                null, // ✅ AGREGAR: imageUrl (String)
                Arrays.asList("spa_treatment_room.jpg", "spa_jacuzzi.jpg", "spa_sauna.jpg", "spa_relaxation.jpg"),
                "ic_spa", false, null,
                ServiceCategory.WELLNESS, true, 1, "9:00 AM - 9:00 PM",
                new String[]{"Masaje terapéutico 45 min", "Acceso a sauna finlandesa", "Jacuzzi con hidroterapia", "Aromaterapia incluida", "Té de hierbas de cortesía"},
                false
        ));

        allServices.add(new HotelService(
                "gym", "Entrenamiento Personal Privado",
                "Sesión privada de 60 minutos con entrenador certificado en gimnasio completamente equipado.",
                75.0,
                null, // ✅ AGREGAR: imageUrl (String)
                Arrays.asList("gym_equipment.jpg", "personal_trainer.jpg", "gym_cardio.jpg"),
                "ic_gym", false, null,
                ServiceCategory.WELLNESS, false, 2, "6:00 AM - 10:00 PM",
                new String[]{"Evaluación física completa", "Rutina personalizada", "Equipos Technogym", "Hidratación premium", "Seguimiento progreso"},
                false
        ));

        allServices.add(new HotelService(
                "laundry", "Servicio de Lavandería Express",
                "Servicio profesional de lavandería y planchado con entrega el mismo día.",
                35.0,
                null, // ✅ AGREGAR: imageUrl (String)
                Arrays.asList("laundry_facility.jpg", "professional_cleaning.jpg"),
                "ic_laundry", false, null,
                ServiceCategory.COMFORT, false, 5, "8:00 AM - 8:00 PM",
                new String[]{"Lavado y planchado profesional", "Entrega mismo día", "Tratamiento prendas delicadas", "Embalaje especial"},
                false
        ));

        allServices.add(new HotelService(
                "minibar", "Minibar Premium Gourmet",
                "Selección exclusiva de bebidas premium, snacks gourmet y productos artesanales locales.",
                80.0,
                null, // ✅ AGREGAR: imageUrl (String)
                Arrays.asList("minibar_selection.jpg", "premium_drinks.jpg", "local_products.jpg"),
                "ic_minibar", false, null,
                ServiceCategory.GASTRONOMY, false, 1, "24/7",
                new String[]{"Bebidas premium importadas", "Snacks gourmet artesanales", "Productos locales exclusivos", "Reposición diaria gratuita"},
                false
        ));

        allServices.add(new HotelService(
                "room_service", "Room Service 24/7",
                "Servicio de habitaciones las 24 horas con menú ejecutivo completo.",
                25.0,
                null, // ✅ AGREGAR: imageUrl (String)
                Arrays.asList("room_service_setup.jpg", "gourmet_dinner.jpg", "elegant_presentation.jpg"),
                "ic_room_service", false, null,
                ServiceCategory.GASTRONOMY, false, 3, "24/7",
                new String[]{"Menú ejecutivo completo", "Vajilla premium", "Presentación gourmet", "Servicio silencioso"},
                false
        ));
    }

    public boolean isTaxiFreeForTotal(double totalAmount) {
        return totalAmount >= 350.0;
    }

    public String getTaxiStatusMessage(double currentTotal) {
        if (currentTotal >= 350.0) {
            return "🎉 ¡GRATIS! Has alcanzado el mínimo de S/. 350";
        } else {
            double needed = 350.0 - currentTotal;
            return String.format("💡 Agrega S/. %.0f más para obtenerlo GRATIS", needed);
        }
    }

    // ✅ NUEVO: Método para obtener servicios destacados (4 básicos + taxi)
    public List<HotelService> getFeaturedServices() {
        // ✅ CREAR nueva lista cada vez para evitar referencias compartidas
        List<HotelService> featured = new ArrayList<>();

        // Solo servicios básicos que TODOS los cuartos tienen
        String[] basicServiceIds = {"wifi", "reception", "pool", "parking"};

        for (String id : basicServiceIds) {
            HotelService service = getServiceById(id);
            if (service != null) {
                // ✅ CREAR COPIA del servicio para evitar modificaciones compartidas
                HotelService serviceCopy = createServiceCopy(service);
                featured.add(serviceCopy);
            }
        }

        Log.d("ServicesRepository", "Featured services creados: " + featured.size());
        return featured; // Siempre retorna exactamente 4 servicios únicos
    }
    private HotelService createServiceCopy(HotelService original) {
        return new HotelService(
                original.getId(),
                original.getName(),
                original.getDescription(),
                original.getPrice(),
                original.getImageUrl(),
                original.getImageUrls(),
                original.getIconResourceName(),
                original.isConditional(),
                original.getConditionalDescription(),
                original.getCategory(),
                original.isPopular(),
                original.getSortOrder(),
                original.getAvailability(),
                original.getFeatures() != null ? original.getFeatures().toArray(new String[0]) : null,
                original.isIncludedInRoom()
        );
    }

    // Métodos existentes se mantienen igual...
    public List<HotelService> getAllServices() {
        return new ArrayList<>(allServices);
    }

    public List<HotelService> getServicesByCategory(ServiceCategory category) {
        return allServices.stream()
                .filter(service -> service.getCategory() == category)
                .collect(Collectors.toList());
    }

    public HotelService getServiceById(String id) {
        return allServices.stream()
                .filter(service -> service.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void updateServiceEligibility(String serviceId, boolean isEligible) {
        HotelService service = getServiceById(serviceId);
        if (service != null) {
            service.setEligibleForFree(isEligible);
        }
    }

    // ✅ NUEVO: Método para obtener servicios disponibles para agregar a habitación
    public List<HotelService> getAdditionalServices() {
        List<HotelService> additional = new ArrayList<>();

        // ✅ SOLO servicios que se pueden agregar como extras
        String[] additionalIds = {"taxi", "breakfast", "spa", "gym", "laundry", "minibar", "room_service"};

        for (String id : additionalIds) {
            HotelService service = getServiceById(id);
            if (service != null) {
                additional.add(service);
            }
        }

        Log.d("ServicesRepository", "Servicios adicionales disponibles: " + additional.size());
        return additional;
    }
}