package com.example.test.data.remote

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val TAG = "VNPT-Network"
    private val fakeServer = FakeApiServer().apply { start() }

    val api: InventoryApi by lazy {
        Log.i(TAG, "Creating Retrofit client with base URL ${fakeServer.baseUrl}")
        Retrofit.Builder()
            .baseUrl(fakeServer.baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InventoryApi::class.java)
    }

    fun close() = fakeServer.stop()
}
