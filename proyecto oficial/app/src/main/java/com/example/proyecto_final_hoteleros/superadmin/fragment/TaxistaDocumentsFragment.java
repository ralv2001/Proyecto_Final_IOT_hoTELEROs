package com.example.proyecto_final_hoteleros.superadmin.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.models.TaxistaUser;

public class TaxistaDocumentsFragment extends Fragment {

    private TaxistaUser taxista;

    // Views
    private TextView tvNombreCompleto, tvTipoDocumento, tvNumeroDocumento, tvFechaNacimiento;
    private TextView tvEmail, tvTelefono, tvDomicilio, tvLicensePlate;
    private ImageView ivProfilePhoto, ivCarPhoto, ivBrevetePhoto, ivBack;
    private CardView cardProfilePhoto, cardCarPhoto, cardBrevetePhoto;

    public static TaxistaDocumentsFragment newInstance(TaxistaUser taxista) {
        TaxistaDocumentsFragment fragment = new TaxistaDocumentsFragment();
        Bundle args = new Bundle();
        args.putSerializable("taxista", taxista);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_taxista_documents, container, false);

        initViews(view);
        loadTaxistaData();

        return view;
    }

    private void initViews(View view) {
        android.util.Log.d("TaxistaDocuments", "=== INICIO initViews ===");

        // TextViews
        tvNombreCompleto = view.findViewById(R.id.tv_nombre_completo);
        tvTipoDocumento = view.findViewById(R.id.tv_tipo_documento);
        tvNumeroDocumento = view.findViewById(R.id.tv_numero_documento);
        tvFechaNacimiento = view.findViewById(R.id.tv_fecha_nacimiento);
        tvEmail = view.findViewById(R.id.tv_email);
        tvTelefono = view.findViewById(R.id.tv_telefono);
        tvDomicilio = view.findViewById(R.id.tv_domicilio);
        tvLicensePlate = view.findViewById(R.id.tv_license_plate);

        // ImageViews
        ivProfilePhoto = view.findViewById(R.id.iv_profile_photo);
        ivBrevetePhoto = view.findViewById(R.id.iv_brevete_photo);
        ivBack = view.findViewById(R.id.iv_back);

        // CardViews
        cardProfilePhoto = view.findViewById(R.id.card_profile_photo);
        cardBrevetePhoto = view.findViewById(R.id.card_brevete_photo);

        // Debug de las vistas
        android.util.Log.d("TaxistaDocuments", "cardProfilePhoto: " + (cardProfilePhoto != null ? "OK" : "NULL"));
        android.util.Log.d("TaxistaDocuments", "cardBrevetePhoto: " + (cardBrevetePhoto != null ? "OK" : "NULL"));
        android.util.Log.d("TaxistaDocuments", "ivProfilePhoto: " + (ivProfilePhoto != null ? "OK" : "NULL"));
        android.util.Log.d("TaxistaDocuments", "ivBrevetePhoto: " + (ivBrevetePhoto != null ? "OK" : "NULL"));

        // 🔥 SOLUCION: Configurar botón de back con navegación específica
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                android.util.Log.d("TaxistaDocuments", "Back button clicked - navegando a TaxistasFragment");

                // Usar FragmentManager para volver al fragment anterior
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    // Si no hay back stack, navegar específicamente a TaxistasFragment
                    if (getActivity() instanceof com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity) {
                        com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity activity =
                                (com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity) getActivity();
                        activity.navigateBackToTaxistas(); // Método específico que vamos a crear
                    }
                }
            });
            android.util.Log.d("TaxistaDocuments", "ivBack configurado correctamente");
        } else {
            android.util.Log.e("TaxistaDocuments", "ivBack es null!");
        }

        // Click listeners para las fotos
        setupPhotoClickListeners();

        android.util.Log.d("TaxistaDocuments", "=== FIN initViews ===");
    }

    private void setupPhotoClickListeners() {
        if (cardProfilePhoto != null) {
            cardProfilePhoto.setOnClickListener(v -> {
                // Ampliar foto de perfil
                showFullScreenImage("Foto de Perfil", taxista != null ? taxista.getProfileImageUrl() : null);
            });
        } else {
            android.util.Log.e("TaxistaDocuments", "cardProfilePhoto es null!");
        }

        if (cardBrevetePhoto != null) {
            cardBrevetePhoto.setOnClickListener(v -> {
                // Ampliar foto del brevete
                showFullScreenImage("Brevete", taxista != null ? taxista.getBreveteImageUrl() : null);
            });
        } else {
            android.util.Log.e("TaxistaDocuments", "cardBrevetePhoto es null!");
        }
    }

    private void loadTaxistaData() {
        if (getArguments() != null) {
            taxista = (TaxistaUser) getArguments().getSerializable("taxista");

            if (taxista != null) {
                populateFields();
            } else {
                android.util.Log.e("TaxistaDocuments", "TaxistaUser es null");
                showError("Error al cargar los datos del taxista");
            }
        }
    }

    private void populateFields() {
        // Datos personales
        tvNombreCompleto.setText(taxista.getFullName());
        tvTipoDocumento.setText(getDocumentTypeDisplay(taxista.getTipoDocumento()));
        tvNumeroDocumento.setText(taxista.getDocumentNumber() != null ? taxista.getDocumentNumber() : "No especificado");
        tvFechaNacimiento.setText(taxista.getFechaNacimiento() != null ? taxista.getFechaNacimiento() : "No especificado");

        // Datos de contacto
        tvEmail.setText(taxista.getEmail());
        tvTelefono.setText(taxista.getPhoneNumber() != null ? taxista.getPhoneNumber() : "No especificado");
        tvDomicilio.setText(taxista.getDomicilio() != null ? taxista.getDomicilio() : "No especificado");

        // Datos del vehículo
        tvLicensePlate.setText(taxista.getLicensePlate());

        // Cargar fotos
        loadImages();
    }

    private String getDocumentTypeDisplay(String tipoDocumento) {
        if (tipoDocumento == null) return "No especificado";

        switch (tipoDocumento.toUpperCase()) {
            case "DNI":
                return "DNI - Documento Nacional de Identidad";
            case "PASAPORTE":
                return "Pasaporte";
            case "CARNET":
            case "CARNET_EXTRANJERIA":
                return "Carnet de Extranjería";
            default:
                return tipoDocumento;
        }
    }

    private void loadImages() {
        // Cargar foto de perfil
        if (ivProfilePhoto != null) {
            if (taxista.getProfileImageUrl() != null && !taxista.getProfileImageUrl().isEmpty()) {
                ivProfilePhoto.setImageResource(R.drawable.ic_person);
            } else {
                ivProfilePhoto.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            android.util.Log.e("TaxistaDocuments", "ivProfilePhoto es null!");
        }

        // Cargar foto del brevete
        if (ivBrevetePhoto != null) {
            if (taxista.getBreveteImageUrl() != null && !taxista.getBreveteImageUrl().isEmpty()) {
                ivBrevetePhoto.setImageResource(R.drawable.ic_document);
            } else {
                ivBrevetePhoto.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            android.util.Log.e("TaxistaDocuments", "ivBrevetePhoto es null!");
        }
    }

    private void showFullScreenImage(String title, String imageUrl) {
        // Crear dialog para mostrar imagen en pantalla completa
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_full_image, null);
        ImageView imageView = dialogView.findViewById(R.id.iv_full_image);
        TextView titleView = dialogView.findViewById(R.id.tv_image_title);

        titleView.setText(title);

        // Cargar imagen (por ahora placeholder)
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Implementar carga real de imagen
            imageView.setImageResource(R.drawable.ic_image_placeholder);
        } else {
            imageView.setImageResource(R.drawable.ic_no_image);
        }

        builder.setView(dialogView)
                .setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showError(String message) {
        android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_LONG).show();
    }
}