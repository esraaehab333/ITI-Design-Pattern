package com.example.designpatternwithandroidlab1.allproduct;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.designpatternwithandroidlab1.R;
import com.example.designpatternwithandroidlab1.datasource.ProductNetworkResponse;
import com.example.designpatternwithandroidlab1.datasource.ProductRemoteDataSource;
import com.example.designpatternwithandroidlab1.models.Product;
import com.example.designpatternwithandroidlab1.network.ProductService;

import java.util.List;

public class allProducts extends AppCompatActivity {
    RecyclerView allRecyclerView;
    ProductAdapter adapter;
    // we change the network to product service
    ProductService productService;
    ProgressBar loadingProgressBar;
    TextView errorTextView;
    ProductRemoteDataSource productRemoteDataSource;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_products);
        productRemoteDataSource = new ProductRemoteDataSource();
        allRecyclerView = findViewById(R.id.rv_movies);
        loadingProgressBar= findViewById(R.id.progress_circular);
        errorTextView = findViewById(R.id.tv_error);
        productRemoteDataSource.getProducts(
                new ProductNetworkResponse() {
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
                }
        );
       adapter = new ProductAdapter();
       allRecyclerView.setAdapter(adapter);
       // productService= new Network().getProductService();

    }
}