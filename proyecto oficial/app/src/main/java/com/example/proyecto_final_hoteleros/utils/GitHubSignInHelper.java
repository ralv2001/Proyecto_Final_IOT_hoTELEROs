package com.example.proyecto_final_hoteleros.utils;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthCredential;
import com.google.firebase.auth.OAuthProvider;

import java.util.Arrays;

public class GitHubSignInHelper {

    private static final String TAG = "GitHubSignInHelper";
    private static final String GITHUB_PROVIDER_ID = "github.com";

    private FirebaseAuth firebaseAuth;
    private Activity activity;
    private GitHubSignInCallback callback;

    public interface GitHubSignInCallback {
        void onSignInSuccess(FirebaseUser user);
        void onSignInFailure(String error);
        void onSignInCanceled();
        void onAccountCollision(String email);
    }

    public GitHubSignInHelper(Activity activity) {
        this.activity = activity;
        this.firebaseAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "✅ GitHub Sign-In Helper inicializado");
    }

    /**
     * Método principal para iniciar sesión con GitHub
     * Sigue la documentación oficial de Firebase
     */
    public void signIn(GitHubSignInCallback callback) {
        this.callback = callback;

        if (firebaseAuth == null) {
            Log.e(TAG, "FirebaseAuth no está inicializado");
            if (callback != null) {
                callback.onSignInFailure("Error de configuración de Firebase");
            }
            return;
        }

        Log.d(TAG, "Iniciando GitHub Sign-In...");

        // PASO 1: Crear el proveedor OAuth según documentación oficial
        OAuthProvider.Builder provider = OAuthProvider.newBuilder(GITHUB_PROVIDER_ID);

        // PASO 2: Configurar scopes (opcional pero recomendado)
        provider.setScopes(Arrays.asList("user:email"));

        // PASO 3: Verificar si hay resultado pendiente (OBLIGATORIO según docs)
        Task<AuthResult> pendingResultTask = firebaseAuth.getPendingAuthResult();
        if (pendingResultTask != null) {
            Log.d(TAG, "Hay una operación pendiente, manejándola...");
            pendingResultTask
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            handleSuccess(authResult);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Operación pendiente falló, iniciando nueva...", e);
                            startNewSignIn(provider);
                        }
                    });
        } else {
            // PASO 4: No hay operación pendiente, iniciar nueva
            startNewSignIn(provider);
        }
    }

    /**
     * Inicia un nuevo flujo de autenticación con GitHub
     */
    private void startNewSignIn(OAuthProvider.Builder provider) {
        Log.d(TAG, "Iniciando nuevo flujo de GitHub Sign-In...");

        // Usar el método oficial de Firebase
        firebaseAuth.startActivityForSignInWithProvider(activity, provider.build())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        handleSuccess(authResult);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleFailure(e);
                    }
                });
    }

    /**
     * Maneja el éxito de la autenticación
     */
    private void handleSuccess(AuthResult authResult) {
        Log.d(TAG, "✅ GitHub Sign-In exitoso!");

        FirebaseUser user = authResult.getUser();
        if (user != null) {
            Log.d(TAG, "Usuario autenticado: " + user.getEmail());

            // Opcional: Obtener el token de acceso de GitHub
            if (authResult.getCredential() instanceof OAuthCredential) {
                OAuthCredential credential = (OAuthCredential) authResult.getCredential();
                String accessToken = credential.getAccessToken();
                Log.d(TAG, "GitHub Access Token obtenido");
                // Puedes usar este token para llamar a la API de GitHub si es necesario
            }

            if (callback != null) {
                callback.onSignInSuccess(user);
            }
        } else {
            Log.e(TAG, "Usuario es null después de autenticación exitosa");
            if (callback != null) {
                callback.onSignInFailure("Error: Usuario null");
            }
        }
    }

    /**
     * Maneja los errores de autenticación
     */
    private void handleFailure(Exception e) {
        Log.e(TAG, "❌ Error en GitHub Sign-In", e);

        if (callback != null) {
            if (e instanceof FirebaseAuthUserCollisionException) {
                // El email ya está en uso con otro proveedor
                FirebaseAuthUserCollisionException collisionException = (FirebaseAuthUserCollisionException) e;
                String email = collisionException.getEmail();
                Log.w(TAG, "Colisión de cuenta para email: " + email);
                callback.onAccountCollision(email != null ? email : "Email desconocido");
            } else {
                String errorMessage = e.getMessage();
                if (errorMessage != null && errorMessage.contains("CANCELED")) {
                    Log.d(TAG, "Usuario canceló el GitHub Sign-In");
                    callback.onSignInCanceled();
                } else if (errorMessage != null && errorMessage.contains("NETWORK_ERROR")) {
                    callback.onSignInFailure("Error de red. Verifica tu conexión a internet");
                } else {
                    callback.onSignInFailure("Error en GitHub Sign-In: " + errorMessage);
                }
            }
        }
    }

    /**
     * Vincular GitHub a usuario ya autenticado
     */
    /**
     * Vincular GitHub a usuario ya autenticado
     */
    public void linkGitHubToCurrentUser(GitHubSignInCallback callback) {
        this.callback = callback;

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) {
                callback.onSignInFailure("No hay usuario autenticado para vincular");
            }
            return;
        }

        Log.d(TAG, "Vinculando GitHub a usuario: " + currentUser.getEmail());

        // Configurar proveedor
        OAuthProvider.Builder provider = OAuthProvider.newBuilder(GITHUB_PROVIDER_ID);
        provider.setScopes(Arrays.asList("user:email"));

        // Usar API oficial de linking de Firebase
        currentUser.startActivityForLinkWithProvider(activity, provider.build())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "✅ GitHub vinculado exitosamente!");
                        if (callback != null) {
                            callback.onSignInSuccess(authResult.getUser());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "❌ Error vinculando GitHub", e);
                        if (callback != null) {
                            callback.onSignInFailure("Error vinculando GitHub: " + e.getMessage());
                        }
                    }
                });
    }

    /**
     * Cerrar sesión
     */
    public void signOut() {
        if (firebaseAuth != null) {
            firebaseAuth.signOut();
            Log.d(TAG, "Usuario desconectado de GitHub/Firebase");
        }
    }
}