package com.example.mini_project2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mini_project2.adapters.OrderDetailAdapter;
import com.example.mini_project2.database.AppDatabase;
import com.example.mini_project2.database.entities.Order;
import com.example.mini_project2.database.entities.OrderDetail;
import com.example.mini_project2.database.entities.User;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InvoiceActivity extends AppCompatActivity {
    private TextView tvOrderId, tvOrderDate, tvCustomerName, tvStatus, tvTotalAmount;
    private RecyclerView rvInvoiceItems;
    private MaterialButton btnBackHome;
    private AppDatabase db;
    private OrderDetailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        db = AppDatabase.getInstance(this);

        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvStatus = findViewById(R.id.tvStatus);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        rvInvoiceItems = findViewById(R.id.rvInvoiceItems);
        btnBackHome = findViewById(R.id.btnBackHome);

        rvInvoiceItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderDetailAdapter(new ArrayList<>(), db);
        rvInvoiceItems.setAdapter(adapter);

        int orderId = getIntent().getIntExtra("orderId", -1);
        if (orderId == -1) {
            finish();
            return;
        }

        loadInvoice(orderId);

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadInvoice(int orderId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Order order = db.orderDao().getOrderById(orderId);
            if (order == null) {
                runOnUiThread(this::finish);
                return;
            }

            User user = db.userDao().getUserById(order.getUserId());
            List<OrderDetail> details = db.orderDetailDao().getDetailsByOrder(orderId);

            runOnUiThread(() -> {
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                tvOrderId.setText("#" + order.getId());
                tvOrderDate.setText(order.getOrderDate());
                tvCustomerName.setText(user != null ? user.getFullName() : "N/A");
                tvStatus.setText(order.getStatus());
                tvTotalAmount.setText(formatter.format(order.getTotalAmount()) + " đ");
                adapter.updateData(details);
            });
        });
    }
}
