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
        Log.d(TAG, "🔄 Cargando datos reales del conductor para editar perfil");

        // Limpiar items existentes
        profileItems.clear();

        // 🔥 CARGAR DATOS REALES DESDE EL ACTIVITY
        if (getActivity() instanceof com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity) {
            com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity activity =
                    (com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity) getActivity();

            String userId = activity.getUserId();
            String userName = activity.getUserName();
            String userEmail = activity.getUserEmail();

            Log.d(TAG, "UserId: " + userId);
            Log.d(TAG, "Name: " + userName);
            Log.d(TAG, "Email: " + userEmail);

            // 🔥 MOSTRAR DATOS BÁSICOS INMEDIATAMENTE
            createBasicProfileItems(userName, userEmail);

            // 🔥 CARGAR DATOS COMPLETOS DESDE FIREBASE
            if (userId != null && !userId.isEmpty()) {
                loadCompleteDataFromFirebase(userId);
            }
        } else {
            // Fallback: crear perfil básico
            createBasicProfileItems("Conductor", "conductor@email.com");
        }
    }

    // 🔥 CREAR ITEMS BÁSICOS DEL PERFIL (SIN CAMPOS QUE NO EXISTEN)
    private void createBasicProfileItems(String fullName, String email) {
        // Header con foto de perfil
        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.PROFILE_HEADER,
                "Foto de Perfil",
                "", // Se actualizará desde Firebase
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
                extractFirstName(fullName),
                "first_name",
                false,
                R.drawable.ic_person
        ));

        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Apellidos",
                extractLastName(fullName),
                "last_name",
                false,
                R.drawable.ic_person
        ));

        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Correo Electrónico",
                email,
                "email",
                false,
                R.drawable.ic_email
        ));

        // Información de contacto editable
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
                "Cargando...", // Se actualizará desde Firebase
                "phone",
                true,
                R.drawable.ic_phone
        ));

        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.EDITABLE_FIELD,
                "Dirección",
                "Cargando...", // Se actualizará desde Firebase
                "address",
                true,
                R.drawable.ic_location
        ));

        // Información del documento
        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.SECTION_HEADER,
                "Información del Documento",
                "",
                "",
                false,
                0
        ));

        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Tipo de Documento",
                "Cargando...", // Se actualizará desde Firebase
                "document_type",
                false,
                R.drawable.ic_document
        ));

        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Número de Documento",
                "Cargando...", // Se actualizará desde Firebase
                "document_number",
                false,
                R.drawable.ic_document
        ));

        profileItems.add(new EditProfileItem(
                EditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Fecha de Nacimiento",
                "Cargando...", // Se actualizará desde Firebase
                "birth_date",
                false,
                R.drawable.ic_calendar
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
                EditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Placa del Vehículo",
                "Cargando...", // Se actualizará desde Firebase
                "license_plate",
                false,
                R.drawable.ic_taxi
        ));

        // 🚫 ELIMINAMOS: "Foto del Auto" y "Modelo del Auto" porque no existen en la BD

        adapter.notifyDataSetChanged();
    }

    // 🔥 CARGAR DATOS COMPLETOS DESDE FIREBASE
    private void loadCompleteDataFromFirebase(String userId) {
        Log.d(TAG, "🔄 Obteniendo datos completos desde Firebase");

        com.example.proyecto_final_hoteleros.utils.FirebaseManager firebaseManager =
                com.example.proyecto_final_hoteleros.utils.FirebaseManager.getInstance();

        firebaseManager.getUserDataFromAnyCollection(userId, new com.example.proyecto_final_hoteleros.utils.FirebaseManager.UserCallback() {
            @Override
            public void onUserFound(com.example.proyecto_final_hoteleros.models.UserModel user) {
                Log.d(TAG, "✅ Datos completos obtenidos desde Firebase");
                Log.d(TAG, "Teléfono: " + user.getTelefono());
                Log.d(TAG, "Dirección: " + user.getDireccion());
                Log.d(TAG, "Documento: " + user.getNumeroDocumento());
                Log.d(TAG, "Placa: " + user.getPlacaVehiculo());

                // 🔥 ACTUALIZAR UI EN EL HILO PRINCIPAL
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        updateProfileItemsWithRealData(user);
                    });
                }
            }

            @Override
            public void onUserNotFound() {
                Log.w(TAG, "⚠️ Usuario no encontrado en Firebase");
                if (getContext() != null && isAdded()) {
                    Toast.makeText(getContext(), "No se pudieron cargar todos los datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando datos completos: " + error);
                if (getContext() != null && isAdded()) {
                    Toast.makeText(getContext(), "Error cargando datos: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 🔥 ACTUALIZAR ITEMS CON DATOS REALES DE FIREBASE
    private void updateProfileItemsWithRealData(com.example.proyecto_final_hoteleros.models.UserModel user) {
        Log.d(TAG, "🔄 Actualizando items con datos reales");

        for (int i = 0; i < profileItems.size(); i++) {
            EditProfileItem item = profileItems.get(i);
            boolean needsUpdate = false;

            switch (item.getKey()) {
                case "profile_image":
                    if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                        item.setValue(user.getPhotoUrl());
                        needsUpdate = true;
                    }
                    break;

                case "phone":
                    item.setValue(user.getTelefono() != null ? user.getTelefono() : "No especificado");
                    needsUpdate = true;
                    break;

                case "address":
                    item.setValue(user.getDireccion() != null ? user.getDireccion() : "No especificado");
                    needsUpdate = true;
                    break;

                case "document_type":
                    item.setValue(user.getTipoDocumento() != null ? user.getTipoDocumento() : "DNI");
                    needsUpdate = true;
                    break;

                case "document_number":
                    item.setValue(user.getNumeroDocumento() != null ? user.getNumeroDocumento() : "No especificado");
                    needsUpdate = true;
                    break;

                case "birth_date":
                    item.setValue(user.getFechaNacimiento() != null ? user.getFechaNacimiento() : "No especificado");
                    needsUpdate = true;
                    break;

                case "license_plate":
                    item.setValue(user.getPlacaVehiculo() != null ? user.getPlacaVehiculo() : "No especificado");
                    needsUpdate = true;
                    break;
            }

            if (needsUpdate) {
                adapter.notifyItemChanged(i);
            }
        }

        Log.d(TAG, "✅ Items actualizados con datos reales");
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
        Log.d(TAG, "💾 Guardando perfil...");

        String newPhone = "";
        String newAddress = "";
        String newProfileImageUrl = "";
        boolean hasChanges = false;

        // Recopilar cambios de los campos editables
        for (EditProfileItem item : profileItems) {
            if (item.getType() == EditProfileItem.EditItemType.EDITABLE_FIELD) {
                switch (item.getKey()) {
                    case "phone":
                        newPhone = item.getValue().trim();
                        break;
                    case "address":
                        newAddress = item.getValue().trim();
                        break;
                }
            } else if (item.getType() == EditProfileItem.EditItemType.PROFILE_HEADER) {
                // Solo si se seleccionó una nueva imagen
                if (selectedProfileImageUri != null) {
                    newProfileImageUrl = selectedProfileImageUri.toString();
                    hasChanges = true;
                }
            }
        }

        // Validar datos obligatorios
        if (newPhone.isEmpty()) {
            Toast.makeText(getContext(), "El teléfono es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newAddress.isEmpty()) {
            Toast.makeText(getContext(), "La dirección es obligatoria", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si hay cambios
        if (!hasChanges && newPhone.equals("Cargando...") && newAddress.equals("Cargando...")) {
            Toast.makeText(getContext(), "No hay cambios para guardar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar indicador de carga
        btnSave.setEnabled(false);
        btnSave.setText("Guardando...");

        // 🔥 OBTENER userId DEL ACTIVITY
        if (getActivity() instanceof com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity) {
            com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity activity =
                    (com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity) getActivity();

            String userId = activity.getUserId();

            if (userId != null && !userId.isEmpty()) {
                // 🔥 SI HAY NUEVA FOTO, SUBIRLA PRIMERO
                if (selectedProfileImageUri != null) {
                    uploadNewProfilePhoto(userId, newPhone, newAddress);
                } else {
                    // 🔥 ACTUALIZAR SOLO TELÉFONO Y DIRECCIÓN
                    updateProfileInFirebase(userId, newPhone, newAddress, null);
                }
            } else {
                showError("Error: No se pudo obtener ID de usuario");
            }
        } else {
            showError("Error: No se pudo acceder a los datos de usuario");
        }
    }

    // 🔥 SUBIR NUEVA FOTO DE PERFIL A AWS S3 (IMPLEMENTACIÓN REAL)
    private void uploadNewProfilePhoto(String userId, String newPhone, String newAddress) {
        Log.d(TAG, "📸 Subiendo nueva foto de perfil a AWS S3...");

        try {
            // Crear instancia del AwsFileManager
            com.example.proyecto_final_hoteleros.utils.AwsFileManager awsManager =
                    new com.example.proyecto_final_hoteleros.utils.AwsFileManager(requireContext());

            // Generar nombre único para la foto
            String fileName = "profile_" + userId + "_" + System.currentTimeMillis() + ".jpg";
            String folder = "photos"; // Carpeta en S3 para fotos de perfil

            Log.d(TAG, "📤 Subiendo archivo: " + fileName);
            Log.d(TAG, "📁 Carpeta S3: " + folder);
            Log.d(TAG, "👤 UserId: " + userId);

            // Subir archivo a AWS S3
            awsManager.uploadFile(selectedProfileImageUri, userId, folder,
                    new com.example.proyecto_final_hoteleros.utils.AwsFileManager.UploadCallback() {
                        @Override
                        public void onSuccess(com.example.proyecto_final_hoteleros.utils.AwsFileManager.AwsFileInfo fileInfo) {
                            Log.d(TAG, "✅ Foto subida exitosamente a AWS S3");
                            Log.d(TAG, "🔗 URL generada: " + fileInfo.fileUrl);
                            Log.d(TAG, "🗂️ S3 Key: " + fileInfo.s3Key);
                            Log.d(TAG, "📊 Tamaño: " + fileInfo.fileSizeMB + " MB");

                            // Continuar con la actualización en Firebase usando la URL real
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    updateProfileInFirebase(userId, newPhone, newAddress, fileInfo.fileUrl);
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "❌ Error subiendo foto a AWS: " + error);

                            if (getActivity() != null && isAdded()) {
                                getActivity().runOnUiThread(() -> {
                                    // Mostrar error específico
                                    showError("Error subiendo foto: " + error);

                                    // Preguntar si quiere continuar sin cambiar la foto
                                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                            .setTitle("Error subiendo foto")
                                            .setMessage("No se pudo subir la nueva foto. ¿Deseas guardar solo los cambios de teléfono y dirección?")
                                            .setPositiveButton("Sí, guardar", (dialog, which) -> {
                                                // Continuar sin cambiar la foto
                                                updateProfileInFirebase(userId, newPhone, newAddress, null);
                                            })
                                            .setNegativeButton("Cancelar", (dialog, which) -> {
                                                // Restaurar botón
                                                btnSave.setEnabled(true);
                                                btnSave.setText("Guardar Cambios");
                                            })
                                            .show();
                                });
                            }
                        }

                        @Override
                        public void onProgress(int percentage) {
                            Log.d(TAG, "📊 Progreso subida: " + percentage + "%");

                            // Opcional: Actualizar UI con progreso
                            if (getActivity() != null && isAdded()) {
                                getActivity().runOnUiThread(() -> {
                                    btnSave.setText("Subiendo... " + percentage + "%");
                                });
                            }
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error inicializando subida a AWS: " + e.getMessage());
            showError("Error inicializando subida: " + e.getMessage());
        }
    }

    // 🔥 ACTUALIZAR PERFIL EN FIREBASE
    private void updateProfileInFirebase(String userId, String newPhone, String newAddress, String newPhotoUrl) {
        Log.d(TAG, "🔥 Actualizando perfil en Firebase...");

        com.example.proyecto_final_hoteleros.utils.FirebaseManager firebaseManager =
                com.example.proyecto_final_hoteleros.utils.FirebaseManager.getInstance();

        firebaseManager.updateDriverProfile(userId, newPhone, newAddress, newPhotoUrl,
                new com.example.proyecto_final_hoteleros.utils.FirebaseManager.DataCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Perfil actualizado exitosamente en Firebase");

                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                // Restaurar botón
                                btnSave.setEnabled(true);
                                btnSave.setText("Guardar Cambios");

                                Toast.makeText(getContext(), "✅ Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();

                                // 🔥 ACTUALIZAR DATOS EN TIEMPO REAL
                                updateLocalProfileData(newPhone, newAddress, newPhotoUrl);

                                // Regresar a la pantalla anterior después de un momento
                                new android.os.Handler().postDelayed(() -> {
                                    if (getActivity() != null) {
                                        getActivity().getSupportFragmentManager().popBackStack();
                                    }
                                }, 1500);
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "❌ Error actualizando perfil: " + error);

                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                // Restaurar botón
                                btnSave.setEnabled(true);
                                btnSave.setText("Guardar Cambios");

                                showError("Error actualizando perfil: " + error);
                            });
                        }
                    }
                });
    }

    // 🔥 ACTUALIZAR DATOS LOCALES PARA REFLEJAR CAMBIOS INMEDIATAMENTE
    private void updateLocalProfileData(String newPhone, String newAddress, String newPhotoUrl) {
        Log.d(TAG, "🔄 Actualizando datos locales...");

        // Actualizar items en la lista actual
        for (int i = 0; i < profileItems.size(); i++) {
            EditProfileItem item = profileItems.get(i);
            boolean needsUpdate = false;

            switch (item.getKey()) {
                case "phone":
                    if (!newPhone.equals(item.getValue())) {
                        item.setValue(newPhone);
                        needsUpdate = true;
                    }
                    break;

                case "address":
                    if (!newAddress.equals(item.getValue())) {
                        item.setValue(newAddress);
                        needsUpdate = true;
                    }
                    break;

                case "profile_image":
                    if (newPhotoUrl != null && !newPhotoUrl.equals(item.getValue())) {
                        item.setValue(newPhotoUrl);
                        needsUpdate = true;
                    }
                    break;
            }

            if (needsUpdate) {
                adapter.notifyItemChanged(i);
            }
        }

        // 🔥 ACTUALIZAR OTROS FRAGMENTS QUE PUEDAN ESTAR ABIERTOS
        notifyOtherFragmentsOfProfileUpdate(newPhone, newAddress, newPhotoUrl);
    }

    // 🔥 NOTIFICAR A OTROS FRAGMENTS SOBRE LA ACTUALIZACIÓN
    private void notifyOtherFragmentsOfProfileUpdate(String newPhone, String newAddress, String newPhotoUrl) {
        if (getActivity() != null) {
            // Buscar DriverPerfilFragment y actualizarlo
            androidx.fragment.app.Fragment perfilFragment = getActivity().getSupportFragmentManager()
                    .findFragmentByTag("PERFIL");

            if (perfilFragment instanceof com.example.proyecto_final_hoteleros.taxista.fragment.DriverPerfilFragment) {
                // Actualizar DriverPerfilFragment si está abierto
                ((com.example.proyecto_final_hoteleros.taxista.fragment.DriverPerfilFragment) perfilFragment)
                        .updateProfileData(extractFirstName(newPhone) + " " + extractLastName(newAddress),
                                "Toyota Rush 2023", newPhotoUrl);
            }
        }
    }

    // 🔥 MÉTODO HELPER PARA MOSTRAR ERRORES
    private void showError(String message) {
        if (getContext() != null && isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }

        // Restaurar botón en caso de error
        if (btnSave != null) {
            btnSave.setEnabled(true);
            btnSave.setText("Guardar Cambios");
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

    // 🔥 MÉTODO HELPER PARA OBTENER BITMAP DESDE URI (SI ES NECESARIO)
    private void uploadProfileImageAsBitmap(String userId, String newPhone, String newAddress) {
        Log.d(TAG, "📸 Convirtiendo URI a Bitmap para subida...");

        try {
            // Convertir URI a Bitmap
            android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(
                    requireContext().getContentResolver(), selectedProfileImageUri);

            // Redimensionar si es muy grande (máximo 1024x1024)
            if (bitmap.getWidth() > 1024 || bitmap.getHeight() > 1024) {
                float scale = Math.min(1024f / bitmap.getWidth(), 1024f / bitmap.getHeight());
                int newWidth = (int) (bitmap.getWidth() * scale);
                int newHeight = (int) (bitmap.getHeight() * scale);

                bitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                Log.d(TAG, "🔄 Imagen redimensionada a: " + newWidth + "x" + newHeight);
            }

            // Crear AwsFileManager
            com.example.proyecto_final_hoteleros.utils.AwsFileManager awsManager =
                    new com.example.proyecto_final_hoteleros.utils.AwsFileManager(requireContext());

            // Generar nombre único
            String fileName = "profile_" + userId + "_" + System.currentTimeMillis() + ".jpg";

            // Subir como Bitmap
            awsManager.uploadImage(bitmap, fileName, userId, "photos",
                    new com.example.proyecto_final_hoteleros.utils.AwsFileManager.UploadCallback() {
                        @Override
                        public void onSuccess(com.example.proyecto_final_hoteleros.utils.AwsFileManager.AwsFileInfo fileInfo) {
                            Log.d(TAG, "✅ Bitmap subido exitosamente: " + fileInfo.fileUrl);

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    updateProfileInFirebase(userId, newPhone, newAddress, fileInfo.fileUrl);
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "❌ Error subiendo bitmap: " + error);
                            if (getActivity() != null && isAdded()) {
                                getActivity().runOnUiThread(() -> {
                                    showError("Error subiendo imagen: " + error);
                                });
                            }
                        }

                        @Override
                        public void onProgress(int percentage) {
                            Log.d(TAG, "📊 Progreso: " + percentage + "%");
                            if (getActivity() != null && isAdded()) {
                                getActivity().runOnUiThread(() -> {
                                    btnSave.setText("Subiendo... " + percentage + "%");
                                });
                            }
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error convirtiendo a bitmap: " + e.getMessage());
            showError("Error procesando imagen: " + e.getMessage());
        }
    }
}