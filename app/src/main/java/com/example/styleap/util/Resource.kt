package com.example.styleap.util

/**
 * A generic class that holds a value with its loading status.
 * @param <T> Type of the resource.
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
} 