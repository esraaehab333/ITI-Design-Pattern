package com.example.designpatternwithandroidlab1.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.designpatternwithandroidlab1.datasource.products.local.ProductDao;
import com.example.designpatternwithandroidlab1.models.Product;

@Database(entities = {Product.class}, version = 1)

public abstract class AppDatabase extends RoomDatabase {
    public abstract ProductDao productDao() ;
    private static AppDatabase INSTANCE;
    public static AppDatabase getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context , AppDatabase.class , "productsDB").allowMainThreadQueries().build();
        }
        return INSTANCE;
    }
}
