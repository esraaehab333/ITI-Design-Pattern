package com.example.architechturestartercode.data.movie.datasource.remote

import com.example.architechturestartercode.common.ApiState
import com.example.architechturestartercode.data.movie.model.Movie
import com.example.architechturestartercode.data.movie.model.MovieResponse
import com.example.architechturestartercode.data.network.Network
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class MoviesRemoteDataSource {
    private val moviesService: MoviesService = Network.moviesService

    suspend fun getAllMovies(): ApiState<List<Movie>> {
        return try {
            val response = moviesService.getMovies()

            if (response.isSuccessful) {
                val movies = response.body()?.results
                    ?: return ApiState.Failure(IllegalStateException("Empty response body"))
                ApiState.Success(movies)
            } else {
                ApiState.Failure(RuntimeException("Server error: ${response.code()}"))
            }
        } catch (e: IOException) {
            ApiState.Failure(IOException("Network failure, please try again", e))
        } catch (e: Exception) {
            ApiState.Failure(RuntimeException("Conversion issue! Big problems :(", e))
        }
    }
}

