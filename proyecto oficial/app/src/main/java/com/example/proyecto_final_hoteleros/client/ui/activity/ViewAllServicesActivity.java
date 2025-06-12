package com.example.proyecto_final_hoteleros.client.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.adapters.ServicesViewOnlyAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.HotelService;
import com.example.proyecto_final_hoteleros.client.data.repository.ServicesRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewAllServicesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewServices;
    private LinearLayout emptyState;
    private ImageButton btnBack;
    private TextView tvHotelName;

    private List<HotelService> allServices = new ArrayList<>();
    private ServicesViewOnlyAdapter adapter;
    private String hotelName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_activity_view_all_services);

        initViews();
        getIntentData();
        loadServices();
        setupAdapter();
        setupClickListeners();
    }

    private void initViews() {
        recyclerViewServices = findViewById(R.id.rv_services_view_only);
        emptyState = findViewById(R.id.empty_state);
        btnBack = findViewById(R.id.btn_back);
        tvHotelName = findViewById(R.id.tv_hotel_name);

        if (recyclerViewServices != null) {
            recyclerViewServices.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewServices.setHasFixedSize(true);
        }
    }

    private void getIntentData() {
        hotelName = getIntent().getStringExtra("hotel_name");
        if (hotelName == null) hotelName = "Hotel";
        tvHotelName.setText(hotelName);
    }

    private void loadServices() {
        ServicesRepository repository = ServicesRepository.getInstance();
        allServices = repository.getAllServices();
    }

    private void setupAdapter() {
        // Organizar servicios por categoría
        Map<HotelService.ServiceCategory, List<HotelService>> servicesByCategory = new HashMap<>();

        for (HotelService service : allServices) {
            HotelService.ServiceCategory category = service.getCategory();
            if (!servicesByCategory.containsKey(category)) {
                servicesByCategory.put(category, new ArrayList<>());
            }
            servicesByCategory.get(category).add(service);
        }

        adapter = new ServicesViewOnlyAdapter(servicesByCategory);
        if (recyclerViewServices != null) {
            recyclerViewServices.setAdapter(adapter);
        }
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}