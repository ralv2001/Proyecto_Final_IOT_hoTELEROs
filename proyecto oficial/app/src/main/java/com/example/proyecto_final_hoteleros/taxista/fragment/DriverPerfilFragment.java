package com.example.proyecto_final_hoteleros.taxista.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adapters.DriverProfileAdapter;
import com.example.proyecto_final_hoteleros.taxista.model.DriverProfile;
import com.example.proyecto_final_hoteleros.taxista.model.ProfileMenuItem;

import java.util.ArrayList;
import java.util.List;

public class DriverPerfilFragment extends Fragment implements
        DriverProfileAdapter.OnProfileItemClickListener,
        DriverProfileAdapter.OnProfileCloseListener {

    private static final String TAG = "DriverPerfilFragment";

    private RecyclerView recyclerProfile;
    private DriverProfileAdapter adapter;
    private List<Object> profileItems;
    private DriverProfile currentDriver;

    public DriverPerfilFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_perfil, container, false);

        recyclerProfile = view.findViewById(R.id.recycler_profile);

        setupRecyclerView();
        loadProfileData();

        return view;
    }

    private void setupRecyclerView() {
        profileItems = new ArrayList<>();
        adapter = new DriverProfileAdapter(getContext(), profileItems, this);
        recyclerProfile.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerProfile.setAdapter(adapter);
    }

    private void loadProfileData() {
        // Generar datos de ejemplo del conductor
        currentDriver = generateDriverProfile();

        // Agregar header (perfil del conductor con estadísticas integradas)
        profileItems.add(currentDriver);

        // Agregar items del menú
        profileItems.addAll(generateMenuItems());

        // Notificar al adapter
        adapter.notifyDataSetChanged();
    }

    private DriverProfile generateDriverProfile() {
        return new DriverProfile(
                "driver001",
                "Renato Delgado Aquino",
                "renato.delgado@email.com",
                "+51 987 654 321",
                "https://png.pngtree.com/png-clipart/20241214/original/pngtree-cat-in-a-suit-and-shirt-png-image_17854633.png",
                "Av. Lima 123, San Miguel, Lima",
                "L12345678",
                true,
                true,
                4.8f,
                127,
                124,
                2450.50
        );
    }

    private List<ProfileMenuItem> generateMenuItems() {
        List<ProfileMenuItem> menuItems = new ArrayList<>();

        menuItems.add(new ProfileMenuItem(
                "Hoteles Disponibles",
                R.drawable.ic_hotel,
                "15 hoteles cercanos",
                true,
                ProfileMenuItem.ProfileMenuType.MENU_ITEM
        ));

        menuItems.add(new ProfileMenuItem(
                "Editar Perfil",
                R.drawable.ic_profile,
                "Actualizar información personal",
                true,
                ProfileMenuItem.ProfileMenuType.MENU_ITEM
        ));

        menuItems.add(new ProfileMenuItem(
                "Historial",
                R.drawable.ic_historial,
                "Ver viajes anteriores",
                true,
                ProfileMenuItem.ProfileMenuType.MENU_ITEM
        ));

        menuItems.add(new ProfileMenuItem(
                "Métodos de pago",
                R.drawable.ic_payment,
                "Gestionar pagos",
                true,
                ProfileMenuItem.ProfileMenuType.MENU_ITEM
        ));

        menuItems.add(new ProfileMenuItem(
                "Notificaciones",
                R.drawable.ic_notification,
                "Configurar alertas",
                true,
                ProfileMenuItem.ProfileMenuType.MENU_ITEM
        ));

        menuItems.add(new ProfileMenuItem(
                "Cerrar Sesión",
                R.drawable.ic_logout,
                "",
                true,
                ProfileMenuItem.ProfileMenuType.MENU_ITEM
        ));

        return menuItems;
    }

    @Override
    public void onMenuItemClick(ProfileMenuItem item) {
        Log.d(TAG, "Menu item clicked: " + item.getTitle());

        switch (item.getTitle()) {
            case "Hoteles Disponibles":
                handleHotelesDisponibles();
                break;
            case "Editar Perfil":
                handleEditarPerfil();
                break;
            case "Historial":
                handleHistorial();
                break;
            case "Métodos de pago":
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

    @Override
    public void onAvailabilityToggle(boolean isAvailable) {
        Log.d(TAG, "Availability toggled: " + isAvailable);

        // Actualizar el estado en el modelo
        if (currentDriver != null) {
            currentDriver.setAvailable(isAvailable);
        }

        // Mostrar mensaje al usuario
        String message = isAvailable ? "Ahora estás disponible para viajes" : "Has pausado la recepción de viajes";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCloseClick() {
        Log.d(TAG, "Close button clicked");
        // Cerrar el fragmento - regresar a la pantalla anterior
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private void handleHotelesDisponibles() {
        Log.d(TAG, "Hoteles Disponibles clicked");

        // Navegar al fragmento de hoteles disponibles
        AvailableHotelsFragment hotelsFragment = new AvailableHotelsFragment();

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, hotelsFragment)
                .addToBackStack(null)
                .commit();
    }

    private void handleEditarPerfil() {
        Log.d(TAG, "Editar Perfil clicked");

        // Crear el fragmento de editar perfil
        EditDriverProfileFragment editFragment = new EditDriverProfileFragment();

        // Pasar el perfil actual como argumento (opcional pero recomendado)
        Bundle args = new Bundle();
        args.putParcelable("driver_profile", currentDriver);
        editFragment.setArguments(args);

        // Navegar al fragmento de editar perfil
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editFragment) // Cambia este ID por el correcto de tu layout
                .addToBackStack(null)
                .commit();
    }

    private void handleHistorial() {
        Log.d(TAG, "Historial clicked");
        Toast.makeText(getContext(), "Ver historial de viajes...", Toast.LENGTH_SHORT).show();

        // Navegar al fragmento de historial que ya tienes (DriverHistorialFragment)
        // getParentFragmentManager().beginTransaction()
        //     .replace(R.id.fragment_container, new DriverHistorialFragment())
        //     .addToBackStack(null)
        //     .commit();
    }

    private void handleMetodosPago() {
        Log.d(TAG, "Métodos de Pago clicked");
        Toast.makeText(getContext(), "Gestionar métodos de pago...", Toast.LENGTH_SHORT).show();

        // TODO: Implementar navegación a fragmento de métodos de pago
    }

    private void handleNotificaciones() {
        Log.d(TAG, "Notificaciones clicked");
        Toast.makeText(getContext(), "Configurar notificaciones...", Toast.LENGTH_SHORT).show();

        // TODO: Implementar navegación a fragmento de configuración de notificaciones
    }

    private void handleCerrarSesion() {
        Log.d(TAG, "Cerrar Sesión clicked");

        // Mostrar diálogo de confirmación con estilo moderno
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Cerrar Sesión")
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
        Toast.makeText(getContext(), "Cerrando sesión...", Toast.LENGTH_SHORT).show();

        // TODO: Implementar la lógica de logout:
        // 1. Limpiar datos del usuario guardados (SharedPreferences, etc.)
        // 2. Cerrar sesión en Firebase/backend
        // 3. Actualizar estado del conductor a "offline"
        // 4. Redirigir a la pantalla de login

        // Ejemplo de redirección (ajustar según tu estructura):
        // Intent intent = new Intent(getActivity(), LoginActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // startActivity(intent);
        // getActivity().finish();

        Log.d(TAG, "Logout completado");
    }

    // Método público para actualizar datos del perfil desde otras partes de la app
    public void updateProfileData(String driverName, String vehicleInfo, String profileImageUrl) {
        if (currentDriver != null) {
            currentDriver.setFullName(driverName);
            currentDriver.setProfileImageUrl(profileImageUrl);
            // Actualizar el primer item (header) del RecyclerView
            adapter.notifyItemChanged(0);
        }
    }

    // Método para actualizar el estado en línea/fuera de línea
    public void setOnlineStatus(boolean isOnline) {
        if (currentDriver != null) {
            currentDriver.setAvailable(isOnline);
            // Actualizar el primer item (header) del RecyclerView
            adapter.notifyItemChanged(0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed - actualizando datos si es necesario");
        // Aquí puedes actualizar datos si es necesario
        // Por ejemplo, refrescar estadísticas o estado del conductor
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