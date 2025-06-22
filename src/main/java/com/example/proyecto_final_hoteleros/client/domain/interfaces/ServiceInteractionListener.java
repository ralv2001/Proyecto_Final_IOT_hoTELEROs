package com.example.proyecto_final_hoteleros.client.domain.interfaces;

import com.example.proyecto_final_hoteleros.client.data.model.HotelService;

public interface ServiceInteractionListener {
    /**
     * Llamado cuando un servicio es seleccionado o deseleccionado
     */
    void onServiceSelected(HotelService service, boolean isSelected);

    /**
     * Llamado cuando se solicita más información sobre un servicio
     */
    void onServiceInfoRequested(HotelService service);
}