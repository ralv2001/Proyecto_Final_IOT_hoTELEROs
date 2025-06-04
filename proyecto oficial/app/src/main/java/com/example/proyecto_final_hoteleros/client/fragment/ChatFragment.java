package com.example.proyecto_final_hoteleros.client.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.adapters.ChatListAdapter;
import com.example.proyecto_final_hoteleros.client.model.ChatSummary;
import com.example.proyecto_final_hoteleros.client.service.FirebaseChatService;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment implements ChatListAdapter.OnChatClickListener {
    private static final String TAG = "ChatFragment";

    // Variables para el navegador inferior
    private LinearLayout navHome, navExplore, navChat, navProfile;
    private ImageView ivHome, ivExplore, ivChat, ivProfile;

    // Variables para la lista de chats
    private RecyclerView rvChatList;
    private View emptyStateContainer;
    private ChatListAdapter chatListAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Servicio Firebase
    private FirebaseChatService chatService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        try {
            Log.d(TAG, "Creating ChatFragment view");

            // Inicializar servicio Firebase
            chatService = FirebaseChatService.getInstance();

            // Configuración del navegador inferior
            setupBottomNavigation(rootView);

            // Configuración de la lista de chats
            setupChatList(rootView);

            // Configurar swipe to refresh
            setupSwipeRefresh(rootView);

            // Cargar chats desde Firebase
            loadChatsFromFirebase();

        } catch (Exception e) {
            Log.e(TAG, "Error en onCreateView: " + e.getMessage());
            e.printStackTrace();
        }

        return rootView;
    }

    private void setupChatList(View rootView) {
        try {
            // Inicializar vistas
            rvChatList = rootView.findViewById(R.id.rvChatList);
            emptyStateContainer = rootView.findViewById(R.id.emptyStateContainer);

            // Verificar que no sean null
            if (rvChatList == null) {
                Log.e(TAG, "rvChatList es null");
                return;
            }

            // Configurar RecyclerView
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            rvChatList.setLayoutManager(layoutManager);

            // Inicializar adaptador con la implementación de la interfaz de clic
            chatListAdapter = new ChatListAdapter(getContext(), this);
            rvChatList.setAdapter(chatListAdapter);

            Log.d(TAG, "Chat list setup complete");

        } catch (Exception e) {
            Log.e(TAG, "Error en setupChatList: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSwipeRefresh(View rootView) {
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(
                    ContextCompat.getColor(getContext(), R.color.orange),
                    ContextCompat.getColor(getContext(), R.color.blue),
                    ContextCompat.getColor(getContext(), R.color.green)
            );

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // Recargar chats desde Firebase
                    loadChatsFromFirebase();
                }
            });
        }
    }

    private void loadChatsFromFirebase() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        chatService.loadChatSummaries(new FirebaseChatService.OnChatSummariesLoadedListener() {
            @Override
            public void onChatSummariesLoaded(List<ChatSummary> chatSummaries) {
                if (isAdded() && getContext() != null) {
                    // Actualizar adaptador con los chats cargados
                    chatListAdapter.setChatList(chatSummaries);

                    // Mostrar estado vacío si no hay chats
                    updateEmptyState(chatSummaries.isEmpty());

                    // Detener indicador de refresco
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    Log.d(TAG, "Chats cargados desde Firebase: " + chatSummaries.size());
                }
            }

            @Override
            public void onChatSummariesError(String error) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Error al cargar chats: " + error, Toast.LENGTH_SHORT).show();

                    // Usar datos de ejemplo si hay un error en la carga
                    loadDemoChats();

                    // Detener indicador de refresco
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    Log.e(TAG, "Error al cargar chats: " + error);
                }
            }
        });
    }

    private void loadDemoChats() {
        try {
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
            chat1.setHotelImageUrl("https://example.com/hotel1.jpg");
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
            chat2.setHotelImageUrl("https://example.com/hotel2.jpg");
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
            chat3.setHotelImageUrl("https://example.com/hotel3.jpg");
            demoChats.add(chat3);

            // Actualizar adaptador
            if (chatListAdapter != null) {
                Log.d(TAG, "Setting " + demoChats.size() + " demo chats to adapter");
                chatListAdapter.setChatList(demoChats);
            }

            // Mostrar estado vacío si no hay chats
            updateEmptyState(demoChats.isEmpty());

        } catch (Exception e) {
            Log.e(TAG, "Error en loadDemoChats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        if (emptyStateContainer != null && rvChatList != null) {
            if (isEmpty) {
                rvChatList.setVisibility(View.GONE);
                emptyStateContainer.setVisibility(View.VISIBLE);
                Log.d(TAG, "Showing empty state");
            } else {
                rvChatList.setVisibility(View.VISIBLE);
                emptyStateContainer.setVisibility(View.GONE);
                Log.d(TAG, "Showing chat list");
            }
        }
    }

    @Override
    public void onChatClick(ChatSummary chat) {
        try {
            Log.d(TAG, "Iniciando click en chat: " + chat.getHotelName());

            // Validaciones
            if (!isAdded() || getActivity() == null) {
                Log.e(TAG, "Fragmento no adjunto o actividad nula");
                return;
            }

            // Navegar con manejo de excepciones mejorado
            navigateToChatConversation(chat);

        } catch (Exception e) {
            Log.e(TAG, "Error en onChatClick: " + e.getMessage());
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error al abrir chat: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToChatConversation(ChatSummary chat) {
        try {
            // Verificar que el contexto y la actividad estén disponibles
            if (!isAdded() || getActivity() == null) {
                Log.e(TAG, "Fragment not attached or activity is null");
                return;
            }

            // Verify chat ID
            if (chat.getId() == null || chat.getId().isEmpty()) {
                Log.e(TAG, "Chat ID is null or empty");
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error: ID de chat inválido", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // Use safer fragment container lookup
            View fragmentContainer = getActivity().findViewById(R.id.fragment_container);
            if (fragmentContainer == null) {
                Log.e(TAG, "El contenedor de fragmentos no existe");
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error en la navegación: contenedor no encontrado", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // Crear fragmento de conversación y pasar datos
            ChatConversationFragment chatConversationFragment = new ChatConversationFragment();
            Bundle args = new Bundle();
            args.putString("chat_id", chat.getId());
            args.putString("hotel_name", chat.getHotelName());
            args.putString("hotel_id", chat.getHotelId());
            args.putString("chat_status", chat.getStatus().name());
            chatConversationFragment.setArguments(args);

            Log.d(TAG, "Prepared ChatConversationFragment with args: chat_id=" + chat.getId()
                    + ", status=" + chat.getStatus().name());

            // Perform the fragment transaction with try-catch
            try {
                // Start with a clean transaction
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

                // Apply animations
                transaction.setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                );

                // Replace fragment and commit
                transaction.replace(R.id.fragment_container, chatConversationFragment)
                        .addToBackStack(null)
                        .commit();

                Log.d(TAG, "Navigation transaction committed successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to perform fragment transaction: " + e.getMessage());
                e.printStackTrace();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error en navegación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error general en navigateToChatConversation: " + e.getMessage());
            e.printStackTrace();
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), "Error al navegar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Limpiar listeners de Firebase
        if (chatService != null) {
            chatService.cleanup();
        }
    }



    // Método para configurar el navegador inferior
    private void setupBottomNavigation(View rootView) {
        try {
            // Referencias a los elementos del navegador
            navHome = rootView.findViewById(R.id.nav_home);
            navExplore = rootView.findViewById(R.id.nav_explore);
            navChat = rootView.findViewById(R.id.nav_chat);
            navProfile = rootView.findViewById(R.id.nav_profile);
            ivHome = rootView.findViewById(R.id.iv_home);
            ivExplore = rootView.findViewById(R.id.iv_explore);
            ivChat = rootView.findViewById(R.id.iv_chat);
            ivProfile = rootView.findViewById(R.id.iv_profile);

            // Verificar que no sean null antes de continuar
            if (navChat == null || ivChat == null) {
                Log.e(TAG, "Elementos de navegación no encontrados");
                return;
            }

            // Marcar Chat como seleccionado
            setSelectedNavItem(navChat, ivChat);

            // Configurar listeners de manera segura
            if (navHome != null) {
                navHome.setOnClickListener(v -> {
                    Log.d(TAG, "Clic en Home");
                    try {
                        if (!isCurrentFragment(HomeFragment.class)) {
                            setSelectedNavItem(navHome, ivHome);
                            // Si HomeFragment no está incluido en el código, manejar de manera segura
                            try {
                                navigateToFragment(new HomeFragment(), false);
                            } catch (Exception e) {
                                Log.e(TAG, "Error al crear HomeFragment: " + e.getMessage());
                                Toast.makeText(getContext(), "HomeFragment no disponible", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error en navHome click: " + e.getMessage());
                    }
                });
            }

            if (navExplore != null) {
                navExplore.setOnClickListener(v -> {
                    Log.d(TAG, "Clic en Explore");
                    try {
                        if (!isCurrentFragment(HistorialFragment.class)) {
                            setSelectedNavItem(navExplore, ivExplore);
                            try {
                                navigateToFragment(new HistorialFragment(), true);
                            } catch (Exception e) {
                                Log.e(TAG, "Error al crear HistorialFragment: " + e.getMessage());
                                Toast.makeText(getContext(), "HistorialFragment no disponible", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error en navExplore click: " + e.getMessage());
                    }
                });
            }

            if (navChat != null) {
                navChat.setOnClickListener(v -> {
                    Log.d(TAG, "Clic en Chat");
                    setSelectedNavItem(navChat, ivChat);
                    // Ya estamos en Chat, no necesitamos cambiar
                });
            }

            if (navProfile != null) {
                navProfile.setOnClickListener(v -> {
                    Log.d(TAG, "Clic en Profile");
                    try {
                        if (!isCurrentFragment(ProfileFragment.class)) {
                            setSelectedNavItem(navProfile, ivProfile);
                            try {
                                navigateToFragment(new ProfileFragment(), true);
                            } catch (Exception e) {
                                Log.e(TAG, "Error al crear ProfileFragment: " + e.getMessage());
                                Toast.makeText(getContext(), "ProfileFragment no disponible", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error en navProfile click: " + e.getMessage());
                    }
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error en setupBottomNavigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para navegar a un fragmento con animación
    private void navigateToFragment(Fragment fragment, boolean addToBackStack) {
        if (getActivity() == null) {
            Log.e(TAG, "getActivity() es null");
            return;
        }

        try {
            // Verificar que el contenedor de fragmentos existe
            if (getActivity().findViewById(R.id.fragment_container) == null) {
                Log.e(TAG, "El contenedor de fragmentos no existe");
                return;
            }

            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

            // Intentar aplicar animaciones
            try {
                transaction.setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                );
            } catch (Exception e) {
                Log.e(TAG, "Error al aplicar animaciones: " + e.getMessage());
                // Continuar sin animaciones
            }

            transaction.replace(R.id.fragment_container, fragment);
            if (addToBackStack) {
                transaction.addToBackStack(null);
            }
            transaction.commit();

        } catch (Exception e) {
            Log.e(TAG, "Error en navigateToFragment: " + e.getMessage());
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error al navegar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para verificar si el fragmento actual es del tipo especificado
    private boolean isCurrentFragment(Class<? extends Fragment> fragmentClass) {
        try {
            if (getActivity() == null) {
                return false;
            }

            Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            return currentFragment != null && fragmentClass.isInstance(currentFragment);

        } catch (Exception e) {
            Log.e(TAG, "Error en isCurrentFragment: " + e.getMessage());
            return false;
        }
    }

    // Método para resaltar el ítem seleccionado
    private void setSelectedNavItem(LinearLayout navItem, ImageView icon) {
        try {
            if (getContext() == null) {
                return;
            }

            // Reset all icons to white
            if (ivHome != null) ivHome.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.white));
            if (ivExplore != null) ivExplore.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.white));
            if (ivChat != null) ivChat.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.white));
            if (ivProfile != null) ivProfile.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.white));

            // Set selected icon to orange
            if (icon != null) {
                icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.orange));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error en setSelectedNavItem: " + e.getMessage());
            e.printStackTrace();
        }
    }
}