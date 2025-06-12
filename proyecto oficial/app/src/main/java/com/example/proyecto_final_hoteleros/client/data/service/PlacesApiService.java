package com.example.proyecto_final_hoteleros.client.data.service;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlacesApiService {

    @GET("nearbysearch/json")
    Call<PlacesResponse> getNearbyPlaces(
            @Query("location") String location,
            @Query("radius") int radius,
            @Query("type") String type,
            @Query("language") String language,  // ✅ NUEVO parámetro
            @Query("key") String apiKey
    );

    // Clase para la respuesta de la API
    class PlacesResponse {
        @SerializedName("results")
        public List<PlaceResult> results;

        @SerializedName("status")
        public String status;
    }

    // Clase para cada lugar en los resultados
    class PlaceResult {
        @SerializedName("place_id")
        public String placeId;

        @SerializedName("name")
        public String name;

        @SerializedName("vicinity")
        public String vicinity;

        @SerializedName("rating")
        public double rating;

        @SerializedName("geometry")
        public Geometry geometry;

        @SerializedName("photos")
        public List<Photo> photos;

        @SerializedName("types")
        public List<String> types;

        @SerializedName("opening_hours")
        public OpeningHours openingHours;

        @SerializedName("price_level")
        public int priceLevel;
    }

    class Geometry {
        @SerializedName("location")
        public Location location;
    }

    class Location {
        @SerializedName("lat")
        public double lat;

        @SerializedName("lng")
        public double lng;
    }

    class Photo {
        @SerializedName("photo_reference")
        public String photoReference;

        @SerializedName("width")
        public int width;

        @SerializedName("height")
        public int height;
    }

    class OpeningHours {
        @SerializedName("open_now")
        public boolean openNow;
    }
}