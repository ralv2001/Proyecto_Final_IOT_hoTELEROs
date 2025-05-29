package com.example.proyecto_final_hoteleros;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.BuildConfig;
import com.example.proyecto_final_hoteleros.client.fragment.HomeFragment;
import com.example.proyecto_final_hoteleros.client.model.ChatSummary;
import com.example.proyecto_final_hoteleros.services.FirebaseChatService;
import com.example.proyecto_final_hoteleros.services.HotelResponseSimulator;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";

    private HotelResponseSimulator responseSimulator;
    private FirebaseChatService chatService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Inicializar Firebase - asegúrate de que esto se ejecute primero
        try {
            FirebaseApp.initializeApp(this);
            testFirebaseConnection();
            Log.d(TAG, "Firebase inicializado correctamente");

            // CAMBIO AQUÍ: Inicializar chats de ejemplo automáticamente en modo DEBUG
            if (BuildConfig.DEBUG) {
                initializeExampleChats();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar Firebase: " + e.getMessage(), e);
        }


        // Inicializar servicios de Firebase
        initializeFirebaseServices();

        // Configurar sistema de insets para pantallas con notch
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Cargar el fragmento HomeFragment como fragmento inicial
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

    }

    private void initializeFirebaseServices() {
        // Inicializar servicio de chat
        chatService = FirebaseChatService.getInstance();

        // Inicializar y activar simulador de respuestas para los chats de demostración
        responseSimulator = HotelResponseSimulator.getInstance();

        // Cargar lista de chats del usuario actual
        chatService.loadChatSummaries(new FirebaseChatService.OnChatSummariesLoadedListener() {
            @Override
            public void onChatSummariesLoaded(List<ChatSummary> chatSummaries) {
                // Para cada chat activo, iniciar el simulador de respuestas
                for (ChatSummary chat : chatSummaries) {
                    if (chat.getStatus() == ChatSummary.ChatStatus.ACTIVE ||
                            chat.getStatus() == ChatSummary.ChatStatus.AVAILABLE) {
                        responseSimulator.startListening(chat.getId(), chat.getHotelId());
                    }
                }

                // También puedes iniciar los simuladores para los chats de demostración
                // si estás en modo desarrollo
                // Solo para desarrollo, hacer visible el botón
                if (BuildConfig.DEBUG) {
                    Button btnInitChats = findViewById(R.id.btnInitChats);
                    if (btnInitChats != null) {
                        btnInitChats.setVisibility(View.VISIBLE);
                        btnInitChats.setOnClickListener(v -> initializeExampleChats());
                    }
                }
            }

            @Override
            public void onChatSummariesError(String error) {
                // Si hay un error al cargar los chats, aún podemos iniciar los simuladores
                // para los chats de demostración en modo desarrollo
                // Solo para desarrollo, hacer visible el botón
                if (BuildConfig.DEBUG) {
                    Button btnInitChats = findViewById(R.id.btnInitChats);
                    if (btnInitChats != null) {
                        btnInitChats.setVisibility(View.VISIBLE);
                        btnInitChats.setOnClickListener(v -> initializeExampleChats());
                    }
                }
            }
        });
    }
    private void startResponseSimulators() {
        // Iniciar simuladores para todos los chats de prueba
        HotelResponseSimulator simulator = HotelResponseSimulator.getInstance();
        simulator.startListening("chat_available", "hotel_1");
        simulator.startListening("chat_active", "hotel_2");
        // No iniciamos para chat_finished porque ya está cerrado
    }
    // NUEVO: Método para inicializar chats de ejemplo en Firebase
    private void initializeExampleChats() {
        // Obtén el ID del usuario actual
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "user_1";

        // Crear timestamp base
        long now = System.currentTimeMillis();
        long oneDayAgo = now - 86400000;
        long twoDaysAgo = now - 172800000;
        long threeDaysAgo = now - 259200000;

        // ========== CHAT DISPONIBLE (AVAILABLE) ==========
        DatabaseReference chatAvailableRef = FirebaseDatabase.getInstance()
                .getReference("chatSummaries").child("chat_available");
        Map<String, Object> chatAvailable = new HashMap<>();
        chatAvailable.put("id", "chat_available");
        chatAvailable.put("hotelId", "hotel_1");
        chatAvailable.put("hotelName", "Hotel Las Palmeras");
        chatAvailable.put("reservationId", "12345");
        chatAvailable.put("reservationDates", "19-24 Mayo 2025");
        chatAvailable.put("status", "AVAILABLE"); // Asegurarnos de que este campo exista
        chatAvailable.put("userId", userId);
        chatAvailable.put("lastUpdated", now);
        chatAvailable.put("lastMessage", ""); // Agregar un valor por defecto

        chatAvailableRef.setValue(chatAvailable)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Chat AVAILABLE creado exitosamente");
                    // Mensaje de sistema para chat AVAILABLE
                    DatabaseReference msgRef = FirebaseDatabase.getInstance()
                            .getReference("messages").child("chat_available").child("system_welcome");

                    Map<String, Object> msgData = new HashMap<>();
                    msgData.put("id", "system_welcome");
                    msgData.put("senderId", "system");
                    msgData.put("receiverId", userId);
                    msgData.put("text", "Bienvenido a su nueva reserva. Puede iniciar una conversación con el hotel desde aquí.");
                    msgData.put("timestamp", now);
                    msgData.put("type", "SYSTEM");
                    msgData.put("read", false);

                    msgRef.setValue(msgData);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al crear chat AVAILABLE: " + e.getMessage()));

        // ========== CHAT ACTIVO (ACTIVE) ==========
        DatabaseReference chatActiveRef = FirebaseDatabase.getInstance()
                .getReference("chatSummaries").child("chat_active");

        Map<String, Object> chatActive = new HashMap<>();
        chatActive.put("id", "chat_active");
        chatActive.put("hotelId", "hotel_2");
        chatActive.put("hotelName", "Grand Hotel Central");
        chatActive.put("reservationId", "67890");
        chatActive.put("reservationDates", "15-18 Mayo 2025");
        chatActive.put("status", "ACTIVE");
        chatActive.put("userId", userId);
        chatActive.put("lastMessage", "¿Podría solicitar servicio de habitaciones?");
        chatActive.put("lastUpdated", now);

        chatActiveRef.setValue(chatActive)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Chat ACTIVE creado exitosamente");

                    // Crear mensajes para chat ACTIVE
                    DatabaseReference msgsRef = FirebaseDatabase.getInstance()
                            .getReference("messages").child("chat_active");

                    // Mensaje de bienvenida del sistema
                    Map<String, Object> msg1 = new HashMap<>();
                    msg1.put("id", "system_welcome");
                    msg1.put("senderId", "system");
                    msg1.put("receiverId", userId);
                    msg1.put("text", "Bienvenido al chat. Un representante del hotel le atenderá en breve.");
                    msg1.put("timestamp", threeDaysAgo);
                    msg1.put("type", "SYSTEM");
                    msg1.put("read", true);

                    // Primer mensaje del usuario
                    Map<String, Object> msg2 = new HashMap<>();
                    msg2.put("id", "user_msg_1");
                    msg2.put("senderId", userId);
                    msg2.put("receiverId", "hotel_2");
                    msg2.put("text", "Hola, ¿podrían indicarme cómo llegar a la piscina?");
                    msg2.put("timestamp", twoDaysAgo);
                    msg2.put("type", "USER");
                    msg2.put("read", true);

                    // Respuesta del hotel
                    Map<String, Object> msg3 = new HashMap<>();
                    msg3.put("id", "hotel_msg_1");
                    msg3.put("senderId", "hotel_2");
                    msg3.put("receiverId", userId);
                    msg3.put("text", "¡Buenos días! La piscina se encuentra en la planta baja, siguiendo las indicaciones hacia el spa.");
                    msg3.put("timestamp", twoDaysAgo + 600000);
                    msg3.put("type", "HOTEL");
                    msg3.put("read", true);

                    // Último mensaje del usuario
                    Map<String, Object> msg4 = new HashMap<>();
                    msg4.put("id", "user_msg_2");
                    msg4.put("senderId", userId);
                    msg4.put("receiverId", "hotel_2");
                    msg4.put("text", "¿Podría solicitar servicio de habitaciones?");
                    msg4.put("timestamp", oneDayAgo);
                    msg4.put("type", "USER");
                    msg4.put("read", true);

                    // Guardar todos los mensajes
                    msgsRef.child("system_welcome").setValue(msg1);
                    msgsRef.child("user_msg_1").setValue(msg2);
                    msgsRef.child("hotel_msg_1").setValue(msg3);
                    msgsRef.child("user_msg_2").setValue(msg4);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al crear chat ACTIVE: " + e.getMessage()));

        // ========== CHAT FINALIZADO (FINISHED) ==========
        DatabaseReference chatFinishedRef = FirebaseDatabase.getInstance()
                .getReference("chatSummaries").child("chat_finished");

        Map<String, Object> chatFinished = new HashMap<>();
        chatFinished.put("id", "chat_finished");
        chatFinished.put("hotelId", "hotel_3");
        chatFinished.put("hotelName", "Sunset Resort & Spa");
        chatFinished.put("reservationId", "54321");
        chatFinished.put("reservationDates", "1-10 Mayo 2025");
        chatFinished.put("status", "FINISHED");
        chatFinished.put("userId", userId);
        chatFinished.put("lastMessage", "Gracias por su estancia, esperamos verle pronto.");
        chatFinished.put("lastUpdated", now);

        chatFinishedRef.setValue(chatFinished)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Chat FINISHED creado exitosamente");

                    // Crear mensajes para chat FINISHED
                    DatabaseReference msgsRef = FirebaseDatabase.getInstance()
                            .getReference("messages").child("chat_finished");

                    // Mensaje de bienvenida del sistema
                    Map<String, Object> msg1 = new HashMap<>();
                    msg1.put("id", "system_welcome");
                    msg1.put("senderId", "system");
                    msg1.put("receiverId", userId);
                    msg1.put("text", "Bienvenido al chat. Un representante del hotel le atenderá en breve.");
                    msg1.put("timestamp", threeDaysAgo);
                    msg1.put("type", "SYSTEM");
                    msg1.put("read", true);

                    // Primer mensaje del usuario
                    Map<String, Object> msg2 = new HashMap<>();
                    msg2.put("id", "user_msg_1");
                    msg2.put("senderId", userId);
                    msg2.put("receiverId", "hotel_3");
                    msg2.put("text", "Necesito hacer checkout mañana a las 6 AM, ¿es posible?");
                    msg2.put("timestamp", threeDaysAgo + 3600000);
                    msg2.put("type", "USER");
                    msg2.put("read", true);

                    // Respuesta del hotel
                    Map<String, Object> msg3 = new HashMap<>();
                    msg3.put("id", "hotel_msg_1");
                    msg3.put("senderId", "hotel_3");
                    msg3.put("receiverId", userId);
                    msg3.put("text", "Por supuesto, hemos registrado su solicitud de checkout temprano. El personal estará disponible para atenderle.");
                    msg3.put("timestamp", threeDaysAgo + 7200000);
                    msg3.put("type", "HOTEL");
                    msg3.put("read", true);

                    // Mensaje de agradecimiento del usuario
                    Map<String, Object> msg4 = new HashMap<>();
                    msg4.put("id", "user_msg_2");
                    msg4.put("senderId", userId);
                    msg4.put("receiverId", "hotel_3");
                    msg4.put("text", "Muchas gracias por todo, ha sido una estancia muy agradable.");
                    msg4.put("timestamp", twoDaysAgo);
                    msg4.put("type", "USER");
                    msg4.put("read", true);

                    // Último mensaje del hotel
                    Map<String, Object> msg5 = new HashMap<>();
                    msg5.put("id", "hotel_msg_2");
                    msg5.put("senderId", "hotel_3");
                    msg5.put("receiverId", userId);
                    msg5.put("text", "Gracias por su estancia, esperamos verle pronto.");
                    msg5.put("timestamp", twoDaysAgo + 3600000);
                    msg5.put("type", "HOTEL");
                    msg5.put("read", true);

                    // Mensaje de sistema indicando que el chat ha finalizado
                    Map<String, Object> msg6 = new HashMap<>();
                    msg6.put("id", "system_closed");
                    msg6.put("senderId", "system");
                    msg6.put("receiverId", "system");
                    msg6.put("text", "Este chat ha sido cerrado. Su reserva ha finalizado.");
                    msg6.put("timestamp", twoDaysAgo + 7200000);
                    msg6.put("type", "SYSTEM");
                    msg6.put("read", true);

                    // Guardar todos los mensajes
                    msgsRef.child("system_welcome").setValue(msg1);
                    msgsRef.child("user_msg_1").setValue(msg2);
                    msgsRef.child("hotel_msg_1").setValue(msg3);
                    msgsRef.child("user_msg_2").setValue(msg4);
                    msgsRef.child("hotel_msg_2").setValue(msg5);
                    msgsRef.child("system_closed").setValue(msg6);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al crear chat FINISHED: " + e.getMessage()));

        Toast.makeText(this, "Chats de ejemplo iniciados con éxito", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Limpiar recursos del simulador
        if (responseSimulator != null) {
            responseSimulator.cleanup();
        }
    }
    private void testFirebaseConnection() {
        DatabaseReference testRef = FirebaseDatabase.getInstance().getReference("test");
        testRef.setValue("Hello Firebase: " + System.currentTimeMillis())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "¡Conexión a Firebase exitosa! Se pudo escribir en la base de datos.");
                        Toast.makeText(HomeActivity.this, "Conexión a Firebase exitosa", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al escribir en Firebase: " + e.getMessage(), e);
                        Toast.makeText(HomeActivity.this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}