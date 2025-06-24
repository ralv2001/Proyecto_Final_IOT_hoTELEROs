package com.example.proyecto_final_hoteleros.superadmin.fragment;

import android.os.Bundle;
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
import com.example.proyecto_final_hoteleros.superadmin.adapters.UsuariosAdapter;
import com.example.proyecto_final_hoteleros.superadmin.models.Usuario;

import java.util.ArrayList;
import java.util.List;

public class UsuariosFragment extends Fragment {

    private RecyclerView rvUsuarios;
    private UsuariosAdapter usuariosAdapter;
    private LinearLayout layoutEmptyState;

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

        // Debug: verificar que se encontraron las vistas
        android.util.Log.d("UsuariosFragment", "rvUsuarios: " + (rvUsuarios != null ? "OK" : "NULL"));
        android.util.Log.d("UsuariosFragment", "layoutEmptyState: " + (layoutEmptyState != null ? "OK" : "NULL"));

        // Configurar botón de back
        ImageView ivBack = view.findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
            android.util.Log.d("UsuariosFragment", "ivBack configurado correctamente");
        } else {
            android.util.Log.e("UsuariosFragment", "ivBack es null!");
        }

        // Configurar botón de filtro
        ImageView ivFilter = view.findViewById(R.id.iv_filter);
        if (ivFilter != null) {
            ivFilter.setOnClickListener(v -> showFilterOptions());
            android.util.Log.d("UsuariosFragment", "ivFilter configurado correctamente");
        } else {
            android.util.Log.e("UsuariosFragment", "ivFilter es null!");
        }

        android.util.Log.d("UsuariosFragment", "=== FIN initViews ===");
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
        android.util.Log.d("UsuariosFragment", "Cargando datos...");
        try {
            // Datos de prueba - después conectar con Firebase
            List<Usuario> usuarios = new ArrayList<>();

            // Clientes
            usuarios.add(new Usuario("1", "Ana García", "ana.garcia@email.com", "CLIENTE", true, "15/05/2025"));
            usuarios.add(new Usuario("2", "Luis Martínez", "luis.martinez@email.com", "CLIENTE", true, "12/05/2025"));
            usuarios.add(new Usuario("3", "Carmen López", "carmen.lopez@email.com", "CLIENTE", false, "10/05/2025"));

            // Administradores de hotel
            usuarios.add(new Usuario("4", "María Rodríguez", "maria.rodriguez@email.com", "ADMIN_HOTEL", true, "08/05/2025"));
            usuarios.add(new Usuario("5", "Carlos Mendoza", "carlos.mendoza@email.com", "ADMIN_HOTEL", true, "05/05/2025"));

            // Taxistas
            usuarios.add(new Usuario("6", "Pedro Silva", "pedro.silva@email.com", "TAXISTA", true, "03/05/2025"));
            usuarios.add(new Usuario("7", "José Ramírez", "jose.ramirez@email.com", "TAXISTA", false, "01/05/2025"));
            usuarios.add(new Usuario("8", "Roberto Torres", "roberto.torres@email.com", "TAXISTA", true, "28/04/2025"));

            android.util.Log.d("UsuariosFragment", "Datos creados: " + usuarios.size() + " usuarios");

            if (usuariosAdapter != null) {
                usuariosAdapter.updateData(usuarios);
                android.util.Log.d("UsuariosFragment", "Adapter actualizado");
            } else {
                android.util.Log.e("UsuariosFragment", "usuariosAdapter es null!");
            }

            // Verificar que las vistas no sean null antes de usarlas
            if (layoutEmptyState != null && rvUsuarios != null) {
                if (usuarios.isEmpty()) {
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    rvUsuarios.setVisibility(View.GONE);
                    android.util.Log.d("UsuariosFragment", "Mostrando estado vacío");
                } else {
                    layoutEmptyState.setVisibility(View.GONE);
                    rvUsuarios.setVisibility(View.VISIBLE);
                    android.util.Log.d("UsuariosFragment", "Mostrando lista de usuarios");
                }
            }

            android.util.Log.d("UsuariosFragment", "Datos cargados exitosamente");
        } catch (Exception e) {
            android.util.Log.e("UsuariosFragment", "Error cargando datos: " + e.getMessage());
            e.printStackTrace();
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