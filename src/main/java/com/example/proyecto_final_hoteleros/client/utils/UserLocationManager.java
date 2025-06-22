package com.example.proyecto_final_hoteleros.client.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserLocationManager {
    private static final String PREF_NAME = "user_location_preferences";
    private static final String KEY_CURRENT_CITY = "current_city";
    private static final String KEY_CURRENT_DISTRICT = "current_district";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LAST_UPDATE = "last_update";

    private SharedPreferences preferences;
    private static UserLocationManager instance;

    private UserLocationManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static UserLocationManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserLocationManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveCurrentLocation(String city, String district, double latitude, double longitude) {
        preferences.edit()
                .putString(KEY_CURRENT_CITY, city)
                .putString(KEY_CURRENT_DISTRICT, district)
                .putFloat(KEY_LATITUDE, (float) latitude)
                .putFloat(KEY_LONGITUDE, (float) longitude)
                .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
                .apply();
    }

    public String getCurrentCity() {
        return preferences.getString(KEY_CURRENT_CITY, "Lima"); // Default Lima
    }

    public String getCurrentDistrict() {
        return preferences.getString(KEY_CURRENT_DISTRICT, "Centro");
    }

    public double getCurrentLatitude() {
        return preferences.getFloat(KEY_LATITUDE, -12.046374f); // Lima por defecto
    }

    public double getCurrentLongitude() {
        return preferences.getFloat(KEY_LONGITUDE, -77.042793f); // Lima por defecto
    }

    public long getLastUpdateTime() {
        return preferences.getLong(KEY_LAST_UPDATE, 0);
    }

    public boolean hasValidLocation() {
        return System.currentTimeMillis() - getLastUpdateTime() < 24 * 60 * 60 * 1000; // 24 horas
    }

    public String getLocationDisplayName() {
        String district = getCurrentDistrict();
        String city = getCurrentCity();

        if (!district.equals("Centro") && !district.isEmpty()) {
            return district + ", " + city;
        }
        return city;
    }
}