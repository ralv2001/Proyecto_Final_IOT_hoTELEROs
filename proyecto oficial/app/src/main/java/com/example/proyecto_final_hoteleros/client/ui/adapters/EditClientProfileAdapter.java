package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.ClientEditProfileItem;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class EditClientProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PROFILE_HEADER = 0;
    private static final int TYPE_SECTION_HEADER = 1;
    private static final int TYPE_READ_ONLY_FIELD = 2;
    private static final int TYPE_EDITABLE_FIELD = 3;

    private Context context;
    private List<ClientEditProfileItem> items;
    private OnEditClientProfileListener listener;

    public interface OnEditClientProfileListener {
        void onFieldChanged(String fieldKey, String newValue);
        void onImageClick(String imageType);
    }

    public EditClientProfileAdapter(Context context, List<ClientEditProfileItem> items, OnEditClientProfileListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        ClientEditProfileItem item = items.get(position);
        switch (item.getType()) {
            case PROFILE_HEADER:
                return TYPE_PROFILE_HEADER;
            case SECTION_HEADER:
                return TYPE_SECTION_HEADER;
            case READ_ONLY_FIELD:
                return TYPE_READ_ONLY_FIELD;
            case EDITABLE_FIELD:
                return TYPE_EDITABLE_FIELD;
            default:
                return TYPE_READ_ONLY_FIELD;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case TYPE_PROFILE_HEADER:
                View headerView = inflater.inflate(R.layout.client_item_edit_profile_header, parent, false);
                return new ProfileHeaderViewHolder(headerView);
            case TYPE_SECTION_HEADER:
                View sectionView = inflater.inflate(R.layout.taxi_item_edit_section_header, parent, false);
                return new SectionHeaderViewHolder(sectionView);
            case TYPE_READ_ONLY_FIELD:
                View readOnlyView = inflater.inflate(R.layout.taxi_item_edit_readonly_field, parent, false);
                return new ReadOnlyFieldViewHolder(readOnlyView);
            case TYPE_EDITABLE_FIELD:
                View editableView = inflater.inflate(R.layout.taxi_item_edit_editable_field, parent, false);
                return new EditableFieldViewHolder(editableView);
            default:
                View defaultView = inflater.inflate(R.layout.taxi_item_edit_readonly_field, parent, false);
                return new ReadOnlyFieldViewHolder(defaultView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ClientEditProfileItem item = items.get(position);

        switch (holder.getItemViewType()) {
            case TYPE_PROFILE_HEADER:
                bindProfileHeader((ProfileHeaderViewHolder) holder, item);
                break;
            case TYPE_SECTION_HEADER:
                bindSectionHeader((SectionHeaderViewHolder) holder, item);
                break;
            case TYPE_READ_ONLY_FIELD:
                bindReadOnlyField((ReadOnlyFieldViewHolder) holder, item);
                break;
            case TYPE_EDITABLE_FIELD:
                bindEditableField((EditableFieldViewHolder) holder, item);
                break;
        }
    }

    private void bindProfileHeader(ProfileHeaderViewHolder holder, ClientEditProfileItem item) {
        // Cargar imagen de perfil
        Glide.with(context)
                .load(item.getValue())
                .placeholder(R.drawable.perfil)
                .circleCrop()
                .into(holder.ivProfileImage);

        // Click listener para cambiar foto
        holder.cardProfileImage.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick("profile_image");
            }
        });
    }

    private void bindSectionHeader(SectionHeaderViewHolder holder, ClientEditProfileItem item) {
        holder.tvSectionTitle.setText(item.getTitle());
    }

    private void bindReadOnlyField(ReadOnlyFieldViewHolder holder, ClientEditProfileItem item) {
        holder.tvFieldTitle.setText(item.getTitle());
        holder.tvFieldValue.setText(item.getValue());

        if (item.getIconResId() != 0) {
            holder.ivFieldIcon.setImageResource(item.getIconResId());
            holder.ivFieldIcon.setVisibility(View.VISIBLE);
        } else {
            holder.ivFieldIcon.setVisibility(View.GONE);
        }
    }

    private void bindEditableField(EditableFieldViewHolder holder, ClientEditProfileItem item) {
        holder.textInputLayout.setHint(item.getTitle());
        holder.etFieldValue.setText(item.getValue());

        if (item.getIconResId() != 0) {
            holder.textInputLayout.setStartIconDrawable(item.getIconResId());
        }

        // Limpiar listener anterior
        holder.etFieldValue.setTag(null);

        // TextWatcher para detectar cambios
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                item.setValue(s.toString());
                if (listener != null) {
                    listener.onFieldChanged(item.getKey(), s.toString());
                }
            }
        };

        holder.etFieldValue.addTextChangedListener(textWatcher);
        holder.etFieldValue.setTag(textWatcher);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolders
    static class ProfileHeaderViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardProfileImage;
        ImageView ivProfileImage;
        ImageView ivCameraIcon;

        ProfileHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardProfileImage = itemView.findViewById(R.id.card_profile_image);
            ivProfileImage = itemView.findViewById(R.id.iv_profile_image);
            ivCameraIcon = itemView.findViewById(R.id.iv_camera_icon);
        }
    }

    static class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvSectionTitle;

        SectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionTitle = itemView.findViewById(R.id.tv_section_title);
        }
    }

    static class ReadOnlyFieldViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFieldIcon;
        TextView tvFieldTitle;
        TextView tvFieldValue;

        ReadOnlyFieldViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFieldIcon = itemView.findViewById(R.id.iv_field_icon);
            tvFieldTitle = itemView.findViewById(R.id.tv_field_title);
            tvFieldValue = itemView.findViewById(R.id.tv_field_value);
        }
    }

    static class EditableFieldViewHolder extends RecyclerView.ViewHolder {
        TextInputLayout textInputLayout;
        TextInputEditText etFieldValue;

        EditableFieldViewHolder(@NonNull View itemView) {
            super(itemView);
            textInputLayout = itemView.findViewById(R.id.text_input_layout);
            etFieldValue = itemView.findViewById(R.id.et_field_value);
        }
    }
}