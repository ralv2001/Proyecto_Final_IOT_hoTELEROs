package com.example.proyecto_final_hoteleros.auth.register;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddProfilePhotoActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    // Constantes para guardar el estado
    private static final String KEY_IS_PHOTO_SELECTED = "is_photo_selected";
    private static final String KEY_PHOTO_URI = "photo_uri";
    private static final String KEY_TEMP_PHOTO_PATH = "temp_photo_path";

    private Bitmap savedImageBitmap; // Para guardar la bitmap de la imagen
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
    private String tempPhotoPath; // Para guardar la ruta del archivo temporal

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
            String uriString = savedInstanceState.getString(KEY_PHOTO_URI);
            tempPhotoPath = savedInstanceState.getString(KEY_TEMP_PHOTO_PATH);

            if (isPhotoSelected) {
                if (uriString != null && !uriString.isEmpty()) {
                    profilePhotoUri = Uri.parse(uriString);
                    loadImageFromUri(profilePhotoUri);
                } else if (tempPhotoPath != null && !tempPhotoPath.isEmpty()) {
                    File tempFile = new File(tempPhotoPath);
                    if (tempFile.exists()) {
                        profilePhotoUri = Uri.fromFile(tempFile);
                        loadImageFromUri(profilePhotoUri);
                    }
                }
            }
        } else {
            // Verificar si hay datos en el ViewModel
            if (mViewModel.hasProfilePhoto() && mViewModel.getProfilePhotoUri() != null) {
                profilePhotoUri = mViewModel.getProfilePhotoUri();
                isPhotoSelected = true;
                loadImageFromUri(profilePhotoUri);
            } else {
                // Intentar recuperar de SharedPreferences
                String savedPhotoPath = getSharedPreferences("UserData", MODE_PRIVATE)
                        .getString("photoPath", "");
                String savedPhotoUri = getSharedPreferences("UserData", MODE_PRIVATE)
                        .getString("photoUri", "");

                if (!savedPhotoPath.isEmpty()) {
                    File tempFile = new File(savedPhotoPath);
                    if (tempFile.exists()) {
                        tempPhotoPath = savedPhotoPath;
                        profilePhotoUri = Uri.fromFile(tempFile);
                        isPhotoSelected = true;
                        loadImageFromUri(profilePhotoUri);
                    }
                } else if (!savedPhotoUri.isEmpty()) {
                    profilePhotoUri = Uri.parse(savedPhotoUri);
                    isPhotoSelected = true;
                    loadImageFromUri(profilePhotoUri);
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

    private void loadImageFromUri(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            displaySelectedImage(null, bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
        }
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
                    savedImageBitmap = imageBitmap; // Guardamos la referencia

                    // Guardar la imagen en un archivo temporal
                    try {
                        File tempDir = getCacheDir();
                        File tempFile = File.createTempFile("profile_photo", ".jpg", tempDir);
                        FileOutputStream fos = new FileOutputStream(tempFile);
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.close();

                        // Guardar la ruta para restaurarla después
                        tempPhotoPath = tempFile.getAbsolutePath();
                        profilePhotoUri = Uri.fromFile(tempFile);

                        // Guardar la ruta en SharedPreferences
                        getSharedPreferences("UserData", MODE_PRIVATE)
                                .edit()
                                .putString("photoPath", tempPhotoPath)
                                .apply();

                        // Mostrar la imagen
                        displaySelectedImage(null, imageBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                // Foto elegida de la galería
                Uri selectedImageUri = data.getData();
                profilePhotoUri = selectedImageUri;

                // Guardar la URI en SharedPreferences como String
                if (selectedImageUri != null) {
                    getSharedPreferences("UserData", MODE_PRIVATE)
                            .edit()
                            .putString("photoUri", selectedImageUri.toString())
                            .apply();
                }

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    savedImageBitmap = bitmap; // Guardamos la referencia
                    displaySelectedImage(null, bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void displaySelectedImage(Uri imageUri, Bitmap imageBitmap) {
        // Mostrar la imagen seleccionada y actualizar la UI
        ivProfilePhoto.setVisibility(View.VISIBLE);
        ivCameraIcon.setVisibility(View.GONE);
        ivCircleOutline.setVisibility(View.GONE);

        if (imageBitmap != null) {
            ivProfilePhoto.setImageBitmap(imageBitmap);
            savedImageBitmap = imageBitmap; // Guardar la bitmap
        } else if (imageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ivProfilePhoto.setImageBitmap(bitmap);
                savedImageBitmap = bitmap; // Guardar la bitmap
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                return;
            }
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

        // Intenta obtener el email del ViewModel primero
        String email = mViewModel.getEmail();

        // Si el email es null o vacío, intenta obtenerlo de SharedPreferences
        if (email == null || email.isEmpty()) {
            email = getSharedPreferences("UserData", MODE_PRIVATE)
                    .getString("email", "");
            Log.d("AddProfilePhoto", "Email recuperado de SharedPreferences: " + email);
        }

        // Log para depuración
        Log.d("AddProfilePhoto", "Email en completeRegistration: " + email);

        // Si todavía es null o vacío, usa uno de prueba (solo para desarrollo)
        if (email == null || email.isEmpty()) {
            email = "test@example.com";
            Log.e("AddProfilePhoto", "Usando email por defecto: " + email);
        }

        // Crear un intent específico para esta actividad
        Intent intent = new Intent(this, RegisterVerifyActivity.class);
        intent.putExtra("email", email);

        // Hacer log del email que enviamos
        Log.d("AddProfilePhoto", "Enviando email: " + email);

        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_PHOTO_SELECTED, isPhotoSelected);

        if (profilePhotoUri != null) {
            outState.putString(KEY_PHOTO_URI, profilePhotoUri.toString());
        }

        if (tempPhotoPath != null) {
            outState.putString(KEY_TEMP_PHOTO_PATH, tempPhotoPath);
        }
    }
}