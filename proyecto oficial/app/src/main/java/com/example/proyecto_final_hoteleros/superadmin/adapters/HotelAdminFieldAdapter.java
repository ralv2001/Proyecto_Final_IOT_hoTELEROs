package com.example.proyecto_final_hoteleros.superadmin.adapters;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.models.HotelAdminField;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HotelAdminFieldAdapter extends RecyclerView.Adapter<HotelAdminFieldAdapter.FieldViewHolder> {

    private List<HotelAdminField> fieldsList;

    public HotelAdminFieldAdapter(List<HotelAdminField> fieldsList) {
        this.fieldsList = fieldsList;
    }

    @NonNull
    @Override
    public FieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hotel_admin_field, parent, false);
        return new FieldViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FieldViewHolder holder, int position) {
        HotelAdminField field = fieldsList.get(position);
        holder.bind(field);
    }

    @Override
    public int getItemCount() {
        return fieldsList.size();
    }

    // 🔥 MÉTODO CORREGIDO: Obtener datos del formulario
    public Map<String, String> getFormData() {
        Map<String, String> formData = new HashMap<>();

        // Actualizar valores desde los EditText antes de obtener los datos
        for (HotelAdminField field : fieldsList) {
            formData.put(field.getFieldId(), field.getValue());
        }

        return formData;
    }

    static class FieldViewHolder extends RecyclerView.ViewHolder {
        private TextInputLayout textInputLayout;
        private TextInputEditText editText;
        private TextView requiredIndicator;
        private HotelAdminField currentField;

        public FieldViewHolder(@NonNull View itemView) {
            super(itemView);
            textInputLayout = itemView.findViewById(R.id.text_input_layout);
            editText = itemView.findViewById(R.id.et_field_value);
            requiredIndicator = itemView.findViewById(R.id.tv_required_indicator);
        }

        public void bind(HotelAdminField field) {
            this.currentField = field;

            // Configurar el layout
            textInputLayout.setHint(field.getLabel());
            textInputLayout.setStartIconDrawable(field.getIconResId());

            // Configurar el EditText
            editText.setHint(field.getHint());
            editText.setText(field.getValue());

            // Configurar tipo de input
            setInputType(field.getInputType());

            // Mostrar indicador de requerido
            if (field.isRequired()) {
                requiredIndicator.setVisibility(View.VISIBLE);
            } else {
                requiredIndicator.setVisibility(View.GONE);
            }

            // 🔥 LISTENER ACTUALIZADO: Guardar valor en tiempo real
            editText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && currentField != null) {
                    currentField.setValue(editText.getText().toString());
                }
            });

            // 🔥 NUEVO: Listener para cambios de texto en tiempo real
            editText.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (currentField != null) {
                        currentField.setValue(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        private void setInputType(String inputType) {
            switch (inputType) {
                case "email":
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    break;
                case "phone":
                    editText.setInputType(InputType.TYPE_CLASS_PHONE);
                    break;
                case "password":
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    break;
                case "number":
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    break;
                default:
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    break;
            }
        }

        public String getValue() {
            return editText.getText().toString();
        }

        public String getFieldId() {
            return currentField != null ? currentField.getFieldId() : "";
        }
    }
}