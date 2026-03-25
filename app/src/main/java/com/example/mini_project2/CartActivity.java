package com.example.mini_project2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
                    adapter.updateData(details);
                    NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                    tvTotalAmount.setText(formatter.format(pendingOrder.getTotalAmount()) + " đ");
                    btnCheckout.setEnabled(!details.isEmpty());
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
        if (pendingOrder == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận thanh toán")
                .setMessage("Bạn có chắc chắn muốn thanh toán đơn hàng này?")
                .setPositiveButton("Thanh toán", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        // Update order date and status
                        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
                        db.orderDao().updateStatus(pendingOrder.getId(), "Paid");

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                            // Navigate to invoice
                            Intent intent = new Intent(this, InvoiceActivity.class);
                            intent.putExtra("orderId", pendingOrder.getId());
                            startActivity(intent);
                            finish();
                        });
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
