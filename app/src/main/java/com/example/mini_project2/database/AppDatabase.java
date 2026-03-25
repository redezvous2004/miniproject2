package com.example.mini_project2.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.mini_project2.database.dao.CategoryDao;
import com.example.mini_project2.database.dao.OrderDao;
import com.example.mini_project2.database.dao.OrderDetailDao;
import com.example.mini_project2.database.dao.ProductDao;
import com.example.mini_project2.database.dao.UserDao;
import com.example.mini_project2.database.entities.Category;
import com.example.mini_project2.database.entities.Order;
import com.example.mini_project2.database.entities.OrderDetail;
import com.example.mini_project2.database.entities.Product;
import com.example.mini_project2.database.entities.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Category.class, Product.class, Order.class, OrderDetail.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract CategoryDao categoryDao();
    public abstract ProductDao productDao();
    public abstract OrderDao orderDao();
    public abstract OrderDetailDao orderDetailDao();

    private static volatile AppDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "shopping_db")
                            .addCallback(seedDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback seedDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                // Seed Users
                UserDao userDao = INSTANCE.userDao();
                userDao.insert(new User("admin", "123456", "Admin User", "admin@shop.com"));
                userDao.insert(new User("user1", "123456", "Nguyen Van A", "vana@shop.com"));
                userDao.insert(new User("user2", "123456", "Tran Thi B", "thib@shop.com"));

                // Seed Categories
                CategoryDao categoryDao = INSTANCE.categoryDao();
                categoryDao.insert(new Category("Điện thoại", "Các loại điện thoại thông minh"));
                categoryDao.insert(new Category("Laptop", "Máy tính xách tay các loại"));
                categoryDao.insert(new Category("Phụ kiện", "Phụ kiện điện tử"));
                categoryDao.insert(new Category("Tablet", "Máy tính bảng"));

                // Seed Products
                ProductDao productDao = INSTANCE.productDao();
                // Điện thoại (categoryId = 1)
                productDao.insert(new Product("iPhone 15 Pro Max", "Điện thoại Apple cao cấp, chip A17 Pro, camera 48MP", 34990000, "iphone15", 1));
                productDao.insert(new Product("Samsung Galaxy S24 Ultra", "Điện thoại Samsung flagshlip, S-Pen, camera 200MP", 31990000, "samsung_s24", 1));
                productDao.insert(new Product("Xiaomi 14 Ultra", "Điện thoại Xiaomi cao cấp, camera Leica", 22990000, "xiaomi14", 1));
                productDao.insert(new Product("OPPO Find X7 Ultra", "Điện thoại OPPO flagship, camera Hasselblad", 24990000, "oppo_findx7", 1));

                // Laptop (categoryId = 2)
                productDao.insert(new Product("MacBook Pro 14 M3", "Laptop Apple chip M3, 16GB RAM, 512GB SSD", 49990000, "macbook_m3", 2));
                productDao.insert(new Product("Dell XPS 15", "Laptop Dell cao cấp, Intel i7, 16GB RAM", 39990000, "dell_xps15", 2));
                productDao.insert(new Product("ASUS ROG Strix G16", "Laptop gaming ASUS, RTX 4060, 16GB RAM", 35990000, "asus_rog", 2));

                // Phụ kiện (categoryId = 3)
                productDao.insert(new Product("AirPods Pro 2", "Tai nghe không dây Apple, chống ồn chủ động", 6990000, "airpods_pro", 3));
                productDao.insert(new Product("Samsung Galaxy Watch 6", "Đồng hồ thông minh Samsung", 7490000, "galaxy_watch", 3));
                productDao.insert(new Product("Anker PowerBank 20000mAh", "Sạc dự phòng Anker, sạc nhanh 65W", 1290000, "anker_powerbank", 3));

                // Tablet (categoryId = 4)
                productDao.insert(new Product("iPad Pro M2 12.9", "Máy tính bảng Apple, chip M2, Liquid Retina XDR", 31990000, "ipad_pro", 4));
                productDao.insert(new Product("Samsung Galaxy Tab S9", "Máy tính bảng Samsung, AMOLED, S-Pen", 22990000, "galaxy_tab", 4));
            });
        }
    };
}
