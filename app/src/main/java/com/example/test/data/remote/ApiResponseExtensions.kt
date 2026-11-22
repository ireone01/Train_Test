package com.example.test.data.remote

import com.google.gson.Gson
import retrofit2.Response

class ApiCallException(message: String) : Exception(message)

private val gson = Gson()

fun <T : Any> Response<T>.bodyOrThrow(): T {
    if (isSuccessful) return body() ?: throw ApiCallException("API trả về response rỗng")
    val error = errorBody()?.string()?.let {
        runCatching { gson.fromJson(it, ApiErrorDto::class.java)?.message }.getOrNull()
    }
    throw ApiCallException(error ?: "API lỗi HTTP ${code()}")
}
