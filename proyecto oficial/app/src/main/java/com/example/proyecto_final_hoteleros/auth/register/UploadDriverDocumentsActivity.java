package com.example.proyecto_final_hoteleros.auth.register;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyecto_final_hoteleros.AuthActivity;
import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.button.MaterialButton;
// Imports para Room Database y Repositorios
import com.example.proyecto_final_hoteleros.database.entities.FileStorageEntity;
import com.example.proyecto_final_hoteleros.repository.UserRegistrationRepository;
import com.example.proyecto_final_hoteleros.repository.FileStorageRepository;
import com.example.proyecto_final_hoteleros.utils.NotificationHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

public class UploadDriverDocumentsActivity extends AppCompatActivity {

    private static final String TAG = "UploadDriverDocActivity";
    private static final int PICK_PDF_FILE = 2;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB en bytes

    private LinearLayout layoutSelectedFile;
    private TextView tvFileName, tvFileSize, tvUploadStatus;
    private ProgressBar progressUpload;
    private Button btnBrowseFile;
    private ImageButton btnDeleteFile;
    private MaterialButton btnContinuar;
    private TextView tvSunarpLink;
    private TextView tvLoginTab, tvRegisterTab;
    private View viewTabIndicatorLogin, viewTabIndicatorRegister;

    private Uri pdfUri;
    private File tempPdfFile;
    private RegisterViewModel mViewModel;
    private String userType;
    private String placaVehiculo;

