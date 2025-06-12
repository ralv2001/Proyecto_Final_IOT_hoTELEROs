package com.example.proyecto_final_hoteleros.superadmin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.models.UserModel;
import com.example.proyecto_final_hoteleros.superadmin.fragment.DashboardFragment;
import com.example.proyecto_final_hoteleros.superadmin.fragment.AdminsFragment;
import com.example.proyecto_final_hoteleros.superadmin.fragment.TaxistasFragment;
import com.example.proyecto_final_hoteleros.superadmin.fragment.UsuariosFragment;
import com.example.proyecto_final_hoteleros.superadmin.fragment.ReportesFragment;
import com.example.proyecto_final_hoteleros.superadmin.fragment.TaxistaDocumentsFragment;
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;

import java.util.List;

public class SuperAdminActivity extends AppCompatActivity {

    private TextView tvAdminName;
    private ImageView ivProfile;
    private FragmentManager fragmentManager;

    // 🔥 NUEVOS CAMPOS PARA DATOS DEL USUARIO LOGUEADO
    private String userId;
    private String userEmail;
    private String userName;
    private String userType;

    private TaxistasFragment taxistasFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin);

        // 🔥 RECIBIR DATOS DEL INTENT
        receiveUserDataFromIntent();

        initViews();
        setupInitialData();
        loadInitialFragment();
    }

    // 🔥 NUEVO MÉTODO: Recibir datos del login desde el Intent
    private void receiveUserDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("userId");
            userEmail = intent.getStringExtra("userEmail");
            userName = intent.getStringExtra("userName");
            userType = intent.getStringExtra("userType");

            android.util.Log.d("SuperAdminActivity", "=== DATOS DEL USUARIO RECIBIDOS ===");
            android.util.Log.d("SuperAdminActivity", "UserId: " + userId);
            android.util.Log.d("SuperAdminActivity", "Email: " + userEmail);
            android.util.Log.d("SuperAdminActivity", "Name: " + userName);
            android.util.Log.d("SuperAdminActivity", "Type: " + userType);
        } else {
            android.util.Log.w("SuperAdminActivity", "No se recibieron datos del usuario");
            // Valores por defecto para desarrollo/testing
            userId = "superadmin_default";
            userEmail = "superadmin@hotel.com";
            userName = "Super Administrador";
            userType = "superadmin";
        }
    }

    // 🔥 NUEVO MÉTODO: Establecer datos del usuario (alternativo al Intent)
    public void setUserData(String userId, String userEmail, String userName, String userType) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userType = userType;

        android.util.Log.d("SuperAdminActivity", "=== DATOS DEL USUARIO ESTABLECIDOS ===");
        android.util.Log.d("SuperAdminActivity", "UserId: " + userId);
        android.util.Log.d("SuperAdminActivity", "Email: " + userEmail);
        android.util.Log.d("SuperAdminActivity", "Name: " + userName);
        android.util.Log.d("SuperAdminActivity", "Type: " + userType);

        // Actualizar la interfaz con los nuevos datos
        updateUserInterface();
    }

    // 🔥 GETTERS PARA QUE LOS FRAGMENTS ACCEDAN A LOS DATOS
    public String getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public String getUserName() { return userName; }
    public String getUserType() { return userType; }

    private void initViews() {
        tvAdminName = findViewById(R.id.tv_admin_name);
        ivProfile = findViewById(R.id.iv_profile);
        fragmentManager = getSupportFragmentManager();

        setupClickListeners();
    }
    // Método para cargar taxistas pendientes
    public void loadPendingDrivers() {
        FirebaseManager firebaseManager = FirebaseManager.getInstance();

        firebaseManager.getPendingDrivers(new FirebaseManager.DriverListCallback() {
            @Override
            public void onSuccess(List<UserModel> pendingDrivers) {
                android.util.Log.d("SuperAdmin", "Taxistas pendientes obtenidos: " + pendingDrivers.size());

                // Actualizar métricas en el dashboard
                Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof DashboardFragment) {
                    ((DashboardFragment) currentFragment).updatePendingDriversCount(pendingDrivers.size());
                }

                // Si el TaxistasFragment está activo, actualizarlo
                if (taxistasFragment != null) {
                    taxistasFragment.updatePendingDrivers(pendingDrivers);
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("SuperAdmin", "Error obteniendo taxistas: " + error);
            }
        });
    }

    private void setupClickListeners() {
        ivProfile.setOnClickListener(v -> showProfileOptions());
    }

    private void setupInitialData() {
        updateUserInterface();
    }

    // 🔥 NUEVO MÉTODO: Actualizar interfaz con datos del usuario
    private void updateUserInterface() {
        if (userName != null && !userName.isEmpty()) {
            tvAdminName.setText(userName);
        } else {
            tvAdminName.setText("Super Administrador");
        }
    }

    private String getAdminName() {
        return userName != null ? userName : "Super Administrador";
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

    // Métodos de navegación existentes...
    public void navigateToAdmins() {
        AdminsFragment adminsFragment = new AdminsFragment();
        loadFragment(adminsFragment, "ADMINS", true);
    }

    public void navigateToTaxistas() {
        taxistasFragment = new TaxistasFragment();
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
        showToast("Logs del Sistema - Próximamente");
    }

    public void navigateToAddAdmin() {
        showToast("Registro de Admin - Próximamente");
    }

    public void handleQuickAccessClick(String action) {
        android.util.Log.d("SuperAdminActivity", "Handling action: " + action);
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

    public void navigateBackToDashboard() {
        DashboardFragment dashboardFragment = new DashboardFragment();
        loadFragment(dashboardFragment, "DASHBOARD", false);

        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private void showProfileOptions() {
        String[] options;
        if (userEmail != null) {
            options = new String[]{
                    "Ver perfil (" + userEmail + ")",
                    "Configuración",
                    "Cerrar sesión"
            };
        } else {
            options = new String[]{"Ver perfil", "Configuración", "Cerrar sesión"};
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Opciones de perfil")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showUserProfile();
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

    // 🔥 NUEVO MÉTODO: Mostrar perfil del usuario
    private void showUserProfile() {
        StringBuilder profileInfo = new StringBuilder();
        profileInfo.append("👤 Información del Usuario\n\n");
        profileInfo.append("📧 Email: ").append(userEmail != null ? userEmail : "No disponible").append("\n");
        profileInfo.append("👨‍💼 Nombre: ").append(userName != null ? userName : "No disponible").append("\n");
        profileInfo.append("🔑 Tipo: ").append(userType != null ? userType : "No disponible").append("\n");
        profileInfo.append("🆔 ID: ").append(userId != null ? userId : "No disponible");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Perfil del Usuario")
                .setMessage(profileInfo.toString())
                .setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // 🔥 CERRAR SESIÓN Y VOLVER AL LOGIN
                    logoutUser();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // 🔥 NUEVO MÉTODO: Logout del usuario
    private void logoutUser() {
        // Limpiar datos del usuario
        userId = null;
        userEmail = null;
        userName = null;
        userType = null;

        // Volver al MainActivity (pantalla de login)
        Intent intent = new Intent(this, com.example.proyecto_final_hoteleros.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        showToast("Sesión cerrada exitosamente");
    }
    // 🔥 MÉTODO ESTÁTICO PARA QUE TU COMPAÑERO INICIE EL SUPERADMIN
    public static void startWithUserData(android.content.Context context,
                                         String userId,
                                         String userEmail,
                                         String userName,
                                         String userType) {
        Intent intent = new Intent(context, SuperAdminActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("userEmail", userEmail);
        intent.putExtra("userName", userName);
        intent.putExtra("userType", userType);
        context.startActivity(intent);
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