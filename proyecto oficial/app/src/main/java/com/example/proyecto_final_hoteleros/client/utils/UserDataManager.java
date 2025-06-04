package com.example.proyecto_final_hoteleros.client.utils;

import android.os.Bundle;

public class UserDataManager {
    private static UserDataManager instance;
    private String userId, userName, userFullName, userEmail, userType;

    public static UserDataManager getInstance() {
        if (instance == null) {
            instance = new UserDataManager();
        }
        return instance;
    }

    public void setUserData(String userId, String userName, String userFullName,
                            String userEmail, String userType) {
        this.userId = userId;
        this.userName = userName;
        this.userFullName = userFullName;
        this.userEmail = userEmail;
        this.userType = userType;
    }

    public Bundle getUserBundle() {
        Bundle args = new Bundle();
        args.putString("user_id", userId);
        args.putString("user_name", userName);
        args.putString("user_full_name", userFullName);
        args.putString("user_email", userEmail);
        args.putString("user_type", userType);
        return args;
    }

    // Getters individuales
    public String getUserId() { return userId; }
    public String getUserName() { return userName != null ? userName : "Huésped"; }
    public String getUserFullName() { return userFullName != null ? userFullName : getUserName(); }
    public String getUserEmail() { return userEmail; }
    public String getUserType() { return userType; }
}