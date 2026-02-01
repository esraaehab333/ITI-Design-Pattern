package com.example.designpatternwithandroidlab2.data.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Network {
    private ProductService productService;
    private Retrofit retrofit;
    public static String baseUrl ="https://dummyjson.com/";
    public Network(){
        // taking object from retrofit
        // don not forget the gson to parsing from json to object
         retrofit = new Retrofit.Builder()
                 .baseUrl(baseUrl)
                 .addConverterFactory(GsonConverterFactory.create())
                 .build();
    }
    // before this not forget to write your function
    // getProductServices
    public ProductService getProductService(){
        // here we check if the product service is null or not
        // it is a single tone to avoid one more network class
        if(productService== null){
            // if it is null use the create from retrofit to create it
            productService = retrofit.create(ProductService.class);
        }
        return productService;
    }

}
