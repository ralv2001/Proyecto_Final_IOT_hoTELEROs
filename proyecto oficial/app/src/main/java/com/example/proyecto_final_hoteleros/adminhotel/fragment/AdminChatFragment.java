package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.AdminChatListAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.ChatSummary;
import com.example.proyecto_final_hoteleros.client.data.service.FirebaseChatService;

import java.util.List;

public class AdminChatFragment extends Fragment implements AdminChatListAdapter.OnChatClickListener {
    private static final String TAG = "AdminChatFragment";

    private RecyclerView rvChatList;
    private View emptyStateContainer;
    private AdminChatListAdapter chatListAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseChatService chatService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_chat_admin, container, false);

        try {
            Log.d(TAG, "Creating AdminChatFragment view");

            chatService = FirebaseChatService.getInstance();
            setupChatList(rootView);
            setupSwipeRefresh(rootView);
            loadChatsFromFirebase();

        } catch (Exception e) {
            Log.e(TAG, "Error en onCreateView: " + e.getMessage());
            e.printStackTrace();
        }

        return rootView;
    }

    private void setupChatList(View rootView) {
        try {
            rvChatList = rootView.findViewById(R.id.rvChatList);
            emptyStateContainer = rootView.findViewById(R.id.emptyStateContainer);

            if (rvChatList == null) {
                Log.e(TAG, "rvChatList es null");
                return;
            }

            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            rvChatList.setLayoutManager(layoutManager);
            chatListAdapter = new AdminChatListAdapter(getContext(), this);
            rvChatList.setAdapter(chatListAdapter);

            Log.d(TAG, "Admin chat list setup complete");

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

            swipeRefreshLayout.setOnRefreshListener(() -> loadChatsFromFirebase());
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
                    Log.d(TAG, "Chats cargados para admin: " + chatSummaries.size());
                }
            }

            @Override
            public void onChatSummariesError(String error) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Error al cargar chats: " + error, Toast.LENGTH_SHORT).show();

                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    Log.e(TAG, "Error al cargar chats: " + error);
                }
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (emptyStateContainer != null && rvChatList != null) {
            if (isEmpty) {
                rvChatList.setVisibility(View.GONE);
                emptyStateContainer.setVisibility(View.VISIBLE);
                Log.d(TAG, "Showing empty state for admin");
            } else {
                rvChatList.setVisibility(View.VISIBLE);
                emptyStateContainer.setVisibility(View.GONE);
                Log.d(TAG, "Showing admin chat list");
            }
        }
    }

    @Override
    public void onChatClick(ChatSummary chat) {
        try {
            Log.d(TAG, "Admin iniciando chat con reserva: " + chat.getReservationId());

            if (!isAdded() || getActivity() == null) {
                Log.e(TAG, "Fragmento no adjunto o actividad nula");
                return;
            }

            navigateToAdminChatConversation(chat);

        } catch (Exception e) {
            Log.e(TAG, "Error en onChatClick: " + e.getMessage());
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error al abrir chat: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToAdminChatConversation(ChatSummary chat) {
        try {
            if (!isAdded() || getActivity() == null) {
                Log.e(TAG, "Fragment not attached or activity is null");
                return;
            }

            if (chat.getId() == null || chat.getId().isEmpty()) {
                Log.e(TAG, "Chat ID is null or empty");
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error: ID de chat inválido", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            View fragmentContainer = getActivity().findViewById(R.id.fragment_container);
            if (fragmentContainer == null) {
                Log.e(TAG, "El contenedor de fragmentos no existe");
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error en la navegación: contenedor no encontrado", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            AdminChatConversationFragment conversationFragment = new AdminChatConversationFragment();
            Bundle args = new Bundle();
            args.putString("chat_id", chat.getId());
            args.putString("hotel_name", chat.getHotelName());
            args.putString("hotel_id", chat.getHotelId());
            args.putString("client_reservation", chat.getReservationId());
            args.putString("chat_status", chat.getStatus().name());
            conversationFragment.setArguments(args);

            Log.d(TAG, "Prepared AdminChatConversationFragment with args: chat_id=" + chat.getId()
                    + ", status=" + chat.getStatus().name());

            try {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

                transaction.setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                );

                transaction.replace(R.id.fragment_container, conversationFragment)
                        .addToBackStack(null)
                        .commit();

                Log.d(TAG, "Admin navigation transaction committed successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to perform fragment transaction: " + e.getMessage());
                e.printStackTrace();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error en navegación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error general en navigateToAdminChatConversation: " + e.getMessage());
            e.printStackTrace();
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), "Error al navegar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
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