package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.IconSelectorAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.utils.IconHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import java.util.List;

public class IconSelectorDialog extends AppCompatDialog {

    public interface OnIconSelectedListener {
        void onIconSelected(String iconKey, String iconName);
    }

    private Context context;
    private TabLayout tabCategories;
    private SearchView searchView;
    private RecyclerView rvIcons;
    private MaterialButton btnCancel, btnSelect;

    private IconSelectorAdapter adapter;
    private OnIconSelectedListener listener;
    private String selectedIconKey;
    private List<IconHelper.IconItem> currentIcons;

    public IconSelectorDialog(Context context, String currentIconKey, OnIconSelectedListener listener) {
        super(context, R.style.DialogTheme);
        this.context = context;
        this.selectedIconKey = currentIconKey;
        this.listener = listener;
        setupDialog();
    }

    private void setupDialog() {
        setContentView(R.layout.admin_hotel_dialog_icon_selector);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (context.getResources().getDisplayMetrics().heightPixels * 0.8));
        }

        initViews();
        setupRecyclerView();
        setupTabs();
        setupClickListeners();
        loadIcons("Conectividad"); // Categoría por defecto
    }

    private void initViews() {
        tabCategories = findViewById(R.id.tabCategories);
        searchView = findViewById(R.id.searchView);
        rvIcons = findViewById(R.id.rvIcons);
        btnCancel = findViewById(R.id.btnCancel);
        btnSelect = findViewById(R.id.btnSelect);
    }

    private void setupRecyclerView() {
        adapter = new IconSelectorAdapter(selectedIconKey, new IconSelectorAdapter.OnIconClickListener() {
            @Override
            public void onIconClick(IconHelper.IconItem iconItem) {
                selectedIconKey = iconItem.getKey();
                adapter.setSelectedIconKey(selectedIconKey);
                btnSelect.setText("✅ Seleccionar: " + iconItem.getName());
                btnSelect.setEnabled(true);
            }
        });

        rvIcons.setLayoutManager(new GridLayoutManager(context, 4));
        rvIcons.setAdapter(adapter);
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
                loadIcons(category);
                searchView.setQuery("", false);
                searchView.clearFocus();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupClickListeners() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterIcons(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2 || newText.isEmpty()) {
                    filterIcons(newText);
                }
                return true;
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        btnSelect.setOnClickListener(v -> {
            if (selectedIconKey != null && listener != null) {
                String iconName = IconHelper.getIconName(selectedIconKey);
                listener.onIconSelected(selectedIconKey, iconName);
            }
            dismiss();
        });
    }

    private void loadIcons(String category) {
        currentIcons = IconHelper.getIconsByCategory(category);
        adapter.updateIcons(currentIcons);
    }

    private void filterIcons(String query) {
        if (query.isEmpty()) {
            // Si la búsqueda está vacía, mostrar la categoría actual
            TabLayout.Tab selectedTab = tabCategories.getTabAt(tabCategories.getSelectedTabPosition());
            if (selectedTab != null) {
                loadIcons(selectedTab.getText().toString());
            }
        } else {
            // Filtrar iconos por la consulta
            List<IconHelper.IconItem> filteredIcons = IconHelper.searchIcons(query);
            adapter.updateIcons(filteredIcons);
        }
    }
}