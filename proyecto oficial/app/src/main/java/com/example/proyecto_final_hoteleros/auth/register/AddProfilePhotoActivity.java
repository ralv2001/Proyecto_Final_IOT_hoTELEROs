package com.example.proyecto_final_hoteleros.auth.register;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;

public class AddProfilePhotoActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    // Constantes para guardar el estado
    private static final String KEY_IS_PHOTO_SELECTED = "is_photo_selected";
    private static final String KEY_PHOTO_URI = "photo_uri";
    private static final String KEY_PHOTO_BITMAP = "photo_bitmap";

    private RegisterViewModel mViewModel;
    private ImageView ivProfilePhoto;
    private ImageView ivCameraIcon;
    private ImageView ivCircleOutline;
    private Button btnAddPhoto;
    private MaterialButton btnContinuar;
    private MaterialButton btnOmitir;
    private Uri profilePhotoUri;
    private boolean isPhotoSelected = false;
    private String userType;
    private Bitmap savedImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sistema_add_profile_photo);

        // Obtener el userType del intent
        if (getIntent() != null && getIntent().hasExtra("userType")) {
            userType = getIntent().getStringExtra("userType");
        } else {
            userType = "client"; // valor por defecto
        }

        // Inicializar el ViewModel
        mViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // Inicializar vistas
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        ivCameraIcon = findViewById(R.id.ivCameraIcon);
        ivCircleOutline = findViewById(R.id.ivCircleOutline);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnContinuar = findViewById(R.id.btnContinuar);
        btnOmitir = findViewById(R.id.btnOmitir);
        ImageButton btnBack = findViewById(R.id.btnBack);

        // Si el usuario es taxista, deshabilitar el botón de omitir
        if ("driver".equals(userType)) {
            btnOmitir.setEnabled(false);
            btnOmitir.setAlpha(0.4f);
        }

        // Restaurar el estado si existe
        if (savedInstanceState != null) {
            isPhotoSelected = savedInstanceState.getBoolean(KEY_IS_PHOTO_SELECTED, false);

            if (isPhotoSelected) {
                String uriString = savedInstanceState.getString(KEY_PHOTO_URI);
                if (uriString != null && !uriString.isEmpty()) {
                    profilePhotoUri = Uri.parse(uriString);
                    displaySelectedImage(profilePhotoUri, null);
                } else if (savedInstanceState.containsKey(KEY_PHOTO_BITMAP)) {
                    // Restaurar la imagen capturada por la cámara
                    savedImageBitmap = savedInstanceState.getParcelable(KEY_PHOTO_BITMAP);
                    if (savedImageBitmap != null) {
                        displaySelectedImage(null, savedImageBitmap);
                    }
                }
            }
        }

        // Configurar listeners
        btnAddPhoto.setOnClickListener(v -> showImagePickOptions());

        btnContinuar.setOnClickListener(v -> {
            if (isPhotoSelected || "client".equals(userType)) {
                // Si una foto fue seleccionada o es un cliente (opcional)
                completeRegistration();
            } else {
                Toast.makeText(this, "Por favor selecciona una foto de perfil", Toast.LENGTH_SHORT).show();
            }
        });

        btnOmitir.setOnClickListener(v -> {
            if ("client".equals(userType)) {
                // Solo los clientes pueden omitir la foto
                completeRegistration();
            }
        });

        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void showImagePickOptions() {
        String[] options = {"Tomar una foto", "Elegir de la galería", "Cancelar"};

        new android.app.AlertDialog.Builder(this)
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
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No hay aplicación de cámara disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ivProfilePhoto.setImageBitmap(bitmap);
                this.savedImageBitmap = bitmap; // Guardar la bitmap
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (imageBitmap != null) {
            ivProfilePhoto.setImageBitmap(imageBitmap);
            this.savedImageBitmap = imageBitmap; // Guardar la bitmap
        }

        isPhotoSelected = true;
        btnAddPhoto.setText("Cambiar");
    }

    private void completeRegistration() {
        // Guardar la URI de la foto en el ViewModel si se seleccionó una
        if (isPhotoSelected) {
            if (profilePhotoUri != null) {
                mViewModel.setProfilePhotoUri(profilePhotoUri);
            }
            mViewModel.setHasProfilePhoto(true);
        }

        // Navegar a la pantalla principal o mostrar mensaje de éxito
        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();

        // Aquí deberías navegar a la pantalla principal o home
        // Intent intent = new Intent(this, HomeActivity.class);
        // startActivity(intent);
        // finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_PHOTO_SELECTED, isPhotoSelected);

        if (profilePhotoUri != null) {
            outState.putString(KEY_PHOTO_URI, profilePhotoUri.toString());
        }

        if (savedImageBitmap != null) {
            outState.putParcelable(KEY_PHOTO_BITMAP, savedImageBitmap);
        }
    }
}