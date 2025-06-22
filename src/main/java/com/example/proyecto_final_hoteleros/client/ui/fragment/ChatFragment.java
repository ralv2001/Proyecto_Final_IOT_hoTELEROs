package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.adapters.ChatListAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.ChatSummary;
import com.example.proyecto_final_hoteleros.client.data.service.FirebaseChatService;
import com.example.proyecto_final_hoteleros.client.navigation.NavigationManager;
import com.example.proyecto_final_hoteleros.client.utils.UserDataManager;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends BaseBottomNavigationFragment implements ChatListAdapter.OnChatClickListener {
    private static final String TAG = "ChatFragment";

    // Variables para la lista de chats
    private RecyclerView rvChatList;
    private View emptyStateContainer;
    private ChatListAdapter chatListAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Servicio Firebase
    private FirebaseChatService chatService;

    @Override
    protected NavigationTab getCurrentTab() {
        return NavigationTab.CHAT;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.client_fragment_chat, container, false);

        try {
            Log.d(TAG, "Creating ChatFragment view");

            // Inicializar servicio Firebase
            chatService = FirebaseChatService.getInstance();

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

            swipeRefreshLayout.setOnRefreshListener(this::loadChatsFromFirebase);
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
                    chatListAdapter.setChatList(chatSummaries);
                    updateEmptyState(chatSummaries.isEmpty());

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
                    loadDemoChats();

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

            if (chatListAdapter != null) {
                Log.d(TAG, "Setting " + demoChats.size() + " demo chats to adapter");
                chatListAdapter.setChatList(demoChats);
            }

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

            // ✅ CORRECTO - Usa NavigationManager con animación BOTTOM_TO_TOP automática
            NavigationManager.getInstance().navigateToChatConversation(
                    chat.getId(),
                    chat.getHotelName(),
                    chat.getHotelId(),
                    chat.getStatus().name(),
                    UserDataManager.getInstance().getUserBundle()
            );

        } catch (Exception e) {
            Log.e(TAG, "Error en onChatClick: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatService != null) {
            chatService.cleanup();
        }
    }
}