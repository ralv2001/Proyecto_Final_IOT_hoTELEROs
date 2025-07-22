package com.example.proyecto_final_hoteleros.adminhotel.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.File;

public class ImageCompressor {
    private static final String TAG = "ImageCompressor";
    private static final int MAX_WIDTH = 1024;
    private static final int MAX_HEIGHT = 1024;
    private static final int QUALITY = 80; // 80% quality
    private static final long MAX_SIZE_BYTES = 1024 * 1024; // 1MB max

    /**
     * Comprime una imagen desde Uri y devuelve un nuevo Uri del archivo comprimido
     */
    public static Uri compressImage(Context context, Uri originalUri) {
        try {
            Log.d(TAG, "🔄 Comprimiendo imagen: " + originalUri.toString());

            // Leer la imagen original
            InputStream inputStream = context.getContentResolver().openInputStream(originalUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (originalBitmap == null) {
                Log.e(TAG, "❌ No se pudo decodificar la imagen");
                return originalUri;
            }

            // Obtener dimensiones originales
            int originalWidth = originalBitmap.getWidth();
            int originalHeight = originalBitmap.getHeight();

            Log.d(TAG, "📏 Dimensiones originales: " + originalWidth + "x" + originalHeight);

            // Calcular nuevas dimensiones manteniendo aspecto
            int newWidth = originalWidth;
            int newHeight = originalHeight;

            if (originalWidth > MAX_WIDTH || originalHeight > MAX_HEIGHT) {
                float ratio = Math.min(
                        (float) MAX_WIDTH / originalWidth,
                        (float) MAX_HEIGHT / originalHeight
                );

                newWidth = Math.round(originalWidth * ratio);
                newHeight = Math.round(originalHeight * ratio);

                Log.d(TAG, "📏 Nuevas dimensiones: " + newWidth + "x" + newHeight);
            }

            // Redimensionar si es necesario
            Bitmap resizedBitmap;
            if (newWidth != originalWidth || newHeight != originalHeight) {
                resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
                originalBitmap.recycle(); // Liberar memoria
            } else {
                resizedBitmap = originalBitmap;
            }

            // Comprimir en JPEG
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, outputStream);
            byte[] compressedData = outputStream.toByteArray();
            outputStream.close();
            resizedBitmap.recycle();

            long compressedSize = compressedData.length;
            Log.d(TAG, "📊 Tamaño comprimido: " + (compressedSize / 1024) + " KB");

            // Si aún es muy grande, comprimir más
            int currentQuality = QUALITY;
            while (compressedSize > MAX_SIZE_BYTES && currentQuality > 20) {
                currentQuality -= 10;
                Log.d(TAG, "🔄 Recomprimiendo con calidad: " + currentQuality + "%");

                outputStream = new ByteArrayOutputStream();
                resizedBitmap = BitmapFactory.decodeByteArray(compressedData, 0, compressedData.length);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, outputStream);
                compressedData = outputStream.toByteArray();
                compressedSize = compressedData.length;
                outputStream.close();
                resizedBitmap.recycle();
            }

            // Guardar archivo comprimido
            File compressedFile = new File(context.getCacheDir(), "compressed_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fileOutput = new FileOutputStream(compressedFile);
            fileOutput.write(compressedData);
            fileOutput.close();

            Uri compressedUri = Uri.fromFile(compressedFile);
            Log.d(TAG, "✅ Imagen comprimida guardada: " + compressedUri.toString());
            Log.d(TAG, "📊 Tamaño final: " + (compressedSize / 1024) + " KB");

            return compressedUri;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error comprimiendo imagen: " + e.getMessage());
            e.printStackTrace();
            return originalUri; // Devolver original si falla
        }
    }

    /**
     * Comprime múltiples imágenes
     */
    public static java.util.List<Uri> compressImages(Context context, java.util.List<Uri> originalUris) {
        java.util.List<Uri> compressedUris = new java.util.ArrayList<>();

        for (Uri originalUri : originalUris) {
            Uri compressedUri = compressImage(context, originalUri);
            compressedUris.add(compressedUri);
        }

        return compressedUris;
    }

    /**
     * Limpia archivos temporales de compresión
     */
    public static void cleanupTempFiles(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            File[] files = cacheDir.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.getName().startsWith("compressed_") && file.getName().endsWith(".jpg")) {
                        boolean deleted = file.delete();
                        Log.d(TAG, "🗑️ Archivo temporal eliminado: " + file.getName() + " (" + deleted + ")");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error limpiando archivos temporales: " + e.getMessage());
        }
    }
}