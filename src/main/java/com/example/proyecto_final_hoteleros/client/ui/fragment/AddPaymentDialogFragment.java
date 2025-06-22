package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.proyecto_final_hoteleros.client.utils.CardUtils;
import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Objects;

public class AddPaymentDialogFragment extends DialogFragment {

    public interface PaymentDialogListener {
        void onPaymentMethodAdded(String cardNumber, String cardHolderName);
    }

    private PaymentDialogListener listener;

    // Card form fields
    private TextInputEditText etCardNumber, etExpiryDate, etCvv, etCardHolder;
    private TextInputLayout tilCardNumber, tilExpiryDate, tilCvv, tilCardHolder;

    // Card preview elements
    private MaterialCardView cardFrontView, cardBackView;
    private TextView tvCardNumberPreview, tvExpiryDatePreview, tvCvvPreview, tvCardHolderPreview;
    private ImageView ivCardType;
    private ImageButton btnFlipCard;

    // Buttons
    private MaterialButton btnSave, btnCancel;

    // Animation and state
    private boolean isCardFlipped = false;
    private boolean isAnimating = false;
    private int currentCardType = CardUtils.UNKNOWN;

    public AddPaymentDialogFragment() { }

    public static AddPaymentDialogFragment newInstance() {
        return new AddPaymentDialogFragment();
    }

    public void setPaymentDialogListener(PaymentDialogListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(DialogFragment.STYLE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_dialog_add_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupCardFlipAnimations();
        setupListeners();
        setupInputFormatting();
        updateSaveButtonState();
    }

    private void initViews(View view) {
        // Input fields
        etCardNumber = view.findViewById(R.id.et_card_number);
        etExpiryDate = view.findViewById(R.id.et_expiry_date);
        etCvv = view.findViewById(R.id.et_cvv);
        etCardHolder = view.findViewById(R.id.et_card_holder);

        // Input layouts
        tilCardNumber = view.findViewById(R.id.til_card_number);
        tilExpiryDate = view.findViewById(R.id.til_expiry_date);
        tilCvv = view.findViewById(R.id.til_cvv);
        tilCardHolder = view.findViewById(R.id.til_card_holder);

        // Card preview elements
        cardFrontView = view.findViewById(R.id.card_front_view);
        cardBackView = view.findViewById(R.id.card_back_view);
        tvCardNumberPreview = view.findViewById(R.id.tv_card_number_preview);
        tvExpiryDatePreview = view.findViewById(R.id.tv_expiry_date_preview);
        tvCvvPreview = view.findViewById(R.id.tv_cvv_preview);
        tvCardHolderPreview = view.findViewById(R.id.tv_card_holder_preview);
        ivCardType = view.findViewById(R.id.iv_card_type);
        btnFlipCard = view.findViewById(R.id.btn_flip_card);

        // Buttons
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);

        // Initial state - back card hidden
        cardBackView.setVisibility(View.GONE);
        cardBackView.setRotationY(180f);
    }

    private void setupCardFlipAnimations() {
        // Set camera distance for 3D effect
        float scale = requireContext().getResources().getDisplayMetrics().density * 8000;
        cardFrontView.setCameraDistance(scale);
        cardBackView.setCameraDistance(scale);
    }

    private void setupListeners() {
        // Button listeners
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> {
            if (validateFields()) {
                String rawNumber = Objects.requireNonNull(etCardNumber.getText()).toString().replaceAll("\\s", "");
                String masked = "**** **** **** " + rawNumber.substring(Math.max(0, rawNumber.length() - 4));
                String nameUpper = Objects.requireNonNull(etCardHolder.getText()).toString().toUpperCase();
                if (listener != null) {
                    listener.onPaymentMethodAdded(masked, nameUpper);
                }
                dismiss();
            }
        });

        // Flip card button - manual flip
        btnFlipCard.setOnClickListener(v -> {
            if (!isAnimating) {
                flipCard();
            }
        });

