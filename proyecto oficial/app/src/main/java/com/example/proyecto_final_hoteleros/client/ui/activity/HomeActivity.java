package com.example.proyecto_final_hoteleros.client.ui.activity;

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
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.fragment.HomeFragment;
import com.example.proyecto_final_hoteleros.client.data.model.ChatSummary;
import com.example.proyecto_final_hoteleros.client.data.service.FirebaseChatService;
import com.example.proyecto_final_hoteleros.client.data.service.HotelResponseSimulator;
import com.example.proyecto_final_hoteleros.client.navigation.NavigationManager;
import com.example.proyecto_final_hoteleros.client.utils.UserDataManager;
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

    // Variables para datos del usuario
    private String userId;
    private String userName;
    private String userFullName;
    private String userEmail;
    private String userType;

    private HotelResponseSimulator responseSimulator;
    private FirebaseChatService chatService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.client_activity_home);

        // ========== INICIALIZAR MANAGERS ==========
        NavigationManager.getInstance().init(this);

        // ========== OBTENER DATOS DEL USUARIO DESDE EL INTENT ==========
        getUserDataFromIntent();
        UserDataManager.getInstance().setUserData(userId, userName, userFullName, userEmail, userType);

        // Inicializar Firebase
        try {
            FirebaseApp.initializeApp(this);
            testFirebaseConnection();
            Log.d(TAG, "Firebase inicializado correctamente");

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

        // ========== CARGAR FRAGMENTO INICIAL USANDO NAVIGATIONMANAGER ==========
        if (savedInstanceState == null) {
            NavigationManager.getInstance().navigateToHome(
                    UserDataManager.getInstance().getUserBundle()
            );
        }
    }

    private void getUserDataFromIntent() {
        userId = getIntent().getStringExtra("user_id");
        userName = getIntent().getStringExtra("user_name");
        userFullName = getIntent().getStringExtra("user_full_name");
        userEmail = getIntent().getStringExtra("user_email");
        userType = getIntent().getStringExtra("user_type");

        Log.d(TAG, "=== DATOS DEL USUARIO RECIBIDOS ===");
        Log.d(TAG, "User ID: " + userId);
        Log.d(TAG, "User Name: " + userName);
        Log.d(TAG, "User Full Name: " + userFullName);
        Log.d(TAG, "User Email: " + userEmail);
        Log.d(TAG, "User Type: " + userType);

        // Valores por defecto si no se reciben datos
        if (userName == null || userName.isEmpty()) {
            userName = "Huésped";
            Log.d(TAG, "Usando nombre por defecto: " + userName);
        }

        if (userFullName == null || userFullName.isEmpty()) {
            userFullName = userName;
        }
    }

    // ========== MÉTODOS PÚBLICOS PARA ACCESO A DATOS (MANTENIDOS PARA COMPATIBILIDAD) ==========
    public String getUserName() {
        return UserDataManager.getInstance().getUserName();
    }

    public String getUserFullName() {
        return UserDataManager.getInstance().getUserFullName();
    }

    public String getUserId() {
        return UserDataManager.getInstance().getUserId();
    }

    public String getUserEmail() {
        return UserDataManager.getInstance().getUserEmail();
    }

    public String getUserType() {
        return UserDataManager.getInstance().getUserType();
    }

    private void initializeFirebaseServices() {
        chatService = FirebaseChatService.getInstance();
        responseSimulator = HotelResponseSimulator.getInstance();

        chatService.loadChatSummaries(new FirebaseChatService.OnChatSummariesLoadedListener() {
            @Override
            public void onChatSummariesLoaded(List<ChatSummary> chatSummaries) {
                for (ChatSummary chat : chatSummaries) {
                    if (chat.getStatus() == ChatSummary.ChatStatus.ACTIVE ||
                            chat.getStatus() == ChatSummary.ChatStatus.AVAILABLE) {
                        responseSimulator.startListening(chat.getId(), chat.getHotelId());
                    }
                }

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
        HotelResponseSimulator simulator = HotelResponseSimulator.getInstance();
        simulator.startListening("chat_available", "hotel_1");
        simulator.startListening("chat_active", "hotel_2");
    }

    private void initializeExampleChats() {
        String currentUserId = userId != null ? userId :
                (FirebaseAuth.getInstance().getCurrentUser() != null ?
                        FirebaseAuth.getInstance().getCurrentUser().getUid() : "user_1");

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
        chatAvailable.put("status", "AVAILABLE");
        chatAvailable.put("userId", currentUserId);
        chatAvailable.put("lastUpdated", now);
        chatAvailable.put("lastMessage", "");

        chatAvailableRef.setValue(chatAvailable)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Chat AVAILABLE creado exitosamente");
                    DatabaseReference msgRef = FirebaseDatabase.getInstance()
                            .getReference("messages").child("chat_available").child("system_welcome");

                    Map<String, Object> msgData = new HashMap<>();
                    msgData.put("id", "system_welcome");
                    msgData.put("senderId", "system");
                    msgData.put("receiverId", currentUserId);
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
        chatActive.put("userId", currentUserId);
        chatActive.put("lastMessage", "¿Podría solicitar servicio de habitaciones?");
        chatActive.put("lastUpdated", now);

        chatActiveRef.setValue(chatActive)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Chat ACTIVE creado exitosamente");

                    DatabaseReference msgsRef = FirebaseDatabase.getInstance()
                            .getReference("messages").child("chat_active");

                    Map<String, Object> msg1 = new HashMap<>();
                    msg1.put("id", "system_welcome");
                    msg1.put("senderId", "system");
                    msg1.put("receiverId", currentUserId);
                    msg1.put("text", "Bienvenido al chat. Un representante del hotel le atenderá en breve.");
                    msg1.put("timestamp", threeDaysAgo);
                    msg1.put("type", "SYSTEM");
                    msg1.put("read", true);

                    Map<String, Object> msg2 = new HashMap<>();
                    msg2.put("id", "user_msg_1");
                    msg2.put("senderId", currentUserId);
                    msg2.put("receiverId", "hotel_2");
                    msg2.put("text", "Hola, ¿podrían indicarme cómo llegar a la piscina?");
                    msg2.put("timestamp", twoDaysAgo);
                    msg2.put("type", "USER");
                    msg2.put("read", true);

                    Map<String, Object> msg3 = new HashMap<>();
                    msg3.put("id", "hotel_msg_1");
                    msg3.put("senderId", "hotel_2");
                    msg3.put("receiverId", currentUserId);
                    msg3.put("text", "¡Buenos días! La piscina se encuentra en la planta baja, siguiendo las indicaciones hacia el spa.");
                    msg3.put("timestamp", twoDaysAgo + 600000);
                    msg3.put("type", "HOTEL");
                    msg3.put("read", true);

                    Map<String, Object> msg4 = new HashMap<>();
                    msg4.put("id", "user_msg_2");
                    msg4.put("senderId", currentUserId);
                    msg4.put("receiverId", "hotel_2");
                    msg4.put("text", "¿Podría solicitar servicio de habitaciones?");
                    msg4.put("timestamp", oneDayAgo);
                    msg4.put("type", "USER");
                    msg4.put("read", true);

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
        chatFinished.put("userId", currentUserId);
        chatFinished.put("lastMessage", "Gracias por su estancia, esperamos verle pronto.");
        chatFinished.put("lastUpdated", now);

        chatFinishedRef.setValue(chatFinished)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Chat FINISHED creado exitosamente");

                    DatabaseReference msgsRef = FirebaseDatabase.getInstance()
                            .getReference("messages").child("chat_finished");

                    Map<String, Object> msg1 = new HashMap<>();
                    msg1.put("id", "system_welcome");
                    msg1.put("senderId", "system");
                    msg1.put("receiverId", currentUserId);
                    msg1.put("text", "Bienvenido al chat. Un representante del hotel le atenderá en breve.");
                    msg1.put("timestamp", threeDaysAgo);
                    msg1.put("type", "SYSTEM");
                    msg1.put("read", true);

                    Map<String, Object> msg2 = new HashMap<>();
                    msg2.put("id", "user_msg_1");
                    msg2.put("senderId", currentUserId);
                    msg2.put("receiverId", "hotel_3");
                    msg2.put("text", "Necesito hacer checkout mañana a las 6 AM, ¿es posible?");
                    msg2.put("timestamp", threeDaysAgo + 3600000);
                    msg2.put("type", "USER");
                    msg2.put("read", true);

                    Map<String, Object> msg3 = new HashMap<>();
                    msg3.put("id", "hotel_msg_1");
                    msg3.put("senderId", "hotel_3");
                    msg3.put("receiverId", currentUserId);
                    msg3.put("text", "Por supuesto, hemos registrado su solicitud de checkout temprano. El personal estará disponible para atenderle.");
                    msg3.put("timestamp", threeDaysAgo + 7200000);
                    msg3.put("type", "HOTEL");
                    msg3.put("read", true);

                    Map<String, Object> msg4 = new HashMap<>();
                    msg4.put("id", "user_msg_2");
                    msg4.put("senderId", currentUserId);
                    msg4.put("receiverId", "hotel_3");
                    msg4.put("text", "Muchas gracias por todo, ha sido una estancia muy agradable.");
                    msg4.put("timestamp", twoDaysAgo);
                    msg4.put("type", "USER");
                    msg4.put("read", true);

                    Map<String, Object> msg5 = new HashMap<>();
                    msg5.put("id", "hotel_msg_2");
                    msg5.put("senderId", "hotel_3");
                    msg5.put("receiverId", currentUserId);
                    msg5.put("text", "Gracias por su estancia, esperamos verle pronto.");
                    msg5.put("timestamp", twoDaysAgo + 3600000);
                    msg5.put("type", "HOTEL");
                    msg5.put("read", true);

                    Map<String, Object> msg6 = new HashMap<>();
                    msg6.put("id", "system_closed");
                    msg6.put("senderId", "system");
                    msg6.put("receiverId", "system");
                    msg6.put("text", "Este chat ha sido cerrado. Su reserva ha finalizado.");
                    msg6.put("timestamp", twoDaysAgo + 7200000);
                    msg6.put("type", "SYSTEM");
                    msg6.put("read", true);

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