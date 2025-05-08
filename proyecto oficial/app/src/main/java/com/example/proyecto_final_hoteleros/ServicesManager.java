package com.example.proyecto_final_hoteleros;

import android.content.Context;
import android.widget.Toast;

public class ServicesManager {
    private static final double TAXI_MINIMUM_AMOUNT = 350.0;

    // Verifica si el usuario califica para el taxi gratuito
    public static boolean qualifiesForFreeTaxi(double reservationTotal) {
        return reservationTotal >= TAXI_MINIMUM_AMOUNT;
    }

    // Verifica en el checkout y ofrece taxi si corresponde
    public static void checkoutServiceValidation(Context context, double reservationTotal) {
        if (qualifiesForFreeTaxi(reservationTotal)) {
            Toast.makeText(context,
                    "¡Felicidades! Calificas para el servicio de taxi gratuito al aeropuerto",
                    Toast.LENGTH_LONG).show();
            // Aquí podrías mostrar un diálogo para confirmar
        } else {
            // Aquí podrías ofrecer el servicio de taxi por su precio normal
            showTaxiOfferDialog(context);
        }
    }

    private static void showTaxiOfferDialog(Context context) {
        // Implementar diálogo para ofrecer el servicio de taxi por su precio
        // Esto sería un AlertDialog personalizado
    }
}