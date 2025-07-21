package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
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

import com.example.proyecto_final_hoteleros.client.data.model.NearbyPlace;
import com.example.proyecto_final_hoteleros.client.data.repository.NearbyPlacesRepository;
import com.example.proyecto_final_hoteleros.client.data.repository.ServicesRepository;
import com.example.proyecto_final_hoteleros.client.domain.interfaces.ServiceClickListener;
import com.example.proyecto_final_hoteleros.client.ui.activity.AllHotelServicesActivity;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.activity.ViewAllServicesActivity;
import com.example.proyecto_final_hoteleros.client.ui.adapters.CitiesAdapter;
import com.example.proyecto_final_hoteleros.client.ui.adapters.EnhancedCitiesAdapter;
import com.example.proyecto_final_hoteleros.client.ui.adapters.FeaturedServicesAdapter;
import com.example.proyecto_final_hoteleros.client.ui.adapters.HotelImageAdapter;
import com.example.proyecto_final_hoteleros.client.ui.adapters.ThumbnailAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.City;
import com.example.proyecto_final_hoteleros.client.data.model.HotelService;
import com.example.proyecto_final_hoteleros.client.utils.HotelPriceUtils;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelProfile;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseHotelManager;
import com.example.proyecto_final_hoteleros.client.utils.HotelPhotoUtils;

