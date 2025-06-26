package com.example.proyecto_final_hoteleros.auth.login;


import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_final_hoteleros.MainActivity;
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;
import com.example.proyecto_final_hoteleros.models.UserModel;

import android.app.Activity;
import com.example.proyecto_final_hoteleros.utils.GoogleSignInHelper;
import com.google.firebase.auth.FirebaseUser;

import com.example.proyecto_final_hoteleros.AuthActivity;
import com.example.proyecto_final_hoteleros.R;

import java.util.Arrays;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    private LoginViewModel mViewModel;
    private LinearLayout layoutGeneralError;

    private EditText etEmail;
    private EditText etPassword;
    private ImageButton ibTogglePassword;
    private TextView tvErrorMessage;
    private TextView tvForgotPassword;
    private Button btnContinue;
    private Button btnFacebookLogin;
    private Button btnGoogleLogin;
    private TextView tvRegisterPrompt;

    private boolean isPasswordVisible = false;

    // ========== NUEVAS VARIABLES PARA GOOGLE SIGN-IN ==========
    private GoogleSignInHelper googleSignInHelper;

    private View passwordLayoutContainer;
    private TextView tvGeneralError;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sistema_fragment_login, container, false);
        // Inicializar vistas
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        ibTogglePassword = view.findViewById(R.id.ibTogglePassword);
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        btnContinue = view.findViewById(R.id.btnContinue);
        btnFacebookLogin = view.findViewById(R.id.btnFacebookLogin);
        btnGoogleLogin = view.findViewById(R.id.btnGoogleLogin);
        tvRegisterPrompt = view.findViewById(R.id.tvRegisterPrompt);

        // ========== INICIALIZAR GOOGLE SIGN-IN ==========
        googleSignInHelper = new GoogleSignInHelper(getActivity());

        // ========== CONFIGURAR ACTIVITY RESULT LAUNCHER ==========
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "=== ACTIVITY RESULT LAUNCHER ===");
                    Log.d(TAG, "Result Code: " + result.getResultCode());
                    Log.d(TAG, "Data: " + (result.getData() != null ? "presente" : "null"));

                    if (result.getData() != null) {
                        googleSignInHelper.handleSignInResult(result.getData());
                    } else {
                        Log.e(TAG, "No se recibieron datos del Google Sign-In");
                        resetGoogleLoginButton();
                    }
                }
        );

        passwordLayoutContainer = view.findViewById(R.id.passwordLayout);
        tvGeneralError = view.findViewById(R.id.tvGeneralError);
        layoutGeneralError = view.findViewById(R.id.layoutGeneralError);

        etEmail.addTextChangedListener(new TextWatcher() {
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
                validateEmail(s.toString());
            }
        });

        // Configurar TextWatcher para validación de campos
        TextWatcher loginFieldsWatcher = new TextWatcher() {
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
                // Habilitar o deshabilitar el botón según si ambos campos tienen texto
                boolean emailNotEmpty = !etEmail.getText().toString().trim().isEmpty();
                boolean passwordNotEmpty = !etPassword.getText().toString().trim().isEmpty();
                boolean enableButton = emailNotEmpty && passwordNotEmpty;

                btnContinue.setEnabled(enableButton);
                btnContinue.setAlpha(enableButton ? 1.0f : 0.4f);

                // Limpiar errores cuando el usuario empiece a escribir
                clearLoginErrors();
            }
        };

        // Aplicar el TextWatcher a ambos campos
        etEmail.addTextChangedListener(loginFieldsWatcher);
        etPassword.addTextChangedListener(loginFieldsWatcher);

        // Ocultar mensaje de error inicialmente
        tvErrorMessage.setVisibility(View.GONE);

        // Configurar listeners
        ibTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AuthActivity) getActivity()).goToForgotPassword();
            }
        });

        btnFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Iniciar sesión con Facebook", Toast.LENGTH_SHORT).show();
                // Implementar inicio de sesión con Facebook
            }
        });

        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateGoogleSignIn();
            }
        });

        tvRegisterPrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cambiar a la pestaña de registro
                if (getActivity() instanceof AuthActivity) {
                    ((AuthActivity) getActivity()).goToRegister();
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        // TODO: Use the ViewModel
    }

    // Si necesitas manejar el botón de retroceso, hazlo de manera muy simple:
    @Override
    public void onResume() {
        super.onResume();
        // Cualquier lógica adicional que necesites
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar cualquier estado persistente si es necesario
    }

    // Añade también este métodito para asegurar que el callback se limpia apropiadamente
    @Override
    public void onDetach() {
        super.onDetach();
        // El callback se eliminará automáticamente ya que está asociado al ciclo de vida del fragmento
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Ocultar contraseña
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ibTogglePassword.setImageResource(R.drawable.ic_visibility_custom);
        } else {
            // Mostrar contraseña
            etPassword.setTransformationMethod(null);
            ibTogglePassword.setImageResource(R.drawable.ic_visibility_off_custom);
        }
        isPasswordVisible = !isPasswordVisible;

        // Mover cursor al final del texto
        etPassword.setSelection(etPassword.getText().length());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Las validaciones básicas ya se hacen con el TextWatcher,
        // pero podemos agregar validaciones más específicas aquí
        boolean isValid = true;

        // Verificar si hay errores en el campo de email
        if (etEmail.getError() != null) {
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // El resto del métodito se mantiene igual
        // ========== LOGIN CON FIREBASE ==========
        FirebaseManager firebaseManager = FirebaseManager.getInstance();

        // Deshabilitar botón mientras se procesa
        btnContinue.setEnabled(false);
        btnContinue.setText("Iniciando sesión...");
        btnContinue.setAlpha(0.6f);

        Log.d(TAG, "=== INTENTANDO LOGIN ===");
        Log.d(TAG, "Email: " + email);
        Log.d(TAG, "Password length: " + password.length());

        firebaseManager.loginUser(email, password, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                Log.d("LoginFragment", "✅ Login exitoso: " + userId);

                // Obtener datos del usuario desde Firestore
                firebaseManager.getUserData(userId, new FirebaseManager.UserCallback() {
                    // En LoginFragment.java, dentro del método attemptLogin(), reemplaza la parte del onUserFound:

                    @Override
                    public void onUserFound(UserModel user) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.d("LoginFragment", "✅ Datos del usuario obtenidos: " + user.getFullName());

                                // Verificar si es superadmin
                                if ("superadmin".equals(user.getUserType())) {
                                    Toast.makeText(getContext(),
                                            "¡Bienvenido Superadmin " + user.getNombres() + "!",
                                            Toast.LENGTH_SHORT).show();

                                    // Navegar a SuperAdminActivity
                                    Intent intent = new Intent(getActivity(), com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity.class);

                                    // Pasar datos del usuario
                                    intent.putExtra("userId", userId);
                                    intent.putExtra("userEmail", user.getEmail());
                                    intent.putExtra("userName", user.getFullName());
                                    intent.putExtra("userType", user.getUserType());

                                    startActivity(intent);
                                    getActivity().finish();
                                    return;
                                }

                                // Para otros tipos de usuario (código existente)
                                Toast.makeText(getContext(),
                                        "¡Bienvenido " + user.getNombres() + "!",
                                        Toast.LENGTH_SHORT).show();

                                // Navegar según el tipo de usuario
                                Intent intent;
                                switch (user.getUserType()) {
                                    case "client":
                                        intent = new Intent(getActivity(), com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity.class);
                                        break;
                                    case "driver":
                                        intent = new Intent(getActivity(), com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity.class);
                                        break;
                                    case "admin_hotel":
                                        intent = new Intent(getActivity(), com.example.proyecto_final_hoteleros.adminhotel.activity.AdminHotelActivity.class);
                                        break;
                                    default:
                                        Log.w("LoginFragment", "Tipo de usuario desconocido: " + user.getUserType());
                                        return;
                                }

                                // Pasar datos del usuario
                                intent.putExtra("userId", userId);
                                intent.putExtra("userEmail", user.getEmail());
                                intent.putExtra("userName", user.getFullName());
                                intent.putExtra("userType", user.getUserType());

                                startActivity(intent);
                                getActivity().finish();
                            });
                        }
                    }

                    @Override
                    public void onUserNotFound() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.w("LoginFragment", "⚠️ Usuario autenticado pero sin datos en Firestore");
                                Toast.makeText(getContext(),
                                        "Error: Datos de usuario no encontrados", Toast.LENGTH_SHORT).show();
                                resetLoginButton();
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.e("LoginFragment", "❌ Error obteniendo datos: " + error);
                                Toast.makeText(getContext(),
                                        "Error obteniendo datos del usuario", Toast.LENGTH_SHORT).show();
                                resetLoginButton();
                            });
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d(TAG, "=== ERROR CALLBACK ===");
                        Log.d(TAG, "Error original: " + error);
                        Log.d(TAG, "Error traducido: " + translateFirebaseError(error));

                        Log.e(TAG, "❌ Error en login: " + error);

                        // Mostrar errores visuales
                        showLoginError(error);

                        // Restaurar botón
                        resetLoginButton();
                    });
                }
            }
        });
    }

    // Añadir justo antes del último corchete de cierre de la clase
    private void validateEmail(String email) {
        if (email.isEmpty()) {
            etEmail.setError(null);
            return;
        }

        // Validar formato básico con Patterns de Android
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Formato de correo electrónico inválido");
            return;
        }

        // Lista de dominios válidos
        List<String> validDomains = Arrays.asList(
                "gmail.com", "hotmail.com", "yahoo.es", "pucp.edu.pe", "outlook.com",
                "icloud.com", "yahoo.com", "live.com", "msn.com", "protonmail.com",
                "yahoo.com.mx", "hotmail.es", "me.com", "aol.com", "mail.com"
        );

        // Obtener el dominio del correo y validar si está en nuestra lista
        String domain = email.substring(email.lastIndexOf("@") + 1).toLowerCase();
        if (!validDomains.contains(domain)) {
            etEmail.setError("Dominio de correo no reconocido");
            return;
        }

        // Si pasa todas las validaciones, eliminar error
        etEmail.setError(null);
    }

    private void resetLoginButton() {
        btnContinue.setEnabled(true);
        btnContinue.setText("Continuar");
        btnContinue.setAlpha(1.0f);
    }

    private String translateFirebaseError(String error) {
        if (error == null) return "Error desconocido";

        if (error.contains("user-not-found") || error.contains("wrong-password") ||
                error.contains("invalid-credential") || error.contains("auth credential is incorrect")) {
            return "Correo o contraseña incorrectos. Intente nuevamente";
        } else if (error.contains("invalid-email")) {
            return "Formato de correo electrónico inválido";
        } else if (error.contains("user-disabled")) {
            return "Esta cuenta ha sido deshabilitada. Contacta al administrador";
        } else if (error.contains("too-many-requests")) {
            return "Demasiados intentos fallidos. Inténtalo más tarde";
        } else if (error.contains("network-request-failed")) {
            return "Error de conexión. Verifica tu internet e inténtalo de nuevo";
        } else {
            return "Error de inicio de sesión: " + error;
        }
    }

    private void showLoginError(String error) {
        // Cambiar el borde del campo de contraseña a rojo
        passwordLayoutContainer.setBackgroundResource(R.drawable.sistema_se_ff0000_sw2cr12);

        // Mostrar "Contraseña incorrecta" en gris
        tvErrorMessage.setText("Contraseña incorrecta");
        tvErrorMessage.setTextColor(getResources().getColor(R.color.colorTextTertiary));
        tvErrorMessage.setVisibility(View.VISIBLE);

        // Mostrar mensaje general de error
        String userFriendlyError = translateFirebaseError(error);
        tvGeneralError.setText("¡Ups! " + userFriendlyError);
        layoutGeneralError.setVisibility(View.VISIBLE);

        Log.d(TAG, "Mostrando error: " + userFriendlyError);
    }

    private void clearLoginErrors() {
        // Restaurar borde normal del campo de contraseña
        passwordLayoutContainer.setBackgroundResource(R.drawable.se1e1e1sw2cr12);

        // Ocultar mensajes de error
        tvErrorMessage.setVisibility(View.GONE);
        layoutGeneralError.setVisibility(View.GONE);

        Log.d(TAG, "Errores de login limpiados");
    }


    private void initiateGoogleSignIn() {
        Log.d(TAG, "Iniciando Google Sign-In");

        // Deshabilitar botón mientras se procesa
        btnGoogleLogin.setEnabled(false);
        btnGoogleLogin.setText("Conectando...");
        btnGoogleLogin.setAlpha(0.6f);

        googleSignInHelper.signIn(new GoogleSignInHelper.GoogleSignInCallback() {
            @Override
            public void onSignInSuccess(FirebaseUser user) {
                Log.d(TAG, "✅ Google Sign-In exitoso: " + user.getEmail());
                handleGoogleSignInSuccess(user);
            }

            @Override
            public void onSignInFailure(String error) {
                Log.e(TAG, "❌ Error en Google Sign-In: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                        resetGoogleLoginButton();
                    });
                }
            }

            @Override
            public void onSignInCanceled() {
                Log.d(TAG, "Google Sign-In cancelado por el usuario");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        resetGoogleLoginButton();
                    });
                }
            }
        }, googleSignInLauncher);  // ← Aquí agregamos el launcher
    }

    private void handleGoogleSignInSuccess(FirebaseUser firebaseUser) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Log.d(TAG, "Procesando usuario de Google: " + firebaseUser.getEmail());

                // Buscar si el usuario ya existe en Firestore
                FirebaseManager firebaseManager = FirebaseManager.getInstance();
                firebaseManager.getUserDataFromAnyCollection(firebaseUser.getUid(), new FirebaseManager.UserCallback() {
                    @Override
                    public void onUserFound(UserModel user) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.d(TAG, "✅ Usuario encontrado en Firestore: " + user.getFullName());

                                // Verificar el estado del usuario
                                String userType = user.getUserType();
                                boolean isActive = user.isActive(); // Usar el método correcto

                                if ("superadmin".equals(userType)) {
                                    Toast.makeText(getContext(),
                                            "¡Bienvenido Superadmin " + user.getNombres() + "!",
                                            Toast.LENGTH_SHORT).show();

                                    // Navegar a SuperAdminActivity
                                    Intent intent = new Intent(getActivity(), com.example.proyecto_final_hoteleros.superadmin.activity.SuperAdminActivity.class);
                                    intent.putExtra("userId", firebaseUser.getUid());
                                    intent.putExtra("userEmail", user.getEmail());
                                    intent.putExtra("userName", user.getFullName());
                                    intent.putExtra("userType", user.getUserType());
                                    startActivity(intent);
                                    getActivity().finish();

                                } else if ("client".equals(userType)) {
                                    // Verificar si el cliente está activo
                                    if (isActive) {
                                        Toast.makeText(getContext(),
                                                "¡Bienvenido " + user.getNombres() + "!",
                                                Toast.LENGTH_SHORT).show();
                                        // Navegar a HomeActivity de cliente
                                        Intent intent = new Intent(getActivity(), com.example.proyecto_final_hoteleros.client.ui.activity.HomeActivity.class);
                                        intent.putExtra("userId", firebaseUser.getUid());
                                        intent.putExtra("userEmail", user.getEmail());
                                        intent.putExtra("userName", user.getFullName());
                                        intent.putExtra("userType", user.getUserType());
                                        startActivity(intent);
                                        getActivity().finish();
                                    } else {
                                        Toast.makeText(getContext(),
                                                "Tu cuenta ha sido desactivada. Contacta al administrador para más información.",
                                                Toast.LENGTH_LONG).show();
                                        googleSignInHelper.signOut();
                                        resetGoogleLoginButton();
                                    }

                                } else if ("driver".equals(userType)) {
                                    // Para taxistas, verificar si está activo
                                    if (isActive) {
                                        Toast.makeText(getContext(),
                                                "¡Bienvenido conductor " + user.getNombres() + "!",
                                                Toast.LENGTH_SHORT).show();
                                        // Navegar a DriverActivity
                                        Intent intent = new Intent(getActivity(), com.example.proyecto_final_hoteleros.taxista.activity.DriverActivity.class);
                                        intent.putExtra("userId", firebaseUser.getUid());
                                        intent.putExtra("userEmail", user.getEmail());
                                        intent.putExtra("userName", user.getFullName());
                                        intent.putExtra("userType", user.getUserType());
                                        startActivity(intent);
                                        getActivity().finish();
                                    } else {
                                        Toast.makeText(getContext(),
                                                "Tu cuenta de conductor está pendiente de aprobación o ha sido desactivada. Te notificaremos por email cuando sea aprobada.",
                                                Toast.LENGTH_LONG).show();
                                        googleSignInHelper.signOut();
                                        resetGoogleLoginButton();
                                    }

                                } else if ("admin_hotel".equals(userType)) {
                                    // Verificar si el administrador de hotel está activo
                                    if (isActive) {
                                        Toast.makeText(getContext(),
                                                "¡Bienvenido " + user.getNombres() + "!",
                                                Toast.LENGTH_SHORT).show();
                                        // Navegar a AdminHotelActivity
                                        Intent intent = new Intent(getActivity(), com.example.proyecto_final_hoteleros.adminhotel.activity.AdminHotelActivity.class);
                                        intent.putExtra("userId", firebaseUser.getUid());
                                        intent.putExtra("userEmail", user.getEmail());
                                        intent.putExtra("userName", user.getFullName());
                                        intent.putExtra("userType", user.getUserType());
                                        startActivity(intent);
                                        getActivity().finish();
                                    } else {
                                        Toast.makeText(getContext(),
                                                "Tu cuenta ha sido desactivada. Contacta al administrador.",
                                                Toast.LENGTH_LONG).show();
                                        googleSignInHelper.signOut();
                                        resetGoogleLoginButton();
                                    }

                                } else {
                                    Toast.makeText(getContext(),
                                            "Tipo de usuario no válido para inicio de sesión.",
                                            Toast.LENGTH_SHORT).show();
                                    googleSignInHelper.signOut();
                                    resetGoogleLoginButton();
                                }
                            });
                        }
                    }

                    @Override
                    public void onUserNotFound() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.w(TAG, "Usuario no encontrado en Firestore. Debe registrarse primero.");
                                Toast.makeText(getContext(),
                                        "No tienes una cuenta registrada con este correo. Por favor regístrate primero usando el formulario de registro.",
                                        Toast.LENGTH_LONG).show();

                                // Cerrar sesión de Google/Firebase ya que no está registrado en nuestro sistema
                                googleSignInHelper.signOut();
                                resetGoogleLoginButton();
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.e(TAG, "❌ Error obteniendo datos de usuario: " + error);
                                Toast.makeText(getContext(),
                                        "Error verificando tu cuenta. Por favor inténtalo de nuevo.",
                                        Toast.LENGTH_LONG).show();
                                googleSignInHelper.signOut();
                                resetGoogleLoginButton();
                            });
                        }
                    }
                });
            });
        }
    }

    private void resetGoogleLoginButton() {
        if (btnGoogleLogin != null) {
            btnGoogleLogin.setEnabled(true);
            btnGoogleLogin.setText("Continuar con Google");
            btnGoogleLogin.setAlpha(1.0f);
        }
    }

}