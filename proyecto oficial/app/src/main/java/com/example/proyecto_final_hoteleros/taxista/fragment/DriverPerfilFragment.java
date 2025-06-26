package com.example.proyecto_final_hoteleros.taxista.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.taxista.adapters.DriverProfileAdapter;
import com.example.proyecto_final_hoteleros.taxista.model.DriverProfile;
import com.example.proyecto_final_hoteleros.taxista.model.ProfileMenuItem;
import com.example.proyecto_final_hoteleros.taxista.utils.DriverPreferenceManager;
import com.example.proyecto_final_hoteleros.taxista.utils.NotificationHelper;

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
    private DriverPreferenceManager preferenceManager;
    private NotificationHelper notificationHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.taxi_fragment_driver_perfil, container, false);

        recyclerProfile = view.findViewById(R.id.recycler_profile);

        // Inicializar managers
        preferenceManager = new DriverPreferenceManager(requireContext());
        notificationHelper = new NotificationHelper(requireContext());

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
        // 🔥 OBTENER DATOS REALES DEL USUARIO LOGUEADO
        if (getActivity() instanceof com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity) {
            com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity activity =
                    (com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity) getActivity();

            String userId = activity.getUserId();
            String userEmail = activity.getUserEmail();
            String userName = activity.getUserName();

            Log.d(TAG, "=== CARGANDO PERFIL REAL ===");
            Log.d(TAG, "UserId: " + userId);
            Log.d(TAG, "Email: " + userEmail);
            Log.d(TAG, "Name: " + userName);

            // 🔥 CARGAR DATOS REALES DESDE FIREBASE
            if (userId != null && !userId.isEmpty()) {
                loadRealDriverProfile(userId);
            }
        }

        // 🔥 MIENTRAS SE CARGAN LOS DATOS REALES, MOSTRAR DATOS BÁSICOS
        return createTemporaryProfile();
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

        EditDriverProfileFragment editFragment = new EditDriverProfileFragment();

        // Pasar el perfil actual como argumento
        Bundle args = new Bundle();
        args.putParcelable("driver_profile", currentDriver);
        editFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editFragment)
                .addToBackStack(null)
                .commit();
    }

    private void handleHistorial() {
        Log.d(TAG, "Historial clicked");

        try {
            DriverHistorialFragment historialFragment = new DriverHistorialFragment();

            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, historialFragment)
                        .addToBackStack("historial")
                        .commit();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navegando al historial: " + e.getMessage(), e);
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), "Error al abrir historial", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleMetodosPago() {
        Log.d(TAG, "Métodos de Pago clicked");

        PaymentMethodsFragment paymentFragment = new PaymentMethodsFragment();

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, paymentFragment)
                .addToBackStack("payment_methods")
                .commit();
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
        Log.d(TAG, "🚪 Iniciando proceso de cierre de sesión...");

        // Mostrar indicador de progreso
        androidx.appcompat.app.AlertDialog progressDialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Cerrando Sesión")
                .setMessage("Cerrando sesión, por favor espera...")
                .setCancelable(false)
                .create();

        progressDialog.show();

        // 🔥 LIMPIAR DATOS LOCALES PRIMERO
        clearLocalUserData();

        // 🔥 CERRAR SESIÓN EN FIREBASE
        com.example.proyecto_final_hoteleros.utils.FirebaseManager firebaseManager =
                com.example.proyecto_final_hoteleros.utils.FirebaseManager.getInstance();

        try {
            // Cerrar sesión en Firebase Auth
            firebaseManager.signOut();
            Log.d(TAG, "✅ Sesión cerrada en Firebase Auth");

            // Simular pequeña espera para mejor UX
            new android.os.Handler().postDelayed(() -> {
                if (isAdded() && getActivity() != null) {
                    progressDialog.dismiss();

                    // 🔥 NAVEGAR A PANTALLA DE LOGIN
                    navigateToLogin();

                    Log.d(TAG, "✅ Logout completado exitosamente");
                }
            }, 1500);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error durante logout: " + e.getMessage());

            if (isAdded() && getActivity() != null) {
                progressDialog.dismiss();

                // Mostrar error pero continuar con logout local
                Toast.makeText(getContext(),
                        "Sesión cerrada localmente",
                        Toast.LENGTH_SHORT).show();

                navigateToLogin();
            }
        }
    }

    // 🔥 LIMPIAR TODOS LOS DATOS LOCALES
    private void clearLocalUserData() {
        Log.d(TAG, "🧹 Limpiando datos locales...");

        try {
            // Limpiar preferencias del conductor
            if (preferenceManager != null) {
                preferenceManager.clearAllData();
                Log.d(TAG, "✅ Datos de DriverPreferenceManager limpiados");
            }

            // Limpiar SharedPreferences generales
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("UserData", android.content.Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
            Log.d(TAG, "✅ SharedPreferences de UserData limpiados");

            // Limpiar cualquier otro dato de sesión que puedas tener
            android.content.SharedPreferences authPrefs = requireContext().getSharedPreferences("auth_data", android.content.Context.MODE_PRIVATE);
            authPrefs.edit().clear().apply();
            Log.d(TAG, "✅ Datos de autenticación limpiados");

            // Ocultar notificaciones persistentes
            if (notificationHelper != null) {
                notificationHelper.hideOnlineStatusNotification();
                Log.d(TAG, "✅ Notificaciones persistentes ocultadas");
            }

            // Actualizar estado del conductor a offline
            if (preferenceManager != null) {
                preferenceManager.setDriverAvailable(false);
                preferenceManager.setDriverStatus("Fuera de servicio");
            }

        } catch (Exception e) {
            Log.e(TAG, "⚠️ Error limpiando algunos datos locales: " + e.getMessage());
            // Continuar de todos modos
        }
    }

    // 🔥 NAVEGAR A PANTALLA DE LOGIN
    // 🔥 NAVEGAR A PANTALLA DE LOGIN (CORREGIDO)
    private void navigateToLogin() {
        Log.d(TAG, "🔄 Navegando a pantalla de login...");

        try {
            // Crear intent para ir a AuthActivity (RUTA CORRECTA)
            android.content.Intent intent = new android.content.Intent(getActivity(),
                    com.example.proyecto_final_hoteleros.AuthActivity.class);

            // Configurar intent para mostrar pestaña de login
            intent.putExtra("mode", "login");

            // Limpiar stack de actividades para que no pueda regresar
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK |
                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Iniciar AuthActivity
            startActivity(intent);

            // Finalizar DriverActivity actual
            if (getActivity() != null) {
                getActivity().finish();
            }

            Log.d(TAG, "✅ Navegación a login completada");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error navegando a login: " + e.getMessage());

            // Fallback: cerrar la aplicación
            if (getActivity() != null) {
                getActivity().finishAffinity();
            }
        }
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

    // 🔥 MÉTODO PARA CARGAR DATOS REALES DESDE FIREBASE
    private void loadRealDriverProfile(String userId) {
        Log.d(TAG, "🔄 Cargando datos reales desde Firebase para userId: " + userId);

        com.example.proyecto_final_hoteleros.utils.FirebaseManager firebaseManager =
                com.example.proyecto_final_hoteleros.utils.FirebaseManager.getInstance();

        // 🔥 USAR EL MÉTODO CORRECTO QUE SÍ EXISTE EN FIREBASEMANAGER
        firebaseManager.getUserDataFromAnyCollection(userId, new com.example.proyecto_final_hoteleros.utils.FirebaseManager.UserCallback() {
            @Override
            public void onUserFound(com.example.proyecto_final_hoteleros.models.UserModel user) {
                Log.d(TAG, "✅ Datos del usuario obtenidos desde Firebase");
                Log.d(TAG, "Nombre: " + user.getFullName());
                Log.d(TAG, "Email: " + user.getEmail());
                Log.d(TAG, "Placa: " + user.getPlacaVehiculo());
                Log.d(TAG, "Foto: " + user.getPhotoUrl());

                // 🔥 CONVERTIR UserModel → DriverProfile
                DriverProfile realProfile = convertUserModelToDriverProfile(user);

                // 🔥 ACTUALIZAR LA VISTA CON DATOS REALES
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateProfileWithRealData(realProfile);
                    });
                }
            }

            @Override
            public void onUserNotFound() {
                Log.w(TAG, "⚠️ Usuario no encontrado en Firebase");
                if (getContext() != null && isAdded()) {
                    Toast.makeText(getContext(), "No se pudieron cargar los datos del perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando datos del usuario: " + error);
                if (getContext() != null && isAdded()) {
                    Toast.makeText(getContext(), "Error cargando perfil: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 🔥 CREAR PERFIL TEMPORAL MIENTRAS SE CARGAN LOS DATOS REALES
    private DriverProfile createTemporaryProfile() {
        if (getActivity() instanceof com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity) {
            com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity activity =
                    (com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity) getActivity();

            String userName = activity.getUserName();
            String userEmail = activity.getUserEmail();

            return new DriverProfile(
                    "loading", // driverId
                    userName != null ? userName : "Cargando...", // fullName
                    userEmail != null ? userEmail : "Cargando...", // email
                    "Cargando...", // phoneNumber
                    null, // profileImageUrl
                    "Cargando...", // address
                    "Cargando...", // licenseNumber
                    true, // isActive
                    true, // isAvailable
                    4.5f, // averageRating
                    0, // totalTrips
                    0, // completedTrips
                    0.0 // monthlyEarnings
            );
        }

        // Fallback profile
        return new DriverProfile(
                "temp", "Conductor", "conductor@email.com", "999999999",
                null, "Lima, Perú", "ABC123",
                true, true, 4.5f, 0, 0, 0.0
        );
    }

    // 🔥 CONVERTIR UserModel A DriverProfile
    private DriverProfile convertUserModelToDriverProfile(com.example.proyecto_final_hoteleros.models.UserModel user) {
        return new DriverProfile(
                user.getUserId(),
                user.getFullName(), // nombres + apellidos
                user.getEmail(),
                user.getTelefono() != null ? user.getTelefono() : "No especificado",
                user.getPhotoUrl(), // URL de la foto
                user.getDireccion() != null ? user.getDireccion() : "No especificado",
                user.getPlacaVehiculo() != null ? user.getPlacaVehiculo() : "No especificado",
                user.isActive(),
                true, // Por defecto disponible
                4.5f, // Rating por defecto
                0, // Viajes totales (se puede calcular después)
                0, // Viajes completados (se puede calcular después)
                0.0 // Ganancias (se puede calcular después)
        );
    }

    // 🔥 ACTUALIZAR LA VISTA CON DATOS REALES
    private void updateProfileWithRealData(DriverProfile realProfile) {
        Log.d(TAG, "🔄 Actualizando vista con datos reales");

        // Reemplazar el perfil temporal con el real
        if (profileItems != null && !profileItems.isEmpty()) {
            profileItems.set(0, realProfile); // El primer item es el header con el perfil
            currentDriver = realProfile;

            // Notificar al adapter que se actualizó el primer item
            if (adapter != null) {
                adapter.notifyItemChanged(0);
            }

            Log.d(TAG, "✅ Vista actualizada con datos reales");
        }
    }
}