// AddPaymentDialogFragment.java - VERSIÓN SIMPLIFICADA PARA PRUEBAS
package com.example.proyecto_final_hoteleros.client.ui.fragment;
import com.example.proyecto_final_hoteleros.client.utils.PaymentMethodManager;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
        default void onPaymentMethodAdded(String cardNumber, String cardHolderName, String expiryDate, String cardType) {
            onPaymentMethodAdded(cardNumber, cardHolderName);
        }
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

        // ✅ HABILITAR BOTÓN INMEDIATAMENTE PARA PRUEBAS
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
        if (cardBackView != null) {
            cardBackView.setVisibility(View.GONE);
            cardBackView.setRotationY(180f);
        }
    }

    private void setupCardFlipAnimations() {
        if (cardFrontView != null && cardBackView != null) {
            float scale = requireContext().getResources().getDisplayMetrics().density * 8000;
            cardFrontView.setCameraDistance(scale);
            cardBackView.setCameraDistance(scale);
        }
    }

    private void setupListeners() {
        // Button listeners
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dismiss());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                // ✅ VALIDACIÓN SÚPER SIMPLE
                if (validateFieldsSimple()) {
                    saveCard();
                }
            });
        }

        // Flip card button
        if (btnFlipCard != null) {
            btnFlipCard.setOnClickListener(v -> {
                if (!isAnimating) {
                    flipCard();
                }
            });
        }

        // Auto-flip logic simplificado
        if (etCvv != null) {
            etCvv.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && !isCardFlipped && !isAnimating) {
                    flipCard();
                }
            });
        }

        // Auto-flip to front when other fields are focused
        View.OnFocusChangeListener flipToFrontListener = (v, hasFocus) -> {
            if (hasFocus && isCardFlipped && !isAnimating) {
                flipCard();
            }
        };

        if (etCardNumber != null) etCardNumber.setOnFocusChangeListener(flipToFrontListener);
        if (etExpiryDate != null) etExpiryDate.setOnFocusChangeListener(flipToFrontListener);
        if (etCardHolder != null) etCardHolder.setOnFocusChangeListener(flipToFrontListener);
    }

    private void setupInputFormatting() {
        // ✅ FORMATEO BÁSICO PARA NÚMERO DE TARJETA
        if (etCardNumber != null) {
            etCardNumber.addTextChangedListener(new TextWatcher() {
                private boolean isFormatting = false;

                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override public void afterTextChanged(Editable s) {
                    if (isFormatting) return;
                    isFormatting = true;

                    String input = s.toString().replaceAll("\\s", "");
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

                    s.replace(0, s.length(), formatted.toString());

                    try {
                        etCardNumber.setSelection(formatted.length());
                    } catch (Exception ignored) {}

                    // Update preview
                    updateCardNumberPreview(formatted.toString());

                    isFormatting = false;
                    updateSaveButtonState(); // ✅ ACTUALIZAR BOTÓN
                }
            });
        }

        // ✅ FORMATEO BÁSICO PARA FECHA
        if (etExpiryDate != null) {
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

                    if (tvExpiryDatePreview != null) {
                        tvExpiryDatePreview.setText(formatted.length() > 0 ? formatted.toString() : "MM/YY");
                    }

                    isFormatting = false;
                    updateSaveButtonState(); // ✅ ACTUALIZAR BOTÓN
                }
            });
        }

        // ✅ FORMATEO BÁSICO PARA CVV
        if (etCvv != null) {
            etCvv.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override public void afterTextChanged(Editable s) {
                    if (s.length() > 3) {
                        s.delete(3, s.length());
                    }
                    if (tvCvvPreview != null) {
                        tvCvvPreview.setText(s.length() > 0 ? s.toString() : "CVV");
                    }
                    updateSaveButtonState(); // ✅ ACTUALIZAR BOTÓN
                }
            });
        }

        // ✅ FORMATEO BÁSICO PARA TITULAR
        if (etCardHolder != null) {
            etCardHolder.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override public void afterTextChanged(Editable s) {
                    if (tvCardHolderPreview != null) {
                        tvCardHolderPreview.setText(s.length() > 0 ? s.toString().toUpperCase() : "NOMBRE DEL TITULAR");
                    }
                    updateSaveButtonState(); // ✅ ACTUALIZAR BOTÓN
                }
            });
        }
    }

    private void updateCardNumberPreview(String cardNumber) {
        if (tvCardNumberPreview == null) return;

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
        if (isAnimating || cardFrontView == null || cardBackView == null) return;

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

    // ✅ VALIDACIÓN SÚPER SIMPLE - SOLO VERIFICAR QUE HAYA ALGO ESCRITO
    private boolean validateFieldsSimple() {
        String cardNumber = etCardNumber != null ? Objects.requireNonNull(etCardNumber.getText()).toString().trim() : "";
        String holder = etCardHolder != null ? Objects.requireNonNull(etCardHolder.getText()).toString().trim() : "";

        // ✅ SOLO VERIFICAR QUE NO ESTÉN VACÍOS
        if (cardNumber.isEmpty()) {
            Log.d("PaymentDialog", "Número de tarjeta vacío");
            return false;
        }

        if (holder.isEmpty()) {
            Log.d("PaymentDialog", "Nombre del titular vacío");
            return false;
        }

        Log.d("PaymentDialog", "✅ Validación simple APROBADA");
        return true;
    }

    // ✅ GUARDAR TARJETA CON DATOS BÁSICOS
    private void saveCard() {
        try {
            String rawNumber = Objects.requireNonNull(etCardNumber.getText()).toString().replaceAll("\\s", "");
            String expiryDate = Objects.requireNonNull(etExpiryDate.getText()).toString();
            String nameUpper = Objects.requireNonNull(etCardHolder.getText()).toString().toUpperCase();

            // ✅ SI NO HAY FECHA, USAR POR DEFECTO
            if (expiryDate.isEmpty()) {
                expiryDate = "12/28";
            }

            // ✅ DETERMINAR TIPO DE TARJETA
            String cardType = PaymentMethodManager.determineCardType(rawNumber);

            // ✅ ENMASCARAR NÚMERO DE TARJETA
            String maskedNumber = PaymentMethodManager.maskCardNumber(rawNumber);

            Log.d("PaymentDialog", "💳 Guardando tarjeta:");
            Log.d("PaymentDialog", "   Número: " + maskedNumber);
            Log.d("PaymentDialog", "   Titular: " + nameUpper);
            Log.d("PaymentDialog", "   Tipo: " + cardType);
            Log.d("PaymentDialog", "   Vencimiento: " + expiryDate);

            if (listener != null) {
                try {
                    listener.onPaymentMethodAdded(maskedNumber, nameUpper, expiryDate, cardType);
                } catch (Exception e) {
                    // Fallback al método original
                    listener.onPaymentMethodAdded(maskedNumber, nameUpper);
                }
            }
            dismiss();

        } catch (Exception e) {
            Log.e("PaymentDialog", "Error guardando tarjeta: " + e.getMessage());
        }
    }

    // ✅ MÉTODO SIMPLIFICADO PARA HABILITAR BOTÓN
    private void updateSaveButtonState() {
        if (btnSave == null) return;

        try {
            String cardNumber = etCardNumber != null ? Objects.requireNonNull(etCardNumber.getText()).toString().trim() : "";
            String holder = etCardHolder != null ? Objects.requireNonNull(etCardHolder.getText()).toString().trim() : "";

            // ✅ HABILITAR SI HAY ALGO EN NÚMERO Y TITULAR
            boolean hasBasicInfo = !cardNumber.isEmpty() && !holder.isEmpty();

            btnSave.setEnabled(hasBasicInfo);
            btnSave.setAlpha(hasBasicInfo ? 1.0f : 0.6f);

            Log.d("PaymentDialog", "🔄 Estado del botón: " + (hasBasicInfo ? "HABILITADO" : "DESHABILITADO"));

        } catch (Exception e) {
            Log.e("PaymentDialog", "Error actualizando botón: " + e.getMessage());
            // ✅ EN CASO DE ERROR, HABILITAR EL BOTÓN DE TODAS FORMAS
            btnSave.setEnabled(true);
            btnSave.setAlpha(1.0f);
        }
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

        Log.d("PaymentDialog", "✅ Diálogo iniciado - Verificando botón...");

        // ✅ FORZAR ACTUALIZACIÓN DEL BOTÓN AL INICIAR
        updateSaveButtonState();
    }
}