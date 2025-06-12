package com.example.proyecto_final_hoteleros.adminhotel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.fragment.AdminChatFragment;
import com.example.proyecto_final_hoteleros.adminhotel.fragment.AdminHotelFragment;
import com.example.proyecto_final_hoteleros.adminhotel.fragment.AdminCheckoutFragment;
import com.example.proyecto_final_hoteleros.adminhotel.fragment.AdminReportsFragment;
import com.example.proyecto_final_hoteleros.adminhotel.fragment.AdminTaxiFragment;
import com.example.proyecto_final_hoteleros.adminhotel.fragment.AdminProfileFragment;

public class AdminHotelActivity extends AppCompatActivity {

    private View fragmentContainer;
    private String currentFragmentTag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_hotel);

        fragmentContainer = findViewById(R.id.fragment_container);

        setupBarraNavegacion();

        // Cargar fragment inicial (Hotel)
        if (savedInstanceState == null) {
            loadFragment(new AdminHotelFragment(), "HOTEL");
        }
    }

    private void setupBarraNavegacion() {
        // Botón Hotel - Gestión del hotel
        findViewById(R.id.nav_hotel).setOnClickListener(v -> {
            updateNavigationState(R.id.nav_hotel);
            loadFragment(new AdminHotelFragment(), "HOTEL");
        });

        // Botón Checkout - Gestión de checkouts
        findViewById(R.id.nav_checkout).setOnClickListener(v -> {
            updateNavigationState(R.id.nav_checkout);
            loadFragment(new AdminCheckoutFragment(), "CHECKOUT");
        });

        // Botón Chat - Fragment de chat
        findViewById(R.id.nav_chat).setOnClickListener(v -> {
            updateNavigationState(R.id.nav_chat);
            loadFragment(new AdminChatFragment(), "CHAT");
        });

        // Botón Reportes - Reportes de ventas
        findViewById(R.id.nav_reports).setOnClickListener(v -> {
            updateNavigationState(R.id.nav_reports);
            loadFragment(new AdminReportsFragment(), "REPORTS");
        });

        // Botón Taxi - Monitoreo de estado de taxis
        findViewById(R.id.nav_taxi).setOnClickListener(v -> {
            updateNavigationState(R.id.nav_taxi);
            loadFragment(new AdminTaxiFragment(), "TAXI");
        });

        // Botón Perfil - Perfil del administrador
        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            updateNavigationState(R.id.nav_profile);
            loadFragment(new AdminProfileFragment(), "PROFILE");
        });
    }

    private void updateNavigationState(int selectedNavId) {
        // Reset all icons to white
        ((ImageView) findViewById(R.id.iv_hotel)).setColorFilter(getResources().getColor(R.color.white));
        ((ImageView) findViewById(R.id.iv_checkout)).setColorFilter(getResources().getColor(R.color.white));
        ((ImageView) findViewById(R.id.iv_chat)).setColorFilter(getResources().getColor(R.color.white));
        ((ImageView) findViewById(R.id.iv_reports)).setColorFilter(getResources().getColor(R.color.white));
        ((ImageView) findViewById(R.id.iv_taxi)).setColorFilter(getResources().getColor(R.color.white));
        ((ImageView) findViewById(R.id.iv_profile)).setColorFilter(getResources().getColor(R.color.white));

        // Set selected icon to orange using if-else instead of switch
        if (selectedNavId == R.id.nav_hotel) {
            ((ImageView) findViewById(R.id.iv_hotel)).setColorFilter(getResources().getColor(R.color.orange));
        } else if (selectedNavId == R.id.nav_checkout) {
            ((ImageView) findViewById(R.id.iv_checkout)).setColorFilter(getResources().getColor(R.color.orange));
        } else if (selectedNavId == R.id.nav_chat) {
            ((ImageView) findViewById(R.id.iv_chat)).setColorFilter(getResources().getColor(R.color.orange));
        } else if (selectedNavId == R.id.nav_reports) {
            ((ImageView) findViewById(R.id.iv_reports)).setColorFilter(getResources().getColor(R.color.orange));
        } else if (selectedNavId == R.id.nav_taxi) {
            ((ImageView) findViewById(R.id.iv_taxi)).setColorFilter(getResources().getColor(R.color.orange));
        } else if (selectedNavId == R.id.nav_profile) {
            ((ImageView) findViewById(R.id.iv_profile)).setColorFilter(getResources().getColor(R.color.orange));
        }
    }

    private void loadFragment(Fragment fragment, String tag) {
        if (tag.equals(currentFragmentTag)) {
            return; // No recargar el mismo fragment
        }

        currentFragmentTag = tag;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);

        // Solo agregar al backstack si no es el fragment principal
        if (!tag.equals("HOTEL")) {
            transaction.addToBackStack(tag);
        }

        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            // Update navigation state based on current fragment
            updateNavigationForCurrentFragment();
        } else {
            super.onBackPressed();
        }
    }

    private void updateNavigationForCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof AdminHotelFragment) {
            updateNavigationState(R.id.nav_hotel);
            currentFragmentTag = "HOTEL";
        } else if (currentFragment instanceof AdminCheckoutFragment) {
            updateNavigationState(R.id.nav_checkout);
            currentFragmentTag = "CHECKOUT";
        } else if (currentFragment instanceof AdminChatFragment) {
            updateNavigationState(R.id.nav_chat);
            currentFragmentTag = "CHAT";
        } else if (currentFragment instanceof AdminReportsFragment) {
            updateNavigationState(R.id.nav_reports);
            currentFragmentTag = "REPORTS";
        } else if (currentFragment instanceof AdminTaxiFragment) {
            updateNavigationState(R.id.nav_taxi);
            currentFragmentTag = "TAXI";
        } else if (currentFragment instanceof AdminProfileFragment) {
            updateNavigationState(R.id.nav_profile);
            currentFragmentTag = "PROFILE";
        }
    }
}