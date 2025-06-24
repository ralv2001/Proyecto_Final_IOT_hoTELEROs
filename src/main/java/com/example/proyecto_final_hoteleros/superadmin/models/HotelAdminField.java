package com.example.proyecto_final_hoteleros.superadmin.models;

public class HotelAdminField {
    private String fieldId;
    private String label;
    private String hint;
    private int iconResId;
    private String inputType;
    private boolean isRequired;
    private String value;

    public HotelAdminField(String fieldId, String label, String hint, int iconResId,
                           String inputType, boolean isRequired) {
        this.fieldId = fieldId;
        this.label = label;
        this.hint = hint;
        this.iconResId = iconResId;
        this.inputType = inputType;
        this.isRequired = isRequired;
        this.value = "";
    }

    // Getters y Setters
    public String getFieldId() { return fieldId; }
    public void setFieldId(String fieldId) { this.fieldId = fieldId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }

    public String getInputType() { return inputType; }
    public void setInputType(String inputType) { this.inputType = inputType; }

    public boolean isRequired() { return isRequired; }
    public void setRequired(boolean required) { isRequired = required; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}