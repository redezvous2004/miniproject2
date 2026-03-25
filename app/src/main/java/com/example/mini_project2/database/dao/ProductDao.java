package com.example.mini_project2.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.mini_project2.database.entities.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    void insert(Product product);

    @Query("SELECT * FROM products")
    List<Product> getAllProducts();

    @Query("SELECT * FROM products WHERE categoryId = :categoryId")
    List<Product> getProductsByCategory(int categoryId);

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    Product getProductById(int id);

    @Query("SELECT * FROM products WHERE name LIKE '%' || :keyword || '%'")
    List<Product> searchProducts(String keyword);
}
