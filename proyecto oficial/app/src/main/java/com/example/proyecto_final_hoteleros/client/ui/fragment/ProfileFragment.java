package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.MainActivity;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.adapters.ClientProfileAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.ClientProfile;
import com.example.proyecto_final_hoteleros.client.data.model.ClientProfileMenuItem;
import com.example.proyecto_final_hoteleros.client.navigation.NavigationManager;
import com.example.proyecto_final_hoteleros.client.utils.UserDataManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends BaseBottomNavigationFragment implements
        ClientProfileAdapter.OnClientProfileItemClickListener {

    private static final String TAG = "ProfileFragment";

    private RecyclerView recyclerProfile;
    private ClientProfileAdapter adapter;
    private List<Object> profileItems;
    private ClientProfile currentClient;

    @Override
    protected NavigationTab getCurrentTab() {
        return NavigationTab.PROFILE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.client_fragment_profile, container, false);

        recyclerProfile = view.findViewById(R.id.recycler_profile);

        setupRecyclerView();
        loadProfileData();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener datos del usuario y guardarlos en el manager si vienen en argumentos
        if (getArguments() != null) {
            String userId = getArguments().getString("user_id");
            String userName = getArguments().getString("user_name");
            String userFullName = getArguments().getString("user_full_name");
            String userEmail = getArguments().getString("user_email");
            String userType = getArguments().getString("user_type");

            UserDataManager.getInstance().setUserData(userId, userName, userFullName, userEmail, userType);

            Log.d(TAG, "=== DATOS RECIBIDOS EN PROFILEFRAGMENT ===");
            Log.d(TAG, "User ID: " + userId);
            Log.d(TAG, "User Name: " + userName);
        }
    }

    private void setupRecyclerView() {
        profileItems = new ArrayList<>();
        adapter = new ClientProfileAdapter(getContext(), profileItems, this);
        recyclerProfile.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerProfile.setAdapter(adapter);
    }

    private void loadProfileData() {
        // Generar datos del cliente
        currentClient = generateClientProfile();

        // Agregar header (perfil del cliente con estadísticas integradas)
        profileItems.add(currentClient);

        // Agregar items del menú
        profileItems.addAll(generateMenuItems());

        // Notificar al adapter
        adapter.notifyDataSetChanged();
    }

    private ClientProfile generateClientProfile() {
        // 🔥 OBTENER DATOS REALES DEL USUARIO LOGUEADO
        if (getActivity() instanceof com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity) {
            com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity activity =
                    (com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity) getActivity();

            String userId = activity.getUserId();
            String userEmail = activity.getUserEmail();
            String userFullName = activity.getUserFullName();

            Log.d(TAG, "=== CARGANDO PERFIL REAL DEL CLIENTE ===");
            Log.d(TAG, "UserId: " + userId);
            Log.d(TAG, "Email: " + userEmail);
            Log.d(TAG, "Name: " + userFullName);

            // 🔥 CARGAR DATOS REALES DESDE FIREBASE
            if (userId != null && !userId.isEmpty()) {
                loadRealClientProfile(userId);
            }
        }

        // 🔥 MIENTRAS SE CARGAN LOS DATOS REALES, MOSTRAR DATOS BÁSICOS
        return createTemporaryProfile();
    }

    // 🔥 MÉTODO PARA CARGAR DATOS REALES DESDE FIREBASE
    private void loadRealClientProfile(String userId) {
        Log.d(TAG, "🔄 Cargando datos reales desde Firebase para userId: " + userId);

        com.example.proyecto_final_hoteleros.utils.FirebaseManager firebaseManager =
                com.example.proyecto_final_hoteleros.utils.FirebaseManager.getInstance();

        firebaseManager.getUserDataFromAnyCollection(userId, new com.example.proyecto_final_hoteleros.utils.FirebaseManager.UserCallback() {
            @Override
            public void onUserFound(com.example.proyecto_final_hoteleros.models.UserModel user) {
                Log.d(TAG, "✅ Datos del cliente obtenidos desde Firebase");
                Log.d(TAG, "Nombre: " + user.getFullName());
                Log.d(TAG, "Email: " + user.getEmail());
                Log.d(TAG, "Teléfono: " + user.getTelefono());
                Log.d(TAG, "Foto: " + user.getPhotoUrl());

                // 🔥 CONVERTIR UserModel → ClientProfile
                ClientProfile realProfile = convertUserModelToClientProfile(user);

                // 🔥 ACTUALIZAR LA VISTA CON DATOS REALES
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateProfileWithRealData(realProfile);
                    });
                }
            }

            @Override
            public void onUserNotFound() {
                Log.w(TAG, "⚠️ Cliente no encontrado en Firebase");
                if (getContext() != null && isAdded()) {
                    Toast.makeText(getContext(), "No se pudieron cargar los datos del perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando datos del cliente: " + error);
                if (getContext() != null && isAdded()) {
                    Toast.makeText(getContext(), "Error cargando perfil: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 🔥 CREAR PERFIL TEMPORAL MIENTRAS SE CARGAN LOS DATOS REALES
    private ClientProfile createTemporaryProfile() {
        if (getActivity() instanceof com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity) {
            com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity activity =
                    (com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity) getActivity();

            String userFullName = activity.getUserFullName();
            String userEmail = activity.getUserEmail();
            String userId = activity.getUserId();

            return new ClientProfile(
                    userId != null ? userId : "loading", // clientId
                    userFullName != null ? userFullName : "Cargando...", // fullName
                    userEmail != null ? userEmail : "Cargando...", // email
                    "Cargando...", // phoneNumber
                    null, // profileImageUrl
                    "Cargando...", // address
                    true, // isActive
                    0, // totalReservations
                    0, // completedStays
                    4.5f, // averageRating
                    0.0 // totalSpent
            );
        }

        // Fallback profile
        return new ClientProfile(
                "temp", "Cliente", "cliente@email.com", "999999999",
                null, "Lima, Perú", true, 0, 0, 4.5f, 0.0
        );
    }

    // 🔥 CONVERTIR UserModel A ClientProfile
    private ClientProfile convertUserModelToClientProfile(com.example.proyecto_final_hoteleros.models.UserModel user) {
        return new ClientProfile(
                user.getUserId(),
                user.getFullName(), // nombres + apellidos
                user.getEmail(),
                user.getTelefono() != null ? user.getTelefono() : "No especificado",
                user.getPhotoUrl(), // URL de la foto
                user.getDireccion() != null ? user.getDireccion() : "No especificado",
                user.isActive(),
                0, // Reservas totales (se puede calcular después)
                0, // Estancias completadas (se puede calcular después)
                4.5f, // Rating por defecto
                0.0 // Total gastado (se puede calcular después)
        );
    }

    // 🔥 ACTUALIZAR LA VISTA CON DATOS REALES
    private void updateProfileWithRealData(ClientProfile realProfile) {
        Log.d(TAG, "🔄 Actualizando vista con datos reales del cliente");

        // Reemplazar el perfil temporal con el real
        if (profileItems != null && !profileItems.isEmpty()) {
            profileItems.set(0, realProfile); // El primer item es el header con el perfil
            currentClient = realProfile;

            // Notificar al adapter que se actualizó el primer item
            if (adapter != null) {
                adapter.notifyItemChanged(0);
            }

            Log.d(TAG, "✅ Vista del cliente actualizada con datos reales");
        }
    }

    private List<ClientProfileMenuItem> generateMenuItems() {
        List<ClientProfileMenuItem> menuItems = new ArrayList<>();

        menuItems.add(new ClientProfileMenuItem(
                "Editar Perfil",
                R.drawable.ic_group,
                "Actualizar información personal",
                true,
                ClientProfileMenuItem.ProfileMenuType.MENU_ITEM
        ));

        menuItems.add(new ClientProfileMenuItem(
                "Métodos de Pago",
                R.drawable.ic_payment,
                "Gestionar tarjetas y pagos",
                true,
                ClientProfileMenuItem.ProfileMenuType.MENU_ITEM
        ));

        menuItems.add(new ClientProfileMenuItem(
                "Notificaciones",
                R.drawable.ic_notification,
                "Configurar alertas",
                true,
                ClientProfileMenuItem.ProfileMenuType.MENU_ITEM
        ));

        menuItems.add(new ClientProfileMenuItem(
                "Cerrar Sesión",
                R.drawable.ic_logout,
                "",
                true,
                ClientProfileMenuItem.ProfileMenuType.MENU_ITEM
        ));

        return menuItems;
    }

    @Override
    public void onMenuItemClick(ClientProfileMenuItem item) {
        Log.d(TAG, "Menu item clicked: " + item.getTitle());

        switch (item.getTitle()) {
            case "Editar Perfil":
                handleEditarPerfil();
                break;
            case "Métodos de Pago":
                handleMetodosPago();
                break;
            case "Notificaciones":
                handleNotificaciones();
                break;
            case "Cerrar Sesión":
                handleCerrarSesion();
                break;
            default:
                Toast.makeText(getContext(), "Función no implementada: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleMisReservas() {
        Log.d(TAG, "Mis Reservas clicked");
        Toast.makeText(getContext(), "Mis Reservas (próximamente)", Toast.LENGTH_SHORT).show();
        // TODO: Implementar navegación a fragmento de reservas
    }

    private void handleEditarPerfil() {
        Log.d(TAG, "Editar Perfil clicked");
        NavigationManager.getInstance().navigateToEditProfile(currentClient);
    }

    private void handleMetodosPago() {
        Log.d(TAG, "Métodos de Pago clicked");
        Toast.makeText(getContext(), "Métodos de Pago (próximamente)", Toast.LENGTH_SHORT).show();
        // TODO: Implementar navegación a fragmento de métodos de pago
    }

    private void handleNotificaciones() {
        Log.d(TAG, "Notificaciones clicked");
        NavigationManager.getInstance().navigateToNotificationSettings(
                UserDataManager.getInstance().getUserBundle()
        );
    }

    private void handleCerrarSesion() {
        Log.d(TAG, "Cerrar Sesión clicked");

        // Mostrar diálogo de confirmación
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setIcon(R.drawable.ic_logout)
                .setPositiveButton("Sí, cerrar sesión", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void performLogout() {
        Log.d(TAG, "🚪 Iniciando proceso de cierre de sesión del cliente...");

        // Mostrar indicador de progreso
        androidx.appcompat.app.AlertDialog progressDialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Cerrando Sesión")
                .setMessage("Cerrando sesión, por favor espera...")
                .setCancelable(false)
                .create();

        progressDialog.show();

        // 🔥 LIMPIAR DATOS LOCALES DEL CLIENTE
        clearLocalClientData();

        // 🔥 CERRAR SESIÓN EN FIREBASE
        com.example.proyecto_final_hoteleros.utils.FirebaseManager firebaseManager =
                com.example.proyecto_final_hoteleros.utils.FirebaseManager.getInstance();

        try {
            firebaseManager.signOut();
            Log.d(TAG, "✅ Sesión cerrada en Firebase Auth");

            // Simular pequeña espera para mejor UX
            new android.os.Handler().postDelayed(() -> {
                if (isAdded() && getActivity() != null) {
                    progressDialog.dismiss();

                    // 🔥 NAVEGAR A PANTALLA DE LOGIN
                    navigateToLogin();

                    Log.d(TAG, "✅ Logout del cliente completado exitosamente");
                }
            }, 1500);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error durante logout del cliente: " + e.getMessage());

            if (isAdded() && getActivity() != null) {
                progressDialog.dismiss();
                navigateToLogin();
            }
        }
    }

    // 🔥 LIMPIAR TODOS LOS DATOS LOCALES DEL CLIENTE
    private void clearLocalClientData() {
        Log.d(TAG, "🧹 Limpiando datos locales del cliente...");

        try {
            // Limpiar UserDataManager
            UserDataManager.getInstance().clearUserData();
            Log.d(TAG, "✅ UserDataManager limpiado");

            // Limpiar SharedPreferences del cliente
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ClientData", android.content.Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
            Log.d(TAG, "✅ SharedPreferences de ClientData limpiados");

            // Limpiar datos de autenticación
            android.content.SharedPreferences authPrefs = requireContext().getSharedPreferences("auth_data", android.content.Context.MODE_PRIVATE);
            authPrefs.edit().clear().apply();
            Log.d(TAG, "✅ Datos de autenticación limpiados");

        } catch (Exception e) {
            Log.e(TAG, "⚠️ Error limpiando algunos datos locales del cliente: " + e.getMessage());
        }
    }

    // 🔥 NAVEGAR A PANTALLA DE LOGIN
    private void navigateToLogin() {
        Log.d(TAG, "🔄 Navegando a pantalla de login...");

        try {
            android.content.Intent intent = new android.content.Intent(getActivity(),
                    com.example.proyecto_final_hoteleros.AuthActivity.class);

            intent.putExtra("mode", "login");
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK |
                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            if (getActivity() != null) {
                getActivity().finish();
            }

            Log.d(TAG, "✅ Navegación a login completada");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error navegando a login: " + e.getMessage());

            if (getActivity() != null) {
                getActivity().finishAffinity();
            }
        }
    }

    // Métodos públicos para acceder a los datos del usuario
    public String getUserName() {
        return UserDataManager.getInstance().getUserName();
    }

    public String getUserFullName() {
        return UserDataManager.getInstance().getUserFullName();
    }

    public String getUserId() {
        return UserDataManager.getInstance().getUserId();
    }

    public String getUserEmail() {
        return UserDataManager.getInstance().getUserEmail();
    }

    public String getUserType() {
        return UserDataManager.getInstance().getUserType();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed - actualizando datos si es necesario");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias para evitar memory leaks
        recyclerProfile = null;
        adapter = null;
        profileItems = null;
        Log.d(TAG, "Vista destruida y referencias limpiadas");
    }
}