package com.example.designpatternwithandroidlab2.presentation.fav.presenter;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.designpatternwithandroidlab2.data.products.datasource.local.ProductsLocalDataSource;
import com.example.designpatternwithandroidlab2.presentation.fav.view.FavView;
import com.example.designpatternwithandroidlab2.data.products.models.Product;

import java.util.List;

public class FavPresenterImp implements FavPresenter{
    ProductsLocalDataSource productsLocalDataSource;
    FavView favView;
    public FavPresenterImp(Context context, FavView favView){
        this.productsLocalDataSource = new ProductsLocalDataSource(context);
        this.favView = favView;
    }
    @Override
    public LiveData<List<Product>> getFavProducts() {
        return productsLocalDataSource.getProducts();
    }

    @Override
    public void deleteFromFav(Product product) {
        try {
            productsLocalDataSource.deleteProduct(product);
            favView.onProductDeleted();
        }catch (Exception e){

        }

    }
}
