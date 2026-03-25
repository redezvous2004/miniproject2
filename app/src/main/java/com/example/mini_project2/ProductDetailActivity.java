package com.example.mini_project2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mini_project2.database.AppDatabase;
import com.example.mini_project2.database.entities.Category;
import com.example.mini_project2.database.entities.Order;
import com.example.mini_project2.database.entities.OrderDetail;
import com.example.mini_project2.database.entities.Product;
import com.example.mini_project2.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {
    private TextView tvProductName, tvProductPrice, tvProductCategory, tvProductDescription, tvQuantity;
    private MaterialButton btnAddToCart;
    private ImageButton btnMinus, btnPlus;
    private AppDatabase db;
    private SessionManager sessionManager;
    private Product product;
    private int quantity = 1;

    private static final int LOGIN_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        db = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);

        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvProductCategory = findViewById(R.id.tvProductCategory);
        tvProductDescription = findViewById(R.id.tvProductDescription);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);

        int productId = getIntent().getIntExtra("productId", -1);
        if (productId == -1) {
            finish();
            return;
        }

        loadProduct(productId);

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });

        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    private void loadProduct(int productId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            product = db.productDao().getProductById(productId);
            if (product != null) {
                Category category = db.categoryDao().getCategoryById(product.getCategoryId());
                runOnUiThread(() -> {
                    tvProductName.setText(product.getName());
                    NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                    tvProductPrice.setText(formatter.format(product.getPrice()) + " đ");
                    tvProductCategory.setText(category != null ? category.getName() : "");
                    tvProductDescription.setText(product.getDescription());
                });
            }
        });
    }

    private void addToCart() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Bạn cần đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LOGIN_REQUEST_CODE);
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            int userId = sessionManager.getUserId();

            // Get or create pending order
            Order pendingOrder = db.orderDao().getPendingOrder(userId);
            int orderId;

            if (pendingOrder == null) {
                String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
                Order newOrder = new Order(userId, date, 0, "Pending");
                orderId = (int) db.orderDao().insert(newOrder);
            } else {
                orderId = pendingOrder.getId();
            }

            // Check if product already in order
            OrderDetail existingDetail = db.orderDetailDao().getByOrderAndProduct(orderId, product.getId());
            if (existingDetail != null) {
                db.orderDetailDao().updateQuantity(existingDetail.getId(), existingDetail.getQuantity() + quantity);
            } else {
                OrderDetail detail = new OrderDetail(orderId, product.getId(), quantity, product.getPrice());
                db.orderDetailDao().insert(detail);
            }

            // Update order total
            double total = 0;
            for (OrderDetail d : db.orderDetailDao().getDetailsByOrder(orderId)) {
                total += d.getQuantity() * d.getUnitPrice();
            }
            db.orderDao().updateTotalAmount(orderId, total);

            runOnUiThread(() -> {
                Toast.makeText(this, "Đã thêm " + quantity + " sản phẩm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            // User logged in successfully, now add to cart
            addToCart();
        }
    }
}
