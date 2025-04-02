package com.uilover.project2152.Activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.uilover.project2152.Adapter.CategoryAdapter;
import com.uilover.project2152.Adapter.PopularAdapter;
import com.uilover.project2152.Model.CategoryModel;
import com.uilover.project2152.Model.ItemModel;
import com.uilover.project2152.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initCategory();
        initPopular();
    }

    private void initPopular() {
        DatabaseReference myRef = database.getReference("Popular");
        binding.progressBarPopular.setVisibility(View.VISIBLE);

        ArrayList<ItemModel> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(ItemModel.class));
                    }

                    if (!list.isEmpty()) {
                        binding.recyclerViewPopular.setLayoutManager(new LinearLayoutManager(MainActivity.this,
                                LinearLayoutManager.HORIZONTAL,false));
                        RecyclerView.Adapter adapter = new PopularAdapter(list);
                        binding.recyclerViewPopular.setAdapter(adapter);
                    }
                    binding.progressBarPopular.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initCategory() {
        DatabaseReference myref = database.getReference("Category");
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        ArrayList<CategoryModel> list = new ArrayList<>();

        myref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(CategoryModel.class));
                    }
                    if (!list.isEmpty()) {
                        binding.recyclerViewCategory.setLayoutManager(
                                new GridLayoutManager(MainActivity.this, 4)
                        );
                        RecyclerView.Adapter adapter = new CategoryAdapter(list);
                        binding.recyclerViewCategory.setAdapter(adapter);
                    }
                    binding.progressBarCategory.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}