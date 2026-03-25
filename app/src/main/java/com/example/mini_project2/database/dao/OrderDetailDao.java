package com.example.mini_project2.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.mini_project2.database.entities.OrderDetail;

import java.util.List;

@Dao
public interface OrderDetailDao {
    @Insert
    void insert(OrderDetail orderDetail);

    @Query("SELECT * FROM order_details WHERE orderId = :orderId")
    List<OrderDetail> getDetailsByOrder(int orderId);

    @Query("DELETE FROM order_details WHERE orderId = :orderId")
    void deleteByOrder(int orderId);

    @Query("SELECT * FROM order_details WHERE orderId = :orderId AND productId = :productId LIMIT 1")
    OrderDetail getByOrderAndProduct(int orderId, int productId);

    @Query("UPDATE order_details SET quantity = :quantity WHERE id = :id")
    void updateQuantity(int id, int quantity);
}
