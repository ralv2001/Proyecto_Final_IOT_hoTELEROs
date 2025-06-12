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
import com.bumptech.glide.Glide;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity;
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
                android.util.Log.d("TaxistaDocuments", "Back button clicked");
                if (getActivity() instanceof SuperAdminActivity) {
                    ((SuperAdminActivity) getActivity()).navigateBackToTaxistas();
                }
            });
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
        android.util.Log.d("TaxistaDocuments", "=== CARGANDO IMÁGENES ===");
        android.util.Log.d("TaxistaDocuments", "PhotoURL: " + taxista.getProfileImageUrl());
        android.util.Log.d("TaxistaDocuments", "DocumentURL: " + taxista.getBreveteImageUrl());

        // Cargar foto de perfil con Glide
        if (ivProfilePhoto != null) {
            if (taxista.getProfileImageUrl() != null && !taxista.getProfileImageUrl().isEmpty()) {
                android.util.Log.d("TaxistaDocuments", "📷 Cargando foto de perfil desde: " + taxista.getProfileImageUrl());

                com.bumptech.glide.Glide.with(this)
                        .load(taxista.getProfileImageUrl())
                        .placeholder(R.drawable.ic_person) // Placeholder mientras carga
                        .error(R.drawable.ic_image_placeholder) // Error si falla
                        .centerCrop()
                        .into(ivProfilePhoto);
            } else {
                android.util.Log.w("TaxistaDocuments", "❌ No hay URL de foto de perfil");
                ivProfilePhoto.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            android.util.Log.e("TaxistaDocuments", "ivProfilePhoto es null!");
        }

        // Cargar documento (PDF como icono, no imagen)
        if (ivBrevetePhoto != null) {
            if (taxista.getBreveteImageUrl() != null && !taxista.getBreveteImageUrl().isEmpty()) {
                android.util.Log.d("TaxistaDocuments", "📄 Documento PDF disponible: " + taxista.getBreveteImageUrl());

                // Para PDFs, mostrar icono de documento en lugar de intentar cargar como imagen
                if (taxista.getBreveteImageUrl().toLowerCase().contains(".pdf")) {
                    ivBrevetePhoto.setImageResource(R.drawable.ic_document);
                } else {
                    // Si no es PDF, intentar cargar como imagen
                    com.bumptech.glide.Glide.with(this)
                            .load(taxista.getBreveteImageUrl())
                            .placeholder(R.drawable.ic_document)
                            .error(R.drawable.ic_image_placeholder)
                            .centerCrop()
                            .into(ivBrevetePhoto);
                }
            } else {
                android.util.Log.w("TaxistaDocuments", "❌ No hay URL de documento");
                ivBrevetePhoto.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            android.util.Log.e("TaxistaDocuments", "ivBrevetePhoto es null!");
        }
    }

    private void showFullScreenImage(String title, String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            android.widget.Toast.makeText(getContext(), "No hay imagen disponible", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Si es un PDF, abrir en navegador o visor externo
        if (imageUrl.toLowerCase().contains(".pdf")) {
            android.widget.Toast.makeText(getContext(), "Abriendo documento PDF...", android.widget.Toast.LENGTH_SHORT).show();

            try {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse(imageUrl));
                startActivity(intent);
            } catch (Exception e) {
                android.widget.Toast.makeText(getContext(), "No se puede abrir el documento", android.widget.Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Para imágenes, crear dialog simple sin layout personalizado
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());

        // Crear vista programáticamente
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        // Título
        android.widget.TextView titleView = new android.widget.TextView(getContext());
        titleView.setText(title);
        titleView.setTextSize(18);
        titleView.setGravity(android.view.Gravity.CENTER);
        titleView.setPadding(0, 0, 0, 20);
        layout.addView(titleView);

        // ImageView
        android.widget.ImageView imageView = new android.widget.ImageView(getContext());
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                600 // Altura fija
        );
        imageView.setLayoutParams(params);
        imageView.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        layout.addView(imageView);

        // Cargar imagen con Glide
        com.bumptech.glide.Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_no_image)
                .into(imageView);

        builder.setView(layout)
                .setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showError(String message) {
        android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_LONG).show();
    }
}