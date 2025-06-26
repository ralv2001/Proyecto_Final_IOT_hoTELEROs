package com.example.proyecto_final_hoteleros.superadmin.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.models.UserModel;
import com.example.proyecto_final_hoteleros.superadmin.adapters.UsuariosAdapter;
import com.example.proyecto_final_hoteleros.superadmin.models.Usuario;
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;

import java.util.ArrayList;
import java.util.List;

public class UsuariosFragment extends Fragment {

    private RecyclerView rvUsuarios;
    private UsuariosAdapter usuariosAdapter;
    private LinearLayout layoutEmptyState;

    // Variables para búsqueda y filtros
    private com.google.android.material.textfield.TextInputEditText etSearch;
    private LinearLayout layoutSearch;
    private ImageView ivSearchToggle, ivFilter, ivStats;
    private com.google.android.material.chip.Chip chipAll, chipClients, chipHotelAdmins,
            chipDrivers, chipPending, chipActive, chipInactive;

    // Listas para filtros
    private List<Usuario> allUsuarios = new ArrayList<>();
    private List<Usuario> filteredUsuarios = new ArrayList<>();
    private String currentFilter = "ALL";
    private boolean isSearchVisible = false;
    private FirebaseManager.UserStatistics currentStats;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.superadmin_fragment_usuarios, container, false);

        initViews(view);
        setupRecyclerView();
        loadData();

        return view;
    }

    private void initViews(View view) {
        android.util.Log.d("UsuariosFragment", "=== INICIO initViews ===");

        rvUsuarios = view.findViewById(R.id.rv_usuarios);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);

        // Nuevas vistas de búsqueda y filtros
        etSearch = view.findViewById(R.id.et_search);
        layoutSearch = view.findViewById(R.id.layout_search);
        ivSearchToggle = view.findViewById(R.id.iv_search_toggle);
        ivFilter = view.findViewById(R.id.iv_filter);
        ivStats = view.findViewById(R.id.iv_stats);
        chipAll = view.findViewById(R.id.chip_all);
        chipClients = view.findViewById(R.id.chip_clients);
        chipHotelAdmins = view.findViewById(R.id.chip_hotel_admins);
        chipDrivers = view.findViewById(R.id.chip_drivers);
        chipPending = view.findViewById(R.id.chip_pending);
        chipActive = view.findViewById(R.id.chip_active);
        chipInactive = view.findViewById(R.id.chip_inactive);

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

        android.util.Log.d("UsuariosFragment", "=== FIN initViews ===");
    }

    private void setupSearchAndFilters() {
        // Toggle de búsqueda
        ivSearchToggle.setOnClickListener(v -> toggleSearch());

        // Botón de estadísticas
        ivStats.setOnClickListener(v -> showDetailedStatistics());

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
        chipAll.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                uncheckOtherChips(chipAll);
                applyFilter("ALL");
            }
        });

        chipClients.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                uncheckOtherChips(chipClients);
                applyFilter("CLIENTE");
            }
        });

        chipHotelAdmins.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                uncheckOtherChips(chipHotelAdmins);
                applyFilter("ADMIN_HOTEL");
            }
        });

        chipDrivers.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                uncheckOtherChips(chipDrivers);
                applyFilter("TAXISTA");
            }
        });

        chipPending.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                uncheckOtherChips(chipPending);
                applyFilter("TAXISTA_PENDIENTE");
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
    }

    private void uncheckOtherChips(com.google.android.material.chip.Chip selectedChip) {
        if (selectedChip != chipAll) chipAll.setChecked(false);
        if (selectedChip != chipClients) chipClients.setChecked(false);
        if (selectedChip != chipHotelAdmins) chipHotelAdmins.setChecked(false);
        if (selectedChip != chipDrivers) chipDrivers.setChecked(false);
        if (selectedChip != chipPending) chipPending.setChecked(false);
        if (selectedChip != chipActive) chipActive.setChecked(false);
        if (selectedChip != chipInactive) chipInactive.setChecked(false);
    }

    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            applyFilter(currentFilter);
            return;
        }

        List<Usuario> searchResults = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (Usuario usuario : allUsuarios) {
            // Buscar en nombre, email y tipo de usuario
            if (usuario.getName().toLowerCase().contains(lowerQuery) ||
                    usuario.getEmail().toLowerCase().contains(lowerQuery) ||
                    usuario.getUserTypeText().toLowerCase().contains(lowerQuery)) {
                searchResults.add(usuario);
            }
        }

        filteredUsuarios = filterUsuariosByType(searchResults, currentFilter);
        updateUsuariosList(filteredUsuarios);

        Log.d("UsuariosFragment", "Búsqueda: '" + query + "' - " + filteredUsuarios.size() + " resultados");
    }

    private void applyFilter(String filter) {
        currentFilter = filter;

        String searchQuery = etSearch.getText().toString().trim();
        List<Usuario> baseList = allUsuarios;

        if (!searchQuery.isEmpty()) {
            baseList = new ArrayList<>();
            String lowerQuery = searchQuery.toLowerCase();

            for (Usuario usuario : allUsuarios) {
                if (usuario.getName().toLowerCase().contains(lowerQuery) ||
                        usuario.getEmail().toLowerCase().contains(lowerQuery) ||
                        usuario.getUserTypeText().toLowerCase().contains(lowerQuery)) {
                    baseList.add(usuario);
                }
            }
        }

        filteredUsuarios = filterUsuariosByType(baseList, filter);
        updateUsuariosList(filteredUsuarios);

        Log.d("UsuariosFragment", "Filtro aplicado: " + filter + " - " + filteredUsuarios.size() + " resultados");
    }

    private List<Usuario> filterUsuariosByType(List<Usuario> usuarios, String filter) {
        List<Usuario> result = new ArrayList<>();

        switch (filter) {
            case "ALL":
                result.addAll(usuarios);
                break;
            case "ACTIVE":
                for (Usuario usuario : usuarios) {
                    if (usuario.isActive()) result.add(usuario);
                }
                break;
            case "INACTIVE":
                for (Usuario usuario : usuarios) {
                    if (!usuario.isActive()) result.add(usuario);
                }
                break;
            default:
                // Filtrar por tipo específico
                for (Usuario usuario : usuarios) {
                    if (filter.equals(usuario.getUserType())) {
                        result.add(usuario);
                    }
                }
                break;
        }

        return result;
    }

    private void showDetailedStatistics() {
        if (currentStats == null) {
            android.widget.Toast.makeText(getContext(), "Cargando estadísticas...", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder statsMessage = new StringBuilder();
        statsMessage.append("📊 ESTADÍSTICAS DETALLADAS\n");
        statsMessage.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        statsMessage.append("👥 USUARIOS TOTALES: ").append(currentStats.totalUsers).append("\n");
        statsMessage.append("✅ Usuarios Activos: ").append(currentStats.totalActiveUsers).append("\n");
        statsMessage.append("❌ Usuarios Inactivos: ").append(currentStats.totalUsers - currentStats.totalActiveUsers).append("\n\n");

        statsMessage.append("📋 POR TIPO DE USUARIO:\n");
        statsMessage.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        statsMessage.append("👤 Clientes: ").append(currentStats.totalClients).append(" (").append(currentStats.activeClients).append(" activos)\n");
        statsMessage.append("🏨 Admins Hotel: ").append(currentStats.totalHotelAdmins).append(" (").append(currentStats.activeHotelAdmins).append(" activos)\n");
        statsMessage.append("🚗 Taxistas Aprobados: ").append(currentStats.approvedDrivers).append("\n");
        statsMessage.append("🕒 Taxistas Pendientes: ").append(currentStats.pendingDrivers).append("\n");
        statsMessage.append("⚙️ SuperAdmins: ").append(currentStats.totalSuperAdmins).append("\n\n");

        statsMessage.append("📈 PORCENTAJES:\n");
        statsMessage.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        if (currentStats.totalUsers > 0) {
            double clientPercent = (currentStats.totalClients * 100.0) / currentStats.totalUsers;
            double driverPercent = (currentStats.totalDrivers * 100.0) / currentStats.totalUsers;
            double adminPercent = (currentStats.totalHotelAdmins * 100.0) / currentStats.totalUsers;

            statsMessage.append(String.format("👤 Clientes: %.1f%%\n", clientPercent));
            statsMessage.append(String.format("🚗 Taxistas: %.1f%%\n", driverPercent));
            statsMessage.append(String.format("🏨 Admins: %.1f%%\n", adminPercent));
        }

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("📊 Estadísticas del Sistema")
                .setMessage(statsMessage.toString())
                .setPositiveButton("✅ Entendido", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("🔄 Actualizar", (dialog, which) -> {
                    dialog.dismiss();
                    loadUserStatistics();
                })
                .show();
    }

    private void setupRecyclerView() {
        android.util.Log.d("UsuariosFragment", "Configurando RecyclerView...");
        try {
            rvUsuarios.setLayoutManager(new LinearLayoutManager(getContext()));
            usuariosAdapter = new UsuariosAdapter(new ArrayList<>(), this::onUsuarioAction);
            rvUsuarios.setAdapter(usuariosAdapter);
            android.util.Log.d("UsuariosFragment", "RecyclerView configurado exitosamente");
        } catch (Exception e) {
            android.util.Log.e("UsuariosFragment", "Error configurando RecyclerView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadData() {
        Log.d("UsuariosFragment", "Cargando usuarios reales desde Firebase...");

        // Mostrar loading
        showLoading(true);

        FirebaseManager.getInstance().getAllUsers(new FirebaseManager.DriverListCallback() {
            @Override
            public void onSuccess(List<UserModel> allUsers) {
                Log.d("UsuariosFragment", "✅ " + allUsers.size() + " usuarios obtenidos");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Convertir UserModel a Usuario
                        List<Usuario> usuarios = convertUserModelsToUsuarios(allUsers);
                        updateUsuariosList(usuarios);
                        showLoading(false);

                        // Cargar estadísticas
                        loadUserStatistics();
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e("UsuariosFragment", "❌ Error obteniendo usuarios: " + error);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showError("Error cargando usuarios: " + error);
                        showLoading(false);
                    });
                }
            }
        });
    }

    // 🔥 NUEVO MÉTODO: Convertir UserModel a Usuario
    private List<Usuario> convertUserModelsToUsuarios(List<UserModel> userModels) {
        List<Usuario> usuarios = new ArrayList<>();

        for (UserModel userModel : userModels) {
            Usuario usuario = new Usuario();
            usuario.setId(userModel.getUserId());
            usuario.setName(userModel.getFullName());
            usuario.setEmail(userModel.getEmail());
            usuario.setUserType(mapUserType(userModel.getUserType()));
            usuario.setActive(userModel.isActive());
            usuario.setRegistrationDate(formatTimestamp(userModel.getCreatedAt()));
            usuario.setPhoneNumber(userModel.getTelefono());

            // Determinar estado específico para taxistas
            if ("driver".equals(userModel.getUserType()) && !userModel.isActive()) {
                usuario.setUserType("TAXISTA_PENDIENTE");
            }

            usuarios.add(usuario);
        }

        // Guardar lista completa para filtros
        allUsuarios.clear();
        allUsuarios.addAll(usuarios);

        Log.d("UsuariosFragment", "Convertidos " + usuarios.size() + " usuarios");
        return usuarios;
    }

    // 🔥 NUEVO MÉTODO: Mapear tipos de usuario
    private String mapUserType(String firebaseUserType) {
        switch (firebaseUserType) {
            case "client":
                return "CLIENTE";
            case "driver":
                return "TAXISTA";
            case "hotel_admin":
                return "ADMIN_HOTEL";
            case "superadmin":
                return "SUPERADMIN";
            default:
                return "DESCONOCIDO";
        }
    }

    // 🔥 NUEVO MÉTODO: Formatear timestamp
    private String formatTimestamp(long timestamp) {
        if (timestamp == 0) return "Fecha no disponible";

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }

    // 🔥 NUEVO MÉTODO: Actualizar lista
    private void updateUsuariosList(List<Usuario> usuarios) {
        if (usuariosAdapter != null) {
            usuariosAdapter.updateData(usuarios);
            Log.d("UsuariosFragment", "Adapter actualizado con " + usuarios.size() + " usuarios");
        }

        // Mostrar/ocultar estado vacío
        if (layoutEmptyState != null && rvUsuarios != null) {
            if (usuarios.isEmpty()) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                rvUsuarios.setVisibility(View.GONE);
            } else {
                layoutEmptyState.setVisibility(View.GONE);
                rvUsuarios.setVisibility(View.VISIBLE);
            }
        }
    }

    // 🔥 NUEVO MÉTODO: Cargar estadísticas
    private void loadUserStatistics() {
        FirebaseManager.getInstance().getUserStatistics(new FirebaseManager.UserStatsCallback() {
            @Override
            public void onSuccess(FirebaseManager.UserStatistics stats) {
                Log.d("UsuariosFragment", "✅ Estadísticas: " + stats.toString());

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showUserStatistics(stats);
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e("UsuariosFragment", "❌ Error obteniendo estadísticas: " + error);
            }
        });
    }

    // 🔥 NUEVO MÉTODO: Mostrar estadísticas
    private void showUserStatistics(FirebaseManager.UserStatistics stats) {
        this.currentStats = stats; // Guardar para mostrar después

        // Log detallado
        String statsMessage = String.format(
                "📊 ESTADÍSTICAS:\n" +
                        "👥 Total: %d usuarios\n" +
                        "✅ Activos: %d\n" +
                        "👤 Clientes: %d\n" +
                        "🚗 Taxistas: %d (%d aprobados, %d pendientes)\n" +
                        "🏨 Admins Hotel: %d",
                stats.totalUsers, stats.totalActiveUsers, stats.totalClients,
                stats.totalDrivers, stats.approvedDrivers, stats.pendingDrivers,
                stats.totalHotelAdmins
        );

        Log.d("UsuariosFragment", statsMessage);
    }

    // 🔥 MÉTODOS AUXILIARES
    private void showLoading(boolean show) {
        Log.d("UsuariosFragment", "Loading: " + show);
        // Aquí puedes agregar un ProgressBar si quieres
    }

    private void showError(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void onUsuarioAction(Usuario usuario, String action) {
        android.util.Log.d("UsuariosFragment", "Acción: " + action + " para usuario: " + usuario.getName());
        try {
            switch (action) {
                case "toggle_status":
                    toggleUsuarioStatus(usuario);
                    break;
                case "view_details":
                    viewUsuarioDetails(usuario);
                    break;
                case "edit":
                    editUsuario(usuario);
                    break;
                case "view_activity":
                    viewUsuarioActivity(usuario);
                    break;
                case "reset_password":
                    resetPassword(usuario);
                    break;
                default:
                    android.util.Log.w("UsuariosFragment", "Acción no reconocida: " + action);
                    break;
            }
        } catch (Exception e) {
            android.util.Log.e("UsuariosFragment", "Error en onUsuarioAction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void toggleUsuarioStatus(Usuario usuario) {
        String action = usuario.isActive() ? "desactivar" : "activar";
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle(action.substring(0, 1).toUpperCase() + action.substring(1) + " usuario")
                .setMessage("¿Estás seguro que deseas " + action + " a " + usuario.getName() + "?")
                .setPositiveButton(action.substring(0, 1).toUpperCase() + action.substring(1), (dialog, which) -> {
                    usuario.setActive(!usuario.isActive());
                    usuariosAdapter.notifyDataSetChanged();
                    String message = usuario.isActive() ? "Usuario activado" : "Usuario desactivado";
                    android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void viewUsuarioDetails(Usuario usuario) {
        android.widget.Toast.makeText(getContext(), "Ver detalles de " + usuario.getName(), android.widget.Toast.LENGTH_SHORT).show();
    }

    private void editUsuario(Usuario usuario) {
        android.widget.Toast.makeText(getContext(), "Editar " + usuario.getName(), android.widget.Toast.LENGTH_SHORT).show();
    }

    private void viewUsuarioActivity(Usuario usuario) {
        android.widget.Toast.makeText(getContext(), "Ver actividad de " + usuario.getName(), android.widget.Toast.LENGTH_SHORT).show();
    }

    private void resetPassword(Usuario usuario) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Restablecer contraseña")
                .setMessage("¿Estás seguro que deseas restablecer la contraseña de " + usuario.getName() + "?")
                .setPositiveButton("Restablecer", (dialog, which) -> {
                    android.widget.Toast.makeText(getContext(), "Contraseña restablecida para " + usuario.getName(), android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showFilterOptions() {
        String[] options = {"Todos", "Clientes", "Admins de Hotel", "Taxistas", "Activos", "Inactivos"};
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Filtrar usuarios")
                .setItems(options, (dialog, which) -> {
                    // Implementar filtrado
                    android.widget.Toast.makeText(getContext(), "Filtro: " + options[which], android.widget.Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}