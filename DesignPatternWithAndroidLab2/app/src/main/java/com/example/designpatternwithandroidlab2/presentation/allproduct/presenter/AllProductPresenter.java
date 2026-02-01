package com.example.designpatternwithandroidlab2.presentation.allproduct.presenter;

import com.example.designpatternwithandroidlab2.data.products.models.Product;

public interface AllProductPresenter {
    void getAllProducts();
    void onProductClicked(Product product);
}
