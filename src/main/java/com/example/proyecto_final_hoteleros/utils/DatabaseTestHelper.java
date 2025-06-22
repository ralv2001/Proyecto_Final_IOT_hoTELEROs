package com.example.proyecto_final_hoteleros.utils;

import android.content.Context;
import android.util.Log;

import com.example.proyecto_final_hoteleros.database.entities.FileStorageEntity;
import com.example.proyecto_final_hoteleros.database.entities.UserRegistrationEntity;
import com.example.proyecto_final_hoteleros.repository.FileStorageRepository;
import com.example.proyecto_final_hoteleros.repository.UserRegistrationRepository;

import java.util.List;

public class DatabaseTestHelper {

    private static final String TAG = "DatabaseTestHelper";

    private final UserRegistrationRepository userRepo;
    private final FileStorageRepository fileRepo;

    public DatabaseTestHelper(Context context) {
        this.userRepo = new UserRegistrationRepository(context);
        this.fileRepo = new FileStorageRepository(context);
    }

    // Método para probar la funcionalidad de la base de datos
    public void runDatabaseTests() {
        Log.d(TAG, "=== INICIANDO TESTS DE BASE DE DATOS ===");

        // Test 1: Listar todos los registros
        listAllRegistrations();

        // Test 2: Listar todos los archivos
        listAllFiles();

        // Test 3: Limpiar registros antiguos
        cleanupOldData();
    }

    public void listAllRegistrations() {
        Log.d(TAG, "--- LISTANDO TODOS LOS REGISTROS ---");
        userRepo.getIncompleteRegistrations(new UserRegistrationRepository.RegistrationListCallback() {
            @Override
            public void onSuccess(List<UserRegistrationEntity> registrations) {
                Log.d(TAG, "Registros incompletos encontrados: " + registrations.size());

                for (UserRegistrationEntity reg : registrations) {
                    Log.d(TAG, "ID: " + reg.id +
                            " | Email: " + reg.email +
                            " | Tipo: " + reg.userType +
                            " | Completado: " + reg.isCompleted);
                }

                if (registrations.isEmpty()) {
                    Log.d(TAG, "✅ No hay registros incompletos");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error listando registros: " + error);
            }
        });
    }

    public void listAllFiles() {
        Log.d(TAG, "--- LISTANDO TODOS LOS ARCHIVOS ---");
        // Nota: Este método requeriría una modificación en FileStorageRepository
        // para obtener todos los archivos, por simplicidad solo logueamos
        Log.d(TAG, "Para ver archivos específicos, use getFilesByRegistrationId()");
    }

    public void cleanupOldData() {
        Log.d(TAG, "--- LIMPIANDO DATOS ANTIGUOS ---");
        userRepo.cleanupOldIncompleteRegistrations();
        Log.d(TAG, "✅ Limpieza de registros antiguos completada");
    }

    // Método para verificar un registro específico
    public void checkRegistrationById(int registrationId) {
        Log.d(TAG, "--- VERIFICANDO REGISTRO ID: " + registrationId + " ---");

        userRepo.getUserRegistrationById(registrationId, new UserRegistrationRepository.RegistrationCallback() {
            @Override
            public void onSuccess(UserRegistrationEntity registration) {
                Log.d(TAG, "✅ Registro encontrado:");
                Log.d(TAG, "   Email: " + registration.email);
                Log.d(TAG, "   Nombre: " + registration.nombres + " " + registration.apellidos);
                Log.d(TAG, "   Tipo: " + registration.userType);
                Log.d(TAG, "   Completado: " + registration.isCompleted);
                Log.d(TAG, "   Creado: " + new java.util.Date(registration.createdAt));

                // Verificar archivos asociados
                checkFilesForRegistration(registrationId);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error encontrando registro: " + error);
            }
        });
    }

    public void checkFilesForRegistration(int registrationId) {
        Log.d(TAG, "--- VERIFICANDO ARCHIVOS PARA REGISTRO: " + registrationId + " ---");

        fileRepo.getFilesByRegistrationId(registrationId, new FileStorageRepository.FileListCallback() {
            @Override
            public void onSuccess(List<FileStorageEntity> files) {
                Log.d(TAG, "✅ Archivos encontrados: " + files.size());

                for (FileStorageEntity file : files) {
                    Log.d(TAG, "   Tipo: " + file.fileType +
                            " | Nombre: " + file.originalName +
                            " | Tamaño: " + (file.fileSize / 1024) + " KB" +
                            " | Ruta: " + file.storedPath);

                    // Verificar si el archivo físico existe
                    java.io.File physicalFile = new java.io.File(file.storedPath);
                    Log.d(TAG, "   Archivo físico existe: " + physicalFile.exists());
                }

                if (files.isEmpty()) {
                    Log.d(TAG, "   No hay archivos para este registro");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error obteniendo archivos: " + error);
            }
        });
    }

    // Método para simular notificaciones
    public void testNotifications(Context context) {
        Log.d(TAG, "--- PROBANDO NOTIFICACIONES ---");

        NotificationHelper notificationHelper = new NotificationHelper(context);

        // Test notificación de registro completado para cliente
        notificationHelper.showRegistrationCompleteNotification("client", "Juan Pérez");
        Log.d(TAG, "✅ Notificación de cliente enviada");

        // Test notificación de registro completado para taxista (después de 3 segundos)
        new android.os.Handler().postDelayed(() -> {
            notificationHelper.showRegistrationCompleteNotification("driver", "María García");
            Log.d(TAG, "✅ Notificación de taxista enviada");
        }, 3000);

        // Test notificación de taxista aprobado (después de 6 segundos)
        new android.os.Handler().postDelayed(() -> {
            notificationHelper.showDriverApprovedNotification("Carlos Ruiz");
            Log.d(TAG, "✅ Notificación de aprobación enviada");
        }, 6000);
    }
}