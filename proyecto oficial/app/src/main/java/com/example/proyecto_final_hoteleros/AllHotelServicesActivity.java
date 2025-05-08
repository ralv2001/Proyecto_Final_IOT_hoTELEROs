package com.example.proyecto_final_hoteleros;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.proyecto_final_hoteleros.client.model.HotelService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class AllHotelServicesActivity extends AppCompatActivity {
    private RecyclerView recyclerViewAllServices;
    private MaterialButton btnAddServices;
    private ImageButton btnBack;
    private TextView tvCartCount, tvCartTotal;
    private View cartSummaryLayout;
    private List<HotelService> allServices = new ArrayList<>();
    private Set<String> selectedServiceIds = new HashSet<>();
    private double currentReservationTotal = 350.0; // Simulando una reserva existente
    private static final double TAXI_MIN_AMOUNT = 350.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_hotel_services);


        // Inicializar vistas
        recyclerViewAllServices = findViewById(R.id.rv_services_list);
        btnAddServices = findViewById(R.id.btn_add_service);
        btnBack = findViewById(R.id.btn_back);
        cartSummaryLayout = findViewById(R.id.cart_summary_layout);
        tvCartCount = findViewById(R.id.tv_cart_count);
        tvCartTotal = findViewById(R.id.tv_cart_total);


        // Configurar RecyclerView
        recyclerViewAllServices.setLayoutManager(new LinearLayoutManager(this));


        // Cargar todos los servicios
        loadAllServices();


        // Configurar el adaptador
        AllServicesAdapter adapter = new AllServicesAdapter(allServices, currentReservationTotal);
        recyclerViewAllServices.setAdapter(adapter);


        // Configurar el botón de retroceso
        btnBack.setOnClickListener(v -> onBackPressed());


        // Inicialmente el carrito está oculto hasta que se añada algún servicio
        updateCartSummary();


        // Botón de añadir servicios
        btnAddServices.setOnClickListener(v -> {
            if (selectedServiceIds.isEmpty()) {
                Toast.makeText(this, "No has seleccionado ningún servicio", Toast.LENGTH_SHORT).show();
            } else {
                // Aquí enviaríamos los servicios seleccionados a la siguiente actividad o los guardaríamos
                Toast.makeText(this, "Servicios añadidos correctamente", Toast.LENGTH_SHORT).show();


                // Simulación de envío a otra actividad
                Intent intent = new Intent();
                intent.putExtra("SELECTED_SERVICES", selectedServiceIds.toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }


    private void loadAllServices() {
        // Servicios gratuitos
        allServices.add(new HotelService(
                "wifi",
                "WiFi",
                "Conectarse a nuestra red inalámbrica en todas las áreas del establecimiento.",
                null, // Precio null = gratis
                null,
                "ic_wifi",
                false,
                null
        ));


        allServices.add(new HotelService(
                "reception",
                "Recepción 24 horas",
                "Atención personalizada las 24 horas del día.",
                null, // Precio null = gratis
                null,
                "ic_reception",
                false,
                null
        ));


        allServices.add(new HotelService(
                "pool",
                "Piscina",
                "Acceso a nuestra piscina temperada con vista panorámica.",
                null, // Precio null = gratis
                null,
                "ic_pool",
                false,
                null
        ));


        // Servicio condicional (taxi)
        allServices.add(new HotelService(
                "taxi",
                "Taxi al aeropuerto",
                "El servicio de taxi gratuito hacia el aeropuerto estará disponible si se adquiere una reserva de S/. 350",
                60.0, // Precio si no cumple la condición
                null,
                "ic_taxi",
                true,
                "Disponible gratis con reserva mínima de S/. 350"
        ));


        // Servicios de pago
        allServices.add(new HotelService(
                "breakfast",
                "Desayuno a la habitación",
                "Desayuno gourmet servido en la comodidad de la habitación.",
                40.0, // Precio en soles
                null,
                "ic_breakfast",
                false,
                null
        ));


        allServices.add(new HotelService(
                "spa",
                "Acceso al spa",
                "Incluye sauna, jacuzzi y una sesión de masajes de 30 minutos.",
                85.0,
                null,
                "ic_spa",
                false,
                null
        ));


        allServices.add(new HotelService(
                "gym",
                "Gimnasio privado",
                "Reserva exclusiva del gimnasio por 1 hora.",
                45.0,
                null,
                "ic_gym",
                false,
                null
        ));
    }


    private void updateCartSummary() {
        int count = 0;
        double total = 0.0;


        // Calcular el total (solo para servicios pagados)
        for (HotelService service : allServices) {
            if (selectedServiceIds.contains(service.getId())) {
                // Solo contamos servicios pagados
                if (service.getPrice() != null) {
                    count++;
                    // Para el taxi, verificar si es gratis debido a la condición
                    if (service.getId().equals("taxi") && currentReservationTotal >= TAXI_MIN_AMOUNT) {
                        // No añadir al total, es gratis
                    } else {
                        total += service.getPrice();
                    }
                }
            }
        }


        // Actualizar la UI
        if (count > 0) {
            cartSummaryLayout.setVisibility(View.VISIBLE);
            tvCartCount.setText(String.valueOf(count));
            tvCartTotal.setText(String.format("S/. %.2f", total));
        } else {
            cartSummaryLayout.setVisibility(View.GONE);
        }
    }


    // Adaptador para la lista de servicios
    class AllServicesAdapter extends RecyclerView.Adapter<AllServicesAdapter.AllServicesViewHolder> {
        private List<HotelService> services;
        private double currentTotal;


        public AllServicesAdapter(List<HotelService> services, double currentTotal) {
            this.services = services;
            this.currentTotal = currentTotal;
        }


        @NonNull
        @Override
        public AllServicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_service_detail, parent, false);
            return new AllServicesViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull AllServicesViewHolder holder, int position) {
            HotelService service = services.get(position);
            holder.bind(service);
        }


        @Override
        public int getItemCount() {
            return services.size();
        }


        // El siguiente fragmento debe reemplazar la clase AllServicesViewHolder en AllHotelServicesActivity.java

        class AllServicesViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivServiceImage;
            private TextView tvServiceName;
            private TextView tvServiceDescription;
            private TextView tvServicePrice;
            private ImageView expandButton;
            private Chip chipIncludedService;
            private MaterialButton btnAddToCart;
            private TextView tvServiceConditionalHint;
            private View divider;
            private View descriptionContainer;
            private View actionsContainer;
            private boolean isExpanded = false;

            public AllServicesViewHolder(@NonNull View itemView) {
                super(itemView);
                ivServiceImage = itemView.findViewById(R.id.iv_service_detail_icon);
                tvServiceName = itemView.findViewById(R.id.tv_service_detail_name);
                tvServiceDescription = itemView.findViewById(R.id.tv_service_detail_description);
                tvServicePrice = itemView.findViewById(R.id.tv_service_detail_price);
                expandButton = itemView.findViewById(R.id.iv_expand_service);
                chipIncludedService = itemView.findViewById(R.id.chip_included_service);
                btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
                tvServiceConditionalHint = itemView.findViewById(R.id.tv_service_conditional_hint);
                divider = itemView.findViewById(R.id.divider);
                descriptionContainer = itemView.findViewById(R.id.description_container);
                actionsContainer = itemView.findViewById(R.id.actions_container);
            }

            public void bind(HotelService service) {
                tvServiceName.setText(service.getName());
                tvServiceDescription.setText(service.getDescription());

                // Configurar icono/imagen
                if (service.getImageUrl() != null) {
                    // Cargar imagen desde URL (se usaría Glide o similar)
                } else {
                    int resourceId = itemView.getContext().getResources().getIdentifier(
                            service.getIconResourceName(), "drawable", itemView.getContext().getPackageName());
                    ivServiceImage.setImageResource(resourceId > 0 ? resourceId : R.drawable.ic_hotel_service_default);
                }

                // Estado de selección inicial
                boolean isSelected = selectedServiceIds.contains(service.getId());

                // Gestionar servicios condicionales (como el taxi)
                if (service.isConditional() && service.getId().equals("taxi")) {
                    boolean isFree = currentTotal >= TAXI_MIN_AMOUNT;

                    if (isFree) {
                        tvServicePrice.setText("Incluido");
                        tvServicePrice.setTextColor(itemView.getContext().getResources().getColor(R.color.orange_dark));
                        tvServiceConditionalHint.setVisibility(View.VISIBLE);
                        tvServiceConditionalHint.setText("¡Incluido con tu reserva actual! (Ahorro de S/. 60.00)");
                        chipIncludedService.setVisibility(View.VISIBLE);
                        btnAddToCart.setVisibility(View.GONE);
                        chipIncludedService.setText("Incluido en la reserva");
                        chipIncludedService.setChipIconResource(R.drawable.ic_check_circle);
                    } else {
                        tvServicePrice.setText(String.format("S/. %.2f", service.getPrice()));
                        tvServicePrice.setTextColor(itemView.getContext().getResources().getColor(R.color.orange_primary));
                        tvServiceConditionalHint.setVisibility(View.VISIBLE);
                        tvServiceConditionalHint.setText("*Disponible gratis con una reserva mínima de S/. 350.00");
                        tvServiceConditionalHint.setTextColor(itemView.getContext().getResources().getColor(R.color.orange_accent));
                        chipIncludedService.setVisibility(View.VISIBLE);
                        btnAddToCart.setVisibility(View.GONE); // No se puede agregar directamente
                        chipIncludedService.setText("Servicio condicional");
                        chipIncludedService.setChipIconResource(R.drawable.ic_info_outline);
                    }
                }
                // Servicios con precio fijo
                else if (!service.isFree()) {
                    tvServicePrice.setText(String.format("S/. %.2f", service.getPrice()));
                    tvServicePrice.setTextColor(itemView.getContext().getResources().getColor(R.color.orange_primary));
                    tvServiceConditionalHint.setVisibility(View.GONE);
                    chipIncludedService.setVisibility(View.GONE);
                    btnAddToCart.setVisibility(View.VISIBLE);
                    btnAddToCart.setText(isSelected ? "Quitar del carrito" : "Añadir al carrito");

                    // Actualizar color del botón según estado
                    if (isSelected) {
                        btnAddToCart.setBackgroundTintList(
                                itemView.getContext().getResources().getColorStateList(R.color.orange_accent));
                    } else {
                        btnAddToCart.setBackgroundTintList(
                                itemView.getContext().getResources().getColorStateList(R.color.orange_primary));
                    }
                }
                // Servicios gratuitos
                else {
                    tvServicePrice.setText("Incluido");
                    tvServicePrice.setTextColor(itemView.getContext().getResources().getColor(R.color.orange_dark));
                    tvServiceConditionalHint.setVisibility(View.GONE);
                    chipIncludedService.setVisibility(View.VISIBLE);
                    btnAddToCart.setVisibility(View.GONE);
                    chipIncludedService.setText("Incluido en la reserva");
                    chipIncludedService.setChipIconResource(R.drawable.ic_check_circle);
                    chipIncludedService.setClickable(false); // No es clickable para servicios incluidos
                }

                // Gestionar el estado inicial de la descripción (colapsada)
                descriptionContainer.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);
                expandButton.setImageResource(android.R.drawable.arrow_down_float);
                expandButton.setColorFilter(itemView.getContext().getResources().getColor(R.color.orange_primary));
                isExpanded = false;

                // Expandir/contraer descripción con mejor manejo de espacio
                expandButton.setOnClickListener(v -> {
                    isExpanded = !isExpanded;
                    if (isExpanded) {
                        descriptionContainer.setVisibility(View.VISIBLE);
                        expandButton.setImageResource(android.R.drawable.arrow_up_float);
                        // Mostrar el divisor si el servicio tiene nota condicional
                        if (service.isConditional()) {
                            divider.setVisibility(View.VISIBLE);
                        }
                    } else {
                        descriptionContainer.setVisibility(View.GONE);
                        divider.setVisibility(View.GONE);
                        expandButton.setImageResource(android.R.drawable.arrow_down_float);
                    }
                });

                // El chip de servicios incluidos no es interactivo
                chipIncludedService.setClickable(false);

                // Click en botón para servicios de pago
                btnAddToCart.setOnClickListener(v -> {
                    boolean newState = !selectedServiceIds.contains(service.getId());
                    if (newState) {
                        selectedServiceIds.add(service.getId());
                        btnAddToCart.setText("Quitar del carrito");
                        btnAddToCart.setBackgroundTintList(
                                itemView.getContext().getResources().getColorStateList(R.color.orange_accent));
                    } else {
                        selectedServiceIds.remove(service.getId());
                        btnAddToCart.setText("Añadir al carrito");
                        btnAddToCart.setBackgroundTintList(
                                itemView.getContext().getResources().getColorStateList(R.color.orange_primary));
                    }
                    updateCartSummary();
                });
            }
        }
    }
}