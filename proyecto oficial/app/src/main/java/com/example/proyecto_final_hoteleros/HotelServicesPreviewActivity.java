package com.example.proyecto_final_hoteleros;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.model.HotelService;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
public class HotelServicesPreviewActivity extends AppCompatActivity {
    private RecyclerView recyclerViewServices;
    private TextView tvSeeAll;
    private List<HotelService> servicesToShow = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_services_preview);

        recyclerViewServices = findViewById(R.id.rv_services_preview);
        tvSeeAll = findViewById(R.id.tv_see_all_services);

        // Configurar RecyclerView en formato de grid
        recyclerViewServices.setLayoutManager(new GridLayoutManager(this, 4));

        // Cargar servicios destacados (solo mostramos 4-6 en vista previa)
        loadFeaturedServices();

        // Configurar el adaptador
        ServicePreviewAdapter adapter = new ServicePreviewAdapter(servicesToShow);
        recyclerViewServices.setAdapter(adapter);

        // Configurar botón de "Ver todo"
        tvSeeAll.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllHotelServicesActivity.class);
            startActivity(intent);
        });
    }

    private void loadFeaturedServices() {
        // Estos serían los servicios destacados que aparecen en la pantalla principal
        // Normalmente estos vendrían de una API o base de datos

        servicesToShow.add(new HotelService(
                "wifi",
                "WiFi",
                "Conectarse a nuestra red inalámbrica en todas las áreas del establecimiento.",
                null, // Precio null = gratis
                null, // Sin imagen específica
                "ic_wifi", // Usa icono predeterminado
                false,
                null
        ));

        servicesToShow.add(new HotelService(
                "reception",
                "Recepción 24 horas",
                "Atención personalizada las 24 horas del día.",
                null, // Precio null = gratis
                null, // Sin imagen específica
                "ic_reception", // Usa icono predeterminado
                false,
                null
        ));

        servicesToShow.add(new HotelService(
                "pool",
                "Piscina",
                "Conectarse a nuestra red inalámbrica en todas las áreas del establecimiento.",
                null, // Precio null = gratis
                null, // Sin imagen específica
                "ic_pool", // Usa icono predeterminado
                false,
                null
        ));

        servicesToShow.add(new HotelService(
                "taxi",
                "Taxi*",
                "El servicio de taxi gratuito hacia el aeropuerto estará disponible si se adquiere una reserva de S/. 350",
                null, // Precio base null ya que depende de condición
                null, // Sin imagen específica
                "ic_taxi", // Usa icono predeterminado
                true, // Es condicional
                "Disponible gratis con reserva mínima de S/. 350"
        ));
    }

    // Adaptador para la vista previa de servicios
    class ServicePreviewAdapter extends RecyclerView.Adapter<ServicePreviewAdapter.ServiceViewHolder> {
        private List<HotelService> services;

        public ServicePreviewAdapter(List<HotelService> services) {
            this.services = services;
        }

        @NonNull
        @Override
        public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_service_preview, parent, false);
            return new ServiceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
            HotelService service = services.get(position);
            holder.bind(service);
        }

        @Override
        public int getItemCount() {
            return services.size();
        }

        class ServiceViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivServiceIcon;
            private TextView tvServiceName;

            public ServiceViewHolder(@NonNull View itemView) {
                super(itemView);
                ivServiceIcon = itemView.findViewById(R.id.iv_service_icon);
                tvServiceName = itemView.findViewById(R.id.tv_service_name);
            }

            public void bind(HotelService service) {
                tvServiceName.setText(service.getName());

                // Configurar icono
                if (service.getImageUrl() != null) {
                    // Aquí cargarías la imagen desde la URL usando Glide, Picasso, etc.
                    // Por ejemplo con Glide:
                    // Glide.with(itemView.getContext()).load(service.getImageUrl()).into(ivServiceIcon);
                } else {
                    // Usar icono por defecto
                    int resourceId = itemView.getContext().getResources().getIdentifier(
                            service.getIconResourceName(), "drawable", itemView.getContext().getPackageName());
                    ivServiceIcon.setImageResource(resourceId);
                }

                // Fondo naranja circular para el icono (como en tu imagen)
                ivServiceIcon.setBackgroundResource(R.drawable.bg_circle_orange);

                // Manejar clics - Mostrar detalle en un Toast por ahora
                itemView.setOnClickListener(v -> {
                    String message = service.getName();
                    if (service.isConditional()) {
                        message += " - " + service.getConditionalDescription();
                    }
                    Toast.makeText(itemView.getContext(), message, Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
}