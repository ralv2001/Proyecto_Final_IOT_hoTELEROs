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
import com.example.proyecto_final_hoteleros.superadmin.adapters.TaxistasAdapter;
import com.example.proyecto_final_hoteleros.superadmin.models.TaxistaUser;
import com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity;
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;
import com.example.proyecto_final_hoteleros.models.UserModel;

import java.util.ArrayList;
import java.util.List;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

public class TaxistasFragment extends Fragment implements TaxistasAdapter.OnTaxistaActionListener {

    private RecyclerView rvTaxistas;
    private TaxistasAdapter taxistasAdapter;
    private LinearLayout layoutEmptyState;
    private FirebaseManager firebaseManager;

    private List<TaxistaUser> allTaxistas = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.superadmin_fragment_taxistas, container, false);

        firebaseManager = FirebaseManager.getInstance();

        initViews(view);
        setupRecyclerView();
        loadDataFromFirebase(); // 🔥 Cambiar a datos reales


        // Al final del método onCreateView(), antes del return view;
        applyInitialFilter();
        return view;
    }


    // 🔥 MÉTODO ACTUALIZADO: Aplicar filtro inicial si viene de dashboard
    private void applyInitialFilter() {
        if (getArguments() != null) {
            String initialFilter = getArguments().getString("initial_filter");
            android.util.Log.d("TaxistasFragment", "Filtro inicial recibido: " + initialFilter);

            if ("PENDING".equals(initialFilter)) {
                // Esperar a que los datos se carguen antes de filtrar
                new android.os.Handler().postDelayed(() -> {
                    android.util.Log.d("TaxistasFragment", "Aplicando filtro inicial: PENDING");
                    filterTaxistasByStatus("PENDING");
                }, 1000); // Esperar 1 segundo para que los datos se carguen
            }
        }
    }

    private void initViews(View view) {
        android.util.Log.d("TaxistasFragment", "=== INICIO initViews ===");

        rvTaxistas = view.findViewById(R.id.rv_taxistas);
        layoutEmptyState = view.findViewById(R.id.tv_empty_state);

        android.util.Log.d("TaxistasFragment", "rvTaxistas: " + (rvTaxistas != null ? "OK" : "NULL"));
        android.util.Log.d("TaxistasFragment", "layoutEmptyState: " + (layoutEmptyState != null ? "OK" : "NULL"));

        // 🔥 SOLUCION: Configurar botón de back con navegación específica
        ImageView ivBack = view.findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                android.util.Log.d("TaxistasFragment", "Back button clicked");
                if (getActivity() instanceof SuperAdminActivity) {
                    ((SuperAdminActivity) getActivity()).navigateBackToDashboard();
                }
            });
        }

        // Configurar botón de filtro
        ImageView ivFilter = view.findViewById(R.id.iv_filter);
        if (ivFilter != null) {
            ivFilter.setOnClickListener(v -> showFilterOptions());
            android.util.Log.d("TaxistasFragment", "ivFilter configurado correctamente");
        } else {
            android.util.Log.e("TaxistasFragment", "ivFilter es null!");
        }

        android.util.Log.d("TaxistasFragment", "=== FIN initViews ===");
    }

    private void setupRecyclerView() {
        android.util.Log.d("TaxistasFragment", "Configurando RecyclerView...");
        try {
            rvTaxistas.setLayoutManager(new LinearLayoutManager(getContext()));
            taxistasAdapter = new TaxistasAdapter(new ArrayList<>(), this); // Pasar this como listener
            rvTaxistas.setAdapter(taxistasAdapter);
            android.util.Log.d("TaxistasFragment", "RecyclerView configurado exitosamente");
        } catch (Exception e) {
            android.util.Log.e("TaxistasFragment", "Error configurando RecyclerView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 🔥 NUEVO MÉTODO: Cargar datos reales desde Firebase
    // 🔥 MÉTODO ACTUALIZADO: Cargar todos los taxistas
    private void loadDataFromFirebase() {
        android.util.Log.d("TaxistasFragment", "Cargando TODOS los taxistas desde Firebase...");
        showLoading(true);

        firebaseManager.getAllDrivers(new FirebaseManager.DriverListCallback() {
            @Override
            public void onSuccess(List<UserModel> allDrivers) {
                android.util.Log.d("TaxistasFragment", "✅ Todos los taxistas obtenidos: " + allDrivers.size());

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Convertir UserModel a TaxistaUser
                        List<TaxistaUser> taxistas = convertUserModelsToTaxistaUsersWithStatus(allDrivers);
                        updateTaxistasList(taxistas);
                        showLoading(false);
                    });
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("TaxistasFragment", "❌ Error obteniendo todos los taxistas: " + error);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showError("Error cargando taxistas: " + error);
                        showLoading(false);
                    });
                }
            }
        });
    }

    // 🔥 NUEVO MÉTODO: Convertir UserModel a TaxistaUser
    private List<TaxistaUser> convertUserModelsToTaxistaUsers(List<UserModel> userModels) {
        List<TaxistaUser> taxistas = new ArrayList<>();

        for (UserModel userModel : userModels) {
            TaxistaUser taxista = new TaxistaUser();
            taxista.setId(userModel.getUserId());
            taxista.setName(userModel.getNombres());
            taxista.setApellidos(userModel.getApellidos());
            taxista.setEmail(userModel.getEmail());
            taxista.setPhoneNumber(userModel.getTelefono());
            taxista.setDomicilio(userModel.getDireccion());
            taxista.setDocumentNumber(userModel.getNumeroDocumento());
            taxista.setLicensePlate(userModel.getPlacaVehiculo());
            taxista.setProfileImageUrl(userModel.getPhotoUrl()); // O usar getters correctos cuando los sepamos
            taxista.setBreveteImageUrl(userModel.getDocumentUrl()); // O usar getters correctos cuando los sepamos
            taxista.setStatus("PENDING"); // Todos los de pending_drivers están pendientes
            taxista.setRegistrationDate(formatTimestamp(userModel.getCreatedAt()));
            taxista.setTipoDocumento(userModel.getTipoDocumento()); // ✅ CORREGIDO
            taxista.setFechaNacimiento(userModel.getFechaNacimiento()); // ✅ CORREGIDO

            taxistas.add(taxista);
        }

        return taxistas;
    }

    // 🔥 NUEVO MÉTODO: Formatear timestamp
    private String formatTimestamp(long timestamp) {
        if (timestamp == 0) return "Fecha no disponible";

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }

    // 🔥 MÉTODO ACTUALIZADO: Actualizar lista de taxistas
    private void updateTaxistasList(List<TaxistaUser> taxistas) {
        // ✅ IMPORTANTE: Almacenar todos los taxistas para filtros
        this.allTaxistas = new ArrayList<>(taxistas);

        if (taxistasAdapter != null) {
            taxistasAdapter.updateData(taxistas);
            android.util.Log.d("TaxistasFragment", "Adapter actualizado con " + taxistas.size() + " taxistas");
        }

        // Mostrar/ocultar estado vacío
        if (layoutEmptyState != null && rvTaxistas != null) {
            if (taxistas.isEmpty()) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                rvTaxistas.setVisibility(View.GONE);
                android.util.Log.d("TaxistasFragment", "Mostrando estado vacío");
            } else {
                layoutEmptyState.setVisibility(View.GONE);
                rvTaxistas.setVisibility(View.VISIBLE);
                android.util.Log.d("TaxistasFragment", "Mostrando lista de taxistas");
            }
        }
    }

    // 🔥 NUEVO MÉTODO: Mostrar/ocultar loading
    private void showLoading(boolean show) {
        // Puedes agregar un ProgressBar al layout si quieres
        android.util.Log.d("TaxistasFragment", "Loading: " + show);
    }

    // 🔥 NUEVO MÉTODO: Mostrar error
    private void showError(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_LONG).show();
        }
    }

    // 🔥 IMPLEMENTAR INTERFACE: Acciones de taxista
    @Override
    public void onTaxistaAction(TaxistaUser taxista, String action) {
        android.util.Log.d("TaxistasFragment", "Acción: " + action + " para taxista: " + taxista.getName());
        try {
            switch (action) {
                case "approve":
                    showApprovalConfirmation(taxista);
                    break;
                case "reject":
                    showRejectionDialog(taxista);
                    break;
                case "view_details":
                    viewTaxistaDetails(taxista);
                    break;
                case "view_documents":
                    viewDocuments(taxista);
                    break;
                case "contact":
                    contactTaxista(taxista);
                    break;
                case "view_trips":
                    viewTrips(taxista);
                    break;
                default:
                    android.util.Log.w("TaxistasFragment", "Acción no reconocida: " + action);
                    break;
            }
        } catch (Exception e) {
            android.util.Log.e("TaxistasFragment", "Error en onTaxistaAction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 🔥 NUEVO MÉTODO: Mostrar confirmación de aprobación
    private void showApprovalConfirmation(TaxistaUser taxista) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Aprobar Taxista")
                .setMessage("¿Estás seguro que deseas aprobar a " + taxista.getFullName() + "?\n\n" +
                        "📧 " + taxista.getEmail() + "\n" +
                        "🚗 " + taxista.getLicensePlate())
                .setPositiveButton("✅ APROBAR", (dialog, which) -> {
                    approveTaxistaInFirebase(taxista);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // 🔥 NUEVO MÉTODO: Mostrar diálogo de rechazo
    private void showRejectionDialog(TaxistaUser taxista) {
        android.widget.EditText reasonInput = new android.widget.EditText(getContext());
        reasonInput.setHint("Motivo del rechazo (opcional)");
        reasonInput.setMinLines(2);

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Rechazar Taxista")
                .setMessage("¿Estás seguro que deseas rechazar a " + taxista.getFullName() + "?")
                .setView(reasonInput)
                .setPositiveButton("❌ RECHAZAR", (dialog, which) -> {
                    String reason = reasonInput.getText().toString().trim();
                    rejectTaxistaInFirebase(taxista, reason);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // 🔥 NUEVO MÉTODO: Aprobar taxista en Firebase
    private void approveTaxistaInFirebase(TaxistaUser taxista) {
        android.util.Log.d("TaxistasFragment", "Aprobando taxista: " + taxista.getId());
        showLoading(true);

        // Convertir TaxistaUser de vuelta a UserModel
        UserModel userModel = convertTaxistaUserToUserModel(taxista);

        firebaseManager.approveDriver(taxista.getId(), userModel, new FirebaseManager.DataCallback() {
            @Override
            public void onSuccess() {
                android.util.Log.d("TaxistasFragment", "✅ Taxista aprobado exitosamente");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        android.widget.Toast.makeText(getContext(),
                                "✅ " + taxista.getFullName() + " aprobado exitosamente",
                                android.widget.Toast.LENGTH_SHORT).show();

                        // Recargar la lista
                        loadDataFromFirebase();
                    });
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("TaxistasFragment", "❌ Error aprobando taxista: " + error);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        android.widget.Toast.makeText(getContext(),
                                "❌ Error aprobando taxista: " + error,
                                android.widget.Toast.LENGTH_LONG).show();
                        showLoading(false);
                    });
                }
            }
        });
    }

    // 🔥 NUEVO MÉTODO: Rechazar taxista en Firebase
    private void rejectTaxistaInFirebase(TaxistaUser taxista, String reason) {
        android.util.Log.d("TaxistasFragment", "Rechazando taxista: " + taxista.getId() + " - Motivo: " + reason);
        showLoading(true);

        UserModel userModel = convertTaxistaUserToUserModel(taxista);

        firebaseManager.rejectDriver(taxista.getId(), userModel, reason, new FirebaseManager.DataCallback() {
            @Override
            public void onSuccess() {
                android.util.Log.d("TaxistasFragment", "✅ Taxista rechazado exitosamente");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        android.widget.Toast.makeText(getContext(),
                                "❌ " + taxista.getFullName() + " rechazado",
                                android.widget.Toast.LENGTH_SHORT).show();

                        // Recargar la lista
                        loadDataFromFirebase();
                    });
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("TaxistasFragment", "❌ Error rechazando taxista: " + error);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        android.widget.Toast.makeText(getContext(),
                                "❌ Error rechazando taxista: " + error,
                                android.widget.Toast.LENGTH_LONG).show();
                        showLoading(false);
                    });
                }
            }
        });
    }

    // 🔥 NUEVO MÉTODO: Convertir TaxistaUser a UserModel
    // 🔥 MÉTODO CORREGIDO: Convertir TaxistaUser a UserModel
    // 🔥 MÉTODO CORREGIDO: Convertir TaxistaUser a UserModel
    private UserModel convertTaxistaUserToUserModel(TaxistaUser taxista) {
        UserModel userModel = new UserModel();
        userModel.setUserId(taxista.getId());
        userModel.setNombres(taxista.getName());
        userModel.setApellidos(taxista.getApellidos());
        userModel.setEmail(taxista.getEmail());
        userModel.setTelefono(taxista.getPhoneNumber());
        userModel.setDireccion(taxista.getDomicilio());
        userModel.setNumeroDocumento(taxista.getDocumentNumber());
        userModel.setPlacaVehiculo(taxista.getLicensePlate());

        // ✅ AGREGAR DATOS FALTANTES
        userModel.setTipoDocumento(taxista.getTipoDocumento());
        userModel.setFechaNacimiento(taxista.getFechaNacimiento());

        // ✅ PRESERVAR URLs DE AWS
        userModel.setPhotoUrl(taxista.getProfileImageUrl());
        userModel.setDocumentUrl(taxista.getBreveteImageUrl());

        userModel.setUserType("driver");
        userModel.setActive(true);

        android.util.Log.d("TaxistasFragment", "💾 Preservando URLs en aprobación:");
        android.util.Log.d("TaxistasFragment", "📷 PhotoURL: " + taxista.getProfileImageUrl());
        android.util.Log.d("TaxistasFragment", "📄 DocumentURL: " + taxista.getBreveteImageUrl());

        return userModel;
    }

    // MÉTODOS EXISTENTES (sin cambios)
    private void viewTaxistaDetails(TaxistaUser taxista) {
        android.widget.Toast.makeText(getContext(), "Ver detalles de " + taxista.getName(), android.widget.Toast.LENGTH_SHORT).show();
    }

    private void viewDocuments(TaxistaUser taxista) {
        android.util.Log.d("TaxistasFragment", "Ver documentos de: " + taxista.getName());

        // Navegar al fragment de documentos
        if (getActivity() instanceof SuperAdminActivity) {
            TaxistaDocumentsFragment documentsFragment = TaxistaDocumentsFragment.newInstance(taxista);
            // 🔥 IMPORTANTE: Usar true para agregar al back stack
            ((SuperAdminActivity) getActivity()).loadFragment(documentsFragment, "TAXISTA_DOCUMENTS", true);
        }
    }

    private void contactTaxista(TaxistaUser taxista) {
        android.widget.Toast.makeText(getContext(), "Contactar a " + taxista.getName(), android.widget.Toast.LENGTH_SHORT).show();
    }

    private void viewTrips(TaxistaUser taxista) {
        android.widget.Toast.makeText(getContext(), "Ver viajes de " + taxista.getName(), android.widget.Toast.LENGTH_SHORT).show();
    }

    private void showFilterOptions() {
        String[] options = {"Todos", "Pendientes", "Aprobados"};
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Filtrar por estado")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Todos
                            android.util.Log.d("TaxistasFragment", "Filtro: Mostrar todos");
                            if (taxistasAdapter != null) {
                                taxistasAdapter.showAllTaxistas(); // ✅ Usar método del adapter
                            }
                            android.widget.Toast.makeText(getContext(),
                                    "Mostrando todos los taxistas", android.widget.Toast.LENGTH_SHORT).show();
                            break;
                        case 1: // Pendientes
                            android.util.Log.d("TaxistasFragment", "Filtro: Solo pendientes");
                            filterTaxistasByStatus("PENDING");
                            break;
                        case 2: // Aprobados
                            android.util.Log.d("TaxistasFragment", "Filtro: Solo aprobados");
                            filterTaxistasByStatus("APPROVED");
                            break;
                    }
                })
                .show();
    }

    // ✅ NUEVO MÉTODO: Filtrar por estado
    // ✅ MÉTODO MEJORADO: Filtrar por estado
    private void filterTaxistasByStatus(String status) {
        if (taxistasAdapter == null) {
            android.widget.Toast.makeText(getContext(), "Error: Adapter no inicializado", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        List<TaxistaUser> allTaxistas = taxistasAdapter.getAllTaxistas();
        if (allTaxistas.isEmpty()) {
            android.widget.Toast.makeText(getContext(), "No hay taxistas para filtrar", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        List<TaxistaUser> filteredTaxistas = new ArrayList<>();

        for (TaxistaUser taxista : allTaxistas) {
            if (status.equals(taxista.getStatus())) {
                filteredTaxistas.add(taxista);
            }
        }

        taxistasAdapter.updateList(filteredTaxistas);

        String statusText = status.equals("PENDING") ? "pendientes" : "aprobados";

        android.util.Log.d("TaxistasFragment", "Filtro aplicado - " + statusText + ": " + filteredTaxistas.size());
    }

    // 🔥 MÉTODO PÚBLICO: Para actualizar desde SuperAdminActivity
    public void updatePendingDrivers(List<UserModel> pendingDrivers) {
        android.util.Log.d("TaxistasFragment", "Actualizando lista desde SuperAdminActivity: " + pendingDrivers.size());

        List<TaxistaUser> taxistas = convertUserModelsToTaxistaUsers(pendingDrivers);
        updateTaxistasList(taxistas);
    }

    // 🔥 MÉTODO PÚBLICO: Para refrescar datos
    public void refreshData() {
        loadDataFromFirebase();
    }

    // 🔥 NUEVO MÉTODO: Convertir con estados
    private List<TaxistaUser> convertUserModelsToTaxistaUsersWithStatus(List<UserModel> userModels) {
        List<TaxistaUser> taxistas = new ArrayList<>();

        for (UserModel userModel : userModels) {
            TaxistaUser taxista = new TaxistaUser();
            taxista.setId(userModel.getUserId());
            taxista.setName(userModel.getNombres());
            taxista.setApellidos(userModel.getApellidos());
            taxista.setEmail(userModel.getEmail());
            taxista.setPhoneNumber(userModel.getTelefono());
            taxista.setDomicilio(userModel.getDireccion());
            taxista.setDocumentNumber(userModel.getNumeroDocumento());
            taxista.setLicensePlate(userModel.getPlacaVehiculo());
            taxista.setProfileImageUrl(userModel.getPhotoUrl());
            taxista.setBreveteImageUrl(userModel.getDocumentUrl());
            taxista.setTipoDocumento(userModel.getTipoDocumento());
            taxista.setFechaNacimiento(userModel.getFechaNacimiento());
            taxista.setRegistrationDate(formatTimestamp(userModel.getCreatedAt()));

            // 🎯 DETERMINAR ESTADO BASADO EN isActive
            if (userModel.isActive()) {
                taxista.setStatus("APPROVED"); // En users collection = aprobado
            } else {
                taxista.setStatus("PENDING");   // En pending_drivers = pendiente
            }

            taxistas.add(taxista);
        }

        return taxistas;
    }






}