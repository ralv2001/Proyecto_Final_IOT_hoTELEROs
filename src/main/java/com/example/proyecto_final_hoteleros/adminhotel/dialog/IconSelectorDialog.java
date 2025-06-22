package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class IconSelectorDialog extends Dialog {
    private static final String TAG = "IconSelectorDialog";

    private Context context;
    private OnIconSelectedListener listener;
    private IconAdapter iconAdapter;

    // UI Components
    private TextInputEditText etSearchLocal;
    private MaterialButton btnAddCustom, btnSelect, btnCancel;
    private ImageButton btnCloseDialog, btnClearSelection;
    private RecyclerView rvIcons;
    private LinearLayout selectedIconPreview;
    private TabLayout tabCategories;
    private ImageView ivSelectedIconPreview;
    private TextView tvSelectedIconPreviewName;

    // Data
    private String selectedIconKey = "";
    private String selectedIconName = "";
    private String selectedIconUrl = "";
    private boolean isCustomIcon = false;

    public interface OnIconSelectedListener {
        void onIconSelected(String iconKey, String iconName);
    }

    public IconSelectorDialog(Context context, OnIconSelectedListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        setupDialog();
    }

    public IconSelectorDialog(Context context, String currentIconKey, OnIconSelectedListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.selectedIconKey = currentIconKey;
        setupDialog();
    }

    private void setupDialog() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.admin_hotel_dialog_icon_selector);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        initViews();
        setupRecyclerView();
        setupTabs();
        setupClickListeners();
        setupSearch();
        loadLocalIcons("Básicos");
    }

    private void initViews() {
        // Search components (usando elementos que SÍ existen)
        etSearchLocal = findViewById(R.id.etSearchOnline); // Reutilizamos este campo
        btnAddCustom = findViewById(R.id.btnSearchOnline); // Reutilizamos este botón

        // Navigation
        btnCloseDialog = findViewById(R.id.btnCloseDialog);
        btnSelect = findViewById(R.id.btnSelect);
        btnCancel = findViewById(R.id.btnCancel);

        // Preview
        selectedIconPreview = findViewById(R.id.selectedIconPreview);
        ivSelectedIconPreview = findViewById(R.id.ivSelectedIconPreview);
        tvSelectedIconPreviewName = findViewById(R.id.tvSelectedIconPreviewName);
        btnClearSelection = findViewById(R.id.btnClearSelection);

        // Content
        rvIcons = findViewById(R.id.rvIcons);
        tabCategories = findViewById(R.id.tabCategories);

        // Cambiar texto del botón para la nueva funcionalidad
        btnAddCustom.setText("🔗 URL Personalizada");
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 4);
        rvIcons.setLayoutManager(gridLayoutManager);
        iconAdapter = new IconAdapter();
        rvIcons.setAdapter(iconAdapter);
    }

    private void setupTabs() {
        List<String> categories = IconHelper.getCategories();
        for (String category : categories) {
            tabCategories.addTab(tabCategories.newTab().setText(category));
        }

        tabCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String category = tab.getText().toString();
                loadLocalIcons(category);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupClickListeners() {
        btnCloseDialog.setOnClickListener(v -> dismiss());
        btnCancel.setOnClickListener(v -> dismiss());

        btnSelect.setOnClickListener(v -> {
            if (!selectedIconKey.isEmpty() && listener != null) {
                listener.onIconSelected(selectedIconKey, selectedIconName);
                dismiss();
            }
        });

        btnAddCustom.setOnClickListener(v -> showCustomUrlInput());
        btnClearSelection.setOnClickListener(v -> clearSelection());
    }

    private void setupSearch() {
        etSearchLocal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    searchLocalIcons(query);
                } else if (query.isEmpty()) {
                    loadLocalIcons("Básicos");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showCustomUrlInput() {
        // Crear un diálogo simple para ingresar URL
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("🔗 Agregar Icono Personalizado");

        final TextInputEditText input = new TextInputEditText(context);
        input.setHint("https://ejemplo.com/mi-icono.png");
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String url = input.getText().toString().trim();
            if (!url.isEmpty()) {
                addCustomIcon(url);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addCustomIcon(String url) {
        // Crear un icono personalizado
        String customKey = "custom_" + System.currentTimeMillis();
        IconHelper.IconItem customIcon = new IconHelper.IconItem(
                "Icono Personalizado",
                customKey,
                url,
                "Personalizado"
        );

        // Agregarlo temporalmente a la lista
        List<IconHelper.IconItem> currentIcons = new ArrayList<>();
        currentIcons.add(customIcon);
        currentIcons.addAll(IconHelper.getAllIcons());

        iconAdapter.updateIcons(currentIcons);

        // Seleccionarlo automáticamente
        selectedIconKey = customKey;
        selectedIconName = "Icono Personalizado";
        selectedIconUrl = url;
        isCustomIcon = true;

        showIconPreview(customKey, "Icono Personalizado", url, true);
        Toast.makeText(context, "✅ Icono personalizado agregado", Toast.LENGTH_SHORT).show();
    }

    private void searchLocalIcons(String query) {
        List<IconHelper.IconItem> icons = IconHelper.searchIcons(query);
        iconAdapter.updateIcons(icons);
        Toast.makeText(context, "📱 " + icons.size() + " iconos encontrados", Toast.LENGTH_SHORT).show();
    }

    private void loadLocalIcons(String category) {
        List<IconHelper.IconItem> icons = IconHelper.getIconsByCategory(category);
        iconAdapter.updateIcons(icons);
    }

    private void showIconPreview(String iconKey, String iconName, String imageUrl, boolean isCustom) {
        selectedIconKey = iconKey;
        selectedIconName = iconName;
        isCustomIcon = isCustom;

        selectedIconPreview.setVisibility(View.VISIBLE);
        tvSelectedIconPreviewName.setText(iconName);
        btnSelect.setEnabled(true);

        // Reducir altura del RecyclerView cuando aparece preview
        ViewGroup.LayoutParams params = rvIcons.getLayoutParams();
        params.height = dpToPx(220);
        rvIcons.setLayoutParams(params);

        if (isCustom && imageUrl != null) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_service_default)
                    .error(R.drawable.ic_service_default)
                    .into(ivSelectedIconPreview);
        } else {
            int iconResource = IconHelper.getIconResource(iconKey);
            ivSelectedIconPreview.setImageResource(iconResource);
        }
    }

    private void clearSelection() {
        selectedIconKey = "";
        selectedIconName = "";
        selectedIconUrl = "";
        isCustomIcon = false;
        selectedIconPreview.setVisibility(View.GONE);
        btnSelect.setEnabled(false);

        // Restaurar altura original del RecyclerView
        ViewGroup.LayoutParams params = rvIcons.getLayoutParams();
        params.height = dpToPx(280);
        rvIcons.setLayoutParams(params);

        iconAdapter.clearSelection();
    }

    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    // RecyclerView Adapter
    private class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {
        private List<IconHelper.IconItem> icons = new ArrayList<>();
        private int selectedPosition = -1;

        public void updateIcons(List<IconHelper.IconItem> newIcons) {
            this.icons = newIcons != null ? newIcons : new ArrayList<>();
            selectedPosition = -1;
            notifyDataSetChanged();
        }

        public void clearSelection() {
            int oldPosition = selectedPosition;
            selectedPosition = -1;
            if (oldPosition != -1) {
                notifyItemChanged(oldPosition);
            }
        }

        @Override
        public IconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.admin_hotel_item_icon_selector, parent, false);
            return new IconViewHolder(view);
        }

        @Override
        public void onBindViewHolder(IconViewHolder holder, int position) {
            if (position < icons.size()) {
                IconHelper.IconItem icon = icons.get(position);
                holder.bind(icon, position == selectedPosition);
            }
        }

        @Override
        public int getItemCount() {
            return icons.size();
        }

        class IconViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivIcon, ivSelectionIndicator;
            private TextView tvIconName;
            private View iconContainer, iconBackground;

            public IconViewHolder(View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.ivIcon);
                tvIconName = itemView.findViewById(R.id.tvIconName);
                iconContainer = itemView.findViewById(R.id.iconContainer);
                iconBackground = itemView.findViewById(R.id.iconBackground);
                ivSelectionIndicator = itemView.findViewById(R.id.ivSelectionIndicator);
            }

            public void bind(IconHelper.IconItem icon, boolean isSelected) {
                if (icon == null) return;

                tvIconName.setText(icon.getName());

                // Configurar selección visual
                if (isSelected) {
                    iconContainer.setBackgroundResource(R.drawable.bg_service_card_selected);
                    if (iconBackground != null) iconBackground.setAlpha(0.6f);
                    if (ivSelectionIndicator != null) {
                        ivSelectionIndicator.setVisibility(View.VISIBLE);
                    }
                } else {
                    iconContainer.setBackgroundResource(R.drawable.bg_service_card);
                    if (iconBackground != null) iconBackground.setAlpha(0.3f);
                    if (ivSelectionIndicator != null) {
                        ivSelectionIndicator.setVisibility(View.GONE);
                    }
                }

                // Cargar icono
                try {
                    if (icon.isCustomUrl() && icon.getImageUrl() != null) {
                        // Icono personalizado con URL
                        Log.d(TAG, "🔗 Cargando icono personalizado: " + icon.getImageUrl());
                        Glide.with(context)
                                .load(icon.getImageUrl())
                                .override(64, 64)
                                .placeholder(R.drawable.ic_service_default)
                                .error(R.drawable.ic_service_default)
                                .into(ivIcon);
                    } else {
                        // Icono local
                        int iconResource = IconHelper.getIconResource(icon.getKey());
                        ivIcon.setImageResource(iconResource);
                        ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.orange));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error cargando icono: " + e.getMessage());
                    ivIcon.setImageResource(R.drawable.ic_service_default);
                    ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.orange));
                }

                // Click listener
                iconContainer.setOnClickListener(v -> {
                    int oldPosition = selectedPosition;
                    selectedPosition = getAdapterPosition();

                    if (oldPosition != -1) {
                        notifyItemChanged(oldPosition);
                    }
                    notifyItemChanged(selectedPosition);

                    showIconPreview(
                            icon.getKey(),
                            icon.getName(),
                            icon.getImageUrl(),
                            icon.isCustomUrl()
                    );
                });
            }
        }
    }
}