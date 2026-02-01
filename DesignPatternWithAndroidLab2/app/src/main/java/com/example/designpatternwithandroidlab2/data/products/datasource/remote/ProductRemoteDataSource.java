package com.example.designpatternwithandroidlab2.data.products.datasource.remote;

import com.example.designpatternwithandroidlab2.data.products.models.Product;
import com.example.designpatternwithandroidlab2.data.products.models.ProductResponse;
import com.example.designpatternwithandroidlab2.data.network.Network;
import com.example.designpatternwithandroidlab2.data.network.ProductService;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRemoteDataSource {
    private ProductService productService;
    public ProductRemoteDataSource(){
        this.productService = new Network().getProductService();
    }
    public void getProducts(ProductNetworkResponse productNetworkResponse){
        productService.getProducts().enqueue(
                new Callback<ProductResponse>() {
                    @Override
                    public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                        if(response.code() == 200){
                            ProductResponse productResponse = response.body();
                            List<Product> product = productResponse.getProducts();
                            // this is the call back to separation
                            productNetworkResponse.onSuccess(product);
                        }
                        else {
                            productNetworkResponse.onFailure("Error server error");
                        }
                    }

                    @Override
                    public void onFailure(Call<ProductResponse> call, Throwable t) {
                        if(t instanceof IOException){
                            productNetworkResponse.noInternet();
                        }
                        else{
                           productNetworkResponse.onFailure("Convertion error!");
                        }
                    }
                }
        );
    }
}
