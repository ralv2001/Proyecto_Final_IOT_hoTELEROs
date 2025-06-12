package com.example.proyecto_final_hoteleros.superadmin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.superadmin.models.TaxistaUser;

import java.util.List;

public class TaxistasAdapter extends RecyclerView.Adapter<TaxistasAdapter.TaxistaViewHolder> {

    private List<TaxistaUser> taxistas;
    private OnTaxistaActionListener actionListener;

    public interface OnTaxistaActionListener {
        void onTaxistaAction(TaxistaUser taxista, String action);
    }

    public TaxistasAdapter(List<TaxistaUser> taxistas, OnTaxistaActionListener actionListener) {
        this.taxistas = taxistas;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public TaxistaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_taxista_user, parent, false);
        return new TaxistaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxistaViewHolder holder, int position) {
        TaxistaUser taxista = taxistas.get(position);
        holder.bind(taxista, actionListener);
    }

    @Override
    public int getItemCount() {
        return taxistas.size();
    }

    public void updateData(List<TaxistaUser> newTaxistas) {
        this.taxistas.clear();
        this.taxistas.addAll(newTaxistas);
        notifyDataSetChanged();
    }

    static class TaxistaViewHolder extends RecyclerView.ViewHolder {
        private CardView cardTaxista;
        private ImageView ivProfile, ivCar, ivMore;
        private TextView tvName, tvEmail, tvLicensePlate, tvCarModel, tvRegistrationDate;
        private TextView  chipStatus;
        private MaterialButton btnApprove, btnReject;
        private View layoutActions;

        public TaxistaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTaxista = itemView.findViewById(R.id.card_taxista);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            ivCar = itemView.findViewById(R.id.iv_car);
            ivMore = itemView.findViewById(R.id.iv_more);
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvLicensePlate = itemView.findViewById(R.id.tv_license_plate);
            tvRegistrationDate = itemView.findViewById(R.id.tv_registration_date);
            chipStatus = itemView.findViewById(R.id.chip_status);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnReject = itemView.findViewById(R.id.btn_reject);
            layoutActions = itemView.findViewById(R.id.layout_actions);
        }

        public void bind(TaxistaUser taxista, OnTaxistaActionListener actionListener) {
            // Configurar datos básicos
            tvName.setText(taxista.getFullName()); // Usar nombre completo
            tvEmail.setText(taxista.getEmail());
            tvLicensePlate.setText("🚗 " + taxista.getLicensePlate());
            // REMOVER esta línea: tvCarModel.setText(taxista.getCarModel());
            tvRegistrationDate.setText("Registrado: " + taxista.getRegistrationDate());

            // Configurar chip de estado
            chipStatus.setText(taxista.getStatusText());
            chipStatus.setBackgroundColor(taxista.getStatusColor());

            // Configurar imágenes (placeholders por ahora)
            ivProfile.setImageResource(R.drawable.ic_person);
            ivCar.setImageResource(R.drawable.ic_car);

            // Mostrar/ocultar botones de acción según el estado
            if (taxista.isPending()) {
                layoutActions.setVisibility(View.VISIBLE);
                btnApprove.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
            } else {
                layoutActions.setVisibility(View.GONE);
            }

            // Click listeners
            cardTaxista.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onTaxistaAction(taxista, "view_details");
                }
            });

            btnApprove.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onTaxistaAction(taxista, "approve");
                }
            });

            btnReject.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onTaxistaAction(taxista, "reject");
                }
            });

            ivMore.setOnClickListener(v -> {
                showMoreOptions(v, taxista, actionListener);
            });
        }

        private void showMoreOptions(View anchor, TaxistaUser taxista, OnTaxistaActionListener actionListener) {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(anchor.getContext(), anchor);
            popup.inflate(R.menu.menu_taxista_options);

            popup.setOnMenuItemClickListener(item -> {
                if (actionListener != null) {
                    if (item.getItemId() == R.id.action_view_documents) {
                        actionListener.onTaxistaAction(taxista, "view_documents");
                        return true;
                    } else if (item.getItemId() == R.id.action_contact) {
                        actionListener.onTaxistaAction(taxista, "contact");
                        return true;
                    } else if (item.getItemId() == R.id.action_view_trips) {
                        actionListener.onTaxistaAction(taxista, "view_trips");
                        return true;
                    }
                }
                return false;
            });

            popup.show();
        }
    }
}