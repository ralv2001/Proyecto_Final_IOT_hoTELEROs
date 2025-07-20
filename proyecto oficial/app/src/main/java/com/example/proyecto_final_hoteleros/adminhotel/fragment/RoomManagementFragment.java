package com.example.proyecto_final_hoteleros.adminhotel.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.adminhotel.adapters.RoomTypeAdapter;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.AddRoomTypeDialog;
import com.example.proyecto_final_hoteleros.adminhotel.dialog.EditRoomTypeDialog;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseRoomManager;
import com.example.proyecto_final_hoteleros.adminhotel.utils.FirebaseServiceManager;
import com.example.proyecto_final_hoteleros.adminhotel.model.RoomType;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class RoomManagementFragment extends Fragment implements FirebaseRoomManager.OnRoomsChangedListener {

    private static final String TAG = "RoomManagementFragment";

    // Views
    private RecyclerView rvRoomTypes;
    private FloatingActionButton fabAddRoom;
    private ImageView ivBack;
    private LinearLayout layoutEmptyState;
    private LinearLayout layoutLoading;
    private MaterialButton btnCreateFirstRoom;

    // Firebase
    private FirebaseRoomManager firebaseRoomManager;
    private FirebaseServiceManager firebaseServiceManager;

    // Adapter y datos
    private RoomTypeAdapter roomAdapter;
    private List<RoomType> roomTypes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.admin_hotel_fragment_room_management, container, false);

        initViews(rootView);
        initFirebase();
        setupRecyclerView();
        setupClickListeners();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Registrar listeners de Firebase
        if (firebaseRoomManager != null) {
            firebaseRoomManager.addListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Quitar listeners de Firebase
        if (firebaseRoomManager != null) {
            firebaseRoomManager.removeListener(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (firebaseRoomManager != null) {
            firebaseRoomManager.cleanup();
        }
    }

    // ========== INICIALIZACIÓN ==========

    private void initViews(View rootView) {
        ivBack = rootView.findViewById(R.id.ivBack);
        rvRoomTypes = rootView.findViewById(R.id.rvRoomTypes);
        fabAddRoom = rootView.findViewById(R.id.fabAddRoom);
        layoutEmptyState = rootView.findViewById(R.id.layoutEmptyState);
        layoutLoading = rootView.findViewById(R.id.layoutLoading);
        btnCreateFirstRoom = rootView.findViewById(R.id.btnCreateFirstRoom);
    }

    private void initFirebase() {
        firebaseRoomManager = FirebaseRoomManager.getInstance(getContext());
        firebaseServiceManager = FirebaseServiceManager.getInstance(getContext());

        Log.d(TAG, "✅ Firebase managers inicializados");
    }

    private void setupRecyclerView() {
        roomTypes = new ArrayList<>();

        RoomTypeAdapter.OnRoomActionListener actionListener = new RoomTypeAdapter.OnRoomActionListener() {
            @Override
            public void onEditRoom(RoomType roomType, int position) {
                editRoomType(roomType, position);
            }

            @Override
            public void onDeleteRoom(RoomType roomType, int position) {
                deleteRoomType(roomType, position);
            }
        };

        roomAdapter = new RoomTypeAdapter(roomTypes, actionListener, actionListener);
        rvRoomTypes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRoomTypes.setAdapter(roomAdapter);

        Log.d(TAG, "✅ RecyclerView configurado");
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        fabAddRoom.setOnClickListener(v -> showAddRoomDialog());

        btnCreateFirstRoom.setOnClickListener(v -> showAddRoomDialog());
    }

    // ========== GESTIÓN DE ESTADOS ==========

    private void showLoading() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                layoutLoading.setVisibility(View.VISIBLE);
                layoutEmptyState.setVisibility(View.GONE);
                rvRoomTypes.setVisibility(View.GONE);
                fabAddRoom.setVisibility(View.GONE);
            });
        }
    }

    private void showEmptyState() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                layoutLoading.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.VISIBLE);
                rvRoomTypes.setVisibility(View.GONE);
                fabAddRoom.setVisibility(View.VISIBLE); // FAB siempre visible
            });
        }
    }

    private void showRoomsList() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                layoutLoading.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.GONE);
                rvRoomTypes.setVisibility(View.VISIBLE);
                fabAddRoom.setVisibility(View.VISIBLE);
            });
        }
    }

    // ========== FIREBASE LISTENERS ==========

    @Override
    public void onRoomsLoaded(List<RoomType> rooms) {
        Log.d(TAG, "🏨 Habitaciones cargadas desde Firebase: " + rooms.size());

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                roomTypes.clear();
                roomTypes.addAll(rooms);

                if (roomAdapter != null) {
                    roomAdapter.notifyDataSetChanged();
                }

                // Mostrar estado apropiado
                if (rooms.isEmpty()) {
                    showEmptyState();
                } else {
                    showRoomsList();
                }
            });
        }
    }

    @Override
    public void onRoomAdded(RoomType room) {
        Log.d(TAG, "➕ Habitación agregada: " + room.getName());
        // onRoomsLoaded maneja la actualización completa
    }

    @Override
    public void onRoomUpdated(RoomType room) {
        Log.d(TAG, "🔄 Habitación actualizada: " + room.getName());
        // onRoomsLoaded maneja la actualización completa
    }

    @Override
    public void onRoomDeleted(String roomId) {
        Log.d(TAG, "🗑️ Habitación eliminada: " + roomId);
        // onRoomsLoaded maneja la actualización completa
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "❌ Error desde Firebase: " + error);
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();

                // Si hay error y no hay habitaciones, mostrar estado vacío
                if (roomTypes.isEmpty()) {
                    showEmptyState();
                } else {
                    showRoomsList();
                }
            });
        }
    }

    // ========== DIÁLOGOS ==========

    private void showAddRoomDialog() {
        if (firebaseServiceManager == null) {
            Toast.makeText(getContext(), "❌ Error: Servicio no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        AddRoomTypeDialog dialog = new AddRoomTypeDialog(getContext(), firebaseServiceManager, roomType -> {
            Log.d(TAG, "🏨 Creando nueva habitación: " + roomType.getName());

            if (firebaseRoomManager != null) {
                firebaseRoomManager.createRoom(roomType, new FirebaseRoomManager.RoomCallback() {
                    @Override
                    public void onSuccess(RoomType createdRoom) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "✅ Habitación '" + createdRoom.getName() + "' creada exitosamente", Toast.LENGTH_SHORT).show();
                            });
                        }
                        Log.d(TAG, "✅ Habitación creada exitosamente: " + createdRoom.getId());
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "❌ Error creando habitación: " + error, Toast.LENGTH_LONG).show();
                            });
                        }
                        Log.e(TAG, "❌ Error creando habitación: " + error);
                    }
                });
            }
        });
        dialog.show();
    }

    private void editRoomType(RoomType roomType, int position) {
        if (firebaseServiceManager == null) {
            Toast.makeText(getContext(), "❌ Error: Servicio no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        EditRoomTypeDialog dialog = new EditRoomTypeDialog(getContext(), roomType, firebaseServiceManager, updatedRoom -> {
            Log.d(TAG, "🔄 Actualizando habitación: " + updatedRoom.getName());

            if (firebaseRoomManager != null) {
                firebaseRoomManager.updateRoom(updatedRoom, new FirebaseRoomManager.RoomCallback() {
                    @Override
                    public void onSuccess(RoomType room) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "✅ Habitación actualizada exitosamente", Toast.LENGTH_SHORT).show();
                            });
                        }
                        Log.d(TAG, "✅ Habitación actualizada exitosamente: " + room.getId());
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "❌ Error actualizando habitación: " + error, Toast.LENGTH_LONG).show();
                            });
                        }
                        Log.e(TAG, "❌ Error actualizando habitación: " + error);
                    }
                });
            }
        });
        dialog.show();
    }

    private void deleteRoomType(RoomType roomType, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("🗑️ Eliminar Habitación")
                .setMessage("¿Estás seguro de eliminar '" + roomType.getName() + "'?\n\n" +
                        "Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    Log.d(TAG, "🗑️ Eliminando habitación: " + roomType.getName());

                    if (firebaseRoomManager != null && roomType.getId() != null) {
                        firebaseRoomManager.deleteRoom(roomType.getId(), new FirebaseRoomManager.RoomCallback() {
                            @Override
                            public void onSuccess(RoomType room) {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(getContext(), "✅ Habitación eliminada exitosamente", Toast.LENGTH_SHORT).show();
                                    });
                                }
                                Log.d(TAG, "✅ Habitación eliminada exitosamente");
                            }

                            @Override
                            public void onError(String error) {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(getContext(), "❌ Error eliminando habitación: " + error, Toast.LENGTH_LONG).show();
                                    });
                                }
                                Log.e(TAG, "❌ Error eliminando habitación: " + error);
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "❌ Error: ID de habitación no válido", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}