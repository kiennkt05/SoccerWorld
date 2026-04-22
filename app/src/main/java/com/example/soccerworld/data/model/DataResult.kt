package com.example.soccerworld.data.model

sealed class DataResult<out T> {
    data class Success<T>(val data: T, val fromCache: Boolean = false) : DataResult<T>()
    data class Error(val type: ErrorType, val message: String? = null) : DataResult<Nothing>()
    object Loading : DataResult<Nothing>()
}

enum class ErrorType {
    NETWORK,
    RATE_LIMITED,
    NOT_FOUND,
    ENRICHMENT_FAIL,
    MEDIA_FAIL,
    UNKNOWN
}
