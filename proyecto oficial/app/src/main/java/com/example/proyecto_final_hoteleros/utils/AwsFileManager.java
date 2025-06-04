package com.example.proyecto_final_hoteleros.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AwsFileManager {

    private static final String TAG = "AwsFileManager";

    // ✅ TU URL REAL DE API GATEWAY
    private static final String API_ENDPOINT = "https://etiqhcgbxe.execute-api.us-east-1.amazonaws.com/prod/upload-file";

    private final Context context;
    private final OkHttpClient httpClient;
    private final ExecutorService executor;

    public AwsFileManager(Context context) {
        this.context = context.getApplicationContext();
        this.httpClient = new OkHttpClient();
        this.executor = Executors.newFixedThreadPool(3);
    }

    // Interfaces para callbacks
    public interface UploadCallback {
        void onSuccess(AwsFileInfo fileInfo);
        void onError(String error);
        void onProgress(int percentage);
    }

    // Clase para información del archivo
    public static class AwsFileInfo {
        public String originalName;
        public String storedName;
        public String s3Key;
        public String fileUrl;
        public String fileType;
        public long fileSizeBytes;
        public double fileSizeMB;
        public String userId;
        public String folder;
        public String uploadTimestamp;
        public String etag;

        public static AwsFileInfo fromJson(JSONObject json) throws JSONException {
            AwsFileInfo info = new AwsFileInfo();
            JSONObject fileInfo = json.getJSONObject("file_info");

            info.originalName = fileInfo.getString("original_name");
            info.storedName = fileInfo.getString("stored_name");
            info.s3Key = fileInfo.getString("s3_key");
            info.fileUrl = fileInfo.getString("file_url");
            info.fileType = fileInfo.getString("file_type");
            info.fileSizeBytes = fileInfo.getLong("file_size_bytes");
            info.fileSizeMB = fileInfo.getDouble("file_size_mb");
            info.userId = fileInfo.getString("user_id");
            info.folder = fileInfo.getString("folder");
            info.uploadTimestamp = fileInfo.getString("upload_timestamp");
            info.etag = json.optString("etag", "");

            return info;
        }
    }

    // Subir imagen desde Bitmap
    public void uploadImage(Bitmap bitmap, String fileName, String userId, String folder, UploadCallback callback) {
        executor.execute(() -> {
            try {
                // Comprimir imagen a JPEG
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
                byte[] imageData = baos.toByteArray();

                // Convertir a Base64
                String base64Data = Base64.encodeToString(imageData, Base64.NO_WRAP);

                // Subir archivo
                uploadFileInternal(base64Data, fileName, "image/jpeg", userId, folder, callback);

            } catch (Exception e) {
                Log.e(TAG, "Error preparando imagen: " + e.getMessage());
                callback.onError("Error preparando imagen: " + e.getMessage());
            }
        });
    }

    // Subir archivo desde URI
    public void uploadFile(Uri fileUri, String userId, String folder, UploadCallback callback) {
        executor.execute(() -> {
            try {
                // Leer archivo
                InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
                if (inputStream == null) {
                    callback.onError("No se pudo leer el archivo");
                    return;
                }

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }
                inputStream.close();

                byte[] fileData = buffer.toByteArray();

                // Obtener nombre y tipo de archivo
                String fileName = getFileName(fileUri);
                String mimeType = getMimeType(fileUri);

                if (mimeType == null) {
                    mimeType = "application/octet-stream";
                }

                // Convertir a Base64
                String base64Data = Base64.encodeToString(fileData, Base64.NO_WRAP);

                // Subir archivo
                uploadFileInternal(base64Data, fileName, mimeType, userId, folder, callback);

            } catch (IOException e) {
                Log.e(TAG, "Error leyendo archivo: " + e.getMessage());
                callback.onError("Error leyendo archivo: " + e.getMessage());
            }
        });
    }

    // Subir archivo desde path local
    public void uploadFileFromPath(String filePath, String originalName, String mimeType,
                                   String userId, String folder, UploadCallback callback) {
        executor.execute(() -> {
            try {
                // Leer archivo directamente desde path
                java.io.File file = new java.io.File(filePath);
                if (!file.exists()) {
                    callback.onError("Archivo no encontrado: " + filePath);
                    return;
                }

                java.io.FileInputStream fis = new java.io.FileInputStream(file);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }
                fis.close();

                byte[] fileData = buffer.toByteArray();

                // Convertir a Base64
                String base64Data = Base64.encodeToString(fileData, Base64.NO_WRAP);

                // Subir archivo
                uploadFileInternal(base64Data, originalName, mimeType, userId, folder, callback);

            } catch (IOException e) {
                Log.e(TAG, "Error leyendo archivo desde path: " + e.getMessage());
                callback.onError("Error leyendo archivo desde path: " + e.getMessage());
            }
        });
    }

    private void uploadFileInternal(String base64Data, String fileName, String mimeType,
                                    String userId, String folder, UploadCallback callback) {
        try {
            // Crear JSON payload
            JSONObject payload = new JSONObject();
            payload.put("file_data", base64Data);
            payload.put("file_name", fileName);
            payload.put("file_type", mimeType);
            payload.put("user_id", userId);
            payload.put("folder", folder);

            // Crear request
            RequestBody body = RequestBody.create(
                    payload.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(API_ENDPOINT)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // Reportar progreso (50% al enviar)
            callback.onProgress(50);

            // Ejecutar request
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error en request: " + e.getMessage());
                    callback.onError("Error de conexión: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();

                    // ========== LOGS DE DEBUGGING ==========
                    Log.d(TAG, "=== RESPUESTA DEL SERVIDOR ===");
                    Log.d(TAG, "Status Code: " + response.code());
                    Log.d(TAG, "Response Body: " + responseBody);
                    Log.d(TAG, "Is Successful: " + response.isSuccessful());

                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);

                            // ========== MANEJAR RESPUESTA ANIDADA DE API GATEWAY ==========
                            JSONObject actualResponse;
                            if (jsonResponse.has("body")) {
                                // Respuesta de API Gateway con Lambda Proxy
                                String bodyString = jsonResponse.getString("body");
                                actualResponse = new JSONObject(bodyString);
                            } else {
                                // Respuesta directa
                                actualResponse = jsonResponse;
                            }

                            AwsFileInfo fileInfo = AwsFileInfo.fromJson(actualResponse);

                            Log.d(TAG, "Archivo subido exitosamente: " + fileInfo.s3Key);
                            callback.onProgress(100);
                            callback.onSuccess(fileInfo);

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parseando respuesta: " + e.getMessage());
                            callback.onError("Error procesando respuesta del servidor");
                        }
                    } else {
                        Log.e(TAG, "Error del servidor: " + response.code() + " - " + responseBody);

                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            String errorMessage = errorJson.optString("error", "Error desconocido del servidor");
                            callback.onError(errorMessage);
                        } catch (JSONException e) {
                            callback.onError("Error del servidor: " + response.code());
                        }
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error creando payload: " + e.getMessage());
            callback.onError("Error preparando datos: " + e.getMessage());
        }
    }

    private String getFileName(Uri uri) {
        String fileName = "file_" + System.currentTimeMillis();

        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = context.getContentResolver()
                    .query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error obteniendo nombre del archivo: " + e.getMessage());
            }
        } else {
            fileName = uri.getLastPathSegment();
        }

        return fileName != null ? fileName : "file_" + System.currentTimeMillis();
    }

    private String getMimeType(Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);

        if (mimeType == null) {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (fileExtension != null) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
            }
        }

        return mimeType;
    }
}