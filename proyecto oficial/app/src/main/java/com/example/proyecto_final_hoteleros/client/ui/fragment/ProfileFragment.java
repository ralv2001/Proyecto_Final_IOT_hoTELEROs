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
        UserDataManager userManager = UserDataManager.getInstance();

        return new ClientProfile(
                userManager.getUserId() != null ? userManager.getUserId() : "client001",
                userManager.getUserFullName() != null ? userManager.getUserFullName() : "Cliente Usuario",
                userManager.getUserEmail() != null ? userManager.getUserEmail() : "cliente@email.com",
                "+51 987 654 321",
                "https://via.placeholder.com/150", // URL de imagen por defecto
                "Av. Lima 123, San Miguel, Lima",
                true,
                15, // total reservas
                12, // estancias completadas
                4.8f, // rating promedio
                2580.50 // total gastado
        );
    }

    private List<ClientProfileMenuItem> generateMenuItems() {
        List<ClientProfileMenuItem> menuItems = new ArrayList<>();

        menuItems.add(new ClientProfileMenuItem(
                "Mis Reservas",
                R.drawable.ic_hotel,
                "Ver reservas actuales y pasadas",
                true,
                ClientProfileMenuItem.ProfileMenuType.MENU_ITEM
        ));

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
            case "Mis Reservas":
                handleMisReservas();
                break;
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
        Bundle args = UserDataManager.getInstance().getUserBundle();
        String message = "Editar perfil de: " + UserDataManager.getInstance().getUserFullName();
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        // TODO: Implementar navegación a fragmento de editar perfil
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
        try {
            Log.d(TAG, "Iniciando proceso de cierre de sesión...");

            FirebaseAuth.getInstance().signOut();
            Log.d(TAG, "Sesión de Firebase cerrada exitosamente");

            Toast.makeText(requireContext(), "Cerrando sesión...", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            requireActivity().finish();
            Log.d(TAG, "Navegación a MainActivity completada");

        } catch (Exception e) {
            Log.e(TAG, "Error al cerrar sesión: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error al cerrar sesión", Toast.LENGTH_SHORT).show();
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