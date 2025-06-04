package com.example.proyecto_final_hoteleros.client.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.proyecto_final_hoteleros.client.model.LocationItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocationPreferences {
    private static final String PREF_NAME = "location_preferences";
    private static final String KEY_RECENT_LOCATIONS = "recent_locations";
    private static final int MAX_RECENT_LOCATIONS = 5;

    private SharedPreferences preferences;
    private Gson gson;

    public LocationPreferences(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveRecentLocation(LocationItem location) {
        List<LocationItem> recentLocations = getRecentLocations();

        // Evitar duplicados - eliminar si ya existe
        for (int i = 0; i < recentLocations.size(); i++) {
            if (recentLocations.get(i).getName().equals(location.getName())) {
                recentLocations.remove(i);
                break;
            }
        }

        // Añadir la nueva ubicación al principio
        recentLocations.add(0, location);

        // Mantener sólo MAX_RECENT_LOCATIONS elementos
        if (recentLocations.size() > MAX_RECENT_LOCATIONS) {
            recentLocations = recentLocations.subList(0, MAX_RECENT_LOCATIONS);
        }

        // Guardar la lista actualizada
        String json = gson.toJson(recentLocations);
        preferences.edit().putString(KEY_RECENT_LOCATIONS, json).apply();
    }

    public List<LocationItem> getRecentLocations() {
        String json = preferences.getString(KEY_RECENT_LOCATIONS, null);
        if (json == null) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<LocationItem>>(){}.getType();
        List<LocationItem> locations = gson.fromJson(json, type);
        return locations != null ? locations : new ArrayList<>();
    }
}