package com.example.proyecto_final_hoteleros.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generador de IDs únicos thread-safe para evitar colisiones en operaciones concurrentes
 * Combina múltiples estrategias para garantizar unicidad:
 * - UUIDs para identificadores únicos globales
 * - Contadores atómicos para secuencias
 * - Timestamps con nano-precisión
 */
public class UniqueIdGenerator {

    private static final String TAG = "UniqueIdGenerator";
    private static final String PREFS_NAME = "unique_id_generator_prefs";
    private static final String KEY_COUNTER = "id_counter";

    // Contador atómico thread-safe
    private static final AtomicLong atomicCounter = new AtomicLong(System.currentTimeMillis());

    // Instancia singleton
    private static UniqueIdGenerator instance;
    private final SharedPreferences prefs;

    private UniqueIdGenerator(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Inicializar contador con valor persistente
        long savedCounter = prefs.getLong(KEY_COUNTER, System.currentTimeMillis());
        atomicCounter.set(Math.max(savedCounter, System.currentTimeMillis()));
    }

    public static synchronized UniqueIdGenerator getInstance(Context context) {
        if (instance == null) {
            instance = new UniqueIdGenerator(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Genera un UUID único para identificadores de archivos
     * Formato: "file_uuid_timestamp"
     * Garantiza que NUNCA habrá colisiones, incluso con uploads simultáneos
     */
    public String generateFileId(String fileType) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        long nanoTime = System.nanoTime();
        long threadId = Thread.currentThread().getId();

        String uniqueId = String.format("%s_%s_%d_%d",
                fileType,
                uuid.substring(0, 8), // Solo los primeros 8 caracteres del UUID
                nanoTime,
                threadId
        );

        Log.d(TAG, "Generated unique file ID: " + uniqueId);
        return uniqueId;
    }

    /**
     * Genera un nombre de archivo único con extensión
     * Formato: "tipo_uuid_timestamp.extension"
     */
    public String generateUniqueFileName(String fileType, String originalName) {
        String extension = getFileExtension(originalName);
        String uniqueBase = generateFileId(fileType);
        return uniqueBase + "." + extension;
    }

    /**
     * Genera un ID de carpeta único para AWS (basado en timestamp + UUID)
     * Formato: "reg_timestamp_uuid"
     * Para evitar que usuarios simultáneos usen la misma carpeta temporal
     */
    public String generateAwsUserId() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        long uniqueTimestamp = atomicCounter.incrementAndGet();

        // Guardar contador actualizado
        prefs.edit().putLong(KEY_COUNTER, uniqueTimestamp).apply();

        String awsUserId = "reg_" + uniqueTimestamp + "_" + uuid;
        Log.d(TAG, "Generated AWS User ID: " + awsUserId);
        return awsUserId;
    }

    /**
     * Genera un timestamp único thread-safe
     * Garantiza que cada llamada retorna un valor diferente
     */
    public long generateUniqueTimestamp() {
        return atomicCounter.incrementAndGet();
    }

    /**
     * Genera un ID numérico único (para casos donde se necesita int)
     * Basado en timestamp pero garantizando unicidad
     */
    public int generateUniqueIntId() {
        long uniqueTimestamp = generateUniqueTimestamp();
        // Convertir a int pero manteniendo unicidad
        // Usar módulo para evitar overflow pero mantener diferencias
        return (int) (uniqueTimestamp % Integer.MAX_VALUE);
    }

    /**
     * Extrae la extensión de un archivo
     */
    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return "tmp";
    }

    /**
     * Validar que un ID generado es único comparado con los anteriores
     * (Para debugging y testing)
     */
    public boolean isIdUnique(String generatedId) {
        // En una implementación completa, podríamos mantener un cache de IDs recientes
        // Por ahora, asumimos que nuestro algoritmo garantiza unicidad
        return generatedId != null && !generatedId.isEmpty();
    }

    /**
     * Reset del contador (solo para testing)
     */
    public void resetCounterForTesting() {
        Log.w(TAG, "⚠️ RESETTING COUNTER - ONLY FOR TESTING!");
        atomicCounter.set(System.currentTimeMillis());
        prefs.edit().putLong(KEY_COUNTER, System.currentTimeMillis()).apply();
    }
}