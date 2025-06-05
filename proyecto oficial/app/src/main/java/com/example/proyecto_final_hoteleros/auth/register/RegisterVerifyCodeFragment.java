package com.example.proyecto_final_hoteleros.auth.register;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import com.google.firebase.auth.FirebaseUser;
import com.example.proyecto_final_hoteleros.auth.register.AddProfilePhotoActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.database.entities.FileStorageEntity;
import com.example.proyecto_final_hoteleros.repository.FileStorageRepository;
import com.example.proyecto_final_hoteleros.utils.AwsFileManager;
import com.google.android.material.button.MaterialButton;
import com.example.proyecto_final_hoteleros.repository.UserRegistrationRepository;
import com.example.proyecto_final_hoteleros.database.entities.UserRegistrationEntity;

// ========== NUEVOS IMPORTS FIREBASE ==========
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;
import com.example.proyecto_final_hoteleros.models.UserModel;
import java.util.List;

public class RegisterVerifyCodeFragment extends Fragment {

    private static final String TAG = "RegisterVerifyCodeFragment";

    // ========== FIREBASE ==========
    private FirebaseManager firebaseManager;

    private MaterialButton btnVerifyEmail;
    private TextView tvEmailSent, tvResendCode, tvInstructions;
    private String email;

    private EditText etCode1, etCode2, etCode3, etCode4, etCode5;

    // Variables para email verification
    private static final int CHECK_INTERVAL = 3000; // 3 segundos
    private Handler verificationHandler;
    private Runnable verificationRunnable;
    private boolean isCheckingVerification = false;
    private boolean emailVerificationSent = false;

    public static RegisterVerifyCodeFragment newInstance(String email) {
        RegisterVerifyCodeFragment fragment = new RegisterVerifyCodeFragment();
        Bundle args = new Bundle();
        args.putString("email", email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sistema_fragment_verify_code, container, false);

        // ========== INICIALIZAR FIREBASE ==========
        firebaseManager = FirebaseManager.getInstance();

        // Obtener el email de los argumentos
        if (getArguments() != null) {
            email = getArguments().getString("email", "");
            Log.d(TAG, "Email from arguments: " + email);
        }

        // Inicializar todas las vistas
        etCode1 = view.findViewById(R.id.etCode1);
        etCode2 = view.findViewById(R.id.etCode2);
        etCode3 = view.findViewById(R.id.etCode3);
        etCode4 = view.findViewById(R.id.etCode4);
        etCode5 = view.findViewById(R.id.etCode5);
        btnVerifyEmail = view.findViewById(R.id.btnVerifyCode);
        btnVerifyEmail.setText("Verificar Email");
        tvInstructions = view.findViewById(R.id.tvInstructions);

        tvEmailSent = view.findViewById(R.id.tvEmailSent);
        tvResendCode = view.findViewById(R.id.tvResendCode);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText("Verifica tu correo electrónico");

        ImageButton btnBack = view.findViewById(R.id.btnBack);

        // Configurar el texto del email enmascarado
        if (!email.isEmpty()) {
            String maskedEmail = maskEmail(email);
            Log.d(TAG, "Masked email: " + maskedEmail);
            tvEmailSent.setText("Email de verificación enviado a " + maskedEmail);
        } else {
            Log.e(TAG, "Email is empty!");
        }

        // Ocultar campos de código y mostrar mensaje de verificación
        hideCodeInputsAndShowEmailMessage();

        // Enviar email de verificación automáticamente
        sendEmailVerificationToUser();

        // Inicializar verificación automática
        initializeEmailVerificationCheck();

        btnVerifyEmail.setOnClickListener(v -> {
            checkEmailVerificationManually();
        });

        tvResendCode.setOnClickListener(v -> {
            resendCode();
        });


        // Configurar el botón de retroceso
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                // Obtener datos del registro
                int registrationId = -1;
                String userType = "client";

                if (getArguments() != null) {
                    registrationId = getArguments().getInt("registrationId", -1);
                    userType = getArguments().getString("userType", "client");
                }

                Log.d(TAG, "Navegando de vuelta a AddProfilePhoto");
                Log.d(TAG, "RegistrationId: " + registrationId + ", UserType: " + userType);

                // Navegar específicamente a AddProfilePhotoActivity
                Intent intent = new Intent(getActivity(), AddProfilePhotoActivity.class);
                intent.putExtra("registrationId", registrationId);
                intent.putExtra("userType", userType);
                startActivity(intent);

                // Cerrar la actividad actual
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener el email de los argumentos
        if (getArguments() != null) {
            email = getArguments().getString("email", "");
            Log.d(TAG, "Email recibido: " + email);

            // Actualizar el texto inmediatamente
            if (tvEmailSent != null && !email.isEmpty()) {
                String maskedEmail = maskEmail(email);
                tvEmailSent.setText("Email de verificación enviado a " + maskedEmail + "\n\nHaz clic en el enlace del email y presiona 'Verificar Email'");
            }
        }
    }

