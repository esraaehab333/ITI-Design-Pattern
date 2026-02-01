package com.example.designpatternwithandroidlab2.presentation.allproduct.presenter;

import android.app.Application;

import com.example.designpatternwithandroidlab2.data.products.ProductRepository;
import com.example.designpatternwithandroidlab2.presentation.allproduct.view.AllProductView;
import com.example.designpatternwithandroidlab2.data.products.datasource.local.ProductsLocalDataSource;
import com.example.designpatternwithandroidlab2.data.products.datasource.remote.ProductNetworkResponse;
import com.example.designpatternwithandroidlab2.data.products.datasource.remote.ProductRemoteDataSource;
import com.example.designpatternwithandroidlab2.data.products.models.Product;

import java.util.List;

public class AllProductPresenterImp implements AllProductPresenter{
    ProductRepository productRepository;
    AllProductView allProductView;

    public AllProductPresenterImp (Application application, AllProductView allProductView) {
        this.productRepository = new ProductRepository(application);
        this.allProductView = allProductView;
    }
    public void getAllProducts(){
        allProductView.showLoading();
        productRepository.getAllProducts(new ProductNetworkResponse() {
            @Override
            public void onSuccess(List<Product> product) {
               allProductView.hideLeading();
               allProductView.showProducts(product);
            }
            @Override
            public void noInternet() {
               allProductView.hideLeading();
               allProductView.showError("No Internet Connection");
            }
            @Override
            public void onFailure(String errorMessage) {
                allProductView.hideLeading();
                allProductView.showError(errorMessage);
            }
        });
    }

    @Override
    public void onProductClicked(Product product) {

    }

    public void addToFav(Product product){
        productRepository.addToFav(product);
    }
}
