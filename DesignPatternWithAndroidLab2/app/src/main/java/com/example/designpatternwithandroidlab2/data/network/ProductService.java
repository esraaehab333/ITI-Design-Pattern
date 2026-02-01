package com.example.designpatternwithandroidlab2.data.network;

import com.example.designpatternwithandroidlab2.data.products.models.ProductResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ProductService {
    @GET("products")
    Call<ProductResponse> getProducts();
}
