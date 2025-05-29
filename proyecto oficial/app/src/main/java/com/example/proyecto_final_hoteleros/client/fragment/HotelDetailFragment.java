package com.example.proyecto_final_hoteleros.client.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.proyecto_final_hoteleros.AllHotelServicesActivity;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adapters.CitiesAdapter;
import com.example.proyecto_final_hoteleros.adapters.HotelImageAdapter;
import com.example.proyecto_final_hoteleros.adapters.ThumbnailAdapter;
import com.example.proyecto_final_hoteleros.client.model.City;
import com.example.proyecto_final_hoteleros.client.model.HotelService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HotelDetailFragment extends Fragment implements ThumbnailAdapter.OnThumbnailClickListener {

    private ViewPager2 viewPagerImages;
    private RecyclerView rvImageThumbnails;
    private RecyclerView rvNearbyPlaces;
    private TextView tvHotelName, tvHotelLocation, tvHotelPrice, tvPriceDescription;
    private TextView tvImageCounter;
    private ImageButton btnPreviousImage, btnNextImage;
    private TextView btnSeeAllPhotos;

    // Nuevas variables para los servicios de alojamiento
    private RecyclerView rvServices;
    private TextView tvSeeAllServices;
    private List<HotelService> featuredServices = new ArrayList<>();

    private HotelImageAdapter imageAdapter;
    private ThumbnailAdapter thumbnailAdapter;
    private List<Integer> hotelImages;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hotel_selection, container, false);

        // Inicializar vistas
        viewPagerImages = view.findViewById(R.id.view_pager_hotel_images);
        rvImageThumbnails = view.findViewById(R.id.rv_image_thumbnails);
        rvNearbyPlaces = view.findViewById(R.id.rv_nearby_places);
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        tvHotelLocation = view.findViewById(R.id.tv_hotel_location);
        tvHotelPrice = view.findViewById(R.id.tv_hotel_price);
        tvPriceDescription = view.findViewById(R.id.tv_price_description);
        tvImageCounter = view.findViewById(R.id.tv_image_counter);
        btnPreviousImage = view.findViewById(R.id.btn_previous_image);
        btnNextImage = view.findViewById(R.id.btn_next_image);
        btnSeeAllPhotos = view.findViewById(R.id.btn_see_all_photos);

        // Inicializar vistas para los servicios de alojamiento
        rvServices = view.findViewById(R.id.rv_services_preview);
        tvSeeAllServices = view.findViewById(R.id.tv_see_all_services);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener argumentos si existen
        if (getArguments() != null) {
            String hotelName = getArguments().getString("hotel_name", "Belmond Miraflores Park");
            String hotelLocation = getArguments().getString("hotel_location", "Miraflores, frente al malecón, Lima");
            String hotelPrice = getArguments().getString("hotel_price", "S/2,050");
            String hotelRating = getArguments().getString("hotel_rating", "4.7");

            // Actualizar la UI con los datos
            tvHotelName.setText(hotelName);
            tvHotelLocation.setText(hotelLocation);
            tvHotelPrice.setText(hotelPrice);
        }

        // Configurar galería de imágenes
        setupImageGallery();

        // Configurar lugares turísticos cercanos
        setupNearbyPlaces();

        // Configurar servicios de alojamiento
        setupHotelServices();

        // Configurar botones y eventos
        setupActions(view);
    }

    private void setupImageGallery() {
        // Lista de imágenes para la galería
        hotelImages = Arrays.asList(
                R.drawable.belmond,
                R.drawable.belmond,
                R.drawable.belmond,
                R.drawable.belmond
        );

        // Configurar ViewPager principal
        imageAdapter = new HotelImageAdapter(hotelImages);
        viewPagerImages.setAdapter(imageAdapter);

        // Mostrar contador inicial
        updateImageCounter(0);

        // Configurar listener de cambio de página
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateImageCounter(position);
                thumbnailAdapter.updateSelectedPosition(position);
                // Hacer scroll a la miniatura seleccionada
                rvImageThumbnails.smoothScrollToPosition(position);
            }
        });

        // Configurar miniaturas
        thumbnailAdapter = new ThumbnailAdapter(hotelImages, this);
        rvImageThumbnails.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvImageThumbnails.setAdapter(thumbnailAdapter);

        // Configurar botones de navegación
        btnPreviousImage.setOnClickListener(v -> {
            int currentPosition = viewPagerImages.getCurrentItem();
            if (currentPosition > 0) {
                viewPagerImages.setCurrentItem(currentPosition - 1, true);
            }
        });

        btnNextImage.setOnClickListener(v -> {
            int currentPosition = viewPagerImages.getCurrentItem();
            if (currentPosition < hotelImages.size() - 1) {
                viewPagerImages.setCurrentItem(currentPosition + 1, true);
            }
        });

        // Configurar botón Ver todas las fotos
        btnSeeAllPhotos.setOnClickListener(v -> {
            // Implementar navegación a la galería completa
            navigateToFullGallery();
        });

        // Configurar listener de clic en imagen principal
        imageAdapter.setOnImageClickListener(position -> {
            // También puede navegar a la galería completa
            navigateToFullGallery();
        });
    }

    private void updateImageCounter(int position) {
        // Actualizar el contador de imágenes (1/4, 2/4, etc.)
        String counterText = (position + 1) + "/" + hotelImages.size();
        tvImageCounter.setText(counterText);
    }

    private void navigateToFullGallery() {
        // Esta función navegaría a una galería completa
        // Puedes implementarla cuando decidas añadir esa funcionalidad
        // Por ahora, podemos dejarla vacía o mostrar un mensaje
        if (getContext() != null) {
            // Toast.makeText(getContext(), "Ver todas las fotos", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onThumbnailClick(int position) {
        // Cuando se hace clic en una miniatura, actualizamos el ViewPager
        viewPagerImages.setCurrentItem(position, true);
    }

    private void setupNearbyPlaces() {
        // Crear datos de ejemplo para lugares cercanos
        List<City> nearbyPlaces = new ArrayList<>();
        nearbyPlaces.add(new City("Parque del Amor", R.drawable.lima));
        nearbyPlaces.add(new City("Larcomar", R.drawable.lima));
        nearbyPlaces.add(new City("Kennedy Park", R.drawable.lima));

        // Configurar RecyclerView
        CitiesAdapter adapter = new CitiesAdapter(nearbyPlaces);
        rvNearbyPlaces.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvNearbyPlaces.setAdapter(adapter);
    }

    private void setupHotelServices() {
        try {
            // Cargar servicios destacados
            loadFeaturedServices();

            // Configurar RecyclerView en formato de grid
            if (rvServices != null) {
                rvServices.setLayoutManager(new GridLayoutManager(getContext(), 4));

                // Crear y configurar adaptador personalizado para servicios
                ServicePreviewAdapter adapter = new ServicePreviewAdapter(featuredServices);
                rvServices.setAdapter(adapter);
            }

            // Configurar botón "Ver todo"
            if (tvSeeAllServices != null) {
                tvSeeAllServices.setOnClickListener(v -> {
                    try {
                        // Navegar a la actividad de todos los servicios de alojamiento
                        Intent intent = new Intent(getContext(), AllHotelServicesActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e("HotelDetailFragment", "Error navegando a servicios: " + e.getMessage());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error abriendo servicios", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } catch (Exception e) {
            Log.e("HotelDetailFragment", "Error configurando servicios: " + e.getMessage());
        }
    }


    private void loadFeaturedServices() {
        // Estos serían los servicios destacados que aparecen en la pantalla principal
        featuredServices.add(new HotelService(
                "wifi",
                "WiFi",
                "Conectarse a nuestra red inalámbrica en todas las áreas del establecimiento.",
                null, // Precio null = gratis
                null, // Sin imagen específica
                "ic_wifi", // Usa icono predeterminado
                false,
                null
        ));

        featuredServices.add(new HotelService(
                "reception",
                "Recepción 24h",
                "Atención personalizada las 24 horas del día.",
                null, // Precio null = gratis
                null, // Sin imagen específica
                "ic_reception", // Usa icono predeterminado
                false,
                null
        ));

        featuredServices.add(new HotelService(
                "pool",
                "Piscina",
                "Piscina con vistas panorámicas en la azotea del hotel.",
                null, // Precio null = gratis
                null, // Sin imagen específica
                "ic_pool", // Usa icono predeterminado
                false,
                null
        ));

        featuredServices.add(new HotelService(
                "taxi",
                "Taxi*",
                "El servicio de taxi gratuito hacia el aeropuerto estará disponible si se adquiere una reserva de S/. 350",
                null, // Precio base null ya que depende de condición
                null, // Sin imagen específica
                "ic_taxi", // Usa icono predeterminado
                true, // Es condicional
                "Disponible gratis con reserva mínima de S/. 350"
        ));
    }

    private void setupActions(View view) {
        // Configurar botón de retroceso
        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        // Configurar botón de favoritos
        view.findViewById(R.id.btn_favorite).setOnClickListener(v -> {
            // Implementar lógica para añadir a favoritos
        });

        // Configurar botón de elegir habitación
        view.findViewById(R.id.btn_choose_room).setOnClickListener(v -> {
            // Implementar navegación a la pantalla de selección de habitaciones
            navigateToRoomSelection();
        });

        // Ver todas las reseñas
        view.findViewById(R.id.tv_see_all_reviews).setOnClickListener(v -> {
            // Implementar navegación a la pantalla de todas las reseñas
        });
    }

    private void navigateToRoomSelection() {
        // Crear el fragmento de selección de habitación
        RoomSelectionFragment roomSelectionFragment = new RoomSelectionFragment();
        // Pasar datos del hotel al fragmento
        Bundle args = new Bundle();
        args.putString("hotel_name", tvHotelName.getText().toString());
        args.putString("hotel_price", tvHotelPrice.getText().toString());
        roomSelectionFragment.setArguments(args);

        // Navegar al fragmento
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, roomSelectionFragment)
                .addToBackStack(null)
                .commit();
    }

    // Adaptador para la vista previa de servicios (adaptado de ServicePreviewAdapter)
    class ServicePreviewAdapter extends RecyclerView.Adapter<ServicePreviewAdapter.ServiceViewHolder> {
        private List<HotelService> services;

        public ServicePreviewAdapter(List<HotelService> services) {
            this.services = services != null ? services : new ArrayList<>();
        }

        @NonNull
        @Override
        public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_service_preview, parent, false);
            return new ServiceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
            if (position < services.size()) {
                HotelService service = services.get(position);
                holder.bind(service);
            }
        }

        @Override
        public int getItemCount() {
            return services.size();
        }

        class ServiceViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivServiceIcon;
            private TextView tvServiceName;
            private CardView iconContainer;

            public ServiceViewHolder(@NonNull View itemView) {
                super(itemView);
                ivServiceIcon = itemView.findViewById(R.id.iv_service_icon);
                tvServiceName = itemView.findViewById(R.id.tv_service_name);
                iconContainer = itemView.findViewById(R.id.fl_service_icon_container);
            }

            public void bind(HotelService service) {
                try {
                    if (service == null) return;

                    tvServiceName.setText(service.getName());

                    // Configurar icono
                    setupServiceIcon(service);

                    // Configurar click
                    itemView.setOnClickListener(v -> {
                        try {
                            String message = service.getName();
                            if (service.isConditional()) {
                                message += " - " + service.getConditionalDescription();
                            }

                            if (getContext() != null) {
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("ServiceViewHolder", "Error en click: " + e.getMessage());
                        }
                    });

                } catch (Exception e) {
                    Log.e("ServiceViewHolder", "Error en bind: " + e.getMessage());
                }
            }

            private void setupServiceIcon(HotelService service) {
                try {
                    if (ivServiceIcon == null || iconContainer == null) return;

                    // Configurar icono
                    String iconName = service.getIconResourceName();
                    if (iconName != null && !iconName.isEmpty()) {
                        int resourceId = itemView.getContext().getResources().getIdentifier(
                                iconName, "drawable", itemView.getContext().getPackageName());

                        if (resourceId > 0) {
                            ivServiceIcon.setImageResource(resourceId);
                        } else {
                            ivServiceIcon.setImageResource(R.drawable.ic_hotel_service_default);
                        }
                    } else {
                        ivServiceIcon.setImageResource(R.drawable.ic_hotel_service_default);
                    }

                    // Configurar fondo según tipo de servicio
                    int backgroundRes = R.color.orange_light;
                    switch (service.getServiceType()) {
                        case "free":
                            backgroundRes = R.color.success_light;
                            break;
                        case "conditional":
                            backgroundRes = R.color.purple_light;
                            break;
                        case "paid":
                        default:
                            backgroundRes = R.color.orange_light;
                            break;
                    }

                    iconContainer.setCardBackgroundColor(
                            ContextCompat.getColor(itemView.getContext(), backgroundRes));

                } catch (Exception e) {
                    Log.e("ServiceViewHolder", "Error configurando icono: " + e.getMessage());
                }
            }
        }
    }

}