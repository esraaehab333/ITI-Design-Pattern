package com.example.designpatternwithandroidlab1.datasource;

import com.example.designpatternwithandroidlab1.models.Product;

import java.util.List;

public interface ProductNetworkResponse {
    void onSuccess(List<Product> product);
    void noInternet();
    void onFailure(String errorMessage);
}
