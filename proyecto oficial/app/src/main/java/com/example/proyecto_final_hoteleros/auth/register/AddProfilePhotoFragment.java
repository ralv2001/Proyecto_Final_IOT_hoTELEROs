package com.example.proyecto_final_hoteleros.auth.register;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;

public class AddProfilePhotoFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private RegisterViewModel mViewModel;
    private ImageView ivProfilePhoto;
    private ImageView ivPhotoPlaceholder;
    private ImageView ivCameraIcon;
    private ImageView ivCircleOutline;
    private Button btnAddPhoto;
    private MaterialButton btnContinuar;
    private MaterialButton btnOmitir;
    private Uri profilePhotoUri;
    private boolean isPhotoSelected = false;
    private String userType;

    public static AddProfilePhotoFragment newInstance(String userType) {
        AddProfilePhotoFragment fragment = new AddProfilePhotoFragment();
        Bundle args = new Bundle();
        args.putString("userType", userType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_profile_photo, container, false);

        // Obtener el tipo de usuario
        if (getArguments() != null) {
            userType = getArguments().getString("userType", "client");
        }

        // Inicializar vistas
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        ivCameraIcon = view.findViewById(R.id.ivCameraIcon);
        ivCircleOutline = view.findViewById(R.id.ivCircleOutline);
        btnAddPhoto = view.findViewById(R.id.btnAddPhoto);
        btnContinuar = view.findViewById(R.id.btnContinuar);
        btnOmitir = view.findViewById(R.id.btnOmitir);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        // Si el usuario es taxista, deshabilitar el botón de omitir
        if ("driver".equals(userType)) {
            btnOmitir.setEnabled(false);
            btnOmitir.setAlpha(0.4f);
        }

        // Configurar listeners
        btnAddPhoto.setOnClickListener(v -> showImagePickOptions());

        btnContinuar.setOnClickListener(v -> {
            if (isPhotoSelected || "client".equals(userType)) {
                // Si una foto fue seleccionada o es un cliente (opcional)
                completeRegistration();
            } else {
                Toast.makeText(getContext(), "Por favor selecciona una foto de perfil", Toast.LENGTH_SHORT).show();
            }
        });

        btnOmitir.setOnClickListener(v -> {
            if ("client".equals(userType)) {
                // Solo los clientes pueden omitir la foto
                completeRegistration();
            }
        });

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(RegisterViewModel.class);
    }

    private void showImagePickOptions() {
        String[] options = {"Tomar una foto", "Elegir de la galería", "Cancelar"};

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Seleccionar foto de perfil")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Tomar foto con la cámara
                        dispatchTakePictureIntent();
                    } else if (which == 1) {
                        // Elegir de la galería
                        openGallery();
                    }
                    // which == 2 es cancelar, no hacemos nada
                })
                .show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(getContext(), "No hay aplicación de cámara disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                // Foto tomada con la cámara
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    displaySelectedImage(null, imageBitmap);
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                // Foto elegida de la galería
                Uri selectedImageUri = data.getData();
                profilePhotoUri = selectedImageUri;
                displaySelectedImage(selectedImageUri, null);
            }
        }
    }

    private void displaySelectedImage(Uri imageUri, Bitmap imageBitmap) {
        // Mostrar la imagen seleccionada y actualizar la UI
        ivProfilePhoto.setVisibility(View.VISIBLE);
        ivCameraIcon.setVisibility(View.GONE);
        ivCircleOutline.setVisibility(View.GONE);

        if (imageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                ivProfilePhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (imageBitmap != null) {
            ivProfilePhoto.setImageBitmap(imageBitmap);
        }

        isPhotoSelected = true;
        btnAddPhoto.setText("Cambiar");
    }

    private void completeRegistration() {
        // Aquí enviarías la imagen a Firebase Storage y completarías el registro
        // Por ahora, simplemente mostramos un mensaje de éxito

        Toast.makeText(getContext(), "Registro exitoso", Toast.LENGTH_SHORT).show();

        // Navegar a la siguiente pantalla (por ejemplo, la pantalla principal)
        // TODO: Implementar navegación a la pantalla principal
    }
}