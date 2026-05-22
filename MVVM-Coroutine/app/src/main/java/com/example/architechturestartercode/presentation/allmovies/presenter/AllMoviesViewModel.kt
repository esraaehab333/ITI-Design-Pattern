package com.example.architechturestartercode.presentation.allmovies.presenter

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.architechturestartercode.common.ApiState
import com.example.architechturestartercode.data.movie.MoviesRepository
import com.example.architechturestartercode.data.movie.model.Movie
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AllMoviesViewModel(
    context: Context
) : ViewModel() {

    private val moviesRepository = MoviesRepository(context.applicationContext)

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _allMovies = MutableStateFlow<List<Movie>>(emptyList())
    val allMovies: StateFlow<List<Movie>> = _allMovies

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage

    private val _addToFavSuccess = MutableSharedFlow<String>()
    val addToFavSuccess: SharedFlow<String> = _addToFavSuccess

    init {
        getAllMovies()
    }

    fun getAllMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = moviesRepository.getAllMovies()
            when(result){
                is ApiState.Success -> {
                    _allMovies.value = result.data
                    _errorMessage.emit("")
                    _addToFavSuccess.emit("Added Successfully")
                }
                is ApiState.Failure -> {
                    _errorMessage.emit(result.msg.message.toString())
                    _addToFavSuccess.emit("Added Failed")
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun addToFav(movie: Movie) {
        viewModelScope.launch {
            moviesRepository.insertMovieToFav(movie)
            _addToFavSuccess.emit("success add to fav")
        }
    }

}


class AllMoviesViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AllMoviesViewModel(context.applicationContext) as T
    }
}
