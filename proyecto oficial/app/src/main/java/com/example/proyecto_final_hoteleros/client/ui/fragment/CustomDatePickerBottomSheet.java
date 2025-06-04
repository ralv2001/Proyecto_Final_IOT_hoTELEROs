package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.proyecto_final_hoteleros.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomDatePickerBottomSheet extends BottomSheetDialogFragment {
    private static final String TAG = "CustomDatePickerBottomSheet";

    public interface DateRangeListener {
        void onDateRangeSelected(Date startDate, Date endDate);
    }

    private DateRangeListener listener;
    private SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM", new Locale("es", "ES"));
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
    private Calendar currentMonth = Calendar.getInstance();
    private Calendar selectedStartDate = null;
    private Calendar selectedEndDate = null;
    private CalendarAdapter adapter;
    private TextView tvCurrentMonthYear;
    private TextView tvStartDate;
    private TextView tvEndDate;

    public void setListener(DateRangeListener listener) {
        this.listener = listener;
    }

    // Modificación a realizar en onCreateDialog
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog);

        // Configurar el comportamiento del BottomSheet
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);

                // En lugar de expandir completamente, configurar para que se ajuste al contenido
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                // Esto hace que se muestre inicialmente a la altura especificada en peekHeight

                behavior.setSkipCollapsed(false); // Para que respete el estado collapsed

                // Opcional: definir la altura máxima como un porcentaje de la pantalla
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                int displayHeight = getActivity().getResources().getDisplayMetrics().heightPixels;
                int maxHeight = (int) (displayHeight * 0.80); // 80% de la altura de la pantalla
                layoutParams.height = maxHeight;
                bottomSheet.setLayoutParams(layoutParams);
            }
        });
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.client_bottom_sheet_custom_date_picker, container, false);

        tvCurrentMonthYear = view.findViewById(R.id.tvCurrentMonthYear);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        ImageButton btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        ImageButton btnNextMonth = view.findViewById(R.id.btnNextMonth);
        GridView calendarGridView = view.findViewById(R.id.calendarGridView);
        Button btnConfirm = view.findViewById(R.id.btnConfirmDates);

        // Configurar el adaptador
        adapter = new CalendarAdapter(requireContext());
        calendarGridView.setAdapter(adapter);
        updateMonthDisplay();

        // Listeners
        btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            updateMonthDisplay();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            updateMonthDisplay();
        });

        calendarGridView.setOnItemClickListener((parent, v, position, id) -> {
            CalendarDay day = adapter.getItem(position);
            if (day.isEnabled) {
                handleDateSelection(day.date);
                updateMonthDisplay(); // Actualizar el calendario para reflejar los cambios
                updateSelectedDatesDisplay();
            }
        });

        btnConfirm.setOnClickListener(v -> {
            if (selectedStartDate != null && selectedEndDate != null && listener != null) {
                listener.onDateRangeSelected(selectedStartDate.getTime(), selectedEndDate.getTime());
                dismiss();
            }
        });

        // Configurar fechas iniciales
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        selectedStartDate = (Calendar) today.clone();
        selectedEndDate = (Calendar) today.clone();
        selectedEndDate.add(Calendar.DAY_OF_MONTH, 1);

        updateSelectedDatesDisplay();

        return view;
    }

    private void updateMonthDisplay() {
        tvCurrentMonthYear.setText(monthYearFormat.format(currentMonth.getTime()));
        adapter.updateCalendar(currentMonth);
    }

    private void updateSelectedDatesDisplay() {
        if (selectedStartDate != null) {
            tvStartDate.setText(displayFormat.format(selectedStartDate.getTime()));
        }
        if (selectedEndDate != null) {
            tvEndDate.setText(displayFormat.format(selectedEndDate.getTime()));
        }
    }

    private void handleDateSelection(Calendar date) {
        // Clonar la fecha para evitar modificaciones no deseadas
        Calendar selectedDate = (Calendar) date.clone();

        // Establecer la hora a 0 para comparaciones correctas
        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
        selectedDate.set(Calendar.MINUTE, 0);
        selectedDate.set(Calendar.SECOND, 0);
        selectedDate.set(Calendar.MILLISECOND, 0);

        // Si no hay fecha de inicio o si ya hay un rango completo seleccionado
        // O si se selecciona una fecha anterior a la fecha de inicio actual
        if (selectedStartDate == null || selectedEndDate != null ||
                selectedDate.before(selectedStartDate)) {
            // Iniciar nueva selección
            selectedStartDate = selectedDate;
            selectedEndDate = null;
        } else {
            // Ya existe una fecha de inicio, establecer fecha final
            selectedEndDate = selectedDate;

            // Asegurar que la fecha final no sea anterior a la inicial
            if (selectedEndDate.before(selectedStartDate)) {
                Calendar temp = selectedStartDate;
                selectedStartDate = selectedEndDate;
                selectedEndDate = temp;
            }
        }
    }

    private static class CalendarDay {
        Calendar date;
        boolean isEnabled;
        boolean isToday;
        boolean isSelected;
        boolean isInRange;
        boolean isStartDate;
        boolean isEndDate;

        public CalendarDay(Calendar date, boolean isEnabled, boolean isToday) {
            this.date = (Calendar) date.clone();
            this.isEnabled = isEnabled;
            this.isToday = isToday;
            this.isSelected = false;
            this.isInRange = false;
            this.isStartDate = false;
            this.isEndDate = false;
        }
    }

    private class CalendarAdapter extends BaseAdapter {
        private List<CalendarDay> days;
        private LayoutInflater inflater;
        private Calendar today;

        public CalendarAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            days = new ArrayList<>();
            today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
        }

        public void updateCalendar(Calendar month) {
            days.clear();

            Calendar calendar = (Calendar) month.clone();
            calendar.set(Calendar.DAY_OF_MONTH, 1);

            int monthStartDay = calendar.get(Calendar.DAY_OF_WEEK);
            // Ajustar para que la semana comience en domingo (DAY_OF_WEEK = 1)
            calendar.add(Calendar.DAY_OF_MONTH, -(monthStartDay - 1));

            // Añadir días anteriores al mes actual
            while (calendar.get(Calendar.MONTH) != month.get(Calendar.MONTH)) {
                CalendarDay day = new CalendarDay(calendar, false, false);
                days.add(day);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Añadir días del mes actual
            while (calendar.get(Calendar.MONTH) == month.get(Calendar.MONTH)) {
                boolean isToday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                        calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);

                CalendarDay day = new CalendarDay(calendar, true, isToday);

                // Comprobar si el día es fecha de inicio
                if (selectedStartDate != null && isSameDay(calendar, selectedStartDate)) {
                    day.isStartDate = true;
                    day.isSelected = true;
                }

                // Comprobar si el día es fecha de fin
                if (selectedEndDate != null && isSameDay(calendar, selectedEndDate)) {
                    day.isEndDate = true;
                    day.isSelected = true;
                }

                // Comprobar si el día está en el rango (entre inicio y fin)
                if (selectedStartDate != null && selectedEndDate != null) {
                    if (calendar.after(selectedStartDate) && calendar.before(selectedEndDate)) {
                        day.isInRange = true;
                    }
                }

                days.add(day);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Completar la cuadrícula con días del mes siguiente
            while (days.size() < 42) {
                CalendarDay day = new CalendarDay(calendar, false, false);
                days.add(day);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            notifyDataSetChanged();
        }

        // Método auxiliar para comparar si dos fechas son el mismo día
        private boolean isSameDay(Calendar cal1, Calendar cal2) {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                    cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
        }

        @Override
        public int getCount() {
            return days.size();
        }

        @Override
        public CalendarDay getItem(int position) {
            return days.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.client_item_calendar_day, parent, false);
                holder = new ViewHolder();
                holder.tvDayText = convertView.findViewById(R.id.tvDayText);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            CalendarDay day = getItem(position);
            holder.tvDayText.setText(String.valueOf(day.date.get(Calendar.DAY_OF_MONTH)));

            // Resetear el estilo
            holder.tvDayText.setBackgroundResource(R.drawable.bg_day_normal);

            if (!day.isEnabled) {
                // Días de meses anterior/siguiente
                holder.tvDayText.setTextColor(
                        ContextCompat.getColor(holder.tvDayText.getContext(), android.R.color.darker_gray)
                );
            } else if (day.isStartDate && day.isEndDate) {
                // Cuando es el mismo día para inicio y fin (un solo día seleccionado)
                holder.tvDayText.setTextColor(
                        ContextCompat.getColor(holder.tvDayText.getContext(), android.R.color.white)
                );
                holder.tvDayText.setBackgroundResource(R.drawable.bg_day_single);
            } else if (day.isStartDate) {
                // Primer día del rango
                holder.tvDayText.setTextColor(
                        ContextCompat.getColor(holder.tvDayText.getContext(), android.R.color.white)
                );
                holder.tvDayText.setBackgroundResource(R.drawable.bg_day_start);
            } else if (day.isEndDate) {
                // Último día del rango
                holder.tvDayText.setTextColor(
                        ContextCompat.getColor(holder.tvDayText.getContext(), android.R.color.white)
                );
                holder.tvDayText.setBackgroundResource(R.drawable.bg_day_end);
            } else if (day.isInRange) {
                // Días en el rango seleccionado
                holder.tvDayText.setTextColor(
                        ContextCompat.getColor(holder.tvDayText.getContext(), R.color.black)
                );
                holder.tvDayText.setBackgroundResource(R.drawable.bg_day_range);
                // Para un efecto continuo, usamos un layout con ancho completo
                holder.tvDayText.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            } else if (day.isToday) {
                // Día actual
                holder.tvDayText.setTextColor(
                        ContextCompat.getColor(holder.tvDayText.getContext(), R.color.colorPrimary)
                );
                // Restaurar ancho normal
                holder.tvDayText.getLayoutParams().width = getResources().getDimensionPixelSize(R.dimen.day_cell_width);
            } else {
                // Días normales
                holder.tvDayText.setTextColor(
                        ContextCompat.getColor(holder.tvDayText.getContext(), R.color.black)
                );
                // Restaurar ancho normal
                holder.tvDayText.getLayoutParams().width = getResources().getDimensionPixelSize(R.dimen.day_cell_width);
            }

            // Asegurarnos de que los cambios de layout se apliquen
            holder.tvDayText.requestLayout();

            return convertView;
        }

        private class ViewHolder {
            TextView tvDayText;
        }
    }
}