package com.example.proyecto_final_hoteleros.superadmin.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.models.UserModel;
import com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity;
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.adapters.AdminsAdapter;
import com.example.proyecto_final_hoteleros.superadmin.models.AdminUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

public class AdminsFragment extends Fragment {

    private RecyclerView rvAdmins;
    private AdminsAdapter adminsAdapter;
    private FloatingActionButton fabAddAdmin;
    private LinearLayout layoutEmptyState;

    // Variables para búsqueda y filtros
    private com.google.android.material.textfield.TextInputEditText etSearch;
    private LinearLayout layoutSearch;
    private ImageView ivSearchToggle, ivFilter;
    private com.google.android.material.chip.Chip chipAll, chipActive, chipInactive, chipRecent;

    // Listas para filtros
    private List<AdminUser> allAdmins = new ArrayList<>();
    private List<AdminUser> filteredAdmins = new ArrayList<>();
    private String currentFilter = "ALL";
    private boolean isSearchVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.superadmin_fragment_admins, container, false);

        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadData();

        return view;
    }

    // ✅ AGREGAR ESTE MÉTODO NUEVO DESPUÉS DE onCreateView
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ✅ CONFIGURAR WINDOW INSETS
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });
    }


    private void initViews(View view) {
        android.util.Log.d("AdminsFragment", "=== INICIO initViews ===");

        rvAdmins = view.findViewById(R.id.rv_admins);
        fabAddAdmin = view.findViewById(R.id.fab_add_admin);
        layoutEmptyState = view.findViewById(R.id.tv_empty_state);

        // Nuevas vistas de búsqueda y filtros
        etSearch = view.findViewById(R.id.et_search);
        layoutSearch = view.findViewById(R.id.layout_search);
        ivSearchToggle = view.findViewById(R.id.iv_search_toggle);
        //ivFilter = view.findViewById(R.id.iv_filter);
        chipAll = view.findViewById(R.id.chip_all);
        chipActive = view.findViewById(R.id.chip_active);
        chipInactive = view.findViewById(R.id.chip_inactive);
        chipRecent = view.findViewById(R.id.chip_recent);

        // Configurar botón de back
        ImageView ivBack = view.findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        setupSearchAndFilters();

        android.util.Log.d("AdminsFragment", "=== FIN initViews ===");
    }

    private void setupSearchAndFilters() {
        // Toggle de búsqueda
        ivSearchToggle.setOnClickListener(v -> toggleSearch());

        // Listener de búsqueda
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Configurar chips
        setupFilterChips();
    }

    private void toggleSearch() {
        isSearchVisible = !isSearchVisible;

        if (isSearchVisible) {
            layoutSearch.setVisibility(View.VISIBLE);
            layoutSearch.setAlpha(0f);
            layoutSearch.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();

            // ✅ Usar ícono personalizado de cerrar
            ivSearchToggle.setImageResource(R.drawable.ic_close_superadmin);

            etSearch.requestFocus();
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);

        } else {
            layoutSearch.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        layoutSearch.setVisibility(View.GONE);
                        etSearch.setText("");
                    })
                    .start();

            // ✅ Restaurar ícono personalizado de búsqueda
            ivSearchToggle.setImageResource(R.drawable.ic_search_superadmin);

            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }

    private void setupFilterChips() {
        // Configurar grupo de chips mutuamente exclusivos
        chipAll.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                uncheckOtherChips(chipAll);
                applyFilter("ALL");
            }
        });

        chipActive.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                uncheckOtherChips(chipActive);
                applyFilter("ACTIVE");
            }
        });

        chipInactive.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                uncheckOtherChips(chipInactive);
                applyFilter("INACTIVE");
            }
        });

        chipRecent.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                uncheckOtherChips(chipRecent);
                applyFilter("RECENT");
            }
        });
    }

    private void uncheckOtherChips(com.google.android.material.chip.Chip selectedChip) {
        if (selectedChip != chipAll) chipAll.setChecked(false);
        if (selectedChip != chipActive) chipActive.setChecked(false);
        if (selectedChip != chipInactive) chipInactive.setChecked(false);
        if (selectedChip != chipRecent) chipRecent.setChecked(false);
    }

    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            // Si no hay búsqueda, mostrar todos según el filtro actual
            applyFilter(currentFilter);
            return;
        }

        List<AdminUser> searchResults = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (AdminUser admin : allAdmins) {
            // Buscar en nombre y email
            if (admin.getName().toLowerCase().contains(lowerQuery) ||
                    admin.getEmail().toLowerCase().contains(lowerQuery)) {
                searchResults.add(admin);
            }
        }

        // Aplicar filtro actual a los resultados de búsqueda
        filteredAdmins = filterAdminsByStatus(searchResults, currentFilter);
        updateAdminsList(filteredAdmins);

        Log.d("AdminsFragment", "Búsqueda: '" + query + "' - " + filteredAdmins.size() + " resultados");
    }

    private void applyFilter(String filter) {
        currentFilter = filter;

        String searchQuery = etSearch.getText().toString().trim();
        List<AdminUser> baseList = allAdmins;

        // Si hay búsqueda activa, usar solo los resultados de búsqueda
        if (!searchQuery.isEmpty()) {
            baseList = new ArrayList<>();
            String lowerQuery = searchQuery.toLowerCase();

            for (AdminUser admin : allAdmins) {
                if (admin.getName().toLowerCase().contains(lowerQuery) ||
                        admin.getEmail().toLowerCase().contains(lowerQuery)) {
                    baseList.add(admin);
                }
            }
        }

        filteredAdmins = filterAdminsByStatus(baseList, filter);
        updateAdminsList(filteredAdmins);

        Log.d("AdminsFragment", "Filtro aplicado: " + filter + " - " + filteredAdmins.size() + " resultados");
    }

    private List<AdminUser> filterAdminsByStatus(List<AdminUser> admins, String filter) {
        List<AdminUser> result = new ArrayList<>();

        switch (filter) {
            case "ALL":
                result.addAll(admins);
                break;
            case "ACTIVE":
                for (AdminUser admin : admins) {
                    if (admin.isActive()) result.add(admin);
                }
                break;
            case "INACTIVE":
                for (AdminUser admin : admins) {
                    if (!admin.isActive()) result.add(admin);
                }
                break;
            case "RECENT":
                // Ordenar por fecha de registro (más recientes primero)
                List<AdminUser> sortedAdmins = new ArrayList<>(admins);
                sortedAdmins.sort((a1, a2) -> a2.getRegistrationDate().compareTo(a1.getRegistrationDate()));
                // Tomar solo los primeros 5 o todos si son menos
                int limit = Math.min(5, sortedAdmins.size());
                result.addAll(sortedAdmins.subList(0, limit));
                break;
        }

        return result;
    }

    private void setupRecyclerView() {
        rvAdmins.setLayoutManager(new LinearLayoutManager(getContext()));
        adminsAdapter = new AdminsAdapter(new ArrayList<>(), this::onAdminAction);
        rvAdmins.setAdapter(adminsAdapter);
    }

    private void setupClickListeners() {
        fabAddAdmin.setOnClickListener(v -> {
            // Navegar a registro de nuevo admin
            if (getActivity() instanceof com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity) {
                ((com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity) getActivity()).navigateToAddAdmin();
            }
        });
    }

    private void loadData() {
        Log.d("AdminsFragment", "Cargando administradores de hotel desde Firebase...");

        // Mostrar loading si tienes un indicador
        showLoading(true);

        FirebaseManager.getInstance().getHotelAdmins(new FirebaseManager.DriverListCallback() {
            @Override
            public void onSuccess(List<UserModel> hotelAdmins) {
                Log.d("AdminsFragment", "✅ " + hotelAdmins.size() + " administradores obtenidos [" + new Date() + "]");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        List<AdminUser> admins = convertUserModelsToAdminUsers(hotelAdmins);
                        updateAdminsList(admins);
                        showLoading(false);

                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e("AdminsFragment", "❌ Error obteniendo administradores: " + error);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showError("Error cargando administradores: " + error);
                        showLoading(false);
                    });
                }
            }
        });
    }

    // 🔥 MÉTODO AUXILIAR: Convertir UserModel a AdminUser
    private List<AdminUser> convertUserModelsToAdminUsers(List<UserModel> userModels) {
        List<AdminUser> admins = new ArrayList<>();

        for (UserModel userModel : userModels) {
            AdminUser admin = new AdminUser();
            admin.setId(userModel.getUserId());
            admin.setName(userModel.getFullName());
            admin.setEmail(userModel.getEmail());
            admin.setHotelName("Hotel Asignado");
            admin.setActive(userModel.isActive());
            admin.setRegistrationDate(formatTimestamp(userModel.getCreatedAt()));

            admins.add(admin);
        }

        // Guardar lista completa para filtros
        allAdmins.clear();
        allAdmins.addAll(admins);

        return admins;
    }

    // 🔥 MÉTODO AUXILIAR: Formatear timestamp
    private String formatTimestamp(long timestamp) {
        if (timestamp == 0) return "Fecha no disponible";

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }

    // 🔥 MÉTODO AUXILIAR: Actualizar lista
    private void updateAdminsList(List<AdminUser> admins) {
        if (adminsAdapter != null) {
            adminsAdapter.updateData(admins);
            Log.d("AdminsFragment", "Adapter actualizado con " + admins.size() + " administradores");
        }

        // Mostrar/ocultar estado vacío
        if (layoutEmptyState != null && rvAdmins != null) {
            if (admins.isEmpty()) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                rvAdmins.setVisibility(View.GONE);
            } else {
                layoutEmptyState.setVisibility(View.GONE);
                rvAdmins.setVisibility(View.VISIBLE);
            }
        }
    }

    // 🔥 MÉTODO AUXILIAR: Mostrar loading
    private void showLoading(boolean show) {
        Log.d("AdminsFragment", "Loading: " + show);
        // Aquí puedes agregar un ProgressBar si quieres
    }

    // 🔥 MÉTODO AUXILIAR: Mostrar error
    private void showError(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void onAdminAction(AdminUser admin, String action) {
        Log.d("AdminsFragment", "Acción: " + action + " para admin: " + admin.getName());

        switch (action) {
            case "toggle_status":
                toggleAdminStatus(admin);
                break;
            case "view_info":
                showAdminInformation(admin);
                break;
            default:
                Log.w("AdminsFragment", "Acción no reconocida: " + action);
                break;
        }
    }

    private void toggleAdminStatus(AdminUser admin) {
        String action = admin.isActive() ? "desactivar" : "activar";
        String actionCapitalized = action.substring(0, 1).toUpperCase() + action.substring(1);

        // Mostrar diálogo de confirmación mejorado
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle(actionCapitalized + " Administrador")
                .setMessage("¿Estás seguro que deseas " + action + " a " + admin.getName() + "?\n\n" +
                        "📧 " + admin.getEmail() + "\n" +
                        "🏨 " + admin.getHotelName())
                .setPositiveButton("✅ " + actionCapitalized.toUpperCase(), (dialog, which) -> {
                    performToggleAction(admin);
                })
                .setNegativeButton("❌ Cancelar", (dialog, which) -> dialog.dismiss())
                .setIcon(admin.isActive() ? R.drawable.ic_exclamacioncampoerroneo : R.drawable.ic_check)
                .show();
    }

    // 🔥 NUEVO MÉTODO: Realizar la acción de toggle
    private void performToggleAction(AdminUser admin) {
        boolean newStatus = !admin.isActive();
        String actionText = newStatus ? "Activando..." : "Desactivando...";

        // Mostrar progress (puedes usar un Toast simple)
        android.widget.Toast progressToast = android.widget.Toast.makeText(getContext(),
                actionText, android.widget.Toast.LENGTH_SHORT);
        progressToast.show();

        FirebaseManager.getInstance().toggleHotelAdminStatus(admin.getId(), newStatus,
                new FirebaseManager.DataCallback() {
                    @Override
                    public void onSuccess() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                // Actualizar el modelo
                                admin.setActive(newStatus);

                                // Actualizar la vista
                                adminsAdapter.notifyDataSetChanged();

                                // Mostrar mensaje de éxito
                                String successMessage = newStatus ?
                                        "✅ " + admin.getName() + " activado correctamente" :
                                        "🔒 " + admin.getName() + " desactivado correctamente";

                                android.widget.Toast.makeText(getContext(),
                                        successMessage, android.widget.Toast.LENGTH_LONG).show();

                                // Log para debug
                                Log.d("AdminsFragment", "Admin " + admin.getId() + " estado cambiado a: " + newStatus);
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                String errorMessage = "❌ Error al " + (newStatus ? "activar" : "desactivar") +
                                        " el administrador: " + error;
                                android.widget.Toast.makeText(getContext(),
                                        errorMessage, android.widget.Toast.LENGTH_LONG).show();

                                Log.e("AdminsFragment", "Error toggle admin: " + error);
                            });
                        }
                    }
                });
    }

    private void viewAdminDetails(AdminUser admin) {
        Log.d("AdminsFragment", "Mostrando detalles de: " + admin.getName());
        showAdminDetailsDialog(admin);
    }

    // 🔥 NUEVO MÉTODO: Mostrar diálogo de detalles completo
    private void showAdminDetailsDialog(AdminUser admin) {
        // Inflar el layout personalizado
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_admin_details, null);

        // Configurar los datos
        TextView tvAdminName = dialogView.findViewById(R.id.tv_admin_name);
        TextView tvAdminEmail = dialogView.findViewById(R.id.tv_admin_email);
        TextView tvAdminStatus = dialogView.findViewById(R.id.tv_admin_status);
        TextView tvHotelName = dialogView.findViewById(R.id.tv_hotel_name);
        TextView tvRegistrationDate = dialogView.findViewById(R.id.tv_registration_date);


        // Llenar los datos
        tvAdminName.setText(admin.getName());
        tvAdminEmail.setText(admin.getEmail());
        tvAdminStatus.setText(admin.isActive() ? "✅ ACTIVO" : "❌ INACTIVO");
        tvHotelName.setText(admin.getHotelName());
        tvRegistrationDate.setText(admin.getRegistrationDate());

        // Configurar botones
        com.google.android.material.button.MaterialButton btnEdit = dialogView.findViewById(R.id.btn_edit_admin);
        com.google.android.material.button.MaterialButton btnToggle = dialogView.findViewById(R.id.btn_toggle_status);

        // Crear el diálogo
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Configurar listeners de botones
        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            editAdmin(admin);
        });

        btnToggle.setOnClickListener(v -> {
            dialog.dismiss();
            toggleAdminStatus(admin);
        });

        // Mostrar el diálogo
        dialog.show();

        // Opcional: Hacer el diálogo más ancho
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.95),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void editAdmin(AdminUser admin) {
        Log.d("AdminsFragment", "Navegando a editar admin: " + admin.getName());

        if (getActivity() instanceof SuperAdminActivity) {
            EditAdminFragment editFragment = EditAdminFragment.newInstance(admin);
            ((SuperAdminActivity) getActivity()).loadFragment(editFragment, "EDIT_ADMIN", true);
        }
    }

    private void viewHotelDetails(AdminUser admin) {
        android.widget.Toast.makeText(getContext(), "Ver hotel de " + admin.getName(), android.widget.Toast.LENGTH_SHORT).show();
    }

    private void resetAdminPassword(AdminUser admin) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Restablecer contraseña")
                .setMessage("¿Estás seguro que deseas restablecer la contraseña de " + admin.getName() + "?")
                .setPositiveButton("Restablecer", (dialog, which) -> {
                    android.widget.Toast.makeText(getContext(), "Funcionalidad próximamente", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }


    // 🔥 NUEVO: Método público para refrescar desde SuperAdminActivity
    public void refreshAdminsList() {
        Log.d("AdminsFragment", "🔄 Refresh forzado de administradores...");
        loadData();
    }
    // 🔥 NUEVO: Refresh con delay para sincronización
    public void refreshAdminsListWithDelay() {
        Log.d("AdminsFragment", "🔄 Iniciando refresh con delay para sincronización...");

        // Delay para asegurar que Firebase esté sincronizado
        new Handler().postDelayed(() -> {
            Log.d("AdminsFragment", "⚡ Ejecutando refresh después del delay...");
            refreshAdminsListWithRetry(0);
        }, 1500); // 1.5 segundos de delay
    }

    // 🔥 NUEVO: Refresh con reintentos automáticos
    private void refreshAdminsListWithRetry(int attemptCount) {
        final int MAX_ATTEMPTS = 3;

        Log.d("AdminsFragment", "📋 Cargando admins (intento " + (attemptCount + 1) + "/" + MAX_ATTEMPTS + ")...");

        showLoading(true);

        FirebaseManager.getInstance().getHotelAdmins(new FirebaseManager.DriverListCallback() {
            @Override
            public void onSuccess(List<UserModel> hotelAdmins) {
                Log.d("AdminsFragment", "✅ " + hotelAdmins.size() + " administradores obtenidos (intento " + (attemptCount + 1) + ")");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        List<AdminUser> admins = convertUserModelsToAdminUsers(hotelAdmins);
                        updateAdminsList(admins);
                        showLoading(false);

                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e("AdminsFragment", "❌ Error obteniendo administradores (intento " + (attemptCount + 1) + "): " + error);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Si no es el último intento, reintentar
                        if (attemptCount < MAX_ATTEMPTS - 1) {
                            Log.d("AdminsFragment", "🔄 Reintentando en 2 segundos... (intento " + (attemptCount + 2) + ")");

                            new Handler().postDelayed(() -> {
                                refreshAdminsListWithRetry(attemptCount + 1);
                            }, 2000);
                        } else {
                            // Último intento falló
                            Log.e("AdminsFragment", "❌ Todos los intentos fallaron");
                            showError("Error cargando administradores después de " + MAX_ATTEMPTS + " intentos: " + error);
                            showLoading(false);
                        }
                    });
                }
            }
        });
    }

    // 🔥 MEJORAR onResume() existente
    @Override
    public void onResume() {
        super.onResume();
        Log.d("AdminsFragment", "📱 AdminsFragment onResume() - Refrescando con delay...");

        // 🔥 USAR REFRESH CON DELAY EN VEZ DEL INMEDIATO
        refreshAdminsListWithDelay();
    }


    private void showAdminInformation(AdminUser admin) {
        Log.d("AdminsFragment", "📋 Mostrando información de: " + admin.getName());

        // Construir información del administrador
        StringBuilder info = new StringBuilder();

        // 📋 INFORMACIÓN BÁSICA
        info.append("🏨 INFORMACIÓN DEL ADMINISTRADOR\n");
        info.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        info.append("📛 Nombre: ").append(admin.getName()).append("\n");
        info.append("📧 Email: ").append(admin.getEmail()).append("\n");
        info.append("🏨 Hotel: ").append(admin.getHotelName()).append("\n");
        info.append("📅 Registrado: ").append(admin.getRegistrationDate()).append("\n");
        info.append("⚡ Estado: ").append(admin.getStatusText()).append("\n");

        // 🔧 PERMISOS Y RESPONSABILIDADES
        info.append("\n🔧 PERMISOS DE ADMINISTRADOR\n");
        info.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        info.append("• Gestionar reservas del hotel\n");
        info.append("• Administrar servicios disponibles\n");
        info.append("• Ver reportes del hotel\n");
        info.append("• Gestionar disponibilidad de habitaciones\n");
        info.append("• Coordinar con el equipo de taxi\n");

        // 📊 INFORMACIÓN DEL ESTADO
        if (admin.isActive()) {
            info.append("\n✅ ESTADO ACTIVO\n");
            info.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            info.append("• Acceso completo al sistema\n");
            info.append("• Puede gestionar reservas\n");
            info.append("• Notificaciones habilitadas\n");
        } else {
            info.append("\n❌ ESTADO INACTIVO\n");
            info.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            info.append("• Acceso suspendido temporalmente\n");
            info.append("• No puede gestionar reservas\n");
            info.append("• Notificaciones deshabilitadas\n");
        }

        // 📝 NOTA IMPORTANTE
        info.append("\n📝 NOTA\n");
        info.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        info.append("Esta información es solo de lectura.\n");

        // Mostrar el diálogo
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("🏨 Información del Administrador")
                .setMessage(info.toString())
                .setPositiveButton("✅ Entendido", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.ic_info)
                .show();
    }

}