package com.example.proyecto_final_hoteleros.superadmin.fragment;


import android.os.Bundle;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.adapters.AdminsAdapter;
import com.example.proyecto_final_hoteleros.superadmin.models.AdminUser;

import java.util.ArrayList;
import java.util.List;

public class AdminsFragment extends Fragment {

    private RecyclerView rvAdmins;
    private AdminsAdapter adminsAdapter;
    private FloatingActionButton fabAddAdmin;
    private LinearLayout layoutEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admins, container, false);

        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadData();

        return view;
    }

    private void initViews(View view) {
        android.util.Log.d("AdminsFragment", "=== INICIO initViews ===");

        rvAdmins = view.findViewById(R.id.rv_admins);
        fabAddAdmin = view.findViewById(R.id.fab_add_admin);
        layoutEmptyState = view.findViewById(R.id.tv_empty_state);

        // Debug: verificar que se encontraron las vistas
        android.util.Log.d("AdminsFragment", "rvAdmins: " + (rvAdmins != null ? "OK" : "NULL"));
        android.util.Log.d("AdminsFragment", "fabAddAdmin: " + (fabAddAdmin != null ? "OK" : "NULL"));
        android.util.Log.d("AdminsFragment", "tvEmptyState: " + (layoutEmptyState != null ? "OK" : "NULL"));

        // Configurar botón de back
        ImageView ivBack = view.findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
            android.util.Log.d("AdminsFragment", "ivBack configurado correctamente");
        } else {
            android.util.Log.e("AdminsFragment", "ivBack es null!");
        }

        // Configurar botón de búsqueda
        ImageView ivSearch = view.findViewById(R.id.iv_search);
        if (ivSearch != null) {
            ivSearch.setOnClickListener(v -> {
                android.widget.Toast.makeText(getContext(), "Búsqueda - Próximamente", android.widget.Toast.LENGTH_SHORT).show();
            });
            android.util.Log.d("AdminsFragment", "ivSearch configurado correctamente");
        } else {
            android.util.Log.e("AdminsFragment", "ivSearch es null!");
        }

        android.util.Log.d("AdminsFragment", "=== FIN initViews ===");
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
        // Datos de prueba - después conectar con Firebase
        List<AdminUser> admins = new ArrayList<>();
        admins.add(new AdminUser("1", "María García", "maria.garcia@email.com", "Hotel Plaza", true));
        admins.add(new AdminUser("2", "Carlos López", "carlos.lopez@email.com", "Hotel Seaside", true));
        admins.add(new AdminUser("3", "Ana Martínez", "ana.martinez@email.com", "Hotel Mountain", false));
        admins.add(new AdminUser("4", "Luis Rodríguez", "luis.rodriguez@email.com", "Hotel Downtown", true));

        adminsAdapter.updateData(admins);

        // Verificar que las vistas no sean null antes de usarlas
        if (layoutEmptyState != null && rvAdmins != null) {
            // Mostrar/ocultar estado vacío
            if (admins.isEmpty()) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                rvAdmins.setVisibility(View.GONE);
            } else {
                layoutEmptyState.setVisibility(View.GONE);
                rvAdmins.setVisibility(View.VISIBLE);
            }
        } else {
            android.util.Log.e("AdminsFragment", "tvEmptyState o rvAdmins es null!");
            if (layoutEmptyState == null) android.util.Log.e("AdminsFragment", "tvEmptyState es null");
            if (rvAdmins == null) android.util.Log.e("AdminsFragment", "rvAdmins es null");
        }
    }

    private void onAdminAction(AdminUser admin, String action) {
        switch (action) {
            case "toggle_status":
                toggleAdminStatus(admin);
                break;
            case "view_details":
                viewAdminDetails(admin);
                break;
            case "edit":
                editAdmin(admin);
                break;
        }
    }

    private void toggleAdminStatus(AdminUser admin) {
        admin.setActive(!admin.isActive());
        adminsAdapter.notifyDataSetChanged();

        String message = admin.isActive() ? "Administrador activado" : "Administrador desactivado";
        android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
    }

    private void viewAdminDetails(AdminUser admin) {
        // Implementar vista de detalles
        android.widget.Toast.makeText(getContext(), "Ver detalles de " + admin.getName(), android.widget.Toast.LENGTH_SHORT).show();
    }

    private void editAdmin(AdminUser admin) {
        // Implementar edición
        android.widget.Toast.makeText(getContext(), "Editar " + admin.getName(), android.widget.Toast.LENGTH_SHORT).show();
    }
}