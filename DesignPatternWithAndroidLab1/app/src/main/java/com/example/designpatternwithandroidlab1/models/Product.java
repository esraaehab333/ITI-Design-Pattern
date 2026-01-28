package com.example.designpatternwithandroidlab1.models;

import com.example.designpatternwithandroidlab1.network.Network;
import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("id")
    private int id;
    @SerializedName("thumbnail")
    private String thumnail;
    @SerializedName("brand")
    private String brand;
    @SerializedName("price")
    private double price;
    @SerializedName("title")
    private String title;
    public String getThumnail() {
        return thumnail;
    }
    public void setThumnail(String thumnail) {
        this.thumnail = thumnail;
    }
    public int getId() {
        return id;
    }

    public Product(int id, String thumnail, String brand, double price, String title) {
        this.id = id;
        this.thumnail = thumnail;
        this.brand = brand;
        this.price = price;
        this.title = title;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getBrand() {
        return brand;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
