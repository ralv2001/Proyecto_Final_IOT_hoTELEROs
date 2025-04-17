package com.example.proyecto_final_hoteleros.auth.register;

import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.proyecto_final_hoteleros.R;

public class RegisterFragment extends Fragment {

    private RegisterViewModel mViewModel;

    public static RegisterFragment newInstance(String userType) {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        args.putString("userType", userType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Solo configurar botones básicos
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        //ImageView btnBack = view.findViewById(R.id.btnBack);
        Button btnContinuar = view.findViewById(R.id.btnContinuar);

        btnBack.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Botón volver presionado", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        btnContinuar.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Botón continuar presionado", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
    }
}