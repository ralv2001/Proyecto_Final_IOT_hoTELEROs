package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.BasicServicePhotosAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceItem;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServiceManagementAdapter extends RecyclerView.Adapter<ServiceManagementAdapter.ServiceViewHolder> {

    private static final String TAG = "ServiceManagementAdapter";

    public interface OnServiceActionListener {
        void onEditService(HotelServiceItem service, int position);
        void onDeleteService(HotelServiceItem service, int position);
        void onToggleService(HotelServiceItem service, int position, boolean isActive);
    }

    public interface OnServicePhotoClickListener {
        void onPhotoClick(String photoUrl, int position, List<String> allPhotos);
    }

    private List<HotelServiceItem> services;
    private OnServiceActionListener actionListener;
    private OnServicePhotoClickListener photoClickListener;
    private NumberFormat currencyFormat;
    private Context context;

    // ✅ CONSTRUCTOR PRINCIPAL QUE NECESITA ServiceManagementFragment
    public ServiceManagementAdapter(Context context, List<HotelServiceItem> services,
                                    OnServiceActionListener actionListener,
                                    OnServicePhotoClickListener photoClickListener) {
        this.context = context;
        this.services = services;
        this.actionListener = actionListener;
        this.photoClickListener = photoClickListener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
    }

    // ✅ CONSTRUCTOR ALTERNATIVO
    public ServiceManagementAdapter(Context context, List<HotelServiceItem> services,
                                    OnServiceActionListener actionListener) {
        this(context, services, actionListener, null);
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_hotel_item_service_management, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        HotelServiceItem service = services.get(position);
        holder.bind(service, position);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public void updateServices(List<HotelServiceItem> newServices) {
        this.services = newServices;
        notifyDataSetChanged();
        android.util.Log.d(TAG, "📋 Servicios actualizados: " + newServices.size());
    }

    public class ServiceViewHolder extends RecyclerView.ViewHolder {

        // Views principales
        private LinearLayout serviceIconContainer;
        private ImageView ivServiceIcon;
        private TextView tvServiceName;
        private TextView tvServiceDescription;
        private TextView tvServiceType;
        private TextView tvServicePrice;
        private TextView tvConditionalInfo;
        private ImageView optionsButton;

        // Views para fotos (EXACTOS como BasicServicesAdapter)
        private LinearLayout photoBadgeContainer, photosExpandableSection;
        private TextView tvPhotoCount, tvPhotosCounter;
        private ImageView ivExpandIcon;
        private RecyclerView rvServicePhotos;
        private BasicServicePhotosAdapter photosAdapter;
        private boolean isPhotosExpanded = false;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            setupRecyclerView();
        }

        private void initViews() {
            // Referencias a las vistas principales
            serviceIconContainer = itemView.findViewById(R.id.serviceIconContainer);
            ivServiceIcon = itemView.findViewById(R.id.ivServiceIcon);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceDescription = itemView.findViewById(R.id.tvServiceDescription);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            tvConditionalInfo = itemView.findViewById(R.id.tvConditionalInfo);
            optionsButton = itemView.findViewById(R.id.optionsButton);

            // Elementos para fotos (EXACTOS como BasicServicesAdapter)
            photoBadgeContainer = itemView.findViewById(R.id.photoBadgeContainer);
            tvPhotoCount = itemView.findViewById(R.id.tvPhotoCount);
            ivExpandIcon = itemView.findViewById(R.id.ivExpandIcon);
            photosExpandableSection = itemView.findViewById(R.id.photosExpandableSection);
            rvServicePhotos = itemView.findViewById(R.id.rvServicePhotos);
            tvPhotosCounter = itemView.findViewById(R.id.tvPhotosCounter);

            android.util.Log.d(TAG, "🔧 ViewHolder inicializado");
        }

        private void setupRecyclerView() {
            if (rvServicePhotos != null) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(
                        context, LinearLayoutManager.HORIZONTAL, false);
                rvServicePhotos.setLayoutManager(layoutManager);
                rvServicePhotos.setNestedScrollingEnabled(false);
                rvServicePhotos.setHasFixedSize(true);
                android.util.Log.d(TAG, "📷 RecyclerView de fotos configurado");
            }
        }

        public void bind(HotelServiceItem service, int position) {
            // Configurar información básica del servicio
            int iconResource = IconHelper.getIconResource(service.getIconKey());
            ivServiceIcon.setImageResource(iconResource);
            tvServiceName.setText(service.getName());

            // Configurar descripción
            if (service.getDescription() != null && !service.getDescription().isEmpty()) {
                tvServiceDescription.setText(service.getDescription());
                tvServiceDescription.setVisibility(View.VISIBLE);
            } else {
                tvServiceDescription.setVisibility(View.GONE);
            }

            // Configurar tipo de servicio
            tvServiceType.setText(service.getTypeLabel());

            // ✅ CORREGIDO: Usar colores que SÍ existen o códigos hex directos
            switch (service.getType()) {
                case INCLUDED:
                case BASIC:
                    tvServicePrice.setText("Gratuito");
                    try {
                        tvServicePrice.setTextColor(ContextCompat.getColor(context, R.color.orange));
                    } catch (Exception e) {
                        tvServicePrice.setTextColor(0xFF4CAF50); // Verde directo
                    }
                    if (tvConditionalInfo != null) {
                        tvConditionalInfo.setVisibility(View.GONE);
                    }
                    break;
                case PAID:
                    tvServicePrice.setText(currencyFormat.format(service.getPrice()));
                    try {
                        tvServicePrice.setTextColor(ContextCompat.getColor(context, R.color.orange));
                    } catch (Exception e) {
                        tvServicePrice.setTextColor(0xFFFF9800); // Naranja directo
                    }
                    if (tvConditionalInfo != null) {
                        tvConditionalInfo.setVisibility(View.GONE);
                    }
                    break;
                case CONDITIONAL:
                    tvServicePrice.setText("Condicional");
                    try {
                        tvServicePrice.setTextColor(ContextCompat.getColor(context, R.color.orange));
                    } catch (Exception e) {
                        tvServicePrice.setTextColor(0xFF2196F3); // Azul directo
                    }
                    if (tvConditionalInfo != null && service.getConditionalAmount() > 0) {
                        tvConditionalInfo.setText("Gratis por compras de " +
                                currencyFormat.format(service.getConditionalAmount()) + " o más");
                        tvConditionalInfo.setVisibility(View.VISIBLE);
                    } else if (tvConditionalInfo != null) {
                        tvConditionalInfo.setVisibility(View.GONE);
                    }
                    break;
            }

            // Configurar fotos del servicio
            setupServicePhotos(service);

            // Configurar botón de opciones
            if (optionsButton != null) {
                optionsButton.setOnClickListener(v -> showOptionsMenu(service, position));
            }

            android.util.Log.d(TAG, "🔧 Servicio vinculado: " + service.getName() +
                    " - Tipo: " + service.getType() +
                    " - Fotos: " + (service.getPhotos() != null ? service.getPhotos().size() : 0));
        }

        private void setupServicePhotos(HotelServiceItem service) {
            // Convertir List<Uri> a List<String>
            List<String> photos = new ArrayList<>();
            if (service.getPhotos() != null && !service.getPhotos().isEmpty()) {
                for (android.net.Uri uri : service.getPhotos()) {
                    if (uri != null) {
                        photos.add(uri.toString());
                    }
                }
            }

            if (photos == null || photos.isEmpty()) {
                // No hay fotos - ocultar badge y sección expandible
                if (photoBadgeContainer != null) {
                    photoBadgeContainer.setVisibility(View.GONE);
                }
                if (photosExpandableSection != null) {
                    photosExpandableSection.setVisibility(View.GONE);
                }
                isPhotosExpanded = false;
                android.util.Log.d(TAG, "📷 Servicio sin fotos: " + service.getName());
            } else {
                // Hay fotos - mostrar badge y configurar funcionalidad
                if (photoBadgeContainer != null) {
                    photoBadgeContainer.setVisibility(View.VISIBLE);

                    // Configurar texto del contador
                    String photoText = photos.size() == 1 ?
                            "1 foto" : photos.size() + " fotos";
                    if (tvPhotoCount != null) {
                        tvPhotoCount.setText(photoText);
                    }

                    if (tvPhotosCounter != null) {
                        tvPhotosCounter.setText(photos.size() + " de " + photos.size());
                    }

                    // Configurar adapter de fotos
                    if (rvServicePhotos != null) {
                        if (photosAdapter == null) {
                            photosAdapter = new BasicServicePhotosAdapter(context, photos,
                                    (photoUrl, pos, allPhotos) -> {
                                        if (photoClickListener != null) {
                                            photoClickListener.onPhotoClick(photoUrl, pos, allPhotos);
                                        }
                                    });
                            rvServicePhotos.setAdapter(photosAdapter);
                        } else {
                            photosAdapter.updatePhotos(photos);
                        }
                    }

                    // Click listener para expandir/colapsar fotos
                    photoBadgeContainer.setOnClickListener(v -> togglePhotosSection());
                }

                android.util.Log.d(TAG, "📷 Servicio con fotos configurado: " + service.getName() +
                        " - " + photos.size() + " fotos");
            }
        }

        private void togglePhotosSection() {
            if (isPhotosExpanded) {
                collapsePhotosSection();
            } else {
                expandPhotosSection();
            }
            isPhotosExpanded = !isPhotosExpanded;
            android.util.Log.d(TAG, "📷 Toggle fotos: " + (isPhotosExpanded ? "expandido" : "colapsado"));
        }

        private void expandPhotosSection() {
            if (photosExpandableSection != null && ivExpandIcon != null) {
                photosExpandableSection.setVisibility(View.VISIBLE);

                // Animar icono
                RotateAnimation rotate = new RotateAnimation(0, 180,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(200);
                rotate.setFillAfter(true);
                ivExpandIcon.startAnimation(rotate);
            }
        }

        private void collapsePhotosSection() {
            if (photosExpandableSection != null && ivExpandIcon != null) {
                photosExpandableSection.setVisibility(View.GONE);

                // Animar icono
                RotateAnimation rotate = new RotateAnimation(180, 0,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(200);
                rotate.setFillAfter(true);
                ivExpandIcon.startAnimation(rotate);
            }
        }

        private void showOptionsMenu(HotelServiceItem service, int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Opciones del servicio");

            String[] options = {"Editar", "Eliminar", service.isActive() ? "Desactivar" : "Activar"};

            builder.setItems(options, (dialog, which) -> {
                if (actionListener != null) {
                    switch (which) {
                        case 0: // Editar
                            actionListener.onEditService(service, position);
                            break;
                        case 1: // Eliminar
                            actionListener.onDeleteService(service, position);
                            break;
                        case 2: // Activar/Desactivar
                            actionListener.onToggleService(service, position, !service.isActive());
                            break;
                    }
                }
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        }
    }
}