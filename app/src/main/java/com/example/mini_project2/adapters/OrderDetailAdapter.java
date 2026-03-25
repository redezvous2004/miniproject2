package com.example.mini_project2.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mini_project2.R;
import com.example.mini_project2.database.entities.OrderDetail;
import com.example.mini_project2.database.entities.Product;
import com.example.mini_project2.database.AppDatabase;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {
    private List<OrderDetail> orderDetails;
    private final AppDatabase db;

    public OrderDetailAdapter(List<OrderDetail> orderDetails, AppDatabase db) {
        this.orderDetails = orderDetails;
        this.db = db;
    }

    public void updateData(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetail detail = orderDetails.get(position);
        Product product = db.productDao().getProductById(detail.getProductId());

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        if (product != null) {
            holder.tvItemName.setText(product.getName());
        } else {
            holder.tvItemName.setText("Sản phẩm #" + detail.getProductId());
        }

        holder.tvItemDetail.setText("SL: " + detail.getQuantity() + " x " + formatter.format(detail.getUnitPrice()) + " đ");
        holder.tvItemPrice.setText(formatter.format(detail.getQuantity() * detail.getUnitPrice()) + " đ");
    }

    @Override
    public int getItemCount() {
        return orderDetails != null ? orderDetails.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvItemDetail, tvItemPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemDetail = itemView.findViewById(R.id.tvItemDetail);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
        }
    }
}
