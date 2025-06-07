package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.Reservation;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.text.DecimalFormat;

public class ReservationDetailFragment extends Fragment {

    private ImageButton btnBack;
    private ImageView imgHotelBanner;
    private TextView tvHotelName, tvHotelAddress, tvRating;
    private TextView tvCheckInOut, tvNumberOfGuests, tvRoomType, tvRoomNumber;
    private TextView tvReservationId, tvReservationStatus, tvConfirmationCode;
    private MaterialButton btnPrimaryAction, btnSecondaryAction;

    // ✅ NUEVOS ELEMENTOS PROFESIONALES
    private MaterialCardView cardStatusIndicator, cardServicesBreakdown, cardPaymentInfo;
    private TextView tvStatusDescription, tvStatusIcon;
    private RecyclerView recyclerServices;
    private TextView tvTotalBreakdown, tvPaymentMethod;
    private View layoutModificationOptions, layoutCheckoutOptions, layoutReviewOptions;
    private MaterialButton btnModifyDates, btnModifyRoom, btnAddServices;
    private MaterialButton btnCheckout, btnViewBill;
    private MaterialButton btnSubmitReview, btnDownloadInvoice;

    private Reservation reservation;
    private boolean isViewMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.client_fragment_reservation_detail_professional, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        retrieveReservationData();
        setupProfessionalLayout();
        setupActions();
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btn_back);
        imgHotelBanner = view.findViewById(R.id.img_hotel_banner);
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        tvHotelAddress = view.findViewById(R.id.tv_hotel_address);
        tvRating = view.findViewById(R.id.tv_rating);
        tvCheckInOut = view.findViewById(R.id.tv_check_in_out);
        tvNumberOfGuests = view.findViewById(R.id.tv_number_of_guests);
        tvRoomType = view.findViewById(R.id.tv_room_info);
        tvRoomNumber = view.findViewById(R.id.tv_room_number);
        tvReservationId = view.findViewById(R.id.tv_reservation_id);
        tvReservationStatus = view.findViewById(R.id.tv_reservation_status);
        tvConfirmationCode = view.findViewById(R.id.tv_confirmation_code);
        btnPrimaryAction = view.findViewById(R.id.btn_primary_action);
        btnSecondaryAction = view.findViewById(R.id.btn_secondary_action);

        // ✅ NUEVOS ELEMENTOS
        cardStatusIndicator = view.findViewById(R.id.card_status_indicator);
        cardServicesBreakdown = view.findViewById(R.id.card_services_breakdown);
        cardPaymentInfo = view.findViewById(R.id.card_payment_info);
        tvStatusDescription = view.findViewById(R.id.tv_status_description);
        tvStatusIcon = view.findViewById(R.id.tv_status_icon);
        recyclerServices = view.findViewById(R.id.recycler_services);
        tvTotalBreakdown = view.findViewById(R.id.tv_total_breakdown);
        tvPaymentMethod = view.findViewById(R.id.tv_payment_method);

        // ✅ OPCIONES SEGÚN ESTADO
        layoutModificationOptions = view.findViewById(R.id.layout_modification_options);
        layoutCheckoutOptions = view.findViewById(R.id.layout_checkout_options);
        layoutReviewOptions = view.findViewById(R.id.layout_review_options);

        btnModifyDates = view.findViewById(R.id.btn_modify_dates);
        btnModifyRoom = view.findViewById(R.id.btn_modify_room);
        btnAddServices = view.findViewById(R.id.btn_add_services);
        btnCheckout = view.findViewById(R.id.btn_checkout);
        btnViewBill = view.findViewById(R.id.btn_view_bill);
        btnSubmitReview = view.findViewById(R.id.btn_submit_review);
        btnDownloadInvoice = view.findViewById(R.id.btn_download_invoice);
    }

    private void retrieveReservationData() {
        if (getArguments() != null) {
            reservation = getArguments().getParcelable("reservation");
            isViewMode = getArguments().getBoolean("view_mode", false);

            if (reservation == null) {
                createReservationFromArgs();
            }
        }
    }

    private void createReservationFromArgs() {
        if (getArguments() == null) return;

        String hotelName = getArguments().getString("hotel_name", "Hotel");
        String hotelAddress = getArguments().getString("hotel_address", "Ubicación");
        String hotelPrice = getArguments().getString("hotel_price", "0");
        float hotelRating = getArguments().getFloat("hotel_rating", 4.5f);
        int hotelImage = getArguments().getInt("hotel_image", R.drawable.belmond);

        reservation = new Reservation(hotelName, hotelAddress, "Fechas",
                Double.parseDouble(hotelPrice), hotelRating, hotelImage,
                Reservation.STATUS_UPCOMING);
    }

    private void setupProfessionalLayout() {
        if (reservation == null) return;

        // ✅ DATOS BÁSICOS
        tvHotelName.setText(reservation.getHotelName());
        tvHotelAddress.setText(reservation.getLocation());
        tvRating.setText(String.valueOf(reservation.getRating()));
        tvCheckInOut.setText(reservation.getDate());
        tvRoomType.setText(reservation.getRoomType());
        tvRoomNumber.setText("#" + reservation.getRoomNumber());
        tvReservationId.setText("ID: " + reservation.getReservationId());
        tvConfirmationCode.setText(reservation.getConfirmationCode());

        if (reservation.getImageResource() != 0) {
            imgHotelBanner.setImageResource(reservation.getImageResource());
        }

        // ✅ CONFIGURAR SEGÚN ESTADO
        setupStatusSpecificLayout();
        setupActionButtons();
        setupServicesBreakdown();
        setupPaymentInfo();
    }

    private void setupStatusSpecificLayout() {
        // ✅ INDICADOR DE ESTADO PROFESIONAL
        tvReservationStatus.setText(reservation.getStatusText());
        tvReservationStatus.setBackgroundColor(reservation.getStatusBackgroundColor());
        tvStatusDescription.setText(reservation.getContextualInfo());

        // ✅ ÍCONO SEGÚN ESTADO
        tvStatusIcon.setText(getStatusEmoji());
        tvStatusIcon.setVisibility(View.VISIBLE);

        // ✅ MOSTRAR OPCIONES SEGÚN ESTADO
        hideAllActionLayouts();

        switch (reservation.getStatus()) {
            case Reservation.STATUS_UPCOMING:
                if (reservation.canModify()) {
                    layoutModificationOptions.setVisibility(View.VISIBLE);
                }
                break;

            case Reservation.STATUS_ACTIVE:
                layoutCheckoutOptions.setVisibility(View.VISIBLE);
                if (reservation.isCheckoutPending()) {
                    btnViewBill.setVisibility(View.VISIBLE);
                    btnCheckout.setVisibility(View.GONE);
                } else if (reservation.canRequestCheckout()) {
                    btnCheckout.setVisibility(View.VISIBLE);
                    btnViewBill.setVisibility(View.GONE);
                }
                break;

            case Reservation.STATUS_COMPLETED:
                layoutReviewOptions.setVisibility(View.VISIBLE);
                if (reservation.isReviewSubmitted()) {
                    btnSubmitReview.setVisibility(View.GONE);
                    btnDownloadInvoice.setVisibility(View.VISIBLE);
                } else {
                    btnSubmitReview.setVisibility(View.VISIBLE);
                    btnDownloadInvoice.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void hideAllActionLayouts() {
        layoutModificationOptions.setVisibility(View.GONE);
        layoutCheckoutOptions.setVisibility(View.GONE);
        layoutReviewOptions.setVisibility(View.GONE);
    }

    private String getStatusEmoji() {
        switch (reservation.getStatus()) {
            case Reservation.STATUS_UPCOMING: return "📅";
            case Reservation.STATUS_ACTIVE:
                return reservation.isCheckoutPending() ? "⏳" : "🏨";
            case Reservation.STATUS_COMPLETED: return "✅";
            default: return "📋";
        }
    }

    private void setupActionButtons() {
        btnPrimaryAction.setText(reservation.getActionButtonText());

        switch (reservation.getStatus()) {
            case Reservation.STATUS_UPCOMING:
                btnSecondaryAction.setText("Contactar hotel");
                btnSecondaryAction.setVisibility(View.VISIBLE);
                break;
            case Reservation.STATUS_ACTIVE:
                btnSecondaryAction.setText("Servicios del hotel");
                btnSecondaryAction.setVisibility(View.VISIBLE);
                break;
            case Reservation.STATUS_COMPLETED:
                btnSecondaryAction.setText("Reservar de nuevo");
                btnSecondaryAction.setVisibility(View.VISIBLE);
                break;
            default:
                btnSecondaryAction.setVisibility(View.GONE);
                break;
        }
    }

    private void setupServicesBreakdown() {
        if (reservation.getServices().isEmpty() && reservation.getAdditionalChargesList().isEmpty()) {
            cardServicesBreakdown.setVisibility(View.GONE);
            return;
        }

        cardServicesBreakdown.setVisibility(View.VISIBLE);

        // ✅ DESGLOSE TOTAL
        DecimalFormat formatter = new DecimalFormat("0.00");
        StringBuilder breakdown = new StringBuilder();

        breakdown.append("💰 DESGLOSE DETALLADO:\n\n");
        breakdown.append("🏠 Habitación: S/").append(formatter.format(reservation.getBasePrice())).append("\n");

        if (reservation.getServicesTotal() > 0) {
            breakdown.append("🍽️ Servicios: S/").append(formatter.format(reservation.getServicesTotal())).append("\n");
        }

        if (reservation.getAdditionalCharges() > 0) {
            breakdown.append("⚠️ Cargos adicionales: S/").append(formatter.format(reservation.getAdditionalCharges())).append("\n");
        }

        breakdown.append("\n💳 TOTAL: S/").append(formatter.format(reservation.getFinalTotal()));

        if (reservation.isEligibleForFreeTaxi()) {
            breakdown.append("\n🚖 Incluye taxi gratuito");
        }

        tvTotalBreakdown.setText(breakdown.toString());
    }

    private void setupPaymentInfo() {
        if (reservation.getGuaranteeCard() != null) {
            cardPaymentInfo.setVisibility(View.VISIBLE);
            Reservation.PaymentMethod card = reservation.getGuaranteeCard();
            tvPaymentMethod.setText("💳 " + card.getCardType() + " ****" + card.getCardNumber() +
                    "\n👤 " + card.getHolderName());
        } else {
            cardPaymentInfo.setVisibility(View.GONE);
        }
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnPrimaryAction.setOnClickListener(v -> handlePrimaryAction());
        btnSecondaryAction.setOnClickListener(v -> handleSecondaryAction());

        // ✅ ACCIONES ESPECÍFICAS
        if (btnModifyDates != null) btnModifyDates.setOnClickListener(v -> showModifyDatesDialog());
        if (btnModifyRoom != null) btnModifyRoom.setOnClickListener(v -> showModifyRoomDialog());
        if (btnAddServices != null) btnAddServices.setOnClickListener(v -> navigateToServices());
        if (btnCheckout != null) btnCheckout.setOnClickListener(v -> showCheckoutDialog());
        if (btnViewBill != null) btnViewBill.setOnClickListener(v -> showPendingBill());
        if (btnSubmitReview != null) btnSubmitReview.setOnClickListener(v -> showReviewDialog());
        if (btnDownloadInvoice != null) btnDownloadInvoice.setOnClickListener(v -> downloadInvoice());
    }

    private void handlePrimaryAction() {
        switch (reservation.getStatus()) {
            case Reservation.STATUS_UPCOMING:
                if (reservation.canModify()) {
                    showModificationOptions();
                } else {
                    Toast.makeText(requireContext(), "Ver detalles completos", Toast.LENGTH_SHORT).show();
                }
                break;
            case Reservation.STATUS_ACTIVE:
                if (reservation.canRequestCheckout()) {
                    showCheckoutDialog();
                } else if (reservation.isCheckoutPending()) {
                    showPendingBill();
                }
                break;
            case Reservation.STATUS_COMPLETED:
                if (reservation.isReviewSubmitted()) {
                    downloadInvoice();
                } else {
                    showReviewDialog();
                }
                break;
        }
    }

    private void handleSecondaryAction() {
        switch (reservation.getStatus()) {
            case Reservation.STATUS_UPCOMING:
                Toast.makeText(requireContext(), "Contactando hotel...", Toast.LENGTH_SHORT).show();
                break;
            case Reservation.STATUS_ACTIVE:
                navigateToServices();
                break;
            case Reservation.STATUS_COMPLETED:
                Toast.makeText(requireContext(), "Reservar de nuevo - Próximamente", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // ✅ MÉTODOS DE DIÁLOGO Y ACCIONES

    private void showModificationOptions() {
        String[] options = {"Cambiar fechas", "Cambiar tipo de habitación", "Agregar servicios", "Cancelar reserva"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Modificar Reserva")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: showModifyDatesDialog(); break;
                        case 1: showModifyRoomDialog(); break;
                        case 2: navigateToServices(); break;
                        case 3: showCancelDialog(); break;
                    }
                })
                .show();
    }

    private void showModifyDatesDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_modify_dates, null);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Cambiar Fechas")
                .setView(dialogView)
                .setPositiveButton("Confirmar cambio", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Fechas actualizadas exitosamente", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showModifyRoomDialog() {
        String[] roomTypes = {"Estándar - S/650", "Suite Junior - S/850", "Suite Deluxe - S/1200", "Suite Premium - S/1500"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Cambiar Tipo de Habitación")
                .setSingleChoiceItems(roomTypes, 2, null)
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    String selectedRoom = roomTypes[which].split(" - ")[0];
                    Toast.makeText(requireContext(), "Cambiado a " + selectedRoom, Toast.LENGTH_SHORT).show();
                    reservation.setRoomType(selectedRoom);
                    tvRoomType.setText(selectedRoom);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showCheckoutDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_checkout_confirmation, null);

        TextView tvHotelNameDialog = dialogView.findViewById(R.id.tv_hotel_name_dialog);
        TextView tvTotalAmountDialog = dialogView.findViewById(R.id.tv_total_amount_dialog);
        TextView tvBreakdownDialog = dialogView.findViewById(R.id.tv_breakdown_dialog);

        tvHotelNameDialog.setText(reservation.getHotelName());
        tvTotalAmountDialog.setText("S/" + String.format("%.2f", reservation.getFinalTotal()));
        tvBreakdownDialog.setText(reservation.getDetailedBill());

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setView(dialogView)
                .setPositiveButton("Confirmar Checkout", (dialog, which) -> {
                    reservation.requestCheckout();
                    setupStatusSpecificLayout(); // Refrescar UI
                    Toast.makeText(requireContext(), "Checkout solicitado exitosamente", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showPendingBill() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pending_bill, null);

        TextView tvBillDetails = dialogView.findViewById(R.id.tv_bill_details);
        tvBillDetails.setText(reservation.getDetailedBill());

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setView(dialogView)
                .setPositiveButton("Entendido", null)
                .setNeutralButton("Contactar Hotel", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Contactando hotel...", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showReviewDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_review, null);

        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
        TextView tvHotelNameReview = dialogView.findViewById(R.id.tv_hotel_name_review);

        tvHotelNameReview.setText(reservation.getHotelName());

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setView(dialogView)
                .setPositiveButton("Enviar Review", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    reservation.setReviewSubmitted(true);
                    setupStatusSpecificLayout(); // Refrescar UI
                    Toast.makeText(requireContext(), "¡Gracias por tu valoración de " + rating + " estrellas!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Después", null)
                .show();
    }

    private void showCancelDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_cancel_reservation, null);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setView(dialogView)
                .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Reserva cancelada", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void navigateToServices() {
        Toast.makeText(requireContext(), "Navegando a servicios del hotel...", Toast.LENGTH_SHORT).show();
    }

    private void downloadInvoice() {
        Toast.makeText(requireContext(), "Descargando factura PDF...", Toast.LENGTH_SHORT).show();
    }
}