    private void hideCodeInputsAndShowEmailMessage() {
        Log.d(TAG, "=== OCULTANDO CÓDIGOS Y MOSTRANDO EMAIL MESSAGE ===");
        Log.d(TAG, "Email: " + email);
        // Ocultar los campos de código
        etCode1.setVisibility(View.GONE);
        etCode2.setVisibility(View.GONE);
        etCode3.setVisibility(View.GONE);
        etCode4.setVisibility(View.GONE);
        etCode5.setVisibility(View.GONE);

        // Actualizar instrucciones usando el TextView existente
        if (tvEmailSent != null) {
            tvEmailSent.setText("Email de verificación enviado a " + maskEmail(email) + "\n\nHaz clic en el enlace del email y presiona 'Verificar Email'");
        }

        // Enviar email de verificación automáticamente
        sendEmailVerificationToUser();

        // Inicializar verificación automática
        initializeEmailVerificationCheck();

        // Habilitar botón inmediatamente
        btnVerifyEmail.setEnabled(true);
        btnVerifyEmail.setAlpha(1.0f);
    }

    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "";
        }

        // Verificar si el correo tiene formato válido
        if (!email.contains("@")) {
            Log.e(TAG, "Email inválido (sin @): " + email);
            return email;
        }

        int atIndex = email.indexOf('@');
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex); // Incluye el @

        Log.d(TAG, "Username: " + username + ", Domain: " + domain);

        // Solo mostrar el primer carácter del nombre de usuario
        if (username.length() > 0) {
            return username.charAt(0) + "*****" + domain;
        } else {
            return "*****" + domain;
        }
    }

    private void sendEmailVerificationToUser() {
        Log.d(TAG, "=== ENVIANDO EMAIL DE VERIFICACIÓN ===");

        // Obtener datos del registro para crear nuevo usuario
        int registrationId = -1;
        if (getArguments() != null && getArguments().containsKey("registrationId")) {
            registrationId = getArguments().getInt("registrationId", -1);
        }

        if (registrationId == -1) {
            Log.e(TAG, "No hay registrationId para crear usuario");
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error: No hay datos de registro", Toast.LENGTH_SHORT).show();
                });
            }
            return;
        }

        // Obtener datos desde Room Database
        UserRegistrationRepository repository = new UserRegistrationRepository(getActivity());
        repository.getUserRegistrationById(registrationId, new UserRegistrationRepository.RegistrationCallback() {
            @Override
            public void onSuccess(UserRegistrationEntity registration) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        createUserAndSendVerification(registration);
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error obteniendo registro: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error obteniendo datos: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void createUserAndSendVerification(UserRegistrationEntity registration) {
        Log.d(TAG, "Creando usuario Firebase para: " + registration.email);

        // Crear usuario en Firebase Auth
        firebaseManager.registerUser(registration.email, registration.password, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                Log.d(TAG, "✅ Usuario creado en Firebase Auth: " + userId);
                sendVerificationEmail();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error creando usuario Firebase: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (error.contains("email-already-in-use")) {
                            // Si el email ya existe, intentar hacer login
                            Log.d(TAG, "Email ya existe, intentando login...");
                            loginExistingUserAndSendVerification(registration.email, registration.password);
                        } else {
                            Toast.makeText(getContext(), translateFirebaseError(error), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void loginExistingUserAndSendVerification(String email, String password) {
        Log.d(TAG, "=== HACIENDO LOGIN A USUARIO EXISTENTE ===");

        firebaseManager.loginUser(email, password, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                Log.d(TAG, "✅ Login exitoso a usuario existente: " + userId);
                sendVerificationEmail();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error en login a usuario existente: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error: " + translateFirebaseError(error), Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void sendVerificationEmail() {
        FirebaseUser currentUser = firebaseManager.getCurrentUser();
        if (currentUser != null && !emailVerificationSent) {
            Log.d(TAG, "Enviando email de verificación a: " + currentUser.getEmail());

            firebaseManager.sendEmailVerification(currentUser, new FirebaseManager.DataCallback() {
                @Override
                public void onSuccess() {
                    emailVerificationSent = true;
                    Log.d(TAG, "✅ Email de verificación enviado exitosamente");
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Email de verificación enviado", Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error enviando email: " + error);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error enviando email: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        } else if (currentUser == null) {
            Log.e(TAG, "❌ No hay usuario actual para enviar verificación");
        }
    }

    private void initializeEmailVerificationCheck() {
        verificationHandler = new Handler(Looper.getMainLooper());
        verificationRunnable = new Runnable() {
            @Override
            public void run() {
                if (isCheckingVerification && getActivity() != null && !getActivity().isFinishing()) {
                    checkEmailVerificationSilently();
                    verificationHandler.postDelayed(this, CHECK_INTERVAL);
                }
            }
        };

        isCheckingVerification = true;
        verificationHandler.postDelayed(verificationRunnable, 2000); // Empezar después de 2 segundos
    }

    private void checkEmailVerificationSilently() {
        FirebaseUser currentUser = firebaseManager.getCurrentUser();
        if (currentUser == null) return;

        firebaseManager.checkEmailVerification(currentUser, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                Log.d(TAG, "✅ Email verificado automáticamente");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        stopEmailVerificationCheck();
                        Toast.makeText(getContext(), "¡Email verificado! Completando registro...", Toast.LENGTH_SHORT).show();
                        proceedToCompleteRegistration();
                    });
                }
            }

            @Override
            public void onError(String error) {
                // Silencioso, solo logging
                Log.d(TAG, "Email aún no verificado (check automático)");
            }
        });
    }

    private void stopEmailVerificationCheck() {
        isCheckingVerification = false;
        if (verificationHandler != null && verificationRunnable != null) {
            verificationHandler.removeCallbacks(verificationRunnable);
        }
    }

    // ========== NUEVO MÉTODO CON FIREBASE ==========
    private void verifyCodeAndRegister(String code) {
        Log.d(TAG, "=== VERIFICANDO EMAIL MANUALMENTE ===");

        btnVerifyEmail.setEnabled(false);
        btnVerifyEmail.setText("Verificando...");

        FirebaseUser currentUser = firebaseManager.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Error: No hay usuario logueado", Toast.LENGTH_SHORT).show();
            resetVerificationButton();
            return;
        }

        firebaseManager.checkEmailVerification(currentUser, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                Log.d(TAG, "✅ Email verificado exitosamente");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        stopEmailVerificationCheck();
                        Toast.makeText(getContext(), "¡Email verificado exitosamente!", Toast.LENGTH_SHORT).show();
                        proceedToCompleteRegistration();
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.d(TAG, "❌ Email aún no verificado: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Por favor, verifica tu email primero", Toast.LENGTH_SHORT).show();
                        resetVerificationButton();
                    });
                }
            }
        });
    }

    private void resetVerificationButton() {
        btnVerifyEmail.setEnabled(true);
        btnVerifyEmail.setText("Verificar Email");
    }

    private void getRegistrationDataAndRegisterInFirebase() {
        // Obtener registrationId si está disponible
        int registrationId = -1;
        if (getArguments() != null && getArguments().containsKey("registrationId")) {
            registrationId = getArguments().getInt("registrationId", -1);
        }

        String userType = "client"; // Valor por defecto

        if (registrationId != -1) {
            // Si tenemos registrationId, obtener datos desde Room
            UserRegistrationRepository repository = new UserRegistrationRepository(getActivity());
            repository.getUserRegistrationById(registrationId, new UserRegistrationRepository.RegistrationCallback() {
                @Override
                public void onSuccess(UserRegistrationEntity registration) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Log.d(TAG, "✅ Datos obtenidos desde Room Database");
                            registerUserInFirebase(registration);
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error obteniendo registro desde Room: " + error);
                    // Fallback al método anterior
                    proceedWithFallbackRegistration();
                }
            });
        } else {
            // Fallback al método anterior si no hay registrationId
            proceedWithFallbackRegistration();
        }
    }

    private void registerUserInFirebase(UserRegistrationEntity registration) {
        Log.d(TAG, "=== REGISTRANDO EN FIREBASE ===");
        Log.d(TAG, "Email: " + registration.email);
        Log.d(TAG, "UserType: " + registration.userType);
        Log.d(TAG, "Nombre: " + registration.nombres + " " + registration.apellidos);

        // Crear UserModel desde UserRegistrationEntity
        UserModel userModel = new UserModel();
        userModel.setUserType(registration.userType);
        userModel.setNombres(registration.nombres);
        userModel.setApellidos(registration.apellidos);
        userModel.setEmail(registration.email);
        userModel.setFechaNacimiento(registration.fechaNacimiento);
        userModel.setTelefono(registration.telefono);
        userModel.setTipoDocumento(registration.tipoDocumento);
        userModel.setNumeroDocumento(registration.numeroDocumento);
        userModel.setDireccion(registration.direccion);
        userModel.setPlacaVehiculo(registration.placaVehiculo);

        // El usuario ya debería estar registrado y logueado desde sendEmailVerificationToUser
        // Solo necesitamos obtener el userId actual
        FirebaseUser currentUser = firebaseManager.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "✅ Usando usuario ya logueado: " + currentUser.getUid());

            // Agregar userId al modelo
            userModel.setUserId(currentUser.getUid());

            // Guardar datos en Firestore
            saveUserDataInFirestore(userModel, registration);
        } else {
            Log.e(TAG, "❌ No hay usuario logueado");
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    btnVerifyEmail.setEnabled(true);
                    btnVerifyEmail.setText("Verificar Email");
                    Toast.makeText(getContext(), "Error: No hay usuario logueado", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void saveUserDataInFirestore(UserModel userModel, UserRegistrationEntity registration) {
        Log.d(TAG, "=== GUARDANDO DATOS EN FIRESTORE ===");

        if ("driver".equals(userModel.getUserType())) {
            // Para taxistas: guardar en colección de pendientes
            firebaseManager.savePendingDriver(userModel.getUserId(), userModel, new FirebaseManager.DataCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "✅ Taxista guardado en pending_drivers");
                    onRegistrationComplete(userModel, registration);
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error guardando taxista: " + error);
                    handleFirestoreError(error);
                }
            });
        } else {
            // Para clientes: guardar directamente en users
            firebaseManager.saveUserData(userModel.getUserId(), userModel, new FirebaseManager.DataCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "✅ Cliente guardado en users");
                    onRegistrationComplete(userModel, registration);
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error guardando cliente: " + error);
                    handleFirestoreError(error);
                }
            });
        }
    }

    private void onRegistrationComplete(UserModel userModel, UserRegistrationEntity registration) {
        Log.d(TAG, "🎉 REGISTRO COMPLETADO - INICIANDO UPLOAD A AWS");

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // Mostrar mensaje de progreso
                btnVerifyEmail.setText("Subiendo archivos...");
                btnVerifyEmail.setEnabled(false);

                // Subir archivos a AWS antes de mostrar éxito
                uploadFilesToAwsAndComplete(userModel, registration);
            });
        }
    }

    private void uploadFilesToAwsAndComplete(UserModel userModel, UserRegistrationEntity registration) {
        Log.d(TAG, "=== SUBIENDO ARCHIVOS A AWS ===");

        // Obtener archivos desde Room Database
        FileStorageRepository fileRepo = new FileStorageRepository(getActivity());

        fileRepo.getFilesByRegistrationId(registration.id, new FileStorageRepository.FileListCallback() {
            @Override
            public void onSuccess(List<FileStorageEntity> files) {
                if (files.isEmpty()) {
                    Log.d(TAG, "No hay archivos para subir, completando registro");
                    completeRegistrationSuccess(userModel, registration);
                    return;
                }

                Log.d(TAG, "Encontrados " + files.size() + " archivos para subir a AWS");
                uploadFilesSequentially(files, 0, userModel, registration);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error obteniendo archivos: " + error);
                // Continuar sin archivos
                completeRegistrationSuccess(userModel, registration);
            }
        });
    }

    private void uploadFilesSequentially(List<FileStorageEntity> files, int currentIndex,
                                         UserModel userModel, UserRegistrationEntity registration) {

        if (currentIndex >= files.size()) {
            Log.d(TAG, "✅ Todos los archivos subidos a AWS exitosamente");
            completeRegistrationSuccess(userModel, registration);
            return;
        }

        FileStorageEntity fileEntity = files.get(currentIndex);
        Log.d(TAG, "Subiendo archivo " + (currentIndex + 1) + "/" + files.size() + ": " + fileEntity.originalName);

        // Actualizar progreso en UI
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                btnVerifyEmail.setText("Subiendo " + (currentIndex + 1) + "/" + files.size() + "...");
            });
        }

        // Crear AwsFileManager
        AwsFileManager awsManager = new AwsFileManager(getActivity());

        // Determinar folder y userId
        String awsFolder = fileEntity.isPdf() ? "documents" : "photos";
        String userId = userModel.getUserId();

        // Crear URI desde el path almacenado
        Uri fileUri = Uri.parse(fileEntity.storedPath);

        awsManager.uploadFileFromPath(fileEntity.storedPath, fileEntity.originalName,
                fileEntity.mimeType, userId, awsFolder, new AwsFileManager.UploadCallback() {
            @Override
            public void onSuccess(AwsFileManager.AwsFileInfo fileInfo) {
                Log.d(TAG, "✅ Archivo subido a AWS: " + fileInfo.s3Key);

                // Actualizar el FileStorageEntity con datos de AWS
                fileEntity.awsS3Key = fileInfo.s3Key;
                fileEntity.awsStoredName = fileInfo.storedName;
                fileEntity.awsETag = fileInfo.etag;
                fileEntity.storedPath = fileInfo.fileUrl; // Actualizar con URL de AWS

                // Guardar cambios en Room Database
                FileStorageRepository fileRepo = new FileStorageRepository(getActivity());
                fileRepo.updateFile(fileEntity, new FileStorageRepository.FileOperationCallback() {
                    @Override
                    public void onSuccess(FileStorageEntity updatedEntity) {
                        Log.d(TAG, "✅ FileEntity actualizado con datos AWS");
                        // Continuar con el siguiente archivo
                        uploadFilesSequentially(files, currentIndex + 1, userModel, registration);
                    }

                    @Override
                    public void onError(String error) {
                        Log.w(TAG, "⚠️ Error actualizando FileEntity: " + error);
                        // Continuar de todos modos
                        uploadFilesSequentially(files, currentIndex + 1, userModel, registration);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error subiendo archivo " + fileEntity.originalName + ": " + error);
                // Continuar con el siguiente archivo (no fallar todo por un archivo)
                uploadFilesSequentially(files, currentIndex + 1, userModel, registration);
            }

            @Override
            public void onProgress(int percentage) {
                Log.d(TAG, "📊 Progreso archivo " + (currentIndex + 1) + ": " + percentage + "%");
            }
        });
    }

    private void completeRegistrationSuccess(UserModel userModel, UserRegistrationEntity registration) {
        Log.d(TAG, "🎉 REGISTRO Y UPLOAD COMPLETADOS EXITOSAMENTE");

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // Mostrar mensaje de éxito
                Toast.makeText(getContext(), "¡Registro completado exitosamente!", Toast.LENGTH_SHORT).show();

                // Navegar a la pantalla de éxito
                Intent intent = new Intent(getActivity(), RegisterSuccessActivity.class);
                intent.putExtra("userType", userModel.getUserType());
                intent.putExtra("registrationId", registration.id);
                intent.putExtra("userName", userModel.getFullName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            });
        }
    }

    private void handleFirestoreError(String error) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                btnVerifyEmail.setEnabled(true);
                btnVerifyEmail.setText("Verificar Email");

                Toast.makeText(getContext(),
                        "Error guardando datos: " + error, Toast.LENGTH_LONG).show();
            });
        }
    }

    private String translateFirebaseError(String error) {
        if (error == null) return "Error desconocido";

        if (error.contains("email-already-in-use")) {
            return "Este correo electrónico ya está registrado. Por favor, usa otro correo o inicia sesión.";
        } else if (error.contains("weak-password")) {
            return "La contraseña es muy débil. Debe tener al menos 6 caracteres.";
        } else if (error.contains("invalid-email")) {
            return "El formato del correo electrónico no es válido.";
        } else if (error.contains("network-request-failed")) {
            return "Error de conexión. Verifica tu conexión a internet e inténtalo de nuevo.";
        } else {
            return "Error en el registro: " + error;
        }
    }

    private void proceedWithFallbackRegistration() {
        if (getActivity() != null) {
            String userType = "client"; // Valor por defecto

            // Intentar obtener desde argumentos
            if (getArguments() != null && getArguments().containsKey("userType")) {
                userType = getArguments().getString("userType", "client");
                Log.d(TAG, "UserType from arguments: " + userType);
            }
            // Intentar desde ViewModel
            else {
                try {
                    RegisterViewModel viewModel = new ViewModelProvider(getActivity()).get(RegisterViewModel.class);
                    String vmUserType = viewModel.getUserType();
                    if (vmUserType != null && !vmUserType.isEmpty()) {
                        userType = vmUserType;
                        Log.d(TAG, "UserType from ViewModel: " + userType);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error getting userType from ViewModel", e);
                }
            }

            // Intentar desde SharedPreferences como último recurso
            if ("client".equals(userType)) {
                String spUserType = getActivity().getSharedPreferences("UserData", getActivity().MODE_PRIVATE)
                        .getString("userType", "client");
                if (!"client".equals(spUserType)) {
                    userType = spUserType;
                    Log.d(TAG, "UserType from SharedPreferences: " + userType);
                }
            }

            Log.d(TAG, "Final userType being sent to RegisterSuccessActivity: " + userType);

            // FALLBACK: Por ahora, mostrar mensaje de que necesitamos implementar Firebase
            getActivity().runOnUiThread(() -> {
                btnVerifyEmail.setEnabled(true);
                btnVerifyEmail.setText("Verificar Email");

                Toast.makeText(getContext(),
                        "Funcionalidad en desarrollo. Contacta al administrador.", Toast.LENGTH_LONG).show();
            });
        }
    }

    private void resendCode() {
        Log.d(TAG, "=== REENVIANDO EMAIL DE VERIFICACIÓN ===");

        FirebaseUser currentUser = firebaseManager.getCurrentUser();
        if (currentUser != null) {
            firebaseManager.sendEmailVerification(currentUser, new FirebaseManager.DataCallback() {
                @Override
                public void onSuccess() {
                    emailVerificationSent = true;
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Email de verificación reenviado a " + maskEmail(currentUser.getEmail()), Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error reenviando email: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), "Error: No hay usuario logueado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopEmailVerificationCheck();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopEmailVerificationCheck();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isCheckingVerification) {
            initializeEmailVerificationCheck();
        }
    }

    private void checkEmailVerificationManually() {
        Log.d(TAG, "=== VERIFICACIÓN MANUAL SOLICITADA ===");

        btnVerifyEmail.setEnabled(false);
        btnVerifyEmail.setText("Verificando...");

        FirebaseUser currentUser = firebaseManager.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "❌ No hay usuario logueado para verificar");
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error: No hay usuario logueado", Toast.LENGTH_SHORT).show();
                    resetVerifyButton();
                });
            }
            return;
        }

        // Recargar el usuario desde Firebase para obtener el estado más actual
        currentUser.reload().addOnCompleteListener(task -> {
            if (task.isSuccessful() && currentUser.isEmailVerified()) {
                Log.d(TAG, "✅ Email verificado exitosamente");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        stopEmailVerificationCheck();
                        Toast.makeText(getContext(), "¡Email verificado exitosamente!", Toast.LENGTH_SHORT).show();
                        proceedToCompleteRegistration();
                    });
                }
            } else {
                Log.d(TAG, "❌ Email aún no verificado");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                                "Por favor, revisa tu email y haz clic en el enlace de verificación",
                                Toast.LENGTH_LONG).show();
                        resetVerifyButton();
                    });
                }
            }
        });
    }

    private void resetVerifyButton() {
        btnVerifyEmail.setEnabled(true);
        btnVerifyEmail.setText("Verificar Email");
    }

    private void proceedToCompleteRegistration() {
        // Tu lógica existente para completar el registro
        getRegistrationDataAndRegisterInFirebase();
    }
}