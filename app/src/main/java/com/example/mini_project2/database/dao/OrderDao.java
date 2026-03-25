package com.example.mini_project2.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.mini_project2.database.entities.Order;

import java.util.List;
/**comment
 * Phương thức lấy instance database
 *  Context ứng dụng
 *  Instance của AppDatabase
 * public interface OrderDao {
    @Insert
    long insert(Order order);

    @Query("SELECT * FROM orders WHERE userId = :userId")
    List<Order> getOrdersByUser(int userId);

    @Query("SELECT * FROM orders WHERE userId = :userId AND status = 'Pending' LIMIT 1")
    Order getPendingOrder(int userId);

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    void updateStatus(int orderId, String status);

    @Query("UPDATE orders SET totalAmount = :total WHERE id = :orderId")
    void updateTotalAmount(int orderId, double total);

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    Order getOrderById(int id);
 */
@Dao
public interface OrderDao {
    @Insert
    long insert(Order order);

    @Query("SELECT * FROM orders WHERE userId = :userId")
    List<Order> getOrdersByUser(int userId);

    @Query("SELECT * FROM orders WHERE userId = :userId AND status = 'Pending' LIMIT 1")
    Order getPendingOrder(int userId);

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    void updateStatus(int orderId, String status);

    @Query("UPDATE orders SET totalAmount = :total WHERE id = :orderId")
    void updateTotalAmount(int orderId, double total);

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    Order getOrderById(int id);
}
