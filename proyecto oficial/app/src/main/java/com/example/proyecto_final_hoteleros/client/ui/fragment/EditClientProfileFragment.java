package com.example.proyecto_final_hoteleros.client.ui.fragment;

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
import com.example.proyecto_final_hoteleros.client.ui.adapters.EditClientProfileAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.ClientEditProfileItem;
import com.example.proyecto_final_hoteleros.client.data.model.ClientProfile;
import com.example.proyecto_final_hoteleros.client.utils.UserDataManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class EditClientProfileFragment extends Fragment implements
        EditClientProfileAdapter.OnEditClientProfileListener {

    private static final String TAG = "EditClientProfileFragment";

    private RecyclerView recyclerView;
    private EditClientProfileAdapter adapter;
    private List<ClientEditProfileItem> profileItems;
    private MaterialButton btnSave;
    private MaterialToolbar toolbar;

    private ClientProfile currentClient;
    private Uri selectedProfileImageUri;

    // Launcher para seleccionar imagen
    private ActivityResultLauncher<Intent> profileImageLauncher;

    public EditClientProfileFragment() {
        // Constructor vacío requerido
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar launcher para selección de imagen
        profileImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedProfileImageUri = result.getData().getData();
                        updateProfileImage();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.client_fragment_edit_profile, container, false);

        // Recibir el perfil del cliente si fue pasado como argumento
        if (getArguments() != null && getArguments().containsKey("client_profile")) {
            currentClient = getArguments().getParcelable("client_profile");
        }

        initViews(view);
        setupToolbar();
        setupRecyclerView();
        loadClientData();

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
        adapter = new EditClientProfileAdapter(getContext(), profileItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadClientData() {
        Log.d(TAG, "🔄 Cargando datos reales del cliente para editar perfil");

        // Limpiar items existentes
        profileItems.clear();

        // 🔥 CARGAR DATOS REALES DESDE EL ACTIVITY
        if (getActivity() instanceof com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity) {
            com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity activity =
                    (com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity) getActivity();

            String userId = activity.getUserId();
            String userFullName = activity.getUserFullName();
            String userEmail = activity.getUserEmail();

            Log.d(TAG, "UserId: " + userId);
            Log.d(TAG, "Name: " + userFullName);
            Log.d(TAG, "Email: " + userEmail);

            // 🔥 MOSTRAR DATOS BÁSICOS INMEDIATAMENTE
            createBasicProfileItems(userFullName, userEmail);

            // 🔥 CARGAR DATOS COMPLETOS DESDE FIREBASE
            if (userId != null && !userId.isEmpty()) {
                loadCompleteDataFromFirebase(userId);
            }
        } else {
            // Fallback: crear perfil básico
            createBasicProfileItems("Cliente", "cliente@email.com");
        }
    }

    // 🔥 CREAR ITEMS BÁSICOS DEL PERFIL (SIN CAMPOS QUE NO EXISTEN PARA CLIENTES)
    private void createBasicProfileItems(String fullName, String email) {
        // Header con foto de perfil
        profileItems.add(new ClientEditProfileItem(
                ClientEditProfileItem.EditItemType.PROFILE_HEADER,
                "Foto de Perfil",
                "", // Se actualizará desde Firebase
                "profile_image",
                true,
                R.drawable.ic_camera
        ));

        // Información personal (solo lectura)
        profileItems.add(new ClientEditProfileItem(
                ClientEditProfileItem.EditItemType.SECTION_HEADER,
                "Información Personal",
                "",
                "",
                false,
                0
        ));

        profileItems.add(new ClientEditProfileItem(
                ClientEditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Nombres",
                extractFirstName(fullName),
                "first_name",
                false,
                R.drawable.ic_group
        ));

        profileItems.add(new ClientEditProfileItem(
                ClientEditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Apellidos",
                extractLastName(fullName),
                "last_name",
                false,
                R.drawable.ic_group
        ));

        profileItems.add(new ClientEditProfileItem(
                ClientEditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Correo Electrónico",
                email,
                "email",
                false,
                R.drawable.ic_email
        ));

        // Información de contacto editable
        profileItems.add(new ClientEditProfileItem(
                ClientEditProfileItem.EditItemType.SECTION_HEADER,
                "Información de Contacto",
                "",
                "",
                false,
                0
        ));

        profileItems.add(new ClientEditProfileItem(
                ClientEditProfileItem.EditItemType.EDITABLE_FIELD,
                "Teléfono",
                "Cargando...", // Se actualizará desde Firebase
                "phone",
                true,
                R.drawable.ic_phone
        ));

        profileItems.add(new ClientEditProfileItem(
                ClientEditProfileItem.EditItemType.EDITABLE_FIELD,
                "Dirección",
                "Cargando...", // Se actualizará desde Firebase
                "address",
                true,
                R.drawable.ic_location
        ));

        // Información del documento (solo lectura)
        profileItems.add(new ClientEditProfileItem(
                ClientEditProfileItem.EditItemType.SECTION_HEADER,
                "Información del Documento",
                "",
                "",
                false,
                0
        ));

        profileItems.add(new ClientEditProfileItem(
                ClientEditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Tipo de Documento",
                "Cargando...", // Se actualizará desde Firebase
                "document_type",
                false,
                R.drawable.ic_document
        ));

        profileItems.add(new ClientEditProfileItem(
                ClientEditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Número de Documento",
                "Cargando...", // Se actualizará desde Firebase
                "document_number",
                false,
                R.drawable.ic_document
        ));

        profileItems.add(new ClientEditProfileItem(
                ClientEditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Fecha de Nacimiento",
                "Cargando...", // Se actualizará desde Firebase
                "birth_date",
                false,
                R.drawable.ic_calendar
        ));

        // 🚫 NO AGREGAMOS información de vehículo (solo para taxistas)

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
                Log.d(TAG, "✅ Datos completos del cliente obtenidos desde Firebase");
                Log.d(TAG, "Teléfono: " + user.getTelefono());
                Log.d(TAG, "Dirección: " + user.getDireccion());
                Log.d(TAG, "Documento: " + user.getNumeroDocumento());
                Log.d(TAG, "Foto: " + user.getPhotoUrl());

                // 🔥 ACTUALIZAR UI EN EL HILO PRINCIPAL
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        updateProfileItemsWithRealData(user);
                    });
                }
            }

            @Override
            public void onUserNotFound() {
                Log.w(TAG, "⚠️ Cliente no encontrado en Firebase");
                if (getContext() != null && isAdded()) {
                    Toast.makeText(getContext(), "No se pudieron cargar todos los datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error cargando datos completos del cliente: " + error);
                if (getContext() != null && isAdded()) {
                    Toast.makeText(getContext(), "Error cargando datos: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 🔥 ACTUALIZAR ITEMS CON DATOS REALES DE FIREBASE
    private void updateProfileItemsWithRealData(com.example.proyecto_final_hoteleros.models.UserModel user) {
        Log.d(TAG, "🔄 Actualizando items del cliente con datos reales");

        for (int i = 0; i < profileItems.size(); i++) {
            ClientEditProfileItem item = profileItems.get(i);
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
            }

            if (needsUpdate) {
                adapter.notifyItemChanged(i);
            }
        }

        Log.d(TAG, "✅ Items del cliente actualizados con datos reales");
    }

    private ClientProfile getCurrentClientData() {
        UserDataManager userManager = UserDataManager.getInstance();

        return new ClientProfile(
                userManager.getUserId() != null ? userManager.getUserId() : "client001",
                userManager.getUserFullName() != null ? userManager.getUserFullName() : "Cliente Usuario",
                userManager.getUserEmail() != null ? userManager.getUserEmail() : "cliente@email.com",
                "+51 987 654 321",
                "https://via.placeholder.com/150",
                "Av. Lima 123, San Miguel, Lima",
                true,
                15, 12, 4.8f, 2580.50
        );
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
        }
    }

    private void updateProfileImage() {
        if (selectedProfileImageUri != null) {
            // Actualizar la imagen en el adapter
            for (int i = 0; i < profileItems.size(); i++) {
                ClientEditProfileItem item = profileItems.get(i);
                if (item.getType() == ClientEditProfileItem.EditItemType.PROFILE_HEADER) {
                    item.setValue(selectedProfileImageUri.toString());
                    adapter.notifyItemChanged(i);
                    break;
                }
            }
            Toast.makeText(getContext(), "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile() {
        Log.d(TAG, "💾 Guardando perfil del cliente...");

        String newPhone = "";
        String newAddress = "";
        boolean hasChanges = false;

        // Recopilar cambios de los campos editables
        for (ClientEditProfileItem item : profileItems) {
            if (item.getType() == ClientEditProfileItem.EditItemType.EDITABLE_FIELD) {
                switch (item.getKey()) {
                    case "phone":
                        newPhone = item.getValue().trim();
                        break;
                    case "address":
                        newAddress = item.getValue().trim();
                        break;
                }
            }
        }

        // Verificar si hay nueva foto
        if (selectedProfileImageUri != null) {
            hasChanges = true;
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

        // Mostrar indicador de carga
        btnSave.setEnabled(false);
        btnSave.setText("Guardando...");

        // 🔥 OBTENER userId DEL ACTIVITY
        if (getActivity() instanceof com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity) {
            com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity activity =
                    (com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity) getActivity();

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
        Log.d(TAG, "📸 Subiendo nueva foto de perfil del cliente a AWS S3...");

        try {
            // Crear instancia del AwsFileManager
            com.example.proyecto_final_hoteleros.utils.AwsFileManager awsManager =
                    new com.example.proyecto_final_hoteleros.utils.AwsFileManager(requireContext());

            // Generar nombre único para la foto
            String fileName = "client_profile_" + userId + "_" + System.currentTimeMillis() + ".jpg";
            String folder = "photos"; // Carpeta en S3 para fotos de perfil

            Log.d(TAG, "📤 Subiendo archivo: " + fileName);
            Log.d(TAG, "📁 Carpeta S3: " + folder);
            Log.d(TAG, "👤 UserId: " + userId);

            // Subir archivo a AWS S3
            awsManager.uploadFile(selectedProfileImageUri, userId, folder,
                    new com.example.proyecto_final_hoteleros.utils.AwsFileManager.UploadCallback() {
                        @Override
                        public void onSuccess(com.example.proyecto_final_hoteleros.utils.AwsFileManager.AwsFileInfo fileInfo) {
                            Log.d(TAG, "✅ Foto del cliente subida exitosamente a AWS S3");
                            Log.d(TAG, "🔗 URL generada: " + fileInfo.fileUrl);

                            // Continuar con la actualización en Firebase usando la URL real
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    updateProfileInFirebase(userId, newPhone, newAddress, fileInfo.fileUrl);
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "❌ Error subiendo foto del cliente a AWS: " + error);

                            if (getActivity() != null && isAdded()) {
                                getActivity().runOnUiThread(() -> {
                                    // Preguntar si quiere continuar sin cambiar la foto
                                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                            .setTitle("Error subiendo foto")
                                            .setMessage("No se pudo subir la nueva foto. ¿Deseas guardar solo los cambios de teléfono y dirección?")
                                            .setPositiveButton("Sí, guardar", (dialog, which) -> {
                                                updateProfileInFirebase(userId, newPhone, newAddress, null);
                                            })
                                            .setNegativeButton("Cancelar", (dialog, which) -> {
                                                btnSave.setEnabled(true);
                                                btnSave.setText("Guardar Cambios");
                                            })
                                            .show();
                                });
                            }
                        }

                        @Override
                        public void onProgress(int percentage) {
                            Log.d(TAG, "📊 Progreso subida cliente: " + percentage + "%");

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
        Log.d(TAG, "🔥 Actualizando perfil del cliente en Firebase...");

        com.example.proyecto_final_hoteleros.utils.FirebaseManager firebaseManager =
                com.example.proyecto_final_hoteleros.utils.FirebaseManager.getInstance();

        firebaseManager.updateClientProfile(userId, newPhone, newAddress, newPhotoUrl,
                new com.example.proyecto_final_hoteleros.utils.FirebaseManager.DataCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Perfil del cliente actualizado exitosamente en Firebase");

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
                        Log.e(TAG, "❌ Error actualizando perfil del cliente: " + error);

                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                showError("Error actualizando perfil: " + error);
                            });
                        }
                    }
                });
    }

    // 🔥 ACTUALIZAR DATOS LOCALES PARA REFLEJAR CAMBIOS INMEDIATAMENTE
    private void updateLocalProfileData(String newPhone, String newAddress, String newPhotoUrl) {
        Log.d(TAG, "🔄 Actualizando datos locales del cliente...");

        // Actualizar items en la lista actual
        for (int i = 0; i < profileItems.size(); i++) {
            ClientEditProfileItem item = profileItems.get(i);
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

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "EditClientProfileFragment resumed");
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