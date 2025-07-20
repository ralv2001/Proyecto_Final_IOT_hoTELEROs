package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.BasicServicePhotosAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceModel;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServiceManagementAdapter extends RecyclerView.Adapter<ServiceManagementAdapter.ServiceViewHolder> {

    private static final String TAG = "ServiceManagementAdapter";

    public interface OnServiceActionListener {
        void onEditService(HotelServiceModel service, int position);
        void onDeleteService(HotelServiceModel service, int position);
        void onToggleService(HotelServiceModel service, int position, boolean isActive);
    }

    public interface OnServicePhotoClickListener {
        void onPhotoClick(String photoUrl, int position, List<String> allPhotos);
    }

    private List<HotelServiceModel> services;
    private OnServiceActionListener actionListener;
    private OnServicePhotoClickListener photoClickListener;
    private NumberFormat currencyFormat;
    public Context context;

    // ✅ CONSTRUCTOR PRINCIPAL QUE NECESITA ServiceManagementFragment
    public ServiceManagementAdapter(Context context, List<HotelServiceModel> services,
                                    OnServiceActionListener actionListener,
                                    OnServicePhotoClickListener photoClickListener) {
        this.context = context;
        this.services = services;
        this.actionListener = actionListener;
        this.photoClickListener = photoClickListener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
    }

    // ✅ CONSTRUCTOR ALTERNATIVO
    public ServiceManagementAdapter(Context context, List<HotelServiceModel> services,
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
        HotelServiceModel service = services.get(position);
        holder.bind(service, position);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public void updateServices(List<HotelServiceModel> newServices) {
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
        private TextView tvConditionalInfo; // Se mantiene para compatibilidad pero no se usa
        private ImageView optionsButton;

        // Views para fotos
        private LinearLayout photoBadgeContainer, photosExpandableSection;
        private TextView tvPhotoCount, tvPhotosCounter;
        private ImageView ivExpandIcon;
        private RecyclerView rvServicePhotos;

        // Variables internas
        private boolean isExpanded = false;
        private BasicServicePhotosAdapter photosAdapter;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            initializeViews();
        }

        private void initializeViews() {
            // Views principales
            serviceIconContainer = itemView.findViewById(R.id.serviceIconContainer);
            ivServiceIcon = itemView.findViewById(R.id.ivServiceIcon);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceDescription = itemView.findViewById(R.id.tvServiceDescription);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            tvConditionalInfo = itemView.findViewById(R.id.tvConditionalInfo);

            // ✅ BOTÓN DE OPCIONES - ID CORRECTO del layout
            optionsButton = itemView.findViewById(R.id.optionsButton);

            // Views para fotos
            photoBadgeContainer = itemView.findViewById(R.id.photoBadgeContainer);
            photosExpandableSection = itemView.findViewById(R.id.photosExpandableSection);
            tvPhotoCount = itemView.findViewById(R.id.tvPhotoCount);
            tvPhotosCounter = itemView.findViewById(R.id.tvPhotosCounter);
            ivExpandIcon = itemView.findViewById(R.id.ivExpandIcon);
            rvServicePhotos = itemView.findViewById(R.id.rvServicePhotos);

            // Configurar RecyclerView de fotos
            if (rvServicePhotos != null) {
                rvServicePhotos.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                rvServicePhotos.setHasFixedSize(true);
            }
        }

        public void bind(HotelServiceModel service, int position) {
            android.util.Log.d(TAG, "🔧 Binding servicio: " + service.getName() +
                    " (Tipo: " + service.getServiceType() +
                    ", Precio: " + service.getPrice() +
                    ", Fotos: " + (service.getPhotoUrls() != null ? service.getPhotoUrls().size() : 0) + ")");

            // ✅ INFORMACIÓN BÁSICA DEL SERVICIO
            tvServiceName.setText(service.getName());
            tvServiceDescription.setText(service.getDescription());

            // ✅ TIPO DE SERVICIO CON FORMATO
            setupServiceType(service);

            // ✅ PRECIO DEL SERVICIO
            setupServicePrice(service);

            // ✅ ICONO DEL SERVICIO
            setupServiceIcon(service);

            // ✅ GESTIÓN DE FOTOS
            setupPhotosSection(service);

            // ✅ BOTÓN DE OPCIONES
            setupOptionsButton(service, position);

            // ✅ ESTADO ACTIVO/INACTIVO
            setupServiceState(service);
        }

        private void setupServiceType(HotelServiceModel service) {
            String serviceType = service.getServiceType();
            String displayType;
            int backgroundColor;

            switch (serviceType.toLowerCase()) {
                case "basic":
                case "básico":
                    displayType = "BÁSICO";
                    backgroundColor = 0xFF2196F3; // Azul
                    break;
                case "included":
                case "incluido":
                    displayType = "INCLUIDO";
                    backgroundColor = 0xFF4CAF50; // Verde
                    break;
                case "paid":
                case "pagado":
                    displayType = "PAGADO";
                    backgroundColor = 0xFFFF9800; // Naranja
                    break;
                default:
                    displayType = serviceType.toUpperCase();
                    backgroundColor = 0xFF757575; // Gris
                    break;
            }

            if (tvServiceType != null) {
                tvServiceType.setText(displayType);

                // ✅ APLICAR COLOR DE FONDO DIRECTO
                try {
                    tvServiceType.setBackgroundColor(backgroundColor);
                    tvServiceType.setTextColor(0xFFFFFFFF); // Texto blanco
                } catch (Exception e) {
                    android.util.Log.w(TAG, "Error aplicando color al tipo de servicio: " + e.getMessage());
                }
            }
        }

        private void setupServicePrice(HotelServiceModel service) {
            double price = service.getPrice();

            if (price > 0) {
                tvServicePrice.setText(currencyFormat.format(price));
                tvServicePrice.setVisibility(View.VISIBLE);
            } else {
                tvServicePrice.setVisibility(View.GONE);
            }

            // Ocultar información condicional ya que no se usa más
            if (tvConditionalInfo != null) {
                tvConditionalInfo.setVisibility(View.GONE);
            }
        }

        private void setupServiceIcon(HotelServiceModel service) {
            if (service.getIconKey() != null && !service.getIconKey().isEmpty()) {
                int iconResId = IconHelper.getIconResource(service.getIconKey());
                if (iconResId != 0) {
                    ivServiceIcon.setImageResource(iconResId);
                } else {
                    ivServiceIcon.setImageResource(R.drawable.ic_service_default);
                    android.util.Log.w(TAG, "⚠️ Icono no encontrado para: " + service.getIconKey());
                }
            } else {
                ivServiceIcon.setImageResource(R.drawable.ic_service_default);
            }
        }

        private void setupPhotosSection(HotelServiceModel service) {
            List<String> photoUrls = service.getPhotoUrls();
            boolean hasPhotos = photoUrls != null && !photoUrls.isEmpty();

            android.util.Log.d(TAG, "📷 Configurando fotos para " + service.getName() + ": " +
                    (hasPhotos ? photoUrls.size() + " fotos" : "sin fotos"));

            if (hasPhotos) {
                // ✅ MOSTRAR BADGE DE FOTOS
                photoBadgeContainer.setVisibility(View.VISIBLE);
                tvPhotoCount.setText(String.valueOf(photoUrls.size()));

                // ✅ CONFIGURAR CLICK PARA EXPANDIR/CONTRAER
                photoBadgeContainer.setOnClickListener(v -> togglePhotosExpansion(service));

                // ✅ CONFIGURAR ADAPTER DE FOTOS
                if (photosAdapter == null) {
                    photosAdapter = new BasicServicePhotosAdapter(context, photoUrls,
                            (photoUrl, photoPosition, allPhotos) -> {
                                if (photoClickListener != null) {
                                    photoClickListener.onPhotoClick(photoUrl, photoPosition, allPhotos);
                                }
                            });
                    rvServicePhotos.setAdapter(photosAdapter);
                } else {
                    photosAdapter.updatePhotos(photoUrls);
                }

                // ✅ ACTUALIZAR CONTADOR EN SECCIÓN EXPANDIBLE
                if (tvPhotosCounter != null) {
                    tvPhotosCounter.setText(photoUrls.size() + " fotos");
                }

            } else {
                // ✅ OCULTAR BADGE SI NO HAY FOTOS
                photoBadgeContainer.setVisibility(View.GONE);
                photosExpandableSection.setVisibility(View.GONE);
                isExpanded = false;
            }
        }

        private void setupOptionsButton(HotelServiceModel service, int position) {
            if (optionsButton != null && actionListener != null) {
                optionsButton.setOnClickListener(v -> showOptionsMenu(service, position));
            }
        }

        private void setupServiceState(HotelServiceModel service) {
            float alpha = service.isActive() ? 1.0f : 0.6f;
            itemView.setAlpha(alpha);
        }

        private void togglePhotosExpansion(HotelServiceModel service) {
            List<String> photoUrls = service.getPhotoUrls();
            if (photoUrls == null || photoUrls.isEmpty()) return;

            android.util.Log.d(TAG, "🔄 Toggle expansión de fotos para: " + service.getName());

            isExpanded = !isExpanded;

            // ✅ ANIMACIÓN DEL ICONO
            RotateAnimation rotation = new RotateAnimation(
                    isExpanded ? 0f : 180f,
                    isExpanded ? 180f : 0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotation.setDuration(300);
            rotation.setFillAfter(true);
            ivExpandIcon.startAnimation(rotation);

            // ✅ MOSTRAR/OCULTAR SECCIÓN DE FOTOS CON ANIMACIONES SIMPLES
            if (isExpanded) {
                photosExpandableSection.setVisibility(View.VISIBLE);
                photosExpandableSection.setAlpha(0f);
                photosExpandableSection.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start();
            } else {
                photosExpandableSection.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> photosExpandableSection.setVisibility(View.GONE))
                        .start();
            }
        }

        private void showOptionsMenu(HotelServiceModel service, int position) {
            String[] options = {
                    "✏️ Editar servicio",
                    service.isActive() ? "⏸️ Desactivar servicio" : "▶️ Activar servicio",
                    "🗑️ Eliminar servicio"
            };

            new AlertDialog.Builder(context)
                    .setTitle("Opciones para " + service.getName())
                    .setItems(options, (dialog, which) -> {
                        switch (which) {
                            case 0: // Editar
                                if (actionListener != null) {
                                    actionListener.onEditService(service, position);
                                }
                                break;
                            case 1: // Activar/Desactivar
                                if (actionListener != null) {
                                    actionListener.onToggleService(service, position, !service.isActive());
                                }
                                break;
                            case 2: // Eliminar
                                showDeleteConfirmation(service, position);
                                break;
                        }
                    })
                    .show();
        }

        private void showDeleteConfirmation(HotelServiceModel service, int position) {
            new AlertDialog.Builder(context)
                    .setTitle("⚠️ Confirmar eliminación")
                    .setMessage("¿Estás seguro de que quieres eliminar el servicio '" + service.getName() + "'?\n\nEsta acción no se puede deshacer.")
                    .setPositiveButton("🗑️ Eliminar", (dialog, which) -> {
                        if (actionListener != null) {
                            actionListener.onDeleteService(service, position);
                        }
                    })
                    .setNegativeButton("❌ Cancelar", null)
                    .show();
        }
    }
}