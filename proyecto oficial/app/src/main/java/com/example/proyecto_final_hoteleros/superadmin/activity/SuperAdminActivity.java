package com.example.proyecto_final_hoteleros.superadmin.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.fragment.DashboardFragment;
import com.example.proyecto_final_hoteleros.superadmin.fragment.AdminsFragment;
import com.example.proyecto_final_hoteleros.superadmin.fragment.ReportesFragment;
import com.example.proyecto_final_hoteleros.superadmin.fragment.TaxistasFragment;
import com.example.proyecto_final_hoteleros.superadmin.fragment.UsuariosFragment;

public class SuperAdminActivity extends AppCompatActivity {

    private TextView tvAdminName;
    private ImageView ivProfile;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin);

        initViews();
        setupInitialData();
        loadInitialFragment();
    }

    private void initViews() {
        tvAdminName = findViewById(R.id.tv_admin_name);
        ivProfile = findViewById(R.id.iv_profile);
        fragmentManager = getSupportFragmentManager();

        setupClickListeners();
    }

    private void setupClickListeners() {
        ivProfile.setOnClickListener(v -> showProfileOptions());
    }

    private void setupInitialData() {
        String adminName = getAdminName();
        tvAdminName.setText(adminName);
    }

    private String getAdminName() {
        return "Superadmin";
    }

    private void loadInitialFragment() {
        DashboardFragment dashboardFragment = new DashboardFragment();
        loadFragment(dashboardFragment, "DASHBOARD", false);
    }

    public void loadFragment(Fragment fragment, String tag, boolean addToBackStack) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);

        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }

        transaction.commit();
    }

    // Métodos de navegación actualizados
    public void navigateToAdmins() {
        AdminsFragment adminsFragment = new AdminsFragment();
        loadFragment(adminsFragment, "ADMINS", true);
    }

    public void navigateToTaxistas() {
        TaxistasFragment taxistasFragment = new TaxistasFragment();
        loadFragment(taxistasFragment, "TAXISTAS", true);
    }

    public void navigateToUsuarios() {
        UsuariosFragment usuariosFragment = new UsuariosFragment();
        loadFragment(usuariosFragment, "USUARIOS", true);
    }

    public void navigateToReportes() {
        ReportesFragment reportesFragment = new ReportesFragment();
        loadFragment(reportesFragment, "REPORTES", true);
    }

    public void navigateToLogs() {
        // LogsFragment logsFragment = new LogsFragment();
        // loadFragment(logsFragment, "LOGS", true);
        showToast("Logs del Sistema - Próximamente");
    }

    public void navigateToAddAdmin() {
        // AddAdminFragment addAdminFragment = new AddAdminFragment();
        // loadFragment(addAdminFragment, "ADD_ADMIN", true);
        showToast("Registro de Admin - Próximamente");
    }

    // Método para manejar clicks desde DashboardFragment
    public void handleQuickAccessClick(String action) {
        switch (action) {
            case "admins":
                navigateToAdmins();
                break;
            case "taxistas":
                navigateToTaxistas();
                break;
            case "usuarios":
                navigateToUsuarios();
                break;
            case "reportes":
                navigateToReportes();
                break;
            case "logs":
                navigateToLogs();
                break;
            case "add_admin":
                navigateToAddAdmin();
                break;
        }
    }

    // Método para volver al dashboard desde otros fragments
    public void navigateBackToDashboard() {
        DashboardFragment dashboardFragment = new DashboardFragment();
        loadFragment(dashboardFragment, "DASHBOARD", false);

        // Limpiar back stack
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private void showProfileOptions() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Opciones de perfil")
                .setItems(new String[]{"Ver perfil", "Configuración", "Cerrar sesión"},
                        (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    showToast("Ver perfil");
                                    break;
                                case 1:
                                    showToast("Configuración");
                                    break;
                                case 2:
                                    showLogoutConfirmation();
                                    break;
                            }
                        })
                .show();
    }

    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Volver al MainActivity
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            showExitConfirmation();
        }
    }

    private void showExitConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Salir")
                .setMessage("¿Estás seguro que deseas salir?")
                .setPositiveButton("Sí", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCurrentFragment();
    }

    private void refreshCurrentFragment() {
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof DashboardFragment) {
            // Refrescar dashboard si es necesario
        }
    }
}