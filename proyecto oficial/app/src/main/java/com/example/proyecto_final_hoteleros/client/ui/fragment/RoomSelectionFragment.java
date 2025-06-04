package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.adapters.RoomTypeAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.RoomType;

import java.util.ArrayList;
import java.util.List;

public class RoomSelectionFragment extends Fragment {

    private RecyclerView rvRoomTypes;
    private Button btnNextStep;
    private TextView tvHotelName;
    private ImageButton btnBack;
    private RoomTypeAdapter adapter;
    private String hotelName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_selection, container, false);

        // Inicializar vistas
        rvRoomTypes = view.findViewById(R.id.rv_room_types);
        btnNextStep = view.findViewById(R.id.btn_next_step);
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        btnBack = view.findViewById(R.id.btn_back);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener datos del hotel desde los argumentos
        if (getArguments() != null) {
            hotelName = getArguments().getString("hotel_name", "Belmond Miraflores Park");
            tvHotelName.setText(hotelName);
        }

        // Configurar el RecyclerView
        setupRoomTypes();

        // Configurar botones y eventos
        setupActions();
    }

    private void setupRoomTypes() {
        // Crear lista de tipos de habitación
        List<RoomType> roomTypes = new ArrayList<>();
        roomTypes.add(new RoomType("Habitación Estándar", 30, "S/290", R.drawable.belmond));
        roomTypes.add(new RoomType("Habitación Deluxe", 40, "S/350", R.drawable.belmond));
        roomTypes.add(new RoomType("Suite Junior", 50, "S/450", R.drawable.belmond));
        roomTypes.add(new RoomType("Suite Presidencial", 70, "S/650", R.drawable.belmond));

        // Configurar adapter
        adapter = new RoomTypeAdapter(roomTypes, position -> {
            // Callback cuando se selecciona una habitación
            Toast.makeText(getContext(), "Habitación seleccionada: " + roomTypes.get(position).getName(), Toast.LENGTH_SHORT).show();
        });

        rvRoomTypes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRoomTypes.setAdapter(adapter);
    }

    private void setupActions() {
        // Botón de retroceso
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Botón de siguiente paso
        btnNextStep.setOnClickListener(v -> {
            // Verificar si hay una habitación seleccionada
            if (adapter.getSelectedPosition() == -1) {
                Toast.makeText(getContext(), "Por favor selecciona una habitación", Toast.LENGTH_SHORT).show();
                return;
            }

            // Implementar navegación al siguiente paso (reserva)
            navigateToBooking();
        });
    }


    private void navigateToBooking() {
        // Obtener la habitación seleccionada
        RoomType selectedRoom = adapter.getSelectedPosition() != -1 ?
                adapter.getRoomTypes().get(adapter.getSelectedPosition()) : null;

        if (selectedRoom == null) {
            Toast.makeText(getContext(), "Por favor selecciona una habitación", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear bundle con los datos
        Bundle args = new Bundle();
        args.putString("hotel_name", hotelName);
        args.putString("hotel_address", "Miraflores, Lima, Perú");
        args.putParcelable("selected_room", selectedRoom);
        args.putString("check_in_date", "8 abril");
        args.putString("check_out_date", "9 abril");
        args.putInt("num_adults", 2);
        args.putInt("num_children", 0);
        args.putString("room_number", "9634448852");
        args.putBoolean("has_free_transport", false);
        args.putDouble("additional_services_price", 60.0);

        // Navegar al fragmento de resumen de reserva
        // Con Navigation Component sería:
        // NavDirections action = RoomSelectionFragmentDirections.actionRoomSelectionToBookingSummary(args);
        // Navigation.findNavController(requireView()).navigate(action);

        // Sin Navigation Component puedes hacer:
        BookingSummaryFragment bookingSummaryFragment = new BookingSummaryFragment();
        bookingSummaryFragment.setArguments(args);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, bookingSummaryFragment)
                .addToBackStack(null)
                .commit();

        Toast.makeText(getContext(), "Continuando con la reserva...", Toast.LENGTH_SHORT).show();
    }

    // Interface para comunicarse con el adapter
    public interface OnRoomSelectedListener {
        void onRoomSelected(int position);
    }
}