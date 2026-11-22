package com.example.test.data.remote

import android.content.Context
import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val TAG = "VNPT-Network"

    @Volatile
    private var localApiServer: LocalApiServer? = null

    @Volatile
    private var apiInstance: InventoryApi? = null

    val api: InventoryApi
        get() = checkNotNull(apiInstance) {
            "NetworkModule.initialize(context) must be called before requesting the API"
        }

    fun initialize(context: Context) {
        if (apiInstance != null) return
        synchronized(this) {
            if (apiInstance != null) return
            val server = LocalApiServerFactory.create(context.applicationContext)
            localApiServer = server
            Log.i(TAG, "Creating Retrofit client with base URL ${server.baseUrl}")
            apiInstance = Retrofit.Builder()
                .baseUrl(server.baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(InventoryApi::class.java)
        }
    }

    fun close() {
        localApiServer?.close()
        localApiServer = null
        apiInstance = null
    }
}
