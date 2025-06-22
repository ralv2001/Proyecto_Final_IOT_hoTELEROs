package com.example.proyecto_final_hoteleros.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PasswordResetManager {

    private static final String TAG = "PasswordResetManager";
    private static final String PREFS_NAME = "password_reset_prefs";
    private static final String KEY_EMAIL = "reset_email";
    private static final String KEY_CODE = "reset_code";
    private static final String KEY_TIMESTAMP = "reset_timestamp";
    private static final String KEY_ATTEMPTS = "reset_attempts";

    // Configuraciones
    private static final long CODE_EXPIRY_TIME = 10 * 60 * 1000; // 10 minutos
    private static final int MAX_ATTEMPTS = 3;

    private final SharedPreferences prefs;
    private final EmailService emailService;

    public PasswordResetManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.emailService = new EmailService();
    }

    // Interfaces
    public interface ResetCallback {
        void onCodeSent(String maskedEmail);
        void onError(String error);
    }

    public interface ValidationCallback {
        void onValidCode(String email);
        void onInvalidCode();
        void onExpiredCode();
        void onMaxAttemptsReached();
        void onError(String error);
    }

    // Iniciar proceso de reset
    public void startPasswordReset(String email, ResetCallback callback) {
        Log.d(TAG, "=== INICIANDO RESET DE CONTRASEÑA ===");
        Log.d(TAG, "Email: " + email);

        // Generar nuevo código
        String code = emailService.generateVerificationCode();
        long currentTime = System.currentTimeMillis();

        // Guardar datos del reset
        prefs.edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_CODE, code)
                .putLong(KEY_TIMESTAMP, currentTime)
                .putInt(KEY_ATTEMPTS, 0)
                .apply();

        Log.d(TAG, "Código generado: " + code);

        // Enviar email
        emailService.sendPasswordResetCode(email, code, new EmailService.CodeGenerationCallback() {
            @Override
            public void onCodeGenerated(String generatedCode) {
                Log.d(TAG, "Código generado exitosamente: " + generatedCode);
            }

            @Override
            public void onEmailSent() {
                Log.d(TAG, "✅ Email enviado exitosamente");
                callback.onCodeSent(maskEmail(email));
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error enviando email: " + error);
                clearResetData();
                callback.onError(error);
            }
        });
    }

    // Validar código ingresado
    public void validateCode(String inputCode, ValidationCallback callback) {
        Log.d(TAG, "=== VALIDANDO CÓDIGO ===");
        Log.d(TAG, "Código ingresado: " + inputCode);

        String savedEmail = prefs.getString(KEY_EMAIL, "");
        String savedCode = prefs.getString(KEY_CODE, "");
        long savedTimestamp = prefs.getLong(KEY_TIMESTAMP, 0);
        int attempts = prefs.getInt(KEY_ATTEMPTS, 0);

        // Verificar si hay datos de reset
        if (savedEmail.isEmpty() || savedCode.isEmpty()) {
            Log.e(TAG, "No hay proceso de reset activo");
            callback.onError("No hay proceso de reset activo");
            return;
        }

        // Verificar intentos máximos
        if (attempts >= MAX_ATTEMPTS) {
            Log.e(TAG, "Máximo de intentos alcanzado");
            clearResetData();
            callback.onMaxAttemptsReached();
            return;
        }

        // Verificar expiración
        long currentTime = System.currentTimeMillis();
        if (currentTime - savedTimestamp > CODE_EXPIRY_TIME) {
            Log.e(TAG, "Código expirado");
            clearResetData();
            callback.onExpiredCode();
            return;
        }

        // Incrementar intentos
        attempts++;
        prefs.edit().putInt(KEY_ATTEMPTS, attempts).apply();

        // Validar código
        if (savedCode.equals(inputCode)) {
            Log.d(TAG, "✅ Código válido");
            callback.onValidCode(savedEmail);
        } else {
            Log.d(TAG, "❌ Código inválido. Intentos: " + attempts + "/" + MAX_ATTEMPTS);
            callback.onInvalidCode();
        }
    }

    // Limpiar datos de reset
    public void clearResetData() {
        prefs.edit()
                .remove(KEY_EMAIL)
                .remove(KEY_CODE)
                .remove(KEY_TIMESTAMP)
                .remove(KEY_ATTEMPTS)
                .apply();
        Log.d(TAG, "Datos de reset limpiados");
    }

    // Obtener email actual del proceso
    public String getCurrentResetEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    // Verificar si hay proceso activo
    public boolean hasActiveReset() {
        String email = prefs.getString(KEY_EMAIL, "");
        long timestamp = prefs.getLong(KEY_TIMESTAMP, 0);
        long currentTime = System.currentTimeMillis();

        return !email.isEmpty() && (currentTime - timestamp) <= CODE_EXPIRY_TIME;
    }

    // Enmascarar email
    private String maskEmail(String email) {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            return email;
        }

        int atIndex = email.indexOf('@');
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (username.length() > 0) {
            return username.charAt(0) + "*****" + domain;
        } else {
            return "*****" + domain;
        }
    }

    // Limpiar recursos
    public void cleanup() {
        if (emailService != null) {
            emailService.cleanup();
        }
    }
}