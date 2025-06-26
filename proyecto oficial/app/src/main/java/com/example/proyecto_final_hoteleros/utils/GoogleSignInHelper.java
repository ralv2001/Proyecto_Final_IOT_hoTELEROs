package com.example.proyecto_final_hoteleros.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.example.proyecto_final_hoteleros.R;

public class GoogleSignInHelper {

    private static final String TAG = "GoogleSignInHelper";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private Activity activity;
    private GoogleSignInCallback callback;

    public interface GoogleSignInCallback {
        void onSignInSuccess(FirebaseUser user);
        void onSignInFailure(String error);
        void onSignInCanceled();
    }

    public GoogleSignInHelper(Activity activity) {
        this.activity = activity;
        this.firebaseAuth = FirebaseAuth.getInstance();
        initializeGoogleSignIn();
    }

    private void initializeGoogleSignIn() {
        try {
            Log.d(TAG, "Inicializando Google Sign-In...");

            // Verificar que Google Play Services esté disponible
            GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
            int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(activity);

            if (resultCode != ConnectionResult.SUCCESS) {
                Log.e(TAG, "Google Play Services no está disponible. Código: " + resultCode);
                if (googleApiAvailability.isUserResolvableError(resultCode)) {
                    googleApiAvailability.getErrorDialog(activity, resultCode, 9000).show();
                }
                return;
            }

            Log.d(TAG, "Google Play Services OK, configurando GoogleSignInOptions...");

            // Configurar Google Sign-In Options
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(activity.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            googleSignInClient = GoogleSignIn.getClient(activity, gso);
            Log.d(TAG, "✅ Google Sign-In inicializado correctamente");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error inicializando Google Sign-In", e);
        }
    }

    public void signIn(GoogleSignInCallback callback, ActivityResultLauncher<Intent> launcher) {
        this.callback = callback;

        if (googleSignInClient == null) {
            Log.e(TAG, "GoogleSignInClient no está inicializado");
            if (callback != null) {
                callback.onSignInFailure("Error de configuración de Google Sign-In");
            }
            return;
        }

        // Cerrar cualquier sesión anterior
        googleSignInClient.signOut().addOnCompleteListener(activity, task -> {
            Log.d(TAG, "Sesión anterior cerrada, iniciando Google Sign-In...");
            // Iniciar el proceso de sign-in usando el launcher
            Intent signInIntent = googleSignInClient.getSignInIntent();
            launcher.launch(signInIntent);
        });
    }

    public void handleSignInResult(Intent data) {
        Log.d(TAG, "=== MANEJANDO RESULTADO DE GOOGLE SIGN-IN ===");

        if (callback == null) {
            Log.w(TAG, "Callback is null, cannot handle sign-in result");
            return;
        }

        if (data == null) {
            Log.e(TAG, "Intent data is null");
            callback.onSignInFailure("Error: no se recibieron datos del sign-in");
            return;
        }

        try {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Log.d(TAG, "Task obtenido, verificando resultado...");

            GoogleSignInAccount account = task.getResult(ApiException.class);

            if (account != null) {
                Log.d(TAG, "✅ Google Sign-In exitoso!");
                Log.d(TAG, "Email: " + account.getEmail());
                Log.d(TAG, "Display Name: " + account.getDisplayName());
                Log.d(TAG, "ID Token disponible: " + (account.getIdToken() != null));
                firebaseAuthWithGoogle(account.getIdToken());
            } else {
                Log.e(TAG, "Account is null después del sign-in");
                callback.onSignInFailure("Error al obtener la cuenta de Google");
            }

        } catch (ApiException e) {
            Log.e(TAG, "=== ERROR EN GOOGLE SIGN-IN ===");
            Log.e(TAG, "Status Code: " + e.getStatusCode());
            Log.e(TAG, "Status Message: " + e.getMessage());
            Log.e(TAG, "Status: " + e.getStatus());

            // Manejar códigos de error específicos
            switch (e.getStatusCode()) {
                case 12501: // SIGN_IN_CANCELLED
                    Log.d(TAG, "Usuario canceló el sign-in");
                    callback.onSignInCanceled();
                    break;
                case 7: // NETWORK_ERROR
                    Log.e(TAG, "Error de red durante sign-in");
                    callback.onSignInFailure("Error de conexión. Verifica tu internet e inténtalo de nuevo");
                    break;
                case 10: // DEVELOPER_ERROR
                    Log.e(TAG, "Error de configuración del desarrollador");
                    callback.onSignInFailure("Error de configuración. El SHA-1 o Client ID pueden estar mal configurados");
                    break;
                default:
                    Log.e(TAG, "Error desconocido: " + e.getStatusCode());
                    callback.onSignInFailure("Error en Google Sign-In: " + e.getMessage());
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Excepción inesperada durante sign-in", e);
            callback.onSignInFailure("Error inesperado: " + e.getMessage());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d(TAG, "Autenticando con Firebase usando token de Google");

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "✅ Autenticación Firebase exitosa");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (callback != null && user != null) {
                                callback.onSignInSuccess(user);
                            }
                        } else {
                            Log.e(TAG, "❌ Error en autenticación Firebase", task.getException());
                            if (callback != null) {
                                String error = task.getException() != null ?
                                        task.getException().getMessage() :
                                        "Error desconocido en autenticación";
                                callback.onSignInFailure(error);
                            }
                        }
                    }
                });
    }

    public void signOut() {
        if (googleSignInClient != null) {
            googleSignInClient.signOut();
        }
        firebaseAuth.signOut();
    }

    public static int getSignInRequestCode() {
        return RC_SIGN_IN;
    }
}