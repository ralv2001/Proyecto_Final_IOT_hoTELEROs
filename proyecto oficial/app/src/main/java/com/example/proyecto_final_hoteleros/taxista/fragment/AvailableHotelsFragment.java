package com.example.proyecto_final_hoteleros.taxista.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.taxista.adapters.AvailableHotelsAdapter;
import com.example.proyecto_final_hoteleros.taxista.model.AvailableHotel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AvailableHotelsFragment extends Fragment implements AvailableHotelsAdapter.HotelListener {

    private static final String TAG = "AvailableHotelsFragment";

    private MaterialToolbar toolbar;
    private RecyclerView recyclerHotels;
    private AvailableHotelsAdapter adapter;
    private List<AvailableHotel> hotelsList;

    private LinearLayout emptyState;
    private LinearLayout loadingState;
    private TextView tvHotelsCount;
    private FloatingActionButton btnRefresh;

    public AvailableHotelsFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_available_hotels, container, false);

        initViews(view);
        setupToolbar();
        setupRecyclerView();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadAvailableHotels();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        recyclerHotels = view.findViewById(R.id.recycler_hotels);
        emptyState = view.findViewById(R.id.empty_state);
        loadingState = view.findViewById(R.id.loading_state);
        tvHotelsCount = view.findViewById(R.id.tv_hotels_count);
        btnRefresh = view.findViewById(R.id.btn_refresh);

        btnRefresh.setOnClickListener(v -> loadAvailableHotels());
    }

    private void setupToolbar() {
        toolbar.setTitle("Hoteles Disponibles");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void setupRecyclerView() {
        hotelsList = new ArrayList<>();
        adapter = new AvailableHotelsAdapter(getContext(), hotelsList, this);
        recyclerHotels.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerHotels.setAdapter(adapter);
    }

    private void loadAvailableHotels() {
        showLoadingState();

        // Simular carga de datos (reemplazar con llamada real al API)
        new Handler().postDelayed(() -> {
            List<AvailableHotel> newHotels = generateHotelsData();

            hotelsList.clear();
            hotelsList.addAll(newHotels);
            adapter.notifyDataSetChanged();

            updateHotelsCount();
            updateVisibility();

        }, 1500);
    }

    private void updateHotelsCount() {
        int count = hotelsList.size();
        tvHotelsCount.setText("Encontrados " + count + " hotel" + (count == 1 ? "" : "es") + " cercano" + (count == 1 ? "" : "s"));
    }

    private void updateVisibility() {
        if (hotelsList.isEmpty()) {
            recyclerHotels.setVisibility(View.GONE);
            loadingState.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerHotels.setVisibility(View.VISIBLE);
            loadingState.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private void showLoadingState() {
        recyclerHotels.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        loadingState.setVisibility(View.VISIBLE);
    }

    @Override
    public void onHotelClick(AvailableHotel hotel) {
        Log.d(TAG, "Hotel clicked: " + hotel.getName());
        Toast.makeText(getContext(), "Ver detalles de " + hotel.getName(), Toast.LENGTH_SHORT).show();

        // TODO: Navegar a detalles del hotel o mostrar información adicional
        // HotelDetailsFragment detailsFragment = HotelDetailsFragment.newInstance(hotel);
        // getParentFragmentManager().beginTransaction()
        //     .replace(R.id.fragment_container, detailsFragment)
        //     .addToBackStack(null)
        //     .commit();
    }

    @Override
    public void onCallHotel(AvailableHotel hotel) {
        Log.d(TAG, "Call hotel: " + hotel.getName());

        if (hotel.getPhoneNumber() != null && !hotel.getPhoneNumber().isEmpty()) {
            // Verificar permisos antes de realizar la llamada
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Solicitar permiso
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 100);
                return;
            }

            try {
                // Limpiar el número telefónico (remover espacios, guiones, etc.)
                String cleanPhoneNumber = hotel.getPhoneNumber().replaceAll("[^0-9+]", "");

                // Crear intent para realizar llamada
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + cleanPhoneNumber));

                // Iniciar la llamada
                startActivity(callIntent);

                Log.d(TAG, "Llamando a: " + cleanPhoneNumber);

            } catch (SecurityException e) {
                Log.e(TAG, "Error de permisos al llamar: " + e.getMessage());
                Toast.makeText(getContext(), "Error: Permisos de llamada no concedidos", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error al realizar llamada: " + e.getMessage());
                Toast.makeText(getContext(), "Error al realizar la llamada", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(getContext(), "No hay número disponible", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGetDirections(AvailableHotel hotel) {
        Log.d(TAG, "Get directions to: " + hotel.getName());

        if (hotel.getLatitude() != 0 && hotel.getLongitude() != 0) {
            // TODO: Abrir Google Maps con direcciones
            // String uri = "google.navigation:q=" + hotel.getLatitude() + "," + hotel.getLongitude();
            // Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            // mapIntent.setPackage("com.google.android.apps.maps");
            // startActivity(mapIntent);

            Toast.makeText(getContext(), "Abriendo direcciones a " + hotel.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Ubicación no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private List<AvailableHotel> generateHotelsData() {
        List<AvailableHotel> hotels = new ArrayList<>();

        hotels.add(new AvailableHotel(
                "hotel1",
                "Hotel Gran Plaza",
                "Av. La Marina 123, San Miguel",
                "San Miguel",
                4.8f,
                "https://cf.bstatic.com/xdata/images/hotel/max1024x768/237363319.jpg",
                0.8, // 0.8 km
                "987654321", // Número de 9 dígitos
                "Asociado desde 2020",
                "Hotel de 4 estrellas con servicios premium",
                127, // solicitudes totales
                "Activo",
                -12.0864, // Latitud (San Miguel, Lima)
                -77.0844  // Longitud
        ));

        hotels.add(new AvailableHotel(
                "hotel2",
                "Hotel Miraflores Park",
                "Av. Malecón 456, Miraflores",
                "Miraflores",
                4.9f,
                "https://cf.bstatic.com/xdata/images/hotel/max1024x768/185963555.jpg",
                1.2, // 1.2 km
                "912345678", // Número de 9 dígitos
                "Asociado desde 2019",
                "Hotel boutique frente al mar",
                89,
                "Activo",
                -12.1211,
                -77.0289
        ));

        hotels.add(new AvailableHotel(
                "hotel3",
                "Hotel Lima Centro",
                "Jr. De la Unión 789, Cercado de Lima",
                "Lima Centro",
                4.5f,
                "https://cf.bstatic.com/xdata/images/hotel/max1024x768/298765432.jpg",
                2.1, // 2.1 km
                "998877665", // Número de 9 dígitos
                "Asociado desde 2021",
                "Hotel histórico en el centro de Lima",
                156,
                "Activo",
                -12.0431,
                -77.0282
        ));

        hotels.add(new AvailableHotel(
                "hotel4",
                "Hotel San Isidro Business",
                "Av. República de Panamá 321, San Isidro",
                "San Isidro",
                4.7f,
                "https://cf.bstatic.com/xdata/images/hotel/max1024x768/401234567.jpg",
                1.8, // 1.8 km
                "955443322", // Número de 9 dígitos
                "Asociado desde 2018",
                "Hotel ejecutivo en zona financiera",
                203,
                "Activo",
                -12.0931,
                -77.0465
        ));

        hotels.add(new AvailableHotel(
                "hotel5",
                "Hotel Barranco Boutique",
                "Av. Pedro de Osma 654, Barranco",
                "Barranco",
                4.6f,
                "https://cf.bstatic.com/xdata/images/hotel/max1024x768/567890123.jpg",
                3.2, // 3.2 km
                "966778899", // Número de 9 dígitos
                "Asociado desde 2022",
                "Hotel artístico en distrito bohemio",
                78,
                "Activo",
                -12.1464,
                -77.0208
        ));

        return hotels;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) { // Código de permiso para llamadas
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permiso de llamada concedido. Intenta nuevamente.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Permiso de llamada denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias
        recyclerHotels = null;
        adapter = null;
        hotelsList = null;
        Log.d(TAG, "Vista destruida y referencias limpiadas");
    }
}