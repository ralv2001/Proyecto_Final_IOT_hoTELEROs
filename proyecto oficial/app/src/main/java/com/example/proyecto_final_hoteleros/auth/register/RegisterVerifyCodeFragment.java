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
    
    import androidx.activity.OnBackPressedCallback;
    
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
    
        private boolean emailAlreadySent = false;
    
        private Handler resendHandler; // Ya está comentado, descoméntalo
    
        // Variables para cooldown de reenvío
    
    
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
            btnVerifyEmail = view.findViewById(R.id.btnVerifyEmail);
            btnVerifyEmail.setText("Verificar Email");
            tvInstructions = view.findViewById(R.id.tvInstructions);
    
            tvEmailSent = view.findViewById(R.id.tvEmailSent);
            TextView tvTitle = view.findViewById(R.id.tvTitle);
            tvTitle.setText("Verifica tu correo electrónico");
    
    //        ImageButton btnBack = view.findViewById(R.id.btnBack);
    
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
    
            // ❌ ELIMINAR ESTA LÍNEA (está duplicada en hideCodeInputsAndShowEmailMessage)
            // sendEmailVerificationToUser();  <-- ELIMINAR ESTA LÍNEA
    
            // Inicializar verificación automática
            initializeEmailVerificationCheck();
    
            btnVerifyEmail.setOnClickListener(v -> {
                Log.d(TAG, "=== BOTÓN VERIFICAR EMAIL PRESIONADO ===");
    
                // Cambiar estado del botón a "Verificando..." con spinner
                btnVerifyEmail.setEnabled(false);
                btnVerifyEmail.setText("Verificando...");
    
                FirebaseUser currentUser = firebaseManager.getCurrentUser();
                if (currentUser != null) {
                    Log.d(TAG, "Usuario actual: " + currentUser.getEmail());
                    Log.d(TAG, "Email verificado (estado actual): " + currentUser.isEmailVerified());
    
                    // Forzar reload y luego verificar
                    firebaseManager.checkEmailVerification(currentUser, new FirebaseManager.AuthCallback() {
                        @Override
                        public void onSuccess(String userId) {
                            Log.d(TAG, "✅ Email verificado exitosamente!");
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    btnVerifyEmail.setText("¡Verificado! Completando...");
                                    Toast.makeText(getContext(), "¡Email verificado exitosamente!", Toast.LENGTH_SHORT).show();
    
                                    // Esperar un poco y luego proceder
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        handleEmailVerificationComplete();
                                    }, 1000);
                                });
                            }
                        }
    
                        @Override
                        public void onError(String error) {
                            Log.d(TAG, "❌ Email no verificado aún: " + error);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    btnVerifyEmail.setEnabled(false);
                                    btnVerifyEmail.setText("Verificando...");
    
                                    Toast.makeText(getContext(),
                                            "Por favor, asegúrate de haber hecho clic en el enlace de tu correo electrónico",
                                            Toast.LENGTH_LONG).show();
                                });
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "❌ No hay usuario logueado");
                    Toast.makeText(getContext(), "Error: No hay usuario logueado", Toast.LENGTH_SHORT).show();
    
                    // Resetear botón
                    btnVerifyEmail.setEnabled(false);
                    btnVerifyEmail.setText("Verificando...");
                }
            });
    
    //        // Botón temporal para debugging - ELIMINAR DESPUÉS
    //        MaterialButton btnDebugCheck = view.findViewById(R.id.btnDebugCheck);
    //        btnDebugCheck.setOnClickListener(v -> {
    //            FirebaseUser currentUser = firebaseManager.getCurrentUser();
    //            if (currentUser != null) {
    //                Log.d(TAG, "=== DEBUG INFO ===");
    //                Log.d(TAG, "Email: " + currentUser.getEmail());
    //                Log.d(TAG, "Email verificado (antes del reload): " + currentUser.isEmailVerified());
    //                Log.d(TAG, "UID: " + currentUser.getUid());
    //
    //                // Forzar reload
    //                currentUser.reload().addOnCompleteListener(task -> {
    //                    if (task.isSuccessful()) {
    //                        Log.d(TAG, "Email verificado (después del reload): " + currentUser.isEmailVerified());
    //
    //                        if (getActivity() != null) {
    //                            getActivity().runOnUiThread(() -> {
    //                                String status = currentUser.isEmailVerified() ? "VERIFICADO ✅" : "NO VERIFICADO ❌";
    //                                Toast.makeText(getContext(),
    //                                        "Estado: " + status + "\nEmail: " + currentUser.getEmail(),
    //                                        Toast.LENGTH_LONG).show();
    //                            });
    //                        }
    //                    } else {
    //                        Log.e(TAG, "Error en reload: " + task.getException());
    //                        if (getActivity() != null) {
    //                            getActivity().runOnUiThread(() -> {
    //                                Toast.makeText(getContext(), "Error recargando usuario", Toast.LENGTH_SHORT).show();
    //                            });
    //                        }
    //                    }
    //                });
    //            } else {
    //                Log.e(TAG, "No hay usuario logueado");
    //                Toast.makeText(getContext(), "No hay usuario logueado", Toast.LENGTH_SHORT).show();
    //            }
    //        });
    
            return view;
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
    
            // Actualizar instrucciones usando el TextView existente (sin duplicar)
            if (tvEmailSent != null) {
                tvEmailSent.setText("Email de verificación enviado a " + maskEmail(email));
            }
    
            // Actualizar las instrucciones en el TextView correspondiente
            if (tvInstructions != null) {
                tvInstructions.setText("Haz clic en el enlace del email de verificación y luego presiona 'Verificar Email'");
            }
    
            // Enviar email de verificación automáticamente
            // ✅ SOLO UNA LLAMADA AQUÍ
            sendEmailVerificationToUser();
    
            // Inicializar verificación automática
            initializeEmailVerificationCheck();
    
            // Deshabilitar botón inicialmente con estado "Verificando..."
            btnVerifyEmail.setEnabled(false);
            btnVerifyEmail.setText("Verificando...");
            btnVerifyEmail.setAlpha(0.6f);
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
            Log.d(TAG, "=== VERIFICANDO SI NECESITA ENVIAR EMAIL ===");
    
            // Verificar si ya se envió el email para evitar spam
            if (emailAlreadySent) {
                Log.d(TAG, "Email ya fue enviado, saltando...");
                return;
            }
    
            // Obtener datos del registro
            int registrationId = -1;
            if (getArguments() != null) {
                registrationId = getArguments().getInt("registrationId", -1);
            }
    
            if (registrationId != -1) {
                Log.d(TAG, "Obteniendo datos de registro con ID: " + registrationId);
    
                UserRegistrationRepository repository = new UserRegistrationRepository(getContext());
                repository.getUserRegistrationById(registrationId, new UserRegistrationRepository.RegistrationCallback() {
                    @Override
                    public void onSuccess(UserRegistrationEntity registration) {
                        Log.d(TAG, "=== DATOS DE REGISTRO OBTENIDOS ===");
                        Log.d(TAG, "Email: " + registration.email);
    
                        // PASO 1: Verificar si el email ya existe
                        checkEmailAndProceed(registration);
                    }
    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error obteniendo registro: " + error);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
            } else {
                Log.e(TAG, "No hay registrationId válido");
            }
        }
    
        private void checkEmailAndProceed(UserRegistrationEntity registration) {
            Log.d(TAG, "=== VERIFICANDO SI EMAIL YA EXISTE ===");
    
            firebaseManager.checkIfEmailExists(registration.email, new FirebaseManager.AuthCallback() {
                @Override
                public void onSuccess(String result) {
                    // Email ya existe
                    Log.d(TAG, "✅ Email ya existe, haciendo login...");
                    loginExistingUserAndSendVerification(registration.email, registration.password);
                }
    
                @Override
                public void onError(String error) {
                    if ("EMAIL_NOT_EXISTS".equals(error)) {
                        // Email no existe, crear cuenta nueva
                        Log.d(TAG, "✅ Email no existe, creando cuenta nueva...");
                        createNewFirebaseUserAndSendEmail(registration);
                    } else {
                        // Error real
                        Log.e(TAG, "❌ Error verificando email: " + error);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Error verificando email: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                }
            });
        }
    
        private void createNewFirebaseUserAndSendEmail(UserRegistrationEntity registration) {
            Log.d(TAG, "=== CREANDO NUEVA CUENTA FIREBASE ===");
    
            firebaseManager.registerUser(registration.email, registration.password, new FirebaseManager.AuthCallback() {
                @Override
                public void onSuccess(String userId) {
                    Log.d(TAG, "✅ Nueva cuenta creada en Firebase: " + userId);
                    sendVerificationEmailToCurrentUser();
                }
    
                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error creando nueva cuenta: " + error);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), translateFirebaseError(error), Toast.LENGTH_LONG).show();
                        });
                    }
                }
            });
        }
    
        private void loginExistingUserAndSendVerification(String email, String password) {
            Log.d(TAG, "=== HACIENDO LOGIN A CUENTA EXISTENTE ===");
    
            firebaseManager.loginUser(email, password, new FirebaseManager.AuthCallback() {
                @Override
                public void onSuccess(String userId) {
                    Log.d(TAG, "✅ Login exitoso a cuenta existente: " + userId);
    
                    // Verificar si ya está verificado
                    FirebaseUser currentUser = firebaseManager.getCurrentUser();
                    if (currentUser != null && currentUser.isEmailVerified()) {
                        Log.d(TAG, "✅ Email ya está verificado!");
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Email ya verificado", Toast.LENGTH_SHORT).show();
                                handleEmailVerificationComplete();
                            });
                        }
                    } else {
                        // Solo enviar verificación si no está verificado Y no se ha enviado ya
                        if (!emailAlreadySent) {
                            sendVerificationEmailToCurrentUser();
                        } else {
                            Log.d(TAG, "Email de verificación ya fue enviado anteriormente");
                        }
                    }
                }
    
                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error en login a cuenta existente: " + error);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error: " + translateFirebaseError(error), Toast.LENGTH_LONG).show();
                        });
                    }
                }
            });
        }
    
        private void sendVerificationEmailToCurrentUser() {
            FirebaseUser currentUser = firebaseManager.getCurrentUser();
            if (currentUser != null && !emailAlreadySent) {
                Log.d(TAG, "Enviando email de verificación a: " + currentUser.getEmail());
    
                firebaseManager.sendEmailVerification(currentUser, new FirebaseManager.DataCallback() {
                    @Override
                    public void onSuccess() {
                        emailAlreadySent = true;
                        Log.d(TAG, "✅ Email de verificación enviado exitosamente");
    
                        // Programar limpieza automática
                        scheduleUnverifiedUserCleanup();
    
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
            } else {
                Log.d(TAG, "Email ya fue enviado anteriormente");
            }
        }
    
        private void initializeEmailVerificationCheck() {
            Log.d(TAG, "=== INICIALIZANDO CHECK DE VERIFICACIÓN DE EMAIL ===");
    
            if (isCheckingVerification) {
                Log.d(TAG, "Ya hay un check en progreso, saltando...");
                return;
            }
    
            isCheckingVerification = true;
            verificationHandler = new Handler(Looper.getMainLooper());
    
            verificationRunnable = new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null || !isAdded()) {
                        Log.d(TAG, "Fragment no está activo, deteniendo verificación");
                        isCheckingVerification = false;
                        return;
                    }
    
                    Log.d(TAG, "Verificando estado del email...");
    
                    FirebaseUser currentUser = firebaseManager.getCurrentUser();
                    if (currentUser != null) {
                        firebaseManager.checkEmailVerification(currentUser, new FirebaseManager.AuthCallback() {
                            @Override
                            public void onSuccess(String userId) {
                                if (getActivity() != null && isAdded()) {
                                    Log.d(TAG, "¡Email verificado! Habilitando botón...");
    
                                    // Mostrar spinner y texto "Verificando..."
                                    getActivity().runOnUiThread(() -> {
                                        btnVerifyEmail.setEnabled(false);
                                        btnVerifyEmail.setText("Verificando...");
                                        // El spinner ya está en el XML
                                    });
    
                                    // Esperar un poco para mostrar el estado de "verificando" y luego proceder
                                    verificationHandler.postDelayed(() -> {
                                        if (getActivity() != null && isAdded()) {
                                            // Detener el check periódico
                                            isCheckingVerification = false;
                                            // Proceder con la creación del usuario
                                            handleEmailVerificationComplete();
                                        }
                                    }, 1500); // 1.5 segundos de delay para mostrar "Verificando..."
                                }
                            }
    
                            @Override
                            public void onError(String error) {
                                Log.d(TAG, "Email aún no verificado (check automático)");
                                if (getActivity() != null && isAdded()) {
                                    // Continuar verificando cada 3 segundos
                                    verificationHandler.postDelayed(verificationRunnable, CHECK_INTERVAL);
                                }
                            }
                        });
                    } else {
                        Log.e(TAG, "No hay usuario logueado");
                        // Continuar verificando
                        verificationHandler.postDelayed(verificationRunnable, CHECK_INTERVAL);
                    }
                }
            };
    
            // Iniciar el primer check después de 2 segundos
            verificationHandler.postDelayed(verificationRunnable, 2000);
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
                    debugRegistrationFlow();
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
                            Log.d(TAG, "📸 URL del archivo: " + fileInfo.fileUrl);
    
                            // Actualizar el FileStorageEntity con datos de AWS
                            fileEntity.awsS3Key = fileInfo.s3Key;
                            fileEntity.awsStoredName = fileInfo.storedName;
                            fileEntity.awsETag = fileInfo.etag;
                            fileEntity.storedPath = fileInfo.fileUrl; // Actualizar con URL de AWS
    
                            // 🚀 NUEVO: Actualizar UserModel con URLs de AWS
                            if (fileEntity.isPdf()) {
                                // Es un documento PDF
                                userModel.setDocumentUrl(fileInfo.fileUrl);
                                Log.d(TAG, "📄 URL de documento PDF guardada: " + fileInfo.fileUrl);
                            } else {
                                // Es una foto
                                userModel.setPhotoUrl(fileInfo.fileUrl);
                                Log.d(TAG, "📷 URL de foto guardada: " + fileInfo.fileUrl);
                            }
    
                            // Guardar cambios en Room Database
                            FileStorageRepository fileRepo = new FileStorageRepository(getActivity());
                            fileRepo.updateFile(fileEntity, new FileStorageRepository.FileOperationCallback() {
                                @Override
                                public void onSuccess(FileStorageEntity updatedEntity) {
                                    Log.d(TAG, "✅ FileEntity actualizado con datos AWS");
    
                                    // 🚀 NUEVO: Actualizar Firebase con las nuevas URLs
                                    updateFirebaseWithUrls(userModel, () -> {
                                        // Continuar con el siguiente archivo solo después de actualizar Firebase
                                        uploadFilesSequentially(files, currentIndex + 1, userModel, registration);
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e(TAG, "❌ Error subiendo archivo " + (currentIndex + 1) + ": " + error);

                                    // ✅ MEJORAR MANEJO DE ERRORES
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            btnVerifyEmail.setText("Error: " + error);
                                            btnVerifyEmail.setEnabled(true);

                                            // Mostrar toast con el error específico
                                            Toast.makeText(getActivity(),
                                                    "Error subiendo " + fileEntity.originalName + ": " + error,
                                                    Toast.LENGTH_LONG).show();
                                        });
                                    }

                                    // ✅ NO CONTINUAR con otros archivos si hay error crítico
                                    if (error.contains("Error de conexión") || error.contains("Error del servidor")) {
                                        Log.e(TAG, "❌ Error crítico, deteniendo upload");
                                        return;
                                    }

                                    // Continuar con siguiente archivo solo si es error recuperable
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
    
            // Inicializar verificación de email si no está en progreso
            if (!isCheckingVerification) {
                initializeEmailVerificationCheck();
            }
    
            // Bloquear el botón de atrás físico de Android
            requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    // No hacer nada - esto bloquea el botón de atrás
                }
            });
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
    
        @Override
        public void onDestroyView() {
            super.onDestroyView();
    
            // Limpiar handlers para evitar memory leaks
            if (verificationHandler != null && verificationRunnable != null) {
                verificationHandler.removeCallbacks(verificationRunnable);
            }
    
            if (resendHandler != null) {
                resendHandler.removeCallbacksAndMessages(null);
            }
    
            isCheckingVerification = false;
        }
    
        private void handleEmailVerificationComplete() {
            Log.d(TAG, "=== MANEJANDO VERIFICACIÓN COMPLETA ===");
    
            // Detener el check automático
            isCheckingVerification = false;
            if (verificationHandler != null && verificationRunnable != null) {
                verificationHandler.removeCallbacks(verificationRunnable);
            }
    
            // Cambiar estado del botón
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    btnVerifyEmail.setText("Completando registro...");
                    btnVerifyEmail.setEnabled(false);
                });
            }
    
            // Proceder con el registro
            proceedToCompleteRegistration();
        }
    
        private void showExitConfirmationDialog() {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("¿Salir del registro?")
                    .setMessage("Si sales ahora, perderás el progreso del registro. ¿Estás seguro?")
                    .setPositiveButton("Sí, salir", (dialog, which) -> {
                        // Permitir salir y limpiar el usuario no verificado
                        cleanupUnverifiedUserAndExit();
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setCancelable(false)
                    .show();
        }
    
        private void cleanupUnverifiedUserAndExit() {
            Log.d(TAG, "=== LIMPIANDO USUARIO NO VERIFICADO ===");
    
            FirebaseUser currentUser = firebaseManager.getCurrentUser();
            if (currentUser != null && !currentUser.isEmailVerified()) {
                Log.d(TAG, "Eliminando usuario no verificado: " + currentUser.getEmail());
    
                currentUser.delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "✅ Usuario no verificado eliminado");
                    } else {
                        Log.e(TAG, "❌ Error eliminando usuario: " + task.getException());
                    }
    
                    // Navegar hacia atrás independientemente del resultado
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            navigateBack();
                        });
                    }
                });
            } else {
                // Si no hay usuario o ya está verificado, solo navegar
                navigateBack();
            }
        }
    
        private void navigateBack() {
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
        }
    
        private void scheduleUnverifiedUserCleanup() {
            Log.d(TAG, "=== PROGRAMANDO LIMPIEZA DE USUARIO NO VERIFICADO ===");
    
            FirebaseUser currentUser = firebaseManager.getCurrentUser();
            if (currentUser == null) return;
    
            // ✅ CAPTURAR EL UID ESPECÍFICO ahora
            String userIdToClean = currentUser.getUid();
            String emailToClean = currentUser.getEmail();
    
            Log.d(TAG, "Programando limpieza para usuario: " + emailToClean + " (UID: " + userIdToClean + ")");
    
            // Limpiar después de 10 minutos
            Handler cleanupHandler = new Handler(Looper.getMainLooper());
            cleanupHandler.postDelayed(() -> {
                // ✅ Verificar si el usuario específico aún existe y no está verificado
                cleanupSpecificUser(userIdToClean, emailToClean);
            }, 10 * 60 * 1000); // 10 minutos
        }
    
        private void cleanupSpecificUser(String userIdToCheck, String emailToCheck) {
            Log.d(TAG, "⏰ Verificando si debe limpiar usuario: " + emailToCheck);
    
            FirebaseUser currentUser = firebaseManager.getCurrentUser();
    
            // Solo eliminar si:
            // 1. Hay un usuario logueado
            // 2. Es exactamente el mismo usuario (mismo UID)
            // 3. No está verificado
            if (currentUser != null &&
                    currentUser.getUid().equals(userIdToCheck) &&
                    !currentUser.isEmailVerified()) {
    
                Log.d(TAG, "🗑️ Eliminando usuario no verificado: " + emailToCheck);
    
                currentUser.delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "✅ Usuario no verificado eliminado por timeout: " + emailToCheck);
                    } else {
                        Log.e(TAG, "❌ Error eliminando usuario por timeout: " + task.getException());
                    }
                });
            } else {
                Log.d(TAG, "ℹ️ Usuario ya verificado o cambiado, no se elimina: " + emailToCheck);
            }
        }
    
    
        /**
         * Actualizar Firebase Firestore con las URLs de AWS
         */
        private void updateFirebaseWithUrls(UserModel userModel, Runnable onComplete) {
            Log.d(TAG, "🔄 Actualizando Firebase con URLs de AWS...");

            // ✅ VALIDAR URLs ANTES DE ACTUALIZAR
            Log.d(TAG, "🔍 Validando URLs antes de actualizar Firebase:");
            Log.d(TAG, "  PhotoURL: " + (userModel.getPhotoUrl() != null ? userModel.getPhotoUrl() : "null"));
            Log.d(TAG, "  DocumentURL: " + (userModel.getDocumentUrl() != null ? userModel.getDocumentUrl() : "null"));

            // Verificar que las URLs sean válidas URLs de AWS
            if (userModel.getPhotoUrl() != null && !userModel.getPhotoUrl().startsWith("https://")) {
                Log.w(TAG, "⚠️ PhotoURL no es una URL válida: " + userModel.getPhotoUrl());
                userModel.setPhotoUrl(null);
            }

            if (userModel.getDocumentUrl() != null && !userModel.getDocumentUrl().startsWith("https://")) {
                Log.w(TAG, "⚠️ DocumentURL no es una URL válida: " + userModel.getDocumentUrl());
                userModel.setDocumentUrl(null);
            }
    
            if ("driver".equals(userModel.getUserType())) {
                // Para taxistas: actualizar en pending_drivers
                firebaseManager.updatePendingDriverUrls(userModel.getUserId(),
                        userModel.getPhotoUrl(), userModel.getDocumentUrl(),
                        new FirebaseManager.DataCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "✅ URLs actualizadas en pending_drivers");
                                if (onComplete != null) onComplete.run();
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "❌ Error actualizando URLs en Firebase: " + error);
                                // Continuar de todos modos
                                if (onComplete != null) onComplete.run();
                            }
                        });
            } else {
                // Para clientes: actualizar en users
                firebaseManager.updateUserUrls(userModel.getUserId(),
                        userModel.getPhotoUrl(), userModel.getDocumentUrl(),
                        new FirebaseManager.DataCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "✅ URLs actualizadas en users");
                                if (onComplete != null) onComplete.run();
                            }
    
                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "❌ Error actualizando URLs en Firebase: " + error);
                                // Continuar de todos modos
                                if (onComplete != null) onComplete.run();
                            }
                        });
            }
        }

        // ✅ MÉTODO PARA DEBUGGEAR FLUJO COMPLETO
        private void debugRegistrationFlow() {
            Log.d(TAG, "=== 🔍 DEBUG REGISTRATION FLOW ===");

            FirebaseUser currentUser = firebaseManager.getCurrentUser();
            if (currentUser != null) {
                Log.d(TAG, "👤 Usuario actual: " + currentUser.getEmail());
                Log.d(TAG, "🔑 UID: " + currentUser.getUid());
                Log.d(TAG, "✅ Email verificado: " + currentUser.isEmailVerified());
            }

            // Debug archivos en Room
            FileStorageRepository fileRepo = new FileStorageRepository(getActivity());

            // Obtener registrationId desde argumentos
            int registrationId = -1;
            if (getArguments() != null) {
                registrationId = getArguments().getInt("registrationId", -1);
            }

            if (registrationId != -1) {
                final int finalRegistrationId = registrationId;
                fileRepo.getFilesByRegistrationId(finalRegistrationId, new FileStorageRepository.FileListCallback() {
                    @Override
                    public void onSuccess(List<FileStorageEntity> files) {
                        Log.d(TAG, "📁 Archivos en Room para registro " + finalRegistrationId + ": " + files.size());
                        for (FileStorageEntity file : files) {
                            Log.d(TAG, "  - " + file.originalName + " | AWS URL: " +
                                    (file.storedPath != null && file.storedPath.startsWith("https://") ? "✅" : "❌ " + file.storedPath));
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "❌ Error obteniendo archivos: " + error);
                    }
                });
            } else {
                Log.w(TAG, "⚠️ No hay registrationId disponible para debug");
            }
        }
    
    
    }