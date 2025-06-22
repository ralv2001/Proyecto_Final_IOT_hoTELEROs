package com.example.proyecto_final_hoteleros.client.domain.interfaces;

import com.example.proyecto_final_hoteleros.client.data.model.HotelService;

/**
 * Interface para manejar los eventos de selección de servicios
 */
public interface ServiceSelectListener {
    /**
     * Llamado cuando un servicio es seleccionado o deseleccionado
     * @param service El servicio que fue seleccionado
     * @param isSelected Si el servicio fue seleccionado (true) o deseleccionado (false)
     */
    void onServiceSelected(HotelService service, boolean isSelected);
}