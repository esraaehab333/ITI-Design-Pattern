package com.example.designpatternwithandroidlab1.network;

import com.example.designpatternwithandroidlab1.models.Product;
import com.example.designpatternwithandroidlab1.models.ProductResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ProductService {
    @GET("products")
    Call<ProductResponse> getProducts();
}
