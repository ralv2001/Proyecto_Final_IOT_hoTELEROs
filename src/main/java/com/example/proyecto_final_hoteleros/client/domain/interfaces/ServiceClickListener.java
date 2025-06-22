package com.example.proyecto_final_hoteleros.client.domain.interfaces;

import com.example.proyecto_final_hoteleros.client.data.model.HotelService;

/**
 * Interface para manejar los clics en servicios
 */
public interface ServiceClickListener {
    /**
     * Llamado cuando se hace clic en un servicio
     * @param service El servicio en el que se hizo clic
     */
    void onServiceClicked(HotelService service);
}