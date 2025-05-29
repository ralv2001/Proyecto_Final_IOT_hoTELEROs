package com.example.proyecto_final_hoteleros.client.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.model.RoomType;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class BookingSummaryFragment extends Fragment implements AddPaymentDialogFragment.PaymentDialogListener {

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_summary, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        retrieveArguments();
        setupData();
        setupActions();
        updateConfirmButtonState();

        // Add entrance animation
        animateCardEntrance();
    }

    private void initViews(View view) {
        // AppBar
        btnBack = view.findViewById(R.id.btn_back);

        // Hotel Info Card
        imgHotelBanner = view.findViewById(R.id.img_hotel_banner);
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        tvHotelAddress = view.findViewById(R.id.tv_hotel_address);
        tvRating = view.findViewById(R.id.tv_rating);

        // Stay Info Card
        tvCheckInOut = view.findViewById(R.id.tv_check_in_out);
        tvNumberOfGuests = view.findViewById(R.id.tv_number_of_guests);
        tvRoomType = view.findViewById(R.id.tv_room_type);
        tvRoomNumber = view.findViewById(R.id.tv_room_number);
        tvFreeTransport = view.findViewById(R.id.tv_free_transport);

        // Price Details Card
        tvRoomPriceValue = view.findViewById(R.id.tv_room_price_value);
        tvAdditionalServices = view.findViewById(R.id.tv_additional_services);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);

        // Payment Method Card
        btnAddPaymentMethod = view.findViewById(R.id.btn_add_payment_method);
        layoutCardInfo = view.findViewById(R.id.layout_card_info);
        tvCardNumber = view.findViewById(R.id.tv_card_number);
        tvCardName = view.findViewById(R.id.tv_card_name);
        btnChangeCard = view.findViewById(R.id.btn_change_card);
        tvPaymentInfo = view.findViewById(R.id.tv_payment_info);

        // Action Button
        btnConfirmReservation = view.findViewById(R.id.btn_confirm_reservation);

        // Confirmation Dialog Overlay
        confirmationDialogOverlay = view.findViewById(R.id.confirmation_dialog_overlay);
        btnOk = view.findViewById(R.id.btn_ok);
    }

    private void retrieveArguments() {
        if (getArguments() != null) {
            // Hotel info
            hotelName = getArguments().getString("hotel_name", "Belmond Miraflores Park");
            hotelAddress = getArguments().getString("hotel_address", "Miraflores, Lima, Perú");
            hotelRating = getArguments().getFloat("hotel_rating", 4.9f);
            hotelImageResource = getArguments().getInt("hotel_image", R.drawable.belmond);

            // Room info
            selectedRoom = getArguments().getParcelable("selected_room");

            // Stay info
            checkInDate = getArguments().getString("check_in_date", "8 abril");
            checkOutDate = getArguments().getString("check_out_date", "9 abril");
            numAdults = getArguments().getInt("num_adults", 2);
            numChildren = getArguments().getInt("num_children", 0);
            roomNumber = getArguments().getString("room_number", generateRandomRoomNumber());

            // Additional services
            hasFreeTransport = getArguments().getBoolean("has_free_transport", false);
            additionalServicesPrice = getArguments().getDouble("additional_services_price", 60.0);
        } else {
            // Default values
            setDefaultValues();
        }
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
        // Hotel info
        tvHotelName.setText(hotelName);
        tvHotelAddress.setText(hotelAddress);
        tvRating.setText(String.format(Locale.getDefault(), "%.1f", hotelRating));

        if (hotelImageResource != 0) {
            imgHotelBanner.setImageResource(hotelImageResource);
        }

        // Room info and price calculation
        if (selectedRoom != null) {
            tvRoomType.setText(selectedRoom.getName());
            try {
                String priceStr = selectedRoom.getPrice().replace("S/", "").trim();
                roomPrice = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                roomPrice = 290.0;
            }
        } else {
            tvRoomType.setText("Estándar");
            roomPrice = 290.0;
        }

        // Configure stay details
        tvCheckInOut.setText(String.format("%s - %s", checkInDate, checkOutDate));
        tvNumberOfGuests.setText(String.format("%d adultos - %d niños", numAdults, numChildren));
        tvRoomNumber.setText(roomNumber);
        tvFreeTransport.setText(hasFreeTransport ? "Sí" : "No");

        // Configure prices with animation
        animatePriceUpdate(tvRoomPriceValue, roomPrice);
        animatePriceUpdate(tvAdditionalServices, additionalServicesPrice);

        totalPrice = roomPrice + additionalServicesPrice;
        animatePriceUpdate(tvTotalPrice, totalPrice);
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