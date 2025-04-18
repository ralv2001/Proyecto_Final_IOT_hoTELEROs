package com.example.proyecto_final_hoteleros.auth.register;

import androidx.lifecycle.ViewModelProvider;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_final_hoteleros.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

public class RegisterFragment extends Fragment {

    private RegisterViewModel mViewModel;

    // Campos de texto
    private EditText etNombres, etApellidos, etEmail, etFechaNacimiento, etTelefono, etNumeroDocumento, etDireccion;
    private Button btnContinuar;
    private TextView tvDocType;

    // Constantes para tipos de documento
    private static final String DOC_TYPE_DNI = "DNI";
    private static final String DOC_TYPE_CE = "CE";
    private String currentDocType = DOC_TYPE_DNI; // Por defecto DNI

    // Calendario para guardar la fecha seleccionada
    private final Calendar calendar = Calendar.getInstance();
    // Formato para la fecha
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());




    // Variables para la selección de país
    private ImageView ivCountryFlag;
    private TextView tvCountryCode;
    private LinearLayout countryCodeContainer;

    // Constantes para los códigos de país
    private static final String COUNTRY_CODE_PE = "(+51) ";
    private static final String COUNTRY_CODE_CO = "(+57) ";
    private static final String COUNTRY_CODE_VE = "(+58) ";
    private String currentCountryCode = COUNTRY_CODE_PE; // Por defecto Perú



    public static RegisterFragment newInstance(String userType) {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        args.putString("userType", userType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Inicializar vistas
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnContinuar = view.findViewById(R.id.btnContinuar);

        // Inicializar campos de texto
        etNombres = view.findViewById(R.id.etNombres);
        etApellidos = view.findViewById(R.id.etApellidos);
        etEmail = view.findViewById(R.id.etEmail);
        etFechaNacimiento = view.findViewById(R.id.etFechaNacimiento);
        etTelefono = view.findViewById(R.id.etTelefono);
        // Inicializar las vistas relacionadas con el selector de país
        ivCountryFlag = view.findViewById(R.id.ivCountryFlag);
        tvCountryCode = view.findViewById(R.id.tvCountryCode);
        countryCodeContainer = view.findViewById(R.id.countryCodeContainer);
        etNumeroDocumento = view.findViewById(R.id.etNumeroDocumento);
        etDireccion = view.findViewById(R.id.etDireccion);

        // Después de inicializar etNumeroDocumento, configurar el límite para DNI
        etNumeroDocumento.setFilters(new android.text.InputFilter[] {
                new android.text.InputFilter.LengthFilter(8)
        });

        // Configurar listener para el selector de país
        countryCodeContainer.setOnClickListener(v -> {
            showCountryDialog();
        });

        // Llamada a setupPhoneField con la vista
        setupPhoneField(view);

        // Configurar el campo de fecha de nacimiento
        ImageButton btnCalendar = view.findViewById(R.id.btnCalendar);

        // Configurar listener para el campo de fecha
        View.OnClickListener dateClickListener = v -> showDatePickerDialog();
        etFechaNacimiento.setOnClickListener(dateClickListener);
        btnCalendar.setOnClickListener(dateClickListener);

        // Configurar el selector de tipo de documento
        tvDocType = view.findViewById(R.id.tvDocType);
        LinearLayout docTypeContainer = view.findViewById(R.id.docTypeContainer);

        docTypeContainer.setOnClickListener(v -> {
            showDocTypeDialog();
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

        // Configurar listener para botón volver
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Configurar listener para botón continuar
        btnContinuar.setOnClickListener(v -> {
            if (areAllFieldsFilled()) {
                // Procesar el formulario si todos los campos están llenos
                Toast.makeText(getContext(), "Formulario completo", Toast.LENGTH_SHORT).show();
                // Aquí iría la lógica para continuar con el registro
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

        return view;
    }

    private void showDatePickerDialog() {
        // Obtener fecha actual para mostrar en el picker (o la fecha ya seleccionada)
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                        etNumeroDocumento.setFilters(new android.text.InputFilter[] {
                                new android.text.InputFilter.LengthFilter(8)
                        });
                        etNumeroDocumento.setHint("Ingrese su DNI (8 dígitos)");
                    } else {
                        // Para CE: Alfanumérico, máximo 12 caracteres
                        etNumeroDocumento.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                                android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                        etNumeroDocumento.setFilters(new android.text.InputFilter[] {
                                new android.text.InputFilter.LengthFilter(12)
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        // Verificar si el documento tiene error
        boolean documentValid = etNumeroDocumento.getError() == null &&
                !etNumeroDocumento.getText().toString().trim().isEmpty();

        // Verificar si el teléfono tiene error o está vacío
        String phoneDigits = etTelefono.getText().toString().replaceAll("[^0-9]", "");
        boolean phoneValid = etTelefono.getError() == null &&
                !etTelefono.getText().toString().trim().isEmpty() &&
                phoneDigits.length() == 9;

        return !etNombres.getText().toString().trim().isEmpty() &&
                !etApellidos.getText().toString().trim().isEmpty() &&
                !etEmail.getText().toString().trim().isEmpty() &&
                !etFechaNacimiento.getText().toString().trim().isEmpty() &&
                phoneValid &&
                documentValid &&
                !etDireccion.getText().toString().trim().isEmpty();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
    }

    private void setupPhoneField(View rootView) {
        // Inicializar las vistas relacionadas con el selector de país
        tvCountryCode = rootView.findViewById(R.id.tvCountryCode);
        ivCountryFlag = rootView.findViewById(R.id.ivCountryFlag);
        countryCodeContainer = rootView.findViewById(R.id.countryCodeContainer);

        // Configuramos el código de país inicial
        tvCountryCode.setText(currentCountryCode);

        // Configurar listener para el selector de país
        countryCodeContainer.setOnClickListener(v -> {
            showCountryDialog();
        });

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

}