// ✅ IMPORTACIONES PARA GOOGLE MAPS
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HotelDetailFragment extends Fragment implements ThumbnailAdapter.OnThumbnailClickListener, OnMapReadyCallback, ServiceClickListener {

    private static final String TAG = "HotelDetailFragment";

    private ViewPager2 viewPagerImages;
    private RecyclerView rvImageThumbnails;
    private RecyclerView rvNearbyPlaces;
    private TextView tvHotelName, tvHotelLocation, tvHotelPrice, tvPriceDescription;
    private TextView tvImageCounter;
    private ImageButton btnPreviousImage, btnNextImage;
    private TextView btnSeeAllPhotos;
    private RecyclerView rvServicesPreview;
    private TextView tvSeeAllServices;
    private List<HotelService> featuredServices = new ArrayList<>();

    // ✅ NUEVAS VARIABLES PARA ESTANCIA
    private TextView tvStayDates, tvStayDetail;

    // ✅ VARIABLES PARA EL MAPA
    private GoogleMap mMap;
    private MaterialButton btnOpenInMaps;
    private LatLng hotelLocation;
    private String hotelName;
    private String hotelAddress;

    private HotelImageAdapter imageAdapter;
    private ThumbnailAdapter thumbnailAdapter;
    private List<String> hotelImageUrls = new ArrayList<>(); // ✅ CAMBIAR A String URLs
    private NearbyPlacesRepository nearbyPlacesRepository;

    private List<NearbyPlace> nearbyPlacesList = new ArrayList<>();
    private EnhancedCitiesAdapter enhancedCitiesAdapter;

    // ✅ NUEVO: Variables para datos del hotel
    private HotelProfile currentHotel;
    private FirebaseHotelManager hotelManager;
    private com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseServiceManager serviceManager; // ✅ AGREGAR

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.client_fragment_hotel_detail, container, false);

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
        rvServicesPreview = view.findViewById(R.id.rv_services_preview);
        tvSeeAllServices = view.findViewById(R.id.tv_see_all_services);

        // ✅ INICIALIZAR NUEVAS VISTAS DE ESTANCIA
        tvStayDates = view.findViewById(R.id.tv_stay_dates);
        tvStayDetail = view.findViewById(R.id.tv_stay_detail);

        // ✅ INICIALIZAR VISTA DEL MAPA
        btnOpenInMaps = view.findViewById(R.id.btn_open_in_maps);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar managers
        hotelManager = FirebaseHotelManager.getInstance(getContext());
        serviceManager = com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseServiceManager.getInstance(getContext()); // ✅ AGREGAR

        // Obtener argumentos si existen
        if (getArguments() != null) {
            hotelName = getArguments().getString("hotel_name", "Belmond Miraflores Park");
            hotelAddress = getArguments().getString("hotel_location", "Miraflores, frente al malecón, Lima");
            String hotelPrice = getArguments().getString("hotel_price", "S/2,050");
            String hotelRating = getArguments().getString("hotel_rating", "4.7");

            // Actualizar la UI con los datos básicos primero
            tvHotelName.setText(hotelName);
            tvHotelLocation.setText(hotelAddress);
            tvHotelPrice.setText(hotelPrice);
        } else {
            hotelName = "Belmond Miraflores Park";
            hotelAddress = "Miraflores, frente al malecón, Lima";
        }

        // ✅ BUSCAR DATOS COMPLETOS DEL HOTEL EN FIREBASE PRIMERO
        // Y SOLO DESPUÉS CONFIGURAR LUGARES CERCANOS
        loadHotelDetailsFromFirebase();

        // ✅ CONFIGURAR ESTANCIA POR DEFECTO (1 noche)
        setupDefaultStayInfo();

        // ✅ CONFIGURAR SERVICIOS (se cargarán los reales cuando se encuentre el hotel)
        // Solo inicializar el adapter vacío por ahora
        if (savedInstanceState == null) {
            featuredServices.clear(); // Empezar con lista vacía
            setupServicesPreview(); // Configurar adapter vacío
        }

        // ✅ CONFIGURAR MAPA (se actualizará cuando lleguen las coordenadas reales)
        setupMap();

        // Configurar botones y eventos
        setupActions(view);
        setupClickListeners();
    }

    // ✅ NUEVO: Buscar datos completos del hotel en Firebase
    private void loadHotelDetailsFromFirebase() {
        Log.d(TAG, "🔍 Buscando datos del hotel: " + hotelName);

        hotelManager.findHotelsNearLocation(0, 0, 999999, new FirebaseHotelManager.HotelsCallback() {
            @Override
            public void onSuccess(List<HotelProfile> hotels) {
                // Buscar hotel por nombre
                for (HotelProfile hotel : hotels) {
                    if (hotel.getName().equalsIgnoreCase(hotelName)) {
                        currentHotel = hotel;
                        Log.d(TAG, "✅ Hotel encontrado en Firebase: " + hotel.getName());

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                updateUIWithHotelDetails(hotel);
                            });
                        }
                        return;
                    }
                }

                Log.w(TAG, "⚠️ Hotel no encontrado en Firebase: " + hotelName);
                setupDefaultHotelData();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error buscando hotel: " + error);
                setupDefaultHotelData();
            }
        });
    }

    // ✅ NUEVO: Actualizar UI con datos reales del hotel
    private void updateUIWithHotelDetails(HotelProfile hotel) {
        Log.d(TAG, "📊 Actualizando UI con datos reales del hotel");

        // ✅ ACTUALIZAR COORDENADAS PARA EL MAPA
        if (hotel.hasValidLocation()) {
            hotelLocation = new LatLng(hotel.getLatitude(), hotel.getLongitude());
            Log.d(TAG, "📍 Coordenadas del hotel: " + hotel.getLatitude() + ", " + hotel.getLongitude());

            // ✅ AHORA SÍ CONFIGURAR LUGARES CERCANOS CON COORDENADAS REALES
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    setupNearbyPlaces(); // ✅ LLAMAR AQUÍ CON COORDENADAS REALES
                });
            }

            if (mMap != null) {
                updateMapWithHotelLocation();
            }
        } else {
            Log.w(TAG, "⚠️ Hotel no tiene coordenadas válidas, usando coordenadas por defecto");
            // ✅ USAR COORDENADAS POR DEFECTO PERO AÚN ASÍ LLAMAR A LA API
            hotelLocation = new LatLng(-12.1191, -77.0306); // Miraflores por defecto

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    setupNearbyPlaces(); // ✅ LLAMAR TAMBIÉN CON COORDENADAS POR DEFECTO
                });
            }
        }

        // ✅ CARGAR FOTOS REALES DEL HOTEL
        loadRealHotelPhotos(hotel);

        // ✅ CARGAR SERVICIOS BÁSICOS REALES DEL HOTEL
        loadRealHotelBasicServices(hotel);

        // ✅ ACTUALIZAR PRECIO CON HABITACIÓN MÁS BARATA
        updatePriceWithRealRoomPrice(hotel);

        // ✅ ACTUALIZAR DIRECCIÓN COMPLETA
        if (hotel.getFullAddress() != null) {
            tvHotelLocation.setText(hotel.getFullAddress());
            hotelAddress = hotel.getFullAddress();
        }
    }

    // ✅ NUEVO: Cargar fotos reales del hotel
    private void loadRealHotelPhotos(HotelProfile hotel) {
        Log.d(TAG, "📷 Cargando fotos reales del hotel");

        List<String> hotelPhotos = HotelPhotoUtils.getAllPhotosFromProfile(hotel);

        if (hotelPhotos != null && !hotelPhotos.isEmpty()) {
            hotelImageUrls.clear();
            hotelImageUrls.addAll(hotelPhotos);

            Log.d(TAG, "✅ " + hotelImageUrls.size() + " fotos reales encontradas");
        } else {
            // Usar fotos por defecto si no hay fotos reales
            hotelImageUrls.clear();
            hotelImageUrls.addAll(Arrays.asList(
                    "https://example.com/hotel_default_1.jpg",
                    "https://example.com/hotel_default_2.jpg",
                    "https://example.com/hotel_default_3.jpg",
                    "https://example.com/hotel_default_4.jpg"
            ));
            Log.d(TAG, "⚠️ Usando fotos por defecto");
        }

        // Configurar galería de imágenes con las fotos reales
        setupImageGalleryWithRealPhotos();
    }

    // ✅ NUEVO: Configurar galería con fotos reales
    private void setupImageGalleryWithRealPhotos() {
        Log.d(TAG, "🖼️ Configurando galería con " + hotelImageUrls.size() + " fotos");

        // Configurar ViewPager principal con URLs reales
        imageAdapter = new HotelImageAdapter(hotelImageUrls);
        viewPagerImages.setAdapter(imageAdapter);

        // Mostrar contador inicial
        updateImageCounter(0);

        // Configurar listener de cambio de página
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateImageCounter(position);
                if (thumbnailAdapter != null) {
                    thumbnailAdapter.updateSelectedPosition(position);
                    // Hacer scroll a la miniatura seleccionada
                    rvImageThumbnails.smoothScrollToPosition(position);
                }
            }
        });

        // Configurar miniaturas con URLs reales
        thumbnailAdapter = new ThumbnailAdapter(hotelImageUrls, this);
        rvImageThumbnails.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvImageThumbnails.setAdapter(thumbnailAdapter);

        // Configurar botones de navegación
        setupImageNavigationButtons();

        // Configurar botón Ver todas las fotos
        btnSeeAllPhotos.setOnClickListener(v -> {
            navigateToFullGallery();
        });

        // Configurar listener de clic en imagen principal
        if (imageAdapter != null) {
            imageAdapter.setOnImageClickListener(position -> {
                navigateToFullGallery();
            });
        }
    }

    // ✅ NUEVO: Cargar servicios básicos reales del hotel desde Firebase
    private void loadRealHotelBasicServices(HotelProfile hotel) {
        Log.d(TAG, "📋 Cargando servicios básicos reales del hotel: " + hotel.getName());

        if (hotel.getHotelAdminId() == null) {
            Log.w(TAG, "⚠️ Hotel sin hotelAdminId, usando servicios por defecto");
            setupFallbackServices();
            return;
        }

        // ✅ OBTENER SERVICIOS BÁSICOS ESPECÍFICOS DEL HOTEL
        getHotelBasicServices(hotel.getHotelAdminId(), new HotelServicesCallback() {
            @Override
            public void onSuccess(List<HotelService> services) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        featuredServices.clear();

                        // ✅ TOMAR SOLO LOS PRIMEROS 4 SERVICIOS BÁSICOS
                        int servicesToShow = Math.min(4, services.size());
                        for (int i = 0; i < servicesToShow; i++) {
                            featuredServices.add(services.get(i));
                        }

                        Log.d(TAG, "✅ " + featuredServices.size() + " servicios básicos reales cargados del hotel");

                        // Actualizar el adapter
                        setupServicesPreview();
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando servicios del hotel: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        setupFallbackServices();
                    });
                }
            }
        });
    }

    // ✅ INTERFACE PARA CALLBACK DE SERVICIOS DEL HOTEL
    private interface HotelServicesCallback {
        void onSuccess(List<HotelService> services);
        void onError(String error);
    }

    // ✅ MÉTODO PARA OBTENER SERVICIOS BÁSICOS DE UN HOTEL ESPECÍFICO
    private void getHotelBasicServices(String hotelAdminId, HotelServicesCallback callback) {
        Log.d(TAG, "🔍 Obteniendo servicios básicos para hotelAdminId: " + hotelAdminId);

        // Usar Firebase directamente para obtener servicios del hotel específico
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("hotel_services")
                .whereEqualTo("hotelAdminId", hotelAdminId)
                .whereEqualTo("serviceType", "basic")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<HotelService> services = new ArrayList<>();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            // Convertir HotelServiceModel a HotelService
                            String name = doc.getString("name");
                            String description = doc.getString("description");
                            String iconKey = doc.getString("iconKey");

                            if (name != null && description != null) {
                                // ✅ USAR EL CONSTRUCTOR COMPLETO PARA ESPECIFICAR CATEGORÍA
                                HotelService service = new HotelService(
                                        doc.getId(),                    // id
                                        name,                          // name
                                        description,                   // description
                                        null,                          // price (servicios básicos son gratis)
                                        null,                          // imageUrl
                                        new ArrayList<>(),             // imageUrls
                                        iconKey != null ? iconKey : "ic_hotel_service_default", // iconResourceName
                                        false,                         // isConditional
                                        null,                          // conditionalDescription
                                        HotelService.ServiceCategory.ESSENTIALS, // ✅ CATEGORÍA BÁSICOS
                                        false,                         // isPopular
                                        0,                             // sortOrder
                                        "24/7",                        // availability
                                        null,                          // features
                                        false                          // isIncludedInRoom (false para básicos)
                                );

                                services.add(service);
                                Log.d(TAG, "✅ Servicio básico agregado: " + name);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parseando servicio: " + e.getMessage());
                        }
                    }

                    Log.d(TAG, "✅ Total servicios básicos obtenidos: " + services.size());
                    callback.onSuccess(services);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error obteniendo servicios de Firebase: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    // ✅ FALLBACK: Servicios por defecto si no se encuentran servicios del hotel - CORREGIDO
    private void setupFallbackServices() {
        Log.d(TAG, "⚠️ Usando servicios básicos por defecto");

        featuredServices.clear();

        // ✅ USAR EL CONSTRUCTOR COMPLETO PARA ESPECIFICAR CATEGORÍA ESSENTIALS (básicos)
        featuredServices.add(new HotelService("wifi", "WiFi Gratuito", "Internet de alta velocidad",
                null, null, new ArrayList<>(), "ic_wifi", false, null,
                HotelService.ServiceCategory.ESSENTIALS, false, 0, "24/7", null, false));
        featuredServices.add(new HotelService("reception", "Recepción 24h", "Atención las 24 horas",
                null, null, new ArrayList<>(), "ic_reception", false, null,
                HotelService.ServiceCategory.ESSENTIALS, false, 0, "24/7", null, false));
        featuredServices.add(new HotelService("ac", "Aire Acondicionado", "Climatización individual",
                null, null, new ArrayList<>(), "ic_ac", false, null,
                HotelService.ServiceCategory.ESSENTIALS, false, 0, "24/7", null, false));
        featuredServices.add(new HotelService("tv", "TV por Cable", "Televisión con canales premium",
                null, null, new ArrayList<>(), "ic_tv", false, null,
                HotelService.ServiceCategory.ESSENTIALS, false, 0, "24/7", null, false));

        setupServicesPreview();
    }

    private void updatePriceWithRealRoomPrice(HotelProfile hotel) {
        Log.d(TAG, "💰 Obteniendo precio real de habitación más barata");

        HotelPriceUtils.getMinimumRoomPrice(hotel, getContext(), new HotelPriceUtils.PriceCallback() {
            @Override
            public void onPriceObtained(String formattedPrice, double rawPrice) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // ✅ ACTUALIZAR PRECIO PARA 1 NOCHE
                        tvHotelPrice.setText(formattedPrice);
                        tvPriceDescription.setText("por noche");

                        Log.d(TAG, "✅ Precio actualizado: " + formattedPrice + " por noche");
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "⚠️ Error obteniendo precio real: " + error);
                // Mantener precio por defecto
            }
        });
    }

    // ✅ NUEVO: Configurar estancia por defecto (1 noche, hoy-mañana, 2 adultos)
    private void setupDefaultStayInfo() {
        // Configurar 1 noche por defecto
        if (tvStayDates != null) {
            tvStayDates.setText("1 noche");
        }

        // Configurar fechas (hoy - mañana) y huéspedes (2 adultos)
        if (tvStayDetail != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", new Locale("es", "ES"));

            Calendar today = Calendar.getInstance();
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.add(Calendar.DAY_OF_MONTH, 1);

            String todayStr = dateFormat.format(today.getTime());
            String tomorrowStr = dateFormat.format(tomorrow.getTime());

            String stayDetail = todayStr + " - " + tomorrowStr + " | 2 adultos";
            tvStayDetail.setText(stayDetail);
        }

        Log.d(TAG, "✅ Estancia configurada: 1 noche, hoy-mañana, 2 adultos");
    }

    // ✅ MÉTODO MEJORADO: Configurar datos por defecto si no se encuentra el hotel
    private void setupDefaultHotelData() {
        // ✅ CONFIGURAR UBICACIÓN POR DEFECTO (coordenadas de ejemplo para Lima, Miraflores)
        hotelLocation = new LatLng(-12.1191, -77.0306);

        // ✅ CONFIGURAR FOTOS POR DEFECTO
        hotelImageUrls.clear();
        hotelImageUrls.addAll(Arrays.asList(
                "https://example.com/hotel_default_1.jpg",
                "https://example.com/hotel_default_2.jpg",
                "https://example.com/hotel_default_3.jpg",
                "https://example.com/hotel_default_4.jpg"
        ));

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                setupImageGalleryWithRealPhotos();

                // ✅ CONFIGURAR LUGARES CERCANOS TAMBIÉN CON COORDENADAS POR DEFECTO
                setupNearbyPlaces();

                // ✅ CARGAR SERVICIOS POR DEFECTO
                setupFallbackServices();

                if (mMap != null) {
                    updateMapWithHotelLocation();
                }
            });
        }

        Log.d(TAG, "⚠️ Usando datos por defecto para el hotel");
    }

    private void setupImageNavigationButtons() {
        if (btnPreviousImage != null) {
            btnPreviousImage.setOnClickListener(v -> {
                int currentPosition = viewPagerImages.getCurrentItem();
                if (currentPosition > 0) {
                    viewPagerImages.setCurrentItem(currentPosition - 1, true);
                }
            });
        }

        if (btnNextImage != null) {
            btnNextImage.setOnClickListener(v -> {
                int currentPosition = viewPagerImages.getCurrentItem();
                if (currentPosition < hotelImageUrls.size() - 1) {
                    viewPagerImages.setCurrentItem(currentPosition + 1, true);
                }
            });
        }
    }

    private void setupClickListeners() {
        // Click en "Ver todo" - navegar a ViewAllServicesActivity
        if (tvSeeAllServices != null) {
            tvSeeAllServices.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ViewAllServicesActivity.class);
                intent.putExtra("hotel_name", getHotelName());
                intent.putExtra("mode", "browse_only");
                startActivity(intent);
            });
        }
    }

    @Override
    public void onServiceClicked(HotelService service) {
        try {
            Log.d(TAG, "Servicio clickeado: " + service.getName());

            if (service.getId().equals("taxi")) {
                showTaxiInfoDialog(service);
            } else {
                showServiceInfoDialog(service);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error en onServiceClicked: " + e.getMessage());
        }
    }

    private void showTaxiInfoDialog(HotelService taxiService) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.client_dialog_taxi_info_preview, null);

        ImageView ivTaxiIcon = dialogView.findViewById(R.id.iv_taxi_icon);
        TextView tvTitle = dialogView.findViewById(R.id.tv_taxi_title);
        TextView tvDescription = dialogView.findViewById(R.id.tv_taxi_description);
        TextView tvCondition = dialogView.findViewById(R.id.tv_taxi_condition);
        MaterialButton btnGotIt = dialogView.findViewById(R.id.btn_got_it);
        MaterialButton btnSeeAllServices = dialogView.findViewById(R.id.btn_see_all_services);

        tvTitle.setText(taxiService.getName());
        tvDescription.setText(taxiService.getDescription());

        if (taxiService.isEligibleForFree()) {
            tvCondition.setText("🎉 ¡Este servicio sería GRATUITO con tu reserva actual!");
            tvCondition.setTextColor(ContextCompat.getColor(getContext(), R.color.success_green));
        } else {
            tvCondition.setText("💡 " + taxiService.getConditionalDescription());
            tvCondition.setTextColor(ContextCompat.getColor(getContext(), R.color.orange_primary));
        }

        AlertDialog dialog = builder.setView(dialogView).create();

        btnGotIt.setOnClickListener(v -> dialog.dismiss());

        btnSeeAllServices.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(getActivity(), ViewAllServicesActivity.class);
            intent.putExtra("hotel_name", getHotelName());
            intent.putExtra("mode", "browse_only");
            startActivity(intent);
        });

        dialog.show();
    }

    private void showServiceInfoDialog(HotelService service) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        String message = service.getName() + "\n\n" + service.getDescription();
        if (!service.getFeatures().isEmpty()) {
            message += "\n\nCaracterísticas:\n• " + String.join("\n• ", service.getFeatures());
        }

        builder.setTitle("Información del servicio")
                .setMessage(message)
                .setPositiveButton("Ver todos los servicios", (dialog, which) -> {
                    Intent intent = new Intent(getActivity(), ViewAllServicesActivity.class);
                    intent.putExtra("hotel_name", getHotelName());
                    intent.putExtra("mode", "browse_only");
                    startActivity(intent);
                })
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private String getHotelName() {
        if (getArguments() != null) {
            return getArguments().getString("hotel_name", "Hotel");
        }
        return "Hotel";
    }

    private void setupServicesPreview() {
        if (rvServicesPreview != null) {
            // ✅ VERIFICAR si ya tiene adaptador
            if (rvServicesPreview.getAdapter() == null) {
                // Primera vez - crear adaptador
                FeaturedServicesAdapter adapter = new FeaturedServicesAdapter(featuredServices, this);

                GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
                rvServicesPreview.setLayoutManager(layoutManager);
                rvServicesPreview.setAdapter(adapter);

                Log.d(TAG, "Adaptador creado por primera vez");
            } else {
                // Ya existe - solo actualizar datos
                rvServicesPreview.getAdapter().notifyDataSetChanged();
                Log.d(TAG, "Adaptador actualizado");
            }

            Log.d(TAG, "Services preview configurado con " + featuredServices.size() + " servicios");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // ✅ RECARGAR servicios solo si es necesario
        if (featuredServices.isEmpty()) {
            setupFeaturedServices();
            setupServicesPreview();
        }

        Log.d(TAG, "onResume - Servicios: " + featuredServices.size());
    }

    private void setupFeaturedServices() {
        // ✅ ESTE MÉTODO AHORA ES SOLO FALLBACK
        // Los servicios reales se cargan en loadRealHotelBasicServices()
        Log.d(TAG, "⚠️ setupFeaturedServices llamado como fallback");
        setupFallbackServices();
    }

    private boolean isServiceAlreadyAdded(String serviceId) {
        for (HotelService service : featuredServices) {
            if (service.getId().equals(serviceId)) {
                return true;
            }
        }
        return false;
    }

    // ✅ NUEVO MÉTODO PARA CONFIGURAR EL MAPA
    private void setupMap() {
        try {
            // Obtener el fragmento del mapa
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map_fragment);

            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }

            // Configurar botón para abrir en Google Maps
            if (btnOpenInMaps != null) {
                btnOpenInMaps.setOnClickListener(v -> openInGoogleMaps());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error configurando mapa: " + e.getMessage());
        }
    }

    // ✅ IMPLEMENTAR OnMapReadyCallback
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        try {
            mMap = googleMap;

            // Configurar el mapa
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);

            // Si ya tenemos la ubicación del hotel, configurar el mapa
            if (hotelLocation != null) {
                updateMapWithHotelLocation();
            }

            // Configurar listener para abrir Google Maps cuando se toque el marcador
            mMap.setOnMarkerClickListener(marker -> {
                openInGoogleMaps();
                return true;
            });

            Log.d(TAG, "Mapa configurado correctamente");

        } catch (Exception e) {
            Log.e(TAG, "Error en onMapReady: " + e.getMessage());
        }
    }

    // ✅ NUEVO: Actualizar mapa con ubicación real del hotel
    private void updateMapWithHotelLocation() {
        if (mMap == null || hotelLocation == null) return;

        try {
            // Limpiar marcadores anteriores
            mMap.clear();

            // Agregar marcador del hotel
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(hotelLocation)
                    .title(hotelName != null ? hotelName : "Hotel")
                    .snippet(hotelAddress != null ? hotelAddress : "")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            mMap.addMarker(markerOptions);

            // Centrar el mapa en el hotel con zoom apropiado
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hotelLocation, 15f));

            Log.d(TAG, "✅ Mapa actualizado con ubicación real del hotel");

        } catch (Exception e) {
            Log.e(TAG, "Error actualizando mapa: " + e.getMessage());
        }
    }

    // ✅ MÉTODO PARA ABRIR GOOGLE MAPS
    private void openInGoogleMaps() {
        try {
            if (hotelLocation != null) {
                // Crear URI para Google Maps
                String uri = String.format("geo:%f,%f?q=%f,%f(%s)",
                        hotelLocation.latitude, hotelLocation.longitude,
                        hotelLocation.latitude, hotelLocation.longitude,
                        hotelName != null ? hotelName.replace(" ", "+") : "Hotel");

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");

                // Verificar si Google Maps está instalado
                if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    // Si Google Maps no está instalado, abrir en navegador
                    String webUri = String.format("https://www.google.com/maps/search/?api=1&query=%f,%f",
                            hotelLocation.latitude, hotelLocation.longitude);
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUri));
                    startActivity(webIntent);
                }

                Log.d(TAG, "Abriendo Google Maps");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error abriendo Google Maps: " + e.getMessage());
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error abriendo mapas", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupNearbyPlaces() {
        Log.d(TAG, "=== INICIANDO setupNearbyPlaces ===");

        try {
            // Verificar que hotelLocation existe
            if (hotelLocation == null) {
                Log.e(TAG, "❌ hotelLocation es NULL - usando coordenadas por defecto");
                hotelLocation = new LatLng(-12.1191, -77.0306); // Miraflores por defecto
            }

            Log.d(TAG, "✅ Hotel location: " + hotelLocation.latitude + ", " + hotelLocation.longitude);

            // Inicializar repositorio
            nearbyPlacesRepository = new NearbyPlacesRepository();
            Log.d(TAG, "✅ Repository inicializado");

            // ✅ ASEGURAR QUE nearbyPlacesList ESTÉ INICIALIZADA
            if (nearbyPlacesList == null) {
                nearbyPlacesList = new ArrayList<>();
            }

            // Configurar adaptador con datos dinámicos
            enhancedCitiesAdapter = new EnhancedCitiesAdapter(nearbyPlacesList, "AIzaSyBdghOu6DZktjZcg0_PJzffH72NC-nR0ok", hotelLocation);
            rvNearbyPlaces.setLayoutManager(
                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            rvNearbyPlaces.setAdapter(enhancedCitiesAdapter);

            Log.d(TAG, "✅ Adaptador configurado");

            // Configurar click listener
            enhancedCitiesAdapter.setOnPlaceClickListener((place, position) -> {
                Log.d(TAG, "Click en lugar: " + place.getName());
                openPlaceInMaps(place);
            });

            // ✅ LLAMAR A CARGAR LUGARES INMEDIATAMENTE
            Log.d(TAG, "🔄 Iniciando carga de lugares...");
            loadNearbyPlaces();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error configurando lugares cercanos: " + e.getMessage());
            e.printStackTrace();
            setupStaticNearbyPlaces();
        }
    }

    // ✅ MÉTODO YA FUNCIONA - ARREGLADO PARA USAR DIRECCIONES EN LUGAR DE COORDENADAS
    private void openPlaceInMaps(NearbyPlace place) {
        try {
            if (hotelLocation == null || hotelName == null) {
                // Fallback: abrir solo el destino
                String destinationQuery = place.getName().replace(" ", "+");
                if (place.getVicinity() != null) {
                    destinationQuery += "+" + place.getVicinity().replace(" ", "+");
                }
                String uri = "https://www.google.com/maps/search/?api=1&query=" + destinationQuery;
                openMapIntent(uri);
                return;
            }

            // ✅ CREAR RUTA USANDO DIRECCIONES LEGIBLES (como funcionaba antes)
            String origin = hotelName.replace(" ", "+");
            if (hotelAddress != null) {
                origin += "+" + hotelAddress.replace(" ", "+");
            }

            String destination = place.getName().replace(" ", "+");
            if (place.getVicinity() != null) {
                destination += "+" + place.getVicinity().replace(" ", "+");
            }

            // ✅ URI MEJORADA CON DIRECCIONES EN LUGAR DE COORDENADAS
            String routeUri = String.format(
                    "https://www.google.com/maps/dir/?api=1&origin=%s&destination=%s&travelmode=walking",
                    origin, destination
            );

            Log.d(TAG, "🗺️ Abriendo ruta desde hotel a: " + place.getName());
            Log.d(TAG, "📍 Origen: " + origin);
            Log.d(TAG, "📍 Destino: " + destination);
            Log.d(TAG, "🔗 URI: " + routeUri);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(routeUri));
            intent.setPackage("com.google.android.apps.maps");

            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);

                // Mostrar toast informativo (como funcionaba antes)
                Toast.makeText(getContext(),
                        "🗺️ Ruta desde " + hotelName + " a " + place.getName(),
                        Toast.LENGTH_LONG).show();
            } else {
                // Si Google Maps no está instalado, abrir en navegador
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(routeUri));
                startActivity(webIntent);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error abriendo ruta: " + e.getMessage());
            Toast.makeText(getContext(), "Error abriendo mapa", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMapIntent(String uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(webIntent);
        }
    }

    private void loadNearbyPlaces() {
        Log.d(TAG, "=== INICIANDO loadNearbyPlaces ===");

        if (hotelLocation != null) {
            Log.d(TAG, "🌍 Buscando lugares cerca de: " + hotelLocation.latitude + ", " + hotelLocation.longitude);

            nearbyPlacesRepository.getNearbyTouristAttractions(
                    hotelLocation.latitude,
                    hotelLocation.longitude,
                    2000, // Radio de 2km
                    new NearbyPlacesRepository.NearbyPlacesCallback() {
                        @Override
                        public void onSuccess(List<NearbyPlace> places) {
                            Log.d(TAG, "✅ SUCCESS: Recibidos " + places.size() + " lugares");

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    nearbyPlacesList.clear();
                                    nearbyPlacesList.addAll(places);
                                    enhancedCitiesAdapter.updatePlaces(nearbyPlacesList);

                                    Log.d(TAG, "✅ UI actualizada con " + places.size() + " lugares");

                                    // ✅ MOSTRAR NOMBRES DE LOS LUGARES
                                    for (NearbyPlace place : places) {
                                        Log.d(TAG, "   📍 " + place.getName() + " - " + place.getVicinity());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "❌ ERROR cargando lugares: " + error);

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Log.d(TAG, "🔄 Usando lugares estáticos como fallback");
                                    setupStaticNearbyPlaces();
                                });
                            }
                        }
                    }
            );
        } else {
            Log.e(TAG, "❌ hotelLocation es null en loadNearbyPlaces");
            setupStaticNearbyPlaces();
        }
    }

    private void setupStaticNearbyPlaces() {
        List<City> nearbyPlaces = new ArrayList<>();
        nearbyPlaces.add(new City("Parque del Amor", R.drawable.lima));
        nearbyPlaces.add(new City("Larcomar", R.drawable.lima));
        nearbyPlaces.add(new City("Kennedy Park", R.drawable.lima));

        CitiesAdapter adapter = new CitiesAdapter(nearbyPlaces);
        rvNearbyPlaces.setAdapter(adapter);
    }

    private void updateImageCounter(int position) {
        // Actualizar el contador de imágenes (1/4, 2/4, etc.)
        String counterText = (position + 1) + "/" + hotelImageUrls.size();
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

    private void setupActions(View view) {
        try {
            // Configurar botón de retroceso - VERIFICAR SI EXISTE
            View btnBack = view.findViewById(R.id.btn_back);
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> {
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                });
                Log.d(TAG, "btn_back configurado correctamente");
            } else {
                Log.w(TAG, "btn_back no encontrado en el layout");
            }

            // Configurar botón de favoritos - VERIFICAR SI EXISTE
            View btnFavorite = view.findViewById(R.id.btn_favorite);
            if (btnFavorite != null) {
                btnFavorite.setOnClickListener(v -> {
                    // Implementar lógica para añadir a favoritos
                    Toast.makeText(getContext(), "Añadido a favoritos", Toast.LENGTH_SHORT).show();
                });
                Log.d(TAG, "btn_favorite configurado correctamente");
            } else {
                Log.w(TAG, "btn_favorite no encontrado en el layout");
            }

            // Configurar botón de elegir habitación - VERIFICAR SI EXISTE
            View btnChooseRoom = view.findViewById(R.id.btn_choose_room);
            if (btnChooseRoom != null) {
                btnChooseRoom.setOnClickListener(v -> {
                    navigateToRoomSelection();
                });
                Log.d(TAG, "btn_choose_room configurado correctamente");
            } else {
                Log.w(TAG, "btn_choose_room no encontrado en el layout");
            }

            // Ver todas las reseñas - VERIFICAR SI EXISTE
            View tvSeeAllReviews = view.findViewById(R.id.tv_see_all_reviews);
            if (tvSeeAllReviews != null) {
                tvSeeAllReviews.setOnClickListener(v -> {
                    navigateToAllReviews();
                });
                Log.d(TAG, "tv_see_all_reviews configurado correctamente");
            } else {
                Log.w(TAG, "tv_see_all_reviews no encontrado en el layout");
            }

            Log.d(TAG, "setupActions completado sin errores");

        } catch (Exception e) {
            Log.e(TAG, "Error en setupActions: " + e.getMessage());
            e.printStackTrace();

            // No lanzar el error, solo registrarlo
            if (getContext() != null) {
                Toast.makeText(getContext(), "Algunas funciones podrían no estar disponibles", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToAllReviews() {
        try {
            // Crear el fragmento de todas las reseñas
            AllReviewsFragment allReviewsFragment = new AllReviewsFragment();

            // Pasar datos del hotel al fragmento
            Bundle args = new Bundle();
            args.putString("hotel_name", tvHotelName.getText().toString());
            allReviewsFragment.setArguments(args);

            // Navegar al fragmento con animación
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                    )
                    .replace(R.id.fragment_container, allReviewsFragment)
                    .addToBackStack(null)
                    .commit();

            Log.d(TAG, "Navegando a AllReviewsFragment");

        } catch (Exception e) {
            Log.e(TAG, "Error navegando a reseñas: " + e.getMessage());
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error abriendo reseñas", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToRoomSelection() {
        try {
            Log.d(TAG, "🏨 Iniciando navegación a selección de habitaciones");

            // Crear el fragmento de selección de habitación
            RoomSelectionFragment roomSelectionFragment = new RoomSelectionFragment();

            // Pasar datos del hotel al fragmento
            Bundle args = new Bundle();

            // ✅ DATOS BÁSICOS (ya existentes)
            String hotelName = tvHotelName != null ? tvHotelName.getText().toString() : "Hotel";
            String hotelPrice = tvHotelPrice != null ? tvHotelPrice.getText().toString() : "S/0";

            args.putString("hotel_name", hotelName);
            args.putString("hotel_price", hotelPrice);

            // ✅ NUEVO: Pasar el HotelProfile completo si está disponible
            if (currentHotel != null) {
                Log.d(TAG, "✅ Pasando HotelProfile completo: " + currentHotel.getName() +
                        " (AdminId: " + currentHotel.getHotelAdminId() + ")");
                args.putParcelable("hotel_profile", currentHotel);
            } else {
                // ✅ FALLBACK: Si no hay currentHotel, intentar obtener hotelAdminId desde argumentos
                String hotelAdminId = getHotelAdminIdFromArguments();
                if (hotelAdminId != null) {
                    Log.d(TAG, "✅ Usando hotelAdminId desde argumentos: " + hotelAdminId);
                    args.putString("hotel_admin_id", hotelAdminId);
                } else {
                    Log.w(TAG, "⚠️ No se puede obtener hotelAdminId - Las habitaciones no se podrán cargar");
                    Toast.makeText(getContext(), "Error: No se puede acceder a las habitaciones del hotel", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            roomSelectionFragment.setArguments(args);

            // Navegar al fragmento con animación
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                    )
                    .replace(R.id.fragment_container, roomSelectionFragment)
                    .addToBackStack("room_selection")
                    .commit();

            Log.d(TAG, "✅ Navegación a RoomSelectionFragment completada");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error navegando a selección de habitaciones: " + e.getMessage());
            Toast.makeText(getContext(), "Error abriendo selección de habitaciones", Toast.LENGTH_SHORT).show();
        }
    }

    private String getHotelAdminIdFromArguments() {
        if (getArguments() != null) {
            // Buscar en diferentes posibles nombres de argumentos
            String hotelAdminId = getArguments().getString("hotel_admin_id");
            if (hotelAdminId == null) {
                hotelAdminId = getArguments().getString("hotelAdminId");
            }
            if (hotelAdminId == null) {
                hotelAdminId = getArguments().getString("admin_id");
            }

            Log.d(TAG, "🔍 HotelAdminId desde argumentos: " + hotelAdminId);
            return hotelAdminId;
        }
        return null;
    }
    public void setCurrentHotel(HotelProfile hotel) {
        this.currentHotel = hotel;
        Log.d(TAG, "✅ HotelProfile establecido: " + (hotel != null ? hotel.getName() : "null"));
    }
    public HotelProfile getCurrentHotel() {
        return currentHotel;
    }
}