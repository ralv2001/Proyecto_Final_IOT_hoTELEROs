package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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

public class CreditCardDialogFragment extends DialogFragment {

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
    private ImageView ivCardType;
    private MaterialButton btnCancel;
    private MaterialButton btnSave;

    private AnimatorSet frontAnimation;
    private AnimatorSet backAnimation;
    private boolean isCardFlipped = false;

    private CreditCardListener listener;

    public interface CreditCardListener {
        void onCardSaved(String cardNumber, String expiryDate, String cvv, String cardHolder);
        void onCancelled();
    }

    public static CreditCardDialogFragment newInstance() {
        return new CreditCardDialogFragment();
    }

    public void setListener(CreditCardListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, 0);
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

        // Initialize views
        cardFrontView = view.findViewById(R.id.card_front_view);
        cardBackView = view.findViewById(R.id.card_back_view);
        btnFlipCard = view.findViewById(R.id.btn_flip_card);
        etCardNumber = view.findViewById(R.id.et_card_number);
        etExpiryDate = view.findViewById(R.id.et_expiry_date);
        etCvv = view.findViewById(R.id.et_cvv);
        etCardHolder = view.findViewById(R.id.et_card_holder);
        tvCardNumberPreview = view.findViewById(R.id.tv_card_number_preview);
        tvExpiryDatePreview = view.findViewById(R.id.tv_expiry_date_preview);
        tvCvvPreview = view.findViewById(R.id.tv_cvv_preview);
        tvCardHolderPreview = view.findViewById(R.id.tv_card_holder_preview);
        ivCardType = view.findViewById(R.id.iv_card_type);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSave = view.findViewById(R.id.btn_save);

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

        // Set up buttons
        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelled();
            }
            dismiss();
        });

        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                if (listener != null) {
                    listener.onCardSaved(
                            etCardNumber.getText().toString(),
                            etExpiryDate.getText().toString(),
                            etCvv.getText().toString(),
                            etCardHolder.getText().toString()
                    );
                }
                dismiss();
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void setupCardFlipAnimations() {
        // Load flip animations
        frontAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(requireContext(), R.animator.card_flip_front);
        backAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(requireContext(), R.animator.card_flip_back);

        // Set animation target views
        frontAnimation.setTarget(cardFrontView);
        backAnimation.setTarget(cardBackView);
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

                // Update the card preview
                updateCardNumberPreview(formatted.toString());

                // Detect and update card type
                int cardType = CardUtils.getCardType(input);
                CardUtils.updateCardTypeIcon(cardType, ivCardType);

                // Change card color based on card type
                updateCardColor(cardType);

                // Add the text watcher back
                etCardNumber.addTextChangedListener(this);
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
                String input = s.toString().replaceAll("/", "");
                StringBuilder formatted = new StringBuilder();

                // Format the expiry date as MM/YY
                for (int i = 0; i < input.length(); i++) {
                    if (i == 2 && input.length() > 2) {
                        formatted.append("/");
                    }
                    formatted.append(input.charAt(i));
                }

                // Remove any existing text watchers to prevent infinite loop
                etExpiryDate.removeTextChangedListener(this);

                // Set the formatted text
                if (!s.toString().equals(formatted.toString()) && input.length() >= 2) {
                    etExpiryDate.setText(formatted);
                    etExpiryDate.setSelection(formatted.length());
                }

                // Update the card preview
                tvExpiryDatePreview.setText(s.length() > 0 ? s.toString() : "MM/YY");

                // Add the text watcher back
                etExpiryDate.addTextChangedListener(this);
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
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate card number
        String cardNumber = etCardNumber.getText().toString().replaceAll("\\s", "");
        if (cardNumber.isEmpty() || !CardUtils.isValidCardNumber(cardNumber)) {
            etCardNumber.setError("Número de tarjeta inválido");
            isValid = false;
        } else {
            etCardNumber.setError(null);
        }

        // Validate expiry date
        String expiryDate = etExpiryDate.getText().toString();
        if (expiryDate.isEmpty() || !CardUtils.isValidExpiryDate(expiryDate)) {
            etExpiryDate.setError("Fecha de expiración inválida");
            isValid = false;
        } else {
            etExpiryDate.setError(null);
        }

        // Validate CVV
        String cvv = etCvv.getText().toString();
        if (cvv.isEmpty() || cvv.length() < 3) {
            etCvv.setError("CVV inválido");
            isValid = false;
        } else {
            etCvv.setError(null);
        }

        // Validate card holder
        String cardHolder = etCardHolder.getText().toString();
        if (cardHolder.isEmpty()) {
            etCardHolder.setError("Nombre del titular requerido");
            isValid = false;
        } else {
            etCardHolder.setError(null);
        }

        return isValid;
    }
}