package com.example.proyecto_final_hoteleros.taxista.services;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.proyecto_final_hoteleros.R;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.net.URLEncoder;

public class DirectionsService {
    private static final String TAG = "DirectionsService";
    private static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json";

    private Context context;
    private RequestQueue requestQueue;

    public DirectionsService(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public interface DirectionsCallback {
        void onSuccess(List<LatLng> route, String distance, String duration);
        void onError(String error);
    }

    public void getDirections(LatLng origin, LatLng destination, DirectionsCallback callback) {
        String apiKey = context.getString(R.string.google_maps_key);

        // ✅ VALIDAR API KEY
        if (apiKey == null || apiKey.trim().isEmpty()) {
            callback.onError("API Key de Google Maps no configurada");
            return;
        }

        String url = DIRECTIONS_API_URL +
                "?origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
                "&key=" + apiKey +
                "&mode=driving" +
                "&language=es" +
                "&region=pe";

        Log.d(TAG, "Requesting directions: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d(TAG, "📡 Respuesta directions: " + response.toString());

                        // ✅ VERIFICAR STATUS DE LA RESPUESTA
                        String status = response.getString("status");
                        if (!"OK".equals(status)) {
                            Log.e(TAG, "❌ Status directions: " + status);
                            callback.onError("Error directions: " + status);
                            return;
                        }
                        JSONArray routes = response.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);

                            // Extraer información de distancia y duración
                            JSONArray legs = route.getJSONArray("legs");
                            JSONObject leg = legs.getJSONObject(0);

                            String distance = leg.getJSONObject("distance").getString("text");
                            String duration = leg.getJSONObject("duration").getString("text");

                            // Decodificar la ruta
                            String encodedPolyline = route.getJSONObject("overview_polyline").getString("points");
                            List<LatLng> routePoints = decodePolyline(encodedPolyline);

                            Log.d(TAG, "✅ Ruta obtenida: " + distance + ", " + duration);
                            callback.onSuccess(routePoints, distance, duration);

                        } else {
                            callback.onError("No se encontró ruta");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing directions: " + e.getMessage());
                        callback.onError("Error procesando direcciones: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "❌ Error getting directions: " + error.getMessage());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Código respuesta: " + error.networkResponse.statusCode);
                    }
                    callback.onError("Error obteniendo direcciones: " + error.getMessage());
                }
        );

        requestQueue.add(request);
    }

    public void geocodeAddress(String address, GeocodeCallback callback) {
        String apiKey = context.getString(R.string.google_maps_key); // ✅ Usar la clave correcta

        // ✅ ENCODING CORRECTO DE LA DIRECCIÓN
        String encodedAddress;
        try {
            encodedAddress = java.net.URLEncoder.encode(address, "UTF-8");
        } catch (Exception e) {
            callback.onError("Error encoding dirección: " + e.getMessage());
            return;
        }

        String url = "https://maps.googleapis.com/maps/api/geocode/json" +
                "?address=" + encodedAddress +
                "&key=" + apiKey +
                "&language=es" +
                "&region=pe"; // ✅ Agregar región Perú para mejores resultados

        Log.d(TAG, "🔍 Geocoding URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d(TAG, "📡 Respuesta geocoding: " + response.toString());

                        String status = response.getString("status");
                        if (!"OK".equals(status)) {
                            Log.e(TAG, "❌ Status geocoding: " + status);
                            callback.onError("Error geocoding: " + status);
                            return;
                        }

                        JSONArray results = response.getJSONArray("results");
                        if (results.length() > 0) {
                            JSONObject location = results.getJSONObject(0)
                                    .getJSONObject("geometry")
                                    .getJSONObject("location");

                            double lat = location.getDouble("lat");
                            double lng = location.getDouble("lng");

                            Log.d(TAG, "✅ Coordenadas encontradas: " + lat + ", " + lng);
                            callback.onSuccess(new LatLng(lat, lng));
                        } else {
                            Log.e(TAG, "❌ No hay resultados para: " + address);
                            callback.onError("Dirección no encontrada: " + address);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parsing geocoding: " + e.getMessage());
                        callback.onError("Error procesando geocoding: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "❌ Error red geocoding: " + error.getMessage());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Código respuesta: " + error.networkResponse.statusCode);
                    }
                    callback.onError("Error de red geocoding: " + error.getMessage());
                }
        );

        requestQueue.add(request);
    }

    public interface GeocodeCallback {
        void onSuccess(LatLng location);
        void onError(String error);
    }

    // Decodificar polyline de Google
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(p);
        }

        return poly;
    }
}