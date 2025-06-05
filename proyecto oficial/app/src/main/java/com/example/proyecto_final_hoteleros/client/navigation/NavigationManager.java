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

    // Enum para identificar la posición de los tabs en la bottom navigation
    public enum BottomNavTab {
        HOME(0), EXPLORE(1), CHAT(2), PROFILE(3);

        private final int position;

        BottomNavTab(int position) {
            this.position = position;
        }

        public int getPosition() {
            return position;
        }
    }

    private BottomNavTab currentBottomNavTab = BottomNavTab.HOME;

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void init(FragmentActivity activity) {
        this.activity = activity;
    }

    // ============= NAVEGACIÓN BOTTOM NAVIGATION CON ANIMACIONES INTELIGENTES =============

    public void navigateToHome(Bundle userArgs) {
        AnimationDirection direction = getBottomNavAnimationDirection(BottomNavTab.HOME);
        currentBottomNavTab = BottomNavTab.HOME;

        HomeFragment fragment = new HomeFragment();
        if (userArgs != null) fragment.setArguments(userArgs);
        replaceFragment(fragment, false, direction);
    }

    public void navigateToExplore(Bundle userArgs) {
        AnimationDirection direction = getBottomNavAnimationDirection(BottomNavTab.EXPLORE);
        currentBottomNavTab = BottomNavTab.EXPLORE;

        HistorialFragment fragment = new HistorialFragment();
        if (userArgs != null) fragment.setArguments(userArgs);
        replaceFragment(fragment, true, direction);
    }

    public void navigateToChat(Bundle userArgs) {
        AnimationDirection direction = getBottomNavAnimationDirection(BottomNavTab.CHAT);
        currentBottomNavTab = BottomNavTab.CHAT;

        ChatFragment fragment = new ChatFragment();
        if (userArgs != null) fragment.setArguments(userArgs);
        replaceFragment(fragment, true, direction);
    }

    public void navigateToProfile(Bundle userArgs) {
        AnimationDirection direction = getBottomNavAnimationDirection(BottomNavTab.PROFILE);
        currentBottomNavTab = BottomNavTab.PROFILE;

        ProfileFragment fragment = new ProfileFragment();
        if (userArgs != null) fragment.setArguments(userArgs);
        replaceFragment(fragment, true, direction);
    }

    // ============= NAVEGACIÓN SECUNDARIA CON ANIMACIONES ESPECÍFICAS =============

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

        // ANIMACIÓN SCALE_UP para detalles (viene de una tarjeta/foto)
        replaceFragment(fragment, true, AnimationDirection.SCALE_UP);
    }

    public void navigateToNotifications(Bundle userArgs) {
        NotificationFragment fragment = new NotificationFragment();
        if (userArgs != null) fragment.setArguments(userArgs);

        // ANIMACIÓN SLIDE_UP para notificaciones (modal style)
        replaceFragment(fragment, true, AnimationDirection.SLIDE_UP);
    }

    public void navigateToRoomSelection(String hotelName, String hotelPrice, Bundle userArgs) {
        RoomSelectionFragment fragment = new RoomSelectionFragment();
        Bundle args = userArgs != null ? new Bundle(userArgs) : new Bundle();
        args.putString("hotel_name", hotelName);
        args.putString("hotel_price", hotelPrice);
        fragment.setArguments(args);

        // ANIMACIÓN RIGHT_TO_LEFT para selección (proceso hacia adelante)
        replaceFragment(fragment, true, AnimationDirection.RIGHT_TO_LEFT);
    }

    public void navigateToBookingSummary(Bundle bookingArgs) {
        BookingSummaryFragment fragment = new BookingSummaryFragment();
        if (bookingArgs != null) fragment.setArguments(bookingArgs);

        // ANIMACIÓN RIGHT_TO_LEFT para resumen (continuación del proceso)
        replaceFragment(fragment, true, AnimationDirection.RIGHT_TO_LEFT);
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

        // ANIMACIÓN BOTTOM_TO_TOP para conversación (emerge desde abajo)
        replaceFragment(fragment, true, AnimationDirection.BOTTOM_TO_TOP);
    }

    public void navigateToNotificationSettings(Bundle userArgs) {
        NotificationSettingsFragment fragment = new NotificationSettingsFragment();
        if (userArgs != null) fragment.setArguments(userArgs);

        // ANIMACIÓN RIGHT_TO_LEFT para configuraciones (navegación hacia adelante)
        replaceFragment(fragment, true, AnimationDirection.RIGHT_TO_LEFT);
    }

    // ============= MÉTODOS AUXILIARES =============

    /**
     * Determina la dirección de animación basada en la posición de los tabs
     */
    private AnimationDirection getBottomNavAnimationDirection(BottomNavTab targetTab) {
        int currentPos = currentBottomNavTab.getPosition();
        int targetPos = targetTab.getPosition();

        if (currentPos == targetPos) {
            return AnimationDirection.NONE; // Mismo tab, sin animación
        } else if (currentPos < targetPos) {
            return AnimationDirection.RIGHT_TO_LEFT; // Moverse hacia la derecha
        } else {
            return AnimationDirection.LEFT_TO_RIGHT; // Moverse hacia la izquierda
        }
    }

    /**
     * Método base mejorado para reemplazar fragmentos con animaciones específicas
     */
    private void replaceFragment(Fragment fragment, boolean addToBackStack, AnimationDirection direction) {
        if (activity == null) return;

        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

        // Aplicar animaciones basadas en la dirección
        AnimationHelper.AnimationSet animSet = AnimationHelper.getAnimationSet(direction);
        if (animSet.enter != 0) {
            transaction.setCustomAnimations(
                    animSet.enter,
                    animSet.exit,
                    animSet.popEnter,
                    animSet.popExit
            );
        }

        transaction.replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    // ============= MÉTODOS PÚBLICOS ADICIONALES =============

    /**
     * Navegación con animación personalizada
     */
    public void navigateWithCustomAnimation(Fragment fragment, AnimationDirection direction, boolean addToBackStack) {
        replaceFragment(fragment, addToBackStack, direction);
    }

    /**
     * Navegación con fade (para transiciones suaves)
     */
    public void navigateWithFade(Fragment fragment, boolean addToBackStack) {
        replaceFragment(fragment, addToBackStack, AnimationDirection.FADE);
    }

    public void goBack() {
        if (activity != null && activity.getSupportFragmentManager().getBackStackEntryCount() > 0) {
            activity.getSupportFragmentManager().popBackStack();
        }
    }

    /**
     * Actualizar el tab actual (llamar desde BaseBottomNavigationFragment)
     */
    public void setCurrentBottomNavTab(BottomNavTab tab) {
        this.currentBottomNavTab = tab;
    }

    public BottomNavTab getCurrentBottomNavTab() {
        return currentBottomNavTab;
    }
}