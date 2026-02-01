package com.example.designpatternwithandroidlab1.datasource.products.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.designpatternwithandroidlab1.models.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void addToFav(Product product);
    @Delete
    void deleteFromFav(Product product);
    @Query("SELECT * FROM products")
    LiveData<List<Product>> getProducts();
}
