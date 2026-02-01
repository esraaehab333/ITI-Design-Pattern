package com.example.designpatternwithandroidlab2.presentation.allproduct.view;

import com.example.designpatternwithandroidlab2.data.products.models.Product;

import java.util.List;

public interface AllProductView {
    //why we create this to create a function dealing with the ui
    void showLoading();
    void hideLeading();
    void showProducts(List<Product> productListduct);
    void showError(String errorMessage);
}
