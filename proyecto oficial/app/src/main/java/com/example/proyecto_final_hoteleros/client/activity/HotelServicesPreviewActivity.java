package com.example.proyecto_final_hoteleros.client.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.adapters.ServicePreviewAdapter;
import com.example.proyecto_final_hoteleros.client.model.HotelService;
import com.example.proyecto_final_hoteleros.client.interfaces.ServiceClickListener;
import com.example.proyecto_final_hoteleros.client.repository.ServicesRepository;

import java.util.ArrayList;
import java.util.List;

public class HotelServicesPreviewActivity extends AppCompatActivity implements ServiceClickListener {
    private static final String TAG = "HotelServicesPreview";

    private RecyclerView recyclerViewServices;
    private TextView tvSeeAll;
    private List<HotelService> servicesToShow = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_hotel_services_preview);
            Log.d(TAG, "Activity creada");

            initViews();
            loadFeaturedServices();
            setupAdapter();
            setupClickListeners();

        } catch (Exception e) {
            Log.e(TAG, "Error en onCreate: " + e.getMessage());
            // Si hay error cargando la actividad, ir directamente a AllServices
            startAllServicesActivity();
        }
    }

    private void initViews() {
        recyclerViewServices = findViewById(R.id.rv_services_preview);
        tvSeeAll = findViewById(R.id.tv_see_all_services);

        if (recyclerViewServices != null) {
            recyclerViewServices.setLayoutManager(new GridLayoutManager(this, 4));
        }

        Log.d(TAG, "Views inicializadas");
    }

    private void loadFeaturedServices() {
        try {
            ServicesRepository repository = ServicesRepository.getInstance();
            servicesToShow = repository.getFeaturedServices();

            // Si no hay servicios destacados, mostrar los primeros 4
            if (servicesToShow.isEmpty()) {
                List<HotelService> allServices = repository.getAllServices();
                servicesToShow = allServices.size() > 4 ?
                        allServices.subList(0, 4) : allServices;
            }

            Log.d(TAG, "Servicios cargados: " + servicesToShow.size());
        } catch (Exception e) {
            Log.e(TAG, "Error cargando servicios: " + e.getMessage());
            servicesToShow = new ArrayList<>();
        }
    }

    private void setupAdapter() {
        try {
            if (recyclerViewServices != null) {
                ServicePreviewAdapter adapter = new ServicePreviewAdapter(servicesToShow, this);
                recyclerViewServices.setAdapter(adapter);
                Log.d(TAG, "Adapter configurado");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error configurando adapter: " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        if (tvSeeAll != null) {
            tvSeeAll.setOnClickListener(v -> {
                Log.d(TAG, "Botón 'Ver todo' presionado");
                startAllServicesActivity();
            });
        }
    }

    private void startAllServicesActivity() {
        try {
            Intent intent = new Intent(this, AllHotelServicesActivity.class);
            startActivity(intent);
            Log.d(TAG, "Navegando a AllHotelServicesActivity");
        } catch (Exception e) {
            Log.e(TAG, "Error navegando a AllServices: " + e.getMessage());
            Toast.makeText(this, "Error abriendo servicios", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onServiceClicked(HotelService service) {
        try {
            Log.d(TAG, "Servicio clickeado: " + service.getName());

            // Mostrar información del servicio
            String message = service.getName();
            if (service.isConditional()) {
                message += " - " + service.getConditionalDescription();
            } else if (service.isFree()) {
                message += " - Incluido en tu reserva";
            } else if (service.getPrice() != null) {
                message += " - " + service.getPriceDisplay();
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            // Navegar a la pantalla completa de servicios
            startAllServicesActivity();

        } catch (Exception e) {
            Log.e(TAG, "Error en onServiceClicked: " + e.getMessage());
            startAllServicesActivity();
        }
    }
}