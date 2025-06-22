package com.example.proyecto_final_hoteleros.adminhotel.utils;

import com.example.proyecto_final_hoteleros.adminhotel.model.BasicService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BasicServiceManager {

    // Servicios por defecto para hoteles
    private static final List<BasicService> DEFAULT_SERVICES = Arrays.asList(
            new BasicService("WiFi Gratis", "Internet inalámbrico gratuito en todo el hotel", "ic_wifi"),
            new BasicService("Recepción 24h", "Atención las 24 horas del día", "ic_reception"),
            new BasicService("Desayuno", "Desayuno continental incluido", "ic_breakfast"),
            new BasicService("Estacionamiento", "Parqueadero gratuito para huéspedes", "ic_parking"),
            new BasicService("Aire Acondicionado", "Climatización en todas las habitaciones", "ic_air_condition"),
            new BasicService("Televisión", "TV por cable en habitaciones", "ic_tv"),
            new BasicService("Servicio de Habitación", "Room service disponible", "ic_room_service"),
            new BasicService("Lavandería", "Servicio de lavandería y planchado", "ic_laundry")
    );

    public static List<BasicService> getDefaultServices() {
        return new ArrayList<>(DEFAULT_SERVICES);
    }

    public static List<BasicService> searchServices(String query) {
        List<BasicService> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (BasicService service : DEFAULT_SERVICES) {
            if (service.getName().toLowerCase().contains(lowerQuery) ||
                    service.getDescription().toLowerCase().contains(lowerQuery)) {
                results.add(service);
            }
        }

        return results;
    }
}