package com.example.proyecto_final_hoteleros.taxista.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.taxista.utils.QRCodeScanner;
import com.example.proyecto_final_hoteleros.utils.FirebaseManager;
import com.google.android.material.button.MaterialButton;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class ServiceCompletionFragment extends Fragment implements QRCodeScanner.QRScanCallback {

    private static final String TAG = "ServiceCompletionFragment";

    private String reservationId;
    private String clientName;
    private String hotelName;

    private TextView tvServiceInfo;
    private DecoratedBarcodeView barcodeView;
    private MaterialButton btnStartScan;
    private MaterialButton btnCancel;
    private View scannerPlaceholder;

    private QRCodeScanner qrScanner;
    private FirebaseManager firebaseManager;

    public static ServiceCompletionFragment newInstance(String reservationId, String clientName, String hotelName) {
        ServiceCompletionFragment fragment = new ServiceCompletionFragment();
        Bundle args = new Bundle();
        args.putString("reservationId", reservationId);
        args.putString("clientName", clientName);
        args.putString("hotelName", hotelName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            reservationId = getArguments().getString("reservationId");
            clientName = getArguments().getString("clientName");
            hotelName = getArguments().getString("hotelName");
        }

        firebaseManager = FirebaseManager.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.taxi_fragment_service_completion_simple, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        tvServiceInfo = view.findViewById(R.id.tvServiceInfo);
        barcodeView = view.findViewById(R.id.barcodeView);
        btnStartScan = view.findViewById(R.id.btnStartScan);
        btnCancel = view.findViewById(R.id.btnCancel);
        scannerPlaceholder = view.findViewById(R.id.scannerPlaceholder); // Nueva línea

        // Configurar QR scanner
        qrScanner = new QRCodeScanner(this, barcodeView);

        // Configurar información
        updateServiceInfo();

        // Configurar listeners
        btnStartScan.setOnClickListener(v -> startQRScan());
        btnCancel.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });
    }

    private void updateServiceInfo() {
        String info = "🏨 Hotel: " + hotelName + "\n\n" +
                "👤 Cliente: " + clientName + "\n\n" +
                "📍 Destino: Aeropuerto";

        tvServiceInfo.setText(info);
    }

    private void startQRScan() {
        btnStartScan.setEnabled(false);
        btnStartScan.setText("📸 Activando cámara...");

        // Animación para ocultar placeholder y mostrar scanner
        if (scannerPlaceholder != null) {
            scannerPlaceholder.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        scannerPlaceholder.setVisibility(View.GONE);
                        barcodeView.setVisibility(View.VISIBLE);
                        barcodeView.setAlpha(0f);
                        barcodeView.animate()
                                .alpha(1f)
                                .setDuration(300)
                                .start();
                    })
                    .start();
        } else {
            barcodeView.setVisibility(View.VISIBLE);
        }

        // Actualizar texto del botón después de un delay
        new android.os.Handler().postDelayed(() -> {
            if (btnStartScan != null) {
                btnStartScan.setText("🔍 Escaneando...");
            }
        }, 1000);

        qrScanner.startScanning(this);
    }

    @Override
    public void onQRScanned(String qrContent) {
        Log.d(TAG, "✅ QR leído: " + qrContent);

        // ✅ ACEPTAR CUALQUIER QR - NO VALIDAR:
        if (qrContent != null && !qrContent.trim().isEmpty()) {
            Log.d(TAG, "✅ QR válido detectado, finalizando servicio...");
            completeService(qrContent);
        } else {
            Log.w(TAG, "⚠️ QR vacío o inválido");
            onScanError("Código QR vacío. Inténtalo de nuevo.");
        }
    }

    @Override
    public void onScanError(String error) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                resetScanButton();
                barcodeView.setVisibility(View.GONE);
            });
        }
    }

    private void resetScanButton() {
        btnStartScan.setEnabled(true);
        btnStartScan.setText("📱 Escanear QR del Cliente");

        // Animación para volver al placeholder
        if (barcodeView != null && scannerPlaceholder != null) {
            barcodeView.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        barcodeView.setVisibility(View.GONE);
                        scannerPlaceholder.setVisibility(View.VISIBLE);
                        scannerPlaceholder.setAlpha(0f);
                        scannerPlaceholder.animate()
                                .alpha(1f)
                                .setDuration(300)
                                .start();
                    })
                    .start();
        }
    }


    // Nuevo método para mejorar el feedback visual cuando se completa el escaneo
    private void completeService(String qrContent) {
        Log.d(TAG, "🎯 Completando servicio con QR: " + qrContent);

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // Cambiar botón a estado de éxito
                btnStartScan.setText("✅ ¡QR Leído!");
                btnStartScan.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                androidx.core.content.ContextCompat.getColor(getContext(), R.color.success_green)
                        )
                );

                Toast.makeText(getContext(),
                        "✅ QR leído! Finalizando servicio...",
                        Toast.LENGTH_SHORT).show();

                // Esperar un poco antes de finalizar servicio
                new android.os.Handler().postDelayed(() -> {
                    showSuccess();
                }, 1500);
            });
        }
    }

    private void showSuccess() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(),
                        "🎉 ¡Servicio completado exitosamente!\n" +
                                "Puedes aceptar un nuevo viaje.",
                        Toast.LENGTH_LONG).show();

                // ✅ REGRESAR AL MAPA Y LIMPIAR SERVICIO ACTIVO:
                regresarAlMapaLimpio();
            });
        }
    }
    // ✅ NUEVO MÉTODO PARA REGRESAR AL MAPA LIMPIO:
    private void regresarAlMapaLimpio() {
        if (getParentFragmentManager() != null) {
            // Crear nuevo DriverMapFragment limpio (sin argumentos de servicio)
            DriverMapFragment mapFragment = new DriverMapFragment();

            // Limpiar stack y regresar al mapa
            getParentFragmentManager().popBackStack(null,
                    androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);

            // Cargar mapa limpio
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mapFragment)
                    .commit();

            Log.d(TAG, "✅ Regresado al mapa limpio - listo para nuevo servicio");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (qrScanner != null) {
            qrScanner.handlePermissionResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (qrScanner != null) {
            qrScanner.stopScanning();
        }
    }
}