package com.example.proyecto_final_hoteleros.taxista.services;

import android.os.Handler;
import android.os.Looper;
import com.example.proyecto_final_hoteleros.taxista.utils.NotificationHelper;
import com.example.proyecto_final_hoteleros.taxista.utils.DriverPreferenceManager;
import java.util.Random;

public class MockNotificationService {

    private NotificationHelper notificationHelper;
    private DriverPreferenceManager preferenceManager;
    private Handler handler;
    private Random random;
    private boolean isRunning = false;

    // Contadores para simular el estado del conductor
    private int hoursActive = 0;
    private double dailyEarnings = 0.0;
    private int tripsToday = 0;

    // Datos de ejemplo para notificaciones
    private String[] hotelNames = {
            "Hotel Gran Plaza", "Hotel Miraflores Park", "Hotel Lima Centro",
            "Hotel San Isidro Business", "Hotel Barranco Boutique"
    };

    private String[] clientNames = {
            "Carlos Mendoza", "Ana García", "Luis Rodríguez",
            "María Torres", "José Hernández", "Carmen López", "Pedro Vargas", "Sofía Rivera"
    };

    private String[] pickupLocations = {
            "Av. La Marina 123, San Miguel",
            "Av. Malecón 456, Miraflores",
            "Jr. De la Unión 789, Lima Centro",
            "Av. República de Panamá 321, San Isidro",
            "Av. Pedro de Osma 654, Barranco"
    };

    private String[] highDemandZones = {
            "Miraflores", "San Isidro", "Barranco", "La Molina", "Surco"
    };

    private String[] clientFeedbacks = {
            "Excelente servicio, muy puntual",
            "Conductor muy amable y profesional",
            "Viaje cómodo y seguro",
            "Llegó rápido y fue muy cortés",
            "Auto limpio y en buen estado"
    };

    private String[] documents = {
            "licencia de conducir", "SOAT", "revisión técnica", "tarjeta de propiedad"
    };

    private String[] promoTitles = {
            "Promoción Fin de Semana", "Bonus Nocturno", "Hora Pico Extra", "Promoción Aeropuerto"
    };

    private String[] promoDetails = {
            "Completa 5 viajes", "Entre 10pm y 6am", "De 7am a 9am y 6pm a 8pm", "Viajes al aeropuerto"
    };

    private double[] prices = {45.50, 65.00, 85.50, 120.00, 95.75, 78.25};

    public MockNotificationService(NotificationHelper notificationHelper,
                                   DriverPreferenceManager preferenceManager) {
        this.notificationHelper = notificationHelper;
        this.preferenceManager = preferenceManager;
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
    }

    public void startMockNotifications() {
        if (isRunning) return;

        isRunning = true;
        scheduleNextNotification();

        // Programar notificaciones especiales con intervalos específicos
        scheduleDocumentExpirationCheck();
        scheduleRestSuggestionCheck();
        scheduleEarningsGoalCheck();
    }

    public void stopMockNotifications() {
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
    }

    public void scheduleNextNotification() {
        if (!isRunning) return;

        // Programar próxima notificación entre 20 segundos y 2 minutos
        int delay = 20000 + random.nextInt(100000); // 20s a 2min

        handler.postDelayed(() -> {
            if (isRunning && preferenceManager.isDriverAvailable()) {
                sendRandomNotification();
            }
            scheduleNextNotification();
        }, delay);
    }

    public void sendRandomNotification() {
        // Probabilidades diferentes para cada tipo de notificación
        int notificationType = random.nextInt(100);

        if (notificationType < 40) {
            // 40% - Solicitud de viaje
            sendRandomTripNotification();
        } else if (notificationType < 55) {
            // 15% - Viaje completado
            sendTripCompletedNotification();
        } else if (notificationType < 70) {
            // 15% - Zona de alta demanda
            sendHighDemandZoneNotification();
        } else if (notificationType < 80) {
            // 10% - Feedback de cliente
            sendClientFeedbackNotification();
        } else if (notificationType < 90) {
            // 10% - Promoción especial
            sendSpecialPromoNotification();
        } else {
            // 10% - Notificación general
            sendTestGeneralNotification();
        }
    }

    public void sendRandomTripNotification() {
        String hotel = hotelNames[random.nextInt(hotelNames.length)];
        String client = clientNames[random.nextInt(clientNames.length)];
        String pickup = pickupLocations[random.nextInt(pickupLocations.length)];
        double price = prices[random.nextInt(prices.length)];

        notificationHelper.showTripRequestNotification(hotel, client, pickup, price);
    }

    public void sendTripCompletedNotification() {
        String client = clientNames[random.nextInt(clientNames.length)];
        double earnings = 40.0 + (random.nextDouble() * 80.0); // Entre 40 y 120 soles
        float rating = 4.0f + (random.nextFloat() * 1.0f); // Entre 4.0 y 5.0

        // Simular acumulación de ganancias y viajes
        dailyEarnings += earnings;
        tripsToday++;

        notificationHelper.showTripCompletedNotification(client, earnings, rating);
    }

    public void sendHighDemandZoneNotification() {
        String zone = highDemandZones[random.nextInt(highDemandZones.length)];
        String estimatedEarnings = "S/ " + String.format("%.0f", 80 + (random.nextDouble() * 40)) + " - " +
                String.format("%.0f", 120 + (random.nextDouble() * 50));

        notificationHelper.showHighDemandZoneNotification(zone, estimatedEarnings);
    }

