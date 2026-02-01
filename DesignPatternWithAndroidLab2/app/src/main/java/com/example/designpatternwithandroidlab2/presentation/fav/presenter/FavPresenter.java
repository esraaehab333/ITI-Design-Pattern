package com.example.designpatternwithandroidlab2.presentation.fav.presenter;

import androidx.lifecycle.LiveData;

import com.example.designpatternwithandroidlab2.data.products.models.Product;

import java.util.List;

public interface FavPresenter {
    LiveData<List<Product>> getFavProducts();
    void deleteFromFav(Product product);
}
