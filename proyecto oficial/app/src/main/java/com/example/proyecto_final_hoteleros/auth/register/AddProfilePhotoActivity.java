package com.example.proyecto_final_hoteleros.auth.register;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
// Imports para Room Database y Repositorios
import com.example.proyecto_final_hoteleros.database.entities.UserRegistrationEntity;
import com.example.proyecto_final_hoteleros.database.entities.FileStorageEntity;
import com.example.proyecto_final_hoteleros.repository.UserRegistrationRepository;
import com.example.proyecto_final_hoteleros.repository.FileStorageRepository;
import com.example.proyecto_final_hoteleros.utils.NotificationHelper;

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

    // Agregar estas nuevas variables:
    private UserRegistrationRepository userRegistrationRepository;
    private FileStorageRepository fileStorageRepository;
    private NotificationHelper notificationHelper;
    private int currentRegistrationId = -1;

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

        // Inicializar repositorios
        userRegistrationRepository = new UserRegistrationRepository(this);
        fileStorageRepository = new FileStorageRepository(this);
        notificationHelper = new NotificationHelper(this);

        // Recuperar ID de registro del intent
        if (getIntent() != null && getIntent().hasExtra("registrationId")) {
            currentRegistrationId = getIntent().getIntExtra("registrationId", -1);
            Log.d("AddProfilePhoto", "Registration ID recibido: " + currentRegistrationId);
        }

        // Verificar si hay una foto existente
        checkExistingPhoto();

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

    private void checkExistingPhoto() {
        if (currentRegistrationId == -1) {
            Log.d("AddProfilePhoto", "No registration ID, no photo to recover");
            return;
        }

        Log.d("AddProfilePhoto", "=== CHECKING EXISTING PHOTO CON ROOM ===");
        Log.d("AddProfilePhoto", "Registration ID: " + currentRegistrationId);

        // Buscar foto en la base de datos
        fileStorageRepository.getFileByRegistrationIdAndType(
                currentRegistrationId,
                FileStorageEntity.FILE_TYPE_PHOTO,
                new FileStorageRepository.FileOperationCallback() {
                    @Override
                    public void onSuccess(FileStorageEntity fileEntity) {
                        runOnUiThread(() -> {
                            Log.d("AddProfilePhoto", "Foto encontrada en base de datos: " + fileEntity.originalName);

                            // Verificar que el archivo físico existe
                            File file = new File(fileEntity.storedPath);
                            if (file.exists()) {
                                try {
                                    // Cargar imagen desde archivo
                                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                    if (bitmap != null) {
                                        tempPhotoPath = fileEntity.storedPath;
                                        profilePhotoUri = Uri.fromFile(file);
                                        isPhotoSelected = true;

                                        displaySelectedImage(null, bitmap);
                                        Log.d("AddProfilePhoto", "Foto recuperada exitosamente desde Room Database");
                                    } else {
                                        Log.e("AddProfilePhoto", "No se pudo decodificar la imagen");
                                    }
                                } catch (Exception e) {
                                    Log.e("AddProfilePhoto", "Error cargando imagen existente", e);
                                }
                            } else {
                                Log.e("AddProfilePhoto", "Archivo de foto no existe físicamente: " + fileEntity.storedPath);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Log.d("AddProfilePhoto", "No hay foto guardada en la base de datos: " + error);
                        // Es normal, no hay foto previa
                    }
                }
        );
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
        Log.d("AddProfilePhoto", "=== COMPLETANDO REGISTRO CON ROOM ===");
        Log.d("AddProfilePhoto", "Registration ID: " + currentRegistrationId);
        Log.d("AddProfilePhoto", "User Type: " + userType);
        Log.d("AddProfilePhoto", "Photo selected: " + isPhotoSelected);

        if (currentRegistrationId == -1) {
            Toast.makeText(this, "Error: No hay registro activo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si hay foto seleccionada, guardarla primero
        if (isPhotoSelected && profilePhotoUri != null && tempPhotoPath != null && savedImageBitmap != null) {
            Log.d("AddProfilePhoto", "Guardando foto de perfil...");

            String mimeType = "image/jpeg";
            String originalName = "profile_photo.jpg";

            fileStorageRepository.saveFile(
                    currentRegistrationId,
                    FileStorageEntity.FILE_TYPE_PHOTO,
                    originalName,
                    profilePhotoUri,
                    mimeType,
                    new FileStorageRepository.FileOperationCallback() {
                        @Override
                        public void onSuccess(FileStorageEntity fileEntity) {
                            Log.d("AddProfilePhoto", "Foto guardada exitosamente: " + fileEntity.id);
                            // Continuar con el completion del registro
                            finalizeRegistration();
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("AddProfilePhoto", "Error guardando foto: " + error);
                            // Continuar de todos modos si no es crítico
                            finalizeRegistration();
                        }
                    }
            );
        } else {
            Log.d("AddProfilePhoto", "No hay foto para guardar, finalizando registro...");
            finalizeRegistration();
        }
    }

    private void finalizeRegistration() {
        // Marcar el registro como completado
        userRegistrationRepository.markRegistrationAsCompleted(
                currentRegistrationId,
                new UserRegistrationRepository.RegistrationCallback() {
                    @Override
                    public void onSuccess(UserRegistrationEntity registration) {
                        Log.d("AddProfilePhoto", "Registro marcado como completado exitosamente");

                        // Mostrar notificación de registro completado
                        String userName = registration.nombres + " " + registration.apellidos;
                        notificationHelper.showRegistrationCompleteNotification(registration.userType, userName);

                        // Limpiar datos temporales
                        clearTemporaryData();

                        // Mostrar Toast con mensaje de éxito
                        runOnUiThread(() -> {
                            String email = registration.email;
                            Toast.makeText(AddProfilePhotoActivity.this,
                                    "Código de verificación enviado a " + email, Toast.LENGTH_SHORT).show();

                            // Navegar a la pantalla de verificación
                            Intent intent = new Intent(AddProfilePhotoActivity.this, RegisterVerifyActivity.class);
                            intent.putExtra("email", email);
                            intent.putExtra("userType", registration.userType);
                            intent.putExtra("registrationId", currentRegistrationId);
                            startActivity(intent);
                            finish();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("AddProfilePhoto", "Error marcando registro como completado: " + error);
                        runOnUiThread(() -> {
                            Toast.makeText(AddProfilePhotoActivity.this,
                                    "Error completando el registro: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }

    private void clearTemporaryData() {
        // Limpiar SharedPreferences legacy
        getSharedPreferences("UserData", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        Log.d("AddProfilePhoto", "Datos temporales limpiados");
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