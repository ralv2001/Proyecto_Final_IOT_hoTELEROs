package com.example.proyecto_final_hoteleros.taxista.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.proyecto_final_hoteleros.taxista.model.CompletedTrip;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.proyecto_final_hoteleros.taxista.model.SolicitudViaje;
import com.example.proyecto_final_hoteleros.taxista.model.DriverProfile;
import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;

public class DriverPreferenceManager {
    private static final String PREF_NAME = "driver_preferences";
    private static final String KEY_DRIVER_PROFILE = "driver_profile";
    private static final String KEY_DRIVER_STATUS = "driver_status";
    private static final String KEY_IS_AVAILABLE = "is_available";
    private static final String KEY_TRIP_REQUESTS = "trip_requests";
    private static final String KEY_NOTIFICATION_COUNT = "notification_count";
    private static final String KEY_DRIVER_LOCATION_LAT = "driver_location_lat";
    private static final String KEY_DRIVER_LOCATION_LNG = "driver_location_lng";
    private static final String KEY_TOTAL_EARNINGS = "total_earnings";
    private static final String KEY_MONTHLY_EARNINGS = "monthly_earnings";
    private static final String KEY_COMPLETED_TRIPS = "completed_trips";
    private static final String KEY_DAILY_HOURS = "daily_hours";
    private static final String KEY_CAR_MODEL = "car_model";
    private static final String KEY_CAR_IMAGE_URL = "car_image_url";
    private static final String KEY_LICENSE_PLATE = "license_plate";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public DriverPreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // === PERFIL DEL CONDUCTOR ===
    public void saveDriverProfile(DriverProfile profile) {
        String profileJson = gson.toJson(profile);
        sharedPreferences.edit()
                .putString(KEY_DRIVER_PROFILE, profileJson)
                .apply();
    }

    public DriverProfile getDriverProfile() {
        String profileJson = sharedPreferences.getString(KEY_DRIVER_PROFILE, null);
        if (profileJson != null) {
            return gson.fromJson(profileJson, DriverProfile.class);
        }
        return getDefaultDriverProfile();
    }

    private DriverProfile getDefaultDriverProfile() {
        return new DriverProfile(
                "driver001",
                "Renato Delgado Aquino",
                "renato.delgado@email.com",
                "+51 987 654 321",
                "https://png.pngtree.com/png-clipart/20241214/original/pngtree-cat-in-a-suit-and-shirt-png-image_17854633.png",
                "Av. Lima 123, San Miguel, Lima",
                "L12345678",
                true,
                true,
                4.8f,
                127,
                124,
                2450.50
        );
    }

    // === HISTORIAL DE VIAJES ===
    private static final String KEY_COMPLETED_TRIPS_HISTORY = "completed_trips_history";

    public void saveCompletedTripsHistory(List<CompletedTrip> trips) {
        String tripsJson = gson.toJson(trips);
        sharedPreferences.edit()
                .putString(KEY_COMPLETED_TRIPS_HISTORY, tripsJson)
                .apply();
    }

    public List<CompletedTrip> getCompletedTripsHistory() {
        String tripsJson = sharedPreferences.getString(KEY_COMPLETED_TRIPS_HISTORY, null);
        if (tripsJson != null) {
            Type listType = new TypeToken<List<CompletedTrip>>(){}.getType();
            return gson.fromJson(tripsJson, listType);
        }
        return new ArrayList<>();
    }
    // === ESTADO DEL CONDUCTOR ===
    public void setDriverAvailable(boolean isAvailable) {
        sharedPreferences.edit()
                .putBoolean(KEY_IS_AVAILABLE, isAvailable)
                .apply();
    }

    public boolean isDriverAvailable() {
        return sharedPreferences.getBoolean(KEY_IS_AVAILABLE, false);
    }

    public void setDriverStatus(String status) {
        sharedPreferences.edit()
                .putString(KEY_DRIVER_STATUS, status)
                .apply();
    }

    public String getDriverStatus() {
        return sharedPreferences.getString(KEY_DRIVER_STATUS, "Fuera de servicio");
    }

    // === UBICACIÓN DEL CONDUCTOR ===
    public void saveDriverLocation(double latitude, double longitude) {
        sharedPreferences.edit()
                .putString(KEY_DRIVER_LOCATION_LAT, String.valueOf(latitude))
                .putString(KEY_DRIVER_LOCATION_LNG, String.valueOf(longitude))
                .apply();
    }