    // Agregar estas nuevas variables:
    private UserRegistrationRepository userRegistrationRepository;
    private FileStorageRepository fileStorageRepository;
    private NotificationHelper notificationHelper;
    private int currentRegistrationId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sistema_activity_upload_driver_documents);

        // Inicializar ViewModel
        mViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // Inicializar repositorios
        userRegistrationRepository = new UserRegistrationRepository(this);
        fileStorageRepository = new FileStorageRepository(this);
        notificationHelper = new NotificationHelper(this);

        // Recuperar ID de registro del intent
        if (getIntent() != null && getIntent().hasExtra("registrationId")) {
            currentRegistrationId = getIntent().getIntExtra("registrationId", -1);
            Log.d(TAG, "Registration ID recibido: " + currentRegistrationId);
        }

        // Recuperar datos de usuario del intent
        if (getIntent() != null) {
            userType = getIntent().getStringExtra("userType");
            placaVehiculo = getIntent().getStringExtra("placaVehiculo");
        }

        // Si no hay userType, usar valor por defecto
        if (userType == null || userType.isEmpty()) {
            userType = "driver"; // Por defecto es driver, ya que esta pantalla es solo para taxistas
        }

        // Inicializar vistas
        initViews();
        setupTabsAndIndicators();
        setupSunarpLink();
        setupFileSelection();

        // AGREGAR ESTE LOG ANTES DE checkExistingPdfFile()
        Log.d(TAG, "=== INICIANDO UploadDriverDocumentsActivity ===");
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        Log.d(TAG, "SharedPreferences al iniciar:");
        Log.d(TAG, "  - pdfPath: " + prefs.getString("pdfPath", "NO_ENCONTRADO"));
        Log.d(TAG, "  - pdfUri: " + prefs.getString("pdfUri", "NO_ENCONTRADO"));

        // Verificar si ya hay un archivo PDF en SharedPreferences
        checkExistingPdfFile();
    }

    private void initViews() {
        layoutSelectedFile = findViewById(R.id.layoutSelectedFile);
        tvFileName = findViewById(R.id.tvFileName);
        tvFileSize = findViewById(R.id.tvFileSize);
        tvUploadStatus = findViewById(R.id.tvUploadStatus);
        progressUpload = findViewById(R.id.progressUpload);
        btnBrowseFile = findViewById(R.id.btnBrowseFile);
        btnDeleteFile = findViewById(R.id.btnDeleteFile);
        btnContinuar = findViewById(R.id.btnContinuar);
        tvSunarpLink = findViewById(R.id.tvSunarpLink);
        tvLoginTab = findViewById(R.id.tvLoginTab);
        tvRegisterTab = findViewById(R.id.tvRegisterTab);
        viewTabIndicatorLogin = findViewById(R.id.viewTabIndicatorLogin);
        viewTabIndicatorRegister = findViewById(R.id.viewTabIndicatorRegister);

        // Configurar botón de volver
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Configurar botón continuar
        btnContinuar.setOnClickListener(v -> {
            if (pdfUri != null) {
                goToAddProfilePhoto();
            } else {
                Toast.makeText(this, "Por favor, seleccione un archivo PDF primero", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTabsAndIndicators() {
        // Configurar clic en pestaña "Iniciar Sesión"
        tvLoginTab.setOnClickListener(v -> {
            // Limpiar datos al cambiar a login
            getSharedPreferences("UserData", MODE_PRIVATE)
                    .edit()
                    .remove("photoPath")
                    .remove("photoUri")
                    .remove("pdfPath")
                    .remove("pdfUri")
                    .remove("email")
                    .remove("photoSkipped")
                    .apply();

            // Ir a AuthActivity mostrando la pestaña de login
            Intent intent = new Intent(UploadDriverDocumentsActivity.this, AuthActivity.class);
            intent.putExtra("mode", "login");
            startActivity(intent);
            finish();
        });
    }

    private void setupSunarpLink() {
        // Hacemos el enlace clickable
        tvSunarpLink.setMovementMethod(LinkMovementMethod.getInstance());

        // Aseguramos que el texto se maneje como un enlace
        SpannableString spannableString = new SpannableString(tvSunarpLink.getText());
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                String url = "https://consultavehicular.sunarp.gob.pe/consulta-vehicular/inicio";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        };
        spannableString.setSpan(clickableSpan, 0, tvSunarpLink.getText().length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvSunarpLink.setText(spannableString);
    }

    private void setupFileSelection() {
        btnBrowseFile.setOnClickListener(v -> {
            openFilePicker();
        });

        findViewById(R.id.dropZone).setOnClickListener(v -> {
            openFilePicker();
        });

        btnDeleteFile.setOnClickListener(v -> {
            deleteSelectedFile();
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");

        startActivityForResult(intent, PICK_PDF_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (isPdfFile(uri) && isFileSizeValid(uri)) {
                    processSelectedFile(uri);
                }
            }
        }
    }

    private boolean isPdfFile(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType != null && mimeType.equals("application/pdf")) {
            return true;
        } else {
            Toast.makeText(this, "Por favor, seleccione un archivo PDF", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean isFileSizeValid(Uri uri) {
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                long fileSize = cursor.getLong(sizeIndex);
                cursor.close();

                if (fileSize > MAX_FILE_SIZE) {
                    Toast.makeText(this, "El archivo no debe exceder los 5MB", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar el tamaño del archivo", e);
        }
        return false;
    }

    private void processSelectedFile(Uri uri) {
        try {
            Log.d(TAG, "=== PROCESANDO ARCHIVO CON ROOM ===");
            Log.d(TAG, "URI: " + uri.toString());
            Log.d(TAG, "Registration ID: " + currentRegistrationId);

            if (currentRegistrationId == -1) {
                Toast.makeText(this, "Error: No hay registro activo", Toast.LENGTH_SHORT).show();
                return;
            }

            String originalName = getFileName(uri);
            String mimeType = getContentResolver().getType(uri);

            Log.d(TAG, "Nombre original: " + originalName);
            Log.d(TAG, "MIME type: " + mimeType);

            // Guardar archivo usando el repository
            fileStorageRepository.saveFile(
                    currentRegistrationId,
                    FileStorageEntity.FILE_TYPE_PDF,
                    originalName,
                    uri,
                    mimeType,
                    new FileStorageRepository.FileOperationCallback() {
                        @Override
                        public void onSuccess(FileStorageEntity fileEntity) {
                            runOnUiThread(() -> {
                                // Configurar variables locales
                                tempPdfFile = new File(fileEntity.storedPath);
                                pdfUri = Uri.fromFile(tempPdfFile);

                                Log.d(TAG, "Archivo guardado exitosamente:");
                                Log.d(TAG, "  - ID: " + fileEntity.id);
                                Log.d(TAG, "  - Ruta: " + fileEntity.storedPath);
                                Log.d(TAG, "  - Tamaño: " + fileEntity.fileSize + " bytes");

                                // Mostrar información del archivo
                                displayFileInfoFromFileEntity(fileEntity);
                                updateButtonState();

                                Toast.makeText(UploadDriverDocumentsActivity.this,
                                        "Archivo guardado exitosamente", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Log.e(TAG, "Error guardando archivo: " + error);
                                Toast.makeText(UploadDriverDocumentsActivity.this,
                                        "Error al guardar archivo: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
            );

        } catch (Exception e) {
            Log.e(TAG, "Error al procesar el archivo seleccionado", e);
            Toast.makeText(this, "Error al procesar el archivo", Toast.LENGTH_SHORT).show();
        }
    }

    private File saveUriToTempFile(Uri uri) throws IOException {
        String fileName = getFileName(uri);
        File tempFile = File.createTempFile("pdf_", ".pdf", getCacheDir());

        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream != null) {
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;
        }
        return null;
    }

    private String getFileName(Uri uri) {
        String result = "documento.pdf";
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener el nombre del archivo", e);
            }
        }
        return result;
    }

    private String getFileSize(Uri uri) {
        String fileSize = "0 KB";
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    long size = cursor.getLong(sizeIndex);
                    fileSize = formatSize(size);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener el tamaño del archivo", e);
        }
        return fileSize;
    }

    private String formatSize(long size) {
        if (size <= 0) return "0 KB";

        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private void displayFileInfo(Uri uri) {
        String fileName = getFileName(uri);
        String fileSize = getFileSize(uri);

        tvFileName.setText(fileName);
        tvFileSize.setText(fileSize);
        tvUploadStatus.setText("Completado");
        tvUploadStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        progressUpload.setProgress(100);

        layoutSelectedFile.setVisibility(View.VISIBLE);
    }

    private void deleteSelectedFile() {
        if (currentRegistrationId == -1) {
            Log.w(TAG, "No registration ID for deleting file");
            return;
        }

        // Eliminar archivo de la base de datos
        fileStorageRepository.getFileByRegistrationIdAndType(
                currentRegistrationId,
                FileStorageEntity.FILE_TYPE_PDF,
                new FileStorageRepository.FileOperationCallback() {
                    @Override
                    public void onSuccess(FileStorageEntity fileEntity) {
                        // Eliminar el archivo usando el repository
                        fileStorageRepository.deleteFile(fileEntity, new FileStorageRepository.FileOperationCallback() {
                            @Override
                            public void onSuccess(FileStorageEntity deletedFileEntity) {
                                runOnUiThread(() -> {
                                    // Limpiar variables locales
                                    pdfUri = null;
                                    tempPdfFile = null;

                                    // Ocultar vista de archivo
                                    layoutSelectedFile.setVisibility(View.GONE);

                                    // Actualizar estado del botón
                                    updateButtonState();

                                    Log.d(TAG, "Archivo eliminado exitosamente");
                                    Toast.makeText(UploadDriverDocumentsActivity.this,
                                            "Archivo eliminado", Toast.LENGTH_SHORT).show();
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    Log.e(TAG, "Error eliminando archivo: " + error);
                                    Toast.makeText(UploadDriverDocumentsActivity.this,
                                            "Error al eliminar archivo", Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Log.d(TAG, "No hay archivo para eliminar: " + error);
                            // Limpiar UI de todos modos
                            layoutSelectedFile.setVisibility(View.GONE);
                            updateButtonState();
                        });
                    }
                }
        );
    }

    private void updateButtonState() {
        boolean pdfSelected = pdfUri != null;
        btnContinuar.setEnabled(pdfSelected);
        btnContinuar.setAlpha(pdfSelected ? 1.0f : 0.4f);
    }

    private void checkExistingPdfFile() {
        Log.d(TAG, "=== CHECKING EXISTING PDF FILE CON ROOM ===");
        Log.d(TAG, "Current Registration ID: " + currentRegistrationId);

        if (currentRegistrationId == -1) {
            Log.d(TAG, "No registration ID, no PDF to recover");
            return;
        }

        // Buscar archivo PDF en la base de datos
        fileStorageRepository.getFileByRegistrationIdAndType(
                currentRegistrationId,
                FileStorageEntity.FILE_TYPE_PDF,
                new FileStorageRepository.FileOperationCallback() {
                    @Override
                    public void onSuccess(FileStorageEntity fileEntity) {
                        runOnUiThread(() -> {
                            Log.d(TAG, "PDF encontrado en base de datos: " + fileEntity.originalName);

                            // Verificar que el archivo físico existe
                            File file = new File(fileEntity.storedPath);
                            if (file.exists()) {
                                // Configurar variables locales
                                tempPdfFile = file;
                                pdfUri = Uri.fromFile(file);

                                // Mostrar información del archivo
                                displayFileInfoFromFile(file);
                                updateButtonState();

                                Log.d(TAG, "PDF recuperado exitosamente desde Room Database");
                            } else {
                                Log.e(TAG, "Archivo PDF no existe físicamente: " + fileEntity.storedPath);
                                // El archivo será eliminado automáticamente por el repository
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Log.d(TAG, "No hay PDF guardado en la base de datos: " + error);
                        // Es normal, no hay archivo previo
                    }
                }
        );
    }

    private void displayFileInfoFromFile(File file) {
        String fileName = file.getName();
        String fileSize = formatSize(file.length());

        tvFileName.setText(fileName);
        tvFileSize.setText(fileSize);
        tvUploadStatus.setText("Completado");
        tvUploadStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        progressUpload.setProgress(100);

        layoutSelectedFile.setVisibility(View.VISIBLE);

        Log.d(TAG, "Mostrando info del archivo: " + fileName + " (" + fileSize + ")");
    }

    private void displayFileInfoFromFileEntity(FileStorageEntity fileEntity) {
        String fileName = fileEntity.originalName;
        String fileSize = formatSize(fileEntity.fileSize);

        tvFileName.setText(fileName);
        tvFileSize.setText(fileSize);
        tvUploadStatus.setText("Completado");
        tvUploadStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        progressUpload.setProgress(100);

        layoutSelectedFile.setVisibility(View.VISIBLE);

        Log.d(TAG, "Mostrando info del archivo desde FileEntity: " + fileName + " (" + fileSize + ")");
    }

    private void goToAddProfilePhoto() {
        // Guardar documentos en ViewModel para compatibilidad
        if (pdfUri != null) {
            mViewModel.setDriverDocumentsUri(pdfUri);
            Log.d(TAG, "PDF guardado en ViewModel para compatibilidad: " + pdfUri.toString());
        }

        // Verificar que tenemos un PDF guardado en la base de datos
        if (currentRegistrationId == -1) {
            Toast.makeText(this, "Error: No hay registro activo", Toast.LENGTH_SHORT).show();
            return;
        }

        fileStorageRepository.getFileByRegistrationIdAndType(
                currentRegistrationId,
                FileStorageEntity.FILE_TYPE_PDF,
                new FileStorageRepository.FileOperationCallback() {
                    @Override
                    public void onSuccess(FileStorageEntity fileEntity) {
                        runOnUiThread(() -> {
                            Log.d(TAG, "PDF confirmado en base de datos antes de navegar:");
                            Log.d(TAG, "  - File ID: " + fileEntity.id);
                            Log.d(TAG, "  - Registration ID: " + fileEntity.registrationId);
                            Log.d(TAG, "  - File path: " + fileEntity.storedPath);

                            // Navegar a la actividad de añadir foto de perfil
                            Intent intent = new Intent(UploadDriverDocumentsActivity.this, AddProfilePhotoActivity.class);
                            intent.putExtra("userType", userType);
                            intent.putExtra("registrationId", currentRegistrationId);
                            startActivity(intent);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "No se encontró PDF en la base de datos: " + error);
                            Toast.makeText(UploadDriverDocumentsActivity.this,
                                    "Error: Debe seleccionar un archivo PDF primero", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }

    @Override
    public void onBackPressed() {
        // Al volver atrás desde UploadDriverDocuments hacia RegisterUserActivity,
        // estamos navegando DENTRO del flujo, por lo que NO limpiamos la foto ni el PDF

        // Asegurar que el PDF se mantenga en SharedPreferences
        if (tempPdfFile != null && tempPdfFile.exists() && pdfUri != null) {
            getSharedPreferences("UserData", MODE_PRIVATE)
                    .edit()
                    .putString("pdfPath", tempPdfFile.getAbsolutePath())
                    .putString("pdfUri", pdfUri.toString())
                    .apply();
        }

        // Marcar que estamos navegando DENTRO del flujo
        getSharedPreferences("UserData", MODE_PRIVATE)
                .edit()
                .putBoolean("navigatingWithinFlow", true)
                .apply();

        Log.d("UploadDriverDocuments", "Navegando hacia atrás DENTRO del flujo - foto y PDF mantenidos");

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        // Si la aplicación se cierra sin completar el registro, eliminar archivos temporales
        if (isFinishing() && !isChangingConfigurations()) {
            cleanupTemporaryFiles();
        }
        super.onDestroy();
    }

    private void cleanupTemporaryFiles() {
        // Solo eliminamos los archivos temporales si estamos saliendo del flujo de registro
        // No cuando estamos rotando la pantalla o navegando en el flujo
        boolean isExitingRegistrationFlow = isFinishing() && !isChangingConfigurations();

        if (isExitingRegistrationFlow) {
            if (tempPdfFile != null && tempPdfFile.exists()) {
                tempPdfFile.delete();
            }

            // Limpiar SharedPreferences
            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
            prefs.edit()
                    .remove("pdfPath")
                    .remove("pdfUri")
                    .apply();
        }
    }
}