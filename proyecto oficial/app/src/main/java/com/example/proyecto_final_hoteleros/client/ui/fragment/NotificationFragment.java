package com.example.proyecto_final_hoteleros.client.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.ui.adapters.NotificationAdapter;
import com.example.proyecto_final_hoteleros.client.data.model.Notification;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NotificationFragment extends Fragment implements NotificationAdapter.OnNotificationListener {
    private static final String TAG = "NotificationFragment";

    private RecyclerView rvNotifications;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyState;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private ImageButton btnMarkAllRead;
    private FloatingActionButton fabCreateNotification;

    // Firebase Realtime Database
    private DatabaseReference databaseRef;
    private DatabaseReference notificationsRef;
    private FirebaseAuth auth;
    private String currentUserId;
    private ChildEventListener notificationsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.client_notification_fragment, container, false);

        // Inicializar Firebase Realtime Database
        databaseRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "user_demo";
        notificationsRef = databaseRef.child("notifications").child(currentUserId);

        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupButtons();
        loadNotifications();

        return view;
    }

    private void initViews(View view) {
        rvNotifications = view.findViewById(R.id.rv_notifications);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        emptyState = view.findViewById(R.id.empty_state);
        btnMarkAllRead = view.findViewById(R.id.btn_mark_all_read);
        fabCreateNotification = view.findViewById(R.id.fab_create_notification);

        // Botón de regreso
        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupRecyclerView() {
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(getContext(), notificationList);
        adapter.setOnNotificationListener(this);

        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setHasFixedSize(true);
        rvNotifications.setAdapter(adapter);
        rvNotifications.setItemAnimator(new DefaultItemAnimator());
        rvNotifications.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.orange_primary);
        swipeRefresh.setOnRefreshListener(this::loadNotifications);
    }

    private void setupButtons() {
        // Botón para marcar todas como leídas
        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());

        // Botón flotante para crear notificaciones de prueba
        fabCreateNotification.setOnClickListener(v -> showCreateNotificationDialog());
    }

    /**
     * Cargar notificaciones desde Firebase Realtime Database
     */
    private void loadNotifications() {
        swipeRefresh.setRefreshing(true);

        Log.d(TAG, "Cargando notificaciones para usuario: " + currentUserId);

        // Remover listener anterior si existe
        if (notificationsListener != null) {
            notificationsRef.removeEventListener(notificationsListener);
        }

        // Crear nuevo listener
        notificationsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    Notification notification = parseNotification(snapshot);
                    if (notification != null) {
                        notificationList.add(0, notification); // Agregar al inicio
                        sortNotificationsByTimestamp();
                        adapter.notifyDataSetChanged();
                        updateUI();
                        Log.d(TAG, "Notificación agregada: " + notification.getTitle());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error agregando notificación: " + e.getMessage());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    Notification updatedNotification = parseNotification(snapshot);
                    if (updatedNotification != null) {
                        // Buscar y actualizar la notificación existente
                        for (int i = 0; i < notificationList.size(); i++) {
                            if (notificationList.get(i).getId().equals(updatedNotification.getId())) {
                                notificationList.set(i, updatedNotification);
                                adapter.notifyItemChanged(i);
                                updateMarkAllReadButton();
                                Log.d(TAG, "Notificación actualizada: " + updatedNotification.getTitle());
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error actualizando notificación: " + e.getMessage());
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                try {
                    String removedId = snapshot.getKey();
                    for (int i = 0; i < notificationList.size(); i++) {
                        if (notificationList.get(i).getId().equals(removedId)) {
                            notificationList.remove(i);
                            adapter.notifyItemRemoved(i);
                            updateUI();
                            Log.d(TAG, "Notificación eliminada: " + removedId);
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error eliminando notificación: " + e.getMessage());
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // No necesario para este caso
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar notificaciones: " + error.getMessage());
                swipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(), "Error al cargar notificaciones", Toast.LENGTH_SHORT).show();
            }
        };

        // Inicializar carga completa
        notificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        Notification notification = parseNotification(child);
                        if (notification != null) {
                            notificationList.add(notification);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parseando notificación: " + e.getMessage());
                    }
                }
                sortNotificationsByTimestamp();
                adapter.notifyDataSetChanged();
                updateUI();
                swipeRefresh.setRefreshing(false);

                // Ahora agregar el listener para cambios en tiempo real
                notificationsRef.addChildEventListener(notificationsListener);

                Log.d(TAG, "Notificaciones cargadas: " + notificationList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error inicial: " + error.getMessage());
                swipeRefresh.setRefreshing(false);
                showEmptyState();
            }
        });
    }

    /**
     * Parsear notificación desde DataSnapshot
     */
    private Notification parseNotification(DataSnapshot snapshot) {
        try {
            String id = snapshot.getKey();
            String title = snapshot.child("title").getValue(String.class);
            String message = snapshot.child("message").getValue(String.class);
            Integer type = snapshot.child("type").getValue(Integer.class);
            Long timestamp = snapshot.child("timestamp").getValue(Long.class);
            Boolean read = snapshot.child("read").getValue(Boolean.class);
            String actionData = snapshot.child("actionData").getValue(String.class);

            if (id != null && title != null && message != null && type != null && timestamp != null) {
                Date date = new Date(timestamp);
                boolean isRead = read != null ? read : false;
                return new Notification(id, title, message, type, date, isRead, actionData);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parseando notificación: " + e.getMessage());
        }
        return null;
    }

    /**
     * Ordenar notificaciones por timestamp (más reciente primero)
     */
    private void sortNotificationsByTimestamp() {
        Collections.sort(notificationList, (n1, n2) ->
                Long.compare(n2.getTimestamp().getTime(), n1.getTimestamp().getTime()));
    }

    /**
     * Marcar una notificación como leída
     */
    private void markAsRead(Notification notification) {
        if (notification.isRead()) return;

        notificationsRef.child(notification.getId()).child("read").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notificación marcada como leída: " + notification.getId());
                    Toast.makeText(getContext(), "Marcada como leída", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error marcando como leída: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al marcar como leída", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Marcar todas las notificaciones como leídas
     */
    private void markAllAsRead() {
        Map<String, Object> updates = new HashMap<>();
        boolean hasUnread = false;

        for (Notification notification : notificationList) {
            if (!notification.isRead()) {
                updates.put(notification.getId() + "/read", true);
                hasUnread = true;
            }
        }

        if (!hasUnread) {
            Toast.makeText(getContext(), "No hay notificaciones por leer", Toast.LENGTH_SHORT).show();
            return;
        }

        notificationsRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Todas marcadas como leídas", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Todas las notificaciones marcadas como leídas");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error marcando todas como leídas: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al marcar todas como leídas", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Mostrar diálogo para crear notificación de prueba
     */
    private void showCreateNotificationDialog() {
        String[] notificationTypes = {
                "Reserva Confirmada",
                "Check-in Disponible",
                "Check-out Completado",
                "Oferta Especial",
                "Recordatorio"
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Crear Notificación de Prueba")
                .setItems(notificationTypes, (dialog, which) -> {
                    createTestNotification(which + 1);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Crear notificación de prueba
     */
    private void createTestNotification(int type) {
        String notificationId = UUID.randomUUID().toString();
        String title, message, actionData;

        switch (type) {
            case Notification.TYPE_BOOKING:
                title = "Reserva confirmada";
                message = "Tu reserva para Hotel Miraflores ha sido confirmada exitosamente. Te esperamos el 15 de Mayo.";
                actionData = "booking" + System.currentTimeMillis();
                break;
            case Notification.TYPE_CHECK_IN:
                title = "Check-in disponible";
                message = "Ya puedes realizar tu check-in para tu estadía en Hotel Lima Centro. Te esperamos mañana.";
                actionData = "booking" + System.currentTimeMillis();
                break;
            case Notification.TYPE_CHECK_OUT:
                title = "Check-out completado";
                message = "Tu check-out del Hotel San Isidro ha sido procesado. ¡Gracias por tu estadía!";
                actionData = "booking" + System.currentTimeMillis();
                break;
            case Notification.TYPE_PROMO:
                title = "Oferta especial";
                message = "¡Obtén un 20% de descuento en tu próxima reserva! Usa el código HOTEL20 al reservar.";
                actionData = "promo" + System.currentTimeMillis();
                break;
            case Notification.TYPE_REMINDER:
                title = "Recordatorio de reserva";
                message = "Te recordamos que tienes una reserva en Hotel Barranco para mañana. ¡Te esperamos!";
                actionData = "booking" + System.currentTimeMillis();
                break;
            default:
                title = "Notificación de prueba";
                message = "Esta es una notificación de prueba del sistema.";
                actionData = "test";
        }

        // Crear el objeto de notificación
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("id", notificationId);
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("type", type);
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("read", false);
        notificationData.put("actionData", actionData);

        // Guardar en Firebase Realtime Database
        notificationsRef.child(notificationId).setValue(notificationData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Notificación creada: " + title, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Notificación creada exitosamente: " + notificationId);

                    // Enviar notificación push local
                    sendLocalPushNotification(title, message, type);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creando notificación: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al crear notificación", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Enviar notificación push local
     */
    private void sendLocalPushNotification(String title, String message, int type) {
        try {
            com.example.proyecto_final_hoteleros.client.data.service.NotificationService notificationService =
                    new com.example.proyecto_final_hoteleros.client.data.service.NotificationService(requireContext());
            notificationService.showNotification(title, message, type, "local_test");
        } catch (Exception e) {
            Log.e(TAG, "Error enviando notificación push: " + e.getMessage());
        }
    }

    /**
     * Actualizar UI según el estado de las notificaciones
     */
    private void updateUI() {
        if (notificationList.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            updateMarkAllReadButton();
        }
    }

    /**
     * Actualizar estado del botón de marcar todas como leídas
     */
    private void updateMarkAllReadButton() {
        if (btnMarkAllRead == null) return;

        boolean hasUnread = false;
        for (Notification notification : notificationList) {
            if (!notification.isRead()) {
                hasUnread = true;
                break;
            }
        }

        btnMarkAllRead.setEnabled(hasUnread);
        btnMarkAllRead.setAlpha(hasUnread ? 1.0f : 0.5f);
    }

    /**
     * Mostrar estado vacío
     */
    private void showEmptyState() {
        if (emptyState != null) {
            emptyState.setVisibility(View.VISIBLE);
        }
        if (rvNotifications != null) {
            rvNotifications.setVisibility(View.GONE);
        }
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setVisibility(View.GONE);
        }
    }

    /**
     * Ocultar estado vacío
     */
    private void hideEmptyState() {
        if (emptyState != null) {
            emptyState.setVisibility(View.GONE);
        }
        if (rvNotifications != null) {
            rvNotifications.setVisibility(View.VISIBLE);
        }
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setVisibility(View.VISIBLE);
        }
    }

    // Implementación de OnNotificationListener
    @Override
    public void onNotificationClick(Notification notification) {
        markAsRead(notification);

        switch (notification.getType()) {
            case Notification.TYPE_BOOKING:
                navigateToBookingDetails(notification.getActionData());
                break;
            case Notification.TYPE_CHECK_IN:
                navigateToCheckInDetails(notification.getActionData());
                break;
            case Notification.TYPE_CHECK_OUT:
                Toast.makeText(getContext(), "Check-out completado", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(getContext(), "Notificación: " + notification.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewDetailsClick(Notification notification) {
        markAsRead(notification);
        Toast.makeText(getContext(), "Ver detalles: " + notification.getActionData(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkAsReadClick(Notification notification) {
        markAsRead(notification);
    }

    private void navigateToBookingDetails(String bookingId) {
        Toast.makeText(getContext(), "Ver detalles de reserva: " + bookingId, Toast.LENGTH_SHORT).show();
    }

    private void navigateToCheckInDetails(String bookingId) {
        Toast.makeText(getContext(), "Ver detalles de check-in: " + bookingId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar listener
        if (notificationsListener != null && notificationsRef != null) {
            notificationsRef.removeEventListener(notificationsListener);
        }
    }
}