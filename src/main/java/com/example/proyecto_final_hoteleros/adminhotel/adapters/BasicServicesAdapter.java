package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.content.Context;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.BasicService;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;

import java.util.List;

public class BasicServicesAdapter extends RecyclerView.Adapter<BasicServicesAdapter.ServiceViewHolder> {

    private static final String TAG = "BasicServicesAdapter";

    // ❌ ELIMINADO: OnServiceRemovedListener - Ya no se necesita eliminar servicios desde perfil

    public interface OnServicePhotoClickListener {
        void onPhotoClick(String photoUrl, int position, List<String> allPhotos);
    }

    private List<BasicService> services;
    private OnServicePhotoClickListener photoClickListener;
    private Context context;

    // ✅ CONSTRUCTOR PRINCIPAL - Solo para visualización y ver fotos
    public BasicServicesAdapter(Context context, List<BasicService> services,
                                OnServicePhotoClickListener photoClickListener) {
        this.context = context;
        this.services = services;
        this.photoClickListener = photoClickListener;
        Log.d(TAG, "🔧 Adapter creado con " + services.size() + " servicios (solo visualización)");
    }

    // ✅ CONSTRUCTOR SIMPLE - Solo para mostrar servicios básicos
    public BasicServicesAdapter(List<BasicService> services) {
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
        BasicService service = services.get(position);
        holder.bind(service, position);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public void updateServices(List<BasicService> newServices) {
        this.services = newServices;
        notifyDataSetChanged();
        Log.d(TAG, "📋 Servicios actualizados: " + newServices.size());
    }

    public class ServiceViewHolder extends RecyclerView.ViewHolder {

        // Views principales
        private LinearLayout serviceIconContainer;
        private ImageView ivServiceIcon;
        private TextView tvServiceName, tvServiceDescription;

        // Views para fotos - EXACTOS como original
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

            // Elementos para fotos - EXACTOS como original
            photoBadgeContainer = itemView.findViewById(R.id.photoBadgeContainer);
            tvPhotoCount = itemView.findViewById(R.id.tvPhotoCount);
            ivExpandIcon = itemView.findViewById(R.id.ivExpandIcon);
            photosExpandableSection = itemView.findViewById(R.id.photosExpandableSection);
            rvServicePhotos = itemView.findViewById(R.id.rvServicePhotos);
            tvPhotosCounter = itemView.findViewById(R.id.tvPhotosCounter);

            Log.d(TAG, "🔧 ViewHolder inicializado (modo solo visualización)");
        }

        private void setupRecyclerView() {
            if (rvServicePhotos != null) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(
                        context, LinearLayoutManager.HORIZONTAL, false);
                rvServicePhotos.setLayoutManager(layoutManager);
                rvServicePhotos.setNestedScrollingEnabled(false);
                rvServicePhotos.setHasFixedSize(true);
                Log.d(TAG, "📷 RecyclerView de fotos configurado");
            }
        }

        public void bind(BasicService service, int position) {
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

            // Configurar fotos del servicio - EXACTO como original
            setupServicePhotos(service);

            Log.d(TAG, "🔧 Servicio vinculado: " + service.getName() +
                    " con " + (service.getPhotos() != null ? service.getPhotos().size() : 0) + " fotos");
        }

        private void setupServicePhotos(BasicService service) {
            List<String> photos = service.getPhotos();

            if (photos == null || photos.isEmpty()) {
                // No hay fotos - ocultar badge y sección expandible
                photoBadgeContainer.setVisibility(View.GONE);
                photosExpandableSection.setVisibility(View.GONE);
                isPhotosExpanded = false;
                Log.d(TAG, "📷 Servicio sin fotos: " + service.getName());
            } else {
                // Hay fotos - mostrar badge y configurar funcionalidad
                photoBadgeContainer.setVisibility(View.VISIBLE);

                // Configurar texto del contador
                String photoText = photos.size() == 1 ?
                        "1 foto" : photos.size() + " fotos";
                tvPhotoCount.setText(photoText);

                if (tvPhotosCounter != null) {
                    tvPhotosCounter.setText(photos.size() + " de " + photos.size());
                }

                // Configurar adapter de fotos
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

                // Click listener para expandir/colapsar fotos - EXACTO como original
                photoBadgeContainer.setOnClickListener(v -> togglePhotosSection());

                Log.d(TAG, "📷 Servicio con fotos configurado: " + service.getName() +
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
            Log.d(TAG, "📷 Toggle fotos: " + (isPhotosExpanded ?
                    "expandido" : "colapsado"));
        }

        private void expandPhotosSection() {
            // Animar ícono de expansión - EXACTO como original
            RotateAnimation rotateAnimation = new RotateAnimation(0, 180,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(200);
            rotateAnimation.setFillAfter(true);
            ivExpandIcon.startAnimation(rotateAnimation);

            // Mostrar sección de fotos con animación
            photosExpandableSection.setVisibility(View.VISIBLE);
            Animation slideDown = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            if (slideDown != null) {
                photosExpandableSection.startAnimation(slideDown);
            }
        }

        private void collapsePhotosSection() {
            // Animar ícono de expansión - EXACTO como original
            RotateAnimation rotateAnimation = new RotateAnimation(180, 0,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(200);
            rotateAnimation.setFillAfter(true);
            ivExpandIcon.startAnimation(rotateAnimation);

            // Ocultar sección de fotos con animación
            Animation slideUp = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right);
            if (slideUp != null) {
                slideUp.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        photosExpandableSection.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                photosExpandableSection.startAnimation(slideUp);
            } else {
                photosExpandableSection.setVisibility(View.GONE);
            }
        }
    }
}