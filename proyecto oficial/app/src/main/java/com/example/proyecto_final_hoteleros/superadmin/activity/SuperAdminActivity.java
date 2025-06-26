package com.example.proyecto_final_hoteleros.superadmin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Handler;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.models.UserModel;
import com.example.proyecto_final_hoteleros.superadmin.fragment.AddHotelAdminFragment;
import com.example.proyecto_final_hoteleros.superadmin.fragment.DashboardFragment;
import com.example.proyecto_final_hoteleros.superadmin.fragment.AdminsFragment;
import com.example.proyecto_final_hoteleros.superadmin.fragment.TaxistasFragment;
import com.example.proyecto_final_hoteleros.superadmin.fragment.UsuariosFragment;
import com.example.proyecto_final_hoteleros.superadmin.fragment.ReportesFragment;
import com.example.proyecto_final_hoteleros.superadmin.fragment.TaxistaDocumentsFragment;
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;

import java.util.List;

public class SuperAdminActivity extends AppCompatActivity {

    private static final String TAG = "SuperAdminActivity";

    private TextView tvAdminName;
    private ImageView ivProfile;
    private FragmentManager fragmentManager;

    // 🔥 CAMPOS PARA DATOS DEL USUARIO LOGUEADO
    private String userId;
    private String userEmail;
    private String userName;
    private String userType;

