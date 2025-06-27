package com.example.proyecto_final_hoteleros.adminhotel.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelServiceItem;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ServiceManagementAdapter extends RecyclerView.Adapter<ServiceManagementAdapter.ServiceViewHolder> {

    public interface OnServiceActionListener {
        void onEditService(HotelServiceItem service, int position);
        void onDeleteService(HotelServiceItem service, int position);
        void onToggleService(HotelServiceItem service, int position, boolean isActive);
    }

    private List<HotelServiceItem> services;
    private OnServiceActionListener listener;
    private NumberFormat currencyFormat;

    public ServiceManagementAdapter(List<HotelServiceItem> services, OnServiceActionListener listener) {
        this.services = services;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_service_management, parent, false);
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
        private RecyclerView rvServicePhotos;
        private View dividerLine;

        // Adapter para fotos
        private ServicePhotosAdapter photosAdapter;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);

            // Referencias a las vistas (ACTUALIZADAS según el nuevo layout)
            serviceIconContainer = itemView.findViewById(R.id.serviceIconContainer);
            ivServiceIcon = itemView.findViewById(R.id.ivServiceIcon);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceDescription = itemView.findViewById(R.id.tvServiceDescription);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            tvConditionalInfo = itemView.findViewById(R.id.tvConditionalInfo);
            optionsButton = itemView.findViewById(R.id.optionsButton);
            rvServicePhotos = itemView.findViewById(R.id.rvServicePhotos);
            dividerLine = itemView.findViewById(R.id.dividerLine);
        }

        public void bind(HotelServiceItem service, int position) {
            Context context = itemView.getContext();

            // Configurar nombre del servicio
            tvServiceName.setText(service.getName());

            // Configurar descripción
            if (service.getDescription() != null && !service.getDescription().isEmpty()) {
                tvServiceDescription.setText(service.getDescription());
                tvServiceDescription.setVisibility(View.VISIBLE);
            } else {
                tvServiceDescription.setVisibility(View.GONE);
            }

            // Configurar icono del servicio
            int iconResource = IconHelper.getIconResource(service.getIconKey());
            ivServiceIcon.setImageResource(iconResource);

            // Configurar color del contenedor según tipo de servicio
            setupServiceTypeAppearance(service, context);

            // Configurar badge de tipo y precio
            setupServiceTypeAndPrice(service);

            // Configurar información condicional
            setupConditionalInfo(service);

            // Configurar galería de fotos
            setupPhotosGallery(service);

            // Configurar botón de opciones
            setupOptionsButton(service, position, context);

            // Configurar click en toda la tarjeta
            itemView.setOnClickListener(v -> showServiceOptionsDialog(service, position, context));
        }

        private void setupServiceTypeAppearance(HotelServiceItem service, Context context) {
            int backgroundColor;
            switch (service.getType()) {
                case BASIC:
                    backgroundColor = ContextCompat.getColor(context, R.color.orange);
                    break;
                case INCLUDED:
                    backgroundColor = ContextCompat.getColor(context, R.color.green);
                    break;
                case PAID:
                    backgroundColor = ContextCompat.getColor(context, R.color.blue);
                    break;
                case CONDITIONAL:
                    backgroundColor = ContextCompat.getColor(context, R.color.purple);
                    break;
                default:
                    backgroundColor = ContextCompat.getColor(context, R.color.orange);
                    break;
            }

            // Aplicar color de fondo al contenedor del icono
            serviceIconContainer.setBackgroundTintList(ContextCompat.getColorStateList(context, getColorForServiceType(service.getType())));
        }

        private void setupServiceTypeAndPrice(HotelServiceItem service) {
            // Configurar badge de tipo
            String typeText;
            int typeColor;
            int typeBackground;

            switch (service.getType()) {
                case BASIC:
                    typeText = "Básico";
                    typeColor = R.color.orange;
                    typeBackground = R.drawable.bg_chip_orange;
                    break;
                case INCLUDED:
                    typeText = "Incluido";
                    typeColor = R.color.green;
                    typeBackground = R.drawable.bg_chip_green;
                    break;
                case PAID:
                    typeText = "De Pago";
                    typeColor = R.color.blue;
                    typeBackground = R.drawable.bg_chip_blue;
                    break;
                case CONDITIONAL:
                    typeText = "Condicional";
                    typeColor = R.color.purple;
                    typeBackground = R.drawable.bg_chip_purple;
                    break;
                default:
                    typeText = "Incluido";
                    typeColor = R.color.green;
                    typeBackground = R.drawable.bg_chip_green;
                    break;
            }

            tvServiceType.setText(typeText);
            tvServiceType.setTextColor(ContextCompat.getColor(itemView.getContext(), typeColor));
            tvServiceType.setBackgroundResource(typeBackground);

            // Configurar precio (solo para servicios de pago)
            if (service.getType() == HotelServiceItem.ServiceType.PAID && service.getPrice() > 0) {
                tvServicePrice.setText(currencyFormat.format(service.getPrice()));
                tvServicePrice.setVisibility(View.VISIBLE);
            } else {
                tvServicePrice.setVisibility(View.GONE);
            }
        }

        private void setupConditionalInfo(HotelServiceItem service) {
            if (service.getType() == HotelServiceItem.ServiceType.CONDITIONAL && service.getConditionalAmount() > 0) {
                String conditionalText = "Más de " + service.getConditionalAmount() + " huéspedes";
                tvConditionalInfo.setText(conditionalText);
                tvConditionalInfo.setVisibility(View.VISIBLE);
            } else {
                tvConditionalInfo.setVisibility(View.GONE);
            }
        }

        private void setupPhotosGallery(HotelServiceItem service) {
            if (service.getPhotos() != null && !service.getPhotos().isEmpty()) {
                // Configurar adapter para fotos
                if (photosAdapter == null) {
                    photosAdapter = new ServicePhotosAdapter(service.getPhotos(), photo -> {
                        // Mostrar foto en pantalla completa
                        // Aquí puedes implementar la lógica para mostrar la foto completa
                    });
                    rvServicePhotos.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                    rvServicePhotos.setAdapter(photosAdapter);
                } else {
                    photosAdapter.updatePhotos(service.getPhotos());
                }

                rvServicePhotos.setVisibility(View.VISIBLE);
                dividerLine.setVisibility(View.VISIBLE);
            } else {
                rvServicePhotos.setVisibility(View.GONE);
                dividerLine.setVisibility(View.GONE);
            }
        }

        private void setupOptionsButton(HotelServiceItem service, int position, Context context) {
            optionsButton.setOnClickListener(v -> showServiceOptionsDialog(service, position, context));
        }

        private void showServiceOptionsDialog(HotelServiceItem service, int position, Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            // Crear layout personalizado para el diálogo
            View dialogView = LayoutInflater.from(context).inflate(R.layout.admin_hotel_dialog_service_options, null);

            // Configurar vistas del diálogo
            ImageView dialogServiceIcon = dialogView.findViewById(R.id.ivServiceIcon);
            TextView dialogServiceName = dialogView.findViewById(R.id.tvServiceName);
            TextView dialogServiceType = dialogView.findViewById(R.id.tvServiceType);
            LinearLayout optionEdit = dialogView.findViewById(R.id.optionEdit);
            LinearLayout optionViewPhotos = dialogView.findViewById(R.id.optionViewPhotos);
            LinearLayout optionDelete = dialogView.findViewById(R.id.optionDelete);
            TextView tvPhotoCount = dialogView.findViewById(R.id.tvPhotoCount);

            // Configurar datos del servicio en el diálogo
            int iconResource = IconHelper.getIconResource(service.getIconKey());
            dialogServiceIcon.setImageResource(iconResource);
            dialogServiceName.setText(service.getName());
            dialogServiceType.setText(getServiceTypeText(service.getType()));

            // Mostrar/ocultar contador de fotos
            if (service.getPhotos() != null && !service.getPhotos().isEmpty()) {
                tvPhotoCount.setText(String.valueOf(service.getPhotos().size()));
                tvPhotoCount.setVisibility(View.VISIBLE);
            } else {
                tvPhotoCount.setVisibility(View.GONE);
                optionViewPhotos.setAlpha(0.5f); // Opción deshabilitada
            }

            AlertDialog dialog = builder.setView(dialogView).create();

            // Configurar clicks de las opciones
            optionEdit.setOnClickListener(v -> {
                dialog.dismiss();
                if (listener != null) {
                    listener.onEditService(service, position);
                }
            });

            optionViewPhotos.setOnClickListener(v -> {
                dialog.dismiss();
                if (service.getPhotos() != null && !service.getPhotos().isEmpty()) {
                    // Aquí puedes implementar la lógica para ver las fotos
                    // Por ejemplo, abrir un fragmento de galería
                }
            });

            optionDelete.setOnClickListener(v -> {
                dialog.dismiss();
                showDeleteConfirmationDialog(service, position, context);
            });

            dialog.show();
        }

        private void showDeleteConfirmationDialog(HotelServiceItem service, int position, Context context) {
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar Servicio")
                    .setMessage("¿Estás seguro de que quieres eliminar el servicio \"" + service.getName() + "\"?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        if (listener != null) {
                            listener.onDeleteService(service, position);
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        }

        private int getColorForServiceType(HotelServiceItem.ServiceType type) {
            switch (type) {
                case BASIC: return R.color.orange;
                case INCLUDED: return R.color.green;
                case PAID: return R.color.blue;
                case CONDITIONAL: return R.color.purple;
                default: return R.color.orange;
            }
        }

        private String getServiceTypeText(HotelServiceItem.ServiceType type) {
            switch (type) {
                case BASIC: return "Servicio Básico";
                case INCLUDED: return "Servicio Incluido";
                case PAID: return "Servicio de Pago";
                case CONDITIONAL: return "Servicio Condicional";
                default: return "Servicio";
            }
        }
    }

    // Clase para adapter de fotos del servicio
    private static class ServicePhotosAdapter extends RecyclerView.Adapter<ServicePhotosAdapter.PhotoViewHolder> {

        public interface OnPhotoClickListener {
            void onPhotoClick(android.net.Uri photoUri);
        }

        private List<android.net.Uri> photos;
        private OnPhotoClickListener listener;

        public ServicePhotosAdapter(List<android.net.Uri> photos, OnPhotoClickListener listener) {
            this.photos = photos;
            this.listener = listener;
        }

        @NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_hotel_item_basic_service_photo, parent, false);
            return new PhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
            android.net.Uri photo = photos.get(position);
            holder.bind(photo);
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        public void updatePhotos(List<android.net.Uri> newPhotos) {
            this.photos = newPhotos;
            notifyDataSetChanged();
        }

        class PhotoViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivPhoto;

            public PhotoViewHolder(@NonNull View itemView) {
                super(itemView);
                ivPhoto = itemView.findViewById(R.id.ivPhoto);
            }

            public void bind(android.net.Uri photoUri) {
                // Aquí puedes usar Glide, Picasso o cualquier librería para cargar la imagen
                // Por ejemplo con Glide:
                // Glide.with(itemView.getContext()).load(photoUri).into(ivPhoto);

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onPhotoClick(photoUri);
                    }
                });
            }
        }
    }
}