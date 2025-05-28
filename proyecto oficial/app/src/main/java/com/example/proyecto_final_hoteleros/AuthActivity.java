package com.example.proyecto_final_hoteleros;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
        setContentView(R.layout.sistema_activity_auth);

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

    public void goToLogin() {
        // Asegurar que las pestañas estén visibles
        showTabLayout();

        // Cambiar a pestaña de inicio de sesión
        FragmentManager fm = getSupportFragmentManager();
        tvLoginTab.setTextColor(getResources().getColor(R.color.colorAccent));
        viewTabIndicatorLogin.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        viewTabIndicatorRegister.setBackgroundColor(getResources().getColor(android.R.color.white));
        tvRegisterTab.setTextColor(getResources().getColor(android.R.color.darker_gray));
        FragmentTransaction ft = fm.beginTransaction(); // Crear una transacción de fragmento
        ft.replace(R.id.fragmentContainer, new LoginFragment()); // Reemplazar el fragmento actual con LoginFragment
        ft.addToBackStack(null); // Agregar a la pila de retroceso
        ft.commit(); // Ejecutar la transacción
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
        FragmentTransaction ft = fm.beginTransaction(); // Crear una transacción de fragmento
        ft.replace(R.id.fragmentContainer, new SelectUserTypeFragment()); // Reemplazar el fragmento actual con LoginFragment
        ft.addToBackStack(null); // Agregar a la pila de retroceso
        ft.commit(); // Ejecutar la transacción
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
}