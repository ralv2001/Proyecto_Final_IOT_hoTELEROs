package com.example.proyecto_final_hoteleros.client.data.repository;

import android.util.Log;
import com.example.proyecto_final_hoteleros.client.data.model.NearbyPlace;
import com.example.proyecto_final_hoteleros.client.data.service.PlacesApiService;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NearbyPlacesRepository {
    private static final String TAG = "NearbyPlacesRepository";
    private static final String PLACES_BASE_URL = "https://maps.googleapis.com/maps/api/place/";
    private static final String API_KEY = "AIzaSyBdghOu6DZktjZcg0_PJzffH72NC-nR0ok"; // ✅ TU API KEY REAL

    private PlacesApiService apiService;

    public NearbyPlacesRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(PLACES_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(PlacesApiService.class);
    }

    public interface NearbyPlacesCallback {
        void onSuccess(List<NearbyPlace> places);
        void onError(String error);
    }

    public void getNearbyTouristAttractions(double latitude, double longitude,
                                            int radiusMeters, NearbyPlacesCallback callback) {
        Log.d("NearbyPlacesRepository", "=== INICIANDO BÚSQUEDA DE LUGARES TURÍSTICOS ===");
        Log.d("NearbyPlacesRepository", "Ubicación: " + latitude + ", " + longitude);
        Log.d("NearbyPlacesRepository", "Radio: " + radiusMeters + " metros");

        String location = latitude + "," + longitude;

        // Tipos de lugares turísticos
        String[] types = {
                "tourist_attraction",
                "museum",
                "park",
                "art_gallery",
                "zoo",
                "aquarium",
                "church",
                "synagogue",
                "hindu_temple",
                "stadium",
                "casino"
        };

        List<NearbyPlace> allPlaces = new ArrayList<>();
        final int[] completedRequests = {0};

        for (String type : types) {
            Log.d("NearbyPlacesRepository", "🔍 Buscando tipo: " + type);

            // ✅ AGREGAR IDIOMA ESPAÑOL
            Call<PlacesApiService.PlacesResponse> call = apiService.getNearbyPlaces(
                    location, radiusMeters, type, "es", API_KEY  // ✅ "es" = español
            );

            call.enqueue(new Callback<PlacesApiService.PlacesResponse>() {
                @Override
                public void onResponse(Call<PlacesApiService.PlacesResponse> call,
                                       Response<PlacesApiService.PlacesResponse> response) {
                    completedRequests[0]++;

                    Log.d("NearbyPlacesRepository", "📡 Respuesta para " + type + ": " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        PlacesApiService.PlacesResponse placesResponse = response.body();

                        if ("OK".equals(placesResponse.status)) {
                            List<NearbyPlace> places = convertToNearbyPlaces(placesResponse.results);
                            List<NearbyPlace> validPlaces = filterValidTouristPlaces(places);
                            allPlaces.addAll(validPlaces);
                            Log.d("NearbyPlacesRepository", "✅ Agregados " + validPlaces.size() + " lugares válidos de tipo " + type);
                        } else {
                            Log.w("NearbyPlacesRepository", "❌ Places API status para " + type + ": " + placesResponse.status);
                        }
                    }

                    if (completedRequests[0] == types.length) {
                        Log.d("NearbyPlacesRepository", "🏁 Todas las búsquedas completadas. Total lugares: " + allPlaces.size());
                        List<NearbyPlace> finalPlaces = removeDuplicatesAndSortByRelevance(allPlaces);
                        Log.d("NearbyPlacesRepository", "✅ Lugares finales: " + finalPlaces.size());
                        callback.onSuccess(finalPlaces);
                    }
                }

                @Override
                public void onFailure(Call<PlacesApiService.PlacesResponse> call, Throwable t) {
                    completedRequests[0]++;
                    Log.e("NearbyPlacesRepository", "❌ Error en llamada API para " + type + ": " + t.getMessage());

                    if (completedRequests[0] == types.length) {
                        if (allPlaces.isEmpty()) {
                            callback.onError("No se pudieron cargar lugares cercanos: " + t.getMessage());
                        } else {
                            List<NearbyPlace> finalPlaces = removeDuplicatesAndSortByRelevance(allPlaces);
                            callback.onSuccess(finalPlaces);
                        }
                    }
                }
            });
        }
    }

    private List<NearbyPlace> removeDuplicatesAndSortByRelevance(List<NearbyPlace> places) {
        List<NearbyPlace> uniquePlaces = new ArrayList<>();
        List<String> addedIds = new ArrayList<>();

        for (NearbyPlace place : places) {
            if (!addedIds.contains(place.getPlaceId())) {
                uniquePlaces.add(place);
                addedIds.add(place.getPlaceId());
            }
        }

        // Ordenar por relevancia turística y rating
        uniquePlaces.sort((a, b) -> {
            int relevanceA = getTouristRelevance(a.getName());
            int relevanceB = getTouristRelevance(b.getName());

            if (relevanceA != relevanceB) {
                return Integer.compare(relevanceB, relevanceA); // Mayor relevancia primero
            }

            return Double.compare(b.getRating(), a.getRating()); // Mayor rating primero
        });

        // Limitar a máximo 12 lugares
        if (uniquePlaces.size() > 12) {
            uniquePlaces = uniquePlaces.subList(0, 12);
        }

        return uniquePlaces;
    }
    private int getTouristRelevance(String name) {
        String lowerName = name.toLowerCase();

        // Lugares muy importantes (puntuación alta)
        if (lowerName.contains("parque") || lowerName.contains("park") ||
                lowerName.contains("museo") || lowerName.contains("museum") ||
                lowerName.contains("plaza") || lowerName.contains("malecón") ||
                lowerName.contains("centro histórico") || lowerName.contains("catedral")) {
            return 10;
        }

        // Lugares importantes (puntuación media)
        if (lowerName.contains("iglesia") || lowerName.contains("church") ||
                lowerName.contains("galería") || lowerName.contains("gallery") ||
                lowerName.contains("mercado") || lowerName.contains("market") ||
                lowerName.contains("mirador") || lowerName.contains("jardín")) {
            return 7;
        }

        // Lugares menores (puntuación baja)
        if (lowerName.contains("centro") || lowerName.contains("cultural") ||
                lowerName.contains("monumento") || lowerName.contains("monument")) {
            return 5;
        }

        return 1; // Puntuación base
    }
    private List<NearbyPlace> filterValidTouristPlaces(List<NearbyPlace> places) {
        List<NearbyPlace> validPlaces = new ArrayList<>();

        for (NearbyPlace place : places) {
            String name = place.getName().toLowerCase();

            // ✅ EXCLUIR lugares que NO son turísticos
            if (isValidTouristPlace(name)) {
                validPlaces.add(place);
            } else {
                Log.d("NearbyPlacesRepository", "❌ Excluido: " + place.getName() + " (no es lugar turístico)");
            }
        }

        return validPlaces;
    }

    private boolean isValidTouristPlace(String name) {
        // Palabras que indican que SÍ es turístico (en español e inglés)
        String[] touristKeywords = {
                "parque", "park", "museo", "museum", "galería", "gallery",
                "plaza", "iglesia", "church", "catedral", "cathedral",
                "malecón", "mirador", "centro", "historic", "histórico",
                "cultural", "monument", "monumento", "teatro", "theater",
                "zoo", "zoológico", "acuario", "aquarium", "jardín", "garden",
                "mercado", "market", "casino", "estadio", "stadium",
                "basílica", "templo", "santuario", "cementerio", "muelle",
                "fortaleza", "castillo", "palacio", "casa", "biblioteca"
        };

        // Palabras que NO son turísticas
        String[] excludeKeywords = {
                "distrito", "district", "avenida", "avenue", "calle", "street",
                "oficina", "office", "banco", "bank", "farmacia", "pharmacy",
                "hospital", "clínica", "clinic", "dentista", "dentist",
                "peluquería", "salon", "taller", "workshop", "garage",
                "supermercado", "supermarket", "tienda", "store", "shop",
                "restaurante", "restaurant", "café", "hotel", "hostal",
                "empresa", "company", "corporación", "corporativo"
        };

        // Si contiene palabras excluidas, no es turístico
        for (String exclude : excludeKeywords) {
            if (name.contains(exclude)) {
                return false;
            }
        }

        // Si contiene palabras turísticas, SÍ es turístico
        for (String keyword : touristKeywords) {
            if (name.contains(keyword)) {
                return true;
            }
        }

        return name.length() > 5 && !name.matches(".*\\d{3,}.*");
    }


    private List<NearbyPlace> convertToNearbyPlaces(List<PlacesApiService.PlaceResult> results) {
        List<NearbyPlace> places = new ArrayList<>();

        for (PlacesApiService.PlaceResult result : results) {
            try {
                String photoReference = null;
                if (result.photos != null && !result.photos.isEmpty()) {
                    photoReference = result.photos.get(0).photoReference;
                }

                String type = "tourist_attraction";
                if (result.types != null && !result.types.isEmpty()) {
                    type = result.types.get(0);
                }

                NearbyPlace place = new NearbyPlace(
                        result.placeId,
                        result.name,
                        result.vicinity,
                        result.rating,
                        photoReference,
                        result.geometry.location.lat,
                        result.geometry.location.lng,
                        type
                );

                if (result.openingHours != null) {
                    place.setOpen(result.openingHours.openNow);
                }

                place.setPriceLevel(result.priceLevel);
                places.add(place);

            } catch (Exception e) {
                Log.e(TAG, "Error convertiendo lugar: " + e.getMessage());
            }
        }

        return places;
    }

    private List<NearbyPlace> removeDuplicatesAndSort(List<NearbyPlace> places) {
        // Remover duplicados por place_id
        List<NearbyPlace> uniquePlaces = new ArrayList<>();
        List<String> addedIds = new ArrayList<>();

        for (NearbyPlace place : places) {
            if (!addedIds.contains(place.getPlaceId())) {
                uniquePlaces.add(place);
                addedIds.add(place.getPlaceId());
            }
        }

        // Ordenar por rating (los mejores primero)
        uniquePlaces.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));

        // Limitar a máximo 10 lugares
        if (uniquePlaces.size() > 10) {
            uniquePlaces = uniquePlaces.subList(0, 10);
        }

        return uniquePlaces;
    }

    public String getImageUrl(String photoReference) {
        if (photoReference != null && !photoReference.isEmpty()) {
            return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="
                    + photoReference + "&key=" + API_KEY;
        }
        return null;
    }
}