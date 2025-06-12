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

import java.util.ArrayList;
import java.util.List;

public class TaxistasFragment extends Fragment {

    private RecyclerView rvTaxistas;
    private TaxistasAdapter taxistasAdapter;
    private LinearLayout layoutEmptyState;  // Cambiado de tvEmptyState

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_taxistas, container, false);

        initViews(view);
        setupRecyclerView();
        loadData();

        return view;
    }

    private void initViews(View view) {
        android.util.Log.d("TaxistasFragment", "=== INICIO initViews ===");

        rvTaxistas = view.findViewById(R.id.rv_taxistas);
        layoutEmptyState = view.findViewById(R.id.tv_empty_state);  // Cambiado

        // Debug: verificar que se encontraron las vistas
        android.util.Log.d("TaxistasFragment", "rvTaxistas: " + (rvTaxistas != null ? "OK" : "NULL"));
        android.util.Log.d("TaxistasFragment", "layoutEmptyState: " + (layoutEmptyState != null ? "OK" : "NULL"));

        // Configurar botón de back
        ImageView ivBack = view.findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
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
            taxistasAdapter = new TaxistasAdapter(new ArrayList<>(), this::onTaxistaAction);
            rvTaxistas.setAdapter(taxistasAdapter);
            android.util.Log.d("TaxistasFragment", "RecyclerView configurado exitosamente");
        } catch (Exception e) {
            android.util.Log.e("TaxistasFragment", "Error configurando RecyclerView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadData() {
        android.util.Log.d("TaxistasFragment", "Cargando datos...");
        try {
            // Datos de prueba - después conectar con Firebase
            List<TaxistaUser> taxistas = new ArrayList<>();

            // Taxista 1 - Pendiente
            TaxistaUser taxista1 = new TaxistaUser();
            taxista1.setId("1");
            taxista1.setName("Carlos");
            taxista1.setApellidos("Mendoza Silva");
            taxista1.setEmail("carlos.mendoza@email.com");
            taxista1.setLicensePlate("ABC-123");
            taxista1.setStatus("PENDING");
            taxista1.setRegistrationDate("12/06/2025");
            taxista1.setTipoDocumento("DNI");
            taxista1.setDocumentNumber("12345678");
            taxista1.setFechaNacimiento("15/03/1985");
            taxista1.setPhoneNumber("+51 987 654 321");
            taxista1.setDomicilio("Av. Lima 123, San Isidro, Lima");
            taxista1.setProfileImageUrl("profile_1.jpg");
            taxista1.setCarImageUrl("car_1.jpg");
            taxista1.setBreveteImageUrl("brevete_1.jpg");
            taxistas.add(taxista1);

            // Taxista 2 - Aprobado
            TaxistaUser taxista2 = new TaxistaUser();
            taxista2.setId("2");
            taxista2.setName("Luis");
            taxista2.setApellidos("García Pérez");
            taxista2.setEmail("luis.garcia@email.com");
            taxista2.setLicensePlate("XYZ-456");
            taxista2.setStatus("APPROVED");
            taxista2.setRegistrationDate("10/06/2025");
            taxista2.setTipoDocumento("DNI");
            taxista2.setDocumentNumber("87654321");
            taxista2.setFechaNacimiento("22/08/1980");
            taxista2.setPhoneNumber("+51 912 345 678");
            taxista2.setDomicilio("Jr. Bolivar 456, Miraflores, Lima");
            taxista2.setProfileImageUrl("profile_2.jpg");
            taxista2.setCarImageUrl("car_2.jpg");
            taxista2.setBreveteImageUrl("brevete_2.jpg");
            taxistas.add(taxista2);

            // Taxista 3 - Pendiente
            TaxistaUser taxista3 = new TaxistaUser();
            taxista3.setId("3");
            taxista3.setName("Ana");
            taxista3.setApellidos("Rodríguez López");
            taxista3.setEmail("ana.rodriguez@email.com");
            taxista3.setLicensePlate("DEF-789");
            taxista3.setStatus("PENDING");
            taxista3.setRegistrationDate("11/06/2025");
            taxista3.setTipoDocumento("PASAPORTE");
            taxista3.setDocumentNumber("PA123456");
            taxista3.setFechaNacimiento("10/12/1992");
            taxista3.setPhoneNumber("+51 965 432 187");
            taxista3.setDomicilio("Av. Arequipa 789, La Victoria, Lima");
            taxista3.setProfileImageUrl("profile_3.jpg");
            taxista3.setCarImageUrl("car_3.jpg");
            taxista3.setBreveteImageUrl("brevete_3.jpg");
            taxistas.add(taxista3);

            // Taxista 4 - Rechazado
            TaxistaUser taxista4 = new TaxistaUser();
            taxista4.setId("4");
            taxista4.setName("Miguel");
            taxista4.setApellidos("Torres Vargas");
            taxista4.setEmail("miguel.torres@email.com");
            taxista4.setLicensePlate("GHI-012");
            taxista4.setStatus("REJECTED");
            taxista4.setRegistrationDate("09/06/2025");
            taxista4.setTipoDocumento("CARNET_EXTRANJERIA");
            taxista4.setDocumentNumber("CE789456");
            taxista4.setFechaNacimiento("05/07/1988");
            taxista4.setPhoneNumber("+51 934 567 890");
            taxista4.setDomicilio("Calle Real 321, Surco, Lima");
            taxista4.setProfileImageUrl("profile_4.jpg");
            taxista4.setCarImageUrl("car_4.jpg");
            taxista4.setBreveteImageUrl("brevete_4.jpg");
            taxistas.add(taxista4);

            android.util.Log.d("TaxistasFragment", "Datos creados: " + taxistas.size() + " taxistas");

            if (taxistasAdapter != null) {
                taxistasAdapter.updateData(taxistas);
                android.util.Log.d("TaxistasFragment", "Adapter actualizado");
            } else {
                android.util.Log.e("TaxistasFragment", "taxistasAdapter es null!");
            }

            // Verificar que las vistas no sean null antes de usarlas
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

            android.util.Log.d("TaxistasFragment", "Datos cargados exitosamente");
        } catch (Exception e) {
            android.util.Log.e("TaxistasFragment", "Error cargando datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void onTaxistaAction(TaxistaUser taxista, String action) {
        android.util.Log.d("TaxistasFragment", "Acción: " + action + " para taxista: " + taxista.getName());
        try {
            switch (action) {
                case "approve":
                    approveTaxista(taxista);
                    break;
                case "reject":
                    rejectTaxista(taxista);
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

    private void approveTaxista(TaxistaUser taxista) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Aprobar taxista")
                .setMessage("¿Estás seguro que deseas aprobar a " + taxista.getName() + "?")
                .setPositiveButton("Aprobar", (dialog, which) -> {
                    taxista.setStatus("APPROVED");
                    taxistasAdapter.notifyDataSetChanged();
                    android.widget.Toast.makeText(getContext(), "Taxista aprobado", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void rejectTaxista(TaxistaUser taxista) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Rechazar taxista")
                .setMessage("¿Estás seguro que deseas rechazar a " + taxista.getName() + "?")
                .setPositiveButton("Rechazar", (dialog, which) -> {
                    taxista.setStatus("REJECTED");
                    taxistasAdapter.notifyDataSetChanged();
                    android.widget.Toast.makeText(getContext(), "Taxista rechazado", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void viewTaxistaDetails(TaxistaUser taxista) {
        android.widget.Toast.makeText(getContext(), "Ver detalles de " + taxista.getName(), android.widget.Toast.LENGTH_SHORT).show();
    }

    private void viewDocuments(TaxistaUser taxista) {
        android.util.Log.d("TaxistasFragment", "Ver documentos de: " + taxista.getName());

        // Navegar al fragment de documentos
        if (getActivity() instanceof com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity) {
            TaxistaDocumentsFragment documentsFragment = TaxistaDocumentsFragment.newInstance(taxista);
            ((com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity) getActivity())
                    .loadFragment(documentsFragment, "TAXISTA_DOCUMENTS", true);
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
                    // Implementar filtrado
                    android.widget.Toast.makeText(getContext(), "Filtro: " + options[which], android.widget.Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}