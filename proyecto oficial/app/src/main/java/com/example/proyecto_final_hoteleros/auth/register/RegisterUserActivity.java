package com.example.proyecto_final_hoteleros.auth.register;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_final_hoteleros.AuthActivity;
import com.example.proyecto_final_hoteleros.R;
// Imports para Room Database y Repositorios
import com.example.proyecto_final_hoteleros.database.entities.UserRegistrationEntity;
import com.example.proyecto_final_hoteleros.repository.UserRegistrationRepository;
import com.example.proyecto_final_hoteleros.repository.FileStorageRepository;
import com.example.proyecto_final_hoteleros.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class RegisterUserActivity extends AppCompatActivity {

    private RegisterViewModel mViewModel;
    private String userType = "client"; // Valor por defecto

    // Pestañas
    private TextView tvLoginTab;
    private TextView tvRegisterTab;
    private View viewTabIndicatorLogin;
    private View viewTabIndicatorRegister;

    // Campos de texto
    private EditText etNombres, etApellidos, etEmail, etFechaNacimiento, etTelefono, etNumeroDocumento, etDireccion, etPlacaVehiculo;
    private Button btnContinuar;
    private TextView tvDocType;
    private TextView tvPlacaVehiculoLabel;

    // Constantes para tipos de documento
    private static final String DOC_TYPE_DNI = "DNI";
    private static final String DOC_TYPE_CE = "CE";
    private String currentDocType = DOC_TYPE_DNI; // Por defecto DNI

    // Calendario para guardar la fecha seleccionada
    private final Calendar calendar = Calendar.getInstance();

    // Formato para la fecha
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Lista de dominios válidos para correo electrónico
    private final List<String> VALID_EMAIL_DOMAINS = Arrays.asList(
            "gmail.com", "hotmail.com", "yahoo.es", "pucp.edu.pe", "outlook.com",
            "icloud.com", "yahoo.com", "live.com", "msn.com", "protonmail.com",
            "yahoo.com.mx", "hotmail.es", "me.com", "aol.com", "mail.com"
    );

    // Variables para la selección de país
    private ImageView ivCountryFlag;
    private TextView tvCountryCode;
    private LinearLayout countryCodeContainer;

    // Constantes para los códigos de país
    private static final String COUNTRY_CODE_PE = "(+51) ";
    private static final String COUNTRY_CODE_CO = "(+57) ";
    private static final String COUNTRY_CODE_VE = "(+58) ";
    private String currentCountryCode = COUNTRY_CODE_PE; // Por defecto Perú

    // Campos para contraseña
    private EditText etContrasena, etConfirmarContrasena;
    private ImageButton ibTogglePassword, ibToggleConfirmPassword;
    private ImageView passwordStrengthBar;
    private ImageView ivReq1Icon, ivReq2Icon, ivReq3Icon;
    private TextView tvReq1, tvReq2, tvReq3;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    // Agregar estas nuevas variables aquí:
    private UserRegistrationRepository userRegistrationRepository;
    private FileStorageRepository fileStorageRepository;
    private NotificationHelper notificationHelper;
    private int currentRegistrationId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sistema_activity_register_user);

        // Obtener el tipo de usuario del intent
        if (getIntent() != null && getIntent().hasExtra("userType")) {
            userType = getIntent().getStringExtra("userType");
        }

        // Inicializar el ViewModel
        mViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // Inicializar repositorios
        userRegistrationRepository = new UserRegistrationRepository(this);
        fileStorageRepository = new FileStorageRepository(this);
        notificationHelper = new NotificationHelper(this);

        // Recuperar registro existente si existe
        recoverExistingRegistration();

        // Inicializar las pestañas
        tvLoginTab = findViewById(R.id.tvLoginTab);
        tvRegisterTab = findViewById(R.id.tvRegisterTab);
        viewTabIndicatorLogin = findViewById(R.id.viewTabIndicatorLogin);
        viewTabIndicatorRegister = findViewById(R.id.viewTabIndicatorRegister);

        // Configurar clic en pestaña "Iniciar Sesión"
        tvLoginTab.setOnClickListener(v -> {
            // Limpiar datos al cambiar a login
            getSharedPreferences("UserData", MODE_PRIVATE)
                    .edit()
                    .remove("photoPath")
                    .remove("photoUri")
                    .remove("email")
                    .remove("photoSkipped")
                    .apply();

            // Ir a AuthActivity mostrando la pestaña de login
            Intent intent = new Intent(RegisterUserActivity.this, AuthActivity.class);
            intent.putExtra("mode", "login");
            startActivity(intent);
            finish();
        });

        // Inicializar vistas
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnContinuar = findViewById(R.id.btnContinuar);

        // Inicializar campos de texto
        etNombres = findViewById(R.id.etNombres);
        etApellidos = findViewById(R.id.etApellidos);
        etEmail = findViewById(R.id.etEmail);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etTelefono = findViewById(R.id.etTelefono);
        etPlacaVehiculo = findViewById(R.id.etPlacaVehiculo);
        tvPlacaVehiculoLabel = findViewById(R.id.tvPlacaVehiculoLabel);

        // Mostrar u ocultar campos según el tipo de usuario
        if ("driver".equals(userType)) {
            tvPlacaVehiculoLabel.setVisibility(View.VISIBLE);
            etPlacaVehiculo.setVisibility(View.VISIBLE);

            // Validación de formato de placa
            etPlacaVehiculo.addTextChangedListener(new TextWatcher() {
                boolean isFormatting;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (isFormatting) return;
                    isFormatting = true;

                    // Convertir a mayúsculas
                    String text = s.toString().toUpperCase();

                    // Filtrar solo letras y números
                    StringBuilder filtered = new StringBuilder();
                    for (char c : text.toCharArray()) {
                        if ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                            filtered.append(c);
                        }
                    }

                    // Limitar a 6 caracteres (3 letras + 3 números)
                    if (filtered.length() > 6) {
                        filtered.delete(6, filtered.length());
                    }

                    // Colocar el texto formateado
                    String formattedText = filtered.toString();
                    if (!formattedText.equals(s.toString())) {
                        s.replace(0, s.length(), formattedText);
                    }

                    // Validar el formato después de aplicar el formateo
                    validatePlaca(formattedText);
                    registerFieldsWatcher.afterTextChanged(s);

                    isFormatting = false;
                }
            });
        }

        // Inicializar las vistas relacionadas con el selector de país
        ivCountryFlag = findViewById(R.id.ivCountryFlag);
        tvCountryCode = findViewById(R.id.tvCountryCode);
        countryCodeContainer = findViewById(R.id.countryCodeContainer);
        etNumeroDocumento = findViewById(R.id.etNumeroDocumento);
        etDireccion = findViewById(R.id.etDireccion);

        // Inicializar campos de contraseña
        etContrasena = findViewById(R.id.etContrasena);
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena);
        ibTogglePassword = findViewById(R.id.ibTogglePassword);
        ibToggleConfirmPassword = findViewById(R.id.ibToggleConfirmPassword);
        passwordStrengthBar = findViewById(R.id.passwordStrengthBar);
        ivReq1Icon = findViewById(R.id.ivReq1Icon);
        ivReq2Icon = findViewById(R.id.ivReq2Icon);
        ivReq3Icon = findViewById(R.id.ivReq3Icon);
        tvReq1 = findViewById(R.id.tvReq1);
        tvReq2 = findViewById(R.id.tvReq2);
        tvReq3 = findViewById(R.id.tvReq3);

        // Después de inicializar etNumeroDocumento, configurar el límite para DNI
        etNumeroDocumento.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(8)
        });

        // Configurar listener para el selector de país
        countryCodeContainer.setOnClickListener(v -> {
            showCountryDialog();
        });

        // Llamada a setupPhoneField
        setupPhoneField();

        // Configurar el campo de fecha de nacimiento
        ImageButton btnCalendar = findViewById(R.id.btnCalendar);

        // Configurar listener para el campo de fecha
        View.OnClickListener dateClickListener = v -> showDatePickerDialog();
        etFechaNacimiento.setOnClickListener(dateClickListener);
        btnCalendar.setOnClickListener(dateClickListener);

        // Configurar el selector de tipo de documento
        tvDocType = findViewById(R.id.tvDocType);
        LinearLayout docTypeContainer = findViewById(R.id.docTypeContainer);

        docTypeContainer.setOnClickListener(v -> {
            showDocTypeDialog();
        });

        // Configurar listeners para mostrar/ocultar contraseña
        ibTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            togglePasswordVisibility(etContrasena, ibTogglePassword, isPasswordVisible);
        });

        ibToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            togglePasswordVisibility(etConfirmarContrasena, ibToggleConfirmPassword, isConfirmPasswordVisible);
        });

        // Añadir TextWatcher para validar el correo electrónico
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateEmail(s.toString());
                // Verificar todos los campos para el botón continuar
                registerFieldsWatcher.afterTextChanged(s);
            }
        });

        // Añadir listener a etNumeroDocumento para validar según tipo de documento
        etNumeroDocumento.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateDocumentNumber(s.toString());
                // Verificar todos los campos para el botón continuar
                registerFieldsWatcher.afterTextChanged(s);
            }
        });

        // Añadir TextWatcher para validar contraseña
        etContrasena.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePassword(s.toString());
                // Verificar si las contraseñas coinciden
                validatePasswordMatch();
                // Verificar todos los campos para el botón continuar
                registerFieldsWatcher.afterTextChanged(s);
            }
        });

        // Añadir TextWatcher para confirmar contraseña
        etConfirmarContrasena.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePasswordMatch();
                // Verificar todos los campos para el botón continuar
                registerFieldsWatcher.afterTextChanged(s);
            }
        });

        // Configurar listener para botón volver
        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });

        // Configurar listener para botón continuar
        btnContinuar.setOnClickListener(v -> {
            if (areAllFieldsFilled()) {
                // Guardar los datos del formulario usando Room Database
                saveFormDataToViewModel(); // Este método ahora usa Room internamente
            } else {
                Toast.makeText(this, "Por favor complete todos los campos correctamente", Toast.LENGTH_SHORT).show();
            }
        });

        // Aplicar TextWatcher a todos los campos
        etNombres.addTextChangedListener(registerFieldsWatcher);
        etApellidos.addTextChangedListener(registerFieldsWatcher);
        etEmail.addTextChangedListener(registerFieldsWatcher);
        etFechaNacimiento.addTextChangedListener(registerFieldsWatcher);
        etTelefono.addTextChangedListener(registerFieldsWatcher);
        etNumeroDocumento.addTextChangedListener(registerFieldsWatcher);
        etDireccion.addTextChangedListener(registerFieldsWatcher);

        // Establecer hint inicial para el número de documento según el tipo por defecto
        etNumeroDocumento.setHint("Ingrese su DNI (8 dígitos)");
    }

    // Método para validar el formato de correo electrónico
    private void validateEmail(String email) {
        if (email.isEmpty()) {
            etEmail.setError(null);
            return;
        }

        // Validar formato básico con Patterns de Android
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Formato de correo electrónico inválido");
            return;
        }

        // Obtener el dominio del correo y validar si está en nuestra lista
        String domain = email.substring(email.lastIndexOf("@") + 1).toLowerCase();
        if (!VALID_EMAIL_DOMAINS.contains(domain)) {
            etEmail.setError("Dominio de correo no reconocido");
            return;
        }

        // Si pasa todas las validaciones, eliminar error
        etEmail.setError(null);
    }

    private void showDatePickerDialog() {
        // Obtener fecha actual para mostrar en el picker (o la fecha ya seleccionada)
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Actualizar el calendario con la fecha seleccionada
                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                    // Actualizar el campo de texto con la fecha formateada
                    etFechaNacimiento.setText(dateFormat.format(calendar.getTime()));

                    // Verificar si se habilita el botón continuar
                    // afterTextChanged no se dispara automáticamente cuando establecemos el texto programáticamente
                    registerFieldsWatcher.afterTextChanged(etFechaNacimiento.getText());
                },
                year, month, dayOfMonth);

        // Establecer fecha máxima (hoy)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Mostrar el diálogo
        datePickerDialog.show();
    }

    private void showDocTypeDialog() {
        final String[] options = {DOC_TYPE_DNI, DOC_TYPE_CE};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccione tipo de documento")
                .setItems(options, (dialog, which) -> {
                    String selectedType = options[which];
                    currentDocType = selectedType;
                    tvDocType.setText(selectedType);

                    // Limpiar el campo de número de documento
                    etNumeroDocumento.setText("");

                    if (DOC_TYPE_DNI.equals(selectedType)) {
                        // Para DNI: Solo números, máximo 8 caracteres
                        etNumeroDocumento.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                        etNumeroDocumento.setFilters(new InputFilter[] {
                                new InputFilter.LengthFilter(8)
                        });
                        etNumeroDocumento.setHint("Ingrese su DNI (8 dígitos)");
                    } else {
                        // Para CE: Alfanumérico, máximo 12 caracteres
                        etNumeroDocumento.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                                android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                        etNumeroDocumento.setFilters(new InputFilter[] {
                                new InputFilter.LengthFilter(12)
                        });
                        etNumeroDocumento.setHint("Ingrese su CE (máx. 12 caracteres)");
                    }
                });

        builder.create().show();
    }

    private void showCountryDialog() {
        final String[] countries = {"Perú", "Colombia", "Venezuela"};
        final String[] countryCodes = {COUNTRY_CODE_PE, COUNTRY_CODE_CO, COUNTRY_CODE_VE};
        final int[] countryFlags = {
                R.drawable.circle_flag_pe,
                R.drawable.circle_flag_co,
                R.drawable.circle_flag_ve
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccione país")
                .setItems(countries, (dialog, which) -> {
                    // Actualizar el código de país seleccionado
                    currentCountryCode = countryCodes[which];
                    tvCountryCode.setText(currentCountryCode);
                    ivCountryFlag.setImageResource(countryFlags[which]);

                    // Si el usuario cambia de país, podemos limpiar el campo de teléfono
                    // O dejarlo como está si los formatos son similares
                    // etTelefono.setText("");
                });

        builder.create().show();
    }

    private void validateDocumentNumber(String docNumber) {
        if (docNumber.isEmpty()) {
            etNumeroDocumento.setError(null);
            return;
        }

        if (DOC_TYPE_DNI.equals(currentDocType)) {
            // Validar DNI: solo números y exactamente 8 dígitos
            if (!Pattern.matches("^\\d+$", docNumber)) {
                etNumeroDocumento.setError("El DNI solo debe contener números");
            } else if (docNumber.length() < 8) {
                etNumeroDocumento.setError("El DNI debe tener 8 dígitos");
            } else {
                etNumeroDocumento.setError(null);
            }
        } else if (DOC_TYPE_CE.equals(currentDocType)) {
            // Validar CE: formato alfanumérico, mínimo 9 caracteres
            if (docNumber.length() < 9) {
                etNumeroDocumento.setError("El CE debe tener al menos 9 caracteres");
            } else {
                etNumeroDocumento.setError(null);
            }
        }
    }

    // TextWatcher para validar todos los campos
    private final TextWatcher registerFieldsWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // No se necesita implementación
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // No se necesita implementación
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Verificar si todos los campos están completos
            boolean allFieldsFilled = areAllFieldsFilled();

            // Habilitar o deshabilitar el botón según si todos los campos están completos
            btnContinuar.setEnabled(allFieldsFilled);
            btnContinuar.setAlpha(allFieldsFilled ? 1.0f : 0.4f);
        }
    };

    // Método para verificar si todos los campos están completos y válidos
    private boolean areAllFieldsFilled() {
        // Verificar si el email tiene error
        boolean emailValid = etEmail.getError() == null &&
                !etEmail.getText().toString().trim().isEmpty();

        // Verificar si el documento tiene error
        boolean documentValid = etNumeroDocumento.getError() == null &&
                !etNumeroDocumento.getText().toString().trim().isEmpty();

        // Verificar si el teléfono tiene error o está vacío
        String phoneDigits = etTelefono.getText().toString().replaceAll("[^0-9]", "");
        boolean phoneValid = etTelefono.getError() == null &&
                !etTelefono.getText().toString().trim().isEmpty() &&
                phoneDigits.length() == 9;

        // Verificar requisitos de contraseña
        boolean passwordValid = passwordRequirementsMet() &&
                !etContrasena.getText().toString().trim().isEmpty();

        // Verificar si las contraseñas coinciden
        boolean passwordsMatch = etContrasena.getText().toString().equals(
                etConfirmarContrasena.getText().toString()) &&
                !etConfirmarContrasena.getText().toString().trim().isEmpty();

        // Al final del método, antes del return
        boolean placaValid = true;
        if ("driver".equals(userType)) {
            placaValid = etPlacaVehiculo.getError() == null &&
                    !etPlacaVehiculo.getText().toString().trim().isEmpty();
        }

        return !etNombres.getText().toString().trim().isEmpty() &&
                !etApellidos.getText().toString().trim().isEmpty() &&
                emailValid &&
                !etFechaNacimiento.getText().toString().trim().isEmpty() &&
                phoneValid &&
                documentValid &&
                !etDireccion.getText().toString().trim().isEmpty() &&
                passwordValid &&
                passwordsMatch &&
                placaValid;
    }

    private void setupPhoneField() {
        // Configuramos el código de país inicial
        tvCountryCode.setText(currentCountryCode);

        // Establecer el máximo de caracteres a 11 (9 dígitos + 2 guiones)
        etTelefono.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(11)
        });

        // TextWatcher para formatear el teléfono mientras se escribe
        etTelefono.addTextChangedListener(new TextWatcher() {
            boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                // Eliminar caracteres no numéricos
                String text = s.toString().replaceAll("[^0-9]", "");

                // Limitar a 9 dígitos
                if (text.length() > 9) {
                    text = text.substring(0, 9);
                }

                // Formatear el número con guiones
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < text.length(); i++) {
                    if (i == 3 || i == 6) {
                        formatted.append('-');
                    }
                    formatted.append(text.charAt(i));
                }

                // Colocar el texto formateado
                String formattedText = formatted.toString();
                if (!formattedText.equals(s.toString())) {
                    s.replace(0, s.length(), formattedText);
                }

                // Validar si el número está completo
                if (text.length() < 9) {
                    etTelefono.setError("El número debe tener 9 dígitos");
                } else {
                    etTelefono.setError(null);
                }

                isFormatting = false;
            }
        });
    }

    // Método para mostrar/ocultar contraseña
    private void togglePasswordVisibility(EditText editText, ImageButton button, boolean isVisible) {
        if (isVisible) {
            // Mostrar contraseña
            editText.setTransformationMethod(null);
            button.setImageResource(R.drawable.ic_visibility_off_custom);
        } else {
            // Ocultar contraseña
            editText.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
            button.setImageResource(R.drawable.ic_visibility_custom);
        }

        // Mover cursor al final del texto
        editText.setSelection(editText.getText().length());
    }

    // Método para validar los requisitos de la contraseña
    private void validatePassword(String password) {
        int progress = 0;

        // Requisito 1: entre 8 y 32 caracteres
        boolean req1Met = password.length() >= 8 && password.length() <= 32;
        if (req1Met) {
            ivReq1Icon.setImageResource(R.drawable.ic_check);
            tvReq1.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            tvReq1.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_regular));
            progress++;
        } else {
            ivReq1Icon.setImageResource(R.drawable.ic_sin_check);
            tvReq1.setTextColor(getResources().getColor(R.color.colorTextTertiary));
            tvReq1.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_bold));
        }

        // Requisito 2: un número y un símbolo
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        boolean req2Met = hasNumber && hasSymbol;
        if (req2Met) {
            ivReq2Icon.setImageResource(R.drawable.ic_check);
            tvReq2.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            tvReq2.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_regular));
            progress++;
        } else {
            ivReq2Icon.setImageResource(R.drawable.ic_sin_check);
            tvReq2.setTextColor(getResources().getColor(R.color.colorTextTertiary));
            tvReq2.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_bold));
        }

        // Requisito 3: una mayúscula
        boolean req3Met = password.matches(".*[A-Z].*");
        if (req3Met) {
            ivReq3Icon.setImageResource(R.drawable.ic_check);
            tvReq3.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            tvReq3.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_regular));
            progress++;
        } else {
            ivReq3Icon.setImageResource(R.drawable.ic_sin_check);
            tvReq3.setTextColor(getResources().getColor(R.color.colorTextTertiary));
            tvReq3.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_bold));
        }

        // Actualizar la barra de progreso según los requisitos cumplidos
        switch (progress) {
            case 0:
                passwordStrengthBar.setImageResource(R.drawable.progress_bar_0);
                break;
            case 1:
                passwordStrengthBar.setImageResource(R.drawable.progress_bar_1);
                break;
            case 2:
                passwordStrengthBar.setImageResource(R.drawable.progress_bar_2);
                break;
            case 3:
                passwordStrengthBar.setImageResource(R.drawable.progress_bar_3);
                break;
        }
    }

    // Método para validar si las contraseñas coinciden
    private void validatePasswordMatch() {
        String password = etContrasena.getText().toString();
        String confirmPassword = etConfirmarContrasena.getText().toString();

        if (!confirmPassword.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                etConfirmarContrasena.setError("Las contraseñas no coinciden");
            } else {
                etConfirmarContrasena.setError(null);
            }
        }
    }

    // Método para verificar si todos los requisitos de la contraseña se cumplen
    private boolean passwordRequirementsMet() {
        String password = etContrasena.getText().toString();

        // Requisito 1: entre 8 y 32 caracteres
        boolean req1Met = password.length() >= 8 && password.length() <= 32;

        // Requisito 2: un número y un símbolo
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        boolean req2Met = hasNumber && hasSymbol;

        // Requisito 3: una mayúscula
        boolean req3Met = password.matches(".*[A-Z].*");

        return req1Met && req2Met && req3Met;
    }

    private void saveFormDataToViewModel() {
        // Solo llamar a la función de base de datos, no duplicar la lógica
        saveFormDataToDatabase();
    }

    @Override
    public void onBackPressed() {
        // Verificar de dónde venimos para decidir si limpiar o no
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        boolean navigatingWithinFlow = prefs.getBoolean("navigatingWithinFlow", false);

        // Log para debugging
        Log.d("RegisterUser", "onBackPressed - navigatingWithinFlow: " + navigatingWithinFlow);
        Log.d("RegisterUser", "Estado actual de SharedPreferences:");
        Log.d("RegisterUser", "  - photoPath: " + prefs.getString("photoPath", "NO_ENCONTRADO"));
        Log.d("RegisterUser", "  - pdfPath: " + prefs.getString("pdfPath", "NO_ENCONTRADO"));
        Log.d("RegisterUser", "  - email: " + prefs.getString("email", "NO_ENCONTRADO"));

        if (navigatingWithinFlow) {
            // Venimos de una vista posterior del formulario (PDF o Foto), NO limpiar NADA
            Log.d("RegisterUser", "Navegando hacia atrás DENTRO del flujo - NO SE LIMPIA NADA");

            // Solo limpiar el flag de navegación AHORA que regresamos
            prefs.edit().remove("navigatingWithinFlow").apply();

            super.onBackPressed();
            return;
        }

        // Si llegamos aquí, estamos yendo hacia SelectUserType (SALIENDO del flujo)
        Log.d("RegisterUser", "Usuario SALIÓ del flujo hacia SelectUserType - limpiando TODOS los datos");

        // Limpiar SharedPreferences
        prefs.edit()
                .remove("photoPath")
                .remove("photoUri")
                .remove("pdfPath")
                .remove("pdfUri")
                .remove("email")
                .remove("photoSkipped")
                .remove("navigatingWithinFlow")
                .apply();

        // IMPORTANTE: Limpiar registro INCOMPLETO de la base de datos
        if (currentRegistrationId != -1) {
            Log.d("RegisterUser", "Eliminando registro incompleto de la base de datos: " + currentRegistrationId);

            // Primero eliminar archivos asociados
            fileStorageRepository.clearFilesByRegistrationId(currentRegistrationId);

            // Luego eliminar el registro
            userRegistrationRepository.deleteUserRegistration(currentRegistrationId, new UserRegistrationRepository.RegistrationCallback() {
                @Override
                public void onSuccess(UserRegistrationEntity registration) {
                    Log.d("RegisterUser", "✅ Registro incompleto eliminado exitosamente: " + currentRegistrationId);
                }

                @Override
                public void onError(String error) {
                    Log.e("RegisterUser", "❌ Error eliminando registro incompleto: " + error);
                }
            });

            // Resetear el ID
            currentRegistrationId = -1;
        }

        super.onBackPressed();
    }

    // Métodito para validar el formato de la placa
    private void validatePlaca(String placa) {
        if (placa.isEmpty()) {
            etPlacaVehiculo.setError(null);
            return;
        }

        // Formato peruano simplificado: 3 letras seguidas de 3 números (ABC123)
        String regex = "^[A-Z]{3}\\d{3}$";
        if (!placa.matches(regex)) {
            etPlacaVehiculo.setError("Formato inválido. Use el formato: ABC123");
        } else {
            etPlacaVehiculo.setError(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Log para debugging PERO NO LIMPIAR EL FLAG
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        Log.d("RegisterUser", "onResume - Estado de SharedPreferences:");
        Log.d("RegisterUser", "  - photoPath: " + prefs.getString("photoPath", "NO_ENCONTRADO"));
        Log.d("RegisterUser", "  - pdfPath: " + prefs.getString("pdfPath", "NO_ENCONTRADO"));
        Log.d("RegisterUser", "  - navigatingWithinFlow: " + prefs.getBoolean("navigatingWithinFlow", false));

        // NO limpiar el flag aquí - se mantendrá hasta que realmente salgamos del flujo
        Log.d("RegisterUser", "onResume completado - flag mantenido");
    }

    // Método para recuperar registro existente
    private void recoverExistingRegistration() {
        // Solo recuperar si venimos DENTRO del flujo, no si es un nuevo registro
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        boolean navigatingWithinFlow = prefs.getBoolean("navigatingWithinFlow", false);

        if (!navigatingWithinFlow) {
            Log.d("RegisterUser", "Nuevo registro iniciado - NO recuperar datos anteriores");
            return;
        }

        userRegistrationRepository.getLatestUserRegistration(new UserRegistrationRepository.RegistrationCallback() {
            @Override
            public void onSuccess(UserRegistrationEntity registration) {
                if (!registration.isCompleted) {
                    runOnUiThread(() -> {
                        currentRegistrationId = registration.id;
                        populateFieldsFromRegistration(registration);
                        Log.d("RegisterUser", "Registro recuperado: " + currentRegistrationId);
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.d("RegisterUser", "No hay registros anteriores: " + error);
            }
        });
    }

    // Método para poblar campos desde el registro recuperado
    private void populateFieldsFromRegistration(UserRegistrationEntity registration) {
        etNombres.setText(registration.nombres != null ? registration.nombres : "");
        etApellidos.setText(registration.apellidos != null ? registration.apellidos : "");
        etEmail.setText(registration.email != null ? registration.email : "");
        etFechaNacimiento.setText(registration.fechaNacimiento != null ? registration.fechaNacimiento : "");
        etTelefono.setText(registration.telefono != null ? registration.telefono : "");
        etNumeroDocumento.setText(registration.numeroDocumento != null ? registration.numeroDocumento : "");
        etDireccion.setText(registration.direccion != null ? registration.direccion : "");

        if ("driver".equals(registration.userType) && registration.placaVehiculo != null) {
            etPlacaVehiculo.setText(registration.placaVehiculo);
        }

        // Configurar tipo de documento si existe
        if (registration.tipoDocumento != null) {
            currentDocType = registration.tipoDocumento;
            tvDocType.setText(currentDocType);
            updateDocumentFieldForType(currentDocType);
        }
    }

    // Método helper para actualizar campo de documento
    private void updateDocumentFieldForType(String docType) {
        etNumeroDocumento.setText("");

        if ("DNI".equals(docType)) {
            etNumeroDocumento.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            etNumeroDocumento.setFilters(new android.text.InputFilter[] {
                    new android.text.InputFilter.LengthFilter(8)
            });
            etNumeroDocumento.setHint("Ingrese su DNI (8 dígitos)");
        } else {
            etNumeroDocumento.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                    android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            etNumeroDocumento.setFilters(new android.text.InputFilter[] {
                    new android.text.InputFilter.LengthFilter(12)
            });
            etNumeroDocumento.setHint("Ingrese su CE (máx. 12 caracteres)");
        }
    }

    // Nuevo método para guardar datos usando Room
    // Nuevo método para guardar datos usando Room
    private void saveFormDataToDatabase() {
        // Primero guardar en ViewModel para compatibilidad (SOLO UNA VEZ)
        if (mViewModel != null) {
            mViewModel.setNombres(etNombres.getText().toString().trim());
            mViewModel.setApellidos(etApellidos.getText().toString().trim());
            String email = etEmail.getText().toString().trim();
            mViewModel.setEmail(email);

            // También guarda el email en SharedPreferences para compatibilidad
            getSharedPreferences("UserData", MODE_PRIVATE)
                    .edit()
                    .putString("email", email)
                    .apply();

            Log.d("RegisterUser", "Guardando email en ViewModel y SharedPreferences: " + email);

            mViewModel.setFechaNacimiento(etFechaNacimiento.getText().toString().trim());
            mViewModel.setTelefono(etTelefono.getText().toString().trim());
            mViewModel.setTipoDocumento(currentDocType);
            mViewModel.setNumeroDocumento(etNumeroDocumento.getText().toString().trim());
            mViewModel.setDireccion(etDireccion.getText().toString().trim());
            mViewModel.setPassword(etContrasena.getText().toString());
            mViewModel.setUserType(userType);

            if ("driver".equals(userType)) {
                mViewModel.setPlacaVehiculo(etPlacaVehiculo.getText().toString().trim());
            }
        }

        // Crear entidad de registro
        UserRegistrationEntity registration = userRegistrationRepository.createFromViewModel(
                userType,
                etNombres.getText().toString().trim(),
                etApellidos.getText().toString().trim(),
                etEmail.getText().toString().trim(),
                etFechaNacimiento.getText().toString().trim(),
                etTelefono.getText().toString().trim(),
                currentDocType,
                etNumeroDocumento.getText().toString().trim(),
                etDireccion.getText().toString().trim(),
                "driver".equals(userType) ? etPlacaVehiculo.getText().toString().trim() : null,
                etContrasena.getText().toString()
        );

        // Si ya existe un registro, usar su ID
        if (currentRegistrationId != -1) {
            registration.id = currentRegistrationId;

            userRegistrationRepository.updateUserRegistration(registration, new UserRegistrationRepository.RegistrationCallback() {
                @Override
                public void onSuccess(UserRegistrationEntity updatedRegistration) {
                    Log.d("RegisterUser", "Registro actualizado exitosamente: " + updatedRegistration.id);
                    // IMPORTANTE: NO llamar a proceedToNextStep desde aquí para evitar el bucle
                    runOnUiThread(() -> proceedToNextStep(updatedRegistration.id));
                }

                @Override
                public void onError(String error) {
                    Log.e("RegisterUser", "Error actualizando registro: " + error);
                    runOnUiThread(() -> Toast.makeText(RegisterUserActivity.this, "Error al guardar: " + error, Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            // Crear nuevo registro
            userRegistrationRepository.saveUserRegistration(registration, new UserRegistrationRepository.RegistrationIdCallback() {
                @Override
                public void onSuccess(int registrationId) {
                    currentRegistrationId = registrationId;
                    Log.d("RegisterUser", "Nuevo registro creado: " + registrationId);
                    // IMPORTANTE: NO llamar a proceedToNextStep desde aquí para evitar el bucle
                    runOnUiThread(() -> proceedToNextStep(registrationId));
                }

                @Override
                public void onError(String error) {
                    Log.e("RegisterUser", "Error creando registro: " + error);
                    runOnUiThread(() -> Toast.makeText(RegisterUserActivity.this, "Error al guardar: " + error, Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    // Método para proceder al siguiente paso
    private void proceedToNextStep(int registrationId) {
        // NO llamar a saveFormDataToViewModel aquí - ya se hizo antes
        Log.d("RegisterUser", "Procediendo al siguiente paso con registro ID: " + registrationId);

        if ("driver".equals(userType)) {
            // Para taxistas: primero documentos, luego foto
            Intent intent = new Intent(RegisterUserActivity.this, UploadDriverDocumentsActivity.class);
            intent.putExtra("userType", userType);
            intent.putExtra("registrationId", registrationId);
            intent.putExtra("placaVehiculo", etPlacaVehiculo.getText().toString().trim());
            startActivity(intent);
        } else {
            // Para clientes: directamente a la foto de perfil
            Intent intent = new Intent(RegisterUserActivity.this, AddProfilePhotoActivity.class);
            intent.putExtra("userType", userType);
            intent.putExtra("registrationId", registrationId);
            startActivity(intent);
        }
    }
}