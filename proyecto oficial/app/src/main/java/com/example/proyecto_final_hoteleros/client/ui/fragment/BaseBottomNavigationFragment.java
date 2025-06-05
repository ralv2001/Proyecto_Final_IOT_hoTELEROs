package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.navigation.NavigationManager;
import com.example.proyecto_final_hoteleros.client.utils.UserDataManager;

public abstract class BaseBottomNavigationFragment extends Fragment {

    // Views de navegación
    protected LinearLayout navHome, navExplore, navChat, navProfile;
    protected ImageView ivHome, ivExplore, ivChat, ivProfile;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBottomNavigation(view);

        // ✅ NUEVO: Informar al NavigationManager sobre el tab actual
        NavigationManager.BottomNavTab currentTab = getCurrentNavigationTab();
        NavigationManager.getInstance().setCurrentBottomNavTab(currentTab);
    }

    private void setupBottomNavigation(View rootView) {
        initNavigationViews(rootView);
        setupNavigationListeners();
        highlightCurrentTab();
    }

    private void initNavigationViews(View rootView) {
        navHome = rootView.findViewById(R.id.nav_home);
        navExplore = rootView.findViewById(R.id.nav_explore);
        navChat = rootView.findViewById(R.id.nav_chat);
        navProfile = rootView.findViewById(R.id.nav_profile);

        ivHome = rootView.findViewById(R.id.iv_home);
        ivExplore = rootView.findViewById(R.id.iv_explore);
        ivChat = rootView.findViewById(R.id.iv_chat);
        ivProfile = rootView.findViewById(R.id.iv_profile);
    }

    private void setupNavigationListeners() {
        Bundle userArgs = UserDataManager.getInstance().getUserBundle();

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (!isCurrentTab(NavigationTab.HOME)) {
                    // ✅ USA NavigationManager - animación automática LEFT_TO_RIGHT o RIGHT_TO_LEFT
                    NavigationManager.getInstance().navigateToHome(userArgs);
                }
            });
        }

        if (navExplore != null) {
            navExplore.setOnClickListener(v -> {
                if (!isCurrentTab(NavigationTab.EXPLORE)) {
                    // ✅ USA NavigationManager - animación automática según dirección
                    NavigationManager.getInstance().navigateToExplore(userArgs);
                }
            });
        }

        if (navChat != null) {
            navChat.setOnClickListener(v -> {
                if (!isCurrentTab(NavigationTab.CHAT)) {
                    // ✅ USA NavigationManager - animación automática según dirección
                    NavigationManager.getInstance().navigateToChat(userArgs);
                }
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                if (!isCurrentTab(NavigationTab.PROFILE)) {
                    // ✅ USA NavigationManager - animación automática según dirección
                    NavigationManager.getInstance().navigateToProfile(userArgs);
                }
            });
        }
    }

    private void highlightCurrentTab() {
        resetAllTabs();

        NavigationTab currentTab = getCurrentTab();
        switch (currentTab) {
            case HOME:
                if (ivHome != null) setTabActive(ivHome);
                break;
            case EXPLORE:
                if (ivExplore != null) setTabActive(ivExplore);
                break;
            case CHAT:
                if (ivChat != null) setTabActive(ivChat);
                break;
            case PROFILE:
                if (ivProfile != null) setTabActive(ivProfile);
                break;
        }
    }

    private void resetAllTabs() {
        if (ivHome != null) setTabInactive(ivHome);
        if (ivExplore != null) setTabInactive(ivExplore);
        if (ivChat != null) setTabInactive(ivChat);
        if (ivProfile != null) setTabInactive(ivProfile);
    }

    private void setTabActive(ImageView icon) {
        icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.orange));
    }

    private void setTabInactive(ImageView icon) {
        icon.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));
    }

    // ✅ NUEVO: Mapear NavigationTab a BottomNavTab
    private NavigationManager.BottomNavTab getCurrentNavigationTab() {
        switch (getCurrentTab()) {
            case HOME: return NavigationManager.BottomNavTab.HOME;
            case EXPLORE: return NavigationManager.BottomNavTab.EXPLORE;
            case CHAT: return NavigationManager.BottomNavTab.CHAT;
            case PROFILE: return NavigationManager.BottomNavTab.PROFILE;
            default: return NavigationManager.BottomNavTab.HOME;
        }
    }

    // Métodos abstractos
    protected abstract NavigationTab getCurrentTab();

    private boolean isCurrentTab(NavigationTab tab) {
        return getCurrentTab() == tab;
    }

    // Enum para identificar tabs
    public enum NavigationTab {
        HOME, EXPLORE, CHAT, PROFILE
    }
}