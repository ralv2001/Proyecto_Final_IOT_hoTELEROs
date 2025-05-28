package com.example.proyecto_final_hoteleros.client.fragment;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
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
import com.example.proyecto_final_hoteleros.CardUtils;
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

    // Animation
    private AnimatorSet frontAnimation, backAnimation;
    private boolean isCardFlipped = false;

    // Guardar los valores de los campos
    private String savedCardNumber = "";
    private String savedExpiryDate = "";
    private String savedCvv = "";
    private String savedCardHolder = "";

    public AddPaymentDialogFragment() { /* Required empty constructor */ }

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
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupCardFlipAnimations();
        setupListeners();
        setupInputFormatting();
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
    }

    private void setupCardFlipAnimations() {
        // Load flip animations
        frontAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(requireContext(), R.animator.card_flip_front);
        backAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(requireContext(), R.animator.card_flip_back);

        // Garantizar que la distancia de la cámara sea suficiente para evitar deformaciones
        float scale = requireContext().getResources().getDisplayMetrics().density * 8000;
        cardFrontView.setCameraDistance(scale);
        cardBackView.setCameraDistance(scale);

        // Set animation target views
        frontAnimation.setTarget(cardFrontView);
        backAnimation.setTarget(cardBackView);
    }

    private void setupListeners() {
        // Button listeners
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> {
            if (validateFields()) {
                String rawNumber = Objects.requireNonNull(etCardNumber.getText()).toString().replaceAll("\\s", "");
                String masked = "**** **** **** " + rawNumber.substring(rawNumber.length() - 4);
                String nameUpper = Objects.requireNonNull(etCardHolder.getText()).toString().toUpperCase();
                if (listener != null) {
                    listener.onPaymentMethodAdded(masked, nameUpper);
                }
                dismiss();
            }
        });

        // Flip card button
        btnFlipCard.setOnClickListener(v -> flipCard());

        // Auto-flip to back when CVV field is focused
        etCvv.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !isCardFlipped) {
                // Guardar los valores antes de voltear
                saveFieldValues();
                flipCard();
            }
        });

        // Auto-flip to front when other fields are focused
        View.OnFocusChangeListener flipToFrontListener = (v, hasFocus) -> {
            if (hasFocus && isCardFlipped) {
                // Guardar los valores antes de voltear
                saveFieldValues();
                flipCard();
            }
        };

        etCardNumber.setOnFocusChangeListener(flipToFrontListener);
        etExpiryDate.setOnFocusChangeListener(flipToFrontListener);
        etCardHolder.setOnFocusChangeListener(flipToFrontListener);

        // Clear errors on text change
        TextWatcher errorClearingWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                clearErrors();
            }
        };

        etCardNumber.addTextChangedListener(errorClearingWatcher);
        etExpiryDate.addTextChangedListener(errorClearingWatcher);
        etCvv.addTextChangedListener(errorClearingWatcher);
        etCardHolder.addTextChangedListener(errorClearingWatcher);
    }

    // Nuevo método para guardar los valores de los campos
    private void saveFieldValues() {
        savedCardNumber = etCardNumber.getText() != null ? etCardNumber.getText().toString() : "";
        savedExpiryDate = etExpiryDate.getText() != null ? etExpiryDate.getText().toString() : "";
        savedCvv = etCvv.getText() != null ? etCvv.getText().toString() : "";
        savedCardHolder = etCardHolder.getText() != null ? etCardHolder.getText().toString() : "";
    }

    // Nuevo método para restaurar los valores guardados
    private void restoreFieldValues() {
        etCardNumber.setText(savedCardNumber);
        etExpiryDate.setText(savedExpiryDate);
        etCvv.setText(savedCvv);
        etCardHolder.setText(savedCardHolder);

        // Actualizar las vistas previas
        updateCardNumberPreview(savedCardNumber);
        tvExpiryDatePreview.setText(savedExpiryDate.isEmpty() ? "MM/YY" : savedExpiryDate);
        tvCvvPreview.setText(savedCvv.isEmpty() ? "CVV" : savedCvv);
        tvCardHolderPreview.setText(savedCardHolder.isEmpty() ? "NOMBRE DEL TITULAR" : savedCardHolder.toUpperCase());
    }

    private void setupInputFormatting() {
        // Card Number blocks of 4
        etCardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private String lastFormatted = "";

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String digits = s.toString().replaceAll("\\s", "");

                if (digits.equals(lastFormatted.replaceAll("\\s", ""))) {
                    isFormatting = false;
                    return;
                }

                lastFormatted = digits;

                if (digits.length() > 16) {
                    digits = digits.substring(0, 16);
                }

                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    if (i > 0 && i % 4 == 0) formatted.append(' ');
                    formatted.append(digits.charAt(i));
                }

                s.replace(0, s.length(), formatted.toString());

                // Update the card preview
                updateCardNumberPreview(formatted.toString());

                // Detect and update card type
                int cardType = CardUtils.getCardType(digits);
                CardUtils.updateCardTypeIcon(cardType, ivCardType);

                // Change card color based on card type
                updateCardColor(cardType);

                isFormatting = false;

                // Guardar los valores actualizados
                savedCardNumber = formatted.toString();
            }
        });

        // Expiry Date MM/YY
        etExpiryDate.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

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

                // Update the card preview
                tvExpiryDatePreview.setText(s.length() > 0 ? s.toString() : "MM/YY");

                isFormatting = false;

                // Guardar el valor actualizado
                savedExpiryDate = formatted.toString();
            }
        });

        // CVV Text Watcher
        etCvv.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override public void afterTextChanged(Editable s) {
                // Update the card preview
                tvCvvPreview.setText(s.length() > 0 ? s.toString() : "CVV");

                // Guardar el valor actualizado
                savedCvv = s.toString();
            }
        });

        // Card Holder Text Watcher
        etCardHolder.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override public void afterTextChanged(Editable s) {
                // Update the card preview
                tvCardHolderPreview.setText(s.length() > 0 ? s.toString().toUpperCase() : "NOMBRE DEL TITULAR");

                // Guardar el valor actualizado
                savedCardHolder = s.toString();
            }
        });
    }

    private void updateCardNumberPreview(String cardNumber) {
        StringBuilder preview = new StringBuilder();
        String maskedNumber = cardNumber.replaceAll("\\s", "");

        // Show the last 4 digits, mask the rest
        if (maskedNumber.length() > 4) {
            for (int i = 0; i < maskedNumber.length() - 4; i++) {
                preview.append("•");
            }
            preview.append(maskedNumber.substring(maskedNumber.length() - 4));
        } else {
            preview.append(maskedNumber);
        }

        // Add spaces for readability
        StringBuilder formattedPreview = new StringBuilder();
        for (int i = 0; i < preview.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formattedPreview.append(" ");
            }
            formattedPreview.append(preview.charAt(i));
        }

        // Fill with dots if empty or incomplete
        if (formattedPreview.length() == 0) {
            tvCardNumberPreview.setText("•••• •••• •••• ••••");
        } else {
            tvCardNumberPreview.setText(formattedPreview);
        }
    }

    private void updateCardColor(int cardType) {
        int frontColor;
        int backColor;

        switch (cardType) {
            case CardUtils.VISA:
                frontColor = getResources().getColor(R.color.card_visa_blue);
                backColor = frontColor;
                break;
            case CardUtils.MASTERCARD:
                frontColor = getResources().getColor(R.color.card_mastercard_red);
                backColor = frontColor;
                break;
            case CardUtils.AMEX:
                frontColor = getResources().getColor(R.color.card_amex_green);
                backColor = frontColor;
                break;
            case CardUtils.DISCOVER:
                frontColor = getResources().getColor(R.color.card_discover_orange);
                backColor = frontColor;
                break;
            default:
                frontColor = getResources().getColor(R.color.card_default_blue);
                backColor = frontColor;
                break;
        }

        cardFrontView.setCardBackgroundColor(frontColor);
        cardBackView.setCardBackgroundColor(backColor);
    }

    private void flipCard() {
        isCardFlipped = !isCardFlipped;

        if (isCardFlipped) {
            frontAnimation.start();
            cardBackView.setVisibility(View.VISIBLE);
            cardFrontView.setVisibility(View.GONE);
        } else {
            backAnimation.start();
            cardFrontView.setVisibility(View.VISIBLE);
            cardBackView.setVisibility(View.GONE);

            // Restaurar los valores al volver al frente
            restoreFieldValues();
        }
    }

    private boolean validateFields() {
        boolean valid = true;

        String num = Objects.requireNonNull(etCardNumber.getText()).toString().replaceAll("\\s", "");


        String exp = Objects.requireNonNull(etExpiryDate.getText()).toString();
        if (exp.length() != 5 || !exp.contains("/") || !CardUtils.isValidExpiryDate(exp)) {
            tilExpiryDate.setError("Formato MM/YY");
            valid = false;
        } else {
            String[] parts = exp.split("/");
            try {
                int month = Integer.parseInt(parts[0]);
                if (month < 1 || month > 12) {
                    tilExpiryDate.setError("Mes inválido");
                    valid = false;
                }
            } catch (Exception e) {
                tilExpiryDate.setError("Formato MM/YY");
                valid = false;
            }
        }

        String cvv = Objects.requireNonNull(etCvv.getText()).toString();
        if (cvv.length() < 3) {
            tilCvv.setError("CVV inválido");
            valid = false;
        }

        String holder = Objects.requireNonNull(etCardHolder.getText()).toString();
        if (holder.isEmpty()) {
            tilCardHolder.setError("Ingresa el nombre del titular");
            valid = false;
        }

        return valid;
    }

    private void clearErrors() {
        tilCardNumber.setError(null);
        tilExpiryDate.setError(null);
        tilCvv.setError(null);
        tilCardHolder.setError(null);
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