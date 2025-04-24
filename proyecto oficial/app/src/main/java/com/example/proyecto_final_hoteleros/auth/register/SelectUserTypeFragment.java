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

import com.example.proyecto_final_hoteleros.AuthActivity;
import com.example.proyecto_final_hoteleros.R;

public class SelectUserTypeFragment extends Fragment {

    private SelectUserTypeViewModel mViewModel;

    public static SelectUserTypeFragment newInstance() {
        return new SelectUserTypeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sistema_fragment_select_type_user, container, false);

        Button registerAsClientButton = view.findViewById(R.id.btnRegistrarCliente);
        registerAsClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AuthActivity) getActivity()).gotoMainRegister("client");
            }});
        Button registerAsDriverButton = view.findViewById(R.id.btnRegistrarTaxista);
        registerAsDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AuthActivity) getActivity()).gotoMainRegister("driver");
            }});

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SelectUserTypeViewModel.class);
        // TODO: Use the ViewModel
    }
}