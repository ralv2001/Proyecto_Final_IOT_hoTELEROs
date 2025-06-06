package com.example.proyecto_final_hoteleros.utils;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import com.example.proyecto_final_hoteleros.models.UserModel;
import java.util.Map;

public class FirebaseManager {

    private static final String TAG = "FirebaseManager";
    private static final String USERS_COLLECTION = "users";
    private static final String PENDING_DRIVERS_COLLECTION = "pending_drivers";

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


}