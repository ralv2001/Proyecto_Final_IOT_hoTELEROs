package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelProfile;
import com.example.proyecto_final_hoteleros.client.data.model.RoomType;
import com.example.proyecto_final_hoteleros.client.utils.UserDataManager;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseHotelManager;
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;
import com.example.proyecto_final_hoteleros.models.UserModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class BookingSummaryFragment extends Fragment implements AddPaymentDialogFragment.PaymentDialogListener {

    private static final String TAG = "BookingSummaryFragment";

    // UI Components
    private ImageButton btnBack;
    private ImageView imgHotelBanner;
    private TextView tvHotelName, tvHotelAddress, tvRating;
    private TextView tvCheckInOut, tvNumberOfGuests, tvRoomType, tvRoomNumber, tvFreeTransport;
    private TextView tvRoomPriceValue, tvAdditionalServices, tvTotalPrice;
    private MaterialButton btnConfirmReservation;
    private MaterialButton btnAddPaymentMethod;
    private MaterialButton btnChangeCard;
    private ConstraintLayout layoutCardInfo;
    private TextView tvCardNumber, tvCardName, tvPaymentInfo;
    private ConstraintLayout confirmationDialogOverlay;
    private MaterialButton btnOk;

    // ✅ NUEVAS VISTAS para servicios seleccionados
    private CardView cardSelectedServices;
    private View layoutTaxiDetail;
    private TextView tvTaxiServiceStatus, tvTaxiServiceDescription;
    private LinearLayout roomServicesInfo;
    private TextView tvRoomServicesList;

    // State variables
    private boolean isPaymentMethodAdded = false;
    private String savedCardNumber = "";
    private String savedCardHolderName = "";

    // ✅ DATOS DINÁMICOS DE FIREBASE
    private HotelProfile currentHotel;
    private RoomType selectedRoom;
    private UserModel currentUser;
    private String selectedServices = "";
    private boolean isTaxiIncluded = false;
    private double roomPrice = 0.0;
    private double additionalServicesPrice = 0.0;
    private double totalPrice = 0.0;

    // ✅ DATOS DE RESERVA
    private String checkInDate;
    private String checkOutDate;
    private int numAdults;
    private int numChildren;
    private String roomNumber;

    // ✅ FIREBASE MANAGERS
    private FirebaseHotelManager hotelManager;
    private FirebaseManager firebaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.client_fragment_booking_summary, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ✅ INICIALIZAR MANAGERS DE FIREBASE
        initFirebaseManagers();

        // ✅ OBTENER DATOS PASADOS DESDE EL FRAGMENTO ANTERIOR
        retrieveArguments();

        // ✅ CARGAR DATOS DEL USUARIO ACTUAL
        loadCurrentUserData();

        // ✅ CARGAR DATOS DEL HOTEL DESDE FIREBASE
        loadHotelDataFromFirebase();

        // ✅ CONFIGURAR ACCIONES
        setupActions();

        // ✅ CONFIGURAR ESTADO INICIAL DEL BOTÓN
        updateConfirmButtonState();

        // ✅ CONFIGURAR INFORMACIÓN INICIAL DEL TAXI Y SERVICIOS (antes de cargar hotel)
        setupTaxiDisplay();
        setupSelectedServices();

        // ✅ CALCULAR PRECIOS INICIALES
        calculateAndDisplayPrices();

        // ✅ ANIMAR ENTRADA DE TARJETAS
        animateCardEntrance();
    }

    private void initViews(View view) {
        // Vistas existentes
        btnBack = view.findViewById(R.id.btn_back);
        imgHotelBanner = view.findViewById(R.id.img_hotel_banner);
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        tvHotelAddress = view.findViewById(R.id.tv_hotel_address);
        tvRating = view.findViewById(R.id.tv_rating);
        tvCheckInOut = view.findViewById(R.id.tv_check_in_out);
        tvNumberOfGuests = view.findViewById(R.id.tv_number_of_guests);
        tvRoomType = view.findViewById(R.id.tv_room_type);
        tvRoomNumber = view.findViewById(R.id.tv_room_number);
        tvFreeTransport = view.findViewById(R.id.tv_free_transport);
        tvRoomPriceValue = view.findViewById(R.id.tv_room_price_value);
        tvAdditionalServices = view.findViewById(R.id.tv_additional_services);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        btnAddPaymentMethod = view.findViewById(R.id.btn_add_payment_method);
        layoutCardInfo = view.findViewById(R.id.layout_card_info);
        tvCardNumber = view.findViewById(R.id.tv_card_number);
        tvCardName = view.findViewById(R.id.tv_card_name);
        btnChangeCard = view.findViewById(R.id.btn_change_card);
        tvPaymentInfo = view.findViewById(R.id.tv_payment_info);
        btnConfirmReservation = view.findViewById(R.id.btn_confirm_reservation);
        confirmationDialogOverlay = view.findViewById(R.id.confirmation_dialog_overlay);
        btnOk = view.findViewById(R.id.btn_ok);

        // ✅ NUEVAS VISTAS para servicios seleccionados
        cardSelectedServices = view.findViewById(R.id.card_selected_services);
        layoutTaxiDetail = view.findViewById(R.id.layout_taxi_detail);
        tvTaxiServiceStatus = view.findViewById(R.id.tv_taxi_service_status);
        tvTaxiServiceDescription = view.findViewById(R.id.tv_taxi_service_description);
        roomServicesInfo = view.findViewById(R.id.room_services_info);
        tvRoomServicesList = view.findViewById(R.id.tv_room_services_list);
    }

    private void initFirebaseManagers() {
        hotelManager = FirebaseHotelManager.getInstance(requireContext());
        firebaseManager = FirebaseManager.getInstance();
    }

    private void retrieveArguments() {
        if (getArguments() != null) {
            // ✅ OBTENER DATOS DE LA RESERVA
            selectedRoom = getArguments().getParcelable("selected_room");
            checkInDate = getArguments().getString("check_in_date", "");
            checkOutDate = getArguments().getString("check_out_date", "");
            numAdults = getArguments().getInt("num_adults", 2);
            numChildren = getArguments().getInt("num_children", 0);
            additionalServicesPrice = getArguments().getDouble("additional_services_price", 0.0);
            selectedServices = getArguments().getString("selected_services", "");

            // ✅ DATOS DEL HOTEL
            String hotelName = getArguments().getString("hotel_name", "");
            String hotelAddress = getArguments().getString("hotel_address", "");
            float hotelRating = getArguments().getFloat("hotel_rating", 4.9f);

            Log.d(TAG, "✅ Argumentos obtenidos:");
            Log.d(TAG, "   - Hotel: " + hotelName);
            Log.d(TAG, "   - Fechas: " + checkInDate + " - " + checkOutDate);
            Log.d(TAG, "   - Huéspedes: " + numAdults + " adultos, " + numChildren + " niños");
            Log.d(TAG, "   - Servicios adicionales: " + selectedServices);
            Log.d(TAG, "   - Precio servicios: " + additionalServicesPrice);

            // ✅ CONFIGURAR DATOS BÁSICOS INMEDIATAMENTE CON VALIDACIÓN
            setupBasicReservationDataFromArguments();

            // ✅ CALCULAR SI TAXI ESTÁ INCLUIDO
            calculateTaxiStatus();
        } else {
            Log.w(TAG, "⚠️ No se recibieron argumentos, usando valores por defecto");
            setDefaultValues();
        }
    }

    private void setupBasicReservationDataFromArguments() {
        // ✅ CONFIGURAR FECHAS - ASEGURAR QUE SE MUESTREN LAS REALES
        String displayDates;
        if (!checkInDate.isEmpty() && !checkOutDate.isEmpty()) {
            displayDates = String.format("%s - %s", checkInDate, checkOutDate);
        } else if (!checkInDate.isEmpty()) {
            displayDates = checkInDate + " - (sin fecha fin)";
        } else {
            displayDates = "Fechas no especificadas";
        }

        if (tvCheckInOut != null) {
            tvCheckInOut.setText(displayDates);
            Log.d(TAG, "📅 Fechas configuradas en UI: " + displayDates);
        }

        // ✅ CONFIGURAR HUÉSPEDES - FORZAR ACTUALIZACIÓN
        String guestsText = String.format(Locale.getDefault(), "%d adultos - %d niños", numAdults, numChildren);
        if (tvNumberOfGuests != null) {
            tvNumberOfGuests.setText(guestsText);
            Log.d(TAG, "👥 Huéspedes configurados en UI: " + guestsText);
        }

        // ✅ CONFIGURAR TIPO DE HABITACIÓN
        if (selectedRoom != null) {
            if (tvRoomType != null) {
                tvRoomType.setText(selectedRoom.getName());
            }
            roomPrice = extractPriceFromRoom(selectedRoom);
            Log.d(TAG, "💰 Precio de habitación extraído: S/ " + roomPrice);
        } else {
            if (tvRoomType != null) {
                tvRoomType.setText("Habitación no especificada");
            }
            roomPrice = 0.0;
        }
    }

    private double extractPriceFromRoom(RoomType room) {
        try {
            if (room.getPrice() != null) {
                String priceStr = room.getPrice().replace("S/", "").replace("S/ ", "").trim();
                return Double.parseDouble(priceStr);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "❌ Error extrayendo precio de habitación: " + e.getMessage());
        }
        return 0.0;
    }

    private void calculateTaxiStatus() {
        if (selectedServices != null && !selectedServices.isEmpty()) {
            isTaxiIncluded = selectedServices.toLowerCase().contains("taxi");
            Log.d(TAG, "🚖 Taxi incluido: " + (isTaxiIncluded ? "SÍ" : "NO"));
        } else {
            isTaxiIncluded = false;
        }
    }

    private void loadCurrentUserData() {
        // ✅ OBTENER DATOS DEL USUARIO ACTUAL
        UserDataManager userManager = UserDataManager.getInstance();
        String userId = userManager.getUserId();

        if (userId != null && !userId.startsWith("guest_")) {
            // ✅ CARGAR DATOS COMPLETOS DESDE FIREBASE
            firebaseManager.getUserDataFromAnyCollection(userId, new FirebaseManager.UserCallback() {
                @Override
                public void onUserFound(UserModel user) {
                    currentUser = user;

                    // ✅ GENERAR NÚMERO DE HABITACIÓN BASADO EN EL DOCUMENTO
                    generateRoomNumberFromUserData(user);

                    Log.d(TAG, "✅ Datos del usuario cargados: " + user.getFullName());
                }

                @Override
                public void onUserNotFound() {
                    Log.w(TAG, "⚠️ Usuario no encontrado en Firebase");
                    generateRandomRoomNumber();
                }

                @Override
                public void onError(String error) {
                    Log.w(TAG, "⚠️ Error cargando datos del usuario: " + error);
                    generateRandomRoomNumber();
                }
            });
        } else {
            // ✅ USUARIO HUÉSPED - GENERAR NÚMERO ALEATORIO
            Log.d(TAG, "👤 Usuario huésped, generando número de habitación aleatorio");
            generateRandomRoomNumber();
        }
    }

    private void generateRoomNumberFromUserData(UserModel user) {
        try {
            // ✅ USAR NÚMERO DE DOCUMENTO PARA GENERAR NÚMERO DE HABITACIÓN ÚNICO
            String documentNumber = user.getNumeroDocumento();
            if (documentNumber != null && !documentNumber.isEmpty() && !documentNumber.equals("")) {
                // Tomar los últimos 3 dígitos del documento y agregar un piso aleatorio
                String lastDigits = documentNumber.length() >= 3 ?
                        documentNumber.substring(documentNumber.length() - 3) : documentNumber;

                int floor = (documentNumber.hashCode() % 9) + 1; // Piso del 1 al 9
                roomNumber = String.format(Locale.getDefault(), "%d%s", floor, lastDigits);

                Log.d(TAG, "🏠 Número de habitación generado desde documento: " + roomNumber);
            } else {
                generateRandomRoomNumber();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error generando número desde documento: " + e.getMessage());
            generateRandomRoomNumber();
        }

        // ✅ ACTUALIZAR UI
        if (tvRoomNumber != null) {
            tvRoomNumber.setText(roomNumber);
        }
    }

    private void generateRandomRoomNumber() {
        Random random = new Random();
        int floor = random.nextInt(9) + 1; // Piso 1-9
        int room = random.nextInt(99) + 1;  // Habitación 1-99
        roomNumber = String.format(Locale.getDefault(), "%d%02d", floor, room);

        Log.d(TAG, "🎲 Número de habitación aleatorio: " + roomNumber);

        if (tvRoomNumber != null) {
            tvRoomNumber.setText(roomNumber);
        }
    }

    private void loadHotelDataFromFirebase() {
        String hotelName = getArguments() != null ? getArguments().getString("hotel_name", "") : "";

        if (hotelName.isEmpty()) {
            Log.w(TAG, "⚠️ No se especificó nombre del hotel");
            setDefaultHotelValues();
            return;
        }

        Log.d(TAG, "🔍 Buscando hotel en Firebase: " + hotelName);

        // ✅ BUSCAR HOTEL EN FIREBASE
        hotelManager.findHotelsNearLocation(0, 0, 999999, new FirebaseHotelManager.HotelsCallback() {
            @Override
            public void onSuccess(List<HotelProfile> hotels) {
                // ✅ BUSCAR HOTEL POR NOMBRE
                for (HotelProfile hotel : hotels) {
                    if (hotel.getName() != null && hotel.getName().equalsIgnoreCase(hotelName)) {
                        currentHotel = hotel;
                        Log.d(TAG, "✅ Hotel encontrado en Firebase: " + hotel.getName());

                        // ✅ ACTUALIZAR UI EN EL HILO PRINCIPAL
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                updateUIWithHotelData(hotel);
                            });
                        }
                        return;
                    }
                }

                Log.w(TAG, "⚠️ Hotel no encontrado en Firebase: " + hotelName);
                setDefaultHotelValues();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error buscando hotel: " + error);
                setDefaultHotelValues();
            }
        });
    }

    private void updateUIWithHotelData(HotelProfile hotel) {
        // ✅ ACTUALIZAR INFORMACIÓN BÁSICA DEL HOTEL
        tvHotelName.setText(hotel.getName());
        tvHotelAddress.setText(hotel.getFullAddress() != null ? hotel.getFullAddress() : hotel.getAddress());

        // ✅ GENERAR RATING DINÁMICO (ya que HotelProfile no tiene getRating())
        double generatedRating = generateRatingFromProfile(hotel);
        tvRating.setText(String.format(Locale.getDefault(), "%.1f", generatedRating));

        // ✅ CARGAR IMAGEN REAL DEL HOTEL DESDE FIREBASE
        loadHotelImageFromFirebase(hotel);

        // ✅ RECONFIGURAR DATOS DE RESERVA PARA ASEGURAR QUE NO SE PIERDAN
        refreshReservationDataInUI();

        // ✅ CONFIGURAR INFORMACIÓN DEL TAXI
        setupTaxiDisplay();

        // ✅ CONFIGURAR SERVICIOS SELECCIONADOS
        setupSelectedServices();

        // ✅ CALCULAR Y MOSTRAR PRECIOS
        calculateAndDisplayPrices();
    }

    /**
     * ✅ MÉTODO PARA REFRESCAR DATOS DE RESERVA EN LA UI
     * Asegura que los datos reales se mantengan después de cargar el hotel
     */
    private void refreshReservationDataInUI() {
        // ✅ RECONFIGURAR FECHAS
        String displayDates;
        if (!checkInDate.isEmpty() && !checkOutDate.isEmpty()) {
            displayDates = String.format("%s - %s", checkInDate, checkOutDate);
        } else if (!checkInDate.isEmpty()) {
            displayDates = checkInDate + " - (sin fecha fin)";
        } else {
            displayDates = "Fechas no especificadas";
        }

        if (tvCheckInOut != null) {
            tvCheckInOut.setText(displayDates);
            Log.d(TAG, "🔄 Fechas refrescadas: " + displayDates);
        }

        // ✅ RECONFIGURAR HUÉSPEDES
        String guestsText = String.format(Locale.getDefault(), "%d adultos - %d niños", numAdults, numChildren);
        if (tvNumberOfGuests != null) {
            tvNumberOfGuests.setText(guestsText);
            Log.d(TAG, "🔄 Huéspedes refrescados: " + guestsText);
        }

        // ✅ RECONFIGURAR HABITACIÓN
        if (selectedRoom != null && tvRoomType != null) {
            tvRoomType.setText(selectedRoom.getName());
        }
    }

    /**
     * ✅ GENERAR RATING CONSISTENTE BASADO EN EL HOTEL
     * Similar a como se hace en otros archivos del proyecto
     */
    private double generateRatingFromProfile(HotelProfile hotel) {
        if (hotel != null && hotel.getName() != null) {
            // Usar el hash del nombre para generar un rating consistente
            Random random = new Random(hotel.getName().hashCode());
            double rating = 4.0 + (random.nextDouble() * 1.0); // Entre 4.0 y 5.0

            // Redondear a 1 decimal
            rating = Math.round(rating * 10.0) / 10.0;

            return rating;
        }
        return 4.5; // Rating por defecto
    }

    private void loadHotelImageFromFirebase(HotelProfile hotel) {
        if (hotel.getPhotoUrls() != null && !hotel.getPhotoUrls().isEmpty()) {
            String firstImageUrl = hotel.getPhotoUrls().get(0);

            Log.d(TAG, "📸 Cargando imagen del hotel desde Firebase: " + firstImageUrl);

            // ✅ USAR GLIDE PARA CARGAR IMAGEN
            Glide.with(this)
                    .load(firstImageUrl)
                    .transform(new CenterCrop(), new RoundedCorners(16))
                    .placeholder(R.drawable.belmond) // Imagen por defecto mientras carga
                    .error(R.drawable.belmond) // Imagen por defecto si hay error
                    .into(imgHotelBanner);
        } else {
            Log.d(TAG, "📸 Hotel sin imágenes, usando imagen por defecto");
            imgHotelBanner.setImageResource(R.drawable.belmond);
        }
    }

    private void setDefaultHotelValues() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                tvHotelName.setText("Hotel no encontrado");
                tvHotelAddress.setText("Ubicación no disponible");
                tvRating.setText("0.0");
                imgHotelBanner.setImageResource(R.drawable.belmond);

                setupTaxiDisplay();
                setupSelectedServices();
                calculateAndDisplayPrices();
            });
        }
    }

    private void setDefaultValues() {
        // ✅ SOLO USAR VALORES POR DEFECTO SI NO HAY DATOS EN LOS ARGUMENTOS
        if (checkInDate == null || checkInDate.isEmpty()) {
            checkInDate = "Fechas no seleccionadas";
        }
        if (checkOutDate == null || checkOutDate.isEmpty()) {
            checkOutDate = "";
        }
        // ✅ NO SOBRESCRIBIR numAdults y numChildren si ya se obtuvieron de los argumentos

        if (additionalServicesPrice == 0.0 && selectedServices.isEmpty()) {
            additionalServicesPrice = 0.0;
            selectedServices = "";
        }

        if (roomPrice == 0.0) {
            roomPrice = 0.0;
        }

        setupBasicReservationDataFromArguments(); // ✅ CORREGIDO: Usar el método correcto
        generateRandomRoomNumber();
    }

    private void setupTaxiDisplay() {
        if (isTaxiIncluded) {
            // ✅ TAXI SIEMPRE ES GRATUITO CUANDO ESTÁ INCLUIDO
            tvFreeTransport.setText("Sí");
            tvFreeTransport.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_green));
        } else {
            tvFreeTransport.setText("No");
            tvFreeTransport.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        }
    }

    private void setupSelectedServices() {
        if (selectedServices == null || selectedServices.isEmpty() || selectedServices.equals("[]")) {
            if (cardSelectedServices != null) {
                cardSelectedServices.setVisibility(View.GONE);
            }
            return;
        }

        if (cardSelectedServices != null) {
            cardSelectedServices.setVisibility(View.VISIBLE);
        }

        // ✅ MOSTRAR INFORMACIÓN DEL TAXI SI ESTÁ INCLUIDO
        if (isTaxiIncluded && layoutTaxiDetail != null) {
            layoutTaxiDetail.setVisibility(View.VISIBLE);

            if (tvTaxiServiceStatus != null) {
                tvTaxiServiceStatus.setText("🚖 Taxi Premium al Aeropuerto");
            }

            if (tvTaxiServiceDescription != null) {
                tvTaxiServiceDescription.setText("🎉 ¡INCLUIDO GRATIS! - Servicio cortesía del hotel");
                tvTaxiServiceDescription.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_green));
            }
        } else if (layoutTaxiDetail != null) {
            layoutTaxiDetail.setVisibility(View.GONE);
        }

        // ✅ MOSTRAR SERVICIOS INCLUIDOS EN LA HABITACIÓN
        showIncludedServicesInfo();
    }

    private void showIncludedServicesInfo() {
        if (selectedRoom != null && roomServicesInfo != null) {
            roomServicesInfo.setVisibility(View.VISIBLE);

            if (tvRoomServicesList != null) {
                List<String> serviceNames = new ArrayList<>();

                // ✅ OBTENER SERVICIOS INCLUIDOS DE LA HABITACIÓN
                List<String> includedServices = selectedRoom.getIncludedServiceIds();
                if (includedServices != null && !includedServices.isEmpty()) {
                    for (String serviceId : includedServices) {
                        serviceNames.add(getServiceDisplayName(serviceId));
                    }
                } else {
                    // ✅ SERVICIOS POR DEFECTO SI NO HAY DATOS
                    serviceNames.add("WiFi Premium");
                    serviceNames.add("Recepción 24/7");
                    serviceNames.add("Limpieza diaria");
                }

                tvRoomServicesList.setText("✓ " + String.join(" • ", serviceNames));
            }
        }
    }

    private String getServiceDisplayName(String serviceId) {
        switch (serviceId.toLowerCase()) {
            case "wifi": return "WiFi Premium";
            case "reception": return "Recepción 24/7";
            case "pool": return "Piscina";
            case "parking": return "Estacionamiento";
            case "minibar": return "Minibar";
            case "room_service": return "Room Service";
            case "laundry": return "Lavandería";
            case "gym": return "Gimnasio";
            case "spa": return "Spa";
            case "cleaning": return "Limpieza diaria";
            default: return "Servicio";
        }
    }

    private void calculateAndDisplayPrices() {
        // ✅ VERIFICAR Y CALCULAR PRECIO DE SERVICIOS ADICIONALES SI ES NECESARIO
        if (additionalServicesPrice == 0.0 && selectedServices != null && !selectedServices.isEmpty()) {
            additionalServicesPrice = calculateServicesPrice(selectedServices);
            Log.d(TAG, "💰 Precio de servicios recalculado: S/ " + additionalServicesPrice);
        }

        // ✅ CALCULAR TOTAL (HABITACIÓN + SERVICIOS ADICIONALES)
        // NOTA: El taxi es gratuito, NO se suma al precio
        totalPrice = roomPrice + additionalServicesPrice;

        Log.d(TAG, "💰 Cálculo de precios:");
        Log.d(TAG, "   - Habitación: S/ " + roomPrice);
        Log.d(TAG, "   - Servicios adicionales: S/ " + additionalServicesPrice);
        Log.d(TAG, "   - Taxi incluido: " + (isTaxiIncluded ? "GRATIS" : "NO"));
        Log.d(TAG, "   - TOTAL: S/ " + totalPrice);

        // ✅ ACTUALIZAR UI CON ANIMACIÓN
        animatePriceUpdate(tvRoomPriceValue, roomPrice);
        animatePriceUpdate(tvAdditionalServices, additionalServicesPrice);
        animatePriceUpdate(tvTotalPrice, totalPrice);
    }

    /**
     * ✅ CALCULAR PRECIO DE SERVICIOS ADICIONALES DESDE LA LISTA
     * Si viene el precio desde los argumentos, usarlo. Si no, calcularlo basado en los servicios.
     */
    private double calculateServicesPrice(String servicesString) {
        if (servicesString == null || servicesString.isEmpty() || servicesString.equals("[]")) {
            return 0.0;
        }

        // ✅ SI YA VIENE EL PRECIO DESDE LOS ARGUMENTOS, USARLO
        double priceFromArgs = getArguments() != null ?
                getArguments().getDouble("additional_services_price", 0.0) : 0.0;

        if (priceFromArgs > 0.0) {
            Log.d(TAG, "💰 Usando precio de servicios desde argumentos: S/ " + priceFromArgs);
            return priceFromArgs;
        }

        // ✅ SI NO HAY PRECIO, CALCULAR BASADO EN SERVICIOS
        // Quitar brackets y separar por comas
        String cleanServices = servicesString.replace("[", "").replace("]", "").trim();
        if (cleanServices.isEmpty()) {
            return 0.0;
        }

        String[] services = cleanServices.split(",");
        double totalPrice = 0.0;

        for (String service : services) {
            service = service.trim();
            if (!service.isEmpty() && !service.equalsIgnoreCase("taxi")) {
                // ✅ PRECIO ESTIMADO POR SERVICIO (puedes ajustar estos valores)
                totalPrice += 25.0; // S/ 25 por servicio adicional (excluyendo taxi)
                Log.d(TAG, "💰 Servicio agregado al cálculo: " + service + " (+S/ 25)");
            }
        }

        Log.d(TAG, "💰 Precio total calculado de servicios: S/ " + totalPrice);
        return totalPrice;
    }

    private void animatePriceUpdate(TextView textView, double price) {
        if (textView != null) {
            textView.setAlpha(0f);
            textView.setText(String.format(Locale.getDefault(), "S/ %.2f", price));
            textView.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setStartDelay(300)
                    .start();
        }
    }

    private void setupActions() {
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                animateExit(() -> requireActivity().onBackPressed());
            });
        }

        // Add payment method button
        if (btnAddPaymentMethod != null) {
            btnAddPaymentMethod.setOnClickListener(v -> showAddPaymentMethodDialog());
        }

        // Change card button
        if (btnChangeCard != null) {
            btnChangeCard.setOnClickListener(v -> showAddPaymentMethodDialog());
        }

        // Confirm reservation button
        if (btnConfirmReservation != null) {
            btnConfirmReservation.setOnClickListener(v -> {
                if (isPaymentMethodAdded) {
                    confirmBooking();
                } else {
                    showPaymentRequiredMessage();
                }
            });
        }

        // OK button in confirmation dialog
        if (btnOk != null) {
            btnOk.setOnClickListener(v -> {
                if (confirmationDialogOverlay != null) {
                    confirmationDialogOverlay.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .setInterpolator(new DecelerateInterpolator())
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    confirmationDialogOverlay.setVisibility(View.GONE);
                                    navigateToBookingDetails();
                                }
                            });
                }
            });
        }

        // Dialog overlay click handling
        if (confirmationDialogOverlay != null) {
            confirmationDialogOverlay.setOnClickListener(v -> {
                // Prevent closing dialog by clicking outside
            });
        }
    }

    private void animateCardEntrance() {
        // Animate hotel info card
        View hotelCard = requireView().findViewById(R.id.card_hotel_info);
        if (hotelCard != null) {
            hotelCard.setAlpha(0f);
            hotelCard.setTranslationY(50f);
            hotelCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setStartDelay(100)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // Animate other cards with staggered delay
        animateCardWithDelay(R.id.card_stay_info, 200);
        animateCardWithDelay(R.id.card_price_details, 300);
        animateCardWithDelay(R.id.card_payment_method, 400);
    }

    private void animateCardWithDelay(int cardId, long delay) {
        View card = requireView().findViewById(cardId);
        if (card != null) {
            card.setAlpha(0f);
            card.setTranslationY(50f);
            card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setStartDelay(delay)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    private void updateConfirmButtonState() {
        if (btnConfirmReservation != null) {
            btnConfirmReservation.setEnabled(isPaymentMethodAdded);
            btnConfirmReservation.setAlpha(isPaymentMethodAdded ? 1.0f : 0.6f);

            if (isPaymentMethodAdded) {
                btnConfirmReservation.setText("Confirmar Reserva");
                btnConfirmReservation.setIcon(null);
            } else {
                btnConfirmReservation.setText("Agregar método de pago");
                btnConfirmReservation.setIconResource(R.drawable.ic_payment);
            }
        }
    }

    private void showAddPaymentMethodDialog() {
        AddPaymentDialogFragment dialogFragment = AddPaymentDialogFragment.newInstance();
        dialogFragment.setPaymentDialogListener(this);
        dialogFragment.show(getChildFragmentManager(), "AddPaymentDialog");
    }

    private void showPaymentRequiredMessage() {
        Snackbar.make(requireView(), "Agrega un método de pago para continuar", Snackbar.LENGTH_LONG)
                .setAction("Agregar", v -> showAddPaymentMethodDialog())
                .setActionTextColor(getResources().getColor(R.color.orange_primary, null))
                .show();
    }

    @Override
    public void onPaymentMethodAdded(String cardNumber, String cardHolderName) {
        savedCardNumber = cardNumber;
        savedCardHolderName = cardHolderName;

        // Update UI with card information
        if (tvCardNumber != null) tvCardNumber.setText(cardNumber);
        if (tvCardName != null) tvCardName.setText(cardHolderName);

        // Animate the transition from button to card info
        animatePaymentMethodAdded();

        isPaymentMethodAdded = true;
        updateConfirmButtonState();

        // Show success message
        Snackbar.make(requireView(), "Tarjeta agregada exitosamente", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getResources().getColor(R.color.security_green, null))
                .show();
    }

    private void animatePaymentMethodAdded() {
        if (btnAddPaymentMethod != null && layoutCardInfo != null) {
            // Hide add button with animation
            btnAddPaymentMethod.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            btnAddPaymentMethod.setVisibility(View.GONE);

                            // Show card info with animation
                            layoutCardInfo.setVisibility(View.VISIBLE);
                            layoutCardInfo.setAlpha(0f);
                            layoutCardInfo.setScaleX(0.8f);
                            layoutCardInfo.setScaleY(0.8f);

                            layoutCardInfo.animate()
                                    .alpha(1f)
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(300)
                                    .setInterpolator(new DecelerateInterpolator())
                                    .start();
                        }
                    });
        }
    }

    private void confirmBooking() {
        // Add loading state to button
        if (btnConfirmReservation != null) {
            btnConfirmReservation.setEnabled(false);
            btnConfirmReservation.setText("Procesando...");
        }

        // Simulate processing delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showConfirmationDialog();
        }, 1500);
    }

    private void showConfirmationDialog() {
        // Configure success message
        TextView tvSuccessMessage = confirmationDialogOverlay.findViewById(R.id.tv_success_message);
        if (tvSuccessMessage != null && currentHotel != null) {
            tvSuccessMessage.setText(
                    "Tu reserva en " + currentHotel.getName() +
                            " ha sido procesada exitosamente. Recibirás un correo con todos los detalles."
            );
        }

        // Show overlay with animation
        if (confirmationDialogOverlay != null) {
            confirmationDialogOverlay.setVisibility(View.VISIBLE);
            confirmationDialogOverlay.setAlpha(0f);
            confirmationDialogOverlay.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            // Animate confirmation dialog
            MaterialCardView confirmationDialog = confirmationDialogOverlay.findViewById(R.id.confirmation_dialog);
            if (confirmationDialog != null) {
                confirmationDialog.setScaleX(0.7f);
                confirmationDialog.setScaleY(0.7f);
                confirmationDialog.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        }
    }

    private void animateExit(Runnable onComplete) {
        View rootView = requireView();
        rootView.animate()
                .alpha(0f)
                .translationY(-50f)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                });
    }

    private void navigateToBookingDetails() {
        Toast.makeText(requireContext(), "Reserva confirmada exitosamente", Toast.LENGTH_SHORT).show();
        animateExit(() -> requireActivity().onBackPressed());
    }
}