    private TaxistasFragment taxistasFragment;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin);

        // 🔥 MANEJO MODERNO DEL BACK BUTTON
        setupModernBackHandler();

        // 🔥 RECIBIR DATOS DEL INTENT
        receiveUserDataFromIntent();

        initViews();
        setupInitialData();
        loadInitialFragment();
    }

    // 🔥 NUEVO MÉTODO: Configurar manejo moderno del back button
    private void setupModernBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "OnBackPressedCallback - handleOnBackPressed()");
                handleCustomBackPress();
            }
        });
        Log.d(TAG, "OnBackPressedCallback configurado");
    }

    // 🔥 NUEVO MÉTODO: Lógica personalizada para back press
    private void handleCustomBackPress() {
        Log.d(TAG, "handleCustomBackPress - Fragment actual: " +
                (currentFragment != null ? currentFragment.getClass().getSimpleName() : "null"));

        // Obtener el fragment actual
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof TaxistaDocumentsFragment) {
            // Si estamos en documentos, ir a taxistas
            Log.d(TAG, "Navegando: TaxistaDocuments → Taxistas");
            navigateBackToTaxistas();
            return;
        }

        if (currentFragment instanceof TaxistasFragment) {
            // Si estamos en taxistas, ir al dashboard
            Log.d(TAG, "Navegando: Taxistas → Dashboard");
            navigateBackToDashboard();
            return;
        }

        if (currentFragment instanceof DashboardFragment) {
            // Si estamos en el dashboard, mostrar confirmación de salir
            Log.d(TAG, "En Dashboard - Mostrando confirmación de salida");
            showExitConfirmation();
            return;
        }

        // Si hay fragments en el back stack, hacer pop normal
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            Log.d(TAG, "Hay fragments en back stack, haciendo pop");
            getSupportFragmentManager().popBackStack();
            return;
        }

        // Por defecto, mostrar confirmación de salida
        showExitConfirmation();
    }

    // 🔥 MÉTODO: Recibir datos del login desde el Intent
    private void receiveUserDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("userId");
            userEmail = intent.getStringExtra("userEmail");
            userName = intent.getStringExtra("userName");
            userType = intent.getStringExtra("userType");

            Log.d(TAG, "=== DATOS DEL USUARIO RECIBIDOS ===");
            Log.d(TAG, "UserId: " + userId);
            Log.d(TAG, "Email: " + userEmail);
            Log.d(TAG, "Name: " + userName);
            Log.d(TAG, "Type: " + userType);
        } else {
            Log.w(TAG, "No se recibieron datos del usuario");
            // Valores por defecto para desarrollo/testing
            userId = "superadmin_default";
            userEmail = "superadmin@hotel.com";
            userName = "Super Administrador";
            userType = "superadmin";
        }
    }

    // 🔥 MÉTODO: Navegar específicamente al TaxistasFragment
    public void navigateBackToTaxistas() {
        Log.d(TAG, "Navegando de vuelta al TaxistasFragment");

        // Verificar si hay un TaxistasFragment en el back stack
        boolean foundTaxistas = false;
        FragmentManager fragmentManager = getSupportFragmentManager();

        for (int i = fragmentManager.getBackStackEntryCount() - 1; i >= 0; i--) {
            FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(i);
            if ("TAXISTAS".equals(entry.getName())) {
                // Hacer pop hasta ese fragment
                fragmentManager.popBackStack("TAXISTAS", 0);
                foundTaxistas = true;
                break;
            }
        }

        // Si no se encontró, crear uno nuevo
        if (!foundTaxistas) {
            // Limpiar stack actual y crear TaxistasFragment
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            TaxistasFragment taxistasFragment = new TaxistasFragment();
            loadFragment(taxistasFragment, "TAXISTAS", false);
        }
    }

    // 🔥 MÉTODO: Establecer datos del usuario (alternativo al Intent)
    public void setUserData(String userId, String userEmail, String userName, String userType) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userType = userType;

        Log.d(TAG, "=== DATOS DEL USUARIO ESTABLECIDOS ===");
        Log.d(TAG, "UserId: " + userId);
        Log.d(TAG, "Email: " + userEmail);
        Log.d(TAG, "Name: " + userName);
        Log.d(TAG, "Type: " + userType);

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
                Log.d(TAG, "Taxistas pendientes obtenidos: " + pendingDrivers.size());

                runOnUiThread(() -> {
                    // Si el TaxistasFragment está activo, actualizarlo
                    if (taxistasFragment != null) {
                        taxistasFragment.updatePendingDrivers(pendingDrivers);
                    }

                    // Actualizar métricas en el dashboard si está visible
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (currentFragment instanceof DashboardFragment) {
                        ((DashboardFragment) currentFragment).updatePendingDriversCount(pendingDrivers.size());
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error obteniendo taxistas: " + error);
                runOnUiThread(() -> {
                    showToast("Error cargando taxistas: " + error);
                });
            }
        });
    }

    private void setupClickListeners() {
        ivProfile.setOnClickListener(v -> showProfileOptions());
    }

    private void setupInitialData() {
        updateUserInterface();
    }

    // 🔥 MÉTODO: Actualizar interfaz con datos del usuario
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
        Log.d(TAG, "Cargando fragment: " + tag + ", addToBackStack: " + addToBackStack);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);

        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }

        transaction.commit();

        // Actualizar referencia del fragment actual
        this.currentFragment = fragment;
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
        Log.d(TAG, "Navegando a AddHotelAdminFragment");
        AddHotelAdminFragment addHotelAdminFragment = new AddHotelAdminFragment();
        loadFragment(addHotelAdminFragment, "ADD_HOTEL_ADMIN", true);
    }

    public void handleQuickAccessClick(String action) {
        Log.d(TAG, "Handling action: " + action);
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
        Log.d(TAG, "Navegando de vuelta al Dashboard");

        // Limpiar el back stack
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // Cargar el dashboard
        DashboardFragment dashboardFragment = new DashboardFragment();
        loadFragment(dashboardFragment, "DASHBOARD", false);
    }

    public void navigateBackToDashboardWithRefresh() {
        Log.d(TAG, "Navegando de vuelta al Dashboard con refresh forzado");

        // Limpiar el back stack
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // Crear nuevo dashboard y forzar recarga de datos
        DashboardFragment dashboardFragment = new DashboardFragment();
        loadFragment(dashboardFragment, "DASHBOARD", false);

        // 🔥 FORZAR REFRESH después de un delay adicional
        new Handler().postDelayed(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("DASHBOARD");
            if (currentFragment instanceof DashboardFragment) {
                ((DashboardFragment) currentFragment).forceDataRefresh();
            }
        }, 500);
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

    // 🔥 MÉTODO: Mostrar perfil del usuario
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

    // 🔥 MÉTODO: Logout del usuario
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

    // 🔥 MÉTODO: Mostrar confirmación de salida
    private void showExitConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Salir")
                .setMessage("¿Estás seguro que deseas salir?")
                .setPositiveButton("SÍ", (dialog, which) -> {
                    Log.d(TAG, "Usuario confirmó salida");
                    finishAndLogout();
                })
                .setNegativeButton("NO", (dialog, which) -> {
                    Log.d(TAG, "Usuario canceló salida");
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }

    private void finishAndLogout() {
        // Aquí puedes agregar lógica para limpiar la sesión si es necesario
        // Por ejemplo: SharedPreferences, Firebase signOut, etc.
        finish(); // Cerrar la actividad
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCurrentFragment();
        // 🔥 RECARGAR datos de taxistas
        loadPendingDrivers();
    }

    private void refreshCurrentFragment() {
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof DashboardFragment) {
            // Refrescar dashboard si es necesario
        }
    }

    // 🔥 MÉTODO onBackPressed() REMOVIDO - Ahora se maneja con OnBackPressedCallback
    /*
    @Override
    public void onBackPressed() {
        // Este método ya no es necesario - se maneja con OnBackPressedCallback
        // La lógica se movió a handleCustomBackPress()
    }
    */
}