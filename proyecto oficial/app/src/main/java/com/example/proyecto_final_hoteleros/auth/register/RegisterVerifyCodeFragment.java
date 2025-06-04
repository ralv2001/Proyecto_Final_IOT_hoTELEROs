package com.example.proyecto_final_hoteleros.auth.register;

import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.auth.register.AddProfilePhotoActivity;
import com.google.android.material.button.MaterialButton;
import com.example.proyecto_final_hoteleros.repository.UserRegistrationRepository;
import com.example.proyecto_final_hoteleros.database.entities.UserRegistrationEntity;

// ========== NUEVOS IMPORTS FIREBASE ==========
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;
import com.example.proyecto_final_hoteleros.models.UserModel;

public class RegisterVerifyCodeFragment extends Fragment {

    private static final String TAG = "RegisterVerifyCodeFragment";

    private EditText etCode1, etCode2, etCode3, etCode4, etCode5;
    private MaterialButton btnRegister;
    private TextView tvEmailSent, tvResendCode;
    private String email;

    // ========== FIREBASE ==========
    private FirebaseManager firebaseManager;

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

        // Inicializar vistas
        etCode1 = view.findViewById(R.id.etCode1);
        etCode2 = view.findViewById(R.id.etCode2);
        etCode3 = view.findViewById(R.id.etCode3);
        etCode4 = view.findViewById(R.id.etCode4);
        etCode5 = view.findViewById(R.id.etCode5);
        btnRegister = view.findViewById(R.id.btnVerifyCode);
        btnRegister.setText("Registrarse");  // Cambiar el texto del botón a "Registrarse"

        tvEmailSent = view.findViewById(R.id.tvEmailSent);
        tvResendCode = view.findViewById(R.id.tvResendCode);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText("Revise su correo electrónico");

        ImageButton btnBack = view.findViewById(R.id.btnBack);

        // Configurar el texto del email enmascarado
        if (!email.isEmpty()) {
            String maskedEmail = maskEmail(email);
            Log.d(TAG, "Masked email: " + maskedEmail);
            tvEmailSent.setText("Hemos enviado un código a " + maskedEmail);
        } else {
            Log.e(TAG, "Email is empty!");
        }

        // Configurar el focus automático
        setupCodeInputs();

        // Configurar el botón de verificación
        btnRegister.setOnClickListener(v -> {
            String code = getCompleteCode();
            if (code.length() == 5) {
                verifyCodeAndRegister(code);
            } else {
                Toast.makeText(getContext(), "Por favor, ingrese el código completo", Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar el reenvío de código
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
                tvEmailSent.setText("Hemos enviado un código a " + maskedEmail);
            }
        }
    }

    private void setupCodeInputs() {
        // Asignar TextWatchers a cada EditText para moverse al siguiente cuando se ingresa un dígito
        EditText[] editTexts = new EditText[]{etCode1, etCode2, etCode3, etCode4, etCode5};

        for (int i = 0; i < editTexts.length; i++) {
            final int currentIndex = i;
            editTexts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    // Si se ingresó un dígito y no es el último campo, mover al siguiente
                    if (s.length() == 1 && currentIndex < editTexts.length - 1) {
                        editTexts[currentIndex + 1].requestFocus();
                    }

                    // Verificar si todos los campos están llenos para habilitar el botón
                    checkAllFieldsFilled();
                }
            });

            // Para permitir borrar con tecla de retroceso y moverse al campo anterior
            editTexts[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && currentIndex > 0 &&
                        editTexts[currentIndex].getText().toString().isEmpty()) {
                    // Si el campo actual está vacío y se presiona borrar, ir al campo anterior
                    editTexts[currentIndex - 1].requestFocus();
                    editTexts[currentIndex - 1].setText("");
                    return true;
                }
                return false;
            });
        }

        // Enfocar el primer campo al iniciar
        etCode1.requestFocus();
    }

    private void checkAllFieldsFilled() {
        boolean allFilled = !etCode1.getText().toString().isEmpty() &&
                !etCode2.getText().toString().isEmpty() &&
                !etCode3.getText().toString().isEmpty() &&
                !etCode4.getText().toString().isEmpty() &&
                !etCode5.getText().toString().isEmpty();

        btnRegister.setEnabled(allFilled);
        btnRegister.setAlpha(allFilled ? 1.0f : 0.4f);
    }

    private String getCompleteCode() {
        return etCode1.getText().toString() +
                etCode2.getText().toString() +
                etCode3.getText().toString() +
                etCode4.getText().toString() +
                etCode5.getText().toString();
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

    // ========== NUEVO MÉTODO CON FIREBASE ==========
    private void verifyCodeAndRegister(String code) {
        Log.d(TAG, "=== INICIANDO VERIFICACIÓN Y REGISTRO CON FIREBASE ===");

        // TODO: Implementar la validación real del código
        // Por ahora, cualquier código de 5 dígitos se considera válido para testing
        if (code.length() == 5) {
            // Mostrar loading
            btnRegister.setEnabled(false);
            btnRegister.setText("Registrando...");

            // Obtener datos del registro desde Room
            getRegistrationDataAndRegisterInFirebase();
        } else {
            Toast.makeText(getContext(), "Código incorrecto. Por favor, inténtelo de nuevo.", Toast.LENGTH_SHORT).show();
        }
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

        // Registrar usuario en Firebase Auth
        firebaseManager.registerUser(registration.email, registration.password, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                Log.d(TAG, "✅ Usuario registrado en Firebase Auth: " + userId);

                // Agregar userId al modelo
                userModel.setUserId(userId);

                // Guardar datos en Firestore
                saveUserDataInFirestore(userModel, registration);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error registrando en Firebase Auth: " + error);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Registrarse");

                        String userFriendlyError = translateFirebaseError(error);
                        Toast.makeText(getContext(), userFriendlyError, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
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
        Log.d(TAG, "🎉 REGISTRO COMPLETADO EXITOSAMENTE");

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
                btnRegister.setEnabled(true);
                btnRegister.setText("Registrarse");

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
                btnRegister.setEnabled(true);
                btnRegister.setText("Registrarse");

                Toast.makeText(getContext(),
                        "Funcionalidad en desarrollo. Contacta al administrador.", Toast.LENGTH_LONG).show();
            });
        }
    }

    private void resendCode() {
        // TODO: Implementar lógica para reenviar el código
        Toast.makeText(getContext(), "Se ha reenviado el código a tu correo", Toast.LENGTH_SHORT).show();

        // Limpiar los campos de código
        etCode1.setText("");
        etCode2.setText("");
        etCode3.setText("");
        etCode4.setText("");
        etCode5.setText("");

        // Volver a poner el foco en el primer campo
        etCode1.requestFocus();
    }
}