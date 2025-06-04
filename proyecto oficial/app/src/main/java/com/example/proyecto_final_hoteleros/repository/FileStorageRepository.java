package com.example.proyecto_final_hoteleros.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.proyecto_final_hoteleros.database.AppDatabase;
import com.example.proyecto_final_hoteleros.database.dao.FileStorageDao;
import com.example.proyecto_final_hoteleros.database.entities.FileStorageEntity;
import com.example.proyecto_final_hoteleros.utils.AwsFileManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileStorageRepository {

    private static final String TAG = "FileStorageRepository";
    private static final String UPLOADS_DIRECTORY = "user_uploads";

    private final FileStorageDao fileStorageDao;
    private final Context context;
    private final ExecutorService executor;
    private final File uploadsDir;

    public FileStorageRepository(Context context) {
        this.context = context.getApplicationContext();
        this.fileStorageDao = AppDatabase.getInstance(context).fileStorageDao();
        this.executor = Executors.newFixedThreadPool(2);

        // Crear directorio permanente para archivos
        this.uploadsDir = new File(context.getFilesDir(), UPLOADS_DIRECTORY);
        if (!uploadsDir.exists()) {
            boolean created = uploadsDir.mkdirs();
            Log.d(TAG, "Uploads directory created: " + created);
        }
    }

    // Interfaz para callbacks
    public interface FileOperationCallback {
        void onSuccess(FileStorageEntity fileEntity);
        void onError(String error);
    }

    public interface FileListCallback {
        void onSuccess(List<FileStorageEntity> files);
        void onError(String error);
    }

    // Guardar archivo de forma asíncrona
    public void saveFile(int registrationId, String fileType, String originalName,
                         Uri tempUri, String mimeType, FileOperationCallback callback) {
        executor.execute(() -> {
            try {
                // Crear nombre único para el archivo
                String fileName = generateUniqueFileName(originalName, fileType);
                File permanentFile = new File(uploadsDir, fileName);

                // Copiar archivo temporal a ubicación permanente
                copyFile(tempUri, permanentFile);

                // Crear entidad de base de datos
                FileStorageEntity fileEntity = new FileStorageEntity(
                        registrationId,
                        fileType,
                        originalName,
                        permanentFile.getAbsolutePath(),
                        permanentFile.length(),
                        mimeType
                );

                // Guardar en base de datos
                long id = fileStorageDao.insertFile(fileEntity);
                fileEntity.id = (int) id;

                Log.d(TAG, "File saved successfully: " + fileName);
                callback.onSuccess(fileEntity);

            } catch (Exception e) {
                Log.e(TAG, "Error saving file", e);
                callback.onError("Error al guardar el archivo: " + e.getMessage());
            }
        });
    }

    // Obtener archivo por ID de registro y tipo
    public void getFileByRegistrationIdAndType(int registrationId, String fileType,
                                               FileOperationCallback callback) {
        executor.execute(() -> {
            try {
                FileStorageEntity fileEntity = fileStorageDao
                        .getFileByRegistrationIdAndType(registrationId, fileType);

                if (fileEntity != null) {
                    // Verificar que el archivo físico existe
                    File file = new File(fileEntity.storedPath);
                    if (file.exists()) {
                        callback.onSuccess(fileEntity);
                    } else {
                        Log.w(TAG, "File not found on disk: " + fileEntity.storedPath);
                        // Eliminar referencia de la base de datos
                        fileStorageDao.deleteFile(fileEntity);
                        callback.onError("El archivo no existe en el sistema");
                    }
                } else {
                    callback.onError("Archivo no encontrado");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file", e);
                callback.onError("Error al obtener el archivo: " + e.getMessage());
            }
        });
    }

    // Obtener todos los archivos de un registro
    public void getFilesByRegistrationId(int registrationId, FileListCallback callback) {
        executor.execute(() -> {
            try {
                List<FileStorageEntity> files = fileStorageDao.getFilesByRegistrationId(registrationId);
                callback.onSuccess(files);
            } catch (Exception e) {
                Log.e(TAG, "Error getting files", e);
                callback.onError("Error al obtener los archivos: " + e.getMessage());
            }
        });
    }

    // Eliminar archivo
    public void deleteFile(FileStorageEntity fileEntity, FileOperationCallback callback) {
        executor.execute(() -> {
            try {
                // Eliminar archivo físico
                File file = new File(fileEntity.storedPath);
                if (file.exists()) {
                    boolean deleted = file.delete();
                    Log.d(TAG, "Physical file deleted: " + deleted);
                }

                // Eliminar de la base de datos
                fileStorageDao.deleteFile(fileEntity);

                callback.onSuccess(fileEntity);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting file", e);
                callback.onError("Error al eliminar el archivo: " + e.getMessage());
            }
        });
    }

    // Limpiar archivos de un registro
    public void clearFilesByRegistrationId(int registrationId) {
        executor.execute(() -> {
            try {
                List<FileStorageEntity> files = fileStorageDao.getFilesByRegistrationId(registrationId);

                // Eliminar archivos físicos
                for (FileStorageEntity file : files) {
                    File physicalFile = new File(file.storedPath);
                    if (physicalFile.exists()) {
                        physicalFile.delete();
                    }
                }

                // Eliminar registros de la base de datos
                fileStorageDao.deleteFilesByRegistrationId(registrationId);

                Log.d(TAG, "Cleared " + files.size() + " files for registration " + registrationId);
            } catch (Exception e) {
                Log.e(TAG, "Error clearing files", e);
            }
        });
    }

    // Métodos helper privados
    private String generateUniqueFileName(String originalName, String fileType) {
        long timestamp = System.currentTimeMillis();
        String extension = getFileExtension(originalName);
        return fileType.toLowerCase() + "_" + timestamp + "." + extension;
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return "tmp";
    }

    private void copyFile(Uri sourceUri, File destFile) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(sourceUri);
             FileOutputStream outputStream = new FileOutputStream(destFile)) {

            if (inputStream == null) {
                throw new IOException("No se pudo abrir el archivo fuente");
            }

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
        }
    }

    // Obtener URI del archivo guardado
    public Uri getFileUri(FileStorageEntity fileEntity) {
        File file = new File(fileEntity.storedPath);
        if (file.exists()) {
            return Uri.fromFile(file);
        }
        return null;
    }

    // Limpiar archivos antiguos (para mantenimiento)
    public void cleanupOldFiles(long olderThanTimestamp) {
        executor.execute(() -> {
            try {
                // Obtener archivos de registros antiguos
                // Esta lógica se puede expandir según necesidades
                Log.d(TAG, "Cleanup old files executed");
            } catch (Exception e) {
                Log.e(TAG, "Error during cleanup", e);
            }
        });
    }

    // ========== INTEGRACIÓN CON AWS ==========
    private AwsFileManager awsFileManager;

    // Inicializar AWS Manager
    private void initializeAwsManager() {
        if (awsFileManager == null) {
            awsFileManager = new AwsFileManager(context);
        }
    }

    // Método para subir archivo a AWS y guardarlo en Room
    public void saveFileWithAws(int registrationId, String fileType, String originalName,
                                Uri tempUri, String mimeType, FileOperationCallback callback) {

        initializeAwsManager();

        executor.execute(() -> {
            try {
                // Determinar folder según tipo de archivo
                String awsFolder = fileType.equals(FileStorageEntity.FILE_TYPE_PDF) ? "documents" : "photos";
                String userId = "reg_" + registrationId; // Usar registration ID como user ID temporal

                Log.d(TAG, "Subiendo archivo a AWS: " + originalName);

                awsFileManager.uploadFile(tempUri, userId, awsFolder, new AwsFileManager.UploadCallback() {
                    @Override
                    public void onSuccess(AwsFileManager.AwsFileInfo fileInfo) {
                        Log.d(TAG, "Archivo subido a AWS exitosamente: " + fileInfo.s3Key);

                        // Ahora guardar en Room Database con la URL de AWS
                        FileStorageEntity fileEntity = new FileStorageEntity(
                                registrationId,
                                fileType,
                                originalName,
                                fileInfo.fileUrl, // Usar URL de AWS en lugar de path local
                                fileInfo.fileSizeBytes,
                                mimeType
                        );

                        // Añadir metadatos de AWS
                        fileEntity.awsS3Key = fileInfo.s3Key;
                        fileEntity.awsStoredName = fileInfo.storedName;
                        fileEntity.awsETag = fileInfo.etag;

                        long id = fileStorageDao.insertFile(fileEntity);
                        fileEntity.id = (int) id;

                        Log.d(TAG, "Archivo guardado en Room con referencia AWS");
                        callback.onSuccess(fileEntity);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error subiendo a AWS: " + error);

                        // Fallback: guardar localmente como antes
                        Log.d(TAG, "Fallback: guardando archivo localmente");
                        saveFile(registrationId, fileType, originalName, tempUri, mimeType, callback);
                    }

                    @Override
                    public void onProgress(int percentage) {
                        Log.d(TAG, "Progreso upload AWS: " + percentage + "%");
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error iniciando upload AWS: " + e.getMessage());
                // Fallback: guardar localmente
                saveFile(registrationId, fileType, originalName, tempUri, mimeType, callback);
            }
        });
    }

    // Actualizar archivo existente
    public void updateFile(FileStorageEntity fileEntity, FileOperationCallback callback) {
        executor.execute(() -> {
            try {
                fileStorageDao.updateFile(fileEntity);
                Log.d(TAG, "File updated successfully: " + fileEntity.id);
                callback.onSuccess(fileEntity);
            } catch (Exception e) {
                Log.e(TAG, "Error updating file", e);
                callback.onError("Error al actualizar el archivo: " + e.getMessage());
            }
        });
    }

}