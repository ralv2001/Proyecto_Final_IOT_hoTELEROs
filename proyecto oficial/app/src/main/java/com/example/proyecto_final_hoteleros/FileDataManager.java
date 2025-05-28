package com.example.proyecto_final_hoteleros;

import android.net.Uri;
import android.util.Log;

import java.io.File;

public class FileDataManager {
    private static final String TAG = "FileDataManager";
    private static FileDataManager instance;

    private Uri pdfUri;
    private String pdfPath;
    private Uri photoUri;
    private String photoPath;

    private FileDataManager() {}

    public static synchronized FileDataManager getInstance() {
        if (instance == null) {
            instance = new FileDataManager();
        }
        return instance;
    }

    // Métodos para PDF
    public void setPdfData(Uri uri, String path) {
        this.pdfUri = uri;
        this.pdfPath = path;
        Log.d(TAG, "PDF data guardado - URI: " + uri + ", Path: " + path);
    }

    public Uri getPdfUri() {
        return pdfUri;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public boolean hasPdf() {
        boolean hasData = pdfUri != null && pdfPath != null;

        Log.d(TAG, "hasPdf() - pdfUri != null: " + (pdfUri != null));
        Log.d(TAG, "hasPdf() - pdfPath != null: " + (pdfPath != null));
        Log.d(TAG, "hasPdf() - resultado final: " + hasData);

        // No verificar si el archivo existe físicamente, solo si tenemos los datos
        return hasData;
    }

    // Métodos para Photo
    public void setPhotoData(Uri uri, String path) {
        this.photoUri = uri;
        this.photoPath = path;
        Log.d(TAG, "Photo data guardado - URI: " + uri + ", Path: " + path);
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public boolean hasPhoto() {
        return photoUri != null && photoPath != null && new File(photoPath).exists();
    }

    // Método para limpiar todos los datos
    public void clearAll() {
        pdfUri = null;
        pdfPath = null;
        photoUri = null;
        photoPath = null;
        Log.d(TAG, "Todos los datos han sido limpiados");
    }

    // Método para limpiar solo PDF
    public void clearPdf() {
        pdfUri = null;
        pdfPath = null;
        Log.d(TAG, "Datos de PDF limpiados");
    }

    // Método para limpiar solo Photo
    public void clearPhoto() {
        photoUri = null;
        photoPath = null;
        Log.d(TAG, "Datos de Photo limpiados");
    }
}