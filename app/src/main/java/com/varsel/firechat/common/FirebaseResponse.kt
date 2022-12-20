package com.varsel.firechat.common

sealed class FirebaseResponse<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T?) : FirebaseResponse<T>(data)
    class Failure<T>(message: String, data: T? = null) : FirebaseResponse<T>(data, message)
    class Loading<T>(data: T?) : FirebaseResponse<T>(data)
}