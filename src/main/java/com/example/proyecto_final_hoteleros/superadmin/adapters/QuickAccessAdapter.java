package com.example.proyecto_final_hoteleros.superadmin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.models.QuickAccessItem;

import java.util.List;

public class QuickAccessAdapter extends RecyclerView.Adapter<QuickAccessAdapter.QuickAccessViewHolder> {

    private List<QuickAccessItem> quickAccessItems;
    private OnQuickAccessClickListener clickListener;

    public interface OnQuickAccessClickListener {
        void onQuickAccessClick(QuickAccessItem item);
    }

    public QuickAccessAdapter(List<QuickAccessItem> quickAccessItems, OnQuickAccessClickListener clickListener) {
        this.quickAccessItems = quickAccessItems;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public QuickAccessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.superadmin_item_quick_access, parent, false);
        return new QuickAccessViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuickAccessViewHolder holder, int position) {
        QuickAccessItem item = quickAccessItems.get(position);
        holder.bind(item, clickListener);
    }

    @Override
    public int getItemCount() {
        return quickAccessItems.size();
    }

    public void updateData(List<QuickAccessItem> newItems) {
        this.quickAccessItems.clear();
        this.quickAccessItems.addAll(newItems);
        notifyDataSetChanged();
    }

    static class QuickAccessViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView tvTitle;

        public QuickAccessViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_quick_access_icon);
            tvTitle = itemView.findViewById(R.id.tv_quick_access_title);
        }

        public void bind(QuickAccessItem item, OnQuickAccessClickListener clickListener) {
            ivIcon.setImageResource(item.getIconResId());
            tvTitle.setText(item.getTitle());

            // 🎨 APLICAR COLORES TEMÁTICOS
            int primaryColor = item.getPrimaryColor(itemView.getContext());
            int lightColor = item.getLightColor(itemView.getContext());

            // Cambiar color del ícono
            ivIcon.setColorFilter(primaryColor, android.graphics.PorterDuff.Mode.SRC_IN);

            // Crear drawable circular con color temático
            android.graphics.drawable.GradientDrawable circleDrawable =
                    new android.graphics.drawable.GradientDrawable();
            circleDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            circleDrawable.setColor(lightColor);

            // Aplicar el fondo circular al contenedor del ícono
            View circleView = itemView.findViewById(R.id.circle_background);
            if (circleView != null) {
                circleView.setBackground(circleDrawable);
            }

            // ⚡ ANIMACIÓN DE ENTRADA CON DELAY
            int position = getAdapterPosition();
            long delay = (position + 4) * 80L; // Delay después de las métricas

            itemView.setAlpha(0f);
            itemView.setTranslationY(30f);
            itemView.setScaleX(0.9f);
            itemView.setScaleY(0.9f);

            itemView.animate()
                    .alpha(1.0f)
                    .translationY(0f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(350)
                    .setStartDelay(delay)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
                    .start();

            // ⚡ EFECTO RIPPLE Y ANIMACIÓN MEJORADA
            itemView.setBackground(itemView.getContext().getDrawable(R.drawable.ripple_effect_themed));

            itemView.setOnClickListener(v -> {
                // ⚡ ANIMACIÓN DE CLICK ESPECTACULAR
                v.animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            v.animate()
                                    .scaleX(1.05f)
                                    .scaleY(1.05f)
                                    .setDuration(100)
                                    .withEndAction(() -> {
                                        v.animate()
                                                .scaleX(1.0f)
                                                .scaleY(1.0f)
                                                .setDuration(100)
                                                .start();
                                    })
                                    .start();
                        })
                        .start();

                // ⚡ VIBRACIÓN SUTIL (si está disponible)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    android.os.VibrationEffect effect = android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE);
                    android.os.Vibrator vibrator = (android.os.Vibrator) v.getContext().getSystemService(android.content.Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        vibrator.vibrate(effect);
                    }
                }

                if (clickListener != null) {
                    clickListener.onQuickAccessClick(item);
                }
            });
        }

        // ✅ NUEVO: Método para animación pulsante
        private void addPulseAnimation() {
            itemView.animate()
                    .scaleX(1.03f)
                    .scaleY(1.03f)
                    .setDuration(600)
                    .withEndAction(() -> {
                        itemView.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(600)
                                .withEndAction(() -> {
                                    // Repetir la animación después de 3 segundos
                                    itemView.postDelayed(this::addPulseAnimation, 3000);
                                })
                                .start();
                    })
                    .start();
        }
    }
}
