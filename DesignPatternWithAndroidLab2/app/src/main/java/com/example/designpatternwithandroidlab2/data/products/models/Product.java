package com.example.designpatternwithandroidlab2.data.products.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
@Entity(tableName = "products")
public class Product {
    @SerializedName("id")
    @PrimaryKey
    private int id;
    @SerializedName("thumbnail")
    @ColumnInfo(name="thumbnail")
    private String thumnail;
    @SerializedName("brand")
    @ColumnInfo(name="brand")
    private String brand;
    @SerializedName("price")
    @ColumnInfo(name="price")
    private double price;
    @SerializedName("title")
    @ColumnInfo(name="title")
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
