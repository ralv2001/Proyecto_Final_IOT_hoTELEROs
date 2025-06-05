package com.example.proyecto_final_hoteleros.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EmailService {

    private static final String TAG = "EmailService";

    // Configuración de EmailJS (servicio gratuito)
    // Configuración de EmailJS (servicio gratuito)
    // Configuración de EmailJS (servicio gratuito)
    // Configuración de EmailJS (servicio gratuito)
    private static final String EMAILJS_SERVICE_ID = "service_hoteleros";
    private static final String EMAILJS_TEMPLATE_ID = "template_xc5attq";
    private static final String EMAILJS_PUBLIC_KEY = "kzeiVl2-nghdVvxao";
    private static final String EMAILJS_ENDPOINT = "https://api.emailjs.com/api/v1.0/email/send-form";

    private final OkHttpClient httpClient;
    private final ExecutorService executor;

    public EmailService() {
        this.httpClient = new OkHttpClient();
        this.executor = Executors.newFixedThreadPool(2);
    }

    // Interfaces para callbacks
    public interface EmailCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface CodeGenerationCallback {
        void onCodeGenerated(String code);
        void onEmailSent();
        void onError(String error);
    }

    // Generar código de 5 dígitos
    public String generateVerificationCode() {
        Random random = new Random();
        int code = 10000 + random.nextInt(90000); // Genera número entre 10000-99999
        return String.valueOf(code);
    }

    // Enviar email con código usando template HTML hermoso
    public void sendPasswordResetCode(String toEmail, String code, CodeGenerationCallback callback) {
        Log.d(TAG, "=== ENVIANDO CÓDIGO DE RECUPERACIÓN ===");
        Log.d(TAG, "Email: " + toEmail);
        Log.d(TAG, "Código: " + code);

        executor.execute(() -> {
            try {

                // Enviar email real
                sendEmailViaEmailJS(toEmail, code, new EmailCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Email enviado exitosamente");
                        callback.onCodeGenerated(code);
                        callback.onEmailSent();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "❌ Error enviando email: " + error);
                        callback.onError(error);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "❌ Error procesando email: " + e.getMessage());
                callback.onError("Error procesando email: " + e.getMessage());
            }
        });
    }

    // Enviar email usando EmailJS con tu correo springlezzzteam@gmail.com
    private void sendEmailViaEmailJS(String toEmail, String code, EmailCallback callback) {
        try {
            // Formato actualizado de EmailJS
            JSONObject emailData = new JSONObject();
            emailData.put("service_id", EMAILJS_SERVICE_ID);
            emailData.put("template_id", EMAILJS_TEMPLATE_ID);
            emailData.put("user_id", EMAILJS_PUBLIC_KEY);  // ← CAMBIO: user_id en lugar de public_key

            JSONObject templateParams = new JSONObject();
            templateParams.put("to_email", toEmail);
            templateParams.put("from_name", "hoTELITOs");
            templateParams.put("from_email", "springlezzzteam@gmail.com");
            templateParams.put("subject", "Recuperación de Contraseña - Código: " + code);
            templateParams.put("verification_code", code);
            templateParams.put("message_text", "Recupera tu contraseña con el siguiente código de confirmación.");
            templateParams.put("disclaimer_text", "Has recibido este correo electrónico porque has solicitado recuperar tu contraseña. Puedes ignorar este mensaje si no lo has solicitado.");

            emailData.put("template_params", templateParams);

            Log.d(TAG, "=== NUEVO FORMATO EMAILJS ===");
            Log.d(TAG, "JSON actualizado: " + emailData.toString());

            RequestBody body = RequestBody.create(
                    emailData.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(EMAILJS_ENDPOINT)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error enviando email: " + e.getMessage());
                    callback.onError("Error de conexión: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";

                    Log.d(TAG, "=== RESPUESTA EMAILJS ===");
                    Log.d(TAG, "Status Code: " + response.code());
                    Log.d(TAG, "Response: " + responseBody);

                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ Email enviado exitosamente via EmailJS");
                        callback.onSuccess();
                    } else {
                        Log.e(TAG, "❌ Error del servidor EmailJS: " + response.code());
                        callback.onError("Error del servidor: " + response.code());
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error creando payload: " + e.getMessage());
            callback.onError("Error preparando email: " + e.getMessage());
        }
    }

    // Validar formato de código
    public boolean isValidCode(String code) {
        return code != null && code.matches("\\d{5}");
    }

    // Limpiar recursos
    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}