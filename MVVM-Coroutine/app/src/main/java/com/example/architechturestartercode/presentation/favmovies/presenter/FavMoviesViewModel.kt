package com.example.architechturestartercode.presentation.favmovies.presenter

import android.content.Context
import androidx.collection.MutableLongList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.architechturestartercode.data.movie.MoviesRepository
import com.example.architechturestartercode.data.movie.model.Movie
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavMoviesViewModel(
    context: Context
) : ViewModel() {

    private val moviesRepository = MoviesRepository(context.applicationContext)

    private val _deleteSuccess = MutableSharedFlow<String>()
    val deleteSuccess: SharedFlow<String> = _deleteSuccess

    val favMovies: StateFlow<List<Movie>> =
        moviesRepository.getAllFavMovies().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )

    fun deleteFavMovie(movie: Movie) {
        viewModelScope.launch {
            moviesRepository.deleteMovieFromFav(movie)
            _deleteSuccess.emit("Deleted from favorites")
        }
    }
}

class FavMoviesViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavMoviesViewModel(context.applicationContext) as T
    }
}