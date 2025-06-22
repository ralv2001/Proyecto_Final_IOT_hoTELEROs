package com.example.proyecto_final_hoteleros.superadmin.models;

import java.util.List;

public class ChartData {
    private String title;
    private String chartType; // BAR_CHART, PIE_CHART, LINE_CHART, HORIZONTAL_BAR
    private List<String> data;
    private String description;

    public ChartData(String title, String chartType, List<String> data, String description) {
        this.title = title;
        this.chartType = chartType;
        this.data = data;
        this.description = description;
    }

    // Getters
    public String getTitle() { return title; }
    public String getChartType() { return chartType; }
    public List<String> getData() { return data; }
    public String getDescription() { return description; }

    public int getChartIcon() {
        switch (chartType) {
            case "BAR_CHART":
                return android.R.drawable.ic_menu_sort_by_size;
            case "PIE_CHART":
                return android.R.drawable.ic_menu_compass;
            case "LINE_CHART":
                return android.R.drawable.ic_menu_agenda;
            case "HORIZONTAL_BAR":
                return android.R.drawable.ic_menu_sort_alphabetically;
            default:
                return android.R.drawable.ic_menu_info_details;
        }
    }
}