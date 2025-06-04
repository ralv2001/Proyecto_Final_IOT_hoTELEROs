package com.example.proyecto_final_hoteleros.client.manager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

/**
 * Clase para gestionar los servicios del hotel y las lógicas relacionadas
 */
public class ServicesManager {
    private static final double TAXI_MINIMUM_AMOUNT = 350.0;

    /**
     * Verifica si el usuario califica para el taxi gratuito
     * @param reservationTotal El monto total de la reserva
     * @return true si califica para taxi gratis, false en caso contrario
     */
    public static boolean qualifiesForFreeTaxi(double reservationTotal) {
        return reservationTotal >= TAXI_MINIMUM_AMOUNT;
    }

    /**
     * Verifica en el checkout y ofrece taxi si corresponde
     * @param context Contexto de la aplicación
     * @param reservationTotal El monto total de la reserva
     */
    public static void checkoutServiceValidation(Context context, double reservationTotal) {
        if (qualifiesForFreeTaxi(reservationTotal)) {
            Toast.makeText(context,
                    "¡Felicidades! Calificas para el servicio de taxi gratuito al aeropuerto",
                    Toast.LENGTH_LONG).show();
            // Aquí se podría mostrar un diálogo para confirmar
        } else {
            // Ofrecer el servicio de taxi por su precio normal
            showTaxiOfferDialog(context);
        }
    }

    /**
     * Muestra un diálogo ofreciendo el servicio de taxi pagado
     * @param context Contexto de la aplicación
     */
    private static void showTaxiOfferDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Servicio de Taxi");
        builder.setMessage("¿Deseas añadir el servicio de taxi al aeropuerto por S/. 60.00?");
        builder.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Lógica para añadir el taxi al carrito
                Toast.makeText(context, "Servicio de taxi añadido", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("No, gracias", null);
        builder.show();
    }

    /**
     * Obtiene el precio del taxi según si califica para gratis o no
     * @param reservationTotal El monto total de la reserva
     * @return El precio del taxi (0.0 si es gratis)
     */
    public static double getTaxiPrice(double reservationTotal) {
        return qualifiesForFreeTaxi(reservationTotal) ? 0.0 : 60.0;
    }
}