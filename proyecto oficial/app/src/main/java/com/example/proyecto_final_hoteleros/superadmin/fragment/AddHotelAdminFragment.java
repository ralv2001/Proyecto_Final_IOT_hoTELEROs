package com.example.proyecto_final_hoteleros.superadmin.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.models.UserModel;
import com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity;
import com.example.proyecto_final_hoteleros.superadmin.adapters.HotelAdminFieldAdapter;
import com.example.proyecto_final_hoteleros.superadmin.models.HotelAdminField;
// import com.example.proyecto_final_hoteleros.utils.FirebaseManager; // 🔥 COMENTADO - Firebase
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddHotelAdminFragment extends Fragment {

    private static final String TAG = "AddHotelAdminFragment";

    private RecyclerView recyclerView;
    private MaterialButton btnCreateAdmin;
    private MaterialToolbar toolbar;
    private HotelAdminFieldAdapter adapter;
    private List<HotelAdminField> fieldsList;

    // 🔥 NUEVO: Handler como variable de clase para manejo seguro
    private Handler delayHandler;
    private Runnable pendingNavigation;
    private boolean isNavigationPending = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.superadmin_fragment_add_hotel_admin, container, false);

        initViews(view);
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadFormFields();

        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        recyclerView = view.findViewById(R.id.recycler_admin_fields);
        btnCreateAdmin = view.findViewById(R.id.btn_create_admin);
    }

    private void setupToolbar() {
        toolbar.setTitle("Registrar Admin de Hotel");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> {
            Log.d(TAG, "Usuario hizo click en back button");

            // Cancelar cualquier navegación pendiente
            cancelPendingNavigation();

            // Navegación segura hacia atrás
            if (getActivity() instanceof SuperAdminActivity) {
                ((SuperAdminActivity) getActivity()).navigateBackToDashboard();
            } else {
                emergencyNavigation();
            }
        });
    }

    private void setupRecyclerView() {
        fieldsList = new ArrayList<>();
        adapter = new HotelAdminFieldAdapter(fieldsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnCreateAdmin.setOnClickListener(v -> validateAndCreateAdmin());
    }

    private void loadFormFields() {
        fieldsList.clear();

        // Información del Administrador (SEPARADO: nombre y apellido)
        fieldsList.add(new HotelAdminField("admin_nombres", "Nombres del Administrador",
                "Ej: Juan Carlos", R.drawable.ic_profile, "text", true));
        fieldsList.add(new HotelAdminField("admin_apellidos", "Apellidos del Administrador",
                "Ej: Pérez García", R.drawable.ic_profile, "text", true));
        fieldsList.add(new HotelAdminField("admin_email", "Email del Administrador",
                "Ej: admin@hotel.com", R.drawable.ic_email, "email", true));
        fieldsList.add(new HotelAdminField("admin_password", "Contraseña",
                "Mínimo 6 caracteres", R.drawable.ic_lock, "password", true));
        fieldsList.add(new HotelAdminField("admin_confirm_password", "Confirmar Contraseña",
                "Repita la contraseña", R.drawable.ic_lock, "password", true));

        adapter.notifyDataSetChanged();
    }

    private void validateAndCreateAdmin() {
        Map<String, String> formData = adapter.getFormData();
        List<String> errors = new ArrayList<>();

        // Validar campos requeridos
        for (HotelAdminField field : fieldsList) {
            if (field.isRequired()) {
                String value = formData.get(field.getFieldId());
                if (value == null || value.trim().isEmpty()) {
                    errors.add("• " + field.getLabel() + " es requerido");
                }
            }
        }

        // Validar email
        String email = formData.get("admin_email");
        if (email != null && !email.isEmpty() && !isValidEmail(email)) {
            errors.add("• Email inválido");
        }

        // Validar contraseñas
        String password = formData.get("admin_password");
        String confirmPassword = formData.get("admin_confirm_password");

        if (password != null && password.length() < 6) {
            errors.add("• La contraseña debe tener al menos 6 caracteres");
        }

        if (!password.equals(confirmPassword)) {
            errors.add("• Las contraseñas no coinciden");
        }

        if (!errors.isEmpty()) {
            showValidationErrors(errors);
            return;
        }

        // Si todo está correcto, crear el administrador
        showCreateConfirmation(formData);
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showValidationErrors(List<String> errors) {
        StringBuilder message = new StringBuilder("Por favor corrige los siguientes errores:\n\n");
        for (String error : errors) {
            message.append(error).append("\n");
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Errores de Validación")
                .setMessage(message.toString())
                .setPositiveButton("Entendido", null)
                .setIcon(R.drawable.ic_exclamacioncampoerroneo)
                .show();
    }

    private void showCreateConfirmation(Map<String, String> formData) {
        String adminNombres = formData.get("admin_nombres");
        String adminApellidos = formData.get("admin_apellidos");
        String adminEmail = formData.get("admin_email");
        String fullName = adminNombres + " " + adminApellidos;

        String message = "¿Está seguro de crear el siguiente administrador?\n\n" +
                "👤 Administrador: " + fullName + "\n" +
                "📧 Email: " + adminEmail;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirmar Creación")
                .setMessage(message)
                .setPositiveButton("Crear", (dialog, which) -> createHotelAdmin(formData))
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_check)
                .show();
    }

    private void createHotelAdmin(Map<String, String> formData) {
        // Mostrar loading
        btnCreateAdmin.setEnabled(false);
        btnCreateAdmin.setText("Creando...");

        String email = formData.get("admin_email");
        String password = formData.get("admin_password");

        Log.d(TAG, "=== CREANDO ADMIN DE HOTEL EN FIREBASE ===");
        Log.d(TAG, "Email: " + email);
        Log.d(TAG, "Nombres: " + formData.get("admin_nombres"));
        Log.d(TAG, "Apellidos: " + formData.get("admin_apellidos"));

        // 🔥 CREAR EN FIREBASE AUTH
        FirebaseManager.getInstance().registerUser(email, password, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                Log.d(TAG, "✅ Usuario Auth creado: " + userId);

                // Crear modelo de usuario para admin de hotel
                UserModel adminUser = new UserModel();
                adminUser.setUserId(userId);
                adminUser.setNombres(formData.get("admin_nombres"));
                adminUser.setApellidos(formData.get("admin_apellidos"));
                adminUser.setEmail(email);
                adminUser.setUserType("hotel_admin");
                adminUser.setActive(true);
                adminUser.setCreatedAt(System.currentTimeMillis());

                // Guardar en Firestore
                FirebaseManager.getInstance().saveUserData(userId, adminUser, new FirebaseManager.DataCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Admin de hotel guardado en Firestore");

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showSuccessMessage(formData);
                                resetForm();
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "❌ Error guardando en Firestore: " + error);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showErrorMessage(error);
                                resetButtons();
                            });
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error creando usuario Auth: " + error);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showErrorMessage(error);
                        resetButtons();
                    });
                }
            }
        });
    }

    private void showSuccessMessage(Map<String, String> formData) {
        String adminNombres = formData.get("admin_nombres");
        String adminApellidos = formData.get("admin_apellidos");
        String fullName = adminNombres + " " + adminApellidos;

        Log.d(TAG, "🎉 Mostrando mensaje de éxito para: " + fullName);

        // Mostrar Snackbar de éxito
        Snackbar.make(requireView(),
                        "✅ Admin de hotel creado exitosamente",
                        Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.success_green))
                .setTextColor(getResources().getColor(android.R.color.white))
                .show();

        // Mostrar diálogo de éxito con detalles
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("¡Éxito!")
                .setMessage("El administrador " + fullName +
                        " ha sido creado exitosamente y guardado en Firebase.")
                .setPositiveButton("Continuar", (dialog, which) -> {
                    Log.d(TAG, "Usuario hizo click en Continuar");

                    if (getActivity() instanceof SuperAdminActivity) {
                        // 🔥 INCREMENTAR DELAY PARA MEJOR SINCRONIZACIÓN
                        new Handler().postDelayed(() -> {
                            // Verificaciones de seguridad
                            if (getActivity() != null && isAdded() && !isDetached()) {
                                ((SuperAdminActivity) getActivity()).navigateBackToDashboardWithRefresh();
                            }
                        }, 2000); // 🔥 INCREMENTAR A 2 SEGUNDOS
                    } else {
                        // Navegación inmediata como fallback
                        navigateBackSafely();
                    }
                })
                .setOnDismissListener(dialog -> {
                    // 🔥 CANCELAR cualquier navegación pendiente si el diálogo se cierra
                    cancelPendingNavigation();
                })
                .setIcon(R.drawable.ic_check)
                .setCancelable(false)
                .show();

        // Enviar notificación local
        sendLocalNotification("Nuevo Admin de Hotel",
                "Se ha registrado " + fullName);
    }
    // 🔥 NUEVO MÉTODO: Navegación segura con múltiples verificaciones
    private void navigateBackSafely() {
        Log.d(TAG, "=== INICIANDO NAVEGACIÓN SEGURA ===");

        // Verificación 1: Fragment está adjunto y activo
        if (!isAdded() || isDetached() || isRemoving()) {
            Log.w(TAG, "❌ Fragment no está en estado válido para navegar");
            return;
        }

        // Verificación 2: Activity existe y es del tipo correcto
        if (!(getActivity() instanceof SuperAdminActivity)) {
            Log.w(TAG, "❌ Activity no es SuperAdminActivity o es null");
            return;
        }

        // Verificación 3: Activity no está siendo destruida
        if (getActivity().isFinishing() || getActivity().isDestroyed()) {
            Log.w(TAG, "❌ Activity está siendo destruida");
            return;
        }

        SuperAdminActivity activity = (SuperAdminActivity) getActivity();

        // 🔥 OPCIÓN A: Navegación inmediata (recomendada)
        executeImmediateNavigation(activity);

        // 🔥 OPCIÓN B: Navegación con delay seguro (comentada)
        // executeDelayedNavigation(activity);
    }

    // 🔥 MÉTODO: Navegación inmediata
    private void executeImmediateNavigation(SuperAdminActivity activity) {
        Log.d(TAG, "✅ Ejecutando navegación inmediata");

        try {
            activity.navigateBackToDashboardWithRefresh();
            Log.d(TAG, "✅ Navegación exitosa");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error en navegación inmediata: " + e.getMessage());
            // Fallback: navegación normal
            try {
                activity.navigateBackToDashboard();
                Log.d(TAG, "✅ Fallback navegación exitosa");
            } catch (Exception fallbackError) {
                Log.e(TAG, "❌ Error en fallback navegación: " + fallbackError.getMessage());
            }
        }
    }

    // 🔥 MÉTODO: Navegación con delay seguro (alternativa)
    private void executeDelayedNavigation(SuperAdminActivity activity) {
        Log.d(TAG, "⏳ Ejecutando navegación con delay seguro");

        // Cancelar cualquier navegación pendiente
        cancelPendingNavigation();

        // Crear Handler y Runnable
        delayHandler = new Handler();
        pendingNavigation = () -> {
            Log.d(TAG, "🔄 Ejecutando navegación diferida...");

            // Verificaciones adicionales antes de ejecutar
            if (isAdded() && !isDetached() && !isRemoving() &&
                    getActivity() != null && !getActivity().isFinishing()) {

                try {
                    ((SuperAdminActivity) getActivity()).navigateBackToDashboardWithRefresh();
                    Log.d(TAG, "✅ Navegación diferida exitosa");
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error en navegación diferida: " + e.getMessage());
                }
            } else {
                Log.w(TAG, "❌ Navegación diferida cancelada - Fragment no válido");
            }

            // Limpiar referencias
            isNavigationPending = false;
            pendingNavigation = null;
        };

        // Ejecutar después del delay
        isNavigationPending = true;
        delayHandler.postDelayed(pendingNavigation, 1500);
        Log.d(TAG, "⏰ Navegación programada para 1.5 segundos");
    }

    // 🔥 MÉTODO: Cancelar navegación pendiente
    private void cancelPendingNavigation() {
        if (isNavigationPending && delayHandler != null && pendingNavigation != null) {
            Log.d(TAG, "🚫 Cancelando navegación pendiente");
            delayHandler.removeCallbacks(pendingNavigation);
            isNavigationPending = false;
            pendingNavigation = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "📱 Fragment onPause() - Cancelando navegaciones pendientes");
        cancelPendingNavigation();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "📱 Fragment onStop() - Limpiando recursos");
        cancelPendingNavigation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "📱 Fragment onDestroyView() - Limpieza final");
        cancelPendingNavigation();

        // Limpiar referencias
        delayHandler = null;
        pendingNavigation = null;
        isNavigationPending = false;
    }

    // 🔥 MÉTODO: Navegación de emergencia si falla todo lo demás
    private void emergencyNavigation() {
        Log.d(TAG, "🚨 Ejecutando navegación de emergencia");

        if (getActivity() != null) {
            try {
                // Intentar ir atrás usando el back stack
                getActivity().onBackPressed();
                Log.d(TAG, "✅ Navegación de emergencia exitosa");
            } catch (Exception e) {
                Log.e(TAG, "❌ Error en navegación de emergencia: " + e.getMessage());

                // Último recurso: finalizar el fragmento
                try {
                    getActivity().finish();
                    Log.d(TAG, "⚠️ Activity finalizada como último recurso");
                } catch (Exception finalError) {
                    Log.e(TAG, "❌ Error finalizando activity: " + finalError.getMessage());
                }
            }
        }
    }

    private void showErrorMessage(String error) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Error")
                .setMessage("No se pudo crear el administrador:\n\n" + error)
                .setPositiveButton("Reintentar", null)
                .setIcon(R.drawable.ic_exclamacioncampoerroneo)
                .show();
    }

    private void resetForm() {
        loadFormFields();
        resetButtons();
    }

    private void resetButtons() {
        btnCreateAdmin.setEnabled(true);
        btnCreateAdmin.setText("Crear Administrador");
    }

    private String getCurrentSuperAdminId() {
        if (getActivity() instanceof SuperAdminActivity) {
            return ((SuperAdminActivity) getActivity()).getUserId();
        }
        return "superadmin_unknown";
    }

    private void sendLocalNotification(String title, String message) {
        // 🔥 NOTIFICACIÓN SIMPLE CON TOAST
        Toast.makeText(getContext(), title + ": " + message, Toast.LENGTH_LONG).show();

        // 🔥 LOG PARA DEBUG
        Log.d(TAG, "📱 NOTIFICACIÓN: " + title + " - " + message);
    }
}