    public double[] getDriverLocation() {
        String lat = sharedPreferences.getString(KEY_DRIVER_LOCATION_LAT, "0.0");
        String lng = sharedPreferences.getString(KEY_DRIVER_LOCATION_LNG, "0.0");
        return new double[]{Double.parseDouble(lat), Double.parseDouble(lng)};
    }

    // === SOLICITUDES DE VIAJE ===
    public void saveTripRequests(List<SolicitudViaje> requests) {
        String requestsJson = gson.toJson(requests);
        sharedPreferences.edit()
                .putString(KEY_TRIP_REQUESTS, requestsJson)
                .apply();
    }

    public List<SolicitudViaje> getTripRequests() {
        String requestsJson = sharedPreferences.getString(KEY_TRIP_REQUESTS, null);
        if (requestsJson != null) {
            Type listType = new TypeToken<List<SolicitudViaje>>(){}.getType();
            return gson.fromJson(requestsJson, listType);
        }
        return new ArrayList<>();
    }

    // === NOTIFICACIONES ===
    public void setNotificationCount(int count) {
        sharedPreferences.edit()
                .putInt(KEY_NOTIFICATION_COUNT, count)
                .apply();
    }

    public int getNotificationCount() {
        return sharedPreferences.getInt(KEY_NOTIFICATION_COUNT, 0);
    }

    // === ESTADÍSTICAS ===
    public void updateEarnings(double todayEarnings, double monthlyEarnings) {
        sharedPreferences.edit()
                .putString(KEY_TOTAL_EARNINGS, String.valueOf(todayEarnings))
                .putString(KEY_MONTHLY_EARNINGS, String.valueOf(monthlyEarnings))
                .apply();
    }

    public double getTotalEarnings() {
        String earnings = sharedPreferences.getString(KEY_TOTAL_EARNINGS, "0.0");
        return Double.parseDouble(earnings);
    }

    public double getMonthlyEarnings() {
        String earnings = sharedPreferences.getString(KEY_MONTHLY_EARNINGS, "0.0");
        return Double.parseDouble(earnings);
    }

    public void updateCompletedTrips(int trips) {
        sharedPreferences.edit()
                .putInt(KEY_COMPLETED_TRIPS, trips)
                .apply();
    }

    public int getCompletedTrips() {
        return sharedPreferences.getInt(KEY_COMPLETED_TRIPS, 0);
    }

    public void updateDailyHours(String hours) {
        sharedPreferences.edit()
                .putString(KEY_DAILY_HOURS, hours)
                .apply();
    }

    public String getDailyHours() {
        return sharedPreferences.getString(KEY_DAILY_HOURS, "0h 0m");
    }

    // === INFORMACIÓN ADICIONAL DEL VEHÍCULO ===
    public void saveCarInfo(String model, String imageUrl, String licensePlate) {
        sharedPreferences.edit()
                .putString(KEY_CAR_MODEL, model)
                .putString(KEY_CAR_IMAGE_URL, imageUrl)
                .putString(KEY_LICENSE_PLATE, licensePlate)
                .apply();
    }

    public String getCarModel() {
        return sharedPreferences.getString(KEY_CAR_MODEL, "Toyota Rush 2023");
    }

    public String getCarImageUrl() {
        return sharedPreferences.getString(KEY_CAR_IMAGE_URL, "");
    }

    public String getLicensePlate() {
        return sharedPreferences.getString(KEY_LICENSE_PLATE, "ABC-123");
    }

    // 🔥 LIMPIAR TODOS LOS DATOS DEL CONDUCTOR (CORREGIDO)
    public void clearAllData() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            android.util.Log.d("DriverPreferenceManager", "✅ Todos los datos del conductor limpiados");
        } catch (Exception e) {
            android.util.Log.e("DriverPreferenceManager", "❌ Error limpiando datos: " + e.getMessage());
        }
    }

    public void logout() {
        // Mantener algunos datos básicos pero limpiar sesión
        sharedPreferences.edit()
                .remove(KEY_IS_AVAILABLE)
                .remove(KEY_DRIVER_STATUS)
                .remove(KEY_TRIP_REQUESTS)
                .putBoolean(KEY_IS_AVAILABLE, false)
                .putString(KEY_DRIVER_STATUS, "Fuera de servicio")
                .apply();
    }
}