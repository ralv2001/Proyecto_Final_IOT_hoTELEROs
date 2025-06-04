package com.example.proyecto_final_hoteleros.client.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_final_hoteleros.R;
import com.example.proyecto_final_hoteleros.client.data.model.City;

import java.util.List;

public class CitiesAdapter extends RecyclerView.Adapter<CitiesAdapter.CityViewHolder> {
    private List<City> citiesList;
    private Context context;
    private OnCityClickListener listener;

    // Interfaz para manejar clics en las ciudades
    public interface OnCityClickListener {
        void onCityClick(City city);
    }

    public CitiesAdapter(List<City> citiesList) {
        this.citiesList = citiesList;
    }

    // Setter para el listener
    public void setOnCityClickListener(OnCityClickListener listener) {
        this.listener = listener;
    }

    @Override
    public CityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_city_card, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CityViewHolder holder, int position) {
        City city = citiesList.get(position);
        holder.tvCityName.setText(city.getName());

        // Cargar la imagen dependiendo de cómo estés manejando las imágenes
        if (city.getImageResourceId() != 0) {
            holder.ivCityImage.setImageResource(city.getImageResourceId());
        } else {
            // Si estás usando URLs de imágenes, puedes usar Glide o Picasso
            // Glide.with(context).load(city.getImageUrl()).into(holder.ivCityImage);
        }

        // Configurar click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Llamar al listener personalizado en lugar del Toast
                listener.onCityClick(city);
            } else {
                // Fallback al comportamiento anterior
                Toast.makeText(context, "Ciudad seleccionada: " + city.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return citiesList.size();
    }

    class CityViewHolder extends RecyclerView.ViewHolder {
        TextView tvCityName;
        ImageView ivCityImage;

        public CityViewHolder(View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tvCityName);
            ivCityImage = itemView.findViewById(R.id.ivCityImage);
        }
    }
}