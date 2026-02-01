package com.example.designpatternwithandroidlab2.data.products.datasource.remote;

import com.example.designpatternwithandroidlab2.data.products.models.Product;

import java.util.List;

public interface ProductNetworkResponse {
    void onSuccess(List<Product> product);
    void noInternet();
    void onFailure(String errorMessage);
}
