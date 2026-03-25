package com.example.mini_project2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mini_project2.ProductDetailActivity;
import com.example.mini_project2.R;
import com.example.mini_project2.adapters.ProductAdapter;
import com.example.mini_project2.database.AppDatabase;
import com.example.mini_project2.database.entities.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductListFragment extends Fragment implements ProductAdapter.OnProductClickListener {
    private RecyclerView rvProducts;
    private ProductAdapter adapter;
    private AppDatabase db;
    private int categoryId = -1; // -1 means all products

    public static ProductListFragment newInstance() {
        return new ProductListFragment();
    }

    public static ProductListFragment newInstance(int categoryId) {
        ProductListFragment fragment = new ProductListFragment();
        Bundle args = new Bundle();
        args.putInt("categoryId", categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getInt("categoryId", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_list, container, false);
        rvProducts = view.findViewById(R.id.rvProducts);
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new ProductAdapter(new ArrayList<>(), this);
        rvProducts.setAdapter(adapter);

        db = AppDatabase.getInstance(requireContext());
        loadProducts();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProducts();
    }

    private void loadProducts() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Product> products;
            if (categoryId > 0) {
                products = db.productDao().getProductsByCategory(categoryId);
            } else {
                products = db.productDao().getAllProducts();
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.updateProducts(products));
            }
        });
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra("productId", product.getId());
        startActivity(intent);
    }
}