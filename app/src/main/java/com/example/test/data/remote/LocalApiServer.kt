package com.example.test.data.remote

interface LocalApiServer {
    val baseUrl: String
    fun close()
}
