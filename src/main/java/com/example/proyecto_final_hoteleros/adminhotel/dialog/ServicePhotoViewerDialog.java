package com.example.proyecto_final_hoteleros.adminhotel.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.FullScreenPhotoAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ServicePhotoViewerDialog extends Dialog {

    private static final String TAG = "ServicePhotoViewerDialog";

    private ViewPager2 viewPager;
    private TextView tvPhotoCounter, tvPhotoTitle;
    private MaterialButton btnClose;
    private ImageView ivPrevious, ivNext;

    private List<String> photoUrls;
    private int currentPosition;
    private String serviceTitle;
    private FullScreenPhotoAdapter adapter;

    public ServicePhotoViewerDialog(@NonNull Context context, List<String> photoUrls,
                                    int initialPosition, String serviceTitle) {
        super(context);
        this.photoUrls = photoUrls;
        this.currentPosition = initialPosition;
        this.serviceTitle = serviceTitle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar dialog fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.admin_hotel_dialog_service_photo_viewer);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            getWindow().setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        initViews();
        setupViewPager();
        setupListeners();
        updatePhotoCounter();

        Log.d(TAG, "📸 Dialog creado con " + photoUrls.size() + " fotos, posición inicial: " + currentPosition);
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPagerPhotos);
        tvPhotoCounter = findViewById(R.id.tvPhotoCounter);
        tvPhotoTitle = findViewById(R.id.tvPhotoTitle);
        btnClose = findViewById(R.id.btnClose);
        ivPrevious = findViewById(R.id.ivPrevious);
        ivNext = findViewById(R.id.ivNext);

        // Configurar título
        if (tvPhotoTitle != null && serviceTitle != null) {
            tvPhotoTitle.setText("Fotos de " + serviceTitle);
        }
    }

    private void setupViewPager() {
        adapter = new FullScreenPhotoAdapter(getContext(), photoUrls);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                updatePhotoCounter();
                updateNavigationButtons();
            }
        });
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());

        ivPrevious.setOnClickListener(v -> {
            if (currentPosition > 0) {
                viewPager.setCurrentItem(currentPosition - 1, true);
            }
        });

        ivNext.setOnClickListener(v -> {
            if (currentPosition < photoUrls.size() - 1) {
                viewPager.setCurrentItem(currentPosition + 1, true);
            }
        });

        updateNavigationButtons();
    }

    private void updatePhotoCounter() {
        if (tvPhotoCounter != null) {
            String counterText = (currentPosition + 1) + " de " + photoUrls.size();
            tvPhotoCounter.setText(counterText);
        }
    }

    private void updateNavigationButtons() {
        if (ivPrevious != null) {
            ivPrevious.setAlpha(currentPosition > 0 ? 1.0f : 0.3f);
            ivPrevious.setEnabled(currentPosition > 0);
        }

        if (ivNext != null) {
            ivNext.setAlpha(currentPosition < photoUrls.size() - 1 ? 1.0f : 0.3f);
            ivNext.setEnabled(currentPosition < photoUrls.size() - 1);
        }
    }
}