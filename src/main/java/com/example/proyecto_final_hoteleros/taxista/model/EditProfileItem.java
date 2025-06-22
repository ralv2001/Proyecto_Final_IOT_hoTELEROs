package com.example.proyecto_final_hoteleros.taxista.model;

public class EditProfileItem {

    public enum EditItemType {
        PROFILE_HEADER,      // Header con foto de perfil
        SECTION_HEADER,      // Encabezado de sección
        READ_ONLY_FIELD,     // Campo solo lectura
        EDITABLE_FIELD,      // Campo editable
        CAR_IMAGE           // Imagen del auto
    }

    private EditItemType type;
    private String title;
    private String value;
    private String key;         // Identificador único del campo
    private boolean isEditable;
    private int iconResId;
    private String hint;        // Texto de ayuda para campos editables

    public EditProfileItem(EditItemType type, String title, String value, String key,
                           boolean isEditable, int iconResId) {
        this.type = type;
        this.title = title;
        this.value = value;
        this.key = key;
        this.isEditable = isEditable;
        this.iconResId = iconResId;
        this.hint = "";
    }

    public EditProfileItem(EditItemType type, String title, String value, String key,
                           boolean isEditable, int iconResId, String hint) {
        this.type = type;
        this.title = title;
        this.value = value;
        this.key = key;
        this.isEditable = isEditable;
        this.iconResId = iconResId;
        this.hint = hint;
    }

    // Getters
    public EditItemType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getHint() {
        return hint;
    }

    // Setters
    public void setType(EditItemType type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    @Override
    public String toString() {
        return "EditProfileItem{" +
                "type=" + type +
                ", title='" + title + '\'' +
                ", value='" + value + '\'' +
                ", key='" + key + '\'' +
                ", isEditable=" + isEditable +
                ", iconResId=" + iconResId +
                ", hint='" + hint + '\'' +
                '}';
    }
}