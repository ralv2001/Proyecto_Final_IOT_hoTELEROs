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

public class TaxistasFragment extends Fragment implements TaxistasAdapter.OnTaxistaActionListener {

    private RecyclerView rvTaxistas;
    private TaxistasAdapter taxistasAdapter;
    private LinearLayout layoutEmptyState;
    private FirebaseManager firebaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_taxistas, container, false);

        firebaseManager = FirebaseManager.getInstance();

        initViews(view);
        setupRecyclerView();
        loadDataFromFirebase(); // 🔥 Cambiar a datos reales

        return view;
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
                android.util.Log.d("TaxistasFragment", "Back button clicked - navegando a Dashboard");

                // Usar FragmentManager para volver al dashboard
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    // Si no hay back stack, navegar específicamente al dashboard
                    if (getActivity() instanceof com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity) {
                        com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity activity =
                                (com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity) getActivity();
                        activity.navigateBackToDashboard();
                    }
                }
            });
            android.util.Log.d("TaxistasFragment", "ivBack configurado correctamente");
        } else {
            android.util.Log.e("TaxistasFragment", "ivBack es null!");
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
    private void loadDataFromFirebase() {
        android.util.Log.d("TaxistasFragment", "Cargando taxistas pendientes desde Firebase...");
        showLoading(true);

        firebaseManager.getPendingDrivers(new FirebaseManager.DriverListCallback() {
            @Override
            public void onSuccess(List<UserModel> pendingDrivers) {
                android.util.Log.d("TaxistasFragment", "✅ Taxistas obtenidos: " + pendingDrivers.size());

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Convertir UserModel a TaxistaUser
                        List<TaxistaUser> taxistas = convertUserModelsToTaxistaUsers(pendingDrivers);
                        updateTaxistasList(taxistas);
                        showLoading(false);
                    });
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("TaxistasFragment", "❌ Error obteniendo taxistas: " + error);

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

    // 🔥 NUEVO MÉTODO: Actualizar lista de taxistas
    private void updateTaxistasList(List<TaxistaUser> taxistas) {
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

        userModel.setUserType("driver");  // ✅ YA ESTÁ CORRECTO
        userModel.setActive(true);

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
        String[] options = {"Todos", "Pendientes", "Aprobados", "Rechazados"};
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Filtrar por estado")
                .setItems(options, (dialog, which) -> {
                    android.widget.Toast.makeText(getContext(), "Filtro: " + options[which], android.widget.Toast.LENGTH_SHORT).show();
                    // TODO: Implementar filtrado real
                })
                .show();
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
}