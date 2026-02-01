package com.example.designpatternwithandroidlab2.presentation.allproduct.view;

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

import com.example.designpatternwithandroidlab2.R;
import com.example.designpatternwithandroidlab2.presentation.allproduct.presenter.AllProductPresenter;
import com.example.designpatternwithandroidlab2.presentation.allproduct.presenter.AllProductPresenterImp;
import com.example.designpatternwithandroidlab2.data.products.models.Product;
import com.example.designpatternwithandroidlab2.data.network.ProductService;

import java.util.List;

public class allProducts extends AppCompatActivity  implements OnProductClick,AllProductView{
    @Override
    public void showLoading() {
        loadingProgressBar.setVisibility(VISIBLE);
    }

    @Override
    public void hideLeading() {
        loadingProgressBar.setVisibility(GONE);

    }

    @Override
    public void showProducts(List<Product> productListduct) {
        adapter.setProductList(productListduct);
    }

    @Override
    public void showError(String errorMessage) {
        errorTextView.setVisibility(VISIBLE);
        errorTextView.setText(errorMessage);
    }

    RecyclerView allRecyclerView;
    ProductAdapter adapter;
    // we change the network to product service
    ProductService productService;
    ProgressBar loadingProgressBar;
    TextView errorTextView;
    AllProductPresenterImp presenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_products);
        allRecyclerView = findViewById(R.id.rv_movies);
        loadingProgressBar = findViewById(R.id.progress_circular);
        presenter = new AllProductPresenterImp(getApplication(), this);
        errorTextView = findViewById(R.id.tv_error);
        presenter.getAllProducts();
        adapter = new ProductAdapter(this);
        allRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        allRecyclerView.setAdapter(adapter);
    }

    @Override
    public void addToFav(Product product) {
        presenter.addToFav(product);
        Toast.makeText(this,
                "Product added to favorite",
                LENGTH_SHORT).show();
    }



}