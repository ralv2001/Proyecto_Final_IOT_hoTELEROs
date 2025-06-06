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
import androidx.annotation.NonNull;
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
        View view = inflater.inflate(R.layout.fragment_edit_client_profile, container, false);

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
        // Obtener datos del cliente actual
        if (currentClient == null) {
            currentClient = getCurrentClientData();
        }

        // Limpiar items existentes
        profileItems.clear();

        // Agregar header con foto de perfil
        profileItems.add(new ClientEditProfileItem(
                ClientEditProfileItem.EditItemType.PROFILE_HEADER,
                "Foto de Perfil",
                currentClient.getProfileImageUrl(),
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
                extractFirstName(currentClient.getFullName()),
                "first_name",
                false,
                R.drawable.ic_group
        ));

        profileItems.add(new ClientEditProfileItem(
                ClientEditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Apellidos",
                extractLastName(currentClient.getFullName()),
                "last_name",
                false,
                R.drawable.ic_group
        ));

        profileItems.add(new ClientEditProfileItem(
                ClientEditProfileItem.EditItemType.READ_ONLY_FIELD,
                "ID de Cliente",
                currentClient.getClientId(),
                "client_id",
                false,
                R.drawable.ic_document
        ));

        profileItems.add(new ClientEditProfileItem(
                ClientEditProfileItem.EditItemType.READ_ONLY_FIELD,
                "Correo Electrónico",
                currentClient.getEmail(),
                "email",
                false,
                R.drawable.ic_email
        ));

        // Información editable
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
                currentClient.getPhoneNumber(),
                "phone",
                true,
                R.drawable.ic_phone
        ));

        profileItems.add(new ClientEditProfileItem(
                ClientEditProfileItem.EditItemType.EDITABLE_FIELD,
                "Dirección",
                currentClient.getAddress(),
                "address",
                true,
                R.drawable.ic_location
        ));

        adapter.notifyDataSetChanged();
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
        Log.d(TAG, "Guardando perfil...");

        String newPhone = "";
        String newAddress = "";
        String newProfileImageUrl = currentClient.getProfileImageUrl();

        // Recopilar cambios de los campos editables
        for (ClientEditProfileItem item : profileItems) {
            if (item.getType() == ClientEditProfileItem.EditItemType.EDITABLE_FIELD) {
                switch (item.getKey()) {
                    case "phone":
                        newPhone = item.getValue();
                        break;
                    case "address":
                        newAddress = item.getValue();
                        break;
                }
            }
        }

        // Obtener URL de imagen si fue cambiada
        if (selectedProfileImageUri != null) {
            newProfileImageUrl = selectedProfileImageUri.toString();
        }

        // Validar datos obligatorios
        if (newPhone.trim().isEmpty()) {
            Toast.makeText(getContext(), "El teléfono es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newAddress.trim().isEmpty()) {
            Toast.makeText(getContext(), "La dirección es obligatoria", Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizar el perfil del cliente
        currentClient.setPhoneNumber(newPhone);
        currentClient.setAddress(newAddress);
        currentClient.setProfileImageUrl(newProfileImageUrl);

        // Actualizar UserDataManager si es necesario
        UserDataManager.getInstance().updateUserData(
                currentClient.getClientId(),
                extractFirstName(currentClient.getFullName()),
                currentClient.getFullName(),
                currentClient.getEmail(),
                "client"
        );

        Toast.makeText(getContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();

        // Regresar a la pantalla anterior
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
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