package com.example.designpatternwithandroidlab2.data.products;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.designpatternwithandroidlab2.data.products.datasource.local.ProductsLocalDataSource;
import com.example.designpatternwithandroidlab2.data.products.datasource.remote.ProductNetworkResponse;
import com.example.designpatternwithandroidlab2.data.products.datasource.remote.ProductRemoteDataSource;
import com.example.designpatternwithandroidlab2.data.products.models.Product;

import java.util.List;

public class ProductRepository {
    ProductsLocalDataSource productsLocalDataSource;
    ProductRemoteDataSource productRemoteDataSource;

    public ProductRepository(Application application) {
        this.productsLocalDataSource = new ProductsLocalDataSource(application);
        this.productRemoteDataSource = new ProductRemoteDataSource();
    }

    public void getAllProducts(ProductNetworkResponse response){
        productRemoteDataSource.getProducts(response);

    }
    public void addToFav(Product product){
        productsLocalDataSource.inserProduct(product);

    }
    public void deleteProduct(Product product){
        productsLocalDataSource.deleteProduct(product);

    }
    public LiveData<List<Product>> getFavProducts(){
        return productsLocalDataSource.getProducts();
    }
}
