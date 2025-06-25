package com.example.proyecto_final_hoteleros.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_final_hoteleros.database.entities.UserRegistrationEntity;
import com.example.proyecto_final_hoteleros.repository.UserRegistrationRepository;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyTestHelper {
    private static final String TAG = "ConcurrencyTest";

    public static void testConcurrentRegistrations(Context context) {
        Log.d(TAG, "=== INICIANDO TEST DE CONCURRENCIA ===");

        if (!(context instanceof AppCompatActivity)) {
            Log.e(TAG, "Context debe ser AppCompatActivity para mostrar Toast");
            return;
        }

        AppCompatActivity activity = (AppCompatActivity) context;
        UserRegistrationRepository repository = new UserRegistrationRepository(context);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);

        String timestamp = String.valueOf(System.currentTimeMillis());
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Simular 10 usuarios registrándose simultáneamente
        for (int i = 0; i < 10; i++) {
            final int userId = i;
            final String baseEmail = "concurrent.test." + timestamp;

            executor.submit(() -> {
                try {
                    // Crear registro único para cada usuario
                    UserRegistrationEntity registration = new UserRegistrationEntity(
                            "client",
                            "Usuario" + userId,
                            "ConcurrentTest",
                            baseEmail + "." + userId + "@test.com", // Email único
                            "01/01/1990",
                            "999888" + String.format("%03d", userId),
                            "DNI",
                            String.format("1234567%02d", userId), // Documento único
                            "Dirección Test " + userId,
                            null,
                            "password123"
                    );

                    Log.d(TAG, "🚀 Registrando usuario " + userId + " - Thread: " + Thread.currentThread().getId());

                    repository.saveUserRegistrationSafe(registration,
                            new UserRegistrationRepository.RegistrationIdCallback() {
                                @Override
                                public void onSuccess(int registrationId) {
                                    int count = successCount.incrementAndGet();
                                    Log.d(TAG, "✅ Usuario " + userId + " registrado exitosamente con ID: " + registrationId + " (Total exitosos: " + count + ")");
                                    latch.countDown();
                                }

                                @Override
                                public void onError(String error) {
                                    int count = errorCount.incrementAndGet();
                                    Log.e(TAG, "❌ Error registrando usuario " + userId + ": " + error + " (Total errores: " + count + ")");
                                    latch.countDown();
                                }
                            });

                } catch (Exception e) {
                    Log.e(TAG, "❌ Excepción en usuario " + userId, e);
                    errorCount.incrementAndGet();
                    latch.countDown();
                }
            });
        }

        // Esperar resultados en un thread separado
        new Thread(() -> {
            try {
                latch.await();

                int finalSuccess = successCount.get();
                int finalError = errorCount.get();

                Log.d(TAG, "=== RESULTADOS DEL TEST DE CONCURRENCIA ===");
                Log.d(TAG, "✅ Registros exitosos: " + finalSuccess);
                Log.d(TAG, "❌ Registros con error: " + finalError);
                Log.d(TAG, "📊 Total procesado: " + (finalSuccess + finalError));

                activity.runOnUiThread(() -> {
                    String message = String.format("Test Concurrencia:\n✅ Exitosos: %d\n❌ Errores: %d",
                            finalSuccess, finalError);
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                });

            } catch (InterruptedException e) {
                Log.e(TAG, "Test interrumpido", e);
            }

            executor.shutdown();
        }).start();
    }

    // Test para simular el mismo email (esto debería fallar correctamente)
    public static void testDuplicateEmailConcurrency(Context context) {
        Log.d(TAG, "=== TEST DE EMAIL DUPLICADO ===");

        if (!(context instanceof AppCompatActivity)) return;

        AppCompatActivity activity = (AppCompatActivity) context;
        UserRegistrationRepository repository = new UserRegistrationRepository(context);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);

        String timestamp = String.valueOf(System.currentTimeMillis());
        String sameEmail = "duplicate.test." + timestamp + "@test.com";
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicInteger duplicateResolvedCount = new AtomicInteger(0);

        // Intentar registrar 5 usuarios con el MISMO email
        for (int i = 0; i < 5; i++) {
            final int userId = i;

            executor.submit(() -> {
                try {
                    UserRegistrationEntity registration = new UserRegistrationEntity(
                            "client",
                            "Usuario" + userId,
                            "DuplicateTest",
                            sameEmail, // MISMO EMAIL PARA TODOS
                            "01/01/1990",
                            "999888" + String.format("%03d", userId),
                            "DNI",
                            String.format("9876543%02d", userId), // DOCUMENTOS DIFERENTES
                            "Dirección Test " + userId,
                            null,
                            "password123"
                    );

                    Log.d(TAG, "🔄 Intentando registro duplicado " + userId + " con email: " + sameEmail);

                    repository.saveUserRegistrationSafe(registration,
                            new UserRegistrationRepository.RegistrationIdCallback() {
                                @Override
                                public void onSuccess(int registrationId) {
                                    int count = successCount.incrementAndGet();

                                    // Verificar si este es un registro nuevo o uno reutilizado
                                    if (count == 1) {
                                        Log.d(TAG, "✅ Primer registro exitoso " + userId + " con ID: " + registrationId + " (NUEVO)");
                                    } else {
                                        duplicateResolvedCount.incrementAndGet();
                                        Log.d(TAG, "🔄 Registro duplicado " + userId + " resuelto con ID existente: " + registrationId + " (REUTILIZADO)");
                                    }
                                    latch.countDown();
                                }

                                @Override
                                public void onError(String error) {
                                    int count = errorCount.incrementAndGet();
                                    Log.d(TAG, "🛡️ Registro duplicado " + userId + " correctamente rechazado: " + error + " (Total rechazados: " + count + ")");
                                    latch.countDown();
                                }
                            });

                } catch (Exception e) {
                    Log.e(TAG, "❌ Excepción en registro duplicado " + userId, e);
                    errorCount.incrementAndGet();
                    latch.countDown();
                }
            });
        }

        // Esperar resultados
        new Thread(() -> {
            try {
                latch.await();

                int finalSuccess = successCount.get();
                int finalError = errorCount.get();
                int finalDuplicateResolved = duplicateResolvedCount.get();

                Log.d(TAG, "=== RESULTADOS TEST EMAIL DUPLICADO ===");
                Log.d(TAG, "✅ Registros únicos creados: " + (finalSuccess - finalDuplicateResolved) + " (debería ser 1)");
                Log.d(TAG, "🔄 Duplicados resueltos: " + finalDuplicateResolved + " (debería ser 4)");
                Log.d(TAG, "🛡️ Registros rechazados: " + finalError + " (debería ser 0)");

                boolean testPassed = (finalSuccess - finalDuplicateResolved) == 1 &&
                        finalDuplicateResolved == 4 &&
                        finalError == 0;

                activity.runOnUiThread(() -> {
                    String message = String.format("Test Email Duplicado:\n✅ Nuevos: %d\n🔄 Reutilizados: %d\n🛡️ Rechazados: %d\n%s",
                            (finalSuccess - finalDuplicateResolved), finalDuplicateResolved, finalError,
                            testPassed ? "✅ TEST EXITOSO" : "❌ TEST FALLÓ");
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                });

            } catch (InterruptedException e) {
                Log.e(TAG, "Test interrumpido", e);
            }

            executor.shutdown();
        }).start();
    }
}