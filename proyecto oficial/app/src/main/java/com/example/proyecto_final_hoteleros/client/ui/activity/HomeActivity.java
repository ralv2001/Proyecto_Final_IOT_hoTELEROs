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
        // 🔥 VERIFICAR DATOS ANTES DE GUARDAR
        Log.d(TAG, "🔄 Guardando datos en UserDataManager...");
        Log.d(TAG, "  userId: " + userId);
        Log.d(TAG, "  userName: " + userName);
        Log.d(TAG, "  userFullName: " + userFullName);
        Log.d(TAG, "  userEmail: " + userEmail);
        Log.d(TAG, "  userType: " + userType);

        UserDataManager.getInstance().setUserData(userId, userName, userFullName, userEmail, userType);

// 🔥 VERIFICAR QUE SE GUARDÓ CORRECTAMENTE
        Log.d(TAG, "✅ Datos guardados. Verificando:");
        Log.d(TAG, "  UserDataManager.getUserId(): " + UserDataManager.getInstance().getUserId());
        Log.d(TAG, "  UserDataManager.getUserName(): " + UserDataManager.getInstance().getUserName());
        Log.d(TAG, "  UserDataManager.getUserEmail(): " + UserDataManager.getInstance().getUserEmail());

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
    private void verifyAuthenticatedUser() {
        // ✅ Si ya tenemos todos los datos del intent, no hacer nada
        if (userId != null && userName != null && userType != null) {
            Log.d(TAG, "✅ Datos completos recibidos del intent");
            return;
        }

        // ✅ Verificar si hay un usuario autenticado en Firebase
        com.google.firebase.auth.FirebaseAuth firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
        com.google.firebase.auth.FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            Log.d(TAG, "🔍 Usuario autenticado encontrado, cargando perfil...");

            // ✅ Usuario autenticado pero sin datos en el intent
            // Cargar datos desde Firebase y crearlos si no existen
            loadUserProfileOrCreateDefault(currentUser);
        } else {
            Log.d(TAG, "👤 Usuario no autenticado - modo huésped");
            // ✅ Usuario no autenticado (huésped) - configurar datos por defecto
            setupGuestUser();
        }
    }


    private void loadUserProfileOrCreateDefault(com.google.firebase.auth.FirebaseUser firebaseUser) {
        String firebaseUserId = firebaseUser.getUid();
        String firebaseEmail = firebaseUser.getEmail();

        Log.d(TAG, "📥 Cargando perfil para: " + firebaseUserId);

        com.example.proyecto_final_hoteleros.utils.FirebaseManager firebaseManager =
                com.example.proyecto_final_hoteleros.utils.FirebaseManager.getInstance();

        firebaseManager.getUserDataFromAnyCollection(firebaseUserId, new com.example.proyecto_final_hoteleros.utils.FirebaseManager.UserCallback() {
            @Override
            public void onUserFound(com.example.proyecto_final_hoteleros.models.UserModel user) {
                Log.d(TAG, "✅ Perfil encontrado: " + user.getFullName());

                runOnUiThread(() -> {
                    // ✅ Actualizar datos con el perfil encontrado
                    updateUserDataFromProfile(user);
                });
            }

            @Override
            public void onUserNotFound() {
                Log.w(TAG, "⚠️ Perfil no encontrado, creando perfil básico de cliente...");

                runOnUiThread(() -> {
                    // ✅ Crear perfil básico de cliente
                    createBasicClientProfile(firebaseUserId, firebaseEmail);
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando perfil: " + error);

                runOnUiThread(() -> {
                    // ✅ En caso de error, crear perfil básico para que funcione
                    createBasicClientProfile(firebaseUserId, firebaseEmail);
                });
            }
        });
    }
    private void updateUserDataFromProfile(com.example.proyecto_final_hoteleros.models.UserModel user) {
        userId = user.getUserId();
        userName = user.getFullName();
        userFullName = user.getFullName();
        userEmail = user.getEmail();
        userType = user.getUserType() != null ? user.getUserType() : "client";  // ✅ DEFAULT a client

        Log.d(TAG, "✅ Datos actualizados desde Firebase:");
        Log.d(TAG, "   userId: " + userId);
        Log.d(TAG, "   userName: " + userName);
        Log.d(TAG, "   userType: " + userType);

        // ✅ Actualizar UserDataManager
        UserDataManager.getInstance().setUserData(userId, userName, userFullName, userEmail, userType);
    }

    // ✅ MÉTODO: Crear perfil básico de cliente
    private void createBasicClientProfile(String firebaseUserId, String firebaseEmail) {
        Log.d(TAG, "🚀 Creando perfil básico de cliente");

        // ✅ Extraer nombre del email
        String extractedName = extractNameFromEmail(firebaseEmail);
        String[] nameParts = extractedName.split(" ");
        String firstName = nameParts.length > 0 ? nameParts[0] : "Cliente";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        // ✅ Crear modelo de usuario
        com.example.proyecto_final_hoteleros.models.UserModel clientUser =
                new com.example.proyecto_final_hoteleros.models.UserModel();
        clientUser.setUserId(firebaseUserId);
        clientUser.setEmail(firebaseEmail);
        clientUser.setNombres(firstName);
        clientUser.setApellidos(lastName);
        clientUser.setUserType("client");  // ✅ CRÍTICO: Marcar como cliente
        clientUser.setTelefono("");
        clientUser.setDireccion("");
        clientUser.setNumeroDocumento("");
        clientUser.setTipoDocumento("DNI");
        clientUser.setFechaNacimiento("01/01/1990");
        clientUser.setActive(true);

        // ✅ Intentar guardar en Firebase (pero no bloquear si falla)
        com.example.proyecto_final_hoteleros.utils.FirebaseManager firebaseManager =
                com.example.proyecto_final_hoteleros.utils.FirebaseManager.getInstance();

        firebaseManager.saveUserData(firebaseUserId, clientUser, new com.example.proyecto_final_hoteleros.utils.FirebaseManager.DataCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Perfil básico creado en Firebase");
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "⚠️ Error guardando perfil en Firebase: " + error);
                Log.w(TAG, "⚠️ Continuando con datos en memoria...");
            }
        });

        // ✅ Usar los datos inmediatamente (no esperar a Firebase)
        updateUserDataFromProfile(clientUser);

        // ✅ Mostrar mensaje al usuario
        Toast.makeText(this, "Perfil configurado como cliente", Toast.LENGTH_SHORT).show();
    }

    private void setupGuestUser() {
        userId = "guest_" + System.currentTimeMillis();
        userName = "Huésped";
        userFullName = "Huésped";
        userEmail = "";
        userType = "guest";

        Log.d(TAG, "✅ Usuario configurado como huésped");

        // ✅ Actualizar UserDataManager
        UserDataManager.getInstance().setUserData(userId, userName, userFullName, userEmail, userType);
    }
    private String extractNameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "Cliente";
        }

        String localPart = email.split("@")[0];

        // Reemplazar caracteres con espacios
        localPart = localPart.replace(".", " ");
        localPart = localPart.replace("_", " ");
        localPart = localPart.replace("-", " ");

        // Capitalizar cada palabra
        StringBuilder result = new StringBuilder();
        String[] words = localPart.split("\\s+");

        for (String word : words) {
            if (!word.isEmpty()) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(capitalize(word));
            }
        }

        return result.toString().isEmpty() ? "Cliente" : result.toString();
    }

    // ✅ MÉTODO AUXILIAR: Capitalizar primera letra
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    private void getUserDataFromIntent() {
        // 🔥 USAR LOS NOMBRES EXACTOS QUE ENVÍA LoginFragment
        userId = getIntent().getStringExtra("userId");
        userEmail = getIntent().getStringExtra("userEmail");
        userName = getIntent().getStringExtra("userName");  // Este es el nombre completo
        userType = getIntent().getStringExtra("userType");

        // 🔥 PARA CLIENTES: userName viene con el nombre completo
        userFullName = userName;  // userName ya contiene el nombre completo

        Log.d(TAG, "=== DATOS DEL USUARIO RECIBIDOS ===");
        Log.d(TAG, "User ID: " + userId);
        Log.d(TAG, "User Email: " + userEmail);
        Log.d(TAG, "User Name (completo): " + userName);
        Log.d(TAG, "User Full Name: " + userFullName);
        Log.d(TAG, "User Type: " + userType);

        // Valores por defecto si no se reciben datos
        if (userName == null || userName.isEmpty()) {
            userName = "Huésped";
            Log.d(TAG, "Usando nombre por defecto: " + userName);
        }

        if (userFullName == null || userFullName.isEmpty()) {
            userFullName = userName;
        }

        // 🔥 VERIFICAR QUE REALMENTE LLEGARON LOS DATOS
        if (userId == null) {
            Log.e(TAG, "❌ ERROR: userId llegó como null");
            Log.e(TAG, "❌ Todos los extras del intent:");
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Log.e(TAG, "  " + key + " = " + extras.get(key));
                }
            } else {
                Log.e(TAG, "❌ El intent no tiene extras");
            }
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