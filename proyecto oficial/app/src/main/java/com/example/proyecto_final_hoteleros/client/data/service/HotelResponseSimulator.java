package com.example.proyecto_final_hoteleros.client.data.service;

import android.os.Handler;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HotelResponseSimulator {
    private static final String TAG = "HotelResponseSimulator";

    private static HotelResponseSimulator instance;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mMessagesRef;
    private Map<String, ChildEventListener> messageListeners = new HashMap<>();
    private Handler responseHandler = new Handler();
    private Random random = new Random();

    // Lista de respuestas automáticas según palabras clave
    private static final Map<String, List<String>> AUTO_RESPONSES = new HashMap<>();
    static {
        // Saludos
        List<String> greetings = new ArrayList<>();
        greetings.add("¡Hola! Bienvenido al servicio de chat del hotel. ¿En qué podemos ayudarle?");
        greetings.add("¡Buenos días! Gracias por contactar con nosotros. ¿Cómo podemos asistirle hoy?");
        greetings.add("Saludos, estamos a su disposición. ¿En qué podemos servirle?");
        AUTO_RESPONSES.put("saludo", greetings);

        // Habitación
        List<String> room = new ArrayList<>();
        room.add("Nuestro servicio de habitaciones está disponible 24/7. ¿Qué necesita que le enviemos?");
        room.add("Con gusto atenderemos su solicitud para la habitación. ¿Podría especificar qué necesita?");
        room.add("Enseguida enviaremos a alguien para atender sus necesidades. ¿Hay algo específico que requiera?");
        AUTO_RESPONSES.put("habitacion", room);

        // Instalaciones
        List<String> facilities = new ArrayList<>();
        facilities.add("Nuestras instalaciones de ocio están en la planta baja. La piscina está abierta de 7AM a 10PM, el spa de 9AM a 9PM, y el gimnasio 24 horas.");
        facilities.add("Puede encontrar la piscina y el spa en la planta baja, siguiendo las indicaciones. El gimnasio está en el primer piso y abierto las 24 horas.");
        facilities.add("Todas nuestras instalaciones están señalizadas en el lobby. El personal de recepción también puede acompañarle si lo desea.");
        AUTO_RESPONSES.put("instalaciones", facilities);

        // Comida
        List<String> food = new ArrayList<>();
        food.add("Nuestro restaurante principal sirve desayuno de 6AM a 10:30AM, almuerzo de 12:30PM a 3PM, y cena de 7PM a 10:30PM. ¿Desea hacer una reserva?");
        food.add("Tenemos varios restaurantes en el hotel. El restaurante principal está en la planta baja, y el bar de la azotea ofrece comidas ligeras hasta la medianoche.");
        food.add("Puede consultar nuestro menú de servicio a la habitación en la carpeta de información en su habitación, o podemos enviarle una copia digital si lo prefiere.");
        AUTO_RESPONSES.put("comida", food);

        // Checkout
        List<String> checkout = new ArrayList<>();
        checkout.add("El checkout estándar es a las 12 del mediodía. Podemos ofrecer late checkout dependiendo de la disponibilidad con un cargo adicional.");
        checkout.add("Estaremos encantados de ayudarle con su checkout. ¿A qué hora tiene previsto salir?");
        checkout.add("Para el checkout temprano, solo necesita pasar por recepción. Podemos preparar su factura con antelación si lo desea.");
        AUTO_RESPONSES.put("checkout", checkout);

        // Agradecimiento
        List<String> thanks = new ArrayList<>();
        thanks.add("Ha sido un placer ayudarle. No dude en contactarnos si necesita cualquier otra cosa.");
        thanks.add("Gracias a usted por elegirnos. Estamos aquí para hacer su estancia lo más agradable posible.");
        thanks.add("Nos alegra haber sido de ayuda. ¿Hay algo más en lo que podamos asistirle?");
        AUTO_RESPONSES.put("gracias", thanks);

        // Respuesta genérica
        List<String> generic = new ArrayList<>();
        generic.add("Gracias por su mensaje. Uno de nuestros representantes le atenderá en breve. ¿Hay algo específico en lo que podamos ayudarle mientras tanto?");
        generic.add("Hemos recibido su mensaje. ¿Podría proporcionarnos más detalles para poder ayudarle mejor?");
        generic.add("Estamos revisando su consulta. Un miembro de nuestro equipo le responderá lo antes posible.");
        AUTO_RESPONSES.put("generico", generic);
    }

    private HotelResponseSimulator() {
        mDatabase = FirebaseDatabase.getInstance();
        mMessagesRef = mDatabase.getReference("messages");
    }

    public static HotelResponseSimulator getInstance() {
        if (instance == null) {
            instance = new HotelResponseSimulator();
        }
        return instance;
    }

    public void startListening(String chatId, final String hotelId) {
        // Si ya estamos escuchando, detenemos la escucha anterior
        stopListening(chatId);

        // Consultar solo por mensajes nuevos de usuario
        Query query = mMessagesRef.child(chatId).orderByChild("type").equalTo("USER");

        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                try {
                    // Extraer datos del mensaje
                    if (dataSnapshot.exists() && dataSnapshot.child("senderId").exists()) {
                        String senderId = dataSnapshot.child("senderId").getValue(String.class);
                        String text = dataSnapshot.child("text").getValue(String.class);
                        String messageId = dataSnapshot.getKey();

                        // Verificar que sea un mensaje reciente (menos de 1 minuto)
                        long timestamp = dataSnapshot.child("timestamp").getValue(Long.class);
                        if (System.currentTimeMillis() - timestamp < 60000) {
                            // Generar respuesta automática después de un tiempo aleatorio
                            scheduleAutoResponse(chatId, hotelId, senderId, text, messageId);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error procesando mensaje: " + e.getMessage());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // No necesitamos manejar cambios
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // No necesitamos manejar eliminaciones
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // No necesitamos manejar movimientos
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error en listener de mensajes: " + databaseError.getMessage());
            }
        };

        query.addChildEventListener(listener);
        messageListeners.put(chatId, listener);

        Log.d(TAG, "Iniciado simulador de respuestas para chat: " + chatId);
    }

    public void stopListening(String chatId) {
        if (messageListeners.containsKey(chatId)) {
            mMessagesRef.child(chatId).removeEventListener(messageListeners.get(chatId));
            messageListeners.remove(chatId);
            Log.d(TAG, "Detenido simulador de respuestas para chat: " + chatId);
        }
    }

    private void scheduleAutoResponse(final String chatId, final String hotelId,
                                      final String userId, final String userMessage,
                                      final String userMessageId) {
        // Tiempo aleatorio entre 2 y 6 segundos para simular escritura
        int delay = 2000 + random.nextInt(4000);

        responseHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Generar ID único para el mensaje
                DatabaseReference newMessageRef = mMessagesRef.child(chatId).push();
                String messageId = newMessageRef.getKey();

                // Generar respuesta basada en el mensaje del usuario
                String response = generateResponse(userMessage);

                // Crear mensaje
                Map<String, Object> messageData = new HashMap<>();
                messageData.put("id", messageId);
                messageData.put("senderId", hotelId);
                messageData.put("receiverId", userId);
                messageData.put("text", response);
                messageData.put("timestamp", System.currentTimeMillis());
                messageData.put("type", "HOTEL");
                messageData.put("read", false);

                // Guardar en Firebase
                newMessageRef.setValue(messageData);

                Log.d(TAG, "Respuesta automática enviada: " + response);
            }
        }, delay);
    }

    private String generateResponse(String userMessage) {
        if (userMessage == null) {
            return getRandomResponse("generico");
        }

        String message = userMessage.toLowerCase();

        // Detectar palabras clave en el mensaje
        if (message.contains("hola") || message.contains("buenos días") ||
                message.contains("buenas") || message.contains("saludos")) {
            return getRandomResponse("saludo");
        } else if (message.contains("habitaci") || message.contains("cuarto") ||
                message.contains("suite") || message.contains("servicio al cuarto")) {
            return getRandomResponse("habitacion");
        } else if (message.contains("piscina") || message.contains("spa") ||
                message.contains("gym") || message.contains("gimnasio")) {
            return getRandomResponse("instalaciones");
        } else if (message.contains("restaurante") || message.contains("comer") ||
                message.contains("comida") || message.contains("desayuno") ||
                message.contains("almuerzo") || message.contains("cena")) {
            return getRandomResponse("comida");
        } else if (message.contains("check") || message.contains("salida") ||
                message.contains("factura")) {
            return getRandomResponse("checkout");
        } else if (message.contains("gracias") || message.contains("agradezco") ||
                message.contains("muchas gracias")) {
            return getRandomResponse("gracias");
        } else {
            return getRandomResponse("generico");
        }
    }

    private String getRandomResponse(String category) {
        List<String> responses = AUTO_RESPONSES.get(category);
        if (responses == null || responses.isEmpty()) {
            responses = AUTO_RESPONSES.get("generico");
        }

        int index = random.nextInt(responses.size());
        return responses.get(index);
    }

    public void cleanup() {
        // Detener todos los listeners
        for (String chatId : new ArrayList<>(messageListeners.keySet())) {
            stopListening(chatId);
        }

        // Limpiar todos los callbacks pendientes
        responseHandler.removeCallbacksAndMessages(null);
    }
}