package com.example.mini_project2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.mini_project2.database.AppDatabase;
import com.example.mini_project2.fragments.CategoryListFragment;
import com.example.mini_project2.fragments.ProductListFragment;
import com.example.mini_project2.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private Toolbar toolbar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database (triggers seed data on first run)
        AppDatabase.getInstance(this);

        sessionManager = new SessionManager(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        updateToolbarTitle();

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_products) {
                fragment = ProductListFragment.newInstance();
                if (getSupportActionBar() != null) getSupportActionBar().setTitle("Sản phẩm");
            } else if (id == R.id.nav_categories) {
                fragment = CategoryListFragment.newInstance();
                if (getSupportActionBar() != null) getSupportActionBar().setTitle("Danh mục");
            } else if (id == R.id.nav_cart) {
                Intent intent = new Intent(this, CartActivity.class);
                startActivity(intent);
                return false;
            }

            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .commit();
            }
            return true;
        });

        // Default fragment
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_products);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateToolbarTitle();
        invalidateOptionsMenu();
    }

    private void updateToolbarTitle() {
        if (sessionManager.isLoggedIn()) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle("Xin chào, " + sessionManager.getFullName());
            }
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(null);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem loginItem = menu.findItem(R.id.action_login);
        if (sessionManager.isLoggedIn()) {
            loginItem.setTitle("Đăng xuất");
        } else {
            loginItem.setTitle("Đăng nhập");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_login) {
            if (sessionManager.isLoggedIn()) {
                sessionManager.logoutUser();
                updateToolbarTitle();
                invalidateOptionsMenu();
                recreate();
            } else {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}