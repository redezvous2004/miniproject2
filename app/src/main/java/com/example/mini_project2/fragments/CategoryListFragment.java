package com.example.mini_project2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mini_project2.R;
import com.example.mini_project2.adapters.CategoryAdapter;
import com.example.mini_project2.database.AppDatabase;
import com.example.mini_project2.database.entities.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryListFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {
    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private AppDatabase db;

    public static CategoryListFragment newInstance() {
        return new CategoryListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_list, container, false);
        rvCategories = view.findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CategoryAdapter(new ArrayList<>(), this);
        rvCategories.setAdapter(adapter);

        db = AppDatabase.getInstance(requireContext());
        loadCategories();

        return view;
    }

    private void loadCategories() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Category> categories = db.categoryDao().getAllCategories();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.updateCategories(categories));
            }
        });
    }

    @Override
    public void onCategoryClick(Category category) {
        // Navigate to product list filtered by category
        ProductListFragment fragment = ProductListFragment.newInstance(category.getId());
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}
