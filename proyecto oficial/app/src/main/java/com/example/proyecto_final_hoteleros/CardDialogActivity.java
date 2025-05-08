package com.example.proyecto_final_hoteleros;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CardDialogActivity extends AppCompatActivity {
    private int currentCardType = CardUtils.UNKNOWN;
    private boolean isCardValid = false;
    private boolean isCvvValid = false;
    private boolean isExpiryValid = false;
    private boolean isHolderValid = false;
    private MaterialCardView cardFrontView;
    private MaterialCardView cardBackView;
    private ImageButton btnFlipCard;
    private TextInputEditText etCardNumber;
    private TextInputEditText etExpiryDate;
    private TextInputEditText etCvv;
    private TextInputEditText etCardHolder;
    private TextView tvCardNumberPreview;
    private TextView tvExpiryDatePreview;
    private TextView tvCvvPreview;
    private TextView tvCardHolderPreview;

    private AnimatorSet frontAnimation;
    private AnimatorSet backAnimation;
    private boolean isCardFlipped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_payment);

        // Initialize views
        cardFrontView = findViewById(R.id.card_front_view);
        cardBackView = findViewById(R.id.card_back_view);
        btnFlipCard = findViewById(R.id.btn_flip_card);
        etCardNumber = findViewById(R.id.et_card_number);
        etExpiryDate = findViewById(R.id.et_expiry_date);
        etCvv = findViewById(R.id.et_cvv);
        etCardHolder = findViewById(R.id.et_card_holder);
        tvCardNumberPreview = findViewById(R.id.tv_card_number_preview);
        tvExpiryDatePreview = findViewById(R.id.tv_expiry_date_preview);
        tvCvvPreview = findViewById(R.id.tv_cvv_preview);
        tvCardHolderPreview = findViewById(R.id.tv_card_holder_preview);

        // Set up card flip animations
        setupCardFlipAnimations();

        // Set up text watchers for real-time preview
        setupTextWatchers();

        // Set up flip button
        btnFlipCard.setOnClickListener(v -> flipCard());

        // Auto-flip to back when CVV field is focused
        etCvv.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !isCardFlipped) {
                flipCard();
            }
        });

        // Auto-flip to front when other fields are focused
        View.OnFocusChangeListener flipToFrontListener = (v, hasFocus) -> {
            if (hasFocus && isCardFlipped) {
                flipCard();
            }
        };

        etCardNumber.setOnFocusChangeListener(flipToFrontListener);
        etExpiryDate.setOnFocusChangeListener(flipToFrontListener);
        etCardHolder.setOnFocusChangeListener(flipToFrontListener);
    }

    // Modifica setupCardFlipAnimations para mejorar la animación
    private void setupCardFlipAnimations() {
        frontAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(this,
                R.animator.card_flip_front);
        backAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(this,
                R.animator.card_flip_back);

        frontAnimation.setTarget(cardFrontView);
        backAnimation.setTarget(cardBackView);

        // Asegúrate de que las vistas estén correctamente posicionadas
        cardBackView.setCameraDistance(8000 * getResources().getDisplayMetrics().density);
        cardFrontView.setCameraDistance(8000 * getResources().getDisplayMetrics().density);
    }

    private void setupTextWatchers() {
        // Card Number Text Watcher
        etCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().replaceAll("\\s", "");
                StringBuilder formatted = new StringBuilder();

                // Format the card number with spaces every 4 digits
                for (int i = 0; i < input.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(input.charAt(i));
                }

                // Remove any existing text watchers to prevent infinite loop
                etCardNumber.removeTextChangedListener(this);

                // Set the formatted text
                if (!s.toString().equals(formatted.toString())) {
                    etCardNumber.setText(formatted);
                    etCardNumber.setSelection(formatted.length());
                }

                // Get card type and update icon
                currentCardType = CardUtils.getCardType(formatted.toString());
                ImageView cardTypeView = findViewById(R.id.iv_card_type);
                CardUtils.updateCardTypeIcon(currentCardType, cardTypeView);


                // Show error if card is invalid and has complete length
                TextInputLayout tilCardNumber = findViewById(R.id.til_card_number);
                if (input.length() == 16 && !isCardValid) {
                    tilCardNumber.setError("Número de tarjeta inválido");
                } else {
                    tilCardNumber.setError(null);
                }

                // Update the card preview
                updateCardNumberPreview(formatted.toString());

                // Add the text watcher back
                etCardNumber.addTextChangedListener(this);

                // Update save button state
                updateSaveButtonState();
            }
        });


        // Expiry Date Text Watcher
        etExpiryDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Update the card preview
                tvCvvPreview.setText(s.length() > 0 ? s.toString() : "CVV");

                // Validate CVV
                isCvvValid = CardUtils.isValidCvv(s.toString(), currentCardType);

                // Show error if CVV is invalid and has complete length
                TextInputLayout tilCvv = findViewById(R.id.til_cvv);
                if (s.length() == 3 && !isCvvValid) {
                    tilCvv.setError("CVV inválido");
                } else if (s.length() > 0 && s.length() < 3) {
                    tilCvv.setError("El CVV debe tener 3 dígitos");
                } else {
                    tilCvv.setError(null);
                }

                // Update save button state
                updateSaveButtonState();
            }
        });

        // CVV Text Watcher
        etCvv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Update the card preview
                tvCvvPreview.setText(s.length() > 0 ? s.toString() : "CVV");
            }
        });

        // Card Holder Text Watcher
        etCardHolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Update the card preview
                tvCardHolderPreview.setText(s.length() > 0 ? s.toString().toUpperCase() : "NOMBRE DEL TITULAR");
            }
        });
    }
    private void updateSaveButtonState() {
        MaterialButton btnSave = findViewById(R.id.btn_save);

        // Check if all fields are valid
        boolean isFormValid = isCardValid &&
                isCvvValid &&
                isExpiryValid &&
                isHolderValid &&
                currentCardType != CardUtils.UNKNOWN;

        btnSave.setEnabled(isFormValid);
        btnSave.setAlpha(isFormValid ? 1.0f : 0.5f);
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
        }
    }
}