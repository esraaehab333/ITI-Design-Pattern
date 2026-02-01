package com.example.designpatternwithandroidlab1.datasource.products.local;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.designpatternwithandroidlab1.db.AppDatabase;
import com.example.designpatternwithandroidlab1.models.Product;

import java.util.List;

public class ProductsLocalDataSource {
    private ProductDao productsDao;
    public ProductsLocalDataSource(Context context){
        this.productsDao = AppDatabase.getInstance(context).productDao();
    }
    public LiveData<List<Product>> getProducts(){
        return productsDao.getProducts();
    }
    public void inserProduct(Product product){
        new Thread(new Runnable() {
            @Override
            public void run() {
                productsDao.addToFav(product);
            }
        }).start();
    }
    public void deleteProduct(Product product){
        new Thread(new Runnable() {
            @Override
            public void run() {
                productsDao.deleteFromFav(product);
            }
        }).start();
    }
}
