package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class IconSelectorDialog extends Dialog {

    private Context context;
    private OnIconSelectedListener listener;
    private TabLayout tabCategories;
    private RecyclerView rvIcons;
    private IconAdapter iconAdapter;
    private String selectedIconKey = "";

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
        setupTabs();
        setupRecyclerView();
        loadIcons("Básicos"); // Cargar primera categoría por defecto
    }

    private void initViews() {
        tabCategories = findViewById(R.id.tabCategories);
        rvIcons = findViewById(R.id.rvIcons);

        findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
        findViewById(R.id.btnSelect).setOnClickListener(v -> {
            if (!selectedIconKey.isEmpty() && listener != null) {
                listener.onIconSelected(selectedIconKey, IconHelper.getIconName(selectedIconKey));
            }
            dismiss();
        });
    }

    private void setupTabs() {
        List<String> categories = IconHelper.getCategories();

        for (String category : categories) {
            tabCategories.addTab(tabCategories.newTab().setText(category));
        }

        tabCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadIcons(tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        iconAdapter = new IconAdapter();
        rvIcons.setLayoutManager(new GridLayoutManager(context, 4));
        rvIcons.setAdapter(iconAdapter);
    }

    private void loadIcons(String category) {
        List<IconHelper.IconItem> icons = IconHelper.getIconsByCategory(category);
        iconAdapter.updateIcons(icons);
    }

    private class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {
        private List<IconHelper.IconItem> icons;

        public void updateIcons(List<IconHelper.IconItem> newIcons) {
            this.icons = newIcons;
            notifyDataSetChanged();
        }

        @Override
        public IconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.admin_hotel_item_icon_selector, parent, false);
            return new IconViewHolder(view);
        }

        @Override
        public void onBindViewHolder(IconViewHolder holder, int position) {
            IconHelper.IconItem icon = icons.get(position);
            holder.bind(icon);
        }

        @Override
        public int getItemCount() {
            return icons != null ? icons.size() : 0;
        }

        class IconViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivIcon;
            private TextView tvIconName;
            private View iconContainer;

            public IconViewHolder(View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.ivIcon);
                tvIconName = itemView.findViewById(R.id.tvIconName);
                iconContainer = itemView.findViewById(R.id.iconContainer);
            }

            public void bind(IconHelper.IconItem icon) {
                ivIcon.setImageResource(icon.getResourceId());
                tvIconName.setText(icon.getName());

                // Highlight si está seleccionado
                boolean isSelected = icon.getKey().equals(selectedIconKey);
                iconContainer.setBackgroundResource(isSelected ?
                        R.drawable.bg_service_card_selected : R.drawable.bg_service_card);

                iconContainer.setOnClickListener(v -> {
                    selectedIconKey = icon.getKey();
                    notifyDataSetChanged(); // Refresh para actualizar selección
                });
            }
        }
    }
}
