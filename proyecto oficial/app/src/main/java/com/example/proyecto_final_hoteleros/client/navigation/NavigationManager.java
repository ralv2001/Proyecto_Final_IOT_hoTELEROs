package com.example.proyecto_final_hoteleros.client.navigation;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.fragment.*;

public class NavigationManager {
    private static NavigationManager instance;
    private FragmentActivity activity;

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void init(FragmentActivity activity) {
        this.activity = activity;
    }

    // Navegación principal del bottom navigation
    public void navigateToHome(Bundle userArgs) {
        HomeFragment fragment = new HomeFragment();
        if (userArgs != null) fragment.setArguments(userArgs);
        replaceFragment(fragment, false);
    }

    public void navigateToExplore(Bundle userArgs) {
        HistorialFragment fragment = new HistorialFragment();
        if (userArgs != null) fragment.setArguments(userArgs);
        replaceFragment(fragment, true);
    }

    public void navigateToChat(Bundle userArgs) {
        ChatFragment fragment = new ChatFragment();
        if (userArgs != null) fragment.setArguments(userArgs);
        replaceFragment(fragment, true);
    }

    public void navigateToProfile(Bundle userArgs) {
        ProfileFragment fragment = new ProfileFragment();
        if (userArgs != null) fragment.setArguments(userArgs);
        replaceFragment(fragment, true);
    }

    // Navegación secundaria
    public void navigateToHotelDetail(String hotelName, String hotelLocation,
                                      String hotelPrice, String hotelRating,
                                      String hotelImage, Bundle userArgs) {
        HotelDetailFragment fragment = new HotelDetailFragment();
        Bundle args = userArgs != null ? new Bundle(userArgs) : new Bundle();
        args.putString("hotel_name", hotelName);
        args.putString("hotel_location", hotelLocation);
        args.putString("hotel_price", hotelPrice);
        args.putString("hotel_rating", hotelRating);
        args.putString("hotel_image", hotelImage);
        fragment.setArguments(args);
        replaceFragment(fragment, true);
    }

    public void navigateToNotifications(Bundle userArgs) {
        NotificationFragment fragment = new NotificationFragment();
        if (userArgs != null) fragment.setArguments(userArgs);
        replaceFragment(fragment, true);
    }

    public void navigateToRoomSelection(String hotelName, String hotelPrice, Bundle userArgs) {
        RoomSelectionFragment fragment = new RoomSelectionFragment();
        Bundle args = userArgs != null ? new Bundle(userArgs) : new Bundle();
        args.putString("hotel_name", hotelName);
        args.putString("hotel_price", hotelPrice);
        fragment.setArguments(args);
        replaceFragment(fragment, true);
    }

    public void navigateToBookingSummary(Bundle bookingArgs) {
        BookingSummaryFragment fragment = new BookingSummaryFragment();
        if (bookingArgs != null) fragment.setArguments(bookingArgs);
        replaceFragment(fragment, true);
    }

    public void navigateToChatConversation(String chatId, String hotelName,
                                           String hotelId, String chatStatus, Bundle userArgs) {
        ChatConversationFragment fragment = new ChatConversationFragment();
        Bundle args = userArgs != null ? new Bundle(userArgs) : new Bundle();
        args.putString("chat_id", chatId);
        args.putString("hotel_name", hotelName);
        args.putString("hotel_id", hotelId);
        args.putString("chat_status", chatStatus);
        fragment.setArguments(args);
        replaceFragment(fragment, true);
    }

    // Método base para reemplazar fragmentos
    private void replaceFragment(Fragment fragment, boolean addToBackStack) {
        if (activity == null) return;

        FragmentTransaction transaction = activity.getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    public void goBack() {
        if (activity != null && activity.getSupportFragmentManager().getBackStackEntryCount() > 0) {
            activity.getSupportFragmentManager().popBackStack();
        }
    }

    // Agregar este método en NavigationManager.java
    public void navigateToNotificationSettings(Bundle userArgs) {
        NotificationSettingsFragment fragment = new NotificationSettingsFragment();
        if (userArgs != null) fragment.setArguments(userArgs);
        replaceFragment(fragment, true);
    }
}