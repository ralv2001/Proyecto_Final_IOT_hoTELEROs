package com.example.proyecto_final_hoteleros.taxista.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.taxista.adapters.EditProfileAdapter;
import com.example.proyecto_final_hoteleros.taxista.model.EditProfileItem;
import com.example.proyecto_final_hoteleros.taxista.model.DriverProfile;
import com.example.proyecto_final_hoteleros.taxista.utils.DriverPreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class EditDriverProfileFragment extends Fragment implements
        EditProfileAdapter.OnEditProfileListener {

    private static final String TAG = "EditDriverProfileFragment";

    private RecyclerView recyclerView;
    private EditProfileAdapter adapter;
    private List<EditProfileItem> profileItems;
    private MaterialButton btnSave;
    private MaterialToolbar toolbar;

    private DriverProfile currentDriver;
    private Uri selectedProfileImageUri;
    private Uri selectedCarImageUri;

    // Launchers para seleccionar imágenes
    private ActivityResultLauncher<Intent> profileImageLauncher;
    private ActivityResultLauncher<Intent> carImageLauncher;

    private DriverPreferenceManager preferenceManager;

    public EditDriverProfileFragment() {
        // Constructor vacío requerido
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceManager = new DriverPreferenceManager(requireContext());

        // Configurar launchers para selección de imágenes
        profileImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedProfileImageUri = result.getData().getData();
                        updateProfileImage();
                    }
                }
        );

        carImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedCarImageUri = result.getData().getData();
                        updateCarImage();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.taxi_fragment_edit_driver_profile, container, false);

        // Recibir el perfil del conductor si fue pasado como argumento
        if (getArguments() != null && getArguments().containsKey("driver_profile")) {
            currentDriver = getArguments().getParcelable("driver_profile");
        }

        initViews(view);
        setupToolbar();
        setupRecyclerView();
        loadDriverData();

        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        recyclerView = view.findViewById(R.id.recycler_edit_profile);
        btnSave = view.findViewById(R.id.btn_save_profile);

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void setupToolbar() {
        toolbar.setTitle("Editar Perfil");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void setupRecyclerView() {
        profileItems = new ArrayList<>();
        adapter = new EditProfileAdapter(getContext(), profileItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadDriverData() {
        // Obtener datos del conductor (aquí deberías cargar desde tu fuente de datos)
        currentDriver = getCurrentDriverData();

        // Limpiar items existentes
        profileItems.clear();

        // Agregar header con foto de perfil
        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.PROFILE_HEADER,
                "Foto de Perfil",
                currentDriver.getProfileImageUrl(),
                "profile_image",
                true,
                R.drawable.ic_camera
        ));

        // Información personal (solo lectura)
        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.SECTION_HEADER,
                "Información Personal",
                "",
                "",
                false,
                0
        ));

        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Nombres",
                extractFirstName(currentDriver.getFullName()),
                "first_name",
                false,
                R.drawable.ic_person
        ));

        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Apellidos",
                extractLastName(currentDriver.getFullName()),
                "last_name",
                false,
                R.drawable.ic_person
        ));

        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Tipo de Documento",
                "DNI", // Valor fijo ya que no está en tu modelo
                "document_type",
                false,
                R.drawable.ic_document
        ));

        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Número de Documento",
                currentDriver.getLicenseNumber(), // Usando licenseNumber como documentNumber
                "document_number",
                false,
                R.drawable.ic_document
        ));

        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Fecha de Nacimiento",
                "15/03/1990", // Valor de ejemplo fijo
                "birth_date",
                false,
                R.drawable.ic_calendar
        ));

        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Correo Electrónico",
                currentDriver.getEmail(),
                "email",
                false,
                R.drawable.ic_email
        ));

        // Información editable
        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.SECTION_HEADER,
                "Información de Contacto",
                "",
                "",
                false,
                0
        ));

        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.EDITABLE_FIELD,
                "Teléfono",
                currentDriver.getPhoneNumber(),
                "phone",
                true,
                R.drawable.ic_phone
        ));

        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Domicilio",
                currentDriver.getAddress(),
                "address",
                false,
                R.drawable.ic_location
        ));

        // Información del vehículo
        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.SECTION_HEADER,
                "Información del Vehículo",
                "",
                "",
                false,
                0
        ));

        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.EDITABLE_FIELD,
                "Modelo del Auto",
                "Toyota Rush 2023", // Valor de ejemplo fijo
                "car_model",
                true,
                R.drawable.ic_taxi
        ));

        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Placa del Auto",
                "ABC-123", // Valor de ejemplo fijo
                "license_plate",
                false,
                R.drawable.ic_license_plate
        ));

        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.CAR_IMAGE,
                "Foto del Auto",
                "https://example.com/car_image.jpg", // URL de ejemplo
                "car_image",
                true,
                R.drawable.ic_camera
        ));

        adapter.notifyDataSetChanged();
    }

    private DriverProfile getCurrentDriverData() {
        return preferenceManager.getDriverProfile();
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "";
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 0 ? parts[0] : "";
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length <= 1) return "";

        StringBuilder lastName = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            if (i > 1) lastName.append(" ");
            lastName.append(parts[i]);
        }
        return lastName.toString();
    }


    @Override
    public void onFieldChanged(String fieldKey, String newValue) {
        Log.d(TAG, "Field changed: " + fieldKey + " = " + newValue);
        // Aquí puedes manejar los cambios en tiempo real si es necesario
    }

    @Override
    public void onImageClick(String imageType) {
        Log.d(TAG, "Image click: " + imageType);
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if ("profile_image".equals(imageType)) {
            profileImageLauncher.launch(intent);
        } else if ("car_image".equals(imageType)) {
            carImageLauncher.launch(intent);
        }
    }

    private void updateProfileImage() {
        if (selectedProfileImageUri != null) {
            // Actualizar la imagen en el adapter
            for (int i = 0; i < profileItems.size(); i++) {
                EditProfileItem item = profileItems.get(i);
                if (item.getType() == EditProfileItem.EditItemType.PROFILE_HEADER) {
                    item.setValue(selectedProfileImageUri.toString());
                    adapter.notifyItemChanged(i);
                    break;
                }
            }
            Toast.makeText(getContext(), "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCarImage() {
        if (selectedCarImageUri != null) {
            // Actualizar la imagen del auto en el adapter
            for (int i = 0; i < profileItems.size(); i++) {
                EditProfileItem item = profileItems.get(i);
                if (item.getType() == EditProfileItem.EditItemType.CAR_IMAGE) {
                    item.setValue(selectedCarImageUri.toString());
                    adapter.notifyItemChanged(i);
                    break;
                }
            }
            Toast.makeText(getContext(), "Foto del auto actualizada", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile() {
        Log.d(TAG, "Guardando perfil...");

        String newPhone = "";
        String newCarModel = "";
        String newProfileImageUrl = currentDriver.getProfileImageUrl();
        String newCarImageUrl = "";

        // Recopilar cambios de los campos editables
        for (EditProfileItem item : profileItems) {
            if (item.getType() == EditProfileItem.EditItemType.EDITABLE_FIELD) {
                switch (item.getKey()) {
                    case "phone":
                        newPhone = item.getValue();
                        break;
                    case "car_model":
                        newCarModel = item.getValue();
                        break;
                }
            }
        }

        // Obtener URLs de imágenes si fueron cambiadas
        if (selectedProfileImageUri != null) {
            newProfileImageUrl = selectedProfileImageUri.toString();
        }
        if (selectedCarImageUri != null) {
            newCarImageUrl = selectedCarImageUri.toString();
        }

        // Validar datos obligatorios
        if (newPhone.trim().isEmpty()) {
            Toast.makeText(getContext(), "El teléfono es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newCarModel.trim().isEmpty()) {
            Toast.makeText(getContext(), "El modelo del auto es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizar el perfil del conductor
        currentDriver.setPhoneNumber(newPhone);
        currentDriver.setProfileImageUrl(newProfileImageUrl);

        // Guardar perfil actualizado en local storage
        preferenceManager.saveDriverProfile(currentDriver);

        // Guardar información adicional del vehículo
        preferenceManager.saveCarInfo(newCarModel, newCarImageUrl, "ABC-123");

        Toast.makeText(getContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();

        // Regresar a la pantalla anterior
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    // Método para guardar datos adicionales que no están en el modelo DriverProfile
    private void saveAdditionalProfileData(String carModel, String carImageUrl) {
        // Opción 1: SharedPreferences
        // SharedPreferences prefs = getContext().getSharedPreferences("driver_extra_data", Context.MODE_PRIVATE);
        // SharedPreferences.Editor editor = prefs.edit();
        // editor.putString("car_model_" + currentDriver.getDriverId(), carModel);
        // editor.putString("car_image_" + currentDriver.getDriverId(), carImageUrl);
        // editor.apply();

        // Opción 2: Base de datos local (Room)
        // DriverExtraDataRepository.saveCarInfo(currentDriver.getDriverId(), carModel, carImageUrl);

        Log.d(TAG, "Datos adicionales guardados: carModel=" + carModel + ", carImageUrl=" + carImageUrl);
    }

    // Método para cargar datos adicionales
    public String getCarModel(String driverId) {
        // Cargar desde SharedPreferences o base de datos
        // SharedPreferences prefs = getContext().getSharedPreferences("driver_extra_data", Context.MODE_PRIVATE);
        // return prefs.getString("car_model_" + driverId, "Toyota Rush 2023");
        return "Toyota Rush 2023"; // Valor por defecto
    }

    public String getCarImageUrl(String driverId) {
        // Cargar desde SharedPreferences o base de datos
        // SharedPreferences prefs = getContext().getSharedPreferences("driver_extra_data", Context.MODE_PRIVATE);
        // return prefs.getString("car_image_" + driverId, "");
        return "https://example.com/car_image.jpg"; // Valor por defecto
    }

    // Método para actualizar el perfil en el servidor
    private void saveProfileToServer(DriverProfile driverProfile, String carModel, String carImageUrl) {
        // TODO: Implementar llamada al API
        // El servidor debería manejar tanto los datos del DriverProfile como los datos adicionales
        Log.d(TAG, "Guardando en servidor: " + driverProfile.toString());
        Log.d(TAG, "Datos adicionales: carModel=" + carModel + ", carImageUrl=" + carImageUrl);

        // Ejemplo de estructura JSON que podrías enviar:
        /*
        {
            "driver_profile": {
                "driverId": "driver001",
                "fullName": "Renato Delgado Aquino",
                "phoneNumber": "+51 987 654 321",
                "profileImageUrl": "...",
                // ... otros campos del DriverProfile
            },
            "additional_data": {
                "carModel": "Toyota Rush 2023",
                "carImageUrl": "...",
                "licensePlate": "ABC-123",
                "birthDate": "15/03/1990"
            }
        }
        */
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "EditDriverProfileFragment resumed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias
        recyclerView = null;
        adapter = null;
        profileItems = null;
        btnSave = null;
        toolbar = null;
        Log.d(TAG, "Vista destruida y referencias limpiadas");
    }
}