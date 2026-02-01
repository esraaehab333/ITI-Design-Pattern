package com.example.designpatternwithandroidlab2.presentation.fav.view;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.designpatternwithandroidlab2.R;
import com.example.designpatternwithandroidlab2.data.products.models.Product;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private List<Product> products;
    private OnFavoriteClickListener listener;
    public static final String TAG = "FavoriteAdapter";

    public FavoriteAdapter(OnFavoriteClickListener listener) {
        this.products = new ArrayList<>();
        this.listener = listener;
        Log.i(TAG, "FavoriteAdapter created");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void setList(List<Product> updatedProducts) {
        this.products = updatedProducts;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView favProductImg;
        TextView favProductName;
        TextView favProductCategory;
        Button removeFavBtn;
        ConstraintLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.constraint_fav_movie);
            favProductName = itemView.findViewById(R.id.tv_fav_name);
            favProductCategory = itemView.findViewById(R.id.tv_fav_category);
            removeFavBtn = itemView.findViewById(R.id.btn_fav_delete);
            favProductImg = itemView.findViewById(R.id.iv_fav_poster);
        }

        void bind(Product product) {
            favProductName.setText(product.getTitle());

            Glide.with(itemView.getContext())
                    .load(product.getThumnail())
                    .centerCrop()
                    .into(favProductImg);

            removeFavBtn.setOnClickListener(v -> listener.onClick(product));
        }
    }
}
