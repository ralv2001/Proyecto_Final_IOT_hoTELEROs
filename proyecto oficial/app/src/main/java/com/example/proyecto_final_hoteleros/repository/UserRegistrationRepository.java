package com.example.proyecto_final_hoteleros.repository;

import android.content.Context;
import android.util.Log;

import com.example.proyecto_final_hoteleros.database.AppDatabase;
import com.example.proyecto_final_hoteleros.database.dao.UserRegistrationDao;
import com.example.proyecto_final_hoteleros.database.entities.UserRegistrationEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRegistrationRepository {

    private static final String TAG = "UserRegistrationRepo";

    private final UserRegistrationDao userRegistrationDao;
    private final ExecutorService executor;

    public UserRegistrationRepository(Context context) {
        this.userRegistrationDao = AppDatabase.getInstance(context).userRegistrationDao();
        this.executor = Executors.newFixedThreadPool(2);
    }

    // Interfaces para callbacks
    public interface RegistrationCallback {
        void onSuccess(UserRegistrationEntity registration);
        void onError(String error);
    }

    public interface RegistrationListCallback {
        void onSuccess(List<UserRegistrationEntity> registrations);
        void onError(String error);
    }

    public interface RegistrationIdCallback {
        void onSuccess(int registrationId);
        void onError(String error);
    }

    // Crear o actualizar registro de usuario
    public void saveUserRegistration(UserRegistrationEntity userRegistration,
                                     RegistrationIdCallback callback) {
        executor.execute(() -> {
            try {
                userRegistration.updateTimestamp();
                long id = userRegistrationDao.insertUserRegistration(userRegistration);

                Log.d(TAG, "User registration saved with ID: " + id);
                callback.onSuccess((int) id);
            } catch (Exception e) {
                Log.e(TAG, "Error saving user registration", e);
                callback.onError("Error al guardar el registro: " + e.getMessage());
            }
        });
    }

    // Actualizar registro existente
    public void updateUserRegistration(UserRegistrationEntity userRegistration,
                                       RegistrationCallback callback) {
        executor.execute(() -> {
            try {
                userRegistration.updateTimestamp();
                userRegistrationDao.updateUserRegistration(userRegistration);

                Log.d(TAG, "User registration updated: " + userRegistration.id);
                callback.onSuccess(userRegistration);
            } catch (Exception e) {
                Log.e(TAG, "Error updating user registration", e);
                callback.onError("Error al actualizar el registro: " + e.getMessage());
            }
        });
    }

    // Obtener registro por ID
    public void getUserRegistrationById(int id, RegistrationCallback callback) {
        executor.execute(() -> {
            try {
                UserRegistrationEntity registration = userRegistrationDao.getUserRegistrationById(id);
                if (registration != null) {
                    callback.onSuccess(registration);
                } else {
                    callback.onError("Registro no encontrado");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting user registration by ID", e);
                callback.onError("Error al obtener el registro: " + e.getMessage());
            }
        });
    }

    // Obtener registro por email
    public void getUserRegistrationByEmail(String email, RegistrationCallback callback) {
        executor.execute(() -> {
            try {
                UserRegistrationEntity registration = userRegistrationDao.getUserRegistrationByEmail(email);
                if (registration != null) {
                    callback.onSuccess(registration);
                } else {
                    callback.onError("Usuario no encontrado");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting user registration by email", e);
                callback.onError("Error al obtener el usuario: " + e.getMessage());
            }
        });
    }

    // Obtener el último registro (el más reciente)
    public void getLatestUserRegistration(RegistrationCallback callback) {
        executor.execute(() -> {
            try {
                UserRegistrationEntity registration = userRegistrationDao.getLatestUserRegistration();
                if (registration != null) {
                    callback.onSuccess(registration);
                } else {
                    callback.onError("No hay registros disponibles");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting latest user registration", e);
                callback.onError("Error al obtener el último registro: " + e.getMessage());
            }
        });
    }

    // Marcar registro como completado
    public void markRegistrationAsCompleted(int registrationId, RegistrationCallback callback) {
        executor.execute(() -> {
            try {
                userRegistrationDao.updateCompletionStatus(registrationId, true, System.currentTimeMillis());

                UserRegistrationEntity registration = userRegistrationDao.getUserRegistrationById(registrationId);

                Log.d(TAG, "Registration marked as completed: " + registrationId);
                callback.onSuccess(registration);
            } catch (Exception e) {
                Log.e(TAG, "Error marking registration as completed", e);
                callback.onError("Error al completar el registro: " + e.getMessage());
            }
        });
    }

    // Obtener todos los registros incompletos
    public void getIncompleteRegistrations(RegistrationListCallback callback) {
        executor.execute(() -> {
            try {
                List<UserRegistrationEntity> registrations =
                        userRegistrationDao.getUserRegistrationsByStatus(false);
                callback.onSuccess(registrations);
            } catch (Exception e) {
                Log.e(TAG, "Error getting incomplete registrations", e);
                callback.onError("Error al obtener registros incompletos: " + e.getMessage());
            }
        });
    }

    // Limpiar registros incompletos antiguos (más de 24 horas)
    public void cleanupOldIncompleteRegistrations() {
        executor.execute(() -> {
            try {
                long twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
                userRegistrationDao.deleteIncompleteRegistrationsOlderThan(twentyFourHoursAgo);

                Log.d(TAG, "Cleaned up old incomplete registrations");
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning up old registrations", e);
            }
        });
    }

    // Eliminar registro por ID
    public void deleteUserRegistration(int registrationId, RegistrationCallback callback) {
        executor.execute(() -> {
            try {
                UserRegistrationEntity registration = userRegistrationDao.getUserRegistrationById(registrationId);
                if (registration != null) {
                    userRegistrationDao.deleteUserRegistrationById(registrationId);
                    Log.d(TAG, "User registration deleted: " + registrationId);
                    callback.onSuccess(registration);
                } else {
                    callback.onError("Registro no encontrado");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting user registration", e);
                callback.onError("Error al eliminar el registro: " + e.getMessage());
            }
        });
    }

    // Crear registro desde ViewModel
    public UserRegistrationEntity createFromViewModel(String userType, String nombres, String apellidos,
                                                      String email, String fechaNacimiento, String telefono,
                                                      String tipoDocumento, String numeroDocumento,
                                                      String direccion, String placaVehiculo, String password) {

        return new UserRegistrationEntity(userType, nombres, apellidos, email, fechaNacimiento,
                telefono, tipoDocumento, numeroDocumento, direccion,
                placaVehiculo, password);
    }

    // Verificar si existe un registro con un email específico
    public void checkEmailExists(String email, RegistrationCallback callback) {
        executor.execute(() -> {
            try {
                UserRegistrationEntity registration = userRegistrationDao.getUserRegistrationByEmail(email);
                if (registration != null) {
                    callback.onSuccess(registration);
                } else {
                    callback.onError("Email no encontrado");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking email existence", e);
                callback.onError("Error al verificar el email: " + e.getMessage());
            }
        });
    }
}