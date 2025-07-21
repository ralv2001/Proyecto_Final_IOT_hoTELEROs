package com.example.proyecto_final_hoteleros.taxista.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.google.zxing.ResultPoint;
import java.util.List;

public class QRCodeScanner {
    private static final String TAG = "QRCodeScanner";
    private static final int CAMERA_PERMISSION_REQUEST = 200;

    private Context context;
    private Fragment fragment;
    private DecoratedBarcodeView barcodeView;
    private QRScanCallback callback;

    public interface QRScanCallback {
        void onQRScanned(String qrContent);
        void onScanError(String error);
    }

    public QRCodeScanner(Fragment fragment, DecoratedBarcodeView barcodeView) {
        this.fragment = fragment;
        this.context = fragment.getContext();
        this.barcodeView = barcodeView;
    }

    public void startScanning(QRScanCallback callback) {
        this.callback = callback;

        if (!checkCameraPermission()) {
            requestCameraPermission();
            return;
        }

        Log.d(TAG, "📷 Iniciando escaneo de QR...");

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                // Pausar escaneo inmediatamente
                barcodeView.pause();

                Log.d(TAG, "📱 QR escaneado: " + result.getText());

                if (callback != null) {
                    callback.onQRScanned(result.getText());
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {}
        });

        barcodeView.resume();
    }

    public void stopScanning() {
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }

    private boolean checkCameraPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        fragment.requestPermissions(
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST
        );
    }

    public void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning(callback);
            } else {
                if (callback != null) {
                    callback.onScanError("Se necesita permiso de cámara para escanear códigos QR");
                }
            }
        }
    }
}