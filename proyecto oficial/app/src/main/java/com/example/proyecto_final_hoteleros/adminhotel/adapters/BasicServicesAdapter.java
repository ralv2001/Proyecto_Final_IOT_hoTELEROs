package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.content.Context;
import android.util.Log;
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
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceModel;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.BasicServicePhotosAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;

import java.util.List;

public class BasicServicesAdapter extends RecyclerView.Adapter<BasicServicesAdapter.ServiceViewHolder> {

    private static final String TAG = "BasicServicesAdapter";

    public interface OnServicePhotoClickListener {
        void onPhotoClick(String photoUrl, int position, List<String> allPhotos);
    }

    private List<HotelServiceModel> services;
    private OnServicePhotoClickListener photoClickListener;
    private Context context;

    // ✅ CONSTRUCTOR PRINCIPAL - Solo para visualización y ver fotos
    public BasicServicesAdapter(Context context, List<HotelServiceModel> services,
                                OnServicePhotoClickListener photoClickListener) {
        this.context = context;
        this.services = services;
        this.photoClickListener = photoClickListener;
        Log.d(TAG, "🔧 Adapter creado con " + services.size() + " servicios (solo visualización)");
    }

    // ✅ CONSTRUCTOR SIMPLE - Solo para mostrar servicios básicos
    public BasicServicesAdapter(List<HotelServiceModel> services) {
        this.services = services;
        this.photoClickListener = null;
        Log.d(TAG, "🔧 Adapter creado (modo simple) con " + services.size() + " servicios");
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_hotel_item_basic_service, parent, false);
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
        Log.d(TAG, "📋 Servicios actualizados: " + newServices.size());
    }

    public class ServiceViewHolder extends RecyclerView.ViewHolder {

        // Views principales
        private LinearLayout serviceIconContainer;
        private ImageView ivServiceIcon;
        private TextView tvServiceName;
        private TextView tvServiceDescription;

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
            Log.d(TAG, "🔧 Binding servicio: " + service.getName() + " con " +
                    (service.getPhotoUrls() != null ? service.getPhotoUrls().size() : 0) + " fotos");

            // ✅ INFORMACIÓN BÁSICA DEL SERVICIO
            tvServiceName.setText(service.getName());
            tvServiceDescription.setText(service.getDescription());

            // ✅ ICONO DEL SERVICIO
            if (service.getIconKey() != null && !service.getIconKey().isEmpty()) {
                int iconResId = IconHelper.getIconResource(service.getIconKey());
                if (iconResId != 0) {
                    ivServiceIcon.setImageResource(iconResId);
                } else {
                    ivServiceIcon.setImageResource(R.drawable.ic_service_default);
                    Log.w(TAG, "⚠️ Icono no encontrado para: " + service.getIconKey());
                }
            } else {
                ivServiceIcon.setImageResource(R.drawable.ic_service_default);
            }

            // ✅ GESTIÓN DE FOTOS
            setupPhotosSection(service);
        }

        private void setupPhotosSection(HotelServiceModel service) {
            List<String> photoUrls = service.getPhotoUrls();
            boolean hasPhotos = photoUrls != null && !photoUrls.isEmpty();

            Log.d(TAG, "📷 Configurando fotos para " + service.getName() + ": " +
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
                Log.d(TAG, "🚫 Sin fotos para " + service.getName());
            }
        }

        private void togglePhotosExpansion(HotelServiceModel service) {
            List<String> photoUrls = service.getPhotoUrls();
            if (photoUrls == null || photoUrls.isEmpty()) return;

            Log.d(TAG, "🔄 Toggle expansión de fotos para: " + service.getName());

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
                Log.d(TAG, "📷 Fotos expandidas para: " + service.getName());
            } else {
                photosExpandableSection.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> photosExpandableSection.setVisibility(View.GONE))
                        .start();
                Log.d(TAG, "📷 Fotos contraídas para: " + service.getName());
            }
        }
    }
}