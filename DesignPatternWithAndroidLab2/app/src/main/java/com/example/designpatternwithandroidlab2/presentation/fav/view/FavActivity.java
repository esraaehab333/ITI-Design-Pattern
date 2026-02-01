package com.example.designpatternwithandroidlab2.presentation.fav.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.designpatternwithandroidlab2.R;
import com.example.designpatternwithandroidlab2.presentation.fav.presenter.FavPresenter;
import com.example.designpatternwithandroidlab2.presentation.fav.presenter.FavPresenterImp;
import com.example.designpatternwithandroidlab2.data.products.models.Product;

import java.util.List;

public class FavActivity extends AppCompatActivity implements OnFavoriteClickListener , FavView {

    RecyclerView favRecyclerView;
    FavoriteAdapter favoriteAdapter;
    FavPresenter favPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fav_movies);

        favRecyclerView = findViewById(R.id.rvFavMovies);
        favoriteAdapter = new FavoriteAdapter(this);
        favPresenter = new FavPresenterImp(getApplicationContext(), this);
        favPresenter.getFavProducts().observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> products) {
                Log.d("FAV_ACTIVITY", "Products size: " + products.size());
                favoriteAdapter.setList(products);
            }
        });
        favRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );
        favRecyclerView.setAdapter(favoriteAdapter);

    }

    @Override
    public void onClick(Product product) {
        favPresenter.deleteFromFav(product);
    }

    @Override
    public void onProductDeleted() {
        Toast.makeText(this,"Product was deleted", Toast.LENGTH_SHORT).show();
    }
}
