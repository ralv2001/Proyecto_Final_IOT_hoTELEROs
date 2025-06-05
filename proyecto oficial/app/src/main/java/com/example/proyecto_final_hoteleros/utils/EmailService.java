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
    private static final String EMAILJS_SERVICE_ID = "service_hoteleros";
    private static final String EMAILJS_TEMPLATE_ID = "template_password_reset";
    private static final String EMAILJS_PUBLIC_KEY = "jBqXw5YcF-vNkKqKR"; // Clave pública de EmailJS
    private static final String EMAILJS_ENDPOINT = "https://api.emailjs.com/api/v1.0/email/send";

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
                // Generar HTML del email usando el template de tu guía
                String htmlContent = generateEmailHtml(code);

                // Enviar email real
                sendEmailViaEmailJS(toEmail, code, htmlContent, new EmailCallback() {
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

    // Generar HTML hermoso basado en tu template de guía
    private String generateEmailHtml(String code) {
        return "<div width=\"100%\" bgcolor=\"#F1F1F1\" style=\"margin:0\">\n" +
                "    <center style=\"width:100%;background:#f1f1f1;text-align:left\">\n" +
                "\n" +
                "        <div style=\"max-width:600px;margin:auto\" class=\"email-container\">\n" +
                "            <table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" width=\"100%\"\n" +
                "                style=\"max-width:600px\" class=\"email-container\">\n" +
                "\n" +
                "                <tbody>\n" +
                "                    <tr>\n" +
                "                        <td bgcolor=\"#F35D38\" align=\"center\" valign=\"top\"\n" +
                "                            style=\"text-align:center;background-position:center !important;background-size:cover !important\">\n" +
                "\n" +
                "                            <div>\n" +
                "                                <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\"\n" +
                "                                    width=\"100%\" style=\"max-width:600px;margin:auto\">\n" +
                "\n" +
                "                                    <tbody>\n" +
                "                                        <tr>\n" +
                "                                            <td align=\"center\" valign=\"middle\" style=\"padding:40px;\">\n" +
                "                                                <h1 style=\"margin:0;font-family:Inter,Arial,sans-serif;font-size:36px;line-height:40px;color:#ffffff;font-weight:bold;letter-spacing:0px\">\n" +
                "                                                    hoTELITOs\n" +
                "                                                </h1>\n" +
                "                                                <p style=\"margin:10px 0 0 0;font-family:Inter,Arial,sans-serif;font-size:16px;color:#ffffff;opacity:0.9\">\n" +
                "                                                    Recuperación de Contraseña\n" +
                "                                                </p>\n" +
                "                                            </td>\n" +
                "                                        </tr>\n" +
                "                                    </tbody>\n" +
                "                                </table>\n" +
                "                            </div>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td bgcolor=\"#FFFFFF\">\n" +
                "                            <table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\"\n" +
                "                                align=\"center\" bgcolor=\"#FFFFFF\">\n" +
                "                                <tbody>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding:40px 40px 20px 40px;text-align:center\">\n" +
                "                                            <div style=\"width: 80px; height: 80px; margin: 0 auto 20px auto; background: #F35D38; border-radius: 50%; display: flex; align-items: center; justify-content: center;\">\n" +
                "                                                <span style=\"font-size: 40px; color: white;\">🔐</span>\n" +
                "                                            </div>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding:20px 40px 20px 40px;text-align:center\">\n" +
                "\n" +
                "                                            <h1\n" +
                "                                                style=\"margin:0;font-family:Inter,Arial,sans-serif;font-size:32px;line-height:40px;color:#333333;font-weight:bold;letter-spacing:0px\">\n" +
                "                                                Recupera tu contraseña\n" +
                "                                            </h1>\n" +
                "\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                    <tr>\n" +
                "                                        <td\n" +
                "                                            style=\"padding:0px 40px 20px 40px;font-family:Inter,sans-serif;font-size:17px;line-height:24px;color:#555555;text-align:center;font-weight:300\">\n" +
                "\n" +
                "                                            <p style=\"margin:0 0 5px 0\">\n" +
                "                                                Utiliza el siguiente código de verificación para restablecer tu contraseña en la aplicación hoTELITOs.\n" +
                "                                            </p>\n" +
                "                                            <p style=\"margin:5px 0 0 0;color:#999;font-size:14px;\">\n" +
                "                                                Este código expirará en 10 minutos.\n" +
                "                                            </p>\n" +
                "\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding:20px 40px 40px 40px;text-align:center\" align=\"center\">\n" +
                "                                            <table role=\"presentation\" align=\"center\" cellspacing=\"0\" cellpadding=\"0\"\n" +
                "                                                border=\"0\" class=\"center-on-narrow\">\n" +
                "                                                <tbody>\n" +
                "                                                    <tr>\n" +
                "                                                        <td\n" +
                "                                                            style=\"border-radius:12px;background:#ffffff;text-align:center;border:3px solid #F35D38;\">\n" +
                "                                                            <div\n" +
                "                                                                style=\"background:#ffffff;font-family:Inter,sans-serif;font-size:36px;line-height:1.1;text-align:center;text-decoration:none;display:block;border-radius:8px;font-weight:bold;padding:20px 40px\">\n" +
                "                                                                <span style=\"color:#F35D38;letter-spacing:8px\">" + code + "\n" +
                "                                                                </span>\n" +
                "                                                            </div>\n" +
                "                                                        </td>\n" +
                "                                                    </tr>\n" +
                "                                                </tbody>\n" +
                "                                            </table>\n" +
                "\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding:0px 40px 40px 40px;text-align:center\">\n" +
                "                                            <p style=\"margin:0;font-family:Inter,sans-serif;font-size:16px;color:#666;\">\n" +
                "                                                Ingresa este código en la aplicación para continuar con el restablecimiento de tu contraseña.\n" +
                "                                            </p>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "\n" +
                "                                </tbody>\n" +
                "                            </table>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td bgcolor=\"#fff\">\n" +
                "                            <table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\"\n" +
                "                                bgcolor=\"#FFFFFF\" style=\"border-top:1px solid #e2e2e2\">\n" +
                "                                <tbody>\n" +
                "                                    <tr>\n" +
                "                                        <td\n" +
                "                                            style=\"padding:30px 30px;text-align:center;font-family:Inter,sans-serif;font-size:15px;line-height:20px\">\n" +
                "\n" +
                "                                            <table align=\"center\" style=\"text-align:center\">\n" +
                "                                                <tbody>\n" +
                "                                                    <tr>\n" +
                "                                                        <td\n" +
                "                                                            style=\"font-family:Inter,sans-serif;font-size:12px;line-height:20px;color:#555555;text-align:center;font-weight:300\">\n" +
                "                                                            <p class=\"disclaimer\"\n" +
                "                                                                style=\"margin-bottom:5px\">\n" +
                "                                                                Has recibido este correo electrónico porque has\n" +
                "                                                                solicitado restablecer tu contraseña. Si no has sido tú,\n" +
                "                                                                puedes ignorar este mensaje de forma segura.\n" +
                "                                                            </p>\n" +
                "                                                        </td>\n" +
                "                                                    </tr>\n" +
                "                                                </tbody>\n" +
                "                                            </table>\n" +
                "\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "\n" +
                "                                </tbody>\n" +
                "                            </table>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td bgcolor=\"#F35D38\">\n" +
                "                            <table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\">\n" +
                "                                <tbody>\n" +
                "                                    <tr>\n" +
                "                                        <td align=\"center\"\n" +
                "                                            style=\"padding:40px 20px;font-family:Inter,sans-serif;font-size:14px;line-height:20px;color:#ffffff;text-align:center;font-weight:300\">\n" +
                "                                            <p style=\"margin:0 0 10px 0\">\n" +
                "                                               <span style=\"color:#ffffff;font-size:16px;font-weight:bold;text-decoration:none\">" +
                "                                               hoTELITOs - Equipo de Desarrollo" +
                "                                               </span>\n" +
                "                                            </p>\n" +
                "                                            <p style=\"margin:0\">" +
                "                                               <span style=\"color:#ffffff;font-size:12px;font-weight:300;text-decoration:none\">" +
                "                                               Todos los derechos reservados © 2025" +
                "                                               </span>\n" +
                "                                            </p>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "\n" +
                "                                </tbody>\n" +
                "                            </table>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "\n" +
                "                </tbody>\n" +
                "            </table>\n" +
                "        </div>\n" +
                "    </center>\n" +
                "</div>";
    }

    // Enviar email usando EmailJS con tu correo springlezzzteam@gmail.com
    private void sendEmailViaEmailJS(String toEmail, String code, String htmlContent, EmailCallback callback) {
        try {
            JSONObject emailData = new JSONObject();
            emailData.put("service_id", EMAILJS_SERVICE_ID);
            emailData.put("template_id", EMAILJS_TEMPLATE_ID);
            emailData.put("public_key", EMAILJS_PUBLIC_KEY);

            JSONObject templateParams = new JSONObject();
            templateParams.put("to_email", toEmail);
            templateParams.put("from_name", "hoTELITOs");
            templateParams.put("from_email", "springlezzzteam@gmail.com");
            templateParams.put("subject", "Recuperación de Contraseña - Código: " + code);
            templateParams.put("message_html", htmlContent);
            templateParams.put("verification_code", code);

            emailData.put("template_params", templateParams);

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