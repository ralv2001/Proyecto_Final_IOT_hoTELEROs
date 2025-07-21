package com.example.proyecto_final_hoteleros.utils;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.proyecto_final_hoteleros.taxista.model.CheckoutReservation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import com.example.proyecto_final_hoteleros.models.UserModel;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class FirebaseManager {

    private static final String TAG = "FirebaseManager";
    private FirebaseFirestore db;
    private static final String USERS_COLLECTION = "users";
    private static final String PENDING_DRIVERS_COLLECTION = "pending_drivers";

    private static final String RESERVATIONS_COLLECTION = "reservations"; // NUEVO
    private static final String TAXI_SERVICES_COLLECTION = "taxi_services";

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    // Singleton pattern
    private static FirebaseManager instance;

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    private FirebaseManager() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Interfaces para callbacks
    public interface AuthCallback {
        void onSuccess(String userId);
        void onError(String error);
    }

    public interface DataCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface UserCallback {
        void onUserFound(UserModel user);
        void onUserNotFound();
        void onError(String error);
    }
    public interface CheckoutCallback {
        void onSuccess(List<CheckoutReservation> reservations);
        void onError(String error);
    }

    public interface TaxiServiceCallback {
        void onSuccess();
        void onError(String error);
        void onStatusUpdate(String status);
    }

    public interface RealtimeCheckoutCallback {
        void onNewReservation(CheckoutReservation reservation);
        void onReservationUpdated(CheckoutReservation reservation);
        void onReservationRemoved(String reservationId);
        void onError(String error);
    }
    // ========== AUTENTICACIÓN ==========

    public void registerUser(String email, String password, AuthCallback callback) {
        Log.d(TAG, "Registrando usuario: " + email);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "✅ Usuario registrado exitosamente: " + user.getUid());
                            callback.onSuccess(user.getUid());
                        } else {
                            Log.e(TAG, "❌ Error: Usuario nulo después del registro");
                            callback.onError("Error interno del servidor");
                        }
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        Log.e(TAG, "❌ Error en registro: " + error);
                        callback.onError(error);
                    }
                });
    }

    public void loginUser(String email, String password, AuthCallback callback) {
        Log.d(TAG, "Iniciando sesión: " + email);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "✅ Login exitoso: " + user.getUid());
                            callback.onSuccess(user.getUid());
                        } else {
                            Log.e(TAG, "❌ Error: Usuario nulo después del login");
                            callback.onError("Error interno del servidor");
                        }
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        Log.e(TAG, "❌ Error en login: " + error);
                        callback.onError(error);
                    }
                });
    }

    public void signOut() {
        auth.signOut();
        Log.d(TAG, "Usuario desconectado");
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    // ========== FIRESTORE - GUARDAR DATOS ==========

    public void saveUserData(String userId, UserModel userModel, DataCallback callback) {
        Log.d(TAG, "Guardando datos del usuario: " + userId);

        Map<String, Object> userData = userModel.toMap();

        firestore.collection(USERS_COLLECTION)
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Datos del usuario guardados exitosamente");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error guardando datos del usuario: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    public void savePendingDriver(String userId, UserModel userModel, DataCallback callback) {
        Log.d(TAG, "Guardando taxista pendiente: " + userId);

        Map<String, Object> userData = userModel.toMap();

        firestore.collection(PENDING_DRIVERS_COLLECTION)
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Taxista pendiente guardado exitosamente");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error guardando taxista pendiente: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    // ========== FIRESTORE - OBTENER DATOS ==========

    public void getUserData(String userId, UserCallback callback) {
        Log.d(TAG, "Obteniendo datos del usuario: " + userId);

        firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            try {
                                UserModel user = UserModel.fromMap(document.getData());
                                Log.d(TAG, "✅ Datos del usuario obtenidos");
                                callback.onUserFound(user);
                            } catch (Exception e) {
                                Log.e(TAG, "❌ Error parseando datos del usuario: " + e.getMessage());
                                callback.onError("Error procesando datos del usuario");
                            }
                        } else {
                            Log.d(TAG, "Usuario no encontrado en Firestore");
                            callback.onUserNotFound();
                        }
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        Log.e(TAG, "❌ Error obteniendo datos del usuario: " + error);
                        callback.onError(error);
                    }
                });
    }

    // ========== NUEVO MÉTODO: BUSCAR USUARIO EN CUALQUIER COLECCIÓN ==========
    public void getUserDataFromAnyCollection(String userId, UserCallback callback) {
        Log.d(TAG, "Buscando usuario en todas las colecciones: " + userId);

        // 1. Primero buscar en la colección 'users' (usuarios activos)
        firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        Log.d(TAG, "✅ Usuario encontrado en colección 'users'");
                        // Usuario encontrado en 'users'
                        UserModel user = UserModel.fromMap(task.getResult().getData());
                        user.setUserId(userId);
                        callback.onUserFound(user);
                    } else {
                        Log.d(TAG, "Usuario no encontrado en 'users', buscando en 'pending_drivers'...");

                        // 2. Si no está en 'users', buscar en 'pending_drivers'
                        firestore.collection(PENDING_DRIVERS_COLLECTION)
                                .document(userId)
                                .get()
                                .addOnCompleteListener(pendingTask -> {
                                    if (pendingTask.isSuccessful() && pendingTask.getResult() != null && pendingTask.getResult().exists()) {
                                        Log.d(TAG, "✅ Usuario encontrado en colección 'pending_drivers'");
                                        // Usuario encontrado en 'pending_drivers'
                                        UserModel user = UserModel.fromMap(pendingTask.getResult().getData());
                                        user.setUserId(userId);
                                        // Los pending drivers tienen isActive = false por defecto
                                        user.setActive(false);
                                        callback.onUserFound(user);
                                    } else {
                                        Log.d(TAG, "❌ Usuario no encontrado en ninguna colección");
                                        callback.onUserNotFound();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "❌ Error buscando en pending_drivers: " + e.getMessage());
                                    callback.onError(e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error buscando en users: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    // ========== EMAIL VERIFICATION ==========

    public void sendEmailVerification(FirebaseUser user, DataCallback callback) {
        Log.d(TAG, "Enviando email de verificación a: " + user.getEmail());

        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "✅ Email de verificación enviado exitosamente");
                        callback.onSuccess();
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Error enviando email";
                        Log.e(TAG, "❌ Error enviando email de verificación: " + error);
                        callback.onError(error);
                    }
                });
    }


    public void checkEmailVerification(FirebaseUser user, AuthCallback callback) {
        Log.d(TAG, "Verificando estado del email: " + user.getEmail());

        // Primero hacer reload del usuario para obtener el estado más reciente
        user.reload().addOnCompleteListener(reloadTask -> {
            if (reloadTask.isSuccessful()) {
                // Después del reload, verificar nuevamente
                FirebaseUser reloadedUser = auth.getCurrentUser();
                if (reloadedUser != null && reloadedUser.isEmailVerified()) {
                    Log.d(TAG, "✅ Email verificado exitosamente después del reload");
                    callback.onSuccess(reloadedUser.getUid());
                } else {
                    Log.d(TAG, "❌ Email aún no verificado después del reload");
                    callback.onError("Email no verificado");
                }
            } else {
                String error = reloadTask.getException() != null ?
                        reloadTask.getException().getMessage() : "Error recargando usuario";
                Log.e(TAG, "❌ Error recargando usuario: " + error);
                callback.onError(error);
            }
        });
    }

    public void resendEmailVerification(DataCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            sendEmailVerification(user, callback);
        } else {
            callback.onError("No hay usuario logueado");
        }
    }

    // ========== PASSWORD RESET ==========

    public void sendPasswordResetEmail(String email, DataCallback callback) {
        Log.d(TAG, "Enviando email de reset de contraseña a: " + email);

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "✅ Email de reset de contraseña enviado exitosamente");
                        callback.onSuccess();
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Error enviando email";
                        Log.e(TAG, "❌ Error enviando email de reset: " + error);
                        callback.onError(error);
                    }
                });
    }

    // ========== UTILIDADES ==========

    public void testConnection(DataCallback callback) {
        Log.d(TAG, "Probando conexión a Firebase...");

        Map<String, Object> testData = new HashMap<>();
        testData.put("test", true);
        testData.put("timestamp", System.currentTimeMillis());

        firestore.collection("test")
                .add(testData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "✅ Conexión a Firebase exitosa");
                    // Eliminar el documento de prueba
                    documentReference.delete();
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error de conexión a Firebase: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }


    public void checkIfEmailExists(String email, AuthCallback callback) {
        Log.d(TAG, "Verificando si el email existe: " + email);

        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean emailExists = !task.getResult().getSignInMethods().isEmpty();
                        Log.d(TAG, "Email existe: " + emailExists);

                        if (emailExists) {
                            callback.onSuccess("EMAIL_EXISTS");
                        } else {
                            callback.onError("EMAIL_NOT_EXISTS");
                        }
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Error verificando email";
                        Log.e(TAG, "❌ Error verificando email: " + error);
                        callback.onError(error);
                    }
                });
    }
    // 🔥 NUEVO MÉTODO: Verificar email específicamente para admins de hotel
    public void checkEmailExistsForHotelAdmin(String email, AuthCallback callback) {
        Log.d(TAG, "Verificando si email existe para admin de hotel: " + email);

        // Buscar en colección users con userType hotel_admin
        firestore.collection(USERS_COLLECTION)
                .whereEqualTo("email", email)
                .whereEqualTo("userType", "hotel_admin")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean emailExists = !task.getResult().isEmpty();
                        Log.d(TAG, "Email de admin existe: " + emailExists);

                        if (emailExists) {
                            callback.onSuccess("ADMIN_EMAIL_EXISTS");
                        } else {
                            callback.onError("ADMIN_EMAIL_NOT_EXISTS");
                        }
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Error verificando email";
                        Log.e(TAG, "❌ Error verificando email admin: " + error);
                        callback.onError(error);
                    }
                });
    }

    // ========== MÉTODO PARA CREAR SUPERADMIN ==========
    public void createSuperAdminUser(DataCallback callback) {
        String superAdminEmail = "delgadoaquinor@gmail.com";
        String superAdminPassword = "SuperAdmin123!";

        Log.d(TAG, "Creando usuario superadmin...");

        // Primero registrar en Firebase Auth
        registerUser(superAdminEmail, superAdminPassword, new AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                // Crear el modelo de usuario para superadmin
                UserModel superAdminUser = new UserModel();
                superAdminUser.setEmail(superAdminEmail);
                superAdminUser.setNombres("Super");
                superAdminUser.setApellidos("Admin");
                superAdminUser.setUserType("superadmin");
                superAdminUser.setTelefono("999999999");
                superAdminUser.setDireccion("Oficina Central");
                superAdminUser.setNumeroDocumento("00000000");
                superAdminUser.setActive(true);

                // Guardar en Firestore
                saveUserData(userId, superAdminUser, new DataCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Usuario superadmin creado exitosamente");
                        callback.onSuccess();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "❌ Error guardando datos superadmin: " + error);
                        callback.onError(error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error creando superadmin: " + error);
                callback.onError(error);
            }
        });
    }

    // ========== NUEVO INTERFACE PARA LISTA DE DRIVERS ==========
    public interface DriverListCallback {
        void onSuccess(List<UserModel> drivers);
        void onError(String error);
    }

    // ========== MÉTODO PARA OBTENER TAXISTAS PENDIENTES ==========
    public void getPendingDrivers(DriverListCallback callback) {
        Log.d(TAG, "Obteniendo taxistas pendientes...");

        firestore.collection(PENDING_DRIVERS_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<UserModel> drivers = new ArrayList<>();

                        for (DocumentSnapshot document : task.getResult()) {
                            try {
                                UserModel driver = UserModel.fromMap(document.getData());
                                driver.setUserId(document.getId()); // Asegurar que tenga el ID
                                drivers.add(driver);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parseando taxista: " + e.getMessage());
                            }
                        }

                        Log.d(TAG, "✅ " + drivers.size() + " taxistas pendientes obtenidos");
                        callback.onSuccess(drivers);

                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        Log.e(TAG, "❌ Error obteniendo taxistas: " + error);
                        callback.onError(error);
                    }
                });
    }

    // ========== APROBAR TAXISTA ==========
    public void approveDriver(String driverId, UserModel driver, DataCallback callback) {
        Log.d(TAG, "Aprobando taxista: " + driverId);

        // 1. Mover de pending_drivers a users
        firestore.collection(USERS_COLLECTION)
                .document(driverId)
                .set(driver.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Taxista movido a users");

                    // 2. Eliminar de pending_drivers
                    firestore.collection(PENDING_DRIVERS_COLLECTION)
                            .document(driverId)
                            .delete()
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "✅ Taxista eliminado de pending_drivers");
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "❌ Error eliminando de pending: " + e.getMessage());
                                callback.onError(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error moviendo a users: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    // ========== RECHAZAR TAXISTA ==========
    public void rejectDriver(String driverId, UserModel driver, String reason, DataCallback callback) {
        Log.d(TAG, "Rechazando taxista: " + driverId + " - Motivo: " + reason);

        // Eliminar de pending_drivers
        firestore.collection(PENDING_DRIVERS_COLLECTION)
                .document(driverId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Taxista rechazado y eliminado");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error rechazando taxista: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Actualizar URLs de foto y documento para taxistas pendientes
     */
    public void updatePendingDriverUrls(String userId, String photoUrl, String documentUrl, DataCallback callback) {
        Log.d(TAG, "🔄 Actualizando URLs en pending_drivers para userId: " + userId);

        DocumentReference driverRef = db.collection("pending_drivers").document(userId);

        // Crear mapa con solo las URLs a actualizar
        Map<String, Object> updates = new HashMap<>();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            updates.put("photoUrl", photoUrl);
            Log.d(TAG, "📷 Actualizando photoUrl: " + photoUrl);
        }
        if (documentUrl != null && !documentUrl.isEmpty()) {
            updates.put("documentUrl", documentUrl);
            Log.d(TAG, "📄 Actualizando documentUrl: " + documentUrl);
        }

        if (updates.isEmpty()) {
            Log.d(TAG, "ℹ️ No hay URLs para actualizar");
            callback.onSuccess();
            return;
        }

        driverRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ URLs actualizadas en pending_drivers");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error actualizando URLs en pending_drivers: " + e.getMessage());
                    callback.onError("Error actualizando URLs: " + e.getMessage());
                });
    }

    /**
     * Actualizar URLs de foto y documento para usuarios normales
     */
    public void updateUserUrls(String userId, String photoUrl, String documentUrl, DataCallback callback) {
        Log.d(TAG, "🔄 Actualizando URLs en users para userId: " + userId);

        DocumentReference userRef = db.collection("users").document(userId);

        // Crear mapa con solo las URLs a actualizar
        Map<String, Object> updates = new HashMap<>();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            updates.put("photoUrl", photoUrl);
            Log.d(TAG, "📷 Actualizando photoUrl: " + photoUrl);
        }
        if (documentUrl != null && !documentUrl.isEmpty()) {
            updates.put("documentUrl", documentUrl);
            Log.d(TAG, "📄 Actualizando documentUrl: " + documentUrl);
        }

        if (updates.isEmpty()) {
            Log.d(TAG, "ℹ️ No hay URLs para actualizar");
            callback.onSuccess();
            return;
        }

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ URLs actualizadas en users");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error actualizando URLs en users: " + e.getMessage());
                    callback.onError("Error actualizando URLs: " + e.getMessage());
                });
    }

    // ========== OBTENER TODOS LOS TAXISTAS (PENDIENTES + APROBADOS) ==========
    public void getAllDrivers(DriverListCallback callback) {
        Log.d(TAG, "Obteniendo todos los taxistas (pendientes + aprobados)...");

        List<UserModel> allDrivers = new ArrayList<>();
        final int[] completedQueries = {0}; // Contador para queries completadas
        final boolean[] hasErrors = {false};
        final int totalQueries = 2; // Solo 2 queries

        // Query 1: Obtener taxistas pendientes
        firestore.collection(PENDING_DRIVERS_COLLECTION)
                .get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful() && !hasErrors[0]) {
                        for (DocumentSnapshot document : task1.getResult()) {
                            try {
                                UserModel driver = UserModel.fromMap(document.getData());
                                driver.setUserId(document.getId());
                                driver.setActive(false); // false = pendiente
                                allDrivers.add(driver);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parseando taxista pendiente: " + e.getMessage());
                            }
                        }
                        Log.d(TAG, "✅ " + task1.getResult().size() + " taxistas pendientes obtenidos");
                    } else if (!hasErrors[0]) {
                        Log.e(TAG, "❌ Error obteniendo taxistas pendientes");
                        hasErrors[0] = true;
                        callback.onError("Error obteniendo taxistas pendientes");
                        return;
                    }

                    completedQueries[0]++;
                    if (completedQueries[0] == totalQueries) {
                        Log.d(TAG, "✅ Total: " + allDrivers.size() + " taxistas obtenidos");
                        callback.onSuccess(allDrivers);
                    }
                });

        // Query 2: Obtener taxistas aprobados
        firestore.collection(USERS_COLLECTION)
                .whereEqualTo("userType", "driver")
                .get()
                .addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful() && !hasErrors[0]) {
                        for (DocumentSnapshot document : task2.getResult()) {
                            try {
                                UserModel driver = UserModel.fromMap(document.getData());
                                driver.setUserId(document.getId());
                                driver.setActive(true); // true = aprobado
                                allDrivers.add(driver);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parseando taxista aprobado: " + e.getMessage());
                            }
                        }
                        Log.d(TAG, "✅ " + task2.getResult().size() + " taxistas aprobados obtenidos");
                    } else if (!hasErrors[0]) {
                        Log.e(TAG, "❌ Error obteniendo taxistas aprobados");
                        hasErrors[0] = true;
                        callback.onError("Error obteniendo taxistas aprobados");
                        return;
                    }

                    completedQueries[0]++;
                    if (completedQueries[0] == totalQueries) {
                        Log.d(TAG, "✅ Total: " + allDrivers.size() + " taxistas obtenidos");
                        callback.onSuccess(allDrivers);
                    }
                });
    }

    // Método para obtener reservas en checkout
    public void getCheckoutReservations(CheckoutCallback callback) {
        Log.d(TAG, "🚕 Obteniendo reservas de checkout...");

        firestore.collection(RESERVATIONS_COLLECTION)
                .whereEqualTo("status", "checkout")
                .whereEqualTo("freeTransport", true)
                .whereEqualTo("taxiStatus", "pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<CheckoutReservation> reservations = new ArrayList<>();

                        for (DocumentSnapshot document : task.getResult()) {
                            try {
                                CheckoutReservation reservation = CheckoutReservation.fromDocumentSnapshot(document);
                                reservations.add(reservation);

                                Log.d(TAG, "📋 Reserva encontrada: " + reservation.getHotelName() +
                                        " | Cliente: " + reservation.getClientName() +
                                        " | ID: " + document.getId());
                            } catch (Exception e) {
                                Log.e(TAG, "❌ Error parseando reserva: " + e.getMessage());
                            }
                        }

                        Log.d(TAG, "✅ " + reservations.size() + " reservas de checkout obtenidas");
                        callback.onSuccess(reservations);

                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        Log.e(TAG, "❌ Error obteniendo reservas: " + error);
                        callback.onError(error);
                    }
                });
    }

    /**
     * Listener en tiempo real para nuevas reservas de checkout
     */
    public ListenerRegistration listenToCheckoutReservations(RealtimeCheckoutCallback callback) {
        Log.d(TAG, "🔄 Iniciando listener de reservas de checkout...");

        return firestore.collection(RESERVATIONS_COLLECTION)
                .whereEqualTo("status", "checkout")
                .whereEqualTo("freeTransport", true)
                .whereEqualTo("taxiStatus", "pending")
                .addSnapshotListener((querySnapshot, e) -> {  // ⭐ CAMBIAR AQUÍ
                    if (e != null) {
                        Log.e(TAG, "❌ Error en listener: " + e.getMessage());
                        callback.onError(e.getMessage());
                        return;
                    }

                    if (querySnapshot != null) {  // ⭐ CAMBIAR AQUÍ
                        for (DocumentChange dc : querySnapshot.getDocumentChanges()) {  // ⭐ CAMBIAR AQUÍ
                            try {
                                CheckoutReservation reservation = CheckoutReservation.fromDocumentSnapshot(dc.getDocument());  // ⭐ CAMBIAR AQUÍ

                                switch (dc.getType()) {  // ⭐ CAMBIAR AQUÍ
                                    case ADDED:
                                        Log.d(TAG, "🆕 Nueva reserva: " + reservation.getHotelName());
                                        callback.onNewReservation(reservation);
                                        break;
                                    case MODIFIED:
                                        Log.d(TAG, "🔄 Reserva actualizada: " + reservation.getId());
                                        callback.onReservationUpdated(reservation);
                                        break;
                                    case REMOVED:
                                        Log.d(TAG, "🗑️ Reserva removida: " + dc.getDocument().getId());  // ⭐ CAMBIAR AQUÍ
                                        callback.onReservationRemoved(dc.getDocument().getId());  // ⭐ CAMBIAR AQUÍ
                                        break;
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "❌ Error procesando cambio: " + ex.getMessage());
                            }
                        }
                    }
                });
    }

    // Método para asignar taxista a reserva
    public void assignDriverToReservation(String reservationId, String driverId, DataCallback callback) {
        Log.d(TAG, "🚕 Asignando taxista " + driverId + " a reserva " + reservationId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("assignedDriverId", driverId);
        updates.put("taxiStatus", "assigned");
        updates.put("assignedAt", System.currentTimeMillis());

        firestore.collection(RESERVATIONS_COLLECTION)
                .document(reservationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Taxista asignado exitosamente");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error asignando taxista: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    public void updateTaxiServiceStatus(String reservationId, String newStatus, TaxiServiceCallback callback) {
        Log.d(TAG, "🔄 Actualizando estado de taxi a: " + newStatus);

        Map<String, Object> updates = new HashMap<>();
        updates.put("taxiStatus", newStatus);

        // Agregar timestamp según el estado
        switch (newStatus) {
            case "in_progress":
                updates.put("serviceStartTime", System.currentTimeMillis());
                break;
            case "completed":
                updates.put("serviceCompletedTime", System.currentTimeMillis());
                break;
            case "cancelled":
                updates.put("serviceCancelledTime", System.currentTimeMillis());
                break;
        }

        firestore.collection(RESERVATIONS_COLLECTION)
                .document(reservationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Estado actualizado a: " + newStatus);
                    callback.onSuccess();
                    callback.onStatusUpdate(newStatus);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error actualizando estado: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Crea un registro de servicio de taxi completado
     */
    public void createTaxiServiceRecord(String reservationId, String driverId,
                                        double distance, int duration, String notes, DataCallback callback) {
        Log.d(TAG, "📝 Creando registro de servicio de taxi...");

        Map<String, Object> serviceRecord = new HashMap<>();
        serviceRecord.put("reservationId", reservationId);
        serviceRecord.put("driverId", driverId);
        serviceRecord.put("distance", distance);
        serviceRecord.put("duration", duration);
        serviceRecord.put("notes", notes);
        serviceRecord.put("createdAt", System.currentTimeMillis());
        serviceRecord.put("serviceType", "checkout_taxi");

        firestore.collection(TAXI_SERVICES_COLLECTION)
                .add(serviceRecord)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "✅ Registro de servicio creado: " + documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error creando registro: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Obtiene todas las reservas asignadas a un taxista específico
     */
    public void getDriverAssignedReservations(String driverId, CheckoutCallback callback) {
        Log.d(TAG, "🚕 Obteniendo reservas asignadas al taxista: " + driverId);

        firestore.collection(RESERVATIONS_COLLECTION)
                .whereEqualTo("assignedDriverId", driverId)
                .whereIn("taxiStatus", List.of("assigned", "in_progress"))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<CheckoutReservation> reservations = new ArrayList<>();

                        for (DocumentSnapshot document : task.getResult()) {
                            try {
                                CheckoutReservation reservation = CheckoutReservation.fromDocumentSnapshot(document);
                                reservations.add(reservation);

                                Log.d(TAG, "📋 Reserva asignada: " + reservation.getHotelName() +
                                        " | Estado: " + reservation.getTaxiStatus());
                            } catch (Exception e) {
                                Log.e(TAG, "❌ Error parseando reserva asignada: " + e.getMessage());
                            }
                        }

                        Log.d(TAG, "✅ " + reservations.size() + " reservas asignadas obtenidas");
                        callback.onSuccess(reservations);

                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        Log.e(TAG, "❌ Error obteniendo reservas asignadas: " + error);
                        callback.onError(error);
                    }
                });
    }


    // ========== MÉTODO PARA OBTENER ADMINISTRADORES DE HOTEL ==========
    public void getHotelAdmins(DriverListCallback callback) {
        Log.d(TAG, "📋 Obteniendo administradores de hotel... [timestamp: " + System.currentTimeMillis() + "]");

        firestore.collection(USERS_COLLECTION)
                .whereEqualTo("userType", "hotel_admin")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<UserModel> admins = new ArrayList<>();

                        for (DocumentSnapshot document : task.getResult()) {
                            try {
                                UserModel admin = UserModel.fromMap(document.getData());
                                admin.setUserId(document.getId());
                                admins.add(admin);

                                // 🔥 LOG DETALLADO DE CADA ADMIN ENCONTRADO
                                Log.d(TAG, "👤 Admin encontrado: " + admin.getEmail() +
                                        " | ID: " + document.getId() +
                                        " | Created: " + admin.getCreatedAt());
                            } catch (Exception e) {
                                Log.e(TAG, "Error parseando admin de hotel: " + e.getMessage());
                            }
                        }

                        Log.d(TAG, "✅ " + admins.size() + " administradores de hotel obtenidos [timestamp: " + System.currentTimeMillis() + "]");
                        callback.onSuccess(admins);

                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        Log.e(TAG, "❌ Error obteniendo admins de hotel: " + error);
                        callback.onError(error);
                    }
                });
    }

    // ========== MÉTODO PARA ACTIVAR/DESACTIVAR ADMIN ==========
    public void toggleHotelAdminStatus(String adminId, boolean newStatus, DataCallback callback) {
        Log.d(TAG, "Cambiando estado de admin: " + adminId + " a " + newStatus);

        firestore.collection(USERS_COLLECTION)
                .document(adminId)
                .update("active", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Estado de admin actualizado");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error actualizando estado: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    // ========== MÉTODO PARA ACTUALIZAR ADMIN DE HOTEL ==========
    public void updateHotelAdmin(String adminId, Map<String, Object> updates, DataCallback callback) {
        Log.d(TAG, "Actualizando admin de hotel: " + adminId);

        // Validar parámetros
        if (adminId == null || adminId.isEmpty()) {
            Log.e(TAG, "❌ ID de admin inválido");
            callback.onError("ID de administrador inválido");
            return;
        }

        if (updates == null || updates.isEmpty()) {
            Log.e(TAG, "❌ No hay datos para actualizar");
            callback.onError("No hay datos para actualizar");
            return;
        }

        // Agregar timestamp de actualización
        updates.put("updatedAt", System.currentTimeMillis());

        Log.d(TAG, "Datos a actualizar: " + updates.toString());

        firestore.collection(USERS_COLLECTION)
                .document(adminId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Admin de hotel actualizado exitosamente");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error actualizando admin: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    // ========== MÉTODO PARA OBTENER TODOS LOS USUARIOS DEL SISTEMA ==========
    public void getAllUsers(DriverListCallback callback) {
        Log.d(TAG, "Obteniendo todos los usuarios del sistema...");

        List<UserModel> allUsers = new ArrayList<>();
        final int[] completedQueries = {0};
        final boolean[] hasErrors = {false};
        final int totalQueries = 2; // users + pending_drivers

        // Query 1: Obtener usuarios activos (clientes, admins hotel, taxistas aprobados, superadmins)
        firestore.collection(USERS_COLLECTION)
                .get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful() && !hasErrors[0]) {
                        for (DocumentSnapshot document : task1.getResult()) {
                            try {
                                UserModel user = UserModel.fromMap(document.getData());
                                user.setUserId(document.getId());
                                // Marcar como usuario activo/aprobado
                                user.setActive(true);
                                allUsers.add(user);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parseando usuario activo: " + e.getMessage());
                            }
                        }
                        Log.d(TAG, "✅ " + task1.getResult().size() + " usuarios activos obtenidos");
                    } else if (!hasErrors[0]) {
                        Log.e(TAG, "❌ Error obteniendo usuarios activos");
                        hasErrors[0] = true;
                        callback.onError("Error obteniendo usuarios activos");
                        return;
                    }

                    completedQueries[0]++;
                    if (completedQueries[0] == totalQueries) {
                        Log.d(TAG, "✅ Total: " + allUsers.size() + " usuarios obtenidos");
                        callback.onSuccess(allUsers);
                    }
                });

        // Query 2: Obtener taxistas pendientes
        firestore.collection(PENDING_DRIVERS_COLLECTION)
                .get()
                .addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful() && !hasErrors[0]) {
                        for (DocumentSnapshot document : task2.getResult()) {
                            try {
                                UserModel user = UserModel.fromMap(document.getData());
                                user.setUserId(document.getId());
                                user.setUserType("driver"); // Asegurar que sea driver
                                // Marcar como pendiente
                                user.setActive(false);
                                allUsers.add(user);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parseando taxista pendiente: " + e.getMessage());
                            }
                        }
                        Log.d(TAG, "✅ " + task2.getResult().size() + " taxistas pendientes obtenidos");
                    } else if (!hasErrors[0]) {
                        Log.e(TAG, "❌ Error obteniendo taxistas pendientes");
                        hasErrors[0] = true;
                        callback.onError("Error obteniendo taxistas pendientes");
                        return;
                    }

                    completedQueries[0]++;
                    if (completedQueries[0] == totalQueries) {
                        Log.d(TAG, "✅ Total: " + allUsers.size() + " usuarios obtenidos");
                        callback.onSuccess(allUsers);
                    }
                });
    }

    // ========== MÉTODO PARA OBTENER ESTADÍSTICAS DE USUARIOS ==========
    public void getUserStatistics(UserStatsCallback callback) {
        Log.d(TAG, "Obteniendo estadísticas de usuarios...");

        getAllUsers(new DriverListCallback() {
            @Override
            public void onSuccess(List<UserModel> users) {
                UserStatistics stats = new UserStatistics();

                for (UserModel user : users) {
                    stats.totalUsers++;

                    switch (user.getUserType()) {
                        case "client":
                            stats.totalClients++;
                            if (user.isActive()) stats.activeClients++;
                            break;
                        case "driver":
                            stats.totalDrivers++;
                            if (user.isActive()) {
                                stats.approvedDrivers++;
                            } else {
                                stats.pendingDrivers++;
                            }
                            break;
                        case "hotel_admin":
                            stats.totalHotelAdmins++;
                            if (user.isActive()) stats.activeHotelAdmins++;
                            break;
                        case "superadmin":
                            stats.totalSuperAdmins++;
                            break;
                    }

                    if (user.isActive()) stats.totalActiveUsers++;
                }

                Log.d(TAG, "✅ Estadísticas calculadas: " + stats.totalUsers + " usuarios totales");
                callback.onSuccess(stats);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error obteniendo estadísticas: " + error);
                callback.onError(error);
            }
        });
    }

    // ========== INTERFACES PARA ESTADÍSTICAS ==========
    public interface UserStatsCallback {
        void onSuccess(UserStatistics stats);
        void onError(String error);
    }

    public static class UserStatistics {
        public int totalUsers = 0;
        public int totalActiveUsers = 0;
        public int totalClients = 0;
        public int activeClients = 0;
        public int totalDrivers = 0;
        public int approvedDrivers = 0;
        public int pendingDrivers = 0;
        public int totalHotelAdmins = 0;
        public int activeHotelAdmins = 0;
        public int totalSuperAdmins = 0;

        @Override
        public String toString() {
            return "UserStatistics{" +
                    "totalUsers=" + totalUsers +
                    ", activeUsers=" + totalActiveUsers +
                    ", clients=" + totalClients +
                    ", drivers=" + totalDrivers +
                    ", hotelAdmins=" + totalHotelAdmins +
                    '}';
        }
    }


    // ========== FIX PREVENTIVO: LIMPIEZA DE DATOS ANTIGUOS ==========

    /**
     * Método para limpiar datos antiguos de un usuario antes de crear nuevos
     * Esto previene problemas de reutilización de UID
     */
    private void deleteOldUserData(String userId, Runnable onComplete) {
        Log.d(TAG, "🧹 Limpiando datos antiguos para UID: " + userId);

        final int[] completedDeletions = {0};
        final int totalDeletions = 3; // users, pending_drivers, hotel_admins

        // Eliminar de 'users'
        firestore.collection("users").document(userId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "✅ Datos eliminados de 'users'");
                    } else {
                        Log.d(TAG, "ℹ️ No había datos en 'users' (normal para nuevo usuario)");
                    }

                    completedDeletions[0]++;
                    if (completedDeletions[0] == totalDeletions && onComplete != null) {
                        onComplete.run();
                    }
                });

        // Eliminar de 'pending_drivers'
        firestore.collection("pending_drivers").document(userId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "✅ Datos eliminados de 'pending_drivers'");
                    } else {
                        Log.d(TAG, "ℹ️ No había datos en 'pending_drivers' (normal para nuevo usuario)");
                    }

                    completedDeletions[0]++;
                    if (completedDeletions[0] == totalDeletions && onComplete != null) {
                        onComplete.run();
                    }
                });

        // Eliminar de 'hotel_admins' (por si acaso)
        firestore.collection("hotel_admins").document(userId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "✅ Datos eliminados de 'hotel_admins'");
                    } else {
                        Log.d(TAG, "ℹ️ No había datos en 'hotel_admins' (normal para nuevo usuario)");
                    }

                    completedDeletions[0]++;
                    if (completedDeletions[0] == totalDeletions && onComplete != null) {
                        onComplete.run();
                    }
                });
    }

    public void createSampleCheckoutReservations(DataCallback callback) {
        Log.d(TAG, "🧪 Creando reservas de ejemplo...");

        List<Map<String, Object>> sampleReservations = new ArrayList<>();

        // ✅ SOLO 2 RESERVAS DE EJEMPLO (en lugar de 13+)

        // Reserva 1
        Map<String, Object> reservation1 = new HashMap<>();
        reservation1.put("hotelName", "Hotel Gran Plaza");
        reservation1.put("hotelAddress", "Av. La Marina 123, San Miguel");
        reservation1.put("hotelPhone", "+51 1 234-5678");
        reservation1.put("clientName", "Juan Pérez");
        reservation1.put("clientPhone", "+51 987 654 321");
        reservation1.put("clientEmail", "juan.perez@email.com");
        reservation1.put("checkoutDate", "2025-07-20"); // Fecha actual + 1 día
        reservation1.put("checkoutTime", "11:30");
        reservation1.put("roomNumber", "205");
        reservation1.put("roomType", "Suite Ejecutiva");
        reservation1.put("status", "checkout");
        reservation1.put("freeTransport", true);
        reservation1.put("taxiStatus", "pending");
        reservation1.put("estimatedDistance", 15.5);
        reservation1.put("estimatedDuration", 25);
        reservation1.put("createdAt", System.currentTimeMillis());
        reservation1.put("destinationAddress", "Aeropuerto Internacional Jorge Chávez, Callao");
        reservation1.put("notes", "Cliente esperando en lobby, equipaje pesado. Vuelo nacional a las 14:00");

        // Reserva 2
        Map<String, Object> reservation2 = new HashMap<>();
        reservation2.put("hotelName", "Hotel Miraflores Park");
        reservation2.put("hotelAddress", "Av. Malecón 456, Miraflores");
        reservation2.put("hotelPhone", "+51 1 345-6789");
        reservation2.put("clientName", "María García");
        reservation2.put("clientPhone", "+51 987 123 456");
        reservation2.put("clientEmail", "maria.garcia@email.com");
        reservation2.put("checkoutDate", "2025-07-20"); // Fecha actual + 1 día
        reservation2.put("checkoutTime", "14:15");
        reservation2.put("roomNumber", "812");
        reservation2.put("roomType", "Habitación Doble");
        reservation2.put("status", "checkout");
        reservation2.put("freeTransport", true);
        reservation2.put("taxiStatus", "pending");
        reservation2.put("estimatedDistance", 18.2);
        reservation2.put("estimatedDuration", 30);
        reservation2.put("createdAt", System.currentTimeMillis() + 300000); // 5 min después
        reservation2.put("destinationAddress", "Aeropuerto Internacional Jorge Chávez, Callao");
        reservation2.put("notes", "Vuelo internacional a las 17:45, llegar 3 horas antes. Cliente con equipaje extra");

        sampleReservations.add(reservation1);
        sampleReservations.add(reservation2);

        // ✅ IMPLEMENTACIÓN MEJORADA CON CONTADOR Y MANEJO DE ERRORES
        final int[] createdCount = {0};
        final int[] errorCount = {0};
        final int totalToCreate = sampleReservations.size();

        Log.d(TAG, "📝 Creando " + totalToCreate + " reservas de ejemplo...");

        // Guardar cada reserva en Firebase
        for (int i = 0; i < sampleReservations.size(); i++) {
            Map<String, Object> reservation = sampleReservations.get(i);
            final int reservationIndex = i + 1;

            firestore.collection(RESERVATIONS_COLLECTION)
                    .add(reservation)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "✅ Reserva " + reservationIndex + " creada: " + documentReference.getId());
                        createdCount[0]++;

                        // Verificar si todas las reservas fueron creadas
                        if (createdCount[0] + errorCount[0] == totalToCreate) {
                            if (errorCount[0] == 0) {
                                Log.d(TAG, "🎉 Todas las reservas creadas exitosamente");
                                callback.onSuccess();
                            } else {
                                Log.w(TAG, "⚠️ Algunas reservas fallaron: " + createdCount[0] + " exitosas, " + errorCount[0] + " errores");
                                callback.onSuccess(); // Considerar éxito parcial como éxito
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Error creando reserva " + reservationIndex + ": " + e.getMessage());
                        errorCount[0]++;

                        // Verificar si todas las operaciones terminaron
                        if (createdCount[0] + errorCount[0] == totalToCreate) {
                            if (createdCount[0] > 0) {
                                Log.w(TAG, "⚠️ Creación parcial: " + createdCount[0] + " exitosas, " + errorCount[0] + " errores");
                                callback.onSuccess(); // Éxito parcial
                            } else {
                                Log.e(TAG, "💥 Falló la creación de todas las reservas");
                                callback.onError("Error creando todas las reservas de ejemplo");
                            }
                        }
                    });
        }

        // ✅ TIMEOUT DE SEGURIDAD
        new android.os.Handler().postDelayed(() -> {
            if (createdCount[0] + errorCount[0] < totalToCreate) {
                Log.w(TAG, "⏰ Timeout en creación de reservas. Creadas: " + createdCount[0]);
                if (createdCount[0] > 0) {
                    callback.onSuccess(); // Al menos algunas se crearon
                } else {
                    callback.onError("Timeout creando reservas de ejemplo");
                }
            }
        }, 10000); // 10 segundos timeout
    }

    /**
     * Método mejorado para guardar datos de usuario con limpieza preventiva
     */
    public void saveUserDataSafe(String userId, UserModel userModel, DataCallback callback) {
        Log.d(TAG, "💾 Guardando datos del usuario SAFE: " + userId + " - Tipo: " + userModel.getUserType());

        // PASO 1: Verificar si ya existe algún dato para este UID
        checkIfUserExistsInAnyCollection(userId, new UserExistsCallback() {
            @Override
            public void onUserExists(String foundInCollection) {
                Log.w(TAG, "⚠️ Usuario ya existe en '" + foundInCollection + "', limpiando datos antiguos...");

                // Limpiar datos antiguos y luego proceder
                deleteOldUserData(userId, () -> {
                    // Esperar 1 segundo y proceder con creación limpia
                    new android.os.Handler().postDelayed(() -> {
                        proceedWithUserCreation(userId, userModel, callback);
                    }, 1000);
                });
            }

            @Override
            public void onUserNotExists() {
                Log.d(TAG, "✅ Usuario limpio, procediendo directamente");
                // Usuario limpio, proceder normalmente
                proceedWithUserCreation(userId, userModel, callback);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error verificando existencia de usuario: " + error);
                // En caso de error, proceder normalmente (mejor que fallar)
                proceedWithUserCreation(userId, userModel, callback);
            }
        });
    }

    /**
     * Método para proceder con la creación después de limpieza
     */
    private void proceedWithUserCreation(String userId, UserModel userModel, DataCallback callback) {
        Log.d(TAG, "🚀 Procediendo con creación limpia para: " + userId + " - Tipo: " + userModel.getUserType());

        String targetCollection;

        // Determinar colección según tipo de usuario
        if ("driver".equals(userModel.getUserType())) {
            targetCollection = PENDING_DRIVERS_COLLECTION;
            Log.d(TAG, "📋 Guardando taxista en pending_drivers");
        } else {
            targetCollection = USERS_COLLECTION;
            Log.d(TAG, "👤 Guardando usuario en users");
        }

        // Guardar en la colección apropiada
        firestore.collection(targetCollection)
                .document(userId)
                .set(userModel.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Usuario guardado exitosamente en " + targetCollection);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error guardando usuario: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Interface para verificar existencia de usuario
     */
    public interface UserExistsCallback {
        void onUserExists(String foundInCollection);
        void onUserNotExists();
        void onError(String error);
    }

    /**
     * Verificar si un usuario existe en cualquier colección
     */
    private void checkIfUserExistsInAnyCollection(String userId, UserExistsCallback callback) {
        Log.d(TAG, "🔍 Verificando existencia de usuario en todas las colecciones: " + userId);

        // Verificar en 'users' primero
        firestore.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        Log.d(TAG, "⚠️ Usuario encontrado en 'users'");
                        callback.onUserExists("users");
                        return;
                    }

                    // Si no está en users, verificar en pending_drivers
                    firestore.collection("pending_drivers").document(userId).get()
                            .addOnCompleteListener(pendingTask -> {
                                if (pendingTask.isSuccessful() && pendingTask.getResult() != null && pendingTask.getResult().exists()) {
                                    Log.d(TAG, "⚠️ Usuario encontrado en 'pending_drivers'");
                                    callback.onUserExists("pending_drivers");
                                    return;
                                }

                                // Si no está en ninguna, usuario limpio
                                Log.d(TAG, "✅ Usuario no existe en ninguna colección");
                                callback.onUserNotExists();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "❌ Error verificando pending_drivers: " + e.getMessage());
                                callback.onError(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error verificando users: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    // ========== ACTUALIZAR PERFIL DE TAXISTA ==========
    public void updateDriverProfile(String userId, String newPhone, String newAddress, String newPhotoUrl, DataCallback callback) {
        Log.d(TAG, "🔄 Actualizando perfil de taxista: " + userId);
        Log.d(TAG, "Nuevo teléfono: " + newPhone);
        Log.d(TAG, "Nueva dirección: " + newAddress);
        Log.d(TAG, "Nueva foto: " + (newPhotoUrl != null ? "SÍ" : "NO"));

        // Crear mapa con los campos a actualizar
        Map<String, Object> updates = new HashMap<>();

        if (newPhone != null && !newPhone.trim().isEmpty()) {
            updates.put("telefono", newPhone.trim());
        }

        if (newAddress != null && !newAddress.trim().isEmpty()) {
            updates.put("direccion", newAddress.trim());
        }

        if (newPhotoUrl != null && !newPhotoUrl.trim().isEmpty()) {
            updates.put("photoUrl", newPhotoUrl.trim());
        }

        // Agregar timestamp de actualización
        updates.put("updatedAt", System.currentTimeMillis());

        if (updates.size() <= 1) { // Solo timestamp
            Log.w(TAG, "⚠️ No hay datos para actualizar");
            callback.onError("No hay cambios para guardar");
            return;
        }

        Log.d(TAG, "📝 Datos a actualizar: " + updates.toString());

        // Actualizar en la colección 'users' (taxistas aprobados)
        firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Perfil de taxista actualizado exitosamente en 'users'");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error actualizando perfil en 'users': " + e.getMessage());
                    // Si falla en users, no hay más intentos porque el taxista debe estar aprobado
                    callback.onError("Error actualizando perfil: " + e.getMessage());
                });
    }


    // ========== ACTUALIZAR PERFIL DE CLIENTE ==========
    public void updateClientProfile(String userId, String newPhone, String newAddress, String newPhotoUrl, DataCallback callback) {
        Log.d(TAG, "🔄 Actualizando perfil de cliente: " + userId);
        Log.d(TAG, "Nuevo teléfono: " + newPhone);
        Log.d(TAG, "Nueva dirección: " + newAddress);
        Log.d(TAG, "Nueva foto: " + (newPhotoUrl != null ? "SÍ" : "NO"));

        // Crear mapa con los campos a actualizar
        Map<String, Object> updates = new HashMap<>();

        if (newPhone != null && !newPhone.trim().isEmpty()) {
            updates.put("telefono", newPhone.trim());
        }

        if (newAddress != null && !newAddress.trim().isEmpty()) {
            updates.put("direccion", newAddress.trim());
        }

        if (newPhotoUrl != null && !newPhotoUrl.trim().isEmpty()) {
            updates.put("photoUrl", newPhotoUrl.trim());
        }

        // Agregar timestamp de actualización
        updates.put("updatedAt", System.currentTimeMillis());

        if (updates.size() <= 1) { // Solo timestamp
            Log.w(TAG, "⚠️ No hay datos para actualizar en cliente");
            callback.onError("No hay cambios para guardar");
            return;
        }

        Log.d(TAG, "📝 Datos del cliente a actualizar: " + updates.toString());

        // Actualizar en la colección 'users' (clientes activos)
        firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Perfil de cliente actualizado exitosamente en 'users'");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error actualizando perfil de cliente: " + e.getMessage());
                    callback.onError("Error actualizando perfil: " + e.getMessage());
                });
    }

}