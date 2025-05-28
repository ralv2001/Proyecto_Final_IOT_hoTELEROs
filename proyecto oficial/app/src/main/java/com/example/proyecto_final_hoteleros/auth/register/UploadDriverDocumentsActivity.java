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
import com.example.proyecto_final_hoteleros.FileDataManager;
import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.button.MaterialButton;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sistema_activity_upload_driver_documents);

        // Inicializar ViewModel
        mViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

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
            // Guardar el archivo seleccionado en un archivo temporal
            tempPdfFile = saveUriToTempFile(uri);

            if (tempPdfFile != null) {
                pdfUri = Uri.fromFile(tempPdfFile);

                // Guardar en FileDataManager INMEDIATAMENTE
                FileDataManager.getInstance().setPdfData(pdfUri, tempPdfFile.getAbsolutePath());

                // Guardar en SharedPreferences
                SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                prefs.edit()
                        .putString("pdfPath", tempPdfFile.getAbsolutePath())
                        .putString("pdfUri", pdfUri.toString())
                        .apply();

                // Guardar en ViewModel también
                if (mViewModel != null) {
                    mViewModel.setDriverDocumentsUri(pdfUri);
                }

                Log.d(TAG, "Archivo procesado y guardado:");
                Log.d(TAG, "  - Ruta temporal: " + tempPdfFile.getAbsolutePath());
                Log.d(TAG, "  - URI: " + pdfUri.toString());

                displayFileInfo(uri);
                updateButtonState();
            }
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
        // Eliminar archivo temporal si existe
        if (tempPdfFile != null && tempPdfFile.exists()) {
            tempPdfFile.delete();
        }

        // Limpiar variables
        pdfUri = null;
        tempPdfFile = null;

        // Eliminar de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        prefs.edit()
                .remove("pdfPath")
                .remove("pdfUri")
                .apply();

        // Ocultar vista de archivo
        layoutSelectedFile.setVisibility(View.GONE);

        // Actualizar estado del botón
        updateButtonState();
    }

    private void updateButtonState() {
        boolean pdfSelected = pdfUri != null;
        btnContinuar.setEnabled(pdfSelected);
        btnContinuar.setAlpha(pdfSelected ? 1.0f : 0.4f);
    }

    private void checkExistingPdfFile() {
        // PRIMERO: Verificar en FileDataManager
        FileDataManager fileManager = FileDataManager.getInstance();

        Log.d(TAG, "=== DEBUGGING FileDataManager ===");
        Log.d(TAG, "FileDataManager instance: " + fileManager);
        Log.d(TAG, "FileDataManager.getPdfUri(): " + fileManager.getPdfUri());
        Log.d(TAG, "FileDataManager.getPdfPath(): " + fileManager.getPdfPath());
        Log.d(TAG, "FileDataManager.hasPdf(): " + fileManager.hasPdf());

        if (fileManager.hasPdf()) {
            String filePath = fileManager.getPdfPath();
            pdfUri = fileManager.getPdfUri();

            Log.d(TAG, "PDF encontrado en FileDataManager: " + filePath);

            File file = new File(filePath);
            Log.d(TAG, "Archivo existe físicamente: " + file.exists());

            if (file.exists()) {
                tempPdfFile = file;
                displayFileInfoFromFile(file);
                updateButtonState();

                // Sincronizar con SharedPreferences
                SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                prefs.edit()
                        .putString("pdfPath", filePath)
                        .putString("pdfUri", pdfUri.toString())
                        .apply();

                Log.d(TAG, "Archivo PDF recuperado del FileDataManager exitosamente");
                return;
            } else {
                Log.w(TAG, "El archivo del FileDataManager no existe físicamente, pero tenemos los datos");
                // No limpiar los datos del FileDataManager, solo mostrar mensaje
                Toast.makeText(this, "El archivo PDF anterior ya no está disponible. Por favor, seleccione un nuevo archivo.", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "FileDataManager no tiene PDF guardado");
        }

        // SEGUNDO: Si no está en FileDataManager O el archivo no existe, intentar de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String pdfPath = prefs.getString("pdfPath", "");
        String pdfUriString = prefs.getString("pdfUri", "");

        Log.d(TAG, "Verificando archivo existente - pdfPath: " + pdfPath);
        Log.d(TAG, "Verificando archivo existente - pdfUri: " + pdfUriString);

        if (!pdfPath.isEmpty()) {
            File file = new File(pdfPath);
            Log.d(TAG, "Archivo existe en SharedPreferences: " + file.exists() + ", Ruta: " + file.getAbsolutePath());

            if (file.exists()) {
                tempPdfFile = file;
                pdfUri = Uri.fromFile(file);

                try {
                    displayFileInfoFromFile(file);
                    updateButtonState();

                    // Sincronizar con FileDataManager
                    fileManager.setPdfData(pdfUri, pdfPath);

                    Log.d(TAG, "Archivo PDF recuperado de SharedPreferences exitosamente");
                } catch (Exception e) {
                    Log.e(TAG, "Error al cargar el archivo existente", e);
                    deleteSelectedFile();
                }
            } else {
                Log.e(TAG, "El archivo PDF no existe en la ruta guardada de SharedPreferences");
                // Limpiar referencias inválidas de SharedPreferences
                prefs.edit()
                        .remove("pdfPath")
                        .remove("pdfUri")
                        .apply();

                // Si tenía datos en FileDataManager pero el archivo no existe, también limpiar FileDataManager
                if (fileManager.hasPdf()) {
                    fileManager.clearPdf();
                    Toast.makeText(this, "El archivo PDF anterior ya no está disponible. Por favor, seleccione un nuevo archivo.", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Log.d(TAG, "No hay archivo PDF guardado previamente");
        }
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

    private void goToAddProfilePhoto() {
        // Guardar documentos en ViewModel si es necesario
        if (pdfUri != null) {
            mViewModel.setDriverDocumentsUri(pdfUri);
            Log.d(TAG, "PDF guardado en ViewModel: " + pdfUri.toString());
        }

        // IMPORTANTE: Asegurar que el archivo se guarde en SharedPreferences antes de cambiar de actividad
        if (tempPdfFile != null && tempPdfFile.exists()) {
            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
            prefs.edit()
                    .putString("pdfPath", tempPdfFile.getAbsolutePath())
                    .putString("pdfUri", pdfUri.toString())
                    .apply();

            Log.d(TAG, "PDF guardado en SharedPreferences antes de navegar:");
            Log.d(TAG, "  - pdfPath: " + tempPdfFile.getAbsolutePath());
            Log.d(TAG, "  - pdfUri: " + pdfUri.toString());
        }

        // Navegar a la actividad de añadir foto de perfil
        Log.d("UploadDriverDocuments", "Navigating to AddProfilePhotoActivity");
        Intent intent = new Intent(this, AddProfilePhotoActivity.class);
        intent.putExtra("userType", userType);
        startActivity(intent);
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