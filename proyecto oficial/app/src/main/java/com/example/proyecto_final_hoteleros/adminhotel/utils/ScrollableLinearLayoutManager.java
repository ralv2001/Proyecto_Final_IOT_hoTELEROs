package com.example.proyecto_final_hoteleros.adminhotel.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * LayoutManager personalizado que soluciona el problema de RecyclerViews
 * dentro de ScrollViews que solo muestran algunos elementos.
 */
public class ScrollableLinearLayoutManager extends LinearLayoutManager {

    private static final String TAG = "ScrollableLinearLM";

    public ScrollableLinearLayoutManager(Context context) {
        super(context);
    }

    public ScrollableLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state,
                          int widthSpec, int heightSpec) {

        // ✅ SOLUCIÓN: Cambiar la medición de altura para mostrar todos los elementos
        int newHeightSpec = heightSpec;

        if (getItemCount() > 0) {
            try {
                // ✅ CORREGIDO: Obtener View directamente, no ViewHolder
                View itemView = recycler.getViewForPosition(0);
                if (itemView != null) {
                    // Medir la vista
                    measureChildWithMargins(itemView, 0, 0);

                    int itemHeight = getDecoratedMeasuredHeight(itemView);
                    int itemCount = getItemCount();
                    int totalHeight = itemHeight * itemCount;

                    // Agregar padding extra por seguridad
                    totalHeight += getPaddingTop() + getPaddingBottom() + (itemCount * 16);

                    // Crear nuevo heightSpec sin límites (UNSPECIFIED)
                    newHeightSpec = MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.UNSPECIFIED);

                    Log.d(TAG, "📐 Calculando altura - Items: " + itemCount +
                            ", Altura por item: " + itemHeight +
                            ", Altura total: " + totalHeight);

                    // ✅ CORREGIDO: Reciclar la vista correctamente
                    recycler.recycleView(itemView);
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error calculando altura del RecyclerView: " + e.getMessage());
                // En caso de error, usar medición normal
                newHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            }
        }

        // Llamar al método padre con la nueva especificación
        super.onMeasure(recycler, state, widthSpec, newHeightSpec);
    }

    @Override
    public boolean canScrollVertically() {
        // ✅ CRÍTICO: Deshabilitar scroll vertical para evitar conflictos con ScrollView
        return false;
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    // ✅ OPTIMIZACIÓN: Soporte para predicción de elementos
    @Override
    public int getExtraLayoutSpace(RecyclerView.State state) {
        return 200; // Espacio extra para mejorar el rendimiento
    }

    // ✅ DEBUGGING: Método para obtener información del layout
    public void logLayoutInfo() {
        Log.d(TAG, "📊 Layout Info - Items: " + getItemCount() +
                ", Children: " + getChildCount() +
                ", Width: " + getWidth() +
                ", Height: " + getHeight());
    }
}