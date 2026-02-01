package com.example.designpatternwithandroidlab1.allproduct;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.designpatternwithandroidlab1.R;
import com.example.designpatternwithandroidlab1.datasource.products.local.ProductsLocalDataSource;
import com.example.designpatternwithandroidlab1.datasource.products.remote.ProductNetworkResponse;
import com.example.designpatternwithandroidlab1.datasource.products.remote.ProductRemoteDataSource;
import com.example.designpatternwithandroidlab1.models.Product;
import com.example.designpatternwithandroidlab1.network.ProductService;

import java.util.List;

public class allProducts extends AppCompatActivity  implements OnProductClick{
    RecyclerView allRecyclerView;
    ProductAdapter adapter;
    // we change the network to product service
    ProductService productService;
    ProgressBar loadingProgressBar;
    TextView errorTextView;
    ProductRemoteDataSource productRemoteDataSource;
    ProductsLocalDataSource productsLocalDataSource;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_products);
        allRecyclerView = findViewById(R.id.rv_movies);
        loadingProgressBar = findViewById(R.id.progress_circular);
        errorTextView = findViewById(R.id.tv_error);

        productsLocalDataSource =
                new ProductsLocalDataSource(getApplicationContext());
        adapter = new ProductAdapter(this);
        allRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );
        allRecyclerView.setAdapter(adapter);
        productRemoteDataSource = new ProductRemoteDataSource();
        productRemoteDataSource.getProducts(new ProductNetworkResponse() {
            @Override
            public void onSuccess(List<Product> product) {
                loadingProgressBar.setVisibility(GONE);
                errorTextView.setVisibility(GONE);
                allRecyclerView.setVisibility(VISIBLE);
                adapter.setProductList(product);
            }

            @Override
            public void noInternet() {
                loadingProgressBar.setVisibility(GONE);
                errorTextView.setVisibility(VISIBLE);
                errorTextView.setText("No Internet connection");
            }

            @Override
            public void onFailure(String errorMessage) {
                loadingProgressBar.setVisibility(GONE);
                errorTextView.setVisibility(VISIBLE);
                errorTextView.setText("Something went wrong");
            }
        });
    }

    @Override
    public void addToFav(Product product) {
        productsLocalDataSource.inserProduct(product);
        Toast.makeText(this,
                "Product added to favorite",
                LENGTH_SHORT).show();
    }


}