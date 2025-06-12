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

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.RoomType;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class BookingSummaryFragment extends Fragment implements AddPaymentDialogFragment.PaymentDialogListener {

    // UI Components
    private String selectedServices;
    private boolean isTaxiIncluded = false;
    private double taxiOriginalPrice = 60.0;
    private ImageButton btnBack;
    private ImageView imgHotelBanner;
    private TextView tvHotelName, tvHotelAddress, tvRating;
    private TextView tvCheckInOut, tvNumberOfGuests, tvRoomType, tvRoomNumber, tvFreeTransport;
    private TextView tvRoomPriceValue, tvAdditionalServices, tvTotalPrice;
    private MaterialButton btnConfirmReservation;
    private MaterialButton btnAddPaymentMethod;
    private MaterialButton btnChangeCard;
    private ConstraintLayout layoutCardInfo;
    private MaterialCardView cardInfoContainer;
    private TextView tvCardNumber, tvCardName, tvPaymentInfo;
    private ConstraintLayout confirmationDialogOverlay;
    private MaterialButton btnOk;

    // State variables
    private boolean isPaymentMethodAdded = false;
    private String savedCardNumber = "";
    private String savedCardHolderName = "";

    // Data
    private String hotelName;
    private String hotelAddress;
    private double hotelRating;
    private RoomType selectedRoom;
    private String checkInDate;
    private String checkOutDate;
    private int numAdults;
    private int numChildren;
    private String roomNumber;
    private boolean hasFreeTransport;
    private double roomPrice;
    private double additionalServicesPrice;
    private double totalPrice;
    private int hotelImageResource;
    // ✅ NUEVAS VISTAS para servicios seleccionados
    private CardView cardSelectedServices;
    private View layoutTaxiDetail;
    private TextView tvTaxiServiceStatus, tvTaxiServiceDescription;
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
        retrieveArguments();
        setupData();
        setupSelectedServices(); // ✅ AGREGAR esta línea
        setupActions();
        updateConfirmButtonState();
        animateCardEntrance();
    }

    private void initViews(View view) {
        // ✅ TUS VISTAS EXISTENTES se mantienen
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

        // ✅ AGREGAR nuevas vistas para servicios seleccionados
        cardSelectedServices = view.findViewById(R.id.card_selected_services);
        layoutTaxiDetail = view.findViewById(R.id.layout_taxi_detail);
        tvTaxiServiceStatus = view.findViewById(R.id.tv_taxi_service_status);
        tvTaxiServiceDescription = view.findViewById(R.id.tv_taxi_service_description);
    }

    private void retrieveArguments() {
        if (getArguments() != null) {
            // ✅ TUS ARGUMENTOS EXISTENTES se mantienen
            hotelName = getArguments().getString("hotel_name", "Belmond Miraflores Park");
            hotelAddress = getArguments().getString("hotel_address", "Miraflores, Lima, Perú");
            hotelRating = getArguments().getFloat("hotel_rating", 4.9f);
            hotelImageResource = getArguments().getInt("hotel_image", R.drawable.belmond);
            selectedRoom = getArguments().getParcelable("selected_room");
            checkInDate = getArguments().getString("check_in_date", "8 abril");
            checkOutDate = getArguments().getString("check_out_date", "9 abril");
            numAdults = getArguments().getInt("num_adults", 2);
            numChildren = getArguments().getInt("num_children", 0);
            roomNumber = getArguments().getString("room_number", generateRandomRoomNumber());
            hasFreeTransport = getArguments().getBoolean("has_free_transport", false);
            additionalServicesPrice = getArguments().getDouble("additional_services_price", 0.0);

            // ✅ NUEVO: Obtener servicios seleccionados
            selectedServices = getArguments().getString("selected_services", "");

            // ✅ CALCULAR si taxi está incluido
            calculateTaxiStatus();

        } else {
            setDefaultValues();
        }
    }
    private void calculateTaxiStatus() {
        if (selectedServices != null && selectedServices.contains("taxi")) {
            isTaxiIncluded = true;
            Log.d("BookingSummary", "Taxi está incluido en servicios seleccionados");
        } else {
            isTaxiIncluded = false;
            Log.d("BookingSummary", "Taxi NO está incluido");
        }
    }


    private double getRoomPriceValue() {
        if (selectedRoom != null) {
            try {
                String priceStr = selectedRoom.getPrice().replace("S/", "").trim();
                return Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {

            }
        }
        return 290.0;
    }


    /**
     * Genera un número de habitación aleatorio en formato "FXX",
     * donde F es el piso (1–9) y XX es el número de habitación (01–20).
     */
    private String generateRandomRoomNumber() {
        Random rnd = new Random();
        int floor = rnd.nextInt(9) + 1;         // pisos del 1 al 9
        int room = rnd.nextInt(20) + 1;         // habitaciones del 1 al 20
        return String.format(Locale.getDefault(), "%d%02d", floor, room);
    }

    private void setDefaultValues() {
        hotelName = "Belmond Miraflores Park";
        hotelAddress = "Miraflores, Lima, Perú";
        hotelRating = 4.9f;
        hotelImageResource = R.drawable.belmond;
        checkInDate = "8 abril";
        checkOutDate = "9 abril";
        numAdults = 2;
        numChildren = 0;
        roomNumber = generateRandomRoomNumber();
        hasFreeTransport = false;
        additionalServicesPrice = 60.0;
    }

    private void setupData() {
        // ✅ TU CÓDIGO EXISTENTE se mantiene
        tvHotelName.setText(hotelName);
        tvHotelAddress.setText(hotelAddress);
        tvRating.setText(String.format(Locale.getDefault(), "%.1f", hotelRating));

        if (hotelImageResource != 0) {
            imgHotelBanner.setImageResource(hotelImageResource);
        }

        // ✅ MEJORAR la configuración de precios
        if (selectedRoom != null) {
            tvRoomType.setText(selectedRoom.getName());
            roomPrice = getRoomPriceValue();
        } else {
            tvRoomType.setText("Estándar");
            roomPrice = 290.0;
        }

        // ✅ ACTUALIZAR información de estancia
        tvCheckInOut.setText(String.format("%s - %s", checkInDate, checkOutDate));
        tvNumberOfGuests.setText(String.format("%d adultos - %d niños", numAdults, numChildren));
        tvRoomNumber.setText(roomNumber);

        // ✅ MEJORAR la visualización del taxi
        setupTaxiDisplay();

        // ✅ ACTUALIZAR precios con lógica correcta
        animatePriceUpdate(tvRoomPriceValue, roomPrice);
        animatePriceUpdate(tvAdditionalServices, additionalServicesPrice);

        totalPrice = roomPrice + additionalServicesPrice;
        animatePriceUpdate(tvTotalPrice, totalPrice);
    }
    private void setupTaxiDisplay() {
        if (isTaxiIncluded) {
            if (hasFreeTransport) {
                tvFreeTransport.setText("Sí - ¡GRATIS!");
                tvFreeTransport.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_green));
                addTaxiSavingsInfo();
            } else {
                tvFreeTransport.setText("Sí - S/. 60.00");
                tvFreeTransport.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange_primary));
            }
        } else {
            tvFreeTransport.setText("No");
            tvFreeTransport.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        }
    }

    private void addTaxiSavingsInfo() {
        View taxiContainer = (View) requireView().findViewById(R.id.icon_taxi).getParent();
        if (taxiContainer instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) taxiContainer;

            TextView tvSavings = new TextView(requireContext());
            tvSavings.setText("(Ahorro: S/. 60.00)");
            tvSavings.setTextSize(12);
            tvSavings.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_green));
            tvSavings.setTypeface(null, Typeface.ITALIC);

            // Agregar debajo del status del taxi
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 4, 0, 0);

            if (parent instanceof LinearLayout) {
                ((LinearLayout) parent).addView(tvSavings, params);
            }
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

        // ✅ MOSTRAR información del taxi si está incluido
        if (isTaxiIncluded && layoutTaxiDetail != null) {
            layoutTaxiDetail.setVisibility(View.VISIBLE);

            if (tvTaxiServiceStatus != null) {
                tvTaxiServiceStatus.setText("🚖 Taxi Premium al Aeropuerto");
            }

            if (tvTaxiServiceDescription != null) {
                if (hasFreeTransport) {
                    tvTaxiServiceDescription.setText("🎉 ¡INCLUIDO GRATIS! Has alcanzado S/. 350+ (Ahorro: S/. 60)");
                    tvTaxiServiceDescription.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_green));
                } else {
                    tvTaxiServiceDescription.setText("💰 Servicio premium - S/. 60.00");
                    tvTaxiServiceDescription.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange_primary));
                }
            }
        }

        // ✅ AGREGAR información de servicios incluidos en habitación
        showIncludedServicesInfo();
    }
    private void showIncludedServicesInfo() {
        if (selectedRoom != null) {
            View roomServicesInfo = requireView().findViewById(R.id.room_services_info);
            if (roomServicesInfo != null) {
                roomServicesInfo.setVisibility(View.VISIBLE);

                TextView tvRoomServices = roomServicesInfo.findViewById(R.id.tv_room_services_list);
                if (tvRoomServices != null) {
                    List<String> serviceNames = new ArrayList<>();
                    for (String serviceId : selectedRoom.getIncludedServiceIds()) {
                        serviceNames.add(getServiceDisplayName(serviceId));
                    }
                    tvRoomServices.setText("✓ " + String.join(" • ", serviceNames));
                }
            }
        }
    }

    private String getServiceDisplayName(String serviceId) {
        switch (serviceId) {
            case "wifi": return "WiFi Premium";
            case "reception": return "Recepción 24/7";
            case "pool": return "Piscina Infinity";
            case "parking": return "Estacionamiento";
            case "minibar": return "Minibar Premium";
            case "room_service": return "Room Service";
            case "laundry": return "Lavandería Express";
            default: return "Servicio";
        }
    }

    private void setupOtherSelectedServices() {
        RecyclerView rvSelectedServices = requireView().findViewById(R.id.rv_selected_services);
        if (rvSelectedServices != null) {
            rvSelectedServices.setVisibility(View.GONE);
        }
    }


    private void setupActions() {
        // Back button
        btnBack.setOnClickListener(v -> {
            animateExit(() -> requireActivity().onBackPressed());
        });

        // Add payment method button
        btnAddPaymentMethod.setOnClickListener(v -> showAddPaymentMethodDialog());

        // Change card button
        btnChangeCard.setOnClickListener(v -> showAddPaymentMethodDialog());

        // Confirm reservation button
        btnConfirmReservation.setOnClickListener(v -> {
            if (isPaymentMethodAdded) {
                confirmBooking();
            } else {
                showPaymentRequiredMessage();
            }
        });

        // OK button in confirmation dialog
        btnOk.setOnClickListener(v -> {
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
        });

        // Dialog overlay click handling
        confirmationDialogOverlay.setOnClickListener(v -> {
            // Prevent closing dialog by clicking outside
        });

        MaterialCardView confirmationDialog = confirmationDialogOverlay.findViewById(R.id.confirmation_dialog);
        if (confirmationDialog != null) {
            confirmationDialog.setOnClickListener(v -> {
                // Consume event to prevent propagation
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

    private void animatePriceUpdate(TextView textView, double price) {
        textView.setAlpha(0f);
        textView.setText(String.format(Locale.getDefault(), "S/ %.2f", price));
        textView.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(300)
                .start();
    }

    private void updateConfirmButtonState() {
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
        tvCardNumber.setText(cardNumber);
        tvCardName.setText(cardHolderName);

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

    private void confirmBooking() {
        // Add loading state to button
        btnConfirmReservation.setEnabled(false);
        btnConfirmReservation.setText("Procesando...");

        // Simulate processing delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showConfirmationDialog();
        }, 1500);
    }

    private void showConfirmationDialog() {
        // Configure success message
        TextView tvSuccessMessage = confirmationDialogOverlay.findViewById(R.id.tv_success_message);
        if (tvSuccessMessage != null) {
            tvSuccessMessage.setText(
                    "¡Reserva Confirmada!\n\nTu reserva en " + hotelName +
                            " ha sido procesada exitosamente. Recibirás un correo con todos los detalles."
            );
        }

        // Show overlay with animation
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
        // TODO: Implement navigation to booking details
        Toast.makeText(requireContext(), "Navegando a detalles de reserva...", Toast.LENGTH_SHORT).show();

        // For now, go back to previous screen
        animateExit(() -> requireActivity().onBackPressed());
    }

}