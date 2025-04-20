package com.example.proyecto_final_hoteleros.client.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adapters.ChatListAdapter;
import com.example.proyecto_final_hoteleros.client.model.ChatSummary;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment implements ChatListAdapter.OnChatClickListener {
    // Variables para el navegador inferior
    private LinearLayout navHome, navExplore, navChat, navProfile;
    private ImageView ivHome, ivExplore, ivChat, ivProfile;

    // Variables para la lista de chats
    private RecyclerView rvChatList;
    private View emptyStateContainer;
    private ChatListAdapter chatListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        // Configuración del navegador inferior
        setupBottomNavigation(rootView);

        // Configuración de la lista de chats
        setupChatList(rootView);

        // Cargar chats de ejemplo (en una app real, los cargarías de una base de datos)
        loadDemoChats();

        return rootView;
    }

    private void setupChatList(View rootView) {
        // Inicializar vistas
        rvChatList = rootView.findViewById(R.id.rvChatList);
        emptyStateContainer = rootView.findViewById(R.id.emptyStateContainer);

        // Configurar RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvChatList.setLayoutManager(layoutManager);

        // Inicializar adaptador
        chatListAdapter = new ChatListAdapter(getContext(), this);
        rvChatList.setAdapter(chatListAdapter);
    }

    private void loadDemoChats() {
        // Crear lista de chats de ejemplo
        List<ChatSummary> demoChats = new ArrayList<>();

        // Chat 1: Disponible para iniciar
        ChatSummary chat1 = new ChatSummary(
                "chat_1",
                "hotel_1",
                "Hotel Las Palmeras",
                "12345",
                "19-24 Abril 2025",
                ChatSummary.ChatStatus.AVAILABLE
        );
        demoChats.add(chat1);

        // Chat 2: Activo
        ChatSummary chat2 = new ChatSummary(
                "chat_2",
                "hotel_2",
                "Grand Hotel Central",
                "67890",
                "15-18 Abril 2025",
                ChatSummary.ChatStatus.ACTIVE
        );
        chat2.setLastMessage("¿Podría solicitar servicio de habitaciones?");
        demoChats.add(chat2);

        // Chat 3: Finalizado
        ChatSummary chat3 = new ChatSummary(
                "chat_3",
                "hotel_3",
                "Sunset Resort & Spa",
                "54321",
                "1-10 Abril 2025",
                ChatSummary.ChatStatus.FINISHED
        );
        chat3.setLastMessage("Gracias por su estancia, esperamos verle pronto.");
        demoChats.add(chat3);

        // Actualizar adaptador
        chatListAdapter.setChatList(demoChats);

        // Mostrar estado vacío si no hay chats
        if (demoChats.isEmpty()) {
            rvChatList.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            rvChatList.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onChatClick(ChatSummary chat) {
        // Navegar al fragmento de conversación de chat
        navigateToChatConversation(chat);
    }

    private void navigateToChatConversation(ChatSummary chat) {
        // Crear fragmento de conversación y pasar datos
        ChatConversationFragment chatConversationFragment = new ChatConversationFragment();
        Bundle args = new Bundle();
        args.putString("chat_id", chat.getId());
        args.putString("hotel_name", chat.getHotelName());
        args.putString("hotel_id", chat.getHotelId());
        chatConversationFragment.setArguments(args);

        // Realizar la transacción del fragmento
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, chatConversationFragment)
                .addToBackStack(null)
                .commit();
    }

    // Método para configurar el navegador inferior
    private void setupBottomNavigation(View rootView) {
        // Referencias a los elementos del navegador
        navHome = rootView.findViewById(R.id.nav_home);
        navExplore = rootView.findViewById(R.id.nav_explore);
        navChat = rootView.findViewById(R.id.nav_chat);
        navProfile = rootView.findViewById(R.id.nav_profile);
        ivHome = rootView.findViewById(R.id.iv_home);
        ivExplore = rootView.findViewById(R.id.iv_explore);
        ivChat = rootView.findViewById(R.id.iv_chat);
        ivProfile = rootView.findViewById(R.id.iv_profile);

        // Marcar Chat como seleccionado
        setSelectedNavItem(navChat, ivChat);

        // Establecer listeners para cada elemento del navegador
        navHome.setOnClickListener(v -> {
            if (!isCurrentFragment(HomeFragment.class)) {
                setSelectedNavItem(navHome, ivHome);
                navigateToFragment(new HomeFragment(), false);
            }
        });

        navExplore.setOnClickListener(v -> {
            if (!isCurrentFragment(HistorialFragment.class)) {
                setSelectedNavItem(navExplore, ivExplore);
                navigateToFragment(new HistorialFragment(), true);
            }
        });

        navChat.setOnClickListener(v -> {
            if (!isCurrentFragment(ChatFragment.class)) {
                setSelectedNavItem(navChat, ivChat);
                // Ya estamos en Chat, no necesitamos cambiar
            }
        });

        navProfile.setOnClickListener(v -> {
            if (!isCurrentFragment(ProfileFragment.class)) {
                setSelectedNavItem(navProfile, ivProfile);
                navigateToFragment(new ProfileFragment(), true);
            }
        });
    }

    // Método para navegar a un fragmento con animación
    private void navigateToFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction =
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left,
                                R.anim.slide_in_left,
                                R.anim.slide_out_right
                        )
                        .replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    // Método para verificar si el fragmento actual es del tipo especificado
    private boolean isCurrentFragment(Class<? extends Fragment> fragmentClass) {
        Fragment currentFragment =
                requireActivity().getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
        return currentFragment != null &&
                fragmentClass.isInstance(currentFragment);
    }

    // Método para resaltar el ítem seleccionado
    private void setSelectedNavItem(LinearLayout navItem, ImageView icon) {
        // Resetear todos los íconos a color blanco
        ivHome.setColorFilter(ContextCompat.getColor(requireContext(),
                android.R.color.white));
        ivExplore.setColorFilter(ContextCompat.getColor(requireContext(),
                android.R.color.white));
        ivChat.setColorFilter(ContextCompat.getColor(requireContext(),
                android.R.color.white));
        ivProfile.setColorFilter(ContextCompat.getColor(requireContext(),
                android.R.color.white));
        // Establecer el ícono seleccionado a color naranja
        icon.setColorFilter(ContextCompat.getColor(requireContext(),
                R.color.orange));
    }
}