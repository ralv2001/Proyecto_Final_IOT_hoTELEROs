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
import android.widget.TextView;
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
        TextView tvDescription = findViewById(R.id.tvDescription);


        // Cambiar el texto de descripción según el tipo de usuario
        if ("driver".equals(userType)) {
            // Texto específico para taxistas enfatizando que es obligatorio
            tvDescription.setText("Para continuar con su registro, será necesario que añadas una foto de perfil");

            // Ocultar el botón de omitir para taxistas (en lugar de solo deshabilitarlo)
            btnOmitir.setVisibility(View.GONE);
        } else {
            // Mantener el texto original para clientes
            tvDescription.setText("¡Haz tu perfil más atractivo con una foto!");
        }

        // Verificar si se omitió la foto previamente
        boolean photoSkipped = getSharedPreferences("UserData", MODE_PRIVATE)
                .getBoolean("photoSkipped", false);

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

        // Si se había omitido la foto, resetear su estado
        if (photoSkipped) {
            resetPhotoState();
        }

        // Configurar listeners
        btnAddPhoto.setOnClickListener(v -> showImagePickOptions());

        btnContinuar.setOnClickListener(v -> {
            // Como el botón solo está habilitado cuando hay foto seleccionada, podemos simplificar
            completeRegistration();
        });

        btnOmitir.setOnClickListener(v -> {
            if ("client".equals(userType)) {
                // Marcar que se omitió la foto
                getSharedPreferences("UserData", MODE_PRIVATE)
                        .edit()
                        .putBoolean("photoSkipped", true)
                        .apply();

                // Limpiar la foto si existe (aquí sí queremos limpiar todo)
                resetPhotoState(true);

                // Solo los clientes pueden omitir la foto
                completeRegistration();
            }
        });

        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });

        // Al final del método onCreate(), después de configurar todos los listeners
        updateContinueButtonState();
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

    // Método para resetear el estado de la foto
    private void resetPhotoState() {
        resetPhotoState(true); // Llamada por defecto que limpia todo
    }

    private void resetPhotoState(boolean clearSharedPreferences) {
        profilePhotoUri = null;
        savedImageBitmap = null;
        isPhotoSelected = false;

        // Resetear la UI
        ivProfilePhoto.setVisibility(View.GONE);
        ivCameraIcon.setVisibility(View.VISIBLE);
        ivCircleOutline.setVisibility(View.VISIBLE);
        btnAddPhoto.setText("Añadir");

        // Solo limpiar SharedPreferences si se especifica
        if (clearSharedPreferences) {
            getSharedPreferences("UserData", MODE_PRIVATE)
                    .edit()
                    .remove("photoPath")
                    .remove("photoUri")
                    .apply();
        }

        // Actualizar el estado del botón continuar
        updateContinueButtonState();
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
                if (selectedImageUri != null) {
                    try {
                        // Convertir la imagen de galería a un archivo temporal (como con la cámara)
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                        savedImageBitmap = bitmap; // Guardamos la referencia

                        // Guardar la imagen en un archivo temporal
                        File tempDir = getCacheDir();
                        File tempFile = File.createTempFile("profile_photo_gallery", ".jpg", tempDir);
                        FileOutputStream fos = new FileOutputStream(tempFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                        fos.close();

                        // Usar el archivo temporal en lugar del URI de la galería
                        tempPhotoPath = tempFile.getAbsolutePath();
                        profilePhotoUri = Uri.fromFile(tempFile);

                        // Guardar la ruta en SharedPreferences
                        getSharedPreferences("UserData", MODE_PRIVATE)
                                .edit()
                                .putString("photoPath", tempPhotoPath)
                                .apply();

                        // Mostrar la imagen
                        displaySelectedImage(null, bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar la imagen de la galería", Toast.LENGTH_SHORT).show();
                    }
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

        // Si se había omitido la foto antes, ahora ya no
        getSharedPreferences("UserData", MODE_PRIVATE)
                .edit()
                .putBoolean("photoSkipped", false)
                .apply();

        // Actualizar el estado del botón continuar
        updateContinueButtonState();
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
        Log.d("AddProfilePhoto", "UserType en completeRegistration: " + userType);

        // Si todavía es null o vacío, usa uno de prueba (solo para desarrollo)
        if (email == null || email.isEmpty()) {
            email = "test@example.com";
            Log.e("AddProfilePhoto", "Usando email por defecto: " + email);
        }

        // Guardar userType en SharedPreferences para asegurar persistencia
        getSharedPreferences("UserData", MODE_PRIVATE)
                .edit()
                .putString("userType", userType)
                .apply();

        // Mostrar Toast con el mensaje de envío de código
        Toast.makeText(this, "Código de verificación enviado a " + email, Toast.LENGTH_SHORT).show();

        // Crear un intent específico para esta actividad
        Intent intent = new Intent(this, RegisterVerifyActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("userType", userType);

        // Hacer log del email que enviamos
        Log.d("AddProfilePhoto", "Enviando email: " + email);
        Log.d("AddProfilePhoto", "Enviando userType: " + userType);

        startActivity(intent);
    }

    // Método específico para limpiar cuando realmente salimos del flujo
    private void cleanupWhenExitingFlow() {
        resetPhotoState(true);

        // Limpiar también del ViewModel
        if (mViewModel != null) {
            mViewModel.setHasProfilePhoto(false);
            mViewModel.setProfilePhotoUri(null);
            mViewModel.setProfilePhotoBitmap(null);
        }

        Log.d("AddProfilePhoto", "Limpieza completa - saliendo del flujo de registro");
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

    // Método para actualizar el estado del botón continuar
    private void updateContinueButtonState() {
        // Para todos los usuarios, el botón continuar se habilita solo cuando hay foto
        btnContinuar.setEnabled(isPhotoSelected);
        btnContinuar.setAlpha(isPhotoSelected ? 1.0f : 0.4f);

        // El botón Omitir está visible para clientes y oculto para taxistas
        if ("driver".equals(userType)) {
            btnOmitir.setVisibility(View.GONE);
        } else {
            btnOmitir.setVisibility(View.VISIBLE);
            btnOmitir.setEnabled(true);
            btnOmitir.setAlpha(1.0f);
        }
    }

    @Override
    public void onBackPressed() {
        // Al presionar atrás desde AddProfilePhotoActivity, estamos navegando DENTRO del flujo
        // NO estamos saliendo del flujo, por lo que MANTENEMOS la foto
        if (isPhotoSelected && profilePhotoUri != null && tempPhotoPath != null) {
            // Guardar el estado actual en SharedPreferences para mantener persistencia
            getSharedPreferences("UserData", MODE_PRIVATE)
                    .edit()
                    .putString("photoPath", tempPhotoPath)
                    .putString("photoUri", profilePhotoUri.toString())
                    .putBoolean("photoSkipped", false)
                    .apply();

            // También mantener en el ViewModel
            if (mViewModel != null) {
                mViewModel.setHasProfilePhoto(true);
                mViewModel.setProfilePhotoUri(profilePhotoUri);
                if (savedImageBitmap != null) {
                    mViewModel.setProfilePhotoBitmap(savedImageBitmap);
                }
            }

            Log.d("AddProfilePhoto", "Navegando hacia atrás DENTRO del flujo - foto MANTENIDA");
        } else {
            Log.d("AddProfilePhoto", "Navegando hacia atrás sin foto seleccionada");
        }

        // Marcar que estamos navegando DENTRO del flujo para que RegisterUserActivity no limpie
        getSharedPreferences("UserData", MODE_PRIVATE)
                .edit()
                .putBoolean("navigatingWithinFlow", true)
                .apply();

        // Finalizar esta actividad y regresar a la anterior
        super.onBackPressed();
    }

}