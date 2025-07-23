package com.example.proyecto_final_hoteleros;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.proyecto_final_hoteleros.auth.interfaces.MessagerRegister;
import com.example.proyecto_final_hoteleros.auth.login.LoginFragment;
import com.example.proyecto_final_hoteleros.auth.password.ForgotPasswordFragment;
import com.example.proyecto_final_hoteleros.auth.register.RegisterUserActivity;
import com.example.proyecto_final_hoteleros.auth.register.SelectUserTypeFragment;

public class AuthActivity extends AppCompatActivity implements MessagerRegister {

    private TextView tvLoginTab;
    private TextView tvRegisterTab;
    private View viewTabIndicatorLogin;
    private View viewTabIndicatorRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ CONFIGURAR EDGE-TO-EDGE
        enableEdgeToEdge();

        setContentView(R.layout.sistema_activity_auth);

        // ✅ CONFIGURAR WINDOW INSETS - VERSIÓN CORREGIDA (SIN TOP PADDING)
        View rootLayout = findViewById(android.R.id.content).getRootView();
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            boolean isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            int bottomPadding = Math.max(systemBars.bottom, ime.bottom);

            View mainLayout = findViewById(android.R.id.content);
            if (mainLayout != null) {
                mainLayout.setPadding(
                        mainLayout.getPaddingLeft(),
                        0,               // 🎯 SIN top padding - el XML maneja el margen
                        mainLayout.getPaddingRight(),
                        bottomPadding    // 🎯 Solo bottom padding dinámico
                );
            }

            return insets;
        });

        tvLoginTab = findViewById(R.id.tvLoginTab);
        viewTabIndicatorLogin = findViewById(R.id.viewTabIndicatorLogin);
        tvRegisterTab = findViewById(R.id.tvRegisterTab);
        viewTabIndicatorRegister = findViewById(R.id.viewTabIndicatorRegister);

        // Verificar si debemos mostrar login o registro basado en el intent
        String mode = getIntent().getStringExtra("mode");
        if (mode != null && mode.equals("register")) {
            goToRegister();
        } else {
            goToLogin();
        }

        tvRegisterTab.setOnClickListener(view -> {
            goToRegister();
        });

        tvLoginTab.setOnClickListener(view -> {
            goToLogin();
        });
    }

    // ✅ MÉTODO PARA HABILITAR EDGE-TO-EDGE CON ICONOS OSCUROS (VERSIÓN SEGURA)
    private void enableEdgeToEdge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );

        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }

    // El resto de tu código existente permanece igual...
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.d("AuthActivity", "=== CONFIGURATION CHANGED ===");
        Log.d("AuthActivity", "Orientation: " + (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? "LANDSCAPE" : "PORTRAIT"));

        // No hacer nada especial, solo loguearlo
        // El estado se mantiene automáticamente
    }

    public void goToLogin() {
        // Asegurar que las pestañas estén visibles
        showTabLayout();

        // Cambiar a pestaña de inicio de sesión
        FragmentManager fm = getSupportFragmentManager();
        tvLoginTab.setTextColor(getResources().getColor(R.color.colorAccent));
        viewTabIndicatorLogin.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        viewTabIndicatorRegister.setBackgroundColor(getResources().getColor(android.R.color.white));
        tvRegisterTab.setTextColor(getResources().getColor(android.R.color.darker_gray));

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragmentContainer, new LoginFragment());
        // NO agregar al back stack para el fragmento principal
        ft.commit();
    }

    public void goToRegister() {
        // Asegurar que las pestañas estén visibles
        showTabLayout();

        // Cambiar a pestaña de registro
        FragmentManager fm = getSupportFragmentManager();
        tvLoginTab.setTextColor(getResources().getColor(android.R.color.darker_gray));
        viewTabIndicatorLogin.setBackgroundColor(getResources().getColor(android.R.color.white));
        viewTabIndicatorRegister.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        tvRegisterTab.setTextColor(getResources().getColor(R.color.colorAccent));

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragmentContainer, new SelectUserTypeFragment());
        // NO agregar al back stack para el fragmento principal
        ft.commit();
    }

    public void goToForgotPassword() {
        // Guardar referencia al ScrollView para restaurarlo después
        final ScrollView scrollView = findViewById(R.id.scrollViewLogin);
        final int scrollY = (scrollView != null) ? scrollView.getScrollY() : 0;

        // Ocultar las pestañas para este fragmento específico
        hideTabLayout();

        // Cambiar al fragmento de recuperación de contraseña
        FragmentManager fm = getSupportFragmentManager();
        fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                // Restaurar el scroll cuando volvamos atrás
                if (fm.getBackStackEntryCount() == 0 && scrollView != null) {
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.scrollTo(0, scrollY);
                        }
                    });
                }
                // Eliminar el listener después de su uso
                fm.removeOnBackStackChangedListener(this);
            }
        });

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragmentContainer, ForgotPasswordFragment.newInstance());
        ft.addToBackStack(null);
        ft.commit();
    }

    // Método para mostrar el layout de pestañas
    private void showTabLayout() {
        View tabLayout = findViewById(R.id.tabLayout);
        View viewTabIndicatorLogin = findViewById(R.id.viewTabIndicatorLogin);
        ViewGroup indicatorsLayout = null;

        if (viewTabIndicatorLogin != null) {
            indicatorsLayout = (ViewGroup) viewTabIndicatorLogin.getParent();
        }

        if (tabLayout != null) tabLayout.setVisibility(View.VISIBLE);
        if (indicatorsLayout != null) indicatorsLayout.setVisibility(View.VISIBLE);
    }

    // Método para ocultar el layout de pestañas
    private void hideTabLayout() {
        View tabLayout = findViewById(R.id.tabLayout);
        View viewTabIndicatorLogin = findViewById(R.id.viewTabIndicatorLogin);
        ViewGroup indicatorsLayout = null;

        if (viewTabIndicatorLogin != null) {
            indicatorsLayout = (ViewGroup) viewTabIndicatorLogin.getParent();
        }

        if (tabLayout != null) tabLayout.setVisibility(View.GONE);
        if (indicatorsLayout != null) indicatorsLayout.setVisibility(View.GONE);
    }

    // Implementación de MessagerRegister
    @Override
    public void gotoMainRegister(String userType) {
        // Limpiar datos del formulario cuando se inicia un nuevo registro
        getSharedPreferences("UserData", MODE_PRIVATE)
                .edit()
                .remove("photoPath")
                .remove("photoUri")
                .remove("pdfPath")
                .remove("pdfUri")
                .remove("email")
                .remove("photoSkipped")
                .apply();

        Log.d("AuthActivity", "Iniciando nuevo registro - datos anteriores eliminados");

        // En lugar de cargar un fragmento, iniciamos la nueva actividad
        Intent intent = new Intent(this, RegisterUserActivity.class);
        intent.putExtra("userType", userType);
        startActivity(intent);
    }

    @Override
    public void gotoRegisterPasswordClient() {
        // Implementación pendiente
    }

    @Override
    public void gotoRegisterPasswordDriver() {
        // Implementación pendiente
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        // Si estamos en el back stack (no en el fragmento principal)
        if (fm.getBackStackEntryCount() > 0) {
            // Si estamos en ForgotPassword o cualquier fragmento secundario, volver al login
            fm.popBackStack();
            // Restaurar las pestañas cuando volvemos
            showTabLayout();
        } else {
            // Si estamos en login o registro principal, cerrar la actividad
            super.onBackPressed();
        }
    }
}