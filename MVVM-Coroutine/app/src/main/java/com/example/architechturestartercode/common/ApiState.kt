package com.example.architechturestartercode.common

sealed class ApiState<out T> {
    object Loading : ApiState<Nothing>()
    data class Success<T>(val data: T) : ApiState<T>()
    data class Failure(val msg: Throwable) : ApiState<Nothing>()
}