package com.example.proyecto_final_hoteleros.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;

import java.util.Arrays;
import java.util.List;

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

        // Configurar el proveedor OAuth para GitHub
        OAuthProvider.Builder provider = OAuthProvider.newBuilder(GITHUB_PROVIDER_ID);
        provider.setScopes(Arrays.asList("user:email"));

        // PASO 1: Verificar si hay resultado pendiente
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
                            // Si falla la operación pendiente, intentar nueva
                            Log.w(TAG, "Operación pendiente falló, intentando nueva", e);
                            startNewSignIn(provider);
                        }
                    });
        } else {
            // No hay operación pendiente, iniciar nueva
            startNewSignIn(provider);
        }
    }

    private void startNewSignIn(OAuthProvider.Builder provider) {
        Log.d(TAG, "Iniciando nuevo GitHub Sign-In...");

        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Error iniciando GitHub Sign-In", e);
            if (callback != null) {
                callback.onSignInFailure("Error iniciando GitHub Sign-In: " + e.getMessage());
            }
        }
    }

    private void handleSuccess(AuthResult authResult) {
        Log.d(TAG, "✅ GitHub Sign-In exitoso!");

        FirebaseUser user = authResult.getUser();
        if (user != null) {
            Log.d(TAG, "Usuario GitHub autenticado:");
            Log.d(TAG, "Email: " + user.getEmail());
            Log.d(TAG, "Display Name: " + user.getDisplayName());
            Log.d(TAG, "UID: " + user.getUid());

            if (callback != null) {
                callback.onSignInSuccess(user);
            }
        } else {
            Log.e(TAG, "FirebaseUser es null después del sign-in exitoso");
            if (callback != null) {
                callback.onSignInFailure("Error al obtener los datos del usuario");
            }
        }
    }

    private void handleFailure(Exception exception) {
        Log.e(TAG, "=== ERROR EN GITHUB SIGN-IN ===");

        if (callback == null) return;

        if (exception != null) {
            Log.e(TAG, "Error: " + exception.getMessage(), exception);

            String errorMessage = exception.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains("CANCELLED") || errorMessage.contains("cancelled")) {
                    Log.d(TAG, "Usuario canceló el GitHub Sign-In");
                    callback.onSignInCanceled();

                } else if (errorMessage.contains("An account already exists") ||
                        exception instanceof FirebaseAuthUserCollisionException) {
                    // MANEJO OFICIAL DE COLLISION
                    Log.d(TAG, "Account collision detectada");

                    if (exception instanceof FirebaseAuthUserCollisionException) {
                        FirebaseAuthUserCollisionException collisionException =
                                (FirebaseAuthUserCollisionException) exception;
                        String email = collisionException.getEmail();

                        if (email != null) {
                            Log.d(TAG, "Email en collision: " + email);
                            callback.onAccountCollision(email);
                        } else {
                            callback.onSignInFailure("Error: no se pudo detectar el email");
                        }
                    } else {
                        callback.onSignInFailure("Cuenta ya existe con diferente proveedor");
                    }

                } else if (errorMessage.contains("network") || errorMessage.contains("NETWORK")) {
                    callback.onSignInFailure("Error de conexión. Verifica tu internet");

                } else {
                    callback.onSignInFailure("Error en GitHub Sign-In: " + errorMessage);
                }
            } else {
                callback.onSignInFailure("Error desconocido en GitHub Sign-In");
            }
        } else {
            callback.onSignInFailure("Error desconocido en GitHub Sign-In");
        }
    }

    /**
     * MÉTODO OFICIAL para vincular GitHub a usuario ya autenticado
     */
    public void linkGitHubToCurrentUser(GitHubSignInCallback callback, ActivityResultLauncher<Intent> launcher) {
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

        // USAR API OFICIAL DE LINKING (SIN launcher.launch)
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

    public void signOut() {
        if (firebaseAuth != null) {
            firebaseAuth.signOut();
            Log.d(TAG, "Usuario desconectado de GitHub/Firebase");
        }
    }
}