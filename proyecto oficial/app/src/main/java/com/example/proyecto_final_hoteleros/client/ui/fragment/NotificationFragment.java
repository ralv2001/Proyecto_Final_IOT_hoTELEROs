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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationFragment extends Fragment implements NotificationAdapter.OnNotificationListener {
    private RecyclerView rvNotifications;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyState;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private ImageButton btnMarkAllRead;

    // Referencias a Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.client_notification_fragment, container, false);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

        // Inicializar vistas
        rvNotifications = view.findViewById(R.id.rv_notifications);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        emptyState = view.findViewById(R.id.empty_state);
        btnMarkAllRead = view.findViewById(R.id.btn_mark_all_read);

        // Configurar RecyclerView con animaciones
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(getContext(), notificationList);
        adapter.setOnNotificationListener(this);

        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setHasFixedSize(true);
        rvNotifications.setAdapter(adapter);

        // Añadir animación al RecyclerView
        rvNotifications.setItemAnimator(new DefaultItemAnimator());
        rvNotifications.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        // Configurar SwipeRefreshLayout
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(this::loadNotifications);

        // Botón de regreso
        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Botón para marcar todas como leídas
        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());

        // Cargar notificaciones
        loadNotifications();

        // Para desarrollo, usar notificaciones de ejemplo
        // En producción, usa solo loadNotifications()
        addSampleNotifications();

        return view;
    }

    /**
     * Carga las notificaciones del usuario actual desde Firestore
     */
    private void loadNotifications() {
        swipeRefresh.setRefreshing(true);

        // Logs para depuración
        Log.d("NotificationFragment", "Iniciando carga de notificaciones");

        // Si no hay usuario autenticado, no podemos cargar notificaciones
        if (currentUserId.isEmpty()) {
            Log.d("NotificationFragment", "No hay usuario autenticado");
            showEmptyState();
            swipeRefresh.setRefreshing(false);
            return;
        }

        Log.d("NotificationFragment", "Consultando Firestore para userId: " + currentUserId);

        // Ruta de Firestore: users/{userId}/notifications
        db.collection("users")
                .document(currentUserId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Más recientes primero
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("NotificationFragment", "Obtenidas " + queryDocumentSnapshots.size() + " notificaciones");

                    notificationList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Notification notification = document.toObject(Notification.class);
                        notification.setId(document.getId());
                        notificationList.add(notification);
                    }

                    // Actualizar UI
                    adapter.updateData(notificationList);
                    swipeRefresh.setRefreshing(false);

                    // Mostrar estado vacío si no hay notificaciones
                    if (notificationList.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                        // Actualizar visibilidad del botón de marcar todas como leídas
                        updateMarkAllReadButton();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("NotificationFragment", "Error al cargar notificaciones", e);
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(getContext(), "Error al cargar notificaciones: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    /**
     * Método para marcar una notificación como leída
     */
    private void markAsRead(Notification notification) {
        if (notification.isRead()) return;

        // Mostrar indicador de carga
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(true);
        }

        db.collection("users")
                .document(currentUserId)
                .collection("notifications")
                .document(notification.getId())
                .update("read", true)
                .addOnSuccessListener(aVoid -> {
                    // Actualizar localmente
                    notification.setRead(true);
                    adapter.notifyDataSetChanged();

                    // Ocultar indicador de carga
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }

                    // Actualizar estado del botón de marcar todas como leídas
                    updateMarkAllReadButton();

                    // Mostrar confirmación
                    Toast.makeText(getContext(), "Notificación marcada como leída", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Ocultar indicador de carga
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }

                    Toast.makeText(getContext(), "Error al marcar como leída", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Método para marcar todas las notificaciones como leídas
     */
    private void markAllAsRead() {
        // Mostrar indicador de carga
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(true);
        }

        // Verificar si hay notificaciones sin leer
        boolean hasUnread = false;
        for (Notification notification : notificationList) {
            if (!notification.isRead()) {
                hasUnread = true;
                break;
            }
        }

        if (!hasUnread) {
            swipeRefresh.setRefreshing(false);
            Toast.makeText(getContext(), "No hay notificaciones por leer", Toast.LENGTH_SHORT).show();
            return;
        }

        // Batch para actualizar múltiples documentos
        WriteBatch batch = db.batch();

        // Agregar cada notificación no leída al batch
        for (Notification notification : notificationList) {
            if (!notification.isRead()) {
                DocumentReference docRef = db.collection("users")
                        .document(currentUserId)
                        .collection("notifications")
                        .document(notification.getId());
                batch.update(docRef, "read", true);
            }
        }

        // Ejecutar el batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    // Actualizar localmente
                    for (Notification notification : notificationList) {
                        notification.setRead(true);
                    }

                    adapter.notifyDataSetChanged();

                    // Ocultar indicador de carga
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }

                    // Actualizar estado del botón
                    updateMarkAllReadButton();

                    // Mostrar confirmación
                    Toast.makeText(getContext(), "Todas las notificaciones marcadas como leídas",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Ocultar indicador de carga
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }

                    Toast.makeText(getContext(), "Error al marcar todas como leídas",
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Actualiza la visibilidad del botón de marcar todas como leídas
     */
    private void updateMarkAllReadButton() {
        if (btnMarkAllRead == null) return;

        // Verificar si hay notificaciones sin leer
        boolean hasUnread = false;
        for (Notification notification : notificationList) {
            if (!notification.isRead()) {
                hasUnread = true;
                break;
            }
        }

        // Actualizar visibilidad del botón
        btnMarkAllRead.setEnabled(hasUnread);
        btnMarkAllRead.setAlpha(hasUnread ? 1.0f : 0.5f);
    }

    /**
     * Mostrar estado vacío cuando no hay notificaciones
     */
    private void showEmptyState() {
        if (emptyState != null) {
            emptyState.setVisibility(View.VISIBLE);
        }
        if (rvNotifications != null) {
            rvNotifications.setVisibility(View.GONE);
        }
        // Ocultar botón de marcar todas como leídas
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setVisibility(View.GONE);
        }
    }

    /**
     * Ocultar estado vacío cuando hay notificaciones
     */
    private void hideEmptyState() {
        if (emptyState != null) {
            emptyState.setVisibility(View.GONE);
        }
        if (rvNotifications != null) {
            rvNotifications.setVisibility(View.VISIBLE);
        }
        // Mostrar botón de marcar todas como leídas
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setVisibility(View.VISIBLE);
        }
    }

    // Implementación de los métodos de la interfaz OnNotificationListener
    @Override
    public void onNotificationClick(Notification notification) {
        // Marcar como leída
        markAsRead(notification);

        // Manejar el click según el tipo de notificación
        switch (notification.getType()) {
            case Notification.TYPE_BOOKING:
                navigateToBookingDetails(notification.getActionData());
                break;
            case Notification.TYPE_CHECK_IN:
                navigateToCheckInDetails(notification.getActionData());
                break;
            case Notification.TYPE_CHECK_OUT:
                // Solo mostrar la notificación, no hay acción adicional
                break;
        }
    }

    @Override
    public void onViewDetailsClick(Notification notification) {
        // Marcar como leída
        markAsRead(notification);

        // Navegar según el tipo
        if (notification.getType() == Notification.TYPE_BOOKING) {
            navigateToBookingDetails(notification.getActionData());
        } else if (notification.getType() == Notification.TYPE_CHECK_IN) {
            navigateToCheckInDetails(notification.getActionData());
        }
    }

    @Override
    public void onMarkAsReadClick(Notification notification) {
        markAsRead(notification);
    }

    /**
     * Navegar a la pantalla de detalles de reserva
     */
    private void navigateToBookingDetails(String bookingId) {
        // TODO: Implementar navegación a los detalles de la reserva
        Toast.makeText(getContext(), "Ver detalles de reserva: " + bookingId,
                Toast.LENGTH_SHORT).show();

        // Ejemplo de navegación con FragmentManager
        /*
        BookingDetailFragment fragment = BookingDetailFragment.newInstance(bookingId);
        getParentFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit();
        */
    }

    /**
     * Navegar a la pantalla de check-in
     */
    private void navigateToCheckInDetails(String bookingId) {
        // TODO: Implementar navegación a la pantalla de check-in
        Toast.makeText(getContext(), "Ver detalles de check-in: " + bookingId,
                Toast.LENGTH_SHORT).show();

        // Ejemplo de navegación con FragmentManager
       /*
       CheckInFragment fragment = CheckInFragment.newInstance(bookingId);
       getParentFragmentManager().beginTransaction()
           .replace(R.id.fragment_container, fragment)
           .addToBackStack(null)
           .commit();
       */
    }

    /**
     * Método para simular notificaciones (solo para desarrollo)
     * Quita este método en producción
     */
    private void addSampleNotifications() {
        // Generar notificaciones de ejemplo con mejor diseño de datos
        List<Notification> sampleNotifications = new ArrayList<>();

        // Añadir más variedad de notificaciones para mejor demostración
        sampleNotifications.add(new Notification(
                "1",
                "Reserva confirmada",
                "Tu reserva para Hotel Miraflores ha sido confirmada exitosamente. Te esperamos el 15 de Mayo.",
                Notification.TYPE_BOOKING,
                new Date(System.currentTimeMillis() - 600000), // 10 minutos atrás
                false,
                "booking123"
        ));

        sampleNotifications.add(new Notification(
                "2",
                "Check-in disponible",
                "Ya puedes realizar tu check-in para tu estadía en Hotel Lima Centro. Te esperamos mañana.",
                Notification.TYPE_CHECK_IN,
                new Date(System.currentTimeMillis() - 7200000), // 2 horas atrás
                false,
                "booking456"
        ));

        sampleNotifications.add(new Notification(
                "3",
                "Check-out completado",
                "Tu check-out del Hotel San Isidro ha sido procesado. ¡Gracias por tu estadía!",
                Notification.TYPE_CHECK_OUT,
                new Date(System.currentTimeMillis() - 86400000), // 1 día atrás
                true,
                "booking789"
        ));

        // Añadir más notificaciones para una mejor demostración
        sampleNotifications.add(new Notification(
                "4",
                "Oferta especial",
                "¡Obtén un 20% de descuento en tu próxima reserva! Usa el código HOTEL20 al reservar.",
                Notification.TYPE_BOOKING,
                new Date(System.currentTimeMillis() - 259200000), // 3 días atrás
                false,
                "promo123"
        ));

        sampleNotifications.add(new Notification(
                "5",
                "Recordatorio de reserva",
                "Te recordamos que tienes una reserva en Hotel Barranco para mañana. ¡Te esperamos!",
                Notification.TYPE_BOOKING,
                new Date(System.currentTimeMillis() - 172800000), // 2 días atrás
                true,
                "booking101"
        ));

        notificationList.clear();
        notificationList.addAll(sampleNotifications);
        adapter.notifyDataSetChanged();

        // Actualizar UI
        if (notificationList.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            // Actualizar estado del botón de marcar todas como leídas
            updateMarkAllReadButton();
        }
    }

    /**
     * Implementar un método para guardar notificaciones en Firestore
     * Este método puede ser usado desde otras partes de la aplicación
     */
    public static void saveNotificationToFirestore(Context context, String userId, Notification notification) {
        // Si no hay usuario, no guardamos nada
        if (userId.isEmpty()) {
            Log.e("NotificationFragment", "No se puede guardar: userId vacío");
            return;
        }

        // Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Crear un mapa con los datos de la notificación
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", notification.getTitle());
        notificationData.put("message", notification.getMessage());
        notificationData.put("type", notification.getType());
        notificationData.put("timestamp", notification.getTimestamp());
        notificationData.put("read", notification.isRead());
        notificationData.put("actionData", notification.getActionData());

        // Guardar en Firestore
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .add(notificationData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("NotificationManager", "Notificación guardada con ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("NotificationManager", "Error al guardar notificación", e);
                });
    }
}