package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.HotelPhotosAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.BasicServicesAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.model.HotelProfile;
import com.example.proyecto_final_hoteleros.adminhotel.model.BasicService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class HotelProfileFragment extends Fragment {

    private TextInputEditText etHotelName, etHotelDescription, etHotelAddress, etHotelPhone;
    private RecyclerView rvHotelPhotos, rvBasicServices;
    private MaterialButton btnSaveProfile, btnAddPhoto, btnAddBasicService;
    private ImageView ivBack;

    private HotelPhotosAdapter photosAdapter;
    private BasicServicesAdapter servicesAdapter;
    private List<Uri> hotelPhotos;
    private List<BasicService> basicServices;

    private ActivityResultLauncher<Intent> photoPickerLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_profile_management, container, false);

        initViews(rootView);
        setupPhotoLauncher();
        setupRecyclerViews();
        loadHotelProfile();
        setupClickListeners();

        return rootView;
    }

    private void initViews(View rootView) {
        ivBack = rootView.findViewById(R.id.ivBack);
        etHotelName = rootView.findViewById(R.id.etHotelName);
        etHotelDescription = rootView.findViewById(R.id.etHotelDescription);
        etHotelAddress = rootView.findViewById(R.id.etHotelAddress);
        etHotelPhone = rootView.findViewById(R.id.etHotelPhone);

        rvHotelPhotos = rootView.findViewById(R.id.rvHotelPhotos);
        rvBasicServices = rootView.findViewById(R.id.rvBasicServices);

        btnSaveProfile = rootView.findViewById(R.id.btnSaveProfile);
        btnAddPhoto = rootView.findViewById(R.id.btnAddPhoto);
        btnAddBasicService = rootView.findViewById(R.id.btnAddBasicService);
    }

    private void setupPhotoLauncher() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri photoUri = result.getData().getData();
                        if (photoUri != null && hotelPhotos.size() < 8) {
                            hotelPhotos.add(photoUri);
                            photosAdapter.notifyDataSetChanged();
                        } else if (hotelPhotos.size() >= 8) {
                            Toast.makeText(getContext(), "Máximo 8 fotos permitidas", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void setupRecyclerViews() {
        // Photos RecyclerView
        hotelPhotos = new ArrayList<>();
        photosAdapter = new HotelPhotosAdapter(hotelPhotos, this::removePhoto);
        rvHotelPhotos.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvHotelPhotos.setAdapter(photosAdapter);

        // Basic Services RecyclerView
        basicServices = new ArrayList<>();
        servicesAdapter = new BasicServicesAdapter(basicServices, this::removeBasicService);
        rvBasicServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBasicServices.setAdapter(servicesAdapter);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnAddPhoto.setOnClickListener(v -> {
            if (hotelPhotos.size() >= 8) {
                Toast.makeText(getContext(), "Máximo 8 fotos permitidas", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            photoPickerLauncher.launch(intent);
        });


        btnSaveProfile.setOnClickListener(v -> saveHotelProfile());
    }



    private void removePhoto(int position) {
        hotelPhotos.remove(position);
        photosAdapter.notifyItemRemoved(position);
    }

    private void removeBasicService(int position) {
        basicServices.remove(position);
        servicesAdapter.notifyItemRemoved(position);
    }

    private void loadHotelProfile() {
        // Cargar datos existentes del hotel
        etHotelName.setText("Hotel Belmond");
        etHotelDescription.setText("Hotel de lujo ubicado en el corazón de la ciudad");
        etHotelAddress.setText("Av. Principal 123, Centro");
        etHotelPhone.setText("+51 987 654 321");

        // Agregar servicios básicos por defecto
        basicServices.add(new BasicService("WiFi Gratuito", "Internet de alta velocidad", "ic_wifi"));
        basicServices.add(new BasicService("Aire Acondicionado", "Climatización en todas las habitaciones", "ic_ac"));
        basicServices.add(new BasicService("TV Cable", "Televisión por cable con canales premium", "ic_tv"));
        basicServices.add(new BasicService("Teléfono", "Línea telefónica directa", "ic_phone"));

        servicesAdapter.notifyDataSetChanged();
    }

    private void saveHotelProfile() {
        String name = etHotelName.getText().toString().trim();
        String description = etHotelDescription.getText().toString().trim();
        String address = etHotelAddress.getText().toString().trim();
        String phone = etHotelPhone.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(getContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hotelPhotos.size() < 4) {
            Toast.makeText(getContext(), "Se requieren mínimo 4 fotos del hotel", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Guardar en Firebase/base de datos
        HotelProfile profile = new HotelProfile(name, description, address, phone, hotelPhotos, basicServices);

        Toast.makeText(getContext(), "✅ Perfil del hotel actualizado", Toast.LENGTH_LONG).show();
        getParentFragmentManager().popBackStack();
    }
}