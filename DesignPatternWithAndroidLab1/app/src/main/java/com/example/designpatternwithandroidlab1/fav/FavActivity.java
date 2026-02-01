package com.example.designpatternwithandroidlab1.fav;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.designpatternwithandroidlab1.R;
import com.example.designpatternwithandroidlab1.datasource.products.local.ProductsLocalDataSource;
import com.example.designpatternwithandroidlab1.models.Product;

import java.util.List;

public class FavActivity extends AppCompatActivity implements OnFavoriteClickListener {

    RecyclerView favRecyclerView;
    FavoriteAdapter favoriteAdapter;
    ProductsLocalDataSource productsLocalDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fav_movies);

        favRecyclerView = findViewById(R.id.rvFavMovies);
        favoriteAdapter = new FavoriteAdapter(this);

        favRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );
        favRecyclerView.setAdapter(favoriteAdapter);

        productsLocalDataSource =
                new ProductsLocalDataSource(getApplicationContext());

        productsLocalDataSource.getProducts().observe(
                this,
                products -> favoriteAdapter.setList(products)
        );
    }

    @Override
    public void onClick(Product product) {
        productsLocalDataSource.deleteProduct(product);
    }
}