    public void sendClientFeedbackNotification() {
        String client = clientNames[random.nextInt(clientNames.length)];
        String feedback = clientFeedbacks[random.nextInt(clientFeedbacks.length)];
        float rating = 4.0f + (random.nextFloat() * 1.0f);

        notificationHelper.showClientFeedbackNotification(client, feedback, rating);
    }

    public void sendSpecialPromoNotification() {
        String title = promoTitles[random.nextInt(promoTitles.length)];
        String details = promoDetails[random.nextInt(promoDetails.length)];
        int bonus = 15 + random.nextInt(20); // Entre 15% y 35%

        notificationHelper.showSpecialPromoNotification(title, details, bonus);
    }

    // === NOTIFICACIONES PROGRAMADAS ===

    public void scheduleDocumentExpirationCheck() {
        // Verificar cada 30 minutos si hay documentos por vencer
        handler.postDelayed(() -> {
            if (isRunning && random.nextInt(100) < 10) { // 10% de probabilidad
                String document = documents[random.nextInt(documents.length)];
                int daysRemaining = 1 + random.nextInt(30); // Entre 1 y 30 días

                notificationHelper.showDocumentExpirationNotification(document, daysRemaining);
            }

            if (isRunning) {
                scheduleDocumentExpirationCheck(); // Reprogramar
            }
        }, 30 * 60 * 1000); // 30 minutos
    }

    private void scheduleRestSuggestionCheck() {
        // Verificar cada hora si es momento de sugerir descanso
        handler.postDelayed(() -> {
            if (isRunning && preferenceManager.isDriverAvailable()) {
                hoursActive++;

                // Sugerir descanso después de 4 horas
                if (hoursActive >= 4 && random.nextInt(100) < 30) { // 30% probabilidad
                    notificationHelper.showRestSuggestionNotification(hoursActive);
                    hoursActive = 0; // Resetear contador
                }
            }

            if (isRunning) {
                scheduleRestSuggestionCheck(); // Reprogramar
            }
        }, 60 * 60 * 1000); // 1 hora
    }

    private void scheduleEarningsGoalCheck() {
        // Verificar cada 2 horas si se alcanzó una meta de ganancias
        handler.postDelayed(() -> {
            if (isRunning) {
                double goalAmount = 150.0 + (random.nextDouble() * 100.0); // Meta entre 150-250

                if (dailyEarnings >= goalAmount && random.nextInt(100) < 20) { // 20% probabilidad
                    notificationHelper.showEarningsGoalNotification(goalAmount, dailyEarnings);
                }
            }

            if (isRunning) {
                scheduleEarningsGoalCheck(); // Reprogramar
            }
        }, 2 * 60 * 60 * 1000); // 2 horas
    }

    // === MÉTODOS PÚBLICOS PARA TESTING ===

    public void sendTestEarningsNotification() {
        double todayEarnings = 150.00 + (random.nextDouble() * 200.00);
        int trips = 3 + random.nextInt(8);

        notificationHelper.showEarningsNotification(todayEarnings, trips);

        // Actualizar ganancias en local storage
        preferenceManager.updateEarnings(todayEarnings, todayEarnings * 30);
        preferenceManager.updateCompletedTrips(trips);
    }

    public void sendTestGeneralNotification() {
        String[] messages = {
                "¡Tienes una calificación de 5 estrellas!",
                "Nuevo hotel asociado en tu zona",
                "Actualización de la aplicación disponible",
                "¡Has completado 100 viajes este mes!",
                "Felicitaciones por tu excelente servicio"
        };

        String message = messages[random.nextInt(messages.length)];
        notificationHelper.showGeneralNotification("Conductores App", message);
    }

    public void sendTestDocumentExpirationNotification() {
        String document = documents[random.nextInt(documents.length)];
        int days = 1 + random.nextInt(7);
        notificationHelper.showDocumentExpirationNotification(document, days);
    }

    public void sendTestRestSuggestionNotification() {
        int hours = 4 + random.nextInt(3);
        notificationHelper.showRestSuggestionNotification(hours);
    }

    public void sendTestHighDemandZoneNotification() {
        String zone = highDemandZones[random.nextInt(highDemandZones.length)];
        String earnings = "S/ 90 - 150";
        notificationHelper.showHighDemandZoneNotification(zone, earnings);
    }

    public void sendTestClientFeedbackNotification() {
        String client = clientNames[random.nextInt(clientNames.length)];
        String feedback = clientFeedbacks[random.nextInt(clientFeedbacks.length)];
        float rating = 4.5f + (random.nextFloat() * 0.5f);
        notificationHelper.showClientFeedbackNotification(client, feedback, rating);
    }

    public void sendTestSpecialPromoNotification() {
        String title = promoTitles[random.nextInt(promoTitles.length)];
        String details = promoDetails[random.nextInt(promoDetails.length)];
        notificationHelper.showSpecialPromoNotification(title, details, 25);
    }

    // Método para resetear contadores (útil para testing)
    public void resetCounters() {
        hoursActive = 0;
        dailyEarnings = 0.0;
        tripsToday = 0;
    }
}