package com.example.proyecto_final_hoteleros.client.data.repository;

import com.example.proyecto_final_hoteleros.client.data.model.HotelService;
import com.example.proyecto_final_hoteleros.client.data.model.HotelService.ServiceCategory;
import java.util.ArrayList;
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

        // Servicios Gratuitos (Incluidos)
        allServices.add(new HotelService(
                "wifi", "WiFi Premium",
                "Conexión de alta velocidad en todas las áreas del hotel. Ideal para streaming, videollamadas y trabajo remoto con velocidades de hasta 100 Mbps.",
                null, null, "ic_wifi", false, null,
                ServiceCategory.ESSENTIALS, true, 0, "24/7",
                new String[]{"Conexión ilimitada", "Soporte técnico 24/7", "Acceso en áreas comunes", "Velocidad garantizada"}
        ));

        allServices.add(new HotelService(
                "reception", "Recepción 24/7",
                "Atención personalizada las 24 horas del día. Nuestro equipo de conserjería está disponible para asistirte con cualquier necesidad durante tu estadía.",
                null, null, "ic_reception", false, null,
                ServiceCategory.ESSENTIALS, false, 0, "24/7",
                new String[]{"Check-in/out express", "Custodia de equipaje", "Información turística", "Reservas de restaurantes"}
        ));

        allServices.add(new HotelService(
                "pool", "Piscina Infinity",
                "Piscina climatizada con vista panorámica al océano Pacífico. Disfruta de un ambiente relajante con servicio de bebidas y aperitivos.",
                null, null, "ic_pool", false, null,
                ServiceCategory.ESSENTIALS, true, 0, "6:00 AM - 10:00 PM",
                new String[]{"Toallas incluidas", "Sombrillas y camastros", "Servicio de bebidas", "Vista al océano"}
        ));

        allServices.add(new HotelService(
                "parking", "Estacionamiento Vigilado",
                "Estacionamiento seguro las 24 horas con servicio de valet parking opcional. Incluye carga para vehículos eléctricos.",
                null, null, "ic_parking", false, null,
                ServiceCategory.ESSENTIALS, false, 0, "24/7",
                new String[]{"Vigilancia 24h", "Valet parking", "Carga para vehículos eléctricos", "Espacios techados"}
        ));

        // Servicio Condicional - Taxi
        allServices.add(new HotelService(
                "taxi", "Taxi Premium al Aeropuerto ⭐",
                "Traslado ejecutivo en vehículo de lujo con chofer profesional bilingüe. Incluye WiFi a bordo, agua embotellada y snacks gourmet para hacer tu viaje más cómodo.",
                60.0, null, "ic_taxi", true,
                "¡GRATIS con reservas desde S/. 350! Ahorra S/. 60",
                ServiceCategory.TRANSPORT, true, 1, "Según itinerario de vuelo",
                new String[]{"Vehículo ejecutivo Mercedes-Benz", "WiFi a bordo", "Agua y snacks premium", "Seguro de viaje incluido", "Chofer bilingüe"}
        ));

        // Servicios De Pago
        allServices.add(new HotelService(
                "breakfast", "Desayuno Gourmet a la Habitación",
                "Desayuno continental premium servido en la privacidad de tu habitación. Menú personalizable con ingredientes frescos y opciones para dietas especiales.",
                45.0, null, "ic_breakfast", false, null,
                ServiceCategory.GASTRONOMY, true, 2, "6:30 AM - 11:00 AM",
                new String[]{"Menú personalizable", "Jugos naturales recién exprimidos", "Pan artesanal horneado", "Café premium de especialidad", "Opciones veganas y sin gluten"}
        ));

        allServices.add(new HotelService(
                "spa", "Spa & Wellness Experience",
                "Experiencia completa de relajación que incluye acceso al spa, sauna finlandesa, jacuzzi y una sesión de masaje relajante de 45 minutos con aceites aromáticos.",
                120.0, null, "ic_spa", false, null,
                ServiceCategory.WELLNESS, true, 1, "9:00 AM - 9:00 PM",
                new String[]{"Masaje de 45 minutos", "Sauna y baño de vapor", "Jacuzzi con hidroterapia", "Té de hierbas de cortesía", "Aceites aromáticos premium"}
        ));

        allServices.add(new HotelService(
                "gym", "Entrenamiento Personal Privado",
                "Sesión privada de 60 minutos con un entrenador personal certificado en nuestro gimnasio completamente equipado. Incluye evaluación física y rutina personalizada.",
                75.0, null, "ic_gym", false, null,
                ServiceCategory.WELLNESS, false, 2, "6:00 AM - 10:00 PM",
                new String[]{"Evaluación física completa", "Rutina personalizada", "Hidratación premium", "Toalla de gimnasio", "Acceso exclusivo por 1 hora"}
        ));

        allServices.add(new HotelService(
                "laundry", "Servicio de Lavandería Express",
                "Servicio profesional de lavandería y planchado con entrega garantizada el mismo día. Tratamiento especial para prendas delicadas y de alta calidad.",
                35.0, null, "ic_laundry", false, null,
                ServiceCategory.COMFORT, false, 5, "8:00 AM - 8:00 PM",
                new String[]{"Lavado y planchado profesional", "Entrega el mismo día", "Embalaje especial", "Tratamiento para prendas delicadas"}
        ));

        allServices.add(new HotelService(
                "minibar", "Minibar Premium Gourmet",
                "Selección exclusiva de bebidas premium, snacks gourmet y productos artesanales locales. Reposición diaria incluida durante tu estadía.",
                80.0, null, "ic_minibar", false, null,
                ServiceCategory.GASTRONOMY, false, 1, "24/7",
                new String[]{"Bebidas premium importadas", "Snacks gourmet artesanales", "Productos locales exclusivos", "Reposición diaria"}
        ));

        allServices.add(new HotelService(
                "meeting", "Sala de Reuniones Ejecutiva",
                "Sala de reuniones privada con capacidad para 10 personas, equipada con tecnología de última generación. Incluye coffee break y servicio de catering.",
                150.0, null, "ic_meeting", false, null,
                ServiceCategory.BUSINESS, false, 1, "8:00 AM - 6:00 PM",
                new String[]{"Proyector 4K", "WiFi dedicado de alta velocidad", "Coffee break incluido", "Material de oficina", "Servicio de catering opcional"}
        ));

        allServices.add(new HotelService(
                "tour", "Tour Gastronómico por Lima",
                "Recorrido guiado de 4 horas por los mejores restaurantes, mercados tradicionales y locales gastronómicos de Lima. Incluye degustaciones y transporte.",
                95.0, null, "ic_tour", false, null,
                ServiceCategory.GASTRONOMY, true, 4, "10:00 AM - 2:00 PM",
                new String[]{"Guía gastronómico especializado", "Transporte incluido", "Degustaciones en 5 locales", "Seguro de excursión", "Certificado de participación"}
        ));

        allServices.add(new HotelService(
                "room_service", "Room Service 24/7",
                "Servicio de habitaciones las 24 horas con menú ejecutivo completo. Desde cenas elegantes hasta snacks nocturnos, todo servido en la comodidad de tu habitación.",
                25.0, null, "ic_room_service", false, null,
                ServiceCategory.GASTRONOMY, false, 3, "24/7",
                new String[]{"Menú ejecutivo completo", "Servicio 24 horas", "Vajilla premium", "Presentación gourmet"}
        ));
    }

    public List<HotelService> getAllServices() {
        return new ArrayList<>(allServices);
    }

    public List<HotelService> getServicesByCategory(ServiceCategory category) {
        return allServices.stream()
                .filter(service -> service.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<HotelService> getServicesByType(String type) {
        return allServices.stream()
                .filter(service -> service.getServiceType().equals(type))
                .collect(Collectors.toList());
    }

    public List<HotelService> getFeaturedServices() {
        return allServices.stream()
                .filter(HotelService::isPopular)
                .limit(4)
                .collect(Collectors.toList());
    }

    public List<HotelService> getFreeServices() {
        return allServices.stream()
                .filter(service -> service.isFree() && !service.isConditional())
                .collect(Collectors.toList());
    }

    public List<HotelService> getPaidServices() {
        return allServices.stream()
                .filter(service -> !service.isFree() && !service.isConditional())
                .collect(Collectors.toList());
    }

    public List<HotelService> getConditionalServices() {
        return allServices.stream()
                .filter(HotelService::isConditional)
                .collect(Collectors.toList());
    }

    public HotelService getServiceById(String id) {
        return allServices.stream()
                .filter(service -> service.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<ServiceCategory> getAllCategories() {
        return List.of(ServiceCategory.values());
    }

    public void updateServiceEligibility(String serviceId, boolean isEligible) {
        HotelService service = getServiceById(serviceId);
        if (service != null) {
            service.setEligibleForFree(isEligible);
        }
    }
}