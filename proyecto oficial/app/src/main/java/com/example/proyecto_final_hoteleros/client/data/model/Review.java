// client/data/model/Review.java
package com.example.proyecto_final_hoteleros.client.data.model;

public class Review {
    private String id;
    private String reviewerName;
    private String reviewerAvatar;
    private String reviewText;
    private float rating;
    private String date;
    private String reviewerLocation;
    private boolean isVerified;

    public Review(String id, String reviewerName, String reviewerAvatar, String reviewText,
                  float rating, String date, String reviewerLocation, boolean isVerified) {
        this.id = id;
        this.reviewerName = reviewerName;
        this.reviewerAvatar = reviewerAvatar;
        this.reviewText = reviewText;
        this.rating = rating;
        this.date = date;
        this.reviewerLocation = reviewerLocation;
        this.isVerified = isVerified;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getReviewerAvatar() { return reviewerAvatar; }
    public void setReviewerAvatar(String reviewerAvatar) { this.reviewerAvatar = reviewerAvatar; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getReviewerLocation() { return reviewerLocation; }
    public void setReviewerLocation(String reviewerLocation) { this.reviewerLocation = reviewerLocation; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
}