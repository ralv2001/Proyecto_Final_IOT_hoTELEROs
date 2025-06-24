package com.example.proyecto_final_hoteleros.superadmin.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.models.ChartData;

import java.util.List;
import java.util.Random;

public class ReportChartAdapter extends RecyclerView.Adapter<ReportChartAdapter.ChartViewHolder> {

    private List<ChartData> charts;

    public ReportChartAdapter(List<ChartData> charts) {
        this.charts = charts;
    }

    @NonNull
    @Override
    public ChartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.superadmin_item_report_chart, parent, false);
        return new ChartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChartViewHolder holder, int position) {
        ChartData chart = charts.get(position);
        holder.bind(chart);
    }

    @Override
    public int getItemCount() {
        return charts.size();
    }

    public void updateData(List<ChartData> newCharts) {
        this.charts.clear();
        this.charts.addAll(newCharts);
        notifyDataSetChanged();
    }

    static class ChartViewHolder extends RecyclerView.ViewHolder {
        private CardView cardChart;
        private ImageView ivChartIcon, ivExpand;
        private TextView tvTitle, tvDescription;
        private LinearLayout layoutChartData;
        private LinearLayout  viewChartContainer;
        private boolean isExpanded = false;

        public ChartViewHolder(@NonNull View itemView) {
            super(itemView);
            cardChart = itemView.findViewById(R.id.card_chart);
            ivChartIcon = itemView.findViewById(R.id.iv_chart_icon);
            ivExpand = itemView.findViewById(R.id.iv_expand);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            layoutChartData = itemView.findViewById(R.id.layout_chart_data);
            viewChartContainer = itemView.findViewById(R.id.view_chart_container);
        }

        public void bind(ChartData chart) {
            ivChartIcon.setImageResource(chart.getChartIcon());
            tvTitle.setText(chart.getTitle());
            tvDescription.setText(chart.getDescription());

            // Crear visualización del gráfico
            createChartVisualization(chart);

            // Crear datos detallados para la sección expandible
            createDetailedData(chart);

            // Click listener para expandir/contraer
            cardChart.setOnClickListener(v -> toggleExpansion());
            ivExpand.setOnClickListener(v -> toggleExpansion());

            // Configurar ícono de expansión
            updateExpandIcon();
        }

        private void createDetailedData(ChartData chart) {
            layoutChartData.removeAllViews();

            // Crear contenedor para datos detallados
            LinearLayout container = new LinearLayout(itemView.getContext());
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(16, 16, 16, 16);
            container.setBackgroundColor(Color.parseColor("#F8F9FA"));

            // Título de la sección
            TextView sectionTitle = new TextView(itemView.getContext());
            sectionTitle.setText("📋 Datos Detallados");
            sectionTitle.setTextSize(14);
            sectionTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            sectionTitle.setTextColor(Color.parseColor("#2C2C2C"));
            sectionTitle.setPadding(0, 0, 0, 12);
            container.addView(sectionTitle);

            // Agregar datos según el tipo de gráfico
            switch (chart.getChartType()) {
                case "BAR_CHART":
                    addBarChartDetails(container, chart.getData());
                    break;
                case "PIE_CHART":
                    addPieChartDetails(container, chart.getData());
                    break;
                case "HORIZONTAL_BAR":
                    addHorizontalBarDetails(container, chart.getData());
                    break;
                default:
                    addGenericDetails(container, chart.getData());
                    break;
            }

            layoutChartData.addView(container);
        }

        private void addBarChartDetails(LinearLayout container, List<String> data) {
            int total = 0;
            for (String item : data) {
                int value = Integer.parseInt(item.split(": ")[1]);
                total += value;
            }

            // Mostrar total
            TextView totalView = new TextView(itemView.getContext());
            totalView.setText("📊 Total de reservas: " + total);
            totalView.setTextSize(13);
            totalView.setTextColor(Color.parseColor("#1976D2"));
            totalView.setTypeface(null, android.graphics.Typeface.BOLD);
            totalView.setPadding(0, 0, 0, 8);
            container.addView(totalView);

            // Mostrar promedio
            TextView avgView = new TextView(itemView.getContext());
            avgView.setText("📈 Promedio mensual: " + (total / data.size()));
            avgView.setTextSize(13);
            avgView.setTextColor(Color.parseColor("#388E3C"));
            avgView.setPadding(0, 0, 0, 8);
            container.addView(avgView);

            // Mostrar detalle por mes
            for (String item : data) {
                String[] parts = item.split(": ");
                int value = Integer.parseInt(parts[1]);
                double percentage = (value * 100.0) / total;

                TextView detailView = new TextView(itemView.getContext());
                detailView.setText(String.format("• %s: %d reservas (%.1f%%)", parts[0], value, percentage));
                detailView.setTextSize(12);
                detailView.setTextColor(Color.parseColor("#666666"));
                detailView.setPadding(0, 2, 0, 2);
                container.addView(detailView);
            }
        }

        private void addPieChartDetails(LinearLayout container, List<String> data) {
            TextView infoView = new TextView(itemView.getContext());
            infoView.setText("👥 Análisis de distribución de usuarios:");
            infoView.setTextSize(13);
            infoView.setTextColor(Color.parseColor("#7B1FA2"));
            infoView.setTypeface(null, android.graphics.Typeface.BOLD);
            infoView.setPadding(0, 0, 0, 8);
            container.addView(infoView);

            for (String item : data) {
                TextView detailView = new TextView(itemView.getContext());
                detailView.setText("• " + item);
                detailView.setTextSize(12);
                detailView.setTextColor(Color.parseColor("#666666"));
                detailView.setPadding(0, 4, 0, 4);
                container.addView(detailView);
            }

            TextView trendView = new TextView(itemView.getContext());
            trendView.setText("📈 Tendencia: Los clientes representan la mayoría de usuarios activos");
            trendView.setTextSize(12);
            trendView.setTextColor(Color.parseColor("#FF6F00"));
            trendView.setPadding(0, 8, 0, 0);
            container.addView(trendView);
        }

        private void addHorizontalBarDetails(LinearLayout container, List<String> data) {
            int total = 0;
            for (String item : data) {
                String valueStr = item.split(": S/ ")[1].replace(",", "");
                total += Integer.parseInt(valueStr);
            }

            TextView totalView = new TextView(itemView.getContext());
            totalView.setText("💰 Ingresos totales: S/ " + String.format("%,d", total));
            totalView.setTextSize(13);
            totalView.setTextColor(Color.parseColor("#FF6F00"));
            totalView.setTypeface(null, android.graphics.Typeface.BOLD);
            totalView.setPadding(0, 0, 0, 8);
            container.addView(totalView);

            for (String item : data) {
                String[] parts = item.split(": S/ ");
                int value = Integer.parseInt(parts[1].replace(",", ""));
                double percentage = (value * 100.0) / total;

                TextView detailView = new TextView(itemView.getContext());
                detailView.setText(String.format("• %s: S/ %,d (%.1f%%)", parts[0], value, percentage));
                detailView.setTextSize(12);
                detailView.setTextColor(Color.parseColor("#666666"));
                detailView.setPadding(0, 2, 0, 2);
                container.addView(detailView);
            }
        }

        private void addGenericDetails(LinearLayout container, List<String> data) {
            for (String item : data) {
                TextView detailView = new TextView(itemView.getContext());
                detailView.setText("• " + item);
                detailView.setTextSize(12);
                detailView.setTextColor(Color.parseColor("#666666"));
                detailView.setPadding(0, 4, 0, 4);
                container.addView(detailView);
            }
        }

        private void createChartVisualization(ChartData chart) {
            layoutChartData.removeAllViews();

            switch (chart.getChartType()) {
                case "BAR_CHART":
                    createBarChart(chart.getData());
                    break;
                case "PIE_CHART":
                    createPieChart(chart.getData());
                    break;
                case "HORIZONTAL_BAR":
                    createHorizontalBarChart(chart.getData());
                    break;
                default:
                    createSimpleDataList(chart.getData());
                    break;
            }
        }

        private void createBarChart(List<String> data) {
            viewChartContainer.setBackgroundColor(Color.parseColor("#E3F2FD"));
            viewChartContainer.setPadding(24, 24, 24, 24);

            LinearLayout container = new LinearLayout(itemView.getContext());
            container.setOrientation(LinearLayout.HORIZONTAL);
            container.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);

            int maxValue = getMaxValueFromData(data);
            String[] colors = {"#1976D2", "#1E88E5", "#2196F3", "#42A5F5", "#64B5F6", "#90CAF9"};

            for (int i = 0; i < data.size(); i++) {
                String item = data.get(i);

                LinearLayout barContainer = new LinearLayout(itemView.getContext());
                barContainer.setOrientation(LinearLayout.VERTICAL);
                barContainer.setGravity(android.view.Gravity.CENTER);

                // Configurar márgenes entre barras
                LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f
                );
                containerParams.setMargins(4, 0, 4, 0);
                barContainer.setLayoutParams(containerParams);

                // Extraer valor y etiqueta
                String[] parts = item.split(": ");
                String label = parts[0];
                int value = Integer.parseInt(parts[1]);

                // Etiqueta del valor (arriba de la barra)
                TextView valueLabel = new TextView(itemView.getContext());
                valueLabel.setText(String.valueOf(value));
                valueLabel.setTextSize(12);
                valueLabel.setTextColor(Color.parseColor("#1976D2"));
                valueLabel.setTypeface(null, android.graphics.Typeface.BOLD);
                valueLabel.setGravity(android.view.Gravity.CENTER);
                valueLabel.setPadding(0, 0, 0, 8);

                // Crear barra visual más grande
                View bar = new View(itemView.getContext());
                int barHeight = (int) (120 * ((float) value / maxValue)); // Altura máxima 120dp
                int barWidth = 40; // Ancho fijo más grande
                LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(barWidth, barHeight);
                bar.setLayoutParams(barParams);
                bar.setBackgroundColor(Color.parseColor(colors[i % colors.length]));

                // Agregar esquinas redondeadas a las barras
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
                    shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                    shape.setColor(Color.parseColor(colors[i % colors.length]));
                    shape.setCornerRadii(new float[]{12, 12, 12, 12, 0, 0, 0, 0}); // Solo esquinas superiores
                    bar.setBackground(shape);
                }

                // Etiqueta del mes (debajo de la barra)
                TextView monthLabel = new TextView(itemView.getContext());
                monthLabel.setText(label.substring(0, Math.min(3, label.length()))); // Primeras 3 letras
                monthLabel.setTextSize(11);
                monthLabel.setTextColor(Color.parseColor("#666666"));
                monthLabel.setGravity(android.view.Gravity.CENTER);
                monthLabel.setPadding(0, 12, 0, 0);
                monthLabel.setTypeface(null, android.graphics.Typeface.BOLD);

                barContainer.addView(valueLabel);
                barContainer.addView(bar);
                barContainer.addView(monthLabel);

                container.addView(barContainer);
            }

            viewChartContainer.removeAllViews();
            viewChartContainer.addView(container);
        }

        private void createPieChart(List<String> data) {
            viewChartContainer.setBackgroundColor(Color.parseColor("#F3E5F5"));
            viewChartContainer.setPadding(24, 24, 24, 24);

            LinearLayout container = new LinearLayout(itemView.getContext());
            container.setOrientation(LinearLayout.VERTICAL);
            container.setGravity(android.view.Gravity.CENTER);

            // Agregar título visual
            TextView titleView = new TextView(itemView.getContext());
            titleView.setText("📊 Distribución de Usuarios");
            titleView.setTextSize(14);
            titleView.setTypeface(null, android.graphics.Typeface.BOLD);
            titleView.setTextColor(Color.parseColor("#7B1FA2"));
            titleView.setGravity(android.view.Gravity.CENTER);
            titleView.setPadding(0, 0, 0, 16);
            container.addView(titleView);

            // Crear indicadores de colores más grandes
            String[] colors = {"#2196F3", "#4CAF50", "#FF9800", "#F44336"};

            for (int i = 0; i < data.size(); i++) {
                LinearLayout item = new LinearLayout(itemView.getContext());
                item.setOrientation(LinearLayout.HORIZONTAL);
                item.setGravity(android.view.Gravity.CENTER_VERTICAL);
                item.setPadding(0, 8, 0, 8);

                // Indicador de color más grande
                View colorIndicator = new View(itemView.getContext());
                LinearLayout.LayoutParams colorParams = new LinearLayout.LayoutParams(24, 24);
                colorParams.setMargins(0, 0, 16, 0);
                colorIndicator.setLayoutParams(colorParams);
                colorIndicator.setBackgroundColor(Color.parseColor(colors[i % colors.length]));

                // Esquinas redondeadas para el indicador
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
                    shape.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                    shape.setColor(Color.parseColor(colors[i % colors.length]));
                    colorIndicator.setBackground(shape);
                }

                // Texto del dato más grande
                TextView textData = new TextView(itemView.getContext());
                textData.setText(data.get(i));
                textData.setTextSize(15);
                textData.setTextColor(Color.parseColor("#333333"));
                textData.setTypeface(null, android.graphics.Typeface.BOLD);

                item.addView(colorIndicator);
                item.addView(textData);
                container.addView(item);
            }

            viewChartContainer.removeAllViews();
            viewChartContainer.addView(container);
        }

        private void createHorizontalBarChart(List<String> data) {
            viewChartContainer.setBackgroundColor(Color.parseColor("#FFF3E0"));
            viewChartContainer.setPadding(24, 24, 24, 24);

            LinearLayout container = new LinearLayout(itemView.getContext());
            container.setOrientation(LinearLayout.VERTICAL);

            int maxValue = getMaxValueFromHotelData(data);

            for (String item : data) {
                LinearLayout barItem = new LinearLayout(itemView.getContext());
                barItem.setOrientation(LinearLayout.HORIZONTAL);
                barItem.setGravity(android.view.Gravity.CENTER_VERTICAL);
                barItem.setPadding(0, 8, 0, 8);

                // Extraer nombre y valor
                String[] parts = item.split(": S/ ");
                String hotelName = parts[0];
                int value = Integer.parseInt(parts[1].replace(",", ""));

                // Nombre del hotel
                TextView nameLabel = new TextView(itemView.getContext());
                nameLabel.setText(hotelName);
                nameLabel.setTextSize(12);
                nameLabel.setTextColor(Color.parseColor("#333333"));
                nameLabel.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                nameLabel.setLayoutParams(nameParams);

                // Barra de progreso más grande
                ProgressBar progressBar = new ProgressBar(itemView.getContext(), null, android.R.attr.progressBarStyleHorizontal);
                LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(150, 24); // Más grande
                progressParams.setMargins(12, 0, 12, 0);
                progressBar.setLayoutParams(progressParams);
                progressBar.setMax(maxValue);
                progressBar.setProgress(value);
                progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800")));
                progressBar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFE0B2")));

                // Valor más visible
                TextView valueLabel = new TextView(itemView.getContext());
                valueLabel.setText("S/ " + formatNumber(value));
                valueLabel.setTextSize(11);
                valueLabel.setTextColor(Color.parseColor("#FF6F00"));
                valueLabel.setTypeface(null, android.graphics.Typeface.BOLD);

                barItem.addView(nameLabel);
                barItem.addView(progressBar);
                barItem.addView(valueLabel);

                container.addView(barItem);
            }

            viewChartContainer.removeAllViews();
            viewChartContainer.addView(container);
        }

        private void createSimpleDataList(List<String> data) {
            LinearLayout container = new LinearLayout(itemView.getContext());
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(16, 16, 16, 16);

            for (String item : data) {
                TextView textView = new TextView(itemView.getContext());
                textView.setText("• " + item);
                textView.setTextSize(14);
                textView.setTextColor(Color.parseColor("#333333"));
                textView.setPadding(0, 4, 0, 4);
                container.addView(textView);
            }

            layoutChartData.addView(container);
        }

        private void toggleExpansion() {
            isExpanded = !isExpanded;

            if (isExpanded) {
                layoutChartData.setVisibility(View.VISIBLE);
                // Animación suave de expansión
                layoutChartData.setAlpha(0f);
                layoutChartData.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start();
            } else {
                layoutChartData.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> layoutChartData.setVisibility(View.GONE))
                        .start();
            }

            updateExpandIcon();
        }

        private void updateExpandIcon() {
            ivExpand.animate()
                    .rotation(isExpanded ? 180f : 0f)
                    .setDuration(200)
                    .start();
        }

        private int getMaxValueFromData(List<String> data) {
            int max = 0;
            for (String item : data) {
                try {
                    int value = Integer.parseInt(item.split(": ")[1]);
                    max = Math.max(max, value);
                } catch (Exception e) {
                    // Ignorar errores de parsing
                }
            }
            return max > 0 ? max : 100;
        }

        private int getMaxValueFromHotelData(List<String> data) {
            int max = 0;
            for (String item : data) {
                try {
                    String valueStr = item.split(": S/ ")[1].replace(",", "");
                    int value = Integer.parseInt(valueStr);
                    max = Math.max(max, value);
                } catch (Exception e) {
                    // Ignorar errores de parsing
                }
            }
            return max > 0 ? max : 15000;
        }

        private int getRandomBarColor() {
            String[] colors = {"#2196F3", "#4CAF50", "#FF9800", "#F44336", "#9C27B0", "#00BCD4"};
            Random random = new Random();
            return Color.parseColor(colors[random.nextInt(colors.length)]);
        }

        private String formatNumber(int number) {
            if (number >= 1000) {
                return String.format("%.1fk", number / 1000.0);
            }
            return String.valueOf(number);
        }

    }
}