        // Auto-flip logic
        etCvv.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !isCardFlipped && !isAnimating) {
                flipCard();
            }
        });

        // Auto-flip to front when other fields are focused
        View.OnFocusChangeListener flipToFrontListener = (v, hasFocus) -> {
            if (hasFocus && isCardFlipped && !isAnimating) {
                flipCard();
            }
        };

        etCardNumber.setOnFocusChangeListener(flipToFrontListener);
        etExpiryDate.setOnFocusChangeListener(flipToFrontListener);
        etCardHolder.setOnFocusChangeListener(flipToFrontListener);
    }

    private void setupInputFormatting() {
        // Card Number formatting - SIMPLIFICADO
        etCardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String input = s.toString().replaceAll("\\s", "");

                // Limit to 16 digits
                if (input.length() > 16) {
                    input = input.substring(0, 16);
                }

                // Format with spaces every 4 digits
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < input.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(' ');
                    }
                    formatted.append(input.charAt(i));
                }

                // Update text
                s.replace(0, s.length(), formatted.toString());

                try {
                    etCardNumber.setSelection(formatted.length());
                } catch (Exception ignored) {}

                // Update card type and preview - SIMPLIFICADO
                if (input.length() > 0) {
                    if (input.startsWith("4")) {
                        currentCardType = CardUtils.VISA;
                    } else if (input.startsWith("5") || input.startsWith("2")) {
                        currentCardType = CardUtils.MASTERCARD;
                    } else {
                        currentCardType = CardUtils.UNKNOWN;
                    }
                }

                updateCardTypeAndColor(currentCardType);
                updateCardNumberPreview(formatted.toString());

                isFormatting = false;
                clearErrors();
                updateSaveButtonState();
            }
        });

        // Expiry Date formatting - SIMPLIFICADO
        etExpiryDate.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String input = s.toString().replaceAll("[^\\d]", "");

                if (input.length() > 4) {
                    input = input.substring(0, 4);
                }

                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < input.length(); i++) {
                    if (i == 2) formatted.append('/');
                    formatted.append(input.charAt(i));
                }

                s.replace(0, s.length(), formatted.toString());

                try {
                    etExpiryDate.setSelection(formatted.length());
                } catch (Exception ignored) {}

                tvExpiryDatePreview.setText(formatted.length() > 0 ? formatted.toString() : "MM/YY");

                isFormatting = false;
                clearErrors();
                updateSaveButtonState();
            }
        });

        // CVV formatting - SIMPLIFICADO
        etCvv.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override public void afterTextChanged(Editable s) {
                if (s.length() > 3) {
                    s.delete(3, s.length());
                }
                tvCvvPreview.setText(s.length() > 0 ? s.toString() : "CVV");
                clearErrors();
                updateSaveButtonState();
            }
        });

        // Card Holder formatting - SIMPLIFICADO
        etCardHolder.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override public void afterTextChanged(Editable s) {
                tvCardHolderPreview.setText(s.length() > 0 ? s.toString().toUpperCase() : "NOMBRE DEL TITULAR");
                clearErrors();
                updateSaveButtonState();
            }
        });
    }

    private void updateCardTypeAndColor(int cardType) {
        // Update card type icon
        CardUtils.updateCardTypeIcon(cardType, ivCardType);

        // Update card colors with smooth transition
        int[] colors = CardUtils.getCardColors(cardType);
        int frontColor = colors[0];
        int backColor = colors[1];

        // Animate color change
        ObjectAnimator frontColorAnim = ObjectAnimator.ofArgb(cardFrontView, "cardBackgroundColor",
                cardFrontView.getCardBackgroundColor().getDefaultColor(), frontColor);
        ObjectAnimator backColorAnim = ObjectAnimator.ofArgb(cardBackView, "cardBackgroundColor",
                cardBackView.getCardBackgroundColor().getDefaultColor(), backColor);

        frontColorAnim.setDuration(300);
        backColorAnim.setDuration(300);
        frontColorAnim.start();
        backColorAnim.start();
    }

    private void updateCardNumberPreview(String cardNumber) {
        String digits = cardNumber.replaceAll("\\s", "");
        StringBuilder preview = new StringBuilder();

        if (digits.length() == 0) {
            tvCardNumberPreview.setText("•••• •••• •••• ••••");
            return;
        }

        // Show all digits as entered
        preview.append(digits);
        while (preview.length() < 16) {
            preview.append("•");
        }

        // Format with spaces
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < preview.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(preview.charAt(i));
        }

        tvCardNumberPreview.setText(formatted.toString());
    }

    private void flipCard() {
        if (isAnimating) return;

        isAnimating = true;
        isCardFlipped = !isCardFlipped;

        float rotationY = isCardFlipped ? 180f : 0f;
        float oppositeRotation = isCardFlipped ? 0f : 180f;

        MaterialCardView currentCard = isCardFlipped ? cardFrontView : cardBackView;
        MaterialCardView nextCard = isCardFlipped ? cardBackView : cardFrontView;

        ObjectAnimator hideAnim = ObjectAnimator.ofFloat(currentCard, "rotationY", oppositeRotation);
        hideAnim.setDuration(300);

        ObjectAnimator showAnim = ObjectAnimator.ofFloat(nextCard, "rotationY", rotationY);
        showAnim.setDuration(300);
        showAnim.setStartDelay(150);

        hideAnim.start();

        hideAnim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                currentCard.setVisibility(View.GONE);
                nextCard.setVisibility(View.VISIBLE);
                showAnim.start();
            }
        });

        showAnim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isAnimating = false;
            }
        });
    }

    // VALIDACIÓN SIMPLIFICADA - ESTA ES LA CLAVE
    private boolean validateFields() {
        String cardNumber = Objects.requireNonNull(etCardNumber.getText()).toString().replaceAll("\\s", "");
        String expiryDate = Objects.requireNonNull(etExpiryDate.getText()).toString();
        String cvv = Objects.requireNonNull(etCvv.getText()).toString();
        String holder = Objects.requireNonNull(etCardHolder.getText()).toString().trim();

        // VALIDACIONES MUY BÁSICAS - solo verificar que no estén completamente vacíos
        if (cardNumber.length() < 10) {
            tilCardNumber.setError("Número de tarjeta muy corto");
            return false;
        }

        if (expiryDate.length() < 3) {
            tilExpiryDate.setError("Fecha requerida");
            return false;
        }

        if (cvv.length() < 2) {
            tilCvv.setError("CVV requerido");
            return false;
        }

        if (holder.length() < 1) {
            tilCardHolder.setError("Nombre requerido");
            return false;
        }

        // Si llegamos aquí, todo está bien
        clearErrors();
        return true;
    }

    private void clearErrors() {
        tilCardNumber.setError(null);
        tilExpiryDate.setError(null);
        tilCvv.setError(null);
        tilCardHolder.setError(null);
    }

    // MÉTODO DE VALIDACIÓN SIMPLIFICADO - ESTA ES LA SOLUCIÓN
    private void updateSaveButtonState() {
        // FORZAR BOTÓN SIEMPRE HABILITADO
        btnSave.setEnabled(true);
        btnSave.setAlpha(1.0f);

        android.util.Log.d("PaymentDialog", "Botón FORZADO a estar habilitado");
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}