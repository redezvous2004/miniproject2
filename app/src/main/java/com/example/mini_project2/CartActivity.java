package com.example.mini_project2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mini_project2.adapters.OrderDetailAdapter;
import com.example.mini_project2.database.AppDatabase;
import com.example.mini_project2.database.entities.Order;
import com.example.mini_project2.database.entities.OrderDetail;
import com.example.mini_project2.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCartItems;
    private TextView tvTotalAmount;
    private MaterialButton btnCheckout;
    private OrderDetailAdapter adapter;
    private AppDatabase db;
    private SessionManager sessionManager;
    private Order pendingOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Giỏ hàng");
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvCartItems = findViewById(R.id.rvCartItems);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnCheckout = findViewById(R.id.btnCheckout);

        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderDetailAdapter(new ArrayList<>(), db);
        rvCartItems.setAdapter(adapter);

        btnCheckout.setOnClickListener(v -> checkout());

        loadCart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCart();
    }

    private void loadCart() {
        if (!sessionManager.isLoggedIn()) {
            adapter.updateData(new ArrayList<>());
            tvTotalAmount.setText("0 đ");
            btnCheckout.setEnabled(false);
            Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            int userId = sessionManager.getUserId();
            pendingOrder = db.orderDao().getPendingOrder(userId);

            if (pendingOrder != null) {
                List<OrderDetail> details = db.orderDetailDao().getDetailsByOrder(pendingOrder.getId());

                runOnUiThread(() -> {
                    adapter.updateData(details != null ? details : new ArrayList<>());

                    NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                    tvTotalAmount.setText(formatter.format(pendingOrder.getTotalAmount()) + " đ");

                    boolean canCheckout = details != null && !details.isEmpty();
                    btnCheckout.setEnabled(canCheckout);
                });
            } else {
                runOnUiThread(() -> {
                    adapter.updateData(new ArrayList<>());
                    tvTotalAmount.setText("0 đ");
                    btnCheckout.setEnabled(false);
                });
            }
        });
    }

    private void checkout() {
        if (pendingOrder == null) {
            Toast.makeText(this, "Không có đơn hàng để thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<OrderDetail> details = db.orderDetailDao().getDetailsByOrder(pendingOrder.getId());

            runOnUiThread(() -> {
                if (details == null || details.isEmpty()) {
                    Toast.makeText(this, "Giỏ hàng đang trống", Toast.LENGTH_SHORT).show();
                    btnCheckout.setEnabled(false);
                    return;
                }

                new AlertDialog.Builder(this)
                        .setTitle("Xác nhận thanh toán")
                        .setMessage("Bạn có chắc chắn muốn thanh toán đơn hàng này?")
                        .setPositiveButton("Thanh toán", (dialog, which) -> processCheckout())
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        });
    }

    private void processCheckout() {
        btnCheckout.setEnabled(false);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int orderId = pendingOrder.getId();
                String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(new Date());

                // Nếu Order entity có setter thì cập nhật đầy đủ rồi update
                Order order = db.orderDao().getOrderById(orderId);
                if (order == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
                        btnCheckout.setEnabled(true);
                    });
                    return;
                }

                order.setStatus("Paid");
                order.setOrderDate(date);
                db.orderDao().update(order);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CartActivity.this, InvoiceActivity.class);
                    intent.putExtra("orderId", orderId);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Thanh toán thất bại", Toast.LENGTH_SHORT).show();
                    btnCheckout.setEnabled(true);
                });
            }
        });
    }
}
