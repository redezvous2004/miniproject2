package com.example.mini_project2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mini_project2.database.AppDatabase;
import com.example.mini_project2.database.entities.User;
import com.example.mini_project2.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText edtUsername, edtPassword;
    private MaterialButton btnLogin;
    private TextView tvError;
    private AppDatabase db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);

        // If already logged in, go back
        if (sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvError = findViewById(R.id.tvError);

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            tvError.setText("Vui lòng nhập đầy đủ thông tin");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = db.userDao().login(username, password);
            runOnUiThread(() -> {
                if (user != null) {
                    sessionManager.createLoginSession(user.getId(), user.getUsername(), user.getFullName());
                    Toast.makeText(this, "Đăng nhập thành công! Xin chào " + user.getFullName(), Toast.LENGTH_SHORT).show();

                    // Return result to caller
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("userId", user.getId());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    tvError.setText("Sai tên đăng nhập hoặc mật khẩu");
                    tvError.setVisibility(View.VISIBLE);
                }
            });
        });
    }
}
