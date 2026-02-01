package com.example.designpatternwithandroidlab1.allproduct;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.designpatternwithandroidlab1.R;
import com.example.designpatternwithandroidlab1.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    OnProductClick onProductClick;

    public ProductAdapter(OnProductClick onProductClick) {
        this.productList = new ArrayList<>();
        this.onProductClick = onProductClick;
    }
    public void setProductList(List<Product> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView movieImageView;
        private TextView movieTitleTextView;
        private TextView movieCategoryTextView;
        private Button addToFavoritesButton;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            movieImageView = itemView.findViewById(R.id.iv_poster);
            movieTitleTextView = itemView.findViewById(R.id.tv_name);
            movieCategoryTextView = itemView.findViewById(R.id.tv_category);
            addToFavoritesButton = itemView.findViewById(R.id.btn_addToFav);
        }

        public void bind(Product product) {
            movieTitleTextView.setText(product.getTitle());
            movieCategoryTextView.setText(product.getBrand());
            Glide.with(itemView)
                    .load(product.getThumnail())
                    .into(movieImageView);
            addToFavoritesButton.setOnClickListener(view -> {
                onProductClick.addToFav(product);
                    }
            );
        }
    }
}

