package com.example.proyecto_final_hoteleros;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_final_hoteleros.auth.interfaces.MessagerRegister;
import com.example.proyecto_final_hoteleros.auth.login.LoginFragment;
import com.example.proyecto_final_hoteleros.auth.register.RegisterFragment;
import com.example.proyecto_final_hoteleros.auth.register.SelectUserTypeFragment;

public class AuthActivity extends AppCompatActivity implements MessagerRegister {

    private TextView tvLoginTab;
    private TextView tvRegisterTab;
    private View viewTabIndicatorLogin;
    private View viewTabIndicatorRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

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

    // Implementación de MessagerRegister
    @Override
    public void gotoMainRegister(String userType) {
        Toast.makeText(this, "userType: " + userType, Toast.LENGTH_SHORT).show();
        // Cambiar a pestaña de registro Principal
        try {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragmentContainer, RegisterFragment.newInstance(userType));
            ft.addToBackStack(null);
            ft.commit();
        } catch (Exception e) {
            Toast.makeText(this, "Error navegando: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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